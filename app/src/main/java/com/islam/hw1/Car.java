package com.islam.hw1;

import android.graphics.Rect;
import android.view.View;
import android.widget.ImageView;

public class Car {

    private final ImageView carView;
    private int currentLane;
    private float laneWidth;

    public Car(ImageView carView) {
        this.carView = carView;
        this.currentLane = 1; // Start in the middle lane
    }

    public void moveLeft() {
        if (currentLane > 0) {
            currentLane--;
            updatePosition();
        }
    }

    public void moveRight() {
        if (currentLane < LaneManager.LANE_COUNT - 1) {
            currentLane++;
            updatePosition();
        }
    }

    public void updatePosition() {
        carView.setX(laneWidth * currentLane + (laneWidth - carView.getWidth()) / 2);
    }

    public void setLaneWidth(float laneWidth) {
        this.laneWidth = laneWidth;
        updatePosition();
    }

    public boolean checkCollision(Obstacle obstacle) {
        Rect carRect = new Rect();
        carView.getHitRect(carRect);

        Rect obstacleRect = new Rect();
        obstacle.getView().getHitRect(obstacleRect);

        return Rect.intersects(carRect, obstacleRect);
    }
}
