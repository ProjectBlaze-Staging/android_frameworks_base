package com.android.systemui.qs.colorcc;

/* JADX INFO: loaded from: classes.dex */
final class SpringStep {
    private static final float SUB_STEP = 0.004166667f;

    static void step(float[] fArr, float f, float f2, float f3, float f4) {
        while (f4 > 0.0f) {
            float fMin = Math.min(f4, SUB_STEP);
            float f5 = fArr[1];
            float f6 = fArr[0];
            float f7 = f5 + ((((f - f6) * f2) - (f5 * f3)) * fMin);
            fArr[1] = f7;
            fArr[0] = f6 + (f7 * fMin);
            f4 -= fMin;
        }
    }

    private SpringStep() {
    }
}
