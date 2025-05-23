package com.test.assistant.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.test.assistant.R;

public class LoadingCircleView extends View {

    private Paint mLeftPaint, mRightPaint;

    private int mCircleX = -1, mCircleY = -1;

    private RectF mRectF;

    private int mRadius = 30;

    private float mRotateNumber = 270f;

    private boolean isStart = false;

    public LoadingCircleView(Context context) {
        this(context, null);
    }

    public LoadingCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.LoadingCircleView);
        int leftColor = array.getColor(R.styleable.LoadingCircleView_loadingCircleColorLeft, Color.RED);
        int rightColor = array.getColor(R.styleable.LoadingCircleView_loadingCircleColorRight, Color.BLUE);
        int width = array.getDimensionPixelSize(R.styleable.LoadingCircleView_loadingCircleWidth, 10);
        mRadius = array.getDimensionPixelSize(R.styleable.LoadingCircleView_loadingCircleRadius, mRadius);
        mLeftPaint = getCirclePaint(leftColor, width);
        mRightPaint = getCirclePaint(rightColor, width);
        array.recycle();
    }


    private Paint getCirclePaint(int color, int width) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(width);
        paint.setStrokeCap(Paint.Cap.ROUND);
        return paint;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawArc(canvas);
    }

    private void drawArc(Canvas canvas) {
        if (mRectF == null) {
            int x = (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
            int y = (getHeight() - getPaddingBottom() - getPaddingTop()) / 2 + getPaddingTop();
            mCircleX = x;
            mCircleY = y;
            mRectF = new RectF(x - mRadius, y - mRadius, x + mRadius, y + mRadius);
        }
        mRotateNumber = (mRotateNumber + 8) % 360;
        canvas.rotate(mRotateNumber, mCircleX, mCircleY);//旋转画布
        canvas.drawArc(mRectF, 290, 360, false, mRightPaint);
        canvas.drawArc(mRectF, 110, 80, false, mLeftPaint);
        if (isStart)
            invalidate();
    }

    public void start() {
        isStart = true;
    }

    public void stop() {
        isStart = false;
    }

}
