package com.example.halligalli;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
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
    private ImageView playerCardView, opponentCardView, playerDeckView, opponentDeckView, gameBell;
    private TextView playerNameView;

    private Handler gameHandler = new Handler();
    private int turnTimeLeft = 6; // 9초 제한시간
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

    // 난이도 관련 변수 초기화
    private int[] aiCardPlayDelayRange = {5000, 6000};
    private int[] aiReactionTimeRange = {1000, 3000};
    private int aiMistakeProbability = 50;

    // 뒤로가기 버튼 비활성화
    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            // 뒤로가기 버튼 비활성화
        }
    };

    private void gameBellPressed(boolean bellPresserIsPlayer) {
        String bellPresser = bellPresserIsPlayer ? "플레이어" : "상대방";

        if (isHalliGalliRuleMet()) {
            // "할리갈리" 성공
            Toast.makeText(this, bellPresser + " 할리갈리!!", Toast.LENGTH_SHORT).show();

            // 종을 누른 사람이 모든 낸 카드 가져감
            if (bellPresserIsPlayer) {
                playerDeck.addAll(playerPlayedCards);
                playerDeck.addAll(opponentPlayedCards);
            } else {
                opponentDeck.addAll(playerPlayedCards);
                opponentDeck.addAll(opponentPlayedCards);
            }

            // 낸 카드 초기화 및 UI 업데이트
            resetPlayedCards();
            updateCardCounts();

            // 게임 종료 조건 확인
            checkForGameEnd();
        } else {
            // "할리갈리" 실패
            Toast.makeText(this, bellPresser + " 땡!", Toast.LENGTH_SHORT).show();

            // 종을 잘못 누른 사람에게 페널티
            if (bellPresserIsPlayer) {
                if (!playerDeck.isEmpty()) {
                    String penaltyCard = playerDeck.remove(0);
                    opponentDeck.add(penaltyCard);
                }
            } else {
                if (!opponentDeck.isEmpty()) {
                    String penaltyCard = opponentDeck.remove(0);
                    playerDeck.add(penaltyCard);
                }
            }

            // 0인 덱에 대해 즉시 종료 처리
            if (playerDeck.isEmpty() || opponentDeck.isEmpty()) {
                checkForGameEnd();
            }
        }
    }
    // 난이도 관련 설정
    private void setDifficulty(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "쉬움":
                aiCardPlayDelayRange = new int[]{5000, 6000}; // 5~6초 랜덤
                aiReactionTimeRange = new int[]{1000, 3000}; // 1~3초 랜덤
                aiMistakeProbability = 50; // 50%
                break;
            case "보통":
                aiCardPlayDelayRange = new int[]{3000, 5000}; // 3~5초 랜덤
                aiReactionTimeRange = new int[]{500, 2000}; // 0.5~2초 랜덤
                aiMistakeProbability = 20; // 20%
                break;
            case "어려움":
                aiCardPlayDelayRange = new int[]{1000, 4000}; // 1~4초 랜덤
                aiReactionTimeRange = new int[]{0, 500}; // 0~0.5초 랜덤
                aiMistakeProbability = 10; // 10%
                break;
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback); // 뒤로가기 버튼 비활성화

        // Intent로 전달된 데이터 수신
        Intent intent = getIntent();
        mode = intent.getStringExtra("MODE");
        difficulty = intent.getStringExtra("DIFFICULTY");
        setDifficulty(difficulty);

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

        gameBell.setOnClickListener(v -> gameBellPressed(true)); // true: 플레이어가 벨 누름

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
        // 상대, 플레이어 플레이 카드 이미지 초기화
        playerCardView = findViewById(R.id.playerCard);
        opponentCardView = findViewById(R.id.opponentCard);

        //상대, 플레이어 덱 카드 이미지 초지화
        playerDeckView = findViewById(R.id.playertDeck);
        opponentDeckView = findViewById(R.id.oppoentDeck);

        gameBell = findViewById(R.id.gameBell);

        playerNameView = findViewById(R.id.playerName);
        gameTimerView = findViewById(R.id.gameTimer);

        // 이름 정보 가져오기
        String playerName = getIntent().getStringExtra("PLAYER_NAME");
        playerNameView.setText(playerName);

        TextView opponentName = findViewById(R.id.opponentName);
        opponentName.setText("상대");
    }
    // 게임 로직 초기화
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

        gameHandler.removeCallbacksAndMessages(null);
        turnTimeLeft = 6;
        isCardPlayed = false;
        updateTimerView();

        gameHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (turnTimeLeft > 0) {
                    turnTimeLeft--;

                    updateTimerView();

                    gameHandler.postDelayed(this, 1000); // 1초마다 반복
                } else {
                    // 턴 종료 처리
                    if (!isCardPlayed) {
                            if (isPlayerTurn) {

                                isPlayerTurn = false;
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

            // "할리갈리" 규칙 즉시 확인
            if (isHalliGalliRuleMet()) {
                gameBellPressed(true); // 플레이어의 즉시 "할리갈리" 처리
            }

            endTurn(); // 턴 종료
        } else {
            Toast.makeText(this, "덱에 카드가 없습니다.", Toast.LENGTH_SHORT).show();
            checkForGameEnd();
        }
    }

        private void drawOpponentCard() {
        gameHandler.postDelayed(() -> {
            if (!opponentDeck.isEmpty()) {
                String card = opponentDeck.remove(0); // 덱에서 카드 하나 제거
                opponentPlayedCards.add(card); // 제거한 카드를 플레이 카드 리스트에 추가

                opponentPlayedCardCount++; // 상대방 낸 카드 수 증가
                opponentCardView.setImageResource(getCardImage(card)); // UI 업데이트

                updateCardCounts(); // 덱과 플레이 카드 수 업데이트
                isCardPlayed = true; // 카드 제출 상태 업데이트

                // 할리갈리 규칙 즉시 확인
                if (isHalliGalliRuleMet()) {
                    triggerAIReaction(true); // AI 즉시 "할리갈리" 처리
                }

                endTurn();
            } else {
                Toast.makeText(this, "상대방 덱에 카드가 없습니다.", Toast.LENGTH_SHORT).show();
                checkForGameEnd();
            }
        }, getRandomDelay(aiCardPlayDelayRange));
    }

    private void triggerAIReaction(boolean isRuleMet) {
        int reactionTime = getRandomDelay(aiReactionTimeRange);

        gameHandler.postDelayed(() -> {
            if (isRuleMet || random.nextInt(100) < aiMistakeProbability) {
                gameBellPressed(false); // false: AI가 벨 누름
            }
        }, reactionTime);
    }


    // 랜덤 시간 계산 함수
    private int getRandomDelay(int[] range) {
        return random.nextInt(range[1] - range[0] + 1) + range[0];
    }

    private void endTurn() {
        if (playerDeck.isEmpty() && opponentDeck.isEmpty()) {
            // 양쪽 덱이 모두 비었으면 게임 종료
            checkForGameEnd();
            return;
        }

        // 기존 타이머 제거
        gameHandler.removeCallbacksAndMessages(null);

        // 턴 교체
        if (!playerDeck.isEmpty() && !opponentDeck.isEmpty()) {
            isPlayerTurn = !isPlayerTurn;
        } else if (playerDeck.isEmpty()) {
            isPlayerTurn = false; // 상대방만 카드 낼 수 있음
        } else if (opponentDeck.isEmpty()) {
            isPlayerTurn = true; // 플레이어만 카드 낼 수 있음
        }

        isCardPlayed = false; // 카드 제출 상태 초기화

        // 턴 전환 애니메이션 및 다음 동작
        showTurnTransitionAnimation();

        if (!isPlayerTurn && !opponentDeck.isEmpty()) {
            // AI 턴: 애니메이션 후 카드 제출
            gameHandler.postDelayed(this::drawOpponentCard, 2000);
        } else if (isPlayerTurn && !playerDeck.isEmpty()) {
            // 플레이어 턴: 애니메이션 후 타이머 시작
            gameHandler.postDelayed(this::startTurnTimer, 2000);
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

        // 현재 플레이된 카드만 검사
        List<String> currentPlayedCards = new ArrayList<>();
        if (!playerPlayedCards.isEmpty()) {
            currentPlayedCards.add(playerPlayedCards.get(playerPlayedCards.size() - 1)); // 플레이어 마지막 카드
        }
        if (!opponentPlayedCards.isEmpty()) {
            currentPlayedCards.add(opponentPlayedCards.get(opponentPlayedCards.size() - 1)); // 상대방 마지막 카드
        }

        // 과일 개수 계산
        for (String card : currentPlayedCards) {
            String[] parts = card.split("_");
            if (parts.length == 2) { // "과일_숫자" 형식 확인
                String fruit = parts[0];
                int count = Integer.parseInt(parts[1]);
                fruitCounts.put(fruit, fruitCounts.getOrDefault(fruit, 0) + count);
            } else {
                Log.e(TAG, "Invalid card format: " + card);
            }
        }

        // 같은 과일의 개수가 정확히 5인지 확인
        for (Map.Entry<String, Integer> entry : fruitCounts.entrySet()) {
            Log.d(TAG, "Fruit: " + entry.getKey() + ", Count: " + entry.getValue());
            if (entry.getValue() == 5) {
                return true; // 할리갈리 조건 충족
            }
        }

        return false; // 조건 미충족
    }

    private void resetPlayedCards() {
        // 양쪽 플레이된 카드 초기화
        playerPlayedCards.clear();
        opponentPlayedCards.clear();

        // UI에서 플레이된 카드 수 0으로 표시
        playerPlayedCardCount = 0;
        opponentPlayedCardCount = 0;
        playerCardCountView.setText("0");
        opponentCardCountView.setText("0");

        // 카드 이미지 초기화
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

    private void showTurnTransitionAnimation() {
        TextView turnIndicator = findViewById(R.id.turnIndicator); // XML에서 추가 필요
        turnIndicator.setText(isPlayerTurn ? "플레이어의 턴!" : "상대방의 턴!");
        turnIndicator.setAlpha(0f);
        turnIndicator.setVisibility(View.VISIBLE);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(turnIndicator, "alpha", 0f, 1f);
        fadeIn.setDuration(500);

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(turnIndicator, "alpha", 1f, 0f);
        fadeOut.setDuration(500);
        fadeOut.setStartDelay(1000);

        fadeIn.start();
        fadeOut.start();

        // 애니메이션 종료 후 숨김 처리
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                turnIndicator.setVisibility(View.GONE);
            }
        });
    }

    private void checkForGameEnd() {
        if (playerDeck.isEmpty() && opponentDeck.isEmpty()) {
            Toast.makeText(this, "무승부!", Toast.LENGTH_LONG).show();
            navigateToLobby();
        } else if (playerDeck.isEmpty()) {
            Toast.makeText(this, "상대방이 승리했습니다!", Toast.LENGTH_LONG).show();
            navigateToLobby();
        } else if (opponentDeck.isEmpty()) {
            Toast.makeText(this, "플레이어가 승리했습니다!", Toast.LENGTH_LONG).show();
            navigateToLobby();
        }
    }
}