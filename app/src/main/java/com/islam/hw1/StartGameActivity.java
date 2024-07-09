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

        Button btnMode = findViewById(R.id.btnButtonsMode);
        Button sensorMode = findViewById(R.id.btnSensorsMode);
        Button historyMode = findViewById(R.id.btnHistory);

        btnMode.setOnClickListener(v -> {
            Intent intent = new Intent(StartGameActivity.this, MainActivity.class);
            intent.putExtra("mode", "buttons");
            startActivity(intent);
        });

        sensorMode.setOnClickListener(v -> {
            Intent intent = new Intent(StartGameActivity.this, MainActivity.class);
            intent.putExtra("mode", "sensors");
            startActivity(intent);
        });

        historyMode.setOnClickListener(v -> {
            Intent intent = new Intent(StartGameActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }
}
