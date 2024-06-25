package com.islam.hw1;

import android.content.Context;
import android.widget.Button;
import android.widget.RelativeLayout;

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
    private int obstacleSpeed = 15;
    private int lives = 3;

    private int gameLoopIterations = 0;

    private int lastTimeObsticaleGenerated = -1;

    GameListener gameListener;

    private final Button btnLeft;
    private final Button btnRight;

    public GameManager(Context context, Car car, LaneManager laneManager, Button btnLeft, Button btnRight) {
        this.context = context;
        this.car = car;
        this.laneManager = laneManager;
        this.obstacles = new ArrayList<>();
        this.random = new Random();
        this.btnLeft = btnLeft;
        this.btnRight = btnRight;
    }

    public void updateGame() {
        gameLoopIterations++;
        if (gameLoopIterations - lastTimeObsticaleGenerated > 50 ){
            generateObstacles();
            lastTimeObsticaleGenerated = gameLoopIterations;
        }
        moveObstacles();
        checkCollision();
    }

    private void generateObstacles() {
        Set<Integer> occupiedLanes = new HashSet<>();

        // to ensure leaving at least one lane free
        int maxObstacles = laneManager.getLaneCount() - 1;
        int obstaclesToGenerate = random.nextInt(maxObstacles) + 1;

        for (int i = 0; i < obstaclesToGenerate; i++) {
            int lane;
            do {
                lane = random.nextInt(laneManager.getLaneCount());
            } while (occupiedLanes.contains(lane));
            occupiedLanes.add(lane);

            Obstacle obstacle = new Obstacle(context, lane, laneManager);
            obstacles.add(obstacle);
            ((RelativeLayout) laneManager.getLanesContainer().getParent()).addView(obstacle.getView());
        }

        btnLeft.bringToFront();
        btnRight.bringToFront();
    }

    private void moveObstacles() {
        ArrayList<Obstacle> toRemove = new ArrayList<>();
        for (Obstacle obstacle : obstacles) {
            obstacle.moveDown(obstacleSpeed);
            if (obstacle.isOutOfView()) {
                toRemove.add(obstacle);
            }
        }
        for (Obstacle obstacle : toRemove) {
            ((RelativeLayout) laneManager.getLanesContainer().getParent()).removeView(obstacle.getView());
            obstacles.remove(obstacle);
        }
    }

    private void checkCollision() {
        for (Obstacle obstacle : obstacles) {
            if (car.checkCollision(obstacle)) {
                handleCrash();
                obstacle.resetPosition();
            }
        }
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

    public interface GameListener{
        void onGameOver();
        void onCrashWithObstacle(int gameLiveLeft);
    }
}
