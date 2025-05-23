package com.test.assistant.fragments;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.test.assistant.R;


public class LoadingView extends View {

    private int mExCircleColor = Color.RED;
    private int mInnerCircleColor = Color.BLUE;
    private int mCircleWide = 5;

    private Paint mExCirclePaint;
    private Paint mInnerCirclePaint;

    private float mScanNumber = 270f;
    private float mRotateNumber = 270f;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.LoadingView);
        mCircleWide = array.getDimensionPixelOffset(R.styleable.LoadingView_allCircleWide, mCircleWide);
        mExCircleColor = array.getColor(R.styleable.LoadingView_excircleColor, mExCircleColor);
        mInnerCircleColor = array.getColor(R.styleable.LoadingView_innerCircleColor, mInnerCircleColor);
        array.recycle();
        initPaint();

    }

    private void initPaint() {
        mExCirclePaint = new Paint();
        mExCirclePaint.setAntiAlias(true);
        mExCirclePaint.setStyle(Paint.Style.STROKE);
        mExCirclePaint.setStrokeCap(Paint.Cap.ROUND);
        mExCirclePaint.setColor(mExCircleColor);
        mExCirclePaint.setStrokeWidth(mCircleWide);

        mInnerCirclePaint = new Paint();
        mInnerCirclePaint.setAntiAlias(true);
        mInnerCirclePaint.setStyle(Paint.Style.STROKE);
        mInnerCirclePaint.setStrokeCap(Paint.Cap.ROUND);
        mInnerCirclePaint.setColor(mInnerCircleColor);
        mInnerCirclePaint.setStrokeWidth(mCircleWide);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = MeasureSpec.getSize(widthMeasureSpec);
        int width = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(height > width ? width : height, height > width ? width : height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int circleX = getWidth() / 2;
        int circleY = getHeight() / 2;

        RectF rectF = new RectF(mCircleWide / 2, mCircleWide / 2, getWidth() - mCircleWide / 2, getHeight() - mCircleWide / 2);
        float margin = (float) (getWidth() * 0.2);
        RectF innerRectF = new RectF(mCircleWide / 2 + margin, mCircleWide / 2 + margin, getWidth() - mCircleWide / 2 - margin, getHeight() - mCircleWide / 2 - margin);

        mScanNumber = (mScanNumber + 4) % 360;//这样写的结果是，实现约1.5秒转一圈
        mRotateNumber = (mRotateNumber - 8) % 360;
        canvas.rotate(mScanNumber, circleX, circleY);//旋转画布
        canvas.drawArc(rectF, 26, 310, false, mExCirclePaint);
        //canvas.rotate(mRotateNumber, circleX, circleY);//旋转画布
        canvas.drawArc(innerRectF, 200, 310, false, mInnerCirclePaint);
        invalidate();
    }
}
