/*
 * Copyright (C) 2026 RisingOS (revived) Android Project
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

import android.service.quicksettings.Tile
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.android.compose.animation.Expandable
import com.android.compose.animation.rememberExpandableController
import com.android.systemui.animation.Expandable
import com.android.systemui.plugins.qs.QSTile
import com.android.systemui.qs.panels.shared.model.FloatingTile
import com.android.systemui.qs.panels.shared.model.GridPlacement
import com.android.systemui.qs.panels.shared.model.QSControl
import com.android.systemui.qs.panels.shared.model.QSControlSpan
import com.android.systemui.qs.panels.shared.model.QSLayoutItem
import com.android.systemui.qs.panels.shared.model.SectionConfig
import com.android.systemui.qs.panels.shared.model.SectionType
import com.android.systemui.qs.panels.ui.compose.infinitegrid.CommonTileDefaults.InactiveCornerRadius
import com.android.systemui.qs.panels.ui.compose.infinitegrid.EditModeTileDefaults
import com.android.systemui.qs.panels.ui.viewmodel.EditTileViewModel
import com.android.systemui.qs.panels.ui.viewmodel.SectionEditModeViewModel
import com.android.systemui.qs.pipeline.domain.model.TileModel
import com.android.systemui.qs.pipeline.shared.TileSpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

private val GRID_SPACING = 12.dp
private val SECTION_SPACING = 8.dp

private const val ONEUI_SPRING_STIFFNESS = 350f
private const val ONEUI_SPRING_DAMPING = 0.85f

@Immutable
class QSControlRenderer(
    val cornerRadius: (QSControl, DpSize) -> Dp,
    val content: @Composable (control: QSControl, span: QSControlSpan, interactive: Boolean) -> Unit,
)

val LocalQSControlRenderer = staticCompositionLocalOf<QSControlRenderer?> { null }

fun TileSpec?.asQSControl(): QSControl? =
    this?.let { s -> QSControl.fromId(s.spec) }

fun TileSpec?.bypassesOneUiContainer(): Boolean =
    this != null && (spec == "brightness" || asQSControl() != null)

private fun SectionType.isRenderedInQs(): Boolean =
    this != SectionType.BRIGHTNESS && this != SectionType.MEDIA

object FloatingTileDragState {
    var isDragging by mutableStateOf(false)
    var hasMoved by mutableStateOf(false)
    var draggingTileSpec: TileSpec? by mutableStateOf(null)
    var dragPosition by mutableStateOf(Offset.Zero)
    var tileTopLeft by mutableStateOf(Offset.Zero)
    var draggedTileSize by mutableStateOf(DpSize.Zero)
    var sourceSectionType: SectionType? by mutableStateOf(null)
    var ghostFloatingTile: FloatingTile? by mutableStateOf(null)
    var ghostEditTile: EditTileViewModel? by mutableStateOf(null)
    var fingerOffset by mutableStateOf(Offset.Unspecified)
    var fallbackTiles = mutableStateMapOf<TileSpec, EditTileViewModel>()
    var ghostTargetOrder: List<TileSpec> by mutableStateOf(emptyList())
    var ghostTargetOwnerId: String? by mutableStateOf(null)
    var isExternalDrag by mutableStateOf(false)
    var dropRequested by mutableStateOf(false)

    fun startDrag(
        tile: FloatingTile,
        position: Offset,
        size: DpSize,
        sourceSection: SectionType,
        editTile: EditTileViewModel? = null,
        fingerOffset: Offset = Offset.Unspecified
    ) {
        isDragging = true
        hasMoved = false
        draggingTileSpec = tile.spec
        tileTopLeft = position
        val offsetToUse = if (fingerOffset.isSpecified) fingerOffset else Offset(110f, 110f)
        dragPosition = position + offsetToUse
        draggedTileSize = size
        sourceSectionType = sourceSection
        ghostFloatingTile = tile
        ghostEditTile = editTile
        this.fingerOffset = fingerOffset

        if (editTile != null) {
            fallbackTiles[tile.spec] = editTile
        }
        isExternalDrag = false
        dropRequested = false
    }

    fun updateDrag(delta: Offset) {
        dragPosition += delta
        tileTopLeft += delta
        hasMoved = true
    }

    fun endDrag() {
        isDragging = false
        hasMoved = false
        draggingTileSpec = null
        dragPosition = Offset.Zero
        tileTopLeft = Offset.Zero
        sourceSectionType = null
        ghostFloatingTile = null
        ghostEditTile = null
        fingerOffset = Offset.Unspecified
        ghostTargetOrder = emptyList()
        ghostTargetOwnerId = null
        isExternalDrag = false
        dropRequested = false
    }

    fun clearFallbackTiles() {
        fallbackTiles.clear()
    }

    const val ONE_UI_GHOST_OWNER_ID = "__one_ui_container__"
}

private sealed interface Segment {
    data class Header(
        val item: QSLayoutItem.SectionHeader,
        val flatIndex: Int,
    ) : Segment

    data class TileGroup(
        val tiles: List<QSLayoutItem.TileItem>,
        val groupKey: String,
    ) : Segment
}

private fun Segment.stableId(): String = when (this) {
    is Segment.Header    -> "header_${item.type.name}"
    is Segment.TileGroup -> "tilegroup_$groupKey"
}

private fun safeSplitBands(positions: List<GridPlacement>): List<IntRange> {
    if (positions.isEmpty()) return emptyList()
    val totalRows = positions.maxOf { it.gridY + it.spanY }
    val bands     = mutableListOf<IntRange>()
    var bandStart = 0
    for (row in 0 until totalRows) {
        val nextRow = row + 1
        val canSplit = positions.none { p -> p.gridY <= row && p.gridY + p.spanY > nextRow }
        if (canSplit || nextRow == totalRows) {
            bands.add(bandStart..row)
            bandStart = nextRow
        }
    }
    return bands
}

private fun splitIntoBands(
    allTiles: List<QSLayoutItem.TileItem>,
    sectionLabel: String,
    keyHints: Map<String, String> = emptyMap(),
): List<Segment.TileGroup> {
    if (allTiles.isEmpty()) return emptyList()
    val floating  = allTiles.map { FloatingTile(it.spec, SectionType.TILES, it.spanCols, it.spanRows) }
    val positions = calculateGridPositions(floating, 4)
    val bands     = safeSplitBands(positions)
    return bands.map { rowRange ->
        val bandTiles = allTiles.indices
            .filter { i -> (positions.getOrNull(i)?.gridY ?: 0) in rowRange }
            .map { allTiles[it] }

        val fingerprint = bandTiles.map { it.spec.spec }.sorted().joinToString(",")
        val groupKey = keyHints[fingerprint]
            ?: "${sectionLabel}_${abs(bandTiles.joinToString(",") { it.spec.spec }.hashCode()).toString(16)}"
        Segment.TileGroup(bandTiles, groupKey)
    }.filter { it.tiles.isNotEmpty() }
}

private fun List<QSLayoutItem>.toSegments(
    splitBands: Boolean,
    existingSegments: List<Segment> = emptyList(),
): List<Segment> {
    val keyHints: Map<String, String> = existingSegments
        .filterIsInstance<Segment.TileGroup>()
        .associate { seg ->
            seg.tiles.map { it.spec.spec }.sorted().joinToString(",") to seg.groupKey
        }

    val result           = mutableListOf<Segment>()
    val invisibleHeaders = mutableListOf<Segment.Header>()
    var tileRun          = mutableListOf<QSLayoutItem.TileItem>()
    var sectionLabel     = "root"

    fun flushTiles() {
        if (tileRun.isEmpty()) return
        if (splitBands) {
            result.addAll(splitIntoBands(tileRun, sectionLabel, keyHints))
        } else {
            val fingerprint = tileRun.map { it.spec.spec }.sorted().joinToString(",")
            val groupKey = keyHints[fingerprint]
                ?: "${sectionLabel}_${abs(tileRun.joinToString(",") { it.spec.spec }.hashCode()).toString(16)}"
            result.add(Segment.TileGroup(tileRun, groupKey))
        }
        tileRun = mutableListOf()
    }

    forEachIndexed { idx, item ->
        when (item) {
            is QSLayoutItem.SectionHeader -> {
                if (item.visible) {
                    flushTiles()
                    sectionLabel = item.type.name
                    result.add(Segment.Header(item, idx))
                } else {
                    invisibleHeaders.add(Segment.Header(item, idx))
                }
            }
            is QSLayoutItem.TileItem -> tileRun.add(item)
        }
    }
    flushTiles()

    result.addAll(invisibleHeaders)

    if (result.none { it is Segment.TileGroup }) {
        val lastVisibleHeaderIdx = result.indexOfLast { it is Segment.Header && it.item.visible }
        val insertIdx = if (lastVisibleHeaderIdx != -1) lastVisibleHeaderIdx + 1 else 0
        result.add(insertIdx, Segment.TileGroup(emptyList(), "tilegroup_empty_fallback"))
    }

    return result
}

private fun List<Segment>.repackAll(splitBands: Boolean): List<Segment> {
    val items = this.flatMap {
        when (it) {
            is Segment.Header -> listOf(it.item)
            is Segment.TileGroup -> it.tiles
        }
    }
    return items.toSegments(splitBands, existingSegments = this)
}

@Composable
fun EditableQuickSettingsLayout(
    flatLayout: List<QSLayoutItem>,
    sectionEditModeViewModel: SectionEditModeViewModel,
    isEditMode: Boolean,
    isEditingOneUi: Boolean = false,
    scrollState: ScrollState,
    nonBrightnessAlpha: Float = 1f,
    allTilesFlow: Flow<List<TileModel>>,
    brightness: @Composable () -> Unit,
    tileBrightness: @Composable (isVertical: Boolean) -> Unit = {},
    tiles: @Composable () -> Unit,
    media: @Composable () -> Unit,
    onFloatingTileClick: (TileSpec, Expandable) -> Unit,
    onAddTile: (TileSpec) -> Unit = {},
    onRemoveTileFromSystem: (TileSpec) -> Unit = {},
    oneUIContainerBounds: androidx.compose.ui.geometry.Rect = androidx.compose.ui.geometry.Rect.Zero,
    onDropInOneUIContainer: (TileSpec, List<TileSpec>?) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    val allTilesMap by remember(allTilesFlow) {
        allTilesFlow.map { list -> list.associate { it.spec to it.tile } }
    }.collectAsState(initial = emptyMap())

    val density = LocalDensity.current
    var isSectionDragging by remember { mutableStateOf(false) }
    var isTileDragging    by remember { mutableStateOf(false) }

    var localSegments by remember {
        mutableStateOf(flatLayout.toSegments(splitBands = false))
    }

    val currentFlatLayout = rememberUpdatedState(flatLayout)

    fun segmentsToItems(segs: List<Segment>): List<QSLayoutItem> = segs.flatMap { seg ->
        when (seg) {
            is Segment.Header    -> listOf(seg.item)
            is Segment.TileGroup -> seg.tiles
        }
    }

    var wasInEditMode by remember { mutableStateOf(isEditMode) }

    LaunchedEffect(isEditMode) {
        val committedOnExit = if (wasInEditMode && !isEditMode) {
            segmentsToItems(localSegments)
        } else {
            null
        }
        if (committedOnExit != null) {
            sectionEditModeViewModel.setFlatLayout(committedOnExit)
            localSegments = committedOnExit.toSegments(splitBands = false, existingSegments = localSegments)
        }
        wasInEditMode = isEditMode

        var pendingExitCommit = committedOnExit
        snapshotFlow { currentFlatLayout.value }
            .collect { layout ->
                if (!isEditMode) {
                    val pending = pendingExitCommit
                    if (pending != null && layout != pending) {
                        return@collect
                    }
                    pendingExitCommit = null
                    localSegments = layout.toSegments(splitBands = false)
                } else {
                    val newSegs = localSegments.toMutableList()
                    var changed = false

                    layout.filterIsInstance<QSLayoutItem.SectionHeader>().forEach { incoming ->
                        val idx = newSegs.indexOfFirst {
                            it is Segment.Header && it.item.type == incoming.type
                        }
                        if (idx != -1) {
                            val seg = newSegs[idx] as Segment.Header
                            if (seg.item.visible != incoming.visible) {
                                newSegs[idx] = seg.copy(
                                    item = seg.item.copy(visible = incoming.visible)
                                )
                                changed = true
                            }
                        }
                    }

                    val existingTileSpecs = newSegs.flatMap {
                        if (it is Segment.TileGroup) it.tiles.map { t -> t.spec.spec } else emptyList()
                    }.toSet()

                    val incomingTiles = layout.filterIsInstance<QSLayoutItem.TileItem>()
                    val missingTiles = incomingTiles.filter { it.spec.spec !in existingTileSpecs }

                    if (missingTiles.isNotEmpty()) {
                        val lastTileGroupIdx = newSegs.indexOfLast { it is Segment.TileGroup }
                        if (lastTileGroupIdx != -1) {
                            val seg = newSegs[lastTileGroupIdx] as Segment.TileGroup
                            newSegs[lastTileGroupIdx] = seg.copy(tiles = seg.tiles + missingTiles)
                        } else {
                            newSegs.add(Segment.TileGroup(missingTiles, "tilegroup_appended"))
                        }
                        changed = true
                    }

                    if (changed) localSegments = newSegs.repackAll(false)
                }
            }
    }

    LaunchedEffect(isEditMode) {
        if (!isEditMode) {
            val currentScroll = scrollState.value.toFloat()
            val tileSize = with(density) { 88.dp.toPx() }
            if (tileSize > 0) {
                val targetScroll = (kotlin.math.round(currentScroll / tileSize) * tileSize).toInt()
                scrollState.animateScrollTo(targetScroll)
            }
        }
    }

    val segTopY     = remember { mutableStateMapOf<String, Float>() }
    val segHeightPx = remember { mutableStateMapOf<String, Float>() }

    var draggingSegId     by remember { mutableStateOf<String?>(null) }
    var absoluteDragY     by remember { mutableFloatStateOf(0f) }
    var lastSectionSwapMs by remember { mutableLongStateOf(0L) }

    var tileSourceSegId by remember { mutableStateOf<String?>(null) }
    var dragOverSegId   by remember { mutableStateOf<String?>(null) }

    val segSlideAnims  = remember { mutableStateMapOf<String, Animatable<Float, *>>() }
    val coroutineScope = rememberCoroutineScope()

    var layoutRootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val screenHeight = LocalContext.current.resources.displayMetrics.heightPixels.toFloat()
    var autoScrollSpeed by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(FloatingTileDragState.dragPosition.y, absoluteDragY, isSectionDragging, FloatingTileDragState.isDragging, FloatingTileDragState.hasMoved) {
        val isTileActive = FloatingTileDragState.isDragging && (FloatingTileDragState.hasMoved || FloatingTileDragState.isExternalDrag)
        if (isEditMode && (isTileActive || isSectionDragging)) {
            val rootScreenY = layoutRootCoordinates?.positionInRoot()?.y ?: 0f
            val dragCenterY = if (isSectionDragging) {
                absoluteDragY
            } else {
                FloatingTileDragState.dragPosition.y - rootScreenY
            }
            val dragScreenY = rootScreenY + dragCenterY * (if (isEditMode) 0.93f else 1f)
            val topThreshold = with(density) { 150.dp.toPx() }
            val bottomThreshold = screenHeight - with(density) { 150.dp.toPx() }

            autoScrollSpeed = when {
                dragScreenY < topThreshold -> {
                    val distance = topThreshold - dragScreenY
                    -(distance * 0.15f).coerceIn(5f, 35f)
                }
                dragScreenY > bottomThreshold -> {
                    val distance = dragScreenY - bottomThreshold
                    (distance * 0.15f).coerceIn(5f, 35f)
                }
                else -> 0f
            }
        } else {
            autoScrollSpeed = 0f
        }
    }

    LaunchedEffect(autoScrollSpeed) {
        if (autoScrollSpeed != 0f) {
            while (autoScrollSpeed != 0f) {
                val prevScroll = scrollState.value
                scrollState.scrollBy(autoScrollSpeed)
                val scrollDelta = scrollState.value - prevScroll
                if (scrollDelta != 0) {
                    if (isSectionDragging) {
                        absoluteDragY += scrollDelta
                    }
                }
                kotlinx.coroutines.delay(16)
            }
        }
    }

    fun trySwap() {
        val id = draggingSegId ?: return
        val currentIndex = localSegments.indexOfFirst { it.stableId() == id }
        if (currentIndex == -1 || localSegments[currentIndex] !is Segment.Header) return

        segTopY[id] ?: return
        val myH   = segHeightPx[id] ?: return
        val myTop = absoluteDragY
        val myBottom = absoluteDragY + myH

        val now = System.currentTimeMillis()
        if (now - lastSectionSwapMs < 180L) return

        val sectionSpacingPx = with(density) { SECTION_SPACING.toPx() }

        fun doSwap(header1Idx: Int, header2Idx: Int) {
            val newSegs = localSegments.toMutableList()
            val block1 = newSegs.subList(header1Idx, header2Idx).toList()
            var endBlock2 = header2Idx + 1
            while (endBlock2 < newSegs.size && newSegs[endBlock2] !is Segment.Header) {
                endBlock2++
            }
            val block2 = newSegs.subList(header2Idx, endBlock2).toList()

            val isMovingDown = currentIndex == header1Idx
            val otherHeaderId = localSegments[if (isMovingDown) header2Idx else header1Idx].stableId()
            val otherTop = segTopY[otherHeaderId] ?: 0f
            val otherH = segHeightPx[otherHeaderId] ?: 0f
            val anim = segSlideAnims.getOrPut(otherHeaderId) { Animatable(0f) }
            val slideDir = if (isMovingDown) 1f else -1f
            coroutineScope.launch {
                anim.snapTo(slideDir * (myH + sectionSpacingPx))
                anim.animateTo(0f, spring(ONEUI_SPRING_DAMPING, ONEUI_SPRING_STIFFNESS))
            }

            segTopY[id] = if (isMovingDown) {
                otherTop + otherH - myH
            } else {
                otherTop
            }

            newSegs.subList(header1Idx, endBlock2).clear()
            newSegs.addAll(header1Idx, block2 + block1)

            localSegments = newSegs.repackAll(splitBands = true)
            lastSectionSwapMs = now
        }

        fun isVisibleSwapSegment(seg: Segment): Boolean = when (seg) {
            is Segment.TileGroup -> true
            is Segment.Header -> seg.item.visible && seg.item.type.isRenderedInQs()
        }

        fun swapWithTileBand(targetIdx: Int) {
            val targetId = localSegments[targetIdx].stableId()
            val targetTop = segTopY[targetId] ?: 0f
            val targetH = segHeightPx[targetId] ?: 0f
            val isMovingDown = currentIndex < targetIdx
            val anim = segSlideAnims.getOrPut(targetId) { Animatable(0f) }
            val slideDir = if (isMovingDown) 1f else -1f
            coroutineScope.launch {
                anim.snapTo(slideDir * (myH + sectionSpacingPx))
                anim.animateTo(0f, spring(ONEUI_SPRING_DAMPING, ONEUI_SPRING_STIFFNESS))
            }

            segTopY[id] = if (isMovingDown) {
                targetTop + targetH - myH
            } else {
                targetTop
            }

            val newSegs = localSegments.toMutableList()
            newSegs[currentIndex] = newSegs[targetIdx].also { newSegs[targetIdx] = newSegs[currentIndex] }
            localSegments = newSegs.repackAll(splitBands = true)
            lastSectionSwapMs = now
        }

        var targetUpIdx = currentIndex - 1
        while (targetUpIdx >= 0 && !isVisibleSwapSegment(localSegments[targetUpIdx])) {
            targetUpIdx--
        }

        if (targetUpIdx >= 0) {
            val prevSeg = localSegments[targetUpIdx]
            val prevId = prevSeg.stableId()
            val prevTop = segTopY[prevId]
            val prevH = segHeightPx[prevId]
            if (prevTop != null && prevH != null) {
                val prevBottom = prevTop + prevH
                if (myTop < prevBottom - (prevH * 0.3f)) {
                    if (prevSeg is Segment.Header) {
                        doSwap(targetUpIdx, currentIndex)
                    } else {
                        swapWithTileBand(targetUpIdx)
                    }
                    return
                }
            }
        }

        var targetDownIdx = currentIndex + 1
        while (targetDownIdx < localSegments.size && !isVisibleSwapSegment(localSegments[targetDownIdx])) {
            targetDownIdx++
        }

        if (targetDownIdx < localSegments.size) {
            val nextSeg = localSegments[targetDownIdx]
            val nextId = nextSeg.stableId()
            val nextTop = segTopY[nextId]
            val nextH = segHeightPx[nextId]
            if (nextTop != null && nextH != null) {
                if (myBottom > nextTop + (nextH * 0.3f)) {
                    if (nextSeg is Segment.Header) {
                        doSwap(currentIndex, targetDownIdx)
                    } else {
                        swapWithTileBand(targetDownIdx)
                    }
                    return
                }
            }
        }
    }

    LaunchedEffect(
        FloatingTileDragState.isDragging,
        FloatingTileDragState.dragPosition,
        layoutRootCoordinates,
    ) {
        val dragActive = FloatingTileDragState.isDragging &&
                (isEditMode || FloatingTileDragState.isExternalDrag)
        val rootCoords = layoutRootCoordinates

        if (dragActive && rootCoords != null) {
            isTileDragging = true
            val rootY = rootCoords.positionInRoot().y
            val draggedHeightPx = with(density) { FloatingTileDragState.draggedTileSize.height.toPx() }
            val tileTopY = FloatingTileDragState.tileTopLeft.y - rootY
            val tileBottomY = tileTopY + draggedHeightPx
            val tileCenterY = (tileTopY + tileBottomY) / 2f

            val validSegs = localSegments.filter { seg ->
                segTopY.containsKey(seg.stableId()) &&
                        segHeightPx.containsKey(seg.stableId()) &&
                        when (seg) {
                            is Segment.TileGroup -> true
                            is Segment.Header -> seg.item.visible && seg.item.type.isRenderedInQs()
                        }
            }

            val isBrightnessDrag = FloatingTileDragState.draggingTileSpec.bypassesOneUiContainer()
            if (isEditingOneUi || (!isBrightnessDrag && oneUIContainerBounds.contains(FloatingTileDragState.dragPosition))) {
                dragOverSegId = null
            } else {
                dragOverSegId = validSegs
                    .mapNotNull { seg ->
                        val sid = seg.stableId()
                        val top = segTopY[sid]!!
                        val bottom = top + segHeightPx[sid]!!
                        val overlap = (min(tileBottomY, bottom) - max(tileTopY, top)).coerceAtLeast(0f)
                        if (overlap > 0f) seg to overlap else null
                    }
                    .maxByOrNull { it.second }
                    ?.first
                    ?.stableId()
                    ?: validSegs.minByOrNull { seg ->
                    val sid = seg.stableId()
                    val top = segTopY[sid]!!
                    val h   = segHeightPx[sid]!!
                    abs(tileCenterY - (top + h / 2f))
                }?.stableId()
            }
        } else if (!dragActive) {
            isTileDragging  = false
            dragOverSegId   = null
            tileSourceSegId = null
        }
    }

    fun commitCrossSegmentDrop(tileSpec: TileSpec, targetSegId: String) {
        val newSegs = localSegments.toMutableList()
        var movedTile: QSLayoutItem.TileItem? = null

        val srcIdx = newSegs.indexOfFirst { seg ->
            seg is Segment.TileGroup && seg.tiles.any { it.spec == tileSpec }
        }

        if (srcIdx != -1) {
            val srcSeg = newSegs[srcIdx] as Segment.TileGroup
            movedTile = srcSeg.tiles.first { it.spec == tileSpec }
            val remaining = srcSeg.tiles.filter { it.spec != tileSpec }
            if (remaining.isEmpty()) newSegs.removeAt(srcIdx) else newSegs[srcIdx] = srcSeg.copy(tiles = remaining)
        } else {
            val ghost = FloatingTileDragState.ghostFloatingTile
            if (ghost != null && ghost.spec == tileSpec) {
                movedTile = QSLayoutItem.TileItem(tileSpec, ghost.spanCols, ghost.spanRows)
            }
        }

        val finalMovedTile = movedTile ?: return
        val tgtIdx = newSegs.indexOfFirst { it.stableId() == targetSegId }
        val ghostOrder = if (FloatingTileDragState.ghostTargetOwnerId == targetSegId) {
            FloatingTileDragState.ghostTargetOrder
        } else {
            emptyList()
        }

        when (val tgt = newSegs.getOrNull(tgtIdx)) {
            is Segment.TileGroup -> {
                val existingSpecs = tgt.tiles.map { it.spec }
                val bySpec = tgt.tiles.associateBy { it.spec }
                val mergedTiles: List<QSLayoutItem.TileItem> = if (ghostOrder.isNotEmpty()) {
                    val result = mutableListOf<QSLayoutItem.TileItem>()
                    for (spec in ghostOrder) {
                        when {
                            spec == tileSpec -> result.add(finalMovedTile)
                            bySpec.containsKey(spec) -> result.add(bySpec[spec]!!)
                        }
                    }
                    existingSpecs.forEach { s -> if (result.none { it.spec == s }) result.add(bySpec[s]!!) }
                    if (result.none { it.spec == tileSpec }) result.add(finalMovedTile)
                    result
                } else {
                    tgt.tiles + finalMovedTile
                }
                newSegs[tgtIdx] = tgt.copy(tiles = mergedTiles)
            }
            is Segment.Header -> {
                val nextIdx = tgtIdx + 1
                if (newSegs.getOrNull(nextIdx) is Segment.TileGroup) {
                    val next = newSegs[nextIdx] as Segment.TileGroup
                    newSegs[nextIdx] = next.copy(tiles = next.tiles + finalMovedTile)
                } else {
                    newSegs.add(nextIdx, Segment.TileGroup(listOf(finalMovedTile), "__tmp__"))
                }
            }
            null -> {
                newSegs.add(Segment.TileGroup(listOf(finalMovedTile), "__tmp__"))
            }
        }

        localSegments = newSegs.repackAll(false)
        sectionEditModeViewModel.setFlatLayout(segmentsToItems(localSegments))
    }

    LaunchedEffect(FloatingTileDragState.dropRequested) {
        if (FloatingTileDragState.dropRequested && FloatingTileDragState.isExternalDrag) {
            val spec = FloatingTileDragState.draggingTileSpec
            if (spec != null) {
                val isBrightnessDrag = spec.bypassesOneUiContainer()
                if (isEditingOneUi || (!isBrightnessDrag && oneUIContainerBounds.contains(FloatingTileDragState.dragPosition))) {
                    val canUseOneUiGhostOrder =
                        (FloatingTileDragState.ghostFloatingTile?.spanRows ?: 1) <= 1 &&
                            FloatingTileDragState.ghostTargetOwnerId == FloatingTileDragState.ONE_UI_GHOST_OWNER_ID
                    val newOrder =
                        if (canUseOneUiGhostOrder) FloatingTileDragState.ghostTargetOrder.ifEmpty { null } else null
                    onDropInOneUIContainer(spec, newOrder)
                } else {
                    val targetId = dragOverSegId ?: localSegments.firstOrNull { it is Segment.TileGroup }?.stableId()
                    val controlDefault = spec.asQSControl()?.spans(4)?.default
                    val spanCols = FloatingTileDragState.ghostFloatingTile?.spanCols
                        ?: controlDefault?.columns
                        ?: if (spec.spec == "brightness") 3 else 1
                    val spanRows = FloatingTileDragState.ghostFloatingTile?.spanRows
                        ?: controlDefault?.rows
                        ?: 1

                    if (targetId != null) {
                        sectionEditModeViewModel.addMainQSTile(FloatingTile(spec, SectionType.TILES, spanCols, spanRows))
                        onAddTile(spec)
                        commitCrossSegmentDrop(spec, targetId)
                    } else {
                        sectionEditModeViewModel.addMainQSTile(FloatingTile(spec, SectionType.TILES, spanCols, spanRows))
                        onAddTile(spec)
                        val newTile = QSLayoutItem.TileItem(spec, spanCols, spanRows)
                        localSegments = (localSegments + Segment.TileGroup(listOf(newTile), "tilegroup_fallback")).repackAll(false)
                        sectionEditModeViewModel.setFlatLayout(segmentsToItems(localSegments))
                    }
                }
            }

            FloatingTileDragState.dropRequested = false
            FloatingTileDragState.isExternalDrag = false
            FloatingTileDragState.endDrag()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { layoutRootCoordinates = it }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            val allSegmentsForRender = localSegments.filter {
                !(it is Segment.Header && !it.item.type.isRenderedInQs())
            }

            val visibleSegIds = localSegments
                .filter { seg ->
                    when (seg) {
                        is Segment.Header    -> seg.item.visible && seg.item.type.isRenderedInQs()
                        is Segment.TileGroup -> true
                    }
                }
                .map { it.stableId() }
                .toSet()

            allSegmentsForRender.forEachIndexed { segPos, segment ->
                val segId          = segment.stableId()
                val isThisDragging = segId == draggingSegId
                val isVisible      = segId in visibleSegIds
                val isDropTarget   = isEditMode && isTileDragging &&
                        segId == dragOverSegId && segId != tileSourceSegId
                val isBrightness   = false

                key(segId) {
                    val segmentContent = @Composable {
                        val slideOffsetPx = segSlideAnims[segId]?.value ?: 0f
                        val prevVisibleSeg = allSegmentsForRender.take(segPos).lastOrNull { it.stableId() in visibleSegIds }
                        val gap = if (prevVisibleSeg != null) {
                            if (segment is Segment.TileGroup && prevVisibleSeg is Segment.TileGroup) GRID_SPACING else SECTION_SPACING
                        } else 0.dp

                        Column {
                            if (gap > 0.dp) {
                                Spacer(modifier = Modifier.height(gap))
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        if (!isBrightness) {
                                            alpha = nonBrightnessAlpha
                                        }
                                    }
                                    .onGloballyPositioned { coords ->
                                        segTopY[segId]     = layoutRootCoordinates?.localPositionOf(coords, Offset.Zero)?.y ?: 0f
                                        segHeightPx[segId] = coords.size.height.toFloat()
                                    }
                                    .zIndex(if (isThisDragging) 10f else 0f)
                                    .offset {
                                        IntOffset(
                                            0,
                                            if (isThisDragging) {
                                                val currentLayoutY = segTopY[segId] ?: 0f
                                                (absoluteDragY - currentLayoutY).roundToInt()
                                            } else {
                                                slideOffsetPx.roundToInt()
                                            }
                                        )
                                    },
                            ) {
                                when (segment) {
                                    is Segment.Header -> {
                                        val header = segment.item
                                        val cardDragModifier = if (isEditMode) {
                                            Modifier.pointerInput(segId) {
                                                detectDragGesturesAfterLongPress(
                                                    onDragStart = { _ ->
                                                        draggingSegId     = segId
                                                        isSectionDragging = true
                                                        absoluteDragY     = segTopY[segId] ?: 0f
                                                        localSegments     = localSegments.repackAll(splitBands = true)
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        change.consume()
                                                        absoluteDragY += dragAmount.y
                                                        trySwap()
                                                    },
                                                    onDragEnd = {
                                                        draggingSegId     = null
                                                        isSectionDragging = false
                                                        localSegments     = localSegments.repackAll(splitBands = false)
                                                        sectionEditModeViewModel.setFlatLayout(segmentsToItems(localSegments))
                                                    },
                                                    onDragCancel = {
                                                        draggingSegId     = null
                                                        isSectionDragging = false
                                                        localSegments     = localSegments.repackAll(splitBands = false)
                                                    },
                                                )
                                            }
                                        } else Modifier

                                        SectionCard(
                                            header = header,
                                            isEditMode = isEditMode,
                                            isDragging = isThisDragging,
                                            cardDragModifier = cardDragModifier,
                                            onRemove = {
                                                val newSegs = localSegments.toMutableList()
                                                val idx = newSegs.indexOfFirst { it.stableId() == segId }
                                                if (idx != -1) {
                                                    newSegs[idx] = Segment.Header(
                                                        header.copy(visible = false), segment.flatIndex
                                                    )
                                                    localSegments = newSegs.repackAll(false)
                                                    sectionEditModeViewModel.setFlatLayout(segmentsToItems(localSegments))
                                                }
                                            },
                                            onHeightScaleChange = { newScale ->
                                                val newSegs = localSegments.toMutableList()
                                                val idx = newSegs.indexOfFirst { it.stableId() == segId }
                                                if (idx != -1) {
                                                    newSegs[idx] = Segment.Header(
                                                        header.copy(heightScale = newScale.toFloat()), segment.flatIndex
                                                    )
                                                    localSegments = newSegs
                                                    sectionEditModeViewModel.setFlatLayout(segmentsToItems(localSegments))
                                                }
                                            },
                                            brightness = brightness,
                                            tiles = tiles,
                                            media = media,
                                        )
                                    }

                                    is Segment.TileGroup -> {
                                        val floatingTiles = segment.tiles.map {
                                            FloatingTile(it.spec, SectionType.TILES, it.spanCols, it.spanRows)
                                        }

                                        TileGroupArea(
                                            segId             = segId,
                                            tiles             = floatingTiles,
                                            tileItems         = segment.tiles,
                                            allTilesMap       = allTilesMap,
                                            isEditMode        = isEditMode,
                                            isDropTarget      = isDropTarget,
                                            onTileClick       = onFloatingTileClick,
                                            onTileDragStart   = {
                                                tileSourceSegId = segId
                                            },
                                            isDropExternal  = {
                                                val isBrightnessDrag = FloatingTileDragState.draggingTileSpec.bypassesOneUiContainer()
                                                (dragOverSegId != null && dragOverSegId != segId) ||
                                                isEditingOneUi || (!isBrightnessDrag && oneUIContainerBounds.contains(FloatingTileDragState.dragPosition))
                                            },
                                            onFlatLayoutUpdate = { updatedTiles, repack ->
                                                val newSegs = localSegments.toMutableList()
                                                val idx = newSegs.indexOfFirst { it.stableId() == segId }
                                                if (idx != -1) {
                                                    newSegs[idx] = (newSegs[idx] as Segment.TileGroup).copy(tiles = updatedTiles)
                                                    localSegments = if (repack) newSegs.repackAll(false) else newSegs
                                                    sectionEditModeViewModel.setFlatLayout(segmentsToItems(localSegments))
                                                }
                                            },
                                            onRemoveTile = { spec ->
                                                val newSegs = localSegments.toMutableList()
                                                var changed = false
                                                for (i in newSegs.indices) {
                                                    val seg = newSegs[i]
                                                    if (seg is Segment.TileGroup && seg.tiles.any { it.spec == spec }) {
                                                        val updated = seg.tiles.filter { it.spec != spec }
                                                        newSegs[i] = seg.copy(tiles = updated)
                                                        changed = true
                                                    }
                                                }
                                                if (changed) {
                                                    localSegments = newSegs.repackAll(false)
                                                    sectionEditModeViewModel.setFlatLayout(segmentsToItems(localSegments))
                                                }
                                                onRemoveTileFromSystem(spec)
                                            },
                                            onTileDragEnd = { tileSpec ->
                                                val isBrightnessDrag = tileSpec.bypassesOneUiContainer()
                                                if (isEditingOneUi || (!isBrightnessDrag && oneUIContainerBounds.contains(FloatingTileDragState.dragPosition))) {
                                                    val newSegs = localSegments.toMutableList()
                                                    var changed = false
                                                    for (i in newSegs.indices) {
                                                        val seg = newSegs[i]
                                                        if (seg is Segment.TileGroup && seg.tiles.any { it.spec == tileSpec }) {
                                                            val updated = seg.tiles.filter { it.spec != tileSpec }
                                                            newSegs[i] = seg.copy(tiles = updated)
                                                            changed = true
                                                        }
                                                    }
                                                    if (changed) {
                                                        localSegments = newSegs.repackAll(false)
                                                        sectionEditModeViewModel.setFlatLayout(segmentsToItems(localSegments))
                                                    }
                                                    val canUseOneUiGhostOrder =
                                                        (FloatingTileDragState.ghostFloatingTile?.spanRows ?: 1) <= 1 &&
                                                            FloatingTileDragState.ghostTargetOwnerId == FloatingTileDragState.ONE_UI_GHOST_OWNER_ID
                                                    val newOrder =
                                                        if (canUseOneUiGhostOrder) FloatingTileDragState.ghostTargetOrder.ifEmpty { null } else null
                                                    onDropInOneUIContainer(tileSpec, newOrder)
                                                } else {
                                                    val targetId = dragOverSegId
                                                    val sourceId = tileSourceSegId
                                                    if (targetId != null && targetId != sourceId) {
                                                        commitCrossSegmentDrop(tileSpec, targetId)
                                                    } else {
                                                        localSegments = localSegments.repackAll(false)
                                                        sectionEditModeViewModel.setFlatLayout(segmentsToItems(localSegments))
                                                    }
                                                }
                                                isTileDragging  = false
                                                dragOverSegId   = null
                                                tileSourceSegId = null
                                                FloatingTileDragState.endDrag()
                                            },
                                            tileBrightness = tileBrightness,
                                            layoutRootCoordinates = layoutRootCoordinates
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (segment is Segment.Header) {
                        AnimatedVisibility(
                            visible = isVisible,
                            enter   = fadeIn(tween(220)) + expandVertically(spring(ONEUI_SPRING_DAMPING, ONEUI_SPRING_STIFFNESS)),
                            exit    = fadeOut(tween(180)) + shrinkVertically(spring(ONEUI_SPRING_DAMPING, ONEUI_SPRING_STIFFNESS)),
                        ) {
                            segmentContent()
                        }
                    } else if (isVisible) {
                        segmentContent()
                    }

                    val nextVisibleSeg = allSegmentsForRender
                        .drop(segPos + 1)
                        .firstOrNull { it.stableId() in visibleSegIds }
                    val noTilesBelow = nextVisibleSeg == null || nextVisibleSeg is Segment.Header
                    val showGap = isEditMode && isTileDragging &&
                            segment is Segment.Header &&
                            noTilesBelow &&
                            segId == dragOverSegId &&
                            segId != tileSourceSegId
                    AnimatedVisibility(
                        visible = showGap,
                        enter   = expandVertically(spring(ONEUI_SPRING_DAMPING, ONEUI_SPRING_STIFFNESS)),
                        exit    = shrinkVertically(spring(ONEUI_SPRING_DAMPING, ONEUI_SPRING_STIFFNESS)),
                    ) {
                        Spacer(modifier = Modifier.fillMaxWidth().height(72.dp))
                    }

                    val nextSeg = allSegmentsForRender
                        .drop(segPos + 1)
                        .firstOrNull { it.stableId() in visibleSegIds }
                    val showEdgeGap = run {
                        if (!isEditMode || !isTileDragging) return@run false
                        if (segment !is Segment.Header) return@run false
                        if (nextSeg !is Segment.Header) return@run false
                        if (dragOverSegId?.startsWith("tilegroup_") == true) return@run false

                        val rootY = layoutRootCoordinates?.positionInRoot()?.y ?: 0f
                        val edgeSnapPx = with(density) { 80.dp.toPx() }
                        val myTop    = segTopY[segId]     ?: return@run false
                        val myH      = segHeightPx[segId] ?: return@run false
                        val myBottom = myTop + myH
                        val fingerY  = FloatingTileDragState.dragPosition.y - rootY
                        abs(fingerY - myBottom) < edgeSnapPx
                    }
                    AnimatedVisibility(
                        visible = showEdgeGap,
                        enter   = expandVertically(spring(ONEUI_SPRING_DAMPING, ONEUI_SPRING_STIFFNESS)),
                        exit    = shrinkVertically(spring(ONEUI_SPRING_DAMPING, ONEUI_SPRING_STIFFNESS)),
                    ) {
                        Spacer(modifier = Modifier.fillMaxWidth().height(80.dp))
                    }
                }
            }

            if (isEditMode) Spacer(modifier = Modifier.height(120.dp))
        }

        if ((isEditMode || FloatingTileDragState.isExternalDrag) && FloatingTileDragState.isDragging &&
            FloatingTileDragState.draggingTileSpec != null
        ) {
            val draggingSpec = FloatingTileDragState.draggingTileSpec!!
            val ghost = FloatingTileDragState.ghostFloatingTile
            val draggingTileItem = if (ghost != null && ghost.spec == draggingSpec) {
                QSLayoutItem.TileItem(ghost.spec, ghost.spanCols, ghost.spanRows)
            } else null

            val rootCoords = layoutRootCoordinates
            if (draggingTileItem != null && rootCoords != null) {
                val it = draggingTileItem
                val asTile = FloatingTile(it.spec, SectionType.TILES, it.spanCols, it.spanRows)

                val scaleAnim = remember { Animatable(1.0f) }
                LaunchedEffect(Unit) {
                    scaleAnim.animateTo(1.05f, spring(Spring.DampingRatioLowBouncy, ONEUI_SPRING_STIFFNESS))
                }

                val rootX = rootCoords.positionInRoot().x
                val rootY = rootCoords.positionInRoot().y

                val isBrightnessDrag = draggingSpec.bypassesOneUiContainer()
                val isOneUiDrag = isEditingOneUi || (!isBrightnessDrag && oneUIContainerBounds.contains(FloatingTileDragState.dragPosition))

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (FloatingTileDragState.tileTopLeft.x - rootX).roundToInt(),
                                (FloatingTileDragState.tileTopLeft.y - rootY).roundToInt(),
                            )
                        }
                        .size(FloatingTileDragState.draggedTileSize)
                        .scale(scaleAnim.value)
                        .zIndex(100f)
                ) {
                    DraggableTile(
                        tile         = asTile,
                        qsTile       = allTilesMap[draggingSpec],
                        onClick      = {},
                        gridCellSize = 0.dp,
                        isEditMode   = true,
                        isResizing   = false,
                        elevation    = 8.dp,
                        onRemove     = {},
                        onResizeStart= {},
                        onUpdateGhost = {_,_ ->},
                        onPreviewResize = {_,_ ->},
                        onResizeEnd  = {},
                        tileBrightness = tileBrightness,
                        editTile     = FloatingTileDragState.ghostEditTile,
                        useAospStyle = isOneUiDrag,
                        isOneUi      = isOneUiDrag,
                        isDragOverlay = true
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    header: QSLayoutItem.SectionHeader,
    isEditMode: Boolean,
    isDragging: Boolean,
    cardDragModifier: Modifier,
    onRemove: () -> Unit,
    onHeightScaleChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    brightness: @Composable () -> Unit,
    tiles: @Composable () -> Unit,
    media: @Composable () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue   = if (isDragging && isEditMode) 1.03f else 1f,
        animationSpec = spring(ONEUI_SPRING_DAMPING, ONEUI_SPRING_STIFFNESS),
        label         = "section_scale"
    )
    val borderAlpha by animateFloatAsState(
        targetValue   = if (isEditMode) 1f else 0f,
        animationSpec = spring(ONEUI_SPRING_DAMPING, ONEUI_SPRING_STIFFNESS),
        label         = "section_border_alpha"
    )

    val borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)

    val sectionCornerRadius = 28.dp

    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .then(if (isEditMode) cardDragModifier else Modifier)
                .then(
                    when {
                        isEditMode && header.type == SectionType.TILES ->
                            Modifier.clip(RoundedCornerShape(sectionCornerRadius))

                        isEditMode && header.type == SectionType.BRIGHTNESS ->
                            Modifier.clip(RoundedCornerShape(sectionCornerRadius))

                        isEditMode && header.type == SectionType.MEDIA ->
                            Modifier.clip(RoundedCornerShape(sectionCornerRadius))

                        isEditMode ->
                            Modifier
                                .clip(RoundedCornerShape(sectionCornerRadius))
                                .drawWithContent {
                                    drawContent()
                                    if (borderAlpha > 0f) {
                                        drawRoundRect(
                                            color  = borderColor.copy(alpha = borderColor.alpha * borderAlpha),
                                            cornerRadius = CornerRadius(sectionCornerRadius.toPx()),
                                            style  = Stroke(width = 2.dp.toPx()),
                                        )
                                    }
                                }
                        else -> Modifier
                    }
                )
        ) {
            Box(
                modifier = if (isEditMode && header.type != SectionType.TILES)
                    Modifier.graphicsLayer { shape = RoundedCornerShape(sectionCornerRadius); clip = true }
                else Modifier
            ) {
                when (header.type) {
                    SectionType.BRIGHTNESS -> brightness()
                    SectionType.TILES      -> tiles()
                    SectionType.MEDIA      -> media()
                }
                if (isEditMode && header.type != SectionType.TILES) {
                    Spacer(
                        modifier = Modifier
                            .matchParentSize()
                            .pointerInput(Unit) { detectTapGestures(onPress = {}, onTap = {}) }
                    )
                }
            }
        }

        if (isEditMode) {
            if (header.type.canBeRemoved) {
                Surface(
                    onClick         = onRemove,
                    shape           = CircleShape,
                    color           = MaterialTheme.colorScheme.error,
                    shadowElevation = 4.dp,
                    modifier        = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .zIndex(20f),
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Close,
                        contentDescription = "Remove",
                        modifier           = Modifier.padding(4.dp),
                        tint               = MaterialTheme.colorScheme.onError,
                    )
                }
            }

            if (header.type == SectionType.TILES) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .zIndex(20f),
                ) {
                    CollapsedRowsThumb(
                        value    = header.heightScale.toInt(),
                        onChange = onHeightScaleChange,
                    )
                }
            }
        }
    }
}

@Composable
private fun TileGroupArea(
    segId: String,
    tiles: List<FloatingTile>,
    tileItems: List<QSLayoutItem.TileItem>,
    allTilesMap: Map<TileSpec, QSTile>,
    isEditMode: Boolean,
    isDropTarget: Boolean,
    onTileClick: (TileSpec, Expandable) -> Unit,
    onTileDragStart: () -> Unit,
    onFlatLayoutUpdate: (List<QSLayoutItem.TileItem>, repack: Boolean) -> Unit,
    onRemoveTile: (TileSpec) -> Unit,
    onTileDragEnd: (TileSpec) -> Unit,
    isDropExternal: () -> Boolean,
    tileBrightness: @Composable (isVertical: Boolean) -> Unit,
    layoutRootCoordinates: LayoutCoordinates?
) {
    val draggingFromHere = FloatingTileDragState.isDragging &&
            tiles.any { it.spec == FloatingTileDragState.draggingTileSpec }

    val effectiveTiles = tiles
    if (effectiveTiles.isEmpty() && !isEditMode && !isDropTarget && !draggingFromHere) return

    DraggableGrid(
        tiles        = tiles,
        allTilesMap  = allTilesMap,
        isEditMode   = isEditMode,
        sectionType  = SectionType.TILES,
        columns      = 4,
        isDropTarget = isDropTarget,
        onTileClick  = { tile, expandable -> if (!isEditMode) onTileClick(tile.spec, expandable) },
        onResize     = { tileId, newCols, newRows ->
            val updated = tileItems.map { item ->
                if (item.spec.spec == tileId) item.copy(spanCols = newCols, spanRows = newRows) else item
            }
            onFlatLayoutUpdate(updated, true)
        },
        onRemove     = { tileId ->
            val spec = tiles.find { it.spec.spec == tileId }?.spec ?: return@DraggableGrid
            onRemoveTile(spec)
        },
        onMove       = { reorderedTiles ->
            val specOrder = reorderedTiles.map { it.spec }
            val itemsBySpec = tileItems.associateBy { it.spec }
            val updated = specOrder.mapNotNull { itemsBySpec[it] }
            if (updated.size == tileItems.size) {
                onFlatLayoutUpdate(updated, false)
            }
        },
        onDragStart  = onTileDragStart,
        onDragEnd    = onTileDragEnd,
        isDropExternal = isDropExternal,
        tileBrightness = tileBrightness,
        layoutRootCoordinates = layoutRootCoordinates,
        gridOwnerId  = segId,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
private fun FloatingTilesArea(
    tiles: List<FloatingTile>,
    allTilesMap: Map<TileSpec, QSTile>,
    isEditMode: Boolean,
    sectionType: SectionType,
    sectionEditModeViewModel: SectionEditModeViewModel,
    isDropTarget: Boolean,
    onTileClick: (TileSpec) -> Unit,
    onDragEnd: (TileSpec) -> Unit,
    tileBrightness: @Composable (isVertical: Boolean) -> Unit,
    layoutRootCoordinates: LayoutCoordinates?
) {
    if (tiles.isEmpty() && !isEditMode && !isDropTarget) return

    DraggableGrid(
        tiles        = tiles,
        allTilesMap  = allTilesMap,
        isEditMode   = isEditMode,
        sectionType  = sectionType,
        columns      = 4,
        isDropTarget = isDropTarget,
        onTileClick  = { tile, _ -> if (!isEditMode) onTileClick(tile.spec) },
        onResize     = { tileId, newCols, newRows ->
            val tile = tiles.find { it.spec.spec == tileId }
            if (tile != null) {
                sectionEditModeViewModel.removeFloatingTile(tile.spec)
                sectionEditModeViewModel.addFloatingTile(tile.copy(spanCols = newCols, spanRows = newRows), sectionType)
            }
        },
        onRemove     = { tileId ->
            tiles.find { it.spec.spec == tileId }?.let { sectionEditModeViewModel.removeFloatingTile(it.spec) }
        },
        onMove       = { reorderedTiles ->
            if (reorderedTiles.size == tiles.size && reorderedTiles != tiles) {
                reorderedTiles.forEach { sectionEditModeViewModel.removeFloatingTile(it.spec) }
                reorderedTiles.forEach { sectionEditModeViewModel.addFloatingTile(it, sectionType) }
            }
        },
        onDragEnd    = onDragEnd,
        tileBrightness = tileBrightness,
        layoutRootCoordinates = layoutRootCoordinates,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

fun solveEvenEdgeGrid(
    totalWidth: Dp,
    gapParam: Dp,
    cellSizeScale: Float,
    columns: Int,
    evenEdgeSpacing: Boolean,
): Triple<Dp, Dp, Dp> {
    if (!evenEdgeSpacing) {
        val cellSize = ((totalWidth - gapParam * (columns - 1)) / columns) * cellSizeScale
        return Triple(cellSize, gapParam, 0.dp)
    }
    val cellSize = ((totalWidth - gapParam * (columns + 1)) / columns) * cellSizeScale
    val innerGap = ((totalWidth - cellSize * columns) * 3 / 11).coerceAtLeast(0.dp)
    val outerGap = innerGap / 3
    return Triple(cellSize, innerGap, outerGap)
}

private fun canFitGridItems(tiles: List<FloatingTile>, columns: Int): Boolean {
    val occupied = mutableSetOf<Pair<Int, Int>>()
    var maxRow = 0
    for (tile in tiles) {
        if (tile.spanCols <= 0 || tile.spanRows <= 0) continue
        var placed = false
        var searchRow = maxRow
        while (!placed && searchRow < 200) {
            for (col in 0 until columns) {
                if (col + tile.spanCols > columns) continue
                var fits = true
                for (r in 0 until tile.spanRows) {
                    for (c in 0 until tile.spanCols) {
                        if ((searchRow + r) to (col + c) in occupied) {
                            fits = false
                            break
                        }
                    }
                    if (!fits) break
                }
                if (fits) {
                    for (r in 0 until tile.spanRows) {
                        for (c in 0 until tile.spanCols) {
                            occupied.add((searchRow + r) to (col + c))
                        }
                    }
                    placed = true
                    if (searchRow > maxRow) maxRow = searchRow
                    break
                }
            }
            if (!placed) searchRow++
        }
        if (!placed) return false
    }
    return true
}

@Composable
fun DraggableGrid(
    tiles: List<FloatingTile>,
    allTilesMap: Map<TileSpec, QSTile>,
    isEditMode: Boolean,
    sectionType: SectionType,
    columns: Int,
    isDropTarget: Boolean,
    onTileClick: (FloatingTile, Expandable) -> Unit,
    onResize: (String, Int, Int) -> Unit = { _, _, _ -> },
    onRemove: (String) -> Unit,
    onMove: (List<FloatingTile>) -> Unit,
    onDragStart: () -> Unit = {},
    onDragEnd: (TileSpec) -> Unit,
    isDropExternal: () -> Boolean = { false },
    tileBrightness: @Composable (isVertical: Boolean) -> Unit,
    layoutRootCoordinates: LayoutCoordinates?,
    useAospStyle: Boolean = false,
    isOneUi: Boolean = false,
    gridOwnerId: String? = null,
    modifier: Modifier = Modifier,
    cellSizeScale: Float = 1f,
    gridSpacingParam: Dp = 12.dp,
    evenEdgeSpacing: Boolean = false,
    interactionsEnabled: Boolean = true,
) {
    val currentOnMove by rememberUpdatedState(onMove)
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentIsDropExternal by rememberUpdatedState(isDropExternal)

    var localTiles by remember { mutableStateOf(tiles) }
    var isSource   by remember { mutableStateOf(false) }
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var initialNonDragged by remember { mutableStateOf<List<FloatingTile>>(emptyList()) }

    LaunchedEffect(tiles, isDropTarget, isSource, FloatingTileDragState.isDragging) {
        if (!isSource && isDropTarget && FloatingTileDragState.isDragging) {
            val ghost = FloatingTileDragState.ghostFloatingTile
            if (ghost != null && tiles.none { it.spec == ghost.spec }) {
                val nextTiles = tiles + ghost
                localTiles = nextTiles
                initialNonDragged = tiles
                FloatingTileDragState.ghostTargetOrder = nextTiles.map { it.spec }
                FloatingTileDragState.ghostTargetOwnerId =
                    if (isOneUi) FloatingTileDragState.ONE_UI_GHOST_OWNER_ID else gridOwnerId
            } else {
                localTiles = tiles
            }
        } else {
            localTiles = tiles
            if (!FloatingTileDragState.isDragging) {
                initialNonDragged = emptyList()
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val totalWidth = maxWidth
        val density    = LocalDensity.current

        val (cellSize, gridSpacing, evenEdgeOuterGap) =
            solveEvenEdgeGrid(totalWidth, gridSpacingParam, cellSizeScale, columns, evenEdgeSpacing)

        val gridCenterOffset = if (evenEdgeSpacing) {
            evenEdgeOuterGap
        } else {
            val occupiedGridWidth = (cellSize * columns) + (gridSpacing * (columns - 1))
            ((totalWidth - occupiedGridWidth) / 2).coerceAtLeast(0.dp)
        }

        var lastSwapTime          by remember { mutableLongStateOf(0L) }
        var gridLocalPosition     by remember { mutableStateOf(Offset.Zero) }
        var crossSegmentDrop      by remember { mutableStateOf(false) }
        val isCrossSegment = isSource && currentIsDropExternal()

        val activePositionsList = remember(localTiles, isCrossSegment, draggingIndex) {
            if (isCrossSegment && draggingIndex != -1) {
                localTiles.mapIndexed { idx, tile ->
                    if (idx == draggingIndex) tile.copy(spanCols = 0, spanRows = 0) else tile
                }
            } else {
                localTiles
            }
        }

        val itemPositions = remember(activePositionsList, columns) {
            calculateGridPositions(activePositionsList, columns)
        }

        val totalRows = run {
            val max = itemPositions.maxOfOrNull { it.gridY + it.spanY } ?: 0
            if (isEditMode && max == 0) 1 else max
        }
        val totalHeight = (cellSize * totalRows) + (gridSpacing * (totalRows - 1).coerceAtLeast(0))

        val animatedGridHeight by animateDpAsState(
            targetValue    = totalHeight.coerceAtLeast(0.dp),
            animationSpec  = spring(ONEUI_SPRING_DAMPING, ONEUI_SPRING_STIFFNESS),
            label         = "grid_height"
        )

        fun performSwap(currentIndex: Int): Int {
            val now = System.currentTimeMillis()
            val rootOffset = layoutRootCoordinates?.positionInRoot() ?: Offset.Zero

            val draggedWidthPx = with(density) { FloatingTileDragState.draggedTileSize.width.toPx() }
            val draggedHeightPx = with(density) { FloatingTileDragState.draggedTileSize.height.toPx() }
            val centerOffsetPx = with(density) { gridCenterOffset.toPx() }
            val cellAndSpacing = with(density) { (cellSize + gridSpacing).toPx() }

            val localLeft = FloatingTileDragState.tileTopLeft.x - rootOffset.x - gridLocalPosition.x
            val localTop = FloatingTileDragState.tileTopLeft.y - rootOffset.y - gridLocalPosition.y

            if (now - lastSwapTime < 100L) return currentIndex

            val draggedCenter = Offset(localLeft + draggedWidthPx / 2f, localTop + draggedHeightPx / 2f)
            val draggedTile = localTiles[currentIndex]
            val listWithoutDragged = localTiles.toMutableList().apply { removeAt(currentIndex) }

            val hoverCol = ((draggedCenter.x - centerOffsetPx) / cellAndSpacing).coerceIn(0f, columns - 1f)
            val hoverRow = (draggedCenter.y / cellAndSpacing).coerceAtLeast(0f)

            val candidates = mutableListOf<List<FloatingTile>>()
            if (initialNonDragged.size == listWithoutDragged.size) {
                val initialSpecs = initialNonDragged.map { i -> i.spec.spec }.toSet()
                val currentSpecs = listWithoutDragged.map { i -> i.spec.spec }.toSet()
                if (initialSpecs == currentSpecs) {
                    for (insertIdx in 0..initialNonDragged.size) {
                        candidates.add(initialNonDragged.toMutableList().apply { add(insertIdx, draggedTile) })
                    }
                }
            }

            for (insertIdx in 0..listWithoutDragged.size) {
                candidates.add(listWithoutDragged.toMutableList().apply { add(insertIdx, draggedTile) })
            }

            var bestCandidate = localTiles
            var minScore = Float.MAX_VALUE

            for (candidate in candidates) {
                if (!canFitGridItems(candidate, columns)) continue
                val candidateIdx = candidate.indexOfFirst { i -> i.spec.spec == draggedTile.spec.spec }
                val positions = calculateGridPositions(candidate, columns)
                val p = positions.getOrNull(candidateIdx) ?: continue
                
                val tileCenterCol = p.gridX + (p.spanX / 2f)
                val tileCenterRow = p.gridY + (p.spanY / 2f)

                val dx = tileCenterCol - hoverCol
                val dy = tileCenterRow - hoverRow
                val score = dx * dx + dy * dy
                
                val rows = positions.maxOfOrNull { r -> r.gridY + r.spanY } ?: 0
                val adjustedScore = score + (rows * 0.01f)

                if (adjustedScore < minScore) {
                    minScore = adjustedScore
                    bestCandidate = candidate
                }
            }

            if (minScore < 3.0f && bestCandidate != localTiles) {
                localTiles = bestCandidate
                lastSwapTime = now
                return bestCandidate.indexOfFirst { i -> i.spec.spec == draggedTile.spec.spec }
            }
            return currentIndex
        }

        LaunchedEffect(isDropTarget, isSource, FloatingTileDragState.dragPosition) {
            when {
                isDropTarget && !isSource -> {
                    val ghost = FloatingTileDragState.ghostFloatingTile ?: return@LaunchedEffect
                    val ghostIdx = localTiles.indexOfFirst { i -> i.spec == ghost.spec }
                    if (ghostIdx != -1) {
                        performSwap(ghostIdx)
                        FloatingTileDragState.ghostTargetOrder = localTiles.map { i -> i.spec }
                        FloatingTileDragState.ghostTargetOwnerId =
                            if (isOneUi) FloatingTileDragState.ONE_UI_GHOST_OWNER_ID else gridOwnerId
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(animatedGridHeight)
                .onGloballyPositioned { coords ->
                    gridLocalPosition = layoutRootCoordinates?.localPositionOf(coords, Offset.Zero) ?: Offset.Zero
                }
        ) {
            if (isEditMode) {
                GridBackground(rows = totalRows, cols = columns, cellSize = cellSize, spacing = gridSpacing, offsetX = gridCenterOffset)
            }

            localTiles.forEachIndexed { index, tile ->
                key(tile.spec.spec) {
                    var isResizing by remember { mutableStateOf(false) }
                    var proposedCols by remember { mutableIntStateOf(tile.spanCols) }
                    var proposedRows by remember { mutableIntStateOf(tile.spanRows) }
                    var ghostMagX    by remember { mutableFloatStateOf(0f) }
                    var ghostMagY    by remember { mutableFloatStateOf(0f) }

                    val displayCols = if (isResizing) proposedCols else tile.spanCols
                    val displayRows = if (isResizing) proposedRows else tile.spanRows
                    val placement = itemPositions.getOrElse(index) { GridPlacement(0, 0, 1, 1) }

                    val xPos   = gridCenterOffset + (cellSize + gridSpacing) * placement.gridX
                    val yPos   = (cellSize + gridSpacing) * placement.gridY
                    val xPosPx = with(density) { xPos.toPx() }
                    val yPosPx = with(density) { yPos.toPx() }

                    val tileWidth  = (cellSize * displayCols) + (gridSpacing * (displayCols - 1))
                    val tileHeight = (cellSize * displayRows) + (gridSpacing * (displayRows - 1))

                    val animatedTileWidth by animateDpAsState(
                        targetValue   = if (isCrossSegment && index == draggingIndex) 0.dp else tileWidth,
                        animationSpec = spring(ONEUI_SPRING_DAMPING, ONEUI_SPRING_STIFFNESS),
                        label         = "tile_w_${tile.spec.spec}"
                    )

                    val animatedTileHeight by animateDpAsState(
                        targetValue   = if (isCrossSegment && index == draggingIndex) 0.dp else tileHeight,
                        animationSpec = spring(ONEUI_SPRING_DAMPING, ONEUI_SPRING_STIFFNESS),
                        label         = "tile_h_${tile.spec.spec}"
                    )

                    val animatedOffset by animateOffsetAsState(
                        targetValue   = Offset(xPosPx, yPosPx),
                        animationSpec = spring(ONEUI_SPRING_DAMPING, ONEUI_SPRING_STIFFNESS),
                        label         = "tile_offset_${tile.spec.spec}"
                    )

                    val isGhost = !isSource && FloatingTileDragState.isDragging &&
                            FloatingTileDragState.draggingTileSpec == tile.spec
                    val isBeingDragged = FloatingTileDragState.isDragging &&
                            FloatingTileDragState.draggingTileSpec == tile.spec

                    val tileAlpha = when {
                        isResizing -> 1f
                        isBeingDragged || isGhost -> 0f
                        else -> 1f
                    }

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(animatedOffset.x.roundToInt(), animatedOffset.y.roundToInt()) }
                            .size(animatedTileWidth, animatedTileHeight)
                            .alpha(tileAlpha)
                            .pointerInput(isEditMode, tile.spanCols, tile.spanRows) {
                                if (isEditMode) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { offset ->
                                            val freshIdx = localTiles.indexOfFirst { i -> i.spec == tile.spec }
                                            if (freshIdx == -1) return@detectDragGesturesAfterLongPress
                                            draggingIndex = freshIdx
                                            isSource      = true
                                            initialNonDragged = localTiles.filter { i -> i.spec != tile.spec }

                                            val rootPos = layoutRootCoordinates?.positionInRoot() ?: Offset.Zero
                                            FloatingTileDragState.startDrag(
                                                tile,
                                                rootPos + gridLocalPosition + animatedOffset,
                                                DpSize(tileWidth, tileHeight),
                                                sectionType,
                                                fingerOffset = offset
                                            )
                                            currentOnDragStart()
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            FloatingTileDragState.updateDrag(dragAmount)
                                            if (draggingIndex != -1) {
                                                draggingIndex = performSwap(draggingIndex)
                                            }
                                        },
                                        onDragEnd = {
                                            if (draggingIndex != -1) {
                                                crossSegmentDrop = currentIsDropExternal()
                                                if (!crossSegmentDrop) {
                                                    currentOnMove(localTiles)
                                                }
                                                currentOnDragEnd(tile.spec)
                                                if (crossSegmentDrop) {
                                                    localTiles = localTiles.filter { i -> i.spec != tile.spec }
                                                }
                                            }
                                            draggingIndex    = -1
                                            crossSegmentDrop = false
                                            isSource         = false
                                            FloatingTileDragState.endDrag()
                                        },
                                        onDragCancel = {
                                            localTiles    = tiles
                                            if (draggingIndex != -1) currentOnDragEnd(tile.spec)
                                            draggingIndex = -1
                                            isSource      = false
                                            FloatingTileDragState.endDrag()
                                        }
                                    )
                                }
                            }
                    ) {
                        DraggableTile(
                            tile           = tile,
                            qsTile         = allTilesMap[tile.spec],
                            onClick        = { expandable -> onTileClick(tile, expandable) },
                            gridCellSize   = cellSize,
                            gridSpacing    = gridSpacing,
                            isEditMode     = isEditMode,
                            isResizing     = isResizing,
                            ghostOffsetPx  = Offset(ghostMagX, ghostMagY),
                            elevation      = 0.dp,
                            onRemove     = {
                                localTiles = localTiles.filter { i -> i.spec.spec != tile.spec.spec }
                                onRemove(tile.spec.spec)
                            },
                            onResizeStart = {
                                isResizing   = true
                                ghostMagX    = 0f
                                ghostMagY    = 0f
                                proposedCols = tile.spanCols
                                proposedRows = tile.spanRows
                            },
                            onUpdateGhost = { currentAccumX, currentAccumY ->
                                val stepPx = with(density) { (cellSize + gridSpacing).toPx() }
                                val magnetRadius = stepPx * 0.45f

                                fun magnet(raw: Float): Float {
                                    val nearest = kotlin.math.round(raw / stepPx) * stepPx
                                    val dist = kotlin.math.abs(raw - nearest)
                                    val pull = if (dist < magnetRadius) {
                                        1f - (dist / magnetRadius)
                                    } else {
                                        0f
                                    }
                                    return raw + (nearest - raw) * pull * 0.6f
                                }
                                ghostMagX = magnet(currentAccumX)
                                ghostMagY = magnet(currentAccumY)
                            },
                            onPreviewResize = { newCols, newRows ->
                                val resizeControl = tile.spec.asQSControl()
                                if (resizeControl != null && !resizeControl.isSlider) {
                                    val coerced = resizeControl.coerceSpan(
                                        QSControlSpan(newCols.coerceIn(1, 4), newRows.coerceIn(1, 4)),
                                        columns = 4,
                                    )
                                    proposedCols = coerced.columns
                                    proposedRows = coerced.rows
                                } else if (tile.spec.spec == "brightness" || resizeControl?.isSlider == true) {
                                    val isHorizontalPref = if (newCols > newRows) true
                                                           else if (newRows > newCols) false
                                                           else tile.spanCols > tile.spanRows
                                    proposedCols = if (isHorizontalPref) newCols.coerceIn(3, 4) else 1
                                    proposedRows = if (isHorizontalPref) 1 else newRows.coerceIn(3, 4)
                                } else {
                                    proposedCols = newCols
                                    proposedRows = newRows
                                }

                                if (proposedCols != tile.spanCols || proposedRows != tile.spanRows) {
                                    localTiles = localTiles.map { i ->
                                        if (i.spec.spec == tile.spec.spec)
                                            i.copy(spanCols = proposedCols, spanRows = proposedRows)
                                        else i
                                    }
                                    onResize(tile.spec.spec, proposedCols, proposedRows)
                                }
                            },
                            onResizeEnd = {
                                isResizing   = false
                                ghostMagX    = 0f
                                ghostMagY    = 0f
                            },
                            tileBrightness = tileBrightness,
                            editTile       = if (isGhost) FloatingTileDragState.ghostEditTile else null,
                            useAospStyle   = useAospStyle,
                            isOneUi        = isOneUi,
                            interactionsEnabled = interactionsEnabled,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DraggableTile(
    tile: FloatingTile,
    qsTile: QSTile?,
    onClick: (Expandable) -> Unit,
    gridCellSize: Dp,
    gridSpacing: Dp = 12.dp,
    isEditMode: Boolean,
    isResizing: Boolean = false,
    ghostOffsetPx: Offset = Offset.Zero,
    elevation: Dp,
    onRemove: () -> Unit,
    onResizeStart: () -> Unit = {},
    onUpdateGhost: (Float, Float) -> Unit = { _, _ -> },
    onPreviewResize: (Int, Int) -> Unit = {_,_ ->},
    onResizeEnd: () -> Unit = {},
    tileBrightness: @Composable (isVertical: Boolean) -> Unit,
    editTile: EditTileViewModel? = null,
    useAospStyle: Boolean = false,
    isOneUi: Boolean = false,
    isDragOverlay: Boolean = false,
    interactionsEnabled: Boolean = true,
) {
    val isBrightness = tile.spec.spec == "brightness"
    val controlRenderer = LocalQSControlRenderer.current
    val resolvedControl = remember(tile.spec) { tile.spec.asQSControl() }
    val qsControl = resolvedControl.takeIf { controlRenderer != null }
    val fallbackEditTile = editTile ?: FloatingTileDragState.fallbackTiles[tile.spec]
    val context = LocalContext.current

    val fallbackCustomTileInfo = remember(tile.spec.spec, context) {
        if (tile.spec.spec.startsWith("custom(")) {
            try {
                val componentStr = tile.spec.spec.removePrefix("custom(").removeSuffix(")")
                val componentName = android.content.ComponentName.unflattenFromString(componentStr)
                if (componentName != null) {
                    val pm = context.packageManager
                    val serviceInfo = pm.getServiceInfo(componentName, 0)
                    val icon = serviceInfo.loadIcon(pm)
                    val label = serviceInfo.loadLabel(pm).toString()
                    Pair(icon, label)
                } else null
            } catch (e: Exception) {
                null
            }
        } else null
    }
    val fallbackCustomIcon = fallbackCustomTileInfo?.first
    val fallbackCustomLabel = fallbackCustomTileInfo?.second

    val initialState = remember(qsTile) { qsTile?.state?.copy() }
    val state by produceState<QSTile.State?>(initialValue = initialState, key1 = qsTile) {
        if (qsTile == null || isBrightness) { value = null; return@produceState }
        val callback = object : QSTile.Callback {
            override fun onStateChanged(s: QSTile.State?) {
                value = s?.copy()
                if (s?.icon != null) {
                    FloatingTileDragState.fallbackTiles.remove(tile.spec)
                }
            }
        }
        val listenerKey = Any()
        qsTile.addCallback(callback)
        qsTile.setListening(listenerKey, true)
        qsTile.refreshState()

        if (qsTile.state?.icon != null) {
            FloatingTileDragState.fallbackTiles.remove(tile.spec)
        }
        awaitDispose { qsTile.setListening(listenerKey, false); qsTile.removeCallback(callback) }
    }

    val label = state?.label ?: fallbackEditTile?.label?.text ?: fallbackCustomLabel ?: tile.spec.spec
    val summary = state?.secondaryLabel?.toString()?.trim()?.takeIf { it.isNotEmpty() }
    val isActive = state?.state == Tile.STATE_ACTIVE
    val isSquare = tile.spanCols == tile.spanRows
    val isPill   = !isSquare
    val isDark   = isSystemInDarkTheme()

    val tileBackgroundColor  = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
    val iconContainerColor   = when {
        isActive -> MaterialTheme.colorScheme.primary
        state?.state == Tile.STATE_INACTIVE -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f)
        else -> MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f)
    }
    val contentColor = when {
        isActive || state?.state == Tile.STATE_INACTIVE -> if (isDark) Color.White else Color.Black
        else -> if (isDark) Color.White.copy(alpha = 0.38f) else Color.Black.copy(alpha = 0.38f)
    }
    val iconColor = when {
        isActive -> MaterialTheme.colorScheme.onPrimary
        state?.state == Tile.STATE_INACTIVE -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    val expandableShape = if (useAospStyle) {
        RoundedCornerShape(InactiveCornerRadius)
    } else {
        RoundedCornerShape(percent = 50) 
    }
    val expandableColor = if (useAospStyle) {
        if (isActive) MaterialTheme.colorScheme.primary else EditModeTileDefaults.editTileColors().background
    } else {
        tileBackgroundColor
    }
    val expandableController = rememberExpandableController(
        color = { expandableColor },
        shape = expandableShape,
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val liveW = maxWidth
        val liveH = maxHeight
        val density = LocalDensity.current

        val targetWPx = if (gridCellSize > 0.dp) {
            with(density) { ((gridCellSize * tile.spanCols) + (gridSpacing * (tile.spanCols - 1))).toPx() }
        } else {
            with(density) { liveW.toPx() }
        }
        val targetHPx = if (gridCellSize > 0.dp) {
            with(density) { ((gridCellSize * tile.spanRows) + (gridSpacing * (tile.spanRows - 1))).toPx() }
        } else {
            with(density) { liveH.toPx() }
        }

        if (isResizing && !isBrightness && qsControl == null) {
            val outlineColor = MaterialTheme.colorScheme.primary
            val tilePxW = with(density) { liveW.toPx() }
            val tilePxH = with(density) { liveH.toPx() }
            val cornerPx = minOf(targetWPx, targetHPx) * 0.5f
            val outlinePath = remember { Path() }

            Canvas(modifier = Modifier.fillMaxSize().zIndex(10f)) {
                val outLeft   = 0f
                val outTop    = 0f
                val minWidthPx = if (gridCellSize > 0.dp) gridCellSize.toPx() else tilePxW * 0.25f
                val minHeightPx = if (gridCellSize > 0.dp) gridCellSize.toPx() else tilePxH * 0.25f

                val outRight  = (targetWPx + ghostOffsetPx.x).coerceAtLeast(minWidthPx)
                val outBottom = (targetHPx + ghostOffsetPx.y).coerceAtLeast(minHeightPx)

                outlinePath.reset()
                outlinePath.addRoundRect(
                    RoundRect(
                        left         = outLeft,
                        top          = outTop,
                        right        = outRight,
                        bottom       = outBottom,
                        cornerRadius = CornerRadius(cornerPx, cornerPx)
                    )
                )
                drawPath(
                    path  = outlinePath,
                    color = outlineColor,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawPath(
                    path  = outlinePath,
                    color = outlineColor.copy(alpha = 0.08f)
                )
            }
        }

        val showBorder = (!isResizing || isBrightness || qsControl != null) && isEditMode && !isOneUi

        if (qsControl != null && controlRenderer != null) {
            val controlSpan = QSControlSpan(
                tile.spanCols.coerceIn(QSControlSpan.MIN_COLUMNS, QSControlSpan.MAX_COLUMNS),
                tile.spanRows.coerceIn(QSControlSpan.MIN_ROWS, QSControlSpan.MAX_ROWS),
            )
            val controlShape = RoundedCornerShape(
                controlRenderer.cornerRadius(qsControl, DpSize(liveW, liveH))
            )
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (showBorder) Modifier.border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            shape = controlShape,
                        ) else Modifier
                    ),
                shape = controlShape,
                color = Color.Transparent,
                shadowElevation = 0.dp,
            ) {
                controlRenderer.content(qsControl, controlSpan, !isEditMode && interactionsEnabled)
            }
        } else if (useAospStyle) {
            val aospColors = EditModeTileDefaults.editTileColors()
            val aospBackground = if (isActive) MaterialTheme.colorScheme.primary else aospColors.background
            val aospIconColor = if (isActive) MaterialTheme.colorScheme.onPrimary else aospColors.icon
            val aospLabelColor = if (isActive) MaterialTheme.colorScheme.onPrimary else aospColors.label

            Expandable(
                controller = expandableController,
                modifier = Modifier.fillMaxSize(),
                useModifierBasedImplementation = false,
            ) { expandable ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (showBorder) Modifier.border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(InactiveCornerRadius),
                        ) else Modifier
                    ),
                shape = RoundedCornerShape(InactiveCornerRadius),
                color = aospBackground,
                shadowElevation = if (isResizing) 0.dp else elevation,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .combinedClickable(
                            enabled = !isEditMode && interactionsEnabled,
                            onClick = { onClick(expandable) },
                            onLongClick = if (!isEditMode && interactionsEnabled && qsTile != null) {{ qsTile.longClick(expandable) }} else null,
                        )
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    val iconSize = 28.dp
                    val currentTileState = state
                    if (currentTileState?.icon != null) {
                        val iconDrawable = remember(currentTileState.icon, currentTileState.state) {
                            currentTileState.icon!!.getDrawable(context)
                        }
                        if (iconDrawable != null) {
                            androidx.compose.foundation.Image(
                                painter = rememberDrawablePainter(iconDrawable),
                                contentDescription = null,
                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(aospIconColor),
                                modifier = Modifier.size(iconSize)
                            )
                        } else {
                            QSTileIconRenderer(currentTileState.icon!!, aospIconColor, iconSize)
                        }
                    } else if (fallbackEditTile != null) {
                        val iconDrawable = remember(fallbackEditTile.icon) {
                            when (val i = fallbackEditTile.icon) {
                                is com.android.systemui.common.shared.model.Icon.Loaded -> i.drawable
                                is com.android.systemui.common.shared.model.Icon.Resource -> context.getDrawable((i as com.android.systemui.common.shared.model.Icon.Resource).resId)
                                else -> null
                            }
                        }
                        if (iconDrawable != null) {
                            androidx.compose.foundation.Image(
                                painter = rememberDrawablePainter(iconDrawable),
                                contentDescription = null,
                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(aospIconColor),
                                modifier = Modifier.size(iconSize)
                            )
                        }
                    } else if (fallbackCustomIcon != null) {
                        androidx.compose.foundation.Image(
                            painter = rememberDrawablePainter(fallbackCustomIcon),
                            contentDescription = null,
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(aospIconColor),
                            modifier = Modifier.size(iconSize)
                        )
                    }
                    if (!isOneUi) {
                        Spacer(Modifier.height(4.dp))
                        @OptIn(ExperimentalFoundationApi::class)
                        Text(
                            label.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = aospLabelColor,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee(),
                        )
                    }
                }
            }
            }
        } else {
            val borderModifier = if (showBorder) {
                Modifier.border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(percent = 50)
                )
            } else Modifier

            Expandable(
                controller = expandableController,
                modifier = Modifier.fillMaxSize(),
                useModifierBasedImplementation = false,
            ) { expandable ->
            Surface(
                modifier        = Modifier
                    .fillMaxSize()
                    .then(borderModifier),
                shape           = RoundedCornerShape(percent = 50),
                color           = Color.Transparent,
                shadowElevation = if (isResizing) 0.dp else elevation,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isBrightness) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(percent = 50))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
                        ) {
                            tileBrightness(liveH > liveW)
                        }
                    } else {
                        BoxWithConstraints(
                            modifier         = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val tileWidth    = maxWidth
                            val tileHeight   = maxHeight
                            val minDimension = minOf(tileWidth, tileHeight)
                            val iconContainerSize = minDimension * 0.72f
                            val iconSize          = 28.dp

                            Box(modifier = Modifier.fillMaxSize()
                                .background(tileBackgroundColor, RoundedCornerShape(percent = 50))
                                .blur(16.dp))

                            Row(
                                modifier = Modifier.fillMaxSize()
                                    .clip(RoundedCornerShape(percent = 50))
                                    .combinedClickable(
                                        enabled     = !isEditMode && interactionsEnabled,
                                        onClick     = { onClick(expandable) },
                                        onLongClick = if (!isEditMode && interactionsEnabled && qsTile != null) {{ qsTile.longClick(expandable) }} else null
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = if (tile.spanCols == 2 && tile.spanRows == 3) Arrangement.Center else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier.size(minDimension),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(modifier = Modifier.size(iconContainerSize), contentAlignment = Alignment.Center) {
                                        Box(modifier = Modifier.fillMaxSize()
                                            .background(iconContainerColor, CircleShape)
                                            .then(if (!isActive) Modifier.blur(4.dp) else Modifier))

                                        val currentTileState = state
                                        if (currentTileState?.icon != null) {
                                            val iconDrawable = remember(currentTileState.icon, currentTileState.state) {
                                                currentTileState.icon!!.getDrawable(context)
                                            }
                                            if (iconDrawable != null) {
                                                androidx.compose.foundation.Image(
                                                    painter = rememberDrawablePainter(iconDrawable),
                                                    contentDescription = null,
                                                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(iconColor),
                                                    modifier = Modifier.size(iconSize)
                                                )
                                            } else {
                                                QSTileIconRenderer(currentTileState.icon!!, iconColor, iconSize)
                                            }
                                        } else if (fallbackEditTile != null) {
                                            val iconDrawable = remember(fallbackEditTile.icon) {
                                                when (val i = fallbackEditTile.icon) {
                                                    is com.android.systemui.common.shared.model.Icon.Loaded -> i.drawable
                                                    is com.android.systemui.common.shared.model.Icon.Resource -> context.getDrawable((i as com.android.systemui.common.shared.model.Icon.Resource).resId)
                                                    else -> null
                                                }
                                            }
                                            if (iconDrawable != null) {
                                                androidx.compose.foundation.Image(
                                                    painter = rememberDrawablePainter(iconDrawable),
                                                    contentDescription = null,
                                                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(iconColor),
                                                    modifier = Modifier.size(iconSize)
                                                )
                                            }
                                        } else if (fallbackCustomIcon != null) {
                                            androidx.compose.foundation.Image(
                                                painter = rememberDrawablePainter(fallbackCustomIcon),
                                                contentDescription = null,
                                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(iconColor),
                                                modifier = Modifier.size(iconSize)
                                            )
                                        }
                                    }
                                }

                                AnimatedVisibility(
                                    visible = !isOneUi && isPill && !(tile.spanCols == 2 && tile.spanRows == 3),
                                    modifier = Modifier.weight(1f),
                                    enter = fadeIn(tween(150)),
                                    exit = fadeOut(tween(150))
                                ) {
                                    @OptIn(ExperimentalFoundationApi::class)
                                    Column(
                                        modifier = Modifier.padding(end = 16.dp),
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        Text(label.toString(), style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold, color = contentColor, maxLines = 1,
                                            modifier = Modifier.basicMarquee())
                                        if (summary != null) {
                                            Text(summary, style = MaterialTheme.typography.bodySmall,
                                                color = contentColor.copy(alpha = contentColor.alpha * 0.7f),
                                                maxLines = 1,
                                                modifier = Modifier.basicMarquee())
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
        }

        if (isEditMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { }
                    .zIndex(30f)
            ) {
                val cornerRadius =
                    if (qsControl != null && controlRenderer != null) {
                        controlRenderer.cornerRadius(qsControl, DpSize(liveW, liveH))
                    } else {
                        minOf(liveW, liveH) / 2f
                    }
                val cornerRadiusPx = with(density) { cornerRadius.toPx() }
                val liveWPx = with(density) { liveW.toPx() }
                val liveHPx = with(density) { liveH.toPx() }

                val removeButtonSize = 22.dp
                val removeButtonSizePx = with(density) { removeButtonSize.toPx() }
                val leftArcCenterX = cornerRadiusPx
                val leftArcCenterY = cornerRadiusPx
                val removeAngleRad = 3.6651914f
                val tlEdgeX = leftArcCenterX + cornerRadiusPx * kotlin.math.cos(removeAngleRad)
                val tlEdgeY = leftArcCenterY + cornerRadiusPx * kotlin.math.sin(removeAngleRad)
                val xOffset = if (isOneUi) with(density) { 5.dp.toPx() } else 0f

                Surface(
                    onClick         = onRemove,
                    modifier        = Modifier
                        .offset {
                            IntOffset(
                                (tlEdgeX - removeButtonSizePx / 2f + xOffset).roundToInt(),
                                (tlEdgeY - removeButtonSizePx / 2f).roundToInt()
                            )
                        }
                        .size(removeButtonSize),
                    shape           = CircleShape,
                    color           = MaterialTheme.colorScheme.error,
                    shadowElevation = 4.dp,
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Close,
                        contentDescription = "Remove",
                        modifier           = Modifier.padding(4.dp),
                        tint               = MaterialTheme.colorScheme.onError,
                    )
                }

                if (!isOneUi) {
                    val cos45 = 0.70710678f
                    val brArcCenterX = liveWPx - cornerRadiusPx
                    val brArcCenterY = liveHPx - cornerRadiusPx
                    val brEdgeX = brArcCenterX + cornerRadiusPx * cos45
                    val brEdgeY = brArcCenterY + cornerRadiusPx * cos45

                    val touchSize = 40.dp
                    val touchSizePx = with(density) { touchSize.toPx() }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeW = 3.dp.toPx()
                        val arcRadius = cornerRadiusPx
                        val arcLeft = brArcCenterX - arcRadius
                        val arcTop = brArcCenterY - arcRadius
                        val startAngle = 30f
                        val sweepAngle = 30f

                        drawArc(
                            color = Color.Black.copy(alpha = 0.22f),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(arcLeft, arcTop + 1.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2),
                            style = Stroke(
                                width = strokeW + 0.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )

                        drawArc(
                            color = Color.White,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(arcLeft, arcTop),
                            size = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2),
                            style = Stroke(
                                width = strokeW,
                                cap = StrokeCap.Round
                            )
                        )
                    }

                    ResizeHandle(
                        currentCols  = tile.spanCols,
                        currentRows  = tile.spanRows,
                        gridCellSize = gridCellSize,
                        gridSpacing  = gridSpacing,
                        onResizeStart = onResizeStart,
                        onUpdateGhost = onUpdateGhost,
                        onPreviewResize = onPreviewResize,
                        onResizeEnd   = onResizeEnd,
                        isBrightness = isBrightness || qsControl?.isSlider == true,
                        modifier     = Modifier
                            .offset {
                                IntOffset(
                                    (brEdgeX - touchSizePx / 2f).roundToInt(),
                                    (brEdgeY - touchSizePx / 2f).roundToInt()
                                )
                            }
                            .size(touchSize)
                    )
                }
            }
        }
    }
}

@Composable
private fun ResizeHandle(
    currentCols: Int,
    currentRows: Int,
    gridCellSize: Dp,
    gridSpacing: Dp,
    onResizeStart: () -> Unit,
    onUpdateGhost: (Float, Float) -> Unit = { _, _ -> },
    onPreviewResize: (Int, Int) -> Unit,
    onResizeEnd: () -> Unit,
    isBrightness: Boolean = false,
    modifier: Modifier
) {
    val density              = LocalDensity.current
    val stepPx               = with(density) { (gridCellSize + gridSpacing).toPx() }
    val currentColsState     by rememberUpdatedState(currentCols)
    val currentRowsState     by rememberUpdatedState(currentRows)
    val onResizeStartState   by rememberUpdatedState(onResizeStart)
    val onUpdateGhostState   by rememberUpdatedState(onUpdateGhost)
    val onPreviewResizeState by rememberUpdatedState(onPreviewResize)
    val onResizeEndState     by rememberUpdatedState(onResizeEnd)

    var accumulatedDragX by remember { mutableFloatStateOf(0f) }
    var accumulatedDragY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                while (true) {
                    awaitPointerEventScope {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val pointerId = down.id
                        val downTime = System.currentTimeMillis()
                        var dragStarted = false

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId }

                            if (change == null || change.changedToUp()) {
                                if (dragStarted) {
                                    change?.consume()
                                    onResizeEndState()
                                }
                                break
                            }

                            val elapsed = System.currentTimeMillis() - downTime
                            if (!dragStarted && elapsed >= 150L) {
                                accumulatedDragX = 0f
                                accumulatedDragY = 0f
                                onResizeStartState()
                                dragStarted = true
                            }

                            if (dragStarted) {
                                val dragAmount = change.positionChange()
                                change.consume()
                                accumulatedDragX += dragAmount.x
                                accumulatedDragY += dragAmount.y

                                if (!isBrightness) {
                                    val threshold = stepPx * 0.75f
                                    val colChange = (accumulatedDragX / threshold).toInt()
                                    val rowChange = (accumulatedDragY / threshold).toInt()
                                    val newCols = (currentColsState + colChange).coerceIn(1, 4)
                                    val newRows = (currentRowsState + rowChange).coerceIn(1, 4)
                                    val colDelta = newCols - currentColsState
                                    val rowDelta = newRows - currentRowsState
                                    if (colDelta != 0 || rowDelta != 0) {
                                        onPreviewResizeState(newCols, newRows)
                                        accumulatedDragX -= colDelta * stepPx
                                        accumulatedDragY -= rowDelta * stepPx
                                    }
                                } else {
                                    val swapThreshold = stepPx * 0.5f
                                    val isHorizontal = currentColsState > currentRowsState
                                    if (isHorizontal) {
                                        if (accumulatedDragY > swapThreshold) {
                                            onPreviewResizeState(1, 4)
                                            accumulatedDragX = 0f
                                            accumulatedDragY = 0f
                                        } else {
                                            val colChange = (accumulatedDragX / swapThreshold).toInt()
                                            val newCols = (currentColsState + colChange).coerceIn(3, 4)
                                            onPreviewResizeState(newCols, 1)
                                        }
                                    } else {
                                        if (accumulatedDragX > swapThreshold) {
                                            onPreviewResizeState(4, 1)
                                            accumulatedDragX = 0f
                                            accumulatedDragY = 0f
                                        } else {
                                            val rowChange = (accumulatedDragY / swapThreshold).toInt()
                                            val newRows = (currentRowsState + rowChange).coerceIn(3, 4)
                                            onPreviewResizeState(1, newRows)
                                        }
                                    }
                                }
                                onUpdateGhostState(accumulatedDragX, accumulatedDragY)
                            }
                        }
                    }
                }
            }
    )
}

@Composable
private fun CollapsedRowsThumb(
    value: Int,
    onChange: (Int) -> Unit,
) {
    val density   = LocalDensity.current
    val stepPx    = with(density) { 56.dp.toPx() }
    var accum     by remember { mutableFloatStateOf(0f) }
    var liveValue by remember { mutableIntStateOf(value) }
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(value) { liveValue = value }

    val handleWidth by animateDpAsState(
        targetValue = if (isPressed) 64.dp else 60.dp,
        animationSpec = spring(ONEUI_SPRING_DAMPING, ONEUI_SPRING_STIFFNESS),
        label = "expand_handle_w",
    )
    val handleHeight by animateDpAsState(
        targetValue = if (isPressed) 9.dp else 8.dp,
        animationSpec = spring(ONEUI_SPRING_DAMPING, ONEUI_SPRING_STIFFNESS),
        label = "expand_handle_h",
    )
    val handleAlpha by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0.95f,
        animationSpec = tween(durationMillis = 120),
        label = "expand_handle_alpha",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart  = { accum = 0f; isPressed = true },
                    onDragEnd    = { accum = 0f; isPressed = false },
                    onDragCancel = { accum = 0f; isPressed = false },
                    onDrag = { change, drag ->
                        change.consume()
                        accum += drag.y
                        val delta = (accum / stepPx).roundToInt()
                        if (delta != 0) {
                            val next = (liveValue + delta).coerceIn(1, 3)
                            if (next != liveValue) {
                                accum    -= delta * stepPx
                                liveValue = next
                                onChange(next)
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 6.dp)
                .width(handleWidth)
                .height(handleHeight)
                .background(
                    Color.White.copy(alpha = handleAlpha),
                    RoundedCornerShape(50)
                )
        )
    }
}

@Composable
private fun GridBackground(
    rows: Int,
    cols: Int,
    cellSize: Dp,
    spacing: Dp,
    offsetX: Dp = 0.dp,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = 2.dp.toPx()
        val color  = Color.Gray.copy(alpha = 0.3f)
        val offsetXPx = offsetX.toPx()
        for (r in 0 until rows) for (c in 0 until cols) {
            drawCircle(color, radius, Offset(
                offsetXPx + (cellSize.toPx() + spacing.toPx()) * c + cellSize.toPx() / 2,
                (cellSize.toPx() + spacing.toPx()) * r + cellSize.toPx() / 2,
            ))
        }
    }
}

private fun calculateGridPositions(tiles: List<FloatingTile>, columns: Int): List<GridPlacement> {
    val placements = mutableListOf<GridPlacement>()
    val occupied   = mutableSetOf<Pair<Int, Int>>()
    var maxRow = 0
    tiles.forEach { tile ->
        var placed    = false
        var searchRow = maxRow
        while (!placed) {
            for (col in 0 until columns) {
                if (col + tile.spanCols > columns) continue
                var fits = true
                for (r in 0 until tile.spanRows) {
                    for (c in 0 until tile.spanCols) {
                        if ((searchRow + r) to (col + c) in occupied) { fits = false; break }
                    }
                    if (!fits) break
                }
                if (fits) {
                    placements.add(GridPlacement(col, searchRow, tile.spanCols, tile.spanRows))
                    for (r in 0 until tile.spanRows) for (c in 0 until tile.spanCols)
                        occupied.add((searchRow + r) to (col + c))
                    placed = true
                    if (searchRow > maxRow) maxRow = searchRow
                    break
                }
            }
            if (!placed) searchRow++
        }
    }
    return placements
}

data class GridPlacement(val gridX: Int, val gridY: Int, val spanX: Int, val spanY: Int)

@Composable
fun EditableQuickQuickSettingsLayout(
    sectionConfigs: List<SectionConfig>,
    sectionEditModeViewModel: SectionEditModeViewModel,
    nonBrightnessAlpha: Float = 1f,
    brightness: @Composable () -> Unit,
    tiles: @Composable () -> Unit,
    media: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        sectionConfigs.sortedBy { it.position }.forEach { section ->
            if (section.visible) {
                Box(
                    modifier = Modifier.graphicsLayer {
                        if (section.type != SectionType.BRIGHTNESS) {
                            alpha = nonBrightnessAlpha
                        }
                    }
                ) {
                    when (section.type) {
                        SectionType.BRIGHTNESS -> brightness()
                        SectionType.TILES      -> tiles()
                        SectionType.MEDIA      -> media()
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

class DrawablePainter(val drawable: android.graphics.drawable.Drawable) : Painter() {
    override val intrinsicSize: androidx.compose.ui.geometry.Size
        get() = if (drawable.intrinsicWidth >= 0 && drawable.intrinsicHeight >= 0) {
            androidx.compose.ui.geometry.Size(
                drawable.intrinsicWidth.toFloat(),
                drawable.intrinsicHeight.toFloat()
            )
        } else {
            androidx.compose.ui.geometry.Size.Unspecified
        }

    override fun androidx.compose.ui.graphics.drawscope.DrawScope.onDraw() {
        drawIntoCanvas { canvas ->
            drawable.setBounds(0, 0, size.width.toInt(), size.height.toInt())
            drawable.draw(canvas.nativeCanvas)
        }
    }
}

@Composable
fun rememberDrawablePainter(drawable: android.graphics.drawable.Drawable?): Painter {
    return remember(drawable) {
        if (drawable != null) {
            DrawablePainter(drawable)
        } else {
            ColorPainter(Color.Transparent)
        }
    }
}
