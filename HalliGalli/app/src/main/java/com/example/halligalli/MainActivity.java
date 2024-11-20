package com.example.halligalli;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Spinner difficultySpinner;
    private Button singleGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 위의 첫 번째 XML 파일 이름

        // UI 요소 초기화
        difficultySpinner = findViewById(R.id.difficultySpinner);
        singleGameButton = findViewById(R.id.SingleGameButton);

        // 싱글 모드 버튼 클릭 이벤트
        singleGameButton.setOnClickListener(v -> {
            // 난이도 값 가져오기
            String selectedDifficulty = difficultySpinner.getSelectedItem().toString();

            // GameActivity로 전환
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("MODE", "SINGLE"); // 모드 정보 전달
            intent.putExtra("DIFFICULTY", selectedDifficulty); // 난이도 정보 전달
            startActivity(intent);
        });
    }
}