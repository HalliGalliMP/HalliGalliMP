package com.example.halligalli;



import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;



public class GameActivity extends AppCompatActivity {
    private String difficulty; // 쉬움, 보통, 어려움
    private TextView gameTimerView, playerCardCountView, opponentCardCountView,
            playerDeckCountView, opponentDeckCountView;
    private ImageView playerCardView, opponentCardView, playerDeckView, opponentDeckView, gameBell;
    private TextView playerNameView;

    private Handler gameHandler = new Handler();
    private Handler aiReactionHandler = new Handler();


    private Deck playerPlayedDecks = new Deck();
    private Deck opponentPlayedDecks = new Deck();

    private Deck playerDeck = new Deck();
    private Deck opponentDeck = new Deck();

    private boolean isPlayerTurn = true;
    private boolean isCardPlayed = false; // 한 턴에 카드 제출 여부

    private int playerPlayedCardCount = 0;  // 플레이어가 낸 카드 수
    private int opponentPlayedCardCount = 0;  // 상대방이 낸 카드 수

    private static final String TAG = "GameActivity";
    private Random random = new Random();

    // 난이도 관련 변수 초기화
    private int[] aiCardPlayDelayRange;
    private int[] aiReactionTimeRange;
    private int aiMistakeProbabilityTrue, aiMistakeProbabilityFalse;

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
                playerDeck.addAll(playerPlayedDecks.getCards());
                playerDeck.addAll(opponentPlayedDecks.getCards());
            } else {
                opponentDeck.addAll(playerPlayedDecks.getCards());
                opponentDeck.addAll(opponentPlayedDecks.getCards());
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
                    Card penaltyCard = playerDeck.drawCard();
                    opponentDeck.addCard(penaltyCard);
                }
            } else {
                if (!opponentDeck.isEmpty()) {
                    Card penaltyCard = opponentDeck.drawCard();
                    playerDeck.addCard(penaltyCard);
                }
            }

            // 0인 덱에 대해 즉시 종료 처리
//            if (playerDeck.isEmpty() || opponentDeck.isEmpty()) {
//                checkForGameEnd();
//            }
        }
    }
    // 난이도 관련 설정
    private void setDifficulty(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "쉬움":
                aiCardPlayDelayRange = new int[]{2500, 3500}; // 5~6초 랜덤
                aiReactionTimeRange = new int[]{1500, 2300}; // 1~3초 랜덤
                aiMistakeProbabilityTrue = 2;
                aiMistakeProbabilityFalse = 10; // 20%
                break;
            case "보통":
                aiCardPlayDelayRange = new int[]{1500, 2500}; // 3~5초 랜덤
                aiReactionTimeRange = new int[]{500, 1300}; // 0.5~2초 랜덤
                aiMistakeProbabilityTrue = 1;
                aiMistakeProbabilityFalse = 5; // 10%
                break;
            case "어려움":
                aiCardPlayDelayRange = new int[]{700, 1500}; // 1~4초 랜덤
                aiReactionTimeRange = new int[]{100, 500}; // 0~0.5초 랜덤
                aiMistakeProbabilityTrue = 0;
                aiMistakeProbabilityFalse = 2; // 3%
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
        playerDeckView = findViewById(R.id.playerDeck);
        opponentDeckView = findViewById(R.id.oppoentDeck);

        gameBell = findViewById(R.id.gameBell);


        gameTimerView = findViewById(R.id.gameTimer);

        // 이름 정보 가져오기
//        String playerName = getIntent().getStringExtra("PLAYER_NAME");
//        playerNameView.setText(playerName);

    }
    // 게임 로직 초기화
    private void initializeGame() {
        Deck fullDeck = new Deck();

        createCards(fullDeck, "딸기");
        createCards(fullDeck, "바나나");
        createCards(fullDeck, "라임");
        createCards(fullDeck, "자두");

        fullDeck.shuffle();

        while (fullDeck.size() > 0) {
            playerDeck.addCard(fullDeck.drawCard());
            if (fullDeck.size() > 0) {
                opponentDeck.addCard(fullDeck.drawCard());
            }
        }

        updateCardCounts();
    }



    private void createCards(Deck deck, String fruit) {
        for (int i = 0; i < 5; i++) deck.addCard(new Card(fruit, 1)); // 1개짜리 5장
        for (int i = 0; i < 3; i++) deck.addCard(new Card(fruit, 2)); // 2개짜리 3장
        for (int i = 0; i < 3; i++) deck.addCard(new Card(fruit, 3)); // 3개짜리 3장
        for (int i = 0; i < 2; i++) deck.addCard(new Card(fruit, 4)); // 4개짜리 2장
        deck.addCard(new Card(fruit, 5)); // 5개짜리 1장
    }



    private void drawPlayerCard() {

        aiReactionHandler.removeCallbacksAndMessages(null);

        if (!playerDeck.isEmpty()) {
            // 덱에서 카드 하나 뽑기
            Card card = playerDeck.drawCard();

            // 뽑은 카드 저장
            playerPlayedDecks.addCard(card);
            // 낸 카드 수 증가
            playerPlayedCardCount++;
            // 카드 이미지 업데이트
            playerCardView.setImageResource(card.getCardImageResource());

            // 카드 수 업데이트
            updateCardCounts();
            isCardPlayed = true;

            endTurn(); // 턴 종료
        } else {
            Toast.makeText(this, "덱에 카드가 없습니다.", Toast.LENGTH_SHORT).show();

        }
    }

    private void setCardImage(boolean img){
        if(img){
            playerDeckView.setImageResource(R.drawable.card_deck);
            opponentDeckView.setImageResource(R.drawable.card_deck_grey);
        }
        else{
            playerDeckView.setImageResource(R.drawable.card_deck_grey);
            opponentDeckView.setImageResource(R.drawable.card_deck);
        }

    }

        private void drawOpponentCard() {

        aiReactionHandler.removeCallbacksAndMessages(null);
        triggerAIReaction(isHalliGalliRuleMet());
        gameHandler.postDelayed(() -> {
            if (!opponentDeck.isEmpty()) {
                Card card = opponentDeck.drawCard(); // 덱에서 카드 하나 제거
                opponentPlayedDecks.addCard(card); // 제거한 카드를 플레이 카드 리스트에 추가

                opponentPlayedCardCount++; // 상대방 낸 카드 수 증가
                opponentCardView.setImageResource(card.getCardImageResource());

                updateCardCounts(); // 덱과 플레이 카드 수 업데이트
                isCardPlayed = true; // 카드 제출 상태 업데이트

                aiReactionHandler.removeCallbacksAndMessages(null);
                triggerAIReaction(isHalliGalliRuleMet());

                endTurn();
            } else {
                Toast.makeText(this, "상대방 덱에 카드가 없습니다.", Toast.LENGTH_SHORT).show();

            }
        }, getRandomDelay(aiCardPlayDelayRange));
    }

    private void triggerAIReaction(boolean isRuleMet) {
        System.out.println("AI Reaction Triggered");
        int reactionTime = getRandomDelay(aiReactionTimeRange);

        aiReactionHandler.postDelayed(() -> {
            if (isRuleMet) {
                if (random.nextInt(100) > aiMistakeProbabilityTrue) {
                    gameBellPressed(false);
                    System.out.println("AI Pressed (Rule Met)");
                }
            } else {
                // Rule이 만족되지 않은 경우 aiMistakeProbabilityFalse 확률로 벨 누름
                if (random.nextInt(100) < aiMistakeProbabilityFalse) {
                    gameBellPressed(false);
                    System.out.println("AI Pressed (Mistake)");
                }
            }
        }, reactionTime);
    }



    // 랜덤 시간 계산 함수
    private int getRandomDelay(int[] range) {
        return random.nextInt(range[1] - range[0] + 1) + range[0];
    }

    private void endTurn() {


        // 기존 타이머 제거
        gameHandler.removeCallbacksAndMessages(null);

        // 턴 교체
        if (!playerDeck.isEmpty() && !opponentDeck.isEmpty()) {
            isPlayerTurn = !isPlayerTurn;
            setCardImage(isPlayerTurn);
            if(!isPlayerTurn) {
                drawOpponentCard();
            }

        }
        else if (playerDeck.isEmpty()) {
            isPlayerTurn = false; // 상대방만 카드 낼 수 있음
            drawOpponentCard();

        } else if (opponentDeck.isEmpty()) {
            isPlayerTurn = true; // 플레이어만 카드 낼 수 있음

        }

        isCardPlayed = false; // 카드 제출 상태 초기화
    }


    private void updateCardCounts() {
        // 플레이어와 상대방의 덱과 낸 카드 수 업데이트
        playerDeckCountView.setText(String.valueOf(playerDeck.size()));
        opponentDeckCountView.setText(String.valueOf(opponentDeck.size()));
        playerCardCountView.setText(String.valueOf(playerPlayedCardCount));
        opponentCardCountView.setText(String.valueOf(opponentPlayedCardCount));
    }


    private boolean isHalliGalliRuleMet() {
        if (playerPlayedDecks.isEmpty() || opponentPlayedDecks.isEmpty()) {
            return false; // 카드가 없는 경우 false 반환
        }

        // 각 플레이어의 마지막 카드 가져오기
        Card lastPlayerCard = playerPlayedDecks.getLastCards();
        Card lastOpponentCard = opponentPlayedDecks.getLastCards();

        // 두 카드의 과일이 같을 때 합산 검사
        if (lastPlayerCard.getFruit().equals(lastOpponentCard.getFruit())) {
            int totalCount = lastPlayerCard.getCount() + lastOpponentCard.getCount();
            return totalCount == 5; // 합이 5이면 true 반환
        }

        // 마지막 카드들에서 과일 개수 검사
        if ((lastPlayerCard.getCount() == 5 || lastOpponentCard.getCount() == 5) && (!lastPlayerCard.getFruit().equals(lastOpponentCard.getFruit()))) {
            return true; // 한 장이라도 과일이 5개면 true 반환
        }

        return false; // 위 조건을 만족하지 않으면 false 반환
    }


    private void resetPlayedCards() {
        // 양쪽 플레이된 카드 초기화
        playerPlayedDecks.clearCard();
        opponentPlayedDecks.clearCard();

        // UI에서 플레이된 카드 수 0으로 표시
        playerPlayedCardCount = 0;
        opponentPlayedCardCount = 0;
        playerCardCountView.setText("0");
        opponentCardCountView.setText("0");

        // 카드 이미지 초기화
        playerCardView.setImageResource(R.drawable.card_deck);
        opponentCardView.setImageResource(R.drawable.card_deck);
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

    private void checkForGameEnd() {
        if (playerDeck.isEmpty() && opponentDeck.isEmpty()) {
            showResult("결과", "무승부입니다.");

        } else if (playerDeck.isEmpty()) {
            showResult("결과", "상대방이 승리했습니다.");
        } else if (opponentDeck.isEmpty()) {
            showResult("결과", "플레이어가 승리했습니다!");
        }
    }


    private void showResult(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(title).setMessage(message);
        builder.setPositiveButton("확인", (dialog, id) -> navigateToLobby());

        AlertDialog alertDialog = builder.create();

        alertDialog.show();
        isPlayerTurn = true; // 플레이어만 카드 낼 수 있음
        isCardPlayed = false; // 카드 제출 상태 초기화
        aiReactionHandler.removeCallbacksAndMessages(null);
        gameHandler.removeCallbacksAndMessages(null);


    }
}