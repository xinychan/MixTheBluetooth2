package com.test.assistant.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

import com.test.assistant.R;

@SuppressLint("AppCompatCustomView")
public class ChangeColorTextView extends TextView {

    private Paint mOriginalPaint, mChangePaint;

    private float mCurrentProgress = 0.0f;


    public ChangeColorTextView(Context context) {
        this(context, null);
    }

    public ChangeColorTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChangeColorTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ChangeColorTextView);
        int originalColor = array.getColor(R.styleable.ChangeColorTextView_colorTextOriginalColor, Color.BLACK);
        int changeColor = array.getColor(R.styleable.ChangeColorTextView_colorTextChangeColor, Color.RED);
        mChangePaint = setPaint(changeColor, getTextSize());
        mOriginalPaint = setPaint(originalColor, getTextSize());
        array.recycle();
    }

    private Paint setPaint(int color, float size) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(size);
        paint.setColor(color);
        return paint;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int middle = (int) (mCurrentProgress * getWidth());//偏移的位置
        setDrawText(canvas, mChangePaint, 0, middle);
        setDrawText(canvas, mOriginalPaint, middle, getWidth());

    }

    private void setDrawText(Canvas canvas, Paint paint, float start, float end) {
        canvas.save();
        String text = getText().toString();
        canvas.clipRect(start, 0, end, getHeight());
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        int dx = getWidth() / 2 - rect.width() / 2;
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        int dy = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        int basLine = getHeight() / 2 + dy;
        canvas.drawText(text, dx, basLine, paint);
        canvas.restore();
    }


    private void setCurrentProgress(float currentProgress) {
        this.mCurrentProgress = currentProgress;
        invalidate();
    }

    public void start() {
        final ValueAnimator valueAnimator = ObjectAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(1000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentProgress = (float) animation.getAnimatedValue();
                setCurrentProgress(currentProgress);
            }
        });
        valueAnimator.start();
    }

    public void init() {
        mCurrentProgress = 0;
    }

}
