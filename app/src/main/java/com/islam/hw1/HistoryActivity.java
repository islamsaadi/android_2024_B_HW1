package com.islam.hw1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;

public class HistoryActivity extends AppCompatActivity {

    private ListView listView;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "game_prefs";
    private static final String KEY_POINTS = "points";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        listView = findViewById(R.id.listView);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        ArrayList<Integer> pointsList = getPointsHistory();
        Collections.sort(pointsList);

        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pointsList);
        listView.setAdapter(adapter);
    }

    private ArrayList<Integer> getPointsHistory() {
        ArrayList<Integer> pointsList = new ArrayList<>();
        int points = sharedPreferences.getInt(KEY_POINTS, 0);
        pointsList.add(points);
        return pointsList;
    }
}
