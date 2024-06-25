package com.islam.hw1;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Car car;
    private GameManager gameManager;
    private LaneManager laneManager;
    private Handler handler;
    private Runnable gameLoop;

    private int laneCount = 3; // Default number of lanes

    private int initialLives = 3;

    LinearLayout lifeContainer;

    boolean isGameRunning = false;

    private Button btnLeft;
    private Button btnRight;

    GameManager.GameListener gameListener = new GameManager.GameListener() {
        @Override
        public void onGameOver() {
//            Toast.makeText(MainActivity.this, "Game Over", Toast.LENGTH_SHORT).show();
            initializeLives(0);
            isGameRunning = false;
            gameOver();
        }

        @Override
        public void onCrashWithObstacle(int gameLiveLeft) {
            initializeLives(gameLiveLeft);
            Toast.makeText(MainActivity.this, "Crash!", Toast.LENGTH_SHORT).show();
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

        car = new Car(findViewById(R.id.car));
        laneManager = new LaneManager(findViewById(R.id.lanesContainer), laneCount, car);
        lifeContainer = findViewById(R.id.lifeContainer);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);

        gameManager = new GameManager(this, car, laneManager, btnLeft, btnRight);
        gameManager.setGameListener(gameListener);
        initializeControls();
        initializeLives(initialLives);
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
}
