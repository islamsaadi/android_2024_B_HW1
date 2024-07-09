package com.islam.hw1;

import android.view.View;
import android.widget.LinearLayout;

public class LaneManager {

    public int laneCount;
    private final LinearLayout lanesContainer;
    private final Car car;

    public LaneManager(LinearLayout lanesContainer, int laneCount, Car car) {
        this.lanesContainer = lanesContainer;
        this.car = car;
        this.laneCount = laneCount;
        initializeLanes(laneCount);
    }


    private void initializeLanes(int laneCount) {
        lanesContainer.removeAllViews();

        for (int i = 0; i < laneCount; i++) {
            View lane = new View(lanesContainer.getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
            lane.setLayoutParams(params);
            lanesContainer.addView(lane);
        }

        lanesContainer.post(() -> {
            car.setLaneWidth(this.getLaneWidth());
        });
    }


    public LinearLayout getLanesContainer() {
        return lanesContainer;
    }

    public float getLaneWidth() {
        return (float) lanesContainer.getWidth() / this.getLaneCount();
    }

    public int getLaneCount() {
        return this.laneCount;
    }
}
