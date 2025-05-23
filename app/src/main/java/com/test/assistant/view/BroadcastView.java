package com.test.assistant.view;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.test.assistant.R;


public class BroadcastView extends View {
    private Paint mCirclePaint, mArcPaint;
    private int mArcWidth = 4;
    private int mCircleRadius = 15;

    private int mCx = -1;
    private int mCy = -1;

    private float mPhaseValue = 0;// mPhaseValue -> 0 - 500

    public BroadcastView(Context context) {
        this(context, null);
    }

    public BroadcastView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BroadcastView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BroadcastView);
        int circleColor = array.getColor(R.styleable.BroadcastView_broadcastCircleColor, Color.BLUE);
        int arcColor = array.getColor(R.styleable.BroadcastView_broadcastArcColor, Color.BLUE);
        mArcWidth = array.getDimensionPixelSize(R.styleable.BroadcastView_broadcastArcWidth, mArcWidth);
        mCircleRadius = array.getDimensionPixelSize(R.styleable.BroadcastView_broadcastCircleRadius, mCircleRadius);
        array.recycle();
        mArcPaint = setPaint(arcColor, false);
        mCirclePaint = setPaint(circleColor, true);
    }

    private Paint setPaint(int color, boolean isCircle) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        if (isCircle) {
            paint.setStyle(Paint.Style.FILL);
        } else {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(mArcWidth);
        }
        return paint;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCircle(canvas);
        drawArc(canvas);
    }

    private void drawArc(Canvas canvas) {

        RectF rectF;
        if (mPhaseValue > 100 && mPhaseValue < 400) {
            rectF = new RectF(mCx - mCircleRadius * 3, mCy - mCircleRadius * 3, mCx + mCircleRadius * 3, mCy + mCircleRadius * 3);
            canvas.drawArc(rectF, 315, 90, false, mArcPaint);
            canvas.drawArc(rectF, 135, 90, false, mArcPaint);
        }

        if (mPhaseValue > 180 && mPhaseValue < 450) {
            rectF = new RectF(mCx - mCircleRadius * 6, mCy - mCircleRadius * 6, mCx + mCircleRadius * 6, mCy + mCircleRadius * 6);
            canvas.drawArc(rectF, 315, 90, false, mArcPaint);
            canvas.drawArc(rectF, 135, 90, false, mArcPaint);
        }

        if (mPhaseValue > 250 && mPhaseValue < 500) {
            rectF = new RectF(mCx - mCircleRadius * 9, mCy - mCircleRadius * 9, mCx + mCircleRadius * 9, mCy + mCircleRadius * 9);
            canvas.drawArc(rectF, 315, 90, false, mArcPaint);
            canvas.drawArc(rectF, 135, 90, false, mArcPaint);
        }
    }

    private void drawCircle(Canvas canvas) {
        if (mCx == -1)
            mCx = (getWidth() - getPaddingLeft() - getPaddingRight()) / 2;
        if (mCy == -1)
            mCy = (getHeight() - getPaddingTop() - getPaddingBottom()) / 2;

        canvas.drawCircle(mCx, mCy, mCircleRadius, mCirclePaint);
    }

    public void setPhaseValue(float mPhaseValue) {
        this.mPhaseValue = mPhaseValue % 500;
        invalidate();
    }

    public void start() {
        ValueAnimator animator = ObjectAnimator.ofFloat(0, 2500);
        animator.setInterpolator(new MyInterpolator());
        animator.setDuration(4000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float number = (float) animation.getAnimatedValue();
                setPhaseValue(number);
            }
        });
        animator.start();
    }

    class MyInterpolator implements TimeInterpolator {
        @Override
        public float getInterpolation(float input) {
            return input;
        }
    }
}
