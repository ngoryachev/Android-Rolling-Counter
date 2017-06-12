package com.ng.roll;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class DigitFrame {

    private static float DIGIT_TO_FRAME_RATIO = 0.5f;

    private final Rect mBounds = new Rect();
    private final Paint mFramePaint = new Paint();
    private final Digit[] mDigits = new Digit[5];
    private double mValue = 0.0;

    public DigitFrame() {
        mFramePaint.setColor(Color.BLACK);
        mFramePaint.setAntiAlias(true);
        for (int i = 0; i < mDigits.length; i++) {
            mDigits[i] = new Digit();
        }
    }

    public void setValue(double value) {
        mValue = value;
        setDigitsBoundsByCounterValue(value);
    }

    public void onDraw(Canvas canvas) {
        drawBackground(canvas);
        drawDigits(canvas);
    }

    public void setBounds(Rect bounds) {
        mBounds.set(bounds);
        setDigitsBoundsByCounterValue(mValue);
    }

    private void setDigitsBoundsByCounterValue(double value) {
        float fraction = slowRiseFunction((float) (value % 1));
        int wholePart = (int) (value % 10);

        float frameHeight = mBounds.height();
        float digitSide = frameHeight * DIGIT_TO_FRAME_RATIO;
        float x1 = mBounds.left + (mBounds.width() - digitSide) / 2;
        float x2 = x1 + digitSide;
        float frameCenter = frameHeight * 0.5f;
        float distFromFrameCenter = digitSide * fraction * -1;
        float y1 = frameCenter + distFromFrameCenter - (digitSide * 0.5f);
        float y2 = y1 + digitSide;

        mDigits[2].setValue(wholePart);
        mDigits[2].setBounds((int) x1, (int) y1, (int) x2, (int) y2);

        mDigits[0].setValue(roundRobin(wholePart, -2, 10));
        mDigits[0].setBounds(mDigits[2].getBounds());
        mDigits[0].offset(0, (int) (-2 * digitSide));

        mDigits[1].setValue(roundRobin(wholePart, -1, 10));
        mDigits[1].setBounds(mDigits[2].getBounds());
        mDigits[1].offset(0, (int) -digitSide);

        mDigits[3].setValue(roundRobin(wholePart, 1, 10));
        mDigits[3].setBounds(mDigits[2].getBounds());
        mDigits[3].offset(0, (int) digitSide);

        mDigits[4].setValue(roundRobin(wholePart, 2, 10));
        mDigits[4].setBounds(mDigits[2].getBounds());
        mDigits[4].offset(0, (int) (2 * digitSide));
    }

    private static float slowRiseFunction(float x) {
        float y = x * 7 - 6;
        return Math.min(Math.max(y, 0), 1);
    }

    private static int roundRobin(int value, int by, int around) {
        return (value + by + around) % around;
    }

    private void drawDigits(Canvas canvas) {
        for (int i = 0; i < mDigits.length; i++) {
            mDigits[i].onDraw(canvas);
        }
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawRect(mBounds, mFramePaint);
    }

}
