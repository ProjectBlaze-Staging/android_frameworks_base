/*
 * Copyright (C) 2026 The PenguinOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.internal.penguin.luminous;

import android.graphics.animation.HasNativeInterpolator;
import android.graphics.animation.NativeInterpolator;
import android.graphics.animation.NativeInterpolatorFactory;
import android.view.animation.Interpolator;

/**
 * @hide
 */
@HasNativeInterpolator
public final class SpringInterpolator implements Interpolator, NativeInterpolator {

    private static final double SETTLE_POSITION = 0.002;
    private static final double SETTLE_VELOCITY = 0.02;
    private static final double MAX_SECONDS = 3.0;
    private static final double STEP_SECONDS = 1.0 / 240.0;

    private final double mOmega;
    private final double mDampingRatio;
    private final double mVelocity;
    private final double mDuration;

    public SpringInterpolator(float stiffness, float dampingRatio, float velocity) {
        mOmega = Math.sqrt(Math.max(stiffness, 0.0001f));
        mDampingRatio = Math.max(dampingRatio, 0f);
        mVelocity = velocity;
        mDuration = settleSeconds();
    }

    public long getDurationMillis() {
        return Math.max(1, Math.round(mDuration * 1000));
    }

    public float positionAt(double seconds) {
        if (seconds >= mDuration) return 1f;
        return (float) (1 + displacement(Math.max(seconds, 0)));
    }

    public float velocityAt(double seconds) {
        if (seconds >= mDuration) return 0f;
        double t = Math.max(seconds, 0);
        double h = 1e-4;
        return (float) ((displacement(t + h) - displacement(Math.max(t - h, 0))) / (t < h ? h : 2 * h));
    }

    @Override
    public float getInterpolation(float input) {
        return input >= 1f ? 1f : positionAt(input * mDuration);
    }

    @Override
    public long createNativeInterpolator() {
        return NativeInterpolatorFactory.createSpringInterpolator((float) (mOmega * mOmega),
                (float) mDampingRatio, (float) mVelocity, (float) mDuration);
    }

    private double displacement(double t) {
        double w = mOmega;
        double z = mDampingRatio;
        double d0 = -1;
        double v0 = mVelocity;
        if (z < 1) {
            double wd = w * Math.sqrt(1 - z * z);
            return Math.exp(-z * w * t) * (d0 * Math.cos(wd * t) + (v0 + z * w * d0) / wd * Math.sin(wd * t));
        }
        if (z == 1) {
            return Math.exp(-w * t) * (d0 + (v0 + w * d0) * t);
        }
        double root = Math.sqrt(z * z - 1);
        double r1 = -w * (z - root);
        double r2 = -w * (z + root);
        double c2 = (v0 - r1 * d0) / (r2 - r1);
        double c1 = d0 - c2;
        return c1 * Math.exp(r1 * t) + c2 * Math.exp(r2 * t);
    }

    private double settleSeconds() {
        double settledFor = 0;
        double prev = displacement(0);
        for (double t = STEP_SECONDS; t < MAX_SECONDS; t += STEP_SECONDS) {
            double d = displacement(t);
            double v = (d - prev) / STEP_SECONDS;
            prev = d;
            if (Math.abs(d) < SETTLE_POSITION && Math.abs(v) < SETTLE_VELOCITY) {
                settledFor += STEP_SECONDS;
                if (settledFor >= 2 * STEP_SECONDS) return t;
            } else {
                settledFor = 0;
            }
        }
        return MAX_SECONDS;
    }
}
