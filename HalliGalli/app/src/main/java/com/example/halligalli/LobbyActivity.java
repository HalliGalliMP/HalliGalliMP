package com.example.halligalli;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LobbyActivity extends AppCompatActivity {
    private EditText playerNameInput;
    private Spinner difficultySpinner;
    private Button singleGameButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby); // 위의 첫 번째 XML 파일 이름

        getUI();

        singleGameButton.setOnClickListener(singleGameClickListener);
    }

    private void getUI(){
        // UI 요소 초기화

        difficultySpinner = findViewById(R.id.difficultySpinner);
        singleGameButton = findViewById(R.id.SingleGameButton);
    }



    // 싱글 게임 버튼 리스너
    private final View.OnClickListener singleGameClickListener = v -> {
        //플레이어 이름가져오기
        String playerName = playerNameInput.getText().toString().trim();
        // 난이도 값 가져오기
        String selectedDifficulty = difficultySpinner.getSelectedItem().toString();
        // 플레이어 이름 확인
        if (playerName.isEmpty()) {
            Toast.makeText(this, "플레이어 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // GameActivity로 전환
        Intent intent = new Intent(LobbyActivity.this, GameActivity.class);
        intent.putExtra("PLAYER_NAME", playerName); // 플레이어 이름 전달
        intent.putExtra("MODE", "싱글 모드"); // 모드 정보 전달
        intent.putExtra("DIFFICULTY", selectedDifficulty); // 난이도 정보 전달
        startActivity(intent);
    };


}