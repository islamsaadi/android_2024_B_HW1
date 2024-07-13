package com.islam.hw1;

import android.graphics.Rect;
import android.view.View;
import android.widget.ImageView;

public class Car {

    private final ImageView carView;
    private int currentLane;
    private float laneWidth;
    private final int laneCount;

    public Car(ImageView carView, int laneCount) {
        this.carView = carView;
        this.laneCount = laneCount;
        this.currentLane = 1; // Start in the middle lane
    }

    public void moveLeft() {
        if (currentLane > 0) {
            currentLane--;
            updatePosition();
        }
    }

    public void moveRight() {
        if (currentLane < laneCount - 1) {
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

    public boolean checkCollision(View objectView) {
        Rect carRect = new Rect();
        carView.getHitRect(carRect);

        Rect objectRect = new Rect();
        objectView.getHitRect(objectRect);

        return Rect.intersects(carRect, objectRect);
    }

    public ImageView getView() {
        return carView;
    }
}
