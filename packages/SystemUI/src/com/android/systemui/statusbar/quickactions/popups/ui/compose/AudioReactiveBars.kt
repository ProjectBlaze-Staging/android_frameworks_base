/*
 * Copyright (C) 2024-2026 Lunaris AOSP
 * Copyright (C) 2026 The BlazeAOSP Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.quickactions.popups.ui.compose

import android.Manifest
import android.app.AppOpsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.audiofx.Visualizer
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.Process
import android.os.SystemClock
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val BarCount = 5
private const val MinBarFraction = 0.14f

// Pink-noise / perceptual frequency weighting across the 5 bands:
// [0] Sub-Bass/Kick (45-100Hz), [1] Bass/Snare Body (140-280Hz),
// [2] Low-Mid/Rhythm (320-750Hz), [3] Lead Vocal/Mid (800-2250Hz), [4] Treble/Hi-Hat (2.3-6.5kHz)
private val BandGains = floatArrayOf(1.05f, 1.30f, 1.68f, 2.25f, 3.10f)

private fun ensureVisualizerPermissions(context: Context) {
    runCatching {
        if (
            context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            context.packageManager.grantRuntimePermission(
                context.packageName,
                Manifest.permission.RECORD_AUDIO,
                Process.myUserHandle(),
            )
        }
    }
    runCatching {
        context.getSystemService(AppOpsManager::class.java)?.setUidMode(
            AppOpsManager.OPSTR_RECORD_AUDIO,
            Process.myUid(),
            AppOpsManager.MODE_ALLOWED,
        )
    }
}

/**
 * Five vertical equalizer bars driven in real time by Android's [Visualizer] on session 0
 * (global output mix) using 5-band FFT spectrum decomposition + beat onset detection.
 *
 * During phone calls ([isCall] = true), if the active call is routed through hardware cellular
 * baseband where session 0 is silent, a syllabic speech formant envelope keeps the voice
 * spectrum naturally reactive to the call state.
 */
@Composable
private fun rememberScreenInteractive(context: Context): Boolean {
    val pm = remember(context) { context.getSystemService(PowerManager::class.java) }
    var interactive by remember { mutableStateOf(pm?.isInteractive != false) }
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_ON -> interactive = true
                    Intent.ACTION_SCREEN_OFF -> interactive = false
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        runCatching { context.registerReceiver(receiver, filter) }
        onDispose { runCatching { context.unregisterReceiver(receiver) } }
    }
    return interactive
}

@Composable
fun AudioReactiveBars(
    isPlaying: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    artworkDrawable: Drawable? = null,
    isCall: Boolean = false,
    barWidth: Dp = 2.dp,
    barSpacing: Dp = 1.5.dp,
    barHeight: Dp = 14.dp,
) {
    val context = LocalContext.current
    val screenInteractive = rememberScreenInteractive(context)
    val active = isPlaying && screenInteractive
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val paletteColors by rememberArtworkPalette(artworkDrawable, fallback = color)
    val levels = remember {
        mutableStateListOf<Float>().apply { repeat(BarCount) { add(MinBarFraction) } }
    }
    var lastRealSignalMs by remember { mutableLongStateOf(0L) }
    var lastFftSignalMs by remember { mutableLongStateOf(0L) }

    DisposableEffect(active, isCall) {
        if (!active) {
            for (i in 0 until BarCount) levels[i] = MinBarFraction
            return@DisposableEffect onDispose {}
        }

        ensureVisualizerPermissions(context)

        val smoothed = FloatArray(BarCount) { MinBarFraction }
        val prevBandNorm = FloatArray(BarCount) { 0f }
        var runningFftPeak = 28f
        var runningWavePeak = 18f

        val visualizer: Visualizer? = runCatching {
            Visualizer(0).apply {
                enabled = false
                val range = Visualizer.getCaptureSizeRange()
                captureSize = range[1].coerceIn(range[0], 1024)
                runCatching { scalingMode = Visualizer.SCALING_MODE_NORMALIZED }
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onFftDataCapture(
                            v: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int,
                        ) {
                            if (fft == null || fft.size < 32) return
                            val numBins = fft.size / 2
                            // 5 musical frequency bands (bin indices for 1024-pt FFT @ 48kHz)
                            val scale = (numBins / 512f).coerceAtLeast(0.25f)
                            val b0Start = 1
                            val b0End = max(2, (2 * scale).toInt())
                            val b1Start = b0End + 1
                            val b1End = max(b1Start + 1, (6 * scale).toInt())
                            val b2Start = b1End + 1
                            val b2End = max(b2Start + 2, (16 * scale).toInt())
                            val b3Start = b2End + 1
                            val b3End = max(b3Start + 4, (48 * scale).toInt())
                            val b4Start = b3End + 1
                            val b4End = min(numBins - 1, max(b4Start + 6, (140 * scale).toInt()))

                            val ranges = arrayOf(
                                b0Start..b0End,
                                b1Start..b1End,
                                b2Start..b2End,
                                b3Start..b3End,
                                b4Start..b4End,
                            )

                            val bandMags = FloatArray(BarCount)
                            var maxMag = 0f
                            for (b in 0 until BarCount) {
                                val r = ranges[b]
                                var sumSq = 0f
                                var peak = 0f
                                var count = 0
                                for (bin in r) {
                                    val idx = bin * 2
                                    if (idx + 1 >= fft.size) break
                                    val re = fft[idx].toFloat()
                                    val im = fft[idx + 1].toFloat()
                                    val mag = hypot(re, im)
                                    sumSq += mag * mag
                                    if (mag > peak) peak = mag
                                    count++
                                }
                                if (count > 0) {
                                    val rms = sqrt(sumSq / count)
                                    val combined = (peak * 0.65f + rms * 0.35f) * BandGains[b]
                                    bandMags[b] = combined
                                    if (combined > maxMag) maxMag = combined
                                }
                            }

                            if (maxMag < 3.5f) {
                                // Silence in FFT; decay bars smoothly toward rest
                                if (SystemClock.uptimeMillis() - lastRealSignalMs > 120L) {
                                    mainHandler.post {
                                        for (i in 0 until BarCount) {
                                            prevBandNorm[i] *= 0.6f
                                            smoothed[i] = (smoothed[i] * 0.65f + MinBarFraction * 0.35f)
                                                .coerceAtLeast(MinBarFraction)
                                            levels[i] = smoothed[i]
                                        }
                                    }
                                }
                                return
                            }

                            val now = SystemClock.uptimeMillis()

                            // Adaptive Gain Control (AGC) tracks track loudness while preserving beat dynamics
                            runningFftPeak = max(maxMag, runningFftPeak * 0.975f).coerceAtLeast(22f)
                            val nextLevels = FloatArray(BarCount)

                            for (i in 0 until BarCount) {
                                val rawNorm = (bandMags[i] / runningFftPeak).coerceIn(0f, 1f)
                                // Beat onset transient boost: sharp upward snap when band energy jumps
                                val onsetDelta = (rawNorm - prevBandNorm[i]).coerceAtLeast(0f)
                                prevBandNorm[i] = rawNorm * 0.7f + prevBandNorm[i] * 0.3f

                                val shaped = (rawNorm.pow(1.18f) + onsetDelta * 1.55f).coerceIn(0f, 1f)
                                val target = MinBarFraction + shaped * (1f - MinBarFraction)

                                // Fast attack on beat hit, smooth gravity decay between beats
                                smoothed[i] = if (target > smoothed[i]) {
                                    smoothed[i] * 0.22f + target * 0.78f
                                } else {
                                    smoothed[i] * 0.58f + target * 0.42f
                                }
                                nextLevels[i] = smoothed[i].coerceIn(MinBarFraction, 1f)
                            }
                            mainHandler.post {
                                lastFftSignalMs = now
                                lastRealSignalMs = now
                                for (i in 0 until BarCount) levels[i] = nextLevels[i]
                            }
                        }

                        override fun onWaveFormDataCapture(
                            v: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int,
                        ) {
                            if (waveform == null || waveform.size < 32) return
                            val now = SystemClock.uptimeMillis()
                            // Only use time-domain filterbank if FFT hasn't delivered a recent frame
                            if (now - lastFftSignalMs < 140L) return

                            var bassEnergy = 0f
                            var midEnergy = 0f
                            var highEnergy = 0f
                            var prevSample = (waveform[0].toInt() and 0xFF) - 128f
                            var lowPass = prevSample

                            for (k in 1 until waveform.size) {
                                val sample = (waveform[k].toInt() and 0xFF) - 128f
                                lowPass = lowPass * 0.82f + sample * 0.18f
                                val highPass = sample - prevSample
                                val bandPass = sample - lowPass
                                bassEnergy += lowPass * lowPass
                                midEnergy += bandPass * bandPass
                                highEnergy += highPass * highPass
                                prevSample = sample
                            }

                            val n = (waveform.size - 1).toFloat()
                            val bassRms = sqrt(bassEnergy / n)
                            val midRms = sqrt(midEnergy / n) * 1.35f
                            val highRms = sqrt(highEnergy / n) * 1.65f
                            val maxRms = max(bassRms, max(midRms, highRms))

                            if (maxRms < 1.8f) {
                                if (now - lastRealSignalMs > 120L) {
                                    mainHandler.post {
                                        for (i in 0 until BarCount) {
                                            smoothed[i] = (smoothed[i] * 0.65f + MinBarFraction * 0.35f)
                                                .coerceAtLeast(MinBarFraction)
                                            levels[i] = smoothed[i]
                                        }
                                    }
                                }
                                return
                            }

                            runningWavePeak = max(maxRms, runningWavePeak * 0.975f).coerceAtLeast(12f)

                            val bandTargets = floatArrayOf(
                                (bassRms / runningWavePeak).coerceIn(0f, 1f),
                                ((bassRms * 0.65f + midRms * 0.35f) / runningWavePeak).coerceIn(0f, 1f),
                                (midRms / runningWavePeak).coerceIn(0f, 1f),
                                ((midRms * 0.5f + highRms * 0.5f) / runningWavePeak).coerceIn(0f, 1f),
                                (highRms / runningWavePeak).coerceIn(0f, 1f),
                            )

                            val nextLevels = FloatArray(BarCount)
                            for (i in 0 until BarCount) {
                                val rawNorm = bandTargets[i]
                                val onsetDelta = (rawNorm - prevBandNorm[i]).coerceAtLeast(0f)
                                prevBandNorm[i] = rawNorm * 0.7f + prevBandNorm[i] * 0.3f
                                val shaped = (rawNorm.pow(1.15f) + onsetDelta * 1.45f).coerceIn(0f, 1f)
                                val target = MinBarFraction + shaped * (1f - MinBarFraction)
                                smoothed[i] = if (target > smoothed[i]) {
                                    smoothed[i] * 0.25f + target * 0.75f
                                } else {
                                    smoothed[i] * 0.60f + target * 0.40f
                                }
                                nextLevels[i] = smoothed[i].coerceIn(MinBarFraction, 1f)
                            }
                            mainHandler.post {
                                lastRealSignalMs = now
                                for (i in 0 until BarCount) levels[i] = nextLevels[i]
                            }
                        }
                    },
                    Visualizer.getMaxCaptureRate(),
                    /* waveform = */ true,
                    /* fft = */ true,
                )
                enabled = true
            }
        }.getOrNull()

        onDispose {
            runCatching {
                visualizer?.enabled = false
                visualizer?.release()
            }
        }
    }

    // Syllabic voice formant cadence ONLY for genuinely connected cellular calls
    // when baseband audio bypasses session 0 (never while ringing unanswered or screen off).
    val audioManager = remember(context) { context.getSystemService(AudioManager::class.java) }
    LaunchedEffect(active, isCall) {
        if (!active) return@LaunchedEffect
        var phase = 0f
        while (true) {
            delay(40L)
            val now = SystemClock.uptimeMillis()
            val hasLiveVisualizer = (now - lastRealSignalMs) < 280L
            if (hasLiveVisualizer) continue

            val mode = audioManager?.mode ?: AudioManager.MODE_NORMAL
            val inConnectedVoiceCall =
                mode == AudioManager.MODE_IN_CALL ||
                    mode == AudioManager.MODE_IN_COMMUNICATION
            val musicActive = !isCall && audioManager?.isMusicActive == true

            if (inConnectedVoiceCall) {
                phase += 0.24f
                // Natural speech cadence with syllable envelope (~4.5 Hz) and micro-pauses
                val phraseGate = ((sin(phase * 0.37f) + 0.55f).coerceIn(0f, 1f)).pow(0.8f)
                val syllable = ((sin(phase * 1.85f) + 1f) * 0.5f).pow(1.4f)
                val formantWeights = floatArrayOf(0.45f, 0.85f, 1.0f, 0.78f, 0.40f)
                for (i in 0 until BarCount) {
                    val harmonic = ((sin(phase * (1.3f + i * 0.47f) + i * 1.1f) + 1f) * 0.5f)
                    val voiceLevel =
                        (phraseGate * (syllable * 0.65f + harmonic * 0.35f) * formantWeights[i])
                            .coerceIn(0f, 1f)
                    levels[i] = MinBarFraction + voiceLevel * (0.95f - MinBarFraction)
                }
            } else if (musicActive && (now - lastRealSignalMs) > 900L) {
                // Hardware A2DP/offload fallback only when DSP bypasses session 0 completely
                phase += 0.32f
                val kick = ((sin(phase * 1.6f)).coerceAtLeast(0f)).pow(2.4f)
                val snare = ((sin(phase * 0.8f + 1.57f)).coerceAtLeast(0f)).pow(2.0f)
                val hat = ((sin(phase * 3.2f + 0.7f) + 1f) * 0.5f).pow(1.8f)
                val synth = ((sin(phase * 1.1f + 2.1f) + 1f) * 0.5f)
                val targets = floatArrayOf(
                    (kick * 0.95f).coerceIn(0f, 1f),
                    (kick * 0.6f + snare * 0.4f).coerceIn(0f, 1f),
                    (snare * 0.55f + synth * 0.45f).coerceIn(0f, 1f),
                    (synth * 0.65f + hat * 0.35f).coerceIn(0f, 1f),
                    (hat * 0.85f).coerceIn(0f, 1f),
                )
                for (i in 0 until BarCount) {
                    levels[i] = MinBarFraction + targets[i] * (0.92f - MinBarFraction)
                }
            } else {
                for (i in 0 until BarCount) {
                    levels[i] = (levels[i] * 0.72f + MinBarFraction * 0.28f)
                        .coerceAtLeast(MinBarFraction)
                }
            }
        }
    }

    val barShape = remember(barWidth) { RoundedCornerShape(barWidth / 2) }

    Row(
        modifier = modifier.height(barHeight),
        horizontalArrangement = Arrangement.spacedBy(barSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(BarCount) { i ->
            val topTint by animateColorAsState(
                targetValue = paletteColors.getOrElse(i) { color },
                animationSpec = tween(durationMillis = 420),
                label = "bar_top_color_$i",
            )
            val bottomTint by animateColorAsState(
                targetValue = paletteColors.getOrElse((i + 2) % BarCount) { color }
                    .copy(alpha = 0.78f),
                animationSpec = tween(durationMillis = 420),
                label = "bar_bottom_color_$i",
            )

            val fraction by animateFloatAsState(
                targetValue = if (isPlaying) levels[i] else MinBarFraction,
                animationSpec = spring(dampingRatio = 0.62f, stiffness = 1350f),
                label = "bar_lvl_$i",
            )

            val brush = remember(topTint, bottomTint) {
                Brush.verticalGradient(listOf(topTint, bottomTint))
            }

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(barHeight * fraction.coerceIn(MinBarFraction, 1f))
                    .clip(barShape)
                    .background(brush),
            )
        }
    }
}

/**
 * Extracts up to [BarCount] distinct vibrant/dominant swatches from the album artwork [Drawable]
 * so the equalizer bars reflect the full color mood of the cover.
 */
@Composable
private fun rememberArtworkPalette(
    drawable: Drawable?,
    fallback: Color,
): State<List<Color>> {
    val state = remember { mutableStateOf(List(BarCount) { fallback }) }
    LaunchedEffect(drawable, fallback) {
        if (drawable == null) {
            state.value = List(BarCount) { fallback }
            return@LaunchedEffect
        }
        val extracted = withContext(Dispatchers.Default) {
            runCatching {
                val bitmap = drawable.toSmallBitmap(size = 64) ?: return@runCatching null
                val palette = Palette.from(bitmap).maximumColorCount(16).generate()
                val candidates = listOfNotNull(
                    palette.vibrantSwatch?.rgb,
                    palette.lightVibrantSwatch?.rgb,
                    palette.dominantSwatch?.rgb,
                    palette.mutedSwatch?.rgb,
                    palette.darkVibrantSwatch?.rgb,
                    palette.lightMutedSwatch?.rgb,
                )
                    .map { liftForDarkBackground(it) }
                    .distinct()
                if (candidates.isEmpty()) null
                else List(BarCount) { idx -> candidates[idx % candidates.size] }
            }.getOrNull()
        }
        state.value = extracted ?: List(BarCount) { fallback }
    }
    return state
}

private fun Drawable.toSmallBitmap(size: Int): Bitmap? = runCatching {
    if (this is BitmapDrawable && bitmap != null && !bitmap.isRecycled) {
        return Bitmap.createScaledBitmap(bitmap, size, size, true)
    }
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    setBounds(0, 0, size, size)
    draw(canvas)
    bmp
}.getOrNull()

private fun liftForDarkBackground(rgb: Int): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(rgb, hsl)
    if (hsl[1] < 0.35f) hsl[1] = 0.55f
    if (hsl[2] < 0.55f) hsl[2] = 0.65f
    if (hsl[2] > 0.85f) hsl[2] = 0.80f
    return Color(ColorUtils.HSLToColor(hsl))
}
