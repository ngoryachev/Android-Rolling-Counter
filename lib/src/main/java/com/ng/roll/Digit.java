package com.ng.roll;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;

public class Digit {

    private static final float TEXT_VERTICAL_CENTRING_RATIO = 0.35f;

    private final Rect mBounds = new Rect();
    private final Paint mDigitPaint = new Paint();

    private int mValue;

    public Digit() {
        mDigitPaint.setColor(Color.WHITE);
        mDigitPaint.setTextAlign(Paint.Align.CENTER);
        mDigitPaint.setAntiAlias(true);
    }

    public Rect getBounds() {
        return mBounds;
    }

    public void setBounds(Rect bounds) {
        mBounds.set(bounds);
        mDigitPaint.setTextSize(mBounds.height());
    }

    public void setBounds(int left, int top, int right, int bottom) {
        mBounds.set(left, top, right, bottom);
        mDigitPaint.setTextSize(mBounds.height());
    }

    public void offset(int dx, int dy) {
        mBounds.offset(dx, dy);
    }

    public void setValue(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }

    public void onDraw(Canvas canvas) {
        PointF center = getRectCenter(mBounds);
        drawIntTextAtPoint(canvas, mValue, center.x, center.y);
    }

    private final PointF mTempPointF = new PointF();

    private PointF getRectCenter(Rect rect) {
        float x = (rect.left + ((rect.right - rect.left) * 0.5f));
        float y = (rect.top + ((rect.bottom - rect.top) * 0.5f));

        mTempPointF.set(x, y);
        return mTempPointF;
    }

    private void drawIntTextAtPoint(Canvas canvas, int intToDraw, float x, float y) {
        canvas.drawText(
                Integer.toString(intToDraw),
                x,
                y + (int) (mDigitPaint.getTextSize() * TEXT_VERTICAL_CENTRING_RATIO), mDigitPaint
        );
    }

}
