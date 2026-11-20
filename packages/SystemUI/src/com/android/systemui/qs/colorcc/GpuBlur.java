package com.android.systemui.qs.colorcc;

import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.HardwareRenderer;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RenderEffect;
import android.graphics.RenderNode;
import android.graphics.Shader;
import android.media.Image;
import android.media.ImageReader;
import java.util.ArrayDeque;
import java.util.Iterator;

/* JADX INFO: loaded from: classes.dex */
final class GpuBlur {
    private static final int KEEP = 3;
    final int height;
    private final ArrayDeque<Image> held;
    private final RenderNode node;
    private final Paint paint;
    private final ImageReader reader;
    private final HardwareRenderer renderer;
    private final Rect whole;
    final int width;

    GpuBlur(int r11, int r12) {
        HardwareRenderer hardwareRenderer = new HardwareRenderer();
        this.renderer = hardwareRenderer;
        RenderNode renderNode = new RenderNode("ColorCC blur");
        this.node = renderNode;
        this.paint = new Paint(2);
        this.held = new ArrayDeque<>();
        this.width = r11;
        this.height = r12;
        this.whole = new Rect(0, 0, r11, r12);
        ImageReader imageReaderNewInstance = ImageReader.newInstance(r11, r12, 1, 4, 768L);
        this.reader = imageReaderNewInstance;
        renderNode.setPosition(0, 0, r11, r12);
        hardwareRenderer.setContentRoot(renderNode);
        hardwareRenderer.setSurface(imageReaderNewInstance.getSurface());
    }

    Bitmap draw(Bitmap bitmap, float f, ColorFilter colorFilter) {
        this.node.setRenderEffect(f >= 0.5f ? RenderEffect.createBlurEffect(f, f, Shader.TileMode.CLAMP) : null);
        this.paint.setColorFilter(colorFilter);
        this.node.beginRecording().drawBitmap(bitmap, (Rect) null, this.whole, this.paint);
        this.node.endRecording();
        this.renderer.createRenderRequest().setWaitForPresent(true).syncAndDraw();
        Image imageAcquireLatestImage = this.reader.acquireLatestImage();
        if (imageAcquireLatestImage == null) {
            return null;
        }
        this.held.addLast(imageAcquireLatestImage);
        while (this.held.size() > KEEP) {
            this.held.removeFirst().close();
        }
        return Bitmap.wrapHardwareBuffer(imageAcquireLatestImage.getHardwareBuffer(), null);
    }

    void release() {
        this.renderer.destroy();
        Iterator<Image> it = this.held.iterator();
        while (it.hasNext()) {
            it.next().close();
        }
        this.held.clear();
        this.reader.close();
    }
}
