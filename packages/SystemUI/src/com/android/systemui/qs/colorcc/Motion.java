package com.android.systemui.qs.colorcc;

import android.view.Choreographer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
final class Motion implements Choreographer.FrameCallback {
    private long lastNanos;
    private final Runnable onFailure;
    private boolean scheduled;
    private final Choreographer choreographer = Choreographer.getInstance();
    private final List<Value> active = new ArrayList();
    private final List<Value> frame = new ArrayList();

    interface Listener {
        void onValue(float f, float f2);
    }

    final class Value {
        private final Listener listener;
        private boolean running;
        private final Logic.Spring spring;

        Value(float[] fArr, Listener listener) {
            this.spring = new Logic.Spring(fArr);
            this.listener = listener;
        }

        float value() {
            return this.spring.value;
        }

        float velocity() {
            return this.spring.velocity;
        }

        boolean running() {
            return this.running;
        }

        void tune(float[] fArr) {
            this.spring.tune(fArr);
        }

        void to(float f) {
            if (this.spring.target == f) {
                return;
            }
            this.spring.target = f;
            Motion.this.start(this);
        }

        void snap(float f) {
            if (!this.running && this.spring.value == f && this.spring.target == f) {
                return;
            }
            this.spring.target = f;
            this.spring.value = f;
            this.spring.velocity = 0.0f;
            this.running = false;
            Motion.this.active.remove(this);
            this.listener.onValue(f, 0.0f);
        }

        void kick(float f) {
            this.spring.velocity += f;
            Motion.this.start(this);
        }

        void track(float f) {
            if (this.running) {
                return;
            }
            this.spring.value = f;
            this.spring.target = f;
            this.spring.velocity = 0.0f;
        }

        void shift(float f) {
            this.spring.value += f;
            this.listener.onValue(this.spring.value, this.spring.velocity);
            Motion.this.start(this);
        }
    }

    Motion(Runnable runnable) {
        this.onFailure = runnable;
    }

    Value value(float[] fArr, Listener listener) {
        return new Value(fArr, listener);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void start(Value value) {
        if (!value.running) {
            value.running = true;
            this.active.add(value);
        }
        if (this.scheduled) {
            return;
        }
        this.scheduled = true;
        this.lastNanos = 0L;
        this.choreographer.postFrameCallback(this);
    }

    void stopAll() {
        Iterator<Value> it = this.active.iterator();
        while (it.hasNext()) {
            it.next().running = false;
        }
        this.active.clear();
        if (this.scheduled) {
            this.choreographer.removeFrameCallback(this);
        }
        this.scheduled = false;
    }

    void settleAll() {
        ArrayList<Value> arrayList = new ArrayList(this.active);
        stopAll();
        for (Value value : arrayList) {
            value.snap(value.spring.target);
        }
    }

    @Override // android.view.Choreographer.FrameCallback
    public void doFrame(long j) {
        try {
            long j2 = this.lastNanos;
            float f = j2 == 0 ? 0.008333334f : (j - j2) / 1.0E9f;
            this.lastNanos = j;
            this.frame.clear();
            this.frame.addAll(this.active);
            for (Value value : this.frame) {
                if (value.running) {
                    if (value.spring.step(f)) {
                        value.running = false;
                        this.active.remove(value);
                    }
                    value.listener.onValue(value.spring.value, value.spring.velocity);
                }
            }
            boolean zIsEmpty = this.active.isEmpty();
            this.scheduled = !zIsEmpty;
            if (zIsEmpty) {
                return;
            }
            this.choreographer.postFrameCallback(this);
        } catch (Throwable th) {
            stopAll();
            ColorCCBridge.log("motion stopped: " + th);
            this.onFailure.run();
        }
    }
}
