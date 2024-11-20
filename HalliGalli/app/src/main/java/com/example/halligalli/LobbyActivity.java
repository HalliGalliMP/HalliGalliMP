package com.example.halligalli;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class LobbyActivity extends AppCompatActivity {
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
        // 난이도 값 가져오기
        String selectedDifficulty = difficultySpinner.getSelectedItem().toString();

        // GameActivity로 전환
        Intent intent = new Intent(LobbyActivity.this, GameActivity.class);
        intent.putExtra("MODE", "SINGLE"); // 모드 정보 전달
        intent.putExtra("DIFFICULTY", selectedDifficulty); // 난이도 정보 전달
        startActivity(intent);
    };


}