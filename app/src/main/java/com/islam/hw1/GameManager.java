package com.islam.hw1;

import android.content.Context;
import android.media.MediaPlayer;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GameManager {

    private final Context context;
    private final Car car;
    private final LaneManager laneManager;
    private final ArrayList<Obstacle> obstacles;
    private final Random random;
    private final ArrayList<ImageView> coins;
    private final int obstacleSpeed = 15;
    private int lives = 3;

    private int gameLoopIterations = 0;

    private int lastTimeObsticaleGenerated = -1;

    GameListener gameListener;

    private final Button btnLeft;
    private final Button btnRight;

    private final int laneCount;
    private int score = 0;

    private final String mode;

    private static final String PREFS_NAME = "game_prefs";

    private final MediaPlayer coinCollectSound;



    public GameManager(Context context, String mode, Car car, LaneManager laneManager, Button btnLeft, Button btnRight, int laneCount) {
        this.context = context;
        this.car = car;
        this.laneManager = laneManager;
        this.obstacles = new ArrayList<>();
        this.random = new Random();
        this.btnLeft = btnLeft;
        this.btnRight = btnRight;
        this.laneCount = laneCount;
        this.coins = new ArrayList<>();
        this.mode = mode;
        this.coinCollectSound = MediaPlayer.create(context, R.raw.coin_collect);
    }

    public void updateGame() {
        gameLoopIterations++;
        if (gameLoopIterations - lastTimeObsticaleGenerated > 50 ){
            generateObstaclesAndCoins();
            lastTimeObsticaleGenerated = gameLoopIterations;
        }

        moveObstaclesAndCoins();

        if(! ("sensors".equals(mode))) {
            btnLeft.bringToFront();
            btnRight.bringToFront();
        }

        checkCollision();
    }

    private void generateObstaclesAndCoins() {
        Set<Integer> occupiedLanes = new HashSet<>();
        int maxObstacles = laneCount - 1;
        int obstaclesToGenerate = random.nextInt(maxObstacles) + 1;

        for (int i = 0; i < obstaclesToGenerate; i++) {
            int lane;
            do {
                lane = random.nextInt(laneCount);
            } while (occupiedLanes.contains(lane));
            occupiedLanes.add(lane);

            Obstacle obstacle = new Obstacle(context, lane, laneManager);
            obstacles.add(obstacle);
            ((RelativeLayout) laneManager.getLanesContainer().getParent()).addView(obstacle.getView());
        }

        int coinsToGenerate = random.nextInt(laneCount - obstaclesToGenerate);
        for (int i = 0; i < coinsToGenerate; i++) {
            int lane;
            do {
                lane = random.nextInt(laneCount);
            } while (occupiedLanes.contains(lane));
            occupiedLanes.add(lane);

            ImageView coin = new ImageView(context);
            coin.setImageResource(R.drawable.ic_coin);
            coin.setLayoutParams(new RelativeLayout.LayoutParams(50, 50));
            coin.setX(laneManager.getLaneWidth() * lane + (laneManager.getLaneWidth() - 50) / 2);
            coin.setY(-100);
            coins.add(coin);
            ((RelativeLayout) laneManager.getLanesContainer().getParent()).addView(coin);
        }

    }

    private void moveObstaclesAndCoins() {
        ArrayList<Obstacle> obstaclesToRemove = new ArrayList<>();
        for (Obstacle obstacle : obstacles) {
            obstacle.moveDown(obstacleSpeed);
            if (obstacle.isOutOfView()) {
                obstaclesToRemove.add(obstacle);
            }
        }
        for (Obstacle obstacle : obstaclesToRemove) {
            ((RelativeLayout) laneManager.getLanesContainer().getParent()).removeView(obstacle.getView());
            obstacles.remove(obstacle);
        }

        ArrayList<ImageView> coinsToRemove = new ArrayList<>();
        for (ImageView coin : coins) {
            coin.setY(coin.getY() + obstacleSpeed);
            if (coin.getY() > car.getView().getBottom()) {
                coinsToRemove.add(coin);
            }
        }
        for (ImageView coin : coinsToRemove) {
            ((RelativeLayout) laneManager.getLanesContainer().getParent()).removeView(coin);
            coins.remove(coin);
        }
    }

    private void checkCollision() {
        for (Obstacle obstacle : obstacles) {
            if (car.checkCollision(obstacle.getView())) {
                handleCrash();
                obstacle.resetPosition();
            }
        }

        ArrayList<ImageView> coinsToRemove = new ArrayList<>();
        for (ImageView coin : coins) {
            if (car.checkCollision(coin)) {
                coinsToRemove.add(coin);
                score += 10;
                coinCollectSound.start();
                animateCoinCollection(coin);

            }
        }
        for (ImageView coin : coinsToRemove) {
            ((RelativeLayout) laneManager.getLanesContainer().getParent()).removeView(coin);
            coins.remove(coin);
        }
    }

    private void animateCoinCollection(ImageView coin) {
        coin.animate().alpha(0f).setDuration(500).withEndAction(() -> {
            ((RelativeLayout) laneManager.getLanesContainer().getParent()).removeView(coin);
            coin.setAlpha(1f);
        });
    }

    private void handleCrash() {
        lives--;
        if (lives <= 0) {
            gameOver();
        } else {
            if (gameListener!=null){
                gameListener.onCrashWithObstacle(lives);
            }
        }
    }

    private void gameOver() {
        if (gameListener != null)
            gameListener.onGameOver();
    }


    public void setGameListener(GameListener gameListener) {
        this.gameListener = gameListener;
    }

    public void setScore(int score) {
        if(score >= 0)
            this.score = score;
    }

    public interface GameListener{
        void onGameOver();
        void onCrashWithObstacle(int gameLiveLeft);
    }

    public int getScore() {
        return score;
    }

}
