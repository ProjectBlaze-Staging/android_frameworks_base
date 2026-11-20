/*
 * Copyright (C) 2026 Guest (ColorCC)
 * Copyright (C) 2026 The BlazeAOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.systemui.qs.colorcc;

import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.WindowManagerGlobal;
import android.widget.FrameLayout;
import android.window.ScreenCaptureInternal;

import com.android.systemui.globalactions.GlobalActionsDialogLite;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.FgsManagerController;
import com.android.systemui.qs.QSHost;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.WeakHashMap;

import javax.inject.Provider;

/**
 * Native AOSP bridge replacing ColorCC's Xposed Hook.java.
 * Connects ColorCC Panel, AGSL Glass Engine, Dual-Shade horizontal spring pager,
 * Notification & Footer Glass, and ScreenCaptureInternal live backdrop engine.
 */
public final class ColorCCBridge {
    private static final String TAG = "ColorCC";
    public static final String SETTING_USE_COLORCC = "blaze_qs_use_colorcc";

    private static final int MAX_CRASHES = 3;
    private static final int NOTE_GLASS_DARK = 452984831;
    private static final int NOTE_GLASS_LIGHT = 872415231;
    static final float STILL_SCALE = 2.5f;

    static final float[] BLUR = {0.075f, 0.13f, 0.25f};
    static final float[] WINDOW_BLUR = {0.58f, 1.0f, 1.6f};

    public static volatile QSHost host;
    public static volatile ActivityStarter activityStarter;
    public static volatile FgsManagerController fgsController;
    public static volatile Provider<GlobalActionsDialogLite> globalActionsProvider;
    private static volatile GlobalActionsDialogLite globalActionsDialog;
    private static volatile boolean fgsListenerRegistered = false;
    static volatile Object adapter = new Object();
    static volatile Panel panel;
    static volatile View composeQsView;

    static volatile boolean detailClosing = false;
    static volatile boolean notifGlass = true;
    static volatile boolean footerGlass = true;
    static volatile boolean oneUi8 = false;
    static volatile boolean samsungLiveSeen = true;
    static volatile boolean stillAsked = false;
    static volatile int themeLogs = 0;

    static volatile float qsExpansion = 0.0f;
    private static volatile float shadeFraction = 0.0f;
    private static volatile float blurCurve = 1.0f;
    private static volatile float shadeBlurWanted = 1.0f;
    static volatile float peekClear = 1.0f;
    static volatile float blurArrived = 1.0f;

    private static volatile Drawable shadeBlur;
    private static volatile boolean shadeBlurFresh;
    private static long backdropAskedAt;

    // Dual-Shade & Horizontal Pager State:
    // state 0 = QS (ColorCC), state 1 = Notifications, state 2 = Sliding between them
    private static volatile int currentState = 0;
    private static volatile int stateOnDown = 0;
    private static volatile int stateToChange = 0;
    private static volatile float draggedFraction = 0.0f;
    private static volatile String slideDirectionStr = "DOWN";
    private static volatile boolean splitModeEnabled = true;

    private static volatile WeakReference<View> footerRef;
    private static volatile WeakReference<View[]> footerButtonsRef;
    private static final WeakHashMap<View, Drawable> savedUnderBackgrounds = new WeakHashMap<>();
    private static final WeakHashMap<View, Glass> noteGlassDarkCache = new WeakHashMap<>();
    private static final WeakHashMap<View, Glass> noteGlassLightCache = new WeakHashMap<>();

    private static final File CRASHES = new File("/data/user_de/0/com.android.systemui/files/colorcc_crashes");

    private ColorCCBridge() {
    }

    public static boolean isColorCCEnabled(Context context) {
        if (context == null) return true;
        if (readCrashes() >= MAX_CRASHES) {
            return false;
        }
        try {
            return Settings.Secure.getIntForUser(
                    context.getContentResolver(),
                    SETTING_USE_COLORCC,
                    1,
                    UserHandle.USER_CURRENT) != 0;
        } catch (Throwable unused) {
            return true;
        }
    }

    public static void setColorCCEnabled(Context context, boolean enabled) {
        if (context == null) return;
        if (enabled) {
            clearCrashes();
        }
        try {
            Settings.Secure.putIntForUser(
                    context.getContentResolver(),
                    SETTING_USE_COLORCC,
                    enabled ? 1 : 0,
                    UserHandle.USER_CURRENT);
        } catch (Throwable th) {
            log("Failed to write setting: " + th);
        }
        applyVisibilityMode();
    }

    public static void applyVisibilityMode() {
        Panel p = panel;
        View compose = composeQsView;
        if (p == null) return;
        boolean enabled = isColorCCEnabled(p.getContext());
        if (compose != null) {
            compose.setVisibility(enabled ? View.GONE : View.VISIBLE);
            compose.setAlpha(enabled ? 0f : 1f);
        }
        if (!enabled) {
            p.setVisibility(View.GONE);
        } else {
            p.requestUpdate();
        }
    }

    public static synchronized void attachToQsContainer(
            ViewGroup container,
            View composeView,
            QSHost qsHost,
            ActivityStarter starter) {
        attachToQsContainer(container, composeView, qsHost, starter, null, null);
    }

    public static synchronized void attachToQsContainer(
            ViewGroup container,
            View composeView,
            QSHost qsHost,
            ActivityStarter starter,
            FgsManagerController fgs,
            Provider<GlobalActionsDialogLite> globalActions) {
        if (qsHost != null) {
            host = qsHost;
        }
        if (starter != null) {
            activityStarter = starter;
        }
        if (fgs != null) {
            fgsController = fgs;
            try {
                fgs.init();
            } catch (Throwable ignored) {
            }
            if (!fgsListenerRegistered) {
                fgsListenerRegistered = true;
                fgs.addOnNumberOfPackagesChangedListener(num -> {
                    Panel p = panel;
                    if (p != null) {
                        p.refreshActiveApps();
                    }
                });
                fgs.addOnDialogDismissedListener(() -> {
                    Panel p = panel;
                    if (p != null) {
                        p.refreshActiveApps();
                    }
                });
            }
        }
        if (globalActions != null) {
            globalActionsProvider = globalActions;
        }
        composeQsView = composeView;
        place(container);
        applyVisibilityMode();
        Panel p = panel;
        if (p != null) {
            p.refreshActiveApps();
        }
    }

    public static int getActiveAppsCount() {
        FgsManagerController fgs = fgsController;
        if (fgs == null) return 0;
        try {
            return Math.max(0, fgs.getNumRunningPackages());
        } catch (Throwable unused) {
            return 0;
        }
    }

    public static boolean hasNewActiveApps() {
        FgsManagerController fgs = fgsController;
        if (fgs == null) return false;
        try {
            return fgs.getNewChangesSinceDialogWasDismissed();
        } catch (Throwable unused) {
            return false;
        }
    }

    public static void openActiveAppsDialog() {
        final FgsManagerController fgs = fgsController;
        if (fgs == null) return;
        try {
            ActivityStarter starter = activityStarter;
            if (starter != null) {
                starter.dismissKeyguardThenExecute(() -> {
                    fgs.showDialog(null);
                    return false;
                }, null, true);
            } else {
                fgs.showDialog(null);
            }
        } catch (Throwable th) {
            log("active apps dialog failed: " + th);
        }
    }

    public static void openPowerMenu(Context context) {
        try {
            GlobalActionsDialogLite dialog = globalActionsDialog;
            if (dialog == null && globalActionsProvider != null) {
                dialog = globalActionsProvider.get();
                globalActionsDialog = dialog;
            }
            if (dialog != null) {
                boolean keyguardShowing = false;
                int displayId = 0;
                if (context != null) {
                    KeyguardManager km = context.getSystemService(KeyguardManager.class);
                    if (km != null) {
                        keyguardShowing = km.isKeyguardLocked();
                    }
                    if (context.getDisplay() != null) {
                        displayId = context.getDisplay().getDisplayId();
                    }
                }
                dialog.showOrHideDialog(keyguardShowing, true, null, displayId);
            }
        } catch (Throwable th) {
            log("power menu failed: " + th);
        }
    }

    public static void place(ViewGroup viewGroup) {
        Panel p = panel;
        if (p != null && p.isAttachedToWindow() && p.getParent() == viewGroup) {
            return;
        }
        if (p != null && p.getParent() instanceof ViewGroup) {
            ((ViewGroup) p.getParent()).removeView(p);
            p.retire();
        }
        try {
            Panel panel3 = new Panel(viewGroup.getContext(), viewGroup);
            viewGroup.addView(panel3, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            panel = panel3;
            log("ColorCC native panel attached to QS container");
        } catch (Throwable th) {
            panel = null;
            log("panel not created, stock panel stays: " + th);
        }
    }

    static void remake(Panel panel2) {
        if (panel == panel2 && (panel2.getParent() instanceof ViewGroup)) {
            ViewGroup viewGroup = (ViewGroup) panel2.getParent();
            viewGroup.removeView(panel2);
            panel2.retire();
            panel = null;
            place(viewGroup);
            applyVisibilityMode();
            log("panel made again for a new screen size");
        }
    }

    public static Panel getPanel() {
        return panel;
    }

    /**
     * Called by QuickSettingsControllerImpl when a Dual-Shade pull starts from the status bar / top edge.
     * @param rightHalf true if pulled from right half (Control Centre), false if left half (Notifications)
     */
    public static void onDualShadePullStart(boolean rightHalf) {
        splitModeEnabled = true;
        slideDirectionStr = "DOWN";
        if (rightHalf) {
            currentState = 0;
            stateOnDown = 0;
            stateToChange = 0;
            draggedFraction = 1.0f;
        } else {
            currentState = 1;
            stateOnDown = 1;
            stateToChange = 1;
            draggedFraction = 0.0f;
        }
        Panel p = panel;
        if (p != null && isColorCCEnabled(p.getContext())) {
            p.requestUpdate();
        }
    }

    public static boolean isShowingControlsPage() {
        Panel p = panel;
        if (p == null || !isColorCCEnabled(p.getContext())) {
            return false;
        }
        return isControlsPageState();
    }

    public static boolean isSlidingPages() {
        return currentState == 2;
    }

    public static float getControlsVisibilityFraction() {
        if (currentState == 2) {
            float f = Logic.clamp(draggedFraction);
            return stateToChange == 0 ? f : (1.0f - f);
        }
        return isControlsPageState() ? 1.0f : 0.0f;
    }

    /**
     * Called when user swipes horizontally inside the expanded shade to switch between
     * Notifications (state 1) and ColorCC (state 0).
     */
    public static void onHorizontalShadeSlide(float fraction, boolean toControls) {
        currentState = 2; // SLIDING
        slideDirectionStr = toControls ? "LEFT" : "RIGHT";
        stateOnDown = toControls ? 1 : 0;
        stateToChange = toControls ? 0 : 1;
        draggedFraction = Logic.clamp(fraction);
        PageSwipe.onNativeSlide(
                2,
                draggedFraction,
                0.0f,
                slideDirectionStr,
                toControls ? 0 : 1,
                panel != null ? panel.getRootView() : null);
        Panel p = panel;
        if (p != null) {
            p.requestUpdate();
        }
    }

    public static void onHorizontalShadeSlideEnd(boolean landedOnControls) {
        currentState = landedOnControls ? 0 : 1;
        stateOnDown = currentState;
        stateToChange = currentState;
        draggedFraction = 0.0f;
        PageSwipe.onNativeSlideEnd();
        Panel p = panel;
        if (p != null) {
            p.requestUpdate();
        }
    }

    public static void onQsExpansionChanged(float qsExp, float panelExp) {
        qsExpansion = Logic.clamp(qsExp);
        shadeFraction = Logic.clamp(panelExp);
        if (panelExp <= 0.001f && qsExp <= 0.001f) {
            if (currentState == 2) {
                currentState = stateToChange;
                stateOnDown = currentState;
                draggedFraction = 0.0f;
            }
            PageSwipe.onNativeSlideEnd();
        }
        Panel p = panel;
        if (p != null && isColorCCEnabled(p.getContext())) {
            p.onShadeExpansion(Math.max(shadeFraction, qsExpansion));
            p.requestUpdate();
        }
    }

    public static void onOverExpansionChanged(float overExpansion, boolean isFromGesture) {
        Panel p = panel;
        if (p != null && isColorCCEnabled(p.getContext())) {
            p.onOverExpansion(overExpansion, isFromGesture);
        }
    }

    public static boolean handleBack() {
        Panel p = panel;
        if (p != null && isColorCCEnabled(p.getContext())) {
            return p.handleBack();
        }
        return false;
    }

    public static boolean dispatchHeaderTouch(MotionEvent ev) {
        Panel p = panel;
        if (p != null && isColorCCEnabled(p.getContext())) {
            return p.headerTouch(ev);
        }
        return false;
    }

    public static boolean blocksShadeSlide(MotionEvent ev) {
        Panel p = panel;
        if (p != null && isColorCCEnabled(p.getContext())) {
            return p.blocksShadeSlide(ev);
        }
        return false;
    }

    public static boolean canCollapseAt(float rawX, float rawY) {
        Panel p = panel;
        if (p == null || !isColorCCEnabled(p.getContext())) {
            return true;
        }
        Boolean collapsible = p.collapsibleAt(rawX, rawY);
        return collapsible == null || collapsible.booleanValue();
    }

    /**
     * Captures the live screen content behind the Notification Shade window using AOSP ScreenCaptureInternal.
     * Excludes the NotificationShadeWindowView's own SurfaceControl so there is zero self-feedback loop.
     */
    public static Bitmap captureLiveBackdrop(View panelView, int targetWidth, int targetHeight) {
        if (panelView == null || targetWidth <= 0 || targetHeight <= 0) {
            return null;
        }
        try {
            int displayId = panelView.getDisplay() != null ? panelView.getDisplay().getDisplayId() : 0;
            SurfaceControl[] exclude = null;
            View root = panelView.getRootView();
            if (root != null) {
                ViewRootImpl vri = root.getViewRootImpl();
                if (vri != null) {
                    SurfaceControl sc = vri.getSurfaceControl();
                    if (sc != null && sc.isValid()) {
                        exclude = new SurfaceControl[]{sc};
                    }
                }
            }
            int fullW = root != null && root.getWidth() > 0 ? root.getWidth() : targetWidth;
            int fullH = root != null && root.getHeight() > 0 ? root.getHeight() : targetHeight;
            float scaleX = Math.max(0.05f, Math.min(1.0f, (float) targetWidth / (float) fullW));
            float scaleY = Math.max(0.05f, Math.min(1.0f, (float) targetHeight / (float) fullH));

            ScreenCaptureInternal.CaptureArgs.Builder<?> builder =
                    new ScreenCaptureInternal.CaptureArgs.Builder<>()
                            .setFrameScale(scaleX, scaleY);
            if (exclude != null) {
                builder.setExcludeLayers(exclude);
            }
            ScreenCaptureInternal.SynchronousScreenCaptureListener syncListener =
                    ScreenCaptureInternal.createSyncCaptureListener();
            WindowManagerGlobal.getWindowManagerService().captureDisplay(
                    displayId, builder.build(), syncListener);
            ScreenCaptureInternal.ScreenshotHardwareBuffer buffer = syncListener.getBuffer();
            if (buffer == null) {
                return null;
            }
            return buffer.asBitmap();
        } catch (Throwable th) {
            log("captureLiveBackdrop failed: " + th);
            return null;
        }
    }

    public static boolean shouldDrawNotificationGlass(View view) {
        if (view == null || !isColorCCEnabled(view.getContext())) {
            return false;
        }
        return noteGlassy(view);
    }

    public static boolean drawNotificationGlass(
            View view,
            Canvas canvas,
            int clipTop,
            int clipBottom,
            int actualHeight,
            int actualWidth,
            boolean expandAnimationRunning) {
        if (view == null || canvas == null || expandAnimationRunning || !shouldDrawNotificationGlass(view)) {
            return false;
        }
        int r0 = Math.max(0, clipTop);
        int r1 = Math.max(0, clipBottom);
        int r3 = actualHeight;
        int intField = actualWidth <= -1 ? view.getWidth() : actualWidth;
        if (r0 + r1 >= r3 || intField <= 0) {
            return true;
        }
        WeakHashMap<View, Glass> cache = Glass.dark ? noteGlassDarkCache : noteGlassLightCache;
        Glass glassNoteGlass;
        synchronized (cache) {
            glassNoteGlass = cache.get(view);
            if (glassNoteGlass == null) {
                float cardRadius = 24.0f * view.getResources().getDisplayMetrics().density;
                glassNoteGlass = noteGlass(cardRadius);
                cache.put(view, glassNoteGlass);
            }
        }
        glassNoteGlass.setCallback(view);
        int saveCount = canvas.save();
        canvas.clipRect(0, r0, view.getWidth(), r3 - r1);
        int width = view.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL ? view.getWidth() - intField : 0;
        glassNoteGlass.setBounds(width, r0, intField + width, r3 - r1);
        glassNoteGlass.draw(canvas);
        canvas.restoreToCount(saveCount);

        long jUptimeMillis = SystemClock.uptimeMillis();
        final Panel panel2 = panel;
        if (Glass.look != 3 && !Glass.bending() && panel2 != null && jUptimeMillis - backdropAskedAt > 1000) {
            backdropAskedAt = jUptimeMillis;
            panel2.post(panel2::notesWantBackdrop);
        }
        return true;
    }

    public static void glazeFooterView(View footerView, View... buttons) {
        if (footerView != null) {
            footerRef = new WeakReference<>(footerView);
        }
        if (buttons != null && buttons.length > 0) {
            footerButtonsRef = new WeakReference<>(buttons);
        }
        glazeFooter();
    }

    static void glazeFooter() {
        WeakReference<View[]> ref = footerButtonsRef;
        View[] buttons = ref == null ? null : ref.get();
        if (buttons == null) return;
        for (View view : buttons) {
            if (view == null) continue;
            try {
                boolean enabled = isColorCCEnabled(view.getContext());
                Glass.dark = (view.getResources().getConfiguration().uiMode & 48) == 32;
                boolean z = enabled && footerGlass && !(Glass.look == 2 && !Glass.dark);
                Drawable background = view.getBackground();
                int paddingLeft = view.getPaddingLeft();
                int paddingTop = view.getPaddingTop();
                int paddingRight = view.getPaddingRight();
                int paddingBottom = view.getPaddingBottom();
                if (z) {
                    if (!(background instanceof Glass)) {
                        synchronized (savedUnderBackgrounds) {
                            savedUnderBackgrounds.put(view, background);
                        }
                        view.setBackground(noteGlass());
                    }
                } else if (background instanceof Glass) {
                    Drawable orig;
                    synchronized (savedUnderBackgrounds) {
                        orig = savedUnderBackgrounds.remove(view);
                    }
                    view.setBackground(orig);
                }
                view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
            } catch (Throwable unused) {
            }
        }
    }

    static Glass noteGlass() {
        return noteGlass(-1.0f);
    }

    static Glass noteGlass(float radius) {
        return Glass.dark
                ? new Glass(radius, NOTE_GLASS_DARK, NOTE_GLASS_DARK, 0.5f, 0.5f).forNotes()
                : new Glass(radius, NOTE_GLASS_LIGHT, NOTE_GLASS_LIGHT, 0.5f, 0.5f).steady().forNotes();
    }

    static boolean noteGlassy(View view) {
        Glass.dark = (view.getResources().getConfiguration().uiMode & 48) == 32;
        return notifGlass && !((Glass.look == 2 && !Glass.dark)
                || view.getClass().getSimpleName().contains("Shelf"));
    }

    static String describe(Object obj) {
        Bitmap bitmap;
        try {
            if (obj instanceof Bitmap) {
                bitmap = (Bitmap) obj;
            } else {
                bitmap = obj instanceof BitmapDrawable ? ((BitmapDrawable) obj).getBitmap() : null;
            }
            if (bitmap == null) {
                return obj == null ? "none" : obj.getClass().getSimpleName();
            }
            Bitmap.Config config = bitmap.getConfig();
            return obj.getClass().getSimpleName() + " " + config + " " + bitmap.getWidth() + "x" + bitmap.getHeight();
        } catch (Throwable th) {
            return "not read: " + th;
        }
    }

    static float blurCurve() {
        return blurCurve;
    }

    static boolean samsungLive() {
        Panel panel2 = panel;
        return panel2 == null || panel2.windowBlurUsable();
    }

    static boolean windowBlurBlocked() {
        return false;
    }

    static boolean hasShadeBlur() {
        Drawable drawable = shadeBlur;
        return drawable != null && (drawable.getCallback() instanceof View)
                && ((View) drawable.getCallback()).getBackground() == drawable;
    }

    static float capturedShare(int r1, float f) {
        return Logic.level(BLUR, r1, f);
    }

    static void fadeShadeBlur(float f) {
        shadeBlurWanted = Math.max(0.0f, Math.min(1.0f, f));
        Drawable drawable = shadeBlur;
        if (drawable != null && (drawable.getCallback() instanceof View)
                && ((View) drawable.getCallback()).getBackground() == drawable) {
            int r0 = shadeBlurFresh ? Math.round(shadeBlurWanted * 255.0f) : 0;
            if (drawable.getAlpha() != r0) {
                drawable.setAlpha(r0);
            }
        }
    }

    static void staleShadeBlur() {
        shadeBlurFresh = false;
        fadeShadeBlur(shadeBlurWanted);
    }

    static void freshenShadeBlur() {
        if (shadeBlurFresh) {
            return;
        }
        shadeBlurFresh = true;
        fadeShadeBlur(shadeBlurWanted);
        try {
            reblur();
        } catch (Throwable th) {
            log("still picture not made again: " + th);
        }
    }

    static String backgroundState() {
        Drawable drawable = shadeBlur;
        String string;
        if (drawable == null) {
            string = "none";
        } else {
            string = "alpha " + drawable.getAlpha() + (shadeBlurFresh ? " fresh" : " old");
        }
        return "picture " + string + ", live blur strength " + peekClear;
    }

    static void showShadeBlur(Bitmap bitmap) {
        Drawable drawable = shadeBlur;
        if (drawable == null || !(drawable.getCallback() instanceof View)) {
            return;
        }
        View view = (View) drawable.getCallback();
        if (view.getBackground() != drawable) {
            return;
        }
        BitmapDrawable bitmapDrawable = new BitmapDrawable(view.getResources(), bitmap);
        bitmapDrawable.setAlpha(Math.round(shadeBlurWanted * 255.0f));
        view.setBackground(bitmapDrawable);
        shadeBlur = bitmapDrawable;
        shadeBlurFresh = true;
    }

    static void reblur() {
        redoLiveBlur();
    }

    static void redoLiveBlur() {
        // Handled natively via NotificationShadeDepthController / GpuBlur
    }

    static boolean splitEnabled() {
        return splitModeEnabled;
    }

    static float shadeFraction(float f) {
        float f2 = shadeFraction;
        return f2 >= 0.0f ? Logic.clamp(f2) : f;
    }

    static boolean isSliding() {
        return currentState == 2;
    }

    static int splitHeading() {
        return splitHeading(0.5f);
    }

    static int splitHeading(float f) {
        return draggedFraction >= f ? stateToChange : stateOnDown;
    }

    static float downToControls() {
        if (isSliding() && slidingDown()) {
            float fClamp = Logic.clamp(draggedFraction);
            if (stateToChange == 0) {
                return fClamp;
            }
            if (stateOnDown == 0) {
                return 1.0f - fClamp;
            }
        }
        return -1.0f;
    }

    static boolean isControlsPageState() {
        boolean isQs = currentState == 0;
        return Logic.controlsPage(currentState, splitHeading(), isQs);
    }

    static int splitField(String str) {
        if ("currentState".equals(str)) return currentState;
        if ("stateOnDown".equals(str)) return stateOnDown;
        if ("stateToChange".equals(str)) return stateToChange;
        return -1;
    }

    static boolean slidingDown() {
        return "DOWN".equals(slideDirection());
    }

    static String slideDirection() {
        return slideDirectionStr;
    }

    static boolean isReversed() {
        return false;
    }

    static Object sceneViewModel() {
        return null;
    }

    static int readCrashes() {
        try {
            if (!CRASHES.exists()) return 0;
            return Integer.parseInt(new String(Files.readAllBytes(CRASHES.toPath()), StandardCharsets.UTF_8).trim());
        } catch (Throwable unused) {
            return 0;
        }
    }

    static void writeCrashes(int r2) {
        try {
            File parent = CRASHES.getParentFile();
            if (parent != null) parent.mkdirs();
            Files.write(CRASHES.toPath(), String.valueOf(r2).getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
        } catch (Throwable th) {
            log("crash count not saved: " + th);
        }
    }

    static void clearCrashes() {
        try {
            CRASHES.delete();
        } catch (Throwable unused) {
        }
    }

    static void log(String str) {
        Log.d(TAG, str);
    }
}
