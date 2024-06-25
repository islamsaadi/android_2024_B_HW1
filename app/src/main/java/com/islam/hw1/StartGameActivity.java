package com.islam.hw1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class StartGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);

        Button startButton = findViewById(R.id.btnStartGame);
        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(StartGameActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}
