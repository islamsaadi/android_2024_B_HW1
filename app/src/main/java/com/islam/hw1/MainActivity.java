package com.islam.hw1;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final float FILTER_ALPHA = 0.25f; // Alpha value for the low-pass filter
    private static final float TILT_THRESHOLD = 2.0f; // Tilt threshold for lane change
    private static final int DEBOUNCE_TIME = 300; // Minimum time between movements in milliseconds


    private static final String TAG = "MAIN ACTIVITY";

    private Car car;
    private GameManager gameManager;
    private LaneManager laneManager;
    private Handler handler;
    private Runnable gameLoop;

    private final int laneCount = 5; // Default number of lanes

    private final int initialLives = 3;

    LinearLayout lifeContainer;

    boolean isGameRunning = false;

    private Button btnLeft;
    private Button btnRight;

    private SensorManager sensorManager;

    private Sensor accelerometer;

    private String mode;

    private MediaPlayer crashSound;

    private RoadLinesView roadLinesView;

    private TextView scoreTextView;

    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "game_prefs";
    private static final String KEY_ACHIEVEMENTS = "achievements";
    private float[] smoothedValues = new float[3]; // To store the smoothed sensor values
    private long lastSensorUpdate = 0;

    private long lastMoveTime = 0; // To track the last movement time

    private TextView distanceTextView;

    private double distance = 0; // Distance counter



    GameManager.GameListener gameListener = new GameManager.GameListener() {
        @Override
        public void onGameOver() {
            initializeLives(0);
            isGameRunning = false;
            gameOver();
        }

        @Override
        public void onCrashWithObstacle(int gameLiveLeft) {
            initializeLives(gameLiveLeft);
            Toast.makeText(MainActivity.this, "Crash!", Toast.LENGTH_SHORT).show();
            crashSound.start();
            Vibrator vibrator = (Vibrator) MainActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(500); // Vibrate for 500 milliseconds
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mode = getIntent().getStringExtra("mode");

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);


        scoreTextView = findViewById(R.id.scoreTextView);
        distanceTextView = findViewById(R.id.distanceTextView);

        roadLinesView = findViewById(R.id.roadLinesView);
        if (roadLinesView != null) {
            roadLinesView.setLaneCount(laneCount); // Pass the lane count to the custom view
            Log.d(TAG, "RoadLinesView found and lane count set.");
        } else {
            Log.e(TAG, "RoadLinesView is null!");
        }
        car = new Car(findViewById(R.id.car), laneCount);
        laneManager = new LaneManager(findViewById(R.id.lanesContainer), laneCount, car);

        lifeContainer = findViewById(R.id.lifeContainer);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        crashSound = MediaPlayer.create(this, R.raw.crash_sound);


        gameManager = new GameManager(this, mode, car, laneManager, btnLeft, btnRight, laneCount, scoreTextView);
        gameManager.setGameListener(gameListener);
        initializeControls();
        initializeLives(initialLives);

        if ("sensors".equals(mode)) {

            btnLeft.setVisibility(View.GONE);

            btnRight.setVisibility(View.GONE);

            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        }

        checkLocationPermission();

        startGame();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    private void initializeLives(int lives) {
        lifeContainer.removeAllViews();
        for (int i = 0; i < lives; i++) {
            ImageView heart = new ImageView(this);
            heart.setImageResource(R.drawable.ic_love); // Replace with your heart drawable
            int size = (int) getResources().getDimension(R.dimen.heart_size);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            heart.setLayoutParams(params);
            lifeContainer.addView(heart);
        }
    }

    private void initializeControls() {
        btnLeft.setOnClickListener(v -> car.moveLeft());
        btnRight.setOnClickListener(v -> car.moveRight());

        btnLeft.bringToFront();
        btnRight.bringToFront();
    }

    private void startGame() {
        isGameRunning = true;
        handler = new Handler();
        gameLoop = new Runnable() {
            @Override
            public void run() {
                if (isGameRunning) {
                    gameManager.updateGame();
                    distance += 0.05; // Increase the distance
                    distanceTextView.setText(String.format(Locale.getDefault(), "%.1f m", distance));
                    scoreTextView.setText("Score: " + gameManager.getScore());
                    handler.postDelayed(this, 50); // Adjust delay for difficulty
                }

            }
        };
        handler.post(gameLoop);
    }

    public void gameOver() {
        handler.removeCallbacks(gameLoop);
        saveAchievement(gameManager.getScore());
        Intent intent = new Intent(MainActivity.this, GameOverActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveAchievement(int points) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String achievements = sharedPreferences.getString(KEY_ACHIEVEMENTS, "");
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission();
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String newAchievement = currentTime + " - " + points + " - " + (location != null ? location.getLatitude() + "," + location.getLongitude() : "Unknown");

        ArrayList<String> achievementsList = new ArrayList<>();
        if (!achievements.isEmpty()) {
            String[] achievementsArr = achievements.split(";");
            Collections.addAll(achievementsList, achievementsArr);
        }
        achievementsList.add(newAchievement);

        // Sort and keep top 10
        achievementsList.sort(new Comparator<String>() {
            @Override
            public int compare(String a1, String a2) {
                int score1 = Integer.parseInt(a1.split(" - ")[1]);
                int score2 = Integer.parseInt(a2.split(" - ")[1]);
                return Integer.compare(score2, score1); // Descending order
            }
        });

        if (achievementsList.size() > 10) {
            achievementsList = new ArrayList<>(achievementsList.subList(0, 10));
        }

        StringBuilder sb = new StringBuilder();
        for (String achievement : achievementsList) {
            sb.append(achievement).append(";");
        }

        editor.putString(KEY_ACHIEVEMENTS, sb.toString());
        editor.apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                checkLocationPermission();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ("sensors".equals(mode)) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if ("sensors".equals(mode)) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (handler != null) {
            handler.removeCallbacks(gameLoop);
        }
        // Save the current state (distance and points) if needed
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("current_distance", (float) distance);
        editor.putInt("current_points", gameManager.getScore());
        editor.apply();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: Game restarted and state restored");
        // Restore the last state (distance and points)
        distance = sharedPreferences.getFloat("current_distance", 0);
        int score = sharedPreferences.getInt("current_points", 0);
        gameManager.setScore(score);
        distanceTextView.setText(String.format(Locale.getDefault(), "%.1f m", distance));
        scoreTextView.setText("Points: " + score);
        startGame();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if ("sensors".equals(mode)) {

                smoothedValues[0] = FILTER_ALPHA * event.values[0] + (1 - FILTER_ALPHA) * smoothedValues[0];
                smoothedValues[1] = FILTER_ALPHA * event.values[1] + (1 - FILTER_ALPHA) * smoothedValues[1];
                smoothedValues[2] = FILTER_ALPHA * event.values[2] + (1 - FILTER_ALPHA) * smoothedValues[2];

                float x = smoothedValues[0];
                long currentTimeMillis = System.currentTimeMillis();

                if (currentTimeMillis - lastMoveTime > DEBOUNCE_TIME) {
                    if (x > TILT_THRESHOLD) {
                        car.moveLeft();
                        lastMoveTime = currentTimeMillis;
                    } else if (x < -TILT_THRESHOLD) {
                        car.moveRight();
                        lastMoveTime = currentTimeMillis;
                    }
                }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
