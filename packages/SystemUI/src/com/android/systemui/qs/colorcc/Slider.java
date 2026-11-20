package com.android.systemui.qs.colorcc;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.SeekBar;

/* JADX INFO: loaded from: classes.dex */
final class Slider extends View {
    private static final float A11Y_STEP = 0.05f;
    private static final int FILL_COLOR = -83886081;
    static final float JELLY_MAX = 1.5f;
    private static final int TICKS = 15;
    private static final float TICK_KICK = 12.0f;
    private final ArgbEvaluator argb;
    private final Path clip;
    private float downX;
    private float downY;
    private final Motion.Value fill;
    private final Glass fillGlass;
    private boolean held;
    private final Runnable holdCheck;
    private Drawable icon;
    private int iconColor;
    private final float iconSize;
    private float iconSpin;
    private int lastTick;
    private float lastX;
    private float lastY;
    private Listener listener;
    private final float maxBulge;
    private boolean muteSlash;
    private Runnable onHold;
    private final Motion.Value pop;
    private final Motion.Value press;
    private final float radius;
    private float raw;
    private final Motion.Value slash;
    private final Paint slashGap;
    private final Paint slashLine;
    private final int slop;
    private float startValue;
    private final Motion.Value stretch;
    private final Path surface;
    private final Motion.Value surfaceLag;
    private float value;
    private static final int ICON_DARK = Color.argb(235, 34, 34, 42);
    static float jelly = 1.0f;

    interface Listener {
        void onChange(float f, boolean z);
    }

    Slider(Context context, Motion motion, float f, float f2) {
        super(context);
        this.clip = new Path();
        this.surface = new Path();
        this.argb = new ArgbEvaluator();
        Paint paint = new Paint(1);
        this.slashLine = paint;
        Paint paint2 = new Paint(1);
        this.slashGap = paint2;
        this.iconColor = ICON_DARK;
        this.holdCheck = new Runnable() { // from class: dev.colorcc.Slider.0
            @Override // java.lang.Runnable
            public final void run() {
                Slider.this.fireHold();
            }
        };
        this.radius = f;
        this.iconSize = f2;
        this.maxBulge = f * 0.55f;
        this.slop = ViewConfiguration.get(context).getScaledTouchSlop();
        Glass glass = new Glass(-1.0f, FILL_COLOR, FILL_COLOR, 0.5f, 0.5f);
        this.fillGlass = glass;
        glass.setLevel(1.0f);
        setFocusable(true);
        this.slash = motion.value(Logic.Spring.SNAPPY, new Motion.Listener() { // from class: dev.colorcc.Slider.1
            @Override // dev.colorcc.Motion.Listener
            public final void onValue(float f3, float f4) {
                Slider.this.m246lambda$new$0$devcolorccSlider(f3, f4);
            }
        });
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(0.09f * f2);
        paint2.set(paint);
        paint2.setStrokeWidth(f2 * 0.24f);
        paint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        this.surfaceLag = motion.value(Logic.Spring.SLOSH, new Motion.Listener() { // from class: dev.colorcc.Slider.2
            @Override // dev.colorcc.Motion.Listener
            public final void onValue(float f3, float f4) {
                Slider.this.m247lambda$new$1$devcolorccSlider(f3, f4);
            }
        });
        this.fill = motion.value(Logic.Spring.SNAPPY, new Motion.Listener() { // from class: dev.colorcc.Slider.3
            @Override // dev.colorcc.Motion.Listener
            public final void onValue(float f3, float f4) {
                Slider.this.m248lambda$new$2$devcolorccSlider(f3, f4);
            }
        });
        this.stretch = motion.value(Logic.Spring.BOUNCY, new Motion.Listener() { // from class: dev.colorcc.Slider.4
            @Override // dev.colorcc.Motion.Listener
            public final void onValue(float f3, float f4) {
                Slider.this.m249lambda$new$3$devcolorccSlider(f3, f4);
            }
        });
        this.press = motion.value(Logic.Spring.BOUNCY, new Motion.Listener() { // from class: dev.colorcc.Slider.5
            @Override // dev.colorcc.Motion.Listener
            public final void onValue(float f3, float f4) {
                Slider.this.m250lambda$new$4$devcolorccSlider(f3, f4);
            }
        });
        this.pop = motion.value(Logic.Spring.BOUNCY, new Motion.Listener() { // from class: dev.colorcc.Slider.6
            @Override // dev.colorcc.Motion.Listener
            public final void onValue(float f3, float f4) {
                Slider.this.m251lambda$new$5$devcolorccSlider(f3, f4);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$new$0$dev-colorcc-Slider, reason: not valid java name */
    /* synthetic */ void m246lambda$new$0$devcolorccSlider(float f, float f2) {
        invalidate();
    }

    /* JADX INFO: renamed from: lambda$new$1$dev-colorcc-Slider, reason: not valid java name */
    /* synthetic */ void m247lambda$new$1$devcolorccSlider(float f, float f2) {
        invalidate();
    }

    /* JADX INFO: renamed from: lambda$new$2$dev-colorcc-Slider, reason: not valid java name */
    /* synthetic */ void m248lambda$new$2$devcolorccSlider(float f, float f2) {
        this.surfaceLag.to(f);
        if (this.muteSlash) {
            this.slash.to(this.value == 0.0f ? 1.0f : 0.0f);
        }
        invalidate();
    }

    /* JADX INFO: renamed from: lambda$new$3$dev-colorcc-Slider, reason: not valid java name */
    /* synthetic */ void m249lambda$new$3$devcolorccSlider(float f, float f2) {
        applyScale();
    }

    /* JADX INFO: renamed from: lambda$new$4$dev-colorcc-Slider, reason: not valid java name */
    /* synthetic */ void m250lambda$new$4$devcolorccSlider(float f, float f2) {
        applyScale();
    }

    /* JADX INFO: renamed from: lambda$new$5$dev-colorcc-Slider, reason: not valid java name */
    /* synthetic */ void m251lambda$new$5$devcolorccSlider(float f, float f2) {
        invalidate();
    }

    private void applyScale() {
        float fValue = (this.press.value() * 0.025f) + 1.0f + (this.stretch.value() * 0.08f * jelly);
        float fValue2 = ((this.press.value() * 0.06f) + 1.0f) - ((this.stretch.value() * 0.04f) * jelly);
        setScaleX(across() ? fValue : fValue2);
        if (across()) {
            fValue = fValue2;
        }
        setScaleY(fValue);
    }

    private boolean across() {
        return getWidth() > getHeight();
    }

    void setListener(Listener listener) {
        this.listener = listener;
    }

    void setOnHold(Runnable runnable) {
        this.onHold = runnable;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fireHold() {
        if (isPressed()) {
            this.held = true;
            setPressed(false);
            this.press.to(0.0f);
            this.stretch.to(0.0f);
            float f = this.value;
            float f2 = this.startValue;
            if (f != f2) {
                this.value = f2;
                this.fill.to(f2);
                Listener listener = this.listener;
                if (listener != null) {
                    listener.onChange(this.value, true);
                }
            }
            performHapticFeedback(0);
            this.onHold.run();
        }
    }

    void setIconSpin(float f) {
        this.iconSpin = f;
    }

    void setIconColor(int r1) {
        this.iconColor = r1;
    }

    void setMuteSlash(boolean z) {
        this.muteSlash = z;
    }

    void setIcon(Drawable drawable) {
        this.icon = drawable;
        invalidate();
    }

    void setValue(float f) {
        if (isPressed()) {
            return;
        }
        float fClamp = Logic.clamp(f);
        this.value = fClamp;
        this.fill.to(fClamp);
        updateA11y();
    }

    float value() {
        return this.value;
    }

    void empty() {
        this.value = 0.0f;
        this.fill.snap(0.0f);
        this.surfaceLag.snap(0.0f);
    }

    private void updateA11y() {
        setStateDescription(Math.round(this.value * 100.0f) + "%");
    }

    @Override // android.view.View
    protected void onSizeChanged(int r9, int r10, int r11, int r12) {
        this.clip.reset();
        float fCornerFor = getBackground() instanceof Glass ? ((Glass) getBackground()).cornerFor(r9, r10) : Glass.pillRadius(r9, r10);
        float f = r9;
        float f2 = r10;
        this.clip.addRoundRect(0.0f, 0.0f, f, f2, fCornerFor, fCornerFor, Path.Direction.CW);
        setPivotX(f / 2.0f);
        setPivotY(f2 / 2.0f);
        this.fillGlass.setBounds(0, 0, r9, r10);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        Canvas canvas2 = canvas;
        int width = getWidth();
        int height = getHeight();
        boolean z = width > height;
        float fValue = this.fill.value();
        float f = this.maxBulge * jelly;
        float fMax = Math.max(-f, Math.min(f, (fValue - this.surfaceLag.value()) * (z ? width : height) * jelly));
        int r7 = canvas2.save();
        canvas2.clipPath(this.clip);
        this.surface.reset();
        Path path = this.surface;
        if (z) {
            float f2 = width * fValue;
            path.moveTo(f2, 0.0f);
            float f3 = height;
            this.surface.quadTo((fMax * 2.0f) + f2, f3 / 2.0f, f2, f3);
            this.surface.lineTo(0.0f, f3);
            this.surface.lineTo(0.0f, 0.0f);
        } else {
            float f4 = height;
            float f5 = (1.0f - fValue) * f4;
            path.moveTo(0.0f, f5);
            float f6 = width;
            this.surface.quadTo(f6 / 2.0f, f5 - (fMax * 2.0f), f6, f5);
            this.surface.lineTo(f6, f4);
            this.surface.lineTo(0.0f, f4);
        }
        this.surface.close();
        int r6 = canvas2.save();
        canvas2.clipPath(this.surface);
        this.fillGlass.draw(canvas2);
        canvas2.restoreToCount(r6);
        canvas2.restoreToCount(r7);
        if (this.icon != null) {
            int r8 = z ? height : width;
            float f7 = this.iconSize;
            int r9 = (int) ((r8 - f7) / 2.0f);
            int r10 = z ? (int) (r9 + f7) : height - r9;
            if (!z) {
                width = height;
            }
            float f8 = r9;
            int r11 = ((Integer) this.argb.evaluate(Logic.clamp(((width * fValue) - f8) / f7), -1, Integer.valueOf(this.iconColor))).intValue();
            this.icon.setTint(r11);
            this.icon.setLevel(Math.round(Logic.clamp(fValue) * 10000.0f));
            Drawable drawable = this.icon;
            float f9 = r10;
            float f10 = this.iconSize;
            drawable.setBounds(r9, (int) (f9 - f10), (int) (f10 + f8), r10);
            float fClamp = ((Logic.clamp(fValue) * 0.24f) + 0.86f) * ((Logic.clamp(this.pop.value()) * 0.35f) + 1.0f);
            int r12 = canvas2.save();
            float f11 = this.iconSpin * fValue;
            float f12 = this.iconSize;
            canvas2.rotate(f11, (f12 / 2.0f) + f8, f9 - (f12 / 2.0f));
            float f13 = this.iconSize;
            canvas2.scale(fClamp, fClamp, (f13 / 2.0f) + f8, f9 - (f13 / 2.0f));
            float fClamp2 = Logic.clamp(this.slash.value());
            if (fClamp2 > 0.0f) {
                float f14 = this.iconSize;
                canvas2.saveLayer(f8 - (f14 * 0.2f), f9 - (f14 * 1.2f), (1.2f * f14) + f8, (f14 * 0.2f) + f9, null);
            }
            this.icon.draw(canvas2);
            if (fClamp2 > 0.0f) {
                float f15 = this.iconSize;
                float f16 = f8 + (0.12f * f15);
                float f17 = f9 - (0.88f * f15);
                float f18 = f15 * 0.76f * fClamp2;
                float f19 = f16 + f18;
                float f20 = f17 + f18;
                canvas2.drawLine(f16, f17, f19, f20, this.slashGap);
                this.slashLine.setColor(r11);
                canvas.drawLine(f16, f17, f19, f20, this.slashLine);
                canvas2 = canvas;
            }
            canvas2.restoreToCount(r12);
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        float rawY;
        int height;
        float f;
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            getParent().requestDisallowInterceptTouchEvent(true);
            setPressed(true);
            this.held = false;
            this.startValue = this.value;
            this.downX = motionEvent.getRawX();
            this.downY = motionEvent.getRawY();
            if (this.onHold != null) {
                postDelayed(this.holdCheck, ViewConfiguration.getLongPressTimeout());
            }
            this.lastX = motionEvent.getRawX();
            this.lastY = motionEvent.getRawY();
            float f2 = this.value;
            this.raw = f2;
            this.lastTick = Math.round(f2 * 15.0f);
            setPivotX(getWidth() / 2.0f);
            setPivotY(getHeight() / 2.0f);
            this.press.to(1.0f);
            return true;
        }
        if (actionMasked != 1) {
            if (actionMasked == 2) {
                if (this.held) {
                    return true;
                }
                if (Math.hypot(motionEvent.getRawX() - this.downX, motionEvent.getRawY() - this.downY) > this.slop) {
                    removeCallbacks(this.holdCheck);
                }
                float f3 = this.raw;
                if (across()) {
                    rawY = motionEvent.getRawX() - this.lastX;
                    height = getWidth();
                } else {
                    rawY = this.lastY - motionEvent.getRawY();
                    height = getHeight();
                }
                this.raw = f3 + (rawY / height);
                this.lastX = motionEvent.getRawX();
                this.lastY = motionEvent.getRawY();
                float fClamp = Logic.clamp(this.raw);
                this.value = fClamp;
                this.fill.to(fClamp);
                int r10 = Math.round(this.value * 15.0f);
                if (r10 != this.lastTick) {
                    this.lastTick = r10;
                    this.pop.kick(TICK_KICK);
                }
                float f4 = this.raw;
                if (f4 > 1.0f) {
                    f = f4 - 1.0f;
                } else {
                    f = f4 < 0.0f ? -f4 : 0.0f;
                }
                boolean zAcross = across();
                float f5 = this.raw;
                if (zAcross) {
                    setPivotX(f5 <= 1.0f ? getWidth() : 0.0f);
                } else {
                    setPivotY(f5 > 1.0f ? getHeight() : 0.0f);
                }
                this.stretch.to(Math.min(f * 1.6f, 1.0f));
                Listener listener = this.listener;
                if (listener != null) {
                    listener.onChange(this.value, false);
                }
                return true;
            }
            if (actionMasked != 3) {
                return false;
            }
        }
        removeCallbacks(this.holdCheck);
        if (this.held) {
            this.held = false;
            return true;
        }
        endPress();
        return true;
    }

    private void endPress() {
        setPressed(false);
        this.press.to(0.0f);
        this.stretch.to(0.0f);
        updateA11y();
        Listener listener = this.listener;
        if (listener != null) {
            listener.onChange(this.value, true);
        }
    }

    @Override // android.view.View
    public void onVisibilityAggregated(boolean z) {
        super.onVisibilityAggregated(z);
        if (z || !isPressed()) {
            return;
        }
        ColorCCBridge.log("slider hidden while pressed: its touch never ended");
        removeCallbacks(this.holdCheck);
        this.held = false;
        endPress();
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName(SeekBar.class.getName());
        accessibilityNodeInfo.setRangeInfo(AccessibilityNodeInfo.RangeInfo.obtain(1, 0.0f, 1.0f, this.value));
        accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
        accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
    }

    @Override // android.view.View
    public boolean performAccessibilityAction(int r3, Bundle bundle) {
        if (r3 != 4096 && r3 != 8192) {
            return super.performAccessibilityAction(r3, bundle);
        }
        float fClamp = Logic.clamp(this.value + (r3 == 4096 ? A11Y_STEP : -0.05f));
        this.value = fClamp;
        this.fill.to(fClamp);
        updateA11y();
        Listener listener = this.listener;
        if (listener != null) {
            listener.onChange(this.value, true);
        }
        return true;
    }
}
