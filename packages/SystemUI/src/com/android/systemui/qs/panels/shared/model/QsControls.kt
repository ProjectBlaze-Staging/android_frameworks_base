/*
 * Copyright (C) 2026 RisingOS (revived) Android Project
 * Copyright (C) 2025-2026 AxionOS
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
package com.android.systemui.qs.panels.ui.compose

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.SystemClock
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.compose.ui.graphics.drawInOverlay
import com.android.internal.penguin.glass.LiquidGlass
import com.android.systemui.brightness.ui.viewmodel.BrightnessSliderViewModel
import com.android.systemui.qs.panels.shared.model.QSControl
import com.android.systemui.qs.panels.shared.model.QSControlSpan
import com.android.systemui.qs.tiles.BrightnessTileContent
import com.android.systemui.qs.tiles.TileGlassSliderCore
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

private const val VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION"
private const val STREAM_MUTE_CHANGED_ACTION = "android.media.STREAM_MUTE_CHANGED_ACTION"
private const val INTERNAL_RINGER_MODE_CHANGED_ACTION =
    "android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION"

fun qsControlCornerRadius(control: QSControl, size: DpSize): Dp {
    val half = (minOf(size.width, size.height) / 2).coerceAtLeast(0.dp)
    return when (control) {
        QSControl.BRIGHTNESS,
        QSControl.VOLUME,
        QSControl.RINGER -> half
        QSControl.MEDIA -> minOf(28.dp, half)
    }
}

@Composable
fun QSControlContent(
    control: QSControl,
    span: QSControlSpan,
    interactive: Boolean,
    brightnessViewModel: BrightnessSliderViewModel,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val size = DpSize(maxWidth, maxHeight)
        val shape = RoundedCornerShape(qsControlCornerRadius(control, size))
        val glassStyle = LiquidGlass.getEffectiveStyle(LocalContext.current)
        val trackAlpha = when (glassStyle) {
            LiquidGlass.STYLE_IOS_LIQUID_GLASS -> 0.38f
            LiquidGlass.STYLE_COLOROS_FROSTED -> 0.56f
            else -> 0.75f
        }
        val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = trackAlpha)
        val glassBorderModifier = when (glassStyle) {
            LiquidGlass.STYLE_IOS_LIQUID_GLASS -> Modifier.border(
                width = 1.25.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x6EFFFFFF),
                        Color(0x18FFFFFF),
                        Color(0x4570B8FF),
                    )
                ),
                shape = shape,
            )
            LiquidGlass.STYLE_COLOROS_FROSTED -> Modifier.border(
                width = 0.85.dp,
                color = Color(0x28FFFFFF),
                shape = shape,
            )
            else -> Modifier
        }
        val keepBrightnessControlVisible =
            control == QSControl.BRIGHTNESS && brightnessViewModel.showMirror
        Box(
            Modifier
                .then(if (keepBrightnessControlVisible) Modifier.drawInOverlay() else Modifier)
                .fillMaxSize()
                .then(glassBorderModifier)
                .clip(shape)
                .background(trackColor)
        ) {
            when (control) {
                QSControl.BRIGHTNESS ->
                    BrightnessTileContent(
                        viewModel = brightnessViewModel,
                        isVertical = size.height > size.width,
                        isEditMode = !interactive,
                    )
                QSControl.VOLUME ->
                    VolumeTileContent(
                        isVertical = size.height > size.width,
                        isEditMode = !interactive,
                    )
                QSControl.RINGER -> RingerControl(size, interactive)
                QSControl.MEDIA -> MediaControl(size, interactive)
            }
        }
    }
}

@Composable
fun VolumeTileContent(
    isVertical: Boolean,
    isEditMode: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val audioManager = remember(context) { context.getSystemService(AudioManager::class.java) }
    val stream = AudioManager.STREAM_MUSIC
    val minVolume = remember(audioManager) { audioManager.getStreamMinVolume(stream) }
    val maxVolume =
        remember(audioManager) { audioManager.getStreamMaxVolume(stream).coerceAtLeast(minVolume + 1) }
    var volume by remember { mutableIntStateOf(audioManager.getStreamVolume(stream)) }
    var streamMuted by remember { mutableStateOf(audioManager.isStreamMute(stream)) }
    var lastAudible by remember {
        mutableIntStateOf(
            if (volume > minVolume) volume else (maxVolume / 2).coerceAtLeast(minVolume + 1)
        )
    }

    DisposableEffect(context, audioManager) {
        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    volume = audioManager.getStreamVolume(stream)
                    streamMuted = audioManager.isStreamMute(stream)
                    if (volume > minVolume) lastAudible = volume
                }
            }
        val filter =
            IntentFilter().apply {
                addAction(VOLUME_CHANGED_ACTION)
                addAction(STREAM_MUTE_CHANGED_ACTION)
            }
        context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }

    val enabled = !isEditMode
    val muted = streamMuted || volume <= minVolume

    fun setVolume(newVolume: Int) {
        val v = newVolume.coerceIn(minVolume, maxVolume)
        if (v == volume) return
        volume = v
        if (v > minVolume) lastAudible = v
        audioManager.setStreamVolume(stream, v, 0)
    }

    fun toggleMute() {
        if (muted) {
            if (streamMuted) audioManager.adjustStreamVolume(stream, AudioManager.ADJUST_UNMUTE, 0)
            if (volume <= minVolume) setVolume(lastAudible)
        } else {
            setVolume(minVolume)
        }
    }

    val range = minVolume.toFloat()..maxVolume.toFloat()
    val sliderPainter = rememberVectorPainter(Icons.Rounded.MusicNote)
    val buttonPainter =
        rememberVectorPainter(if (muted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp)

    val slider: @Composable () -> Unit = {
        TileGlassSliderCore(
            value = volume.toFloat(),
            valueRange = range,
            onValueChange = { newValue -> if (enabled) setVolume(newValue.roundToInt()) },
            onValueChangeFinished = null,
            enabled = enabled,
            painter = sliderPainter,
            isVertical = isVertical,
        )
    }
    val muteButtonModifier =
        Modifier.background(MaterialTheme.colorScheme.primary, CircleShape).then(
            if (enabled) Modifier.clickable { toggleMute() } else Modifier
        )
    val muteIcon: @Composable () -> Unit = {
        Icon(
            painter = buttonPainter,
            contentDescription = "Mute",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(20.dp),
        )
    }

    if (isVertical) {
        Column(
            modifier = modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                slider()
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).then(muteButtonModifier),
                contentAlignment = Alignment.Center,
            ) {
                muteIcon()
            }
        }
    } else {
        Row(
            modifier = modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                slider()
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier.fillMaxHeight().aspectRatio(1f).then(muteButtonModifier),
                contentAlignment = Alignment.Center,
            ) {
                muteIcon()
            }
        }
    }
}

private val RingerInset = 6.dp
private val RingerThumbInset = 4.dp

@Composable
private fun RingerControl(outerSize: DpSize, interactive: Boolean, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val audioManager = remember(context) { context.getSystemService(AudioManager::class.java) }
    var mode by remember { mutableIntStateOf(audioManager.getRingerModeInternal()) }

    DisposableEffect(context, audioManager) {
        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    mode = audioManager.getRingerModeInternal()
                }
            }
        context.registerReceiver(
            receiver,
            IntentFilter(INTERNAL_RINGER_MODE_CHANGED_ACTION),
            Context.RECEIVER_NOT_EXPORTED,
        )
        onDispose { context.unregisterReceiver(receiver) }
    }

    val modes =
        remember {
            listOf(
                AudioManager.RINGER_MODE_NORMAL,
                AudioManager.RINGER_MODE_VIBRATE,
                AudioManager.RINGER_MODE_SILENT,
            )
        }
    val icons =
        listOf(Icons.Rounded.Notifications, Icons.Rounded.Vibration, Icons.Rounded.NotificationsOff)
    val descriptions = listOf("Ring", "Vibrate", "Silent")
    val selectedIndex = modes.indexOf(mode).coerceAtLeast(0)
    val thumbPosition by
        animateFloatAsState(
            targetValue = selectedIndex.toFloat(),
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "ringerThumb",
        )

    val innerWidth = outerSize.width - RingerInset * 2
    val innerHeight = outerSize.height - RingerInset * 2
    val segmentWidth = innerWidth / modes.size
    val thumbDiameter =
        minOf(segmentWidth - RingerThumbInset * 2, innerHeight - RingerThumbInset * 2)
    val iconSize = (thumbDiameter * 0.5f).coerceIn(18.dp, 32.dp)

    Box(
        modifier
            .fillMaxSize()
            .padding(RingerInset)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Box(
            Modifier.offset(
                    x = segmentWidth * thumbPosition + (segmentWidth - thumbDiameter) / 2,
                    y = (innerHeight - thumbDiameter) / 2,
                )
                .size(thumbDiameter)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
        Row(Modifier.fillMaxSize()) {
            modes.indices.forEach { i ->
                Box(
                    Modifier.weight(1f).fillMaxHeight().pointerInput(interactive) {
                        if (interactive) {
                            detectTapGestures { audioManager.setRingerModeInternal(modes[i]) }
                        }
                    },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icons[i],
                        contentDescription = descriptions[i],
                        tint =
                            if (i == selectedIndex) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        modifier = Modifier.size(iconSize),
                    )
                }
            }
        }
    }
}

private class MediaUiState(
    val title: String,
    val artist: String?,
    val art: Bitmap?,
    val isPlaying: Boolean,
    val position: Long,
    val duration: Long,
    val controls: MediaController.TransportControls,
)

@Composable
private fun rememberMediaUiState(): MediaUiState? {
    val context = LocalContext.current
    var controller by remember { mutableStateOf<MediaController?>(null) }
    var metadata by remember { mutableStateOf<MediaMetadata?>(null) }
    var playback by remember { mutableStateOf<PlaybackState?>(null) }
    var position by remember { mutableLongStateOf(0L) }

    DisposableEffect(context) {
        val manager = context.getSystemService(MediaSessionManager::class.java)
        fun pick(list: List<MediaController>?): MediaController? =
            list?.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
                ?: list?.firstOrNull()
        val listener =
            MediaSessionManager.OnActiveSessionsChangedListener { controller = pick(it) }
        try {
            manager.addOnActiveSessionsChangedListener(listener, null)
            controller = pick(manager.getActiveSessions(null))
        } catch (e: SecurityException) {
            controller = null
        }
        onDispose { runCatching { manager.removeOnActiveSessionsChangedListener(listener) } }
    }

    DisposableEffect(controller) {
        val current = controller
        metadata = current?.metadata
        playback = current?.playbackState
        val callback =
            object : MediaController.Callback() {
                override fun onMetadataChanged(newMetadata: MediaMetadata?) {
                    metadata = newMetadata
                }

                override fun onPlaybackStateChanged(state: PlaybackState?) {
                    playback = state
                }

                override fun onSessionDestroyed() {
                    controller = null
                }
            }
        current?.registerCallback(callback)
        onDispose { current?.unregisterCallback(callback) }
    }

    LaunchedEffect(playback) {
        val state = playback
        if (state == null) {
            position = 0L
            return@LaunchedEffect
        }
        while (true) {
            val playing = state.state == PlaybackState.STATE_PLAYING
            position =
                if (playing) {
                    state.position +
                        ((SystemClock.elapsedRealtime() - state.lastPositionUpdateTime) *
                                state.playbackSpeed)
                            .toLong()
                } else {
                    state.position
                }
            if (!playing) break
            delay(500)
        }
    }

    val current = controller ?: return null
    val md = metadata ?: return null
    val title =
        md.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: md.getString(MediaMetadata.METADATA_KEY_TITLE)
    if (title.isNullOrBlank()) return null
    val duration = md.getLong(MediaMetadata.METADATA_KEY_DURATION)
    return MediaUiState(
        title = title,
        artist =
            md.getString(MediaMetadata.METADATA_KEY_ARTIST)
                ?: md.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE),
        art =
            md.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: md.getBitmap(MediaMetadata.METADATA_KEY_ART)
                ?: md.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON),
        isPlaying = playback?.state == PlaybackState.STATE_PLAYING,
        position = position,
        duration = duration,
        controls = current.transportControls,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MediaControl(size: DpSize, interactive: Boolean, modifier: Modifier = Modifier) {
    val media = rememberMediaUiState()
    val base = minOf(size.width, size.height)

    if (media == null) {
        val showLabel = size.height >= 72.dp && size.width >= 140.dp
        Column(
            modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.MusicOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size((base * 0.35f).coerceIn(20.dp, 48.dp)),
            )
            if (showLabel) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Nothing playing",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        return
    }

    val compact = size.height < 100.dp
    val pad = if (compact) 12.dp else 16.dp
    val titleSize = (13f + (base.value - 80f) / 14f).coerceIn(13f, 20f).sp
    val subtitleSize = (titleSize.value - 2f).sp
    val buttonSize = (base * 0.22f).coerceIn(32.dp, 48.dp)
    val artSize =
        if (compact) {
            size.height - pad * 2
        } else {
            minOf(size.width * 0.34f, size.height * 0.42f).coerceIn(48.dp, 120.dp)
        }
    val singleLine = compact || size.width < 260.dp

    val text: @Composable (Modifier) -> Unit = { textModifier ->
        Column(textModifier, verticalArrangement = Arrangement.Center) {
            Text(
                text = media.title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = titleSize),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (singleLine) 1 else 2,
                softWrap = !singleLine,
                overflow = TextOverflow.Ellipsis,
                modifier = if (singleLine) Modifier.basicMarquee() else Modifier,
            )
            if (!media.artist.isNullOrBlank()) {
                Text(
                    text = media.artist,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = subtitleSize),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = if (singleLine) Modifier.basicMarquee() else Modifier,
                )
            }
        }
    }
    val previous: @Composable () -> Unit = {
        MediaButton(Icons.Rounded.SkipPrevious, "Previous", buttonSize, false, interactive) {
            media.controls.skipToPrevious()
        }
    }
    val playPause: @Composable () -> Unit = {
        MediaButton(
            if (media.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            if (media.isPlaying) "Pause" else "Play",
            buttonSize,
            true,
            interactive,
        ) {
            if (media.isPlaying) media.controls.pause() else media.controls.play()
        }
    }
    val next: @Composable () -> Unit = {
        MediaButton(Icons.Rounded.SkipNext, "Next", buttonSize, false, interactive) {
            media.controls.skipToNext()
        }
    }

    if (compact) {
        val wide = size.width >= 200.dp
        Row(
            modifier.fillMaxSize().padding(pad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MediaArt(media.art, artSize)
            text(Modifier.weight(1f))
            if (wide) previous()
            playPause()
            if (wide) next()
        }
    } else {
        Column(
            modifier.fillMaxSize().padding(pad),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MediaArt(media.art, artSize)
                text(Modifier.weight(1f).height(artSize))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (size.height >= 150.dp && media.duration > 0) {
                    MediaSeekBar(
                        position = media.position,
                        duration = media.duration,
                        interactive = interactive,
                        onSeek = { media.controls.seekTo(it) },
                    )
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    previous()
                    playPause()
                    next()
                }
            }
        }
    }
}

private val SeekBarIdleThickness = 6.dp
private val SeekBarPressedThickness = 12.dp
private val SeekBarGap = 6.dp

@Composable
private fun MediaSeekBar(
    position: Long,
    duration: Long,
    interactive: Boolean,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pressed by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    var pendingFraction by remember { mutableStateOf<Float?>(null) }
    val currentOnSeek by rememberUpdatedState(onSeek)
    val currentDuration by rememberUpdatedState(duration)

    val actualFraction = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f
    LaunchedEffect(pendingFraction) {
        if (pendingFraction != null) {
            delay(1500)
            pendingFraction = null
        }
    }
    LaunchedEffect(actualFraction) {
        val pending = pendingFraction
        if (pending != null && abs(actualFraction - pending) < 0.02f) pendingFraction = null
    }
    val fraction = dragFraction ?: pendingFraction ?: actualFraction

    val squish =
        spring<Dp>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
    val thickness by
        animateDpAsState(
            if (pressed) SeekBarPressedThickness else SeekBarIdleThickness,
            squish,
            label = "seekThickness",
        )
    val handleWidth by animateDpAsState(if (pressed) 2.dp else 4.dp, squish, label = "seekHandleW")
    val handleHeight by
        animateDpAsState(if (pressed) 24.dp else 16.dp, squish, label = "seekHandleH")

    val active = MaterialTheme.colorScheme.primary
    val inactive = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

    Canvas(
        modifier
            .fillMaxWidth()
            .height(28.dp)
            .pointerInput(interactive) {
                if (!interactive) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    pressed = true
                    dragFraction = (down.position.x / size.width).coerceIn(0f, 1f)
                    drag(down.id) { change ->
                        change.consume()
                        dragFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                    }
                    val target = dragFraction
                    pressed = false
                    dragFraction = null
                    if (target != null && currentDuration > 0) {
                        pendingFraction = target
                        currentOnSeek((target * currentDuration).toLong())
                    }
                }
            }
    ) {
        val width = size.width
        val centerY = size.height / 2f
        val trackHeight = thickness.toPx()
        val handleW = handleWidth.toPx()
        val handleH = handleHeight.toPx()
        val gap = SeekBarGap.toPx()
        val handleX = (fraction * width).coerceIn(handleW / 2f, width - handleW / 2f)
        val activeEnd = handleX - handleW / 2f - gap
        val inactiveStart = handleX + handleW / 2f + gap
        val radius = CornerRadius(trackHeight / 2f)

        if (activeEnd > 0f) {
            drawRoundRect(
                color = active,
                topLeft = Offset(0f, centerY - trackHeight / 2f),
                size = Size(activeEnd, trackHeight),
                cornerRadius = radius,
            )
        }
        if (inactiveStart < width) {
            drawRoundRect(
                color = inactive,
                topLeft = Offset(inactiveStart, centerY - trackHeight / 2f),
                size = Size(width - inactiveStart, trackHeight),
                cornerRadius = radius,
            )
        }
        drawRoundRect(
            color = active,
            topLeft = Offset(handleX - handleW / 2f, centerY - handleH / 2f),
            size = Size(handleW, handleH),
            cornerRadius = CornerRadius(handleW / 2f),
        )
    }
}

@Composable
private fun MediaArt(art: Bitmap?, artSize: Dp) {
    val shape = RoundedCornerShape(artSize * 0.2f)
    if (art != null) {
        Image(
            bitmap = art.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(artSize).clip(shape),
        )
    } else {
        Box(
            Modifier.size(artSize)
                .clip(shape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(artSize * 0.5f),
            )
        }
    }
}

@Composable
private fun MediaButton(
    icon: ImageVector,
    description: String,
    buttonSize: Dp,
    filled: Boolean,
    interactive: Boolean,
    onClick: () -> Unit,
) {
    val currentOnClick by rememberUpdatedState(onClick)
    Box(
        Modifier.size(buttonSize)
            .clip(CircleShape)
            .then(if (filled) Modifier.background(MaterialTheme.colorScheme.primary) else Modifier)
            .pointerInput(interactive) {
                if (interactive) detectTapGestures { currentOnClick() }
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint =
                if (filled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(buttonSize * 0.55f),
        )
    }
}
