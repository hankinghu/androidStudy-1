package com.style.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.style.lib.media.video.R;

/**
 * 自定义通知提醒view(小圆点)
 */
public class HorizontalProgressBar extends View {
    private static final String TAG = "HorizontalProgressBar";
    /**
     * 进度颜色
     */
    private int mProgressColor = Color.GREEN;

    private Paint mPaint;
    private int progress;

    public HorizontalProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalProgressBar(Context context) {
        this(context, null);
    }

    public HorizontalProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.HorizontalProgressBar, defStyle, 0);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.HorizontalProgressBar_horizontalProgressColor) {
                mProgressColor = a.getColor(attr, mProgressColor);
            } else if (attr == R.styleable.HorizontalProgressBar_horizontalProgress) {
                progress = a.getInteger(attr, progress);
            }
        }
        a.recycle();
        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);//设置填满
        mPaint.setColor(mProgressColor);
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
    }

    private float sp2px(int sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    public void setProgress(int progress) {
        if (progress >= 0 && progress <= 100) {
            this.progress = progress;
            invalidate();
        }
    }

    public void setCacheColor(int color) {
        invalidate();
    }

    public void setProgressColor(int color) {
        mProgressColor = color;
        mPaint.setColor(mProgressColor);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e(TAG, "onMeasure");
        invalidate();

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.e(TAG, "onLayout");
        invalidate();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e(TAG, "onDraw");

        int progressRight = getRight() * progress / 100;
        //注意：坐标值始终是相对自己的位置
        canvas.drawRect(0, 0, progressRight, getBottom(), mPaint);

    }
}
