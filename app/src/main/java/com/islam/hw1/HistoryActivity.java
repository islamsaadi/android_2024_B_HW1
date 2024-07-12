package com.islam.hw1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ListView listView;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "game_prefs";
    private static final String KEY_ACHIEVEMENTS = "achievements";
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        listView = findViewById(R.id.listView);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        ArrayList<String> achievements = getAchievements();
        ArrayList<String> scores = extractScores(achievements);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, scores) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setGravity(android.view.Gravity.CENTER);
                return view;
            }
        };
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String achievement = achievements.get(position);
            String[] parts = achievement.split(" - ");
            if (parts.length == 3) {
                String location = parts[2];
                if (!"Unknown".equals(location)) {
                    String[] latLng = location.split(",");
                    double latitude = Double.parseDouble(latLng[0]);
                    double longitude = Double.parseDouble(latLng[1]);
                    LatLng latLngObj = new LatLng(latitude, longitude);
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(latLngObj).title("Record Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngObj, 15));
                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private ArrayList<String> getAchievements() {
        String achievementsStr = sharedPreferences.getString(KEY_ACHIEVEMENTS, "");
        String[] achievementsArr = achievementsStr.split(";");
        ArrayList<String> achievementsList = new ArrayList<>();
        for (String achievement : achievementsArr) {
            if (!achievement.isEmpty()) {
                achievementsList.add(achievement);
            }
        }
        return achievementsList;
    }

    private ArrayList<String> extractScores(ArrayList<String> achievements) {
        ArrayList<String> scores = new ArrayList<>();
        for (String achievement : achievements) {
            String[] parts = achievement.split(" - ");
            if (parts.length >= 2) {
                scores.add(parts[1]); // Add only the score/points
            }
        }
        return scores;
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
    }
}
