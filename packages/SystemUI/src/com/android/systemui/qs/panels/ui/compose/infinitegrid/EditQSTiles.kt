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
package com.android.systemui.qs.panels.ui.compose.infinitegrid

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.compose.theme.LocalAndroidColorScheme
import com.android.compose.ui.graphics.painter.rememberDrawablePainter
import com.android.systemui.common.shared.model.Icon
import com.android.systemui.common.ui.compose.load
import com.android.systemui.qs.panels.shared.model.QSControl
import com.android.systemui.qs.panels.shared.model.QSControlSpan
import com.android.systemui.qs.panels.ui.compose.FloatingTileDragState
import com.android.systemui.qs.panels.ui.compose.infinitegrid.CommonTileDefaults.InactiveCornerRadius
import com.android.systemui.qs.panels.ui.compose.infinitegrid.CommonTileDefaults.TileArrangementPadding
import com.android.systemui.qs.panels.ui.viewmodel.EditTileViewModel
import com.android.systemui.qs.panels.ui.viewmodel.EditTileViewModelConstants.APP_ICON_INLINE_CONTENT_ID
import com.android.systemui.qs.pipeline.shared.TileSpec
import com.android.systemui.qs.shared.model.TileCategory
import com.android.systemui.qs.shared.model.groupAndSort
import com.android.systemui.res.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenTilePicker(
    allTiles: List<EditTileViewModel>,
    insideSpecs: List<TileSpec>,
    onClose: () -> Unit,
    onTileClick: (EditTileViewModel) -> Unit,
    controlPreview: @Composable (QSControl, QSControlSpan) -> Unit,
    onTileDragStart: (EditTileViewModel, Offset) -> Unit = { _, _ -> },
    onTileDrag: (Offset) -> Unit = {},
    onTileDragEnd: () -> Unit = {},
    controls: List<QSControl> = QSControl.entries,
    isControlAdded: (QSControl) -> Boolean = { false },
    onControlDragStart: (QSControl, QSControlSpan, Offset, DpSize) -> Unit = { _, _, _, _ -> },
    columns: Int = 4,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val surfaceEffect2 = LocalAndroidColorScheme.current.surfaceEffect2
    val showHoldSnackbar: () -> Unit = {
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar("Hold on a tile to add it")
        }
    }

    var dragHasStarted by remember { mutableStateOf(false) }
    val isExternalDrag = FloatingTileDragState.isExternalDrag

    LaunchedEffect(isExternalDrag) {
        if (isExternalDrag) {
            dragHasStarted = true
        } else {
            kotlinx.coroutines.delay(50)
            dragHasStarted = false
        }
    }

    val pickerAlpha by animateFloatAsState(
        targetValue = if (dragHasStarted) 0f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "pickerAlpha"
    )

    Scaffold(
        modifier = modifier.graphicsLayer { alpha = pickerAlpha },
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .navigationBarsPadding(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Snackbar(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .wrapContentWidth(),
                        shape = RoundedCornerShape(28.dp),
                        containerColor = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    ) {
                        Text(
                            text = data.visuals.message,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                ),
                title = {
                    Text(
                        text = "Add Actions",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 24.dp),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.drawBehind { drawCircle(surfaceEffect2) },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            tint = Color.White,
                            contentDescription = "Back",
                        )
                    }
                },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 48.dp, bottom = 8.dp, start = 18.dp),
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = modifier.fillMaxSize()
            ) {
                Text(
                    text = "Hold a tile to add it to Quick Settings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                val groupedTileSpecs = remember(allTiles) {
                    groupAndSort(allTiles).mapValues { tiles -> tiles.value }
                }

                val scrollY = scrollState.value
                val maxScroll = scrollState.maxValue

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                        .drawWithContent {
                            drawContent()

                            val fadeH = 32.dp.toPx()

                            if (scrollY > 0) {
                                val progress = (scrollY.toFloat() / fadeH).coerceIn(0f, 1f)
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black),
                                        startY = 0f,
                                        endY = fadeH * progress,
                                    ),
                                    size = size.copy(height = fadeH),
                                    blendMode = BlendMode.DstIn,
                                )
                            }

                            if (scrollY < maxScroll) {
                                val distToBottom = (maxScroll - scrollY).toFloat()
                                val progress = (distToBottom / fadeH).coerceIn(0f, 1f)
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.Black, Color.Transparent),
                                        startY = size.height - fadeH * progress,
                                        endY = size.height,
                                    ),
                                    topLeft = Offset(0f, size.height - fadeH),
                                    size = size.copy(height = fadeH),
                                    blendMode = BlendMode.DstIn,
                                )
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(
                                state = scrollState,
                                enabled = true,
                                flingBehavior = androidx.compose.foundation.gestures.ScrollableDefaults.flingBehavior()
                            )
                            .padding(horizontal = 16.dp),
                        verticalArrangement = spacedBy(8.dp)
                    ) {
                        if (controls.isNotEmpty()) {
                            ControlsSection(
                                controls = controls,
                                columns = columns,
                                isLast = groupedTileSpecs.isEmpty(),
                                controlPreview = controlPreview,
                                isControlAdded = isControlAdded,
                                onControlDragStart = onControlDragStart,
                                onControlDrag = onTileDrag,
                                onControlDragEnd = onTileDragEnd,
                                onShowSnackbar = showHoldSnackbar,
                            )
                        }

                        groupedTileSpecs.entries.forEachIndexed { index, (category, tiles) ->
                            CategorySection(
                                category = category,
                                tiles = tiles,
                                insideSpecs = insideSpecs,
                                columns = columns,
                                onTileClick = onTileClick,
                                onTileDragStart = onTileDragStart,
                                onTileDrag = onTileDrag,
                                onTileDragEnd = onTileDragEnd,
                                isFirst = index == 0 && controls.isEmpty(),
                                isLast = index == groupedTileSpecs.size - 1,
                                onShowSnackbar = showHoldSnackbar
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp).navigationBarsPadding())
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySection(
    category: TileCategory,
    tiles: List<EditTileViewModel>,
    insideSpecs: List<TileSpec>,
    columns: Int,
    onTileClick: (EditTileViewModel) -> Unit,
    onTileDragStart: (EditTileViewModel, Offset) -> Unit,
    onTileDrag: (Offset) -> Unit,
    onTileDragEnd: () -> Unit,
    isFirst: Boolean,
    isLast: Boolean,
    onShowSnackbar: () -> Unit,
) {
    Column(
        modifier = Modifier.sectionBackground(isFirst, isLast),
        verticalArrangement = spacedBy(16.dp)
    ) {
        CategoryHeader(category)

        val rows = (tiles.size + columns - 1) / columns

        Column(verticalArrangement = spacedBy(TileArrangementPadding)) {
            for (rowIndex in 0 until rows) {
                val startIdx = rowIndex * columns
                val endIdx = minOf(startIdx + columns, tiles.size)
                val rowTiles = tiles.subList(startIdx, endIdx)

                Row(
                    horizontalArrangement = spacedBy(TileArrangementPadding),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max)
                ) {
                    for (tile in rowTiles) {
                        TilePickerCell(
                            tile = tile,
                            insideSpecs = insideSpecs,
                            onTileClick = onTileClick,
                            onTileDragStart = onTileDragStart,
                            onTileDrag = onTileDrag,
                            onTileDragEnd = onTileDragEnd,
                            onShowSnackbar = onShowSnackbar,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }

                    repeat(columns - rowTiles.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun sectionShape(isFirst: Boolean, isLast: Boolean) = when {
    isFirst && isLast -> RoundedCornerShape(TilePickerDefaults.GridBackgroundCornerRadius)
    isFirst -> RoundedCornerShape(
        topStart = TilePickerDefaults.GridBackgroundCornerRadius,
        topEnd = TilePickerDefaults.GridBackgroundCornerRadius
    )
    isLast -> RoundedCornerShape(
        bottomStart = TilePickerDefaults.GridBackgroundCornerRadius,
        bottomEnd = TilePickerDefaults.GridBackgroundCornerRadius
    )
    else -> RectangleShape
}

@Composable
private fun Modifier.sectionBackground(isFirst: Boolean, isLast: Boolean): Modifier =
    this
        .fillMaxWidth()
        .background(
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
            shape = sectionShape(isFirst, isLast)
        )
        .padding(16.dp)

@Composable
private fun CategoryHeader(category: TileCategory, modifier: Modifier = Modifier) {
    SectionHeader(
        title = category.label.load() ?: "",
        iconId = category.iconId,
        modifier = modifier,
    )
}

@Composable
private fun SectionHeader(title: String, iconId: Int, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private const val ControlPreviewScale = 0.82f

private fun packControlRows(
    items: List<Pair<QSControl, QSControlSpan>>,
    columns: Int,
): List<List<Pair<QSControl, QSControlSpan>>> = buildList {
    var row = mutableListOf<Pair<QSControl, QSControlSpan>>()
    var usedColumns = 0
    items.forEach { item ->
        val span = item.second
        if (row.isNotEmpty() && usedColumns + span.columns > columns) {
            add(row)
            row = mutableListOf()
            usedColumns = 0
        }
        row.add(item)
        usedColumns += span.columns
    }
    if (row.isNotEmpty()) add(row)
}

@Composable
private fun ControlsSection(
    controls: List<QSControl>,
    columns: Int,
    isLast: Boolean,
    controlPreview: @Composable (QSControl, QSControlSpan) -> Unit,
    isControlAdded: (QSControl) -> Boolean,
    onControlDragStart: (QSControl, QSControlSpan, Offset, DpSize) -> Unit,
    onControlDrag: (Offset) -> Unit,
    onControlDragEnd: () -> Unit,
    onShowSnackbar: () -> Unit,
) {
    Column(
        modifier = Modifier.sectionBackground(isFirst = true, isLast = isLast),
        verticalArrangement = spacedBy(16.dp)
    ) {
        SectionHeader(
            title = "Controls",
            iconId = TileCategory.UTILITIES.iconId,
        )

        val rows = packControlRows(
            controls.map { c ->
                val span = c.spans(columns).default
                c to span.copy(columns = span.columns.coerceAtMost(columns))
            },
            columns,
        )

        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val cellWidth = (maxWidth - TileArrangementPadding * (columns - 1)) / columns

            Column(verticalArrangement = spacedBy(TileArrangementPadding)) {
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = spacedBy(
                            TileArrangementPadding,
                            Alignment.CenterHorizontally
                        ),
                    ) {
                        row.forEach { (control, span) ->
                            ControlPickerCell(
                                control = control,
                                span = span,
                                cellWidth = cellWidth,
                                isAdded = isControlAdded(control),
                                controlPreview = controlPreview,
                                onDragStart = { pos, size ->
                                    onControlDragStart(control, span, pos, size)
                                },
                                onDrag = onControlDrag,
                                onDragEnd = onControlDragEnd,
                                onShowSnackbar = onShowSnackbar,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlPickerCell(
    control: QSControl,
    span: QSControlSpan,
    cellWidth: Dp,
    isAdded: Boolean,
    controlPreview: @Composable (QSControl, QSControlSpan) -> Unit,
    onDragStart: (Offset, DpSize) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onShowSnackbar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = control.label
    val fullWidth = cellWidth * span.columns + TileArrangementPadding * (span.columns - 1)
    val fullHeight = cellWidth * span.rows + TileArrangementPadding * (span.rows - 1)
    val previewWidth = fullWidth * ControlPreviewScale
    val previewHeight = fullHeight * ControlPreviewScale

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(8.dp, Alignment.Top),
        modifier = modifier
            .width(previewWidth)
            .alpha(if (isAdded) 0.28f else 1f)
            .semantics { contentDescription = label }
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(previewHeight)) {
            Box(Modifier.fillMaxSize().clearAndSetSemantics {}) {
                Box(
                    Modifier.requiredSize(fullWidth, fullHeight).graphicsLayer {
                        scaleX = ControlPreviewScale
                        scaleY = ControlPreviewScale
                    }
                ) {
                    controlPreview(control, span)
                }
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .pickerGestures(
                        enabled = !isAdded,
                        onDragStart = { pos -> onDragStart(pos, DpSize(fullWidth, fullHeight)) },
                        onDrag = onDrag,
                        onDragEnd = onDragEnd,
                        onTap = onShowSnackbar,
                    )
            )
        }

        Text(
            text = label,
            maxLines = 2,
            color = TilePickerDefaults.editTileColors().label,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium.copy(hyphens = Hyphens.Auto),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Modifier.pickerGestures(
    enabled: Boolean,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onTap: () -> Unit,
): Modifier {
    val context = LocalContext.current
    var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnTap by rememberUpdatedState(onTap)

    return this
        .onGloballyPositioned { coords = it }
        .pointerInput(enabled) {
            if (enabled) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        val rootPos = coords?.positionInRoot() ?: Offset.Zero
                        currentOnDragStart(rootPos + offset)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        currentOnDrag(dragAmount)
                    },
                    onDragEnd = { currentOnDragEnd() },
                    onDragCancel = { currentOnDragEnd() }
                )
            }
        }
        .pointerInput(enabled) {
            if (enabled) {
                detectTapGestures(
                    onTap = {
                        vibrateTap(context)
                        currentOnTap()
                    }
                )
            }
        }
}

private fun vibrateTap(context: android.content.Context) {
    val vibrator = context.getSystemService(android.os.Vibrator::class.java)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        vibrator?.vibrate(
            android.os.VibrationEffect.createOneShot(
                40L, android.os.VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(40L)
    }
}

@Composable
private fun TilePickerCell(
    tile: EditTileViewModel,
    insideSpecs: List<TileSpec>,
    onTileClick: (EditTileViewModel) -> Unit,
    onTileDragStart: (EditTileViewModel, Offset) -> Unit,
    onTileDrag: (Offset) -> Unit,
    onTileDragEnd: () -> Unit,
    onShowSnackbar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCurrent = tile.isCurrent || insideSpecs.contains(tile.tileSpec)
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(8.dp, Alignment.Top),
        modifier = modifier
            .alpha(if (isCurrent) 0.28f else 1f)
            .semantics { contentDescription = tile.label.text }
            .pickerGestures(
                enabled = !isCurrent,
                onDragStart = { pos -> onTileDragStart(tile, pos) },
                onDrag = onTileDrag,
                onDragEnd = onTileDragEnd,
                onTap = onShowSnackbar,
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            val tileColors = TilePickerDefaults.editTileColors()

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = tileColors.background,
                shape = RoundedCornerShape(InactiveCornerRadius)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(0.6f),
                        contentAlignment = Alignment.Center
                    ) {
                        SmallTileContent(
                            iconProvider = {
                                when (val icon = tile.icon) {
                                    is Icon.Loaded -> icon
                                    is Icon.Resource -> Icon.Loaded(
                                        context.getDrawable((icon as Icon.Resource).resId)!!,
                                        icon.contentDescription
                                    )
                                    else -> Icon.Loaded(context.getDrawable(android.R.drawable.ic_menu_help)!!, null)
                                }
                            },
                            color = tileColors.icon,
                            modifier = Modifier.clearAndSetSemantics {}
                        )
                    }
                }
            }
        }

        val tileColors = TilePickerDefaults.editTileColors()
        Text(
            text = tile.label.text,
            maxLines = 2,
            color = tileColors.label,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium.copy(hyphens = Hyphens.Auto),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private object TilePickerDefaults {
    val GridBackgroundCornerRadius = 28.dp

    @Composable
    fun editTileColors(): TileColors =
        TileColors(
            background = MaterialTheme.colorScheme.surfaceBright,
            iconBackground = MaterialTheme.colorScheme.surfaceBright,
            label = MaterialTheme.colorScheme.onSurface,
            secondaryLabel = MaterialTheme.colorScheme.onSurfaceVariant,
            icon = if (isSystemInDarkTheme()) Color.White else Color.Black,
        )
}

@Composable
private fun dimensionResource(id: Int): Dp = 24.dp
