package com.example.halligalli;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class GameActivity extends AppCompatActivity {
    private String mode; // 싱글 or 멀티
    private String difficulty; // 쉬움, 보통, 어려움
    private TextView gameTimerView, playerCardCountView, opponentCardCountView,
                        playerDeckCountView, opponentDeckCountView;
    private ImageView playerCardView, opponentCardView, playerDeckView, gameBell;
    private TextView playerNameView;

    private Handler gameHandler = new Handler();
    private int turnTimeLeft = 9; // 9초 제한시간
    private boolean isBlinking = false; // 깜박임 애니메이션 상태 확인

    private List<String> playerDeck = new ArrayList<>();
    private List<String> opponentDeck = new ArrayList<>();
    private List<String> playerPlayedCards = new ArrayList<>();
    private List<String> opponentPlayedCards = new ArrayList<>();
    private boolean isPlayerTurn = true;
    private boolean isCardPlayed = false; // 한 턴에 카드 제출 여부
    private int playerPlayedCardCount = 0;  // 플레이어가 낸 카드 수
    private int opponentPlayedCardCount = 0;  // 상대방이 낸 카드 수
    private boolean isGameRunning = true;
    private static final String TAG = "GameActivity";
    private Random random = new Random();

    // 뒤로가기 버튼 비활성화
    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            // 뒤로가기 버튼 비활성화
        }
    };

    private void gameBellPressed(boolean isPlayer) {
        if (isHalliGalliRuleMet()) {
            String winner = isPlayer ? "플레이어" : "상대방";
            Toast.makeText(this, winner + " 할리갈리!!", Toast.LENGTH_SHORT).show();

            // 승리한 사람이 양쪽이 낸 모든 카드를 가져감
            if (isPlayer) {
                playerDeck.addAll(playerPlayedCards);
                playerDeck.addAll(opponentPlayedCards);
            } else {
                opponentDeck.addAll(playerPlayedCards);
                opponentDeck.addAll(opponentPlayedCards);
            }

            // 양쪽 낸 카드 초기화
            resetPlayedCards();

            // 카드 수 업데이트
            updateCardCounts();
        } else {
            String loser = isPlayer ? "플레이어" : "상대방";
            Toast.makeText(this, loser + "떙!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        // Intent로 전달된 데이터 수신
        Intent intent = getIntent();
        mode = intent.getStringExtra("MODE");
        difficulty = intent.getStringExtra("DIFFICULTY");

        // UI 초기화
        initializeUI();
        initializeGame();
        // 덱 클릭 이벤트 처리
        playerDeckView.setOnClickListener(v -> {
            if (isPlayerTurn) {
                if (!isCardPlayed) {
                    drawPlayerCard();
                    isCardPlayed = true;
                } else {
                    Toast.makeText(this, "이미 카드를 제출했습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "상대방 턴입니다.", Toast.LENGTH_SHORT).show();
            }
        });

        gameBell.setOnClickListener(v -> {
            // 벨을 누르면 승리 판정 진행
            gameBellPressed(isPlayerTurn);
        });

        startGame();


        // 로비로 돌아가기 버튼
        Button backToLobbyButton = findViewById(R.id.backToLobbyButton);
        backToLobbyButton.setOnClickListener(v -> showExitConfirmationDialog());

    }

    private void initializeUI() {
        //낸 카드 초기화
        playerCardCountView = findViewById(R.id.playerCardCount);
        opponentCardCountView = findViewById(R.id.opponentCardCount);
        // 남은 카드 초기화
        playerDeckCountView = findViewById(R.id.playerDeckCount);
        opponentDeckCountView = findViewById(R.id.opponentDeckCount);
        // 상대, 플레이어 카드 이미지 초기화
        playerCardView = findViewById(R.id.playerCard);
        opponentCardView = findViewById(R.id.opponentCard);

        playerDeckView = findViewById(R.id.playertDeck);

        gameBell = findViewById(R.id.gameBell);

        playerNameView = findViewById(R.id.playerName);
        gameTimerView = findViewById(R.id.gameTimer);

        // 이름 정보 가져오기
        String playerName = getIntent().getStringExtra("PLAYER_NAME");
        playerNameView.setText(playerName);

        TextView opponentName = findViewById(R.id.opponentName);
        opponentName.setText("상대");
    }

    private void initializeGame() {
        List<String> deck = new ArrayList<>();

        // 카드 생성
        createCards(deck, "딸기");
        createCards(deck, "바나나");
        createCards(deck, "라임");
        createCards(deck, "자두");

        // 카드 섞기
        Collections.shuffle(deck);

        // 플레이어와 상대방에게 카드 분배
        for (int i = 0; i < deck.size(); i++) {
            if (i % 2 == 0) {
                playerDeck.add(deck.get(i));
            } else {
                opponentDeck.add(deck.get(i));
            }
        }

        updateCardCounts();
    }

    private void createCards(List<String> deck, String fruit) {
        // 1개짜리 5장
        for (int i = 0; i < 5; i++) {
            deck.add(fruit + "_1");
        }
        // 2개짜리 3장
        for (int i = 0; i < 3; i++) {
            deck.add(fruit + "_2");
        }
        // 3개짜리 3장
        for (int i = 0; i < 3; i++) {
            deck.add(fruit + "_3");
        }
        // 4개짜리 2장
        for (int i = 0; i < 2; i++) {
            deck.add(fruit + "_4");
        }
        // 5개짜리 1장
        deck.add(fruit + "_5");
    }
    private void startGame() {
        startTurnTimer();
    }

    private void startTurnTimer() {
        turnTimeLeft = 9;
        isCardPlayed = false;
        isBlinking = false; // 새로운 턴에서 깜박임 초기화
        updateTimerView();

        gameHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (turnTimeLeft > 0) {
                    turnTimeLeft--;

                    updateTimerView();

                    // 3초 이하일 때 깜박이기 시작
                    if (turnTimeLeft <= 3 && !isBlinking) {
                        triggerBlinkEffect();
                        isBlinking = true;
                    }

                    updateTimerView();
                    gameHandler.postDelayed(this, 1000); // 1초마다 반복
                } else {
                    // 턴 종료 처리
                    if (!isCardPlayed) {
                        if (isPlayerTurn) {
                            drawPlayerCard();
                        } else {
                            drawOpponentCard();
                        }
                    }
                    endTurn(); // 턴 종료
                }
            }
        }, 1000);
    }

    private void updateTimerView() {
        gameTimerView.setText(getString(R.string.timer_text, turnTimeLeft));
    }

    private void triggerBlinkEffect() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(gameTimerView, "alpha", 1f, 0f, 1f);
        animator.setDuration(300);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.start();

        gameHandler.postDelayed(animator::cancel, 3000); // 3초 후 애니메이션 종료
    }

    private void drawPlayerCard() {
        if (!playerDeck.isEmpty()) {
            // 덱에서 카드 하나 뽑기
            String card = playerDeck.remove(0);

            // 뽑은 카드 저장
            playerPlayedCards.add(card);

            // 낸 카드 수 증가
            playerPlayedCardCount++;

            // 카드 이미지 업데이트
            playerCardView.setImageResource(getCardImage(card));

            // 카드 수 업데이트
            updateCardCounts();
            isCardPlayed = true;
        } else {
            Toast.makeText(this, "덱에 카드가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    private void drawOpponentCard() {
        if (!opponentDeck.isEmpty()) {
            String card = opponentDeck.remove(0);
            opponentPlayedCards.add(card); // 상대방 카드로 바뀌야함

            opponentPlayedCardCount++;

            opponentCardView.setImageResource(getCardImage(card));
            updateCardCounts();

            isCardPlayed = true;

            if (isHalliGalliRuleMet()) {
                Toast.makeText(this, "상대방이 벨을 눌렀습니다!", Toast.LENGTH_SHORT).show();
                resetPlayedCards();
            }
            } else {
            Log.d(TAG, "Opponent deck is empty!");

        }
    }

    private void endTurn() {// 턴 자동으로 넘기는 거 구현해야함
        isPlayerTurn= !isPlayerTurn; //
        isCardPlayed = false;// 턴 교체
        turnTimeLeft = 9; // 타이머 초기화
        if (!isPlayerTurn) {
            // AI 턴에서 1초 후 카드 제출
            gameHandler.postDelayed(() -> {
                drawOpponentCard();
                endTurn();
            }, 1000);
        } else {
            startTurnTimer(); // 플레이어 턴 타이머 시작
        }
    }


    private void updateCardCounts() {
        // 플레이어와 상대방의 덱과 낸 카드 수 업데이트
        playerDeckCountView.setText(String.valueOf(playerDeck.size()));
        opponentDeckCountView.setText(String.valueOf(opponentDeck.size()));
        playerCardCountView.setText(String.valueOf(playerPlayedCardCount));
        opponentCardCountView.setText(String.valueOf(opponentPlayedCardCount));
    }
    private boolean isHalliGalliRuleMet() {
        Map<String, Integer> fruitCounts = new HashMap<>();

        // 모든 제출된 카드 확인
        List<String> allPlayedCards = new ArrayList<>();
        allPlayedCards.addAll(playerPlayedCards);
        allPlayedCards.addAll(opponentPlayedCards);

        for (String card : allPlayedCards) {
            String fruit = card.split("_")[0];
            fruitCounts.put(fruit, fruitCounts.getOrDefault(fruit, 0) + Integer.parseInt(card.split("_")[1]));
        }

        // 같은 과일의 합이 5인지 확인
        for (int count : fruitCounts.values()) {
            if (count == 5) return true;
        }

        return false;
    }

    private void resetPlayedCards() {
        playerPlayedCards.clear();
        opponentPlayedCards.clear();
        playerCardView.setImageResource(R.drawable.card_deck);
        opponentCardView.setImageResource(R.drawable.card_deck);
    }

    private int getCardImage(String cardName) {
        if (cardName.contains("딸기")) {
            if (cardName.contains("_1")) return R.drawable.strawberry1;
            if (cardName.contains("_2")) return R.drawable.strawberry2;
            if (cardName.contains("_3")) return R.drawable.strawberry3;
            if (cardName.contains("_4")) return R.drawable.strawberry4;
            if (cardName.contains("_5")) return R.drawable.strawberry5;
        } else if (cardName.contains("바나나")) {
            if (cardName.contains("_1")) return R.drawable.banana1;
            if (cardName.contains("_2")) return R.drawable.banana2;
            if (cardName.contains("_3")) return R.drawable.banana3;
            if (cardName.contains("_4")) return R.drawable.banana4;
            if (cardName.contains("_5")) return R.drawable.banana5;
        } else if (cardName.contains("라임")) {
            if (cardName.contains("_1")) return R.drawable.lime1;
            if (cardName.contains("_2")) return R.drawable.lime2;
            if (cardName.contains("_3")) return R.drawable.lime3;
            if (cardName.contains("_4")) return R.drawable.lime4;
            if (cardName.contains("_5")) return R.drawable.lime5;
        } else if (cardName.contains("자두")) {
            if (cardName.contains("_1")) return R.drawable.plum1;
            if (cardName.contains("_2")) return R.drawable.plum2;
            if (cardName.contains("_3")) return R.drawable.plum3;
            if (cardName.contains("_4")) return R.drawable.plum4;
            if (cardName.contains("_5")) return R.drawable.plum5;
        }

        return R.drawable.card_deck; // 기본 뒷면 이미지
    }
    
    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getText(R.string.game_end_title))
                .setMessage(getText(R.string.game_end_description))
                .setPositiveButton("예", (dialog, which) -> navigateToLobby())
                .setNegativeButton("아니오", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void navigateToLobby() {
        Intent intent = new Intent(GameActivity.this, LobbyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}