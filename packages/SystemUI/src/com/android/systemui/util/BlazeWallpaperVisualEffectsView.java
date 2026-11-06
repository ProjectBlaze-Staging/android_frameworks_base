/*
 * Copyright (C) 2026 The BlazeAOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.systemui.util;

import android.app.WallpaperManager;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.ScrimState;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlazeWallpaperVisualEffectsView extends View {

    private final Context mContext;
    private final ScrimController mScrimController;
    private final Paint mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint mDimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mVignettePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mFrostPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mGoodLockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mSignaturePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mEdgeRect = new RectF();
    private final Matrix mDrawMatrix = new Matrix();

    // Good Lock Make Up & Life Up state (Stage 4 & Stage 5)
    private String mLockStarSignatureText = "";
    private int mEdgeLightingStyle = 2;       // 0=Off, 1=Eclipse, 2=Cyber Neon, 3=Aurora, 4=Prism
    private long mEdgeLightingPulseUntilMs = 0L;
    private int mWonderlandParticleMode = 2;  // 0=Off, 1=Sakura, 2=Fireflies, 3=Stardust, 4=Snow
    private boolean mBehavioralAuraEnabled = true;

    private View mSmartSidebarHandleView;
    private View mSmartSidebarPanelView;
    private boolean mTorchActive = false;

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
            mContext.getContentResolver().registerContentObserver(
                    Settings.Secure.getUriFor("blaze_lockstar_signature_text"),
                    false, mObserver, UserHandle.USER_ALL);
            mContext.getContentResolver().registerContentObserver(
                    Settings.Secure.getUriFor("blaze_edgelighting_style"),
                    false, mObserver, UserHandle.USER_ALL);
            mContext.getContentResolver().registerContentObserver(
                    Settings.Secure.getUriFor("blaze_edgelighting_preview_pulse"),
                    false, mObserver, UserHandle.USER_ALL);
            mContext.getContentResolver().registerContentObserver(
                    Settings.Secure.getUriFor("blaze_wonderland_particle_mode"),
                    false, mObserver, UserHandle.USER_ALL);
            mContext.getContentResolver().registerContentObserver(
                    Settings.Secure.getUriFor("blaze_behavioral_wallpaper_aura"),
                    false, mObserver, UserHandle.USER_ALL);
            mContext.getContentResolver().registerContentObserver(
                    Settings.Secure.getUriFor("blaze_smart_sidebar_enabled"),
                    false, mObserver, UserHandle.USER_ALL);
            mContext.getContentResolver().registerContentObserver(
                    Settings.Secure.getUriFor("blaze_oho_enabled"),
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

        // 3. ColorOS 17 Smart Sidebar / File Dock + One Hand Operation+ Edge Handle (Stage 5)
        updateSmartSidebarOverlay(wm);
    }

    private void updateSmartSidebarOverlay(final WindowManager wm) {
        boolean sidebarEnabled = Settings.Secure.getIntForUser(
                mContext.getContentResolver(), "blaze_smart_sidebar_enabled", 1, UserHandle.USER_CURRENT) != 0;
        boolean ohoEnabled = Settings.Secure.getIntForUser(
                mContext.getContentResolver(), "blaze_oho_enabled", 1, UserHandle.USER_CURRENT) != 0;

        if (sidebarEnabled || ohoEnabled) {
            if (mSmartSidebarHandleView == null) {
                final float d = mContext.getResources().getDisplayMetrics().density;
                mSmartSidebarHandleView = new View(mContext);
                GradientDrawable handleBg = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{0x9938BDF8, 0x99A855F7});
                handleBg.setCornerRadius(99f);
                handleBg.setStroke(Math.max(1, Math.round(1f * d)), 0x66FFFFFF);
                mSmartSidebarHandleView.setBackground(handleBg);

                final WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                        Math.round(5.5f * d),
                        Math.round(78f * d),
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT);
                lp.gravity = Gravity.TOP | Gravity.END;
                lp.x = Math.round(1.5f * d);
                lp.y = Math.round(230f * d);
                lp.setTitle("BlazeSmartSidebarHandle");

                mSmartSidebarHandleView.setOnTouchListener(new OnTouchListener() {
                    private float startX, startY;
                    private boolean triggered;

                    @Override
                    public boolean onTouch(View v, MotionEvent ev) {
                        switch (ev.getActionMasked()) {
                            case MotionEvent.ACTION_DOWN:
                                startX = ev.getRawX();
                                startY = ev.getRawY();
                                triggered = false;
                                return true;
                            case MotionEvent.ACTION_MOVE:
                                if (triggered) return true;
                                float dx = ev.getRawX() - startX;
                                float dy = ev.getRawY() - startY;
                                float dist = (float) Math.hypot(dx, dy);
                                if (dist > 24f * d) {
                                    triggered = true;
                                    vibrateTick();
                                    boolean oho = Settings.Secure.getIntForUser(
                                            mContext.getContentResolver(), "blaze_oho_enabled", 1, UserHandle.USER_CURRENT) != 0;
                                    if (oho && dy < -20f * d) {
                                        // Upward Diagonal Swipe (↗)
                                        int act = Settings.Secure.getIntForUser(
                                                mContext.getContentResolver(), "blaze_oho_diagonal_up_action", 0, UserHandle.USER_CURRENT);
                                        executeOhoAction(act, wm);
                                    } else if (oho && dy > 20f * d) {
                                        // Downward Diagonal Swipe (↘)
                                        int act = Settings.Secure.getIntForUser(
                                                mContext.getContentResolver(), "blaze_oho_diagonal_down_action", 1, UserHandle.USER_CURRENT);
                                        executeOhoAction(act, wm);
                                    } else {
                                        // Horizontal Pull: Open Smart Sidebar & File Dock
                                        toggleSmartSidebarPanel(wm);
                                    }
                                }
                                return true;
                            case MotionEvent.ACTION_UP:
                                if (!triggered) {
                                    vibrateTick();
                                    toggleSmartSidebarPanel(wm);
                                }
                                return true;
                        }
                        return false;
                    }
                });

                try {
                    wm.addView(mSmartSidebarHandleView, lp);
                } catch (Throwable ignored) {}
            }
        } else {
            if (mSmartSidebarHandleView != null) {
                try {
                    wm.removeView(mSmartSidebarHandleView);
                } catch (Throwable ignored) {}
                mSmartSidebarHandleView = null;
            }
            dismissSmartSidebarPanel(wm);
        }
    }

    private void executeOhoAction(int actionCode, WindowManager wm) {
        switch (actionCode) {
            case 0: // Smart Sidebar & File Dock
                toggleSmartSidebarPanel(wm);
                break;
            case 1: // Expand Notifications
            case 2: // Expand Quick Settings
                try {
                    Object sbService = mContext.getSystemService("statusbar");
                    if (sbService != null) {
                        java.lang.reflect.Method m = sbService.getClass().getMethod(
                                actionCode == 2 ? "expandSettingsPanel" : "expandNotificationsPanel");
                        m.invoke(sbService);
                    }
                } catch (Throwable ignored) {}
                break;
            case 3: // Toggle Flashlight (Torch)
                toggleTorch();
                break;
            case 4: // Volume UI / Quick Action
            default:
                toggleSmartSidebarPanel(wm);
                break;
        }
    }

    private void toggleTorch() {
        try {
            CameraManager cm = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            if (cm != null && cm.getCameraIdList().length > 0) {
                mTorchActive = !mTorchActive;
                cm.setTorchMode(cm.getCameraIdList()[0], mTorchActive);
                Toast.makeText(mContext, mTorchActive ? "🔦 El Feneri Açıldı" : "🔦 El Feneri Kapatıldı", Toast.LENGTH_SHORT).show();
            }
        } catch (Throwable ignored) {}
    }

    private void dismissSmartSidebarPanel(WindowManager wm) {
        if (mSmartSidebarPanelView != null && wm != null) {
            try {
                wm.removeView(mSmartSidebarPanelView);
            } catch (Throwable ignored) {}
            mSmartSidebarPanelView = null;
        }
    }

    private void toggleSmartSidebarPanel(final WindowManager wm) {
        if (mSmartSidebarPanelView != null) {
            dismissSmartSidebarPanel(wm);
            return;
        }
        final float d = mContext.getResources().getDisplayMetrics().density;
        LinearLayout panel = new LinearLayout(mContext);
        panel.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xF00F172A, 0xF01E1B4B});
        bg.setCornerRadius(24f * d);
        bg.setStroke(Math.max(1, Math.round(1.2f * d)), 0x6638BDF8);
        panel.setBackground(bg);
        int pad = Math.round(14f * d);
        panel.setPadding(pad, pad, pad, pad);

        TextView header = new TextView(mContext);
        header.setText("🗂️ Smart Sidebar & File Dock");
        header.setTextColor(Color.WHITE);
        header.setTextSize(13f);
        header.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        panel.addView(header);

        // Clipboard & Smart OTP Detector
        String clipText = "";
        try {
            ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null && cm.hasPrimaryClip()) {
                ClipData cd = cm.getPrimaryClip();
                if (cd != null && cd.getItemCount() > 0 && cd.getItemAt(0).getText() != null) {
                    clipText = cd.getItemAt(0).getText().toString().trim();
                }
            }
        } catch (Throwable ignored) {}

        String otpCode = null;
        if (!TextUtils.isEmpty(clipText)) {
            Matcher m = Pattern.compile("\\b(\\d{4,8})\\b").matcher(clipText);
            if (m.find()) {
                otpCode = m.group(1);
            }
        }

        if (otpCode != null) {
            final String foundOtp = otpCode;
            TextView otpChip = createSidebarPill("🔑 OTP Kodu: " + foundOtp + " (Kopyala)", 0xFF10B981, d);
            otpChip.setOnClickListener(v -> {
                vibrateTick();
                ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null) {
                    cm.setPrimaryClip(ClipData.newPlainText("OTP", foundOtp));
                    Toast.makeText(mContext, "OTP Kodu kopyalandı: " + foundOtp, Toast.LENGTH_SHORT).show();
                }
                dismissSmartSidebarPanel(wm);
            });
            panel.addView(otpChip);
        }

        TextView clipBox = new TextView(mContext);
        clipBox.setText(TextUtils.isEmpty(clipText)
                ? "📌 File Dock Pano: Boş"
                : ("📌 Pano: " + (clipText.length() > 42 ? clipText.substring(0, 42) + "…" : clipText)));
        clipBox.setTextColor(0xFFCBD5E1);
        clipBox.setTextSize(11f);
        clipBox.setPadding(0, Math.round(6 * d), 0, Math.round(8 * d));
        panel.addView(clipBox);

        // Quick Action Buttons
        TextView btnGoodLock = createSidebarPill("✨ Good Lock Studio", 0xFFA855F7, d);
        btnGoodLock.setOnClickListener(v -> {
            vibrateTick();
            dismissSmartSidebarPanel(wm);
            launchSettingsSubFragment("com.blaze.house.fragments.BlazeGoodLockStudioFragment", "Blaze Good Lock Studio");
        });
        panel.addView(btnGoodLock);

        TextView btnBattery = createSidebarPill("🔋 Pil Sağlığı & Termal Stüdyo", 0xFF38BDF8, d);
        btnBattery.setOnClickListener(v -> {
            vibrateTick();
            dismissSmartSidebarPanel(wm);
            launchSettingsSubFragment("com.blaze.house.fragments.BlazeBatteryHealthStudioFragment", "Pil Sağlığı & Termal Stüdyo");
        });
        panel.addView(btnBattery);

        TextView btnTorch = createSidebarPill("🔦 El Feneri Aç / Kapat", 0xFFF59E0B, d);
        btnTorch.setOnClickListener(v -> {
            vibrateTick();
            toggleTorch();
        });
        panel.addView(btnTorch);

        TextView btnVol = createSidebarPill("🔊 Ses Mikseri Panelini Aç", 0xFFEC4899, d);
        btnVol.setOnClickListener(v -> {
            vibrateTick();
            dismissSmartSidebarPanel(wm);
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                am.adjustVolume(AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
            }
        });
        panel.addView(btnVol);

        TextView btnClose = createSidebarPill("✕ Rıhtımı Kapat", 0xFF64748B, d);
        btnClose.setOnClickListener(v -> {
            vibrateTick();
            dismissSmartSidebarPanel(wm);
        });
        panel.addView(btnClose);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                Math.round(235f * d),
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.END;
        lp.x = Math.round(12f * d);
        lp.y = Math.round(170f * d);
        lp.setTitle("BlazeSmartSidebarPanel");

        mSmartSidebarPanelView = panel;
        try {
            wm.addView(mSmartSidebarPanelView, lp);
        } catch (Throwable ignored) {
            mSmartSidebarPanelView = null;
        }
    }

    private TextView createSidebarPill(String text, int strokeColor, float d) {
        TextView tv = new TextView(mContext);
        tv.setText(text);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(11.5f);
        tv.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0x331E293B);
        bg.setCornerRadius(12f * d);
        bg.setStroke(Math.max(1, Math.round(1f * d)), strokeColor);
        tv.setBackground(bg);
        int ph = Math.round(10f * d);
        int pv = Math.round(7f * d);
        tv.setPadding(ph, pv, ph, pv);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Math.round(5f * d);
        tv.setLayoutParams(lp);
        return tv;
    }

    private void launchSettingsSubFragment(String fragmentClass, String title) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName("com.android.settings", "com.android.settings.SubSettings");
            intent.putExtra(":settings:show_fragment", fragmentClass);
            intent.putExtra(":settings:show_fragment_title", title);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivityAsUser(intent, UserHandle.CURRENT);
        } catch (Throwable ignored) {}
    }

    private void vibrateTick() {
        try {
            Vibrator vib = mContext.getSystemService(Vibrator.class);
            if (vib != null && vib.hasVibrator()) {
                vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK));
            }
        } catch (Throwable ignored) {}
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

        String sig = Settings.Secure.getStringForUser(
                mContext.getContentResolver(), "blaze_lockstar_signature_text", UserHandle.USER_CURRENT);
        mLockStarSignatureText = sig != null ? sig : "PROJECT BLAZE 17 • BEYOND INFINITY";
        mEdgeLightingStyle = Settings.Secure.getIntForUser(
                mContext.getContentResolver(), "blaze_edgelighting_style", 2, UserHandle.USER_CURRENT);
        long pulseTs = Settings.Secure.getLongForUser(
                mContext.getContentResolver(), "blaze_edgelighting_preview_pulse", 0L, UserHandle.USER_CURRENT);
        if (System.currentTimeMillis() - pulseTs < 5000L) {
            mEdgeLightingPulseUntilMs = System.currentTimeMillis() + 3500L;
        }
        mWonderlandParticleMode = Settings.Secure.getIntForUser(
                mContext.getContentResolver(), "blaze_wonderland_particle_mode", 2, UserHandle.USER_CURRENT);
        mBehavioralAuraEnabled = Settings.Secure.getIntForUser(
                mContext.getContentResolver(), "blaze_behavioral_wallpaper_aura", 1, UserHandle.USER_CURRENT) != 0;

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

        // OriginOS 6 Unified Blur Arbitrator: suppress secondary wallpaper RenderEffect blur
        // when SystemUI Shade/QS blur is already active or device is dozing (0% GPU double-blur overdraw).
        boolean shadeBlurActive = !ScrimUtils.get().isPanelFullyCollapsed() || ScrimUtils.get().isDozing();
        if (mBlurTarget > 0 && !shadeBlurActive) {
            float radiusPx = switch (mBlurType) {
                case 1 -> 32f;
                case 2 -> 42f;
                case 3 -> 55f;
                case 4 -> 38f; // HyperOS 4 Soft Light Glass
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
        applyRenderEffect();
        boolean activeBlur = matchesTarget(mBlurTarget, isKeyguard);
        boolean activeEffect = mEffectType > 0 && matchesTarget(mEffectTarget, isKeyguard);
        boolean activeDim = matchesTarget(mDimTarget, isKeyguard);
        boolean activeGoodLockKg = isKeyguard && (!TextUtils.isEmpty(mLockStarSignatureText)
                || mWonderlandParticleMode > 0
                || mBehavioralAuraEnabled
                || mEdgeLightingStyle > 0);
        boolean activeEdgePulse = System.currentTimeMillis() < mEdgeLightingPulseUntilMs && mEdgeLightingStyle > 0;

        boolean shouldShow = activeBlur || activeEffect || activeDim || activeGoodLockKg || activeEdgePulse;
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
        applyRenderEffect();
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

            if (activeBlur && (mBlurType == 1 || mBlurType == 4)) {
                // HyperOS 4 Soft Light Glass & Frosted Glass real-time light field refraction tint
                LinearGradient softLightGrad = new LinearGradient(
                        0f, 0f, vw, vh,
                        new int[]{0x2EFFFFFF, 0x1838BDF8, 0x22A855F7},
                        new float[]{0f, 0.5f, 1f},
                        Shader.TileMode.CLAMP);
                mFrostPaint.setShader(softLightGrad);
                canvas.drawRect(0, 0, vw, vh, mFrostPaint);
                mFrostPaint.setShader(null);
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

        if (isKeyguard) {
            drawGoodLockKeyguardLayers(canvas, vw, vh);
        }

        if ((isKeyguard || System.currentTimeMillis() < mEdgeLightingPulseUntilMs) && mEdgeLightingStyle > 0) {
            drawEdgeLightingFrame(canvas, vw, vh);
        }
    }

    private void drawGoodLockKeyguardLayers(Canvas canvas, int vw, int vh) {
        final float d = mContext.getResources().getDisplayMetrics().density;

        // 1. OriginOS 6 Behavioral Telemetry Crystal Aura
        if (mBehavioralAuraEnabled) {
            int battPct = 75;
            boolean charging = false;
            try {
                Intent bi = mContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                if (bi != null) {
                    int lvl = bi.getIntExtra(BatteryManager.EXTRA_LEVEL, 75);
                    int scl = Math.max(1, bi.getIntExtra(BatteryManager.EXTRA_SCALE, 100));
                    battPct = (lvl * 100) / scl;
                    int st = bi.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    charging = (st == BatteryManager.BATTERY_STATUS_CHARGING || st == BatteryManager.BATTERY_STATUS_FULL);
                }
            } catch (Throwable ignored) {}

            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int coreColor;
            if (charging) {
                coreColor = 0x3010B981; // Emerald SuperVooc Bloom
            } else if (hour >= 6 && hour < 11) {
                coreColor = 0x2AF59E0B; // Dawn Amber Bloom
            } else if (hour >= 11 && hour < 18) {
                coreColor = 0x2A38BDF8; // Daylight Polar Cyan
            } else {
                coreColor = 0x2DA855F7; // Twilight Nebula Violet
            }
            float auraRadius = Math.max(vw, vh) * (0.20f + 0.25f * (battPct / 100f));
            RadialGradient auraGrad = new RadialGradient(
                    vw * 0.5f, vh * 0.28f, Math.max(10f, auraRadius),
                    new int[]{coreColor, Color.TRANSPARENT},
                    new float[]{0f, 1f},
                    Shader.TileMode.CLAMP);
            mGoodLockPaint.setStyle(Paint.Style.FILL);
            mGoodLockPaint.setShader(auraGrad);
            canvas.drawCircle(vw * 0.5f, vh * 0.28f, auraRadius, mGoodLockPaint);
            mGoodLockPaint.setShader(null);
        }

        // 2. Wonderland 3D Particle Layer (1=Sakura, 2=Fireflies, 3=Stardust, 4=Crystal Snow)
        if (mWonderlandParticleMode > 0) {
            mGoodLockPaint.setStyle(Paint.Style.FILL);
            mGoodLockPaint.setShader(null);
            for (int i = 0; i < 18; i++) {
                float px = ((i * 137 + 43) % 100) / 100f * vw;
                float py = ((i * 263 + 19) % 100) / 100f * vh;
                float size = (2.2f + (i % 4) * 1.35f) * d;
                if (mWonderlandParticleMode == 1) {
                    // Sakura Petals
                    mGoodLockPaint.setColor(0x55F472B6);
                    canvas.drawOval(px - size * 1.3f, py - size * 0.7f, px + size * 1.3f, py + size * 0.7f, mGoodLockPaint);
                } else if (mWonderlandParticleMode == 2) {
                    // Fireflies
                    mGoodLockPaint.setColor(i % 2 == 0 ? 0x58FACC15 : 0x5034D399);
                    canvas.drawCircle(px, py, size * 1.15f, mGoodLockPaint);
                } else if (mWonderlandParticleMode == 3) {
                    // Cosmic Stardust
                    mGoodLockPaint.setColor(i % 2 == 0 ? 0x6038BDF8 : 0x60C084FC);
                    canvas.drawCircle(px, py, size * 0.9f, mGoodLockPaint);
                } else {
                    // Crystal Snow
                    mGoodLockPaint.setColor(0x66FFFFFF);
                    canvas.drawCircle(px, py, size, mGoodLockPaint);
                }
            }
        }

        // 3. LockStar & HyperOS 4 Magazine Signature Header
        if (!TextUtils.isEmpty(mLockStarSignatureText)) {
            mSignaturePaint.setColor(0xCCFFFFFF);
            mSignaturePaint.setTextSize(10.5f * d);
            mSignaturePaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
            mSignaturePaint.setTextAlign(Paint.Align.CENTER);
            mSignaturePaint.setLetterSpacing(0.16f);
            mSignaturePaint.setShadowLayer(4f * d, 0f, 1.5f * d, 0x88000000);
            String formatted = "✦  " + mLockStarSignatureText.toUpperCase() + "  ✦";
            canvas.drawText(formatted, vw * 0.5f, vh * 0.068f, mSignaturePaint);
        }
    }

    private void drawEdgeLightingFrame(Canvas canvas, int vw, int vh) {
        final float d = mContext.getResources().getDisplayMetrics().density;
        int[] colors;
        switch (mEdgeLightingStyle) {
            case 1: // Eclipse
                colors = new int[]{0x55FFFFFF, 0x5538BDF8, 0x55FFFFFF};
                break;
            case 3: // Aurora Wave
                colors = new int[]{0x6610B981, 0x6638BDF8, 0x66A855F7};
                break;
            case 4: // Prism Pulse
                colors = new int[]{0x66F59E0B, 0x66EC4899, 0x6638BDF8};
                break;
            case 2: // Cyber Neon
            default:
                colors = new int[]{0x6638BDF8, 0x66A855F7, 0x66EC4899};
                break;
        }
        float strokeW = 3.2f * d;
        float pad = strokeW * 0.6f;
        mEdgeRect.set(pad, pad, vw - pad, vh - pad);
        LinearGradient grad = new LinearGradient(
                0f, 0f, vw, vh, colors, new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP);
        mGoodLockPaint.setStyle(Paint.Style.STROKE);
        mGoodLockPaint.setStrokeWidth(strokeW);
        mGoodLockPaint.setShader(grad);
        float cornerR = 34f * d;
        canvas.drawRoundRect(mEdgeRect, cornerR, cornerR, mGoodLockPaint);
        mGoodLockPaint.setShader(null);
    }
}
