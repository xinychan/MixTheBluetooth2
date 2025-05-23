package com.test.assistant.fragments;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import com.test.assistant.R;

import java.util.Random;

import static java.sql.Types.NULL;

/*
 * 说明：可以当成一个正常的View来用，类似于雷达的扫描样式，Radar：雷达，3秒转一次圈
 * 停供调用的方法：start（）：开启扫描；
 *                 stop（）：停止扫描
 *                 setDropNumber(int number)：将会在扫描中添加一些水滴状的被扫描物，添加数会影响扫描数
 * */
public class MyRadarView extends View {

    private int mCircleColor = Color.GREEN;
    private int mCircleWide = 5;
    private int mLineColor = Color.GREEN;
    private int mLineWide = 5;
    private int mCircleDropColor = Color.parseColor("#97C3F3");
    private int mCircleRippleWide = 1;
    private int mCircleRippleColor = Color.parseColor("#97C3F3");
    private float mScanNumber = 270f;
    private boolean mIsScan = false;

    private float mCx = -1, mCy = -1;
    private Random mRandom = new Random(1);

    private boolean mFirstQuadrant = false;//四个象限里出现水滴的判断
    private boolean mSecondQuadrant = false;
    private boolean mThirdQuadrant = false;
    private boolean mFourthQuadrant = false;
    private float mPositionX = -99f;
    private float mPositionY = -99f;

    private int mDropNumber = 0;

    private Paint mCirclePaint, mLinePaint, mCircleDropPaint, mRipplePaint;

    public MyRadarView(Context context) {
        this(context, null);
    }

    public MyRadarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyRadarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获取xml定义的属性值
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MyRadarView);
        mCircleColor = array.getColor(R.styleable.MyRadarView_circleColor, mCircleColor);
        mCircleWide = array.getDimensionPixelSize(R.styleable.MyRadarView_circleWide, mCircleWide);
        mLineColor = array.getColor(R.styleable.MyRadarView_lineColor, mLineColor);
        mLineWide = array.getDimensionPixelSize(R.styleable.MyRadarView_lineWide, mLineWide);
        mCircleDropColor = array.getColor(R.styleable.MyRadarView_circleDropColor, mCircleDropColor);
        mCircleRippleWide = array.getDimensionPixelSize(R.styleable.MyRadarView_circleRippleWide, mCircleRippleWide);
        mCircleRippleColor = array.getColor(R.styleable.MyRadarView_circleRippleColor, mCircleRippleColor);
        array.recycle();

        initPaint();
    }

    //初始化画笔
    private void initPaint() {
        if (mCirclePaint == null) {
            mCirclePaint = new Paint();
            mCirclePaint.setAntiAlias(true);//抗锯齿
            mCirclePaint.setStyle(Paint.Style.STROKE);//画笔空心
            mCirclePaint.setColor(mCircleColor);//设置颜色
            mCirclePaint.setStrokeWidth(mCircleWide);//设置画笔宽度
        }

        if (mCircleDropPaint == null) {
            mCircleDropPaint = new Paint();
            mCircleDropPaint.setAntiAlias(true);
            mCircleDropPaint.setColor(mCircleDropColor);
        }

        if (mRipplePaint == null) {
            mRipplePaint = new Paint();
            mRipplePaint.setColor(mCircleRippleColor);
            mRipplePaint.setAntiAlias(true);
            mRipplePaint.setStrokeWidth(mCircleRippleWide);
            mRipplePaint.setStyle(Paint.Style.STROKE);
        }

        if (mCx != -1 && mCy != -1 && mLinePaint == null) {
            mLinePaint = new Paint();
            mLinePaint.setAntiAlias(true);
            mLinePaint.setStrokeWidth(mLineWide);
            mLinePaint.setColor(mLineColor);
            SweepGradient gradient = new SweepGradient(getWidth() / 2f, getHeight() / 2f,
                    new int[]{NULL, mLineColor}, new float[]{0.8f, 0.9999999999f});
            mLinePaint.setShader(gradient);//设置颜色渐变
        }
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
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        float radius = getHeight() / 2f - mCircleWide / 2f;//圆心f
        //画五个扫描圆
        canvas.drawCircle(cx, cy, radius, mCirclePaint);
        canvas.drawCircle(cx, cy, (float) (0.8 * radius), mCirclePaint);
        canvas.drawCircle(cx, cy, (float) (0.6 * radius), mCirclePaint);
        canvas.drawCircle(cx, cy, (float) (0.4 * radius), mCirclePaint);
        canvas.drawCircle(cx, cy, (float) (0.2 * radius), mCirclePaint);

        //画十字垂直交叉线，交点过圆心
        canvas.drawLine(0, cy, getWidth(), cy, mCirclePaint);
        canvas.drawLine(cx, 0, cx, getHeight(), mCirclePaint);

        mCx = cx;
        mCy = cy;

        drawDrop(canvas, cx, cy, radius);


        if (mIsScan) {//当mIsScan为true时，才开始扫描
            mScanNumber = (mScanNumber + 4) % 360;//这样写的结果是，实现约1.5秒转一圈
            canvas.rotate(mScanNumber, cx, cy);//旋转画布，山不转水转，渐变色的扫描弧不转，就让画布转起来
            if (mLinePaint != null)//首次加载时，画圆弧的笔还没有加载好
                canvas.drawCircle(cx, cy, (float) (radius * 0.96), mLinePaint);
            invalidate();//重绘
        }

    }

    //画水滴及水滴扩散圈
    private void drawDrop(Canvas canvas, float cx, float cy, float radius) {
        if (!mIsScan || mDropNumber <= 0) {
            return;
        }
        getPosition();
        if (mPositionY == -99)
            return;
        float randomX = mPositionX;
        float randomY = mPositionY;
        if (mScanNumber > 315 && mScanNumber <= 358 || mScanNumber >= 0 && mScanNumber < 30) {//第一象限
            canvas.drawCircle(cx * (3 / 2f + randomX), cy * (0.5f + randomY), radius / 25, mCircleDropPaint);
            canvas.drawCircle(cx * (3 / 2f + randomX), cy * (0.5f + randomY), radius / 15, mRipplePaint);
        }
        if (mScanNumber > 45 && mScanNumber < 120) {//第二象限
            canvas.drawCircle(cx * (3 / 2f + randomX), cy * (3 / 2f + randomY), radius / 25, mCircleDropPaint);
            canvas.drawCircle(cx * (3 / 2f + randomX), cy * (3 / 2f + randomY), radius / 15, mRipplePaint);
        }
        if (mScanNumber > 135 && mScanNumber < 210) {//第三象限
            canvas.drawCircle(cx * (0.5f + randomX), cy * (3 / 2f + randomY), radius / 25, mCircleDropPaint);
            canvas.drawCircle(cx * (0.5f + randomX), cy * (3 / 2f + randomY), radius / 15, mRipplePaint);
        }
        if (mScanNumber > 225 && mScanNumber < 300) {//第四象限
            canvas.drawCircle(cx * (0.5f + randomX), cy * (0.5f + randomY), radius / 25, mCircleDropPaint);
            canvas.drawCircle(cx * (0.5f + randomX), cy * (0.5f + randomY), radius / 15, mRipplePaint);
        }
    }

    private synchronized void getPosition() {
        if (mScanNumber == 46) {
            mFirstQuadrant = true;
            mDropNumber--;
        }
        if (mScanNumber == 136) {
            mSecondQuadrant = true;
            mDropNumber--;
        }
        if (mScanNumber == 226) {
            mThirdQuadrant = true;
            mDropNumber--;
        }
        if (mScanNumber == 316) {
            mFourthQuadrant = true;
            mDropNumber--;
        }
        if (mFirstQuadrant) {
            mPositionX = (mRandom.nextInt(5) - 2.5f) / 10f;
            mPositionY = (mRandom.nextInt(5) - 2.5f) / 10f;
            mFirstQuadrant = false;
        }
        if (mSecondQuadrant) {
            mPositionX = (mRandom.nextInt(5) - 2.5f) / 10f;
            mPositionY = (mRandom.nextInt(5) - 2.5f) / 10f;
            mSecondQuadrant = false;
        }
        if (mThirdQuadrant) {
            mPositionX = (mRandom.nextInt(5) - 2.5f) / 10f;
            mPositionY = (mRandom.nextInt(5) - 2.5f) / 10f;
            mThirdQuadrant = false;
        }
        if (mFourthQuadrant) {
            mPositionX = (mRandom.nextInt(5) - 2.5f) / 10f;
            mPositionY = (mRandom.nextInt(5) - 2.5f) / 10f;
            mFourthQuadrant = false;
        }
    }

    public void start() {
        if (!mIsScan) {
            initPaint();
            invalidate();
            mIsScan = true;
        }
    }

    public void stop() {
        if (mIsScan) {
            mIsScan = false;
        }
    }

    public void setDropNumber(int number) {
        mDropNumber += number;
    }

}
