package com.ng.roll;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

    private double mValue = 0.0;
    private DigitFrame[] mDigitFrames = {};
    private ValueAnimator mRollingAnimator = null;
    private Paint mFramePaint = new Paint();

    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator animation) {
            double value = (Double) animation.getAnimatedValue();
            setValueInternal(value);
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
        int frameWidth = getResources().getDimensionPixelOffset(R.dimen.rolling_counter_frame_width);
        mFramePaint.setStyle(Paint.Style.STROKE);
        mFramePaint.setColor(Color.BLACK);
        mFramePaint.setStrokeWidth(frameWidth);
    }

    public void setCounterValue(double value, boolean animate) {
        boolean isAnimationRunning = mRollingAnimator != null && mRollingAnimator.isRunning();
        if (isAnimationRunning) {
            mRollingAnimator.cancel();
        }
        if (animate) {
            startRollingAnimation(value);
        } else {
            setValueInternal(value);
        }
    }

    public void changeCounterBy(int value, boolean animate) {
        setCounterValue(mValue + value, animate);
    }

    public int getDigitCount() {
        return mDigitFrames != null ? mDigitFrames.length : 0;
    }

    private void setValueInternal(double value) {
        mValue = value;
        long digitsCount = getDigitsCount((long) value);
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

    private static long getDigitsCount(long number) {
        if (number == 0) {
            return 1;
        }

        return (long) (Math.log10(number) + 1);
    }

    private void startRollingAnimation(double value) {
        mRollingAnimator = new ValueAnimator();
        mRollingAnimator.setObjectValues(mValue, value);
        mRollingAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mRollingAnimator.setDuration(ANIMATION_DURATION);
        mRollingAnimator.addUpdateListener(mAnimatorUpdateListener);
        mRollingAnimator.setEvaluator(new TypeEvaluator<Double>() {
            @Override
            public Double evaluate(float fraction, Double startValue, Double endValue) {
                return (startValue + (endValue - startValue) * fraction);
            }
        });
        mRollingAnimator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mBounds.set(
                getPaddingLeft(),
                getPaddingTop(),
                w - getPaddingRight(),
                h - getPaddingBottom());

        final Rect tempDigitBounds = new Rect();

        float digitWidth = getDigitWidthByHeight(mBounds.height(), mDigitAspectRatio);
        int digitCount = (int) (mBounds.width() / digitWidth);
        int xOffset = (int) ((mBounds.width() - (digitWidth * digitCount)) * 0.5);
        mDigitFrames = new DigitFrame[digitCount];

        for (int i = 0; i < digitCount; i++) {
            mDigitFrames[i] = new DigitFrame(getResources());
            mDigitFrames[i].setBounds(computeNthDigitBounds(mBounds, tempDigitBounds, digitWidth, i, xOffset));
        }

        invalidate();
    }

    private static float getDigitWidthByHeight(float height, float aspectRatio) {
        return height * aspectRatio;
    }

    private static Rect computeNthDigitBounds(Rect externalBounds, Rect digitBounds, float w, int index, int xOffset) {
        float left = externalBounds.left + (w * index) + xOffset;
        float right = left + w;
        digitBounds.set((int) left, externalBounds.top, (int) right, externalBounds.bottom);

        return digitBounds;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < mDigitFrames.length; i++) {
            DigitFrame digit = mDigitFrames[i];
            digit.onDraw(canvas);
        }
        canvas.drawRect(mBounds, mFramePaint);
    }


}
