package com.android.systemui.qs.colorcc;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.UiModeManager;
import android.app.WallpaperManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.display.DisplayManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaRoute2Info;
import android.media.MediaRouter2;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.FloatProperty;
import android.util.LruCache;
import android.util.Property;
import android.view.Display;
import android.view.FrameMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/* JADX INFO: loaded from: classes.dex */
public final class Panel extends FrameLayout {
    private static final int ACTIVE_BLUE = -15374872;
    private static final int ACTIVE_BLUE_LIGHT = -10773505;
    private static final int ACTIVE_RED = -1756374;
    private static final float ARC = 22.0f;
    private static final float AWAY_SIZE = 0.84f;
    private static final int BACKDROP_BLUR = 24;
    private static final long BACKDROP_FRAME_MS = 33;
    private static final long BACKDROP_RECHECK_MS = 500;
    private static final int BACKDROP_SHRINK = 2;
    private static final int BADGE_REMOVE = -263172008;
    static final int BLUR_CUSTOM = 3;
    static final float BLUR_CUSTOM_MAX = 2.5f;
    private static final float CLOSE_PACE = 1.4f;
    private static final float CLOSE_SPAN = 0.32f;
    private static final float CLOSE_STEP = 0.035f;
    private static final String COLOUR_TONE = "ead_enabled";
    private static final int COVER_HEIGHT = 224;
    private static final int COVER_INSET = 14;
    private static final int COVER_RADIUS = 24;
    private static final float DROP_HEADER = 16.0f;
    private static final float DROP_STEP = 12.0f;
    private static final long EDITOR_SETTLE_MS = 320;
    private static final int EDIT_GAP = 14;
    private static final int EDIT_ROW = 63;
    private static final String EXTRA_BRIGHTNESS = "screen_extra_brightness";
    private static final long FRAME_BUDGET_NS = 16700000;
    private static final float FULLY_OPEN = 0.99f;
    private static final int GAP = 14;
    private static final int GESTURE_ROOM = 22;
    private static final int GLASS_CARD_ON = 587202559;
    private static final int GLASS_DISC_ON = -83886081;
    private static final int GLASS_IDLE = 452984831;
    private static final long GPU_KEEP_MS = 30000;
    private static final int HEADER_ROOM = 94;
    private static final int HEADER_TOP = 64;
    private static final int HEADER_TOP_LANDSCAPE = 36;
    private static final long HOLD_MS = 280;
    private static final int ICONS_COLOROS = 0;
    private static final int ICONS_CUSTOM = 2;
    private static final int ICONS_SYSTEM = 1;
    private static final float ICON_COVERED = 0.15f;
    private static final float IN_BOUNCE = 0.38f;
    private static final float IN_S = 0.24f;
    private static final float IN_STEP_S = 0.012f;
    private static final float LARGE_UNIT_DP = 1.1f;
    private static final int LIST_ROW = 50;
    private static final long LIVE_BACKDROP_MS = 250;
    static final int LIVE_DIM = 1711276032;
    private static final int MARGIN = 24;
    private static final float MAX_SMEAR = 0.05f;
    private static final float MEDIA_EDGE_LIGHT = 0.3f;
    private static final int MEDIA_RADIUS = 34;
    private static final int MEDIA_TINT_ALPHA = 240;
    private static final int MENU_HEIGHT = 380;
    private static final int MENU_ROOM = 96;
    private static final int PAGE_PADDING = 27;
    private static final long PAGE_SETTLE_MS = 40;
    private static final int PANEL_STREAM = 3;
    private static final int PANE_GAP = 24;
    private static final float PANE_LEAST = 0.6f;
    private static final int PANE_WIDTH = 340;
    private static final float PLAYER_TINT = 0.22f;
    private static final int PLAYER_WIDTH = 236;
    private static final long PREBUILD_DELAY_MS = 4000;
    private static final float PRESS_DEPTH = 0.08f;
    private static final int PULL_LIMIT = 140;
    private static final int RADIUS = 26;
    private static final int RINGER_IDLE = -1493172225;
    private static final int RINGER_INSET = 9;
    private static final int RINGER_NORMAL = 2;
    private static final int RINGER_SILENT = 0;
    private static final int RINGER_VIBRATE = 1;
    private static final int ROW_HEIGHT = 63;
    private static final int SCROLL_FADE = 64;
    private static final int SCROLL_SLACK = 40;
    private static final int SHEET_GLASS = 536870911;
    private static final int SPRINGS = 2113929217;
    private static final long STABLE_RUN_MS = 300000;
    private static final float STAGGER_SPAN = 0.42f;
    private static final float STAGGER_START = 0.06f;
    private static final float STAGGER_STEP = 0.045f;
    private static final long STALE_PICTURE_MS = 400;
    private static final int STATE_ACTIVE = 2;
    private static final int STATE_UNAVAILABLE = 0;
    private static final float STATUS_GROW = 0.1f;
    private static final long STILL_FRAME_MS = 66;
    private static final long STOCK_CLOSE_GRACE_MS = 300;
    private static final long STOCK_SCREEN_DELAY_MS = 180;
    private static final long STOCK_SCREEN_TIMEOUT_MS = 1000;
    private static final float SWELL = -1.1f;
    private static final long SWELL_MS = 90;
    private static final float TAP_DEPTH = 0.6f;
    private static final int TILE_DISC = 63;
    private static final int TILE_ICON = 26;
    private static final int TILE_ROW_BARE = 74;
    private static final int TILE_ROW_HEIGHT = 98;
    private static final float TOGGLE_KICK = 7.0f;
    private static final long TURN_SETTLE_MS = 450;
    private static boolean noCollapsible;
    private final ArgbEvaluator argb;
    private int arrangedCards;
    private final AudioManager audio;
    private final ExecutorService audioThread;
    private boolean backdropAgain;
    private boolean backdropBroken;
    private boolean backdropBusy;
    private int backdropFailures;
    private boolean backdropLogged;
    private boolean backdropPauseLogged;
    private long backdropSum;
    private final ExecutorService backdropThread;
    private final Blocks blocks;
    private final Card bluetoothCard;
    volatile int blur;
    private final Motion.Value blurArrive;
    volatile float blurCustom;
    private Boolean blursLive;
    private final Set<View> bodyViews;
    private final List<Bound> bound;
    private volatile float brightMax;
    private volatile float brightMin;
    private final Slider brightness;
    private final FrameLayout brightnessHost;
    private final DisplayManager.DisplayListener brightnessWatch;
    private float buttonsLift;
    private int closeFrames;
    private long closeLastNs;
    private long closeMaxGapNs;
    private long closeMaxWorkNs;
    private float closePace;
    private final Motion.Value closeRide;
    private long closeWorkNs;
    private boolean closing;
    private final Column column;
    private final ViewGroup container;
    private final int[] containerAt;
    private final LinearLayout content;
    private MediaController controller;
    private List<Object> currentTiles;
    private int customColour;
    private boolean dead;
    private final float density;
    private final DisplayManager display;
    private final Dots dots;
    private long drawMaxNs;
    private long drawNs;
    private Editor editor;
    private final Runnable fadeBackdrop;
    private final Map<Class<?>, Method> flowGetters;
    private final Map<Object, Map<String, Object>> flows;
    private final boolean foldable;
    private Object frameObserver;
    private Window frameWindow;
    private final Disc activeApps;
    private final TextView activeAppsCount;
    private final View activeAppsDot;
    private final Disc gear;
    private final Disc power;
    private View goneView;
    private int goneWas;
    private GpuBlur gpu;
    private GpuBlur gpuSoft;
    private volatile boolean gpuWorks;
    private final float[] gravity;
    private boolean gravityRead;
    private int gridRowCount;
    private View header8;
    private int headerPadding;
    private final LinearLayout headerRow;
    private View headerTarget;
    private int heldStates;
    private float heldTop;
    private float heldX;
    private int iconBuilds;
    private int iconMode;
    private boolean interactive;
    private final KeyguardManager keyguard;
    private float lastExpansion;
    private float lastFraction;
    private long lastFractionAt;
    private int lastLogKey;
    private int lastRow;
    private boolean lastStockFlows;
    private final List<Disc> lateIcons;
    private String layout;
    private int layoutsDuringMotion;
    private final FloatProperty<View> litLevel;
    private boolean liveBroken;
    private int liveDim;
    private int liveFailures;
    private boolean liveOn;
    private int liveRadius;
    private Bitmap lockShot;
    private int lockShotId;
    private boolean lockedNow;
    private long lockedReadAt;
    private volatile float loggedBlur;
    private float loggedShift;
    private final Handler main;
    private final LruCache<Drawable.ConstantState, Bitmap> masks;
    private final MediaCard media;
    private final MediaController.Callback mediaCallback;
    private final MediaSessionManager mediaSessions;
    private int[] mediaTones;
    private final Motion motion;
    private String motionKind;
    private boolean notesAskLogged;
    private volatile boolean notesAsked;
    private boolean notesLoopLogged;
    private boolean open;
    private float openPace;
    private boolean overPulling;
    private boolean override;
    private long overrideSince;
    private boolean ownBlurLogged;
    private boolean page;
    private final int[] pageAt;
    private int pageRows;
    private final Pager pager;
    private float paneScale;
    private final List<Integer> parkedAt;
    private final List<ViewGroup> parkedIn;
    private final List<View> parkedViews;
    private final Motion.Value peek;
    private float peekFrom;
    private final Disc pencil;
    private boolean pendingPage;
    private long pendingPageSince;
    private final Map<String, View> pinViews;
    private final Map<String, int[]> pins;
    private String places;
    private Player player;
    private float playerBack;
    private final ViewTreeObserver.OnPreDrawListener preDraw;
    private final Motion.Value pull;
    private boolean pullArmed;
    private long pullAt;
    private float pullFromY;
    private boolean pullLogged;
    private boolean pullOnOpen;
    private float pullSpeed;
    private long pullStartedAt;
    private boolean pulling;
    private int pullsLogged;
    private boolean quickCapture;
    private final Map<String, Integer> redrawnBy;
    private final Runnable refreshBackdrop;
    private final Runnable releaseGpu;
    private int renderedFrames;
    private int renderedSlow;
    private final BroadcastReceiver ringReceiver;
    private boolean ringRefreshQueued;
    private final RingerPill ringer;
    private boolean sawStockScreen;
    private float scrollFromY;
    private long scrollLastAt;
    private float scrollLastY;
    private int scrollMax;
    private float scrollSpeed;
    private float scrollStart;
    private final Motion.Value scroller;
    private boolean scrolling;
    private final MediaSessionManager.OnActiveSessionsChangedListener sessionsListener;
    private boolean settleWorkPending;
    private Sheet sheet;
    private float sheetBack;
    private boolean sheetLayered;
    private int shownBlur;
    private int shownRingerSlot;
    private float shownStatusShift;
    private final LinearLayout side;
    private final Card simCard;
    private long skipAt;
    private int skipWay;
    private final SideSlide slide;
    private final double[] slowStages;
    private final List<Body> staticBodies;
    private View statusCarrier;
    private boolean statusOverwriteLogged;
    private View statusRow;
    private boolean statusRowLogged;
    private boolean statusSearched;
    private List<View> statusSides;
    private final List<View> stock;
    private long stockFlowsSince;
    private boolean stockHidden;
    private boolean stockNeeded;
    private View stockPage;
    private long stockScreenSince;
    private final Motion.Value stockValue;
    private float swipeDownX;
    private float swipeDownY;
    private int swipeOwner;
    private boolean systemWatched;
    private int tileBase;
    private final List<Body> tileBodies;
    private boolean tileNames;
    private int tileRows;
    private final FrameLayout tilesHost;
    private final SensorEventListener tilt;
    private long tiltDrawnAt;
    private Bitmap tonedArt;
    private final List<View> touchControls;
    private final int touchSlop;
    private float turnFrom;
    private boolean twoPane;
    private int uiNight;
    private int uiOrientation;
    private final Slider volume;
    private final FrameLayout volumeHost;
    private final ConcurrentHashMap<Integer, Float> volumeWanted;
    private final TextView vpn;
    private final Map<Network, String> vpnNetworks;
    private ConnectivityManager.NetworkCallback vpnWatch;
    private final Card wifiCard;
    private Resources words;
    private boolean wordsMissing;
    private String worstFrame;
    private long worstFrameNs;
    private static final float[] MEDIA_TINT_STOPS = {0.0f, 0.35f, 0.8f, 1.0f};
    private static final Object NO_FLOW = new Object();
    private static final float[] OUT = Logic.Spring.of(0.12f, 0.0f);
    private static final float DROP_TOP = 36.0f;
    private static final float[] SCROLL_GLIDE = {DROP_TOP, 1.0f};
    private static final float[] PEEK = {320.0f, 1.0f};
    private static final float[] LIVE_BLUR = {27.0f, 46.0f, 80.0f};
    private static final float[] BLUR_ARRIVE = Logic.Spring.of(0.26f, 0.0f);
    private static final int TILES_PER_ROW = 4;
    private static final int[] STAGES = {0, 1, 2, 3, TILES_PER_ROW, 5, 6, 7, 12};
    private static final String[] STAGE_NAMES = {"wait", "input", "anim", "layout", "draw", "sync", "issue", "swap", "gpu"};
    private static final Set<String> NOT_MUSIC = new HashSet(Arrays.asList("com.twitter.android"));

    /* JADX INFO: renamed from: dev.colorcc.Panel$0, reason: invalid class name */
    /* JADX INFO: compiled from: D8$$SyntheticClass */
    public final /* synthetic */ class AnonymousClass0 implements Runnable {
        public /* synthetic */ AnonymousClass0() {
        }

        @Override // java.lang.Runnable
        public final void run() {
            Panel.this.arrange();
        }
    }

    /* JADX INFO: renamed from: dev.colorcc.Panel$27, reason: invalid class name */
    /* JADX INFO: compiled from: D8$$SyntheticClass */
    public final /* synthetic */ class AnonymousClass27 implements Runnable {
        @Override // java.lang.Runnable
        public final void run() {
            ColorCCBridge.redoLiveBlur();
        }
    }

    /* JADX INFO: renamed from: dev.colorcc.Panel$35, reason: invalid class name */
    /* JADX INFO: compiled from: D8$$SyntheticClass */
    public final /* synthetic */ class AnonymousClass35 implements Runnable {
        public /* synthetic */ AnonymousClass35() {
        }

        @Override // java.lang.Runnable
        public final void run() {
            Panel.this.captureBackdrop();
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void access$12000(Panel panel) {
        panel.openMediaApp();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void access$1500(Panel panel) {
        panel.renderMedia();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void access$1700(Panel panel) {
        panel.refreshRing();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void access$4000(Panel panel) {
        panel.showVpn();
    }

    static /* synthetic */ int access$8808(Panel panel) {
        int r0 = panel.heldStates;
        panel.heldStates = r0 + 1;
        return r0;
    }

    static /* synthetic */ int access$9308(Panel panel) {
        int r0 = panel.iconBuilds;
        panel.iconBuilds = r0 + 1;
        return r0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int tileRow() {
        return this.tileNames ? TILE_ROW_HEIGHT : TILE_ROW_BARE;
    }

    private static long brightnessEvents() {
        try {
            return ReflectUtils.getStaticLongField(DisplayManager.class, "EVENT_FLAG_DISPLAY_BRIGHTNESS");
        } catch (Throwable unused) {
            return 32L;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int theme(int r3) {
        int r0 = this.iconMode;
        if (r0 == 1) {
            return getContext().getColor(android.R.color.system_accent1_600);
        }
        return r0 == 2 ? this.customColour : r3;
    }

    /* JADX INFO: renamed from: lambda$new$0$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ boolean m15lambda$new$0$devcolorccPanel() {
        tick();
        return true;
    }

    /* JADX INFO: renamed from: lambda$new$2$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m27lambda$new$2$devcolorccPanel(final List list) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.18
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m16lambda$new$1$devcolorccPanel(list);
            }
        });
    }

    static /* synthetic */ Thread lambda$new$3(Runnable runnable) {
        return new Thread(runnable, "ColorCC-audio");
    }

    Panel(Context context, ViewGroup viewGroup) {
        super(context);
        this.tileNames = true;
        this.main = new Handler(Looper.getMainLooper());
        this.argb = new ArgbEvaluator();
        Motion motion = new Motion(new Runnable() { // from class: dev.colorcc.Panel.95
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.die();
            }
        });
        this.motion = motion;
        this.flows = new IdentityHashMap();
        this.flowGetters = new HashMap();
        this.lastLogKey = -1;
        this.stock = new ArrayList();
        ArrayList arrayList = new ArrayList();
        this.touchControls = arrayList;
        this.bound = new ArrayList();
        this.staticBodies = new ArrayList();
        this.tileBodies = new ArrayList();
        this.currentTiles = new ArrayList();
        this.brightMin = 0.0f;
        this.brightMax = 1.0f;
        this.brightnessWatch = new DisplayManager.DisplayListener() { // from class: dev.colorcc.Panel.1
            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayAdded(int r1) {
            }

            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayRemoved(int r1) {
            }

            @Override // android.hardware.display.DisplayManager.DisplayListener
            public void onDisplayChanged(final int r3) {
                if (r3 != 0 || !Panel.this.open || Panel.this.brightness.isPressed() || Panel.this.sheet.level.isPressed()) {
                    return;
                }
                Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.1.0
                    @Override // java.lang.Runnable
                    public final void run() {
                        m94lambda$onDisplayChanged$0$devcolorccPanel$1(r3);
                    }
                });
            }

            /* JADX INFO: renamed from: lambda$onDisplayChanged$0$dev-colorcc-Panel$1, reason: not valid java name */
            /* synthetic */ void m94lambda$onDisplayChanged$0$devcolorccPanel$1(int r3) {
                float slider = Logic.toSlider(((Float) ReflectUtils.callMethod(Panel.this.display, "getBrightness", new Object[]{Integer.valueOf(r3)})).floatValue(), Panel.this.brightMin, Panel.this.brightMax);
                Panel.this.brightness.setValue(slider);
                Panel.this.sheet.showBrightness(slider);
            }
        };
        this.layout = "wbmlvsd";
        this.tileBase = 11;
        this.pins = new LinkedHashMap();
        this.pinViews = new HashMap();
        this.lateIcons = new ArrayList();
        this.gridRowCount = 3;
        this.tileRows = 3;
        this.iconMode = 0;
        this.customColour = ACTIVE_BLUE;
        this.swipeOwner = 2;
        this.vpnNetworks = new HashMap();
        this.peekFrom = -1.0f;
        this.gravity = new float[3];
        this.tilt = new SensorEventListener() { // from class: dev.colorcc.Panel.2
            @Override // android.hardware.SensorEventListener
            public void onAccuracyChanged(Sensor sensor, int r2) {
            }

            /* JADX INFO: renamed from: lambda$onSensorChanged$0$dev-colorcc-Panel$2, reason: not valid java name */
            /* synthetic */ void m96lambda$onSensorChanged$0$devcolorccPanel$2(SensorEvent sensorEvent) {
                Panel.this.onTilt(sensorEvent.values);
            }

            @Override // android.hardware.SensorEventListener
            public void onSensorChanged(final SensorEvent sensorEvent) {
                Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.2.0
                    @Override // java.lang.Runnable
                    public final void run() {
                        m96lambda$onSensorChanged$0$devcolorccPanel$2(sensorEvent);
                    }
                });
            }
        };
        this.openPace = 1.0f;
        this.closePace = 1.0f;
        this.blur = 1;
        this.blurCustom = 1.0f;
        this.containerAt = new int[2];
        this.pageAt = new int[2];
        this.slide = new SideSlide(new SideSlide.Host() { // from class: dev.colorcc.Panel.3
            @Override // dev.colorcc.SideSlide.Host
            public List<SideSlide.Piece> pieces() {
                ArrayList arrayList2 = new ArrayList();
                Iterator it = Panel.this.staticBodies.iterator();
                while (it.hasNext()) {
                    arrayList2.add(((Body) it.next()).piece);
                }
                Iterator it2 = Panel.this.tileBodies.iterator();
                while (it2.hasNext()) {
                    arrayList2.add(((Body) it2.next()).piece);
                }
                return arrayList2;
            }

            @Override // dev.colorcc.SideSlide.Host
            public boolean shown() {
                View viewStockPage = Panel.this.container != null ? Panel.this.container : Panel.this.stockPage();
                if (!viewStockPage.isShown()) {
                    return false;
                }
                viewStockPage.getLocationOnScreen(Panel.this.pageAt);
                int width = Panel.this.getRootView().getWidth();
                return Panel.this.pageAt[0] < width && Panel.this.pageAt[0] + Math.max(viewStockPage.getWidth(), width) > 0;
            }

            @Override // dev.colorcc.SideSlide.Host
            public boolean drawn() {
                View viewStockPage = Panel.this.container != null ? Panel.this.container : Panel.this.stockPage();
                return viewStockPage.isShown();
            }

            @Override // dev.colorcc.SideSlide.Host
            public boolean visible() {
                return Panel.this.open && Panel.this.getVisibility() == 0 && drawn();
            }

            @Override // dev.colorcc.SideSlide.Host
            public int rootHeight() {
                return Panel.this.getRootView().getHeight();
            }

            @Override // dev.colorcc.SideSlide.Host
            public boolean locked() {
                return Panel.this.locked();
            }

            @Override // dev.colorcc.SideSlide.Host
            public void onSlideIn() {
                Panel.this.setReveal(1.0f);
            }

            @Override // dev.colorcc.SideSlide.Host
            public void onAway() {
                Panel.this.setReveal(0.0f);
            }

            @Override // dev.colorcc.SideSlide.Host
            public void apply() {
                Panel.this.applySheet();
            }
        });
        this.preDraw = new ViewTreeObserver.OnPreDrawListener() { // from class: dev.colorcc.Panel.106
            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public final boolean onPreDraw() {
                return Panel.this.m15lambda$new$0$devcolorccPanel();
            }
        };
        this.sessionsListener = new MediaSessionManager.OnActiveSessionsChangedListener() { // from class: dev.colorcc.Panel.107
            @Override // android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
            public final void onActiveSessionsChanged(List list) {
                Panel.this.m27lambda$new$2$devcolorccPanel(list);
            }
        };
        this.mediaCallback = new MediaController.Callback() { // from class: dev.colorcc.Panel.4
            @Override // android.media.session.MediaController.Callback
            public void onPlaybackStateChanged(PlaybackState playbackState) {
                final Panel panel = Panel.this;
                panel.guard(new Runnable() { // from class: dev.colorcc.Panel.4.0
                    @Override // java.lang.Runnable
                    public final void run() {
                        Panel.access$1500(panel);
                    }
                });
            }

            @Override // android.media.session.MediaController.Callback
            public void onMetadataChanged(MediaMetadata mediaMetadata) {
                final Panel panel = Panel.this;
                panel.guard(new Runnable() { // from class: dev.colorcc.Panel.4.1
                    @Override // java.lang.Runnable
                    public final void run() {
                        Panel.access$1500(panel);
                    }
                });
            }
        };
        this.ringReceiver = new BroadcastReceiver() { // from class: dev.colorcc.Panel.5
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                final Panel panel = Panel.this;
                panel.guard(new Runnable() { // from class: dev.colorcc.Panel.5.0
                    @Override // java.lang.Runnable
                    public final void run() {
                        Panel.access$1700(panel);
                    }
                });
                Panel panel2 = Panel.this;
                final Player player = panel2.player;
                Objects.requireNonNull(player);
                panel2.guard(new Runnable() { // from class: dev.colorcc.Panel.5.1
                    @Override // java.lang.Runnable
                    public final void run() {
                        player.readVolume();
                    }
                });
            }
        };
        this.shownRingerSlot = -1;
        this.audioThread = Executors.newSingleThreadExecutor(new ThreadFactory() { // from class: dev.colorcc.Panel.108
            @Override // java.util.concurrent.ThreadFactory
            public final Thread newThread(Runnable runnable) {
                return Panel.lambda$new$3(runnable);
            }
        });
        this.volumeWanted = new ConcurrentHashMap<>();
        this.skipWay = 1;
        this.litLevel = new FloatProperty<View>("lit") { // from class: dev.colorcc.Panel.7
            @Override // android.util.FloatProperty
            public void setValue(View view, float f) {
                ((Glass) view.getBackground()).setLevel(f);
                ((TextView) view).setTextColor(((Integer) Panel.this.argb.evaluate(Logic.clamp(f), -1, -14671832)).intValue());
            }

            @Override // android.util.Property
            public Float get(View view) {
                return Float.valueOf(((Glass) view.getBackground()).level());
            }
        };
        this.paneScale = 1.0f;
        this.arrangedCards = -1;
        this.uiNight = -1;
        this.uiOrientation = -1;
        this.motionKind = "close";
        this.redrawnBy = new HashMap();
        Set<View> setNewSetFromMap = Collections.newSetFromMap(new WeakHashMap());
        this.bodyViews = setNewSetFromMap;
        this.slowStages = new double[STAGES.length];
        this.masks = new LruCache<>(MENU_ROOM);
        this.worstFrame = "not measured";
        this.loggedShift = -1.0f;
        this.parkedViews = new ArrayList();
        this.parkedIn = new ArrayList();
        this.parkedAt = new ArrayList();
        this.lockedReadAt = -1000L;
        this.backdropThread = Executors.newSingleThreadExecutor();
        this.gpuWorks = true;
        this.releaseGpu = new Runnable() { // from class: dev.colorcc.Panel.109
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m34lambda$new$53$devcolorccPanel();
            }
        };
        this.loggedBlur = -1.0f;
        this.fadeBackdrop = new Runnable() { // from class: dev.colorcc.Panel.9
            @Override // java.lang.Runnable
            public void run() {
                if (Glass.blending()) {
                    Panel.reglaze(Panel.this);
                    Panel.this.postOnAnimation(this);
                }
            }
        };
        this.lockShotId = -1;
        this.refreshBackdrop = new Runnable() { // from class: dev.colorcc.Panel.10
            @Override // java.lang.Runnable
            public void run() {
                Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.10.0
                    @Override // java.lang.Runnable
                    public final void run() {
                        m95lambda$run$0$devcolorccPanel$10();
                    }
                });
            }

            /* JADX INFO: renamed from: lambda$run$0$dev-colorcc-Panel$10, reason: not valid java name */
            /* synthetic */ void m95lambda$run$0$devcolorccPanel$10() {
                boolean zLiveBackdrop = Panel.this.liveBackdrop();
                Panel panel = Panel.this;
                if (zLiveBackdrop) {
                    panel.captureBackdrop();
                } else {
                    panel.pauseBackdrop(this);
                }
            }
        };
        this.container = viewGroup;
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        this.foldable = context.getPackageManager().hasSystemFeature("android.hardware.sensor.hinge_angle");
        this.density = unitNow();
        this.uiNight = getResources().getConfiguration().uiMode & 48;
        StringBuilder sb = new StringBuilder("panel made for a ");
        sb.append(displayMetrics.widthPixels);
        sb.append("x");
        sb.append(displayMetrics.heightPixels);
        sb.append(" screen, ");
        sb.append(getResources().getConfiguration().smallestScreenWidthDp);
        sb.append(" dp at its narrowest");
        sb.append(large() ? ", large: phone-sized panes" : "");
        ColorCCBridge.log(sb.toString());
        this.touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.audio = (AudioManager) context.getSystemService(AudioManager.class);
        this.display = (DisplayManager) context.getSystemService(DisplayManager.class);
        this.keyguard = (KeyguardManager) context.getSystemService(KeyguardManager.class);
        this.mediaSessions = (MediaSessionManager) context.getSystemService(MediaSessionManager.class);
        setVisibility(TILES_PER_ROW);
        Motion.Value value = motion.value(Logic.Spring.SNAPPY, new Motion.Listener() { // from class: dev.colorcc.Panel.110
            @Override // dev.colorcc.Motion.Listener
            public final void onValue(float f, float f2) {
                Panel.this.m31lambda$new$4$devcolorccPanel(f, f2);
            }
        });
        this.stockValue = value;
        value.snap(1.0f);
        this.closeRide = motion.value(Logic.Spring.SNAPPY, new Motion.Listener() { // from class: dev.colorcc.Panel.111
            @Override // dev.colorcc.Motion.Listener
            public final void onValue(float f, float f2) {
                Panel.this.m32lambda$new$5$devcolorccPanel(f, f2);
            }
        });
        this.pull = motion.value(Logic.Spring.PULL, new Motion.Listener() { // from class: dev.colorcc.Panel.112
            @Override // dev.colorcc.Motion.Listener
            public final void onValue(float f, float f2) {
                Panel.this.m35lambda$new$6$devcolorccPanel(f, f2);
            }
        });
        this.scroller = motion.value(SCROLL_GLIDE, new Motion.Listener() { // from class: dev.colorcc.Panel.113
            @Override // dev.colorcc.Motion.Listener
            public final void onValue(float f, float f2) {
                Panel.this.m36lambda$new$7$devcolorccPanel(f, f2);
            }
        });
        this.peek = motion.value(PEEK, new Motion.Listener() { // from class: dev.colorcc.Panel.114
            @Override // dev.colorcc.Motion.Listener
            public final void onValue(float f, float f2) {
                Panel.this.m38lambda$new$9$devcolorccPanel(f, f2);
            }
        });
        Motion.Value value2 = motion.value(BLUR_ARRIVE, new Motion.Listener() { // from class: dev.colorcc.Panel.96
            @Override // dev.colorcc.Motion.Listener
            public final void onValue(float f, float f2) {
                Panel.this.m18lambda$new$11$devcolorccPanel(f, f2);
            }
        });
        this.blurArrive = value2;
        value2.snap(1.0f);
        Column column = new Column(context);
        this.column = column;
        column.setOrientation(1);
        column.setClipChildren(false);
        column.setClipToPadding(false);
        column.setPadding(dp(24.0f), dp(64.0f), dp(24.0f), 0);
        LinearLayout linearLayout = new LinearLayout(context);
        this.side = linearLayout;
        linearLayout.setOrientation(1);
        linearLayout.setClipChildren(false);
        linearLayout.setClipToPadding(false);
        linearLayout.setPadding(dp(24.0f), dp(64.0f), dp(24.0f), 0);
        linearLayout.setVisibility(8);
        LinearLayout linearLayout2 = new LinearLayout(context) { // from class: dev.colorcc.Panel.6
            @Override // android.view.View
            public boolean hasOverlappingRendering() {
                return Panel.this.buttonsLift == 0.0f;
            }
        };
        this.content = linearLayout2;
        linearLayout2.setOrientation(0);
        linearLayout2.setClipChildren(false);
        linearLayout2.setGravity(1);
        linearLayout2.addView(column, new LinearLayout.LayoutParams(0, -1, 1.0f));
        linearLayout2.addView(linearLayout, new LinearLayout.LayoutParams(dp(340.0f), -1));
        addView(linearLayout2, new FrameLayout.LayoutParams(-1, -1));
        setClipChildren(false);
        Glass.bendRoot = linearLayout2;
        guard(new Runnable() { // from class: dev.colorcc.Panel.97
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m19lambda$new$12$devcolorccPanel();
            }
        });
        Disc disc = new Disc(context, dp(DROP_HEADER));
        this.pencil = disc;
        disc.icon.setImageDrawable(Icons.of("M3,17.25V21h3.75L17.81,9.94l-3.75,-3.75L3,17.25zM20.71,7.04c0.39,-0.39 0.39,-1.02 0,-1.41l-2.34,-2.34c-0.39,-0.39 -1.02,-0.39 -1.41,0l-1.83,1.83 3.75,3.75 1.83,-1.83z"));
        disc.setContentDescription(word("edit_tiles", new Object[0]));
        disc.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.98
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                Panel.this.m20lambda$new$13$devcolorccPanel(view);
            }
        });
        Disc discActive = new Disc(context, dp(DROP_HEADER));
        this.activeApps = discActive;
        discActive.icon.setImageDrawable(Icons.of("M11.99,18.54l-7.37,-5.73L3,14.07l9,7 9,-7 -1.63,-1.27 -7.38,5.74zM12,16l7.36,-5.73L21,9l-9,-7 -9,7 1.63,1.27L12,16z"));
        TextView textViewActiveCount = label(context, 13.0f, -1, 1);
        this.activeAppsCount = textViewActiveCount;
        textViewActiveCount.setTypeface(Typeface.create("sans-serif-medium", 0));
        textViewActiveCount.setGravity(17);
        textViewActiveCount.setIncludeFontPadding(false);
        textViewActiveCount.setVisibility(8);
        discActive.addView(textViewActiveCount, new FrameLayout.LayoutParams(-1, -1, 17));
        View viewActiveDot = new View(context);
        this.activeAppsDot = viewActiveDot;
        GradientDrawable dotDrawable = new GradientDrawable();
        dotDrawable.setShape(GradientDrawable.OVAL);
        dotDrawable.setColor(theme(ACTIVE_BLUE));
        viewActiveDot.setBackground(dotDrawable);
        viewActiveDot.setVisibility(8);
        FrameLayout.LayoutParams dotParams = new FrameLayout.LayoutParams(dp(7.0f), dp(7.0f), 85);
        dotParams.rightMargin = dp(1.0f);
        dotParams.bottomMargin = dp(1.0f);
        discActive.addView(viewActiveDot, dotParams);
        discActive.setContentDescription(word("active_apps", new Object[0]));
        discActive.setOnClickListener(new View.OnClickListener() {
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                Panel.this.guard(new Runnable() {
                    @Override // java.lang.Runnable
                    public final void run() {
                        Panel.this.swell(Panel.this.activeApps, new Runnable() {
                            @Override // java.lang.Runnable
                            public final void run() {
                                ColorCCBridge.openActiveAppsDialog();
                            }
                        });
                    }
                });
            }
        });
        Disc disc2 = new Disc(context, dp(DROP_HEADER));
        this.gear = disc2;
        disc2.icon.setImageDrawable(Icons.of("M11.25,2.43L11.51,1.01L12.49,1.01L12.75,2.43A9.6,9.6 0 0,1 13.75,2.56L13.75,2.56L14.37,1.26L15.32,1.51L15.2,2.95A9.6,9.6 0 0,1 16.13,3.34L16.13,3.34L17.07,2.24L17.92,2.73L17.44,4.09A9.6,9.6 0 0,1 18.23,4.7L18.23,4.7L19.42,3.88L20.12,4.58L19.3,5.77A9.6,9.6 0 0,1 19.91,6.56L19.91,6.56L21.27,6.08L21.76,6.93L20.66,7.87A9.6,9.6 0 0,1 21.05,8.8L21.05,8.8L22.49,8.68L22.74,9.63L21.44,10.25A9.6,9.6 0 0,1 21.57,11.25L21.57,11.25L22.99,11.51L22.99,12.49L21.57,12.75A9.6,9.6 0 0,1 21.44,13.75L21.44,13.75L22.74,14.37L22.49,15.32L21.05,15.2A9.6,9.6 0 0,1 20.66,16.13L20.66,16.13L21.76,17.07L21.27,17.92L19.91,17.44A9.6,9.6 0 0,1 19.3,18.23L19.3,18.23L20.12,19.42L19.42,20.12L18.23,19.3A9.6,9.6 0 0,1 17.44,19.91L17.44,19.91L17.92,21.27L17.07,21.76L16.13,20.66A9.6,9.6 0 0,1 15.2,21.05L15.2,21.05L15.32,22.49L14.37,22.74L13.75,21.44A9.6,9.6 0 0,1 12.75,21.57L12.75,21.57L12.49,22.99L11.51,22.99L11.25,21.57A9.6,9.6 0 0,1 10.25,21.44L10.25,21.44L9.63,22.74L8.68,22.49L8.8,21.05A9.6,9.6 0 0,1 7.87,20.66L7.87,20.66L6.93,21.76L6.08,21.27L6.56,19.91A9.6,9.6 0 0,1 5.77,19.3L5.77,19.3L4.58,20.12L3.88,19.42L4.7,18.23A9.6,9.6 0 0,1 4.09,17.44L4.09,17.44L2.73,17.92L2.24,17.07L3.34,16.13A9.6,9.6 0 0,1 2.95,15.2L2.95,15.2L1.51,15.32L1.26,14.37L2.56,13.75A9.6,9.6 0 0,1 2.43,12.75L2.43,12.75L1.01,12.49L1.01,11.51L2.43,11.25A9.6,9.6 0 0,1 2.56,10.25L2.56,10.25L1.26,9.63L1.51,8.68L2.95,8.8A9.6,9.6 0 0,1 3.34,7.87L3.34,7.87L2.24,6.93L2.73,6.08L4.09,6.56A9.6,9.6 0 0,1 4.7,5.77L4.7,5.77L3.88,4.58L4.58,3.88L5.77,4.7A9.6,9.6 0 0,1 6.56,4.09L6.56,4.09L6.08,2.73L6.93,2.24L7.87,3.34A9.6,9.6 0 0,1 8.8,2.95L8.8,2.95L8.68,1.51L9.63,1.26L10.25,2.56A9.6,9.6 0 0,1 11.25,2.43ZM12,4.1A7.9,7.9 0 1,0 12,19.9A7.9,7.9 0 1,0 12,4.1ZM12,12.5L3.7,12.5L3.7,11.5L12,11.5ZM11.57,11.75L15.72,4.56L16.58,5.06L12.43,12.25ZM12.43,11.75L16.58,18.94L15.72,19.44L11.57,12.25ZM12,10.9A1.1,1.1 0 1,1 12,13.1A1.1,1.1 0 1,1 12,10.9Z"));
        disc2.setContentDescription(word("settings", new Object[0]));
        disc2.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.99
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                Panel.this.m22lambda$new$15$devcolorccPanel(view);
            }
        });
        Disc discPower = new Disc(context, dp(DROP_HEADER));
        this.power = discPower;
        discPower.icon.setImageDrawable(Icons.of("M13,3h-2v10h2V3zM17.83,5.17l-1.42,1.42C17.99,7.86 19,9.81 19,12c0,3.87 -3.13,7 -7,7s-7,-3.13 -7,-7c0,-2.19 1.01,-4.14 2.58,-5.42L6.17,5.17C4.23,6.82 3,9.26 3,12c0,4.97 4.03,9 9,9s9,-4.03 9,-9c0,-2.74 -1.23,-5.18 -3.17,-6.83z"));
        discPower.setContentDescription(word("power_menu", new Object[0]));
        discPower.setOnClickListener(new View.OnClickListener() {
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                Panel.this.guard(new Runnable() {
                    @Override // java.lang.Runnable
                    public final void run() {
                        Panel.this.swell(Panel.this.power, new Runnable() {
                            @Override // java.lang.Runnable
                            public final void run() {
                                ColorCCBridge.openPowerMenu(Panel.this.getContext());
                            }
                        });
                    }
                });
            }
        });
        TextView textViewLabel = label(context, DROP_STEP, -1, 1);
        this.vpn = textViewLabel;
        textViewLabel.setGravity(17);
        textViewLabel.setIncludeFontPadding(false);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(LIVE_DIM);
        gradientDrawable.setCornerRadius(dp(14.0f));
        textViewLabel.setBackground(gradientDrawable);
        textViewLabel.setPadding(dp(DROP_STEP), 0, dp(14.0f), 0);
        textViewLabel.setShadowLayer(dp(3.0f), 0.0f, dp(1.0f), -1728053248);
        Drawable drawableOf = Icons.of("M12.65,10C11.83,7.67 9.61,6 7,6c-3.31,0 -6,2.69 -6,6s2.69,6 6,6c2.61,0 4.83,-1.67 5.65,-4H17v4h4v-4h2v-4H12.65zM7,14c-1.1,0 -2,-0.9 -2,-2s0.9,-2 2,-2 2,0.9 2,2 -0.9,2 -2,2z");
        drawableOf.setTint(-1);
        drawableOf.setBounds(0, 0, dp(13.0f), dp(13.0f));
        textViewLabel.setCompoundDrawables(drawableOf, null, null, null);
        textViewLabel.setCompoundDrawablePadding(dp(6.0f));
        textViewLabel.setVisibility(8);
        textViewLabel.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.100
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                Panel.this.m24lambda$new$17$devcolorccPanel(view);
            }
        });
        LinearLayout linearLayoutHorizontal = horizontal(context);
        this.headerRow = linearLayoutHorizontal;
        linearLayoutHorizontal.setGravity(16);
        linearLayoutHorizontal.setClipChildren(false);
        linearLayoutHorizontal.addView(disc, headerDisc(0));
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setClipChildren(false);
        frameLayout.addView(textViewLabel, new FrameLayout.LayoutParams(-2, dp(28.0f), 17));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, -1, 1.0f);
        int r10 = dp(8.0f);
        layoutParams.rightMargin = r10;
        layoutParams.leftMargin = r10;
        linearLayoutHorizontal.addView(frameLayout, layoutParams);
        linearLayoutHorizontal.addView(discActive, headerDisc(0));
        linearLayoutHorizontal.addView(disc2, headerDisc(dp(8.0f)));
        linearLayoutHorizontal.addView(discPower, headerDisc(dp(8.0f)));
        linearLayoutHorizontal.setMinimumHeight(dp(32.0f));
        column.addView(linearLayoutHorizontal, new LinearLayout.LayoutParams(-1, -2));
        Card card = new Card(context);
        this.wifiCard = card;
        Card card2 = new Card(context);
        this.bluetoothCard = card2;
        MediaCard mediaCard = new MediaCard(context);
        this.media = mediaCard;
        Slider slider = new Slider(context, motion, dp(26.0f), dp(ARC));
        this.brightness = slider;
        slider.setIcon(Icons.of("M12,7c-2.76,0 -5,2.24 -5,5s2.24,5 5,5 5,-2.24 5,-5 -2.24,-5 -5,-5zM2,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1H2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1zM20,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1h-2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1zM11,2v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1V2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1zM11,20v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1v-2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1zM5.99,4.58c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39,0.39 1.03,0.39 1.41,0s0.39,-1.03 0,-1.41L5.99,4.58zM18.36,16.95c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39,0.39 1.03,0.39 1.41,0 0.39,-0.39 0.39,-1.03 0,-1.41l-1.06,-1.06zM19.42,5.99c0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06zM7.05,18.36c0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06z"));
        slider.setIconSpin(180.0f);
        slider.setIconColor(-874996);
        slider.setContentDescription(word("brightness", new Object[0]));
        slider.setListener(new Slider.Listener() { // from class: dev.colorcc.Panel.101
            @Override // dev.colorcc.Slider.Listener
            public final void onChange(float f, boolean z) {
                Panel.this.onBrightness(f, z);
            }
        });
        slider.setBackground(new Glass(-1.0f, GLASS_IDLE, GLASS_IDLE, 0.5f, 0.5f).recessed());
        Slider slider2 = new Slider(context, motion, dp(26.0f), dp(ARC));
        this.volume = slider2;
        slider2.setIcon(Icons.speaker());
        slider2.setIconColor(theme(ACTIVE_BLUE));
        slider2.setContentDescription(word("media_volume", new Object[0]));
        slider2.setListener(new Slider.Listener() { // from class: dev.colorcc.Panel.102
            @Override // dev.colorcc.Slider.Listener
            public final void onChange(float f, boolean z) {
                Panel.this.onVolume(f, z);
            }
        });
        slider2.setOnHold(new Runnable() { // from class: dev.colorcc.Panel.103
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m26lambda$new$19$devcolorccPanel();
            }
        });
        slider.setOnHold(new Runnable() { // from class: dev.colorcc.Panel.104
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m29lambda$new$21$devcolorccPanel();
            }
        });
        slider2.setBackground(new Glass(-1.0f, GLASS_IDLE, GLASS_IDLE, 0.5f, 0.5f).recessed());
        FrameLayout frameLayoutHost = host(context, slider);
        this.brightnessHost = frameLayoutHost;
        FrameLayout frameLayoutHost2 = host(context, slider2);
        this.volumeHost = frameLayoutHost2;
        RingerPill ringerPill = new RingerPill(context);
        this.ringer = ringerPill;
        Card card3 = new Card(context);
        this.simCard = card3;
        Blocks blocks = new Blocks(context);
        this.blocks = blocks;
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams2.topMargin = dp(20.0f);
        column.addView(blocks, layoutParams2);
        Pager pager = new Pager(context);
        this.pager = pager;
        FrameLayout frameLayout2 = new FrameLayout(context);
        this.tilesHost = frameLayout2;
        frameLayout2.setClipChildren(false);
        frameLayout2.addView(pager, new FrameLayout.LayoutParams(-1, -2));
        Dots dots = new Dots(context);
        this.dots = dots;
        column.addView(dots, new LinearLayout.LayoutParams(-1, dp(20.0f)));
        blocks.setTranslationZ(-1.0f);
        pager.setTranslationZ(-2.0f);
        dots.setTranslationZ(-3.0f);
        addStaticBody(disc, 0, true);
        addStaticBody(discActive, 1, true);
        addStaticBody(disc2, 1, true);
        addStaticBody(discPower, 1, true);
        addStaticBody(textViewLabel, 2, true);
        addStaticBody(card, 2, true);
        addStaticBody(card2, 3, true);
        addStaticBody(mediaCard, TILES_PER_ROW, true);
        addStaticBody(frameLayoutHost, 5, false);
        addStaticBody(frameLayoutHost2, 6, false);
        addStaticBody(ringerPill, 7, false);
        addStaticBody(card3, 8, true);
        addStaticBody(dots, 21, false);
        arrayList.add(slider);
        arrayList.add(slider2);
        arrayList.add(ringerPill);
        Editor editor = new Editor(context);
        this.editor = editor;
        addView(editor, new FrameLayout.LayoutParams(-1, -1));
        arrayList.add(this.editor);
        Sheet sheet = new Sheet(context);
        this.sheet = sheet;
        addView(sheet, new FrameLayout.LayoutParams(-1, -1));
        Glass.menuRoot = this.sheet;
        Glass.slideRoot = this;
        Glass.moving = setNewSetFromMap;
        arrayList.add(this.sheet);
        Player player = new Player(context);
        this.player = player;
        addView(player, new FrameLayout.LayoutParams(-1, -1));
        arrayList.add(this.player);
        guard(new Runnable() { // from class: dev.colorcc.Panel.105
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m30lambda$new$22$devcolorccPanel();
            }
        });
        if (this.layout.indexOf(103) < 0) {
            String[] strArrWithTiles = Logic.withTiles(this.layout, null);
            this.layout = strArrWithTiles[0];
            this.places = strArrWithTiles[1];
        }
        blocks.arrange(this.layout);
    }

    /* JADX INFO: renamed from: lambda$new$4$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m31lambda$new$4$devcolorccPanel(float f, float f2) {
        Iterator<View> it = this.stock.iterator();
        while (it.hasNext()) {
            it.next().setAlpha(Logic.clamp(f));
        }
    }

    /* JADX INFO: renamed from: lambda$new$5$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m32lambda$new$5$devcolorccPanel(float f, float f2) {
        applySheet();
    }

    /* JADX INFO: renamed from: lambda$new$6$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m35lambda$new$6$devcolorccPanel(float f, float f2) {
        Iterator<Body> it = this.staticBodies.iterator();
        while (it.hasNext()) {
            it.next().apply();
        }
        Iterator<Body> it2 = this.tileBodies.iterator();
        while (it2.hasNext()) {
            it2.next().apply();
        }
        if (Glass.look == 1 && Glass.bending()) {
            reglaze(this);
        }
    }

    /* JADX INFO: renamed from: lambda$new$7$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m36lambda$new$7$devcolorccPanel(float f, float f2) {
        scrollCards(f);
    }

    /* JADX INFO: renamed from: lambda$new$9$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m38lambda$new$9$devcolorccPanel(float f, float f2) {
        Iterator<Body> it = this.staticBodies.iterator();
        while (it.hasNext()) {
            it.next().apply();
        }
        Iterator<Body> it2 = this.tileBodies.iterator();
        while (it2.hasNext()) {
            it2.next().apply();
        }
        if (this.liveOn) {
            guard(new Runnable() { // from class: dev.colorcc.Panel.26
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m37lambda$new$8$devcolorccPanel();
                }
            });
        }
        ColorCCBridge.fadeShadeBlur(1.0f - Logic.clamp(f));
        float fRound = Math.round((1.0f - Logic.clamp(f)) * 8.0f) / 8.0f;
        if (fRound != ColorCCBridge.peekClear) {
            ColorCCBridge.peekClear = fRound;
            if (fRound >= 1.0f || ColorCCBridge.samsungLive()) {
                guard(new AnonymousClass27());
            }
        }
    }

    /* JADX INFO: renamed from: lambda$new$8$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m37lambda$new$8$devcolorccPanel() {
        m69lambda$onShadeExpansion$46$devcolorccPanel(this.lastExpansion);
    }

    /* JADX INFO: renamed from: lambda$new$10$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m17lambda$new$10$devcolorccPanel() {
        m69lambda$onShadeExpansion$46$devcolorccPanel(this.lastExpansion);
    }

    /* JADX INFO: renamed from: lambda$new$11$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m18lambda$new$11$devcolorccPanel(float f, float f2) {
        if (this.liveOn) {
            guard(new Runnable() { // from class: dev.colorcc.Panel.128
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m17lambda$new$10$devcolorccPanel();
                }
            });
        }
        float fRound = Math.round(Logic.clamp(f) * 8.0f) / 8.0f;
        if (fRound != ColorCCBridge.blurArrived) {
            ColorCCBridge.blurArrived = fRound;
            if (fRound >= 1.0f || ColorCCBridge.samsungLive()) {
                guard(new AnonymousClass27());
            }
        }
    }

    /* JADX INFO: renamed from: lambda$new$12$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m19lambda$new$12$devcolorccPanel() {
        SharedPreferences sharedPreferencesPrefs = prefs();
        Glass.look = Math.max(0, Math.min(3, sharedPreferencesPrefs.getInt("look", sharedPreferencesPrefs.getBoolean("blur_only", false) ? 1 : 0)));
        this.iconMode = Math.max(0, Math.min(2, sharedPreferencesPrefs.getInt("iconColours", sharedPreferencesPrefs.getBoolean("systemIcons", false) ? 1 : 0)));
        this.customColour = sharedPreferencesPrefs.getInt("customColour", ACTIVE_BLUE) | (-16777216);
        this.tileNames = sharedPreferencesPrefs.getBoolean("tileNames", true);
        Glass.plainColour = Math.max(0, Math.min(2, sharedPreferencesPrefs.getInt("plainColour", 0)));
        PageSwipe.moveRows = sharedPreferencesPrefs.getBoolean("notifSwipe", false);
        float[][] fArrTunes = Logic.tunes(sharedPreferencesPrefs.getString("glassTune", null), 3, 7);
        if (!sharedPreferencesPrefs.contains("glassTune")) {
            for (float[] fArr : fArrTunes) {
                fArr[0] = Glass.OLD_STEPS[Math.max(0, Math.min(2, sharedPreferencesPrefs.getInt("glassTint", 1)))];
                fArr[1] = Glass.OLD_STEPS[Math.max(0, Math.min(2, sharedPreferencesPrefs.getInt("edgeShine", 1)))];
            }
        }
        for (int r4 = 0; r4 < 3; r4++) {
            for (int r6 = 0; r6 < 7; r6++) {
                Glass.tune[r4][r6] = Math.max(Glass.TUNE_RANGE[r6][0], Math.min(Glass.TUNE_RANGE[r6][1], fArrTunes[r4][r6]));
            }
        }
        Slider.jelly = Math.max(0.0f, Math.min(1.5f, sharedPreferencesPrefs.getFloat("jelly", 1.0f)));
        ColorCCBridge.notifGlass = sharedPreferencesPrefs.getBoolean("notifGlass", true);
        ColorCCBridge.footerGlass = sharedPreferencesPrefs.getBoolean("footerGlass", true);
        ColorCCBridge.glazeFooter();
        this.blur = Math.max(0, Math.min(3, sharedPreferencesPrefs.getInt("blur", !sharedPreferencesPrefs.getBoolean("light_blur", false) ? 1 : 0)));
        this.blurCustom = Math.max(0.0f, Math.min(BLUR_CUSTOM_MAX, sharedPreferencesPrefs.getFloat("blurCustom", 1.0f)));
    }

    /* JADX INFO: renamed from: lambda$new$13$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m20lambda$new$13$devcolorccPanel(View view) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.37
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.openEditor();
            }
        });
    }

    /* JADX INFO: renamed from: lambda$new$14$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m21lambda$new$14$devcolorccPanel() {
        openSettings("android.settings.SETTINGS");
    }

    /* JADX INFO: renamed from: lambda$new$15$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m22lambda$new$15$devcolorccPanel(View view) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.117
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m21lambda$new$14$devcolorccPanel();
            }
        });
    }

    /* JADX INFO: renamed from: lambda$new$16$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m23lambda$new$16$devcolorccPanel() {
        openSettings("android.settings.VPN_SETTINGS");
    }

    /* JADX INFO: renamed from: lambda$new$17$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m24lambda$new$17$devcolorccPanel(View view) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.94
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m23lambda$new$16$devcolorccPanel();
            }
        });
    }

    /* JADX INFO: renamed from: lambda$new$18$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m25lambda$new$18$devcolorccPanel() {
        this.sheet.openVolumes();
    }

    /* JADX INFO: renamed from: lambda$new$19$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m26lambda$new$19$devcolorccPanel() {
        swell(this.volume, new Runnable() { // from class: dev.colorcc.Panel.13
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m25lambda$new$18$devcolorccPanel();
            }
        });
    }

    /* JADX INFO: renamed from: lambda$new$20$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m28lambda$new$20$devcolorccPanel() {
        this.sheet.openBrightness();
    }

    /* JADX INFO: renamed from: lambda$new$21$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m29lambda$new$21$devcolorccPanel() {
        swell(this.brightness, new Runnable() { // from class: dev.colorcc.Panel.17
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m28lambda$new$20$devcolorccPanel();
            }
        });
    }

    /* JADX INFO: renamed from: lambda$new$22$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m30lambda$new$22$devcolorccPanel() {
        String[] strArrWithTiles = Logic.withTiles(Logic.layout(prefs().getString("layout", null)), prefs().getString("places", null));
        this.layout = strArrWithTiles[0];
        this.places = strArrWithTiles[1];
        this.pins.putAll(Logic.pins(prefs().getString("pins", null)));
        this.tileRows = Math.max(1, Math.min(TILES_PER_ROW, prefs().getInt("tileRows", 3)));
        ColorCCBridge.samsungLiveSeen = prefs().getBoolean("samsungLive", false);
    }

    private final class Column extends LinearLayout {
        private final Paint fade;
        private boolean underHeader;

        Column(Context context) {
            super(context);
            Paint paint = new Paint();
            this.fade = paint;
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        }

        @Override // android.view.ViewGroup, android.view.View
        protected void dispatchDraw(Canvas canvas) {
            int scrollY = getScrollY();
            if (scrollY > 0 && Panel.this.headerRow.getParent() == this) {
                float bottom = Panel.this.headerRow.getBottom() + scrollY + Panel.this.dp(4.0f);
                float fDp = Panel.this.dp(64.0f);
                int r13 = canvas.saveLayer(0.0f, bottom, getWidth(), getHeight() + scrollY, null);
                this.underHeader = true;
                super.dispatchDraw(canvas);
                this.underHeader = false;
                float f = bottom + fDp;
                this.fade.setShader(new LinearGradient(0.0f, bottom, 0.0f, f, Math.round(Logic.clamp(scrollY / fDp) * 255.0f) << 24, 0, Shader.TileMode.CLAMP));
                canvas.drawRect(0.0f, bottom, getWidth(), f, this.fade);
                canvas.restoreToCount(r13);
                super.drawChild(canvas, Panel.this.headerRow, getDrawingTime());
                return;
            }
            super.dispatchDraw(canvas);
        }

        @Override // android.view.ViewGroup
        protected boolean drawChild(Canvas canvas, View view, long j) {
            return !(this.underHeader && view == Panel.this.headerRow) && super.drawChild(canvas, view, j);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public SharedPreferences prefs() {
        return getContext().createDeviceProtectedStorageContext().getSharedPreferences("colorcc", 0);
    }

    private final class Blocks extends ViewGroup {
        private String order;
        private int[][] spans;
        private int[][] spots;

        Blocks(Context context) {
            super(context);
            this.order = "";
            this.spots = new int[0][];
            this.spans = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, 2, 0);
            setClipChildren(false);
        }

        void arrange(String str) {
            int r4;
            int r5;
            this.spans = Logic.sizes(Panel.this.places, str);
            this.spots = Logic.spots(Panel.this.places, str, Panel.TILES_PER_ROW);
            int r1 = str.indexOf(103);
            if (r1 >= 0) {
                r4 = this.spots[r1][1];
                ArrayList arrayList = new ArrayList(Arrays.asList(this.spots));
                arrayList.remove(r1);
                int[][] r6 = this.spots;
                int[][] r7 = {new int[r6.length - 1], new int[r6.length - 1]};
                int r8 = 0;
                for (int r9 = 0; r9 < this.spots.length; r9++) {
                    if (r9 != r1) {
                        int[] r10 = r7[0];
                        int[][] r11 = this.spans;
                        r10[r8] = r11[0][r9];
                        r7[1][r8] = r11[1][r9];
                        r8++;
                    }
                }
                this.spans = r7;
                this.spots = (int[][]) arrayList.toArray(new int[0][]);
                str = str.substring(0, r1) + str.substring(r1 + 1);
            } else {
                r4 = Integer.MAX_VALUE;
            }
            this.order = str;
            removeAllViews();
            for (int r12 = 0; r12 < str.length(); r12++) {
                View viewViewOf = viewOf(Character.toLowerCase(str.charAt(r12)));
                if (viewViewOf instanceof Card) {
                    int[][] r13 = this.spans;
                    ((Card) viewViewOf).shape(r13[0][r12], r13[1][r12]);
                }
                addView(viewViewOf);
            }
            int length = this.spots.length;
            for (Map.Entry entry : Panel.this.pins.entrySet()) {
                View view = (View) Panel.this.pinViews.get(entry.getKey());
                if (view != null) {
                    int[][] r14 = this.spots;
                    int length2 = r14.length;
                    int r15 = length2 + 1;
                    int[][] r16 = (int[][]) Arrays.copyOf(r14, r15);
                    int[][] r17 = {Arrays.copyOf(this.spans[0], r15), Arrays.copyOf(this.spans[1], r15)};
                    int[] r18 = Logic.pinSize((int[]) entry.getValue());
                    r17[0][length2] = r18[0];
                    r17[1][length2] = r18[1];
                    if (view instanceof Card) {
                        ((Card) view).shape(r18[0], r18[1]);
                    }
                    r16[length2] = Logic.nearest(r17[0], r17[1], r16, length2, (int[]) entry.getValue(), Panel.TILES_PER_ROW);
                    this.spots = r16;
                    this.spans = r17;
                    addView(view);
                }
            }
            if (r1 >= 0) {
                int[][] r19 = Logic.liftWith(this.spots, this.spans[1], r4);
                int[][] r2 = this.spots;
                r4 = r19[r2.length][1];
                this.spots = (int[][]) Arrays.copyOf(r19, r2.length);
            }
            if (Panel.this.twoPane) {
                r5 = 11;
            } else {
                int length3 = this.spots.length;
                int r3 = (int) Math.ceil(((Panel.this.gridRowCount * Panel.this.tileRow()) + 14) / 77.0f);
                int r20 = Logic.tilesRow(this.spots, this.spans[1], r4);
                int[][] r21 = Logic.below(this.spots, r20, r3);
                this.spots = r21;
                int r22 = length3 + 1;
                int[][] r23 = (int[][]) Arrays.copyOf(r21, r22);
                this.spots = r23;
                r23[length3] = new int[]{0, r20};
                int[][] r24 = {Arrays.copyOf(this.spans[0], r22), Arrays.copyOf(this.spans[1], r22)};
                this.spans = r24;
                r24[0][length3] = Logic.spanW('g');
                this.spans[1][length3] = r3;
                addView(Panel.this.tilesHost);
                r5 = (r20 * 2) + 3;
            }
            for (int r110 = 0; r110 < getChildCount(); r110++) {
                View childAt = getChildAt(r110);
                if (childAt != Panel.this.tilesHost) {
                    int[] r25 = this.spots[r110];
                    int r26 = (r25[1] * 2) + 3 + (r25[0] / 2);
                    Body bodyBodyOf = Panel.this.bodyOf(childAt);
                    if (bodyBodyOf != null) {
                        bodyBodyOf.order = r26;
                        childAt.setTranslationZ(-r26);
                    }
                    for (Body body : Panel.this.tileBodies) {
                        if (body.view == childAt) {
                            body.order = r26;
                        }
                    }
                }
            }
            for (Body body2 : Panel.this.tileBodies) {
                if (body2.view.getParent() != this) {
                    body2.order += r5 - Panel.this.tileBase;
                }
            }
            Panel.this.tileBase = r5;
            requestLayout();
        }

        private View viewOf(char c) {
            if (c == Panel.TILE_ROW_HEIGHT) {
                return Panel.this.bluetoothCard;
            }
            if (c == 's') {
                return Panel.this.ringer;
            }
            if (c == 'l') {
                return Panel.this.brightnessHost;
            }
            if (c == 'm') {
                return Panel.this.media;
            }
            if (c == 'v') {
                return Panel.this.volumeHost;
            }
            Panel panel = Panel.this;
            return c != 'w' ? panel.simCard : panel.wifiCard;
        }

        private int edge(int r2, int r3) {
            return Math.round((r2 * (r3 + Panel.this.dp(14.0f))) / 4.0f);
        }

        @Override // android.view.View
        protected void onMeasure(int r9, int r10) {
            int r2;
            int size = View.MeasureSpec.getSize(r9);
            int r0 = 0;
            int r1 = 0;
            while (true) {
                int[][] r3 = this.spots;
                if (r0 < r3.length) {
                    int r4 = r3[r0][0];
                    int r5 = (edge(this.spans[0][r0] + r4, size) - Panel.this.dp(14.0f)) - edge(r4, size);
                    if (getChildAt(r0) == Panel.this.tilesHost) {
                        r5 += Panel.this.dp(24.0f) * 2;
                    }
                    if (getChildAt(r0) == Panel.this.tilesHost) {
                        int r6 = Panel.this.gridRowCount;
                        Panel panel = Panel.this;
                        r2 = r6 * panel.dp(panel.tileRow());
                    } else {
                        r2 = (this.spans[1][r0] * Panel.this.dp(63.0f)) + ((this.spans[1][r0] - 1) * Panel.this.dp(14.0f));
                    }
                    getChildAt(r0).measure(View.MeasureSpec.makeMeasureSpec(r5, 1073741824), View.MeasureSpec.makeMeasureSpec(r2, 1073741824));
                    r1 = Math.max(r1, rowTop(this.spots[r0][1]) + r2);
                    r0++;
                } else {
                    setMeasuredDimension(size, r1);
                    return;
                }
            }
        }

        private int rowTop(int r7) {
            int r0 = Panel.this.dp(63.0f) + Panel.this.dp(14.0f);
            int r1 = indexOfChild(Panel.this.tilesHost);
            if (r1 >= 0) {
                int[][] r3 = this.spots;
                if (r1 < r3.length) {
                    int r4 = r3[r1][1];
                    int r2 = this.spans[1][r1];
                    if (r7 >= r4 + r2) {
                        int r8 = r7 * r0;
                        int r5 = r2 * r0;
                        int r6 = Panel.this.gridRowCount;
                        Panel panel = Panel.this;
                        return r8 - ((r5 - (r6 * panel.dp(panel.tileRow()))) - Panel.this.dp(14.0f));
                    }
                }
            }
            return r7 * r0;
        }

        @Override // android.view.ViewGroup, android.view.View
        protected void onLayout(boolean z, int r6, int r7, int r8, int r9) {
            for (int r10 = 0; r10 < this.spots.length; r10++) {
                View childAt = getChildAt(r10);
                int r0 = edge(this.spots[r10][0], r8 - r6) - (childAt == Panel.this.tilesHost ? Panel.this.dp(24.0f) : 0);
                int r1 = rowTop(this.spots[r10][1]);
                childAt.layout(r0, r1, childAt.getMeasuredWidth() + r0, childAt.getMeasuredHeight() + r1);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Motion.Value spring(final View view, final Property<View, Float> property, float[] fArr) {
        Map map = (Map) view.getTag(SPRINGS);
        if (map == null) {
            map = new HashMap();
            view.setTag(SPRINGS, map);
        }
        Motion.Value value = (Motion.Value) map.get(property);
        if (value == null) {
            final FloatProperty floatProperty = property instanceof FloatProperty ? (FloatProperty) property : null;
            value = this.motion.value(fArr, floatProperty != null ? new Motion.Listener() { // from class: dev.colorcc.Panel.67
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    floatProperty.setValue(view, f);
                }
            } : new Motion.Listener() { // from class: dev.colorcc.Panel.68
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Panel.lambda$spring$24(property, view, f, f2);
                }
            });
            map.put(property, value);
        }
        value.tune(fArr);
        value.track(property.get(view).floatValue());
        return value;
    }

    static /* synthetic */ void lambda$spring$24(Property property, View view, float f, float f2) {
        property.set(view, Float.valueOf(f));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void scaleTo(View view, float f, float[] fArr) {
        spring(view, View.SCALE_X, fArr).to(f);
        spring(view, View.SCALE_Y, fArr).to(f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void skipped(int r3) {
        this.skipWay = r3;
        this.skipAt = SystemClock.uptimeMillis();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void slideIn(View... viewArr) {
        int r0 = SystemClock.uptimeMillis() - this.skipAt < 3000 ? this.skipWay : 1;
        for (View view : viewArr) {
            Motion.Value valueSpring = spring(view, View.ALPHA, Logic.Spring.SNAPPY);
            Motion.Value valueSpring2 = spring(view, View.TRANSLATION_X, Logic.Spring.BOUNCY);
            valueSpring.snap(0.0f);
            valueSpring2.snap(dp(32.0f) * r0);
            valueSpring.to(1.0f);
            valueSpring2.to(0.0f);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void swellIn(View view) {
        Motion.Value valueSpring = spring(view, View.ALPHA, Logic.Spring.SNAPPY);
        valueSpring.snap(MEDIA_EDGE_LIGHT);
        valueSpring.to(1.0f);
        spring(view, View.SCALE_X, Logic.Spring.BOUNCY).snap(1.08f);
        spring(view, View.SCALE_Y, Logic.Spring.BOUNCY).snap(1.08f);
        scaleTo(view, 1.0f, Logic.Spring.BOUNCY);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void squish(View view) {
        view.setOnTouchListener(new View.OnTouchListener() { // from class: dev.colorcc.Panel.11
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view2, MotionEvent motionEvent) {
                return Panel.this.m83lambda$squish$25$devcolorccPanel(view2, motionEvent);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$squish$25$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ boolean m83lambda$squish$25$devcolorccPanel(View view, MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            scaleTo(view, 0.92f, Logic.Spring.PRESS_IN);
            return false;
        }
        if (actionMasked != 1 && actionMasked != 3) {
            return false;
        }
        scaleTo(view, 1.0f, Logic.Spring.PRESS_OUT);
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Drawable scrim() {
        return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{1929379840, 637534208, 1929379840});
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void backdrop() {
        RenderEffect renderEffectCreateBlurEffect;
        float fClamp = Logic.clamp(Math.max(this.sheetBack, this.playerBack));
        this.content.setAlpha(1.0f - Math.max(Logic.clamp(this.sheetBack) * 0.9f, Logic.clamp(this.playerBack) * MEDIA_EDGE_LIGHT));
        int r0 = Math.round(dp(24.0f) * fClamp);
        if (r0 == this.shownBlur) {
            return;
        }
        this.shownBlur = r0;
        LinearLayout linearLayout = this.content;
        if (r0 < 1) {
            renderEffectCreateBlurEffect = null;
        } else {
            float f = r0;
            renderEffectCreateBlurEffect = RenderEffect.createBlurEffect(f, f, Shader.TileMode.CLAMP);
        }
        linearLayout.setRenderEffect(renderEffectCreateBlurEffect);
    }

    private LinearLayout.LayoutParams pagerParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, this.gridRowCount * dp(tileRow()));
        layoutParams.leftMargin = -dp(24.0f);
        layoutParams.rightMargin = -dp(24.0f);
        return layoutParams;
    }

    private boolean landscape() {
        return getResources().getConfiguration().orientation == 2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void arrange() {
        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) {
            return;
        }
        boolean z = width > height || large();
        if (z != this.twoPane) {
            this.twoPane = z;
            ((ViewGroup) this.pager.getParent()).removeView(this.pager);
            ((ViewGroup) this.dots.getParent()).removeView(this.dots);
            LinearLayout linearLayout = z ? this.side : this.column;
            if (z) {
                this.side.addView(this.pager, pagerParams());
            } else {
                this.tilesHost.addView(this.pager, new FrameLayout.LayoutParams(-1, -2));
            }
            linearLayout.addView(this.dots, new LinearLayout.LayoutParams(-1, dp(20.0f)));
            this.column.setLayoutParams(z ? new LinearLayout.LayoutParams(dp(340.0f), -1) : new LinearLayout.LayoutParams(0, -1, 1.0f));
            this.side.setVisibility(z ? 0 : 8);
            this.blocks.arrange(this.layout);
        }
        int r4 = roomBesideNotes(width);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.content.getLayoutParams();
        int r9 = width - r4;
        if (marginLayoutParams.rightMargin != r9) {
            marginLayoutParams.rightMargin = r9;
            this.content.requestLayout();
            ColorCCBridge.log("large screen: notifications beside from " + r4 + " of " + width + " px, the panes kept left of them");
        }
        float f = r4;
        float fDp = f / (dp(340.0f) * 2);
        float fDp2 = this.headerPadding + (this.gridRowCount * dp(tileRow())) + dp(32.0f);
        View viewLastShown = lastShown(this.column);
        float bottom = viewLastShown == null ? 0.0f : viewLastShown.getBottom() + dp(DROP_STEP);
        this.arrangedCards = viewLastShown == null ? 0 : viewLastShown.getBottom();
        float fMin = z ? Math.min(1.0f, Math.min(fDp, Math.max(0.6f, visibleHeight() / Math.max(fDp2, bottom)))) : 1.0f;
        if (z) {
            int r0 = Math.max(dp(340.0f), (int) ((f / 2.0f) / fMin));
            View[] viewArr = {this.column, this.side};
            for (int r2 = 0; r2 < 2; r2++) {
                View view = viewArr[r2];
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                if (layoutParams.width != r0) {
                    layoutParams.width = r0;
                    view.requestLayout();
                }
            }
        }
        Column column = this.column;
        scalePane(column, fMin, z ? column.getLayoutParams().width : dp(340.0f));
        scalePane(this.side, fMin, 0);
        if (fMin != this.paneScale) {
            this.paneScale = fMin;
            this.editor.requestLayout();
        }
        measureScroll();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fitEditor() {
        int[] r1 = new int[2];
        int[] r0 = new int[2];
        getLocationOnScreen(r1);
        View rootView = getRootView();
        rootView.getLocationOnScreen(r0);
        WindowInsets rootWindowInsets = getRootWindowInsets();
        final int height = ((r0[1] + rootView.getHeight()) - Math.max(rootWindowInsets == null ? 0 : rootWindowInsets.getInsets(WindowInsets.Type.navigationBars()).bottom, dp(ARC))) - r1[1];
        if (height <= 0) {
            return;
        }
        final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.editor.getLayoutParams();
        if (layoutParams.height == height && layoutParams.bottomMargin == 0) {
            return;
        }
        post(new Runnable() { // from class: dev.colorcc.Panel.20
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m10lambda$fitEditor$26$devcolorccPanel(layoutParams, height);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$fitEditor$26$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m10lambda$fitEditor$26$devcolorccPanel(FrameLayout.LayoutParams layoutParams, int r2) {
        layoutParams.height = r2;
        layoutParams.bottomMargin = 0;
        this.editor.requestLayout();
    }

    private int roomBesideNotes(int r4) {
        View viewNoteStack;
        if (large() && (viewNoteStack = noteStack()) != null && viewNoteStack.isShown() && viewNoteStack.getWidth() != 0) {
            int[] r2 = new int[2];
            int[] r1 = new int[2];
            viewNoteStack.getLocationOnScreen(r2);
            getLocationOnScreen(r1);
            int r0 = r2[0] - r1[0];
            if (r0 > r4 / 3 && r0 < r4) {
                return r0;
            }
        }
        return r4;
    }

    private View lastShown(ViewGroup viewGroup) {
        View childAt = null;
        for (int r0 = 0; r0 < viewGroup.getChildCount(); r0++) {
            if (viewGroup.getChildAt(r0).getVisibility() != 8) {
                childAt = viewGroup.getChildAt(r0);
            }
        }
        return childAt;
    }

    private int visibleHeight() {
        int r0 = layoutTop(this);
        WindowInsets rootWindowInsets = getRootWindowInsets();
        return Math.min(getRootView().getHeight() - Math.max(rootWindowInsets == null ? 0 : rootWindowInsets.getInsets(WindowInsets.Type.navigationBars()).bottom, dp(ARC)), getHeight() + r0) - r0;
    }

    private void measureScroll() {
        View viewLastShown = lastShown(this.column);
        int r1 = layoutTop(this);
        int r2 = visibleHeight() + r1;
        if (!this.twoPane && viewLastShown != null) {
            float top = (r2 - r1) - this.column.getTop();
            float bottom = viewLastShown.getBottom();
            float f = (bottom <= top || bottom - top > ((float) dp(40.0f))) ? 1.0f : top / bottom;
            if (this.column.getScaleY() != f) {
                Column column = this.column;
                scalePane(column, f, column.getWidth() / 2);
            }
        }
        int r0 = viewLastShown == null ? 0 : Math.round(viewLastShown.getBottom() - (((r2 - r1) - this.column.getTop()) / Math.max(STATUS_GROW, this.column.getScaleY())));
        this.scrollMax = r0 > dp(40.0f) ? r0 + dp(DROP_HEADER) : 0;
        float fValue = this.scroller.value();
        int r3 = this.scrollMax;
        if (fValue > r3) {
            this.scroller.snap(r3);
        }
    }

    private boolean overCards(float f) {
        if (!this.twoPane) {
            return true;
        }
        int[] r0 = new int[2];
        this.column.getLocationOnScreen(r0);
        return f <= ((float) r0[0]) + (((float) this.column.getWidth()) * this.column.getScaleX());
    }

    private static void scalePane(View view, float f, int r2) {
        view.setPivotX(r2);
        view.setPivotY(0.0f);
        view.setScaleX(f);
        view.setScaleY(f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void watchVpn() {
        NetworkRequest networkRequestBuild = new NetworkRequest.Builder().addTransportType(TILES_PER_ROW).removeCapability(15).removeCapability(12).build();
        this.vpnWatch = new ConnectivityManager.NetworkCallback() { // from class: dev.colorcc.Panel.8
            /* JADX INFO: renamed from: lambda$onCapabilitiesChanged$0$dev-colorcc-Panel$8, reason: not valid java name */
            /* synthetic */ String m97lambda$onCapabilitiesChanged$0$devcolorccPanel$8(NetworkCapabilities networkCapabilities) throws Exception {
                return Panel.this.vpnName(networkCapabilities);
            }

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onCapabilitiesChanged(Network network, final NetworkCapabilities networkCapabilities) {
                Panel.this.vpnNetworks.put(network, (String) Panel.guarded(new Callable() { // from class: dev.colorcc.Panel.8.0
                    @Override // java.util.concurrent.Callable
                    public final Object call() throws Exception {
                        return m97lambda$onCapabilitiesChanged$0$devcolorccPanel$8(networkCapabilities);
                    }
                }));
                final Panel panel = Panel.this;
                panel.guard(new Runnable() { // from class: dev.colorcc.Panel.8.1
                    @Override // java.lang.Runnable
                    public final void run() {
                        Panel.access$4000(panel);
                    }
                });
            }

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onLost(Network network) {
                Panel.this.vpnNetworks.remove(network);
                final Panel panel = Panel.this;
                panel.guard(new Runnable() { // from class: dev.colorcc.Panel.8.2
                    @Override // java.lang.Runnable
                    public final void run() {
                        Panel.access$4000(panel);
                    }
                });
            }
        };
        ((ConnectivityManager) getContext().getSystemService(ConnectivityManager.class)).registerNetworkCallback(networkRequestBuild, this.vpnWatch, this.main);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showVpn() {
        String str = null;
        for (String str2 : this.vpnNetworks.values()) {
            if (str2 != null) {
                str = str2;
            }
        }
        setTextIfChanged(this.vpn, str == null ? "VPN" : str);
        this.vpn.setContentDescription(str == null ? word("vpn_connected", new Object[0]) : word("vpn_connected_to", str));
        int r2 = this.vpnNetworks.isEmpty() ? 8 : 0;
        if (this.vpn.getVisibility() != r2) {
            this.vpn.setVisibility(r2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String vpnName(NetworkCapabilities networkCapabilities) {
        String[] packagesForUid;
        PackageManager packageManager = getContext().getPackageManager();
        Object systemService = getContext().getSystemService("vpn_management");
        Integer num = (Integer) ReflectUtils.callStaticMethod(ActivityManager.class, "getCurrentUser", new Object[0]);
        num.intValue();
        Object objCallMethod = systemService == null ? null : ReflectUtils.callMethod(systemService, "getVpnConfig", new Object[]{num});
        String str = (objCallMethod == null || ReflectUtils.getBooleanField(objCallMethod, "legacy")) ? null : (String) ReflectUtils.getObjectField(objCallMethod, "user");
        if (str == null && networkCapabilities.getOwnerUid() > 0 && (packagesForUid = packageManager.getPackagesForUid(networkCapabilities.getOwnerUid())) != null && packagesForUid.length > 0) {
            str = packagesForUid[0];
        }
        if (str == null) {
            return null;
        }
        try {
            return packageManager.getApplicationLabel(packageManager.getApplicationInfo(str, 0)).toString();
        } catch (PackageManager.NameNotFoundException unused) {
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static <T> T guarded(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Throwable th) {
            ColorCCBridge.log("VPN name not read: " + th);
            return null;
        }
    }

    private LinearLayout.LayoutParams headerDisc(int r4) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(32.0f), dp(32.0f));
        layoutParams.leftMargin = r4;
        return layoutParams;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.stock.clear();
        this.header8 = null;
        StringBuilder sb = new StringBuilder();
        for (int r2 = 0; r2 < this.container.getChildCount(); r2++) {
            View childAt = this.container.getChildAt(r2);
            if (childAt != this) {
                String simpleName = childAt.getClass().getSimpleName();
                boolean z = true;
                boolean z2 = ColorCCBridge.oneUi8 && simpleName.equals("SecQuickStatusBarHeader");
                if (z2) {
                    this.header8 = childAt;
                }
                if (z2 || (!simpleName.equals("SecQSPanelComposeRoot") && !simpleName.equals("SecQSPanel") && !ColorCCBridge.oneUi8)) {
                    z = false;
                }
                if (z) {
                    this.stock.add(childAt);
                }
                sb.append(sb.length() == 0 ? "" : ", ");
                sb.append(simpleName);
                sb.append(z ? " (hidden)" : "");
                sb.append(z2 ? " (notifications page only)" : "");
                sb.append(childAt.getVisibility() != 0 ? " not shown" : "");
            }
        }
        ColorCCBridge.log("quick panel container holds: " + ((Object) sb));
        getViewTreeObserver().addOnPreDrawListener(this.preDraw);
        watchFrames();
        guard(new Runnable() { // from class: dev.colorcc.Panel.46
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.watchVpn();
            }
        });
        guard(new Runnable() { // from class: dev.colorcc.Panel.47
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m40lambda$onAttachedToWindow$27$devcolorccPanel();
            }
        });
        this.main.postDelayed(new Runnable() { // from class: dev.colorcc.Panel.48
            @Override // java.lang.Runnable
            public final void run() {
                ColorCCBridge.clearCrashes();
            }
        }, STABLE_RUN_MS);
        this.main.postDelayed(new Runnable() { // from class: dev.colorcc.Panel.49
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m42lambda$onAttachedToWindow$29$devcolorccPanel();
            }
        }, PREBUILD_DELAY_MS);
    }

    /* JADX INFO: renamed from: lambda$onAttachedToWindow$27$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m40lambda$onAttachedToWindow$27$devcolorccPanel() {
        this.mediaSessions.addOnActiveSessionsChangedListener(this.sessionsListener, null, this.main);
        m16lambda$new$1$devcolorccPanel(this.mediaSessions.getActiveSessions(null));
    }

    /* JADX INFO: renamed from: lambda$onAttachedToWindow$29$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m42lambda$onAttachedToWindow$29$devcolorccPanel() {
        guard(new Runnable() { // from class: dev.colorcc.Panel.31
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m41lambda$onAttachedToWindow$28$devcolorccPanel();
            }
        });
    }

    /* JADX INFO: renamed from: lambda$onAttachedToWindow$28$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m41lambda$onAttachedToWindow$28$devcolorccPanel() {
        if (!this.open && this.currentTiles.isEmpty() && ColorCCBridge.host != null) {
            syncTiles();
        }
        if (!this.open && ColorCCBridge.adapter != null && ColorCCBridge.host != null && supportedScreen()) {
            parkStock(true);
        }
        Glass.prepare();
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (Math.abs(unitNow() - this.density) > 0.01f) {
            this.main.post(new Runnable() { // from class: dev.colorcc.Panel.92
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m53lambda$onConfigurationChanged$31$devcolorccPanel();
                }
            });
        }
        int r0 = configuration.orientation;
        int r1 = this.uiOrientation;
        if (r0 != r1) {
            boolean z = r1 != -1;
            this.uiOrientation = configuration.orientation;
            if (z && this.open) {
                this.main.postDelayed(new Runnable() { // from class: dev.colorcc.Panel.93
                    @Override // java.lang.Runnable
                    public final void run() {
                        Panel.this.m55lambda$onConfigurationChanged$33$devcolorccPanel();
                    }
                }, TURN_SETTLE_MS);
            }
        }
        int r6 = configuration.uiMode & 48;
        if (r6 != this.uiNight) {
            this.uiNight = r6;
            this.releaseGpu.run();
            this.backdropLogged = false;
            ColorCCBridge.themeLogs = TILES_PER_ROW;
            ColorCCBridge.log("light or dark switched: the GPU blur is made again");
        }
    }

    /* JADX INFO: renamed from: lambda$onConfigurationChanged$30$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m52lambda$onConfigurationChanged$30$devcolorccPanel() {
        ColorCCBridge.remake(this);
    }

    /* JADX INFO: renamed from: lambda$onConfigurationChanged$31$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m53lambda$onConfigurationChanged$31$devcolorccPanel() {
        guard(new Runnable() { // from class: dev.colorcc.Panel.119
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m52lambda$onConfigurationChanged$30$devcolorccPanel();
            }
        });
    }

    /* JADX INFO: renamed from: lambda$onConfigurationChanged$33$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m55lambda$onConfigurationChanged$33$devcolorccPanel() {
        guard(new Runnable() { // from class: dev.colorcc.Panel.30
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m54lambda$onConfigurationChanged$32$devcolorccPanel();
            }
        });
    }

    /* JADX INFO: renamed from: lambda$onConfigurationChanged$32$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m54lambda$onConfigurationChanged$32$devcolorccPanel() {
        if (this.open) {
            ColorCCBridge.log("turned while open: background pictures made again");
            ColorCCBridge.reblur();
            this.quickCapture = true;
            captureBackdrop();
        }
    }

    private float unitNow() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return large() ? displayMetrics.density * LARGE_UNIT_DP : Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) / 340.0f;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean large() {
        return getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    void retire() {
        this.audioThread.shutdown();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnPreDrawListener(this.preDraw);
        guard(new Runnable() { // from class: dev.colorcc.Panel.60
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m56lambda$onDetachedFromWindow$34$devcolorccPanel();
            }
        });
        guard(new Runnable() { // from class: dev.colorcc.Panel.61
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m57lambda$onDetachedFromWindow$35$devcolorccPanel();
            }
        });
        final ConnectivityManager.NetworkCallback networkCallback = this.vpnWatch;
        if (networkCallback != null) {
            this.vpnWatch = null;
            guard(new Runnable() { // from class: dev.colorcc.Panel.62
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m58lambda$onDetachedFromWindow$36$devcolorccPanel(networkCallback);
                }
            });
        }
        final Object obj = this.frameObserver;
        if (obj != null) {
            guard(new Runnable() { // from class: dev.colorcc.Panel.63
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m59lambda$onDetachedFromWindow$37$devcolorccPanel(obj);
                }
            });
            this.frameObserver = null;
        }
        this.main.removeCallbacksAndMessages(null);
        this.backdropBusy = false;
        this.ringRefreshQueued = false;
        final Motion motion = this.motion;
        Objects.requireNonNull(motion);
        guard(new Runnable() { // from class: dev.colorcc.Panel.64
            @Override // java.lang.Runnable
            public final void run() {
                motion.settleAll();
            }
        });
        this.media.setVisibility(0);
        this.playerBack = 0.0f;
        this.sheetBack = 0.0f;
        backdrop();
        if (this.open) {
            guard(new Runnable() { // from class: dev.colorcc.Panel.65
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.onClose();
                }
            });
        }
        guard(new Runnable() { // from class: dev.colorcc.Panel.66
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.unbindTiles();
            }
        });
        showStock();
        resetStatusRow();
    }

    /* JADX INFO: renamed from: lambda$onDetachedFromWindow$34$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m56lambda$onDetachedFromWindow$34$devcolorccPanel() {
        this.mediaSessions.removeOnActiveSessionsChangedListener(this.sessionsListener);
    }

    /* JADX INFO: renamed from: lambda$onDetachedFromWindow$35$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m57lambda$onDetachedFromWindow$35$devcolorccPanel() {
        MediaController mediaController = this.controller;
        if (mediaController != null) {
            mediaController.unregisterCallback(this.mediaCallback);
        }
        this.controller = null;
    }

    /* JADX INFO: renamed from: lambda$onDetachedFromWindow$36$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m58lambda$onDetachedFromWindow$36$devcolorccPanel(ConnectivityManager.NetworkCallback networkCallback) {
        ((ConnectivityManager) getContext().getSystemService(ConnectivityManager.class)).unregisterNetworkCallback(networkCallback);
    }

    /* JADX INFO: renamed from: lambda$onDetachedFromWindow$37$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m59lambda$onDetachedFromWindow$37$devcolorccPanel(Object obj) {
        ReflectUtils.callMethod(getRootView(), "removeFrameMetricsListener", new Object[]{obj});
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int r3, int r4, int r5, int r6) {
        super.onLayout(z, r3, r4, r5, r6);
        View view = this;
        int top = 0;
        int left = 0;
        while (view != null) {
            top += view.getTop();
            left += view.getLeft();
            Object parent = view.getParent();
            view = parent instanceof View ? (View) parent : null;
        }
        final int r7 = Math.max(0, dp(landscape() ? DROP_TOP : 88.0f) - top);
        this.headerPadding = r7;
        measureScroll();
        if (r7 != this.column.getPaddingTop()) {
            post(new Runnable() { // from class: dev.colorcc.Panel.70
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m60lambda$onLayout$38$devcolorccPanel(r7);
                }
            });
        }
        final int r8 = -top;
        final int r9 = -left;
        final int r10 = -Math.max(0, (getRootView().getWidth() - left) - getWidth());
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.sheet.getLayoutParams();
        if (layoutParams.topMargin != r8 || layoutParams.leftMargin != r9 || layoutParams.rightMargin != r10) {
            post(new Runnable() { // from class: dev.colorcc.Panel.71
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m61lambda$onLayout$39$devcolorccPanel(r8, r9, r10);
                }
            });
        }
        fitEditor();
        if (this.slide.running || this.closing || this.settleWorkPending) {
            this.layoutsDuringMotion++;
        }
        if (Glass.bending()) {
            post(new Runnable() { // from class: dev.colorcc.Panel.72
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m62lambda$onLayout$40$devcolorccPanel();
                }
            });
        }
        View viewLastShown = lastShown(this.column);
        if (z || (this.twoPane && viewLastShown != null && viewLastShown.getBottom() != this.arrangedCards)) {
            post(new AnonymousClass0());
        }
        Iterator<Body> it = this.staticBodies.iterator();
        while (it.hasNext()) {
            it.next().aim();
        }
        Iterator<Body> it2 = this.tileBodies.iterator();
        while (it2.hasNext()) {
            it2.next().aim();
        }
        assignRows();
    }

    /* JADX INFO: renamed from: lambda$onLayout$38$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m60lambda$onLayout$38$devcolorccPanel(int r6) {
        this.column.setPadding(dp(24.0f), r6, dp(24.0f), 0);
        this.side.setPadding(dp(24.0f), r6, dp(24.0f), 0);
    }

    /* JADX INFO: renamed from: lambda$onLayout$39$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m61lambda$onLayout$39$devcolorccPanel(int r5, int r6, int r7) {
        View[] viewArr = {this.sheet, this.player};
        for (int r3 = 0; r3 < 2; r3++) {
            View view = viewArr[r3];
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
            layoutParams.topMargin = r5;
            layoutParams.leftMargin = r6;
            layoutParams.rightMargin = r7;
            view.requestLayout();
        }
    }

    /* JADX INFO: renamed from: lambda$onLayout$40$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m62lambda$onLayout$40$devcolorccPanel() {
        reglaze(this.content);
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (!this.interactive) {
            return false;
        }
        if (motionEvent.getActionMasked() == 3) {
            this.swipeOwner = 2;
        } else {
            trackSwipe(motionEvent);
        }
        if (motionEvent.getActionMasked() == 0 && Boolean.FALSE.equals(collapsibleAt(motionEvent.getRawX(), motionEvent.getRawY()))) {
            if (getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    private void trackSwipe(MotionEvent motionEvent) {
        float rawX = motionEvent.getRawX();
        float rawY = motionEvent.getRawY();
        int actionMasked = motionEvent.getActionMasked();
        int r3 = 2;
        if (actionMasked == 0) {
            PageSwipe.touching = true;
            this.swipeDownX = rawX;
            this.swipeDownY = rawY;
            if (this.scroller.running()) {
                Motion.Value value = this.scroller;
                value.snap(Math.max(0.0f, Math.min(this.scrollMax, value.value())));
            }
            if (isOverControl(rawX, rawY)) {
                r3 = 1;
            } else if (this.interactive && this.pager.pageCount > 1 && contains(this.pager, rawX, rawY)) {
                r3 = 0;
            }
            this.swipeOwner = r3;
            if (this.pulling) {
                releasePull();
            }
            boolean z = this.interactive;
            this.pullArmed = z && this.swipeOwner != 1;
            this.pullOnOpen = !z;
            this.pulling = false;
            return;
        }
        if (actionMasked != 1) {
            if (actionMasked == 2) {
                if (this.swipeOwner == 0) {
                    int r2 = Logic.swipeOwner(rawX - this.swipeDownX, rawY - this.swipeDownY, this.touchSlop, this.pager.canScrollHorizontally(-1), this.pager.canScrollHorizontally(1));
                    this.swipeOwner = r2;
                    if (r2 != 0) {
                        if (r2 == 1 && getParent() != null) {
                            getParent().requestDisallowInterceptTouchEvent(true);
                        }
                        ColorCCBridge.log("tile swipe goes to ".concat(this.swipeOwner == 1 ? "pager" : "shade"));
                    }
                }
                if (this.pullArmed && Math.max(Math.abs(rawX - this.swipeDownX), Math.abs(rawY - this.swipeDownY)) > this.touchSlop) {
                    this.pullArmed = false;
                    boolean z2 = Math.abs(rawY - this.swipeDownY) > Math.abs(rawX - this.swipeDownX) && this.scrollMax > 0 && overCards(this.swipeDownX) && (rawY < this.swipeDownY || this.scroller.value() > 0.0f);
                    this.scrolling = z2;
                    this.pulling = !z2 && rawY - this.swipeDownY > Math.abs(rawX - this.swipeDownX);
                    this.pullFromY = this.swipeDownY + this.touchSlop;
                    this.scrollFromY = rawY;
                    this.scrollStart = this.scroller.value();
                    this.scrollSpeed = 0.0f;
                    this.scrollLastY = rawY;
                    this.scrollLastAt = motionEvent.getEventTime();
                }
                if (this.scrolling) {
                    this.scroller.snap(Math.max(0.0f, Math.min(this.scrollMax, this.scrollStart - ((rawY - this.scrollFromY) / this.column.getScaleY()))));
                    long eventTime = motionEvent.getEventTime() - this.scrollLastAt;
                    if (eventTime > 0) {
                        this.scrollSpeed = (this.scrollSpeed * 0.6f) + ((((rawY - this.scrollLastY) * 0.4f) * 1000.0f) / eventTime);
                        this.scrollLastY = rawY;
                        this.scrollLastAt = motionEvent.getEventTime();
                    }
                }
                if (this.pullOnOpen && this.interactive && !this.pulling && !this.overPulling) {
                    this.pullOnOpen = false;
                    this.pulling = true;
                    this.pullFromY = rawY;
                }
                if (this.pulling) {
                    dragPull(Logic.rubber(rawY - this.pullFromY, dp(140.0f)), motionEvent.getEventTime());
                    return;
                }
                return;
            }
            if (actionMasked != 3) {
                return;
            }
        }
        PageSwipe.touching = false;
        this.swipeOwner = 2;
        if (this.pulling) {
            releasePull();
        }
        if (this.scrolling) {
            float scaleY = (-this.scrollSpeed) / this.column.getScaleY();
            float fSqrt = (float) Math.sqrt(SCROLL_GLIDE[0]);
            Motion.Value value2 = this.scroller;
            value2.to(Math.max(0.0f, Math.min(this.scrollMax, value2.value() + (scaleY / fSqrt))));
            this.scroller.kick(scaleY);
        }
        this.scrolling = false;
        this.pullOnOpen = false;
        this.pullArmed = false;
        this.pulling = false;
    }

    boolean blocksShadeSlide(MotionEvent motionEvent) {
        trackSwipe(motionEvent);
        if (motionEvent.getActionMasked() == 2) {
            return this.swipeOwner != 2 || this.editor.active;
        }
        return false;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return this.pulling || this.scrolling || super.onInterceptTouchEvent(motionEvent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void tick() {
        if (this.dead) {
            return;
        }
        try {
            long jNanoTime = System.nanoTime();
            update();
            measureClose(jNanoTime, System.nanoTime());
        } catch (Throwable th) {
            ColorCCBridge.log("panel disabled until SystemUI restarts: " + th);
            die();
        }
    }

    private void measureClose(long j, long j2) {
        String str;
        if ((this.closing || this.slide.running || this.settleWorkPending) && getVisibility() == 0) {
            if (this.slide.running) {
                str = "slide";
            } else {
                str = this.closing ? "close" : "open";
            }
            if (this.closeFrames > 0 && !str.equals(this.motionKind)) {
                reportMotion();
            }
            this.motionKind = str;
            if (this.closeFrames > 0) {
                this.closeMaxGapNs = Math.max(this.closeMaxGapNs, j - this.closeLastNs);
            }
            this.closeLastNs = j;
            this.closeFrames++;
            long j3 = j2 - j;
            this.closeWorkNs += j3;
            this.closeMaxWorkNs = Math.max(this.closeMaxWorkNs, j3);
            return;
        }
        reportMotion();
    }

    private void reportMotion() {
        if (this.closeFrames == 0) {
            return;
        }
        String str = this.renderedFrames == 0 ? "UNMEASURED" : this.renderedSlow == 0 ? "PASS" : "FAIL";
        int length = STAGES.length;
        double[] dArr = new double[length];
        for (int r2 = 0; r2 < length; r2++) {
            int r4 = this.renderedSlow;
            dArr[r2] = r4 == 0 ? 0.0d : this.slowStages[r2] / ((double) r4);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "%s %s: %d of %d frames over 16.7 ms; ColorCC per frame: tick avg %.2f max %.2f ms, draw avg %.2f max %.2f ms; longest gap %.1f ms", this.motionKind, str, Integer.valueOf(this.renderedSlow), Integer.valueOf(this.renderedFrames), Double.valueOf((this.closeWorkNs / 1000000.0d) / ((double) this.closeFrames)), Double.valueOf(this.closeMaxWorkNs / 1000000.0d), Double.valueOf((this.drawNs / 1000000.0d) / ((double) this.closeFrames)), Double.valueOf(this.drawMaxNs / 1000000.0d), Double.valueOf(this.closeMaxGapNs / 1000000.0d)));
        sb.append("; slow frames avg ");
        sb.append(this.renderedSlow == 0 ? "none" : stages(dArr));
        sb.append("; worst frame ");
        sb.append(this.worstFrame);
        sb.append("; icons built ");
        sb.append(this.iconBuilds);
        sb.append(", panel layouts ");
        sb.append(this.layoutsDuringMotion);
        sb.append("; redrawn by ");
        sb.append(this.redrawnBy.isEmpty() ? "nothing" : this.redrawnBy.toString());
        ColorCCBridge.log(sb.toString());
        this.redrawnBy.clear();
        this.iconBuilds = 0;
        this.layoutsDuringMotion = 0;
        this.closeFrames = 0;
        this.drawMaxNs = 0L;
        this.drawNs = 0L;
        this.closeMaxWorkNs = 0L;
        this.closeWorkNs = 0L;
        this.closeMaxGapNs = 0L;
        resetFrameStats();
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void onDescendantInvalidated(View view, View view2) {
        super.onDescendantInvalidated(view, view2);
        if (this.closeFrames <= 0 || this.bodyViews.contains(view2)) {
            return;
        }
        this.redrawnBy.merge(view2.getClass().getName().replaceFirst(".*\\.", ""), 1, new BiFunction() { // from class: dev.colorcc.Panel.127
            @Override // java.util.function.BiFunction
            public final Object apply(Object obj, Object obj2) {
                return Integer.valueOf(Integer.sum(((Integer) obj).intValue(), ((Integer) obj2).intValue()));
            }
        });
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        long jNanoTime = System.nanoTime();
        super.dispatchDraw(canvas);
        if (this.closeFrames > 0) {
            long jNanoTime2 = System.nanoTime() - jNanoTime;
            this.drawNs += jNanoTime2;
            this.drawMaxNs = Math.max(this.drawMaxNs, jNanoTime2);
        }
    }

    private void resetFrameStats() {
        this.renderedSlow = 0;
        this.renderedFrames = 0;
        this.worstFrameNs = 0L;
        this.worstFrame = "not measured";
        Arrays.fill(this.slowStages, 0.0d);
    }

    private static String stages(double[] dArr) {
        StringBuilder sb = new StringBuilder();
        int r1 = 0;
        while (r1 < dArr.length) {
            sb.append(r1 == 0 ? "" : " ");
            sb.append(STAGE_NAMES[r1]);
            sb.append(' ');
            sb.append(String.format(Locale.ROOT, "%.1f", Double.valueOf(dArr[r1])));
            r1++;
        }
        return sb.toString();
    }

    private void watchFrames() {
        try {
            Window.OnFrameMetricsAvailableListener onFrameMetricsAvailableListener = new Window.OnFrameMetricsAvailableListener() { // from class: dev.colorcc.Panel.19
                @Override // android.view.Window.OnFrameMetricsAvailableListener
                public final void onFrameMetricsAvailable(Window window, FrameMetrics frameMetrics, int r3) {
                    Panel.this.m88lambda$watchFrames$42$devcolorccPanel(window, frameMetrics, r3);
                }
            };
            this.frameWindow = (Window) ReflectUtils.newInstance(ReflectUtils.findClass("com.android.internal.policy.PhoneWindow", (ClassLoader) null), new Object[]{getContext()});
            ReflectUtils.callMethod(getRootView(), "addFrameMetricsListener", new Class[]{Window.class, Window.OnFrameMetricsAvailableListener.class, Handler.class}, new Object[]{this.frameWindow, onFrameMetricsAvailableListener, this.main});
            this.frameObserver = onFrameMetricsAvailableListener;
        } catch (Throwable th) {
            this.frameObserver = null;
            ColorCCBridge.log("frame stages not measured: " + th);
        }
    }

    /* JADX INFO: renamed from: lambda$watchFrames$42$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m88lambda$watchFrames$42$devcolorccPanel(Window window, final FrameMetrics frameMetrics, int r3) {
        if (this.closing || this.slide.running || this.settleWorkPending) {
            guard(new Runnable() { // from class: dev.colorcc.Panel.24
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m87lambda$watchFrames$41$devcolorccPanel(frameMetrics);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: onFrame, reason: merged with bridge method [inline-methods] */
    public void m87lambda$watchFrames$41$devcolorccPanel(FrameMetrics frameMetrics) {
        long metric = frameMetrics.getMetric(8);
        this.renderedFrames++;
        if (metric > FRAME_BUDGET_NS) {
            this.renderedSlow++;
            int r2 = 0;
            while (true) {
                int[] r4 = STAGES;
                if (r2 >= r4.length) {
                    break;
                }
                double[] dArr = this.slowStages;
                dArr[r2] = dArr[r2] + stage(frameMetrics, r4[r2]);
                r2++;
            }
        }
        if (metric <= this.worstFrameNs) {
            return;
        }
        this.worstFrameNs = metric;
        int length = STAGES.length;
        double[] dArr2 = new double[length];
        for (int r3 = 0; r3 < length; r3++) {
            dArr2[r3] = stage(frameMetrics, STAGES[r3]);
        }
        this.worstFrame = String.format(Locale.ROOT, "%.1f ms: ", Double.valueOf(metric / 1000000.0d)) + stages(dArr2);
    }

    private static double stage(FrameMetrics frameMetrics, int r3) {
        return frameMetrics.getMetric(r3) / 1000000.0d;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void die() {
        if (this.liveRadius > 0) {
            this.liveOn = false;
            guard(new Runnable() { // from class: dev.colorcc.Panel.29
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m9lambda$die$43$devcolorccPanel();
                }
            });
        }
        this.dead = true;
        this.interactive = false;
        setVisibility(8);
        showStock();
        resetStatusRow();
    }

    /* JADX INFO: renamed from: lambda$die$43$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m9lambda$die$43$devcolorccPanel() {
        m69lambda$onShadeExpansion$46$devcolorccPanel(0.0f);
    }

    /* JADX WARN: Failed to calculate best type for var: r1v3 ??
    jadx.core.utils.exceptions.JadxRuntimeException: Type update failed for variable: r1v3 ??, new type: boolean
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:109)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:59)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:147)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:125)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:103)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1612)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:103)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
    Caused by: java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "arg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.verifyType(TypeUpdate.java:210)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.queueTypeUpdate(TypeUpdate.java:171)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.sameFirstArgListener(TypeUpdate.java:454)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:310)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runUpdate(TypeUpdate.java:124)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:91)
    	... 7 more
     */
    /* JADX WARN: Failed to calculate best type for var: r21v0 'this'  ??
    jadx.core.utils.exceptions.JadxRuntimeException: Type update failed for variable: r21v0 'this'  ??, new type: dev.colorcc.Panel
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:109)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:59)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:147)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:125)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:103)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1612)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:103)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
    Caused by: java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "arg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.verifyType(TypeUpdate.java:210)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.queueTypeUpdate(TypeUpdate.java:171)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.sameFirstArgListener(TypeUpdate.java:454)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:310)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runUpdate(TypeUpdate.java:124)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:91)
    	... 7 more
     */
    /* JADX WARN: Failed to calculate best type for var: r5v29 ??
    jadx.core.utils.exceptions.JadxRuntimeException: Type update failed for variable: r5v29 ??, new type: boolean
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:109)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:59)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:147)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:125)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:103)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1612)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:103)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
    Caused by: java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "arg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.verifyType(TypeUpdate.java:210)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.queueTypeUpdate(TypeUpdate.java:171)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.sameFirstArgListener(TypeUpdate.java:454)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:310)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runUpdate(TypeUpdate.java:124)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:91)
    	... 7 more
     */
    /* JADX WARN: Failed to calculate best type for var: r5v30 ??
    jadx.core.utils.exceptions.JadxRuntimeException: Type update failed for variable: r5v30 ??, new type: boolean
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:109)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:59)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:147)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:125)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:103)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1612)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:103)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
    Caused by: java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "arg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.verifyType(TypeUpdate.java:210)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.queueTypeUpdate(TypeUpdate.java:171)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.sameFirstArgListener(TypeUpdate.java:454)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:310)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runUpdate(TypeUpdate.java:124)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:91)
    	... 7 more
     */
    /* JADX WARN: Failed to calculate best type for var: r5v34 ??
    jadx.core.utils.exceptions.JadxRuntimeException: Type update failed for variable: r5v34 ??, new type: boolean
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:109)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:59)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:147)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:125)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:103)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1612)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:103)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
    Caused by: java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "arg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.verifyType(TypeUpdate.java:210)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.queueTypeUpdate(TypeUpdate.java:171)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.sameFirstArgListener(TypeUpdate.java:454)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:310)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runUpdate(TypeUpdate.java:124)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:91)
    	... 7 more
     */
    /* JADX WARN: Failed to set immutable type for var: r21v0 'this'  ??
    jadx.core.utils.exceptions.JadxRuntimeException: Type update failed for variable: r21v0 'this'  ??, new type: dev.colorcc.Panel
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:109)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.applyWithWiderIgnSame(TypeUpdate.java:73)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setImmutableType(TypeInferenceVisitor.java:111)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$1(TypeInferenceVisitor.java:102)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1612)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:102)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
    Caused by: java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "arg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.verifyType(TypeUpdate.java:210)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.queueTypeUpdate(TypeUpdate.java:171)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.sameFirstArgListener(TypeUpdate.java:454)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:310)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runUpdate(TypeUpdate.java:124)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:91)
    	... 6 more
     */
    /*  JADX ERROR: Types fix failed
        jadx.core.utils.exceptions.JadxRuntimeException: Type update failed for variable: r5v27 ??, new type: boolean
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:109)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:59)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryPossibleTypes(FixTypesVisitor.java:186)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.deduceType(FixTypesVisitor.java:245)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryDeduceTypes(FixTypesVisitor.java:224)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.visit(FixTypesVisitor.java:94)
        Caused by: java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "arg" is null
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.verifyType(TypeUpdate.java:210)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.queueTypeUpdate(TypeUpdate.java:171)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.sameFirstArgListener(TypeUpdate.java:454)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:310)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.runUpdate(TypeUpdate.java:124)
        	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:91)
        	... 5 more
        */
    void requestUpdate() {
        if (!this.dead && isAttachedToWindow()) {
            guard(this::tick);
        }
    }

    private void update() {
        holdInPlace();
        if (!ColorCCBridge.isColorCCEnabled(getContext())) {
            if (this.open) {
                onClose();
            }
            if (getVisibility() != View.GONE) {
                setVisibility(View.GONE);
            }
            showStock();
            resetStatusRow();
            return;
        }
        float f3 = ColorCCBridge.qsExpansion >= 0.0f ? Logic.clamp(ColorCCBridge.qsExpansion) : 0.0f;
        boolean zSplit = ColorCCBridge.splitEnabled();
        if (zSplit && !locked()) {
            f3 = Math.max(f3, ColorCCBridge.shadeFraction(f3));
        }
        boolean zControlsPage = ColorCCBridge.isControlsPageState();
        boolean z11 = ColorCCBridge.host == null || this.override || !supportedScreen();
        boolean zSliding = ColorCCBridge.isSliding();
        boolean zOnControlPage = Logic.onControlPage(zSplit, zControlsPage);
        this.pendingPage = zOnControlPage;
        this.page = zOnControlPage;
        boolean zSlideRunning = this.slide.running;
        float fDownToControls = ColorCCBridge.downToControls();
        float f6;
        if (z11) {
            f6 = 0.0f;
        } else if (fDownToControls >= 0.0f) {
            f6 = fDownToControls;
        } else if (zSlideRunning) {
            f6 = this.slide.away ? 0.0f : 1.0f;
        } else if (this.slide.waitingToSlideIn()) {
            f6 = 0.0f;
        } else {
            f6 = Logic.revealTarget(f3, this.page);
        }
        logState(zSplit, zControlsPage, f3, z11);
        if (f6 > 0.0f && !this.open) {
            onOpen();
        } else if (f6 <= 0.0f && this.open) {
            onClose();
        }
        boolean zInteractive = Logic.takesTouches(f3, f6) && (!zSlideRunning || this.slide.target == 1.0f);
        this.interactive = zInteractive;
        if (!zInteractive && (this.pulling || this.pullArmed)) {
            this.pullArmed = false;
            this.pulling = false;
            releasePull();
        }
        whenSettled();
        if (f6 > 0.0f && getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
        }
        long nanoTime = System.nanoTime();
        if (this.open && !this.closing && f3 > this.lastFraction) {
            long dt = nanoTime - this.lastFractionAt;
            if (dt > 4000000L) {
                float pace = Logic.openPace((f3 - this.lastFraction) / (dt / 1.0E9f));
                if (pace > this.openPace) {
                    this.openPace = pace;
                }
            }
        } else if (this.open && this.closing && f3 < this.lastFraction) {
            long dt = nanoTime - this.lastFractionAt;
            if (dt > 4000000L) {
                float pace = Logic.openPace((this.lastFraction - f3) / (dt / 1.0E9f));
                if (pace > this.closePace) {
                    this.closePace = pace;
                }
            }
        }
        this.lastFraction = f3;
        this.lastFractionAt = nanoTime;
        boolean zClosingNow = Logic.closing(this.closing, f6, this.turnFrom);
        if (zClosingNow != this.closing) {
            this.closing = zClosingNow;
            this.turnFrom = f6;
            this.closeRide.to(zClosingNow ? 1.0f : 0.0f);
            if (this.closing) {
                this.closePace = 1.0f;
            } else {
                ColorCCBridge.fadeShadeBlur(1.0f - Logic.clamp(this.peek.value()));
                releaseHeld();
            }
        } else {
            this.turnFrom = this.closing ? Math.min(this.turnFrom, f6) : Math.max(this.turnFrom, f6);
        }
        if (this.closing && stillBehind()) {
            ColorCCBridge.fadeShadeBlur(Logic.clamp(f3) * (1.0f - Logic.clamp(this.peek.value())));
        }
        float fOverlayAlpha = this.closing ? Logic.clamp(2.0f * f6) : 1.0f;
        if (this.sheet.getAlpha() != fOverlayAlpha) {
            this.sheet.setAlpha(fOverlayAlpha);
        }
        if (this.player.getAlpha() != fOverlayAlpha) {
            this.player.setAlpha(fOverlayAlpha);
        }
        boolean zRowsDone = stepRows(f6, SystemClock.uptimeMillis());
        if (f6 <= 0.0f && zRowsDone && getVisibility() != View.INVISIBLE) {
            setVisibility(View.INVISIBLE);
        }
        this.stockNeeded = z11;
        this.stockValue.snap(z11 ? 1.0f : 0.0f);
        setStockHidden(!z11);
        swapStatusRow();
        if (this.interactive && this.page && !this.stockNeeded && !locked()) {
            showStatusRow();
        }
    }

    private void showStatusRow() {
        String str;
        View view = this.statusRow;
        if (!this.statusRowLogged) {
            this.statusRowLogged = true;
            if (view == null) {
                ColorCCBridge.log("status row: not found");
            } else {
                int[] r5 = new int[2];
                view.getLocationOnScreen(r5);
                View view2 = view.getParent() instanceof View ? (View) view.getParent() : null;
                StringBuilder sb = new StringBuilder("status row: visibility ");
                sb.append(view.getVisibility());
                sb.append(", alpha ");
                sb.append(view.getAlpha());
                sb.append(", moved ");
                sb.append(view.getTranslationY());
                sb.append(", at ");
                sb.append(r5[1]);
                sb.append(", ");
                sb.append(view.getWidth());
                sb.append("x");
                sb.append(view.getHeight());
                sb.append(", shown ");
                sb.append(view.isShown());
                if (view2 == null) {
                    str = "";
                } else {
                    str = ", parent " + view2.getClass().getSimpleName() + " alpha " + view2.getAlpha() + " visibility " + view2.getVisibility();
                }
                sb.append(str);
                ColorCCBridge.log(sb.toString());
            }
        }
        if (view == null) {
            return;
        }
        if (view.getVisibility() == TILES_PER_ROW) {
            view.setVisibility(0);
        }
        if (view.getAlpha() < 1.0f) {
            view.setAlpha(1.0f);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public View stockPage() {
        View view = this.stockPage;
        if (view == null || !view.isAttachedToWindow()) {
            this.stockPage = null;
            for (View view2 : this.stock) {
                if (!ColorCCBridge.oneUi8 || view2.getVisibility() != 8) {
                    View viewComposeIn = composeIn(view2);
                    this.stockPage = viewComposeIn;
                    if (viewComposeIn != null) {
                        break;
                    }
                }
            }
            for (View view3 : this.stock) {
                if (this.stockPage == null && view3.getVisibility() == 0) {
                    this.stockPage = view3;
                }
            }
            if (this.stockPage == null && !this.stock.isEmpty()) {
                this.stockPage = this.stock.get(0);
            }
        }
        View view4 = this.stockPage;
        return view4 != null ? view4 : this.container;
    }

    private static View composeIn(View view) {
        if (view.getClass().getName().equals("androidx.compose.ui.platform.AndroidComposeView")) {
            return view;
        }
        if (!(view instanceof ViewGroup)) {
            return null;
        }
        ViewGroup viewGroup = (ViewGroup) view;
        for (int r0 = 0; r0 < viewGroup.getChildCount(); r0++) {
            View viewComposeIn = composeIn(viewGroup.getChildAt(r0));
            if (viewComposeIn != null) {
                return viewComposeIn;
            }
        }
        return null;
    }

    private void holdInPlace() {
        if (getVisibility() != 0) {
            return;
        }
        View view = this.container;
        int top = 0;
        int left = 0;
        while (view != null) {
            top += view.getTop();
            left += view.getLeft();
            Object parent = view.getParent();
            view = parent instanceof View ? (View) parent : null;
        }
        this.container.getLocationOnScreen(this.containerAt);
        this.heldTop = top - this.containerAt[1];
        this.heldX = (this.slide.running || ColorCCBridge.isSliding()) ? 0.0f : left - this.containerAt[0];
        applySheet();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void applySheet() {
        float fSideAt;
        float fMin;
        float fPage = this.slide.running ? this.slide.page() : 1.0f;
        if (this.slide.running) {
            int width = getRootView().getWidth();
            fSideAt = this.slide.sideAt(fPage, width);
            SideSlide sideSlide = this.slide;
            float fSideAt2 = sideSlide.sideAt(sideSlide.tail(), width);
            fMin = Math.min(Math.abs(fSideAt - fSideAt2), getWidth() * MAX_SMEAR);
            if (fSideAt > fSideAt2) {
                fSideAt -= fMin;
            }
        } else {
            fSideAt = 0.0f;
            fMin = 0.0f;
        }
        float fMax = fMin / Math.max(1, getWidth());
        if (getPivotX() != 0.0f) {
            setPivotX(0.0f);
        }
        setScaleX(fMax + 1.0f);
        setScaleY(1.0f - (fMax * 0.25f));
        setAlpha(SideSlide.alphaAt(fPage));
        setTranslationX(this.heldX + fSideAt);
        setTranslationY(this.heldTop * (1.0f - this.closeRide.value()));
        boolean z = this.slide.running && this.buttonsLift == 0.0f;
        if (z != this.sheetLayered) {
            this.sheetLayered = z;
            setLayerType(z ? 2 : 0, null);
        }
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return this.buttonsLift == 0.0f;
    }

    private void swapStatusRow() {
        statusShift();
        float scrollY = this.column.getScrollY();
        if (this.headerRow.getTranslationY() != scrollY) {
            this.headerRow.setTranslationY(scrollY);
        }
        if (!this.statusSearched) {
            this.statusSearched = true;
            int identifier = getResources().getIdentifier("split_shade_status_bar", "id", "com.android.systemui");
            this.statusRow = identifier == 0 ? null : getRootView().findViewById(identifier);
        }
        if (this.statusRow == null) {
            return;
        }
        if (this.statusRow.getTranslationY() != 0.0f) {
            this.statusRow.setTranslationY(0.0f);
        }
        float reveal;
        if (getVisibility() != 0 || (locked() && !this.open)) {
            reveal = 0.0f;
        } else {
            reveal = Logic.clamp(this.pencil.getAlpha() * getAlpha() * (this.editor.getVisibility() == 0 ? this.content.getAlpha() : 1.0f));
        }
        float fClamp = (reveal * 0.035f) + 1.0f;
        if (Math.abs(this.statusRow.getScaleX() - fClamp) > 0.001f) {
            View view = this.statusRow;
            view.setPivotX(view.getWidth() / 2.0f);
            this.statusRow.setPivotY(this.statusRow.getHeight() / 2.0f);
            this.statusRow.setScaleX(fClamp);
            this.statusRow.setScaleY(fClamp);
            keepStatusEdges(fClamp);
        }
    }

    private void keepStatusEdges(float f) {
        if (this.statusSides == null) {
            this.statusSides = new ArrayList();
            collectSides(this.statusRow);
            StringBuilder sb = new StringBuilder();
            for (View view : this.statusSides) {
                sb.append(' ');
                sb.append(view.getClass().getSimpleName());
                sb.append('@');
                sb.append(view.getLeft());
                sb.append('+');
                sb.append(view.getWidth());
            }
            StringBuilder sb2 = new StringBuilder("status row sides kept at the edges:");
            int length = sb.length();
            Object obj = sb;
            if (length == 0) {
                obj = " none found";
            }
            sb2.append(obj);
            ColorCCBridge.log(sb2.toString());
        }
        float width = this.statusRow.getWidth() / 2.0f;
        for (View view2 : this.statusSides) {
            float width2 = 0.0f;
            View view3 = view2;
            while (view3 != this.statusRow && view3 != null) {
                width2 += view3.getLeft();
                view3 = view3.getParent() instanceof View ? (View) view3.getParent() : null;
            }
            if ((view2.getWidth() / 2.0f) + width2 >= width) {
                width2 += view2.getWidth();
            }
            view2.setTranslationX((width - width2) * (1.0f - (1.0f / f)));
        }
    }

    private void collectSides(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int r0 = 0; r0 < viewGroup.getChildCount(); r0++) {
                View childAt = viewGroup.getChildAt(r0);
                if (childAt.getVisibility() == 0 && childAt.getWidth() != 0) {
                    if (childAt.getWidth() < viewGroup.getWidth() * 0.7f) {
                        this.statusSides.add(childAt);
                    } else {
                        collectSides(childAt);
                    }
                }
            }
        }
    }

    private void resetStatusRow() {
        View view = this.statusRow;
        if (view != null) {
            view.setTranslationY(0.0f);
            this.statusRow.setScaleX(1.0f);
            this.statusRow.setScaleY(1.0f);
        }
        List<View> list = this.statusSides;
        if (list != null) {
            Iterator<View> it = list.iterator();
            while (it.hasNext()) {
                it.next().setTranslationX(0.0f);
            }
        }
        this.statusSides = null;
        this.shownStatusShift = 0.0f;
        this.statusSearched = false;
        this.statusCarrier = null;
        this.statusRow = null;
    }

    private float statusShift() {
        this.buttonsLift = 0.0f;
        return 0.0f;
    }

    boolean headerTouch(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.headerTarget = null;
            if (!this.open || !this.interactive || getVisibility() != 0 || this.buttonsLift == 0.0f) {
                return false;
            }
            int[] r5 = new int[2];
            getLocationOnScreen(r5);
            if (motionEvent.getRawY() >= r5[1]) {
                return false;
            }
            View[] viewArr = {this.pencil, this.activeApps, this.gear, this.power};
            for (int r7 = 0; r7 < viewArr.length; r7++) {
                View view = viewArr[r7];
                view.getLocationOnScreen(r5);
                if (view.getAlpha() > 0.5f && motionEvent.getRawX() >= r5[0] && motionEvent.getRawX() < r5[0] + view.getWidth() && motionEvent.getRawY() >= r5[1] && motionEvent.getRawY() < r5[1] + view.getHeight()) {
                    this.headerTarget = view;
                }
            }
        }
        View view2 = this.headerTarget;
        if (view2 == null) {
            return false;
        }
        if (actionMasked == 1 || actionMasked == 3) {
            this.headerTarget = null;
        }
        int[] r12 = new int[2];
        view2.getLocationOnScreen(r12);
        MotionEvent motionEventObtain = MotionEvent.obtain(motionEvent);
        motionEventObtain.setLocation(motionEvent.getRawX() - r12[0], motionEvent.getRawY() - r12[1]);
        view2.dispatchTouchEvent(motionEventObtain);
        motionEventObtain.recycle();
        return true;
    }

    private static int layoutTop(View view) {
        int top = 0;
        while (view != null) {
            top += view.getTop();
            Object parent = view.getParent();
            view = parent instanceof View ? (View) parent : null;
        }
        return top;
    }

    boolean handleBack() {
        if (this.dead || !this.open) {
            return false;
        }
        if (this.editor.active) {
            ColorCCBridge.log("back closes the editor without saving");
            guard(new Runnable() { // from class: dev.colorcc.Panel.89
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m13lambda$handleBack$44$devcolorccPanel();
                }
            });
            return true;
        }
        if (this.sheet.tile != null || this.sheet.volumesOpen || this.sheet.brightnessOpen || this.sheet.outputOpen) {
            ColorCCBridge.log("back closes a menu");
            final Sheet sheet = this.sheet;
            Objects.requireNonNull(sheet);
            guard(new Runnable() { // from class: dev.colorcc.Panel.90
                @Override // java.lang.Runnable
                public final void run() {
                    sheet.close();
                }
            });
            return true;
        }
        if (this.player.opened) {
            ColorCCBridge.log("back closes the player");
            final Player player = this.player;
            Objects.requireNonNull(player);
            guard(new Runnable() { // from class: dev.colorcc.Panel.91
                @Override // java.lang.Runnable
                public final void run() {
                    player.close();
                }
            });
            return true;
        }
        if (this.sheet.getVisibility() != 0 && this.player.getVisibility() != 0 && this.editor.getVisibility() != 0) {
            return false;
        }
        ColorCCBridge.log("back while a menu closes, kept");
        return true;
    }

    /* JADX INFO: renamed from: lambda$handleBack$44$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m13lambda$handleBack$44$devcolorccPanel() {
        this.editor.close(false);
    }

    boolean showing() {
        return this.open && !this.dead;
    }

    void onShadeExpansion(final float f) {
        if (this.dead) {
            return;
        }
        guard(new Runnable() { // from class: dev.colorcc.Panel.50
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m68lambda$onShadeExpansion$45$devcolorccPanel(f);
            }
        });
        guard(new Runnable() { // from class: dev.colorcc.Panel.51
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m69lambda$onShadeExpansion$46$devcolorccPanel(f);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$onShadeExpansion$45$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m68lambda$onShadeExpansion$45$devcolorccPanel(float f) {
        this.slide.onExpansion(f);
    }

    void onOverExpansion(float f, boolean z) {
        if (this.dead || !this.open || this.pulling) {
            return;
        }
        if (z && f > 0.0f) {
            this.overPulling = true;
            dragPull(Logic.rubber(f, dp(140.0f)), SystemClock.uptimeMillis());
        } else if (this.overPulling) {
            this.overPulling = false;
            releasePull();
        }
    }

    private void dragPull(float f, long j) {
        long j2 = j - this.pullAt;
        if (j2 > 0) {
            this.pullSpeed = j2 > 100 ? 0.0f : (this.pullSpeed * 0.6f) + ((((f - this.pull.value()) * 0.4f) * 1000.0f) / j2);
            this.pullAt = j;
        }
        this.pull.snap(f);
    }

    private void releasePull() {
        float fDp = dp(6000.0f);
        this.pull.to(0.0f);
        this.pull.kick(Math.max(-fDp, Math.min(fDp, this.pullSpeed)));
        this.pullSpeed = 0.0f;
    }

    boolean liveBackground() {
        WindowManager windowManager;
        return (this.dead || this.liveBroken || !replacesStockGrid() || ColorCCBridge.samsungLive() || this.keyguard.isKeyguardLocked() || (windowManager = (WindowManager) getContext().getSystemService(WindowManager.class)) == null || !windowManager.isCrossWindowBlurEnabled() || !phoneBlursLive()) ? false : true;
    }

    private boolean phoneBlursLive() {
        if (this.blursLive == null) {
            try {
                this.blursLive = Boolean.valueOf(ReflectUtils.getStaticBooleanField(ReflectUtils.findClass("com.android.systemui.QpRune", getContext().getClassLoader()), "QUICK_PANEL_BLUR_DEFAULT"));
            } catch (Throwable th) {
                this.blursLive = true;
                ColorCCBridge.log("Samsung's live blur switch not read: " + th);
            }
            StringBuilder sb = new StringBuilder("Samsung's live shade blur on this phone: ");
            sb.append(this.blursLive);
            sb.append(this.blursLive.booleanValue() ? "" : ", so ColorCC's own live blur is off and Samsung's still picture stays");
            ColorCCBridge.log(sb.toString());
        }
        return this.blursLive.booleanValue();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: applyLiveBlur, reason: merged with bridge method [inline-methods] */
    public void m69lambda$onShadeExpansion$46$devcolorccPanel(float f) {
        Object objCallMethod;
        int r0;
        String str;
        if (f > 0.0f && this.lastExpansion <= 0.0f) {
            this.liveOn = liveBackground();
            guard(new Runnable() { // from class: dev.colorcc.Panel.34
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.wholeBackground();
                }
            });
            if (!locked()) {
                this.blurArrive.snap(0.0f);
                this.blurArrive.to(1.0f);
            }
            if (ColorCCBridge.notifGlass || ColorCCBridge.footerGlass || stillBehind()) {
                Glass.noteRoot = noteStack();
                this.quickCapture = true;
                guard(new AnonymousClass35());
            }
            this.pullStartedAt = SystemClock.uptimeMillis();
            this.pullLogged = false;
        } else if (f <= 0.0f && this.lastExpansion > 0.0f) {
            ColorCCBridge.staleShadeBlur();
        }
        if (!this.pullLogged && f >= 0.999f && (r0 = this.pullsLogged) < 6) {
            this.pullLogged = true;
            this.pullsLogged = r0 + 1;
            StringBuilder sb = new StringBuilder("pull: shade open in ");
            sb.append(SystemClock.uptimeMillis() - this.pullStartedAt);
            sb.append(" ms on the ");
            sb.append(this.page ? "controls" : "notifications");
            sb.append(locked() ? " (locked)" : "");
            sb.append(", background ");
            if (this.liveOn) {
                str = "ColorCC live blur";
            } else if (ColorCCBridge.samsungLive()) {
                str = "Samsung live blur at " + ColorCCBridge.blurCurve();
            } else {
                str = "still picture";
            }
            sb.append(str);
            sb.append("; ");
            sb.append(ColorCCBridge.backgroundState());
            sb.append(", window blur ");
            sb.append(windowBlurUsable() ? "on" : "off");
            ColorCCBridge.log(sb.toString());
        }
        this.lastExpansion = f;
        float fClamp = (1.0f - Logic.clamp(this.peek.value())) * Logic.clamp(this.blurArrive.value());
        int r1 = Math.max(1, dp(2.0f));
        int r2 = this.liveOn ? Math.round(((dp(Logic.level(LIVE_BLUR, this.blur, this.blurCustom)) * Logic.clamp(f)) * fClamp) / r1) * r1 : 0;
        int r10 = r2 < 1 ? 0 : (Math.round((fClamp * 102.0f) * Logic.clamp(f)) & (-8)) << 24;
        if (r2 == this.liveRadius && r10 == this.liveDim) {
            return;
        }
        this.liveRadius = r2;
        this.liveDim = r10;
        try {
            ClassLoader classLoader = getContext().getClassLoader();
            if (r2 > 0) {
                Object objNewInstance = ReflectUtils.newInstance(ReflectUtils.findClass("android.view.SemBlurInfo$Builder", classLoader), new Object[]{0});
                ReflectUtils.callMethod(objNewInstance, "setRadius", new Object[]{Integer.valueOf(r2)});
                ReflectUtils.callMethod(objNewInstance, "setBackgroundColor", new Object[]{Integer.valueOf(r10)});
                objCallMethod = ReflectUtils.callMethod(objNewInstance, "build", new Object[0]);
            } else {
                objCallMethod = null;
            }
            ReflectUtils.callMethod(getRootView(), "semSetBlurInfo", new Class[]{ReflectUtils.findClass("android.view.SemBlurInfo", classLoader)}, new Object[]{objCallMethod});
            this.liveFailures = 0;
        } catch (Throwable th) {
            int r3 = this.liveFailures + 1;
            this.liveFailures = r3;
            this.liveBroken = r3 >= 3;
            this.liveOn = false;
            StringBuilder sb2 = new StringBuilder("live background failed");
            sb2.append(this.liveBroken ? " three times, Samsung's still picture stays" : ", tried again next pull");
            sb2.append(": ");
            sb2.append(th);
            ColorCCBridge.log(sb2.toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onTilt(float[] fArr) {
        float f;
        for (int r1 = 0; r1 < 3; r1++) {
            float[] fArr2 = this.gravity;
            if (this.gravityRead) {
                float f2 = fArr2[r1];
                f = f2 + ((fArr[r1] - f2) * 0.2f);
            } else {
                f = fArr[r1];
            }
            fArr2[r1] = f;
        }
        this.gravityRead = true;
        long jUptimeMillis = SystemClock.uptimeMillis();
        if (this.editor.getVisibility() != 0 && jUptimeMillis - this.tiltDrawnAt >= BACKDROP_FRAME_MS) {
            float[] fArr3 = this.gravity;
            if (Glass.setLight(fArr3[0] * 2.0f, (-fArr3[1]) - BLUR_CUSTOM_MAX)) {
                this.tiltDrawnAt = jUptimeMillis;
                reglaze(this);
            }
        }
    }

    private void watchTilt(boolean z) {
        SensorManager sensorManager = (SensorManager) getContext().getSystemService(SensorManager.class);
        if (sensorManager == null) {
            return;
        }
        if (!z) {
            sensorManager.unregisterListener(this.tilt);
            return;
        }
        Sensor defaultSensor = sensorManager.getDefaultSensor(RINGER_INSET);
        if (defaultSensor == null) {
            defaultSensor = sensorManager.getDefaultSensor(1);
        }
        if (defaultSensor == null) {
            return;
        }
        this.gravityRead = false;
        sensorManager.registerListener(this.tilt, defaultSensor, 1, this.main);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setReveal(float f) {
        boolean z = f >= 1.0f;
        Iterator<Body> it = this.staticBodies.iterator();
        while (it.hasNext()) {
            it.next().place(z);
        }
        Iterator<Body> it2 = this.tileBodies.iterator();
        while (it2.hasNext()) {
            it2.next().place(z);
        }
    }

    private float shortSide() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    private boolean stepRows(float f, long j) {
        float f2;
        float f3;
        Iterator it = Arrays.asList(this.staticBodies, this.tileBodies).iterator();
        boolean z = true;
        while (it.hasNext()) {
            for (Body body : (List<Body>) it.next()) {
                if (this.closing) {
                    f2 = 0.68f - ((this.lastRow - (body.header ? -1 : body.row)) * CLOSE_STEP);
                    f3 = CLOSE_SPAN;
                } else {
                    f2 = body.header ? 0.0f : (body.row * STAGGER_STEP) + STAGGER_START;
                    f3 = STAGGER_SPAN;
                }
                body.follow(Logic.clamp((f - f2) / f3));
                z &= body.drop.value() <= 0.004f && !body.drop.running();
            }
        }
        return f <= 0.0f && z;
    }

    private void assignRows() {
        ArrayList<Float> arrayList = new ArrayList();
        ArrayList<Body> arrayList2 = new ArrayList(this.staticBodies);
        arrayList2.addAll(this.tileBodies);
        for (Body body : arrayList2) {
            body.header = false;
            for (ViewParent parent = body.view.getParent(); parent instanceof View; parent = parent.getParent()) {
                if (parent == this.headerRow) {
                    body.header = true;
                }
            }
            if (!body.header) {
                arrayList.add(Float.valueOf(body.piece.topY));
            }
        }
        Collections.sort(arrayList);
        ArrayList arrayList3 = new ArrayList();
        for (Float f : arrayList) {
            float fFloatValue = f.floatValue();
            if (arrayList3.isEmpty() || fFloatValue - ((Float) arrayList3.get(arrayList3.size() - 1)).floatValue() > dp(24.0f)) {
                arrayList3.add(f);
            }
        }
        this.lastRow = 0;
        for (Body body2 : arrayList2) {
            if (!body2.header) {
                int r4 = 0;
                for (int r3 = 0; r3 < arrayList3.size(); r3++) {
                    if (body2.piece.topY >= ((Float) arrayList3.get(r3)).floatValue() - dp(24.0f)) {
                        r4 = r3;
                    }
                }
                body2.row = Math.min(23, r4);
                body2.in = Logic.Spring.of((body2.row * IN_STEP_S) + IN_S, IN_BOUNCE);
                this.lastRow = Math.max(this.lastRow, body2.row);
            }
        }
    }

    private void logState(boolean z, boolean z2, float f, boolean z3) {
        int r7;
        String str;
        if (f <= 0.0f) {
            r7 = 0;
        } else {
            r7 = f >= 1.0f ? 1 : 2;
        }
        int r0 = (z ? 1 : 0) | (z2 ? 2 : 0) | (z3 ? TILES_PER_ROW : 0) | (r7 << 3);
        if (r0 == this.lastLogKey) {
            return;
        }
        this.lastLogKey = r0;
        if (r7 == 0) {
            str = "0";
        } else {
            str = r7 == 1 ? "1" : "mid";
        }
        ColorCCBridge.log("split=" + z + " qsState=" + z2 + " fraction=" + str + " stock=" + z3);
    }

    private void showStock() {
        parkStock(false);
        this.stockValue.snap(1.0f);
        setStockHidden(false);
    }

    private void parkStock(boolean z) {
        if (!z) {
            for (int r6 = 0; r6 < this.parkedViews.size(); r6++) {
                ViewGroup viewGroup = this.parkedIn.get(r6);
                if (this.parkedViews.get(r6).getParent() == null && viewGroup.isAttachedToWindow() && composeChild(viewGroup) == null) {
                    viewGroup.addView(this.parkedViews.get(r6), Math.min(this.parkedAt.get(r6).intValue(), viewGroup.getChildCount()));
                }
            }
            View view = this.goneView;
            if (view != null) {
                view.setVisibility(this.goneWas);
                this.goneView = null;
                ColorCCBridge.log("stock quick panel put back");
            }
            if (!this.parkedViews.isEmpty()) {
                ColorCCBridge.log("stock quick panel put back");
            }
            this.parkedViews.clear();
            this.parkedIn.clear();
            this.parkedAt.clear();
            return;
        }
        boolean zStockParked = stockParked();
        for (View view2 : this.stock) {
            if (view2 instanceof ViewGroup) {
                View viewComposeChild = composeChild((ViewGroup) view2);
                if (viewComposeChild == null && ColorCCBridge.oneUi8 && view2.getClass().getSimpleName().equals("NonInterceptingScrollView") && view2.getParent() == this.container) {
                    if (this.goneView == null) {
                        this.goneWas = view2.getVisibility();
                        this.goneView = view2;
                    }
                    view2.setVisibility(8);
                } else if (viewComposeChild != null && (viewComposeChild.getParent() instanceof ViewGroup)) {
                    ViewGroup viewGroup2 = (ViewGroup) viewComposeChild.getParent();
                    this.parkedAt.add(Integer.valueOf(viewGroup2.indexOfChild(viewComposeChild)));
                    viewGroup2.removeView(viewComposeChild);
                    this.parkedViews.add(viewComposeChild);
                    this.parkedIn.add(viewGroup2);
                }
            }
        }
        if (!zStockParked && stockParked()) {
            ColorCCBridge.log("stock quick panel parked".concat(this.goneView != null ? " (One UI 8 scroll view gone, still attached)" : ""));
        }
        markCollapsible();
    }

    boolean stockParked() {
        return (this.parkedViews.isEmpty() && this.goneView == null) ? false : true;
    }

    private void markCollapsible() {
        Object obj = ColorCCBridge.adapter;
        if (!stockParked() || obj == null || noCollapsible) {
            return;
        }
        try {
            ReflectUtils.callMethod(ReflectUtils.getObjectField(obj, "_isCollapsible"), "setValue", new Object[]{Boolean.TRUE});
        } catch (NoSuchFieldError unused) {
            noCollapsible = true;
        } catch (Throwable th) {
            ColorCCBridge.log("action failed: " + th);
        }
    }

    private static View composeChild(ViewGroup viewGroup) {
        for (int r0 = 0; r0 < viewGroup.getChildCount(); r0++) {
            View childAt = viewGroup.getChildAt(r0);
            if (childAt.getClass().getName().equals("androidx.compose.ui.platform.ComposeView")) {
                return childAt;
            }
        }
        return null;
    }

    private void setStockHidden(boolean z) {
        if (z == this.stockHidden) {
            return;
        }
        this.stockHidden = z;
        Iterator<View> it = this.stock.iterator();
        while (it.hasNext()) {
            it.next().setImportantForAccessibility(z ? TILES_PER_ROW : 0);
        }
    }

    private boolean supportedScreen() {
        return (!large() || this.foldable) && getDisplay() != null && getDisplay().getDisplayId() == 0;
    }

    private void onOpen() {
        long jUptimeMillis = SystemClock.uptimeMillis();
        wholeBackground();
        if (large()) {
            guard(new AnonymousClass0());
            StringBuilder sb = new StringBuilder("open on the large screen: panel ");
            sb.append(getWidth());
            sb.append("x");
            sb.append(getHeight());
            sb.append(" in ");
            sb.append(this.container.getClass().getSimpleName());
            sb.append(" ");
            sb.append(this.container.getWidth());
            sb.append("x");
            sb.append(this.container.getHeight());
            sb.append(", ");
            sb.append(this.twoPane ? "two panes" : "one column");
            sb.append(", page shown ");
            sb.append(stockPage().isShown());
            sb.append(", room beside the notifications ");
            sb.append(roomBesideNotes(getWidth()));
            sb.append(", pane size ");
            sb.append(this.paneScale);
            ColorCCBridge.log(sb.toString());
        }
        openNow();
        releaseHeld();
        markCollapsible();
        logSlow("open", jUptimeMillis);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void wholeBackground() {
        if (ColorCCBridge.peekClear != 1.0f) {
            ColorCCBridge.peekClear = 1.0f;
            guard(new AnonymousClass27());
        }
        this.main.postDelayed(new Runnable() { // from class: dev.colorcc.Panel.124
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m89lambda$wholeBackground$47$devcolorccPanel();
            }
        }, STALE_PICTURE_MS);
        ColorCCBridge.fadeShadeBlur(1.0f);
    }

    /* JADX INFO: renamed from: lambda$wholeBackground$47$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m89lambda$wholeBackground$47$devcolorccPanel() {
        guard(new Runnable() { // from class: dev.colorcc.Panel.75
            @Override // java.lang.Runnable
            public final void run() {
                ColorCCBridge.freshenShadeBlur();
            }
        });
    }

    private void logSlow(String str, long j) {
        long jUptimeMillis = SystemClock.uptimeMillis() - j;
        if (jUptimeMillis >= 8) {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append(" took ");
            sb.append(jUptimeMillis);
            sb.append(" ms");
            sb.append(locked() ? " on the lock screen" : "");
            ColorCCBridge.log(sb.toString());
        }
    }

    boolean locked() {
        long jUptimeMillis = SystemClock.uptimeMillis();
        if (jUptimeMillis - this.lockedReadAt > BACKDROP_RECHECK_MS) {
            this.lockedNow = this.keyguard.isKeyguardLocked();
            this.lockedReadAt = jUptimeMillis;
        }
        return this.lockedNow;
    }

    private void openNow() {
        this.open = true;
        this.closeRide.snap(0.0f);
        this.backdropSum = 0L;
        this.scroller.snap(0.0f);
        this.pager.home();
        this.openPace = 1.0f;
        if (this.currentTiles.isEmpty()) {
            syncTiles();
        }
        setTilesListening();
        readControlsInBackground();
        refreshActiveApps();
        this.main.removeCallbacks(this.releaseGpu);
        Glass.dark = (getResources().getConfiguration().uiMode & 48) == 32;
        this.quickCapture = true;
        guard(new AnonymousClass35());
        this.settleWorkPending = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setTilesListening() {
        Iterator<Bound> it = this.bound.iterator();
        while (it.hasNext()) {
            ReflectUtils.callMethod(it.next().tile, "setListening", new Object[]{this, true});
        }
    }

    void refreshActiveApps() {
        this.main.post(new Runnable() {
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.guard(new Runnable() {
                    @Override // java.lang.Runnable
                    public final void run() {
                        int count = ColorCCBridge.getActiveAppsCount();
                        boolean hasNew = ColorCCBridge.hasNewActiveApps();
                        if (count > 0) {
                            Panel.this.activeApps.icon.setVisibility(8);
                            Panel.this.activeAppsCount.setText(String.valueOf(count));
                            Panel.this.activeAppsCount.setVisibility(0);
                            GradientDrawable dotBg = new GradientDrawable();
                            dotBg.setShape(GradientDrawable.OVAL);
                            dotBg.setColor(Panel.this.theme(Panel.ACTIVE_BLUE));
                            Panel.this.activeAppsDot.setBackground(dotBg);
                            Panel.this.activeAppsDot.setVisibility(hasNew ? 0 : 8);
                        } else {
                            Panel.this.activeAppsCount.setVisibility(8);
                            Panel.this.activeApps.icon.setVisibility(0);
                            Panel.this.activeAppsDot.setVisibility(8);
                        }
                    }
                });
            }
        });
    }

    private void readControlsInBackground() {
        final Display display = getDisplay();
        new Thread(new Runnable() { // from class: dev.colorcc.Panel.12
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m75lambda$readControlsInBackground$50$devcolorccPanel(display);
            }
        }, "ColorCC-read").start();
    }

    /* JADX INFO: renamed from: lambda$readControlsInBackground$50$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m75lambda$readControlsInBackground$50$devcolorccPanel(Display display) {
        Process.setThreadPriority(10);
        try {
            try {
                Object objCallMethod = ReflectUtils.callMethod(display, "getBrightnessInfo", new Object[0]);
                float floatField = ReflectUtils.getFloatField(objCallMethod, "brightnessMinimum");
                float floatField2 = ReflectUtils.getFloatField(objCallMethod, "brightnessMaximum");
                if (floatField2 > floatField) {
                    this.brightMin = floatField;
                    this.brightMax = floatField2;
                }
            } catch (Throwable th) {
                ColorCCBridge.log("brightness range not read, 0 to 1 used: " + th);
            }
            final float fFloatValue = ((Float) ReflectUtils.callMethod(this.display, "getBrightness", new Object[]{0})).floatValue();
            final int streamMaxVolume = this.audio.getStreamMaxVolume(3);
            final int streamVolume = this.audio.getStreamVolume(3);
            final int r6 = ((Integer) ReflectUtils.callMethod(this.audio, "getRingerModeInternal", new Object[0])).intValue();
            this.main.post(new Runnable() { // from class: dev.colorcc.Panel.28
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m74lambda$readControlsInBackground$49$devcolorccPanel(fFloatValue, r6, streamVolume, streamMaxVolume);
                }
            });
        } catch (Throwable th2) {
            ColorCCBridge.log("controls not read: " + th2);
        }
    }

    /* JADX INFO: renamed from: lambda$readControlsInBackground$49$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m74lambda$readControlsInBackground$49$devcolorccPanel(final float f, final int r8, final int r9, final int r10) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.118
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m73lambda$readControlsInBackground$48$devcolorccPanel(f, r8, r9, r10);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$readControlsInBackground$48$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m73lambda$readControlsInBackground$48$devcolorccPanel(float f, int r5, int r6, int r7) {
        if (this.open) {
            this.brightness.setValue(Logic.toSlider(f, this.brightMin, this.brightMax));
            showRing(r5, r6, r7, false);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reblur() {
        if (this.open) {
            guard(new Runnable() { // from class: dev.colorcc.Panel.41
                @Override // java.lang.Runnable
                public final void run() {
                    ColorCCBridge.reblur();
                }
            });
            if (this.liveOn) {
                this.liveRadius = -1;
                guard(new Runnable() { // from class: dev.colorcc.Panel.42
                    @Override // java.lang.Runnable
                    public final void run() {
                        Panel.this.m76lambda$reblur$51$devcolorccPanel();
                    }
                });
            }
            guard(new AnonymousClass35());
        }
    }

    /* JADX INFO: renamed from: lambda$reblur$51$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m76lambda$reblur$51$devcolorccPanel() {
        m69lambda$onShadeExpansion$46$devcolorccPanel(this.lastExpansion);
    }

    /* JADX INFO: renamed from: lambda$new$53$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m34lambda$new$53$devcolorccPanel() {
        this.backdropThread.execute(new Runnable() { // from class: dev.colorcc.Panel.32
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m33lambda$new$52$devcolorccPanel();
            }
        });
    }

    /* JADX INFO: renamed from: lambda$new$52$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m33lambda$new$52$devcolorccPanel() {
        GpuBlur gpuBlur = this.gpu;
        if (gpuBlur == null) {
            return;
        }
        Glass.setBackdrop(null, null, 0.0f);
        this.backdropSum = 0L;
        gpuBlur.release();
        this.gpuSoft.release();
        this.gpuSoft = null;
        this.gpu = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void captureBackdrop() {
        if (this.backdropBroken) {
            return;
        }
        final boolean zIsKeyguardLocked = this.keyguard.isKeyguardLocked();
        int r1 = 1;
        if (this.backdropBusy) {
            this.backdropAgain = true;
            return;
        }
        this.backdropBusy = true;
        this.backdropAgain = false;
        Point point = new Point();
        getDisplay().getRealSize(point);
        final int displayId = getDisplay().getDisplayId();
        boolean zLandscape = landscape();
        if (Glass.look == 1 || this.gpuWorks) {
            r1 = TILES_PER_ROW;
        } else if (this.blur != 0 || this.quickCapture) {
            r1 = 2;
        }
        final int r9 = r1;
        final boolean z = this.quickCapture;
        this.quickCapture = false;
        int r0 = point.x;
        int r5 = point.y;
        final int r6 = (zLandscape ? Math.max(r0, r5) : Math.min(r0, r5)) / r9;
        int r2 = point.x;
        final int r7 = (zLandscape ? Math.min(r2, point.y) : Math.max(r2, point.y)) / r9;
        final Context context = getContext();
        final ClassLoader classLoader = context.getClassLoader();
        final ColorFilter colorFilter = this.liveOn ? Glass.LIVE_COLOURS : Glass.SHADE_COLOURS;
        final ColorFilter colorFilter2 = this.liveOn ? Glass.LIVE_FROM_SCREEN : Glass.SHADE_FROM_SCREEN;
        this.backdropThread.execute(new Runnable() { // from class: dev.colorcc.Panel.58
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m8lambda$captureBackdrop$58$devcolorccPanel(z, zIsKeyguardLocked, r6, r7, classLoader, displayId, r9, colorFilter2, context, colorFilter);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$captureBackdrop$54$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m4lambda$captureBackdrop$54$devcolorccPanel() {
        this.backdropBusy = false;
        if (Glass.setBackdrop(null, null, 0.0f)) {
            reglaze(this);
        }
    }

    /* JADX WARN: Code duplicated, block: B:48:0x0114 A[Catch: all -> 0x02c5, TryCatch #2 {all -> 0x02c5, blocks: (B:8:0x0024, B:10:0x002a, B:15:0x0084, B:75:0x02ac, B:48:0x0114, B:50:0x011c, B:64:0x021f, B:66:0x0227, B:67:0x022e, B:69:0x0236, B:71:0x0280, B:74:0x0297, B:63:0x01f8, B:41:0x00f5, B:76:0x02bd, B:77:0x02c4, B:13:0x0037), top: B:89:0x0022 }] */
    /* JADX WARN: Code duplicated, block: B:50:0x011c A[Catch: all -> 0x02c5, TRY_LEAVE, TryCatch #2 {all -> 0x02c5, blocks: (B:8:0x0024, B:10:0x002a, B:15:0x0084, B:75:0x02ac, B:48:0x0114, B:50:0x011c, B:64:0x021f, B:66:0x0227, B:67:0x022e, B:69:0x0236, B:71:0x0280, B:74:0x0297, B:63:0x01f8, B:41:0x00f5, B:76:0x02bd, B:77:0x02c4, B:13:0x0037), top: B:89:0x0022 }] */
    /* JADX WARN: Code duplicated, block: B:56:0x01e9  */
    /* JADX WARN: Code duplicated, block: B:57:0x01ec A[Catch: all -> 0x01f4, TryCatch #1 {all -> 0x01f4, blocks: (B:54:0x0162, B:57:0x01ec, B:58:0x01f3), top: B:87:0x0162 }] */
    /* JADX WARN: Code duplicated, block: B:66:0x0227 A[Catch: all -> 0x02c5, TryCatch #2 {all -> 0x02c5, blocks: (B:8:0x0024, B:10:0x002a, B:15:0x0084, B:75:0x02ac, B:48:0x0114, B:50:0x011c, B:64:0x021f, B:66:0x0227, B:67:0x022e, B:69:0x0236, B:71:0x0280, B:74:0x0297, B:63:0x01f8, B:41:0x00f5, B:76:0x02bd, B:77:0x02c4, B:13:0x0037), top: B:89:0x0022 }] */
    /* JADX WARN: Code duplicated, block: B:69:0x0236 A[Catch: all -> 0x02c5, TryCatch #2 {all -> 0x02c5, blocks: (B:8:0x0024, B:10:0x002a, B:15:0x0084, B:75:0x02ac, B:48:0x0114, B:50:0x011c, B:64:0x021f, B:66:0x0227, B:67:0x022e, B:69:0x0236, B:71:0x0280, B:74:0x0297, B:63:0x01f8, B:41:0x00f5, B:76:0x02bd, B:77:0x02c4, B:13:0x0037), top: B:89:0x0022 }] */
    /* JADX WARN: Code duplicated, block: B:70:0x027e  */
    /* JADX WARN: Code duplicated, block: B:73:0x0295  */
    /* JADX WARN: Instruction removed from duplicated block: B:69:0x0236, please report this as an issue */
    /* JADX INFO: renamed from: lambda$captureBackdrop$58$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m8lambda$captureBackdrop$58$devcolorccPanel(boolean z, boolean z2, int r33, int r34, ClassLoader classLoader, int r36, final int r37, ColorFilter colorFilter, Context context, ColorFilter colorFilter2) {
        Bitmap bitmapLockWallpaper;
        Process.setThreadPriority(z ? -4 : 0);
        long jUptimeMillis = SystemClock.uptimeMillis();
        try {
            if (z2) {
                bitmapLockWallpaper = lockWallpaper(r33, r34);
                if (bitmapLockWallpaper == null) {
                    this.main.post(new Runnable() { // from class: dev.colorcc.Panel.55
                        @Override // java.lang.Runnable
                        public final void run() {
                            Panel.this.m4lambda$captureBackdrop$54$devcolorccPanel();
                        }
                    });
                    return;
                }
            } else {
                bitmapLockWallpaper = ColorCCBridge.captureLiveBackdrop(this, r33, r34);
                if (bitmapLockWallpaper == null) {
                    bitmapLockWallpaper = lockWallpaper(r33, r34);
                }
            }
            Bitmap bitmapCopy2 = bitmapLockWallpaper;
            if (bitmapCopy2 == null) {
                throw new IllegalStateException("no screenshot");
            }
            Bitmap bitmapDraw = null;
            Bitmap bitmapSoften = null;
            long jSumOf = 0;
            if (this.gpuWorks) {
                try {
                    GpuBlur gpuBlur = this.gpu;
                    if (gpuBlur == null || gpuBlur.width != r33 || this.gpu.height != r34) {
                        GpuBlur gpuBlur2 = this.gpu;
                        if (gpuBlur2 != null) {
                            gpuBlur2.release();
                            this.gpuSoft.release();
                        }
                        this.gpu = new GpuBlur(r33, r34);
                        this.gpuSoft = new GpuBlur(Math.max(1, (r33 * r37) / 12), Math.max(1, (r34 * r37) / 12));
                    }
                    Bitmap bitmapDraw2 = this.gpu.draw(bitmapCopy2, dp(Logic.level(LIVE_BLUR, this.blur, this.blurCustom)) / r37, colorFilter);
                    if (bitmapDraw2 != null) {
                        bitmapSoften = this.gpuSoft.draw(bitmapDraw2, 6.0f, null);
                    }
                    bitmapDraw = bitmapDraw2;
                    jSumOf = bitmapDraw2 != null ? sumOf(bitmapDraw2) : 0L;
                } catch (Throwable th) {
                    this.gpuWorks = false;
                    ColorCCBridge.log("GPU blur not used, the slower one instead: " + th);
                    bitmapDraw = null;
                    bitmapSoften = null;
                }
            }
            if (bitmapDraw == null || bitmapSoften == null) {
                if (bitmapCopy2.getConfig() == Bitmap.Config.HARDWARE) {
                    bitmapCopy2 = bitmapCopy2.copy(Bitmap.Config.ARGB_8888, false);
                }
                Bitmap bitmapCreateScaledBitmap = Bitmap.createScaledBitmap(Bitmap.createScaledBitmap(bitmapCopy2, Math.max(1, r33 / 12), Math.max(1, r34 / 12), true), r33, r34, true);
                bitmapDraw = Bitmap.createBitmap(bitmapCreateScaledBitmap.getWidth(), bitmapCreateScaledBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Paint paint = new Paint();
                paint.setColorFilter(colorFilter);
                new Canvas(bitmapDraw).drawBitmap(bitmapCreateScaledBitmap, 0.0f, 0.0f, paint);
                jSumOf = sumOf(bitmapDraw);
                bitmapSoften = Glass.soften(bitmapDraw, r37);
            }
            final Bitmap bitmap6 = bitmapSoften;
            final Bitmap bitmap7 = bitmapDraw;
            final long j2 = jSumOf;
            final long jUptimeMillis3 = SystemClock.uptimeMillis() - jUptimeMillis;
            this.main.post(new Runnable() { // from class: dev.colorcc.Panel.56
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m6lambda$captureBackdrop$56$devcolorccPanel(bitmap7, jUptimeMillis3, j2, bitmap6, r37);
                }
            });
        } catch (Throwable th5) {
            this.main.post(new Runnable() { // from class: dev.colorcc.Panel.57
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m7lambda$captureBackdrop$57$devcolorccPanel();
                }
            });
            StringBuilder sb = new StringBuilder("backdrop not captured");
            sb.append(this.backdropFailures >= RINGER_INSET ? ", glass rims stay unbent: " : ": ");
            sb.append(th5);
            ColorCCBridge.log(sb.toString());
        }
    }

    /* JADX INFO: renamed from: lambda$captureBackdrop$56$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m6lambda$captureBackdrop$56$devcolorccPanel(final Bitmap bitmap, final long j, final long j2, final Bitmap bitmap2, final int r16) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.40
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m5lambda$captureBackdrop$55$devcolorccPanel(bitmap, j, j2, bitmap2, r16);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$captureBackdrop$55$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m5lambda$captureBackdrop$55$devcolorccPanel(Bitmap bitmap, long j, long j2, Bitmap bitmap2, int r13) {
        long j3;
        boolean z = false;
        this.backdropFailures = 0;
        backdropDone();
        if (!this.backdropLogged) {
            this.backdropLogged = true;
            StringBuilder sb = new StringBuilder("backdrop ");
            sb.append(bitmap.getWidth());
            sb.append("x");
            sb.append(bitmap.getHeight());
            sb.append(" in ");
            sb.append(j);
            sb.append(" ms, ");
            sb.append(this.gpuWorks ? "GPU" : "CPU");
            sb.append(", middle ");
            sb.append(Integer.toHexString(bitmap.copy(Bitmap.Config.ARGB_8888, false).getPixel(bitmap.getWidth() / 2, bitmap.getHeight() / 2)));
            ColorCCBridge.log(sb.toString());
        }
        boolean z2 = this.notesAsked;
        this.notesAsked = false;
        if (!this.open && ((this.lastExpansion > 0.0f || z2) && (ColorCCBridge.notifGlass || ColorCCBridge.footerGlass || stillBehind()))) {
            z = true;
        }
        if (this.open || z) {
            if (j2 != this.backdropSum && (z || !this.closing)) {
                this.backdropSum = j2;
                Glass.setBackdrop(bitmap, bitmap2, 1.0f / r13);
                if (stillBehind() && (this.open || (z && this.lastExpansion > 0.0f))) {
                    ColorCCBridge.showShadeBlur(bitmap);
                }
                reglaze(this);
                if (ColorCCBridge.notifGlass || ColorCCBridge.footerGlass) {
                    redrawNotifications();
                }
                removeCallbacks(this.fadeBackdrop);
                postOnAnimation(this.fadeBackdrop);
            }
            if (!liveBackdrop()) {
                pauseBackdrop(this.refreshBackdrop);
                return;
            }
            this.main.removeCallbacks(this.refreshBackdrop);
            if (this.liveOn) {
                j3 = BACKDROP_FRAME_MS;
            } else {
                j3 = stillBehind() ? STILL_FRAME_MS : LIVE_BACKDROP_MS;
            }
            this.main.postDelayed(this.refreshBackdrop, Math.max(0L, j3 - j));
        }
    }

    /* JADX INFO: renamed from: lambda$captureBackdrop$57$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m7lambda$captureBackdrop$57$devcolorccPanel() {
        int r0 = this.backdropFailures + 1;
        this.backdropFailures = r0;
        boolean z = r0 >= 10;
        this.backdropBroken = z;
        this.backdropBusy = false;
        if (z || !liveBackdrop()) {
            return;
        }
        this.main.postDelayed(this.refreshBackdrop, BACKDROP_RECHECK_MS);
    }

    private void backdropDone() {
        this.backdropBusy = false;
        if (this.backdropAgain) {
            captureBackdrop();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean liveBackdrop() {
        if (this.closing || this.slide.running || this.settleWorkPending || this.lastExpansion < 0.999f) {
            return false;
        }
        if (notesStill()) {
            return this.peek.value() == 0.0f;
        }
        return this.open && ((this.liveOn || ColorCCBridge.samsungLive() || stillBehind()) && this.editor.getVisibility() != 0 && this.sheetBack == 0.0f && this.playerBack == 0.0f && this.peek.value() == 0.0f);
    }

    private boolean stillBehind() {
        return (this.liveOn || ColorCCBridge.samsungLive() || this.keyguard.isKeyguardLocked() || !ColorCCBridge.hasShadeBlur()) ? false : true;
    }

    void notesWantBackdrop() {
        guard(new Runnable() { // from class: dev.colorcc.Panel.73
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m39lambda$notesWantBackdrop$59$devcolorccPanel();
            }
        });
    }

    /* JADX INFO: renamed from: lambda$notesWantBackdrop$59$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m39lambda$notesWantBackdrop$59$devcolorccPanel() {
        if (this.open) {
            return;
        }
        this.notesAsked = true;
        if (!this.notesAskLogged) {
            this.notesAskLogged = true;
            ColorCCBridge.log("notification glass had no backdrop: one captured (shade at " + this.lastExpansion + ", locked " + this.keyguard.isKeyguardLocked() + ")");
        }
        captureBackdrop();
    }

    private Bitmap lockWallpaper(int r11, int r12) {
        Bitmap bitmap;
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getContext());
        int wallpaperId = wallpaperManager.getWallpaperId(2);
        if (wallpaperId <= 0) {
            wallpaperId = (-2) - wallpaperManager.getWallpaperId(1);
        }
        if (wallpaperId == this.lockShotId && ((bitmap = this.lockShot) == null || (bitmap.getWidth() == r11 && this.lockShot.getHeight() == r12))) {
            return this.lockShot;
        }
        this.lockShotId = wallpaperId;
        this.lockShot = null;
        try {
            Drawable drawable = wallpaperManager.getDrawable(2);
            if (drawable == null) {
                drawable = wallpaperManager.getDrawable(1);
            }
            int intrinsicHeight = 0;
            int intrinsicWidth = drawable == null ? 0 : drawable.getIntrinsicWidth();
            if (drawable != null) {
                intrinsicHeight = drawable.getIntrinsicHeight();
            }
            if (intrinsicWidth > 0 && intrinsicHeight > 0) {
                float f = intrinsicWidth;
                float f2 = intrinsicHeight;
                float fMax = Math.max(r11 / f, r12 / f2);
                int r6 = Math.round(f * fMax);
                int r3 = Math.round(f2 * fMax);
                Bitmap bitmapCreateBitmap = Bitmap.createBitmap(r11, r12, Bitmap.Config.ARGB_8888);
                int r13 = r11 - r6;
                int r14 = r12 - r3;
                drawable.setBounds(r13 / 2, r14 / 2, (r13 / 2) + r6, (r14 / 2) + r3);
                drawable.draw(new Canvas(bitmapCreateBitmap));
                this.lockShot = bitmapCreateBitmap;
                ColorCCBridge.log("lock screen glass from the wallpaper, " + intrinsicWidth + "x" + intrinsicHeight);
                return this.lockShot;
            }
            ColorCCBridge.log("lock screen glass: no wallpaper picture, plain glass there");
            return null;
        } catch (Throwable th) {
            ColorCCBridge.log("lock screen glass: wallpaper not read: " + th);
            return null;
        }
    }

    void stillPictureCame() {
        guard(new Runnable() { // from class: dev.colorcc.Panel.36
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m84lambda$stillPictureCame$60$devcolorccPanel();
            }
        });
    }

    /* JADX INFO: renamed from: lambda$stillPictureCame$60$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m84lambda$stillPictureCame$60$devcolorccPanel() {
        if (notesStill() || (this.open && stillBehind())) {
            this.quickCapture = true;
            captureBackdrop();
        }
    }

    private boolean notesStill() {
        return !this.open && !this.closing && !this.slide.running && this.lastExpansion >= 0.999f && stillBehind();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void pauseBackdrop(Runnable runnable) {
        String str;
        if (this.open || notesStill()) {
            this.main.removeCallbacks(runnable);
            this.main.postDelayed(runnable, BACKDROP_RECHECK_MS);
            if (this.backdropPauseLogged) {
                return;
            }
            this.backdropPauseLogged = true;
            StringBuilder sb = new StringBuilder("live glass paused: closing ");
            sb.append(this.closing);
            sb.append(", settling ");
            sb.append(this.settleWorkPending);
            sb.append(", background ");
            if (this.liveOn) {
                str = "ColorCC live";
            } else if (ColorCCBridge.samsungLive()) {
                str = "Samsung live";
            } else {
                str = stillBehind() ? "still" : "none";
            }
            sb.append(str);
            sb.append(", editor ");
            sb.append(this.editor.getVisibility() == 0);
            sb.append(", menu ");
            sb.append(this.sheetBack);
            sb.append(", player ");
            sb.append(this.playerBack);
            sb.append(", peek ");
            sb.append(this.peek.value());
            ColorCCBridge.log(sb.toString());
        }
    }

    private static long sumOf(Bitmap bitmap) {
        if (bitmap == null) {
            return 0L;
        }
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 12, 24, true);
        if (scaled != null && scaled.getConfig() == Bitmap.Config.HARDWARE) {
            scaled = scaled.copy(Bitmap.Config.ARGB_8888, false);
        }
        if (scaled == null) {
            return 0L;
        }
        int[] r4 = new int[288];
        scaled.getPixels(r4, 0, 12, 0, 0, 12, 24);
        long j = 0;
        for (int r2 = 0; r2 < 288; r2++) {
            j = (j * 31) + ((long) r4[r2]);
        }
        return j;
    }

    private void onSettled() {
        long jUptimeMillis = SystemClock.uptimeMillis();
        Iterator<Disc> it = this.lateIcons.iterator();
        while (it.hasNext()) {
            it.next().showLateIcon();
        }
        this.lateIcons.clear();
        logSlow("late icons", jUptimeMillis);
        long jUptimeMillis2 = SystemClock.uptimeMillis();
        if (syncTiles()) {
            setTilesListening();
        }
        logSlow("tile check", jUptimeMillis2);
        this.systemWatched = true;
        guard(new Runnable() { // from class: dev.colorcc.Panel.85
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m65lambda$onSettled$61$devcolorccPanel();
            }
        });
        guard(new Runnable() { // from class: dev.colorcc.Panel.86
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m66lambda$onSettled$62$devcolorccPanel();
            }
        });
        boolean zIsKeyguardLocked = this.keyguard.isKeyguardLocked();
        this.gear.setEnabled(!zIsKeyguardLocked);
        this.gear.icon.setAlpha(zIsKeyguardLocked ? 0.4f : 1.0f);
        guard(new Runnable() { // from class: dev.colorcc.Panel.87
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m67lambda$onSettled$63$devcolorccPanel();
            }
        });
        guard(new AnonymousClass35());
        reportMotion();
    }

    /* JADX INFO: renamed from: lambda$onSettled$61$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m65lambda$onSettled$61$devcolorccPanel() {
        IntentFilter intentFilter = new IntentFilter("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION");
        intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
        getContext().registerReceiver(this.ringReceiver, intentFilter, null, this.main, 2);
        refreshRing();
    }

    /* JADX INFO: renamed from: lambda$onSettled$62$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m66lambda$onSettled$62$devcolorccPanel() {
        ReflectUtils.callMethod(this.display, "registerDisplayListener", new Object[]{this.brightnessWatch, this.main, Long.valueOf(brightnessEvents())});
    }

    /* JADX INFO: renamed from: lambda$onSettled$63$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m67lambda$onSettled$63$devcolorccPanel() {
        watchTilt(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onClose() {
        this.open = false;
        this.backdropPauseLogged = false;
        ColorCCBridge.log("close, background ".concat(this.liveOn ? "ColorCC live blur" : ColorCCBridge.samsungLive() ? "Samsung live blur" : "still picture"));
        guard(new Runnable() { // from class: dev.colorcc.Panel.76
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m43lambda$onClose$64$devcolorccPanel();
            }
        });
        this.main.removeCallbacks(this.refreshBackdrop);
        if (!this.page && notesStill()) {
            this.main.postDelayed(this.refreshBackdrop, STILL_FRAME_MS);
            if (!this.notesLoopLogged) {
                ColorCCBridge.log("live picture kept going on the notifications page");
            }
            this.notesLoopLogged = true;
        }
        this.main.removeCallbacks(this.releaseGpu);
        this.peek.snap(0.0f);
        guard(new Runnable() { // from class: dev.colorcc.Panel.77
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m44lambda$onClose$65$devcolorccPanel();
            }
        });
        guard(new Runnable() { // from class: dev.colorcc.Panel.78
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m45lambda$onClose$66$devcolorccPanel();
            }
        });
        guard(new Runnable() { // from class: dev.colorcc.Panel.79
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m46lambda$onClose$67$devcolorccPanel();
            }
        });
        for (final Bound bound : this.bound) {
            guard(new Runnable() { // from class: dev.colorcc.Panel.80
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m47lambda$onClose$68$devcolorccPanel(bound);
                }
            });
        }
        if (!this.stockNeeded) {
            this.main.post(new Runnable() { // from class: dev.colorcc.Panel.81
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m49lambda$onClose$70$devcolorccPanel();
                }
            });
        }
        this.settleWorkPending = false;
        if (this.systemWatched) {
            this.systemWatched = false;
            guard(new Runnable() { // from class: dev.colorcc.Panel.82
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m50lambda$onClose$71$devcolorccPanel();
                }
            });
            guard(new Runnable() { // from class: dev.colorcc.Panel.83
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m51lambda$onClose$72$devcolorccPanel();
                }
            });
        }
    }

    /* JADX INFO: renamed from: lambda$onClose$64$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m43lambda$onClose$64$devcolorccPanel() {
        watchTilt(false);
    }

    /* JADX INFO: renamed from: lambda$onClose$65$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m44lambda$onClose$65$devcolorccPanel() {
        this.editor.close(false);
    }

    /* JADX INFO: renamed from: lambda$onClose$66$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m45lambda$onClose$66$devcolorccPanel() {
        this.sheet.close();
    }

    /* JADX INFO: renamed from: lambda$onClose$67$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m46lambda$onClose$67$devcolorccPanel() {
        this.player.close();
    }

    /* JADX INFO: renamed from: lambda$onClose$68$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m47lambda$onClose$68$devcolorccPanel(Bound bound) {
        ReflectUtils.callMethod(bound.tile, "setListening", new Object[]{this, false});
    }

    /* JADX INFO: renamed from: lambda$onClose$70$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m49lambda$onClose$70$devcolorccPanel() {
        guard(new Runnable() { // from class: dev.colorcc.Panel.39
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m48lambda$onClose$69$devcolorccPanel();
            }
        });
    }

    /* JADX INFO: renamed from: lambda$onClose$69$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m48lambda$onClose$69$devcolorccPanel() {
        if (this.open || this.stockNeeded) {
            return;
        }
        parkStock(true);
    }

    /* JADX INFO: renamed from: lambda$onClose$71$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m50lambda$onClose$71$devcolorccPanel() {
        getContext().unregisterReceiver(this.ringReceiver);
    }

    /* JADX INFO: renamed from: lambda$onClose$72$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m51lambda$onClose$72$devcolorccPanel() {
        this.display.unregisterDisplayListener(this.brightnessWatch);
    }

    private static boolean settled(List<Body> list) {
        for (Body body : list) {
            if (body.drop.running() || Math.abs(body.drop.value() - 1.0f) > 0.001f) {
                return false;
            }
        }
        return true;
    }

    private void whenSettled() {
        if (this.settleWorkPending && !this.slide.running && settled(this.staticBodies) && settled(this.tileBodies)) {
            this.settleWorkPending = false;
            onSettled();
        }
    }

    private final class Body {
        final Motion.Value drop;
        boolean header;
        private boolean layered;
        int order;
        final SideSlide.Piece piece;
        final Motion.Value press;
        private boolean releaseAtDepth;
        int row;
        private float side;
        private float[] tuned;
        final View view;
        float[] in = Logic.Spring.of(Panel.IN_S, Panel.IN_BOUNCE);
        private float goal = -1.0f;

        Body(View view, int r5) {
            SideSlide.Piece piece = new SideSlide.Piece();
            this.piece = piece;
            this.view = view;
            this.order = r5;
            view.forceHasOverlappingRendering(false);
            Panel.this.bodyViews.add(view);
            this.drop = Panel.this.motion.value(this.in, new Motion.Listener() { // from class: dev.colorcc.Panel.Body.0
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Body.this.m99lambda$new$0$devcolorccPanel$Body(f, f2);
                }
            });
            Panel.this.slide.adopt(piece);
            this.press = Panel.this.motion.value(Logic.Spring.PRESS, new Motion.Listener() { // from class: dev.colorcc.Panel.Body.1
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Body.this.m100lambda$new$1$devcolorccPanel$Body(f, f2);
                }
            });
            apply();
        }

        /* JADX INFO: renamed from: lambda$new$0$dev-colorcc-Panel$Body, reason: not valid java name */
        /* synthetic */ void m99lambda$new$0$devcolorccPanel$Body(float f, float f2) {
            apply();
        }

        /* JADX INFO: renamed from: lambda$new$1$dev-colorcc-Panel$Body, reason: not valid java name */
        /* synthetic */ void m100lambda$new$1$devcolorccPanel$Body(float f, float f2) {
            if (this.releaseAtDepth && f >= 0.6f) {
                release();
            }
            apply();
        }

        void place(boolean z) {
            float f = z ? 1.0f : 0.0f;
            this.goal = f;
            this.drop.snap(f);
            apply();
        }

        void follow(float f) {
            float[] fArr;
            float f2 = this.goal;
            if (f == f2) {
                return;
            }
            if (f <= f2) {
                fArr = Panel.OUT;
            } else {
                fArr = this.in;
            }
            this.goal = f;
            if (fArr != this.tuned) {
                this.tuned = fArr;
                this.drop.tune(fArr);
            }
            this.drop.to(f);
        }

        void aim() {
            if (Panel.this.getWidth() == 0) {
                return;
            }
            float height = this.view.getHeight() / 2.0f;
            float width = this.view.getWidth() / 2.0f;
            View view = this.view;
            while (view != null && view != Panel.this) {
                height += view.getTop();
                width += view.getLeft();
                Object parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
            this.piece.centerY = height;
            this.piece.topY = height - (this.view.getHeight() / 2.0f);
            this.side = Math.max(-1.0f, Math.min(1.0f, (width / Math.max(1.0f, Panel.this.getWidth() / 2.0f)) - 1.0f));
            apply();
        }

        void apply() {
            View view;
            float fValue = this.drop.value();
            float f = 1.0f - fValue;
            float fValue2 = ((0.16000003f * fValue) + Panel.AWAY_SIZE) * (1.0f - (this.press.value() * Panel.PRESS_DEPTH));
            float fVelocity = this.press.velocity() * 0.002f;
            float f2 = -f;
            float fStretch = Logic.stretch(this.drop.velocity(), Panel.IN_STEP_S, 0.12f) - (Math.max(0.0f, f2) * 0.6f);
            this.view.setScaleX((fVelocity + 1.0f) * fValue2 * (1.0f - (0.5f * fStretch)));
            this.view.setScaleY(fValue2 * (1.0f - fVelocity) * (fStretch + 1.0f));
            float fClamp = Logic.clamp(fValue * 3.0f);
            View view2 = this.view;
            view2.setAlpha(fClamp * (view2 != Panel.this.brightnessHost ? 1.0f - Logic.clamp(Panel.this.peek.value()) : 1.0f));
            this.view.setTranslationX((-this.side) * Panel.this.dp(Panel.ARC) * f * Math.abs(f));
            this.view.setTranslationY((f2 * Panel.this.dp(this.header ? Panel.DROP_HEADER : (this.row * Panel.DROP_STEP) + Panel.DROP_TOP)) + (Panel.this.pull.value() * ((Logic.clamp(this.piece.centerY / Math.max(1, Panel.this.getHeight())) * 0.65f) + 0.35f)));
            boolean zRunning = this.drop.running();
            if (zRunning != this.layered) {
                this.layered = zRunning;
                if (this.view == Panel.this.brightnessHost) {
                    view = Panel.this.brightness;
                } else {
                    view = this.view == Panel.this.volumeHost ? Panel.this.volume : this.view;
                }
                view.setLayerType(zRunning ? 2 : 0, null);
                if (!zRunning && Panel.this.settleWorkPending) {
                    Panel.this.whenSettled();
                }
            }
        }

        void pressable() {
            this.view.setOnTouchListener(new View.OnTouchListener() { // from class: dev.colorcc.Panel.Body.2
                @Override // android.view.View.OnTouchListener
                public final boolean onTouch(View view, MotionEvent motionEvent) {
                    return Body.this.m101lambda$pressable$2$devcolorccPanel$Body(view, motionEvent);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$pressable$2$dev-colorcc-Panel$Body, reason: not valid java name */
        /* synthetic */ boolean m101lambda$pressable$2$devcolorccPanel$Body(View view, MotionEvent motionEvent) {
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 0) {
                this.releaseAtDepth = false;
                this.press.tune(Logic.Spring.PRESS_IN);
                this.press.to(1.0f);
            } else if (actionMasked == 3) {
                release();
            } else if (actionMasked == 1) {
                if (this.press.value() >= 0.6f) {
                    release();
                } else {
                    this.releaseAtDepth = true;
                }
            }
            return false;
        }

        private void release() {
            this.releaseAtDepth = false;
            this.press.tune(Logic.Spring.PRESS_OUT);
            this.press.to(0.0f);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Body bodyAround(View view) {
        while (view != null) {
            Iterator it = Arrays.asList(this.staticBodies, this.tileBodies).iterator();
            while (it.hasNext()) {
                for (Body body : (List<Body>) it.next()) {
                    if (body.view == view) {
                        return body;
                    }
                }
            }
            view = view.getParent() instanceof View ? (View) view.getParent() : null;
        }
        return null;
    }

    private void swell(View view, final Runnable runnable) {
        final Body bodyBodyAround = bodyAround(view);
        if (bodyBodyAround != null) {
            bodyBodyAround.press.to(SWELL);
        }
        this.main.postDelayed(new Runnable() { // from class: dev.colorcc.Panel.129
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m85lambda$swell$73$devcolorccPanel(bodyBodyAround, runnable);
            }
        }, SWELL_MS);
    }

    /* JADX INFO: renamed from: lambda$swell$73$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m85lambda$swell$73$devcolorccPanel(Body body, Runnable runnable) {
        if (body != null) {
            body.press.to(0.0f);
        }
        guard(runnable);
    }

    private void addStaticBody(View view, int r3, boolean z) {
        Body body = new Body(view, r3);
        if (z) {
            body.pressable();
        }
        this.staticBodies.add(body);
    }

    private final class Sheet extends FrameLayout {
        private final int[] STREAMS;
        private final String[] STREAM_ICONS;
        private Disc adaptive;
        private boolean adaptiveOn;
        private final int[] at;
        private final Rewind back;
        private final TorchBeam beam;
        private boolean brightnessOpen;
        private final LinearLayout card;
        private Glass cardGlass;
        private Disc dark;
        private boolean darkOn;
        private int dndHours;
        private Disc extra;
        private boolean extraOn;
        private final LinearLayout extras;
        private final Drawable headphones;
        private View held;
        private final float[] heldSize;
        private boolean leaving;
        private final Slider level;
        private final LinearLayout list;
        private final BroadcastReceiver listReceiver;
        private final ScrollView listScroll;
        private boolean listWatched;
        private final Slider loud;
        private final TextView loudHeading;
        private final ImageView loudIcon;
        private int menu;
        volatile int menuStream;
        private final boolean[] mutedShown;
        private final LinearLayout note;
        private final int[] origin;
        private String outputNote;
        boolean outputOpen;
        private String outputPackage;
        private Object outputRouter;
        private final List<Row> rows;
        private final TextView settings;
        private final Motion.Value show;
        private String shownRows;
        private Disc silent;
        private Boolean silentShown;
        private final Drawable speaker;
        private final Disc[] streamButtons;
        private final TextView sub;
        private final ImageView sun;
        private Object tile;
        private final TextView title;
        private final Toggle toggle;
        private Disc tone;
        private boolean toneOn;
        private String torchCamera;
        private final TextView torchLabel;
        private int torchLevel;
        private int torchMax;
        private final LinearLayout torchMenu;
        private float torchStrength;
        private String usage;
        private final LinearLayout volumes;
        boolean volumesOpen;
        private Bound watch;
        private float windowH;
        private float windowRound;
        private float windowW;

        /* JADX INFO: renamed from: dev.colorcc.Panel$Sheet$0, reason: invalid class name */
        /* JADX INFO: compiled from: D8$$SyntheticClass */
        public final /* synthetic */ class AnonymousClass0 implements Runnable {
            public /* synthetic */ AnonymousClass0() {
            }

            @Override // java.lang.Runnable
            public final void run() {
                Sheet.this.refreshList();
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public static /* synthetic */ void access$5700(Sheet sheet) {
            sheet.refreshList();
        }

        Sheet(Context context) {
            super(context);
            this.listReceiver = new BroadcastReceiver() { // from class: dev.colorcc.Panel.Sheet.1
                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context2, Intent intent) {
                    Panel panel = Panel.this;
                    final Sheet sheet = Sheet.this;
                    panel.guard(new Runnable() { // from class: dev.colorcc.Panel.Sheet.1.0
                        @Override // java.lang.Runnable
                        public final void run() {
                            Sheet.access$5700(sheet);
                        }
                    });
                }
            };
            this.rows = new ArrayList();
            this.shownRows = "";
            this.torchLevel = -1;
            this.torchStrength = 1.0f;
            Drawable drawableSpeaker = Icons.speaker();
            this.speaker = drawableSpeaker;
            this.headphones = Icons.of("M12,3c-4.97,0 -9,4.03 -9,9v7c0,1.1 0.9,2 2,2h4v-8H5v-1c0,-3.87 3.13,-7 7,-7s7,3.13 7,7v1h-4v8h4c1.1,0 2,-0.9 2,-2v-7c0,-4.97 -4.03,-9 -9,-9z");
            int[] r9 = {3, 2, 5, 1};
            this.STREAMS = r9;
            this.streamButtons = new Disc[r9.length];
            this.STREAM_ICONS = new String[]{"M12,3v10.55c-0.59,-0.34 -1.27,-0.55 -2,-0.55 -2.21,0 -4,1.79 -4,4s1.79,4 4,4 4,-1.79 4,-4V7h4V3h-6z", "M18,16v-5c0,-3.07 -1.63,-5.64 -4.5,-6.32V4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5v0.68C7.64,5.36 6,7.92 6,11v5l-2,2v1h16v-1l-2,-2zM12,22c1.1,0 2,-0.9 2,-2h-4c0,1.1 0.9,2 2,2z", "M20,2H4c-1.1,0 -2,0.9 -2,2v18l4,-4h14c1.1,0 2,-0.9 2,-2V4c0,-1.1 -0.9,-2 -2,-2z", "M16.5,3h-9C6.67,3 6,3.67 6,4.5v15c0,0.83 0.67,1.5 1.5,1.5h9c0.83,0 1.5,-0.67 1.5,-1.5v-15c0,-0.83 -0.67,-1.5 -1.5,-1.5zM16,19H8V5h8v14z"};
            this.mutedShown = new boolean[r9.length];
            this.menuStream = 3;
            this.outputNote = "";
            this.dndHours = -1;
            this.origin = new int[2];
            this.heldSize = new float[2];
            this.at = new int[2];
            setVisibility(8);
            setBackground(Panel.scrim());
            forceHasOverlappingRendering(false);
            setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Sheet.33
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    Sheet.this.m215lambda$new$0$devcolorccPanel$Sheet(view);
                }
            });
            LinearLayout linearLayout = new LinearLayout(context);
            this.card = linearLayout;
            linearLayout.setOrientation(1);
            linearLayout.setClickable(true);
            linearLayout.setPadding(Panel.this.dp(20.0f), Panel.this.dp(20.0f), Panel.this.dp(20.0f), Panel.this.dp(10.0f));
            linearLayout.setClipChildren(false);
            Glass glass = new Glass(Panel.this.dp(34.0f), Panel.SHEET_GLASS, Panel.SHEET_GLASS, 0.5f, 0.5f);
            this.cardGlass = glass;
            linearLayout.setBackground(glass);
            Panel.this.bodyViews.add(linearLayout);
            linearLayout.setOutlineProvider(new ViewOutlineProvider() { // from class: dev.colorcc.Panel.Sheet.2
                @Override // android.view.ViewOutlineProvider
                public void getOutline(View view, Outline outline) {
                    float width = (view.getWidth() - Sheet.this.windowW) / 2.0f;
                    float height = (view.getHeight() - Sheet.this.windowH) / 2.0f;
                    outline.setRoundRect(Math.round(width), Math.round(height), Math.round(width + Sheet.this.windowW), Math.round(height + Sheet.this.windowH), Math.min(Sheet.this.windowRound, Math.min(Sheet.this.windowW, Sheet.this.windowH) / 2.0f));
                }
            });
            LinearLayout linearLayoutHorizontal = Panel.horizontal(context);
            linearLayoutHorizontal.setGravity(16);
            LinearLayout linearLayout2 = new LinearLayout(context);
            linearLayout2.setOrientation(1);
            TextView textViewLabel = Panel.this.label(context, 20.0f, -1, 1);
            this.title = textViewLabel;
            textViewLabel.setTypeface(Typeface.create("sans-serif-medium", 0));
            TextView textViewLabel2 = Panel.this.label(context, 13.0f, -1711276033, 1);
            this.sub = textViewLabel2;
            linearLayout2.addView(textViewLabel);
            linearLayout2.addView(textViewLabel2);
            linearLayoutHorizontal.addView(linearLayout2, new LinearLayout.LayoutParams(0, -2, 1.0f));
            Toggle toggle = Panel.this.new Toggle(context);
            this.toggle = toggle;
            toggle.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Sheet.36
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    Sheet.this.m224lambda$new$2$devcolorccPanel$Sheet(view);
                }
            });
            linearLayoutHorizontal.addView(toggle, new LinearLayout.LayoutParams(Panel.this.dp(52.0f), Panel.this.dp(30.0f)));
            linearLayout.addView(linearLayoutHorizontal);
            ImageView imageView = new ImageView(context);
            this.loudIcon = imageView;
            imageView.setImageDrawable(drawableSpeaker);
            this.loudHeading = Panel.this.label(context, 17.0f, -1, 1);
            Slider slider = new Slider(context, Panel.this.motion, Panel.this.dp(26.0f), Panel.this.dp(26.0f));
            this.loud = slider;
            slider.setContentDescription(Panel.this.word("volume", new Object[0]));
            slider.setListener(new Slider.Listener() { // from class: dev.colorcc.Panel.Sheet.37
                @Override // dev.colorcc.Slider.Listener
                public final void onChange(float f, boolean z) {
                    Sheet.this.m225lambda$new$3$devcolorccPanel$Sheet(f, z);
                }
            });
            slider.setBackground(new Glass(-2.0f, Panel.GLASS_IDLE, Panel.GLASS_IDLE, 0.5f, 0.5f).recessed());
            LinearLayout linearLayoutHorizontal2 = Panel.horizontal(context);
            linearLayoutHorizontal2.setClipChildren(false);
            for (int r2 = 0; r2 < this.STREAMS.length; r2++) {
                this.streamButtons[r2] = streamButton(context, linearLayoutHorizontal2, r2);
            }
            LinearLayout linearLayoutHorizontal3 = Panel.horizontal(context);
            linearLayoutHorizontal3.setClipChildren(false);
            this.silent = roundSwitch(context, linearLayoutHorizontal3, "M16.5,12c0,-1.77 -1.02,-3.29 -2.5,-4.03v2.21l2.45,2.45c0.03,-0.2 0.05,-0.41 0.05,-0.63zM19,12c0,0.94 -0.2,1.82 -0.54,2.64l1.51,1.51C20.63,14.91 21,13.5 21,12c0,-4.28 -2.99,-7.86 -7,-8.77v2.06c2.89,0.86 5,3.54 5,6.71zM4.27,3L3,4.27 7.73,9H3v6h4l5,5v-6.73l4.25,4.25c-0.67,0.52 -1.42,0.93 -2.25,1.18v2.06c1.38,-0.31 2.63,-0.95 3.69,-1.81L19.73,21 21,19.73l-9,-9L4.27,3zM12,4L9.91,6.09 12,8.18V4z", Panel.this.word("silent_mode", new Object[0]), new Runnable() { // from class: dev.colorcc.Panel.Sheet.38
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m226lambda$new$4$devcolorccPanel$Sheet();
                }
            });
            LinearLayout linearLayout3 = new LinearLayout(context);
            linearLayout3.setOrientation(1);
            linearLayout3.addView(linearLayoutHorizontal2, new LinearLayout.LayoutParams(-1, -2));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
            layoutParams.topMargin = Panel.this.dp(Panel.ARC);
            linearLayout3.addView(linearLayoutHorizontal3, layoutParams);
            this.volumes = column(context, this.loudIcon, this.loudHeading, this.loud, linearLayout3);
            ImageView imageView2 = new ImageView(context);
            this.sun = imageView2;
            imageView2.setImageDrawable(Icons.of("M12,7c-2.76,0 -5,2.24 -5,5s2.24,5 5,5 5,-2.24 5,-5 -2.24,-5 -5,-5zM2,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1H2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1zM20,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1h-2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1zM11,2v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1V2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1zM11,20v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1v-2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1zM5.99,4.58c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39,0.39 1.03,0.39 1.41,0s0.39,-1.03 0,-1.41L5.99,4.58zM18.36,16.95c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39,0.39 1.03,0.39 1.41,0 0.39,-0.39 0.39,-1.03 0,-1.41l-1.06,-1.06zM19.42,5.99c0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06zM7.05,18.36c0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06z"));
            TextView textViewLabel3 = Panel.this.label(context, 17.0f, -1, 1);
            textViewLabel3.setText(Panel.this.word("brightness", new Object[0]));
            Slider slider2 = new Slider(context, Panel.this.motion, Panel.this.dp(26.0f), Panel.this.dp(26.0f));
            this.level = slider2;
            slider2.setContentDescription(Panel.this.word("brightness", new Object[0]));
            slider2.setListener(new Slider.Listener() { // from class: dev.colorcc.Panel.Sheet.39
                @Override // dev.colorcc.Slider.Listener
                public final void onChange(float f, boolean z) {
                    Sheet.this.m227lambda$new$5$devcolorccPanel$Sheet(f, z);
                }
            });
            slider2.setBackground(new Glass(-2.0f, Panel.GLASS_IDLE, Panel.GLASS_IDLE, 0.5f, 0.5f).recessed());
            LinearLayout linearLayoutHorizontal4 = Panel.horizontal(context);
            this.adaptive = roundSwitch(context, linearLayoutHorizontal4, "M12,7c-2.76,0 -5,2.24 -5,5s2.24,5 5,5 5,-2.24 5,-5 -2.24,-5 -5,-5zM2,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1H2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1zM20,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1h-2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1zM11,2v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1V2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1zM11,20v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1v-2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1zM5.99,4.58c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39,0.39 1.03,0.39 1.41,0s0.39,-1.03 0,-1.41L5.99,4.58zM18.36,16.95c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39,0.39 1.03,0.39 1.41,0 0.39,-0.39 0.39,-1.03 0,-1.41l-1.06,-1.06zM19.42,5.99c0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06zM7.05,18.36c0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06z", Panel.this.word("auto_brightness", new Object[0]), new Runnable() { // from class: dev.colorcc.Panel.Sheet.40
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m229lambda$new$7$devcolorccPanel$Sheet();
                }
            });
            this.dark = roundSwitch(context, linearLayoutHorizontal4, "M12,22c5.52,0 10,-4.48 10,-10S17.52,2 12,2 2,6.48 2,12s4.48,10 10,10zM13,4.07c3.94,0.49 7,3.85 7,7.93s-3.05,7.44 -7,7.93V4.07z", Panel.this.word("dark_mode", new Object[0]), new Runnable() { // from class: dev.colorcc.Panel.Sheet.41
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m231lambda$new$9$devcolorccPanel$Sheet();
                }
            });
            this.extra = roundSwitch(context, linearLayoutHorizontal4, "M20,8.69V4h-4.69L12,0.69 8.69,4H4v4.69L0.69,12 4,15.31V20h4.69L12,23.31 15.31,20H20v-4.69L23.31,12 20,8.69zM12,18c-3.31,0 -6,-2.69 -6,-6s2.69,-6 6,-6 6,2.69 6,6 -2.69,6 -6,6zM12,8c-2.21,0 -4,1.79 -4,4s1.79,4 4,4 4,-1.79 4,-4 -1.79,-4 -4,-4z", Panel.this.word("extra_brightness", new Object[0]), new Runnable() { // from class: dev.colorcc.Panel.Sheet.42
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m218lambda$new$11$devcolorccPanel$Sheet();
                }
            });
            if (!Panel.this.samsungRune("QUICK_BAR_BRIGHTNESS_EXTRA_BRIGHTNESS")) {
                ((View) this.extra.getParent()).setVisibility(8);
            }
            this.tone = roundSwitch(context, linearLayoutHorizontal4, "M12,3c-4.97,0 -9,4.03 -9,9s4.03,9 9,9c0.83,0 1.5,-0.67 1.5,-1.5 0,-0.39 -0.15,-0.74 -0.39,-1.01 -0.23,-0.26 -0.38,-0.61 -0.38,-0.99 0,-0.83 0.67,-1.5 1.5,-1.5H16c2.76,0 5,-2.24 5,-5 0,-4.42 -4.03,-8 -9,-8zM6.5,12c-0.83,0 -1.5,-0.67 -1.5,-1.5S5.67,9 6.5,9 8,9.67 8,10.5 7.33,12 6.5,12zM9.5,8C8.67,8 8,7.33 8,6.5S8.67,5 9.5,5s1.5,0.67 1.5,1.5S10.33,8 9.5,8zM14.5,8c-0.83,0 -1.5,-0.67 -1.5,-1.5S13.67,5 14.5,5s1.5,0.67 1.5,1.5S15.33,8 14.5,8zM17.5,12c-0.83,0 -1.5,-0.67 -1.5,-1.5S16.67,9 17.5,9s1.5,0.67 1.5,1.5 -0.67,1.5 -1.5,1.5z", Panel.this.word("colour_tone", new Object[0]), new Runnable() { // from class: dev.colorcc.Panel.Sheet.43
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m220lambda$new$13$devcolorccPanel$Sheet();
                }
            });
            this.extras = column(context, imageView2, textViewLabel3, slider2, linearLayoutHorizontal4);
            LinearLayout linearLayout4 = new LinearLayout(context);
            this.list = linearLayout4;
            linearLayout4.setOrientation(1);
            ScrollView scrollView = new ScrollView(context);
            this.listScroll = scrollView;
            scrollView.setVerticalScrollBarEnabled(false);
            scrollView.setVerticalFadingEdgeEnabled(true);
            scrollView.setFadingEdgeLength(Panel.this.dp(24.0f));
            scrollView.setOutlineProvider(ViewOutlineProvider.BOUNDS);
            scrollView.setClipToOutline(true);
            scrollView.addView(linearLayout4, new FrameLayout.LayoutParams(-1, -2));
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, 0, 1.0f);
            layoutParams2.topMargin = Panel.this.dp(14.0f);
            this.card.addView(scrollView, layoutParams2);
            LinearLayout linearLayout5 = new LinearLayout(context);
            this.note = linearLayout5;
            linearLayout5.setOrientation(1);
            linearLayout5.setGravity(17);
            linearLayout5.setVisibility(8);
            ImageView imageView3 = new ImageView(context);
            imageView3.setImageDrawable(Icons.of("M12,3c-4.97,0 -9,4.03 -9,9s4.03,9 9,9 9,-4.03 9,-9c0,-0.46 -0.04,-0.92 -0.1,-1.36 -0.98,1.37 -2.58,2.26 -4.4,2.26 -2.98,0 -5.4,-2.42 -5.4,-5.4 0,-1.81 0.89,-3.42 2.26,-4.4C12.92,3.04 12.46,3 12,3z"));
            imageView3.setImageTintList(ColorStateList.valueOf(-2130706433));
            linearLayout5.addView(imageView3, new LinearLayout.LayoutParams(Panel.this.dp(72.0f), Panel.this.dp(72.0f)));
            TextView textViewLabel4 = Panel.this.label(context, 15.0f, -1711276033, 1);
            textViewLabel4.setText(Panel.this.word("dnd_off", new Object[0]));
            LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-2, -2);
            layoutParams3.topMargin = Panel.this.dp(Panel.DROP_HEADER);
            linearLayout5.addView(textViewLabel4, layoutParams3);
            this.card.addView(linearLayout5, new LinearLayout.LayoutParams(-1, 0, 1.0f));
            LinearLayout linearLayout6 = new LinearLayout(context);
            this.torchMenu = linearLayout6;
            linearLayout6.setOrientation(1);
            linearLayout6.setGravity(1);
            linearLayout6.setVisibility(8);
            TextView textViewLabel5 = Panel.this.label(context, Panel.DROP_HEADER, -1, 1);
            this.torchLabel = textViewLabel5;
            linearLayout6.addView(textViewLabel5, new LinearLayout.LayoutParams(-2, -2));
            TorchBeam torchBeam = Panel.this.new TorchBeam(context, new Consumer() { // from class: dev.colorcc.Panel.Sheet.44
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    Sheet.this.m222lambda$new$15$devcolorccPanel$Sheet((Float) obj);
                }
            });
            this.beam = torchBeam;
            torchBeam.setContentDescription(Panel.this.word("torch_brightness", new Object[0]));
            torchBeam.setBackground(new Glass(Panel.this.dp(34.0f), Panel.SHEET_GLASS, Panel.SHEET_GLASS, 0.5f, 0.5f));
            LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(-1, Panel.this.dp(400.0f));
            layoutParams4.topMargin = Panel.this.dp(40.0f);
            linearLayout6.addView(torchBeam, layoutParams4);
            this.card.addView(linearLayout6, new LinearLayout.LayoutParams(-1, -2));
            LinearLayout linearLayout7 = new LinearLayout(context);
            linearLayout7.setOrientation(1);
            View view = new View(context);
            view.setBackgroundColor(Panel.SHEET_GLASS);
            linearLayout7.addView(view, new LinearLayout.LayoutParams(-1, 1));
            TextView textViewLabel6 = Panel.this.label(context, Panel.DROP_HEADER, -1, 1);
            this.settings = textViewLabel6;
            textViewLabel6.setText(Panel.this.word("more_settings", new Object[0]));
            textViewLabel6.setGravity(17);
            textViewLabel6.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Sheet.34
                @Override // android.view.View.OnClickListener
                public final void onClick(View view2) {
                    Sheet.this.m223lambda$new$16$devcolorccPanel$Sheet(view2);
                }
            });
            linearLayout7.addView(textViewLabel6, new LinearLayout.LayoutParams(-1, Panel.this.dp(52.0f)));
            LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams5.topMargin = Panel.this.dp(8.0f);
            this.card.addView(linearLayout7, layoutParams5);
            addView(this.card, new FrameLayout.LayoutParams(Panel.this.dp(292.0f), Panel.this.dp(380.0f), 17));
            Motion.Value value = Panel.this.motion.value(Logic.Spring.MENU, new Motion.Listener() { // from class: dev.colorcc.Panel.Sheet.35
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Sheet.this.applyShow(f, f2);
                }
            });
            this.show = value;
            this.back = Panel.this.new Rewind(value, new Motion.Listener() { // from class: dev.colorcc.Panel.Sheet.35
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Sheet.this.applyShow(f, f2);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$0$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m215lambda$new$0$devcolorccPanel$Sheet(View view) {
            close();
        }

        /* JADX INFO: renamed from: lambda$new$1$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m216lambda$new$1$devcolorccPanel$Sheet() {
            Panel.this.toggleTileDirect(this.tile);
        }

        /* JADX INFO: renamed from: lambda$new$2$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m224lambda$new$2$devcolorccPanel$Sheet(View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Sheet.24
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m216lambda$new$1$devcolorccPanel$Sheet();
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$3$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m225lambda$new$3$devcolorccPanel$Sheet(float f, boolean z) {
            Panel.this.setVolume(this.menuStream, f);
            if (this.menuStream == 3) {
                Panel.this.volume.setValue(f);
            }
            showLoudness(f);
            if (this.menuStream != 2) {
                return;
            }
            boolean z2 = f <= 0.0f;
            if (z2 != (Panel.this.shownRingerSlot == 2)) {
                Panel.this.onRingerTap(z2 ? 2 : 0);
            }
        }

        /* JADX INFO: renamed from: lambda$new$4$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m226lambda$new$4$devcolorccPanel$Sheet() {
            Panel panel = Panel.this;
            panel.onRingerTap(panel.shownRingerSlot == 2 ? 0 : 2);
        }

        /* JADX INFO: renamed from: lambda$new$5$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m227lambda$new$5$devcolorccPanel$Sheet(float f, boolean z) {
            Panel.this.applyBrightness(f, z);
            Panel.this.brightness.setValue(f);
            showSun(f);
        }

        /* JADX INFO: renamed from: lambda$new$7$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m229lambda$new$7$devcolorccPanel$Sheet() {
            boolean z = !this.adaptiveOn;
            this.adaptiveOn = z;
            showSwitch(this.adaptive, z, true);
            final boolean z2 = this.adaptiveOn;
            Panel.this.audioThread.execute(new Runnable() { // from class: dev.colorcc.Panel.Sheet.3
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m228lambda$new$6$devcolorccPanel$Sheet(z2);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$6$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m228lambda$new$6$devcolorccPanel$Sheet(boolean z) {
            Panel.this.writeSetting(Settings.System.class, "screen_brightness_mode", z);
        }

        /* JADX INFO: renamed from: lambda$new$9$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m231lambda$new$9$devcolorccPanel$Sheet() {
            boolean z = !this.darkOn;
            this.darkOn = z;
            showSwitch(this.dark, z, true);
            final boolean z2 = this.darkOn;
            Panel.this.audioThread.execute(new Runnable() { // from class: dev.colorcc.Panel.Sheet.13
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m230lambda$new$8$devcolorccPanel$Sheet(z2);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$8$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m230lambda$new$8$devcolorccPanel$Sheet(boolean z) {
            Panel.this.writeDarkMode(z);
        }

        /* JADX INFO: renamed from: lambda$new$11$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m218lambda$new$11$devcolorccPanel$Sheet() {
            boolean z = !this.extraOn;
            this.extraOn = z;
            showSwitch(this.extra, z, true);
            final boolean z2 = this.extraOn;
            Panel.this.audioThread.execute(new Runnable() { // from class: dev.colorcc.Panel.Sheet.18
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m217lambda$new$10$devcolorccPanel$Sheet(z2);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$10$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m217lambda$new$10$devcolorccPanel$Sheet(boolean z) {
            Panel.this.writeSetting(Settings.Secure.class, Panel.EXTRA_BRIGHTNESS, z);
        }

        /* JADX INFO: renamed from: lambda$new$13$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m220lambda$new$13$devcolorccPanel$Sheet() {
            boolean z = !this.toneOn;
            this.toneOn = z;
            showSwitch(this.tone, z, true);
            final boolean z2 = this.toneOn;
            Panel.this.audioThread.execute(new Runnable() { // from class: dev.colorcc.Panel.Sheet.7
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m219lambda$new$12$devcolorccPanel$Sheet(z2);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$12$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m219lambda$new$12$devcolorccPanel$Sheet(boolean z) {
            Panel.this.writeSetting(Settings.System.class, Panel.COLOUR_TONE, z);
        }

        /* JADX INFO: renamed from: lambda$new$14$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m221lambda$new$14$devcolorccPanel$Sheet(Float f) {
            onTorch(f.floatValue());
        }

        /* JADX INFO: renamed from: lambda$new$15$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m222lambda$new$15$devcolorccPanel$Sheet(final Float f) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Sheet.17
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m221lambda$new$14$devcolorccPanel$Sheet(f);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$16$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m223lambda$new$16$devcolorccPanel$Sheet(View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Sheet.30
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.openTileSettings();
                }
            });
        }

        private LinearLayout column(Context context, ImageView imageView, TextView textView, Slider slider, LinearLayout linearLayout) {
            LinearLayout linearLayout2 = new LinearLayout(context);
            linearLayout2.setOrientation(1);
            linearLayout2.setGravity(1);
            linearLayout2.setVisibility(8);
            linearLayout2.setClipChildren(false);
            imageView.setImageTintList(ColorStateList.valueOf(-1));
            linearLayout2.addView(imageView, new LinearLayout.LayoutParams(Panel.this.dp(44.0f), Panel.this.dp(44.0f)));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
            layoutParams.topMargin = Panel.this.dp(8.0f);
            linearLayout2.addView(textView, layoutParams);
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(Panel.this.dp(104.0f), Panel.this.dp(250.0f));
            layoutParams2.topMargin = Panel.this.dp(24.0f);
            linearLayout2.addView(Panel.host(context, slider), layoutParams2);
            linearLayout.setClipChildren(false);
            LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams3.topMargin = Panel.this.dp(Panel.DROP_TOP);
            linearLayout2.addView(linearLayout, layoutParams3);
            LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams4.topMargin = Panel.this.dp(10.0f);
            this.card.addView(linearLayout2, layoutParams4);
            return linearLayout2;
        }

        private void only(View view) {
            View[] viewArr = {this.volumes, this.extras, this.listScroll, this.note, this.torchMenu};
            for (int r5 = 0; r5 < 5; r5++) {
                View view2 = viewArr[r5];
                view2.setVisibility(view2 == view ? 0 : 8);
            }
        }

        private void arrive() {
            this.leaving = false;
            this.back.stop();
        }

        private void leave() {
            this.leaving = true;
            this.back.start();
        }

        private void land() {
            Body bodyBodyAround = Panel.this.bodyAround(this.held);
            if (bodyBodyAround != null) {
                bodyBodyAround.press.kick(Panel.TOGGLE_KICK);
            }
        }

        @Override // android.widget.FrameLayout, android.view.View
        protected void onMeasure(int r4, int r5) {
            super.onMeasure(r4, r5);
            if (this.card.getLayoutParams().height == -2) {
                LinearLayout linearLayout = this.card;
                linearLayout.measure(View.MeasureSpec.makeMeasureSpec(linearLayout.getMeasuredWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(0, 0));
            } else if (this.listScroll.getVisibility() == 0) {
                int r6 = Math.min(this.card.getMeasuredHeight() + Math.max(0, this.list.getMeasuredHeight() - this.listScroll.getMeasuredHeight()), getMeasuredHeight() - Panel.this.dp(96.0f));
                if (r6 > this.card.getMeasuredHeight()) {
                    LinearLayout linearLayout2 = this.card;
                    linearLayout2.measure(View.MeasureSpec.makeMeasureSpec(linearLayout2.getMeasuredWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(r6, 1073741824));
                }
            }
        }

        @Override // android.view.ViewGroup, android.view.View
        public boolean dispatchTouchEvent(MotionEvent motionEvent) {
            if (motionEvent.getActionMasked() == 0 && getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            return super.dispatchTouchEvent(motionEvent);
        }

        private void from(View view) {
            this.held = view;
            view.getLocationOnScreen(this.origin);
            float scaleX = 1.0f;
            View view2 = view;
            while (view2 != null) {
                scaleX *= view2.getScaleX();
                view2 = view2.getParent() instanceof View ? (View) view2.getParent() : null;
            }
            this.heldSize[0] = view.getWidth() * scaleX;
            this.heldSize[1] = view.getHeight() * scaleX;
            int[] r6 = this.origin;
            r6[0] = r6[0] + Math.round(this.heldSize[0] / 2.0f);
            int[] r7 = this.origin;
            r7[1] = r7[1] + Math.round(this.heldSize[1] / 2.0f);
        }

        void openOutput(View view, String str) {
            if (this.tile != null || this.volumesOpen || this.brightnessOpen || this.outputOpen) {
                close();
            }
            from(view);
            Panel.this.player.close();
            this.outputOpen = true;
            this.outputPackage = str;
            this.menu = 7;
            this.title.setText(Panel.this.word("media_output", new Object[0]));
            this.toggle.setVisibility(8);
            this.sub.setVisibility(8);
            this.list.removeAllViews();
            this.shownRows = "";
            bare(false);
            only(this.listScroll);
            startOutputScan();
            refreshList();
            long[] jArr = {700, 1800, 3500};
            for (int r0 = 0; r0 < 3; r0++) {
                postDelayed(new Runnable() { // from class: dev.colorcc.Panel.Sheet.50
                    @Override // java.lang.Runnable
                    public final void run() {
                        Sheet.this.m234lambda$openOutput$17$devcolorccPanel$Sheet();
                    }
                }, jArr[r0]);
            }
            this.settings.setVisibility(0);
            arrive();
            setVisibility(0);
            this.show.to(1.0f);
        }

        /* JADX INFO: renamed from: lambda$openOutput$17$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m234lambda$openOutput$17$devcolorccPanel$Sheet() {
            Panel.this.guard(new AnonymousClass0());
        }

        private void startOutputScan() {
            this.outputRouter = null;
            if (this.outputPackage == null) {
                return;
            }
            try {
                Object objCallStaticMethod = ReflectUtils.callStaticMethod(MediaRouter2.class, "getInstance", new Object[]{getContext(), this.outputPackage});
                this.outputRouter = objCallStaticMethod;
                try {
                    ReflectUtils.callMethod(objCallStaticMethod, "startScan", new Object[0]);
                } catch (Throwable th) {
                    ColorCCBridge.log("outputs not scanned, the known ones only: " + th);
                }
            } catch (Throwable th2) {
                ColorCCBridge.log("outputs not read: " + th2);
            }
        }

        private void stopOutputScan() {
            Object obj = this.outputRouter;
            this.outputRouter = null;
            if (obj == null) {
                return;
            }
            try {
                ReflectUtils.callMethod(obj, "stopScan", new Object[0]);
            } catch (Throwable unused) {
            }
        }

        private void listOutputs() {
            final Object obj = this.outputRouter;
            if (obj == null) {
                item(null, Panel.this.word("no_outputs", new Object[0]), null, false, null);
                return;
            }
            List list = (List) ReflectUtils.callMethod(obj, "getControllers", new Object[0]);
            final HashSet hashSet = new HashSet();
            HashSet hashSet2 = new HashSet();
            HashSet hashSet3 = new HashSet();
            int r12 = 1;
            final Object obj2 = list.isEmpty() ? null : list.get(list.size() - 1);
            if (obj2 != null) {
                Iterator it = ((List) ReflectUtils.callMethod(obj2, "getSelectedRoutes", new Object[0])).iterator();
                while (it.hasNext()) {
                    hashSet.add(((MediaRoute2Info) it.next()).getId());
                }
                Iterator it2 = ((List) ReflectUtils.callMethod(obj2, "getSelectableRoutes", new Object[0])).iterator();
                while (it2.hasNext()) {
                    hashSet2.add(((MediaRoute2Info) it2.next()).getId());
                }
                Iterator it3 = ((List) ReflectUtils.callMethod(obj2, "getDeselectableRoutes", new Object[0])).iterator();
                while (it3.hasNext()) {
                    hashSet3.add(((MediaRoute2Info) it3.next()).getId());
                }
            }
            String str = hashSet.size() + " playing, " + hashSet2.size() + " can join";
            if (!str.equals(this.outputNote)) {
                this.outputNote = str;
                ColorCCBridge.log("media output: " + str);
            }
            ArrayList<MediaRoute2Info> arrayList = new ArrayList();
            HashSet hashSet4 = new HashSet();
            for (MediaRoute2Info mediaRoute2Info : (List<MediaRoute2Info>) ReflectUtils.callMethod(obj, "getAllRoutes", new Object[0])) {
                if (!TextUtils.isEmpty(mediaRoute2Info.getName()) && hashSet4.add(mediaRoute2Info.getId())) {
                    arrayList.add(mediaRoute2Info);
                }
            }
            arrayList.sort(new Comparator() { // from class: dev.colorcc.Panel.Sheet.19
                @Override // java.util.Comparator
                public final int compare(Object obj3, Object obj4) {
                    return Sheet.this.m209lambda$listOutputs$18$devcolorccPanel$Sheet(hashSet, (MediaRoute2Info) obj3, (MediaRoute2Info) obj4);
                }
            });
            for (final MediaRoute2Info mediaRoute2Info2 : arrayList) {
                final Runnable runnable;
                boolean zContains = hashSet.contains(mediaRoute2Info2.getId());
                int r2 = (zContains || !hashSet2.contains(mediaRoute2Info2.getId())) ? 0 : r12;
                if (zContains && hashSet.size() > r12 && hashSet3.contains(mediaRoute2Info2.getId())) {
                    runnable = new Runnable() { // from class: dev.colorcc.Panel.Sheet.20
                        @Override // java.lang.Runnable
                        public final void run() {
                            Sheet.lambda$listOutputs$19(obj2, mediaRoute2Info2);
                        }
                    };
                } else if (r2 != 0) {
                    runnable = new Runnable() { // from class: dev.colorcc.Panel.Sheet.21
                        @Override // java.lang.Runnable
                        public final void run() {
                            Sheet.lambda$listOutputs$20(obj2, mediaRoute2Info2);
                        }
                    };
                } else {
                    runnable = zContains ? null : new Runnable() { // from class: dev.colorcc.Panel.Sheet.22
                        @Override // java.lang.Runnable
                        public final void run() {
                            Sheet.lambda$listOutputs$21(obj, mediaRoute2Info2);
                        }
                    };
                }
                item(outputIcon(mediaRoute2Info2.getType()), mediaRoute2Info2.getName().toString(), zContains ? Panel.this.word("connected", new Object[0]) : r2 != 0 ? Panel.this.word("add", new Object[0]) : null, zContains, runnable == null ? null : new Runnable() { // from class: dev.colorcc.Panel.Sheet.23
                    @Override // java.lang.Runnable
                    public final void run() {
                        Sheet.this.m211lambda$listOutputs$23$devcolorccPanel$Sheet(runnable);
                    }
                }).radio = Boolean.valueOf(zContains);
                r12 = 1;
            }
            if (arrayList.isEmpty()) {
                item(null, Panel.this.word("no_outputs", new Object[0]), null, false, null);
            }
        }

        /* JADX INFO: renamed from: lambda$listOutputs$18$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ int m209lambda$listOutputs$18$devcolorccPanel$Sheet(Set set, MediaRoute2Info mediaRoute2Info, MediaRoute2Info mediaRoute2Info2) {
            return Integer.compare(outputRank(mediaRoute2Info, set), outputRank(mediaRoute2Info2, set));
        }

        static /* synthetic */ void lambda$listOutputs$19(Object obj, MediaRoute2Info mediaRoute2Info) {
            ReflectUtils.callMethod(obj, "deselectRoute", new Object[]{mediaRoute2Info});
        }

        static /* synthetic */ void lambda$listOutputs$20(Object obj, MediaRoute2Info mediaRoute2Info) {
            ReflectUtils.callMethod(obj, "selectRoute", new Object[]{mediaRoute2Info});
        }

        static /* synthetic */ void lambda$listOutputs$21(Object obj, MediaRoute2Info mediaRoute2Info) {
            ReflectUtils.callMethod(obj, "transferTo", new Object[]{mediaRoute2Info});
        }

        /* JADX INFO: renamed from: lambda$listOutputs$23$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m211lambda$listOutputs$23$devcolorccPanel$Sheet(Runnable runnable) {
            runnable.run();
            postDelayed(new Runnable() { // from class: dev.colorcc.Panel.Sheet.14
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m210lambda$listOutputs$22$devcolorccPanel$Sheet();
                }
            }, 600L);
        }

        /* JADX INFO: renamed from: lambda$listOutputs$22$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m210lambda$listOutputs$22$devcolorccPanel$Sheet() {
            Panel.this.guard(new AnonymousClass0());
        }

        private int outputRank(MediaRoute2Info mediaRoute2Info, Set<String> set) {
            if (mediaRoute2Info.getType() == 2) {
                return 0;
            }
            return set.contains(mediaRoute2Info.getId()) ? 1 : 2;
        }

        private String outputIcon(int r1) {
            if (r1 == 2) {
                return "M16.5,3h-9C6.67,3 6,3.67 6,4.5v15c0,0.83 0.67,1.5 1.5,1.5h9c0.83,0 1.5,-0.67 1.5,-1.5v-15c0,-0.83 -0.67,-1.5 -1.5,-1.5zM16,19H8V5h8v14z";
            }
            if (r1 == 3 || r1 == Panel.TILES_PER_ROW || r1 == 8 || r1 == 26 || r1 == Panel.GESTURE_ROOM || r1 == 23) {
                return "M12,3c-4.97,0 -9,4.03 -9,9v7c0,1.1 0.9,2 2,2h4v-8H5v-1c0,-3.87 3.13,-7 7,-7s7,3.13 7,7v1h-4v8h4c1.1,0 2,-0.9 2,-2v-7c0,-4.97 -4.03,-9 -9,-9z";
            }
            return "M21,3H3C1.9,3 1,3.9 1,5v3h2V5h18v14h-7v2h7c1.1,0 2,-0.9 2,-2V5C23,3.9 22.1,3 21,3zM1,18v3h3C4,19.34 2.66,18 1,18zM1,14v2c2.76,0 5,2.24 5,5h2C8,17.13 4.87,14 1,14zM1,10v2c4.97,0 9,4.03 9,9h2C12,14.92 7.07,10 1,10z";
        }

        void openBrightness() {
            if (this.tile != null || this.volumesOpen) {
                close();
            }
            Panel.this.player.close();
            from(Panel.this.brightness);
            this.brightnessOpen = true;
            bare(true);
            only(this.extras);
            this.level.setValue(Panel.this.brightness.value());
            showSun(Panel.this.brightness.value());
            boolean z = (getResources().getConfiguration().uiMode & 48) == 32;
            this.darkOn = z;
            showSwitch(this.dark, z, false);
            Panel.this.audioThread.execute(new Runnable() { // from class: dev.colorcc.Panel.Sheet.15
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m233lambda$openBrightness$25$devcolorccPanel$Sheet();
                }
            });
            arrive();
            setVisibility(0);
            this.show.to(1.0f);
        }

        /* JADX INFO: renamed from: lambda$openBrightness$25$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m233lambda$openBrightness$25$devcolorccPanel$Sheet() {
            final boolean setting = Panel.this.readSetting(Settings.System.class, "screen_brightness_mode");
            final boolean setting2 = Panel.this.readSetting(Settings.Secure.class, Panel.EXTRA_BRIGHTNESS);
            final boolean setting3 = Panel.this.readSetting(Settings.System.class, Panel.COLOUR_TONE);
            Panel.this.main.post(new Runnable() { // from class: dev.colorcc.Panel.Sheet.32
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m232lambda$openBrightness$24$devcolorccPanel$Sheet(setting, setting2, setting3);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$openBrightness$24$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m232lambda$openBrightness$24$devcolorccPanel$Sheet(boolean z, boolean z2, boolean z3) {
            if (this.brightnessOpen) {
                this.adaptiveOn = z;
                showSwitch(this.adaptive, z, false);
                this.extraOn = z2;
                showSwitch(this.extra, z2, false);
                this.toneOn = z3;
                showSwitch(this.tone, z3, false);
            }
        }

        private void bare(boolean z) {
            this.card.setBackground(z ? null : this.cardGlass);
            this.card.setClickable(!z);
            this.card.getChildAt(0).setVisibility(z ? 8 : 0);
            LinearLayout linearLayout = this.card;
            linearLayout.getChildAt(linearLayout.getChildCount() - 1).setVisibility(z ? 8 : 0);
            this.card.getLayoutParams().height = z ? -2 : Panel.this.dp(380.0f);
            this.card.requestLayout();
        }

        private Disc roundSwitch(Context context, LinearLayout linearLayout, String str, String str2, final Runnable runnable) {
            LinearLayout linearLayout2 = new LinearLayout(context);
            linearLayout2.setOrientation(1);
            linearLayout2.setGravity(1);
            linearLayout2.setClipChildren(false);
            Panel panel = Panel.this;
            Disc disc = panel.new Disc(context, panel.dp(24.0f));
            disc.icon.setImageDrawable(Icons.of(str));
            disc.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Sheet.49
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    Sheet.this.m235lambda$roundSwitch$26$devcolorccPanel$Sheet(runnable, view);
                }
            });
            linearLayout2.addView(disc, new LinearLayout.LayoutParams(Panel.this.dp(58.0f), Panel.this.dp(58.0f)));
            TextView textViewLabel = Panel.this.label(context, Panel.DROP_STEP, -1, 2);
            textViewLabel.setText(str2);
            textViewLabel.setGravity(1);
            textViewLabel.setIncludeFontPadding(false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
            layoutParams.topMargin = Panel.this.dp(8.0f);
            linearLayout2.addView(textViewLabel, layoutParams);
            TextView textViewLabel2 = Panel.this.label(context, 11.0f, -1275068417, 1);
            textViewLabel2.setGravity(1);
            disc.setTag(textViewLabel2);
            linearLayout2.addView(textViewLabel2, new LinearLayout.LayoutParams(-2, -2));
            linearLayout.addView(linearLayout2, new LinearLayout.LayoutParams(0, -2, 1.0f));
            disc.setContentDescription(str2.replace('\n', ' '));
            return disc;
        }

        /* JADX INFO: renamed from: lambda$roundSwitch$26$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m235lambda$roundSwitch$26$devcolorccPanel$Sheet(Runnable runnable, View view) {
            Panel.this.guard(runnable);
        }

        private Disc streamButton(Context context, LinearLayout linearLayout, final int r13) {
            LinearLayout linearLayout2 = new LinearLayout(context);
            linearLayout2.setOrientation(1);
            linearLayout2.setGravity(1);
            linearLayout2.setClipChildren(false);
            Panel panel = Panel.this;
            Disc disc = panel.new Disc(context, panel.dp(20.0f));
            String[] strArr = {Panel.this.word("media", new Object[0]), Panel.this.word("ringtone", new Object[0]), Panel.this.word("notifications", new Object[0]), Panel.this.word("system", new Object[0])};
            disc.icon.setImageDrawable(Icons.of(this.STREAM_ICONS[r13]));
            disc.setContentDescription(strArr[r13]);
            disc.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Sheet.45
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    Sheet.this.m237lambda$streamButton$28$devcolorccPanel$Sheet(r13, view);
                }
            });
            linearLayout2.addView(disc, new LinearLayout.LayoutParams(Panel.this.dp(46.0f), Panel.this.dp(46.0f)));
            TextView textViewLabel = Panel.this.label(context, 11.0f, -1, 1);
            textViewLabel.setText(strArr[r13]);
            textViewLabel.setGravity(1);
            textViewLabel.setIncludeFontPadding(false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
            layoutParams.topMargin = Panel.this.dp(6.0f);
            linearLayout2.addView(textViewLabel, layoutParams);
            linearLayout.addView(linearLayout2, new LinearLayout.LayoutParams(0, -2, 1.0f));
            return disc;
        }

        /* JADX INFO: renamed from: lambda$streamButton$27$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m236lambda$streamButton$27$devcolorccPanel$Sheet(int r2) {
            pickStream(r2, true);
        }

        /* JADX INFO: renamed from: lambda$streamButton$28$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m237lambda$streamButton$28$devcolorccPanel$Sheet(final int r2, View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Sheet.31
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m236lambda$streamButton$27$devcolorccPanel$Sheet(r2);
                }
            });
        }

        private void pickStream(int r6, boolean z) {
            Drawable drawableOf;
            this.menuStream = this.STREAMS[r6];
            int r1 = 0;
            while (r1 < this.STREAMS.length) {
                Disc[] discArr = this.streamButtons;
                if (z) {
                    discArr[r1].level.to(r1 != r6 ? 0.0f : 1.0f);
                } else {
                    discArr[r1].level.snap(r1 != r6 ? 0.0f : 1.0f);
                }
                this.streamButtons[r1].setSelected(r1 == r6);
                r1++;
            }
            ImageView imageView = this.loudIcon;
            if (this.menuStream != 3) {
                drawableOf = Icons.of(this.STREAM_ICONS[r6]);
            } else {
                drawableOf = headphonesOn() ? this.headphones : this.speaker;
            }
            imageView.setImageDrawable(drawableOf);
            this.loud.setContentDescription(this.streamButtons[r6].getContentDescription());
            Panel.this.refreshRing();
        }

        private void showSwitch(Disc disc, boolean z, boolean z2) {
            if (z2) {
                disc.level.to(z ? 1.0f : 0.0f);
            } else {
                disc.level.snap(z ? 1.0f : 0.0f);
            }
            ((TextView) disc.getTag()).setText(Panel.this.word(z ? "on" : "off", new Object[0]));
            disc.setStateDescription(Panel.this.word(z ? "on" : "off", new Object[0]));
        }

        void openVolumes() {
            if (this.tile != null || this.brightnessOpen) {
                close();
            }
            Panel.this.player.close();
            from(Panel.this.volume);
            this.volumesOpen = true;
            bare(true);
            only(this.volumes);
            pickStream(0, false);
            this.loud.setValue(Panel.this.volume.value());
            showLoudness(Panel.this.volume.value());
            showSwitch(this.silent, Panel.this.shownRingerSlot == 2, false);
            showSilentIcon(Panel.this.shownRingerSlot == 2);
            Panel.this.refreshRing();
            arrive();
            setVisibility(0);
            this.show.to(1.0f);
        }

        private boolean headphonesOn() {
            for (AudioDeviceInfo audioDeviceInfo : Panel.this.audio.getDevices(2)) {
                int type = audioDeviceInfo.getType();
                if (type == 3 || type == Panel.TILES_PER_ROW || type == 8 || type == Panel.GESTURE_ROOM || type == 26) {
                    return true;
                }
            }
            return false;
        }

        private void showLoudness(float f) {
            String strWord;
            int r0 = Math.round(Logic.clamp(f) * 100.0f);
            TextView textView = this.loudHeading;
            if (r0 == 0) {
                strWord = Panel.this.word("volume", new Object[0]);
            } else {
                strWord = r0 + "%";
            }
            textView.setText(strWord);
            this.loudIcon.getDrawable().setLevel(Math.round(Logic.clamp(f) * 10000.0f));
        }

        private void showSun(float f) {
            float fClamp = Logic.clamp(f);
            this.sun.setRotation(120.0f * fClamp);
            float f2 = (fClamp * 0.35f) + 0.8f;
            this.sun.setScaleX(f2);
            this.sun.setScaleY(f2);
        }

        void showBrightness(float f) {
            if (this.brightnessOpen) {
                this.level.setValue(f);
                showSun(this.level.value());
            }
        }

        void showRing(int r2, float f, boolean z, boolean z2) {
            if (this.volumesOpen) {
                if (r2 == this.menuStream) {
                    this.loud.setValue(f);
                    showLoudness(this.loud.value());
                }
                showSwitch(this.silent, z, z2);
                showSilentIcon(z);
            }
        }

        private void showSilentIcon(boolean z) {
            Boolean bool = this.silentShown;
            if (bool == null || bool.booleanValue() != z) {
                this.silentShown = Boolean.valueOf(z);
                this.silent.icon.setImageDrawable(Icons.of(z ? "M16.5,12c0,-1.77 -1.02,-3.29 -2.5,-4.03v2.21l2.45,2.45c0.03,-0.2 0.05,-0.41 0.05,-0.63zM19,12c0,0.94 -0.2,1.82 -0.54,2.64l1.51,1.51C20.63,14.91 21,13.5 21,12c0,-4.28 -2.99,-7.86 -7,-8.77v2.06c2.89,0.86 5,3.54 5,6.71zM4.27,3L3,4.27 7.73,9H3v6h4l5,5v-6.73l4.25,4.25c-0.67,0.52 -1.42,0.93 -2.25,1.18v2.06c1.38,-0.31 2.63,-0.95 3.69,-1.81L19.73,21 21,19.73l-9,-9L4.27,3zM12,4L9.91,6.09 12,8.18V4z" : "M18,16v-5c0,-3.07 -1.63,-5.64 -4.5,-6.32V4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5v0.68C7.64,5.36 6,7.92 6,11v5l-2,2v1h16v-1l-2,-2zM12,22c1.1,0 2,-0.9 2,-2h-4c0,1.1 0.9,2 2,2z"));
            }
        }

        void showMuted(boolean[] zArr) {
            for (int r0 = 0; r0 < zArr.length; r0++) {
                boolean z = zArr[r0];
                boolean[] zArr2 = this.mutedShown;
                if (z != zArr2[r0]) {
                    zArr2[r0] = z;
                    ImageView imageView = this.streamButtons[r0].icon;
                    boolean z2 = zArr[r0];
                    String[] strArr = this.STREAM_ICONS;
                    imageView.setImageDrawable(z2 ? Icons.muted(strArr[r0]) : Icons.of(strArr[r0]));
                }
            }
        }

        void open(Object obj, View view) {
            if (this.tile != null || this.volumesOpen || this.brightnessOpen) {
                close();
            }
            Panel.this.player.close();
            from(view);
            try {
                this.watch = Panel.this.new Bound(obj, new Consumer() { // from class: dev.colorcc.Panel.Sheet.16
                    @Override // java.util.function.Consumer
                    public final void accept(Object obj2) {
                        Sheet.this.render(obj2);
                    }
                });
                this.tile = obj;
                this.toggle.setVisibility(0);
                this.menu = Logic.menu(String.valueOf(ReflectUtils.callMethod(obj, "getTileSpec", new Object[0])));
                this.title.setText(Panel.this.word(Logic.MENU_WORDS[this.menu], new Object[0]));
                this.sub.setVisibility(8);
                this.list.removeAllViews();
                this.shownRows = "";
                boolean z = this.menu == 6;
                bare(z);
                only(z ? this.torchMenu : this.listScroll);
                if (z) {
                    if (!findTorch()) {
                        ColorCCBridge.log("torch menu without a camera torch");
                    }
                } else {
                    watchList();
                }
                render(ReflectUtils.callMethod(obj, "getState", new Object[0]));
                this.settings.setVisibility(0);
                arrive();
                setVisibility(0);
                this.show.to(1.0f);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }

        void close() {
            if (this.volumesOpen || this.brightnessOpen || this.outputOpen) {
                if (this.outputOpen) {
                    stopOutputScan();
                }
                this.outputOpen = false;
                this.brightnessOpen = false;
                this.volumesOpen = false;
                leave();
                return;
            }
            final Object obj = this.tile;
            if (obj == null) {
                return;
            }
            final Object obj2 = this.watch.callback;
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Sheet.4
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.lambda$close$29(obj, obj2);
                }
            });
            this.tile = null;
            this.watch = null;
            this.leaving = true;
            this.shownRows = "";
            if (this.listWatched) {
                this.listWatched = false;
                Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Sheet.5
                    @Override // java.lang.Runnable
                    public final void run() {
                        Sheet.this.m205lambda$close$30$devcolorccPanel$Sheet();
                    }
                });
            }
            leave();
        }

        static /* synthetic */ void lambda$close$29(Object obj, Object obj2) {
            ReflectUtils.callMethod(obj, "removeCallback", new Object[]{obj2});
        }

        /* JADX INFO: renamed from: lambda$close$30$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m205lambda$close$30$devcolorccPanel$Sheet() {
            getContext().unregisterReceiver(this.listReceiver);
        }

        @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
        protected void onLayout(boolean z, int r2, int r3, int r4, int r5) {
            Motion.Value value;
            super.onLayout(z, r2, r3, r4, r5);
            if (z && getVisibility() == 0 && (value = this.show) != null) {
                applyShow(value.value(), 0.0f);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void applyShow(float f, float f2) {
            LinearLayout linearLayout;
            float fClamp = Logic.clamp(f);
            getBackground().setAlpha(Math.round(255.0f * fClamp));
            this.card.setAlpha(Logic.clamp(Panel.BLUR_CUSTOM_MAX * f));
            float fClamp2 = Logic.clamp((f - Panel.MEDIA_EDGE_LIGHT) / 0.5f);
            int r2 = 0;
            while (true) {
                int childCount = this.card.getChildCount();
                linearLayout = this.card;
                if (r2 >= childCount) {
                    break;
                }
                linearLayout.getChildAt(r2).setAlpha(fClamp2);
                r2++;
            }
            if (linearLayout.getWidth() > 0) {
                getLocationOnScreen(this.at);
                int width = this.card.getWidth();
                int height = this.card.getHeight();
                float f3 = width;
                float f4 = f3 / 2.0f;
                float left = this.at[0] + this.card.getLeft() + f4;
                float f5 = height;
                float f6 = f5 / 2.0f;
                float top = this.at[1] + this.card.getTop() + f6;
                float f7 = 1.0f - f;
                float fClamp3 = Logic.clamp(f);
                this.card.setPivotX(f4);
                this.card.setPivotY(f6);
                this.card.setTranslationX((this.origin[0] - left) * f7);
                this.card.setTranslationY((this.origin[1] - top) * f7);
                float fMin = Math.min(1.0f, (getHeight() - Panel.this.dp(24.0f)) / f5);
                float[] fArr = this.heldSize;
                float fMax = Math.max(Panel.MAX_SMEAR, Math.max(fArr[0] / f3, fArr[1] / f5));
                float f8 = ((fMin - fMax) * f) + fMax;
                this.card.setScaleX(f8);
                this.card.setScaleY(f8);
                float fMin2 = Math.min(f3, this.heldSize[0] / fMax);
                float fMin3 = Math.min(f5, this.heldSize[1] / fMax);
                float[] fArr2 = this.heldSize;
                float fPillRadius = Glass.pillRadius(fArr2[0], fArr2[1]) / fMax;
                this.windowW = fMin2 + ((f3 - fMin2) * fClamp3);
                this.windowH = fMin3 + ((f5 - fMin3) * fClamp3);
                this.windowRound = fPillRadius + ((Panel.this.dp(34.0f) - fPillRadius) * fClamp3);
                boolean z = fClamp3 < 0.999f;
                if (this.card.getClipToOutline() != z) {
                    this.card.setClipToOutline(z);
                }
                if (z) {
                    this.card.invalidateOutline();
                }
            }
            Panel.this.sheetBack = fClamp;
            Panel.this.backdrop();
            if (!this.leaving || f > 0.01f || getVisibility() == 8) {
                return;
            }
            setVisibility(8);
            land();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void render(Object obj) {
            if (obj == null || this.tile == null) {
                return;
            }
            int r7 = Panel.stateOf(obj);
            boolean z = r7 == 2;
            this.toggle.set(z, getVisibility() == 0);
            this.toggle.setEnabled(r7 != 0);
            if (this.menu == 6) {
                this.torchLabel.setText(Panel.this.word(z ? "torch_on" : "torch_off", new Object[0]));
                if (this.beam.held()) {
                    return;
                }
                this.beam.set(z ? this.torchStrength : 0.0f, getVisibility() == 0);
                return;
            }
            refreshList();
        }

        private void watchList() {
            IntentFilter intentFilter = new IntentFilter();
            int r0 = this.menu;
            if (r0 == 2) {
                intentFilter.addAction("android.bluetooth.device.action.ACL_CONNECTED");
                intentFilter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
                intentFilter.addAction("android.bluetooth.device.action.BOND_STATE_CHANGED");
                intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
            } else if (r0 == 1) {
                intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
                intentFilter.addAction("android.net.wifi.STATE_CHANGE");
                intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
                Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Sheet.47
                    @Override // java.lang.Runnable
                    public final void run() {
                        Sheet.this.m238lambda$watchList$31$devcolorccPanel$Sheet();
                    }
                });
            } else if (r0 == 3) {
                this.usage = null;
                Panel.this.audioThread.execute(new Runnable() { // from class: dev.colorcc.Panel.Sheet.48
                    @Override // java.lang.Runnable
                    public final void run() {
                        Sheet.this.m240lambda$watchList$33$devcolorccPanel$Sheet();
                    }
                });
            }
            if (intentFilter.countActions() > 0) {
                getContext().registerReceiver(this.listReceiver, intentFilter, null, Panel.this.main, 2);
                this.listWatched = true;
            }
        }

        /* JADX INFO: renamed from: lambda$watchList$31$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m238lambda$watchList$31$devcolorccPanel$Sheet() {
            ((WifiManager) getContext().getSystemService(WifiManager.class)).startScan();
        }

        /* JADX INFO: renamed from: lambda$watchList$33$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m240lambda$watchList$33$devcolorccPanel$Sheet() {
            final String usage = readUsage();
            Panel.this.main.post(new Runnable() { // from class: dev.colorcc.Panel.Sheet.46
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m239lambda$watchList$32$devcolorccPanel$Sheet(usage);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$watchList$32$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m239lambda$watchList$32$devcolorccPanel$Sheet(String str) {
            this.usage = str;
            Panel.this.guard(new AnonymousClass0());
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void refreshList() {
            int r4;
            if (this.tile != null || this.outputOpen) {
                this.rows.clear();
                try {
                    int r1 = this.menu;
                    if (r1 == 2) {
                        listBluetooth();
                    } else if (r1 == 1) {
                        listWifi();
                    } else if (r1 == 3) {
                        listSims();
                    } else if (r1 == Panel.TILES_PER_ROW) {
                        listHotspot();
                    } else if (r1 == 5) {
                        listDnd();
                    } else if (r1 == 7) {
                        listOutputs();
                    }
                } catch (Throwable th) {
                    ColorCCBridge.log("menu not listed: " + th);
                }
                StringBuilder sb = new StringBuilder();
                Iterator<Row> it = this.rows.iterator();
                while (it.hasNext()) {
                    sb.append(it.next());
                    sb.append('\n');
                }
                if (sb.toString().equals(this.shownRows)) {
                    return;
                }
                this.shownRows = sb.toString();
                this.list.removeAllViews();
                for (int r2 = 0; r2 < this.rows.size(); r2++) {
                    Row row = this.rows.get(r2);
                    addRow(row, (row.header || (r4 = r2 + 1) >= this.rows.size() || this.rows.get(r4).header) ? false : true);
                }
            }
        }

        private void heading(String str, String str2, Runnable runnable) {
            Row row = new Row();
            row.header = true;
            row.name = str;
            row.side = str2;
            row.sideAction = runnable;
            this.rows.add(row);
        }

        private Row item(String str, String str2, String str3, boolean z, Runnable runnable) {
            Row row = new Row();
            row.icon = str;
            row.name = str2;
            row.status = str3;
            row.on = z;
            row.action = runnable;
            this.rows.add(row);
            return row;
        }

        private void listBluetooth() {
            BluetoothAdapter adapter = ((BluetoothManager) getContext().getSystemService(BluetoothManager.class)).getAdapter();
            if (adapter == null || !adapter.isEnabled()) {
                item(null, Panel.this.word("bluetooth_off", new Object[0]), null, false, null);
                return;
            }
            ArrayList<BluetoothDevice> arrayList = new ArrayList(adapter.getBondedDevices());
            arrayList.sort(new Comparator() { // from class: dev.colorcc.Panel.Sheet.25
                @Override // java.util.Comparator
                public final int compare(Object obj, Object obj2) {
                    return Sheet.this.m206lambda$listBluetooth$34$devcolorccPanel$Sheet((BluetoothDevice) obj, (BluetoothDevice) obj2);
                }
            });
            heading(Panel.this.word("paired_devices", new Object[0]), null, null);
            for (final BluetoothDevice bluetoothDevice : arrayList) {
                final boolean zConnected = connected(bluetoothDevice);
                item("M17.71,7.71L12,2h-1v7.59L6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 11,14.41V22h1l5.71,-5.71 -4.3,-4.29 4.3,-4.29zM13,5.83l1.88,1.88L13,9.59V5.83zM14.88,16.29L13,18.17v-3.76l1.88,1.88z", bluetoothDevice.getAlias() != null ? bluetoothDevice.getAlias() : bluetoothDevice.getName() != null ? bluetoothDevice.getName() : bluetoothDevice.getAddress(), zConnected ? Panel.this.word("connected", new Object[0]) : null, zConnected, new Runnable() { // from class: dev.colorcc.Panel.Sheet.26
                    @Override // java.lang.Runnable
                    public final void run() {
                        Sheet.this.m207lambda$listBluetooth$35$devcolorccPanel$Sheet(bluetoothDevice, zConnected);
                    }
                });
            }
            if (arrayList.isEmpty()) {
                item(null, Panel.this.word("no_paired", new Object[0]), null, false, null);
            }
        }

        /* JADX INFO: renamed from: lambda$listBluetooth$34$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ int m206lambda$listBluetooth$34$devcolorccPanel$Sheet(BluetoothDevice bluetoothDevice, BluetoothDevice bluetoothDevice2) {
            return Boolean.compare(connected(bluetoothDevice2), connected(bluetoothDevice));
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* JADX INFO: renamed from: bluetoothTap, reason: merged with bridge method [inline-methods] */
        public void m207lambda$listBluetooth$35$devcolorccPanel$Sheet(BluetoothDevice bluetoothDevice, boolean z) {
            Object obj = null;
            try {
                for (Object obj2 : (Collection) ReflectUtils.callMethod(ReflectUtils.getObjectField(this.tile, "mController"), "getDevices", new Object[0])) {
                    if (bluetoothDevice.equals(ReflectUtils.getObjectField(obj2, "mDevice"))) {
                        obj = obj2;
                    }
                }
            } catch (Throwable th) {
                ColorCCBridge.log("bluetooth devices not read from SystemUI: " + th);
            }
            StringBuilder sb = new StringBuilder("bluetooth ");
            sb.append(z ? "disconnect" : "connect");
            sb.append(obj != null ? " via SystemUI" : " direct");
            ColorCCBridge.log(sb.toString());
            if (obj == null) {
                StringBuilder sb2 = new StringBuilder("bluetooth result: ");
                sb2.append(ReflectUtils.callMethod(bluetoothDevice, z ? "disconnect" : "connect", new Object[0]));
                ColorCCBridge.log(sb2.toString());
            } else {
                if (z) {
                    ReflectUtils.callMethod(obj, "disconnect", new Object[0]);
                    return;
                }
                String[] strArr = {"connect", "connect$1", "connectDevice"};
                for (int r6 = 0; r6 < 3; r6++) {
                    try {
                        ReflectUtils.callMethod(obj, strArr[r6], new Object[0]);
                        return;
                    } catch (NoSuchMethodError unused) {
                    }
                }
                ColorCCBridge.log("bluetooth connect method missing");
            }
        }

        private boolean connected(BluetoothDevice bluetoothDevice) {
            try {
                return ((Boolean) ReflectUtils.callMethod(bluetoothDevice, "isConnected", new Object[0])).booleanValue();
            } catch (Throwable unused) {
                return false;
            }
        }

        private void listWifi() {
            final WifiManager wifiManager = (WifiManager) getContext().getSystemService(WifiManager.class);
            if (!wifiManager.isWifiEnabled()) {
                item(null, Panel.this.word("wifi_off", new Object[0]), null, false, null);
                return;
            }
            String strSsid = ssid(wifiManager.getConnectionInfo().getSSID());
            HashMap map = new HashMap();
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            if (configuredNetworks != null) {
                for (WifiConfiguration wifiConfiguration : configuredNetworks) {
                    map.put(ssid(wifiConfiguration.SSID), Integer.valueOf(wifiConfiguration.networkId));
                }
            }
            ArrayList<ScanResult> arrayList = new ArrayList(wifiManager.getScanResults());
            arrayList.sort(new Comparator() { // from class: dev.colorcc.Panel.Sheet.8
                @Override // java.util.Comparator
                public final int compare(Object obj, Object obj2) {
                    return Sheet.lambda$listWifi$36((ScanResult) obj, (ScanResult) obj2);
                }
            });
            LinkedHashMap linkedHashMap = new LinkedHashMap();
            for (ScanResult scanResult : arrayList) {
                String strSsid2 = ssid(scanResult.SSID);
                if (!strSsid2.isEmpty() && !linkedHashMap.containsKey(strSsid2)) {
                    linkedHashMap.put(strSsid2, scanResult);
                }
            }
            List<String> arrayList2 = new ArrayList(linkedHashMap.keySet());
            if (arrayList2.remove(strSsid)) {
                arrayList2.add(0, strSsid);
            }
            if (arrayList2.size() > 12) {
                arrayList2 = arrayList2.subList(0, 12);
            }
            boolean z = false;
            for (final String str : arrayList2) {
                boolean zEquals = str.equals(strSsid);
                final Integer num = (Integer) map.get(str);
                if (zEquals || num != null) {
                    if (!z) {
                        heading(Panel.this.word("saved_networks", new Object[0]), null, null);
                    }
                    mark(item("M1,9l2,2c4.97,-4.97 13.03,-4.97 18,0l2,-2C16.93,2.93 7.08,2.93 1,9zM9,17l3,3 3,-3c-1.65,-1.66 -4.34,-1.66 -6,0zM5,13l2,2c2.76,-2.76 7.24,-2.76 10,0l2,-2C15.14,9.14 8.87,9.14 5,13z", str, zEquals ? Panel.this.word("connected", new Object[0]) : null, zEquals, zEquals ? null : new Runnable() { // from class: dev.colorcc.Panel.Sheet.9
                        @Override // java.lang.Runnable
                        public final void run() {
                            Sheet.this.m213lambda$listWifi$37$devcolorccPanel$Sheet(wifiManager, str, num);
                        }
                    }), (ScanResult) linkedHashMap.get(str));
                    z = true;
                }
            }
            heading(Panel.this.word("other_networks", new Object[0]), Panel.this.word("refresh", new Object[0]), new Runnable() { // from class: dev.colorcc.Panel.Sheet.10
                @Override // java.lang.Runnable
                public final void run() {
                    wifiManager.startScan();
                }
            });
            boolean z2 = false;
            for (final String str2 : arrayList2) {
                if (!str2.equals(strSsid) && !map.containsKey(str2)) {
                    mark(item("M1,9l2,2c4.97,-4.97 13.03,-4.97 18,0l2,-2C16.93,2.93 7.08,2.93 1,9zM9,17l3,3 3,-3c-1.65,-1.66 -4.34,-1.66 -6,0zM5,13l2,2c2.76,-2.76 7.24,-2.76 10,0l2,-2C15.14,9.14 8.87,9.14 5,13z", str2, null, false, new Runnable() { // from class: dev.colorcc.Panel.Sheet.11
                        @Override // java.lang.Runnable
                        public final void run() {
                            Sheet.this.m214lambda$listWifi$39$devcolorccPanel$Sheet(wifiManager, str2);
                        }
                    }), (ScanResult) linkedHashMap.get(str2));
                    z2 = true;
                }
            }
            if (z2) {
                return;
            }
            item(null, Panel.this.word("searching", new Object[0]), null, false, null);
        }

        static /* synthetic */ int lambda$listWifi$36(ScanResult scanResult, ScanResult scanResult2) {
            return Integer.compare(scanResult2.level, scanResult.level);
        }

        /* JADX INFO: renamed from: lambda$listWifi$39$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m214lambda$listWifi$39$devcolorccPanel$Sheet(WifiManager wifiManager, String str) {
            m213lambda$listWifi$37$devcolorccPanel$Sheet(wifiManager, str, null);
        }

        private void mark(Row row, ScanResult scanResult) {
            String str;
            if (scanResult == null) {
                return;
            }
            String str2 = scanResult.capabilities == null ? "" : scanResult.capabilities;
            row.lock = str2.contains("WEP") || str2.contains("PSK") || str2.contains("SAE") || str2.contains("EAP");
            int wifiStandard = scanResult.getWifiStandard();
            if (wifiStandard == 8) {
                str = "7";
            } else if (wifiStandard == 6) {
                str = scanResult.frequency >= 5925 ? "6E" : "6";
            } else {
                str = null;
            }
            row.standard = str;
        }

        private String ssid(String str) {
            if (str == null || str.equals("<unknown ssid>")) {
                return "";
            }
            return (str.length() > 1 && str.startsWith("\"") && str.endsWith("\"")) ? str.substring(1, str.length() - 1) : str;
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* JADX INFO: renamed from: wifiTap, reason: merged with bridge method [inline-methods] */
        public void m213lambda$listWifi$37$devcolorccPanel$Sheet(WifiManager wifiManager, String str, Integer num) {
            Object obj = null;
            try {
                for (Object obj2 : (List) ReflectUtils.callMethod(ReflectUtils.getObjectField(ReflectUtils.getObjectField(this.tile, "accessPointController"), "mWifiPickerTracker"), "getWifiEntries", new Object[0])) {
                    if (str.equals(ReflectUtils.callMethod(obj2, "getSsid", new Object[0]))) {
                        obj = obj2;
                    }
                }
            } catch (Throwable th) {
                ColorCCBridge.log("wifi entries not read from SystemUI: " + th);
            }
            StringBuilder sb = new StringBuilder("wifi connect ");
            sb.append(obj != null ? "via SystemUI" : "direct");
            sb.append(", saved ");
            sb.append(num != null);
            ColorCCBridge.log(sb.toString());
            if (obj != null) {
                boolean z = ((Integer) ReflectUtils.callMethod(obj, "getSecurity", new Object[0])).intValue() == 0;
                if ((((Boolean) ReflectUtils.callMethod(obj, "isSaved", new Object[0])).booleanValue() || z) && !((Boolean) ReflectUtils.callMethod(obj, "shouldEditBeforeConnect", new Object[0])).booleanValue() && ((Boolean) ReflectUtils.callMethod(obj, "canConnect", new Object[0])).booleanValue()) {
                    ReflectUtils.callMethod(obj, "connect", new Object[]{null});
                    return;
                }
            }
            if (num == null || !(obj == null || ((Boolean) ReflectUtils.callMethod(obj, "isSaved", new Object[0])).booleanValue())) {
                openTileSettings();
            } else {
                connectWifi(wifiManager, num.intValue());
            }
        }

        private void connectWifi(WifiManager wifiManager, int r7) {
            try {
                WifiManager.class.getMethod("connect", Integer.TYPE, Class.forName("android.net.wifi.WifiManager$ActionListener")).invoke(wifiManager, Integer.valueOf(r7), null);
            } catch (Throwable th) {
                ColorCCBridge.log("wifi not connected: " + th);
            }
        }

        private void listSims() {
            final SubscriptionManager subscriptionManager = (SubscriptionManager) getContext().getSystemService(SubscriptionManager.class);
            List<SubscriptionInfo> activeSubscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
            if (activeSubscriptionInfoList == null || activeSubscriptionInfoList.isEmpty()) {
                item(null, Panel.this.word("no_sim", new Object[0]), null, false, null);
                return;
            }
            int defaultDataSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId();
            for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList) {
                final int subscriptionId = subscriptionInfo.getSubscriptionId();
                boolean z = subscriptionId == defaultDataSubscriptionId;
                String str = null;
                Runnable runnable = z ? null : new Runnable() { // from class: dev.colorcc.Panel.Sheet.29
                    @Override // java.lang.Runnable
                    public final void run() {
                        Sheet.this.m212lambda$listSims$41$devcolorccPanel$Sheet(subscriptionManager, subscriptionId);
                    }
                };
                String strValueOf = String.valueOf(subscriptionInfo.getDisplayName());
                if (z) {
                    str = this.usage;
                }
                Row rowItem = item(null, strValueOf, str, false, runnable);
                rowItem.badge = String.valueOf(subscriptionInfo.getSimSlotIndex() + 1);
                rowItem.radio = Boolean.valueOf(z);
            }
        }

        /* JADX INFO: renamed from: lambda$listSims$41$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m212lambda$listSims$41$devcolorccPanel$Sheet(final SubscriptionManager subscriptionManager, final int r3) {
            Panel.this.audioThread.execute(new Runnable() { // from class: dev.colorcc.Panel.Sheet.12
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.lambda$listSims$40(subscriptionManager, r3);
                }
            });
        }

        static /* synthetic */ void lambda$listSims$40(SubscriptionManager subscriptionManager, int r2) {
            try {
                ReflectUtils.callMethod(subscriptionManager, "setDefaultDataSubId", new Object[]{Integer.valueOf(r2)});
            } catch (Throwable th) {
                ColorCCBridge.log("data SIM not set: " + th);
            }
        }

        private String readUsage() {
            try {
                NetworkStatsManager networkStatsManager = (NetworkStatsManager) getContext().getSystemService(NetworkStatsManager.class);
                Calendar calendar = Calendar.getInstance();
                long timeInMillis = calendar.getTimeInMillis();
                calendar.set(11, 0);
                calendar.set(12, 0);
                calendar.set(13, 0);
                calendar.set(14, 0);
                long timeInMillis2 = calendar.getTimeInMillis();
                calendar.set(5, 1);
                return Panel.this.word("data_used", Formatter.formatShortFileSize(getContext(), used(networkStatsManager, timeInMillis2, timeInMillis)), Formatter.formatShortFileSize(getContext(), used(networkStatsManager, calendar.getTimeInMillis(), timeInMillis)));
            } catch (Throwable th) {
                ColorCCBridge.log("data usage not read: " + th);
                return null;
            }
        }

        private long used(NetworkStatsManager networkStatsManager, long j, long j2) throws Exception {
            NetworkStats.Bucket bucketQuerySummaryForDevice = networkStatsManager.querySummaryForDevice(0, null, j, j2);
            return bucketQuerySummaryForDevice.getRxBytes() + bucketQuerySummaryForDevice.getTxBytes();
        }

        private void listHotspot() {
            Sheet sheet = this;
            try {
                Object objCallMethod = ReflectUtils.callMethod(getContext().getSystemService(WifiManager.class), "getSoftApConfiguration", new Object[0]);
                item(null, String.valueOf(ReflectUtils.callMethod(objCallMethod, "getSsid", new Object[0])), Panel.this.word("name", new Object[0]), false, null);
                String strWord = Panel.this.keyguard.isKeyguardLocked() ? Panel.this.word("unlock_to_see", new Object[0]) : (String) ReflectUtils.callMethod(objCallMethod, "getPassphrase", new Object[0]);
                if (strWord != null) {
                    sheet.item(null, strWord, Panel.this.word("password", new Object[0]), false, null);
                }
            } catch (Throwable th) {
                ColorCCBridge.log("hotspot details not read: " + th);
            }
            int r15 = sheet.hotspotDevices();
            if (r15 >= 0) {
                Panel panel = Panel.this;
                sheet.item(null, r15 == 1 ? panel.word("device_connected", new Object[0]) : panel.word("devices_connected", Integer.valueOf(r15)), null, false, null);
            }
        }

        private int hotspotDevices() {
            String[] strArr = {"mHotspotController", "hotspotController", "mController"};
            for (int r2 = 0; r2 < 3; r2++) {
                try {
                    return ((Integer) ReflectUtils.callMethod(ReflectUtils.getObjectField(this.tile, strArr[r2]), "getNumConnectedDevices", new Object[0])).intValue();
                } catch (Throwable unused) {
                }
            }
            ColorCCBridge.log("hotspot devices not read");
            return -1;
        }

        private void listDnd() {
            int r2;
            final NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(NotificationManager.class);
            boolean z = ((Integer) ReflectUtils.callMethod(notificationManager, "getZenMode", new Object[0])).intValue() != 0;
            this.listScroll.setVisibility(z ? 0 : 8);
            this.note.setVisibility(z ? 8 : 0);
            long jDndEnd = z ? dndEnd(notificationManager) : 0L;
            this.sub.setVisibility(jDndEnd > 0 ? 0 : 8);
            if (jDndEnd > 0) {
                this.sub.setText(Panel.this.word("off_at", DateFormat.getTimeFormat(getContext()).format(new Date(jDndEnd))));
            }
            if (z) {
                if (jDndEnd <= 0) {
                    r2 = 0;
                } else {
                    r2 = this.dndHours;
                    if (r2 <= 0) {
                        r2 = -1;
                    }
                }
                int[] r4 = {0, 1, 2, 8};
                int r5 = 0;
                while (r5 < Panel.TILES_PER_ROW) {
                    final int r6 = r4[r5];
                    Panel panel = Panel.this;
                    Sheet sheet = this;
                    sheet.item(null, r6 == 0 ? panel.word("dnd_until_off", new Object[0]) : panel.word("hours", Integer.valueOf(r6)), null, false, new Runnable() { // from class: dev.colorcc.Panel.Sheet.6
                        @Override // java.lang.Runnable
                        public final void run() {
                            Sheet.this.m208lambda$listDnd$42$devcolorccPanel$Sheet(notificationManager, r6);
                        }
                    }).radio = Boolean.valueOf(r6 == r2);
                    r5++;
                }
            }
        }

        private long dndEnd(NotificationManager notificationManager) {
            try {
                Object objectField = ReflectUtils.getObjectField(ReflectUtils.callMethod(notificationManager, "getZenModeConfig", new Object[0]), "manualRule");
                Object objectField2 = objectField == null ? null : ReflectUtils.getObjectField(objectField, "conditionId");
                if (objectField2 == null) {
                    return 0L;
                }
                return ((Long) ReflectUtils.callStaticMethod(ReflectUtils.findClass("android.service.notification.ZenModeConfig", (ClassLoader) null), "tryParseCountdownConditionId", new Object[]{objectField2})).longValue();
            } catch (Throwable th) {
                ColorCCBridge.log("do not disturb end not read: " + th);
                return 0L;
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* JADX INFO: renamed from: setDnd, reason: merged with bridge method [inline-methods] */
        public void m208lambda$listDnd$42$devcolorccPanel$Sheet(NotificationManager notificationManager, int r8) {
            this.dndHours = r8;
            ReflectUtils.callMethod(notificationManager, "setZenMode", new Class[]{Integer.TYPE, Uri.class, String.class}, new Object[]{1, r8 != 0 ? ReflectUtils.callStaticMethod(ReflectUtils.findClass("android.service.notification.ZenModeConfig", (ClassLoader) null), "toCountdownConditionId", new Object[]{Long.valueOf(System.currentTimeMillis() + (((long) r8) * 3600000))}) : null, "ColorCC"});
        }

        private void addRow(final Row row, boolean z) {
            View view;
            Context context = getContext();
            LinearLayout linearLayoutHorizontal = Panel.horizontal(context);
            linearLayoutHorizontal.setGravity(16);
            if (row.header) {
                TextView textViewLabel = Panel.this.label(context, 13.0f, -1711276033, 1);
                textViewLabel.setText(row.name);
                linearLayoutHorizontal.addView(textViewLabel, new LinearLayout.LayoutParams(0, -2, 1.0f));
                if (row.side != null) {
                    TextView textViewLabel2 = Panel.this.label(context, 13.0f, -1, 1);
                    textViewLabel2.setText(row.side);
                    textViewLabel2.setPadding(Panel.this.dp(Panel.DROP_STEP), Panel.this.dp(8.0f), 0, Panel.this.dp(8.0f));
                    textViewLabel2.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Sheet.27
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view2) {
                            Sheet.this.m202lambda$addRow$43$devcolorccPanel$Sheet(row, view2);
                        }
                    });
                    linearLayoutHorizontal.addView(textViewLabel2);
                }
                this.list.addView(linearLayoutHorizontal, new LinearLayout.LayoutParams(-1, Panel.this.dp(40.0f)));
                return;
            }
            boolean z2 = (row.icon == null && row.badge == null) ? false : true;
            if (row.badge != null) {
                TextView textViewLabel3 = Panel.this.label(context, Panel.DROP_STEP, -1, 1);
                textViewLabel3.setText(row.badge);
                textViewLabel3.setGravity(17);
                textViewLabel3.setTypeface(Typeface.DEFAULT_BOLD);
                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setColor(-14503604);
                gradientDrawable.setCornerRadius(Panel.this.dp(4.0f));
                textViewLabel3.setBackground(gradientDrawable);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Panel.this.dp(20.0f), Panel.this.dp(26.0f));
                layoutParams.leftMargin = Panel.this.dp(2.0f);
                layoutParams.rightMargin = Panel.this.dp(18.0f);
                linearLayoutHorizontal.addView(textViewLabel3, layoutParams);
            } else if (row.icon != null) {
                int r5 = row.on ? Panel.this.theme(Panel.ACTIVE_BLUE_LIGHT) : -1;
                ImageView imageView = new ImageView(context);
                imageView.setImageDrawable(Icons.of(row.icon));
                imageView.setImageTintList(ColorStateList.valueOf(r5));
                LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(Panel.this.dp(24.0f), Panel.this.dp(24.0f));
                layoutParams2.rightMargin = Panel.this.dp(Panel.DROP_HEADER);
                if (row.lock || row.standard != null) {
                    view = imageView;
                    FrameLayout frameLayout = new FrameLayout(context);
                    frameLayout.setClipChildren(false);
                    frameLayout.addView(imageView, new FrameLayout.LayoutParams(-1, -1));
                    if (row.lock) {
                        ImageView imageView2 = new ImageView(context);
                        imageView2.setImageDrawable(Icons.of("M18,8h-1V6c0,-2.76 -2.24,-5 -5,-5S7,3.24 7,6v2H6c-1.1,0 -2,0.9 -2,2v10c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V10c0,-1.1 -0.9,-2 -2,-2zM9,6c0,-1.66 1.34,-3 3,-3s3,1.34 3,3v2H9V6z"));
                        imageView2.setImageTintList(ColorStateList.valueOf(r5));
                        FrameLayout.LayoutParams layoutParams3 = new FrameLayout.LayoutParams(Panel.this.dp(10.0f), Panel.this.dp(10.0f), 8388693);
                        layoutParams3.rightMargin = -Panel.this.dp(3.0f);
                        layoutParams3.bottomMargin = -Panel.this.dp(1.0f);
                        frameLayout.addView(imageView2, layoutParams3);
                    }
                    if (row.standard != null) {
                        TextView textViewLabel4 = Panel.this.label(context, 9.0f, r5, 1);
                        textViewLabel4.setText(row.standard);
                        textViewLabel4.setTypeface(Typeface.DEFAULT_BOLD);
                        textViewLabel4.setIncludeFontPadding(false);
                        FrameLayout.LayoutParams layoutParams4 = new FrameLayout.LayoutParams(-2, -2, 8388661);
                        layoutParams4.rightMargin = -Panel.this.dp(6.0f);
                        layoutParams4.topMargin = -Panel.this.dp(3.0f);
                        frameLayout.addView(textViewLabel4, layoutParams4);
                    }
                    view = frameLayout;
                } else {
                    view = imageView;
                }
                linearLayoutHorizontal.addView(view, layoutParams2);
            }
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(1);
            TextView textViewLabel5 = Panel.this.label(context, Panel.DROP_HEADER, -1, 1);
            textViewLabel5.setText(row.name);
            linearLayout.addView(textViewLabel5);
            if (row.status != null) {
                TextView textViewLabel6 = Panel.this.label(context, 13.0f, -1711276033, 2);
                textViewLabel6.setText(row.status);
                linearLayout.addView(textViewLabel6);
            }
            linearLayoutHorizontal.addView(linearLayout, new LinearLayout.LayoutParams(0, -2, 1.0f));
            if (row.radio != null) {
                LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(Panel.this.dp(Panel.ARC), Panel.this.dp(Panel.ARC));
                layoutParams5.leftMargin = Panel.this.dp(Panel.DROP_STEP);
                linearLayoutHorizontal.addView(radio(context, row.radio.booleanValue()), layoutParams5);
            }
            if (row.action != null) {
                GradientDrawable gradientDrawable2 = new GradientDrawable();
                gradientDrawable2.setColor(-1);
                gradientDrawable2.setCornerRadius(Panel.this.dp(Panel.DROP_STEP));
                linearLayoutHorizontal.setBackground(new RippleDrawable(ColorStateList.valueOf(872415231), null, gradientDrawable2));
                linearLayoutHorizontal.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Sheet.28
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view2) {
                        Sheet.this.m204lambda$addRow$45$devcolorccPanel$Sheet(row, view2);
                    }
                });
            }
            this.list.addView(linearLayoutHorizontal, new LinearLayout.LayoutParams(-1, Panel.this.dp(row.status != null ? 62.0f : 50.0f)));
            if (z) {
                View view2 = new View(context);
                view2.setBackgroundColor(Panel.SHEET_GLASS);
                LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(-1, 1);
                layoutParams6.leftMargin = z2 ? Panel.this.dp(40.0f) : 0;
                this.list.addView(view2, layoutParams6);
            }
        }

        /* JADX INFO: renamed from: lambda$addRow$43$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m202lambda$addRow$43$devcolorccPanel$Sheet(Row row, View view) {
            Panel.this.guard(row.sideAction);
        }

        /* JADX INFO: renamed from: lambda$addRow$45$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m204lambda$addRow$45$devcolorccPanel$Sheet(Row row, View view) {
            ColorCCBridge.log("menu row tapped: " + row.name);
            Panel.this.guard(row.action);
            Panel.this.main.postDelayed(new Runnable() { // from class: dev.colorcc.Panel.Sheet.51
                @Override // java.lang.Runnable
                public final void run() {
                    Sheet.this.m203lambda$addRow$44$devcolorccPanel$Sheet();
                }
            }, 1200L);
        }

        /* JADX INFO: renamed from: lambda$addRow$44$dev-colorcc-Panel$Sheet, reason: not valid java name */
        /* synthetic */ void m203lambda$addRow$44$devcolorccPanel$Sheet() {
            Panel.this.guard(new AnonymousClass0());
        }

        private View radio(Context context, boolean z) {
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(1);
            ImageView imageView = new ImageView(context);
            Panel panel = Panel.this;
            if (z) {
                gradientDrawable.setColor(panel.theme(Panel.ACTIVE_BLUE));
                GradientDrawable gradientDrawable2 = new GradientDrawable();
                gradientDrawable2.setShape(1);
                gradientDrawable2.setColor(-1);
                imageView.setImageDrawable(gradientDrawable2);
                imageView.setPadding(Panel.this.dp(Panel.TOGGLE_KICK), Panel.this.dp(Panel.TOGGLE_KICK), Panel.this.dp(Panel.TOGGLE_KICK), Panel.this.dp(Panel.TOGGLE_KICK));
            } else {
                gradientDrawable.setStroke(panel.dp(1.5f), -2130706433);
            }
            imageView.setBackground(gradientDrawable);
            return imageView;
        }

        private boolean findTorch() {
            try {
                CameraManager cameraManager = (CameraManager) getContext().getSystemService(CameraManager.class);
                for (String str : cameraManager.getCameraIdList()) {
                    CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(str);
                    Integer num = (Integer) cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                    if (Boolean.TRUE.equals(cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) && num != null && num.intValue() == 1) {
                        Integer num2 = (Integer) cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL);
                        this.torchCamera = str;
                        int r2 = (num2 == null || num2.intValue() < 2) ? 1 : num2.intValue();
                        this.torchMax = r2;
                        this.torchLevel = -1;
                        this.torchStrength = r2 < 2 ? 1.0f : cameraManager.getTorchStrengthLevel(str) / this.torchMax;
                        return true;
                    }
                }
            } catch (Throwable th) {
                ColorCCBridge.log("torch levels missing: " + th);
            }
            this.torchCamera = null;
            return false;
        }

        private void onTorch(float f) {
            int r5 = f < 0.03f ? 0 : Math.max(1, Math.round(f * this.torchMax));
            if (r5 == this.torchLevel || this.torchCamera == null) {
                return;
            }
            this.torchLevel = r5;
            if (r5 > 0) {
                this.torchStrength = r5 / this.torchMax;
            }
            try {
                CameraManager cameraManager = (CameraManager) getContext().getSystemService(CameraManager.class);
                if (r5 <= 0) {
                    cameraManager.setTorchMode(this.torchCamera, false);
                    return;
                }
                int r1 = this.torchMax;
                String str = this.torchCamera;
                if (r1 < 2) {
                    cameraManager.setTorchMode(str, true);
                } else {
                    cameraManager.turnOnTorchWithStrengthLevel(str, r5);
                }
            } catch (Exception e) {
                ColorCCBridge.log("torch not set: " + e);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void openTileSettings() {
            if (this.outputOpen) {
                Panel.this.samsungOutput(this.outputPackage);
                return;
            }
            if (this.volumesOpen) {
                Panel.this.openSettings("android.settings.SOUND_SETTINGS");
                return;
            }
            if (this.brightnessOpen) {
                Panel.this.openSettings("android.settings.DISPLAY_SETTINGS");
                return;
            }
            Object obj = this.tile;
            Object objCallMethod = obj == null ? null : ReflectUtils.callMethod(obj, "getLongClickIntent", new Object[0]);
            if (objCallMethod instanceof Intent) {
                Panel.this.startSettings(new Intent((Intent) objCallMethod));
                return;
            }
            String str = new String[]{null, "android.settings.WIFI_SETTINGS", "android.settings.BLUETOOTH_SETTINGS", "android.settings.DATA_USAGE_SETTINGS", "android.settings.WIRELESS_SETTINGS", "android.settings.ZEN_MODE_PRIORITY_SETTINGS", null}[this.menu];
            if (str != null) {
                Panel.this.openSettings(str);
            }
        }
    }

    private static final class Row {
        Runnable action;
        String badge;
        boolean header;
        String icon;
        boolean lock;
        String name;
        boolean on;
        Boolean radio;
        String side;
        Runnable sideAction;
        String standard;
        String status;

        private Row() {
        }

        public String toString() {
            return this.header + "|" + this.icon + "|" + this.lock + "|" + this.standard + "|" + this.badge + "|" + this.name + "|" + this.status + "|" + this.on + "|" + this.radio + "|" + this.side;
        }
    }

    private final class TorchBeam extends View {
        private static final float BEAM_EDGE = 1.15f;
        private static final float BOX = 415.0f;
        private final Bitmap beamSprite;
        private final DashPathEffect dots;
        private float downLevel;
        private float downY;
        private final Paint glow;
        private boolean held;
        private final Paint led;
        private final Motion.Value level;
        private final Paint light;
        private final Paint line;
        private final Consumer<Float> listener;
        private final Paint metal;
        private boolean moved;
        private final RectF oval;
        private final Path path;
        private final Bitmap sprite;
        private final Path torch;

        TorchBeam(Context context, Consumer<Float> consumer) {
            super(context);
            Paint paint = new Paint(1);
            this.line = paint;
            Paint paint2 = new Paint(1);
            this.glow = paint2;
            this.light = new Paint(2);
            Paint paint3 = new Paint(1);
            this.metal = paint3;
            Paint paint4 = new Paint(1);
            this.led = paint4;
            this.path = new Path();
            Path path = new Path();
            this.torch = path;
            this.sprite = glowSprite();
            this.beamSprite = beamSprite();
            this.oval = new RectF();
            this.listener = consumer;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(-1);
            this.dots = new DashPathEffect(new float[]{Panel.STATUS_GROW, Panel.this.dp(Panel.TOGGLE_KICK)}, 0.0f);
            paint3.setColor(-855310);
            paint4.setColor(-38357);
            path.setFillType(Path.FillType.EVEN_ODD);
            path.moveTo(-18.5f, 11.5f);
            path.quadTo(-18.5f, 9.5f, -16.5f, 9.5f);
            path.lineTo(16.5f, 9.5f);
            path.quadTo(18.5f, 9.5f, 18.5f, 11.5f);
            path.cubicTo(18.5f, 17.5f, Panel.DROP_STEP, 21.0f, 10.3f, 26.0f);
            path.lineTo(10.3f, 66.0f);
            path.quadTo(10.3f, 70.0f, 6.3f, 70.0f);
            path.lineTo(-6.3f, 70.0f);
            path.quadTo(-10.3f, 70.0f, -10.3f, 66.0f);
            path.lineTo(-10.3f, 26.0f);
            path.cubicTo(-12.0f, 21.0f, -18.5f, 17.5f, -18.5f, 11.5f);
            path.close();
            path.addRoundRect(-5.85f, 26.5f, 5.85f, 49.0f, 5.85f, 5.85f, Path.Direction.CW);
            paint2.setFilterBitmap(true);
            this.level = Panel.this.motion.value(Logic.Spring.SNAPPY, new Motion.Listener() { // from class: dev.colorcc.Panel.TorchBeam.0
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    TorchBeam.this.m243lambda$new$0$devcolorccPanel$TorchBeam(f, f2);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$0$dev-colorcc-Panel$TorchBeam, reason: not valid java name */
        /* synthetic */ void m243lambda$new$0$devcolorccPanel$TorchBeam(float f, float f2) {
            invalidate();
        }

        boolean held() {
            return this.held;
        }

        void set(float f, boolean z) {
            Motion.Value value = this.level;
            if (z) {
                value.to(f);
            } else {
                value.snap(f);
            }
        }

        private float base() {
            return getHeight() - Panel.this.dp(100.0f);
        }

        private float reach() {
            return base() - (getHeight() * 0.2f);
        }

        @Override // android.view.View
        public boolean onTouchEvent(MotionEvent motionEvent) {
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 0) {
                this.held = true;
                this.moved = false;
                this.downY = motionEvent.getY();
                this.downLevel = Logic.clamp(this.level.value());
                getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            }
            if (actionMasked == 1) {
                if (!this.moved) {
                    float fMax = this.level.value() > 0.03f ? 0.0f : Math.max(Panel.STATUS_GROW, Panel.this.sheet.torchStrength);
                    this.level.to(fMax);
                    this.listener.accept(Float.valueOf(fMax));
                }
                this.held = false;
                return true;
            }
            if (actionMasked != 2) {
                if (actionMasked != 3) {
                    return true;
                }
                this.held = false;
                return true;
            }
            if (!this.moved && Math.abs(motionEvent.getY() - this.downY) < ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                return true;
            }
            this.moved = true;
            float fClamp = Logic.clamp(this.downLevel + ((this.downY - motionEvent.getY()) / reach()));
            this.level.snap(fClamp);
            this.listener.accept(Float.valueOf(fClamp));
            return true;
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            float f;
            float f2;
            float width = getWidth();
            float f3 = width / BOX;
            float f4 = width / 2.0f;
            float fBase = base();
            float fReach = reach();
            float f5 = width * 0.37f;
            this.line.setPathEffect(this.dots);
            this.line.setStrokeWidth(Panel.this.dp(2.0f));
            this.line.setAlpha(110);
            curve(f4, fBase - fReach, f5, 0.19f * f5);
            canvas.drawPath(this.path, this.line);
            float fClamp = Logic.clamp(this.level.value());
            float f6 = 9.0f * f3;
            float f7 = (fBase - f6) - ((fReach - f6) * fClamp);
            float f8 = 30.0f * f3;
            float f9 = f8 + ((f5 - f8) * fClamp);
            if (fClamp > 0.01f) {
                float f10 = 0.87f * f9 * BEAM_EDGE;
                this.oval.set(f4 - f10, f7 - (4.0f * f3), f10 + f4, (2.0f * f3) + fBase);
                canvas.drawBitmap(this.beamSprite, (Rect) null, this.oval, this.light);
                f = f4;
                f2 = fBase;
                glowAt(canvas, f, f2, 14.0f * f3, f3 * 8.0f, -1);
            } else {
                f = f4;
                f2 = fBase;
            }
            this.line.setPathEffect(null);
            this.line.setStrokeWidth(6.0f * f3);
            this.line.setAlpha(255);
            curve(f, f7, f9, 0.18f * f9 * fClamp);
            canvas.drawPath(this.path, this.line);
            int r9 = canvas.save();
            canvas.translate(f, f2);
            canvas.scale(f3, f3);
            canvas.drawRoundRect(-18.5f, 0.0f, 18.5f, 7.5f, 3.0f, 3.0f, this.metal);
            canvas.drawPath(this.torch, this.metal);
            float fClamp2 = 33.3f - (Logic.clamp(fClamp * 8.0f) * 4.6f);
            canvas.drawRoundRect(-3.0f, fClamp2, 3.0f, fClamp2 + Panel.DROP_STEP, 3.0f, 3.0f, this.metal);
            canvas.drawCircle(0.0f, fClamp2 + 4.6f, 1.3f, this.led);
            canvas.restoreToCount(r9);
        }

        private void glowAt(Canvas canvas, float f, float f2, float f3, float f4, int r10) {
            if (f3 < 1.0f || f4 < 1.0f) {
                return;
            }
            this.glow.setColorFilter(new PorterDuffColorFilter((-16777216) | r10, PorterDuff.Mode.SRC_IN));
            this.glow.setAlpha(r10 >>> 24);
            this.oval.set(f - f3, f2 - f4, f + f3, f2 + f4);
            canvas.drawBitmap(this.sprite, (Rect) null, this.oval, this.glow);
        }

        private Bitmap glowSprite() {
            Bitmap bitmapCreateBitmap = Bitmap.createBitmap(Panel.MENU_ROOM, Panel.MENU_ROOM, Bitmap.Config.ARGB_8888);
            Paint paint = new Paint(1);
            paint.setShader(new RadialGradient(48.0f, 48.0f, 48.0f, new int[]{-1, -2130706433, 16777215}, new float[]{0.0f, 0.55f, 1.0f}, Shader.TileMode.CLAMP));
            new Canvas(bitmapCreateBitmap).drawRect(0.0f, 0.0f, 96.0f, 96.0f, paint);
            return bitmapCreateBitmap;
        }

        private Bitmap beamSprite() {
            int[] r8 = new int[32000];
            for (int r1 = 0; r1 < 200; r1++) {
                for (int r3 = 0; r3 < 160; r3++) {
                    r8[((199 - r1) * 160) + r3] = Logic.beam(((((r3 + 0.5f) / 160.0f) * 2.0f) - 1.0f) * BEAM_EDGE, (r1 + 0.5f) / 200.0f);
                }
            }
            return Bitmap.createBitmap(r8, 160, 200, Bitmap.Config.ARGB_8888);
        }

        private void curve(float f, float f2, float f3, float f4) {
            this.path.reset();
            float f5 = f2 + f4;
            this.path.moveTo(f - f3, f5);
            this.path.quadTo(f, f2 - f4, f3 + f, f5);
        }
    }

    private final class Rewind implements ValueAnimator.AnimatorUpdateListener {
        private final Motion.Listener out;
        private float reach = 1.0f;
        private ValueAnimator run;
        private final Motion.Value show;

        Rewind(Motion.Value value, Motion.Listener listener) {
            this.show = value;
            this.out = listener;
        }

        void start() {
            stop();
            float[] fArr = Logic.Spring.MENU;
            float fValue = this.show.value();
            float fSpringPeak = Logic.springPeak(fArr);
            float fSpringAt = Logic.springAt(fArr, fSpringPeak);
            boolean z = this.show.running() && this.show.velocity() > 0.0f && fValue < fSpringAt;
            if (z) {
                fSpringPeak = Logic.springTimeOf(fArr, fValue);
            }
            this.reach = z ? 1.0f : fValue / fSpringAt;
            this.show.snap(fValue);
            ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(fSpringPeak, 0.0f);
            this.run = valueAnimatorOfFloat;
            valueAnimatorOfFloat.setDuration(Math.round((fSpringPeak * 1000.0f) / Panel.CLOSE_PACE));
            this.run.setInterpolator(new LinearInterpolator());
            this.run.addUpdateListener(this);
            this.run.start();
        }

        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            final float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            final float fSpringAt = this.reach * Logic.springAt(Logic.Spring.MENU, fFloatValue);
            this.show.track(fSpringAt);
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Rewind.0
                @Override // java.lang.Runnable
                public final void run() {
                    Rewind.this.m193lambda$onAnimationUpdate$0$devcolorccPanel$Rewind(fSpringAt, fFloatValue);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$onAnimationUpdate$0$dev-colorcc-Panel$Rewind, reason: not valid java name */
        /* synthetic */ void m193lambda$onAnimationUpdate$0$devcolorccPanel$Rewind(float f, float f2) {
            this.out.onValue(f, (-this.reach) * Logic.springSpeed(Logic.Spring.MENU, f2));
        }

        void stop() {
            ValueAnimator valueAnimator = this.run;
            if (valueAnimator == null) {
                return;
            }
            valueAnimator.removeAllUpdateListeners();
            this.run.cancel();
            this.run = null;
        }
    }

    private final class Toggle extends View {
        private final Motion.Value on;
        private final Paint paint;

        Toggle(Context context) {
            super(context);
            this.paint = new Paint(1);
            setClickable(true);
            this.on = Panel.this.motion.value(Logic.Spring.SNAPPY, new Motion.Listener() { // from class: dev.colorcc.Panel.Toggle.0
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Toggle.this.m242lambda$new$0$devcolorccPanel$Toggle(f, f2);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$0$dev-colorcc-Panel$Toggle, reason: not valid java name */
        /* synthetic */ void m242lambda$new$0$devcolorccPanel$Toggle(float f, float f2) {
            invalidate();
        }

        void set(boolean z, boolean z2) {
            Motion.Value value = this.on;
            if (z2) {
                value.to(z ? 1.0f : 0.0f);
            } else {
                value.snap(z ? 1.0f : 0.0f);
            }
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            float width = getWidth();
            float height = getHeight();
            this.paint.setColor(((Integer) Panel.this.argb.evaluate(Logic.clamp(this.on.value()), 1090519039, Integer.valueOf(Panel.this.theme(Panel.ACTIVE_BLUE)))).intValue());
            float f = height / 2.0f;
            canvas.drawRoundRect(0.0f, 0.0f, width, height, f, f, this.paint);
            float f2 = height * 0.12f;
            float f3 = f - f2;
            this.paint.setColor(-1);
            canvas.drawCircle(f2 + f3 + (this.on.value() * ((width - (f2 * 2.0f)) - (2.0f * f3))), f, f3, this.paint);
        }
    }

    private final class Bound {
        final Consumer<Object> apply;
        final Object callback;
        Object held;
        final Object tile;

        Bound(Object obj, Consumer<Object> consumer) throws ClassNotFoundException {
            this.tile = obj;
            this.apply = consumer;
            ClassLoader classLoader = obj.getClass().getClassLoader();
            Object objNewProxyInstance = Proxy.newProxyInstance(classLoader, new Class[]{Class.forName("com.android.systemui.plugins.qs.QSTile$Callback", false, classLoader)}, new InvocationHandler() { // from class: dev.colorcc.Panel.Bound.0
                @Override // java.lang.reflect.InvocationHandler
                public final Object invoke(Object obj2, Method method, Object[] objArr) throws Throwable {
                    return Bound.this.m104lambda$new$2$devcolorccPanel$Bound(obj2, method, objArr);
                }
            });
            this.callback = objNewProxyInstance;
            ReflectUtils.callMethod(obj, "addCallback", new Object[]{objNewProxyInstance});
            Object objCallMethod = ReflectUtils.callMethod(obj, "getState", new Object[0]);
            if (objCallMethod != null) {
                consumer.accept(objCallMethod);
            }
        }

        /* JADX INFO: renamed from: lambda$new$2$dev-colorcc-Panel$Bound, reason: not valid java name */
        /* synthetic */ Object m104lambda$new$2$devcolorccPanel$Bound(Object obj, Method method, final Object[] objArr) throws Throwable {
            String name = method.getName();
            name.hashCode();
            switch (name) {
                case "toString":
                    return "ColorCC.Callback";
                case "onStateChanged":
                    Panel.this.main.post(new Runnable() { // from class: dev.colorcc.Panel.Bound.1
                        @Override // java.lang.Runnable
                        public final void run() {
                            Bound.this.m103lambda$new$1$devcolorccPanel$Bound(objArr);
                        }
                    });
                    return null;
                case "equals":
                    return Boolean.valueOf(obj == objArr[0]);
                case "hashCode":
                    return Integer.valueOf(System.identityHashCode(obj));
                default:
                    return null;
            }
        }

        /* JADX INFO: renamed from: lambda$new$0$dev-colorcc-Panel$Bound, reason: not valid java name */
        /* synthetic */ void m102lambda$new$0$devcolorccPanel$Bound(Object[] objArr) {
            deliver(objArr[0]);
        }

        /* JADX INFO: renamed from: lambda$new$1$dev-colorcc-Panel$Bound, reason: not valid java name */
        /* synthetic */ void m103lambda$new$1$devcolorccPanel$Bound(final Object[] objArr) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Bound.2
                @Override // java.lang.Runnable
                public final void run() {
                    Bound.this.m102lambda$new$0$devcolorccPanel$Bound(objArr);
                }
            });
        }

        void deliver(Object obj) {
            if (!Panel.this.open || Panel.this.closing) {
                this.held = obj;
                Panel.access$8808(Panel.this);
            } else {
                this.held = null;
                this.apply.accept(obj);
            }
        }

        void release() {
            Object obj = this.held;
            this.held = null;
            if (obj != null) {
                this.apply.accept(obj);
            }
        }
    }

    private void releaseHeld() {
        for (final Bound bound : this.bound) {
            Objects.requireNonNull(bound);
            guard(new Runnable() { // from class: dev.colorcc.Panel.69
                @Override // java.lang.Runnable
                public final void run() {
                    bound.release();
                }
            });
        }
        if (this.sheet.watch != null) {
            final Bound bound2 = this.sheet.watch;
            Objects.requireNonNull(bound2);
            guard(new Runnable() { // from class: dev.colorcc.Panel.69
                @Override // java.lang.Runnable
                public final void run() {
                    bound2.release();
                }
            });
        }
        if (this.heldStates > 0) {
            ColorCCBridge.log("tile reports held during the close: " + this.heldStates);
        }
        this.heldStates = 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static List<Object> hostTiles() {
        if (ColorCCBridge.host == null) return new ArrayList();
        ArrayList arrayList = new ArrayList();
        for (Object t : ColorCCBridge.host.getTiles()) {
            String sp = ((com.android.systemui.plugins.qs.QSTile) t).getTileSpec();
            if ("internet".equalsIgnoreCase(sp)) {
                try {
                    com.android.systemui.plugins.qs.QSTile wifiTile = ColorCCBridge.host.createTile("wifi");
                    if (wifiTile != null) {
                        arrayList.add(wifiTile);
                        continue;
                    }
                } catch (Throwable ignored) {
                }
            }
            arrayList.add(t);
        }
        String[] requiredCards = {"wifi", "bt", "cell"};
        for (String req : requiredCards) {
            boolean found = false;
            for (Object t : arrayList) {
                String sp = ((com.android.systemui.plugins.qs.QSTile) t).getTileSpec();
                if (Logic.kind(sp) == Logic.kind(req)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                try {
                    com.android.systemui.plugins.qs.QSTile created = ColorCCBridge.host.createTile(req);
                    if (created != null) {
                        arrayList.add(created);
                    }
                } catch (Throwable ignored) {
                }
            }
        }
        if (ColorCCBridge.oneUi8) {
            String[] strArr = {"Wifi", "Bluetooth"};
            for (int r3 = 0; r3 < 2; r3++) {
                String str = strArr[r3];
                Iterator it = arrayList.iterator();
                boolean z = false;
                while (it.hasNext()) {
                    z |= Logic.kind((String) ReflectUtils.callMethod(it.next(), "getTileSpec", new Object[0])) == Logic.kind(str);
                }
                if (!z) {
                    try {
                        Object objCallMethod = ReflectUtils.callMethod(ReflectUtils.getObjectField(ColorCCBridge.host, "interactor"), "getTileBySpecString", new Object[]{str});
                        if (objCallMethod != null) {
                            arrayList.add(objCallMethod);
                        } else {
                            ColorCCBridge.log("no top bar tile " + str + " on One UI 8");
                        }
                    } catch (Throwable th) {
                        ColorCCBridge.log("top bar tile " + str + " not read: " + th);
                    }
                }
            }
        }
        return arrayList;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean syncTiles() {
        ArrayList arrayList = new ArrayList();
        for (Object obj : hostTiles()) {
            if (((Boolean) ReflectUtils.callMethod(obj, "isAvailable", new Object[0])).booleanValue()) {
                arrayList.add(obj);
            }
        }
        int r1 = this.tileRows;
        if (arrayList.equals(this.currentTiles) && r1 == this.pageRows) {
            return false;
        }
        unbindTiles();
        this.currentTiles = arrayList;
        this.pageRows = r1;
        this.wifiCard.setVisibility(TILES_PER_ROW);
        this.bluetoothCard.setVisibility(TILES_PER_ROW);
        this.simCard.setVisibility(TILES_PER_ROW);
        ArrayList arrayList2 = new ArrayList();
        this.pinViews.clear();
        for (Object obj2 : arrayList) {
            String str = (String) ReflectUtils.callMethod(obj2, "getTileSpec", new Object[0]);
            int r8 = Logic.kind(str);
            if (r8 == 1) {
                this.wifiCard.bind(obj2);
            } else if (r8 == 2) {
                this.bluetoothCard.bind(obj2);
            } else if (r8 == 3) {
                this.simCard.bind(obj2);
            } else if (this.pins.containsKey(str)) {
                this.pinViews.put(str, pinView(obj2));
            } else {
                arrayList2.add(obj2);
            }
        }
        int r0 = Math.min(r1, Logic.pages(arrayList2.size(), TILES_PER_ROW));
        this.gridRowCount = r0;
        this.pager.setTiles(arrayList2, r0 * TILES_PER_ROW);
        arrange();
        this.blocks.arrange(this.layout);
        return true;
    }

    private View pinView(final Object obj) {
        int[] r0 = Logic.pinSize(this.pins.get((String) ReflectUtils.callMethod(obj, "getTileSpec", new Object[0])));
        if (r0[0] > 1 || r0[1] > 1) {
            Card card = new Card(getContext());
            Body body = new Body(card, this.tileBase);
            body.pressable();
            this.tileBodies.add(body);
            card.body = body;
            card.bind(obj);
            return card;
        }
        final Disc disc = new Disc(getContext(), dp(26.0f));
        guard(new Runnable() { // from class: dev.colorcc.Panel.125
            @Override // java.lang.Runnable
            public final void run() {
                Panel.lambda$pinView$74(disc, obj);
            }
        });
        wireTile(disc, obj);
        final Body body2 = new Body(disc, this.tileBase);
        body2.pressable();
        this.tileBodies.add(body2);
        final int[] r2 = {-1};
        bindTile(obj, new Consumer() { // from class: dev.colorcc.Panel.126
            @Override // java.util.function.Consumer
            public final void accept(Object obj2) {
                Panel.this.m72lambda$pinView$75$devcolorccPanel(r2, disc, body2, obj2);
            }
        });
        return disc;
    }

    static /* synthetic */ void lambda$pinView$74(Disc disc, Object obj) {
        disc.style((String) ReflectUtils.callMethod(obj, "getTileSpec", new Object[0]));
    }

    /* JADX INFO: renamed from: lambda$pinView$75$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m72lambda$pinView$75$devcolorccPanel(int[] r5, Disc disc, Body body, Object obj) {
        int r0 = stateOf(obj);
        int r2 = r5[0];
        boolean z = (r2 == -1 || r2 == r0 || !this.interactive) ? false : true;
        r5[0] = r0;
        disc.render(obj, z);
        if (z && !disc.ownAnimation) {
            body.press.kick(TOGGLE_KICK);
        }
        disc.setContentDescription(text(obj, "label"));
        disc.setStateDescription(word(r0 == 2 ? "on" : "off", new Object[0]));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void unbindTiles() {
        for (final Bound bound : this.bound) {
            guard(new Runnable() { // from class: dev.colorcc.Panel.22
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.lambda$unbindTiles$76(bound);
                }
            });
            if (this.open) {
                guard(new Runnable() { // from class: dev.colorcc.Panel.23
                    @Override // java.lang.Runnable
                    public final void run() {
                        Panel.this.m86lambda$unbindTiles$77$devcolorccPanel(bound);
                    }
                });
            }
        }
        this.bound.clear();
        this.tileBodies.clear();
        this.lateIcons.clear();
    }

    static /* synthetic */ void lambda$unbindTiles$76(Bound bound) {
        ReflectUtils.callMethod(bound.tile, "removeCallback", new Object[]{bound.callback});
    }

    /* JADX INFO: renamed from: lambda$unbindTiles$77$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m86lambda$unbindTiles$77$devcolorccPanel(Bound bound) {
        ReflectUtils.callMethod(bound.tile, "setListening", new Object[]{this, false});
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void bindTile(Object obj, Consumer<Object> consumer) {
        try {
            this.bound.add(new Bound(obj, consumer));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void toggleTileDirect(Object obj) {
        if (obj == null) {
            return;
        }
        try {
            String spec = String.valueOf(ReflectUtils.callMethod(obj, "getTileSpec", new Object[0]));
            int kind = Logic.kind(spec);
            if (kind == 1) {
                WifiManager wm = (WifiManager) getContext().getSystemService(WifiManager.class);
                if (wm != null) {
                    wm.setWifiEnabled(!wm.isWifiEnabled());
                    return;
                }
            } else if (kind == 2) {
                BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
                if (bt != null) {
                    if (bt.isEnabled()) {
                        bt.disable();
                    } else {
                        bt.enable();
                    }
                    return;
                }
            } else if (kind == 3 || "dnd".equalsIgnoreCase(spec) || "modes".equalsIgnoreCase(spec) || "saver".equalsIgnoreCase(spec)) {
                ReflectUtils.callMethod(obj, "secondaryClick", new Object[]{null});
                return;
            }
        } catch (Throwable ignored) {
        }
        ReflectUtils.callMethod(obj, "click", new Object[]{null});
    }

    static /* synthetic */ void lambda$wireTile$78(Panel panel, Object obj) {
        panel.toggleTileDirect(obj);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void wireTile(View view, final Object obj) {
        view.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.53
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                Panel.this.m90lambda$wireTile$79$devcolorccPanel(obj, view2);
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() { // from class: dev.colorcc.Panel.54
            @Override // android.view.View.OnLongClickListener
            public final boolean onLongClick(View view2) {
                return Panel.this.m93lambda$wireTile$82$devcolorccPanel(obj, view2);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$wireTile$79$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m90lambda$wireTile$79$devcolorccPanel(final Object obj, View view) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.15
            @Override // java.lang.Runnable
            public final void run() {
                Panel.lambda$wireTile$78(Panel.this, obj);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$wireTile$82$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ boolean m93lambda$wireTile$82$devcolorccPanel(final Object obj, final View view) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.121
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m92lambda$wireTile$81$devcolorccPanel(obj, view);
            }
        });
        return true;
    }

    /* JADX INFO: renamed from: lambda$wireTile$81$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m92lambda$wireTile$81$devcolorccPanel(final Object obj, final View view) {
        if (Logic.menu(String.valueOf(ReflectUtils.callMethod(obj, "getTileSpec", new Object[0]))) != 0) {
            swell(view, new Runnable() { // from class: dev.colorcc.Panel.120
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m91lambda$wireTile$80$devcolorccPanel(obj, view);
                }
            });
            return;
        }
        Object objCallMethod = ReflectUtils.callMethod(obj, "getLongClickIntent", new Object[0]);
        if (objCallMethod instanceof Intent) {
            startSettings(new Intent((Intent) objCallMethod));
        }
    }

    /* JADX INFO: renamed from: lambda$wireTile$80$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m91lambda$wireTile$80$devcolorccPanel(Object obj, View view) {
        this.sheet.open(obj, view);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int stateOf(Object obj) {
        return ReflectUtils.getIntField(obj, "state");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static CharSequence text(Object obj, String str) {
        return (CharSequence) ReflectUtils.getObjectField(obj, str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Object iconSource(Object obj) {
        Object obj2;
        String[] strArr = {"mAnimatedResId", "mResId", "mId"};
        for (int r1 = 0; r1 < 3; r1++) {
            try {
                Field fieldFindFieldIfExists = ReflectUtils.findFieldIfExists(obj.getClass(), strArr[r1]);
                if (fieldFindFieldIfExists != null) {
                    return obj.getClass().getName() + "#" + fieldFindFieldIfExists.getInt(obj);
                }
            } catch (Throwable unused) {
            }
        }
        try {
            Field fieldFindFieldIfExists2 = ReflectUtils.findFieldIfExists(obj.getClass(), "mDrawable");
            return (fieldFindFieldIfExists2 == null || (obj2 = fieldFindFieldIfExists2.get(obj)) == null) ? obj : obj2;
        } catch (Throwable unused) {
            return obj;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Drawable stillDrawable(Object obj) {
        int r4;
        if (ReflectUtils.findFieldIfExists(obj.getClass(), "mAnimatedResId") == null) {
            return null;
        }
        Field fieldFindFieldIfExists = ReflectUtils.findFieldIfExists(obj.getClass(), "mResId");
        if (fieldFindFieldIfExists == null) {
            r4 = 0;
        } else {
            try {
                r4 = fieldFindFieldIfExists.getInt(obj);
            } catch (Throwable unused) {
                return null;
            }
        }
        if (r4 == 0) {
            return null;
        }
        return getContext().getDrawable(r4);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static Object iconOf(Object obj) {
        Object objectField = ReflectUtils.getObjectField(obj, "iconSupplier");
        Object obj2 = objectField instanceof Supplier ? ((Supplier) objectField).get() : null;
        return obj2 != null ? obj2 : ReflectUtils.getObjectField(obj, "icon");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void writeDarkMode(boolean z) {
        try {
            UiModeManager uiModeManager = (UiModeManager) getContext().getSystemService(UiModeManager.class);
            if (((Boolean) ReflectUtils.callMethod(uiModeManager, "setNightModeActivated", new Object[]{Boolean.valueOf(z)})).booleanValue()) {
                return;
            }
            uiModeManager.setNightMode(z ? 2 : 1);
        } catch (Throwable th) {
            ColorCCBridge.log("dark mode not set: " + th);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean readSetting(Class<?> cls, String str) {
        try {
            return ((Integer) ReflectUtils.callStaticMethod(cls, "getIntForUser", new Object[]{getContext().getContentResolver(), str, 0, -2})).intValue() == 1;
        } catch (Throwable th) {
            ColorCCBridge.log("setting " + str + " not read: " + th);
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void writeSetting(Class<?> cls, String str, boolean z) {
        try {
            ReflectUtils.callStaticMethod(cls, "putIntForUser", new Object[]{getContext().getContentResolver(), str, Integer.valueOf(z ? 1 : 0), -2});
        } catch (Throwable th) {
            ColorCCBridge.log("setting " + str + " not written: " + th);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean samsungRune(String str) {
        try {
            return ReflectUtils.getStaticBooleanField(ReflectUtils.findClass("com.android.systemui.QpRune", getContext().getClassLoader()), str);
        } catch (Throwable unused) {
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void openEditor() {
        fitEditor();
        long jUptimeMillis = SystemClock.uptimeMillis();
        StringBuilder sb = new StringBuilder("pencil tapped: editor ");
        sb.append(this.editor.active ? "already active" : "closed");
        sb.append(", shown ");
        sb.append(this.editor.getVisibility());
        sb.append(", host ");
        sb.append(ColorCCBridge.host != null);
        ColorCCBridge.log(sb.toString());
        this.editor.open();
        ColorCCBridge.log("editor built in " + (SystemClock.uptimeMillis() - jUptimeMillis) + " ms");
        postDelayed(new Runnable() { // from class: dev.colorcc.Panel.122
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m70lambda$openEditor$83$devcolorccPanel();
            }
        }, EDITOR_SETTLE_MS);
        postDelayed(new Runnable() { // from class: dev.colorcc.Panel.123
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m71lambda$openEditor$84$devcolorccPanel();
            }
        }, 800L);
    }

    /* JADX INFO: renamed from: lambda$openEditor$83$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m70lambda$openEditor$83$devcolorccPanel() {
        guard(new Runnable() { // from class: dev.colorcc.Panel.33
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.fitEditor();
            }
        });
    }

    /* JADX INFO: renamed from: lambda$openEditor$84$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m71lambda$openEditor$84$devcolorccPanel() {
        final Editor editor = this.editor;
        Objects.requireNonNull(editor);
        guard(new Runnable() { // from class: dev.colorcc.Panel.52
            @Override // java.lang.Runnable
            public final void run() {
                editor.logFit();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void redrawNotifications() {
        View viewNoteStack = noteStack();
        if (viewNoteStack != null) {
            invalidateAll(viewNoteStack);
        }
    }

    private View noteStack() {
        int identifier = getResources().getIdentifier("notification_stack_scroller", "id", "com.android.systemui");
        if (identifier == 0) {
            return null;
        }
        return getRootView().findViewById(identifier);
    }

    private static void invalidateAll(View view) {
        view.invalidate();
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int r0 = 0; r0 < viewGroup.getChildCount(); r0++) {
                invalidateAll(viewGroup.getChildAt(r0));
            }
        }
    }

    boolean windowBlurUsable() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(WindowManager.class);
        boolean z = false;
        if (windowManager != null && !ColorCCBridge.windowBlurBlocked()) {
            if (windowManager.isCrossWindowBlurEnabled()) {
                return true;
            }
            if (ColorCCBridge.oneUi8 && !ColorCCBridge.stillAsked) {
                z = true;
            }
            if (z && !this.ownBlurLogged) {
                this.ownBlurLogged = true;
                ColorCCBridge.log("One UI 8: cross window blur reported off, Samsung's shade blur taken as live until it asks for a still picture");
            }
        }
        return z;
    }

    void rememberSamsungLive() {
        if (ColorCCBridge.samsungLiveSeen) {
            return;
        }
        ColorCCBridge.samsungLiveSeen = true;
        guard(new Runnable() { // from class: dev.colorcc.Panel.38
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m81lambda$rememberSamsungLive$85$devcolorccPanel();
            }
        });
    }

    /* JADX INFO: renamed from: lambda$rememberSamsungLive$85$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m81lambda$rememberSamsungLive$85$devcolorccPanel() {
        prefs().edit().putBoolean("samsungLive", true).apply();
    }

    private static final Map<String, String> WORDS_EN = new HashMap<>();
    private static final Map<String, String> WORDS_TR = new HashMap<>();
    static {
        WORDS_EN.put("not_playing", "Not playing");
        WORDS_EN.put("sound", "Sound");
        WORDS_EN.put("vibrate", "Vibrate");
        WORDS_EN.put("silent", "Silent");
        WORDS_EN.put("volume", "Volume");
        WORDS_EN.put("media", "Media");
        WORDS_EN.put("ringtone", "Ringtone");
        WORDS_EN.put("notifications", "Notifications");
        WORDS_EN.put("system", "System");
        WORDS_EN.put("brightness", "Brightness");
        WORDS_EN.put("auto_brightness", "Auto\nbrightness");
        WORDS_EN.put("dark_mode", "Dark\nmode");
        WORDS_EN.put("extra_brightness", "Extra\nbrightness");
        WORDS_EN.put("eye_comfort", "Eye\ncomfort");
        WORDS_EN.put("narrow", "Narrow");
        WORDS_EN.put("wide", "Wide");
        WORDS_EN.put("wifi", "Wi-Fi");
        WORDS_EN.put("bluetooth", "Bluetooth");
        WORDS_EN.put("mobile_data", "Mobile data");
        WORDS_EN.put("hotspot", "Mobile Hotspot");
        WORDS_EN.put("dnd", "Do not disturb");
        WORDS_EN.put("torch", "Flashlight");
        WORDS_EN.put("media_output", "Media output");
        WORDS_EN.put("saved_networks", "Saved networks");
        WORDS_EN.put("other_networks", "Other networks");
        WORDS_EN.put("paired_devices", "Paired devices");
        WORDS_EN.put("available_devices", "Available devices");
        WORDS_EN.put("refresh", "Refresh");
        WORDS_EN.put("connected", "Connected");
        WORDS_EN.put("connecting", "Connecting…");
        WORDS_EN.put("disconnecting", "Disconnecting…");
        WORDS_EN.put("add", "Add");
        WORDS_EN.put("searching", "Searching…");
        WORDS_EN.put("no_outputs", "No output devices found");
        WORDS_EN.put("no_sim", "No SIM card");
        WORDS_EN.put("data_used", "%1$s today · %2$s this month");
        WORDS_EN.put("name", "Name");
        WORDS_EN.put("password", "Password");
        WORDS_EN.put("unlock_to_see", "Unlock to see");
        WORDS_EN.put("device_connected", "1 device connected");
        WORDS_EN.put("devices_connected", "%1$d devices connected");
        WORDS_EN.put("dnd_note", "Calls and alerts are silenced while Do not disturb is on.");
        WORDS_EN.put("dnd_until_off", "Until I turn it off");
        WORDS_EN.put("hours", "%1$d hr");
        WORDS_EN.put("off_at", "Turns off at %1$s");
        WORDS_EN.put("edit", "Edit");
        WORDS_EN.put("done", "Done");
        WORDS_EN.put("reset", "Reset to default");
        WORDS_EN.put("edit_how", "Drag to move · Drag the corner to resize · Tap + or −");
        WORDS_EN.put("in_panel", "In panel");
        WORDS_EN.put("more_to_add", "More to add");
        WORDS_EN.put("tiles_to_add", "Tiles to add");
        WORDS_EN.put("all_tiles_added", "All available tiles are in the panel");
        WORDS_EN.put("sound_modes", "Sound modes");
        WORDS_EN.put("tiles", "Tiles");
        WORDS_EN.put("layout", "Layout");
        WORDS_EN.put("look", "Look");
        WORDS_EN.put("appearance", "Appearance");
        WORDS_EN.put("glass", "Glass");
        WORDS_EN.put("liquid", "Liquid");
        WORDS_EN.put("frosted", "Frosted");
        WORDS_EN.put("opaque", "Opaque");
        WORDS_EN.put("none", "None");
        WORDS_EN.put("glass_tint", "Glass tint");
        WORDS_EN.put("edge_shine", "Edge shine");
        WORDS_EN.put("rim_width", "Rim width");
        WORDS_EN.put("rim_glow", "Rim glow");
        WORDS_EN.put("frost", "Frost");
        WORDS_EN.put("colour_strength", "Colour strength");
        WORDS_EN.put("thickness", "Thickness");
        WORDS_EN.put("plain_colour", "Plain colour");
        WORDS_EN.put("plain_auto", "Auto");
        WORDS_EN.put("plain_white", "White");
        WORDS_EN.put("plain_black", "Black");
        WORDS_EN.put("background_blur", "Background blur");
        WORDS_EN.put("light", "Light");
        WORDS_EN.put("normal", "Normal");
        WORDS_EN.put("strong", "Strong");
        WORDS_EN.put("blur_strength", "Blur strength");
        WORDS_EN.put("tile_rows", "Tile rows");
        WORDS_EN.put("icon_colours", "Icon colours");
        WORDS_EN.put("custom", "Custom");
        WORDS_EN.put("hue", "Hue");
        WORDS_EN.put("saturation", "Saturation");
        WORDS_EN.put("tile_names", "Tile names");
        WORDS_EN.put("show", "Show");
        WORDS_EN.put("hide", "Hide");
        WORDS_EN.put("jelly_sliders", "Jelly sliders");
        WORDS_EN.put("bounce", "Bounce");
        WORDS_EN.put("notification_glass", "Notification glass");
        WORDS_EN.put("footer_glass", "Footer button glass");
        WORDS_EN.put("samsung", "Stock");
        WORDS_EN.put("colorcc", "ColorCC");
        WORDS_EN.put("page_swipe", "Page swipe on tiles");
        WORDS_EN.put("tile_pages", "Tile pages");
        WORDS_EN.put("on", "On");
        WORDS_EN.put("off", "Off");
        WORDS_EN.put("close", "Close");
        WORDS_EN.put("settings", "Settings");
        WORDS_EN.put("previous", "Previous");
        WORDS_EN.put("play", "Play");
        WORDS_EN.put("pause", "Pause");
        WORDS_EN.put("next", "Next");
        WORDS_EN.put("add_to_panel", "Add to panel");
        WORDS_EN.put("remove", "Remove");
        WORDS_EN.put("resize", "Resize");
        WORDS_EN.put("vpn_connected", "VPN connected");
        WORDS_EN.put("vpn_connected_to", "VPN connected to %1$s");
        WORDS_EN.put("classic_qs_mode", "Quick Settings Mode");
        WORDS_EN.put("switch_classic_qs", "Switch to Classic Blaze QS");
        WORDS_EN.put("switch_classic_qs_how", "Switches back to the BlazeAOSP Compose Quick Settings panel. You can return to ColorCC anytime from the QS header.");
        WORDS_EN.put("slider_jelly", "Slider jelly");
        WORDS_EN.put("notif_stock", "Stock");
        WORDS_EN.put("notif_match", "Match panel");
        WORDS_EN.put("notif_buttons", "Notification buttons");
        WORDS_EN.put("notif_swipe", "Notification swipe");
        WORDS_EN.put("reset_how", "Puts the layout, the tiles and the look back as they first were.");
        WORDS_EN.put("reset_look_how", "Puts only the look back as it first was. The layout and tiles stay.");
        WORDS_EN.put("all_in", "Everything is in the control centre.");
        WORDS_EN.put("bluetooth_off", "Turn on Bluetooth to see your devices");
        WORDS_EN.put("wifi_off", "Turn on Wi-Fi to see networks");
        WORDS_EN.put("dnd_off", "Do Not Disturb is off");
        WORDS_EN.put("no_paired", "No paired devices");
        WORDS_EN.put("cards_sliders", "Cards and sliders");
        WORDS_EN.put("colour_tone", "Adaptive colour tone");
        WORDS_EN.put("edit_tiles", "Edit tiles");
        WORDS_EN.put("lightness", "Lightness");
        WORDS_EN.put("media_volume", "Media volume");
        WORDS_EN.put("more_settings", "More settings");
        WORDS_EN.put("open_app", "Open the playing app");
        WORDS_EN.put("playback_position", "Playback position");
        WORDS_EN.put("silent_mode", "Silent mode");
        WORDS_EN.put("sound_output", "Sound output");
        WORDS_EN.put("tap_to_add", "Tap + to add");
        WORDS_EN.put("torch_brightness", "Torch brightness");
        WORDS_EN.put("torch_off", "Torch: off");
        WORDS_EN.put("torch_on", "Torch: on");
        WORDS_EN.put("cancel", "Cancel");
        WORDS_EN.put("power_menu", "Power menu");
        WORDS_EN.put("active_apps", "Active apps");

        WORDS_TR.put("not_playing", "Çalmıyor");
        WORDS_TR.put("sound", "Ses");
        WORDS_TR.put("vibrate", "Titreşim");
        WORDS_TR.put("silent", "Sessiz");
        WORDS_TR.put("volume", "Ses düzeyi");
        WORDS_TR.put("media", "Medya");
        WORDS_TR.put("ringtone", "Zil sesi");
        WORDS_TR.put("notifications", "Bildirimler");
        WORDS_TR.put("system", "Sistem");
        WORDS_TR.put("brightness", "Parlaklık");
        WORDS_TR.put("auto_brightness", "Otomatik\nparlaklık");
        WORDS_TR.put("dark_mode", "Karanlık\nmod");
        WORDS_TR.put("extra_brightness", "Ekstra\nparlaklık");
        WORDS_TR.put("eye_comfort", "Göz\nrahatlığı");
        WORDS_TR.put("narrow", "Dar");
        WORDS_TR.put("wide", "Geniş");
        WORDS_TR.put("wifi", "Wi-Fi");
        WORDS_TR.put("bluetooth", "Bluetooth");
        WORDS_TR.put("mobile_data", "Mobil veri");
        WORDS_TR.put("hotspot", "Mobil Erişim Noktası");
        WORDS_TR.put("dnd", "Rahatsız etmeyin");
        WORDS_TR.put("torch", "El feneri");
        WORDS_TR.put("media_output", "Medya çıkışı");
        WORDS_TR.put("saved_networks", "Kayıtlı ağlar");
        WORDS_TR.put("other_networks", "Diğer ağlar");
        WORDS_TR.put("paired_devices", "Eşleştirilen cihazlar");
        WORDS_TR.put("available_devices", "Kullanılabilir cihazlar");
        WORDS_TR.put("refresh", "Yenile");
        WORDS_TR.put("connected", "Bağlı");
        WORDS_TR.put("connecting", "Bağlanıyor…");
        WORDS_TR.put("disconnecting", "Bağlantı kesiliyor…");
        WORDS_TR.put("add", "Ekle");
        WORDS_TR.put("searching", "Aranıyor…");
        WORDS_TR.put("no_outputs", "Çıkış cihazı bulunamadı");
        WORDS_TR.put("no_sim", "SIM kart yok");
        WORDS_TR.put("data_used", "Bugün %1$s · Bu ay %2$s");
        WORDS_TR.put("name", "Ad");
        WORDS_TR.put("password", "Şifre");
        WORDS_TR.put("unlock_to_see", "Görmek için kilidi açın");
        WORDS_TR.put("device_connected", "1 cihaz bağlı");
        WORDS_TR.put("devices_connected", "%1$d cihaz bağlı");
        WORDS_TR.put("dnd_note", "Rahatsız etmeyin açıkken aramalar ve uyarılar sessize alınır.");
        WORDS_TR.put("dnd_until_off", "Ben kapatana kadar");
        WORDS_TR.put("hours", "%1$d sa");
        WORDS_TR.put("off_at", "%1$s itibarıyla kapanır");
        WORDS_TR.put("edit", "Düzenle");
        WORDS_TR.put("done", "Bitti");
        WORDS_TR.put("reset", "Varsayılana sıfırla");
        WORDS_TR.put("edit_how", "Taşımak için sürükleyin · Boyutlandırmak için köşeyi sürükleyin · + veya − düğmesine dokunun");
        WORDS_TR.put("in_panel", "Panelde");
        WORDS_TR.put("more_to_add", "Eklenecek diğer öğeler");
        WORDS_TR.put("tiles_to_add", "Eklenecek karolar");
        WORDS_TR.put("all_tiles_added", "Kullanılabilir tüm karolar panelde");
        WORDS_TR.put("sound_modes", "Ses modları");
        WORDS_TR.put("tiles", "Karolar");
        WORDS_TR.put("layout", "Yerleşim");
        WORDS_TR.put("look", "Görünüm");
        WORDS_TR.put("appearance", "Görünüm");
        WORDS_TR.put("glass", "Cam");
        WORDS_TR.put("liquid", "Sıvı");
        WORDS_TR.put("frosted", "Buzlu");
        WORDS_TR.put("opaque", "Opak");
        WORDS_TR.put("none", "Yok");
        WORDS_TR.put("glass_tint", "Cam tonu");
        WORDS_TR.put("edge_shine", "Kenar parlaklığı");
        WORDS_TR.put("rim_width", "Çerçeve kalınlığı");
        WORDS_TR.put("rim_glow", "Çerçeve ışıltısı");
        WORDS_TR.put("frost", "Buzlanma");
        WORDS_TR.put("colour_strength", "Renk gücü");
        WORDS_TR.put("thickness", "Kalınlık");
        WORDS_TR.put("plain_colour", "Düz renk");
        WORDS_TR.put("plain_auto", "Otomatik");
        WORDS_TR.put("plain_white", "Beyaz");
        WORDS_TR.put("plain_black", "Siyah");
        WORDS_TR.put("background_blur", "Arka plan bulanıklığı");
        WORDS_TR.put("light", "Hafif");
        WORDS_TR.put("normal", "Normal");
        WORDS_TR.put("strong", "Güçlü");
        WORDS_TR.put("blur_strength", "Bulanıklık gücü");
        WORDS_TR.put("tile_rows", "Karo satırları");
        WORDS_TR.put("icon_colours", "Simge renkleri");
        WORDS_TR.put("custom", "Özel");
        WORDS_TR.put("hue", "Renk tonu");
        WORDS_TR.put("saturation", "Doygunluk");
        WORDS_TR.put("tile_names", "Karo adları");
        WORDS_TR.put("show", "Göster");
        WORDS_TR.put("hide", "Gizle");
        WORDS_TR.put("jelly_sliders", "Jöle kaydırıcılar");
        WORDS_TR.put("bounce", "Esneklik");
        WORDS_TR.put("notification_glass", "Bildirim camı");
        WORDS_TR.put("footer_glass", "Alt düğme camı");
        WORDS_TR.put("samsung", "Stok");
        WORDS_TR.put("colorcc", "ColorCC");
        WORDS_TR.put("page_swipe", "Karolarda sayfa kaydırma");
        WORDS_TR.put("tile_pages", "Karo sayfaları");
        WORDS_TR.put("on", "Açık");
        WORDS_TR.put("off", "Kapalı");
        WORDS_TR.put("close", "Kapat");
        WORDS_TR.put("settings", "Ayarlar");
        WORDS_TR.put("previous", "Önceki");
        WORDS_TR.put("play", "Oynat");
        WORDS_TR.put("pause", "Duraklat");
        WORDS_TR.put("next", "Sonraki");
        WORDS_TR.put("add_to_panel", "Panele ekle");
        WORDS_TR.put("remove", "Kaldır");
        WORDS_TR.put("resize", "Boyutlandır");
        WORDS_TR.put("vpn_connected", "VPN bağlı");
        WORDS_TR.put("vpn_connected_to", "%1$s ağına VPN ile bağlı");
        WORDS_TR.put("classic_qs_mode", "Hızlı Ayarlar Modu");
        WORDS_TR.put("switch_classic_qs", "Klasik Blaze QS Paneline Geç");
        WORDS_TR.put("switch_classic_qs_how", "BlazeAOSP Compose Hızlı Ayarlar paneline geri döner. İstediğiniz zaman QS üst çubuğundan ColorCC paneline tekrar geçebilirsiniz.");
        WORDS_TR.put("slider_jelly", "Kaydırıcı esnekliği");
        WORDS_TR.put("notif_stock", "Stok");
        WORDS_TR.put("notif_match", "Panelle aynı");
        WORDS_TR.put("notif_buttons", "Bildirim düğmeleri");
        WORDS_TR.put("notif_swipe", "Bildirim kaydırma");
        WORDS_TR.put("reset_how", "Düzeni, döşemeleri ve stili ilk hallerine döndürür.");
        WORDS_TR.put("reset_look_how", "Yalnızca görünümü ilk hâline döndürür. Düzen ve kutucuklar kalır.");
        WORDS_TR.put("all_in", "Her şey kontrol merkezinde.");
        WORDS_TR.put("bluetooth_off", "Cihazlarınızı görmek için Bluetooth'u açın");
        WORDS_TR.put("wifi_off", "Ağları görmek için Wi-Fi'ı açın");
        WORDS_TR.put("dnd_off", "Rahatsız Etmeyin kapalı");
        WORDS_TR.put("no_paired", "Eşleştirilmiş cihaz yok");
        WORDS_TR.put("cards_sliders", "Kartlar ve kaydırıcılar");
        WORDS_TR.put("colour_tone", "Uyarlanabilir renk tonu");
        WORDS_TR.put("edit_tiles", "Döşemeleri düzenle");
        WORDS_TR.put("lightness", "Açıklık");
        WORDS_TR.put("media_volume", "Medya ses düzeyi");
        WORDS_TR.put("more_settings", "Diğer ayarlar");
        WORDS_TR.put("open_app", "Çalan uygulamayı aç");
        WORDS_TR.put("playback_position", "Oynatma konumu");
        WORDS_TR.put("silent_mode", "Sessiz mod");
        WORDS_TR.put("sound_output", "Ses çıkışı");
        WORDS_TR.put("tap_to_add", "Eklemek için + simgesine dokunun");
        WORDS_TR.put("torch_brightness", "El feneri parlaklığı");
        WORDS_TR.put("torch_off", "El feneri: kapalı");
        WORDS_TR.put("torch_on", "El feneri: açık");
        WORDS_TR.put("cancel", "İptal");
        WORDS_TR.put("power_menu", "Güç menüsü");
        WORDS_TR.put("active_apps", "Etkin uygulamalar");
    }

    String word(String str, Object... objArr) {
        try {
            String lang = Locale.getDefault().getLanguage();
            String val = "tr".equalsIgnoreCase(lang) ? WORDS_TR.get(str) : WORDS_EN.get(str);
            if (val == null) {
                val = WORDS_EN.get(str);
            }
            if (val != null) {
                return (objArr == null || objArr.length == 0) ? val : String.format(Locale.getDefault(), val, objArr);
            }
            Resources resources = getContext().getResources();
            int identifier = resources.getIdentifier("colorcc_" + str, "string", getContext().getPackageName());
            if (identifier != 0) {
                return objArr.length == 0 ? resources.getString(identifier) : resources.getString(identifier, objArr);
            }
        } catch (Throwable th) {
            if (!this.wordsMissing) {
                this.wordsMissing = true;
                ColorCCBridge.log("words not read, names shown: " + th);
            }
        }
        return str;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void samsungOutput(String str) {
        Intent intentPutExtra = new Intent("com.android.systemui.action.LAUNCH_MEDIA_OUTPUT_DIALOG").setPackage("com.android.systemui").putExtra("package_name", str);
        if (str != null && !getContext().getPackageManager().queryBroadcastReceivers(intentPutExtra, 0).isEmpty()) {
            getContext().sendBroadcast(intentPutExtra);
        } else {
            openSettings("android.settings.SOUND_SETTINGS");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void openSettings(String str) {
        startSettings(new Intent(str));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startSettings(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (ColorCCBridge.activityStarter != null) {
            ColorCCBridge.activityStarter.postStartActivityDismissingKeyguard(intent, 0);
            return;
        }
        Iterator<Bound> it = this.bound.iterator();
        while (it.hasNext()) {
            try {
                ReflectUtils.callMethod(ReflectUtils.getObjectField(it.next().tile, "mActivityStarter"), "postStartActivityDismissingKeyguard", new Object[]{intent, 0});
                return;
            } catch (Throwable unused) {
            }
        }
        if (this.keyguard.isKeyguardLocked()) {
            return;
        }
        getContext().startActivity(intent);
        ReflectUtils.callMethod(getContext().getSystemService("statusbar"), "collapsePanels", new Object[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void openMediaApp() {
        MediaController mediaController = this.controller;
        if (mediaController == null) {
            return;
        }
        PendingIntent sessionActivity = mediaController.getSessionActivity();
        if (sessionActivity != null) {
            Iterator<Bound> it = this.bound.iterator();
            while (it.hasNext()) {
                ReflectUtils.callMethod(ReflectUtils.getObjectField(it.next().tile, "mActivityStarter"), "postStartActivityDismissingKeyguard", new Object[]{sessionActivity});
                return;
            }
            try {
                sessionActivity.send();
                ReflectUtils.callMethod(getContext().getSystemService("statusbar"), "collapsePanels", new Object[0]);
                return;
            } catch (Throwable th) {
                ColorCCBridge.log("playing app's screen not opened: " + th);
            }
        }
        Intent launchIntentForPackage = getContext().getPackageManager().getLaunchIntentForPackage(mediaController.getPackageName());
        if (launchIntentForPackage != null) {
            startSettings(launchIntentForPackage);
        } else {
            ColorCCBridge.log("playing app not opened: no screen to open");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onBrightness(float f, boolean z) {
        if (z) {
            this.peekFrom = -1.0f;
            this.peek.to(0.0f);
        } else {
            if (this.peekFrom < 0.0f) {
                this.peekFrom = f;
            }
            if (Math.abs(f - this.peekFrom) > 0.02f) {
                this.peek.to(1.0f);
            }
        }
        applyBrightness(f, z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void applyBrightness(final float f, final boolean z) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.88
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m3lambda$applyBrightness$86$devcolorccPanel(z, f);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$applyBrightness$86$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m3lambda$applyBrightness$86$devcolorccPanel(boolean z, float f) {
        ReflectUtils.callMethod(this.display, z ? "setBrightness" : "setTemporaryBrightness", new Object[]{0, Float.valueOf(Logic.toLinear(f, this.brightMin, this.brightMax))});
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onVolume(float f, boolean z) {
        setVolume(3, f);
    }

    private volatile long lastVolumeWriteAt;

    /* JADX INFO: Access modifiers changed from: private */
    public void setVolume(final int r3, float f) {
        this.lastVolumeWriteAt = SystemClock.uptimeMillis();
        if (this.volumeWanted.put(Integer.valueOf(r3), Float.valueOf(f)) != null) {
            return;
        }
        this.audioThread.execute(new Runnable() { // from class: dev.colorcc.Panel.43
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m82lambda$setVolume$87$devcolorccPanel(r3);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$setVolume$87$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m82lambda$setVolume$87$devcolorccPanel(int r3) {
        try {
            Float wanted;
            while ((wanted = this.volumeWanted.remove(Integer.valueOf(r3))) != null) {
                this.lastVolumeWriteAt = SystemClock.uptimeMillis();
                int maxVol = this.audio.getStreamMaxVolume(r3);
                int r0 = Math.max(0, Math.min(maxVol, Math.round(wanted.floatValue() * maxVol)));
                if (r0 != this.audio.getStreamVolume(r3)) {
                    int r1 = r3 == 2 ? 2 : 0;
                    this.audio.setStreamVolume(r3, r0, r1);
                }
            }
        } catch (Throwable th) {
            ColorCCBridge.log("volume not set: " + th);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onRingerTap(int r3) {
        final int r1;
        if (r3 == 0) {
            r1 = 2;
        } else {
            r1 = r3 == 1 ? 1 : 0;
        }
        showSlot(r3, true);
        this.audioThread.execute(new Runnable() { // from class: dev.colorcc.Panel.74
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m64lambda$onRingerTap$89$devcolorccPanel(r1);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$onRingerTap$89$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m64lambda$onRingerTap$89$devcolorccPanel(int r5) {
        try {
            ReflectUtils.callMethod(this.audio, "setRingerModeInternal", new Object[]{Integer.valueOf(r5)});
        } catch (Throwable th) {
            try {
                this.audio.setRingerMode(r5);
            } catch (Throwable th2) {
                ColorCCBridge.log("ringer not set: " + th2);
            }
        }
        this.main.post(new Runnable() { // from class: dev.colorcc.Panel.14
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m63lambda$onRingerTap$88$devcolorccPanel();
            }
        });
    }

    /* JADX INFO: renamed from: lambda$onRingerTap$88$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m63lambda$onRingerTap$88$devcolorccPanel() {
        guard(new Runnable() { // from class: dev.colorcc.Panel.25
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.refreshRing();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refreshRing() {
        if (this.ringRefreshQueued) {
            return;
        }
        this.ringRefreshQueued = true;
        this.audioThread.execute(new Runnable() { // from class: dev.colorcc.Panel.21
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m80lambda$refreshRing$93$devcolorccPanel();
            }
        });
    }

    /* JADX INFO: renamed from: lambda$refreshRing$93$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m80lambda$refreshRing$93$devcolorccPanel() {
        try {
            final int r5 = ((Integer) ReflectUtils.callMethod(this.audio, "getRingerModeInternal", new Object[0])).intValue();
            final int streamVolume = this.audio.getStreamVolume(3);
            final int streamMaxVolume = this.audio.getStreamMaxVolume(3);
            final int r9 = this.sheet.menuStream;
            final int streamVolume2 = this.audio.getStreamVolume(r9);
            final int streamMaxVolume2 = this.audio.getStreamMaxVolume(r9);
            int length = this.sheet.STREAMS.length;
            final boolean[] zArr = new boolean[length];
            for (int r1 = 0; r1 < length; r1++) {
                zArr[r1] = this.audio.isStreamMute(this.sheet.STREAMS[r1]) || this.audio.getStreamVolume(this.sheet.STREAMS[r1]) == 0;
            }
            this.main.post(new Runnable() { // from class: dev.colorcc.Panel.115
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m78lambda$refreshRing$91$devcolorccPanel(r5, streamVolume, streamMaxVolume, zArr, r9, streamMaxVolume2, streamVolume2);
                }
            });
        } catch (Throwable th) {
            this.main.post(new Runnable() { // from class: dev.colorcc.Panel.116
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.this.m79lambda$refreshRing$92$devcolorccPanel();
                }
            });
            ColorCCBridge.log("ringer not read: " + th);
        }
    }

    /* JADX INFO: renamed from: lambda$refreshRing$91$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m78lambda$refreshRing$91$devcolorccPanel(final int r10, final int r11, final int r12, final boolean[] zArr, final int r14, final int r15, final int r16) {
        this.ringRefreshQueued = false;
        guard(new Runnable() { // from class: dev.colorcc.Panel.59
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m77lambda$refreshRing$90$devcolorccPanel(r10, r11, r12, zArr, r14, r15, r16);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$refreshRing$90$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m77lambda$refreshRing$90$devcolorccPanel(int r2, int r3, int r4, boolean[] zArr, int r6, int r7, int r8) {
        showRing(r2, r3, r4, true);
        this.sheet.showMuted(zArr);
        if (!this.sheet.loud.isPressed() && !this.volumeWanted.containsKey(Integer.valueOf(r6))
                && SystemClock.uptimeMillis() - this.lastVolumeWriteAt >= 180L) {
            this.sheet.showRing(r6, r7 == 0 ? 0.0f : (float) r8 / (float) r7, r2 == 0, true);
        }
    }

    /* JADX INFO: renamed from: lambda$refreshRing$92$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m79lambda$refreshRing$92$devcolorccPanel() {
        this.ringRefreshQueued = false;
    }

    private void showRing(int r2, int r3, int r4, boolean z) {
        if (!this.volume.isPressed() && !this.volumeWanted.containsKey(3)
                && SystemClock.uptimeMillis() - this.lastVolumeWriteAt >= 180L) {
            this.volume.setValue(r4 == 0 ? 0.0f : (float) r3 / (float) r4);
        }
        showSlot(Logic.thumb(r2), z);
    }

    private void showSlot(int r2, boolean z) {
        if (r2 == this.shownRingerSlot) {
            return;
        }
        this.shownRingerSlot = r2;
        this.ringer.select(r2, z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: syncMedia, reason: merged with bridge method [inline-methods] */
    public void m16lambda$new$1$devcolorccPanel(List<MediaController> list) {
        MediaController mediaController;
        ArrayList arrayList = new ArrayList();
        for (MediaController mediaController2 : list) {
            if (!NOT_MUSIC.contains(mediaController2.getPackageName())) {
                arrayList.add(mediaController2);
            }
        }
        Iterator it = arrayList.iterator();
        while (true) {
            if (!it.hasNext()) {
                mediaController = null;
                break;
            }
            mediaController = (MediaController) it.next();
            PlaybackState playbackState = mediaController.getPlaybackState();
            if (playbackState != null && playbackState.getState() == 3) {
                break;
            }
        }
        if (mediaController == null && !arrayList.isEmpty()) {
            mediaController = (MediaController) arrayList.get(0);
        }
        MediaController mediaController3 = this.controller;
        if (mediaController != null ? mediaController3 == null || !mediaController.getSessionToken().equals(this.controller.getSessionToken()) : mediaController3 != null) {
            MediaController mediaController4 = this.controller;
            if (mediaController4 != null) {
                mediaController4.unregisterCallback(this.mediaCallback);
            }
            this.controller = mediaController;
            if (mediaController != null) {
                mediaController.registerCallback(this.mediaCallback, this.main);
            }
        }
        renderMedia();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void renderMedia() {
        Bitmap bitmap;
        MediaController mediaController = this.controller;
        MediaMetadata metadata = mediaController == null ? null : mediaController.getMetadata();
        CharSequence text = metadata == null ? null : metadata.getText("android.media.metadata.TITLE");
        PlaybackState playbackState = mediaController == null ? null : mediaController.getPlaybackState();
        if (metadata != null) {
            Bitmap bitmap2 = metadata.getBitmap("android.media.metadata.ALBUM_ART");
            if (bitmap2 == null) {
                bitmap2 = metadata.getBitmap("android.media.metadata.DISPLAY_ICON");
            }
            bitmap = bitmap2;
        } else {
            bitmap = null;
        }
        if (bitmap != this.tonedArt) {
            this.tonedArt = bitmap;
            this.mediaTones = bitmap == null ? null : artTones(bitmap);
        }
        CharSequence text2 = metadata != null ? metadata.getText("android.media.metadata.ARTIST") : null;
        boolean z = playbackState != null && playbackState.getState() == 3;
        this.media.render(text, text2, bitmap, z, mediaController != null);
        Player player = this.player;
        if (player != null) {
            if (mediaController == null) {
                player.close();
            } else {
                player.render(text, text2, bitmap, z);
            }
        }
    }

    boolean isOverControl(float f, float f2) {
        if (!this.interactive) {
            return false;
        }
        for (View view : this.touchControls) {
            if (view.isShown() && contains(view, f, f2)) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARN: Code duplicated, block: B:11:0x0020  */
    Boolean collapsibleAt(float f, float f2) {
        boolean z;
        if (!this.interactive) {
            return null;
        }
        if (isOverControl(f, f2)) {
            z = false;
        } else {
            z = true;
            if (this.scroller.value() < this.scrollMax - 1 && overCards(f)) {
                z = false;
            }
        }
        return Boolean.valueOf(z);
    }

    boolean replacesStockGrid() {
        if (this.dead) {
            return false;
        }
        return !large() || this.foldable;
    }

    private static boolean contains(View view, float f, float f2) {
        int[] r0 = new int[2];
        view.getLocationOnScreen(r0);
        int r2 = r0[0];
        if (f >= r2 && f <= r2 + view.getWidth()) {
            int r1 = r0[1];
            if (f2 >= r1 && f2 <= r1 + view.getHeight()) {
                return true;
            }
        }
        return false;
    }

    private Object flowValue(Object obj, String str) {
        Map<String, Object> map = this.flows.get(obj);
        if (map == null) {
            if (this.flows.size() > TILES_PER_ROW) {
                this.flows.clear();
            }
            map = new HashMap<>();
            this.flows.put(obj, map);
        }
        Object objectField = map.get(str);
        if (objectField == null) {
            try {
                objectField = ReflectUtils.getObjectField(obj, str);
            } catch (NoSuchFieldError unused) {
                objectField = NO_FLOW;
                ColorCCBridge.log("no " + str + " in " + obj.getClass().getSimpleName() + " on this One UI, read as none");
            }
            if (objectField == null) {
                return null;
            }
            map.put(str, objectField);
        }
        if (objectField == NO_FLOW) {
            return null;
        }
        try {
            Method method = this.flowGetters.get(objectField.getClass());
            if (method == null) {
                method = objectField.getClass().getMethod("getValue");
                method.setAccessible(true);
                this.flowGetters.put(objectField.getClass(), method);
            }
            return method.invoke(objectField);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private float flowNumber(Object obj, String str) {
        Object objFlowValue = flowValue(obj, str);
        if (objFlowValue instanceof Number) {
            return ((Number) objFlowValue).floatValue();
        }
        return 0.0f;
    }

    private boolean flowTrue(Object obj, String str) {
        return Boolean.TRUE.equals(flowValue(obj, str));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void guard(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable th) {
            ColorCCBridge.log("action failed: " + th);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int dp(float f) {
        return Math.round(f * this.density);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static LinearLayout horizontal(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(0);
        linearLayout.setClipChildren(false);
        return linearLayout;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static FrameLayout host(Context context, View view) {
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setClipChildren(false);
        frameLayout.addView(view, new FrameLayout.LayoutParams(-1, -1));
        return frameLayout;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void setTextIfChanged(TextView textView, CharSequence charSequence) {
        if (charSequence == null) {
            charSequence = "";
        }
        if (TextUtils.equals(textView.getText(), charSequence)) {
            return;
        }
        textView.setText(charSequence);
    }

    private void scrollCards(float f) {
        int r3 = Math.round(Math.max(0.0f, Math.min(this.scrollMax, f)));
        if (this.column.getScrollY() == r3) {
            return;
        }
        this.column.scrollTo(0, r3);
        swapStatusRow();
        if (Glass.bending()) {
            reglaze(this.column);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public TextView label(Context context, float f, int r4, int r5) {
        FitText fitText = new FitText(context);
        fitText.setTextSize(0, f * this.density * getResources().getConfiguration().fontScale);
        fitText.setTextColor(r4);
        fitText.setMaxLines(r5);
        fitText.setEllipsize(TextUtils.TruncateAt.END);
        return fitText;
    }

    private static final class FitText extends TextView {
        private static final float SMALLEST = 0.78f;
        private float largest;
        private final Paint probe;

        FitText(Context context) {
            super(context);
            this.probe = new Paint();
        }

        @Override // android.widget.TextView
        public void setTextSize(int r1, float f) {
            super.setTextSize(r1, f);
            this.largest = getTextSize();
        }

        @Override // android.widget.TextView, android.view.View
        protected void onMeasure(int r9, int r10) {
            int size = (View.MeasureSpec.getSize(r9) - getCompoundPaddingLeft()) - getCompoundPaddingRight();
            if (View.MeasureSpec.getMode(r9) != 0 && size > 0) {
                float fMax = 0.0f;
                if (this.largest > 0.0f) {
                    this.probe.set(getPaint());
                    this.probe.setTextSize(this.largest);
                    String string = getText().toString();
                    for (String str : getMaxLines() == 1 ? new String[]{string} : string.split("\\s+")) {
                        fMax = Math.max(fMax, this.probe.measureText(str));
                    }
                    float f = size;
                    float fMax2 = this.largest;
                    if (fMax > f) {
                        fMax2 = Math.max(SMALLEST * fMax2, (fMax2 * f) / fMax);
                    }
                    if (fMax2 != getTextSize()) {
                        super.setTextSize(0, fMax2);
                    }
                }
            }
            super.onMeasure(r9, r10);
        }
    }

    private static final class Cropped extends Drawable implements Drawable.Callback {
        private final int[] box;
        private final Drawable inner;
        private final int size;
        private int tint = -1;

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }

        Cropped(Drawable drawable, int[] r3, int r4) {
            this.inner = drawable;
            this.box = r3;
            this.size = r4;
            drawable.setBounds(0, 0, r4, r4);
            drawable.setCallback(this);
        }

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicWidth() {
            int[] r2 = this.box;
            return r2[2] - r2[0];
        }

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicHeight() {
            int[] r2 = this.box;
            return r2[3] - r2[1];
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            float fWidth = (float) bounds.width() / Math.max(1, getIntrinsicWidth());
            float fHeight = (float) bounds.height() / Math.max(1, getIntrinsicHeight());
            float f = bounds.left - (this.box[0] * fWidth);
            float f2 = bounds.top - (this.box[1] * fHeight);
            int r3 = this.size;
            int r14 = canvas.saveLayer(f, f2, f + (r3 * fWidth), f2 + (r3 * fHeight), null);
            int r4 = canvas.save();
            canvas.translate(bounds.left, bounds.top);
            canvas.scale(fWidth, fHeight);
            int[] r0 = this.box;
            canvas.translate(-r0[0], -r0[1]);
            this.inner.draw(canvas);
            canvas.restoreToCount(r4);
            canvas.drawColor(this.tint, PorterDuff.Mode.SRC_IN);
            canvas.restoreToCount(r14);
        }

        @Override // android.graphics.drawable.Drawable
        public void setTintList(ColorStateList colorStateList) {
            this.tint = colorStateList == null ? -1 : colorStateList.getDefaultColor();
            invalidateSelf();
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int r1) {
            this.inner.setAlpha(r1);
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
            this.inner.setColorFilter(colorFilter);
        }

        @Override // android.graphics.drawable.Drawable.Callback
        public void invalidateDrawable(Drawable drawable) {
            invalidateSelf();
        }

        @Override // android.graphics.drawable.Drawable.Callback
        public void scheduleDrawable(Drawable drawable, Runnable runnable, long j) {
            scheduleSelf(runnable, j);
        }

        @Override // android.graphics.drawable.Drawable.Callback
        public void unscheduleDrawable(Drawable drawable, Runnable runnable) {
            unscheduleSelf(runnable);
        }
    }

    private final class Disc extends FrameLayout {
        int accent;
        int effect;
        final Glass glass;
        final ImageView icon;
        private final int iconSize;
        private final Runnable land;
        private Object lastIcon;
        private int lastTint;
        private Object lateIcon;
        final Motion.Value level;
        boolean ownAnimation;
        private Object[] pendingStill;
        private Runnable settleIcon;
        private int shownState;
        private Drawable stillShown;

        Disc(Context context, int r11) {
            super(context);
            this.effect = 0;
            this.accent = Panel.this.theme(Panel.ACTIVE_BLUE);
            this.shownState = -1;
            this.land = new Runnable() { // from class: dev.colorcc.Panel.Disc.1
                @Override // java.lang.Runnable
                public final void run() {
                    Disc.this.land();
                }
            };
            this.iconSize = r11;
            Glass glass = new Glass(-1.0f, Panel.GLASS_IDLE, Panel.GLASS_DISC_ON, 0.5f, 0.5f);
            this.glass = glass;
            setBackground(glass);
            setClickable(true);
            setClipChildren(false);
            ImageView imageView = new ImageView(context);
            this.icon = imageView;
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            addView(imageView, new FrameLayout.LayoutParams(r11, r11, 17));
            this.level = Panel.this.motion.value(Logic.Spring.FLOOD, new Motion.Listener() { // from class: dev.colorcc.Panel.Disc.2
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Disc.this.m109lambda$new$0$devcolorccPanel$Disc(f, f2);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$0$dev-colorcc-Panel$Disc, reason: not valid java name */
        /* synthetic */ void m109lambda$new$0$devcolorccPanel$Disc(float f, float f2) {
            this.glass.setLevel(f);
            tint(f);
        }

        void accent(int r1) {
            this.accent = r1;
            this.lastTint = 0;
            tint(this.level.value());
        }

        void style(String str) {
            this.effect = Logic.effect(str);
            this.accent = Panel.this.iconMode == 0 ? Logic.accent(str, Panel.ACTIVE_BLUE) : Panel.this.theme(Panel.ACTIVE_BLUE);
            this.lastTint = 0;
            tint(this.level.value());
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void land() {
            from(View.TRANSLATION_X, Logic.Spring.BOUNCY, -this.iconSize, 0.0f);
            from(View.TRANSLATION_Y, Logic.Spring.BOUNCY, this.iconSize, 0.0f);
            Panel.this.spring(this.icon, View.ALPHA, Logic.Spring.SNAPPY).to(1.0f);
        }

        private void play(boolean z) {
            removeCallbacks(this.land);
            ImageView imageView = this.icon;
            imageView.setPivotX(imageView.getWidth() / 2.0f);
            ImageView imageView2 = this.icon;
            imageView2.setPivotY(imageView2.getHeight() / 2.0f);
            float f = this.iconSize;
            switch (this.effect) {
                case 1:
                    from(View.ALPHA, Logic.Spring.WOBBLE, 0.25f, 1.0f);
                    break;
                case 2:
                    Panel.this.spring(this.icon, View.TRANSLATION_X, Logic.Spring.SNAPPY).to(f);
                    Panel.this.spring(this.icon, View.TRANSLATION_Y, Logic.Spring.SNAPPY).to(-f);
                    Panel.this.spring(this.icon, View.ALPHA, Logic.Spring.SNAPPY).to(0.0f);
                    postDelayed(this.land, 150L);
                    break;
                case 3:
                    if (z) {
                        from(View.ROTATION, Logic.Spring.WOBBLE, -90.0f, 0.0f);
                    } else {
                        push(View.ROTATION, Logic.Spring.WOBBLE, 700.0f);
                    }
                    break;
                case Panel.TILES_PER_ROW /* 4 */:
                    from(View.TRANSLATION_Y, Logic.Spring.WOBBLE, f * (-0.45f), 0.0f);
                    break;
                case 5:
                    push(View.ROTATION, Logic.Spring.SWING, 675.0f);
                    break;
                case 6:
                    push(View.SCALE_X, Logic.Spring.BOUNCY, 10.0f);
                    push(View.SCALE_Y, Logic.Spring.BOUNCY, 10.0f);
                    break;
                case 7:
                    ImageView imageView3 = this.icon;
                    imageView3.setPivotY(imageView3.getHeight());
                    from(View.SCALE_X, Logic.Spring.BOUNCY, 0.4f, 1.0f);
                    from(View.SCALE_Y, Logic.Spring.BOUNCY, 0.4f, 1.0f);
                    break;
                case 8:
                    from(View.ROTATION_Y, Logic.Spring.BOUNCY, z ? -360.0f : 360.0f, 0.0f);
                    break;
                case Panel.RINGER_INSET /* 9 */:
                    push(View.TRANSLATION_Y, Logic.Spring.WOBBLE, f * (-5.0f));
                    break;
                case 10:
                    from(View.ROTATION, Logic.Spring.BOUNCY, z ? -360.0f : 360.0f, 0.0f);
                    break;
                case 11:
                    this.icon.setPivotX(0.0f);
                    from(View.SCALE_X, Logic.Spring.BOUNCY, Panel.MEDIA_EDGE_LIGHT, 1.0f);
                    break;
                case 12:
                    push(View.SCALE_X, Logic.Spring.BOUNCY, -11.0f);
                    push(View.SCALE_Y, Logic.Spring.BOUNCY, -11.0f);
                    break;
                case 13:
                    push(View.TRANSLATION_X, Logic.Spring.JITTER, f * 4.5f);
                    break;
                case 14:
                    push(View.TRANSLATION_Y, Logic.Spring.BOUNCY, f * (-12.0f));
                    break;
                default:
                    from(View.SCALE_X, Logic.Spring.BOUNCY, 0.6f, 1.0f);
                    from(View.SCALE_Y, Logic.Spring.BOUNCY, 0.6f, 1.0f);
                    break;
            }
        }

        /* JADX WARN: Multi-variable type inference failed */
        private boolean animateIcon(Object obj) {
            long jRound;
            Drawable frame;
            try {
                Object objCallMethod = ReflectUtils.callMethod(obj, "getDrawable", new Object[]{getContext()});
                if (!(objCallMethod instanceof Animatable)) {
                    return false;
                }
                Drawable drawableMutate = ((Drawable) objCallMethod).mutate();
                int r3 = this.iconSize * 2;
                if (drawableMutate instanceof AnimationDrawable) {
                    AnimationDrawable animationDrawable = (AnimationDrawable) drawableMutate;
                    animationDrawable.setOneShot(true);
                    jRound = 0;
                    for (int r0 = 0; r0 < animationDrawable.getNumberOfFrames(); r0++) {
                        jRound += (long) animationDrawable.getDuration(r0);
                    }
                    frame = animationDrawable.getFrame(animationDrawable.getNumberOfFrames() - 1);
                } else {
                    try {
                        jRound = Math.round(((Float) ReflectUtils.callMethod(ReflectUtils.callMethod(drawableMutate, "getComposition", new Object[0]), "getDuration", new Object[0])).floatValue());
                    } catch (Throwable unused) {
                        jRound = 800;
                    }
                    try {
                        Drawable drawableMutate2 = ((Drawable) ReflectUtils.callMethod(obj, "getDrawable", new Object[]{getContext()})).mutate();
                        toLastFrame(drawableMutate2);
                        frame = drawableMutate2;
                    } catch (Throwable unused2) {
                        frame = drawableMutate;
                    }
                }
                int[] r11 = glyphBox(frame, r3);
                if (r11 == null) {
                    return false;
                }
                final Cropped cropped = new Cropped(drawableMutate, r11, r3);
                this.icon.setImageDrawable(cropped);
                ((Animatable) drawableMutate).start();
                Runnable runnable = this.settleIcon;
                if (runnable != null) {
                    removeCallbacks(runnable);
                }
                Runnable runnable2 = new Runnable() { // from class: dev.colorcc.Panel.Disc.0
                    @Override // java.lang.Runnable
                    public final void run() {
                        Disc.this.m108lambda$animateIcon$2$devcolorccPanel$Disc(cropped);
                    }
                };
                this.settleIcon = runnable2;
                postDelayed(runnable2, jRound + 50);
                return true;
            } catch (Throwable unused3) {
                return false;
            }
        }

        /* JADX INFO: renamed from: lambda$animateIcon$2$dev-colorcc-Panel$Disc, reason: not valid java name */
        /* synthetic */ void m108lambda$animateIcon$2$devcolorccPanel$Disc(Cropped cropped) {
            this.settleIcon = null;
            if (this.icon.getDrawable() != cropped) {
                return;
            }
            final Object[] objArr = this.pendingStill;
            this.pendingStill = null;
            if (objArr != null) {
                Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Disc.4
                    @Override // java.lang.Runnable
                    public final void run() {
                        Disc.this.m107lambda$animateIcon$1$devcolorccPanel$Disc(objArr);
                    }
                });
            } else {
                this.icon.setImageDrawable(this.stillShown);
            }
        }

        /* JADX INFO: renamed from: lambda$animateIcon$1$dev-colorcc-Panel$Disc, reason: not valid java name */
        /* synthetic */ void m107lambda$animateIcon$1$devcolorccPanel$Disc(Object[] objArr) {
            showIcon(objArr[0], objArr[1]);
        }

        /* JADX WARN: Multi-variable type inference failed */
        private void toLastFrame(Drawable drawable) {
            if (drawable instanceof AnimationDrawable) {
                AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
                animationDrawable.selectDrawable(animationDrawable.getNumberOfFrames() - 1);
                return;
            }
            if (drawable instanceof Animatable) {
                try {
                    try {
                        ReflectUtils.callMethod(drawable, "setProgress", new Object[]{Float.valueOf(1.0f)});
                    } catch (Throwable unused) {
                        ReflectUtils.callMethod(drawable, "forceAnimationOnUI", new Object[0]);
                        Animatable animatable = (Animatable) drawable;
                        animatable.start();
                        animatable.stop();
                    }
                } catch (Throwable unused2) {
                    Animatable animatable2 = (Animatable) drawable;
                    animatable2.start();
                    animatable2.stop();
                }
            }
        }

        private int[] glyphBox(Drawable drawable, int r4) {
            Bitmap bitmapCreateBitmap = Bitmap.createBitmap(r4, r4, Bitmap.Config.ARGB_8888);
            drawable.setBounds(0, 0, r4, r4);
            drawable.draw(new Canvas(bitmapCreateBitmap));
            Bitmap bitmapExtractAlpha = bitmapCreateBitmap.extractAlpha();
            bitmapCreateBitmap.recycle();
            byte[] bArr = new byte[bitmapExtractAlpha.getRowBytes() * bitmapExtractAlpha.getHeight()];
            bitmapExtractAlpha.copyPixelsToBuffer(ByteBuffer.wrap(bArr));
            int[] r2 = Logic.alphaBounds(bArr, bitmapExtractAlpha.getRowBytes(), bitmapExtractAlpha.getWidth(), bitmapExtractAlpha.getHeight());
            bitmapExtractAlpha.recycle();
            return r2;
        }

        private void from(Property<View, Float> property, float[] fArr, float f, float f2) {
            Motion.Value valueSpring = Panel.this.spring(this.icon, property, fArr);
            valueSpring.snap(f);
            valueSpring.to(f2);
        }

        private void push(Property<View, Float> property, float[] fArr, float f) {
            Panel.this.spring(this.icon, property, fArr).kick(f);
        }

        private void tint(float f) {
            int r4 = ((Integer) Panel.this.argb.evaluate(Logic.clamp(f / Panel.ICON_COVERED), -1, Integer.valueOf(this.accent))).intValue();
            if (r4 == this.lastTint) {
                return;
            }
            this.lastTint = r4;
            this.icon.setImageTintList(ColorStateList.valueOf(r4));
        }

        private Bitmap maskOf(Drawable drawable) {
            Panel.access$9308(Panel.this);
            try {
                int r5 = this.iconSize * 2;
                Bitmap bitmapCreateBitmap = Bitmap.createBitmap(r5, r5, Bitmap.Config.ARGB_8888);
                drawable.setBounds(0, 0, r5, r5);
                drawable.draw(new Canvas(bitmapCreateBitmap));
                Bitmap bitmapExtractAlpha = bitmapCreateBitmap.extractAlpha();
                bitmapCreateBitmap.recycle();
                byte[] bArr = new byte[bitmapExtractAlpha.getRowBytes() * bitmapExtractAlpha.getHeight()];
                bitmapExtractAlpha.copyPixelsToBuffer(ByteBuffer.wrap(bArr));
                int[] r6 = Logic.alphaBounds(bArr, bitmapExtractAlpha.getRowBytes(), bitmapExtractAlpha.getWidth(), bitmapExtractAlpha.getHeight());
                if (r6 == null) {
                    return bitmapExtractAlpha;
                }
                int r1 = r6[0];
                int r2 = r6[1];
                return Bitmap.createBitmap(bitmapExtractAlpha, r1, r2, r6[2] - r1, r6[3] - r2);
            } catch (Throwable th) {
                ColorCCBridge.log("icon mask failed: " + th);
                return null;
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* JADX WARN: Code duplicated, block: B:52:0x0094  */
        /* JADX WARN: Code duplicated, block: B:56:0x009e  */
        /* JADX WARN: Code duplicated, block: B:59:0x00a3 A[DONT_INVERT] */
        /* JADX WARN: Code duplicated, block: B:61:0x00a6  */
        public void render(Object obj, boolean z) {
            boolean z2;
            int r11;
            Object obj2;
            int r0 = Panel.stateOf(obj);
            Object objIconOf = Panel.iconOf(obj);
            Object objIconSource = objIconOf == null ? null : Panel.iconSource(objIconOf);
            boolean z3 = (!Panel.this.interactive || this.shownState == -1 || this.effect != 13 || objIconOf == null || (obj2 = this.lastIcon) == null || objIconSource.equals(obj2)) ? false : true;
            if (objIconOf != null && !objIconSource.equals(this.lastIcon)) {
                if (!z && this.lastIcon != null && Panel.this.settleWorkPending) {
                    if (this.lateIcon == null) {
                        Panel.this.lateIcons.add(this);
                    }
                    this.lateIcon = objIconOf;
                } else {
                    this.lateIcon = null;
                    showIcon(objIconOf, objIconSource);
                }
            }
            float f = r0 == 2 ? 1.0f : 0.0f;
            Motion.Value value = this.level;
            if (z) {
                value.to(f);
            } else {
                value.snap(f);
            }
            tint(this.level.value());
            this.ownAnimation = false;
            if (z3) {
                if (objIconOf == null && animateIcon(objIconOf)) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                this.ownAnimation = z2;
                if (!z2) {
                    play(r0 == 2);
                }
            } else if (z && (r11 = this.shownState) != -1) {
                if ((r0 == 2) != (r11 == 2)) {
                    if (objIconOf == null) {
                        z2 = false;
                    } else {
                        z2 = false;
                    }
                    this.ownAnimation = z2;
                    if (!z2) {
                        play(r0 == 2);
                    }
                }
            }
            this.shownState = r0;
            setTransitionAlpha(r0 == 0 ? 0.45f : 1.0f);
        }

        void showLateIcon() {
            final Object obj = this.lateIcon;
            if (obj == null) {
                return;
            }
            this.lateIcon = null;
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Disc.3
                @Override // java.lang.Runnable
                public final void run() {
                    Disc.this.m110lambda$showLateIcon$3$devcolorccPanel$Disc(obj);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$showLateIcon$3$dev-colorcc-Panel$Disc, reason: not valid java name */
        /* synthetic */ void m110lambda$showLateIcon$3$devcolorccPanel$Disc(Object obj) {
            showIcon(obj, Panel.iconSource(obj));
        }

        private void showIcon(Object obj, Object obj2) {
            this.lastIcon = obj2;
            if (this.settleIcon == null) {
                Drawable drawableStillDrawable = Panel.this.stillDrawable(obj);
                if (drawableStillDrawable == null) {
                    drawableStillDrawable = (Drawable) ReflectUtils.callMethod(obj, "getDrawable", new Object[]{getContext()});
                }
                Drawable.ConstantState constantState = (drawableStillDrawable == null || (drawableStillDrawable instanceof Animatable) || (drawableStillDrawable instanceof DrawableContainer)) ? null : drawableStillDrawable.getConstantState();
                Bitmap bitmap = constantState != null ? (Bitmap) Panel.this.masks.get(constantState) : null;
                if (bitmap != null) {
                    this.icon.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
                    this.stillShown = this.icon.getDrawable();
                    return;
                }
                if (drawableStillDrawable != null) {
                    Drawable drawableMutate = drawableStillDrawable.mutate();
                    toLastFrame(drawableMutate);
                    Bitmap bitmapMaskOf = maskOf(drawableMutate);
                    if (bitmapMaskOf != null && constantState != null) {
                        Panel.this.masks.put(constantState, bitmapMaskOf);
                    }
                    ImageView imageView = this.icon;
                    if (bitmapMaskOf != null) {
                        drawableMutate = new BitmapDrawable(getResources(), bitmapMaskOf);
                    }
                    imageView.setImageDrawable(drawableMutate);
                    this.stillShown = this.icon.getDrawable();
                    return;
                }
                return;
            }
            this.pendingStill = new Object[]{obj, obj2};
        }
    }

    private final class Card extends LinearLayout {
        private Body body;
        final Disc disc;
        final Glass glass;
        private int lastState;
        final TextView sub;
        private final LinearLayout texts;
        final TextView title;

        Card(Context context) {
            super(context);
            this.lastState = -1;
            Glass glass = new Glass(-1.0f, Panel.GLASS_IDLE, Panel.GLASS_IDLE, 0.18f, 0.5f);
            this.glass = glass;
            setBackground(glass);
            setOrientation(0);
            setGravity(16);
            setPadding(Panel.this.dp(10.0f), 0, Panel.this.dp(15.0f), 0);
            setClickable(true);
            Disc disc = Panel.this.new Disc(context, Panel.this.dp(Panel.ARC));
            this.disc = disc;
            disc.setClickable(false);
            addView(disc, new LinearLayout.LayoutParams(Panel.this.dp(42.0f), Panel.this.dp(42.0f)));
            LinearLayout linearLayout = new LinearLayout(context);
            this.texts = linearLayout;
            linearLayout.setOrientation(1);
            TextView textViewLabel = Panel.this.label(context, 12.5f, -1, 2);
            this.title = textViewLabel;
            TextView textViewLabel2 = Panel.this.label(context, 10.5f, -1275068417, 1);
            this.sub = textViewLabel2;
            textViewLabel.setIncludeFontPadding(false);
            textViewLabel2.setIncludeFontPadding(false);
            textViewLabel2.setVisibility(8);
            linearLayout.addView(textViewLabel);
            linearLayout.addView(textViewLabel2);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, -2, 1.0f);
            layoutParams.leftMargin = Panel.this.dp(9.0f);
            addView(linearLayout, layoutParams);
        }

        void shape(int r6, int r7) {
            int r3;
            int r8;
            boolean z = r6 == 1;
            int r1 = (z || r7 <= 1) ? 0 : 1;
            this.texts.setVisibility(z ? 8 : 0);
            setBackground(z ? null : this.glass);
            setOrientation(r1);
            setGravity(r1 != 0 ? 8388627 : 16);
            float f = Panel.DROP_HEADER;
            if (z) {
                r3 = 0;
            } else {
                r3 = Panel.this.dp(r1 != 0 ? 16.0f : 10.0f);
            }
            if (z) {
                r8 = 0;
            } else {
                Panel panel = Panel.this;
                if (r1 == 0) {
                    f = 15.0f;
                }
                r8 = panel.dp(f);
            }
            setPadding(r3, 0, r8, 0);
            int r9 = z ? -1 : Panel.this.dp(42.0f);
            this.disc.setLayoutParams(new LinearLayout.LayoutParams(r9, r9));
            LinearLayout.LayoutParams layoutParams = r1 != 0 ? new LinearLayout.LayoutParams(-1, -2) : new LinearLayout.LayoutParams(0, -2, 1.0f);
            Panel panel2 = Panel.this;
            if (r1 != 0) {
                layoutParams.topMargin = panel2.dp(10.0f);
            } else {
                layoutParams.leftMargin = panel2.dp(9.0f);
            }
            this.texts.setLayoutParams(layoutParams);
        }

        void bind(final Object obj) {
            setVisibility(0);
            this.disc.setClickable(true);
            this.disc.setOnClickListener(new View.OnClickListener() {
                @Override
                public final void onClick(View view2) {
                    Panel.this.m90lambda$wireTile$79$devcolorccPanel(obj, Card.this);
                }
            });
            this.disc.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public final boolean onLongClick(View view2) {
                    return Panel.this.m93lambda$wireTile$82$devcolorccPanel(obj, Card.this);
                }
            });
            setOnClickListener(new View.OnClickListener() {
                @Override
                public final void onClick(View view2) {
                    if (Card.this.texts.getVisibility() == 0
                            && Logic.menu(String.valueOf(ReflectUtils.callMethod(obj, "getTileSpec", new Object[0]))) != 0) {
                        Panel.this.m93lambda$wireTile$82$devcolorccPanel(obj, Card.this);
                    } else {
                        Panel.this.m90lambda$wireTile$79$devcolorccPanel(obj, Card.this);
                    }
                }
            });
            setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public final boolean onLongClick(View view2) {
                    return Panel.this.m93lambda$wireTile$82$devcolorccPanel(obj, Card.this);
                }
            });
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Card.0
                @Override // java.lang.Runnable
                public final void run() {
                    Card.this.m105lambda$bind$0$devcolorccPanel$Card(obj);
                }
            });
            this.lastState = -1;
            Panel.this.bindTile(obj, new Consumer() { // from class: dev.colorcc.Panel.Card.1
                @Override // java.util.function.Consumer
                public final void accept(Object obj2) {
                    Card.this.m106lambda$bind$1$devcolorccPanel$Card(obj2);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$bind$0$dev-colorcc-Panel$Card, reason: not valid java name */
        /* synthetic */ void m105lambda$bind$0$devcolorccPanel$Card(Object obj) {
            this.disc.style((String) ReflectUtils.callMethod(obj, "getTileSpec", new Object[0]));
        }

        /* JADX INFO: renamed from: lambda$bind$1$dev-colorcc-Panel$Card, reason: not valid java name */
        /* synthetic */ void m106lambda$bind$1$devcolorccPanel$Card(Object obj) {
            int r0 = Panel.stateOf(obj);
            int r1 = this.lastState;
            boolean z = (r1 == -1 || r1 == r0) ? false : true;
            this.lastState = r0;
            Panel.setTextIfChanged(this.title, Panel.text(obj, "label"));
            Panel.setTextIfChanged(this.sub, Panel.text(obj, "secondaryLabel"));
            TextView textView = this.sub;
            textView.setVisibility(TextUtils.isEmpty(textView.getText()) ? 8 : 0);
            boolean z2 = z && Panel.this.interactive;
            this.disc.render(obj, z2);
            if (z2) {
                if (this.body == null) {
                    this.body = Panel.this.bodyOf(this);
                }
                if (this.body != null && !this.disc.ownAnimation) {
                    this.body.press.kick(Panel.TOGGLE_KICK);
                }
            }
            setContentDescription(Panel.text(obj, "label"));
            setStateDescription(Panel.this.word(r0 == 2 ? "on" : "off", new Object[0]));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Body bodyOf(View view) {
        for (Body body : this.staticBodies) {
            if (body.view == view) {
                return body;
            }
        }
        return null;
    }

    private final class RingerPill extends FrameLayout {
        private static final int SLOTS = 3;
        private final int[] activeColors;
        private final RingerGlyph[] cells;
        private float downX;
        private final Motion.Value position;
        private boolean sliding;
        private float step;
        private final View thumb;
        private int under;

        RingerPill(Context context) {
            super(context);
            this.cells = new RingerGlyph[SLOTS];
            this.activeColors = new int[]{Panel.ACTIVE_RED, Panel.ACTIVE_RED, Panel.ACTIVE_RED};
            this.under = -1;
            setBackground(new Glass(-1.0f, Panel.GLASS_IDLE, Panel.GLASS_IDLE, 0.5f, 0.5f));
            View view = new View(context);
            this.thumb = view;
            view.setBackground(new Glass(-1.0f, Panel.GLASS_DISC_ON, Panel.GLASS_DISC_ON, 0.5f, 0.5f));
            addView(view);
            String[] strArr = {Panel.this.word("sound", new Object[0]), Panel.this.word("vibrate", new Object[0]), Panel.this.word("silent", new Object[0])};
            for (int r3 = 0; r3 < SLOTS; r3++) {
                final int slotIdx = r3;
                RingerGlyph ringerGlyph = new RingerGlyph(context, Panel.this.motion, r3, Panel.this.dp(21.0f));
                ringerGlyph.setContentDescription(strArr[r3]);
                ringerGlyph.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.RingerPill.1
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view2) {
                        RingerPill.this.m195lambda$new$1$devcolorccPanel$RingerPill(slotIdx, view2);
                    }
                });
                this.cells[r3] = ringerGlyph;
                addView(ringerGlyph, new FrameLayout.LayoutParams(0, 0));
            }
            this.position = Panel.this.motion.value(Logic.Spring.BOUNCY, new Motion.Listener() { // from class: dev.colorcc.Panel.RingerPill.2
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    RingerPill.this.m196lambda$new$2$devcolorccPanel$RingerPill(f, f2);
                }
            });
            m196lambda$new$2$devcolorccPanel$RingerPill(0.0f, 0.0f);
        }

        /* JADX INFO: renamed from: lambda$new$0$dev-colorcc-Panel$RingerPill, reason: not valid java name */
        /* synthetic */ void m194lambda$new$0$devcolorccPanel$RingerPill(int r1) {
            Panel.this.onRingerTap(r1);
        }

        /* JADX INFO: renamed from: lambda$new$1$dev-colorcc-Panel$RingerPill, reason: not valid java name */
        /* synthetic */ void m195lambda$new$1$devcolorccPanel$RingerPill(final int r2, View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.RingerPill.3
                @Override // java.lang.Runnable
                public final void run() {
                    RingerPill.this.m194lambda$new$0$devcolorccPanel$RingerPill(r2);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* JADX INFO: renamed from: render, reason: merged with bridge method [inline-methods] */
        public void m196lambda$new$2$devcolorccPanel$RingerPill(float f, float f2) {
            float fMin = Math.min(Math.abs(f2) / Panel.DROP_STEP, 1.0f);
            this.thumb.setScaleX((fMin * 0.5f) + 1.0f);
            this.thumb.setScaleY(1.0f - (fMin * Panel.PLAYER_TINT));
            this.thumb.setTranslationX(this.step * f);
            for (int r1 = 0; r1 < SLOTS; r1++) {
                float fClamp = Logic.clamp(1.0f - Math.abs(f - r1));
                this.cells[r1].setColor(((Integer) Panel.this.argb.evaluate(fClamp, Integer.valueOf(Panel.RINGER_IDLE), Integer.valueOf(this.activeColors[r1]))).intValue());
                this.cells[r1].setSelected(fClamp >= 0.5f);
            }
        }

        void select(int r2, boolean z) {
            Motion.Value value = this.position;
            if (z) {
                value.to(r2);
                this.cells[r2].play();
            } else {
                value.snap(r2);
            }
        }

        private float slotAt(float f) {
            float width = this.thumb.getWidth() / 2.0f;
            if (this.step <= 0.0f) {
                return 0.0f;
            }
            return Math.max(0.0f, Math.min(2.0f, ((f - Panel.this.dp(9.0f)) - width) / this.step));
        }

        @Override // android.view.ViewGroup
        public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 0) {
                this.downX = motionEvent.getX();
                this.sliding = false;
                getParent().requestDisallowInterceptTouchEvent(true);
            } else if (actionMasked == 2 && !this.sliding && Math.abs(motionEvent.getX() - this.downX) > Panel.this.touchSlop) {
                this.sliding = true;
                this.under = Panel.this.shownRingerSlot;
            }
            return this.sliding;
        }

        @Override // android.view.View
        public boolean onTouchEvent(MotionEvent motionEvent) {
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 0) {
                this.downX = motionEvent.getX();
                return true;
            }
            if (!this.sliding) {
                if (actionMasked != 2 || Math.abs(motionEvent.getX() - this.downX) <= Panel.this.touchSlop) {
                    return true;
                }
                this.sliding = true;
                this.under = Panel.this.shownRingerSlot;
            }
            float fSlotAt = slotAt(motionEvent.getX());
            if (actionMasked == 2) {
                this.position.to(fSlotAt);
                int r6 = Math.round(fSlotAt);
                if (r6 != this.under) {
                    this.under = r6;
                    performHapticFeedback(Panel.TILES_PER_ROW);
                }
                return true;
            }
            if (actionMasked == 1 || actionMasked == SLOTS) {
                this.sliding = false;
                final int r7 = Math.round(fSlotAt);
                if (r7 != Panel.this.shownRingerSlot) {
                    Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.RingerPill.0
                        @Override // java.lang.Runnable
                        public final void run() {
                            RingerPill.this.m197lambda$onTouchEvent$3$devcolorccPanel$RingerPill(r7);
                        }
                    });
                } else {
                    this.position.to(r7);
                }
            }
            return true;
        }

        /* JADX INFO: renamed from: lambda$onTouchEvent$3$dev-colorcc-Panel$RingerPill, reason: not valid java name */
        /* synthetic */ void m197lambda$onTouchEvent$3$devcolorccPanel$RingerPill(int r1) {
            Panel.this.onRingerTap(r1);
        }

        @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
        protected void onLayout(boolean z, int r8, int r9, int r10, int r11) {
            super.onLayout(z, r8, r9, r10, r11);
            int r12 = r10 - r8;
            int r13 = r11 - r9;
            int r7 = Panel.this.dp(9.0f);
            int r14 = r7 * 2;
            int r15 = Math.max(0, r13 - r14);
            this.step = Math.max(0, (r12 - r14) - r15) / 2.0f;
            int r2 = r7 + r15;
            this.thumb.layout(r7, r7, r2, r2);
            for (int r16 = 0; r16 < SLOTS; r16++) {
                float f = r15;
                float f2 = this.step;
                float f3 = r7 + (f / 2.0f) + (r16 * f2);
                int r3 = Math.round(Math.max(f, f2) / 2.0f);
                this.cells[r16].layout(Math.max(0, Math.round(f3) - r3), 0, Math.min(r12, Math.round(f3) + r3), r13);
            }
            m196lambda$new$2$devcolorccPanel$RingerPill(this.position.value(), 0.0f);
        }
    }

    private final class MediaCard extends LinearLayout {
        private final ImageView art;
        private final TextView artist;
        private final ImageView next;
        private final Drawable noteIcon;
        private final Drawable pauseIcon;
        private final ImageView play;
        private final Drawable playIcon;
        private final Motion.Value playPop;
        private boolean playing;
        private final ImageView prev;
        private CharSequence shownTitle;
        private final ArtTint tint;
        private final TextView title;

        MediaCard(Context context) {
            super(context);
            Drawable drawableOf = Icons.of("M8,5v14l11,-7z");
            this.playIcon = drawableOf;
            this.pauseIcon = Icons.of("M6,19h4V5H6v14zM14,5v14h4V5h-4z");
            this.noteIcon = Icons.of("M12,3v10.55c-0.59,-0.34 -1.27,-0.55 -2,-0.55 -2.21,0 -4,1.79 -4,4s1.79,4 4,4 4,-1.79 4,-4V7h4V3h-6z");
            ArtTint artTint = new ArtTint(Panel.this, Panel.this.dp(34.0f));
            this.tint = artTint;
            setOrientation(1);
            Glass glass = new Glass(Panel.this.dp(34.0f), Panel.GLASS_IDLE, Panel.GLASS_IDLE, 0.5f, 0.5f);
            glass.setUnderlay(artTint);
            setBackground(glass);
            setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.MediaCard.0
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MediaCard.this.m163lambda$new$1$devcolorccPanel$MediaCard(view);
                }
            });
            setPadding(Panel.this.dp(14.0f), Panel.this.dp(14.0f), Panel.this.dp(14.0f), Panel.this.dp(6.0f));
            ImageView imageView = new ImageView(context);
            this.art = imageView;
            imageView.setClipToOutline(true);
            imageView.setOutlineProvider(new ViewOutlineProvider() { // from class: dev.colorcc.Panel.MediaCard.1
                @Override // android.view.ViewOutlineProvider
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), Panel.this.dp(10.0f));
                }
            });
            ImageView imageView2 = new ImageView(context);
            imageView2.setImageDrawable(Icons.of("M21,3H3C1.9,3 1,3.9 1,5v3h2V5h18v14h-7v2h7c1.1,0 2,-0.9 2,-2V5C23,3.9 22.1,3 21,3zM1,18v3h3C4,19.34 2.66,18 1,18zM1,14v2c2.76,0 5,2.24 5,5h2C8,17.13 4.87,14 1,14zM1,10v2c4.97,0 9,4.03 9,9h2C12,14.92 7.07,10 1,10z"));
            imageView2.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView2.setPadding(Panel.this.dp(3.0f), Panel.this.dp(3.0f), Panel.this.dp(3.0f), Panel.this.dp(3.0f));
            imageView2.setContentDescription(Panel.this.word("sound_output", new Object[0]));
            imageView2.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.MediaCard.2
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MediaCard.this.m166lambda$new$3$devcolorccPanel$MediaCard(view);
                }
            });
            Panel.this.squish(imageView2);
            LinearLayout linearLayoutHorizontal = Panel.horizontal(context);
            linearLayoutHorizontal.addView(imageView, new LinearLayout.LayoutParams(Panel.this.dp(42.0f), Panel.this.dp(42.0f)));
            linearLayoutHorizontal.addView(new Space(context), new LinearLayout.LayoutParams(0, 0, 1.0f));
            linearLayoutHorizontal.addView(imageView2, new LinearLayout.LayoutParams(Panel.this.dp(28.0f), Panel.this.dp(28.0f)));
            addView(linearLayoutHorizontal, new LinearLayout.LayoutParams(-1, Panel.this.dp(42.0f)));
            addView(new Space(context), new LinearLayout.LayoutParams(0, 0, 1.0f));
            TextView textViewLabel = Panel.this.label(context, 13.0f, -1, 1);
            this.title = textViewLabel;
            TextView textViewLabel2 = Panel.this.label(context, 10.0f, -855638017, 1);
            this.artist = textViewLabel2;
            addView(textViewLabel, new LinearLayout.LayoutParams(-1, -2));
            addView(textViewLabel2, new LinearLayout.LayoutParams(-1, -2));
            LinearLayout linearLayoutHorizontal2 = Panel.horizontal(context);
            ImageView imageViewControl = control(context, Icons.of("M6,6h2v12H6zM9.5,12l8.5,6V6z"), Panel.this.word("previous", new Object[0]));
            this.prev = imageViewControl;
            ImageView imageViewControl2 = control(context, drawableOf, Panel.this.word("play", new Object[0]));
            this.play = imageViewControl2;
            ImageView imageViewControl3 = control(context, Icons.of("M6,18l8.5,-6L6,6v12zM16,6v12h2V6z"), Panel.this.word("next", new Object[0]));
            this.next = imageViewControl3;
            linearLayoutHorizontal2.addView(imageViewControl, new LinearLayout.LayoutParams(0, Panel.this.dp(Panel.DROP_TOP), 1.0f));
            linearLayoutHorizontal2.addView(imageViewControl2, new LinearLayout.LayoutParams(0, Panel.this.dp(Panel.DROP_TOP), 1.0f));
            linearLayoutHorizontal2.addView(imageViewControl3, new LinearLayout.LayoutParams(0, Panel.this.dp(Panel.DROP_TOP), 1.0f));
            addView(linearLayoutHorizontal2, new LinearLayout.LayoutParams(-1, Panel.this.dp(Panel.DROP_TOP)));
            this.playPop = Panel.this.motion.value(Logic.Spring.BOUNCY, new Motion.Listener() { // from class: dev.colorcc.Panel.MediaCard.3
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    MediaCard.this.m167lambda$new$4$devcolorccPanel$MediaCard(f, f2);
                }
            });
            imageViewControl.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.MediaCard.4
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MediaCard.this.m168lambda$new$6$devcolorccPanel$MediaCard(view);
                }
            });
            imageViewControl3.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.MediaCard.5
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MediaCard.this.m169lambda$new$8$devcolorccPanel$MediaCard(view);
                }
            });
            imageViewControl2.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.MediaCard.6
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MediaCard.this.m164lambda$new$10$devcolorccPanel$MediaCard(view);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$0$dev-colorcc-Panel$MediaCard, reason: not valid java name */
        /* synthetic */ void m162lambda$new$0$devcolorccPanel$MediaCard() {
            Panel.this.player.open();
        }

        /* JADX INFO: renamed from: lambda$new$1$dev-colorcc-Panel$MediaCard, reason: not valid java name */
        /* synthetic */ void m163lambda$new$1$devcolorccPanel$MediaCard(View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.MediaCard.7
                @Override // java.lang.Runnable
                public final void run() {
                    MediaCard.this.m162lambda$new$0$devcolorccPanel$MediaCard();
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$2$dev-colorcc-Panel$MediaCard, reason: not valid java name */
        /* synthetic */ void m165lambda$new$2$devcolorccPanel$MediaCard(View view) {
            Panel.this.sheet.openOutput(view, Panel.this.controller == null ? null : Panel.this.controller.getPackageName());
        }

        /* JADX INFO: renamed from: lambda$new$3$dev-colorcc-Panel$MediaCard, reason: not valid java name */
        /* synthetic */ void m166lambda$new$3$devcolorccPanel$MediaCard(final View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.MediaCard.10
                @Override // java.lang.Runnable
                public final void run() {
                    MediaCard.this.m165lambda$new$2$devcolorccPanel$MediaCard(view);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$4$dev-colorcc-Panel$MediaCard, reason: not valid java name */
        /* synthetic */ void m167lambda$new$4$devcolorccPanel$MediaCard(float f, float f2) {
            float f3 = 1.0f - (f * 0.25f);
            this.play.setScaleX(f3);
            this.play.setScaleY(f3);
        }

        /* JADX INFO: renamed from: lambda$new$6$dev-colorcc-Panel$MediaCard, reason: not valid java name */
        /* synthetic */ void m168lambda$new$6$devcolorccPanel$MediaCard(View view) {
            Panel.this.skipped(-1);
            transport(new Consumer() { // from class: dev.colorcc.Panel.MediaCard.12
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((MediaController.TransportControls) obj).skipToPrevious();
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$8$dev-colorcc-Panel$MediaCard, reason: not valid java name */
        /* synthetic */ void m169lambda$new$8$devcolorccPanel$MediaCard(View view) {
            Panel.this.skipped(1);
            transport(new Consumer() { // from class: dev.colorcc.Panel.MediaCard.8
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((MediaController.TransportControls) obj).skipToNext();
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$10$dev-colorcc-Panel$MediaCard, reason: not valid java name */
        /* synthetic */ void m164lambda$new$10$devcolorccPanel$MediaCard(View view) {
            transport(new Consumer() { // from class: dev.colorcc.Panel.MediaCard.9
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    MediaCard.this.m170lambda$new$9$devcolorccPanel$MediaCard((MediaController.TransportControls) obj);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$9$dev-colorcc-Panel$MediaCard, reason: not valid java name */
        /* synthetic */ void m170lambda$new$9$devcolorccPanel$MediaCard(MediaController.TransportControls transportControls) {
            if (this.playing) {
                transportControls.pause();
            } else {
                transportControls.play();
            }
        }

        private ImageView control(Context context, Drawable drawable, String str) {
            ImageView imageView = new ImageView(context);
            imageView.setImageDrawable(drawable);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(Panel.this.dp(8.0f), Panel.this.dp(8.0f), Panel.this.dp(8.0f), Panel.this.dp(8.0f));
            imageView.setContentDescription(str);
            if (drawable != this.playIcon) {
                Panel.this.squish(imageView);
            }
            return imageView;
        }

        private void transport(final Consumer<MediaController.TransportControls> consumer) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.MediaCard.11
                @Override // java.lang.Runnable
                public final void run() {
                    MediaCard.this.m171lambda$transport$11$devcolorccPanel$MediaCard(consumer);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$transport$11$dev-colorcc-Panel$MediaCard, reason: not valid java name */
        /* synthetic */ void m171lambda$transport$11$devcolorccPanel$MediaCard(Consumer consumer) {
            if (Panel.this.controller != null) {
                consumer.accept(Panel.this.controller.getTransportControls());
            }
        }

        void render(CharSequence charSequence, CharSequence charSequence2, Bitmap bitmap, boolean z, boolean z2) {
            if (z != this.playing) {
                this.playPop.kick(9.0f);
            }
            this.playing = z;
            CharSequence charSequenceWord = TextUtils.isEmpty(charSequence) ? Panel.this.word("not_playing", new Object[0]) : charSequence;
            boolean zEquals = TextUtils.equals(charSequenceWord, this.shownTitle);
            this.shownTitle = charSequenceWord;
            this.title.setText(charSequenceWord);
            this.title.setTextColor(TextUtils.isEmpty(charSequence) ? -1711276033 : -1);
            this.artist.setText(charSequence2);
            this.artist.setVisibility(TextUtils.isEmpty(charSequence2) ? 8 : 0);
            ImageView imageView = this.art;
            if (bitmap == null) {
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                this.art.setPadding(Panel.this.dp(10.0f), Panel.this.dp(10.0f), Panel.this.dp(10.0f), Panel.this.dp(10.0f));
                this.art.setImageDrawable(this.noteIcon);
                this.art.setImageTintList(ColorStateList.valueOf(-1711276033));
            } else {
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                this.art.setPadding(0, 0, 0, 0);
                this.art.setImageTintList(null);
                this.art.setImageBitmap(bitmap);
            }
            this.tint.show(Panel.this.mediaTones);
            if (!zEquals && isShown()) {
                Panel.this.slideIn(this.title, this.artist);
                Panel.this.swellIn(this.art);
            }
            this.play.setImageDrawable(z ? this.pauseIcon : this.playIcon);
            this.play.setContentDescription(Panel.this.word(z ? "pause" : "play", new Object[0]));
            View[] viewArr = {this.prev, this.play, this.next};
            for (int r1 = 0; r1 < 3; r1++) {
                View view = viewArr[r1];
                view.setEnabled(z2);
                view.setAlpha(z2 ? 1.0f : 0.4f);
            }
        }
    }

    private final class ArtTint extends Drawable {
        private final RectF box;
        private final float corner;
        private final boolean even;
        private final Motion.Value fade;
        private int[] from;
        private float mix;
        private final Paint paint;
        private int[] to;

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int r1) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
        }

        /* JADX INFO: renamed from: lambda$new$0$dev-colorcc-Panel$ArtTint, reason: not valid java name */
        /* synthetic */ void m98lambda$new$0$devcolorccPanel$ArtTint(float f, float f2) {
            this.mix = Logic.clamp(f);
            invalidateSelf();
        }

        ArtTint(Panel panel, float f) {
            this(f, false);
        }

        ArtTint(float f, boolean z) {
            this.paint = new Paint(1);
            this.box = new RectF();
            this.from = new int[]{0, 0};
            this.to = new int[]{0, 0};
            this.mix = 1.0f;
            this.fade = Panel.this.motion.value(Logic.Spring.FLOOD, new Motion.Listener() { // from class: dev.colorcc.Panel.ArtTint.0
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f2, float f3) {
                    ArtTint.this.m98lambda$new$0$devcolorccPanel$ArtTint(f2, f3);
                }
            });
            this.corner = f;
            this.even = z;
        }

        void show(int[] r8) {
            int[] r9;
            if (r8 == null) {
                r9 = new int[]{0, 0};
            } else {
                r9 = new int[]{(r8[0] & 16777215) | (-268435456), (r8[1] & 16777215) | (-268435456)};
            }
            if (Arrays.equals(r9, this.to)) {
                return;
            }
            int[] r1 = current();
            for (int r3 = 0; r3 < 2; r3++) {
                if ((r1[r3] >>> 24) == 0) {
                    r1[r3] = r9[r3] & 16777215;
                }
                if ((r9[r3] >>> 24) == 0) {
                    r9[r3] = r1[r3] & 16777215;
                }
            }
            this.from = r1;
            this.to = r9;
            this.fade.snap(0.0f);
            this.fade.to(1.0f);
        }

        private int[] current() {
            return new int[]{((Integer) Panel.this.argb.evaluate(this.mix, Integer.valueOf(this.from[0]), Integer.valueOf(this.to[0]))).intValue(), ((Integer) Panel.this.argb.evaluate(this.mix, Integer.valueOf(this.from[1]), Integer.valueOf(this.to[1]))).intValue()};
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            int[] r0 = current();
            if ((r0[0] >>> 24) == 0 && (r0[1] >>> 24) == 0) {
                return;
            }
            this.box.set(getBounds());
            if (!this.even) {
                this.paint.setShader(new LinearGradient(0.0f, this.box.top, 0.0f, this.box.bottom, new int[]{faded(r0[0], 0.0f), faded(r0[0], Panel.MEDIA_EDGE_LIGHT), faded(r0[1], 0.7f), ((Integer) Panel.this.argb.evaluate(Panel.MEDIA_EDGE_LIGHT, Integer.valueOf(r0[1]), Integer.valueOf(r0[1] | 16777215))).intValue()}, Panel.MEDIA_TINT_STOPS, Shader.TileMode.CLAMP));
                RectF rectF = this.box;
                float f = this.corner;
                canvas.drawRoundRect(rectF, f, f, this.paint);
                return;
            }
            this.paint.setShader(new LinearGradient(0.0f, this.box.top, 0.0f, this.box.bottom, faded(r0[0], Panel.PLAYER_TINT), faded(r0[1], Panel.PLAYER_TINT), Shader.TileMode.CLAMP));
            RectF rectF2 = this.box;
            float f2 = this.corner;
            canvas.drawRoundRect(rectF2, f2, f2, this.paint);
        }

        private int faded(int r1, float f) {
            return (Math.round((r1 >>> 24) * f) << 24) | (r1 & 16777215);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void mediaTransport(final Consumer<MediaController.TransportControls> consumer) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.44
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m14lambda$mediaTransport$94$devcolorccPanel(consumer);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$mediaTransport$94$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m14lambda$mediaTransport$94$devcolorccPanel(Consumer consumer) {
        MediaController mediaController = this.controller;
        if (mediaController != null) {
            consumer.accept(mediaController.getTransportControls());
        }
    }

    private static int[] artTones(Bitmap bitmap) {
        try {
            if (bitmap.getConfig() == Bitmap.Config.HARDWARE) {
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
            }
            int[] r3 = new int[256];
            Bitmap.createScaledBitmap(bitmap, 16, 16, true).getPixels(r3, 0, 16, 0, 0, 16, 16);
            return Logic.artTones(r3, 16);
        } catch (Throwable th) {
            ColorCCBridge.log("artwork colours not read: " + th);
            return null;
        }
    }

    private final class Player extends FrameLayout {
        private final ImageView[] actions;
        private final ImageView art;
        private final TextView artist;
        private final int[] at;
        private final Rewind back;
        private final ImageView badge;
        private String badgeFor;
        private final LinearLayout card;
        private final FrameLayout cover;
        private final TextView elapsed;
        private float fromHeight;
        private float fromScale;
        private float fromX;
        private float fromY;
        private final ImageView ghost;
        private float grown;
        private final SeekLine loudness;
        private final ImageView next;
        private final Drawable noteIcon;
        private boolean opened;
        private final Drawable pauseIcon;
        private final ImageView play;
        private final Drawable playIcon;
        private boolean playing;
        private final ImageView prev;
        private final SeekLine seek;
        private int sentVolume;
        private final Drawable shade;
        private final Motion.Value show;
        private CharSequence shownTitle;
        private final Runnable tick;
        private final ArtTint tint;
        private final TextView title;
        private final TextView total;
        private int volumeSteps;

        /* JADX INFO: renamed from: lambda$new$0$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m176lambda$new$0$devcolorccPanel$Player() {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Player.23
                @Override // java.lang.Runnable
                public final void run() {
                    Player.this.showProgress();
                }
            });
        }

        Player(Context context) {
            super(context);
            this.sentVolume = -1;
            int r7 = 1;
            ArtTint artTint = Panel.this.new ArtTint(Panel.this.dp(34.0f), true);
            this.tint = artTint;
            this.playIcon = Icons.of("M8,5v14l11,-7z");
            this.pauseIcon = Icons.of("M6,19h4V5H6v14zM14,5v14h4V5h-4z");
            this.noteIcon = Icons.of("M12,3v10.55c-0.59,-0.34 -1.27,-0.55 -2,-0.55 -2.21,0 -4,1.79 -4,4s1.79,4 4,4 4,-1.79 4,-4V7h4V3h-6z");
            Drawable drawableScrim = Panel.scrim();
            this.shade = drawableScrim;
            this.tick = new Runnable() { // from class: dev.colorcc.Panel.Player.5
                @Override // java.lang.Runnable
                public final void run() {
                    Player.this.m176lambda$new$0$devcolorccPanel$Player();
                }
            };
            this.at = new int[2];
            this.fromScale = 1.0f;
            this.grown = 1.0f;
            this.actions = new ImageView[2];
            setVisibility(Panel.TILES_PER_ROW);
            setBackground(drawableScrim);
            setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Player.8
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    Player.this.m177lambda$new$1$devcolorccPanel$Player(view);
                }
            });
            LinearLayout linearLayout = new LinearLayout(context);
            this.card = linearLayout;
            linearLayout.setOrientation(1);
            linearLayout.setClickable(true);
            linearLayout.setPivotX(0.0f);
            linearLayout.setPivotY(0.0f);
            Glass glass = new Glass(Panel.this.dp(34.0f), Panel.GLASS_IDLE, Panel.GLASS_IDLE, 0.5f, 0.5f);
            glass.setUnderlay(artTint);
            linearLayout.setBackground(glass);
            linearLayout.setClipToOutline(true);
            linearLayout.setOutlineProvider(new ViewOutlineProvider() { // from class: dev.colorcc.Panel.Player.1
                @Override // android.view.ViewOutlineProvider
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), Math.max(1, Math.round(Player.this.fromHeight + ((view.getHeight() - Player.this.fromHeight) * Player.this.grown))), Panel.this.dp(34.0f) / (Player.this.fromScale + ((1.0f - Player.this.fromScale) * Player.this.grown)));
                }
            });
            FrameLayout frameLayout = new FrameLayout(context);
            this.cover = frameLayout;
            FadingImage fadingImage = new FadingImage(context);
            this.art = fadingImage;
            frameLayout.addView(fadingImage, new FrameLayout.LayoutParams(-1, -1));
            FadingImage fadingImage2 = new FadingImage(context);
            this.ghost = fadingImage2;
            fadingImage2.setAlpha(0.0f);
            frameLayout.addView(fadingImage2, new FrameLayout.LayoutParams(-1, -1));
            TextView textViewLabel = Panel.this.label(context, Panel.ARC, -1, 1);
            this.title = textViewLabel;
            textViewLabel.setTypeface(Typeface.DEFAULT_BOLD);
            textViewLabel.setGravity(1);
            textViewLabel.setShadowLayer(Panel.this.dp(6.0f), 0.0f, Panel.this.dp(1.0f), 1493172224);
            TextView textViewLabel2 = Panel.this.label(context, 15.0f, -637534209, 1);
            this.artist = textViewLabel2;
            textViewLabel2.setGravity(1);
            textViewLabel2.setShadowLayer(Panel.this.dp(6.0f), 0.0f, Panel.this.dp(1.0f), 1291845632);
            LinearLayout linearLayout2 = new LinearLayout(context);
            linearLayout2.setOrientation(1);
            linearLayout2.setPadding(Panel.this.dp(20.0f), 0, Panel.this.dp(20.0f), Panel.this.dp(4.0f));
            linearLayout2.addView(textViewLabel, new LinearLayout.LayoutParams(-1, -2));
            linearLayout2.addView(textViewLabel2, new LinearLayout.LayoutParams(-1, -2));
            frameLayout.addView(linearLayout2, new FrameLayout.LayoutParams(-1, -2, 80));
            ImageView imageView = new ImageView(context);
            this.badge = imageView;
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setOutlineProvider(new ViewOutlineProvider() { // from class: dev.colorcc.Panel.Player.2
                @Override // android.view.ViewOutlineProvider
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), Panel.this.dp(8.0f));
                }
            });
            imageView.setClipToOutline(true);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(Panel.this.dp(30.0f), Panel.this.dp(30.0f), 8388661);
            layoutParams.topMargin = Panel.this.dp(Panel.DROP_HEADER);
            layoutParams.rightMargin = Panel.this.dp(Panel.DROP_HEADER);
            frameLayout.addView(imageView, layoutParams);
            frameLayout.setOutlineProvider(new ViewOutlineProvider() { // from class: dev.colorcc.Panel.Player.3
                @Override // android.view.ViewOutlineProvider
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), Panel.this.dp(24.0f));
                }
            });
            frameLayout.setClipToOutline(true);
            frameLayout.setContentDescription(Panel.this.word("open_app", new Object[0]));
            frameLayout.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Player.9
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    Player.this.m186lambda$new$3$devcolorccPanel$Player(view);
                }
            });
            frameLayout.setOnTouchListener(new View.OnTouchListener() { // from class: dev.colorcc.Panel.Player.10
                @Override // android.view.View.OnTouchListener
                public final boolean onTouch(View view, MotionEvent motionEvent) {
                    return Player.this.m187lambda$new$4$devcolorccPanel$Player(view, motionEvent);
                }
            });
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setCornerRadius(Panel.this.dp(24.0f));
            gradientDrawable.setStroke(Math.max(1, Panel.this.dp(1.0f)), 1308622847);
            frameLayout.setForeground(gradientDrawable);
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, Panel.this.dp(224.0f));
            int r10 = Panel.this.dp(14.0f);
            layoutParams2.topMargin = r10;
            layoutParams2.rightMargin = r10;
            layoutParams2.leftMargin = r10;
            linearLayout.addView(frameLayout, layoutParams2);
            LinearLayout linearLayout3 = new LinearLayout(context);
            linearLayout3.setOrientation(1);
            linearLayout3.setPadding(Panel.this.dp(Panel.ARC), Panel.this.dp(8.0f), Panel.this.dp(Panel.ARC), Panel.this.dp(Panel.DROP_HEADER));
            TextView textViewLabel3 = Panel.this.label(context, 11.0f, -1711276033, 1);
            this.elapsed = textViewLabel3;
            TextView textViewLabel4 = Panel.this.label(context, 11.0f, -1711276033, 1);
            this.total = textViewLabel4;
            SeekLine seekLine = Panel.this.new SeekLine(context);
            this.seek = seekLine;
            seekLine.glow = true;
            seekLine.listener = new Slider.Listener() { // from class: dev.colorcc.Panel.Player.11
                @Override // dev.colorcc.Slider.Listener
                public final void onChange(float f, boolean z) {
                    Player.this.m189lambda$new$7$devcolorccPanel$Player(f, z);
                }
            };
            linearLayout3.addView(seekLine, new LinearLayout.LayoutParams(-1, Panel.this.dp(28.0f)));
            textViewLabel4.setGravity(8388613);
            LinearLayout linearLayoutHorizontal = Panel.horizontal(context);
            linearLayoutHorizontal.addView(textViewLabel3, new LinearLayout.LayoutParams(0, -2, 1.0f));
            linearLayoutHorizontal.addView(textViewLabel4, new LinearLayout.LayoutParams(0, -2, 1.0f));
            linearLayout3.addView(linearLayoutHorizontal, new LinearLayout.LayoutParams(-1, -2));
            ImageView imageViewButton = button(context, Icons.of("M6,6h2v12H6zM9.5,12l8.5,6V6z"), Panel.this.word("previous", new Object[0]), Panel.this.dp(Panel.DROP_STEP));
            this.prev = imageViewButton;
            ImageView imageViewButton2 = button(context, Icons.of("M6,18l8.5,-6L6,6v12zM16,6v12h2V6z"), Panel.this.word("next", new Object[0]), Panel.this.dp(Panel.DROP_STEP));
            this.next = imageViewButton2;
            ImageView[] imageViewArr = {imageViewButton, imageViewButton2};
            int r6 = 0;
            for (int r12 = 2; r6 < r12; r12 = 2) {
                imageViewArr[r6].setPadding(Panel.this.dp(2.0f), Panel.this.dp(11.0f), Panel.this.dp(2.0f), Panel.this.dp(11.0f));
                r6++;
                r7 = r7;
            }
            int r19 = r7;
            ImageView imageViewButton3 = button(context, this.playIcon, Panel.this.word("play", new Object[0]), Panel.this.dp(18.0f));
            this.play = imageViewButton3;
            imageViewButton3.setBackground(new Glass(-1.0f, Panel.GLASS_CARD_ON, Panel.GLASS_CARD_ON, 0.5f, 0.5f));
            int r5 = 0;
            while (true) {
                ImageView[] imageViewArr2 = this.actions;
                if (r5 < imageViewArr2.length) {
                    imageViewArr2[r5] = button(context, null, null, Panel.this.dp(13.0f));
                    this.actions[r5].setPadding(Panel.this.dp(2.0f), Panel.this.dp(Panel.DROP_STEP), Panel.this.dp(2.0f), Panel.this.dp(Panel.DROP_STEP));
                    this.actions[r5].setImageTintList(ColorStateList.valueOf(-419430401));
                    this.actions[r5].setVisibility(Panel.TILES_PER_ROW);
                    Panel.this.squish(this.actions[r5]);
                    r5++;
                } else {
                    Panel.this.squish(this.prev);
                    Panel.this.squish(this.next);
                    Panel.this.squish(this.play);
                    LinearLayout linearLayoutHorizontal2 = Panel.horizontal(context);
                    linearLayoutHorizontal2.setGravity(16);
                    linearLayoutHorizontal2.addView(this.actions[0], new LinearLayout.LayoutParams(0, Panel.this.dp(48.0f), 1.0f));
                    linearLayoutHorizontal2.addView(this.prev, new LinearLayout.LayoutParams(0, Panel.this.dp(52.0f), 1.0f));
                    linearLayoutHorizontal2.addView(this.play, new LinearLayout.LayoutParams(Panel.this.dp(62.0f), Panel.this.dp(62.0f)));
                    linearLayoutHorizontal2.addView(this.next, new LinearLayout.LayoutParams(0, Panel.this.dp(52.0f), 1.0f));
                    linearLayoutHorizontal2.addView(this.actions[r19], new LinearLayout.LayoutParams(0, Panel.this.dp(48.0f), 1.0f));
                    LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, Panel.this.dp(66.0f));
                    layoutParams3.topMargin = Panel.this.dp(6.0f);
                    linearLayout3.addView(linearLayoutHorizontal2, layoutParams3);
                    LinearLayout linearLayoutHorizontal3 = Panel.horizontal(context);
                    linearLayoutHorizontal3.setGravity(16);
                    ImageView imageViewButton4 = button(context, Icons.of("M2,9v6h4l5,5V4L6,9H2z"), null, 0);
                    imageViewButton4.setImageTintList(ColorStateList.valueOf(-1711276033));
                    linearLayoutHorizontal3.addView(imageViewButton4, new LinearLayout.LayoutParams(Panel.this.dp(18.0f), Panel.this.dp(18.0f)));
                    SeekLine seekLine2 = Panel.this.new SeekLine(context);
                    this.loudness = seekLine2;
                    seekLine2.setContentDescription(Panel.this.word("media_volume", new Object[0]));
                    seekLine2.listener = new Slider.Listener() { // from class: dev.colorcc.Panel.Player.12
                        @Override // dev.colorcc.Slider.Listener
                        public final void onChange(float f, boolean z) {
                            Player.this.m191lambda$new$9$devcolorccPanel$Player(f, z);
                        }
                    };
                    LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(0, Panel.this.dp(28.0f), 1.0f);
                    layoutParams4.leftMargin = Panel.this.dp(Panel.DROP_STEP);
                    layoutParams4.rightMargin = Panel.this.dp(Panel.DROP_STEP);
                    linearLayoutHorizontal3.addView(seekLine2, layoutParams4);
                    Drawable drawableSpeaker = Icons.speaker();
                    drawableSpeaker.setLevel(10000);
                    ImageView imageViewButton5 = button(context, drawableSpeaker, null, 0);
                    imageViewButton5.setImageTintList(ColorStateList.valueOf(-1711276033));
                    linearLayoutHorizontal3.addView(imageViewButton5, new LinearLayout.LayoutParams(Panel.this.dp(20.0f), Panel.this.dp(20.0f)));
                    LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(-1, -2);
                    layoutParams5.topMargin = Panel.this.dp(4.0f);
                    linearLayout3.setClipChildren(false);
                    linearLayout3.addView(linearLayoutHorizontal3, layoutParams5);
                    LinearLayout linearLayoutHorizontal4 = Panel.horizontal(context);
                    linearLayoutHorizontal4.setGravity(16);
                    linearLayoutHorizontal4.setPadding(Panel.this.dp(Panel.DROP_STEP), 0, Panel.this.dp(Panel.DROP_HEADER), 0);
                    linearLayoutHorizontal4.setBackground(new Glass(-1.0f, Panel.GLASS_CARD_ON, Panel.GLASS_CARD_ON, 0.5f, 0.5f));
                    ImageView imageViewButton6 = button(context, Icons.of("M21,3H3C1.9,3 1,3.9 1,5v3h2V5h18v14h-7v2h7c1.1,0 2,-0.9 2,-2V5C23,3.9 22.1,3 21,3zM1,18v3h3C4,19.34 2.66,18 1,18zM1,14v2c2.76,0 5,2.24 5,5h2C8,17.13 4.87,14 1,14zM1,10v2c4.97,0 9,4.03 9,9h2C12,14.92 7.07,10 1,10z"), null, 0);
                    imageViewButton6.setImageTintList(ColorStateList.valueOf(-1));
                    linearLayoutHorizontal4.addView(imageViewButton6, new LinearLayout.LayoutParams(Panel.this.dp(20.0f), Panel.this.dp(20.0f)));
                    TextView textViewLabel5 = Panel.this.label(context, 13.0f, -1, r19);
                    textViewLabel5.setText(Panel.this.word("media_output", new Object[0]));
                    LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(-2, -2);
                    layoutParams6.leftMargin = Panel.this.dp(8.0f);
                    linearLayoutHorizontal4.addView(textViewLabel5, layoutParams6);
                    linearLayoutHorizontal4.setContentDescription(Panel.this.word("media_output", new Object[0]));
                    linearLayoutHorizontal4.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Player.13
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            Player.this.m179lambda$new$11$devcolorccPanel$Player(view);
                        }
                    });
                    Panel.this.squish(linearLayoutHorizontal4);
                    LinearLayout.LayoutParams layoutParams7 = new LinearLayout.LayoutParams(-2, Panel.this.dp(38.0f));
                    layoutParams7.gravity = 1;
                    layoutParams7.topMargin = Panel.this.dp(Panel.DROP_STEP);
                    linearLayout3.addView(linearLayoutHorizontal4, layoutParams7);
                    this.card.addView(linearLayout3, new LinearLayout.LayoutParams(-1, -2));
                    this.prev.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Player.14
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            Player.this.m180lambda$new$13$devcolorccPanel$Player(view);
                        }
                    });
                    this.next.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Player.15
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            Player.this.m181lambda$new$15$devcolorccPanel$Player(view);
                        }
                    });
                    this.play.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Player.16
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            Player.this.m183lambda$new$17$devcolorccPanel$Player(view);
                        }
                    });
                    addView(this.card, new FrameLayout.LayoutParams(Panel.this.dp(236.0f), -2, 17));
                    Motion.Value value = Panel.this.motion.value(Logic.Spring.MENU, new Motion.Listener() { // from class: dev.colorcc.Panel.Player.6
                        @Override // dev.colorcc.Motion.Listener
                        public final void onValue(float f, float f2) {
                            Player.this.m185lambda$new$19$devcolorccPanel$Player(f, f2);
                        }
                    });
                    this.show = value;
                    this.back = Panel.this.new Rewind(value, new Motion.Listener() { // from class: dev.colorcc.Panel.Player.7
                        @Override // dev.colorcc.Motion.Listener
                        public final void onValue(float f, float f2) {
                            Player.this.m185lambda$new$19$devcolorccPanel$Player(f, f2);
                        }
                    });
                    return;
                }
            }
        }

        /* JADX INFO: renamed from: lambda$new$1$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m177lambda$new$1$devcolorccPanel$Player(View view) {
            close();
        }

        /* JADX INFO: renamed from: lambda$new$3$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m186lambda$new$3$devcolorccPanel$Player(View view) {
            final Panel panel = Panel.this;
            panel.guard(new Runnable() { // from class: dev.colorcc.Panel.Player.26
                @Override // java.lang.Runnable
                public final void run() {
                    Panel.access$12000(panel);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$4$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ boolean m187lambda$new$4$devcolorccPanel$Player(View view, MotionEvent motionEvent) {
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 0) {
                Panel.this.scaleTo(view, 0.97f, Logic.Spring.PRESS_IN);
                return false;
            }
            if (actionMasked != 1 && actionMasked != 3) {
                return false;
            }
            Panel.this.scaleTo(view, 1.0f, Logic.Spring.PRESS_OUT);
            return false;
        }

        /* JADX INFO: renamed from: lambda$new$7$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m189lambda$new$7$devcolorccPanel$Player(final float f, final boolean z) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Player.21
                @Override // java.lang.Runnable
                public final void run() {
                    Player.this.m188lambda$new$6$devcolorccPanel$Player(f, z);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$6$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m188lambda$new$6$devcolorccPanel$Player(final float f, boolean z) {
            final long jDuration = duration();
            if (jDuration <= 0) {
                return;
            }
            this.elapsed.setText(Logic.clock((long) (jDuration * f)));
            if (z) {
                Panel.this.mediaTransport(new Consumer() { // from class: dev.colorcc.Panel.Player.22
                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        Player.lambda$new$5(f, jDuration, (MediaController.TransportControls) obj);
                    }
                });
            }
        }

        static /* synthetic */ void lambda$new$5(float f, long j, MediaController.TransportControls transportControls) {
            transportControls.seekTo((long) (f * j));
        }

        /* JADX INFO: renamed from: lambda$new$9$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m191lambda$new$9$devcolorccPanel$Player(final float f, boolean z) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Player.25
                @Override // java.lang.Runnable
                public final void run() {
                    Player.this.m190lambda$new$8$devcolorccPanel$Player(f);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$10$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m178lambda$new$10$devcolorccPanel$Player(View view) {
            Panel.this.sheet.openOutput(view, Panel.this.controller == null ? null : Panel.this.controller.getPackageName());
        }

        /* JADX INFO: renamed from: lambda$new$11$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m179lambda$new$11$devcolorccPanel$Player(final View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Player.24
                @Override // java.lang.Runnable
                public final void run() {
                    Player.this.m178lambda$new$10$devcolorccPanel$Player(view);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$13$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m180lambda$new$13$devcolorccPanel$Player(View view) {
            Panel.this.skipped(-1);
            Panel.this.mediaTransport(new Consumer() { // from class: dev.colorcc.Panel.Player.19
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((MediaController.TransportControls) obj).skipToPrevious();
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$15$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m181lambda$new$15$devcolorccPanel$Player(View view) {
            Panel.this.skipped(1);
            Panel.this.mediaTransport(new Consumer() { // from class: dev.colorcc.Panel.Player.0
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((MediaController.TransportControls) obj).skipToNext();
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$17$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m183lambda$new$17$devcolorccPanel$Player(View view) {
            Panel.this.mediaTransport(new Consumer() { // from class: dev.colorcc.Panel.Player.17
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    Player.this.m182lambda$new$16$devcolorccPanel$Player((MediaController.TransportControls) obj);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$16$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m182lambda$new$16$devcolorccPanel$Player(MediaController.TransportControls transportControls) {
            if (this.playing) {
                transportControls.pause();
            } else {
                transportControls.play();
            }
        }

        private ImageView button(Context context, Drawable drawable, String str, int r4) {
            ImageView imageView = new ImageView(context);
            imageView.setImageDrawable(drawable);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(r4, r4, r4, r4);
            imageView.setContentDescription(str);
            return imageView;
        }

        @Override // android.view.ViewGroup, android.view.View
        public boolean dispatchTouchEvent(MotionEvent motionEvent) {
            if (motionEvent.getActionMasked() == 0 && getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            return super.dispatchTouchEvent(motionEvent);
        }

        @Override // android.view.View
        protected void onSizeChanged(int r2, int r3, int r4, int r5) {
            super.onSizeChanged(r2, r3, r4, r5);
            int r6 = Math.max(Panel.this.dp(110.0f), Math.min(Panel.this.dp(224.0f), r3 - Panel.this.dp(300.0f)));
            if (this.cover.getLayoutParams().height == r6) {
                return;
            }
            this.cover.getLayoutParams().height = r6;
            final FrameLayout frameLayout = this.cover;
            Objects.requireNonNull(frameLayout);
            post(new Runnable() { // from class: dev.colorcc.Panel.Player.4
                @Override // java.lang.Runnable
                public final void run() {
                    frameLayout.requestLayout();
                }
            });
        }

        void open() {
            if (Panel.this.controller == null || this.opened) {
                return;
            }
            Panel.this.sheet.close();
            this.opened = true;
            this.shownTitle = null;
            this.ghost.setAlpha(0.0f);
            Panel.this.renderMedia();
            this.back.stop();
            measureFrom();
            m185lambda$new$19$devcolorccPanel$Player(this.show.value(), this.show.velocity());
            setVisibility(0);
            this.show.to(1.0f);
            Panel.this.main.removeCallbacks(this.tick);
            Panel.this.main.post(this.tick);
        }

        void close() {
            if (this.opened) {
                this.opened = false;
                Panel.this.main.removeCallbacks(this.tick);
                measureFrom();
                this.back.start();
            }
        }

        private void measureFrom() {
            int width = this.card.getWidth();
            if (width == 0) {
                this.fromY = 0.0f;
                this.fromX = 0.0f;
                this.fromScale = 0.85f;
                this.fromHeight = this.card.getHeight();
                return;
            }
            getLocationOnScreen(this.at);
            float left = this.at[0] + this.card.getLeft();
            float top = this.at[1] + this.card.getTop();
            Panel.this.media.getLocationOnScreen(this.at);
            this.fromScale = ((Panel.this.media.getWidth() * Panel.this.media.getScaleX()) * Panel.this.column.getScaleX()) / width;
            int[] r0 = this.at;
            this.fromX = r0[0] - left;
            this.fromY = r0[1] - top;
            this.fromHeight = ((Panel.this.media.getHeight() * Panel.this.media.getScaleY()) * Panel.this.column.getScaleY()) / this.fromScale;
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* JADX INFO: renamed from: applyShow, reason: merged with bridge method [inline-methods] and merged with bridge method [inline-methods] */
        public void m185lambda$new$19$devcolorccPanel$Player(float f, float f2) {
            this.grown = Logic.clamp(f);
            float f3 = this.fromScale;
            float f4 = f3 + ((1.0f - f3) * f);
            this.card.setScaleX(f4);
            this.card.setScaleY(f4);
            float f5 = 1.0f - f;
            this.card.setTranslationX(this.fromX * f5);
            this.card.setTranslationY(this.fromY * f5);
            this.card.invalidateOutline();
            this.card.setAlpha(Logic.clamp((f - Panel.MAX_SMEAR) / 0.45f));
            Panel.this.media.setVisibility(f < 0.6f ? 0 : Panel.TILES_PER_ROW);
            this.shade.setAlpha(Math.round(this.grown * 255.0f));
            Panel.this.playerBack = this.grown;
            Panel.this.backdrop();
            if (this.opened || f2 != 0.0f || f > 0.0f) {
                return;
            }
            setVisibility(Panel.TILES_PER_ROW);
        }

        void render(CharSequence charSequence, CharSequence charSequence2, Bitmap bitmap, boolean z) {
            if (this.opened) {
                this.playing = z;
                this.title.setText(TextUtils.isEmpty(charSequence) ? Panel.this.word("not_playing", new Object[0]) : charSequence);
                this.artist.setText(charSequence2);
                this.artist.setVisibility(TextUtils.isEmpty(charSequence2) ? 8 : 0);
                if (TextUtils.isEmpty(charSequence)) {
                    charSequence = "";
                }
                CharSequence charSequence3 = this.shownTitle;
                boolean z2 = (charSequence3 == null || TextUtils.equals(charSequence, charSequence3) || this.show.value() < 1.0f) ? false : true;
                this.shownTitle = charSequence;
                if (z2 && this.art.getDrawable() != null) {
                    this.ghost.setScaleType(this.art.getScaleType());
                    this.ghost.setPadding(this.art.getPaddingLeft(), this.art.getPaddingTop(), this.art.getPaddingRight(), this.art.getPaddingBottom());
                    this.ghost.setImageTintList(this.art.getImageTintList());
                    this.ghost.setImageDrawable(this.art.getDrawable());
                    Panel.this.spring(this.ghost, View.ALPHA, Logic.Spring.REVEAL).snap(1.0f);
                    Panel.this.spring(this.ghost, View.ALPHA, Logic.Spring.REVEAL).to(0.0f);
                    Panel.this.spring(this.ghost, View.SCALE_X, Logic.Spring.REVEAL).snap(1.0f);
                    Panel.this.spring(this.ghost, View.SCALE_Y, Logic.Spring.REVEAL).snap(1.0f);
                    Panel.this.scaleTo(this.ghost, 0.94f, Logic.Spring.REVEAL);
                }
                ImageView imageView = this.art;
                if (bitmap == null) {
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    this.art.setPadding(Panel.this.dp(96.0f), Panel.this.dp(60.0f), Panel.this.dp(96.0f), Panel.this.dp(100.0f));
                    this.art.setImageDrawable(this.noteIcon);
                    this.art.setImageTintList(ColorStateList.valueOf(-1711276033));
                } else {
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    this.art.setPadding(0, 0, 0, 0);
                    this.art.setImageTintList(null);
                    this.art.setImageBitmap(bitmap);
                }
                if (z2) {
                    Panel.this.swellIn(this.art);
                    Panel.this.slideIn(this.title, this.artist);
                }
                this.play.setImageDrawable(z ? this.pauseIcon : this.playIcon);
                this.play.setContentDescription(Panel.this.word(z ? "pause" : "play", new Object[0]));
                this.tint.show(Panel.this.mediaTones);
                showActions();
                showBadge();
                this.seek.light(Panel.this.mediaTones == null ? -1 : Logic.seekColor(Panel.this.mediaTones));
                this.seek.breathing = z;
                this.seek.invalidate();
                readVolume();
                showProgress();
            }
        }

        private void showActions() {
            Context contextCreatePackageContext;
            Drawable drawable;
            PlaybackState playbackState = Panel.this.controller == null ? null : Panel.this.controller.getPlaybackState();
            List<PlaybackState.CustomAction> customActions = playbackState == null ? Collections.EMPTY_LIST : playbackState.getCustomActions();
            try {
                contextCreatePackageContext = getContext().createPackageContext(Panel.this.controller.getPackageName(), 0);
            } catch (Throwable unused) {
                contextCreatePackageContext = null;
            }
            int r4 = 0;
            while (r4 < this.actions.length) {
                final PlaybackState.CustomAction customAction = r4 < customActions.size() ? customActions.get(r4) : null;
                if (customAction == null || contextCreatePackageContext == null) {
                    drawable = null;
                } else {
                    try {
                        drawable = contextCreatePackageContext.getDrawable(customAction.getIcon());
                    } catch (Throwable unused2) {
                        drawable = null;
                    }
                }
                this.actions[r4].setVisibility(drawable == null ? Panel.TILES_PER_ROW : 0);
                if (drawable != null) {
                    this.actions[r4].setImageDrawable(drawable);
                    this.actions[r4].setContentDescription(customAction.getName());
                    this.actions[r4].setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Player.18
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            Player.this.m192lambda$showActions$21$devcolorccPanel$Player(customAction, view);
                        }
                    });
                }
                r4++;
            }
        }

        /* JADX INFO: renamed from: lambda$showActions$21$dev-colorcc-Panel$Player, reason: not valid java name */
        /* synthetic */ void m192lambda$showActions$21$devcolorccPanel$Player(final PlaybackState.CustomAction customAction, View view) {
            Panel.this.mediaTransport(new Consumer() { // from class: dev.colorcc.Panel.Player.20
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((MediaController.TransportControls) obj).sendCustomAction(customAction, (Bundle) null);
                }
            });
        }

        private void showBadge() {
            String packageName = Panel.this.controller == null ? null : Panel.this.controller.getPackageName();
            if (packageName == null || packageName.equals(this.badgeFor)) {
                return;
            }
            this.badgeFor = packageName;
            try {
                this.badge.setImageDrawable(getContext().getPackageManager().getApplicationIcon(packageName));
                this.badge.setVisibility(0);
            } catch (Throwable unused) {
                this.badge.setVisibility(8);
            }
        }

        void readVolume() {
            MediaController.PlaybackInfo playbackInfo;
            if (!this.opened || Panel.this.controller == null || this.loudness.dragging || (playbackInfo = Panel.this.controller.getPlaybackInfo()) == null || playbackInfo.getMaxVolume() <= 0) {
                return;
            }
            this.volumeSteps = playbackInfo.getMaxVolume();
            int currentVolume = playbackInfo.getCurrentVolume();
            this.sentVolume = currentVolume;
            this.loudness.set((float) currentVolume / (float) this.volumeSteps);
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* JADX INFO: renamed from: setVolume, reason: merged with bridge method [inline-methods] */
        public void m190lambda$new$8$devcolorccPanel$Player(float f) {
            int r0;
            int r2;
            if (Panel.this.controller == null || (r0 = this.volumeSteps) <= 0 || (r2 = Math.round(f * r0)) == this.sentVolume) {
                return;
            }
            this.sentVolume = r2;
            Panel.this.controller.setVolumeTo(r2, 0);
        }

        private long duration() {
            MediaMetadata metadata = Panel.this.controller == null ? null : Panel.this.controller.getMetadata();
            if (metadata == null) {
                return 0L;
            }
            return metadata.getLong("android.media.metadata.DURATION");
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void showProgress() {
            long position;
            Panel.this.main.removeCallbacks(this.tick);
            if (!this.opened || Panel.this.controller == null) {
                return;
            }
            PlaybackState playbackState = Panel.this.controller.getPlaybackState();
            long jDuration = duration();
            if (playbackState != null) {
                position = playbackState.getPosition();
                if (playbackState.getState() == 3) {
                    position += (long) ((SystemClock.elapsedRealtime() - playbackState.getLastPositionUpdateTime()) * playbackState.getPlaybackSpeed());
                }
            } else {
                position = 0;
            }
            long jMin = jDuration > 0 ? Math.min(position, jDuration) : Math.max(0L, position);
            this.seek.setEnabled(jDuration > 0);
            if (!this.seek.dragging) {
                this.seek.set(jDuration > 0 ? (float) jMin / (float) jDuration : 0.0f);
                this.elapsed.setText(Logic.clock(jMin));
            }
            this.total.setText(jDuration > 0 ? Logic.clock(jDuration) : "");
            this.seek.setStateDescription(Logic.clock(jMin) + " of " + Logic.clock(jDuration));
            Panel.this.main.postDelayed(this.tick, Panel.BACKDROP_RECHECK_MS);
        }
    }

    private static final class FadingImage extends ImageView {
        private final Paint fade;

        FadingImage(Context context) {
            super(context);
            Paint paint = new Paint();
            this.fade = paint;
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        }

        @Override // android.view.View
        protected void onSizeChanged(int r9, int r10, int r11, int r12) {
            super.onSizeChanged(r9, r10, r11, r12);
            this.fade.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, r10, new int[]{-16777216, -16777216, -1728053248, 0}, new float[]{0.0f, 0.5f, 0.78f, 1.0f}, Shader.TileMode.CLAMP));
        }

        @Override // android.widget.ImageView, android.view.View
        protected void onDraw(Canvas canvas) {
            int r8 = canvas.saveLayer(0.0f, 0.0f, getWidth(), getHeight(), null);
            super.onDraw(canvas);
            canvas.drawRect(0.0f, 0.0f, getWidth(), getHeight(), this.fade);
            canvas.restoreToCount(r8);
        }
    }

    private final class SeekLine extends View {
        private final RectF bar;
        boolean breathing;
        boolean dragging;
        private final Paint fill;
        float from;
        boolean glow;
        private final Paint halo;
        private long lastFrameMs;
        Slider.Listener listener;
        private int lit;
        String name;
        private float phase;
        TextView readout;
        float to;
        private final Paint track;
        private final RectF trail;
        String unit;
        private float value;

        SeekLine(Context context) {
            super(context);
            Paint paint = new Paint(1);
            this.track = paint;
            Paint paint2 = new Paint(1);
            this.fill = paint2;
            Paint paint3 = new Paint(1);
            this.halo = paint3;
            this.bar = new RectF();
            this.trail = new RectF();
            this.lit = -1;
            this.unit = "%";
            paint.setColor(1090519039);
            paint2.setColor(-1);
            paint3.setMaskFilter(new BlurMaskFilter(Panel.this.dp(5.0f), BlurMaskFilter.Blur.NORMAL));
            setContentDescription(Panel.this.word("playback_position", new Object[0]));
        }

        void set(float f) {
            float fClamp = Logic.clamp(f);
            showValue(fClamp);
            if (fClamp == this.value) {
                return;
            }
            this.value = fClamp;
            invalidate();
        }

        private void showValue(float f) {
            TextView textView = this.readout;
            if (textView != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(this.name);
                sb.append("  ");
                float f2 = this.from;
                sb.append(Math.round(f2 + (f * (this.to - f2))));
                sb.append(this.unit);
                textView.setText(sb.toString());
            }
        }

        void light(int r2) {
            if (r2 == this.lit) {
                return;
            }
            this.lit = r2;
            this.fill.setColor(r2);
            invalidate();
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            float fDp = Panel.this.dp(this.dragging ? 8.0f : 5.0f);
            float height = getHeight() / 2.0f;
            float f = fDp / 2.0f;
            this.bar.set(0.0f, height - f, getWidth(), height + f);
            canvas.drawRoundRect(this.bar, f, f, this.track);
            boolean z = this.glow;
            RectF rectF = this.bar;
            if (!z) {
                rectF.right = getWidth() * this.value;
                if (this.bar.right > fDp) {
                    canvas.drawRoundRect(this.bar, f, f, this.fill);
                    return;
                }
                return;
            }
            rectF.right = Math.max(2.4f * fDp, getWidth() * this.value);
            long jUptimeMillis = SystemClock.uptimeMillis();
            long j = this.lastFrameMs;
            float fMin = j == 0 ? 0.0f : Math.min(Panel.MAX_SMEAR, (jUptimeMillis - j) / 1000.0f);
            this.lastFrameMs = jUptimeMillis;
            boolean z2 = this.breathing;
            if (z2) {
                this.phase = (this.phase + (fMin * 2.38f)) % 6.2831855f;
            }
            float fSin = (z2 ? (((float) Math.sin(this.phase)) * Panel.ICON_COVERED) + Panel.MAX_SMEAR : 0.0f) + 0.8f;
            this.halo.setColor(Color.argb(Math.round(114.75f * fSin), Color.red(this.lit), Color.green(this.lit), Color.blue(this.lit)));
            this.trail.set(Math.max(0.0f, this.bar.right - Panel.this.dp(70.0f)), this.bar.top, this.bar.right, this.bar.bottom);
            canvas.drawRoundRect(this.trail, f, f, this.halo);
            this.halo.setColor(Color.argb(Math.round(fSin * 229.5f), (Color.red(this.lit) + 255) / 2, (Color.green(this.lit) + 255) / 2, (Color.blue(this.lit) + 255) / 2));
            canvas.drawCircle(this.bar.right - (0.6f * fDp), height, fDp * 0.95f, this.halo);
            canvas.drawRoundRect(this.bar, f, f, this.fill);
            if (this.breathing) {
                postInvalidateOnAnimation();
            } else {
                this.lastFrameMs = 0L;
            }
        }

        @Override // android.view.View
        public boolean onTouchEvent(MotionEvent motionEvent) {
            if (!isEnabled()) {
                return false;
            }
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 0) {
                getParent().requestDisallowInterceptTouchEvent(true);
                this.dragging = true;
                follow(motionEvent, false);
                return true;
            }
            if (actionMasked == 1) {
                follow(motionEvent, true);
                this.dragging = false;
                invalidate();
                return true;
            }
            if (actionMasked == 2) {
                follow(motionEvent, false);
                return true;
            }
            if (actionMasked != 3) {
                return false;
            }
            this.dragging = false;
            invalidate();
            return true;
        }

        private void follow(MotionEvent motionEvent, boolean z) {
            float fClamp = Logic.clamp(motionEvent.getX() / Math.max(1, getWidth()));
            this.value = fClamp;
            showValue(fClamp);
            invalidate();
            Slider.Listener listener = this.listener;
            if (listener != null) {
                listener.onChange(this.value, z);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String specOf(Object obj) {
        if (obj instanceof String) {
            return (String) obj;
        }
        try {
            return (String) ReflectUtils.callMethod(obj, "getSpec", new Object[0]);
        } catch (Throwable unused) {
            return String.valueOf(obj);
        }
    }

    private final class Editor extends FrameLayout {
        private static final String BADGE = "badge";
        private static final int GROW_LISTENER = 2113929220;
        private static final int GROW_TURN = 2113929221;
        private static final String HANDLE = "handle";
        private static final char PIN = 'p';
        private static final int PIN_SPEC = 2113929219;
        private static final int RESIZE_ACTION = 2113929218;
        private final String TILES_TAG;
        private boolean active;
        private final TextView allIn;
        private final TileGrid availableGrid;
        private List<String> before;
        private final List<Bound> bindings;
        private final TileGrid blockGrid;
        private int blurBefore;
        private LinearLayout blurBox;
        private final Segmented blurChoice;
        private float blurCustomBefore;
        private SeekLine blurLine;
        private final LinearLayout body;
        private final TextView cardsHint;
        private final TileGrid chosenGrid;
        private LinearLayout colourRows;
        private final List<Object> created;
        private boolean draftFooter;
        private final float[] draftHsv;
        private int draftIconMode;
        private int draftLook;
        private boolean draftNames;
        private boolean draftNotif;
        private int draftRows;
        private boolean draftSwipe;
        private View edgeCell;
        private TileGrid edgeGrid;
        private final Runnable edgeStep;
        private float edgeX;
        private float edgeY;
        private int fills;
        private final List<String> fixed;
        private Segmented footerChoice;
        private Motion.Value headerAway;
        private final TileGrid hiddenGrid;
        private SeekLine hueLine;
        private Segmented iconChoice;
        private float jellyBefore;
        private SeekLine jellyLine;
        private int lastSeen;
        private boolean layered;
        private Map<String, Object> live;
        private int lookBefore;
        private final Segmented lookChoice;
        private final ScrollView lookScroll;
        private Segmented namesChoice;
        private Segmented notifChoice;
        private final LinearLayout page;
        private final List<View> parts;
        private int plainBefore;
        private Segmented plainChoice;
        private View plainTitle;
        private Disc previewOff;
        private Disc previewOn;
        private Segmented rowsChoice;
        private SeekLine satLine;
        float scale;
        private final ScrollView scroll;
        private final Motion.Value shown;
        private View swatch;
        private Segmented swipeChoice;
        private int tab;
        private final Motion.Value tabAt;
        private Segmented tabs;
        private final TextView tilesHint;
        private final View top;
        private float[][] tuneBefore;
        private LinearLayout tuneBox;
        private final SeekLine[] tuneLines;
        private SeekLine valLine;
        private boolean wide;

        @Override // android.view.ViewGroup, android.view.View
        public boolean dispatchTouchEvent(MotionEvent motionEvent) {
            if (motionEvent.getActionMasked() == 0 && getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            return super.dispatchTouchEvent(motionEvent);
        }

        Editor(Context context) {
            super(context);
            this.draftRows = 3;
            this.draftHsv = new float[]{220.0f, 0.9f, 0.9f};
            this.tuneLines = new SeekLine[7];
            this.draftNames = true;
            char c = 0;
            this.draftSwipe = false;
            this.bindings = new ArrayList();
            this.created = new ArrayList();
            this.fixed = new ArrayList();
            this.before = new ArrayList();
            this.live = new HashMap();
            ArrayList arrayList = new ArrayList();
            this.parts = arrayList;
            this.scale = 1.0f;
            this.TILES_TAG = "g41";
            this.edgeStep = new Runnable() { // from class: dev.colorcc.Panel.Editor.29
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.this.m150lambda$new$49$devcolorccPanel$Editor();
                }
            };
            setVisibility(8);
            setClickable(true);
            LinearLayout linearLayout = new LinearLayout(context) { // from class: dev.colorcc.Panel.Editor.1
                @Override // android.widget.LinearLayout, android.view.View
                protected void onMeasure(int r3, int r4) {
                    int top;
                    super.onMeasure(r3, r4);
                    if (Editor.this.body == null || Editor.this.body.getVisibility() == 8) {
                        return;
                    }
                    int top2 = Editor.this.body.getTop();
                    int top3 = Editor.this.top.getTop();
                    Editor editor = Editor.this;
                    if (top2 <= top3) {
                        top = Panel.this.dp(94.0f);
                    } else {
                        top = editor.body.getTop() - Editor.this.top.getTop();
                    }
                    Editor.this.body.measure(View.MeasureSpec.makeMeasureSpec(Editor.this.body.getMeasuredWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(Editor.this.body.getMeasuredHeight() + top, 1073741824));
                }
            };
            this.page = linearLayout;
            linearLayout.setOrientation(1);
            linearLayout.setClipToPadding(false);
            linearLayout.setClipChildren(false);
            FrameLayout frameLayout = new FrameLayout(context);
            Disc disc = Panel.this.new Disc(context, Panel.this.dp(14.0f));
            disc.icon.setImageDrawable(Icons.of("M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z"));
            disc.setContentDescription(Panel.this.word("cancel", new Object[0]));
            disc.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Editor.40
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    Editor.this.m132lambda$new$1$devcolorccPanel$Editor(view);
                }
            });
            Panel.this.squish(disc);
            frameLayout.addView(disc, new FrameLayout.LayoutParams(Panel.this.dp(Panel.DROP_TOP), Panel.this.dp(Panel.DROP_TOP), 8388627));
            TextView textViewLabel = Panel.this.label(context, 17.0f, -1, 1);
            textViewLabel.setText(Panel.this.word("edit", new Object[0]));
            textViewLabel.setTypeface(Typeface.DEFAULT_BOLD);
            textViewLabel.setGravity(17);
            frameLayout.addView(textViewLabel, new FrameLayout.LayoutParams(-2, -2, 17));
            TextView textViewPill = pill(context, Panel.this.word("done", new Object[0]), 14.0f, true);
            textViewPill.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Editor.46
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    Editor.this.m148lambda$new$3$devcolorccPanel$Editor(view);
                }
            });
            frameLayout.addView(textViewPill, new FrameLayout.LayoutParams(Panel.this.dp(76.0f), Panel.this.dp(34.0f), 8388629));
            linearLayout.addView(frameLayout, new LinearLayout.LayoutParams(-1, Panel.this.dp(48.0f)));
            arrayList.add(frameLayout);
            this.tabs = Panel.this.new Segmented(context, new String[]{Panel.this.word("layout", new Object[0]), Panel.this.word("look", new Object[0])}, new IntConsumer() { // from class: dev.colorcc.Panel.Editor.47
                @Override // java.util.function.IntConsumer
                public final void accept(int r1) {
                    Editor.this.m149lambda$new$4$devcolorccPanel$Editor(r1);
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, Panel.this.dp(38.0f));
            layoutParams.topMargin = Panel.this.dp(4.0f);
            linearLayout.addView(this.tabs, layoutParams);
            arrayList.add(this.tabs);
            LinearLayout linearLayout2 = new LinearLayout(context);
            linearLayout2.setOrientation(1);
            linearLayout2.setClipChildren(false);
            TextView textViewHint = hint(context, Panel.this.word("edit_how", new Object[0]));
            textViewHint.setMaxLines(3);
            textViewHint.setGravity(1);
            linearLayout2.addView(textViewHint);
            linearLayout2.addView(section(context, Panel.this.word("in_panel", new Object[0]), 10));
            TileGrid tileGrid = Panel.this.new TileGrid(context, true, Panel.TILES_PER_ROW, Panel.this.dp(63.0f), Panel.this.dp(14.0f), true);
            this.blockGrid = tileGrid;
            linearLayout2.addView(tileGrid, new LinearLayout.LayoutParams(-1, -2));
            TileGrid tileGrid2 = Panel.this.new TileGrid(context, true, Panel.TILES_PER_ROW, Panel.this.dp(98.0f), 0, false);
            this.chosenGrid = tileGrid2;
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams2.topMargin = Panel.this.dp(Panel.DROP_HEADER);
            linearLayout2.addView(tileGrid2, layoutParams2);
            linearLayout2.addView(section(context, Panel.this.word("tap_to_add", new Object[0]), 26));
            TextView textViewHint2 = hint(context, Panel.this.word("cards_sliders", new Object[0]));
            this.cardsHint = textViewHint2;
            linearLayout2.addView(textViewHint2);
            TileGrid tileGrid3 = Panel.this.new TileGrid(context, false, Panel.TILES_PER_ROW, Panel.this.dp(63.0f), Panel.this.dp(14.0f), true);
            this.hiddenGrid = tileGrid3;
            linearLayout2.addView(tileGrid3, new LinearLayout.LayoutParams(-1, -2));
            TextView textViewHint3 = hint(context, Panel.this.word("tiles", new Object[0]));
            this.tilesHint = textViewHint3;
            linearLayout2.addView(textViewHint3);
            TileGrid tileGrid4 = Panel.this.new TileGrid(context, false, Panel.TILES_PER_ROW, Panel.this.dp(98.0f), 0, false);
            this.availableGrid = tileGrid4;
            linearLayout2.addView(tileGrid4, new LinearLayout.LayoutParams(-1, -2));
            TextView textViewHint4 = hint(context, Panel.this.word("all_in", new Object[0]));
            this.allIn = textViewHint4;
            linearLayout2.addView(textViewHint4);
            TextView textViewPill2 = pill(context, Panel.this.word("reset", new Object[0]), 14.0f, false);
            textViewPill2.setPadding(Panel.this.dp(20.0f), 0, Panel.this.dp(20.0f), 0);
            textViewPill2.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Editor.48
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    Editor.this.m152lambda$new$6$devcolorccPanel$Editor(view);
                }
            });
            LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-2, Panel.this.dp(44.0f));
            layoutParams3.gravity = 1;
            layoutParams3.topMargin = Panel.this.dp(26.0f);
            linearLayout2.addView(textViewPill2, layoutParams3);
            TextView textViewHint5 = hint(context, Panel.this.word("reset_how", new Object[0]));
            textViewHint5.setGravity(1);
            linearLayout2.addView(textViewHint5);
            for (int r2 = 0; r2 < linearLayout2.getChildCount(); r2++) {
                this.parts.add(linearLayout2.getChildAt(r2));
            }
            LinearLayout linearLayout3 = new LinearLayout(context);
            linearLayout3.setOrientation(1);
            linearLayout3.setClipChildren(false);
            linearLayout3.addView(section(context, Panel.this.word("appearance", new Object[0]), 10));
            LinearLayout linearLayout4 = new LinearLayout(context);
            linearLayout4.setOrientation(1);
            linearLayout4.setBackground(new Glass(Panel.this.dp(26.0f), Panel.GLASS_IDLE, Panel.GLASS_IDLE, 0.5f, 0.5f));
            linearLayout4.setPadding(Panel.this.dp(Panel.DROP_HEADER), Panel.this.dp(Panel.DROP_STEP), Panel.this.dp(Panel.DROP_HEADER), Panel.this.dp(Panel.DROP_HEADER));
            linearLayout4.addView(hint(context, Panel.this.word("glass", new Object[0])));
            Segmented segmented = Panel.this.new Segmented(context, new String[]{Panel.this.word("liquid", new Object[0]), Panel.this.word("frosted", new Object[0]), Panel.this.word("opaque", new Object[0]), Panel.this.word("none", new Object[0])}, new IntConsumer() { // from class: dev.colorcc.Panel.Editor.49
                @Override // java.util.function.IntConsumer
                public final void accept(int r1) {
                    Editor.this.setLook(r1);
                }
            });
            this.lookChoice = segmented;
            linearLayout4.addView(segmented, new LinearLayout.LayoutParams(-1, Panel.this.dp(38.0f)));
            LinearLayout linearLayout5 = new LinearLayout(context);
            this.tuneBox = linearLayout5;
            linearLayout5.setOrientation(1);
            this.tuneBox.setClipChildren(false);
            this.tuneBox.addView(preview(context), new LinearLayout.LayoutParams(-1, Panel.this.dp(72.0f)));
            String[] strArr = {Panel.this.word("glass_tint", new Object[0]), Panel.this.word("edge_shine", new Object[0]), Panel.this.word("rim_width", new Object[0]), Panel.this.word("rim_glow", new Object[0]), Panel.this.word("frost", new Object[0]), Panel.this.word("colour_strength", new Object[0]), Panel.this.word("thickness", new Object[0])};
            int r10 = 0;
            while (r10 < 7) {
                final int tuneIdx = r10;
                this.tuneLines[r10] = tuneLine(context, this.tuneBox, strArr[r10], Glass.TUNE_RANGE[r10][c] * 100.0f, Glass.TUNE_RANGE[r10][1] * 100.0f, new Slider.Listener() { // from class: dev.colorcc.Panel.Editor.50
                    @Override // dev.colorcc.Slider.Listener
                    public final void onChange(float f, boolean z) {
                        Editor.this.m153lambda$new$7$devcolorccPanel$Editor(tuneIdx, f, z);
                    }
                });
                r10++;
                linearLayout4 = linearLayout4;
                linearLayout3 = linearLayout3;
                c = 0;
            }
            LinearLayout linearLayout6 = linearLayout3;
            LinearLayout linearLayout7 = linearLayout4;
            linearLayout7.addView(this.tuneBox, new LinearLayout.LayoutParams(-1, -2));
            TextView textViewHint6 = hint(context, Panel.this.word("plain_colour", new Object[0]));
            this.plainTitle = textViewHint6;
            linearLayout7.addView(textViewHint6);
            Segmented segmented2 = Panel.this.new Segmented(context, new String[]{Panel.this.word("plain_auto", new Object[0]), Panel.this.word("plain_white", new Object[0]), Panel.this.word("plain_black", new Object[0])}, new IntConsumer() { // from class: dev.colorcc.Panel.Editor.51
                @Override // java.util.function.IntConsumer
                public final void accept(int r1) {
                    Editor.this.m154lambda$new$8$devcolorccPanel$Editor(r1);
                }
            });
            this.plainChoice = segmented2;
            linearLayout7.addView(segmented2, new LinearLayout.LayoutParams(-1, Panel.this.dp(38.0f)));
            linearLayout7.addView(hint(context, Panel.this.word("background_blur", new Object[0])));
            Segmented segmented3 = Panel.this.new Segmented(context, new String[]{Panel.this.word("light", new Object[0]), Panel.this.word("normal", new Object[0]), Panel.this.word("strong", new Object[0]), Panel.this.word("custom", new Object[0])}, new IntConsumer() { // from class: dev.colorcc.Panel.Editor.52
                @Override // java.util.function.IntConsumer
                public final void accept(int r1) {
                    Editor.this.setBlur(r1);
                }
            });
            this.blurChoice = segmented3;
            linearLayout7.addView(segmented3, new LinearLayout.LayoutParams(-1, Panel.this.dp(38.0f)));
            LinearLayout linearLayout8 = new LinearLayout(context);
            this.blurBox = linearLayout8;
            linearLayout8.setOrientation(1);
            this.blurLine = tuneLine(context, this.blurBox, Panel.this.word("blur_strength", new Object[0]), 0.0f, 250.0f, new Slider.Listener() { // from class: dev.colorcc.Panel.Editor.53
                @Override // dev.colorcc.Slider.Listener
                public final void onChange(float f, boolean z) {
                    Editor.this.m155lambda$new$9$devcolorccPanel$Editor(f, z);
                }
            });
            linearLayout7.addView(this.blurBox, new LinearLayout.LayoutParams(-1, -2));
            linearLayout7.addView(hint(context, Panel.this.word("tile_rows", new Object[0])));
            Segmented segmented4 = Panel.this.new Segmented(context, new String[]{"1", "2", "3", "4"}, new IntConsumer() { // from class: dev.colorcc.Panel.Editor.30
                @Override // java.util.function.IntConsumer
                public final void accept(int r1) {
                    Editor.this.m133lambda$new$10$devcolorccPanel$Editor(r1);
                }
            });
            this.rowsChoice = segmented4;
            linearLayout7.addView(segmented4, new LinearLayout.LayoutParams(-1, Panel.this.dp(38.0f)));
            linearLayout7.addView(hint(context, Panel.this.word("icon_colours", new Object[0])));
            Segmented segmented5 = Panel.this.new Segmented(context, new String[]{"ColorOS", Panel.this.word("system", new Object[0]), Panel.this.word("custom", new Object[0])}, new IntConsumer() { // from class: dev.colorcc.Panel.Editor.31
                @Override // java.util.function.IntConsumer
                public final void accept(int r1) {
                    Editor.this.m134lambda$new$11$devcolorccPanel$Editor(r1);
                }
            });
            this.iconChoice = segmented5;
            linearLayout7.addView(segmented5, new LinearLayout.LayoutParams(-1, Panel.this.dp(38.0f)));
            LinearLayout linearLayout9 = new LinearLayout(context);
            this.colourRows = linearLayout9;
            linearLayout9.setOrientation(1);
            this.swatch = new View(context);
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(1);
            gradientDrawable.setStroke(Panel.this.dp(1.5f), -2130706433);
            this.swatch.setBackground(gradientDrawable);
            LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(Panel.this.dp(30.0f), Panel.this.dp(30.0f));
            layoutParams4.gravity = 1;
            layoutParams4.topMargin = Panel.this.dp(10.0f);
            this.colourRows.addView(this.swatch, layoutParams4);
            SeekLine seekLineTuneLine = tuneLine(context, this.colourRows, Panel.this.word("hue", new Object[0]), 0.0f, 360.0f, new Slider.Listener() { // from class: dev.colorcc.Panel.Editor.32
                @Override // dev.colorcc.Slider.Listener
                public final void onChange(float f, boolean z) {
                    Editor.this.m135lambda$new$12$devcolorccPanel$Editor(f, z);
                }
            });
            this.hueLine = seekLineTuneLine;
            seekLineTuneLine.unit = "°";
            this.satLine = tuneLine(context, this.colourRows, Panel.this.word("saturation", new Object[0]), 0.0f, 100.0f, new Slider.Listener() { // from class: dev.colorcc.Panel.Editor.33
                @Override // dev.colorcc.Slider.Listener
                public final void onChange(float f, boolean z) {
                    Editor.this.m136lambda$new$13$devcolorccPanel$Editor(f, z);
                }
            });
            this.valLine = tuneLine(context, this.colourRows, Panel.this.word("lightness", new Object[0]), 25.0f, 100.0f, new Slider.Listener() { // from class: dev.colorcc.Panel.Editor.34
                @Override // dev.colorcc.Slider.Listener
                public final void onChange(float f, boolean z) {
                    Editor.this.m137lambda$new$14$devcolorccPanel$Editor(f, z);
                }
            });
            linearLayout7.addView(this.colourRows, new LinearLayout.LayoutParams(-1, -2));
            linearLayout7.addView(hint(context, Panel.this.word("tile_names", new Object[0])));
            Segmented segmented6 = Panel.this.new Segmented(context, new String[]{Panel.this.word("show", new Object[0]), Panel.this.word("hide", new Object[0])}, new IntConsumer() { // from class: dev.colorcc.Panel.Editor.35
                @Override // java.util.function.IntConsumer
                public final void accept(int r1) {
                    Editor.this.m138lambda$new$15$devcolorccPanel$Editor(r1);
                }
            });
            this.namesChoice = segmented6;
            linearLayout7.addView(segmented6, new LinearLayout.LayoutParams(-1, Panel.this.dp(38.0f)));
            this.jellyLine = tuneLine(context, linearLayout7, Panel.this.word("slider_jelly", new Object[0]), 0.0f, 150.0f, new Slider.Listener() { // from class: dev.colorcc.Panel.Editor.36
                @Override // dev.colorcc.Slider.Listener
                public final void onChange(float f, boolean z) {
                    Editor.lambda$new$16(f, z);
                }
            });
            linearLayout7.addView(hint(context, Panel.this.word("notifications", new Object[0])));
            Segmented segmented7 = Panel.this.new Segmented(context, new String[]{Panel.this.word("notif_stock", new Object[0]), Panel.this.word("notif_match", new Object[0])}, new IntConsumer() { // from class: dev.colorcc.Panel.Editor.37
                @Override // java.util.function.IntConsumer
                public final void accept(int r1) {
                    Editor.this.m139lambda$new$17$devcolorccPanel$Editor(r1);
                }
            });
            this.notifChoice = segmented7;
            linearLayout7.addView(segmented7, new LinearLayout.LayoutParams(-1, Panel.this.dp(38.0f)));
            linearLayout7.addView(hint(context, Panel.this.word("notif_buttons", new Object[0])));
            Segmented segmented8 = Panel.this.new Segmented(context, new String[]{Panel.this.word("notif_stock", new Object[0]), Panel.this.word("notif_match", new Object[0])}, new IntConsumer() { // from class: dev.colorcc.Panel.Editor.38
                @Override // java.util.function.IntConsumer
                public final void accept(int r1) {
                    Editor.this.m140lambda$new$18$devcolorccPanel$Editor(r1);
                }
            });
            this.footerChoice = segmented8;
            linearLayout7.addView(segmented8, new LinearLayout.LayoutParams(-1, Panel.this.dp(38.0f)));
            linearLayout7.addView(hint(context, Panel.this.word("notif_swipe", new Object[0])));
            Segmented segmented9 = Panel.this.new Segmented(context, new String[]{Panel.this.word("on", new Object[0]), Panel.this.word("off", new Object[0])}, new IntConsumer() { // from class: dev.colorcc.Panel.Editor.39
                @Override // java.util.function.IntConsumer
                public final void accept(int r1) {
                    Editor.this.m141lambda$new$19$devcolorccPanel$Editor(r1);
                }
            });
            this.swipeChoice = segmented9;
            linearLayout7.addView(segmented9, new LinearLayout.LayoutParams(-1, Panel.this.dp(38.0f)));
            linearLayout6.addView(linearLayout7, new LinearLayout.LayoutParams(-1, -2));
            TextView textViewPill3 = pill(context, Panel.this.word("reset", new Object[0]), 14.0f, false);
            textViewPill3.setPadding(Panel.this.dp(20.0f), 0, Panel.this.dp(20.0f), 0);
            textViewPill3.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Editor.41
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    Editor.this.m143lambda$new$20$devcolorccPanel$Editor(view);
                }
            });
            LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(-2, Panel.this.dp(44.0f));
            layoutParams5.gravity = 1;
            layoutParams5.topMargin = Panel.this.dp(20.0f);
            linearLayout6.addView(textViewPill3, layoutParams5);
            TextView textViewHint7 = hint(context, Panel.this.word("reset_look_how", new Object[0]));
            textViewHint7.setGravity(1);
            linearLayout6.addView(textViewHint7);
            TextView switchClassicBtn = pill(context, Panel.this.word("switch_classic_qs", new Object[0]), 14.0f, false);
            switchClassicBtn.setPadding(Panel.this.dp(20.0f), 0, Panel.this.dp(20.0f), 0);
            switchClassicBtn.setOnClickListener(v -> Panel.this.guard(() -> {
                close(false);
                ColorCCBridge.setColorCCEnabled(getContext(), false);
            }));
            LinearLayout.LayoutParams switchParams = new LinearLayout.LayoutParams(-2, Panel.this.dp(44.0f));
            switchParams.gravity = 1;
            switchParams.topMargin = Panel.this.dp(18.0f);
            linearLayout6.addView(switchClassicBtn, switchParams);
            TextView switchClassicHint = hint(context, Panel.this.word("switch_classic_qs_how", new Object[0]));
            switchClassicHint.setGravity(1);
            linearLayout6.addView(switchClassicHint);
            TextView textViewLabel2 = Panel.this.label(context, 10.0f, -2130706433, 1);
            textViewLabel2.setText("Made by Guest");
            textViewLabel2.setGravity(1);
            LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams6.topMargin = Panel.this.dp(20.0f);
            linearLayout6.addView(textViewLabel2, layoutParams6);
            for (int r3 = 0; r3 < linearLayout6.getChildCount(); r3++) {
                this.parts.add(linearLayout6.getChildAt(r3));
            }
            LinearLayout linearLayoutHorizontal = Panel.horizontal(context);
            this.body = linearLayoutHorizontal;
            linearLayoutHorizontal.setClipChildren(true);
            ScrollView scrollViewScroller = scroller(context, linearLayout2);
            this.scroll = scrollViewScroller;
            ScrollView scrollViewScroller2 = scroller(context, linearLayout6);
            this.lookScroll = scrollViewScroller2;
            linearLayoutHorizontal.addView(scrollViewScroller, paneParams());
            linearLayoutHorizontal.addView(scrollViewScroller2, paneParams());
            LinearLayout.LayoutParams layoutParams7 = new LinearLayout.LayoutParams(-1, 0, 1.0f);
            layoutParams7.topMargin = Panel.this.dp(4.0f);
            int r5 = -Panel.this.dp(14.0f);
            layoutParams7.rightMargin = r5;
            layoutParams7.leftMargin = r5;
            this.page.addView(linearLayoutHorizontal, layoutParams7);
            this.top = frameLayout;
            addView(this.page, new FrameLayout.LayoutParams(Panel.this.dp(340.0f), -1, 1));
            this.tabAt = Panel.this.motion.value(Logic.Spring.BOUNCY, new Motion.Listener() { // from class: dev.colorcc.Panel.Editor.42
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Editor.this.m144lambda$new$21$devcolorccPanel$Editor(f, f2);
                }
            });
            showTab(0, false);
            this.headerAway = Panel.this.motion.value(Logic.Spring.SNAPPY, new Motion.Listener() { // from class: dev.colorcc.Panel.Editor.43
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Editor.this.m145lambda$new$22$devcolorccPanel$Editor(f, f2);
                }
            });
            ScrollView[] scrollViewArr = {scrollViewScroller, scrollViewScroller2};
            for (int r11 = 0; r11 < 2; r11++) {
                scrollViewArr[r11].setOnScrollChangeListener(new View.OnScrollChangeListener() { // from class: dev.colorcc.Panel.Editor.44
                    @Override // android.view.View.OnScrollChangeListener
                    public final void onScrollChange(View view, int r4, int r6, int r7, int r8) {
                        Editor.this.m147lambda$new$24$devcolorccPanel$Editor(view, r4, r6, r7, r8);
                    }
                });
            }
            this.shown = Panel.this.motion.value(Logic.Spring.REVEAL, new Motion.Listener() { // from class: dev.colorcc.Panel.Editor.45
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Editor.this.applyShown(f, f2);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$0$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m131lambda$new$0$devcolorccPanel$Editor() {
            close(false);
        }

        /* JADX INFO: renamed from: lambda$new$1$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m132lambda$new$1$devcolorccPanel$Editor(View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.0
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.this.m131lambda$new$0$devcolorccPanel$Editor();
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$2$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m142lambda$new$2$devcolorccPanel$Editor() {
            close(true);
        }

        /* JADX INFO: renamed from: lambda$new$3$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m148lambda$new$3$devcolorccPanel$Editor(View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.65
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.this.m142lambda$new$2$devcolorccPanel$Editor();
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$4$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m149lambda$new$4$devcolorccPanel$Editor(int r3) {
            this.tab = r3;
            this.tabs.choose(r3, true);
            showTab(r3, true);
            this.headerAway.to(0.0f);
            this.lastSeen = (r3 == 0 ? this.scroll : this.lookScroll).getScrollY();
        }

        /* JADX INFO: renamed from: lambda$new$6$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m152lambda$new$6$devcolorccPanel$Editor(View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.11
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.this.m151lambda$new$5$devcolorccPanel$Editor();
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$7$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m153lambda$new$7$devcolorccPanel$Editor(int r5, float f, boolean z) {
            if (Glass.look > 2) {
                return;
            }
            float[] fArr = Glass.TUNE_RANGE[r5];
            Glass.tune[Glass.look][r5] = fArr[0] + (Logic.clamp(f) * (fArr[1] - fArr[0]));
            Panel.redraw(this);
        }

        /* JADX INFO: renamed from: lambda$new$8$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m154lambda$new$8$devcolorccPanel$Editor(int r3) {
            this.plainChoice.choose(r3, true);
            Glass.plainColour = r3;
            Panel.redraw(this);
        }

        /* JADX INFO: renamed from: lambda$new$9$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m155lambda$new$9$devcolorccPanel$Editor(float f, boolean z) {
            Panel.this.blurCustom = Logic.clamp(f) * Panel.BLUR_CUSTOM_MAX;
            if (z) {
                Panel.this.reblur();
            }
        }

        /* JADX INFO: renamed from: lambda$new$10$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m133lambda$new$10$devcolorccPanel$Editor(int r2) {
            this.draftRows = r2 + 1;
            this.rowsChoice.choose(r2, true);
        }

        /* JADX INFO: renamed from: lambda$new$11$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m134lambda$new$11$devcolorccPanel$Editor(int r3) {
            this.draftIconMode = r3;
            this.iconChoice.choose(r3, true);
            showColour();
        }

        /* JADX INFO: renamed from: lambda$new$12$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m135lambda$new$12$devcolorccPanel$Editor(float f, boolean z) {
            pickColour(0, f * 360.0f);
        }

        /* JADX INFO: renamed from: lambda$new$13$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m136lambda$new$13$devcolorccPanel$Editor(float f, boolean z) {
            pickColour(1, f);
        }

        /* JADX INFO: renamed from: lambda$new$14$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m137lambda$new$14$devcolorccPanel$Editor(float f, boolean z) {
            pickColour(2, (f * 0.75f) + 0.25f);
        }

        /* JADX INFO: renamed from: lambda$new$15$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m138lambda$new$15$devcolorccPanel$Editor(int r3) {
            this.draftNames = r3 == 0;
            this.namesChoice.choose(r3, true);
        }

        static /* synthetic */ void lambda$new$16(float f, boolean z) {
            Slider.jelly = Logic.clamp(f) * 1.5f;
        }

        /* JADX INFO: renamed from: lambda$new$17$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m139lambda$new$17$devcolorccPanel$Editor(int r3) {
            this.draftNotif = r3 == 1;
            this.notifChoice.choose(r3, true);
        }

        /* JADX INFO: renamed from: lambda$new$18$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m140lambda$new$18$devcolorccPanel$Editor(int r3) {
            this.draftFooter = r3 == 1;
            this.footerChoice.choose(r3, true);
        }

        /* JADX INFO: renamed from: lambda$new$19$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m141lambda$new$19$devcolorccPanel$Editor(int r3) {
            this.draftSwipe = r3 == 0;
            this.swipeChoice.choose(r3, true);
        }

        /* JADX INFO: renamed from: lambda$new$20$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m143lambda$new$20$devcolorccPanel$Editor(View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.54
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.this.resetLook();
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$21$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m144lambda$new$21$devcolorccPanel$Editor(float f, float f2) {
            placeTabs(f);
        }

        /* JADX INFO: renamed from: lambda$new$22$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m145lambda$new$22$devcolorccPanel$Editor(float f, float f2) {
            placeHeader(f);
        }

        /* JADX INFO: renamed from: lambda$new$24$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m147lambda$new$24$devcolorccPanel$Editor(final View view, int r2, final int r3, int r4, final int r5) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.6
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.this.m146lambda$new$23$devcolorccPanel$Editor(view, r3, r5);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$23$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m146lambda$new$23$devcolorccPanel$Editor(View view, int r3, int r4) {
            if (!this.wide) {
                if (view != (this.tab == 0 ? this.scroll : this.lookScroll)) {
                    return;
                }
            }
            followScroll(r3, r4);
        }

        private ScrollView scroller(Context context, View view) {
            ScrollView scrollView = new ScrollView(context);
            scrollView.setVerticalScrollBarEnabled(false);
            scrollView.setOverScrollMode(2);
            scrollView.setVerticalFadingEdgeEnabled(true);
            scrollView.setFadingEdgeLength(Panel.this.dp(28.0f));
            view.setPadding(Panel.this.dp(14.0f), 0, Panel.this.dp(14.0f), Panel.this.dp(94.0f));
            scrollView.addView(view, new FrameLayout.LayoutParams(-1, -2));
            scrollView.setOutlineProvider(ViewOutlineProvider.BOUNDS);
            scrollView.setClipToOutline(true);
            return scrollView;
        }

        private void followScroll(int r4, int r5) {
            if (this.active && this.edgeGrid == null) {
                Panel.this.fitEditor();
                int r6 = r4 + Math.round((this.body.getTop() - this.top.getTop()) * this.headerAway.value());
                if (r6 > Panel.this.dp(8.0f) && r6 >= this.lastSeen - Panel.this.dp(6.0f)) {
                    if (r6 > this.lastSeen + Panel.this.dp(6.0f) && r6 > Panel.this.dp(48.0f)) {
                        this.headerAway.to(1.0f);
                    }
                } else {
                    this.headerAway.to(0.0f);
                }
                if (Math.abs(r6 - this.lastSeen) > Panel.this.dp(6.0f) || r6 <= Panel.this.dp(8.0f)) {
                    this.lastSeen = r6;
                }
            }
        }

        private void placeHeader(float f) {
            int top = this.body.getTop() - this.top.getTop();
            float f2 = (-top) * f;
            View[] viewArr = {this.top, this.tabs};
            for (int r4 = 0; r4 < 2; r4++) {
                View view = viewArr[r4];
                view.setTranslationY(f2);
                view.setAlpha(1.0f - f);
            }
            this.body.setTranslationY(f2);
            if (this.body.getHeight() > 0) {
                int r0 = Math.round(top * (1.0f - f));
                ScrollView[] scrollViewArr = {this.scroll, this.lookScroll};
                for (int r3 = 0; r3 < 2; r3++) {
                    View childAt = scrollViewArr[r3].getChildAt(0);
                    if (childAt.getPaddingBottom() != r0) {
                        childAt.setPadding(childAt.getPaddingLeft(), childAt.getPaddingTop(), childAt.getPaddingRight(), r0);
                    }
                }
            }
            this.top.setVisibility(f > Panel.FULLY_OPEN ? Panel.TILES_PER_ROW : 0);
            if (this.wide) {
                return;
            }
            this.tabs.setVisibility(f > Panel.FULLY_OPEN ? Panel.TILES_PER_ROW : 0);
        }

        private LinearLayout.LayoutParams paneParams() {
            return new LinearLayout.LayoutParams(0, -1, 1.0f);
        }

        @Override // android.widget.FrameLayout, android.view.View
        protected void onMeasure(int r10, int r11) {
            int size = View.MeasureSpec.getSize(r10);
            boolean z = size > View.MeasureSpec.getSize(r11) || Panel.this.large();
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.scroll.getLayoutParams();
            LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.lookScroll.getLayoutParams();
            if (!z) {
                int r7 = Panel.this.dp(320.0f);
                layoutParams2.width = r7;
                layoutParams.width = r7;
                layoutParams2.weight = 0.0f;
                layoutParams.weight = 0.0f;
                layoutParams2.leftMargin = Panel.this.dp(24.0f);
            } else {
                layoutParams2.width = 0;
                layoutParams.width = 0;
                layoutParams2.weight = 1.0f;
                layoutParams.weight = 1.0f;
                layoutParams2.leftMargin = Panel.this.dp(-4.0f);
            }
            if (z != this.wide) {
                this.wide = z;
                this.tabs.setVisibility(z ? 8 : 0);
                placeTabs(this.tabAt.value());
            }
            this.scale = this.wide ? Panel.this.paneScale : 1.0f;
            FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) this.page.getLayoutParams();
            layoutParams3.width = this.wide ? Math.min(Math.round(size / this.scale), Panel.this.dp(656.0f)) : Panel.this.dp(340.0f);
            layoutParams3.height = this.scale < 1.0f ? Math.round(View.MeasureSpec.getSize(r11) / this.scale) : -1;
            this.page.setPivotX(layoutParams3.width / 2.0f);
            this.page.setPivotY(0.0f);
            this.page.setScaleX(this.scale);
            this.page.setScaleY(this.scale);
            super.onMeasure(r10, r11);
        }

        void logFit() {
            if (this.active) {
                int[] r1 = new int[2];
                int[] r0 = new int[2];
                getLocationOnScreen(r1);
                this.scroll.getLocationOnScreen(r0);
                ColorCCBridge.log("editor fit: editor " + r1[1] + " to " + (r1[1] + getHeight()) + " of a " + getRootView().getHeight() + " px window, bottom margin " + ((FrameLayout.LayoutParams) getLayoutParams()).bottomMargin + ", lists from " + r0[1] + " " + this.scroll.getHeight() + " px tall at scale " + this.scale + ", layout content " + this.scroll.getChildAt(0).getHeight() + ", look content " + this.lookScroll.getChildAt(0).getHeight() + ", panel translation " + Panel.this.getTranslationY() + ", panel height " + Panel.this.getHeight());
            }
        }

        private void showTab(int r2, boolean z) {
            Motion.Value value = this.tabAt;
            if (z) {
                value.to(r2);
            } else {
                value.snap(r2);
            }
            placeTabs(this.tabAt.value());
        }

        private void placeTabs(float f) {
            ScrollView scrollView = this.scroll;
            View[] viewArr = {scrollView, this.lookScroll};
            int r2 = Math.round((((LinearLayout.LayoutParams) scrollView.getLayoutParams()).width + Panel.this.dp(24.0f)) * f);
            for (int r4 = 0; r4 < 2; r4++) {
                View view = viewArr[r4];
                float fMin = this.wide ? 0.0f : Math.min(1.0f, Math.abs(r4 - f));
                view.setTranslationX(this.wide ? 0.0f : -r2);
                view.setAlpha(1.0f - (0.7f * fMin));
                float f2 = 1.0f - (Panel.STAGGER_START * fMin);
                view.setScaleX(f2);
                view.setScaleY(f2);
                view.setVisibility(fMin < 0.999f ? 0 : Panel.TILES_PER_ROW);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void applyShown(float f, float f2) {
            float fClamp = Logic.clamp(f);
            Panel.this.content.setAlpha(1.0f - fClamp);
            int size = this.parts.size();
            int r3 = size - 1;
            float fMax = 0.45f / Math.max(1, r3);
            int r5 = 0;
            for (int r7 = 0; r7 < size; r7++) {
                float fClamp2 = Logic.clamp((f - ((this.active ? r7 : r3 - r7) * fMax)) / 0.55f);
                View view = this.parts.get(r7);
                view.setAlpha(fClamp2);
                float f3 = fClamp2 - 1.0f;
                boolean z = this.active;
                float f4 = z ? (2.7f * f3 * f3 * f3) + 1.0f + (1.7f * f3 * f3) : fClamp2;
                Panel panel = Panel.this;
                view.setTranslationY((z ? panel.dp(32.0f) : -panel.dp(20.0f)) * (1.0f - f4));
                float f5 = (fClamp2 * Panel.STAGGER_START) + 0.94f;
                view.setScaleX(f5);
                view.setScaleY(f5);
            }
            boolean z2 = f2 != 0.0f;
            if (z2 != this.layered) {
                this.layered = z2;
                Iterator<View> it = this.parts.iterator();
                while (true) {
                    int r8 = 2;
                    if (!it.hasNext()) {
                        break;
                    }
                    View next = it.next();
                    if (!z2) {
                        r8 = 0;
                    }
                    next.setLayerType(r8, null);
                }
                Panel.this.content.setLayerType((z2 && Panel.this.buttonsLift == 0.0f) ? 2 : 0, null);
            }
            LinearLayout linearLayout = Panel.this.content;
            if (this.active && !z2 && fClamp >= 1.0f) {
                r5 = Panel.TILES_PER_ROW;
            }
            linearLayout.setVisibility(r5);
            if (this.active || z2 || f > 0.0f) {
                return;
            }
            setVisibility(8);
            this.chosenGrid.clear();
            this.availableGrid.clear();
            this.blockGrid.clear();
            this.hiddenGrid.clear();
        }

        private TextView section(Context context, String str, int r7) {
            TextView textViewLabel = Panel.this.label(context, 14.0f, -1, 1);
            textViewLabel.setText(str);
            textViewLabel.setTypeface(Typeface.DEFAULT_BOLD);
            textViewLabel.setPadding(Panel.this.dp(4.0f), Panel.this.dp(r7), 0, Panel.this.dp(10.0f));
            return textViewLabel;
        }

        private TextView hint(Context context, String str) {
            TextView textViewLabel = Panel.this.label(context, Panel.DROP_STEP, -1711276033, 2);
            textViewLabel.setText(str);
            textViewLabel.setPadding(Panel.this.dp(4.0f), Panel.this.dp(8.0f), 0, Panel.this.dp(8.0f));
            return textViewLabel;
        }

        private SeekLine tuneLine(Context context, LinearLayout linearLayout, String str, float f, float f2, final Slider.Listener listener) {
            SeekLine seekLine = Panel.this.new SeekLine(context);
            seekLine.readout = hint(context, str);
            seekLine.name = str;
            seekLine.from = f;
            seekLine.to = f2;
            linearLayout.addView(seekLine.readout);
            seekLine.setContentDescription(str);
            seekLine.listener = new Slider.Listener() { // from class: dev.colorcc.Panel.Editor.67
                @Override // dev.colorcc.Slider.Listener
                public final void onChange(float f3, boolean z) {
                    Editor.this.m157lambda$tuneLine$26$devcolorccPanel$Editor(listener, f3, z);
                }
            };
            linearLayout.addView(seekLine, new LinearLayout.LayoutParams(-1, Panel.this.dp(28.0f)));
            return seekLine;
        }

        /* JADX INFO: renamed from: lambda$tuneLine$26$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m157lambda$tuneLine$26$devcolorccPanel$Editor(final Slider.Listener listener, final float f, final boolean z) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.61
                @Override // java.lang.Runnable
                public final void run() {
                    listener.onChange(f, z);
                }
            });
        }

        private View preview(Context context) {
            LinearLayout linearLayoutHorizontal = Panel.horizontal(context);
            linearLayoutHorizontal.setGravity(16);
            linearLayoutHorizontal.setClipChildren(false);
            linearLayoutHorizontal.setPadding(0, Panel.this.dp(8.0f), 0, Panel.this.dp(10.0f));
            Panel panel = Panel.this;
            Disc disc = panel.new Disc(context, panel.dp(Panel.ARC));
            this.previewOff = disc;
            disc.setClickable(false);
            this.previewOff.icon.setImageDrawable(Icons.of("M1,9l2,2c4.97,-4.97 13.03,-4.97 18,0l2,-2C16.93,2.93 7.08,2.93 1,9zM9,17l3,3 3,-3c-1.65,-1.66 -4.34,-1.66 -6,0zM5,13l2,2c2.76,-2.76 7.24,-2.76 10,0l2,-2C15.14,9.14 8.87,9.14 5,13z"));
            Panel panel2 = Panel.this;
            Disc disc2 = panel2.new Disc(context, panel2.dp(Panel.ARC));
            this.previewOn = disc2;
            disc2.setClickable(false);
            this.previewOn.icon.setImageDrawable(Icons.of("M17.71,7.71L12,2h-1v7.59L6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 11,14.41V22h1l5.71,-5.71 -4.3,-4.29 4.3,-4.29zM13,5.83l1.88,1.88L13,9.59V5.83zM14.88,16.29L13,18.17v-3.76l1.88,1.88z"));
            this.previewOn.level.snap(1.0f);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Panel.this.dp(54.0f), Panel.this.dp(54.0f));
            layoutParams.rightMargin = Panel.this.dp(Panel.DROP_STEP);
            linearLayoutHorizontal.addView(this.previewOff, layoutParams);
            linearLayoutHorizontal.addView(this.previewOn, new LinearLayout.LayoutParams(layoutParams));
            View view = new View(context);
            view.setBackground(new Glass(-1.0f, Panel.GLASS_IDLE, Panel.GLASS_IDLE, 0.5f, 0.5f));
            linearLayoutHorizontal.addView(view, new LinearLayout.LayoutParams(0, Panel.this.dp(54.0f), 1.0f));
            View view2 = new View(context);
            view2.setBackground(new Glass(-1.0f, Panel.GLASS_IDLE, Panel.GLASS_IDLE, 0.5f, 0.5f).recessed());
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(Panel.this.dp(30.0f), Panel.this.dp(54.0f));
            layoutParams2.leftMargin = Panel.this.dp(Panel.DROP_STEP);
            linearLayoutHorizontal.addView(view2, layoutParams2);
            Glass.previewRoot = linearLayoutHorizontal;
            return linearLayoutHorizontal;
        }

        private void showTunes() {
            boolean z = Glass.look <= 2;
            this.tuneBox.setVisibility(z ? 0 : 8);
            this.plainTitle.setVisibility(Glass.look == 3 ? 0 : 8);
            this.plainChoice.setVisibility(Glass.look == 3 ? 0 : 8);
            if (z) {
                for (int r0 = 0; r0 < 7; r0++) {
                    float[] fArr = Glass.TUNE_RANGE[r0];
                    SeekLine seekLine = this.tuneLines[r0];
                    float f = Glass.tune[Glass.look][r0];
                    float f2 = fArr[0];
                    seekLine.set((f - f2) / (fArr[1] - f2));
                }
            }
        }

        private void showColour() {
            this.colourRows.setVisibility(this.draftIconMode == 2 ? 0 : 8);
            int color = Color.HSVToColor(this.draftHsv);
            ((GradientDrawable) this.swatch.getBackground()).setColor(color);
            this.hueLine.set(this.draftHsv[0] / 360.0f);
            this.satLine.set(this.draftHsv[1]);
            this.valLine.set((this.draftHsv[2] - 0.25f) / 0.75f);
            SeekLine[] seekLineArr = {this.hueLine, this.satLine, this.valLine};
            for (int r2 = 0; r2 < 3; r2++) {
                seekLineArr[r2].light(color);
            }
            Disc disc = this.previewOn;
            int r3 = this.draftIconMode;
            if (r3 != 2) {
                color = r3 == 1 ? getContext().getColor(android.R.color.system_accent1_600) : Panel.ACTIVE_BLUE;
            }
            disc.accent(color);
        }

        private void pickColour(int r2, float f) {
            this.draftHsv[r2] = f;
            showColour();
        }

        private TextView pill(Context context, String str, float f, boolean z) {
            TextView textViewLabel = Panel.this.label(context, f, -1, 1);
            textViewLabel.setText(str);
            textViewLabel.setGravity(17);
            textViewLabel.setIncludeFontPadding(false);
            textViewLabel.setBackground(new Glass(-1.0f, Panel.GLASS_CARD_ON, Panel.GLASS_DISC_ON, 0.5f, 0.5f));
            Panel panel = Panel.this;
            panel.spring(textViewLabel, panel.litLevel, Logic.Spring.FLOOD).snap(z ? 1.0f : 0.0f);
            Panel.this.squish(textViewLabel);
            return textViewLabel;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void setLook(int r3) {
            this.draftLook = r3;
            this.lookChoice.choose(r3, true);
            Glass.look = r3;
            showTunes();
            Panel.redraw(this);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void setBlur(int r4) {
            this.blurChoice.choose(r4, true);
            this.blurBox.setVisibility(r4 == 3 ? 0 : 8);
            this.blurLine.set(Panel.this.blurCustom / Panel.BLUR_CUSTOM_MAX);
            if (r4 == Panel.this.blur) {
                return;
            }
            Panel.this.blur = r4;
            Panel.this.reblur();
        }

        /* JADX WARN: Type inference fix 'apply assigned field type' failed
        java.lang.UnsupportedOperationException: ArgType.getObject(), call class: class jadx.core.dex.instructions.args.ArgType$UnknownArg
        	at jadx.core.dex.instructions.args.ArgType.getObject(ArgType.java:596)
        	at jadx.core.dex.attributes.nodes.ClassTypeVarsAttr.getTypeVarsMapFor(ClassTypeVarsAttr.java:35)
        	at jadx.core.dex.nodes.utils.TypeUtils.replaceClassGenerics(TypeUtils.java:177)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.insertExplicitUseCast(FixTypesVisitor.java:397)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryFieldTypeWithNewCasts(FixTypesVisitor.java:359)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.applyFieldType(FixTypesVisitor.java:309)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.visit(FixTypesVisitor.java:94)
         */
        void open() {
            Object obj = ColorCCBridge.host;
            if (obj == null || this.active) {
                return;
            }
            this.active = true;
            this.before = new ArrayList();
            Iterator it = ((List) ReflectUtils.callMethod(obj, "getSpecs", new Object[0])).iterator();
            while (it.hasNext()) {
                this.before.add(Panel.specOf(it.next()));
            }
            this.live = new HashMap();
            for (Object obj2 : Panel.hostTiles()) {
                this.live.put((String) ReflectUtils.callMethod(obj2, "getTileSpec", new Object[0]), obj2);
            }
            this.fixed.clear();
            for (String str : this.before) {
                if (Logic.kind(str) != 0 || !shows(str)) {
                    this.fixed.add(str);
                }
            }
            fill(obj, this.before, Panel.this.layout, Panel.this.places, Panel.this.pins);
            this.lookBefore = Glass.look;
            int r8 = Glass.look;
            this.draftLook = r8;
            this.lookChoice.choose(r8, false);
            this.blurBefore = Panel.this.blur;
            this.blurCustomBefore = Panel.this.blurCustom;
            this.blurChoice.choose(Panel.this.blur, false);
            this.blurBox.setVisibility(Panel.this.blur == 3 ? 0 : 8);
            this.blurLine.set(Panel.this.blurCustom / Panel.BLUR_CUSTOM_MAX);
            this.draftRows = Panel.this.tileRows;
            this.rowsChoice.choose(Panel.this.tileRows - 1, false);
            this.draftIconMode = Panel.this.iconMode;
            this.iconChoice.choose(Panel.this.iconMode, false);
            Color.colorToHSV(Panel.this.customColour, this.draftHsv);
            float[] fArr = this.draftHsv;
            fArr[2] = Math.max(0.25f, fArr[2]);
            showColour();
            this.tuneBefore = new float[3][];
            for (int r9 = 0; r9 < 3; r9++) {
                this.tuneBefore[r9] = (float[]) Glass.tune[r9].clone();
            }
            int r10 = Glass.plainColour;
            this.plainBefore = r10;
            this.plainChoice.choose(r10, false);
            showTunes();
            this.jellyBefore = Slider.jelly;
            this.jellyLine.set(Slider.jelly / 1.5f);
            boolean z = PageSwipe.moveRows;
            this.draftSwipe = z;
            this.swipeChoice.choose(!z ? 1 : 0, false);
            this.draftNames = Panel.this.tileNames;
            this.namesChoice.choose(!Panel.this.tileNames ? 1 : 0, false);
            boolean z2 = ColorCCBridge.notifGlass;
            this.draftNotif = z2;
            this.notifChoice.choose(z2 ? 1 : 0, false);
            boolean z3 = ColorCCBridge.footerGlass;
            this.draftFooter = z3;
            this.footerChoice.choose(z3 ? 1 : 0, false);
            this.tab = 0;
            this.tabs.choose(0, false);
            showTab(0, false);
            this.headerAway.snap(0.0f);
            placeHeader(0.0f);
            this.lastSeen = 0;
            this.scroll.scrollTo(0, 0);
            this.lookScroll.scrollTo(0, 0);
            this.page.setPadding(Panel.this.dp(24.0f), Panel.this.headerPadding, Panel.this.dp(24.0f), Panel.this.dp(Panel.DROP_HEADER));
            if (getVisibility() != 0) {
                this.shown.snap(0.0f);
            }
            setVisibility(0);
            this.shown.to(1.0f);
        }

        private void fill(final Object obj, List<String> list, String str, String str2, Map<String, int[]> map) {
            this.chosenGrid.clear();
            this.availableGrid.clear();
            this.blockGrid.clear();
            this.hiddenGrid.clear();
            HashSet hashSet = new HashSet();
            ArrayList<String> arrayList = new ArrayList();
            for (String str3 : list) {
                hashSet.add(str3);
                hashSet.add(samsungSpec(str3));
                if (Logic.kind(str3) == 0 && shows(str3)) {
                    if (map.containsKey(str3)) {
                        arrayList.add(str3);
                    } else {
                        addCell(this.chosenGrid, str3);
                    }
                }
            }
            final ArrayList arrayList2 = new ArrayList();
            Iterator<String> it = candidates(obj).iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                String next = it.next();
                String strSamsungSpec = samsungSpec(next);
                int r9 = (hashSet.contains(next) || hashSet.contains(strSamsungSpec)) ? 1 : 0;
                hashSet.add(next);
                hashSet.add(strSamsungSpec);
                if (r9 == 0) {
                    arrayList2.add(next);
                }
            }
            final int r4 = this.fills + 1;
            this.fills = r4;
            this.tilesHint.setVisibility(8);
            this.allIn.setVisibility(8);
            final Runnable runnable = new Runnable() { // from class: dev.colorcc.Panel.Editor.2
                int next;

                @Override // java.lang.Runnable
                public void run() {
                    if (r4 == Editor.this.fills && Editor.this.active) {
                        long jUptimeMillis = SystemClock.uptimeMillis();
                        while (this.next < arrayList2.size() && SystemClock.uptimeMillis() - jUptimeMillis < 8) {
                            List list2 = arrayList2;
                            int r3 = this.next;
                            this.next = r3 + 1;
                            String str4 = (String) list2.get(r3);
                            if (Editor.this.usable(obj, str4) && Editor.this.shows(str4)) {
                                Editor editor = Editor.this;
                                editor.addCell(editor.availableGrid, str4);
                            }
                        }
                        int r0 = this.next;
                        int size = arrayList2.size();
                        Editor editor2 = Editor.this;
                        if (r0 >= size) {
                            editor2.showAddHints();
                        } else {
                            editor2.postOnAnimation(new Runnable() { // from class: dev.colorcc.Panel.Editor.2.0
                                @Override // java.lang.Runnable
                                public final void run() {
                                    m160lambda$run$0$devcolorccPanel$Editor$2();
                                }
                            });
                        }
                    }
                }

                /* JADX INFO: renamed from: lambda$run$0$dev-colorcc-Panel$Editor$2, reason: not valid java name */
                /* synthetic */ void m160lambda$run$0$devcolorccPanel$Editor$2() {
                    Panel.this.guard(this);
                }
            };
            postDelayed(new Runnable() { // from class: dev.colorcc.Panel.Editor.60
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.this.m130lambda$fill$27$devcolorccPanel$Editor(runnable);
                }
            }, Panel.EDITOR_SETTLE_MS);
            int[][] r5 = Logic.sizes(str2, str);
            int[][] r2 = Logic.spots(str2, str, Panel.TILES_PER_ROW);
            ArrayList arrayList3 = new ArrayList();
            ArrayList arrayList4 = new ArrayList();
            int r11 = Integer.MAX_VALUE;
            for (int r12 = 0; r12 < str.length(); r12++) {
                if (str.charAt(r12) == 'g') {
                    r11 = r2[r12][1];
                } else {
                    this.blockGrid.addCell(blockCell(str.charAt(r12), r5[0][r12], r5[1][r12]), this.blockGrid.order.size());
                    arrayList3.add(r2[r12]);
                    arrayList4.add(Integer.valueOf(r5[1][r12]));
                }
            }
            int size = arrayList3.size();
            ArrayList arrayList5 = new ArrayList();
            for (String str4 : arrayList) {
                View viewPinCell = pinCell(str4);
                if (viewPinCell != null) {
                    arrayList5.add(viewPinCell);
                    arrayList3.add(map.get(str4));
                    arrayList4.add(Integer.valueOf(Logic.pinSize(map.get(str4))[1]));
                }
            }
            int size2 = arrayList4.size();
            int[] r6 = new int[size2];
            for (int r8 = 0; r8 < size2; r8++) {
                r6[r8] = ((Integer) arrayList4.get(r8)).intValue();
            }
            int[][] r7 = Logic.liftWith((int[][]) arrayList3.toArray(new int[0][]), r6, r11);
            int[][] r10 = (int[][]) Arrays.copyOf(r7, size2);
            int r3 = Logic.tilesRow(r10, r6, r7[size2][1]);
            int[][] r13 = Logic.below(r10, r3, 1);
            this.blockGrid.addCell(tilesCell(), this.blockGrid.order.size());
            int[][] r14 = (int[][]) Arrays.copyOf(r13, size + 1);
            r14[size] = new int[]{0, r3};
            this.blockGrid.spots = r14;
            for (int r15 = 0; r15 < arrayList5.size(); r15++) {
                this.blockGrid.addCell((View) arrayList5.get(r15), 0, r13[size + r15]);
            }
            for (char c : "wbmlvsd".toCharArray()) {
                if (str.toLowerCase(Locale.ROOT).indexOf(c) < 0) {
                    View viewBlockCell = blockCell(c, Logic.spanW(c), Logic.spanH(c));
                    glyph(viewBlockCell, false);
                    TileGrid tileGrid = this.hiddenGrid;
                    tileGrid.addCell(viewBlockCell, tileGrid.order.size());
                }
            }
            this.cardsHint.setVisibility(this.hiddenGrid.order.isEmpty() ? 8 : 0);
        }

        /* JADX INFO: renamed from: lambda$fill$27$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m130lambda$fill$27$devcolorccPanel$Editor(Runnable runnable) {
            Panel.this.guard(runnable);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void showAddHints() {
            int r2 = 8;
            this.cardsHint.setVisibility(this.hiddenGrid.order.isEmpty() ? 8 : 0);
            this.tilesHint.setVisibility(this.availableGrid.order.isEmpty() ? 8 : 0);
            TextView textView = this.allIn;
            if (this.hiddenGrid.order.isEmpty() && this.availableGrid.order.isEmpty()) {
                r2 = 0;
            }
            textView.setVisibility(r2);
        }

        private View tilesCell() {
            Context context = getContext();
            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.setTag("g41");
            frameLayout.setContentDescription(Panel.this.word("tiles", new Object[0]));
            frameLayout.setBackground(new Glass(-1.0f, Panel.GLASS_IDLE, Panel.GLASS_IDLE, 0.5f, 0.5f));
            LinearLayout linearLayoutHorizontal = Panel.horizontal(context);
            linearLayoutHorizontal.setGravity(17);
            for (int r4 = 0; r4 < Panel.TILES_PER_ROW; r4++) {
                View view = new View(context);
                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setShape(1);
                gradientDrawable.setColor(-2130706433);
                view.setBackground(gradientDrawable);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Panel.this.dp(Panel.DROP_STEP), Panel.this.dp(Panel.DROP_STEP));
                int r8 = Panel.this.dp(3.0f);
                layoutParams.rightMargin = r8;
                layoutParams.leftMargin = r8;
                linearLayoutHorizontal.addView(view, layoutParams);
            }
            TextView textViewLabel = Panel.this.label(context, Panel.DROP_STEP, -1, 1);
            textViewLabel.setText(Panel.this.word("tiles", new Object[0]));
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, -2);
            layoutParams2.leftMargin = Panel.this.dp(10.0f);
            linearLayoutHorizontal.addView(textViewLabel, layoutParams2);
            frameLayout.addView(linearLayoutHorizontal, new FrameLayout.LayoutParams(-1, -1));
            wire(frameLayout);
            return frameLayout;
        }

        private boolean isTiles(View view) {
            return "g41".equals(view.getTag());
        }

        private String nameOf(char c) {
            return Panel.this.word(new String[]{"wifi", "bluetooth", "media", "brightness", "volume", "sound_modes", "mobile_data"}["wbmlvsd".indexOf(Character.toLowerCase(c))], new Object[0]);
        }

        private View blockCell(char c, int r6, int r7) {
            Context context = getContext();
            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.setClipChildren(false);
            frameLayout.setTag("" + c + r6 + r7);
            frameLayout.setContentDescription(nameOf(c));
            frameLayout.addView(face(c), new FrameLayout.LayoutParams(-1, -1));
            frameLayout.addView(badge(context, frameLayout), new FrameLayout.LayoutParams(Panel.this.dp(Panel.ARC), Panel.this.dp(Panel.ARC), 8388659));
            addHandle(context, frameLayout);
            wire(frameLayout);
            return frameLayout;
        }

        private void addHandle(Context context, FrameLayout frameLayout) {
            ResizeHandle resizeHandle = Panel.this.new ResizeHandle(context);
            resizeHandle.setTag(HANDLE);
            resizeHandle.setImportantForAccessibility(2);
            frameLayout.addView(resizeHandle, new FrameLayout.LayoutParams(Panel.this.dp(26.0f), Panel.this.dp(26.0f), 8388693));
            frameLayout.setAccessibilityDelegate(new View.AccessibilityDelegate() { // from class: dev.colorcc.Panel.Editor.3
                @Override // android.view.View.AccessibilityDelegate
                public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                    super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                    if (view.getParent() == Editor.this.blockGrid) {
                        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(Editor.RESIZE_ACTION, Panel.this.word("resize", new Object[0])));
                    }
                }

                @Override // android.view.View.AccessibilityDelegate
                public boolean performAccessibilityAction(final View view, int r3, Bundle bundle) {
                    if (r3 == Editor.RESIZE_ACTION) {
                        Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.3.0
                            @Override // java.lang.Runnable
                            public final void run() {
                                m161lambda$performAccessibilityAction$0$devcolorccPanel$Editor$3(view);
                            }
                        });
                        return true;
                    }
                    return super.performAccessibilityAction(view, r3, bundle);
                }

                /* JADX INFO: renamed from: lambda$performAccessibilityAction$0$dev-colorcc-Panel$Editor$3, reason: not valid java name */
                /* synthetic */ void m161lambda$performAccessibilityAction$0$devcolorccPanel$Editor$3(View view) {
                    Editor.this.resize(view);
                }
            });
        }

        private View face(char c) {
            Context context = getContext();
            char lowerCase = Character.toLowerCase(c);
            if (lowerCase == 'l' || lowerCase == 'v') {
                return Panel.this.new SliderFace(context, lowerCase == 'l');
            }
            if (lowerCase == 'm') {
                return Panel.this.new MediaFace(context);
            }
            Panel panel = Panel.this;
            final Disc disc = panel.new Disc(context, panel.dp(Panel.DROP_HEADER));
            disc.setClickable(false);
            if (lowerCase == 's') {
                disc.icon.setImageDrawable(Icons.of("M18,16v-5c0,-3.07 -1.63,-5.64 -4.5,-6.32V4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5v0.68C7.64,5.36 6,7.92 6,11v5l-2,2v1h16v-1l-2,-2zM12,22c1.1,0 2,-0.9 2,-2h-4c0,1.1 0.9,2 2,2z"));
                disc.level.snap(1.0f);
            } else {
                int r2 = lowerCase != 'w' ? lowerCase == Panel.TILE_ROW_HEIGHT ? 2 : 3 : 1;
                for (Map.Entry<String, Object> entry : this.live.entrySet()) {
                    if (Logic.kind(entry.getKey()) == r2) {
                        disc.style(entry.getKey());
                        try {
                            this.bindings.add(Panel.this.new Bound(entry.getValue(), new Consumer() { // from class: dev.colorcc.Panel.Editor.7
                                @Override // java.util.function.Consumer
                                public final void accept(Object obj) {
                                    disc.render(obj, false);
                                }
                            }));
                            break;
                        } catch (ClassNotFoundException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                }
            }
            return Panel.this.new CardFace(context, disc, nameOf(c));
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void resize(View view) {
            String str = (String) view.getTag();
            int[] r0 = Logic.nextSize(str.charAt(0), str.charAt(1) - '0', str.charAt(2) - '0');
            setSize(view, r0[0], r0[1]);
        }

        void setSize(View view, int r9, int r10) {
            String str = (String) view.getTag();
            boolean z = str.charAt(1) + 65488 == r9 && str.charAt(2) + 65488 == r10;
            float width = view.getWidth() * view.getScaleX();
            float height = view.getHeight() * view.getScaleY();
            view.setTag("" + str.charAt(0) + r9 + r10);
            if (!z) {
                growFrom(view, width, height);
            }
            this.blockGrid.settle(view);
        }

        private void growFrom(View view, final float f, final float f2) {
            Object tag = view.getTag(GROW_LISTENER);
            if (tag instanceof View.OnLayoutChangeListener) {
                view.removeOnLayoutChangeListener((View.OnLayoutChangeListener) tag);
            }
            final int width = view.getWidth();
            final int height = view.getHeight();
            final Object obj = new Object();
            view.setTag(GROW_TURN, obj);
            View.OnLayoutChangeListener onLayoutChangeListener = new View.OnLayoutChangeListener() { // from class: dev.colorcc.Panel.Editor.4
                @Override // android.view.View.OnLayoutChangeListener
                public void onLayoutChange(final View view2, int r2, int r3, int r4, int r5, int r6, int r7, int r8, int r9) {
                    int r10 = r4 - r2;
                    if (r10 == width && r5 - r3 == height) {
                        return;
                    }
                    view2.removeOnLayoutChangeListener(this);
                    view2.setTag(Editor.GROW_LISTENER, null);
                    view2.setPivotX(0.0f);
                    view2.setPivotY(0.0f);
                    Panel.this.spring(view2, View.SCALE_X, Logic.Spring.BOUNCY).snap(f / Math.max(1, r10));
                    Panel.this.spring(view2, View.SCALE_Y, Logic.Spring.BOUNCY).snap(f2 / Math.max(1, r5 - r3));
                    Panel.this.scaleTo(view2, 1.0f, Logic.Spring.BOUNCY);
                    final Object obj2 = obj;
                    view2.postDelayed(new Runnable() { // from class: dev.colorcc.Panel.Editor.4.0
                        @Override // java.lang.Runnable
                        public final void run() {
                            lambda$onLayoutChange$0(view2, obj2);
                        }
                    }, 600L);
                }

                static /* synthetic */ void lambda$onLayoutChange$0(View view2, Object obj2) {
                    if (view2.getTag(Editor.GROW_TURN) != obj2) {
                        return;
                    }
                    view2.setPivotX(view2.getWidth() / 2.0f);
                    view2.setPivotY(view2.getHeight() / 2.0f);
                }
            };
            view.setTag(GROW_LISTENER, onLayoutChangeListener);
            view.addOnLayoutChangeListener(onLayoutChangeListener);
        }

        private TextView badge(Context context, final View view) {
            TextView textViewLabel = Panel.this.label(context, Panel.DROP_HEADER, -1, 1);
            textViewLabel.setTag(BADGE);
            textViewLabel.setText("−");
            textViewLabel.setGravity(17);
            textViewLabel.setIncludeFontPadding(false);
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(1);
            gradientDrawable.setColor(Panel.BADGE_REMOVE);
            gradientDrawable.setStroke(Panel.this.dp(1.0f), 1090519039);
            textViewLabel.setBackground(gradientDrawable);
            textViewLabel.setContentDescription(Panel.this.word("remove", new Object[0]));
            textViewLabel.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Editor.64
                @Override // android.view.View.OnClickListener
                public final void onClick(View view2) {
                    Editor.this.m114lambda$badge$30$devcolorccPanel$Editor(view, view2);
                }
            });
            return textViewLabel;
        }

        /* JADX INFO: renamed from: lambda$badge$30$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m114lambda$badge$30$devcolorccPanel$Editor(final View view, View view2) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.73
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.this.m113lambda$badge$29$devcolorccPanel$Editor(view);
                }
            });
        }

        private void glyph(View view, boolean z) {
            TextView textView = (TextView) view.findViewWithTag(BADGE);
            if (textView == null) {
                return;
            }
            View view2 = textView;
            while (true) {
                if (view2 == view) {
                    break;
                }
                ViewGroup viewGroup = (ViewGroup) view2.getParent();
                for (int r2 = 0; r2 < viewGroup.getChildCount(); r2++) {
                    if (viewGroup.getChildAt(r2) != view2) {
                        viewGroup.getChildAt(r2).setAlpha(z ? 1.0f : 0.4f);
                    }
                }
                view2 = (View) view2.getParent();
            }
            textView.setText(z ? "−" : "+");
            textView.setContentDescription(Panel.this.word(z ? "remove" : "add", new Object[0]));
            ((GradientDrawable) textView.getBackground()).setColor(z ? Panel.BADGE_REMOVE : Panel.ACTIVE_BLUE);
            View viewFindViewWithTag = view.findViewWithTag(HANDLE);
            if (viewFindViewWithTag != null) {
                viewFindViewWithTag.setVisibility(z ? 0 : 8);
            }
        }

        private void wire(View view) {
            view.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Editor.62
                @Override // android.view.View.OnClickListener
                public final void onClick(View view2) {
                    Editor.this.m159lambda$wire$32$devcolorccPanel$Editor(view2);
                }
            });
            view.setOnTouchListener(new View.OnTouchListener() { // from class: dev.colorcc.Panel.Editor.63
                @Override // android.view.View.OnTouchListener
                public final boolean onTouch(View view2, MotionEvent motionEvent) {
                    return Editor.lambda$wire$33(view2, motionEvent);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$wire$32$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m159lambda$wire$32$devcolorccPanel$Editor(final View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.12
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.this.m158lambda$wire$31$devcolorccPanel$Editor(view);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$wire$31$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m158lambda$wire$31$devcolorccPanel$Editor(View view) {
            ViewParent parent = view.getParent();
            if (parent == this.hiddenGrid || parent == this.availableGrid) {
                m113lambda$badge$29$devcolorccPanel$Editor(view);
            }
        }

        static /* synthetic */ boolean lambda$wire$33(View view, MotionEvent motionEvent) {
            ViewParent parent = view.getParent();
            return (parent instanceof TileGrid) && ((TileGrid) parent).drag(view, motionEvent);
        }

        private List<String> candidates(Object obj) {
            List<String> listDefaults = defaults(obj);
            List<String> listSpecList = specList("sec_quick_settings_tiles_stock");
            if (listSpecList.isEmpty()) {
                listSpecList = specList("quick_settings_tiles_stock");
            }
            listDefaults.addAll(listSpecList);
            listDefaults.addAll(appTiles());
            return listDefaults;
        }

        private List<String> appTiles() {
            final ArrayList arrayList = new ArrayList();
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.59
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.this.m112lambda$appTiles$34$devcolorccPanel$Editor(arrayList);
                }
            });
            return arrayList;
        }

        /* JADX INFO: renamed from: lambda$appTiles$34$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m112lambda$appTiles$34$devcolorccPanel$Editor(List list) {
            Iterator<ResolveInfo> it = getContext().getPackageManager().queryIntentServices(new Intent("android.service.quicksettings.action.QS_TILE"), 128).iterator();
            while (it.hasNext()) {
                ServiceInfo serviceInfo = it.next().serviceInfo;
                if ("android.permission.BIND_QUICK_SETTINGS_TILE".equals(serviceInfo.permission) && (serviceInfo.metaData == null || !serviceInfo.metaData.getBoolean("android.service.quicksettings.SEM_DEFAULT_TILE_DEXMODE_ONLY"))) {
                    list.add("custom(" + new ComponentName(serviceInfo.packageName, serviceInfo.name).flattenToShortString() + ")");
                }
            }
        }

        private String samsungSpec(final String str) {
            Object objQuiet = quiet(new Callable() { // from class: dev.colorcc.Panel.Editor.10
                @Override // java.util.concurrent.Callable
                public final Object call() throws Exception {
                    return Editor.this.m156lambda$samsungSpec$35$devcolorccPanel$Editor(str);
                }
            });
            return objQuiet instanceof String ? (String) objQuiet : str;
        }

        /* JADX INFO: renamed from: lambda$samsungSpec$35$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ Object m156lambda$samsungSpec$35$devcolorccPanel$Editor(String str) throws Exception {
            return ReflectUtils.callStaticMethod(ReflectUtils.findClass("com.android.systemui.qs.pipeline.data.repository.TileNameConverter", getContext().getClassLoader()), "toTileSpec", new Object[]{getResources(), str});
        }

        private List<String> defaults(final Object obj) {
            final ArrayList<String> arrayList = new ArrayList<>();
            try {
                arrayList.addAll(com.android.systemui.qs.QSHost.getDefaultSpecs(getResources()));
            } catch (Throwable ignored) {
            }
            if (arrayList.isEmpty()) {
                arrayList.addAll(specList("quick_settings_tiles_default"));
            }
            return arrayList;
        }

        static /* synthetic */ void lambda$defaults$36(Object obj, List list) {
            Iterator it = ((List) ReflectUtils.callMethod(obj, "getDefaultTileList", new Object[0])).iterator();
            while (it.hasNext()) {
                list.add(Panel.specOf(it.next()));
            }
        }

        private List<String> specList(String str) {
            ArrayList arrayList = new ArrayList();
            int identifier = getResources().getIdentifier(str, "string", "com.android.systemui");
            if (identifier != 0) {
                for (String str2 : getResources().getString(identifier).split(",")) {
                    arrayList.add(str2.trim());
                }
            }
            return arrayList;
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* JADX WARN: Code restructure failed: missing block: B:11:0x003d, code lost:
        
            if (java.lang.Boolean.FALSE.equals(quiet(new dev.colorcc.Panel.Editor.AnonymousClass57())) != false) goto L12;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        public boolean shows(final String str) {
            if (str.startsWith("custom(") && str.endsWith(")")) {
                ComponentName cn = ComponentName.unflattenFromString(str.substring(7, str.length() - 1));
                if (cn == null) {
                    return false;
                }
                try {
                    getContext().getPackageManager().getServiceInfo(cn, 0);
                } catch (Exception unused) {
                    return false;
                }
            }
            Object tile = tileFor(str);
            if (tile == null) {
                return false;
            }
            if (tile instanceof com.android.systemui.plugins.qs.QSTile) {
                return ((com.android.systemui.plugins.qs.QSTile) tile).isAvailable();
            }
            return !Boolean.FALSE.equals(quiet(() -> lambda$shows$38(tile)));
        }

        static /* synthetic */ Object lambda$shows$37(String str) throws Exception {
            return ReflectUtils.callMethod(ColorCCBridge.host, "isAvailableCustomTile", new Object[]{str});
        }

        static /* synthetic */ Object lambda$shows$38(Object obj) throws Exception {
            return ReflectUtils.callMethod(obj, "isAvailable", new Object[0]);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean usable(final Object obj, final String str) {
            return (str.isEmpty() || Logic.kind(str) != 0 || Boolean.TRUE.equals(quiet(new Callable() { // from class: dev.colorcc.Panel.Editor.8
                @Override // java.util.concurrent.Callable
                public final Object call() throws Exception {
                    return Editor.lambda$usable$39(obj, str);
                }
            })) || Boolean.TRUE.equals(quiet(new Callable() { // from class: dev.colorcc.Panel.Editor.9
                @Override // java.util.concurrent.Callable
                public final Object call() throws Exception {
                    return Editor.lambda$usable$40(obj, str);
                }
            }))) ? false : true;
        }

        static /* synthetic */ Object lambda$usable$39(Object obj, String str) throws Exception {
            return ReflectUtils.callMethod(obj, "isUnsupportedTile", new Object[]{str});
        }

        static /* synthetic */ Object lambda$usable$40(Object obj, String str) throws Exception {
            return ReflectUtils.callMethod(obj, "shouldBeHiddenByKnox", new Object[]{str});
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* JADX INFO: renamed from: reset, reason: merged with bridge method [inline-methods] */
        public void m151lambda$new$5$devcolorccPanel$Editor() {
            Object obj = ColorCCBridge.host;
            if (obj == null || !this.active) {
                return;
            }
            List<String> arrayList = new ArrayList<>(this.fixed);
            for (String str : defaults(obj)) {
                if (!arrayList.contains(str) && usable(obj, str)) {
                    arrayList.add(str);
                }
            }
            if (arrayList.size() == this.fixed.size()) {
                arrayList = this.before;
            }
            List<String> list = arrayList;
            String[] strArrWithTiles = Logic.withTiles("wbmlvsd", null);
            fill(obj, list, strArrWithTiles[0], strArrWithTiles[1], new HashMap());
            resetLook();
            this.scroll.smoothScrollTo(0, 0);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void resetLook() {
            if (this.active) {
                setLook(0);
                Panel.this.blurCustom = 1.0f;
                setBlur(1);
                this.draftRows = 3;
                this.rowsChoice.choose(2, true);
                this.draftIconMode = 0;
                this.iconChoice.choose(0, true);
                showColour();
                this.draftNames = true;
                this.namesChoice.choose(0, true);
                this.draftSwipe = false;
                this.swipeChoice.choose(0, true);
                for (float[] fArr : Glass.tune) {
                    Arrays.fill(fArr, 1.0f);
                }
                Glass.plainColour = 0;
                this.plainChoice.choose(0, true);
                showTunes();
                Slider.jelly = 1.0f;
                this.jellyLine.set(0.6666667f);
                this.draftNotif = true;
                this.notifChoice.choose(1, true);
                this.draftFooter = true;
                this.footerChoice.choose(1, true);
                Panel.redraw(this);
                this.lookScroll.smoothScrollTo(0, 0);
            }
        }

        private Object quiet(Callable<Object> callable) {
            try {
                return callable.call();
            } catch (Throwable unused) {
                return null;
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void addCell(TileGrid tileGrid, String str) {
            addCell(tileGrid, str, tileGrid.order.size());
        }

        private Object tileFor(final String str) {
            Object obj = this.live.get(str);
            if (obj != null) {
                return obj;
            }
            final Object objQuiet = quiet(new Callable() { // from class: dev.colorcc.Panel.Editor.55
                @Override // java.util.concurrent.Callable
                public final Object call() throws Exception {
                    return Editor.lambda$tileFor$41(str);
                }
            });
            if (objQuiet == null) {
                return null;
            }
            this.created.add(objQuiet);
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.56
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.lambda$tileFor$42(objQuiet);
                }
            });
            return objQuiet;
        }

        static /* synthetic */ Object lambda$tileFor$41(String str) throws Exception {
            return ReflectUtils.callMethod(ColorCCBridge.host, "createTile", new Object[]{str});
        }

        static /* synthetic */ void lambda$tileFor$42(Object obj) {
            ReflectUtils.callMethod(obj, "refreshState", new Object[0]);
        }

        private void addCell(TileGrid tileGrid, String str, int r19) {
            Object objTileFor = tileFor(str);
            if (objTileFor == null) {
                return;
            }
            Context context = getContext();
            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.setClipChildren(false);
            frameLayout.setTag(str);
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(1);
            linearLayout.setGravity(1);
            linearLayout.setClipChildren(false);
            FrameLayout frameLayout2 = new FrameLayout(context);
            frameLayout2.setClipChildren(false);
            Panel panel = Panel.this;
            final Disc disc = panel.new Disc(context, panel.dp(26.0f));
            disc.setClickable(false);
            disc.style(str);
            frameLayout2.addView(disc, new FrameLayout.LayoutParams(-1, -1));
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(Panel.this.dp(Panel.ARC), Panel.this.dp(Panel.ARC), 8388659);
            layoutParams.leftMargin = -Panel.this.dp(3.0f);
            layoutParams.topMargin = -Panel.this.dp(3.0f);
            frameLayout2.addView(badge(context, frameLayout), layoutParams);
            linearLayout.addView(frameLayout2, new LinearLayout.LayoutParams(Panel.this.dp(63.0f), Panel.this.dp(63.0f)));
            final TextView textViewLabel = Panel.this.label(context, 10.0f, -1, 2);
            textViewLabel.setGravity(1);
            textViewLabel.setIncludeFontPadding(false);
            textViewLabel.setText(str);
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams2.topMargin = Panel.this.dp(3.0f);
            linearLayout.addView(textViewLabel, layoutParams2);
            frameLayout.addView(linearLayout, new FrameLayout.LayoutParams(-1, -1));
            try {
                this.bindings.add(Panel.this.new Bound(objTileFor, new Consumer() { // from class: dev.colorcc.Panel.Editor.28
                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        Editor.lambda$addCell$43(disc, textViewLabel, obj);
                    }
                }));
                wire(frameLayout);
                glyph(frameLayout, tileGrid.reorder);
                tileGrid.addCell(frameLayout, r19);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }

        static /* synthetic */ void lambda$addCell$43(Disc disc, TextView textView, Object obj) {
            disc.render(obj, false);
            CharSequence charSequenceText = Panel.text(obj, "label");
            if (TextUtils.isEmpty(charSequenceText)) {
                return;
            }
            Panel.setTextIfChanged(textView, charSequenceText);
        }

        private View pinCell(String str) {
            Object objTileFor = tileFor(str);
            if (objTileFor == null) {
                return null;
            }
            Context context = getContext();
            final FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.setClipChildren(false);
            int[] r5 = Logic.pinSize((int[]) Panel.this.pins.get(str));
            frameLayout.setTag("p" + r5[0] + r5[1]);
            frameLayout.setTag(PIN_SPEC, str);
            Panel panel = Panel.this;
            final Disc disc = panel.new Disc(context, panel.dp(Panel.DROP_HEADER));
            disc.setClickable(false);
            disc.style(str);
            final CardFace cardFace = Panel.this.new CardFace(context, disc, "");
            cardFace.setBackground(null);
            cardFace.fill = true;
            frameLayout.addView(cardFace, new FrameLayout.LayoutParams(-1, -1));
            frameLayout.addView(badge(context, frameLayout), new FrameLayout.LayoutParams(Panel.this.dp(Panel.ARC), Panel.this.dp(Panel.ARC), 8388659));
            addHandle(context, frameLayout);
            try {
                this.bindings.add(Panel.this.new Bound(objTileFor, new Consumer() { // from class: dev.colorcc.Panel.Editor.13
                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        Editor.lambda$pinCell$44(disc, frameLayout, cardFace, obj);
                    }
                }));
                wire(frameLayout);
                return frameLayout;
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }

        static /* synthetic */ void lambda$pinCell$44(Disc disc, FrameLayout frameLayout, CardFace cardFace, Object obj) {
            disc.render(obj, false);
            frameLayout.setContentDescription(Panel.text(obj, "label"));
            CharSequence charSequenceText = Panel.text(obj, "label");
            if (TextUtils.isEmpty(charSequenceText)) {
                return;
            }
            Panel.setTextIfChanged(cardFace.name, charSequenceText);
        }

        private boolean isPin(View view) {
            return view.getTag(PIN_SPEC) != null;
        }

        boolean overBlocks(float f) {
            int[] r0 = new int[2];
            this.blockGrid.getLocationOnScreen(r0);
            return f < ((float) r0[1]) + (((float) this.blockGrid.getHeight()) * this.scale);
        }

        boolean drop(final TileGrid tileGrid, final View view, float f, final float f2) {
            final Runnable runnable;
            Editor editor;
            if (isTiles(view)) {
                return false;
            }
            final float[] fArr = {f, f2};
            if ((tileGrid == this.chosenGrid || tileGrid == this.availableGrid) && overBlocks(f2)) {
                runnable = new Runnable() { // from class: dev.colorcc.Panel.Editor.69
                    @Override // java.lang.Runnable
                    public final void run() {
                        Editor.this.m126lambda$drop$45$devcolorccPanel$Editor(view, fArr);
                    }
                };
                editor = this;
            } else if (tileGrid == this.blockGrid && isPin(view)) {
                if (overBlocks(f2)) {
                    editor = this;
                    runnable = null;
                } else {
                    editor = this;
                    runnable = new Runnable() { // from class: dev.colorcc.Panel.Editor.70
                        @Override // java.lang.Runnable
                        public final void run() {
                            Editor.this.m127lambda$drop$46$devcolorccPanel$Editor(view, tileGrid, f2, fArr);
                        }
                    };
                }
            } else {
                editor = this;
                runnable = editor.across(tileGrid, f2) ? new Runnable() { // from class: dev.colorcc.Panel.Editor.71
                    @Override // java.lang.Runnable
                    public final void run() {
                        Editor.this.m128lambda$drop$47$devcolorccPanel$Editor(view, fArr);
                    }
                } : null;
            }
            if (runnable == null) {
                return false;
            }
            editor.post(new Runnable() { // from class: dev.colorcc.Panel.Editor.72
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.this.m129lambda$drop$48$devcolorccPanel$Editor(runnable);
                }
            });
            return true;
        }

        /* JADX INFO: renamed from: lambda$drop$46$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m127lambda$drop$46$devcolorccPanel$Editor(View view, TileGrid tileGrid, float f, float[] fArr) {
            unpin(view, across(tileGrid, f) ? this.availableGrid : this.chosenGrid, fArr);
        }

        /* JADX INFO: renamed from: lambda$drop$48$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m129lambda$drop$48$devcolorccPanel$Editor(Runnable runnable) {
            Panel.this.guard(runnable);
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* JADX INFO: renamed from: pin, reason: merged with bridge method [inline-methods] */
        public void m126lambda$drop$45$devcolorccPanel$Editor(View view, float[] fArr) {
            View viewPinCell;
            if ((view.getParent() instanceof TileGrid) && (viewPinCell = pinCell((String) view.getTag())) != null) {
                ((TileGrid) view.getParent()).removeCell(view);
                glyph(viewPinCell, true);
                int[] r5 = this.blockGrid.cellAt(fArr[0], fArr[1]);
                this.blockGrid.addCell(viewPinCell, 0, r5);
                this.blockGrid.placeAt(viewPinCell, r5);
                showAddHints();
                popIn(viewPinCell);
            }
        }

        private void unpin(View view, TileGrid tileGrid, float[] fArr) {
            if (view.getParent() instanceof TileGrid) {
                String str = (String) view.getTag(PIN_SPEC);
                this.blockGrid.removeCell(view);
                int[] r7 = (fArr == null || !tileGrid.reorder) ? null : tileGrid.cellAt(fArr[0], fArr[1]);
                int r5 = Math.min(tileGrid.order.size(), r7 == null ? tileGrid.order.size() : (r7[1] * tileGrid.columns) + r7[0]);
                addCell(tileGrid, str, r5);
                showAddHints();
                if (r5 < tileGrid.order.size()) {
                    popIn(tileGrid.order.get(r5));
                }
            }
        }

        private void popIn(View view) {
            Panel.this.spring(view, View.SCALE_X, Logic.Spring.BOUNCY).snap(0.7f);
            Panel.this.spring(view, View.SCALE_Y, Logic.Spring.BOUNCY).snap(0.7f);
            Panel.this.scaleTo(view, 1.0f, Logic.Spring.BOUNCY);
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* JADX INFO: renamed from: move, reason: merged with bridge method [inline-methods] */
        public void m113lambda$badge$29$devcolorccPanel$Editor(View view) {
            m128lambda$drop$47$devcolorccPanel$Editor(view, null);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* JADX INFO: renamed from: move, reason: merged with bridge method [inline-methods] */
        public void m128lambda$drop$47$devcolorccPanel$Editor(View view, float[] fArr) {
            if (!(view.getParent() instanceof TileGrid) || isTiles(view)) {
                return;
            }
            if (isPin(view)) {
                unpin(view, this.availableGrid, null);
                return;
            }
            TileGrid tileGrid = (TileGrid) view.getParent();
            TileGrid tileGrid2 = this.chosenGrid;
            TileGrid tileGrid3 = this.availableGrid;
            if (tileGrid == tileGrid2) {
                tileGrid2 = tileGrid3;
            } else if (tileGrid != tileGrid3 && tileGrid == (tileGrid2 = this.blockGrid)) {
                tileGrid2 = this.hiddenGrid;
            }
            int[] r3 = new int[2];
            view.getLocationOnScreen(r3);
            int size = 0;
            final float width = r3[0] + ((view.getWidth() * view.getScaleX()) / 2.0f);
            final float height = r3[1] + ((view.getHeight() * view.getScaleY()) / 2.0f);
            tileGrid.removeCell(view);
            int[] r1 = fArr != null ? tileGrid2.cellAt(fArr[0], fArr[1]) : null;
            if (r1 != null) {
                size = r1[0] + (r1[1] * tileGrid2.columns);
            } else if (tileGrid2.reorder) {
                size = tileGrid2.order.size();
            }
            tileGrid2.addCell(view, size, r1);
            glyph(view, tileGrid2.reorder);
            showAddHints();
            tileGrid.setTranslationZ(0.0f);
            tileGrid2.setTranslationZ(Panel.this.dp(1.0f));
            view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: dev.colorcc.Panel.Editor.5
                @Override // android.view.View.OnLayoutChangeListener
                public void onLayoutChange(View view2, int r2, int r4, int r5, int r6, int r7, int r8, int r9, int r10) {
                    if (r5 - r2 == 0) {
                        return;
                    }
                    view2.removeOnLayoutChangeListener(this);
                    int[] r11 = new int[2];
                    view2.getLocationOnScreen(r11);
                    float width2 = r11[0] + ((view2.getWidth() * view2.getScaleX()) / 2.0f);
                    float height2 = r11[1] + ((view2.getHeight() * view2.getScaleY()) / 2.0f);
                    Motion.Value valueSpring = Panel.this.spring(view2, View.TRANSLATION_X, Logic.Spring.REVEAL);
                    Motion.Value valueSpring2 = Panel.this.spring(view2, View.TRANSLATION_Y, Logic.Spring.REVEAL);
                    valueSpring.snap(((width - width2) / Editor.this.scale) + view2.getTranslationX());
                    valueSpring2.snap(((height - height2) / Editor.this.scale) + view2.getTranslationY());
                    valueSpring.to(0.0f);
                    valueSpring2.to(0.0f);
                }
            });
            Panel.this.spring(view, View.SCALE_X, Logic.Spring.BOUNCY).snap(0.7f);
            Panel.this.spring(view, View.SCALE_Y, Logic.Spring.BOUNCY).snap(0.7f);
            Panel.this.scaleTo(view, 1.0f, Logic.Spring.BOUNCY);
        }

        boolean across(TileGrid tileGrid, float f) {
            int[] r0 = new int[2];
            this.chosenGrid.getLocationOnScreen(r0);
            return (tileGrid == this.blockGrid || tileGrid == this.chosenGrid) == ((f > (((float) r0[1]) + (((float) (this.chosenGrid.getHeight() + Panel.this.dp(Panel.DROP_STEP))) * this.scale)) ? 1 : (f == (((float) r0[1]) + (((float) (this.chosenGrid.getHeight() + Panel.this.dp(Panel.DROP_STEP))) * this.scale)) ? 0 : -1)) > 0);
        }

        /* JADX INFO: renamed from: lambda$new$49$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m150lambda$new$49$devcolorccPanel$Editor() {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.66
                @Override // java.lang.Runnable
                public final void run() {
                    Editor.this.stepEdge();
                }
            });
        }

        void edgeScroll(TileGrid tileGrid, View view, float f, float f2) {
            this.edgeGrid = tileGrid;
            this.edgeCell = view;
            this.edgeX = f;
            this.edgeY = f2;
            removeCallbacks(this.edgeStep);
            if (tileGrid != null) {
                postOnAnimation(this.edgeStep);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void stepEdge() {
            float f;
            if (this.edgeGrid == null) {
                return;
            }
            int[] r0 = new int[2];
            this.scroll.getLocationOnScreen(r0);
            float fDp = Panel.this.dp(72.0f);
            float f2 = this.edgeY;
            int r1 = r0[1];
            float f3 = f2 - r1;
            float height = (r1 + (this.scroll.getHeight() * this.scale)) - this.edgeY;
            if (f3 < fDp) {
                f = -(1.0f - (f3 / fDp));
            } else {
                f = height < fDp ? 1.0f - (height / fDp) : 0.0f;
            }
            int scrollY = this.scroll.getScrollY();
            this.scroll.scrollBy(0, Math.round(Math.max(-1.5f, Math.min(1.5f, f)) * Panel.this.dp(14.0f)));
            if (this.scroll.getScrollY() == scrollY) {
                return;
            }
            this.edgeGrid.follow(this.edgeCell, this.edgeX, this.edgeY);
            postOnAnimation(this.edgeStep);
        }

        void close(boolean z) {
            int r13;
            int r16;
            if (this.active) {
                this.active = false;
                edgeScroll(null, null, 0.0f, 0.0f);
                try {
                    if (z) {
                        if (this.draftLook != this.lookBefore) {
                            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.14
                                @Override // java.lang.Runnable
                                public final void run() {
                                    Editor.this.m115lambda$close$50$devcolorccPanel$Editor();
                                }
                            });
                        }
                        if (this.draftRows != Panel.this.tileRows) {
                            Panel.this.tileRows = this.draftRows;
                            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.19
                                @Override // java.lang.Runnable
                                public final void run() {
                                    Editor.this.m116lambda$close$51$devcolorccPanel$Editor();
                                }
                            });
                            Panel.this.currentTiles = new ArrayList();
                        }
                        if (this.draftSwipe != PageSwipe.moveRows) {
                            PageSwipe.moveRows = this.draftSwipe;
                            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.20
                                @Override // java.lang.Runnable
                                public final void run() {
                                    Editor.this.m117lambda$close$52$devcolorccPanel$Editor();
                                }
                            });
                        }
                        if (this.draftNames != Panel.this.tileNames) {
                            Panel.this.tileNames = this.draftNames;
                            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.21
                                @Override // java.lang.Runnable
                                public final void run() {
                                    Editor.this.m118lambda$close$53$devcolorccPanel$Editor();
                                }
                            });
                            Panel.this.currentTiles = new ArrayList();
                        }
                        int r5 = Color.HSVToColor(this.draftHsv);
                        if (this.draftIconMode != Panel.this.iconMode || (this.draftIconMode == 2 && r5 != Panel.this.customColour)) {
                            Panel.this.iconMode = this.draftIconMode;
                            if (Panel.this.iconMode == 2) {
                                Panel.this.customColour = r5;
                            }
                            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.22
                                @Override // java.lang.Runnable
                                public final void run() {
                                    Editor.this.m119lambda$close$54$devcolorccPanel$Editor();
                                }
                            });
                            Panel.this.volume.setIconColor(Panel.this.theme(Panel.ACTIVE_BLUE));
                            Panel.this.currentTiles = new ArrayList();
                        }
                        if (Panel.this.blur != this.blurBefore || Panel.this.blurCustom != this.blurCustomBefore) {
                            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.23
                                @Override // java.lang.Runnable
                                public final void run() {
                                    Editor.this.m120lambda$close$55$devcolorccPanel$Editor();
                                }
                            });
                        }
                        if (Glass.plainColour != this.plainBefore) {
                            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.24
                                @Override // java.lang.Runnable
                                public final void run() {
                                    Editor.this.m121lambda$close$56$devcolorccPanel$Editor();
                                }
                            });
                        }
                        if (!Arrays.deepEquals(Glass.tune, this.tuneBefore) || Slider.jelly != this.jellyBefore) {
                            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.25
                                @Override // java.lang.Runnable
                                public final void run() {
                                    Editor.this.m122lambda$close$57$devcolorccPanel$Editor();
                                }
                            });
                        }
                        if (this.draftNotif != ColorCCBridge.notifGlass) {
                            ColorCCBridge.notifGlass = this.draftNotif;
                            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.26
                                @Override // java.lang.Runnable
                                public final void run() {
                                    Editor.this.m123lambda$close$58$devcolorccPanel$Editor();
                                }
                            });
                        }
                        if (this.draftFooter != ColorCCBridge.footerGlass) {
                            ColorCCBridge.footerGlass = this.draftFooter;
                            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.27
                                @Override // java.lang.Runnable
                                public final void run() {
                                    Editor.this.m124lambda$close$59$devcolorccPanel$Editor();
                                }
                            });
                        }
                        Panel.this.redrawNotifications();
                        Glass.look = this.draftLook;
                        Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.15
                            @Override // java.lang.Runnable
                            public final void run() {
                                ColorCCBridge.glazeFooter();
                            }
                        });
                        StringBuilder sb = new StringBuilder();
                        ArrayList<int[]> arrayList = new ArrayList<>();
                        ArrayList<int[]> arrayList2 = new ArrayList<>();
                        LinkedHashMap<String, int[]> linkedHashMap = new LinkedHashMap<>();
                        this.blockGrid.size();
                        int r10 = Integer.MAX_VALUE;
                        int r11 = 0;
                        while (true) {
                            r13 = 1;
                            if (r11 >= this.blockGrid.order.size()) {
                                break;
                            }
                            if (isTiles(this.blockGrid.order.get(r11))) {
                                r10 = this.blockGrid.spots[r11][1];
                            }
                            r11++;
                        }
                        int r12 = 0;
                        while (r12 < this.blockGrid.order.size()) {
                            View view = this.blockGrid.order.get(r12);
                            if (isTiles(view)) {
                                r16 = r13;
                            } else {
                                int[] r14 = this.blockGrid.spots[r12];
                                int r15 = r14[r13];
                                if (r15 > r10) {
                                    r14 = new int[]{r14[0], r15 - 1};
                                }
                                if (!isPin(view)) {
                                    r16 = r13;
                                    sb.append(((String) view.getTag()).charAt(0));
                                    arrayList.add(r14);
                                    arrayList2.add(new int[]{this.blockGrid.sizes[0][r12], this.blockGrid.sizes[r16][r12]});
                                } else {
                                    int r17 = this.blockGrid.sizes[0][r12];
                                    int r4 = this.blockGrid.sizes[r13][r12];
                                    String str = (String) view.getTag(PIN_SPEC);
                                    if (r17 == r13 && r4 == r13) {
                                        r16 = r13;
                                    } else {
                                        r16 = r13;
                                        r14 = new int[]{r14[0], r14[r16], r17, r4};
                                    }
                                    linkedHashMap.put(str, r14);
                                }
                            }
                            r12++;
                            r13 = r16;
                        }
                        int r18 = r13;
                        int[] r19 = new int[2];
                        r19[r18] = arrayList2.size();
                        r19[0] = 2;
                        int[][] r6 = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, r19);
                        for (int r8 = 0; r8 < arrayList2.size(); r8++) {
                            r6[0][r8] = ((int[]) arrayList2.get(r8))[0];
                            r6[r18][r8] = ((int[]) arrayList2.get(r8))[r18];
                        }
                        int r7 = Logic.rows((int[][]) arrayList.toArray(new int[0][]), r6[r18]);
                        for (int[] r110 : linkedHashMap.values()) {
                            r7 = Math.max(r7, r110[r18] + Logic.pinSize(r110)[r18]);
                        }
                        sb.append('g');
                        arrayList.add(new int[]{0, Math.min(r10, r7)});
                        int[] r9 = r6[0];
                        int[] r20 = Arrays.copyOf(r9, r9.length + 1);
                        int[] r21 = r6[r18];
                        int[][] r22 = {r20, Arrays.copyOf(r21, r21.length + 1)};
                        int[] r23 = r22[0];
                        r23[r23.length - 1] = Logic.spanW('g');
                        int[] r24 = r22[r18];
                        r24[r24.length - 1] = Logic.spanH('g');
                        String strLayout = Logic.layout(sb.toString());
                        String strEncode = Logic.encode((int[][]) arrayList.toArray(new int[0][]), r22);
                        String strEncodePins = Logic.encodePins(Panel.this.pins);
                        final String strEncodePins2 = Logic.encodePins(linkedHashMap);
                        ColorCCBridge.log("editor done: cards " + Panel.this.layout + " " + Panel.this.places + " now " + strLayout + " " + strEncode + ", tile bar at row " + r10);
                        if (!strLayout.equals(Panel.this.layout) || !strEncode.equals(Panel.this.places) || !strEncodePins2.equals(strEncodePins)) {
                            Panel.this.layout = strLayout;
                            Panel.this.places = strEncode;
                            Panel.this.pins.clear();
                            Panel.this.pins.putAll(linkedHashMap);
                            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.16
                                @Override // java.lang.Runnable
                                public final void run() {
                                    Editor.this.m125lambda$close$60$devcolorccPanel$Editor(strEncodePins2);
                                }
                            });
                            if (!strEncodePins2.equals(strEncodePins)) {
                                Panel.this.currentTiles = new ArrayList();
                            }
                            Panel.this.blocks.arrange(Panel.this.layout);
                        }
                    } else {
                        if (Panel.this.blur != this.blurBefore || Panel.this.blurCustom != this.blurCustomBefore) {
                            Panel.this.blur = this.blurBefore;
                            Panel.this.blurCustom = this.blurCustomBefore;
                            Panel.this.reblur();
                        }
                        for (int r0 = 0; r0 < 3; r0++) {
                            System.arraycopy(this.tuneBefore[r0], 0, Glass.tune[r0], 0, 7);
                        }
                        Glass.plainColour = this.plainBefore;
                        Slider.jelly = this.jellyBefore;
                    }
                    Glass.look = z ? this.draftLook : this.lookBefore;
                    Panel.redraw(Panel.this);
                    if (z && ColorCCBridge.host != null) {
                        ArrayList arrayList3 = new ArrayList(this.fixed);
                        Iterator<View> it = this.chosenGrid.order.iterator();
                        while (it.hasNext()) {
                            arrayList3.add((String) it.next().getTag());
                        }
                        for (View view2 : this.blockGrid.order) {
                            if (isPin(view2)) {
                                arrayList3.add((String) view2.getTag(PIN_SPEC));
                            }
                        }
                        if (Panel.this.currentTiles.isEmpty() && Panel.this.syncTiles() && Panel.this.open) {
                            Panel.this.setTilesListening();
                        }
                        if (!arrayList3.equals(this.before)) {
                            ReflectUtils.callMethod(ColorCCBridge.host, "changeTilesByUser", new Object[]{new ArrayList(this.before), arrayList3});
                            ColorCCBridge.log("editor done: " + this.before.size() + " tiles now " + arrayList3.size());
                            Panel.this.followTiles(0);
                        }
                    }
                } catch (Throwable th) {
                    ColorCCBridge.log("editor not saved: " + th);
                }
                for (final Bound bound : this.bindings) {
                    Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.17
                        @Override // java.lang.Runnable
                        public final void run() {
                            Editor.lambda$close$61(bound);
                        }
                    });
                }
                this.bindings.clear();
                for (final Object obj : this.created) {
                    Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Editor.18
                        @Override // java.lang.Runnable
                        public final void run() {
                            Editor.lambda$close$62(obj);
                        }
                    });
                }
                this.created.clear();
                this.live = new HashMap();
                this.shown.to(0.0f);
            }
        }

        /* JADX INFO: renamed from: lambda$close$50$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m115lambda$close$50$devcolorccPanel$Editor() {
            Panel.this.prefs().edit().putInt("look", this.draftLook).apply();
        }

        /* JADX INFO: renamed from: lambda$close$51$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m116lambda$close$51$devcolorccPanel$Editor() {
            Panel.this.prefs().edit().putInt("tileRows", Panel.this.tileRows).apply();
        }

        /* JADX INFO: renamed from: lambda$close$52$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m117lambda$close$52$devcolorccPanel$Editor() {
            Panel.this.prefs().edit().putBoolean("notifSwipe", this.draftSwipe).apply();
        }

        /* JADX INFO: renamed from: lambda$close$53$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m118lambda$close$53$devcolorccPanel$Editor() {
            Panel.this.prefs().edit().putBoolean("tileNames", Panel.this.tileNames).apply();
        }

        /* JADX INFO: renamed from: lambda$close$54$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m119lambda$close$54$devcolorccPanel$Editor() {
            Panel.this.prefs().edit().putInt("iconColours", Panel.this.iconMode).putInt("customColour", Panel.this.customColour).apply();
        }

        /* JADX INFO: renamed from: lambda$close$55$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m120lambda$close$55$devcolorccPanel$Editor() {
            Panel.this.prefs().edit().putInt("blur", Panel.this.blur).putFloat("blurCustom", Panel.this.blurCustom).apply();
        }

        /* JADX INFO: renamed from: lambda$close$56$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m121lambda$close$56$devcolorccPanel$Editor() {
            Panel.this.prefs().edit().putInt("plainColour", Glass.plainColour).apply();
        }

        /* JADX INFO: renamed from: lambda$close$57$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m122lambda$close$57$devcolorccPanel$Editor() {
            Panel.this.prefs().edit().putString("glassTune", Logic.encodeTunes(Glass.tune)).putFloat("jelly", Slider.jelly).apply();
        }

        /* JADX INFO: renamed from: lambda$close$58$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m123lambda$close$58$devcolorccPanel$Editor() {
            Panel.this.prefs().edit().putBoolean("notifGlass", this.draftNotif).apply();
        }

        /* JADX INFO: renamed from: lambda$close$59$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m124lambda$close$59$devcolorccPanel$Editor() {
            Panel.this.prefs().edit().putBoolean("footerGlass", this.draftFooter).apply();
        }

        /* JADX INFO: renamed from: lambda$close$60$dev-colorcc-Panel$Editor, reason: not valid java name */
        /* synthetic */ void m125lambda$close$60$devcolorccPanel$Editor(String str) {
            Panel.this.prefs().edit().putString("layout", Panel.this.layout).putString("places", Panel.this.places).putString("pins", str).apply();
        }

        static /* synthetic */ void lambda$close$61(Bound bound) {
            ReflectUtils.callMethod(bound.tile, "removeCallback", new Object[]{bound.callback});
        }

        static /* synthetic */ void lambda$close$62(Object obj) {
            ReflectUtils.callMethod(obj, "destroy", new Object[0]);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void followTiles(final int r3) {
        this.main.postDelayed(new Runnable() { // from class: dev.colorcc.Panel.45
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m12lambda$followTiles$96$devcolorccPanel(r3);
            }
        }, 200L);
    }

    /* JADX INFO: renamed from: lambda$followTiles$96$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m12lambda$followTiles$96$devcolorccPanel(final int r2) {
        guard(new Runnable() { // from class: dev.colorcc.Panel.84
            @Override // java.lang.Runnable
            public final void run() {
                Panel.this.m11lambda$followTiles$95$devcolorccPanel(r2);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$followTiles$95$dev-colorcc-Panel, reason: not valid java name */
    /* synthetic */ void m11lambda$followTiles$95$devcolorccPanel(int r2) {
        if (!syncTiles()) {
            if (r2 < 20) {
                followTiles(r2 + 1);
                return;
            } else {
                ColorCCBridge.log("tiles not changed by Samsung");
                return;
            }
        }
        if (this.open) {
            setTilesListening();
        }
        ColorCCBridge.log("tiles followed after " + ((r2 + 1) * 200) + " ms");
    }

    private final class CardFace extends ViewGroup {
        private final Glass back;
        private final Disc disc;
        boolean fill;
        private final TextView name;

        CardFace(Context context, Disc disc, String str) {
            super(context);
            Glass glass = new Glass(-1.0f, Panel.GLASS_IDLE, Panel.GLASS_IDLE, 0.5f, 0.5f);
            this.back = glass;
            setBackground(glass);
            this.disc = disc;
            addView(disc);
            TextView textViewLabel = Panel.this.label(context, Panel.DROP_STEP, -1, 1);
            this.name = textViewLabel;
            textViewLabel.setText(str);
            addView(textViewLabel);
        }

        @Override // android.view.View
        protected void onMeasure(int r7, int r8) {
            int size = View.MeasureSpec.getSize(r7);
            int size2 = View.MeasureSpec.getSize(r8);
            int r0 = Panel.this.dp(5.0f);
            boolean z = this.fill;
            boolean z2 = z && ((float) size) < ((float) size2) * 1.5f;
            if (z) {
                setBackground(z2 ? null : this.back);
            }
            int r1 = Math.min(size, size2);
            if (!z2) {
                r1 = Math.max(0, Math.min(r1 - (r0 * 2), Panel.this.dp(38.0f)));
            }
            this.disc.measure(View.MeasureSpec.makeMeasureSpec(r1, 1073741824), View.MeasureSpec.makeMeasureSpec(r1, 1073741824));
            int r2 = ((size - r1) - (r0 * 3)) - Panel.this.dp(4.0f);
            this.name.setVisibility(r2 >= Panel.this.dp(Panel.DROP_TOP) ? 0 : 8);
            if (r2 >= Panel.this.dp(Panel.DROP_TOP)) {
                this.name.measure(View.MeasureSpec.makeMeasureSpec(r2, Integer.MIN_VALUE), 0);
            }
            setMeasuredDimension(size, size2);
        }

        @Override // android.view.ViewGroup, android.view.View
        protected void onLayout(boolean z, int r5, int r6, int r7, int r8) {
            int r9 = r7 - r5;
            int r10 = r8 - r6;
            int r4 = Panel.this.dp(5.0f);
            int measuredWidth = this.disc.getMeasuredWidth();
            boolean z2 = this.name.getVisibility() == 0;
            int r11 = z2 ? r4 : (r9 - measuredWidth) / 2;
            int r2 = r11 + measuredWidth;
            this.disc.layout(r11, (r10 - measuredWidth) / 2, r2, (measuredWidth + r10) / 2);
            if (z2) {
                int r3 = r2 + r4 + Panel.this.dp(2.0f);
                int measuredHeight = this.name.getMeasuredHeight();
                TextView textView = this.name;
                textView.layout(r3, (r10 - measuredHeight) / 2, textView.getMeasuredWidth() + r3, (r10 + measuredHeight) / 2);
            }
        }
    }

    private final class SliderFace extends View {
        private final RectF box;
        private final Paint fill;
        private final Drawable glyph;
        private final Path pill;

        SliderFace(Context context, boolean z) {
            super(context);
            Paint paint = new Paint(1);
            this.fill = paint;
            this.pill = new Path();
            this.box = new RectF();
            setBackground(new Glass(-1.0f, Panel.GLASS_IDLE, Panel.GLASS_IDLE, 0.5f, 0.5f).recessed());
            Drawable drawableOf = z ? Icons.of("M12,7c-2.76,0 -5,2.24 -5,5s2.24,5 5,5 5,-2.24 5,-5 -2.24,-5 -5,-5zM2,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1H2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1zM20,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1h-2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1zM11,2v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1V2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1zM11,20v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1v-2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1zM5.99,4.58c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39,0.39 1.03,0.39 1.41,0s0.39,-1.03 0,-1.41L5.99,4.58zM18.36,16.95c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39,0.39 1.03,0.39 1.41,0 0.39,-0.39 0.39,-1.03 0,-1.41l-1.06,-1.06zM19.42,5.99c0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06zM7.05,18.36c0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06z") : Icons.speaker();
            this.glyph = drawableOf;
            drawableOf.setLevel(10000);
            drawableOf.setTint(z ? -14671832 : Panel.this.theme(Panel.ACTIVE_BLUE));
            paint.setColor(-218103809);
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            float f;
            Canvas canvas2;
            float width = getWidth();
            float height = getHeight();
            float fPillRadius = Glass.pillRadius(width, height);
            this.box.set(0.0f, 0.0f, width, height);
            this.pill.rewind();
            this.pill.addRoundRect(this.box, fPillRadius, fPillRadius, Path.Direction.CW);
            boolean z = height >= width;
            canvas.save();
            canvas.clipPath(this.pill);
            Paint paint = this.fill;
            if (z) {
                canvas.drawRect(0.0f, height * 0.5f, width, height, paint);
                f = height;
                canvas2 = canvas;
            } else {
                f = height;
                canvas2 = canvas;
                canvas2.drawRect(0.0f, 0.0f, width * 0.5f, f, paint);
            }
            canvas2.restore();
            int r12 = Math.round(Math.min(Panel.this.dp(Panel.ARC), Math.min(width, f) * Panel.STAGGER_SPAN));
            int r2 = Math.round(z ? width / 2.0f : width * 0.25f);
            int r0 = Math.round(z ? 0.75f * f : f / 2.0f);
            int r13 = r12 / 2;
            this.glyph.setBounds(r2 - r13, r0 - r13, r2 + r13, r0 + r13);
            this.glyph.draw(canvas2);
        }
    }

    private final class MediaFace extends FrameLayout {
        MediaFace(Context context) {
            super(context);
            setBackground(new Glass(Panel.this.dp(18.0f), Panel.GLASS_IDLE, Panel.GLASS_IDLE, 0.5f, 0.5f));
            setClipChildren(false);
            ImageView imageViewGlyph = glyph(context, "M12,3v10.55c-0.59,-0.34 -1.27,-0.55 -2,-0.55 -2.21,0 -4,1.79 -4,4s1.79,4 4,4 4,-1.79 4,-4V7h4V3h-6z", -1275068417);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(Panel.this.dp(Panel.DROP_HEADER), Panel.this.dp(Panel.DROP_HEADER), 8388659);
            int r4 = Panel.this.dp(10.0f);
            layoutParams.topMargin = r4;
            layoutParams.leftMargin = r4;
            addView(imageViewGlyph, layoutParams);
            TextView textViewLabel = Panel.this.label(context, Panel.DROP_STEP, -1, 1);
            textViewLabel.setText(Panel.this.word("media", new Object[0]));
            textViewLabel.setTypeface(Typeface.DEFAULT_BOLD);
            FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-2, -2, 8388659);
            layoutParams2.leftMargin = Panel.this.dp(10.0f);
            layoutParams2.topMargin = Panel.this.dp(32.0f);
            addView(textViewLabel, layoutParams2);
            LinearLayout linearLayoutHorizontal = Panel.horizontal(context);
            linearLayoutHorizontal.setGravity(17);
            String[] strArr = {"M6,6h2v12H6zM9.5,12l8.5,6V6z", "M8,5v14l11,-7z", "M6,18l8.5,-6L6,6v12zM16,6v12h2V6z"};
            for (int r0 = 0; r0 < 3; r0++) {
                String str = strArr[r0];
                LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(Panel.this.dp(14.0f), Panel.this.dp(14.0f));
                int r8 = Panel.this.dp(8.0f);
                layoutParams3.rightMargin = r8;
                layoutParams3.leftMargin = r8;
                linearLayoutHorizontal.addView(glyph(context, str, -1), layoutParams3);
            }
            FrameLayout.LayoutParams layoutParams4 = new FrameLayout.LayoutParams(-1, -2, 80);
            layoutParams4.bottomMargin = Panel.this.dp(10.0f);
            addView(linearLayoutHorizontal, layoutParams4);
        }

        private ImageView glyph(Context context, String str, int r3) {
            ImageView imageView = new ImageView(context);
            imageView.setImageDrawable(Icons.of(str));
            imageView.setImageTintList(ColorStateList.valueOf(r3));
            return imageView;
        }
    }

    private final class Segmented extends FrameLayout {
        private final Motion.Value at;
        private final TextView[] options;
        private final View thumb;

        Segmented(Context context, String[] strArr, final IntConsumer intConsumer) {
            super(context);
            setBackground(new Glass(-1.0f, Panel.GLASS_IDLE, Panel.GLASS_IDLE, 0.5f, 0.5f).recessed());
            View view = new View(context);
            this.thumb = view;
            view.setBackground(new Glass(-1.0f, Panel.GLASS_DISC_ON, Panel.GLASS_DISC_ON, 0.5f, 0.5f));
            addView(view);
            LinearLayout linearLayoutHorizontal = Panel.horizontal(context);
            this.options = new TextView[strArr.length];
            for (int r2 = 0; r2 < strArr.length; r2++) {
                final int optIdx = r2;
                TextView textViewLabel = Panel.this.label(context, 13.0f, -1, 1);
                textViewLabel.setText(strArr[r2]);
                textViewLabel.setGravity(17);
                textViewLabel.setIncludeFontPadding(false);
                textViewLabel.setOnClickListener(new View.OnClickListener() { // from class: dev.colorcc.Panel.Segmented.0
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view2) {
                        Segmented.this.m199lambda$new$1$devcolorccPanel$Segmented(intConsumer, optIdx, view2);
                    }
                });
                this.options[r2] = textViewLabel;
                linearLayoutHorizontal.addView(textViewLabel, new LinearLayout.LayoutParams(0, -1, 1.0f));
            }
            addView(linearLayoutHorizontal, new FrameLayout.LayoutParams(-1, -1));
            this.at = Panel.this.motion.value(Logic.Spring.BOUNCY, new Motion.Listener() { // from class: dev.colorcc.Panel.Segmented.1
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Segmented.this.place(f, f2);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$1$dev-colorcc-Panel$Segmented, reason: not valid java name */
        /* synthetic */ void m199lambda$new$1$devcolorccPanel$Segmented(final IntConsumer intConsumer, final int r2, View view) {
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Segmented.2
                @Override // java.lang.Runnable
                public final void run() {
                    intConsumer.accept(r2);
                }
            });
        }

        void choose(int r2, boolean z) {
            Motion.Value value = this.at;
            if (z) {
                value.to(r2);
            } else {
                value.snap(r2);
            }
            place(this.at.value(), this.at.velocity());
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void place(float f, float f2) {
            int r0 = Panel.this.dp(3.0f);
            int width = (getWidth() - (r0 * 2)) / this.options.length;
            if (width <= 0) {
                return;
            }
            this.thumb.setTranslationX(r0 + (width * f));
            float fMin = Math.min(Panel.MEDIA_EDGE_LIGHT, Math.abs(f2) * Panel.MAX_SMEAR);
            this.thumb.setScaleX(fMin + 1.0f);
            this.thumb.setScaleY(1.0f - (fMin * Panel.MEDIA_EDGE_LIGHT));
            int r8 = 0;
            while (true) {
                TextView[] textViewArr = this.options;
                if (r8 >= textViewArr.length) {
                    return;
                }
                textViewArr[r8].setTextColor(((Integer) Panel.this.argb.evaluate(Logic.clamp(1.0f - Math.abs(f - r8)), -1, -14671832)).intValue());
                r8++;
            }
        }

        @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
        protected void onLayout(boolean z, int r3, int r4, int r5, int r6) {
            super.onLayout(z, r3, r4, r5, r6);
            int r2 = Panel.this.dp(3.0f);
            this.thumb.layout(0, r2, ((r5 - r3) - (r2 * 2)) / this.options.length, (r6 - r4) - r2);
            place(this.at.value(), 0.0f);
        }
    }

    private final class ResizeHandle extends View {
        private final Paint line;
        private final RectF ring;

        ResizeHandle(Context context) {
            super(context);
            Paint paint = new Paint(1);
            this.line = paint;
            this.ring = new RectF();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Panel.this.dp(2.6f));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(-1);
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            float width = getWidth() - Panel.this.dp(Panel.TOGGLE_KICK);
            float f = -width;
            this.ring.set(f, f, width, width);
            canvas.drawArc(this.ring, 8.0f, 74.0f, false, this.line);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void reglaze(View view) {
        if (view.getBackground() instanceof Glass) {
            view.invalidate();
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int r0 = 0; r0 < viewGroup.getChildCount(); r0++) {
                reglaze(viewGroup.getChildAt(r0));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void redraw(View view) {
        view.invalidate();
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int r0 = 0; r0 < viewGroup.getChildCount(); r0++) {
                redraw(viewGroup.getChildAt(r0));
            }
        }
    }

    private final class TileGrid extends ViewGroup {
        private final int[] at;
        private final int cellHeight;
        private final int columns;
        private float downX;
        private float downY;
        private View dragged;
        private float fingerX;
        private float fingerY;
        private final boolean free;
        private final int gap;
        private float grabX;
        private float grabY;
        private Runnable hold;
        private int[][] home;
        final List<View> order;
        final boolean reorder;
        private boolean sized;
        private int[][] sizes;
        private View sizing;
        private final boolean spans;
        int[][] spots;

        TileGrid(Context context, boolean z, int r6, int r7, int r8, boolean z2) {
            super(context);
            this.order = new ArrayList();
            this.at = new int[2];
            this.spots = new int[0][];
            this.sizes = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, 2, 0);
            this.reorder = z;
            this.columns = r6;
            this.cellHeight = r7;
            this.gap = r8;
            this.spans = z2;
            this.free = z2 && z;
            setClipChildren(false);
        }

        void addCell(View view, int r3) {
            addCell(view, r3, null);
        }

        void addCell(View view, int r9, int[] r10) {
            view.layout(0, 0, 0, 0);
            Panel.this.spring(view, View.TRANSLATION_X, Logic.Spring.SNAPPY).snap(0.0f);
            Panel.this.spring(view, View.TRANSLATION_Y, Logic.Spring.SNAPPY).snap(0.0f);
            List<View> list = this.order;
            list.add(this.free ? list.size() : Math.min(r9, list.size()), view);
            addView(view);
            if (this.free) {
                size();
                int size = this.order.size() - 1;
                int[][] r3 = (int[][]) Arrays.copyOf(this.spots, this.order.size());
                this.spots = r3;
                int[][] r8 = this.sizes;
                int[] r1 = r8[0];
                int[] r2 = r8[1];
                if (r10 == null) {
                    r10 = new int[]{0, 0};
                }
                r3[size] = Logic.nearest(r1, r2, r3, size, r10, this.columns);
            }
        }

        int[] cellAt(float f, float f2) {
            getLocationOnScreen(this.at);
            float f3 = Panel.this.editor.scale;
            return new int[]{Math.max(0, Math.min(this.columns - 1, (int) Math.floor(((f - this.at[0]) / f3) / (cellWidth(getWidth()) + this.gap)))), Math.max(0, (int) Math.floor(((f2 - this.at[1]) / f3) / (this.cellHeight + this.gap)))};
        }

        void removeCell(View view) {
            int r0 = this.order.indexOf(view);
            this.order.remove(r0);
            removeView(view);
            if (this.free) {
                ArrayList arrayList = new ArrayList(Arrays.asList(this.spots));
                arrayList.remove(r0);
                size();
                this.spots = Logic.lift((int[][]) arrayList.toArray(new int[0][]), this.sizes[1]);
            }
        }

        void clear() {
            this.order.clear();
            this.spots = new int[0][];
            removeAllViews();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void size() {
            this.sizes = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, 2, this.order.size());
            for (int r2 = 0; r2 < this.order.size(); r2++) {
                String str = this.spans ? (String) this.order.get(r2).getTag() : null;
                this.sizes[0][r2] = str == null ? 1 : str.charAt(1) - '0';
                this.sizes[1][r2] = str == null ? 1 : str.charAt(2) - '0';
            }
        }

        private int place() {
            size();
            if (!this.free) {
                int[][] r0 = this.sizes;
                this.spots = Logic.place(r0[0], r0[1], this.columns);
            }
            return Logic.rows(this.spots, this.sizes[1]);
        }

        void placeAt(View view, int[] r10) {
            size();
            int r4 = this.order.indexOf(view);
            if (r4 < 0 || r10 == null) {
                return;
            }
            int r5 = Math.max(0, Math.min(r10[0], this.columns - this.sizes[0][r4]));
            int[][] r0 = this.sizes;
            this.spots = Logic.lift(Logic.shove(r0[0], r0[1], this.spots, r4, r5, r10[1], this.columns), this.sizes[1]);
            requestLayout();
        }

        void settle(View view) {
            size();
            int r4 = this.order.indexOf(view);
            int r9 = Math.max(0, Math.min(this.spots[r4][0], this.columns - this.sizes[0][r4]));
            int[][] r1 = this.sizes;
            int[] r0 = r1[0];
            if (r0[r4] == 2) {
                r9 -= r9 % 2;
            }
            int[] r2 = r1[1];
            int[][] r3 = this.spots;
            this.spots = Logic.lift(Logic.shove(r0, r2, r3, r4, r9, r3[r4][1], this.columns), this.sizes[1]);
            requestLayout();
        }

        private int cellWidth(int r3) {
            int r0 = this.columns;
            return (r3 - ((r0 - 1) * this.gap)) / r0;
        }

        @Override // android.view.View
        protected void onMeasure(int r10, int r11) {
            int size = View.MeasureSpec.getSize(r10);
            int r12 = place();
            int r0 = cellWidth(size);
            for (int r2 = 0; r2 < this.order.size(); r2++) {
                View view = this.order.get(r2);
                int r5 = this.sizes[0][r2];
                int r6 = View.MeasureSpec.makeMeasureSpec((r5 * r0) + ((r5 - 1) * this.gap), 1073741824);
                int r7 = this.sizes[1][r2];
                view.measure(r6, View.MeasureSpec.makeMeasureSpec((this.cellHeight * r7) + ((r7 - 1) * this.gap), 1073741824));
            }
            setMeasuredDimension(size, r12 != 0 ? (this.cellHeight * r12) + ((r12 - 1) * this.gap) : 0);
        }

        @Override // android.view.ViewGroup, android.view.View
        protected void onLayout(boolean z, int r11, int r12, int r13, int r14) {
            place();
            int r10 = cellWidth(r13 - r11);
            for (int r15 = 0; r15 < this.order.size(); r15++) {
                View view = this.order.get(r15);
                int[] r16 = this.spots[r15];
                int r0 = r16[0];
                int r1 = this.gap;
                int r2 = r0 * (r10 + r1);
                int r17 = r16[1] * (this.cellHeight + r1);
                boolean z2 = view.getRight() > 0 || view.getBottom() > 0;
                int left = view.getLeft();
                int top = view.getTop();
                int[][] r5 = this.sizes;
                int r6 = r5[0][r15];
                int r8 = this.gap;
                int r7 = r5[1][r15];
                view.layout(r2, r17, (r6 * r10) + r2 + ((r6 - 1) * r8), (this.cellHeight * r7) + r17 + ((r7 - 1) * r8));
                if (view == this.dragged) {
                    view.setTranslationX((this.fingerX - this.grabX) - r2);
                    view.setTranslationY((this.fingerY - this.grabY) - r17);
                } else if (z2 && (left != r2 || top != r17)) {
                    Motion.Value valueSpring = Panel.this.spring(view, View.TRANSLATION_X, Logic.Spring.SNAPPY);
                    Motion.Value valueSpring2 = Panel.this.spring(view, View.TRANSLATION_Y, Logic.Spring.SNAPPY);
                    valueSpring.shift(left - r2);
                    valueSpring2.shift(top - r17);
                    valueSpring.to(0.0f);
                    valueSpring2.to(0.0f);
                }
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* JADX INFO: renamed from: startDrag, reason: merged with bridge method [inline-methods] */
        public void m241lambda$drag$0$devcolorccPanel$TileGrid(View view) {
            this.dragged = view;
            this.home = this.spots;
            setTranslationZ(Panel.this.dp(2.0f));
            this.grabX = this.fingerX - view.getLeft();
            this.grabY = this.fingerY - view.getTop();
            view.cancelLongPress();
            view.setPressed(false);
            getParent().requestDisallowInterceptTouchEvent(true);
            view.performHapticFeedback(0);
            Panel.this.spring(view, View.TRANSLATION_X, Logic.Spring.SNAPPY).snap(view.getTranslationX());
            Panel.this.spring(view, View.TRANSLATION_Y, Logic.Spring.SNAPPY).snap(view.getTranslationY());
            Panel.this.spring(view, View.TRANSLATION_Z, Logic.Spring.SNAPPY).to(Panel.this.dp(8.0f));
            Panel.this.scaleTo(view, this.spans ? 1.05f : 1.12f, Logic.Spring.BOUNCY);
        }

        private void reflow() {
            onLayout(false, getLeft(), getTop(), getRight(), getBottom());
        }

        void follow(View view, float f, float f2) {
            getLocationOnScreen(this.at);
            float f3 = (f - this.at[0]) / Panel.this.editor.scale;
            float f4 = (f2 - this.at[1]) / Panel.this.editor.scale;
            this.fingerX = f3;
            this.fingerY = f4;
            view.setTranslationX((f3 - this.grabX) - view.getLeft());
            view.setTranslationY((f4 - this.grabY) - view.getTop());
            if (!this.reorder || Panel.this.editor.across(this, f2)) {
                return;
            }
            if (this == Panel.this.editor.chosenGrid && Panel.this.editor.overBlocks(f2)) {
                return;
            }
            if (this != Panel.this.editor.blockGrid || Panel.this.editor.overBlocks(f2)) {
                int r6 = this.order.indexOf(view);
                int r13 = Logic.rows(this.spots, this.sizes[1]);
                if (this.free) {
                    int r11 = 0;
                    int r3 = 0;
                    while (true) {
                        int[][] r4 = this.home;
                        if (r11 >= r4.length) {
                            break;
                        }
                        if (r11 != r6) {
                            r3 = Math.max(r3, r4[r11][1] + this.sizes[1][r11]);
                        }
                        r11++;
                    }
                    float fCellWidth = (f3 - this.grabX) / (cellWidth(getWidth()) + this.gap);
                    int r12 = this.spots[r6][0];
                    int r5 = this.columns;
                    int r7 = this.sizes[0][r6];
                    int r8 = Logic.snap(fCellWidth, r12, r5 - r7, r7 != 2 ? 1 : 2);
                    int r9 = Logic.snap((f4 - this.grabY) / (this.cellHeight + this.gap), this.spots[r6][1], r3, 1);
                    int[] r14 = this.spots[r6];
                    if (r8 == r14[0] && r9 == r14[1]) {
                        return;
                    }
                    int[][] r15 = this.sizes;
                    this.spots = Logic.shove(r15[0], r15[1], this.home, r6, r8, r9, this.columns);
                } else {
                    float width = (((f3 - this.grabX) + (view.getWidth() / 2.0f)) + (this.gap / 2.0f)) / (cellWidth(getWidth()) + this.gap);
                    float height = (f4 - this.grabY) + (view.getHeight() / 2.0f);
                    int r1 = this.gap;
                    int r16 = Logic.dropIndex(this.order.size(), r6, width, (height + (r1 / 2.0f)) / (this.cellHeight + r1), this.columns);
                    if (r16 == r6) {
                        return;
                    }
                    this.order.remove(view);
                    this.order.add(r16, view);
                }
                reflow();
                if (Logic.rows(this.spots, this.sizes[1]) != r13) {
                    requestLayout();
                }
            }
        }

        boolean drag(final View view, MotionEvent motionEvent) {
            getLocationOnScreen(this.at);
            float rawX = (motionEvent.getRawX() - this.at[0]) / Panel.this.editor.scale;
            float rawY = (motionEvent.getRawY() - this.at[1]) / Panel.this.editor.scale;
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 0) {
                this.downX = rawX;
                this.fingerX = rawX;
                this.downY = rawY;
                this.fingerY = rawY;
                if (!this.spans || !this.reorder || view.findViewWithTag("handle") == null || rawX <= view.getRight() - Panel.this.dp(30.0f) || rawY <= view.getBottom() - Panel.this.dp(30.0f)) {
                    Panel.this.scaleTo(view, 0.92f, Logic.Spring.PRESS);
                    Runnable runnable = new Runnable() { // from class: dev.colorcc.Panel.TileGrid.0
                        @Override // java.lang.Runnable
                        public final void run() {
                            TileGrid.this.m241lambda$drag$0$devcolorccPanel$TileGrid(view);
                        }
                    };
                    this.hold = runnable;
                    postDelayed(runnable, Panel.HOLD_MS);
                    return false;
                }
                this.sizing = view;
                this.sized = false;
                getParent().requestDisallowInterceptTouchEvent(true);
                Panel.this.scaleTo(view, 1.03f, Logic.Spring.PRESS);
                return true;
            }
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    this.fingerX = rawX;
                    this.fingerY = rawY;
                    if (this.sizing == view) {
                        String str = (String) view.getTag();
                        char cCharAt = str.charAt(0);
                        int r0 = Math.round(((rawX - view.getLeft()) + this.gap) / (cellWidth(getWidth()) + this.gap));
                        float top = rawY - view.getTop();
                        int r6 = this.gap;
                        int[] r1 = Logic.fit(cCharAt, r0, Math.round((top + r6) / (this.cellHeight + r6)));
                        if (r1[0] != str.charAt(1) - '0' || r1[1] != str.charAt(2) - '0') {
                            this.sized = true;
                            Panel.this.editor.setSize(view, r1[0], r1[1]);
                            view.performHapticFeedback(Panel.TILES_PER_ROW);
                        }
                        return true;
                    }
                    if (this.dragged != view) {
                        if (Math.hypot(rawX - this.downX, rawY - this.downY) > Panel.this.touchSlop) {
                            release(view);
                        }
                        return false;
                    }
                    follow(view, motionEvent.getRawX(), motionEvent.getRawY());
                    Panel.this.editor.edgeScroll(this, view, motionEvent.getRawX(), motionEvent.getRawY());
                    return true;
                }
                if (actionMasked != 3) {
                    return this.dragged == view;
                }
            }
            if (this.sizing == view) {
                this.sizing = null;
                view.cancelLongPress();
                view.setPressed(false);
                if (this.sized || motionEvent.getActionMasked() != 1 || Math.hypot(rawX - this.downX, rawY - this.downY) > Panel.this.touchSlop) {
                    Panel.this.scaleTo(view, 1.0f, Logic.Spring.BOUNCY);
                } else {
                    Panel.this.editor.resize(view);
                }
                int r9 = Logic.rows(this.spots, this.sizes[1]);
                this.spots = Logic.lift(this.spots, this.sizes[1]);
                reflow();
                if (Logic.rows(this.spots, this.sizes[1]) != r9) {
                    requestLayout();
                }
                return true;
            }
            if (this.dragged != view) {
                release(view);
                return false;
            }
            this.dragged = null;
            Panel.this.editor.edgeScroll(null, null, 0.0f, 0.0f);
            if (motionEvent.getActionMasked() == 1 && Panel.this.editor.drop(this, view, motionEvent.getRawX(), motionEvent.getRawY())) {
                Panel.this.spring(view, View.TRANSLATION_Z, Logic.Spring.SNAPPY).to(0.0f);
                if (this.free) {
                    this.spots = this.home;
                    reflow();
                }
                return true;
            }
            if (this.free) {
                int r10 = Logic.rows(this.spots, this.sizes[1]);
                this.spots = Logic.lift(this.spots, this.sizes[1]);
                reflow();
                if (Logic.rows(this.spots, this.sizes[1]) != r10) {
                    requestLayout();
                }
            }
            setTranslationZ(0.0f);
            Panel.this.spring(view, View.TRANSLATION_X, Logic.Spring.SNAPPY).to(0.0f);
            Panel.this.spring(view, View.TRANSLATION_Y, Logic.Spring.SNAPPY).to(0.0f);
            Panel.this.spring(view, View.TRANSLATION_Z, Logic.Spring.SNAPPY).to(0.0f);
            Panel.this.scaleTo(view, 1.0f, Logic.Spring.BOUNCY);
            return true;
        }

        private void release(View view) {
            Runnable runnable = this.hold;
            if (runnable != null) {
                removeCallbacks(runnable);
            }
            this.hold = null;
            Panel.this.scaleTo(view, 1.0f, Logic.Spring.BOUNCY);
        }
    }

    private final class Pager extends HorizontalScrollView {
        private final LinearLayout content;
        private final Runnable flatten;
        private boolean flung;
        int pageCount;
        private boolean pagesChecked;
        private boolean pagesLayered;
        private final Motion.Value snap;

        Pager(Context context) {
            super(context);
            this.pageCount = 1;
            this.flatten = new Runnable() { // from class: dev.colorcc.Panel.Pager.2
                @Override // java.lang.Runnable
                public final void run() {
                    Pager.this.m174lambda$new$2$devcolorccPanel$Pager();
                }
            };
            setHorizontalScrollBarEnabled(false);
            setOverScrollMode(2);
            setFillViewport(true);
            setClipChildren(false);
            LinearLayout linearLayoutHorizontal = Panel.horizontal(context);
            this.content = linearLayoutHorizontal;
            addView(linearLayoutHorizontal, new FrameLayout.LayoutParams(-2, -1));
            this.snap = Panel.this.motion.value(Logic.Spring.PAGE, new Motion.Listener() { // from class: dev.colorcc.Panel.Pager.3
                @Override // dev.colorcc.Motion.Listener
                public final void onValue(float f, float f2) {
                    Pager.this.m172lambda$new$0$devcolorccPanel$Pager(f, f2);
                }
            });
            setOnScrollChangeListener(new View.OnScrollChangeListener() { // from class: dev.colorcc.Panel.Pager.4
                @Override // android.view.View.OnScrollChangeListener
                public final void onScrollChange(View view, int r2, int r3, int r4, int r5) {
                    Pager.this.m173lambda$new$1$devcolorccPanel$Pager(view, r2, r3, r4, r5);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$0$dev-colorcc-Panel$Pager, reason: not valid java name */
        /* synthetic */ void m172lambda$new$0$devcolorccPanel$Pager(float f, float f2) {
            scrollTo(Math.round(f), 0);
        }

        /* JADX INFO: renamed from: lambda$new$1$dev-colorcc-Panel$Pager, reason: not valid java name */
        /* synthetic */ void m173lambda$new$1$devcolorccPanel$Pager(View view, int r2, int r3, int r4, int r5) {
            if (getWidth() > 0) {
                Panel.this.dots.set(this.pageCount, (float) r2 / (float) getWidth());
                applyPageDepth(r2);
            }
            layerPages(true);
            removeCallbacks(this.flatten);
            postDelayed(this.flatten, 120L);
        }

        /* JADX INFO: renamed from: lambda$new$2$dev-colorcc-Panel$Pager, reason: not valid java name */
        /* synthetic */ void m174lambda$new$2$devcolorccPanel$Pager() {
            layerPages(false);
            checkPages();
        }

        private void checkPages() {
            if (this.pagesChecked || getWidth() == 0) {
                return;
            }
            StringBuilder sb = new StringBuilder();
            int r3 = 0;
            for (int r2 = 0; r2 < this.content.getChildCount(); r2++) {
                View childAt = this.content.getChildAt(r2);
                if (childAt.getVisibility() == 0 && childAt.getAlpha() > 0.5f) {
                    r3++;
                }
                int r6 = 0;
                if (childAt instanceof ViewGroup) {
                    ViewGroup viewGroup = (ViewGroup) childAt;
                    for (int r5 = 0; r5 < viewGroup.getChildCount(); r5++) {
                        View childAt2 = viewGroup.getChildAt(r5);
                        if (childAt2 instanceof ViewGroup) {
                            ViewGroup viewGroup2 = (ViewGroup) childAt2;
                            for (int r8 = 0; r8 < viewGroup2.getChildCount(); r8++) {
                                if (Math.abs(viewGroup2.getChildAt(r8).getTranslationX()) > 1.0f) {
                                    r6++;
                                }
                            }
                        }
                    }
                }
                sb.append(" [");
                sb.append(r2);
                sb.append(childAt.getVisibility() == 0 ? " shown" : " hidden");
                sb.append(" alpha ");
                sb.append(childAt.getAlpha());
                sb.append(" scale ");
                sb.append(childAt.getScaleX());
                sb.append(" at ");
                sb.append(childAt.getLeft());
                sb.append(" moved ");
                sb.append(childAt.getTranslationX());
                sb.append(" layer ");
                sb.append(childAt.getLayerType());
                sb.append(" tiles off place ");
                sb.append(r6);
                sb.append(']');
            }
            if (r3 <= 1 && this.content.getChildCount() == this.pageCount && getScrollX() % getWidth() == 0) {
                return;
            }
            this.pagesChecked = true;
            ColorCCBridge.log("tile pages wrong at rest: scroll " + getScrollX() + " of " + getWidth() + ", " + this.content.getChildCount() + " pages for " + this.pageCount + ", pager in " + getParent().getClass().getSimpleName() + ":" + ((Object) sb));
        }

        private void layerPages(boolean z) {
            if (z == this.pagesLayered) {
                return;
            }
            this.pagesLayered = z;
            for (int r1 = 0; r1 < this.content.getChildCount(); r1++) {
                this.content.getChildAt(r1).setLayerType(z ? 2 : 0, null);
            }
        }

        private void applyPageDepth(int r8) {
            int width = Math.max(1, getWidth());
            for (int r2 = 0; r2 < this.content.getChildCount(); r2++) {
                View childAt = this.content.getChildAt(r2);
                float fClamp = Logic.clamp(Math.abs(r2 - ((float) r8 / (float) width)));
                if (fClamp > Panel.FULLY_OPEN) {
                    fClamp = 1.0f;
                }
                float f = 1.0f - (0.14f * fClamp);
                childAt.setScaleX(f);
                childAt.setScaleY(f);
                childAt.setAlpha(1.0f - (0.6f * fClamp));
                childAt.setVisibility(fClamp < 1.0f ? 0 : Panel.TILES_PER_ROW);
            }
        }

        @Override // android.widget.HorizontalScrollView, android.view.View
        protected void onSizeChanged(int r2, int r3, int r4, int r5) {
            super.onSizeChanged(r2, r3, r4, r5);
            setClipBounds(new Rect(0, (-r3) * Panel.TILES_PER_ROW, r2, r3 * 5));
        }

        void setTiles(List<Object> list, int r15) {
            Context context = getContext();
            this.content.removeAllViews();
            this.pagesLayered = false;
            this.pageCount = Logic.pages(list.size(), r15);
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            int r3 = r15 / Panel.TILES_PER_ROW;
            Panel panel = Panel.this;
            layoutParams.height = panel.dp(panel.tileRow()) * r3;
            requestLayout();
            int r2 = 0;
            while (r2 < this.pageCount) {
                LinearLayout linearLayout = new LinearLayout(context);
                linearLayout.setOrientation(1);
                linearLayout.setClipChildren(false);
                linearLayout.setClipToPadding(false);
                linearLayout.setPadding(Panel.this.dp(27.0f), 0, Panel.this.dp(27.0f), 0);
                linearLayout.setVisibility(r2 == 0 ? 0 : Panel.TILES_PER_ROW);
                linearLayout.forceHasOverlappingRendering(false);
                for (int r6 = 0; r6 < r3; r6++) {
                    LinearLayout linearLayoutHorizontal = Panel.horizontal(context);
                    for (int r8 = 0; r8 < Panel.TILES_PER_ROW; r8++) {
                        int r9 = (r2 * r15) + (r6 * Panel.TILES_PER_ROW) + r8;
                        View viewTileView = r9 < list.size() ? tileView(context, list.get(r9), Panel.this.tileBase + (r6 * 3) + r8) : new Space(context);
                        viewTileView.setTranslationZ(-r8);
                        Panel panel2 = Panel.this;
                        linearLayoutHorizontal.addView(viewTileView, new LinearLayout.LayoutParams(0, panel2.dp(panel2.tileRow()), 1.0f));
                    }
                    linearLayoutHorizontal.setTranslationZ(-r6);
                    linearLayout.addView(linearLayoutHorizontal);
                }
                this.content.addView(linearLayout, new LinearLayout.LayoutParams(-1, -1));
                r2++;
            }
            this.snap.snap(0.0f);
            Panel.this.dots.set(this.pageCount, 0.0f);
        }

        private View tileView(Context context, final Object obj, int r11) {
            final LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(1);
            linearLayout.setGravity(1);
            linearLayout.setClickable(true);
            Panel panel = Panel.this;
            final Disc disc = panel.new Disc(context, panel.dp(26.0f));
            disc.setClickable(false);
            Panel.this.guard(new Runnable() { // from class: dev.colorcc.Panel.Pager.0
                @Override // java.lang.Runnable
                public final void run() {
                    Pager.lambda$tileView$3(disc, obj);
                }
            });
            linearLayout.addView(disc, new LinearLayout.LayoutParams(Panel.this.dp(63.0f), Panel.this.dp(63.0f)));
            final TextView textViewLabel = Panel.this.label(context, 10.0f, -1, 2);
            textViewLabel.setGravity(1);
            if (!Panel.this.tileNames) {
                textViewLabel.setVisibility(8);
            }
            textViewLabel.setIncludeFontPadding(false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
            layoutParams.topMargin = Panel.this.dp(3.0f);
            linearLayout.addView(textViewLabel, layoutParams);
            Panel.this.wireTile(linearLayout, obj);
            final Body body = Panel.this.new Body(linearLayout, r11);
            body.pressable();
            Panel.this.tileBodies.add(body);
            final int[] r2 = {-1};
            Panel.this.bindTile(obj, new Consumer() { // from class: dev.colorcc.Panel.Pager.1
                @Override // java.util.function.Consumer
                public final void accept(Object obj2) {
                    Pager.this.m175lambda$tileView$4$devcolorccPanel$Pager(r2, textViewLabel, disc, body, linearLayout, obj2);
                }
            });
            return linearLayout;
        }

        static /* synthetic */ void lambda$tileView$3(Disc disc, Object obj) {
            disc.style((String) ReflectUtils.callMethod(obj, "getTileSpec", new Object[0]));
        }

        /* JADX INFO: renamed from: lambda$tileView$4$dev-colorcc-Panel$Pager, reason: not valid java name */
        /* synthetic */ void m175lambda$tileView$4$devcolorccPanel$Pager(int[] r6, TextView textView, Disc disc, Body body, LinearLayout linearLayout, Object obj) {
            int r0 = Panel.stateOf(obj);
            int r2 = r6[0];
            boolean z = (r2 == -1 || r2 == r0) ? false : true;
            r6[0] = r0;
            Panel.setTextIfChanged(textView, Panel.text(obj, "label"));
            boolean z2 = z && Panel.this.interactive;
            disc.render(obj, z2);
            if (z2 && !disc.ownAnimation) {
                body.press.kick(Panel.TOGGLE_KICK);
            }
            linearLayout.setContentDescription(Panel.text(obj, "label"));
            linearLayout.setStateDescription(Panel.this.word(r0 == 2 ? "on" : "off", new Object[0]));
        }

        @Override // android.widget.HorizontalScrollView, android.widget.FrameLayout, android.view.View
        protected void onMeasure(int r4, int r5) {
            int size = View.MeasureSpec.getSize(r4);
            for (int r1 = 0; r1 < this.content.getChildCount(); r1++) {
                this.content.getChildAt(r1).getLayoutParams().width = size;
            }
            super.onMeasure(r4, r5);
        }

        void home() {
            if (getScrollX() == 0) {
                return;
            }
            this.snap.snap(0.0f);
            scrollTo(0, 0);
        }

        private void snapTo(int r3) {
            this.snap.snap(getScrollX());
            this.snap.to(Math.max(0, Math.min(this.pageCount - 1, r3)) * getWidth());
        }

        @Override // android.widget.HorizontalScrollView
        public void fling(int r4) {
            if (getWidth() == 0) {
                return;
            }
            this.flung = true;
            float f = r4;
            snapTo(Logic.pageAfterFling(getScrollX(), getWidth(), f, this.pageCount));
            float width = getWidth() * 4.0f;
            this.snap.kick(Math.max(-width, Math.min(width, f)));
        }

        @Override // android.view.ViewGroup, android.view.View
        public boolean dispatchTouchEvent(MotionEvent motionEvent) {
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked != 0 || Panel.this.swipeOwner != 0) {
                if (actionMasked == 2 && Panel.this.swipeOwner == 2) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
            } else {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            if (actionMasked == 0 && this.pageCount > 1) {
                this.flung = false;
                this.snap.snap(getScrollX());
            }
            boolean zDispatchTouchEvent = super.dispatchTouchEvent(motionEvent);
            if ((actionMasked == 1 || actionMasked == 3) && this.pageCount > 1 && !this.flung && getWidth() > 0) {
                snapTo(Math.round((float) getScrollX() / (float) getWidth()));
            }
            return zDispatchTouchEvent;
        }

        @Override // android.widget.HorizontalScrollView, android.view.ViewGroup
        public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
            return this.pageCount > 1 && Panel.this.swipeOwner != 2 && super.onInterceptTouchEvent(motionEvent);
        }

        @Override // android.widget.HorizontalScrollView, android.view.View
        public boolean onTouchEvent(MotionEvent motionEvent) {
            return this.pageCount > 1 && super.onTouchEvent(motionEvent);
        }
    }

    private final class Dots extends View {
        private final Glass back;
        private int count;
        private final Paint paint;
        private final RectF pill;
        private float position;

        Dots(Context context) {
            super(context);
            this.paint = new Paint(1);
            this.pill = new RectF();
            Glass glass = new Glass(-1.0f, Panel.GLASS_IDLE, Panel.GLASS_IDLE, 0.5f, 0.5f);
            this.back = glass;
            this.count = 1;
            glass.setCallback(this);
        }

        void set(int r3, float f) {
            if (r3 == this.count && f == this.position) {
                return;
            }
            this.count = r3;
            this.position = f;
            setStateDescription("Page " + (Math.round(f) + 1) + " of " + r3);
            invalidate();
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            if (this.count < 2) {
                return;
            }
            float fDp = Panel.this.dp(3.0f);
            float fDp2 = Panel.this.dp(Panel.DROP_STEP);
            float height = getHeight() / 2.0f;
            float width = (getWidth() / 2.0f) - (((this.count - 1) * fDp2) / 2.0f);
            float fDp3 = Panel.this.dp(8.0f);
            int r9 = 0;
            this.back.setBounds(Math.round((width - fDp) - fDp3), 0, Math.round(((this.count - 1) * fDp2) + width + fDp + fDp3), getHeight());
            this.back.draw(canvas);
            this.paint.setColor(1728053247);
            while (true) {
                int r5 = this.count;
                if (r9 >= r5) {
                    float fMax = Math.max(0.0f, Math.min(r5 - 1.0f, this.position));
                    float f = (int) fMax;
                    float f2 = (fMax - f) * 2.0f;
                    this.pill.set((width + ((f + Math.max(f2 - 1.0f, 0.0f)) * fDp2)) - fDp, height - fDp, ((Math.min(f2, 1.0f) + f) * fDp2) + width + fDp, height + fDp);
                    this.paint.setColor(-1);
                    canvas.drawRoundRect(this.pill, fDp, fDp, this.paint);
                    return;
                }
                canvas.drawCircle((r9 * fDp2) + width, height, fDp, this.paint);
                r9++;
            }
        }
    }
}
