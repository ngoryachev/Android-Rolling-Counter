package com.ng.roll;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.ng.roll.library.R;

public class RollingCounterView extends View {

    private static final long ANIMATION_DURATION = 1000L;
    Rect mBounds = new Rect();
    RollingDigit[] mRollingDigits = {};
    float digitAspectRatio;
    float desiredCounterValue = 0;
    float currentCounterValue = 0;
    ValueAnimator mRollingAnimator = null;

    public RollingCounterView(Context context) {
        super(context);
        sharedConstructor(context);
    }

    private void sharedConstructor(Context context) {
        TypedValue outValue = new TypedValue();
        getResources().getValue(R.dimen.rolling_digit_aspect_ratio, outValue, true);
        digitAspectRatio = outValue.getFloat();
    }

    public RollingCounterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructor(context);
    }

    public RollingCounterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        sharedConstructor(context);
    }

    public void setCounterValue(int value, boolean animate) {
        desiredCounterValue = value;
        if (animate) {
//            startRollingAnimation();
        } else {
            setCounterValueInternal(value);
            desiredCounterValue = value;
        }
    }

    public void changeCounterBy(int value) {
        setCounterValue((int) (currentCounterValue + value), false);
    }

    public int getDigitCount() {
        return mRollingDigits != null ? mRollingDigits.length : 0;
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
        for (int i=mRollingDigits.length-1; i >= 0; i--) {
            if (digitsCount > 0) {
                mRollingDigits[i].setCounterValue(value);
                value = value / 10;
                digitsCount--;
            } else {
                mRollingDigits[i].setCounterValue(0);
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

//    private Animator.AnimatorListener animationListener = new AnimatorListenerAdapter() {
//        @Override
//        public void onAnimationStart(Animator animation) {
//
//        }
//
//        @Override
//        public void onAnimationEnd(Animator animation) {
//            if (Math.abs(desiredCounterValue - currentCounterValue) > 0.05) {
//                startRollingAnimation();
//            }
//        }
//    };

//    private void startRollingAnimation() {
//        mRollingAnimator = ValueAnimator.ofFloat(currentCounterValue, desiredCounterValue);
//        mRollingAnimator.setDuration(ANIMATION_DURATION);
//        mRollingAnimator.addUpdateListener(updateListener);
//        mRollingAnimator.addListener(animationListener);
//
//        mRollingAnimator.start();
//    }

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
        mRollingDigits = new RollingDigit[digitCount];

        for (int i = 0; i < digitCount; i++) {
            mRollingDigits[i] = new RollingDigit(getResources());
            mRollingDigits[i].setBounds(computeNthDigitBounds(mBounds, digitBounds, digitWidth, i));
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

        for (int i = 0; i < mRollingDigits.length; i++) {
            RollingDigit digit = mRollingDigits[i];
            digit.onDraw(canvas);
        }
    }

    private static class RollingDigit {

        Rect mBounds = new Rect();
        Paint mFramePaint = new Paint();
        Paint mDigitPaint = new Paint();

        double currentCounterValue = 0.0;

        RollingDigit(Resources resources) {
            mFramePaint.setColor(Color.GRAY);
            mFramePaint.setAntiAlias(true);

            mDigitPaint.setColor(Color.WHITE);
            mDigitPaint.setTextAlign(Paint.Align.CENTER);
            mFramePaint.setAntiAlias(true);
        }

        public void setBounds(Rect bounds) {
            mBounds.set(bounds);
            mDigitPaint.setTextSize(bounds.height() * 0.8f);
        }

        public void setCounterValue(double value) {
            currentCounterValue = value;
        }

        public void onDraw(Canvas canvas) {
            drawBackground(canvas);
            drawDigit(canvas);
        }

        private void drawDigit(Canvas canvas) {
            PointF center = getRectCenter(mBounds);
            float fraction = (float) (currentCounterValue % 1);
            int wholePart = (int) (currentCounterValue % 10);
            float offsetTop = fraction * mBounds.height();
            float offsetBottom = (1 - fraction) * mBounds.height();
            drawIntTextAtPoint(canvas, wholePart, center.x, center.y - offsetTop);
            drawIntTextAtPoint(canvas, (wholePart + 1) % 10, center.x, center.y + offsetBottom);
        }

        private void drawIntTextAtPoint(Canvas canvas, int intToDraw, float x, float y) {
            canvas.drawText(Integer.toString(intToDraw), x, y + (int)(mDigitPaint.getTextSize() * 0.35f), mDigitPaint);
        }

        private void drawBackground(Canvas canvas) {
            canvas.drawRect(mBounds, mFramePaint);
        }

        //TODO avoid allocations
        private static PointF getRectCenter(Rect rect) {
            float x = (rect.left + ((rect.right - rect.left) * 0.5f));
            float y = (rect.top + ((rect.bottom - rect.top) * 0.5f));

            return new PointF(x, y);
        }

    }
}
