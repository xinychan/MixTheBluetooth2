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

public class SlewingRingView extends View {
    private int mRadius = 10;
    private int mArcWidth = 5;
    private Paint mCirclePaint, mArcPaint, mChangeCirclePaint, mChangeArcPaint, mLinePaint;

    private int mCircleX = -1;
    private int mCircleY = -1;
    private RectF mRectF;

    private float mRotateNumber = 270f;


    private enum State {Rest, Work, StopWork, ErrorWork}

    private State mState = State.Rest;

    private float mProgress = 0;

    public SlewingRingView(Context context) {
        this(context, null);
    }

    public SlewingRingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlewingRingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SlewingRingView);
        int originColor = array.getColor(R.styleable.SlewingRingView_slewingRingOriginColor, Color.BLUE);
        int changeColor = array.getColor(R.styleable.SlewingRingView_slewingRingChangeColor, Color.BLACK);
        mRadius = array.getDimensionPixelSize(R.styleable.SlewingRingView_slewingRingRadius, mRadius);
        mArcWidth = array.getDimensionPixelSize(R.styleable.SlewingRingView_slewingRingArcWidth, mArcWidth);
        array.recycle();
        mArcPaint = getCirclePaint(originColor, false);
        mChangeArcPaint = getCirclePaint(changeColor, false);
        mCirclePaint = getCirclePaint(originColor, true);
        mChangeCirclePaint = getCirclePaint(changeColor, true);
        mLinePaint = getLinePaint();
    }

    private Paint getCirclePaint(int color, boolean b) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        if (b)
            paint.setStyle(Paint.Style.FILL);
        else {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(mArcWidth);
            paint.setStrokeCap(Paint.Cap.ROUND);
        }
        return paint;
    }

    private Paint getLinePaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(dp2px());
        paint.setColor(Color.RED);
        paint.setStrokeCap(Paint.Cap.ROUND);
        return paint;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width > height ? height : width, width > height ? height : width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initData();
        drawCircle(canvas);
        drawArc(canvas);
        drawLine(canvas);
    }

    private void drawLine(Canvas canvas) {
        if (mState == State.ErrorWork) {
            int x1 = mCircleX - mRadius;
            int x2 = mCircleX + mRadius;
            int y1 = mCircleY - mRadius;
            int y2 = mCircleY + mRadius;
            canvas.drawLine(x1, y1, x2, y2, mLinePaint);
            canvas.drawLine(x1, y2, x2, y1, mLinePaint);
        }
    }

    private void drawArc(Canvas canvas) {
        if (mState == State.Work) {
            mRotateNumber = (mRotateNumber + 6) % 360;
            canvas.rotate(mRotateNumber, mCircleX, mCircleY);//旋转画布
            canvas.drawArc(mRectF, 22.5f, 40, false, mArcPaint);
            canvas.drawArc(mRectF, 112.5f, 40, false, mArcPaint);
            canvas.drawArc(mRectF, 202.5f, 40, false, mArcPaint);
            canvas.drawArc(mRectF, 292.5f, 40, false, mArcPaint);
            invalidate();
        } else if (mProgress < 500 && mState == State.StopWork) {//将圆弧补成一个圆
            canvas.drawArc(mRectF, 22.5f, 40 + mProgress / 10, false, mArcPaint);
            canvas.drawArc(mRectF, 112.5f, 40 + mProgress / 10, false, mArcPaint);
            canvas.drawArc(mRectF, 202.5f, 40 + mProgress / 10, false, mArcPaint);
            canvas.drawArc(mRectF, 292.5f, 40 + mProgress / 10, false, mArcPaint);
        } else if (mProgress < 1000 && mProgress > 500) {//将圆旋转变色
            float number = mProgress - 500;
            canvas.drawArc(mRectF, 270, 360, false, mArcPaint);
            canvas.drawArc(mRectF, 270, 360 * (number / 500), false, mChangeArcPaint);
        } else if (mProgress > 1000 && mProgress < 1500) {//将圆缩小，回内圆
            float number = mProgress - 1000;
            float left = mCircleX - mRadius * (2 - number / 500);
            float right = mCircleX + mRadius * (2 - number / 500);
            float top = mCircleY - mRadius * (2 - number / 500);
            float bottom = mCircleY + mRadius * (2 - number / 500);
            RectF rectF = new RectF(left, top, right, bottom);
            canvas.drawArc(rectF, 270, 360, false, mChangeArcPaint);
        }
    }

    private void drawCircle(Canvas canvas) {
        if (mProgress < 1500 && mState != State.ErrorWork) {
            canvas.drawCircle(mCircleX, mCircleY, mRadius, mCirclePaint);
        } else if (mProgress > 1500 && mState == State.StopWork) {
            float number = mProgress - 1500;
            canvas.drawCircle(mCircleX, mCircleY, mRadius, mChangeCirclePaint);
            canvas.drawCircle(mCircleX, mCircleY, mRadius * (1 - number / 500), mCirclePaint);
        }
    }

    private void initData() {
        if (mCircleX == -1)
            mCircleX = (getWidth() - getPaddingLeft() - getPaddingRight()) / 2;
        if (mCircleY == -1)
            mCircleY = (getHeight() - getPaddingTop() - getPaddingBottom()) / 2;
        if (mRectF == null) {
            float left = mCircleX - mRadius * 2f;
            float right = mCircleX + mRadius * 2f;
            float top = mCircleY - mRadius * 2f;
            float bottom = mCircleY + mRadius * 2f;
            mRectF = new RectF(left, top, right, bottom);
        }
    }


    public void stop() {
        mState = State.StopWork;
        ValueAnimator animator = ObjectAnimator.ofFloat(0, 2000);
        animator.setInterpolator(new MyInterpolator());
        animator.setDuration(2000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mProgress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();
    }

    public void init() {
        mState = State.Rest;
        mProgress = 0;
    }

    public void error() {
        mState = State.ErrorWork;
    }

    public void start() {
        mState = State.Work;
    }

    private int dp2px() {
        float v = getContext().getResources().getDisplayMetrics().density;
        return (int) (v * 4 + 0.5f);
    }


    class MyInterpolator implements TimeInterpolator {
        @Override
        public float getInterpolation(float input) {
            return input;
        }
    }

}
