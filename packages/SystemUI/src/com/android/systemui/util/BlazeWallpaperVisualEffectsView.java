/*
 * Copyright (C) 2026 The BlazeAOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.systemui.util;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.ScrimState;

public class BlazeWallpaperVisualEffectsView extends View {

    private final Context mContext;
    private final ScrimController mScrimController;
    private final Paint mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint mDimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mVignettePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mFrostPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix mDrawMatrix = new Matrix();

    private int mBlurTarget;   // 0=Off, 1=Lockscreen, 2=Homescreen, 3=Both
    private int mBlurType;     // 0=Gaussian, 1=Frosted, 2=Box, 3=Stack
    private int mEffectType;   // 0=Off, 1=Grayscale, 2=Sepia, 3=Vignette, 4=Pixelation, 5=Saturation, 6=Invert, 7=Warm, 8=Cool, 9=Cyberpunk, 10=Vintage, 11=Posterize
    private int mEffectTarget; // 1=Lockscreen, 2=Homescreen, 3=Both
    private int mSaturationLevel;
    private int mPixelationSize;
    private int mVignetteIntensity;
    private int mPosterizeLevels;
    private int mDimTarget;    // 0=Off, 1=Lockscreen, 2=Homescreen, 3=Both
    private int mDimLevel;

    private Bitmap mCachedWallpaper;
    private Bitmap mProcessedBitmap;
    private long mLastWallCheckMs = 0L;

    // Smart Pixels & GameSpace Floating FPS HUD overlays
    private View mSmartPixelsView;
    private TextView mFpsHudView;
    private WindowManager.LayoutParams mFpsHudParams;
    private boolean mFpsRunning = false;
    private int mFrameCount = 0;
    private long mLastFpsSampleNs = 0L;

    private final Choreographer.FrameCallback mFrameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            if (!mFpsRunning || mFpsHudView == null) return;
            mFrameCount++;
            if (mLastFpsSampleNs == 0L) {
                mLastFpsSampleNs = frameTimeNanos;
            } else {
                long elapsedNs = frameTimeNanos - mLastFpsSampleNs;
                if (elapsedNs >= 500_000_000L) {
                    int fps = Math.min(144, Math.max(1, Math.round((mFrameCount * 1_000_000_000f) / elapsedNs)));
                    float frameMs = 1000f / Math.max(1, fps);
                    float tempC = 32.5f;
                    try {
                        Intent batt = mContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                        if (batt != null) {
                            int rawTemp = batt.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 325);
                            tempC = rawTemp / 10f;
                        }
                    } catch (Throwable ignored) {}
                    mFpsHudView.setText(String.format(java.util.Locale.US,
                            "⚡ %d FPS  •  %.1f ms  •  %.1f°C", fps, frameMs, tempC));
                    mFrameCount = 0;
                    mLastFpsSampleNs = frameTimeNanos;
                }
            }
            Choreographer.getInstance().postFrameCallback(this);
        }
    };

    private final ContentObserver mObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mCachedWallpaper = null;
            reloadSettings(true);
            updateSystemOverlays();
        }
    };

    public BlazeWallpaperVisualEffectsView(Context context, ScrimController scrimController) {
        super(context);
        mContext = context;
        mScrimController = scrimController;
        setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);

        try {
            mContext.getContentResolver().registerContentObserver(
                    Settings.System.getUriFor("blaze_wallpaper_effects_version"),
                    false, mObserver, UserHandle.USER_ALL);
            mContext.getContentResolver().registerContentObserver(
                    Settings.Secure.getUriFor("smart_pixel_filter_enabled"),
                    false, mObserver, UserHandle.USER_ALL);
            mContext.getContentResolver().registerContentObserver(
                    Settings.Secure.getUriFor("smart_pixel_filter_percent"),
                    false, mObserver, UserHandle.USER_ALL);
            mContext.getContentResolver().registerContentObserver(
                    Settings.System.getUriFor("gamespace_show_fps"),
                    false, mObserver, UserHandle.USER_ALL);
        } catch (Throwable ignored) {}

        reloadSettings(true);
        updateSystemOverlays();
    }

    private void updateSystemOverlays() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return;

        // 1. Smart Pixels Overlay
        boolean smartPixelsEnabled = Settings.Secure.getIntForUser(
                mContext.getContentResolver(), "smart_pixel_filter_enabled", 0, UserHandle.USER_CURRENT) != 0;
        int smartPixelsPercent = Settings.Secure.getIntForUser(
                mContext.getContentResolver(), "smart_pixel_filter_percent", 25, UserHandle.USER_CURRENT);
        if (smartPixelsEnabled) {
            if (mSmartPixelsView == null) {
                mSmartPixelsView = new View(mContext) {
                    private final Paint mGridPaint = new Paint();
                    @Override
                    protected void onDraw(Canvas c) {
                        super.onDraw(c);
                        int pct = Settings.Secure.getIntForUser(
                                mContext.getContentResolver(), "smart_pixel_filter_percent", 25, UserHandle.USER_CURRENT);
                        Bitmap pat = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888);
                        int offCount = Math.max(1, Math.min(14, Math.round((pct / 100f) * 16f)));
                        int[] coords = {0,0, 2,2, 0,2, 2,0, 1,1, 3,3, 1,3, 3,1, 0,1, 2,3, 1,0, 3,2, 0,3, 2,1, 1,2, 3,0};
                        for (int i = 0; i < offCount && i * 2 + 1 < coords.length; i++) {
                            pat.setPixel(coords[i * 2], coords[i * 2 + 1], Color.BLACK);
                        }
                        mGridPaint.setShader(new BitmapShader(pat, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
                        c.drawRect(0, 0, getWidth(), getHeight(), mGridPaint);
                    }
                };
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_SECURE_SYSTEM_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSLUCENT);
                lp.setTitle("BlazeSmartPixelsOverlay");
                try {
                    wm.addView(mSmartPixelsView, lp);
                } catch (Throwable ignored) {}
            } else {
                mSmartPixelsView.invalidate();
            }
        } else if (mSmartPixelsView != null) {
            try {
                wm.removeView(mSmartPixelsView);
            } catch (Throwable ignored) {}
            mSmartPixelsView = null;
        }

        // 2. GameSpace Floating Live FPS & CPU HUD
        boolean showFps = Settings.System.getIntForUser(
                mContext.getContentResolver(), "gamespace_show_fps", 0, UserHandle.USER_CURRENT) != 0;
        if (showFps) {
            if (mFpsHudView == null) {
                float d = mContext.getResources().getDisplayMetrics().density;
                mFpsHudView = new TextView(mContext);
                mFpsHudView.setTextColor(0xFFE2E8F0);
                mFpsHudView.setTextSize(11.5f);
                mFpsHudView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
                mFpsHudView.setPadding(Math.round(12 * d), Math.round(6 * d), Math.round(12 * d), Math.round(6 * d));
                GradientDrawable bg = new GradientDrawable();
                bg.setColor(0xD80F172A);
                bg.setCornerRadius(999f);
                bg.setStroke(Math.max(1, Math.round(1.2f * d)), 0x6638BDF8);
                mFpsHudView.setBackground(bg);
                mFpsHudView.setText("⚡ 120 FPS  •  8.3 ms  •  32.0°C");

                mFpsHudParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT);
                mFpsHudParams.gravity = Gravity.TOP | Gravity.START;
                mFpsHudParams.x = Math.round(16 * d);
                mFpsHudParams.y = Math.round(48 * d);
                mFpsHudParams.setTitle("BlazeGameSpaceFpsHud");

                mFpsHudView.setOnTouchListener(new OnTouchListener() {
                    private int initX, initY;
                    private float touchX, touchY;
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getActionMasked()) {
                            case MotionEvent.ACTION_DOWN:
                                initX = mFpsHudParams.x;
                                initY = mFpsHudParams.y;
                                touchX = event.getRawX();
                                touchY = event.getRawY();
                                return true;
                            case MotionEvent.ACTION_MOVE:
                                mFpsHudParams.x = initX + Math.round(event.getRawX() - touchX);
                                mFpsHudParams.y = initY + Math.round(event.getRawY() - touchY);
                                try {
                                    wm.updateViewLayout(mFpsHudView, mFpsHudParams);
                                } catch (Throwable ignored) {}
                                return true;
                        }
                        return false;
                    }
                });

                try {
                    wm.addView(mFpsHudView, mFpsHudParams);
                } catch (Throwable ignored) {}
            }
            if (!mFpsRunning) {
                mFpsRunning = true;
                mFrameCount = 0;
                mLastFpsSampleNs = 0L;
                Choreographer.getInstance().postFrameCallback(mFrameCallback);
            }
        } else {
            mFpsRunning = false;
            if (mFpsHudView != null) {
                try {
                    wm.removeView(mFpsHudView);
                } catch (Throwable ignored) {}
                mFpsHudView = null;
            }
        }
    }

    public void reloadSettings(boolean rebuildBitmap) {
        mBlurTarget = SystemProperties.getInt("persist.sys.wallpaper.blur_enabled", 0);
        mBlurType = SystemProperties.getInt("persist.sys.wallpaper.blur_type", 0);
        mEffectType = SystemProperties.getInt("persist.sys.wallpaper.effect_type", 0);
        mEffectTarget = SystemProperties.getInt("persist.sys.wallpaper.effect_target", 1);
        mSaturationLevel = SystemProperties.getInt("persist.sys.wallpaper.saturation_level", 100);
        mPixelationSize = SystemProperties.getInt("persist.sys.wallpaper.pixelation_size", 8);
        mVignetteIntensity = SystemProperties.getInt("persist.sys.wallpaper.vignette_intensity", 50);
        mPosterizeLevels = SystemProperties.getInt("persist.sys.wallpaper.posterize_levels", 8);
        mDimTarget = SystemProperties.getInt("persist.sys.wallpaper.dim_enabled", 0);
        mDimLevel = SystemProperties.getInt("persist.sys.wallpaper.dim_level", 15);

        if (rebuildBitmap) {
            mProcessedBitmap = null;
        }
        applyRenderEffect();
        updateVisibilityState();
        invalidate();
    }

    public static ColorMatrix buildColorMatrixForEffect(int effectType, int saturationLevel, int posterizeLevels) {
        ColorMatrix cm = new ColorMatrix();
        switch (effectType) {
            case 1: // Grayscale
                cm.setSaturation(0f);
                break;
            case 2: // Sepia
                cm.setSaturation(0f);
                ColorMatrix sepia = new ColorMatrix();
                sepia.setScale(1f, 0.92f, 0.76f, 1f);
                cm.postConcat(sepia);
                break;
            case 5: // Saturation
                cm.setSaturation(Math.max(0f, saturationLevel / 100f));
                break;
            case 6: // Invert
                cm.set(new float[]{
                        -1f,  0f,  0f, 0f, 255f,
                         0f, -1f,  0f, 0f, 255f,
                         0f,  0f, -1f, 0f, 255f,
                         0f,  0f,  0f, 1f,   0f
                });
                break;
            case 7: // Warm
                cm.setScale(1.12f, 1.02f, 0.88f, 1f);
                break;
            case 8: // Cool
                cm.setScale(0.88f, 1.0f, 1.15f, 1f);
                break;
            case 9: // Cyberpunk
                cm.setSaturation(1.45f);
                ColorMatrix cyber = new ColorMatrix(new float[]{
                        1.15f, 0f, 0.15f, 0f, 10f,
                        0f, 0.90f, 0.10f, 0f, 0f,
                        0.15f, 0f, 1.25f, 0f, 20f,
                        0f, 0f, 0f, 1f, 0f
                });
                cm.postConcat(cyber);
                break;
            case 10: // Vintage
                cm.setSaturation(0.65f);
                ColorMatrix vintage = new ColorMatrix(new float[]{
                        1.08f, 0f, 0f, 0f, 12f,
                        0f, 1.02f, 0f, 0f, 6f,
                        0f, 0f, 0.85f, 0f, -8f,
                        0f, 0f, 0f, 1f, 0f
                });
                cm.postConcat(vintage);
                break;
            case 11: // Posterize
                float contrast = 1.2f + ((16 - Math.min(16, posterizeLevels)) / 14f) * 0.65f;
                float translate = (-0.5f * contrast + 0.5f) * 255f;
                cm.set(new float[]{
                        contrast, 0, 0, 0, translate,
                        0, contrast, 0, 0, translate,
                        0, 0, contrast, 0, translate,
                        0, 0, 0, 1, 0
                });
                break;
            default:
                break;
        }
        return cm;
    }

    private void applyRenderEffect() {
        if (mEffectType > 0) {
            ColorMatrix cm = buildColorMatrixForEffect(mEffectType, mSaturationLevel, mPosterizeLevels);
            mBitmapPaint.setColorFilter(new ColorMatrixColorFilter(cm));
        } else {
            mBitmapPaint.setColorFilter(null);
        }

        if (mBlurTarget > 0) {
            float radiusPx = switch (mBlurType) {
                case 1 -> 32f;
                case 2 -> 42f;
                case 3 -> 55f;
                default -> 24f;
            };
            RenderEffect blur = RenderEffect.createBlurEffect(radiusPx, radiusPx, Shader.TileMode.MIRROR);
            setRenderEffect(blur);
        } else {
            setRenderEffect(null);
        }
    }

    private Bitmap ensureWallpaperBitmap() {
        long now = System.currentTimeMillis();
        if (mCachedWallpaper != null && (now - mLastWallCheckMs) < 5000L) {
            return mCachedWallpaper;
        }
        mLastWallCheckMs = now;
        try {
            WallpaperManager wm = WallpaperManager.getInstance(mContext);
            Drawable d = wm.getDrawable();
            if (d instanceof BitmapDrawable) {
                mCachedWallpaper = ((BitmapDrawable) d).getBitmap();
            } else if (d != null) {
                int w = Math.max(1, d.getIntrinsicWidth());
                int h = Math.max(1, d.getIntrinsicHeight());
                Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(b);
                d.setBounds(0, 0, w, h);
                d.draw(c);
                mCachedWallpaper = b;
            }
        } catch (Throwable ignored) {}
        return mCachedWallpaper;
    }

    private Bitmap getPixelatedOrBaseBitmap() {
        Bitmap base = ensureWallpaperBitmap();
        if (base == null) return null;
        if (mEffectType == 4 && mPixelationSize >= 2) {
            if (mProcessedBitmap == null) {
                int block = Math.max(2, mPixelationSize);
                int smallW = Math.max(8, base.getWidth() / block);
                int smallH = Math.max(8, base.getHeight() / block);
                Bitmap small = Bitmap.createScaledBitmap(base, smallW, smallH, false);
                mProcessedBitmap = Bitmap.createScaledBitmap(small, base.getWidth(), base.getHeight(), false);
            }
            return mProcessedBitmap;
        }
        return base;
    }

    private boolean matchesTarget(int targetMode, boolean isKeyguard) {
        if (targetMode <= 0) return false;
        if (targetMode == 1) return isKeyguard;
        if (targetMode == 2) return !isKeyguard;
        return true; // 3 = Both
    }

    public void updateVisibilityState() {
        boolean isKeyguard = ScrimUtils.get().isKeyguardShowing()
                || (mScrimController != null && mScrimController.getState() == ScrimState.KEYGUARD);
        boolean activeBlur = matchesTarget(mBlurTarget, isKeyguard);
        boolean activeEffect = mEffectType > 0 && matchesTarget(mEffectTarget, isKeyguard);
        boolean activeDim = matchesTarget(mDimTarget, isKeyguard);

        boolean shouldShow = activeBlur || activeEffect || activeDim;
        int targetVis = shouldShow ? VISIBLE : GONE;
        if (getVisibility() != targetVis) {
            setVisibility(targetVis);
        }
        if (shouldShow) {
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int vw = getWidth();
        int vh = getHeight();
        if (vw <= 0 || vh <= 0) return;

        boolean isKeyguard = ScrimUtils.get().isKeyguardShowing()
                || (mScrimController != null && mScrimController.getState() == ScrimState.KEYGUARD);
        boolean activeBlur = matchesTarget(mBlurTarget, isKeyguard);
        boolean activeEffect = mEffectType > 0 && matchesTarget(mEffectTarget, isKeyguard);
        boolean activeDim = matchesTarget(mDimTarget, isKeyguard);

        if (activeBlur || activeEffect) {
            Bitmap bmp = getPixelatedOrBaseBitmap();
            if (bmp != null && !bmp.isRecycled()) {
                float scale = Math.max((float) vw / bmp.getWidth(), (float) vh / bmp.getHeight());
                float dx = (vw - bmp.getWidth() * scale) * 0.5f;
                float dy = (vh - bmp.getHeight() * scale) * 0.5f;
                mDrawMatrix.reset();
                mDrawMatrix.postScale(scale, scale);
                mDrawMatrix.postTranslate(dx, dy);
                mBitmapPaint.setFilterBitmap(mEffectType != 4);
                canvas.drawBitmap(bmp, mDrawMatrix, mBitmapPaint);
            }

            if (activeBlur && mBlurType == 1) {
                mFrostPaint.setColor(Color.argb(46, 255, 255, 255));
                canvas.drawRect(0, 0, vw, vh, mFrostPaint);
            }

            if (activeEffect && mEffectType == 3 && mVignetteIntensity > 0) {
                float radius = Math.max(vw, vh) * 0.65f;
                int edgeAlpha = Math.min(235, (int) (mVignetteIntensity * 2.35f));
                RadialGradient rg = new RadialGradient(
                        vw * 0.5f, vh * 0.5f, radius,
                        new int[]{Color.TRANSPARENT, Color.argb(edgeAlpha, 0, 0, 0)},
                        new float[]{0.32f, 1.0f},
                        Shader.TileMode.CLAMP);
                mVignettePaint.setShader(rg);
                canvas.drawRect(0, 0, vw, vh, mVignettePaint);
            }
        }

        if (activeDim && mDimLevel > 0) {
            int dimAlpha = Math.min(230, Math.round(mDimLevel * 2.3f));
            mDimPaint.setColor(Color.argb(dimAlpha, 0, 0, 0));
            canvas.drawRect(0, 0, vw, vh, mDimPaint);
        }
    }
}
