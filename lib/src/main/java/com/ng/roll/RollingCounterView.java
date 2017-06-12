package com.ng.roll;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.ng.roll.library.R;

class RollingCounterView extends View {

    private static final long ANIMATION_DURATION = 500L;

    private final Rect mBounds = new Rect();
    private float mDigitAspectRatio;

    private double mValue = 0;
    private DigitFrame[] mDigitFrames = {};
    private ValueAnimator mRollingAnimator = null;

    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator animation) {
            float value = (Float) animation.getAnimatedValue();
            setCounterValueInternal(value);
        }
    };

    public RollingCounterView(Context context) {
        super(context);
        sharedConstructor();
    }

    public RollingCounterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructor();
    }

    public RollingCounterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        sharedConstructor();
    }

    private void sharedConstructor() {
        TypedValue outValue = new TypedValue();
        getResources().getValue(R.dimen.rolling_digit_aspect_ratio, outValue, true);
        mDigitAspectRatio = outValue.getFloat();
    }

    public void setCounterValue(double value, boolean animate) {
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
        setCounterValue((int) (mValue + value), animate);
    }

    public int getDigitCount() {
        return mDigitFrames != null ? mDigitFrames.length : 0;
    }

    private void setCounterValueInternal(double value) {
        mValue = value;
        int digitsCount = getDigitsCount((int) value);
        for (int i = mDigitFrames.length - 1; i >= 0; i--) {
            if (digitsCount > 0) {
                mDigitFrames[i].setValue(value);
                value = value / 10;
                digitsCount--;
            } else {
                mDigitFrames[i].setValue(0);
            }
        }
        invalidate();
    }

    private static int getDigitsCount(int number) {
        if (number == 0) {
            return 1;
        }

        return (int) (Math.log10(number) + 1);
    }

    private void startRollingAnimation(double value) {
        mRollingAnimator = ValueAnimator.ofFloat((float) mValue, (float) value);
        mRollingAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mRollingAnimator.setDuration(ANIMATION_DURATION);
        mRollingAnimator.addUpdateListener(mAnimatorUpdateListener);

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

        float digitWidth = getDigitWidthByHeight(mBounds.height(), mDigitAspectRatio);
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


}
