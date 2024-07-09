package com.islam.hw1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RoadLinesView extends View {

    private Paint paint;
    private float laneWidth;
    private final int roadCutLinesColor = 0xFFFFFFFF; // White color for road lines
    private final int centralLineColor = 0xFFFFFF00; // Yellow color for central line

    private int laneCount;

    public RoadLinesView(Context context) {
        super(context);
        init();
    }

    public RoadLinesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoadLinesView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
    }

    public void setLaneCount(int laneCount) {
        this.laneCount = laneCount;
        invalidate(); // Redraw the view with the new lane count
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        laneWidth = (float) getWidth() / this.laneCount;
        drawRoadLines(canvas);
    }

    private void drawRoadLines(Canvas canvas) {
        for (int i = 1; i < this.laneCount; i++) {
            if (i == this.laneCount / 2 || i == (this.laneCount / 2) + 1) { // Draw central line as solid line
                drawSolidLine(canvas, i);
            } else { // Draw cut lines for other dividers
                drawCutLine(canvas, i);
            }
        }
    }

    private void drawSolidLine(Canvas canvas, int laneIndex) {
        paint.setColor(centralLineColor);

        float startX = laneIndex * laneWidth;
        float stopX = laneIndex * laneWidth;
        float startY = 0;
        float stopY = getHeight();

        canvas.drawLine(startX, startY, stopX, stopY, paint);
    }

    private void drawCutLine(Canvas canvas, int laneIndex) {
        paint.setColor(roadCutLinesColor);

        float startX = laneIndex * laneWidth;
        float stopX = laneIndex * laneWidth;
        float startY = 0;

        float segmentHeight = 40; // Length of each cut segment
        float segmentGap = 20; // Gap between cut segments

        while (startY < getHeight()) {
            canvas.drawLine(startX, startY, stopX, startY + segmentHeight, paint);
            startY += segmentHeight + segmentGap;
        }
    }
}
