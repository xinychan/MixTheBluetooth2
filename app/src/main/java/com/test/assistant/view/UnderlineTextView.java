package com.test.assistant.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.test.assistant.R;


@SuppressLint("AppCompatCustomView")
public class UnderlineTextView extends TextView {

    private Paint mTextPaint, mLinePaint, mChangeTextPaint;

    private int mLineWidth = 10;

    private enum State {Selected, Unselected, Run}

    private State mState = State.Unselected;

    public UnderlineTextView(Context context) {
        this(context, null);
    }

    public UnderlineTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UnderlineTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.UnderlineTextView);
        mLineWidth = array.getDimensionPixelSize(R.styleable.UnderlineTextView_UTextWidth, 10);
        int color = array.getColor(R.styleable.UnderlineTextView_UTextColor, Color.YELLOW);
        array.recycle();
        mTextPaint = setTextPaint(color, getTextSize());
        mChangeTextPaint = setTextPaint(getCurrentTextColor(), getTextSize());
        mLinePaint = setLinePaint(color, mLineWidth);
    }

    private Paint setTextPaint(int color, float size) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(size);
        paint.setColor(color);
        return paint;
    }

    private Paint setLinePaint(int color, float size) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(size);
        paint.setColor(color);
        return paint;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mState == State.Unselected) {
            drawText(canvas, mTextPaint);
        } else if (mState == State.Selected) {
            drawText(canvas, mChangeTextPaint);
            drawLine(canvas);
        }
    }

    private void drawLine(Canvas canvas) {
        canvas.drawLine(0, getHeight() - mLineWidth / 2f, getWidth(), getHeight() - mLineWidth / 2f, mLinePaint);
    }

    private void drawText(Canvas canvas, Paint paint) {
        String text = getText().toString().trim();
        int textWidth = (int) paint.measureText(text);
        int dx = getWidth() / 2 - textWidth / 2;
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        int dy = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        int basLine = getHeight() / 2 + dy;
        canvas.drawText(text, dx, basLine, paint);
    }

    public void setState() {
        if (mState == State.Unselected) {
            mState = State.Selected;
        } else if (mState == State.Selected) {
            mState = State.Unselected;
        }
        invalidate();
    }

}
