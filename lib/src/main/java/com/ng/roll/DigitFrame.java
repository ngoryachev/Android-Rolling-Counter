package com.ng.roll;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.ng.roll.library.R;

public class DigitFrame {

    private static float DIGIT_TO_FRAME_RATIO = 0.5f;

    private final Rect mBounds = new Rect();
    private final Paint mFramePaint = new Paint();
    private final Digit[] mDigits = new Digit[5];
    private double mValue = 0.0;
    private final int mSideFrameWidth;
    private final int mColorBlack;

    public DigitFrame(Resources resources) {
        mSideFrameWidth = resources.getDimensionPixelOffset(R.dimen.rolling_side_frame_width);
        mColorBlack = resources.getColor(R.color.rolling_frame_bg_color);
        mFramePaint.setColor(mColorBlack);
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

        final int mainDigitIndex = 2;
        mDigits[mainDigitIndex].setValue(wholePart);
        mDigits[mainDigitIndex].setBounds((int) x1, (int) y1, (int) x2, (int) y2);

        setValueBoundsAndOffsetOfNeighborDigit(mDigits, mainDigitIndex, -2, (int) digitSide);
        setValueBoundsAndOffsetOfNeighborDigit(mDigits, mainDigitIndex, -1, (int) digitSide);
        setValueBoundsAndOffsetOfNeighborDigit(mDigits, mainDigitIndex, 1, (int) digitSide);
        setValueBoundsAndOffsetOfNeighborDigit(mDigits, mainDigitIndex, 2, (int) digitSide);
    }

    private static void setValueBoundsAndOffsetOfNeighborDigit(Digit[] digits, int mainDigitIndex, int offset, int digitSide) {
        Digit mainDigit = digits[mainDigitIndex];
        int digitIndex = mainDigitIndex + offset;
        digits[digitIndex].setValue(roundRobin(mainDigit.getValue(), offset, 10));
        digits[digitIndex].setBounds(mainDigit.getBounds());
        digits[digitIndex].offset(0, offset * digitSide);
    }

    private static float slowRiseFunction(float x) {
        float y = x - 0.6f;
        if (x == 1) {
            return 1;
        }
        return Math.max(y, 0);
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
        mFramePaint.setColor(Color.GRAY);
        canvas.drawRect(mBounds, mFramePaint);
        mFramePaint.setColor(mColorBlack);
        canvas.drawRect(mBounds.left + mSideFrameWidth , mBounds.top, mBounds.right - mSideFrameWidth, mBounds.bottom, mFramePaint);
    }

}
