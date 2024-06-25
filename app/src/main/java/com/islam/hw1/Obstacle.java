package com.islam.hw1;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class Obstacle {

    private final ImageView obstacleView;
    private final int lane;
    private final LaneManager laneManager;

    public Obstacle(Context context, int lane, LaneManager laneManager) {
        this.obstacleView = new ImageView(context);
        this.obstacleView.setImageResource(R.drawable.ic_rock); // Replace with your obstacle drawable
        this.obstacleView.setLayoutParams(new RelativeLayout.LayoutParams(100, 100)); // Set obstacle size
        this.lane = lane;
        this.laneManager = laneManager;

        setPosition();
    }

    private void setPosition() {
        obstacleView.setX(laneManager.getLaneWidth() * lane + (laneManager.getLaneWidth() - 100) / 2);
        obstacleView.setY(-100); // Start above the screen
    }

    public void moveDown(int speed) {
        obstacleView.setY(obstacleView.getY() + speed);
    }

    public boolean isOutOfView() {
        return obstacleView.getY() > laneManager.getLanesContainer().getHeight();
    }

    public void resetPosition() {
        setPosition();
    }

    public View getView() {
        return obstacleView;
    }
}
