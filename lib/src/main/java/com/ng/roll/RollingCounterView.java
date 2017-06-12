package com.ng.roll;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.ng.roll.library.R;

public class RollingCounterView extends View {

    private static final long ANIMATION_DURATION = 500L;
    Rect mBounds = new Rect();
    DigitFrame[] mDigitFrames = {};
    float digitAspectRatio;
    float currentCounterValue = 0;
    ValueAnimator mRollingAnimator = null;

    public RollingCounterView(Context context) {
        super(context);
        sharedConstructor();
    }

    private void sharedConstructor() {
        TypedValue outValue = new TypedValue();
        getResources().getValue(R.dimen.rolling_digit_aspect_ratio, outValue, true);
        digitAspectRatio = outValue.getFloat();
    }

    public RollingCounterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructor();
    }

    public RollingCounterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        sharedConstructor();
    }

    public void setCounterValue(float value, boolean animate) {
        boolean isAnimationRunning = mRollingAnimator != null && mRollingAnimator.isRunning();
        if (isAnimationRunning) {
            mRollingAnimator.cancel();
        }
        if (animate) {
            startRollingAnimation(value);
        } else {
            setCounterValueInternal(value);
        }
    }

    public void changeCounterBy(int value, boolean animate) {
        setCounterValue((int) (currentCounterValue + value), animate);
    }

    public int getDigitCount() {
        return mDigitFrames != null ? mDigitFrames.length : 0;
    }

    private ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator animation) {
            float value = (Float) animation.getAnimatedValue();
            setCounterValueInternal(value);
        }
    };

    private void setCounterValueInternal(float value) {
        currentCounterValue = value;
        int digitsCount = getDigitsCount((int) value);
        for (int i= mDigitFrames.length-1; i >= 0; i--) {
            if (digitsCount > 0) {
                mDigitFrames[i].setCounterValue(value);
                value = value / 10;
                digitsCount--;
            } else {
                mDigitFrames[i].setCounterValue(0);
            }
        }
        invalidate();
    }

    private static int getDigitsCount(int number) {
        if (number == 0) {
            return 1;
        }

        return (int)(Math.log10(number)+1);
    }

    private void startRollingAnimation(float value) {
        mRollingAnimator = ValueAnimator.ofFloat(currentCounterValue, value);
        mRollingAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mRollingAnimator.setDuration(ANIMATION_DURATION);
        mRollingAnimator.addUpdateListener(updateListener);

        mRollingAnimator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mBounds.set(
                getPaddingLeft(),
                getPaddingTop(),
                w - getPaddingRight(),
                h - getPaddingBottom());

        Rect digitBounds = new Rect();

        float digitWidth = getDigitWidthByHeight(mBounds.height(), digitAspectRatio);
        int digitCount = (int) (mBounds.width() / digitWidth);
        mDigitFrames = new DigitFrame[digitCount];

        for (int i = 0; i < digitCount; i++) {
            mDigitFrames[i] = new DigitFrame();
            mDigitFrames[i].setBounds(computeNthDigitBounds(mBounds, digitBounds, digitWidth, i));
        }

        invalidate();
    }

    private static float getDigitWidthByHeight(float height, float aspectRatio) {
        return height * aspectRatio;
    }

    private static Rect computeNthDigitBounds(Rect bounds, Rect digitBounds, float w, int index) {
        float left = bounds.left + (w * index);
        float right = left + w;
        digitBounds.set((int) left, bounds.top, (int) right, bounds.bottom);

        return digitBounds;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < mDigitFrames.length; i++) {
            DigitFrame digit = mDigitFrames[i];
            digit.onDraw(canvas);
        }
    }

    private static class DigitFrame {

        Rect mBounds = new Rect();
        Paint mFramePaint = new Paint();
        Digit[] digits = new Digit[5];
        double currentCounterValue = 0.0;
        private static float RATIO = 0.5f;

        DigitFrame() {
            mFramePaint.setColor(Color.BLACK);
            mFramePaint.setAntiAlias(true);
            for (int i = 0; i < digits.length; i++) {
                digits[i] = new Digit();
            }
        }

        public void setBounds(Rect bounds) {
            mBounds.set(bounds);
            setDigitsBoundsByCounterValue(currentCounterValue);
        }

        void setDigitsBoundsByCounterValue(double value) {
            float fraction = slowRiseFunction((float) (value % 1));
            int wholePart = (int) (value % 10);

            float frameHeight = mBounds.height();
            float digitSide = frameHeight * RATIO;
            float mainDigitLeft = mBounds.left + (mBounds.width() - digitSide) / 2;

            float digitHeight = digitSide;
            float frameCenter = frameHeight * 0.5f;
            float distFromFrameCenter = digitHeight * fraction * -1;
            float y1 = frameCenter + distFromFrameCenter - (digitHeight * 0.5f);
            float y2 = y1 + digitHeight;

            digits[2].value = wholePart;
            digits[2].setBounds((int) mainDigitLeft, (int) y1, (int) (mainDigitLeft + digitSide), (int) y2);

            digits[0].value = roundRobin(wholePart, -2, 10);
            digits[0].setBounds(digits[2].mBounds);
            digits[0].mBounds.offset(0, (int) (-2 * digitSide));

            digits[1].value = roundRobin(wholePart, -1, 10);
            digits[1].setBounds(digits[2].mBounds);
            digits[1].mBounds.offset(0, (int) -digitSide);

            digits[3].value = roundRobin(wholePart, 1, 10);
            digits[3].setBounds(digits[2].mBounds);
            digits[3].mBounds.offset(0, (int) digitSide);

            digits[4].value = roundRobin(wholePart, 2, 10);
            digits[4].setBounds(digits[2].mBounds);
            digits[4].mBounds.offset(0, (int) ( 2 * digitSide ));
        }

        private static float slowRiseFunction(float x) {
            float y = x * 7 - 6;
            return Math.min(Math.max(y, 0), 1);
        }

        private static int roundRobin(int value, int by, int around) {
            return (value + by + around) % around;
        }

        public void setCounterValue(double value) {
            currentCounterValue = value;
            setDigitsBoundsByCounterValue(value);
        }

        public void onDraw(Canvas canvas) {
            drawBackground(canvas);
            drawDigits(canvas);
        }

        private void drawDigits(Canvas canvas) {
            for (int i = 0; i < digits.length; i++) {
                digits[i].onDraw(canvas);
            }
        }

        private void drawBackground(Canvas canvas) {
            canvas.drawRect(mBounds, mFramePaint);
        }

    }

    private static class Digit {

        Rect mBounds = new Rect();
        Paint mDigitPaint = new Paint();
        int value;

        public Digit() {
            mDigitPaint.setColor(Color.WHITE);
            mDigitPaint.setTextAlign(Paint.Align.CENTER);
            mDigitPaint.setAntiAlias(true);
        }

        public void setBounds(Rect bounds) {
            mBounds.set(bounds);
            mDigitPaint.setTextSize(mBounds.height());
        }

        public void setBounds(int left, int top, int right, int bottom) {
            mBounds.set(left, top, right, bottom);
            mDigitPaint.setTextSize(mBounds.height());
        }

        public void onDraw(Canvas canvas) {
            PointF center = getRectCenter(mBounds);
            drawIntTextAtPoint(canvas, value, center.x, center.y);
        }

        private static PointF getRectCenter(Rect rect) {
            float x = (rect.left + ((rect.right - rect.left) * 0.5f));
            float y = (rect.top + ((rect.bottom - rect.top) * 0.5f));

            return new PointF(x, y);
        }

        private void drawIntTextAtPoint(Canvas canvas, int intToDraw, float x, float y) {
            canvas.drawText(Integer.toString(intToDraw), x, y + (int) (mDigitPaint.getTextSize() * 0.35f), mDigitPaint);
        }

    }
}
