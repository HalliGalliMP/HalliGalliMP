package com.example.halligalli;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;



public class GameActivity extends AppCompatActivity {
    private String mode; // 싱글 or 멀티
    private String difficulty; // 쉬움, 보통, 어려움
    private TextView gameTimerView;

    private Handler timerHandler = new Handler();
    private int turnTimeLeft = 9; // 9초 제한시간
    private boolean isBlinking = false; // 깜박임 애니메이션 상태 확인


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Intent로 전달된 데이터 수신
        Intent intent = getIntent();
        mode = intent.getStringExtra("MODE");
        difficulty = intent.getStringExtra("DIFFICULTY");

        // 화면 초기화
        initializeGame();

        // 데이터 확인 토스트메시지
        Toast.makeText(this, "모드: " + mode + ", 난이도: " + difficulty, Toast.LENGTH_SHORT).show();

        // 타이머 TextView 초기화
        gameTimerView = findViewById(R.id.gameTimer);

        // 9초 제한 타이머 시작
        startTurnTimer();

        // 로비로 돌아가기 버튼
        Button backToLobbyButton = findViewById(R.id.backToLobbyButton);
        backToLobbyButton.setOnClickListener(v -> showExitConfirmationDialog());
    }

    // 화면 초기화
    private void initializeGame() {
        TextView opponentName = findViewById(R.id.opponentName);
        TextView playerName = findViewById(R.id.playerName);

        // 싱글 모드 기본 설정
        opponentName.setText("상대");
        playerName.setText("나");
    }

    private void startTurnTimer() {
        turnTimeLeft = 9;
        isBlinking = false; // 새로운 턴에서 깜박임 초기화
        updateTimerView();

        // 1초마다 타이머 업데이트
        timerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (turnTimeLeft > 0) {
                    turnTimeLeft--;

                    // 3초 이하일 때 깜박이기 시작
                    if (turnTimeLeft <= 3 && !isBlinking) {
                        triggerBlinkEffect();
                        isBlinking = true;
                    }

                    updateTimerView();
                    timerHandler.postDelayed(this, 1000); // 1초마다 반복
                } else {
                    // 타이머 종료 시 턴 자동 진행
                    endTurn();
                }
            }
        }, 1000);
    }

    private void updateTimerView() {
        // 초 단위로 UI 업데이트
        gameTimerView.setText(String.valueOf(turnTimeLeft) + "초");
    }

    private void triggerBlinkEffect() {
        // 깜박이는 애니메이션 효과
        ObjectAnimator animator = ObjectAnimator.ofFloat(gameTimerView, "alpha", 1f, 0f, 1f);
        animator.setDuration(300); // 깜박이는 속도
        animator.setRepeatCount(ObjectAnimator.INFINITE); // 무한 반복 (3초 동안)
        animator.start();

        // 3초 후 애니메이션 종료
        timerHandler.postDelayed(() -> animator.cancel(), 3000);
    }

    private void endTurn() {
        // 턴 자동 진행 (여기에 게임 로직을 추가)
        turnTimeLeft = 9; // 다음 턴을 위해 타이머 초기화
        startTurnTimer(); // 다음 턴 타이머 시작
    }




    // 확인 대화상자 표시
    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("게임 종료")
                .setMessage("게임을 종료하고 로비로 돌아가시겠습니까?")
                .setPositiveButton("예", (dialog, which) -> navigateToLobby())
                .setNegativeButton("아니오", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // 로비로 이동
    private void navigateToLobby() {
        Intent intent = new Intent(GameActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 기존 액티비티 스택 제거
        startActivity(intent);
    }
}
