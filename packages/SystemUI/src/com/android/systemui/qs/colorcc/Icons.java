package com.android.systemui.qs.colorcc;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/* JADX INFO: loaded from: classes.dex */
final class Icons {
    static final String BELL_BODY = "M18,16v-5c0,-3.07 -1.63,-5.64 -4.5,-6.32V4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5v0.68C7.64,5.36 6,7.92 6,11v5l-2,2v1h16v-1l-2,-2z";
    static final String BELL_CLAPPER = "M12,22c1.1,0 2,-0.9 2,-2h-4c0,1.1 0.9,2 2,2z";
    static final String BLUETOOTH = "M17.71,7.71L12,2h-1v7.59L6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 11,14.41V22h1l5.71,-5.71 -4.3,-4.29 4.3,-4.29zM13,5.83l1.88,1.88L13,9.59V5.83zM14.88,16.29L13,18.17v-3.76l1.88,1.88z";
    static final String BRIGHT_HIGH = "M20,8.69V4h-4.69L12,0.69 8.69,4H4v4.69L0.69,12 4,15.31V20h4.69L12,23.31 15.31,20H20v-4.69L23.31,12 20,8.69zM12,18c-3.31,0 -6,-2.69 -6,-6s2.69,-6 6,-6 6,2.69 6,6 -2.69,6 -6,6zM12,8c-2.21,0 -4,1.79 -4,4s1.79,4 4,4 4,-1.79 4,-4 -1.79,-4 -4,-4z";
    static final String CLOSE = "M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z";
    static final String CONTRAST = "M12,22c5.52,0 10,-4.48 10,-10S17.52,2 12,2 2,6.48 2,12s4.48,10 10,10zM13,4.07c3.94,0.49 7,3.85 7,7.93s-3.05,7.44 -7,7.93V4.07z";
    static final String GEAR = "M11.25,2.43L11.51,1.01L12.49,1.01L12.75,2.43A9.6,9.6 0 0,1 13.75,2.56L13.75,2.56L14.37,1.26L15.32,1.51L15.2,2.95A9.6,9.6 0 0,1 16.13,3.34L16.13,3.34L17.07,2.24L17.92,2.73L17.44,4.09A9.6,9.6 0 0,1 18.23,4.7L18.23,4.7L19.42,3.88L20.12,4.58L19.3,5.77A9.6,9.6 0 0,1 19.91,6.56L19.91,6.56L21.27,6.08L21.76,6.93L20.66,7.87A9.6,9.6 0 0,1 21.05,8.8L21.05,8.8L22.49,8.68L22.74,9.63L21.44,10.25A9.6,9.6 0 0,1 21.57,11.25L21.57,11.25L22.99,11.51L22.99,12.49L21.57,12.75A9.6,9.6 0 0,1 21.44,13.75L21.44,13.75L22.74,14.37L22.49,15.32L21.05,15.2A9.6,9.6 0 0,1 20.66,16.13L20.66,16.13L21.76,17.07L21.27,17.92L19.91,17.44A9.6,9.6 0 0,1 19.3,18.23L19.3,18.23L20.12,19.42L19.42,20.12L18.23,19.3A9.6,9.6 0 0,1 17.44,19.91L17.44,19.91L17.92,21.27L17.07,21.76L16.13,20.66A9.6,9.6 0 0,1 15.2,21.05L15.2,21.05L15.32,22.49L14.37,22.74L13.75,21.44A9.6,9.6 0 0,1 12.75,21.57L12.75,21.57L12.49,22.99L11.51,22.99L11.25,21.57A9.6,9.6 0 0,1 10.25,21.44L10.25,21.44L9.63,22.74L8.68,22.49L8.8,21.05A9.6,9.6 0 0,1 7.87,20.66L7.87,20.66L6.93,21.76L6.08,21.27L6.56,19.91A9.6,9.6 0 0,1 5.77,19.3L5.77,19.3L4.58,20.12L3.88,19.42L4.7,18.23A9.6,9.6 0 0,1 4.09,17.44L4.09,17.44L2.73,17.92L2.24,17.07L3.34,16.13A9.6,9.6 0 0,1 2.95,15.2L2.95,15.2L1.51,15.32L1.26,14.37L2.56,13.75A9.6,9.6 0 0,1 2.43,12.75L2.43,12.75L1.01,12.49L1.01,11.51L2.43,11.25A9.6,9.6 0 0,1 2.56,10.25L2.56,10.25L1.26,9.63L1.51,8.68L2.95,8.8A9.6,9.6 0 0,1 3.34,7.87L3.34,7.87L2.24,6.93L2.73,6.08L4.09,6.56A9.6,9.6 0 0,1 4.7,5.77L4.7,5.77L3.88,4.58L4.58,3.88L5.77,4.7A9.6,9.6 0 0,1 6.56,4.09L6.56,4.09L6.08,2.73L6.93,2.24L7.87,3.34A9.6,9.6 0 0,1 8.8,2.95L8.8,2.95L8.68,1.51L9.63,1.26L10.25,2.56A9.6,9.6 0 0,1 11.25,2.43ZM12,4.1A7.9,7.9 0 1,0 12,19.9A7.9,7.9 0 1,0 12,4.1ZM12,12.5L3.7,12.5L3.7,11.5L12,11.5ZM11.57,11.75L15.72,4.56L16.58,5.06L12.43,12.25ZM12.43,11.75L16.58,18.94L15.72,19.44L11.57,12.25ZM12,10.9A1.1,1.1 0 1,1 12,13.1A1.1,1.1 0 1,1 12,10.9Z";
    static final String HEADPHONES = "M12,3c-4.97,0 -9,4.03 -9,9v7c0,1.1 0.9,2 2,2h4v-8H5v-1c0,-3.87 3.13,-7 7,-7s7,3.13 7,7v1h-4v8h4c1.1,0 2,-0.9 2,-2v-7c0,-4.97 -4.03,-9 -9,-9z";
    static final String KEY = "M12.65,10C11.83,7.67 9.61,6 7,6c-3.31,0 -6,2.69 -6,6s2.69,6 6,6c2.61,0 4.83,-1.67 5.65,-4H17v4h4v-4h2v-4H12.65zM7,14c-1.1,0 -2,-0.9 -2,-2s0.9,-2 2,-2 2,0.9 2,2 -0.9,2 -2,2z";
    static final String LOCK = "M18,8h-1V6c0,-2.76 -2.24,-5 -5,-5S7,3.24 7,6v2H6c-1.1,0 -2,0.9 -2,2v10c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V10c0,-1.1 -0.9,-2 -2,-2zM9,6c0,-1.66 1.34,-3 3,-3s3,1.34 3,3v2H9V6z";
    static final String MOON = "M12,3c-4.97,0 -9,4.03 -9,9s4.03,9 9,9 9,-4.03 9,-9c0,-0.46 -0.04,-0.92 -0.1,-1.36 -0.98,1.37 -2.58,2.26 -4.4,2.26 -2.98,0 -5.4,-2.42 -5.4,-5.4 0,-1.81 0.89,-3.42 2.26,-4.4C12.92,3.04 12.46,3 12,3z";
    static final String MUSIC = "M12,3v10.55c-0.59,-0.34 -1.27,-0.55 -2,-0.55 -2.21,0 -4,1.79 -4,4s1.79,4 4,4 4,-1.79 4,-4V7h4V3h-6z";
    static final String NEXT = "M6,18l8.5,-6L6,6v12zM16,6v12h2V6z";
    static final String NOTE = "M20,2H4c-1.1,0 -2,0.9 -2,2v18l4,-4h14c1.1,0 2,-0.9 2,-2V4c0,-1.1 -0.9,-2 -2,-2z";
    static final String OUTPUT = "M21,3H3C1.9,3 1,3.9 1,5v3h2V5h18v14h-7v2h7c1.1,0 2,-0.9 2,-2V5C23,3.9 22.1,3 21,3zM1,18v3h3C4,19.34 2.66,18 1,18zM1,14v2c2.76,0 5,2.24 5,5h2C8,17.13 4.87,14 1,14zM1,10v2c4.97,0 9,4.03 9,9h2C12,14.92 7.07,10 1,10z";
    static final String PALETTE = "M12,3c-4.97,0 -9,4.03 -9,9s4.03,9 9,9c0.83,0 1.5,-0.67 1.5,-1.5 0,-0.39 -0.15,-0.74 -0.39,-1.01 -0.23,-0.26 -0.38,-0.61 -0.38,-0.99 0,-0.83 0.67,-1.5 1.5,-1.5H16c2.76,0 5,-2.24 5,-5 0,-4.42 -4.03,-8 -9,-8zM6.5,12c-0.83,0 -1.5,-0.67 -1.5,-1.5S5.67,9 6.5,9 8,9.67 8,10.5 7.33,12 6.5,12zM9.5,8C8.67,8 8,7.33 8,6.5S8.67,5 9.5,5s1.5,0.67 1.5,1.5S10.33,8 9.5,8zM14.5,8c-0.83,0 -1.5,-0.67 -1.5,-1.5S13.67,5 14.5,5s1.5,0.67 1.5,1.5S15.33,8 14.5,8zM17.5,12c-0.83,0 -1.5,-0.67 -1.5,-1.5S16.67,9 17.5,9s1.5,0.67 1.5,1.5 -0.67,1.5 -1.5,1.5z";
    static final String PAUSE = "M6,19h4V5H6v14zM14,5v14h4V5h-4z";
    static final String PENCIL = "M3,17.25V21h3.75L17.81,9.94l-3.75,-3.75L3,17.25zM20.71,7.04c0.39,-0.39 0.39,-1.02 0,-1.41l-2.34,-2.34c-0.39,-0.39 -1.02,-0.39 -1.41,0l-1.83,1.83 3.75,3.75 1.83,-1.83z";
    static final String PHONE = "M16.5,3h-9C6.67,3 6,3.67 6,4.5v15c0,0.83 0.67,1.5 1.5,1.5h9c0.83,0 1.5,-0.67 1.5,-1.5v-15c0,-0.83 -0.67,-1.5 -1.5,-1.5zM16,19H8V5h8v14z";
    static final String PLAY = "M8,5v14l11,-7z";
    static final String PREV = "M6,6h2v12H6zM9.5,12l8.5,6V6z";
    static final String SPEAKER = "M2,9v6h4l5,5V4L6,9H2z";
    static final String SUN = "M12,7c-2.76,0 -5,2.24 -5,5s2.24,5 5,5 5,-2.24 5,-5 -2.24,-5 -5,-5zM2,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1H2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1zM20,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1h-2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1zM11,2v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1V2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1zM11,20v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1v-2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1zM5.99,4.58c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39,0.39 1.03,0.39 1.41,0s0.39,-1.03 0,-1.41L5.99,4.58zM18.36,16.95c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39,0.39 1.03,0.39 1.41,0 0.39,-0.39 0.39,-1.03 0,-1.41l-1.06,-1.06zM19.42,5.99c0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06zM7.05,18.36c0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06z";
    static final String VOLUME_OFF = "M16.5,12c0,-1.77 -1.02,-3.29 -2.5,-4.03v2.21l2.45,2.45c0.03,-0.2 0.05,-0.41 0.05,-0.63zM19,12c0,0.94 -0.2,1.82 -0.54,2.64l1.51,1.51C20.63,14.91 21,13.5 21,12c0,-4.28 -2.99,-7.86 -7,-8.77v2.06c2.89,0.86 5,3.54 5,6.71zM4.27,3L3,4.27 7.73,9H3v6h4l5,5v-6.73l4.25,4.25c-0.67,0.52 -1.42,0.93 -2.25,1.18v2.06c1.38,-0.31 2.63,-0.95 3.69,-1.81L19.73,21 21,19.73l-9,-9L4.27,3zM12,4L9.91,6.09 12,8.18V4z";
    static final String WAVES_INNER = "M3,17h2V7H3v10zM19,17h2V7h-2v10z";
    static final String WAVES_OUTER = "M0,15h2V9H0v6zM22,9v6h2V9h-2z";
    static final String WIFI = "M1,9l2,2c4.97,-4.97 13.03,-4.97 18,0l2,-2C16.93,2.93 7.08,2.93 1,9zM9,17l3,3 3,-3c-1.65,-1.66 -4.34,-1.66 -6,0zM5,13l2,2c2.76,-2.76 7.24,-2.76 10,0l2,-2C15.14,9.14 8.87,9.14 5,13z";

    static Path path(String str) {
        return android.util.PathParser.createPathFromPathData(str);
    }

    static Drawable of(String str) {
        return new PathIcon(path(str));
    }

    static Drawable muted(String str) {
        Path path = path(str);
        path.op(band(4.4f), Path.Op.DIFFERENCE);
        path.op(band(2.0f), Path.Op.UNION);
        return new PathIcon(path);
    }

    private static Path band(float f) {
        Path path = new Path();
        path.addRoundRect(-12.73f, (-f) / 2.0f, 12.73f, f / 2.0f, 1.0f, 1.0f, Path.Direction.CW);
        Matrix matrix = new Matrix();
        matrix.setRotate(45.0f);
        matrix.postTranslate(12.0f, 12.0f);
        path.transform(matrix);
        return path;
    }

    static Drawable speaker() {
        return new Speaker();
    }

    private static final class Speaker extends Drawable {
        private final RectF arc;
        private final Path body = Icons.path(Icons.SPEAKER);
        private int color;
        private final Paint paint;

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicHeight() {
            return 72;
        }

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicWidth() {
            return 72;
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }

        Speaker() {
            Paint paint = new Paint(1);
            this.paint = paint;
            this.arc = new RectF();
            this.color = -1;
            paint.setStrokeWidth(1.8f);
            paint.setStrokeCap(Paint.Cap.ROUND);
        }

        @Override // android.graphics.drawable.Drawable
        protected boolean onLevelChange(int r1) {
            invalidateSelf();
            return true;
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            int r6 = canvas.save();
            canvas.translate(bounds.left, bounds.top);
            canvas.scale(bounds.width() / 24.0f, bounds.height() / 24.0f);
            this.paint.setColor(this.color);
            this.paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(this.body, this.paint);
            this.paint.setStyle(Paint.Style.STROKE);
            float fMax = Math.max(0.0f, 1.0f - ((getLevel() / 10000.0f) * 30.0f));
            if (fMax > 0.0f) {
                this.paint.setAlpha(Math.round((this.color >>> 24) * fMax));
                canvas.drawLine(3.0f, 3.0f, 21.0f, 21.0f, this.paint);
            }
            float level = (getLevel() / 10000.0f) * 3.0f;
            for (int r10 = 0; r10 < 3; r10++) {
                float f = r10;
                if (level <= f) {
                    break;
                }
                this.paint.setAlpha(Math.round((this.color >>> 24) * Math.min(1.0f, level - f)));
                float f2 = (f * 3.0f) + 5.0f;
                this.arc.set(9.0f - f2, 12.0f - f2, 9.0f + f2, f2 + 12.0f);
                canvas.drawArc(this.arc, -40.0f, 80.0f, false, this.paint);
            }
            canvas.restoreToCount(r6);
        }

        @Override // android.graphics.drawable.Drawable
        public void setTintList(ColorStateList colorStateList) {
            if (colorStateList != null) {
                this.color = colorStateList.getDefaultColor();
            }
            invalidateSelf();
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int r3) {
            this.color = (r3 << 24) | (this.color & 16777215);
            invalidateSelf();
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
            this.paint.setColorFilter(colorFilter);
            invalidateSelf();
        }
    }

    private static final class PathIcon extends Drawable {
        private final Paint paint;
        private final Path path;

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicHeight() {
            return 72;
        }

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicWidth() {
            return 72;
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }

        PathIcon(Path path) {
            Paint paint = new Paint(1);
            this.paint = paint;
            this.path = path;
            paint.setColor(-1);
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            int r1 = canvas.save();
            canvas.translate(bounds.left, bounds.top);
            canvas.scale(bounds.width() / 24.0f, bounds.height() / 24.0f);
            canvas.drawPath(this.path, this.paint);
            canvas.restoreToCount(r1);
        }

        @Override // android.graphics.drawable.Drawable
        public void setTintList(ColorStateList colorStateList) {
            if (colorStateList != null) {
                this.paint.setColor(colorStateList.getDefaultColor());
            }
            invalidateSelf();
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int r2) {
            this.paint.setAlpha(r2);
            invalidateSelf();
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
            this.paint.setColorFilter(colorFilter);
            invalidateSelf();
        }
    }

    private Icons() {
    }
}
