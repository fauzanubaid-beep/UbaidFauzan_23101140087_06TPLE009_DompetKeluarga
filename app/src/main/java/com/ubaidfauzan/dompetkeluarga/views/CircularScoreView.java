package com.ubaidfauzan.dompetkeluarga.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class CircularScoreView extends View {

    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint innerCirclePaint;
    private Paint dotPaint;

    private float progress = 0f; // 0 to 100
    private RectF rectF;

    public CircularScoreView(Context context) {
        super(context);
        init();
    }

    public CircularScoreView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.parseColor("#E0E0E0")); // Light gray
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(Color.parseColor("#F5C518")); // Yellow
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        innerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerCirclePaint.setColor(Color.parseColor("#F5C518")); // Yellow
        innerCirclePaint.setStyle(Paint.Style.FILL);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(Color.parseColor("#D4A800")); // Darker yellow for dots
        dotPaint.setStyle(Paint.Style.FILL);

        rectF = new RectF();
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    public void setColors(int mainColor, int dotColor) {
        progressPaint.setColor(mainColor);
        innerCirclePaint.setColor(mainColor);
        dotPaint.setColor(dotColor);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float size = Math.min(width, height);
        
        float strokeWidth = size * 0.15f;
        backgroundPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeWidth(strokeWidth);

        float padding = strokeWidth / 2f;
        rectF.set(padding, padding, size - padding, size - padding);

        // Draw background ring
        canvas.drawArc(rectF, 0, 360, false, backgroundPaint);

        // Draw progress ring (start from top, -90 degrees)
        float sweepAngle = (progress / 100f) * 360f;
        canvas.drawArc(rectF, -90, sweepAngle, false, progressPaint);

        // Draw dots at the ends of progress ring
        if (progress > 0) {
            float radius = (size - strokeWidth) / 2f;
            float cx = size / 2f;
            float cy = size / 2f;
            
            // Start dot (-90 deg)
            float startX = cx + (float) (radius * Math.cos(Math.toRadians(-90)));
            float startY = cy + (float) (radius * Math.sin(Math.toRadians(-90)));
            canvas.drawCircle(startX, startY, strokeWidth * 0.4f, dotPaint);

            // End dot
            float endAngle = -90 + sweepAngle;
            float endX = cx + (float) (radius * Math.cos(Math.toRadians(endAngle)));
            float endY = cy + (float) (radius * Math.sin(Math.toRadians(endAngle)));
            canvas.drawCircle(endX, endY, strokeWidth * 0.4f, dotPaint);
        }

        // Draw inner circle with gap
        float gap = size * 0.05f;
        float innerRadius = (size / 2f) - strokeWidth - gap;
        canvas.drawCircle(size / 2f, size / 2f, innerRadius, innerCirclePaint);
    }
}
