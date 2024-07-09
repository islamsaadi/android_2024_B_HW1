package com.islam.hw1;


import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

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

        car = new Car(findViewById(R.id.car), laneCount);
        laneManager = new LaneManager(findViewById(R.id.lanesContainer), laneCount, car);
        roadLinesView = findViewById(R.id.roadLinesView);
        roadLinesView.setLaneCount(laneCount); // Pass the lane count to the custom view
        lifeContainer = findViewById(R.id.lifeContainer);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        crashSound = MediaPlayer.create(this, R.raw.crash_sound);


        gameManager = new GameManager(this, mode, car, laneManager, btnLeft, btnRight, laneCount);
        gameManager.setGameListener(gameListener);
        initializeControls();
        initializeLives(initialLives);

        if ("sensors".equals(mode)) {

            btnLeft.setVisibility(View.GONE);

            btnRight.setVisibility(View.GONE);

            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        }

        startGame();
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
                    handler.postDelayed(this, 50); // Adjust delay for difficulty
                }

            }
        };
        handler.post(gameLoop);
    }

    public void gameOver() {
        handler.removeCallbacks(gameLoop);
        Intent intent = new Intent(MainActivity.this, GameOverActivity.class);
        startActivity(intent);
        finish();
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
    public void onSensorChanged(SensorEvent event) {
        if ("sensors".equals(mode)) {
            float x = event.values[0];
            if (x < -2) {
                car.moveRight();
            } else if (x > 2) {
                car.moveLeft();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
