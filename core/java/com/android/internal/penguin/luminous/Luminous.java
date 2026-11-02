/*
 * Copyright (C) 2026 The PenguinOS Project
 * Copyright (C) 2026 The BlazeAOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.internal.penguin.luminous;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.Nullable;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.SparseArray;
import android.view.RenderNodeAnimator;
import android.view.View;

import java.util.WeakHashMap;

/**
 * @hide
 */
public final class Luminous {

    public static final String SETTING_ANIMATION_ENGINE = "blaze_animation_engine";
    public static final String SETTING_AUTO_LITE_BATTERY_SAVER = "blaze_auto_lite_on_battery_saver";

    public static final int TRANSLATION_X = RenderNodeAnimator.TRANSLATION_X;
    public static final int TRANSLATION_Y = RenderNodeAnimator.TRANSLATION_Y;
    public static final int TRANSLATION_Z = RenderNodeAnimator.TRANSLATION_Z;
    public static final int SCALE_X = RenderNodeAnimator.SCALE_X;
    public static final int SCALE_Y = RenderNodeAnimator.SCALE_Y;
    public static final int ROTATION = RenderNodeAnimator.ROTATION;
    public static final int ALPHA = RenderNodeAnimator.ALPHA;

    private static final WeakHashMap<View, SparseArray<Run>> sRuns = new WeakHashMap<>();
    private static long sLastCheckMs;
    private static boolean sLuminousAllowed = true;

    private Luminous() {
    }

    public static void spring(View view, int property, float target, float stiffness,
            float dampingRatio) {
        spring(view, property, target, stiffness, dampingRatio, Float.NaN, null);
    }

    public static void spring(View view, int property, float target, float stiffness,
            float dampingRatio, float velocity, @Nullable Runnable endAction) {
        long now = SystemClock.uptimeMillis();
        SparseArray<Run> runs = sRuns.get(view);
        if (runs == null) {
            runs = new SparseArray<>();
            sRuns.put(view, runs);
        }

        float from;
        float speed;
        Run old = runs.get(property);
        if (old != null) {
            from = old.value(now);
            speed = Float.isNaN(velocity) ? old.velocity(now) : velocity;
            old.replaced = true;
            old.animator.cancel();
            runs.remove(property);
        } else {
            from = get(view, property);
            speed = Float.isNaN(velocity) ? 0f : velocity;
        }

        float distance = target - from;
        if (Math.abs(distance) < 1e-4f || !canRunOnRenderThread(view)) {
            set(view, property, target);
            if (endAction != null) endAction.run();
            return;
        }

        SpringInterpolator spring = new SpringInterpolator(stiffness, dampingRatio, speed / distance);
        RenderNodeAnimator animator = new RenderNodeAnimator(property, target);
        animator.setTarget(view);
        animator.setStartValue(from);
        animator.setDuration(spring.getDurationMillis());
        animator.setInterpolator(spring);

        Run run = new Run(animator, spring, from, target, now);
        runs.put(property, run);
        SparseArray<Run> owner = runs;
        animator.addListener(new AnimatorListenerAdapter() {
            private boolean mCancelled;

            @Override
            public void onAnimationCancel(Animator animation) {
                mCancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (owner.get(property) == run) owner.remove(property);
                if (!mCancelled && !run.replaced && endAction != null) endAction.run();
            }
        });
        animator.start();
    }

    public static boolean isRunning(View view, int property) {
        SparseArray<Run> runs = sRuns.get(view);
        return runs != null && runs.get(property) != null;
    }

    public static void cancel(View view, int property) {
        SparseArray<Run> runs = sRuns.get(view);
        Run run = runs == null ? null : runs.get(property);
        if (run == null) return;
        float value = run.value(SystemClock.uptimeMillis());
        run.replaced = true;
        run.animator.cancel();
        runs.remove(property);
        set(view, property, value);
    }

    public static void cancelAll(View view) {
        SparseArray<Run> runs = sRuns.get(view);
        if (runs == null) return;
        for (int i = runs.size() - 1; i >= 0; i--) {
            cancel(view, runs.keyAt(i));
        }
    }

    public static boolean isEngineActive(@Nullable Context context) {
        if (context == null) {
            context = android.app.ActivityThread.currentApplication();
        }
        long now = SystemClock.uptimeMillis();
        if (context != null && (now - sLastCheckMs >= 800 || sLastCheckMs == 0)) {
            sLastCheckMs = now;
            try {
                int engine = Settings.Secure.getInt(
                        context.getContentResolver(), SETTING_ANIMATION_ENGINE, 0);
                boolean autoLite = Settings.Secure.getInt(
                        context.getContentResolver(), SETTING_AUTO_LITE_BATTERY_SAVER, 1) != 0;
                PowerManager pm = context.getSystemService(PowerManager.class);
                boolean batterySaver = autoLite && pm != null && pm.isPowerSaveMode();
                sLuminousAllowed = (engine == 0) && !batterySaver;
            } catch (RuntimeException ignored) {
            }
        }
        return sLuminousAllowed;
    }

    public static boolean offloadsViewAnimations(View view) {
        if (!isEngineActive(view.getContext())) return false;
        int mode = SystemProperties.getInt("persist.sys.penguin.luminous", 2);
        if (mode <= 0) return false;
        if (mode >= 2) return true;
        ApplicationInfo info = view.getContext().getApplicationInfo();
        return info != null && (info.isSystemApp() || info.isUpdatedSystemApp());
    }

    private static boolean canRunOnRenderThread(View view) {
        return view.isAttachedToWindow() && view.isHardwareAccelerated();
    }

    private static float get(View view, int property) {
        switch (property) {
            case TRANSLATION_X: return view.getTranslationX();
            case TRANSLATION_Y: return view.getTranslationY();
            case TRANSLATION_Z: return view.getTranslationZ();
            case SCALE_X: return view.getScaleX();
            case SCALE_Y: return view.getScaleY();
            case ROTATION: return view.getRotation();
            case ALPHA: return view.getAlpha();
            default: throw new IllegalArgumentException("Unsupported property " + property);
        }
    }

    private static void set(View view, int property, float value) {
        switch (property) {
            case TRANSLATION_X: view.setTranslationX(value); break;
            case TRANSLATION_Y: view.setTranslationY(value); break;
            case TRANSLATION_Z: view.setTranslationZ(value); break;
            case SCALE_X: view.setScaleX(value); break;
            case SCALE_Y: view.setScaleY(value); break;
            case ROTATION: view.setRotation(value); break;
            case ALPHA: view.setAlpha(value); break;
            default: throw new IllegalArgumentException("Unsupported property " + property);
        }
    }

    private static final class Run {
        final RenderNodeAnimator animator;
        final SpringInterpolator spring;
        final float start;
        final float target;
        final long startMillis;
        boolean replaced;

        Run(RenderNodeAnimator animator, SpringInterpolator spring, float start, float target,
                long startMillis) {
            this.animator = animator;
            this.spring = spring;
            this.start = start;
            this.target = target;
            this.startMillis = startMillis;
        }

        float value(long now) {
            return start + (target - start) * spring.positionAt((now - startMillis) / 1000.0);
        }

        float velocity(long now) {
            return (target - start) * spring.velocityAt((now - startMillis) / 1000.0);
        }
    }
}
