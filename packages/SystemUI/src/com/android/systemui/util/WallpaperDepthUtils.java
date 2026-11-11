/*
 * Copyright (C) 2023-2024 The risingOS Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.systemui.util;

import static com.android.systemui.statusbar.StatusBarState.KEYGUARD;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.MathUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.android.systemui.Dependency;
import com.android.systemui.media.MediaViewController;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.ScrimState;
import com.android.systemui.tuner.TunerService;

public class WallpaperDepthUtils {

    private static final String WALLPAPER_DEPTH_KEY = "system:depth_wallpaper_subject_image_uri";
    private static final String WALLPAPER_DEPTH_ENABLED_KEY = "system:depth_wallpaper_enabled";
    private static final String WALLPAPER_DEPTH_OPACITY_KEY = "system:depth_wallpaper_opacity";
    private static final String WALLPAPER_DEPTH_OFFSET_X_KEY = "system:depth_wallpaper_offset_x";
    private static final String WALLPAPER_DEPTH_OFFSET_Y_KEY = "system:depth_wallpaper_offset_y";
    private static final String WALLPAPER_DEPTH_BOTTOM_FADE_KEY = "system:depth_wallpaper_bottom_fade";
    private static final String WALLPAPER_DEPTH_FADE_CURVE_KEY = "system:depth_wallpaper_fade_curve";
    private static final String WALLPAPER_DEPTH_BOTTOM_INSET_KEY = "system:depth_wallpaper_bottom_inset";

    private static final int DEFAULT_FADE_CURVE_PERCENT = 50;
    private static final int DEFAULT_BOTTOM_FADE_DP = 100;
    private static final int DEFAULT_BOTTOM_INSET_DP = 180;

    private static WallpaperDepthUtils instance;
    private FrameLayout mLockScreenSubject;

    private final Context mContext;
    private final ScrimController mScrimController;
    private final TunerService mTunerService;

    private boolean mDWallpaperEnabled;
    private int mDWallOpacity = 255;
    private String mWallpaperSubjectPath;
    private boolean mDozing;
    private boolean mBouncerShowing;
    private boolean mGlanceableHubShowing;
    private boolean mDynamicBarExpanded;
    private boolean mWallpaperLoaded = false;
    private String mPreviousWallpaperPath;
    private Bitmap mWallpaperBitmap;
    private int mOffsetX;
    private int mOffsetY;
    private int mSubjectScalePercent = 100;
    private int mClockOffsetYDp = 0;
    private int mClockScalePercent = 100;
    private boolean mSmartClockGuard = true;
    private int mAutoClockOffsetYPx = 0;
    private float mAutoClockScale = 1.0f;
    private int mAutoSubjectShiftYPx = 0;
    private boolean mGyroParallaxEnabled = true;
    private SensorManager mSensorManager;
    private Sensor mGravitySensor;
    private boolean mSensorRegistered = false;
    private float mFilteredTiltX = 0f;
    private float mFilteredTiltY = 0f;
    private float mParallaxClockDxPx = 0f;
    private float mParallaxClockDyPx = 0f;
    private boolean mUnlocking;

    private int mBottomFadeDp = DEFAULT_BOTTOM_FADE_DP;
    private int mFadeCurvePercent = DEFAULT_FADE_CURVE_PERCENT;
    private int mBottomInsetDp = DEFAULT_BOTTOM_INSET_DP;

    private WallpaperDepthUtils(Context context, ScrimController scrimController) {
        mContext = context.getApplicationContext();
        mScrimController = scrimController;
        mLockScreenSubject = new FrameLayout(mContext) {
            @Override
            protected void onDetachedFromWindow() {
                super.onDetachedFromWindow();
                WallpaperDepthUtils.this.onDetachedFromWindow();
            }
        };
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -1);
        mLockScreenSubject.setLayoutParams(lp);
        // Make view non-interactive so touches pass through to notifications
        mLockScreenSubject.setClickable(false);
        mLockScreenSubject.setFocusable(false);
        mLockScreenSubject.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        mTunerService = Dependency.get(TunerService.class);
        mTunerService.addTunable(mTunable, WALLPAPER_DEPTH_KEY, 
            WALLPAPER_DEPTH_ENABLED_KEY, WALLPAPER_DEPTH_OPACITY_KEY, 
            WALLPAPER_DEPTH_OFFSET_X_KEY, WALLPAPER_DEPTH_OFFSET_Y_KEY,
            WALLPAPER_DEPTH_BOTTOM_FADE_KEY, WALLPAPER_DEPTH_FADE_CURVE_KEY,
            WALLPAPER_DEPTH_BOTTOM_INSET_KEY);

        android.database.ContentObserver sysObserver = new android.database.ContentObserver(
                new android.os.Handler(android.os.Looper.getMainLooper())) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                reloadFromSystemSettings();
            }
        };
        try {
            android.content.ContentResolver cr = mContext.getContentResolver();
            String[] keys = new String[]{
                "depth_wallpaper_subject_image_uri",
                "depth_wallpaper_enabled",
                "depth_wallpaper_opacity",
                "depth_wallpaper_offset_x",
                "depth_wallpaper_offset_y",
                "depth_wallpaper_scale",
                "depth_wallpaper_clock_offset_y",
                "depth_wallpaper_clock_scale",
                "depth_wallpaper_smart_clock_guard",
                "depth_wallpaper_gyro_parallax",
                "depth_wallpaper_bottom_fade",
                "depth_wallpaper_fade_curve",
                "depth_wallpaper_bottom_inset",
                "depth_wallpaper_auto_subject"
            };
            for (String k : keys) {
                cr.registerContentObserver(Settings.System.getUriFor(k), false, sysObserver,
                        android.os.UserHandle.USER_ALL);
            }
            android.content.BroadcastReceiver wallReceiver = new android.content.BroadcastReceiver() {
                @Override
                public void onReceive(Context ctx, android.content.Intent intent) {
                    try {
                        boolean autoSubj = Settings.System.getIntForUser(
                                mContext.getContentResolver(), "depth_wallpaper_auto_subject", 1,
                                android.os.UserHandle.USER_CURRENT) != 0;
                        if (autoSubj) {
                            new java.io.File("/sdcard/Pictures/blaze_depth_subject.png").delete();
                            new java.io.File("/data/system/users/0/blaze_depth_subject.png").delete();
                            mWallpaperSubjectPath = null;
                            mWallpaperLoaded = false;
                            if (mDWallpaperEnabled) {
                                updateDepthWallpaper(true);
                            }
                        }
                    } catch (Throwable ignored) {}
                }
            };
            mContext.registerReceiver(wallReceiver,
                    new android.content.IntentFilter(android.content.Intent.ACTION_WALLPAPER_CHANGED),
                    Context.RECEIVER_NOT_EXPORTED);
        } catch (Throwable ignored) {}
        reloadFromSystemSettings();
    }

    public void reloadFromSystemSettings() {
        try {
            android.content.ContentResolver cr = mContext.getContentResolver();
            mDWallpaperEnabled = Settings.System.getIntForUser(cr, "depth_wallpaper_enabled", 0,
                    android.os.UserHandle.USER_CURRENT) != 0;
            String path = Settings.System.getStringForUser(cr, "depth_wallpaper_subject_image_uri",
                    android.os.UserHandle.USER_CURRENT);
            if (path == null || path.isEmpty()) {
                java.io.File pubFallback = new java.io.File("/sdcard/Pictures/blaze_depth_subject.png");
                if (pubFallback.exists()) {
                    path = pubFallback.getAbsolutePath();
                } else {
                    java.io.File fallback = new java.io.File("/data/system/users/0/blaze_depth_subject.png");
                    if (fallback.exists()) {
                        path = fallback.getAbsolutePath();
                    }
                }
            }
            mPreviousWallpaperPath = mWallpaperSubjectPath;
            mWallpaperSubjectPath = path;
            int opacity = Settings.System.getIntForUser(cr, "depth_wallpaper_opacity", 100,
                    android.os.UserHandle.USER_CURRENT);
            mDWallOpacity = Math.round(MathUtils.constrain(opacity, 0, 100) * 2.55f);
            mOffsetX = Settings.System.getIntForUser(cr, "depth_wallpaper_offset_x", 0,
                    android.os.UserHandle.USER_CURRENT);
            mOffsetY = Settings.System.getIntForUser(cr, "depth_wallpaper_offset_y", 0,
                    android.os.UserHandle.USER_CURRENT);
            mSubjectScalePercent = MathUtils.constrain(
                    Settings.System.getIntForUser(cr, "depth_wallpaper_scale", 100,
                            android.os.UserHandle.USER_CURRENT), 50, 160);
            mClockOffsetYDp = MathUtils.constrain(
                    Settings.System.getIntForUser(cr, "depth_wallpaper_clock_offset_y", 0,
                            android.os.UserHandle.USER_CURRENT), -160, 160);
            mClockScalePercent = MathUtils.constrain(
                    Settings.System.getIntForUser(cr, "depth_wallpaper_clock_scale", 100,
                            android.os.UserHandle.USER_CURRENT), 65, 135);
            mSmartClockGuard = Settings.System.getIntForUser(cr, "depth_wallpaper_smart_clock_guard", 1,
                    android.os.UserHandle.USER_CURRENT) != 0;
            mGyroParallaxEnabled = Settings.System.getIntForUser(cr, "depth_wallpaper_gyro_parallax", 1,
                    android.os.UserHandle.USER_CURRENT) != 0;
            mBottomFadeDp = Settings.System.getIntForUser(cr, "depth_wallpaper_bottom_fade",
                    DEFAULT_BOTTOM_FADE_DP, android.os.UserHandle.USER_CURRENT);
            mFadeCurvePercent = Settings.System.getIntForUser(cr, "depth_wallpaper_fade_curve",
                    DEFAULT_FADE_CURVE_PERCENT, android.os.UserHandle.USER_CURRENT);
            mBottomInsetDp = Settings.System.getIntForUser(cr, "depth_wallpaper_bottom_inset",
                    DEFAULT_BOTTOM_INSET_DP, android.os.UserHandle.USER_CURRENT);
            updateDepthWallpaper(true);
            applyClockPositionAndScale();
        } catch (Throwable e) {
            Log.e("WallpaperDepthUtils", "reloadFromSystemSettings failed", e);
        }
    }

    public static WallpaperDepthUtils getInstance(Context context, ScrimController scrimController) {
        if (instance == null) {
            instance = new WallpaperDepthUtils(context, scrimController);
        }
        return instance;
    }

    public static WallpaperDepthUtils get() {
        return instance;
    }

    public void onUnlockStarted() {
        mUnlocking = true;
        hideDepthWallpaperImmediate();
    }

    public void onUnlockCancelled() {
        mUnlocking = false;
        updateDepthWallpaperVisibility();
    }

    public void onUnlockCompleted() {
        mUnlocking = false;
        updateGyroParallaxRegistration(false);
        updateDepthWallpaperVisibility();
    }

    public void onScreenTurnedOff() {
        updateGyroParallaxRegistration(false);
    }
    
    public void onDozingChanged(boolean dozing) {
        if (mDozing == dozing) {
            return;
        }
        mDozing = dozing;
        if (mDozing) {
            hideDepthWallpaper();
        } else {
            updateDepthWallpaperVisibility();
        }
    }

    public void onBouncerShowingChanged(boolean showing) {
        if (mBouncerShowing == showing) {
            return;
        }
        mBouncerShowing = showing;
        if (mBouncerShowing) {
            hideDepthWallpaper();
        } else {
            updateDepthWallpaperVisibility();
        }
    }

    public void setDynamicBarExpanded(boolean expanded) {
        if (mDynamicBarExpanded == expanded) return;
        mDynamicBarExpanded = expanded;
        if (expanded) {
            hideDepthWallpaper();
        } else {
            updateDepthWallpaperVisibility();
        }
    }

    public void onGlanceableHubShowingChanged(boolean showing) {
        if (mGlanceableHubShowing == showing) {
            return;
        }
        mGlanceableHubShowing = showing;
        if (mGlanceableHubShowing) {
            hideDepthWallpaper();
        } else {
            updateDepthWallpaperVisibility();
        }
    }

    private final TunerService.Tunable mTunable = new TunerService.Tunable() {
        @Override
        public void onTuningChanged(String key, String newValue) {
            switch (key) {
                case WALLPAPER_DEPTH_ENABLED_KEY:
                    mDWallpaperEnabled = TunerService.parseIntegerSwitch(newValue, false);
                    updateDepthWallpaper(true);
                    break;
                case WALLPAPER_DEPTH_KEY:
                    mPreviousWallpaperPath = mWallpaperSubjectPath;
                    mWallpaperSubjectPath = newValue;
                    updateDepthWallpaper(true);
                    break;
                case WALLPAPER_DEPTH_OPACITY_KEY:
                    int opacity = TunerService.parseInteger(newValue, 100);
                    mDWallOpacity = Math.round(opacity * 2.55f);
                    updateDepthWallpaper(true);
                    break;
                case WALLPAPER_DEPTH_OFFSET_X_KEY:
                    mOffsetX = TunerService.parseInteger(newValue, 0);
                    updateDepthWallpaper(true);
                    break;
                case WALLPAPER_DEPTH_OFFSET_Y_KEY:
                    mOffsetY = TunerService.parseInteger(newValue, 0);
                    updateDepthWallpaper(true);
                    break;
                case WALLPAPER_DEPTH_BOTTOM_FADE_KEY:
                    mBottomFadeDp = TunerService.parseInteger(newValue, DEFAULT_BOTTOM_FADE_DP);
                    applyBottomFade();
                    break;
                case WALLPAPER_DEPTH_FADE_CURVE_KEY:
                    mFadeCurvePercent = TunerService.parseInteger(newValue, DEFAULT_FADE_CURVE_PERCENT);
                    applyFadeCurve();
                    break;
                case WALLPAPER_DEPTH_BOTTOM_INSET_KEY:
                    mBottomInsetDp = TunerService.parseInteger(newValue, DEFAULT_BOTTOM_INSET_DP);
                    applyBottomInset();
                    break;
                default:
                    break;
            }
        }
    };
    
    public void setSubjectAlpha(float subjectAlpha) {
        if (mLockScreenSubject == null) return;
        mLockScreenSubject.post(() -> mLockScreenSubject.setAlpha(subjectAlpha));
    }

    private void applyBottomFade() {
        if (mLockScreenSubject == null) return;
        Drawable background = mLockScreenSubject.getBackground();
        if (background instanceof FadeBottomDrawable) {
            ((FadeBottomDrawable) background).setFadeHeightPx(dpToPx(mBottomFadeDp));
        }
    }

    private void applyBottomInset() {
        if (mLockScreenSubject == null) return;
        Drawable background = mLockScreenSubject.getBackground();
        if (background instanceof FadeBottomDrawable) {
            ((FadeBottomDrawable) background).setBottomInsetPx(dpToPx(mBottomInsetDp));
        }
    }

    private void applyFadeCurve() {
        if (mLockScreenSubject == null) return;
        Drawable background = mLockScreenSubject.getBackground();
        if (background instanceof FadeBottomDrawable) {
            ((FadeBottomDrawable) background).setFadeCurveExponent(computeFadeCurveExponent());
        }
    }

    private float computeFadeCurveExponent() {
        float percent = MathUtils.constrain(mFadeCurvePercent, 0, 100) / 100f;
        return 0.2f + percent * 0.8f;
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        return Math.round(dp * displayMetrics.density);
    }
    
    public void updateDepthWallpaper() {
        updateDepthWallpaper(false);
    }

    public FrameLayout getDepthWallpaperView() {
        return mLockScreenSubject;
    }

    private boolean isDWallpaperEnabled() {
        return mDWallpaperEnabled;
    }

    private boolean canShowDepthWallpaper() {
        ScrimState currentState = mScrimController != null ? mScrimController.getState() : ScrimState.KEYGUARD;
        boolean isKeyguardState = currentState == ScrimState.KEYGUARD
                || ScrimUtils.get().isKeyguardShowing();
        MediaViewController mediaViewController = MediaViewController.getOrNull();
        boolean albumArtVisible = mediaViewController != null && mediaViewController.albumArtVisible();
        // Only show on KEYGUARD state when bouncer is NOT showing
        return mLockScreenSubject != null
                && isDWallpaperEnabled()
                && !mDozing
                && !mBouncerShowing
                && !mGlanceableHubShowing
                && !mDynamicBarExpanded
                && !mUnlocking
                && isKeyguardState
                && mContext.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE
                && !albumArtVisible;
    }

    public void updateDepthWallpaperVisibility() {
        if (mLockScreenSubject == null) return;
        boolean canShow = canShowDepthWallpaper();
        int subjectVisibility = canShow ? View.VISIBLE : View.GONE;
        updateGyroParallaxRegistration(canShow);
        if (mLockScreenSubject.getVisibility() == subjectVisibility) return;
        mLockScreenSubject.post(() -> {
            mLockScreenSubject.setVisibility(subjectVisibility);
            if (subjectVisibility == View.VISIBLE) {
                mLockScreenSubject.invalidate();
            }
        });
    }
    
    public void hideDepthWallpaper() {
        updateGyroParallaxRegistration(false);
        if (mLockScreenSubject == null || mLockScreenSubject.getVisibility() == View.GONE) return;
        mLockScreenSubject.post(() -> mLockScreenSubject.setVisibility(View.GONE));
    }

    public void hideDepthWallpaperImmediate() {
        updateGyroParallaxRegistration(false);
        if (mLockScreenSubject == null) return;
        mLockScreenSubject.post(() -> {
            mLockScreenSubject.animate().cancel();
            mLockScreenSubject.animate()
                    .alpha(0f)
                    .setDuration(120)
                    .withEndAction(() -> {
                        mLockScreenSubject.setVisibility(View.GONE);
                        mLockScreenSubject.setAlpha(1f);
                    })
                    .start();
        });
    }

    private final SensorEventListener mParallaxSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event == null || event.values == null || event.values.length < 2) return;
            // Normalize gravity/accel (-9.8..+9.8 m/s^2) to -1..+1 range
            float normX = MathUtils.constrain(-event.values[0] / 6.5f, -1f, 1f);
            float normY = MathUtils.constrain((event.values[1] - 4.5f) / 6.5f, -1f, 1f);
            mFilteredTiltX += (normX - mFilteredTiltX) * 0.14f;
            mFilteredTiltY += (normY - mFilteredTiltY) * 0.14f;

            float maxSubjDx = dpToPx(9);
            float maxSubjDy = dpToPx(7);
            if (mLockScreenSubject != null) {
                mLockScreenSubject.setTranslationX(mFilteredTiltX * maxSubjDx);
                mLockScreenSubject.setTranslationY(mFilteredTiltY * maxSubjDy);
            }
            mParallaxClockDxPx = -mFilteredTiltX * dpToPx(4);
            mParallaxClockDyPx = -mFilteredTiltY * dpToPx(3);
            applyClockPositionAndScale();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    private void updateGyroParallaxRegistration(boolean shouldRun) {
        boolean wantActive = shouldRun && mGyroParallaxEnabled && isDWallpaperEnabled();
        if (wantActive && !mSensorRegistered) {
            try {
                if (mSensorManager == null) {
                    mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
                }
                if (mSensorManager != null && mGravitySensor == null) {
                    mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
                    if (mGravitySensor == null) {
                        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    }
                }
                if (mSensorManager != null && mGravitySensor != null) {
                    mSensorManager.registerListener(
                            mParallaxSensorListener, mGravitySensor, SensorManager.SENSOR_DELAY_GAME);
                    mSensorRegistered = true;
                }
            } catch (Throwable ignored) {}
        } else if (!wantActive && mSensorRegistered) {
            try {
                if (mSensorManager != null) {
                    mSensorManager.unregisterListener(mParallaxSensorListener);
                }
            } catch (Throwable ignored) {}
            mSensorRegistered = false;
            mFilteredTiltX = 0f;
            mFilteredTiltY = 0f;
            mParallaxClockDxPx = 0f;
            mParallaxClockDyPx = 0f;
            if (mLockScreenSubject != null) {
                mLockScreenSubject.post(() -> {
                    mLockScreenSubject.setTranslationX(0f);
                    mLockScreenSubject.setTranslationY(0f);
                });
            }
            applyClockPositionAndScale();
        }
    }

    public Bitmap getResizedBitmap(Bitmap wallpaperBitmap, float xOffsetDp, float yOffsetDp) {
        Rect displayBounds = mContext.getSystemService(WindowManager.class)
                .getCurrentWindowMetrics()
                .getBounds();
        int screenW = Math.max(1, displayBounds.width());
        int screenH = Math.max(1, displayBounds.height());
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        float xOffsetPx = xOffsetDp * displayMetrics.density;
        float yOffsetPx = yOffsetDp * displayMetrics.density;

        float baseScale = Math.max(
                screenW / (float) wallpaperBitmap.getWidth(),
                screenH / (float) wallpaperBitmap.getHeight());
        float userScale = (mSubjectScalePercent / 100f);

        // First render the center-cropped base bitmap so we can analyze stacked-clock occlusion
        Bitmap baseScreenBitmap = Bitmap.createBitmap(screenW, screenH, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas baseCanvas = new android.graphics.Canvas(baseScreenBitmap);
        android.graphics.Matrix baseMatrix = new android.graphics.Matrix();
        float bw = wallpaperBitmap.getWidth() * baseScale * userScale;
        float bh = wallpaperBitmap.getHeight() * baseScale * userScale;
        float dx = (screenW - bw) * 0.5f + xOffsetPx;
        float dy = (screenH - bh) * 0.5f + yOffsetPx;
        baseMatrix.postScale(baseScale * userScale, baseScale * userScale);
        baseMatrix.postTranslate(dx, dy);
        android.graphics.Paint paint = new android.graphics.Paint(
                android.graphics.Paint.ANTI_ALIAS_FLAG | android.graphics.Paint.FILTER_BITMAP_FLAG);
        baseCanvas.drawBitmap(wallpaperBitmap, baseMatrix, paint);

        // Analyze stacked clock occlusion in the upper-center lockscreen clock zone
        computeSmartClockOcclusionAdjustments(baseScreenBitmap, screenW, screenH);

        if (mAutoSubjectShiftYPx != 0) {
            Bitmap shiftedBitmap = Bitmap.createBitmap(screenW, screenH, Bitmap.Config.ARGB_8888);
            android.graphics.Canvas shiftCanvas = new android.graphics.Canvas(shiftedBitmap);
            android.graphics.Matrix shiftMatrix = new android.graphics.Matrix();
            shiftMatrix.postScale(baseScale * userScale, baseScale * userScale);
            shiftMatrix.postTranslate(dx, dy + mAutoSubjectShiftYPx);
            shiftCanvas.drawBitmap(wallpaperBitmap, shiftMatrix, paint);
            baseScreenBitmap.recycle();
            return shiftedBitmap;
        }
        return baseScreenBitmap;
    }

    /**
     * Scans the foreground subject alpha in the stacked clock zone (y = 12%..46% of screen height).
     * When mSmartClockGuard is enabled and the subject covers > 28% of the lower digit row of a
     * stacked clock, automatically computes an upward clock nudge (mAutoClockOffsetYPx) and a
     * downward subject contour shift (mAutoSubjectShiftYPx) so the bottom half of stacked clocks
     * is never completely buried.
     */
    private void computeSmartClockOcclusionAdjustments(Bitmap subjectBmp, int w, int h) {
        mAutoClockOffsetYPx = 0;
        mAutoClockScale = 1.0f;
        mAutoSubjectShiftYPx = 0;
        if (!mSmartClockGuard || subjectBmp == null) {
            return;
        }
        try {
            int xStart = Math.round(w * 0.22f);
            int xEnd = Math.round(w * 0.78f);
            int yScanTop = Math.round(h * 0.10f);
            int yScanBottom = Math.round(h * 0.52f);
            int stepX = Math.max(4, (xEnd - xStart) / 28);
            int stepY = Math.max(4, (yScanBottom - yScanTop) / 48);

            int ridgeY = -1;
            for (int y = yScanTop; y <= yScanBottom; y += stepY) {
                int opaqueCount = 0;
                int totalSamples = 0;
                for (int x = xStart; x <= xEnd; x += stepX) {
                    totalSamples++;
                    int alpha = Color.alpha(subjectBmp.getPixel(x, y));
                    if (alpha > 48) {
                        opaqueCount++;
                    }
                }
                if (totalSamples > 0 && (opaqueCount / (float) totalSamples) >= 0.16f) {
                    ridgeY = y;
                    break;
                }
            }

            // Stacked clock default bounds when top-biased (verticalBias = 0.14f):
            // Top of hour digits ~ 0.14 * h, bottom of minute digits ~ 0.38 * h.
            // Ideal 3D depth overlap line (where ~24% of bottom minute digits is overlapped): ~ 0.33 * h.
            int idealOverlapY = Math.round(h * 0.33f);
            if (ridgeY > 0 && ridgeY < idealOverlapY) {
                int excessOverlapPx = idealOverlapY - ridgeY;
                int maxClockUpNudgePx = dpToPx(42);
                mAutoClockOffsetYPx = -Math.min(excessOverlapPx / 2, maxClockUpNudgePx);
                int remainingOverlapPx = excessOverlapPx + mAutoClockOffsetYPx;
                if (remainingOverlapPx > 0) {
                    mAutoSubjectShiftYPx = Math.min(remainingOverlapPx, dpToPx(110));
                }
                if (excessOverlapPx > dpToPx(64)) {
                    mAutoClockScale = 0.92f;
                }
                Log.d("WallpaperDepthUtils", "SmartClockGuard: ridgeY=" + ridgeY
                        + " idealY=" + idealOverlapY
                        + " autoClockOffsetYPx=" + mAutoClockOffsetYPx
                        + " autoSubjectShiftYPx=" + mAutoSubjectShiftYPx);
            }
        } catch (Throwable t) {
            Log.w("WallpaperDepthUtils", "computeSmartClockOcclusionAdjustments error", t);
        }
    }

    public void applyClockPositionAndScale() {
        if (mLockScreenSubject == null) return;
        mLockScreenSubject.post(() -> {
            try {
                ViewGroup parent = (ViewGroup) mLockScreenSubject.getParent();
                if (parent == null) return;
                float totalOffsetY = isDWallpaperEnabled()
                        ? (dpToPx(mClockOffsetYDp) + mAutoClockOffsetYPx)
                        : 0f;
                float totalScale = isDWallpaperEnabled()
                        ? ((mClockScalePercent / 100f) * mAutoClockScale)
                        : 1.0f;
                applyClockTransformRecursive(parent, totalOffsetY, totalScale);
            } catch (Throwable ignored) {}
        });
    }

    private void applyClockTransformRecursive(ViewGroup root, float transY, float scale) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child == null || child == mLockScreenSubject) continue;
            String idName = "";
            try {
                if (child.getId() != View.NO_ID) {
                    idName = mContext.getResources().getResourceEntryName(child.getId());
                }
            } catch (Throwable ignored) {}
            String clsName = child.getClass().getSimpleName();
            boolean isClockView = idName.contains("lockscreen_clock_view")
                    || idName.equals("clock_ls")
                    || clsName.contains("ClockStyle")
                    || clsName.contains("AnimatableClockView");
            if (isClockView) {
                child.setTranslationX(isDWallpaperEnabled() ? mParallaxClockDxPx : 0f);
                child.setTranslationY(transY + (isDWallpaperEnabled() ? mParallaxClockDyPx : 0f));
                child.setScaleX(scale);
                child.setScaleY(scale);
            } else if (child instanceof ViewGroup && !idName.contains("notification_stack")) {
                applyClockTransformRecursive((ViewGroup) child, transY, scale);
            }
        }
    }

    public void updateDepthWallpaper(boolean forced) {
        if (mLockScreenSubject == null) return;
        if (!isDWallpaperEnabled()) {
            hideDepthWallpaper();
            return;
        }
        boolean pathChanged = (mPreviousWallpaperPath != null && !mPreviousWallpaperPath.equals(mWallpaperSubjectPath));
        if (!mWallpaperLoaded || pathChanged || forced) {
            Log.d("WallpaperDepthUtils", "updateDepthWallpaper: " + (mWallpaperLoaded || forced ? "update required" : "first load"));
            new LoadWallpaperTask().execute();
            mWallpaperLoaded = true;
            mPreviousWallpaperPath = mWallpaperSubjectPath;
        }
        updateDepthWallpaperVisibility();
    }

    private class LoadWallpaperTask extends AsyncTask<Void, Void, Drawable> {
        @Override
        protected Drawable doInBackground(Void... voids) {
            try {
                Log.d("LoadWallpaperTask", "Wallpaper path: " + mWallpaperSubjectPath);
                Bitmap bitmap = null;
                if (mWallpaperSubjectPath != null && !mWallpaperSubjectPath.isEmpty()) {
                    if (mWallpaperSubjectPath.startsWith("content://") || mWallpaperSubjectPath.startsWith("file://")) {
                        try (java.io.InputStream is = mContext.getContentResolver()
                                .openInputStream(Uri.parse(mWallpaperSubjectPath))) {
                            if (is != null) {
                                bitmap = BitmapFactory.decodeStream(is);
                            }
                        } catch (Throwable ignored) {}
                    }
                    if (bitmap == null) {
                        bitmap = BitmapFactory.decodeFile(mWallpaperSubjectPath);
                    }
                }
                if (bitmap == null) {
                    java.io.File pubFile = new java.io.File("/sdcard/Pictures/blaze_depth_subject.png");
                    if (pubFile.exists()) {
                        bitmap = BitmapFactory.decodeFile(pubFile.getAbsolutePath());
                    }
                }
                if (bitmap == null) {
                    java.io.File fallbackFile = new java.io.File("/data/system/users/0/blaze_depth_subject.png");
                    if (fallbackFile.exists()) {
                        bitmap = BitmapFactory.decodeFile(fallbackFile.getAbsolutePath());
                    }
                }
                if (bitmap == null) {
                    bitmap = extractSubjectFromCurrentWallpaper();
                }
                if (bitmap == null) {
                    Log.d("LoadWallpaperTask", "Failed to decode bitmap from file");
                    return null;
                }
                Bitmap resizedBitmap = getResizedBitmap(bitmap, mOffsetX, mOffsetY);
                if (resizedBitmap == null) {
                    Log.d("LoadWallpaperTask", "Failed to decode resized bitmap from file");
                    return null;
                }
                if (mWallpaperBitmap != null) {
                    mWallpaperBitmap = null;
                }
                mWallpaperBitmap = resizedBitmap;
                Drawable bitmapDrawable = new FadeBottomDrawable(mWallpaperBitmap, dpToPx(mBottomFadeDp));
                ((FadeBottomDrawable) bitmapDrawable).setFadeCurveExponent(computeFadeCurveExponent());
                ((FadeBottomDrawable) bitmapDrawable).setBottomInsetPx(dpToPx(mBottomInsetDp));
                bitmapDrawable.setAlpha(255);
                return bitmapDrawable;
            } catch (OutOfMemoryError e) {
                Log.e("LoadWallpaperTask", "Out of memory error", e);
                return null;
            } catch (Exception e) {
                Log.e("LoadWallpaperTask", "Error loading wallpaper", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            if (drawable == null || mWallpaperBitmap == null) {
                Log.d("LoadWallpaperTask", "decodeFile returned nothing, skipping application of subject as background");
                mWallpaperLoaded = false;
                return;
            }
            if (drawable != null) {
                mLockScreenSubject.setBackground(drawable);
                mLockScreenSubject.getBackground().setAlpha(mDWallOpacity);
                Log.d("LoadWallpaperTask", "Subject Loaded!");
                applyClockPositionAndScale();
                updateDepthWallpaperVisibility();
            } else {
                applyClockPositionAndScale();
                updateDepthWallpaperVisibility();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mWallpaperBitmap = null;
        }
    }
    
    private Bitmap extractSubjectFromCurrentWallpaper() {
        try {
            android.app.WallpaperManager wm = android.app.WallpaperManager.getInstance(mContext);
            Bitmap src = null;
            try {
                android.os.ParcelFileDescriptor pfd = wm.getWallpaperFile(android.app.WallpaperManager.FLAG_LOCK);
                if (pfd == null) {
                    pfd = wm.getWallpaperFile(android.app.WallpaperManager.FLAG_SYSTEM);
                }
                if (pfd != null) {
                    src = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                    pfd.close();
                }
            } catch (Throwable ignored) {}
            if (src == null) {
                try {
                    Drawable d = wm.getDrawable();
                    if (d instanceof BitmapDrawable && ((BitmapDrawable) d).getBitmap() != null) {
                        src = ((BitmapDrawable) d).getBitmap().copy(Bitmap.Config.ARGB_8888, false);
                    }
                } catch (Throwable ignored) {}
            }
            if (src == null) {
                Log.w("WallpaperDepthUtils", "extractSubjectFromCurrentWallpaper: no wallpaper bitmap available");
                return null;
            }
            Rect displayBounds = mContext.getSystemService(WindowManager.class)
                    .getCurrentWindowMetrics().getBounds();
            int dstW = Math.max(1, displayBounds.width());
            int dstH = Math.max(1, displayBounds.height());
            float scale = Math.max(dstW / (float) src.getWidth(), dstH / (float) src.getHeight());
            Bitmap cropped = Bitmap.createBitmap(dstW, dstH, Bitmap.Config.ARGB_8888);
            android.graphics.Canvas c = new android.graphics.Canvas(cropped);
            android.graphics.Matrix m = new android.graphics.Matrix();
            float bw = src.getWidth() * scale;
            float bh = src.getHeight() * scale;
            m.postScale(scale, scale);
            m.postTranslate((dstW - bw) * 0.5f, (dstH - bh) * 0.5f);
            c.drawBitmap(src, m, new android.graphics.Paint(
                    android.graphics.Paint.ANTI_ALIAS_FLAG | android.graphics.Paint.FILTER_BITMAP_FLAG));

            Bitmap segmented = segmentWallpaperForeground(cropped);
            cropped.recycle();
            if (segmented != null) {
                try {
                    java.io.File sysFile = new java.io.File("/data/system/users/0/blaze_depth_subject.png");
                    if (sysFile.getParentFile() != null) sysFile.getParentFile().mkdirs();
                    try (java.io.FileOutputStream out = new java.io.FileOutputStream(sysFile)) {
                        segmented.compress(Bitmap.CompressFormat.PNG, 100, out);
                    }
                    sysFile.setReadable(true, false);
                    mWallpaperSubjectPath = sysFile.getAbsolutePath();
                    mPreviousWallpaperPath = mWallpaperSubjectPath;
                } catch (Throwable fallbackErr) {
                    try {
                        java.io.File cacheFile = new java.io.File(mContext.getFilesDir(), "blaze_depth_subject.png");
                        try (java.io.FileOutputStream out = new java.io.FileOutputStream(cacheFile)) {
                            segmented.compress(Bitmap.CompressFormat.PNG, 100, out);
                        }
                        cacheFile.setReadable(true, false);
                        mWallpaperSubjectPath = cacheFile.getAbsolutePath();
                        mPreviousWallpaperPath = mWallpaperSubjectPath;
                    } catch (Throwable ignored) {}
                }
            }
            return segmented;
        } catch (Throwable t) {
            Log.e("WallpaperDepthUtils", "extractSubjectFromCurrentWallpaper failed", t);
            return null;
        }
    }

    private Bitmap segmentWallpaperForeground(Bitmap source) {
        int origW = source.getWidth();
        int origH = source.getHeight();
        if (origW <= 0 || origH <= 0) return null;
        float scale = Math.min(1f, 360f / (float) origW);
        int w = Math.max(64, Math.round(origW * scale));
        int h = Math.max(64, Math.round(origH * scale));
        Bitmap small = (w != origW || h != origH)
                ? Bitmap.createScaledBitmap(source, w, h, true)
                : source.copy(Bitmap.Config.ARGB_8888, false);

        int[] pixels = new int[w * h];
        small.getPixels(pixels, 0, w, 0, 0, w, h);

        int[] rArr = new int[w * h];
        int[] gArr = new int[w * h];
        int[] bArr = new int[w * h];
        int[] luma = new int[w * h];
        for (int i = 0; i < pixels.length; i++) {
            int c = pixels[i];
            int r = (c >> 16) & 0xFF;
            int g = (c >> 8) & 0xFF;
            int b = c & 0xFF;
            rArr[i] = r;
            gArr[i] = g;
            bArr[i] = b;
            luma[i] = (r * 77 + g * 150 + b * 29) >> 8;
        }

        int[] edgeMag = new int[w * h];
        for (int y = 1; y < h - 1; y++) {
            int row = y * w;
            for (int x = 1; x < w - 1; x++) {
                int idx = row + x;
                int gx = -luma[idx - w - 1] + luma[idx - w + 1]
                        - 2 * luma[idx - 1] + 2 * luma[idx + 1]
                        - luma[idx + w - 1] + luma[idx + w + 1];
                int gy = -luma[idx - w - 1] - 2 * luma[idx - w] - luma[idx - w + 1]
                        + luma[idx + w - 1] + 2 * luma[idx + w] + luma[idx + w + 1];
                edgeMag[idx] = Math.min(255, (Math.abs(gx) + Math.abs(gy)) >> 2);
            }
        }

        boolean[] subjectProtected = new boolean[w * h];
        try {
            Bitmap rgb565 = small.copy(Bitmap.Config.RGB_565, false);
            android.media.FaceDetector.Face[] faces = new android.media.FaceDetector.Face[3];
            int found = new android.media.FaceDetector(w, h, 3).findFaces(rgb565, faces);
            rgb565.recycle();
            android.graphics.PointF mid = new android.graphics.PointF();
            for (int i = 0; i < found; i++) {
                android.media.FaceDetector.Face f = faces[i];
                if (f == null) continue;
                f.getMidPoint(mid);
                float ed = f.eyesDistance();
                float cx = mid.x;
                float cy = mid.y;
                float rx = ed * 1.85f;
                float ryTop = ed * 2.1f;
                float ryBot = (float) h;
                int yStart = Math.max(0, (int) (cy - ryTop));
                int xStart = Math.max(0, (int) (cx - rx * 1.4f));
                int xEnd = Math.min(w, (int) (cx + rx * 1.4f));
                for (int y = yStart; y < h; y++) {
                    for (int x = xStart; x < xEnd; x++) {
                        float nx = (x - cx) / (y > cy + ed ? rx * 1.45f : rx);
                        float ny = (y <= cy) ? (y - cy) / ryTop : (y - cy) / ryBot;
                        if (nx * nx + ny * ny <= 1.0f) {
                            subjectProtected[y * w + x] = true;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}

        boolean[] isBg = new boolean[w * h];
        boolean[] visited = new boolean[w * h];
        java.util.ArrayDeque<Integer> queue = new java.util.ArrayDeque<>(w * h / 2);

        int sideSeedMaxY = (int) (h * 0.48f);
        for (int x = 0; x < w; x++) {
            if (!subjectProtected[x]) {
                visited[x] = true;
                isBg[x] = true;
                queue.add(x);
            }
        }
        for (int y = 1; y < sideSeedMaxY; y++) {
            int leftIdx = y * w;
            int rightIdx = y * w + (w - 1);
            if (!visited[leftIdx] && !subjectProtected[leftIdx]) {
                visited[leftIdx] = true;
                isBg[leftIdx] = true;
                queue.add(leftIdx);
            }
            if (!visited[rightIdx] && !subjectProtected[rightIdx]) {
                visited[rightIdx] = true;
                isBg[rightIdx] = true;
                queue.add(rightIdx);
            }
        }

        int maxFloodY = (int) (h * 0.78f);
        int[] dirs = new int[]{-1, 1, -w, w};
        while (!queue.isEmpty()) {
            int curr = queue.removeFirst();
            int cy = curr / w;
            int cx = curr % w;
            int cr = rArr[curr];
            int cg = gArr[curr];
            int cb = bArr[curr];
            for (int d : dirs) {
                int next = curr + d;
                if (next < 0 || next >= w * h) continue;
                int ny = next / w;
                int nx = next % w;
                if (Math.abs(nx - cx) + Math.abs(ny - cy) != 1) continue;
                if (ny >= maxFloodY || visited[next] || subjectProtected[next]) continue;
                int stepDist = Math.abs(rArr[next] - cr) + Math.abs(gArr[next] - cg) + Math.abs(bArr[next] - cb);
                if (edgeMag[next] < 38 && stepDist < 42) {
                    visited[next] = true;
                    isBg[next] = true;
                    queue.add(next);
                }
            }
        }

        int[] alphaMask = new int[w * h];
        int fgAboveMidCount = 0;
        int midY = (int) (h * 0.45f);
        int topClockClearY = (int) (h * 0.22f);
        for (int y = 0; y < h; y++) {
            int row = y * w;
            for (int x = 0; x < w; x++) {
                int idx = row + x;
                boolean fg = !isBg[idx] && (y >= topClockClearY || subjectProtected[idx]);
                alphaMask[idx] = fg ? 255 : 0;
                if (fg && y >= (h / 10) && y <= midY) {
                    fgAboveMidCount++;
                }
            }
        }

        int minExpectedFg = (w * midY) / 18;
        int maxExpectedFg = (w * midY) * 6 / 10;
        if (fgAboveMidCount < minExpectedFg || fgAboveMidCount > maxExpectedFg) {
            for (int y = 0; y < h; y++) {
                int row = y * w;
                float ny = y / (float) h;
                for (int x = 0; x < w; x++) {
                    float nx = (x / (float) w) - 0.5f;
                    float archY = 0.27f + (nx * nx) * 0.65f;
                    boolean inSubjectZone = ny >= archY;
                    alphaMask[row + x] = inSubjectZone ? 255 : 0;
                }
            }
        }

        int[] smoothed = new int[w * h];
        for (int y = 1; y < h - 1; y++) {
            int row = y * w;
            for (int x = 1; x < w - 1; x++) {
                int idx = row + x;
                int sum = alphaMask[idx - w - 1] + alphaMask[idx - w] + alphaMask[idx - w + 1]
                        + alphaMask[idx - 1] + alphaMask[idx] * 2 + alphaMask[idx + 1]
                        + alphaMask[idx + w - 1] + alphaMask[idx + w] + alphaMask[idx + w + 1];
                smoothed[idx] = sum / 10;
            }
        }

        for (int i = 0; i < pixels.length; i++) {
            int a = MathUtils.constrain(smoothed[i], 0, 255);
            pixels[i] = (a << 24) | (pixels[i] & 0x00FFFFFF);
        }

        Bitmap outSmall = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        outSmall.setPixels(pixels, 0, w, 0, 0, w, h);
        if (small != source) small.recycle();

        if (w != origW || h != origH) {
            Bitmap scaledMaskBmp = Bitmap.createScaledBitmap(outSmall, origW, origH, true);
            outSmall.recycle();
            Bitmap outFull = source.copy(Bitmap.Config.ARGB_8888, true);
            int[] fullPixels = new int[origW * origH];
            int[] maskPixels = new int[origW * origH];
            outFull.getPixels(fullPixels, 0, origW, 0, 0, origW, origH);
            scaledMaskBmp.getPixels(maskPixels, 0, origW, 0, 0, origW, origH);
            scaledMaskBmp.recycle();
            for (int i = 0; i < fullPixels.length; i++) {
                int a = maskPixels[i] >>> 24;
                fullPixels[i] = (a << 24) | (fullPixels[i] & 0x00FFFFFF);
            }
            outFull.setPixels(fullPixels, 0, origW, 0, 0, origW, origH);
            return outFull;
        }
        return outSmall;
    }

    public void onDetachedFromWindow() {
        mTunerService.removeTunable(mTunable);
        mWallpaperBitmap = null;
    }
}
