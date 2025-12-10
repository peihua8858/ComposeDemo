package com.android.composedemo.widgets.nested.help;

import android.content.Context;
import android.hardware.SensorManager;
import android.view.ViewConfiguration;

public final class FlingHelper {
    private static final float DECELERATION_RATE = ((float) (Math.log(0.78d) / Math.log(0.9d)));
    private static final float mFlingFriction = ViewConfiguration.getScrollFriction();
    private static float mPhysicalCoeff;
    private final int mMaxFlingVelocity;

    public FlingHelper(Context context) {
        this(context, 0.38f);
    }

    public FlingHelper(Context context, float factor) {
        final float ppi = context.getResources().getDisplayMetrics().density * 160.0f;
        mPhysicalCoeff = SensorManager.GRAVITY_EARTH // g (m/s^2)
                * 39.37f // inch/meter
                * ppi
                * 0.84f; // look and feel tuning
        mMaxFlingVelocity = (int) (ViewConfiguration.get(context).getScaledMaximumFlingVelocity() * factor);
    }

    private double getSplineDeceleration(int i) {
        return Math.log((0.35f * ((float) Math.abs(i))) / (mFlingFriction * mPhysicalCoeff));
    }

    private double getSplineDecelerationByDistance(double d) {
        return ((((double) DECELERATION_RATE) - 1.0d) * Math.log(d / ((double) (mFlingFriction * mPhysicalCoeff)))) / ((double) DECELERATION_RATE);
    }

    public double getSplineFlingDistance(int velocity) {
        final double l = getSplineDeceleration(velocity);
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        return mFlingFriction * mPhysicalCoeff * Math.exp(DECELERATION_RATE / decelMinusOne * l);
    }

    public int getVelocityByDistance(double d) {
        return (int) (((Math.exp(getSplineDecelerationByDistance(d)) * ((double) mFlingFriction)) * ((double) mPhysicalCoeff)) / 0.3499999940395355d);
    }

    public int getSplineFlingDuration(int velocity) {
        final double l = getSplineDeceleration(velocity);
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        return (int) (1000.0 * Math.exp(l / decelMinusOne));
    }

    /* 限制加速的最大值 */
    public int getFlingVelocity(int velocity) {
        return Math.max(-mMaxFlingVelocity, Math.min(velocity, mMaxFlingVelocity));
    }
}
