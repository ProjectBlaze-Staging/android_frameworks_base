package com.android.systemui.qs.colorcc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/* JADX INFO: loaded from: classes.dex */
final class RingerGlyph extends View {
    private static final float BELL_PIVOT_Y = 3.0f;
    private static final float BUZZ_KICK = 80.0f;
    private static final float BUZZ_REACH = 1.6f;
    static final int SILENT = 2;
    private static final float SILENT_KICK_DEGREES = 300.0f;
    static final int SOUND = 0;
    private static final float SWING_KICK_DEGREES = 620.0f;
    static final int VIBRATE = 1;
    private int color;
    private final Path detail;
    private final int kind;
    private final Path main;
    private final Path outer;
    private final Paint paint;
    private final float size;
    private final Motion.Value slash;
    private final Paint slashPaint;
    private final Motion.Value swing;

    RingerGlyph(Context context, Motion motion, int r6, float f) {
        super(context);
        Paint paint = new Paint(VIBRATE);
        this.paint = paint;
        Paint paint2 = new Paint(VIBRATE);
        this.slashPaint = paint2;
        this.color = -1;
        this.kind = r6;
        this.size = f;
        paint.setColor(-1);
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeCap(Paint.Cap.ROUND);
        paint2.setStrokeWidth(2.0f);
        if (r6 == VIBRATE) {
            this.main = Icons.path("M16.5,3h-9C6.67,3 6,3.67 6,4.5v15c0,0.83 0.67,1.5 1.5,1.5h9c0.83,0 1.5,-0.67 1.5,-1.5v-15c0,-0.83 -0.67,-1.5 -1.5,-1.5zM16,19H8V5h8v14z");
            this.detail = Icons.path("M3,17h2V7H3v10zM19,17h2V7h-2v10z");
            this.outer = Icons.path("M0,15h2V9H0v6zM22,9v6h2V9h-2z");
        } else {
            this.main = Icons.path("M18,16v-5c0,-3.07 -1.63,-5.64 -4.5,-6.32V4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5v0.68C7.64,5.36 6,7.92 6,11v5l-2,2v1h16v-1l-2,-2z");
            this.detail = Icons.path("M12,22c1.1,0 2,-0.9 2,-2h-4c0,1.1 0.9,2 2,2z");
            this.outer = null;
        }
        this.swing = motion.value(r6 == VIBRATE ? Logic.Spring.BUZZ : Logic.Spring.SWING, new Motion.Listener() { // from class: dev.colorcc.RingerGlyph.0
            @Override // dev.colorcc.Motion.Listener
            public final void onValue(float f2, float f3) {
                RingerGlyph.this.m244lambda$new$0$devcolorccRingerGlyph(f2, f3);
            }
        });
        Motion.Value value = motion.value(Logic.Spring.SNAPPY, new Motion.Listener() { // from class: dev.colorcc.RingerGlyph.1
            @Override // dev.colorcc.Motion.Listener
            public final void onValue(float f2, float f3) {
                RingerGlyph.this.m245lambda$new$1$devcolorccRingerGlyph(f2, f3);
            }
        });
        this.slash = value;
        value.snap(1.0f);
    }

    /* JADX INFO: renamed from: lambda$new$0$dev-colorcc-RingerGlyph, reason: not valid java name */
    /* synthetic */ void m244lambda$new$0$devcolorccRingerGlyph(float f, float f2) {
        invalidate();
    }

    /* JADX INFO: renamed from: lambda$new$1$dev-colorcc-RingerGlyph, reason: not valid java name */
    /* synthetic */ void m245lambda$new$1$devcolorccRingerGlyph(float f, float f2) {
        invalidate();
    }

    void setColor(int r2) {
        if (r2 == this.color) {
            return;
        }
        this.color = r2;
        this.paint.setColor(r2);
        invalidate();
    }

    void play() {
        int r0 = this.kind;
        if (r0 == VIBRATE) {
            this.swing.kick(BUZZ_KICK);
            return;
        }
        Motion.Value value = this.swing;
        if (r0 == 0) {
            value.kick(SWING_KICK_DEGREES);
            return;
        }
        value.kick(SILENT_KICK_DEGREES);
        this.slash.snap(0.0f);
        this.slash.to(1.0f);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        int r0 = canvas.save();
        canvas.translate((getWidth() - this.size) / 2.0f, (getHeight() - this.size) / 2.0f);
        float f = this.size;
        canvas.scale(f / 24.0f, f / 24.0f);
        if (this.kind == VIBRATE) {
            drawVibrate(canvas);
        } else {
            drawBell(canvas);
        }
        canvas.restoreToCount(r0);
    }

    private void drawBell(Canvas canvas) {
        float fValue = this.swing.value();
        int r1 = canvas.save();
        canvas.rotate(fValue, 12.0f, BELL_PIVOT_Y);
        canvas.drawPath(this.main, this.paint);
        canvas.translate((-fValue) * 0.05f, 0.0f);
        canvas.drawPath(this.detail, this.paint);
        canvas.restoreToCount(r1);
        if (this.kind == SILENT) {
            this.slashPaint.setColor(this.color);
            float fClamp = (Logic.clamp(this.slash.value()) * 17.0f) + 3.5f;
            canvas.drawLine(3.5f, 3.5f, fClamp, fClamp, this.slashPaint);
        }
    }

    private void drawVibrate(Canvas canvas) {
        float fValue = this.swing.value();
        float fClamp = Logic.clamp(Math.abs(fValue) / BUZZ_REACH);
        int r3 = canvas.save();
        canvas.translate(fValue, 0.0f);
        canvas.rotate(2.2f * fValue, 12.0f, 12.0f);
        canvas.drawPath(this.main, this.paint);
        canvas.restoreToCount(r3);
        drawWaves(canvas, this.detail, (0.1f * fClamp) + 1.0f, 1.0f - (Logic.clamp((-fValue) / BUZZ_REACH) * 0.5f));
        drawWaves(canvas, this.outer, (fClamp * 0.14f) + 1.0f, 1.0f - (Logic.clamp(fValue / BUZZ_REACH) * 0.5f));
    }

    private void drawWaves(Canvas canvas, Path path, float f, float f2) {
        int r0 = canvas.save();
        canvas.scale(f, 1.0f, 12.0f, 12.0f);
        this.paint.setAlpha(Math.round(Color.alpha(this.color) * f2));
        canvas.drawPath(path, this.paint);
        this.paint.setColor(this.color);
        canvas.restoreToCount(r0);
    }
}
