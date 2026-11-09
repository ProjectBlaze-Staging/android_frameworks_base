/*
 * Copyright (C) 2026 The PenguinOS Project
 * Copyright (C) 2026 The BlazeAOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.internal.penguin.glass;

import android.content.Context;
import android.os.Process;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;

/**
 * Universal bottom tab bar scanner and coordinator for View and Jetpack Compose hierarchies.
 *
 * @hide
 */
public final class LiquidTabBars {
    private static final String TAG = "LiquidTabBars";

    public static final String SETTING_ENABLED = "penguin_glass_tab_bars";
    public static final String SETTING_BLAZE_ENABLED = "blaze_glass_tab_bars";
    public static final String SETTING_EXCLUDED = "penguin_glass_tab_bars_excluded";
    public static final String SETTING_FLOAT = "penguin_glass_tab_bars_float";

    private static final String[] BAR_CLASSES = {
            "BottomNavigationView", "NavigationBarView", "PivotBar", "BottomNavigation",
            "BottomTabLayout",
    };
    private static final String[] SCROLLER_CLASSES = {
            "RecyclerView", "ListView", "ScrollView", "GridView",
    };
    private static final long SCAN_INTERVAL_MS = 500;
    private static final long EAGER_MS = 3000;

    private final Context mContext;
    private final boolean mFloat;
    private final float mDensity;
    private final List<LiquidTabBar> mBars = new ArrayList<>();
    private ComposeTabBar mComposeBar;
    private long mNextComposeScan;
    private int mComposeMisses;
    private final WeakHashMap<View, Boolean> mPadded = new WeakHashMap<>();
    private long mLastScan;
    private boolean mScanPending;
    private final long mCreated;

    private LiquidTabBars(Context context, boolean floatOverContent) {
        mContext = context.getApplicationContext() != null ? context.getApplicationContext() : context;
        mFloat = floatOverContent;
        mDensity = context.getResources().getDisplayMetrics().density;
        mCreated = SystemClock.uptimeMillis();
    }

    public static LiquidTabBars create(Context context) {
        if (Process.myUid() < Process.FIRST_APPLICATION_UID) return null;
        try {
            int blazeEnabled = Settings.Secure.getInt(
                    context.getContentResolver(), SETTING_BLAZE_ENABLED, 1);
            int penguinEnabled = Settings.Secure.getInt(
                    context.getContentResolver(), SETTING_ENABLED, blazeEnabled);
            if (blazeEnabled == 0 || penguinEnabled == 0) {
                return null;
            }
            if (LiquidGlass.getEffectiveStyle(context) == LiquidGlass.STYLE_MATERIAL3_LITE) {
                return null;
            }
            String pkg = context.getPackageName();
            if (pkg != null && (pkg.startsWith("com.android.systemui")
                    || pkg.startsWith("com.android.launcher")
                    || pkg.contains("aperture")
                    || pkg.contains("camera")
                    || pkg.contains("inputmethod")
                    || listed(context, SETTING_EXCLUDED, pkg))) {
                return null;
            }
            boolean floatOver = Settings.Secure.getInt(
                    context.getContentResolver(), SETTING_FLOAT, 0) != 0
                    || listed(context, SETTING_FLOAT, pkg);
            return new LiquidTabBars(context, floatOver);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static boolean listed(Context context, String key, String pkg) {
        String list = Settings.Secure.getString(context.getContentResolver(), key);
        return list != null && Arrays.asList(list.split(",")).contains(pkg);
    }

    public void onLayout(View root) {
        if (LiquidGlass.getEffectiveStyle(mContext) == LiquidGlass.STYLE_MATERIAL3_LITE) {
            for (int i = mBars.size() - 1; i >= 0; i--) {
                mBars.get(i).remove();
                mBars.remove(i);
            }
            if (mComposeBar != null) {
                mComposeBar.remove();
                mComposeBar = null;
            }
            return;
        }
        boolean attached = false;
        for (int i = mBars.size() - 1; i >= 0; i--) {
            LiquidTabBar bar = mBars.get(i);
            if (bar.mBar.isAttachedToWindow()) {
                attached = true;
            } else {
                bar.remove();
                mBars.remove(i);
            }
        }
        if (mComposeBar != null) {
            if (mComposeBar.mView.isAttachedToWindow() && mComposeBar.update()) {
                attached = true;
            } else {
                mComposeBar.remove();
                mComposeBar = null;
            }
        }
        if (attached || !(root instanceof ViewGroup)) return;
        long now = SystemClock.uptimeMillis();
        boolean eager = now - mCreated < EAGER_MS;
        long wait = eager ? 0 : SCAN_INTERVAL_MS - (now - mLastScan);
        if (wait > 0) {
            if (!mScanPending) {
                mScanPending = true;
                root.postDelayed(() -> {
                    mScanPending = false;
                    if (root.isAttachedToWindow()) onLayout(root);
                }, wait);
            }
            return;
        }
        mLastScan = now;
        ViewGroup found = find((ViewGroup) root, root.getWidth(), root.getHeight());
        if (found == null) {
            return;
        }
        try {
            mBars.add(new LiquidTabBar(found));
            if (mFloat) floatOverContent(found);
        } catch (RuntimeException e) {
            Log.w(TAG, "Could not restyle " + found.getClass().getName(), e);
        }
    }

    public boolean onTouch(MotionEvent ev) {
        if (mComposeBar != null && mComposeBar.onTouch(ev)) return true;
        for (int i = 0; i < mBars.size(); i++) {
            LiquidTabBar bar = mBars.get(i);
            View view = bar.mBar;
            if (!view.isAttachedToWindow() || !view.isShown()) continue;
            view.getLocationInWindow(mLocation);
            MotionEvent local = MotionEvent.obtain(ev);
            local.offsetLocation(-mLocation[0], -mLocation[1]);
            float sx = view.getScaleX();
            float sy = view.getScaleY();
            if (sx != 1f || sy != 1f) {
                local.setLocation(local.getX() / sx, local.getY() / sy);
            }
            bar.onObserveTouch(local);
            local.recycle();
        }
        return false;
    }

    private final int[] mLocation = new int[2];

    private void findCompose(ViewGroup group, int rootHeight) {
        int maxGap = Math.round(88 * mDensity);
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE || child.getWidth() == 0) continue;
            child.getLocationInWindow(mLocation);
            if (mLocation[1] + child.getHeight() < rootHeight - maxGap) continue;
            if (child.getAccessibilityNodeProvider() != null) {
                try {
                    mComposeBar = ComposeTabBar.find(child, mFloat);
                } catch (RuntimeException e) {
                    Log.w(TAG, "Could not look for a Compose tab bar", e);
                }
                if (mComposeBar != null) {
                    return;
                }
            }
            if (child instanceof ViewGroup g) {
                findCompose(g, rootHeight);
                if (mComposeBar != null) return;
            }
        }
    }

    private ViewGroup find(ViewGroup group, int rootWidth, int rootHeight) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE || !(child instanceof ViewGroup g)) continue;
            if (isBar(g, rootWidth, rootHeight)) return g;
            ViewGroup found = find(g, rootWidth, rootHeight);
            if (found != null) return found;
        }
        return null;
    }

    private boolean isBar(ViewGroup v, int rootWidth, int rootHeight) {
        int h = v.getHeight();
        if (v.getWidth() < rootWidth * 0.85f || h < 40 * mDensity || h > 160 * mDensity) {
            return false;
        }
        int[] loc = new int[2];
        v.getLocationInWindow(loc);
        int bottomInset = 0;
        android.view.WindowInsets insets = v.getRootWindowInsets();
        if (insets != null) {
            bottomInset = insets.getInsets(
                    android.view.WindowInsets.Type.systemBars()
                            | android.view.WindowInsets.Type.displayCutout()).bottom;
        }
        int maxGap = Math.max(bottomInset + Math.round(24 * mDensity), Math.round(80 * mDensity));
        if (loc[1] + h < rootHeight - maxGap) return false;
        return matches(v.getClass(), BAR_CLASSES) && hasHorizontalTabRow(v, rootWidth);
    }

    private boolean hasHorizontalTabRow(ViewGroup v, int rootWidth) {
        if (hasHorizontalTabRowDirect(v, rootWidth)) return true;
        if (v.getChildCount() == 1 && v.getChildAt(0) instanceof ViewGroup inner
                && inner.getVisibility() == View.VISIBLE
                && inner.getWidth() >= rootWidth * 0.85f) {
            return hasHorizontalTabRowDirect(inner, rootWidth);
        }
        return false;
    }

    private boolean hasHorizontalTabRowDirect(ViewGroup v, int rootWidth) {
        int visible = 0;
        int clickableOrSelected = 0;
        int lastRight = Integer.MIN_VALUE / 2;
        for (int i = 0; i < v.getChildCount(); i++) {
            View child = v.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE || child.getWidth() == 0) continue;
            if (child.getWidth() < rootWidth * 0.12f || child.getWidth() > rootWidth * 0.52f) {
                return false;
            }
            if (child.getLeft() < lastRight - 4) return false;
            lastRight = child.getRight();
            visible++;
            if (child.isClickable() || child.isSelected() || child.isActivated()) {
                clickableOrSelected++;
            }
        }
        return visible >= 3 && visible <= 6 && clickableOrSelected >= 2;
    }

    private static boolean matches(Class<?> cls, String[] names) {
        for (Class<?> c = cls; c != null && c != View.class; c = c.getSuperclass()) {
            String name = c.getSimpleName();
            for (String n : names) {
                if (name.contains(n)) return true;
            }
        }
        return false;
    }

    private void floatOverContent(ViewGroup bar) {
        ViewParent p = bar.getParent();
        if (!(p instanceof ViewGroup parent)) return;
        int barHeight = bar.getHeight();
        View content = null;
        if (parent instanceof LinearLayout ll && ll.getOrientation() == LinearLayout.VERTICAL) {
            int index = parent.indexOfChild(bar);
            if (index > 0 && bar.getLayoutParams() instanceof ViewGroup.MarginLayoutParams lp) {
                lp.topMargin -= barHeight;
                bar.setLayoutParams(lp);
                content = parent.getChildAt(index - 1);
            }
        } else {
            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);
                if (child == bar || Math.abs(child.getBottom() - bar.getTop()) > 2) continue;
                if (extendToBottom(child, bar, barHeight)) {
                    content = child;
                    break;
                }
            }
        }
        if (content != null) padScrollers(content, barHeight);
    }

    private static boolean extendToBottom(View child, View bar, int barHeight) {
        ViewGroup.LayoutParams lp = child.getLayoutParams();
        if (lp instanceof RelativeLayout.LayoutParams rl
                && rl.getRule(RelativeLayout.ABOVE) == bar.getId()) {
            rl.removeRule(RelativeLayout.ABOVE);
            rl.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            child.setLayoutParams(rl);
            return true;
        }
        if (setConstraint(lp, bar.getId())) {
            child.setLayoutParams(lp);
            return true;
        }
        if (lp instanceof ViewGroup.MarginLayoutParams m && m.bottomMargin >= barHeight - 2) {
            m.bottomMargin -= barHeight;
            child.setLayoutParams(m);
            return true;
        }
        return false;
    }

    private static boolean setConstraint(ViewGroup.LayoutParams lp, int barId) {
        try {
            Class<?> cls = lp.getClass();
            Field toTop = cls.getField("bottomToTop");
            if (barId == View.NO_ID || toTop.getInt(lp) != barId) return false;
            toTop.setInt(lp, -1);
            cls.getField("bottomToBottom").setInt(lp, 0);
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    private void padScrollers(View view, int barHeight) {
        if (view instanceof ViewGroup group) {
            if (matches(view.getClass(), SCROLLER_CLASSES) && !mPadded.containsKey(view)) {
                mPadded.put(view, true);
                group.setClipToPadding(false);
                view.setPadding(view.getPaddingLeft(), view.getPaddingTop(),
                        view.getPaddingRight(), view.getPaddingBottom() + barHeight);
                return;
            }
            for (int i = 0; i < group.getChildCount(); i++) {
                padScrollers(group.getChildAt(i), barHeight);
            }
        }
    }
}
