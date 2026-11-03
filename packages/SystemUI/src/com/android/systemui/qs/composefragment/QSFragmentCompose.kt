/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.systemui.qs.composefragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.database.ContentObserver
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.os.Bundle
import android.os.Trace
import android.os.UserHandle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.approachLayout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onLayoutRectChanged
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.round
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android.app.tracing.coroutines.launchTraced
import com.android.compose.animation.scene.ContentKey
import com.android.compose.animation.scene.ContentScope
import com.android.compose.animation.scene.ElementKey
import com.android.compose.animation.scene.ElementMatcher
import com.android.compose.animation.scene.MutableSceneTransitionLayoutState
import com.android.compose.animation.scene.SceneKey
import com.android.compose.animation.scene.SceneTransitionLayout
import com.android.compose.animation.scene.SceneTransitionLayoutState
import com.android.compose.animation.scene.content.state.TransitionState
import com.android.compose.animation.scene.rememberMutableSceneTransitionLayoutState
import com.android.compose.animation.scene.transitions
import com.android.compose.gesture.gesturesDisabled
import com.android.compose.modifiers.height
import com.android.compose.modifiers.padding
import com.android.compose.modifiers.thenIf
import com.android.compose.theme.PlatformTheme
import com.android.compose.theme.colorAttr
import com.android.compose.theme.LocalAndroidColorScheme
import com.android.mechanics.GestureContext
import com.android.systemui.Dumpable
import com.android.systemui.Flags
import com.android.systemui.Flags.notificationShadeBlur
import com.android.systemui.brightness.ui.compose.BrightnessSliderContainer
import com.android.systemui.brightness.ui.compose.ContainerColors
import com.android.systemui.compose.modifiers.sysUiResTagContainer
import com.android.systemui.qs.tiles.BrightnessTileContent
import com.android.systemui.compose.modifiers.sysuiResTag
import com.android.systemui.dagger.qualifiers.Background
import com.android.systemui.dump.DumpManager
import com.android.systemui.initOnBackPressedDispatcherOwner
import com.android.systemui.keyboard.shortcut.ui.composable.InteractionsConfig
import com.android.systemui.keyboard.shortcut.ui.composable.ProvideShortcutHelperIndication
import com.android.systemui.lifecycle.rememberViewModel
import com.android.systemui.lifecycle.repeatWhenAttached
import com.android.systemui.lifecycle.setSnapshotBinding
import com.android.systemui.log.table.TableLogBuffer
import com.android.systemui.media.controls.ui.controller.MediaViewLogger
import com.android.systemui.media.controls.ui.view.MediaHost
import com.android.systemui.media.remedia.shared.flag.MediaControlsInComposeFlag
import com.android.systemui.media.remedia.ui.compose.Media
import com.android.systemui.media.remedia.ui.compose.MediaPresentationStyle
import com.android.systemui.media.remedia.ui.compose.MediaUiBehavior
import com.android.systemui.media.remedia.ui.viewmodel.MediaViewModel
import com.android.systemui.media.ui.compose.MiniPlayerCompact
import com.android.systemui.media.ui.viewmodel.MiniPlayerViewModel
import com.android.systemui.plugins.qs.QS
import com.android.systemui.plugins.qs.QSContainerController
import com.android.systemui.qs.composefragment.SceneKeys.QuickQuickSettings
import com.android.systemui.qs.composefragment.SceneKeys.QuickSettings
import com.android.systemui.qs.composefragment.SceneKeys.debugName
import com.android.systemui.qs.composefragment.SceneKeys.toIdleSceneKey
import com.android.systemui.qs.composefragment.ui.GridAnchor
import com.android.systemui.qs.composefragment.ui.NotificationScrimClipParams
import com.android.systemui.qs.composefragment.ui.quickQuickSettingsToQuickSettings
import com.android.systemui.qs.composefragment.ui.toEditMode
import com.android.systemui.qs.composefragment.ui.quickQuickSettingsToQuickSettingsOneUI
import com.android.systemui.qs.composefragment.viewmodel.QSFragmentComposeViewModel
import com.android.systemui.qs.footer.ui.compose.FooterActions
import com.android.systemui.qs.panels.shared.model.QSFragmentComposeClippingTableLog
import com.android.systemui.qs.panels.shared.model.FloatingTile
import com.android.systemui.qs.panels.ui.compose.EditMode
import com.android.systemui.qs.panels.ui.compose.FloatingTileDragState
import com.android.systemui.qs.panels.ui.compose.QuickQuickSettings
import com.android.systemui.qs.panels.ui.compose.TileGrid
import com.android.systemui.qs.shared.ui.QuickSettings.Elements
import com.android.systemui.animation.Expandable
import com.android.systemui.qs.panels.ui.compose.EditableQuickSettingsLayout
import com.android.systemui.qs.panels.ui.compose.EditableQuickQuickSettingsLayout
import com.android.systemui.qs.panels.shared.model.SectionConfig
import com.android.systemui.qs.panels.shared.model.SectionType
import com.android.systemui.qs.panels.shared.model.QSLayoutItem
import com.android.systemui.qs.panels.ui.viewmodel.EditTileViewModel
import com.android.systemui.qs.panels.ui.compose.infinitegrid.FullScreenTilePicker
import com.android.systemui.qs.panels.ui.compose.infinitegrid.LocalCollapsedRows
import com.android.systemui.qs.panels.ui.compose.infinitegrid.LocalQSCompactMode
import com.android.systemui.qs.panels.ui.compose.infinitegrid.LocalTileDragHandler
import com.android.systemui.qs.panels.ui.compose.infinitegrid.OneUITileContainer
import com.android.systemui.qs.panels.ui.compose.infinitegrid.OneUIExpandBar
import com.android.systemui.qs.panels.ui.compose.infinitegrid.OneUiInsideTileArea
import com.android.systemui.qs.panels.ui.compose.infinitegrid.OneUiEditScreen
import com.android.systemui.qs.panels.shared.model.QSControl
import com.android.systemui.qs.panels.ui.compose.LocalQSControlRenderer
import com.android.systemui.qs.panels.ui.compose.QSControlContent
import com.android.systemui.qs.panels.ui.compose.QSControlRenderer
import com.android.systemui.qs.panels.ui.compose.asQSControl
import com.android.systemui.qs.panels.ui.compose.bypassesOneUiContainer
import com.android.systemui.qs.panels.ui.compose.qsControlCornerRadius
import com.android.systemui.qs.ui.composable.QuickSettingsShade
import com.android.systemui.qs.ui.composable.QuickSettingsShade.systemGestureExclusionInShade
import com.android.systemui.qs.ui.composable.QuickSettingsTheme
import com.android.systemui.res.R
import com.android.systemui.shade.ShadeDisplayAware
import com.android.systemui.statusbar.policy.ConfigurationController
import com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
import com.android.systemui.util.LifecycleFragment
import com.android.systemui.util.asIndenting
import com.android.systemui.util.kotlin.pairwise
import androidx.compose.ui.text.style.TextOverflow
import com.android.systemui.util.printSection
import com.android.systemui.util.println
import com.android.systemui.qs.pipeline.shared.TileSpec
import java.io.PrintWriter
import java.util.function.Consumer
import javax.inject.Inject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.scale
import com.android.systemui.qs.pipeline.domain.model.TileModel
import kotlin.math.roundToInt
import kotlin.math.abs

import lineageos.providers.LineageSettings
import com.android.systemui.qs.panels.ui.compose.EditPanelModeTabRow

object MiniPlayerElementKey {
    val MiniPlayer = ElementKey("MiniPlayer")
}

@SuppressLint("ValidFragment")
class QSFragmentCompose
@Inject
constructor(
    private val qsFragmentComposeViewModelFactory: QSFragmentComposeViewModel.Factory,
    @QSFragmentComposeClippingTableLog private val qsClippingTableLogBuffer: TableLogBuffer,
    private val dumpManager: DumpManager,
    @Background private val backgroundDispatcher: CoroutineDispatcher,
    @ShadeDisplayAware private val configurationController: ConfigurationController,
    private val miniPlayerViewModelFactory: MiniPlayerViewModel.Factory,
) : LifecycleFragment(), QS, Dumpable {

    private val scrollListener = MutableStateFlow<QS.ScrollListener?>(null)
    private val collapsedMediaVisibilityChangedListener =
        MutableStateFlow<(Consumer<Boolean>)?>(null)
    private val heightListener = MutableStateFlow<QS.HeightListener?>(null)
    private val qqsHeightListener = MutableStateFlow<QS.QqsHeightListener?>(null)
    private val qsContainerController = MutableStateFlow<QSContainerController?>(null)

    private lateinit var viewModel: QSFragmentComposeViewModel

    private val qqsVisible = MutableStateFlow(false)
    private val qqsPositionOnRoot = Rect()
    private val composeViewPositionOnScreen = Rect()
    private val scrollState = ScrollState(0)
    private var oneUiContainerExpanded by mutableStateOf(false)
    private val locationTemp = IntArray(2)
    private var bottomBarPositionInRoot = IntRect(IntOffset(0, 0), 0)
    private var bottomContentPadding by mutableIntStateOf(0)
    private val containerView: FrameLayoutTouchPassthrough?
        get() = view as? FrameLayoutTouchPassthrough

    override fun onStart() {
        super.onStart()
        registerDumpable()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = qsFragmentComposeViewModelFactory.create(lifecycleScope)

        setListenerCollections()
        lifecycleScope.launch { viewModel.activate() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val context = inflater.context
        val composeView =
            ComposeView(context).apply {
                id = R.id.quick_settings_container
                repeatWhenAttached {
                    repeatOnLifecycle(Lifecycle.State.CREATED) {
                        initOnBackPressedDispatcherOwner(this@repeatWhenAttached.lifecycle)

                        launch {
                            viewModel.sectionEditModeViewModel.isEditingSections.collect { isEditing ->
                                if (isEditing) {
                                } else {
                                }
                            }
                        }

                        setContent {
                            this@QSFragmentCompose.Content(Modifier.sysUiResTagContainer())
                        }
                    }
                }
            }

        val canScrollQs =
            object : CanScrollQs {
                override fun forward(): Boolean {
                    return (scrollState.canScrollForward &&
                        viewModel.isQsFullyExpanded &&
                        !oneUiContainerExpanded) || isCustomizing
                }

                override fun backward(): Boolean {
                    return (scrollState.canScrollBackward && viewModel.isQsFullyExpanded) ||
                        isCustomizing
                }
            }

        val frame =
            FrameLayoutTouchPassthrough(
                context,
                // Only allow scrolling when we are fully expanded. That way, we don't intercept
                // swipes in lockscreen (when somehow QS is receiving touches).
                canScrollQs,
                viewModel::emitMotionEventForFalsingSwipeNested,
                qsClippingTableLogBuffer,
                backgroundDispatcher,
                isInBottomReservedArea = { x, y ->
                    (viewModel.isEditing || viewModel.sectionEditModeViewModel.isEditingSections.value || viewModel.showTilePicker.value) &&
                        bottomBarPositionInRoot.contains(IntOffset(x.toInt(), y.toInt()))
                },
                isInEditMode = { viewModel.isEditing || viewModel.sectionEditModeViewModel.isEditingSections.value || viewModel.isEditingOneUi.value },
                isInSectionEditMode = { viewModel.sectionEditModeViewModel.isEditingSections.value },
                isInTilePickerScene = { viewModel.showTilePicker.value },
            )
        frame.addView(
            composeView,
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        return frame
    }

    @Composable
    private fun Content(modifier: Modifier = Modifier) {
        CompositionLocalProvider(LocalQSControlRenderer provides rememberQSControlRenderer()) {
            ContentInner(modifier)
        }
    }

    @Composable
    private fun rememberQSControlRenderer(): QSControlRenderer {
        val brightnessViewModel = viewModel.containerViewModel.brightnessSliderViewModel
        return remember(brightnessViewModel) {
            QSControlRenderer(
                cornerRadius = { control, size -> qsControlCornerRadius(control, size) },
                content = { control, span, interactive ->
                    QSControlContent(
                        control = control,
                        span = span,
                        interactive = interactive,
                        brightnessViewModel = brightnessViewModel,
                    )
                },
            )
        }
    }

    @Composable
    private fun ContentInner(modifier: Modifier = Modifier) {
        val showTilePicker by viewModel.showTilePicker.collectAsStateWithLifecycle()
        val isEditingSections by viewModel.sectionEditModeViewModel.isEditingSections.collectAsStateWithLifecycle()
        val isEditingOneUi by viewModel.isEditingOneUi.collectAsStateWithLifecycle()
        val isLegacyEditing = viewModel.isEditing && !isEditingSections && !isEditingOneUi
        
        val oneUiTileStore = rememberOneUiTileStore()
        val insideTiles by oneUiTileStore.insideTiles.collectAsStateWithLifecycle()

        var editingQqsMode by rememberSaveable { mutableStateOf(false) }

        val qqsSpecs by viewModel.qqsTilesInteractor.qqsTiles
            .collectAsStateWithLifecycle<List<TileSpec>?>(initialValue = null)

        val insideAllTilesMap by remember(viewModel.containerViewModel.tileGridViewModel.tiles) {
            viewModel.containerViewModel.tileGridViewModel.tiles.map { list ->
                list.associate { it.spec to it.tile }
            }
        }.collectAsStateWithLifecycle(initialValue = emptyMap())

        val context = LocalContext.current
        var hasLoadedLayout by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            // One-time migration: older builds mirrored the layout into the
            // qs_oneui_layout prefs; QSSectionsRepository is the only store now.
            val prefs = context.getSharedPreferences("qs_oneui_layout", Context.MODE_PRIVATE)
            val items = withContext(backgroundDispatcher) {
                val saved = prefs.getString("layout", null)
                if (saved.isNullOrEmpty()) {
                    null
                } else {
                    try {
                        saved.split(";").mapNotNull { part ->
                            val props = part.split(",")
                            if (props[0] == "header") {
                                QSLayoutItem.SectionHeader(SectionType.valueOf(props[1]), props[2].toBoolean(), props.getOrNull(3)?.toFloat() ?: 2f)
                            } else if (props[0] == "tile") {
                                QSLayoutItem.TileItem(TileSpec.create(props[1]), props[2].toInt(), props[3].toInt())
                            } else null
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            }
            if (!items.isNullOrEmpty()) {
                viewModel.sectionEditModeViewModel.setFlatLayout(items)
                prefs.edit().remove("layout").apply()
            }
            hasLoadedLayout = true
        }

        val flatLayout by viewModel.sectionEditModeViewModel.flatLayout.collectAsStateWithLifecycle(initialValue = QSLayoutItem.getDefault())

        var lastPushedSpecs by remember { mutableStateOf<Set<TileSpec>>(emptySet()) }

        LaunchedEffect(flatLayout, insideTiles, qqsSpecs, hasLoadedLayout) {
            val qqs = qqsSpecs
            if (hasLoadedLayout && qqs != null) {
                val flatSpecs = flatLayout.filterIsInstance<QSLayoutItem.TileItem>().map { it.spec }
                val activeSpecs = (flatSpecs + insideTiles + qqs).distinct()
                lastPushedSpecs = activeSpecs.toSet()
                viewModel.containerViewModel.editModeViewModel.setTiles(activeSpecs)
            }
        }

        val backendTiles by viewModel.containerViewModel.tileGridViewModel.tiles.collectAsStateWithLifecycle(emptyList())

        LaunchedEffect(backendTiles, qqsSpecs, hasLoadedLayout) {
            val qqs = qqsSpecs
            if (hasLoadedLayout && qqs != null) {
                val flatSpecs = flatLayout.filterIsInstance<QSLayoutItem.TileItem>().map { it.spec }
                val allFrontendSpecs = (flatSpecs + insideTiles + qqs).toSet()
                val backendSpecs = backendTiles.map { it.spec }
                
                val missingSpecs = backendSpecs.filter { it !in allFrontendSpecs && it !in lastPushedSpecs }
                if (missingSpecs.isNotEmpty()) {
                    missingSpecs.forEach { spec ->
                        val controlDefault = spec.asQSControl()?.spans(4)?.default
                        val spanCols = controlDefault?.columns ?: if (spec.spec == "brightness") 3 else 1
                        viewModel.sectionEditModeViewModel.addMainQSTile(
                            FloatingTile(spec, SectionType.TILES, spanCols, controlDefault?.rows ?: 1)
                        )
                    }
                }
            }
        }

        BackHandler(enabled = showTilePicker || isEditingSections || isLegacyEditing || isEditingOneUi) {
            if (showTilePicker) {
                viewModel.closeTilePicker()
            } else if (isEditingOneUi) {
                viewModel.closeOneUiEdit()
            } else if (isEditingSections) {
                viewModel.sectionEditModeViewModel.stopEditingSections()
            } else if (isLegacyEditing) {
                viewModel.containerViewModel.editModeViewModel.stopEditing()
            }
        }

        PlatformTheme(isDarkTheme = if (notificationShadeBlur()) isSystemInDarkTheme() else true) {
            ProvideShortcutHelperIndication(interactionsConfig = interactionsConfig()) {
                // TODO(b/389985793): Make sure that there is no coroutine work or recompositions
                // happening when alwaysCompose is true but isQsVisibleAndAnyShadeExpanded is false.
                if (alwaysCompose || viewModel.isQsVisibleAndAnyShadeExpanded) {
                    Box(modifier = modifier) {
                        val isExternalDrag = com.android.systemui.qs.panels.ui.compose.FloatingTileDragState.isExternalDrag
                        var useZeroDurationExit by remember { mutableStateOf(false) }

                        LaunchedEffect(isExternalDrag, showTilePicker) {
                            if (isExternalDrag && showTilePicker) {
                                useZeroDurationExit = true
                            } else if (!showTilePicker && !isExternalDrag) {
                                kotlinx.coroutines.delay(300)
                                useZeroDurationExit = false
                            }
                        }

                        val pickerHiddenAlpha by animateFloatAsState(
                            targetValue = if (showTilePicker && !isExternalDrag) 0f else 1f,
                            animationSpec = tween(durationMillis = 180),
                            label = "pickerHiddenAlpha"
                        )

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .thenIf(alwaysCompose) {
                                    Modifier.layout { measurable, constraints ->
                                        measurable.measure(constraints).run {
                                            layout(width, height) {
                                                if (viewModel.isQsVisibleAndAnyShadeExpanded) {
                                                    place(0, 0)
                                                }
                                            }
                                        }
                                    }
                                }
                                .graphicsLayer { 
                                    alpha = if (viewModel.showingMirror) 1f else viewModel.viewAlpha 
                                }
                                .thenIf(!Flags.notificationShadeBlur()) {
                                    Modifier.offset {
                                        IntOffset(
                                            x = 0,
                                            y = viewModel.viewTranslationY.fastRoundToInt(),
                                        )
                                    }
                                }
                                // Disable touches in the whole composable while the mirror is
                                // showing. While the mirror is showing, an ancestor of the
                                // ComposeView is made alpha 0, but touches are still being captured
                                // by the composables.
                                .thenIf(viewModel.showingMirror) { Modifier.gesturesDisabled() }
                                .graphicsLayer { alpha = pickerHiddenAlpha }
                        ) {
                            CollapsableQuickSettingsSTL(oneUiTileStore)
                        }

                        AnimatedVisibility(
                            visible = showTilePicker,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                            exit = if (useZeroDurationExit) fadeOut(tween(0)) else fadeOut(tween(200)),
                            modifier = Modifier.zIndex(100f).fillMaxSize()
                        ) {
                            val availableTiles by viewModel.getTilesForPicker().collectAsStateWithLifecycle(emptyList())
                            val density = LocalDensity.current
                            val controlRenderer = LocalQSControlRenderer.current

                            Box(modifier = Modifier.fillMaxSize()) {
                                if (editingQqsMode) {
                                    com.android.systemui.qs.panels.ui.compose.infinitegrid.QqsEditGrid(
                                        modifier = Modifier.fillMaxSize(),
                                        allTiles = availableTiles,
                                        qqsColumns = viewModel.quickQuickSettingsViewModel.columns,
                                        qqsTilesInteractor = viewModel.qqsTilesInteractor,
                                        ensureCurrent = { spec ->
                                            if (!availableTiles.any { it.tileSpec == spec && it.isCurrent }) {
                                                viewModel.containerViewModel.editModeViewModel.addTile(spec)
                                            }
                                        },
                                        releaseIfUnused = { spec ->
                                            val stillInMainQs =
                                                insideTiles.contains(spec) ||
                                                    flatLayout.any {
                                                        it is QSLayoutItem.TileItem && it.spec == spec
                                                    }
                                            if (!stillInMainQs) {
                                                viewModel.containerViewModel.editModeViewModel.removeTile(spec)
                                            }
                                        },
                                        onStopEditing = { viewModel.closeTilePicker() },
                                    )
                                } else {
                                    FullScreenTilePicker(
                                        modifier = Modifier.fillMaxSize(),
                                        allTiles = availableTiles,
                                        insideSpecs = insideTiles,
                                        onClose = {
                                            viewModel.closeTilePicker()
                                        },
                                        onTileClick = { tile ->
                                            com.android.systemui.qs.panels.ui.compose.FloatingTileDragState.fallbackTiles[tile.tileSpec] = tile
                                            val spanCols = if (tile.tileSpec.spec == "brightness") 3 else 1
                                            viewModel.sectionEditModeViewModel.addMainQSTile(
                                                FloatingTile(tile.tileSpec, SectionType.TILES, spanCols, 1)
                                            )
                                            viewModel.containerViewModel.editModeViewModel.addTile(tile.tileSpec)
                                            viewModel.closeTilePicker()
                                        },
                                        controlPreview = { control, span ->
                                            if (controlRenderer != null) {
                                                controlRenderer.content(control, span, false)
                                            }
                                        },
                                        controls = QSControl.entries,
                                        isControlAdded = { control ->
                                            flatLayout.any { it is QSLayoutItem.TileItem && it.spec.spec == control.id }
                                        },
                                        onControlDragStart = { control, span, position, size ->
                                            if (!viewModel.wasEditingBeforePicker) {
                                                viewModel.containerViewModel.editModeViewModel.stopEditing()
                                            }
                                            viewModel.sectionEditModeViewModel.startEditingSections()

                                            val half = with(density) {
                                                Offset(size.width.toPx() / 2, size.height.toPx() / 2)
                                            }
                                            com.android.systemui.qs.panels.ui.compose.FloatingTileDragState.startDrag(
                                                tile = FloatingTile(
                                                    TileSpec.create(control.id),
                                                    SectionType.TILES,
                                                    span.columns,
                                                    span.rows,
                                                ),
                                                position = position - half,
                                                size = size,
                                                sourceSection = SectionType.TILES,
                                            )
                                            com.android.systemui.qs.panels.ui.compose.FloatingTileDragState.isExternalDrag = true
                                        },
                                        onTileDragStart = { tile, position ->
                                            if (!viewModel.wasEditingBeforePicker) {
                                                viewModel.containerViewModel.editModeViewModel.stopEditing()
                                            }
                                            viewModel.sectionEditModeViewModel.startEditingSections()

                                            val isBrightness = tile.tileSpec.spec == "brightness"
                                            val spanCols = if (isBrightness) 3 else 1

                                            val defaultSize = if (isBrightness) DpSize(264.dp, 88.dp) else DpSize(88.dp, 88.dp)
                                            val offsetPx = with(density) { 44.dp.toPx() }
                                            com.android.systemui.qs.panels.ui.compose.FloatingTileDragState.startDrag(
                                                tile = FloatingTile(tile.tileSpec, SectionType.TILES, spanCols, 1),
                                                position = position - Offset(offsetPx, offsetPx),
                                                size = defaultSize,
                                                sourceSection = SectionType.TILES,
                                                editTile = tile
                                            )
                                            com.android.systemui.qs.panels.ui.compose.FloatingTileDragState.isExternalDrag = true
                                        },
                                        onTileDrag = { dragAmount ->
                                            com.android.systemui.qs.panels.ui.compose.FloatingTileDragState.updateDrag(dragAmount)
                                        },
                                        onTileDragEnd = {
                                            com.android.systemui.qs.panels.ui.compose.FloatingTileDragState.dropRequested = true
                                            viewModel.closeTilePicker(stayInLayoutEditMode = true)
                                        }
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    shadowElevation = 4.dp,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .navigationBarsPadding()
                                        .padding(bottom = 32.dp)
                                        .height(56.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        EditPanelModeTabRow(
                                            editingQqs = editingQqsMode,
                                            onEditQqs = { editingQqsMode = true },
                                            onEditQs = { editingQqsMode = false },
                                            qqsLabel = stringResource(R.string.qs_edit_mode_qqs),
                                            qsLabel = stringResource(R.string.qs_edit_mode_qs),
                                        )
                                    }
                                }
                            }
                        }
                        
                        AnimatedVisibility(
                            visible = isEditingOneUi,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                            exit = fadeOut(tween(200)),
                            modifier = Modifier.zIndex(100f).fillMaxSize()
                        ) {
                            OneUiEditScreen(
                                insideSpecs = insideTiles,
                                allTilesMap = insideAllTilesMap,
                                onReorder = { specs -> oneUiTileStore.setTiles(specs) },
                                onTileRemoved = { spec ->
                                    oneUiTileStore.removeTile(spec)
                                    val spanCols = if (spec.spec == "brightness") 3 else 1
                                    viewModel.sectionEditModeViewModel.addMainQSTile(
                                        FloatingTile(spec, SectionType.TILES, spanCols, 1)
                                    )
                                    viewModel.containerViewModel.editModeViewModel.addTile(spec)
                                },
                                onDone = { viewModel.closeOneUiEdit() }
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * STL that contains both QQS (tiles) and QS (brightness, tiles, footer actions), but no Edit
     * mode. It tracks [QSFragmentComposeViewModel.expansionState] to drive the transition between
     * [SceneKeys.QuickQuickSettings] and [SceneKeys.QuickSettings].
     */
    @Composable
    private fun CollapsableQuickSettingsSTL(oneUiTileStore: OneUiTileStore) {
        val nextCookie = remember {
            object {
                var value = 0
            }
        }
        val transitionToCookie = remember { mutableMapOf<TransitionState.Transition, Int>() }
        val sceneState =
            rememberMutableSceneTransitionLayoutState(
                initialScene = remember { viewModel.expansionState.toIdleSceneKey() },
                transitions =
                    transitions {
                        from(QuickQuickSettings, QuickSettings) {
                            quickQuickSettingsToQuickSettingsOneUI()
                        }
                        to(SceneKeys.EditMode) {
                            spec = tween(durationMillis = EDIT_MODE_TIME_MILLIS)
                            toEditMode()
                        }
                    },
                onTransitionStart = { transition ->
                    val cookie = nextCookie.value++
                    transitionToCookie[transition] = cookie
                    Trace.beginAsyncSection(
                        "CollapsableQuickSettingsSTL ${transition.debugName}",
                        cookie,
                    )
                },
                onTransitionEnd = { transition ->
                    Trace.endAsyncSection(
                        "CollapsableQuickSettingsSTL ${transition.debugName}",
                        transitionToCookie.remove(transition) ?: -1,
                    )
                },
            )

        LaunchedEffect(Unit) {
            launch {
                synchronizeQsState(
                    sceneState,
                    viewModel.containerViewModel.editModeViewModel.isEditing,
                    viewModel.sectionEditModeViewModel.isEditingSections,
                    viewModel.showTilePicker,
                    viewModel.isEditingOneUi,
                    snapshotFlow { viewModel.expansionState }.map { it.progress },
                )
            }
            // Normally, the Edit mode will stop if the composable leaves, but if the shade
            // is closed, because we are always composed, we don't stop edit mode.
            launch {
                snapshotFlow { viewModel.isQsVisibleAndAnyShadeExpanded }
                    .collect {
                        if (!it) {
                            viewModel.containerViewModel.editModeViewModel.stopEditing()
                        }
                    }
            }
            launch {
                snapshotFlow { viewModel.isQsFullyExpanded }
                    .collect {
                        if (!it && viewModel.isEditing) {
                            viewModel.containerViewModel.editModeViewModel.stopEditing()
                        }
                        if (!it && viewModel.sectionEditModeViewModel.isEditingSections.value) {
                            viewModel.sectionEditModeViewModel.stopEditingSections()
                        }
                        if (!it && viewModel.showTilePicker.value) {
                            viewModel.closeTilePicker()
                        }
                    }
            }
        }

        SceneTransitionLayout(
            state = sceneState,
            modifier = Modifier.fillMaxSize(),
            debugName = "QuickSettings",
        ) {
            scene(QuickSettings, alwaysCompose = true) {
                LaunchedEffect(Unit) { viewModel.onQSOpen() }
                Element(QuickSettings.rootElementKey, Modifier) { QuickSettingsElement(oneUiTileStore) }
            }

            scene(QuickQuickSettings, alwaysCompose = true) {
                LaunchedEffect(Unit) { viewModel.onQQSOpen() }
                // Cannot pass the element modifier in because the top element has a `testTag`
                // and this would overwrite it.
                Element(QuickQuickSettings.rootElementKey, Modifier) { QuickQuickSettingsElement() }
            }

            scene(SceneKeys.EditMode) {
                Box(Modifier.fillMaxSize()) {
                    Element(SceneKeys.EditMode.rootElementKey, Modifier) { EditModeElement() }
                    /*
                     * This provides the position of the bottom nav bar wrt to the root. As it's
                     * full screen (and the container view has the same bounds) this can be used to
                     * filter out touches in this bottom bar, and allow the shade to process them
                     * if necessary.
                     */
                    Spacer(
                        Modifier
                            // default debounce 64ms (4+ frames of stability)
                            .onLayoutRectChanged { bottomBarPositionInRoot = it.boundsInRoot }
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .windowInsetsBottomHeight(WindowInsets.systemBars)
                    )
                }
            }
        }
    }

    override fun setPanelView(notificationPanelView: QS.HeightListener?) {
        heightListener.value = notificationPanelView
    }

    override fun setQqsHeightListener(listener: QS.QqsHeightListener?) {
        qqsHeightListener.value = listener
    }

    override fun hideImmediately() {
        //        view?.animate()?.cancel()
        //        view?.y = -qsMinExpansionHeight.toFloat()
    }

    override fun getQsMinExpansionHeight(): Int {
        return if (viewModel.isInSplitShade) {
            getQsMinExpansionHeightForSplitShade()
        } else {
            viewModel.qqsHeight
        }
    }

    /**
     * Returns the min expansion height for split shade.
     *
     * On split shade, QS is always expanded and goes from the top of the screen to the bottom of
     * the QS container.
     */
    private fun getQsMinExpansionHeightForSplitShade(): Int {
        view?.getLocationOnScreen(locationTemp)
        val top = locationTemp.get(1)
        // We want to get the original top position, so we subtract any translation currently set.
        val originalTop = (top - (view?.translationY ?: 0f)).toInt()
        // On split shade the QS view doesn't start at the top of the screen, so we need to add the
        // top margin.
        return originalTop + (view?.height ?: 0)
    }

    override fun getDesiredHeight(): Int {
        /*
         * Looking at the code, it seems that
         * * If customizing, then the height is that of the view post-layout, which is set by
         *   QSContainerImpl.calculateContainerHeight, which is the height the customizer takes
         * * If not customizing, it's the measured height. So we may want to surface that.
         */
        return view?.height ?: 0
    }

    override fun setHeightOverride(desiredHeight: Int) {
        viewModel.heightOverride = desiredHeight
    }

    override fun setHeaderClickable(qsExpansionEnabled: Boolean) {
        // Empty method
    }

    override fun isCustomizing(): Boolean {
        // Return true for legacy and OneUI editing to hide clocks, but keep them visible for layout edits
        return (viewModel.isEditing && !viewModel.sectionEditModeViewModel.isEditingSections.value) || viewModel.isEditingOneUi.value
    }

    override fun closeCustomizer() {
        if (viewModel.showTilePicker.value) {
            viewModel.closeTilePicker()
        }
        viewModel.containerViewModel.editModeViewModel.stopEditing()
        viewModel.sectionEditModeViewModel.stopEditingSections()
        viewModel.isEditingOneUi.value = false
    }

    override fun setOverscrolling(overscrolling: Boolean) {
        viewModel.isStackScrollerOverscrolling = overscrolling
    }

    override fun setPanelExpanded(panelExpanded: Boolean) {
        viewModel.isPanelExpanded = panelExpanded
    }

    override fun setExpanded(qsExpanded: Boolean) {
        viewModel.isQsExpanded = qsExpanded
    }

    override fun setListening(listening: Boolean) {
        // Not needed, views start listening and collection when composed
    }

    override fun setQsVisible(qsVisible: Boolean) {
        containerView?.qsVisible = qsVisible
        viewModel.isQsVisible = qsVisible
    }

    override fun isShowingDetail(): Boolean {
        return isCustomizing
    }

    override fun closeDetail() {
        closeCustomizer()
    }

    override fun animateHeaderSlidingOut() {
        // TODO(b/353254353)
    }

    override fun setQsExpansion(
        qsExpansionFraction: Float,
        panelExpansionFraction: Float,
        headerTranslation: Float,
        squishinessFraction: Float,
    ) {
        if (!(viewModel.isEditing || viewModel.sectionEditModeViewModel.isEditingSections.value)) {
            viewModel.setQsExpansionValue(qsExpansionFraction)
            viewModel.panelExpansionFraction = panelExpansionFraction
            viewModel.squishinessFraction = squishinessFraction
            viewModel.proposedTranslation = headerTranslation
        }
    }

    override fun setContainerController(controller: QSContainerController?) {
        qsContainerController.value = controller
    }

    override fun setCollapseExpandAction(action: Runnable?) {
        viewModel.collapseExpandAccessibilityAction = action
    }

    override fun setShouldUpdateSquishinessOnMedia(shouldUpdate: Boolean) {
        viewModel.shouldUpdateSquishinessOnMedia = shouldUpdate
    }

    override fun setInSplitShade(isInSplitShade: Boolean) {
        viewModel.isInSplitShade = isInSplitShade
    }

    override fun setTransitionToFullShadeProgress(
        isTransitioningToFullShade: Boolean,
        qsTransitionFraction: Float,
        qsSquishinessFraction: Float,
    ) {
        viewModel.isTransitioningToFullShade = isTransitioningToFullShade
        viewModel.lockscreenToShadeProgress = qsTransitionFraction
        if (isTransitioningToFullShade) {
            viewModel.squishinessFraction = qsSquishinessFraction
        }
    }

    override fun setFancyClipping(
        leftInset: Int,
        top: Int,
        rightInset: Int,
        bottom: Int,
        cornerRadius: Int,
        visible: Boolean,
        fullWidth: Boolean,
    ) {
        containerView?.clipData =
            visible to
                NotificationScrimClipParams(
                    top,
                    bottom,
                    if (fullWidth) 0 else leftInset,
                    if (fullWidth) 0 else rightInset,
                    cornerRadius,
                )
    }

    override fun isFullyCollapsed(): Boolean {
        return viewModel.isQsFullyCollapsed
    }

    override fun setCollapsedMediaVisibilityChangedListener(listener: Consumer<Boolean>?) {
        collapsedMediaVisibilityChangedListener.value = listener
    }

    override fun setScrollListener(scrollListener: QS.ScrollListener?) {
        this.scrollListener.value = scrollListener
    }

    override fun setOverScrollAmount(overScrollAmount: Int) {
        viewModel.overScrollAmount = overScrollAmount
    }

    override fun setIsNotificationPanelFullWidth(isFullWidth: Boolean) {
        viewModel.isSmallScreen = isFullWidth
    }

    override fun getHeaderTop(): Int {
        return qqsPositionOnRoot.top
    }

    override fun getHeaderBottom(): Int {
        return qqsPositionOnRoot.bottom
    }

    override fun getHeaderLeft(): Int {
        return qqsPositionOnRoot.left
    }

    override fun getHeaderBoundsOnScreen(outBounds: Rect) {
        outBounds.set(qqsPositionOnRoot)
        view?.getBoundsOnScreen(composeViewPositionOnScreen)
            ?: run { composeViewPositionOnScreen.setEmpty() }
        outBounds.offset(composeViewPositionOnScreen.left, composeViewPositionOnScreen.top)
    }

    override fun isHeaderShown(): Boolean {
        return qqsVisible.value
    }

    override fun setQSContentPaddingBottom(padding: Int) {
        bottomContentPadding = padding
    }

    private val configurationListener =
        object : ConfigurationListener {
            override fun onConfigChanged(newConfig: Configuration) {
                view?.dispatchConfigurationChanged(newConfig)
            }
        }

    private fun setListenerCollections() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                var lastQqsHeight = -1
                var lastQqsMediaVisible: Boolean? = null
                this@QSFragmentCompose.view?.setSnapshotBinding {
                    scrollListener.value?.onQsPanelScrollChanged(scrollState.value)
                    if (lastQqsMediaVisible != viewModel.qqsMediaVisible) {
                        lastQqsMediaVisible = viewModel.qqsMediaVisible
                        collapsedMediaVisibilityChangedListener.value?.accept(
                            viewModel.qqsMediaVisible
                        )
                    }
                    if (lastQqsHeight != viewModel.qqsHeight) {
                        lastQqsHeight = viewModel.qqsHeight
                        qqsHeightListener.value?.onQqsHeightChanged()
                    }
                }
                launch {
                    setListenerJob(
                        heightListener,
                        viewModel.isCustomizingUi,
                    ) {
                        onQsHeightChanged()
                    }
                }
                launch {
                    setListenerJob(
                        qsContainerController,
                        viewModel.isCustomizingUi,
                    ) {
                        setCustomizerShowing(it, EDIT_MODE_TIME_MILLIS.toLong())
                    }
                }
                launch {
                    try {
                        configurationController.addCallback(configurationListener)
                        awaitCancellation()
                    } finally {
                        configurationController.removeCallback(configurationListener)
                    }
                }
            }
        }
    }

    @Composable
    private fun ContentScope.QuickQuickSettingsElement(modifier: Modifier = Modifier) {
        val qqsPadding = viewModel.qqsHeaderHeight
        val bottomPadding = viewModel.qqsBottomPadding
        val sectionConfigs by viewModel.sectionEditModeViewModel.sectionConfigs.collectAsStateWithLifecycle(
            initialValue = SectionConfig.getDefaultConfigs()
        )

        val isMirroring = viewModel.showingMirror
        val contentAlpha by animateFloatAsState(targetValue = if (isMirroring) 0f else 1f, tween(200), label = "mirroring_alpha")
        val qqsContentSettings = rememberQqsContentSettings()

        DisposableEffect(Unit) {
            qqsVisible.value = true
            onDispose { qqsVisible.value = false }
        }
        val squishiness by
            viewModel.quickQuickSettingsViewModel.squishinessViewModel.squishiness
                .collectAsStateWithLifecycle()

        Column(modifier = modifier.sysuiResTag(ResIdTags.quickQsPanel)) {
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .onPlaced { coordinates ->
                            val (leftFromRoot, topFromRoot) = coordinates.positionInRoot().round()
                            qqsPositionOnRoot.set(
                                leftFromRoot,
                                topFromRoot,
                                leftFromRoot + coordinates.size.width,
                                topFromRoot + coordinates.size.height,
                            )
                            if (squishiness == 1f) {
                                viewModel.qqsHeight = coordinates.size.height
                            }
                        }
                        // Use an approach layout to determien the height without squishiness, as
                        // that's the value that NPVC and QuickSettingsController care about
                        // (measured height).
                        .approachLayout(isMeasurementApproachInProgress = { squishiness < 1f }) {
                            measurable,
                            constraints ->
                            viewModel.qqsHeight = lookaheadSize.height
                            val placeable = measurable.measure(constraints)
                            layout(placeable.width, placeable.height) { placeable.place(0, 0) }
                        }
                        .padding(top = { qqsPadding }, bottom = { bottomPadding })
            ) {
                val BrightnessSlider: @Composable () -> Unit = {
                    Element(SceneKeys.QqsBrightnessSlider, modifier = modifier) {
                        BrightnessSlider(viewModel, layoutState)
                    }
                }
                val Tiles =
                    @Composable {
                        // When always compose is false, this will always be true, and we'll be
                        // listening whenever this is composed. When always compose is true, we
                        // listen if we are visible and not fully expanded
                        val isListening: () -> Boolean =
                            remember(viewModel) {
                                    derivedStateOf {
                                        viewModel.isQsVisibleAndAnyShadeExpanded &&
                                            viewModel.expansionState.progress < 1f &&
                                            !viewModel.isEditing
                                    }
                                }
                                .let { state -> { state.value } }

                        Element(SceneKeys.QuickQuickSettingsContent, Modifier) {
                            QuickQuickSettings(
                                viewModel = viewModel.quickQuickSettingsViewModel,
                                listening = isListening,
                            )
                        }
                    }
                val Media = @Composable {
                    Element(MiniPlayerElementKey.MiniPlayer, modifier = Modifier.fillMaxWidth()) {
                        val miniPlayerViewModel = rememberViewModel("MiniPlayerQQS") {
                            miniPlayerViewModelFactory.create()
                        }
                        val expansionProgress by remember {
                            derivedStateOf { viewModel.expansionState.progress }
                        }
                        MiniPlayerCompact(
                            viewModel = miniPlayerViewModel,
                            compact = true,
                            expansionProgress = expansionProgress,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (viewModel.isQsEnabled) {
                    Box(
                        modifier =
                            Modifier.collapseExpandSemanticAction(
                                    stringResource(
                                        id = R.string.accessibility_quick_settings_expand
                                    )
                                )
                                .padding(horizontal = qsHorizontalMargin())
                    ) {
                        EditableQuickQuickSettingsLayout(
                            sectionConfigs = sectionConfigs.filter {
                                when (it.type) {
                                    SectionType.BRIGHTNESS -> qqsContentSettings.showBrightness
                                    SectionType.MEDIA -> qqsContentSettings.showMedia
                                    else -> true
                                }
                            },
                            sectionEditModeViewModel = viewModel.sectionEditModeViewModel,
                            nonBrightnessAlpha = contentAlpha,
                            brightness = BrightnessSlider,
                            tiles = Tiles,
                            media = Media,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }

    @Composable
    private fun ContentScope.QuickSettingsElement(oneUiTileStore: OneUiTileStore, modifier: Modifier = Modifier) {
        val qqsPadding = viewModel.qqsHeaderHeight
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val insideTiles by oneUiTileStore.insideTiles.collectAsStateWithLifecycle()
        val isEditingOneUi by viewModel.isEditingOneUi.collectAsStateWithLifecycle()

        val insideAllTilesMap by remember(viewModel.containerViewModel.tileGridViewModel.tiles) {
            viewModel.containerViewModel.tileGridViewModel.tiles.map { list ->
                list.associate { it.spec to it.tile }
            }
        }.collectAsStateWithLifecycle(initialValue = emptyMap())

        val sectionConfigs by viewModel.sectionEditModeViewModel.sectionConfigs.collectAsStateWithLifecycle(
            initialValue = SectionConfig.getDefaultConfigs()
        )

        val flatLayout by viewModel.sectionEditModeViewModel.flatLayout
            .collectAsStateWithLifecycle(initialValue = QSLayoutItem.getDefault())

        var isTilesExpanded by rememberSaveable { mutableStateOf(false) }

        val isEditingSections by viewModel.sectionEditModeViewModel.isEditingSections.collectAsStateWithLifecycle()

        LaunchedEffect(isEditingSections) {
            if (isEditingSections) {
                isTilesExpanded = false
            }
        }

        LaunchedEffect(Unit) {
            snapshotFlow { layoutState.transitionState }
                .collect { state ->
                    if (state is TransitionState.Idle && state.currentScene == SceneKeys.QuickQuickSettings) {
                        isTilesExpanded = false
                    }
                }
        }

        LaunchedEffect(Unit) {
            snapshotFlow { viewModel.isQsFullyExpanded }
                .collect { fullyExpanded ->
                    if (!fullyExpanded && isTilesExpanded) {
                        isTilesExpanded = false
                        scrollState.scrollTo(0)
                    }
                }
        }
        SideEffect { oneUiContainerExpanded = isTilesExpanded }
        DisposableEffect(Unit) { onDispose { oneUiContainerExpanded = false } }

        var swipeBackProgress by remember { mutableFloatStateOf(0f) }
        val swipeBackAlpha by animateFloatAsState(
            targetValue = 1f - swipeBackProgress,
            animationSpec = tween(durationMillis = 150),
            label = "qs_swipe_back_alpha"
        )

        val editModeScale by animateFloatAsState(
            targetValue = if (isEditingSections) 0.93f else 1f,
            animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
            label = "editModeScale"
        )

        val isMirroring = viewModel.showingMirror
        val contentAlpha by animateFloatAsState(
            targetValue = if (isMirroring) 0f else 1f,
            animationSpec = tween(200),
            label = "mirroring_alpha"
        )

        val isLegacyEditing = viewModel.isEditing && !isEditingSections && !isEditingOneUi
        val legacyEditAlpha by animateFloatAsState(
            targetValue = if (isLegacyEditing || isEditingOneUi) 0f else 1f,
            animationSpec = tween(durationMillis = 200),
            label = "legacyEditAlpha"
        )

        val blurAmount by animateDpAsState(
            targetValue = if (isEditingOneUi) 16.dp else 0.dp,
            label = "one_ui_blur"
        )

        Box(
            modifier = modifier
                .fillMaxSize()
                .collapseExpandSemanticAction(stringResource(id = R.string.accessibility_quick_settings_collapse))
                .graphicsLayer { alpha = swipeBackAlpha * legacyEditAlpha }
                .pointerInput(isEditingSections) {
                    if (isEditingSections) return@pointerInput
                    detectDragGestures(
                        onDragEnd = {
                            if (swipeBackProgress >= 0.3f) {
                                viewModel.collapseExpandAccessibilityAction?.run()
                            }
                            swipeBackProgress = 0f
                        },
                        onDragCancel = { swipeBackProgress = 0f },
                    ) { change, dragAmount ->
                        val isRightSwipe = dragAmount.x > 0
                        val isHorizontal = abs(dragAmount.x) > abs(dragAmount.y)
                        if (isRightSwipe && isHorizontal) {
                            change.consume()
                            val halfWidth = size.width.toFloat() * 0.5f
                            swipeBackProgress =
                                (swipeBackProgress + dragAmount.x / halfWidth).coerceIn(0f, 1f)
                        }
                    }
                }
        ) {
            Column(modifier = Modifier.fillMaxSize().blur(blurAmount)) {
                if (viewModel.isQsEnabled) {
                    Spacer(
                        modifier = Modifier.height { 
                            if (isLandscape) (qqsPadding / 3).coerceAtLeast(8) else qqsPadding 
                        }
                    )
                    Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 70.dp))

                    QuickSettingsTheme {
                        Element(
                            Elements.FooterActions,
                            Modifier.sysuiResTag(ResIdTags.qsFooterActions),
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().graphicsLayer { alpha = contentAlpha }) {
                                Spacer(modifier = Modifier.height(if (isLandscape) 4.dp else 12.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 4.dp),
                                ) {
                                    if (!isEditingSections) {
                                        FooterActions(
                                            viewModel = viewModel.footerActionsViewModel,
                                        )

                                        val backgroundColor = LocalAndroidColorScheme.current.surfaceEffect1
                                        val iconTint = MaterialTheme.colorScheme.onSurface

                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .padding(horizontal = 4.dp)
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    backgroundColor,
                                                    CircleShape
                                                )
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                ) {
                                                    viewModel.sectionEditModeViewModel.startEditingSections()
                                                }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Edit,
                                                contentDescription = stringResource(R.string.qs_edit_tiles),
                                                tint = iconTint,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(40.dp))
                                    }
                                }
                            }
                        }
                    }

                    Element(Elements.QuickSettingsContent, modifier = Modifier.weight(1f)) {
                        if (alwaysCompose) {
                            LaunchedEffect(Unit) {
                                snapshotFlow { viewModel.isQsFullyCollapsed }
                                    .collect { collapsed ->
                                        if (collapsed) {
                                            scrollState.scrollTo(0)
                                        }
                                    }
                            }
                        } else {
                            DisposableEffect(Unit) {
                                lifecycleScope.launch { scrollState.scrollTo(0) }
                                onDispose { lifecycleScope.launch { scrollState.scrollTo(0) } }
                            }
                        }

                        val scrollModifier = Modifier.verticalScroll(scrollState)

                        val scrollY = scrollState.value
                        val maxScroll = scrollState.maxValue

                        Box(
                            modifier = Modifier.fillMaxSize()
                                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                                .drawWithContent {
                                    drawContent()

                                    val fadeH = 100.dp.toPx()

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
                            var oneUIContainerBounds by remember { mutableStateOf(androidx.compose.ui.geometry.Rect.Zero) }

                            Column(
                                modifier =
                                    Modifier.fillMaxSize()
                                        .onPlaced { coordinates ->
                                            val positionOnScreen = coordinates.positionOnScreen()
                                            val left = positionOnScreen.x
                                            val right = left + coordinates.size.width
                                            val top = positionOnScreen.y
                                            val bottom = top + coordinates.size.height
                                            viewModel.applyNewQsScrollerBounds(
                                                left = left,
                                                top = top,
                                                right = right,
                                                bottom = bottom,
                                            )
                                        }
                                        .offset {
                                            IntOffset(
                                                x = 0,
                                                y = viewModel.qsScrollTranslationY.fastRoundToInt(),
                                            )
                                        }
                                        .onSizeChanged { viewModel.qsScrollHeight = it.height }
                                        .then(scrollModifier)
                                        .padding(bottom = 8.dp)
                                        .sysuiResTag(ResIdTags.qsScroll)
                            ) {
                                val containerViewModel = viewModel.containerViewModel

                                val TileGrid =
                                    @Composable {
                                        GridAnchor()

                                        val collapsedRows = flatLayout
                                            .filterIsInstance<QSLayoutItem.SectionHeader>()
                                            .find { it.type == SectionType.TILES }
                                            ?.heightScale?.toInt() ?: 2

                                        val isDropHover by remember {
                                            derivedStateOf {
                                                com.android.systemui.qs.panels.ui.compose.FloatingTileDragState.isDragging &&
                                                    !com.android.systemui.qs.panels.ui.compose.FloatingTileDragState.draggingTileSpec.bypassesOneUiContainer() &&
                                                    oneUIContainerBounds.contains(
                                                        com.android.systemui.qs.panels.ui.compose.FloatingTileDragState.dragPosition
                                                    )
                                            }
                                        }
                                        val canReorderDropInOneUi by remember {
                                            derivedStateOf {
                                                (FloatingTileDragState.ghostFloatingTile?.spanRows ?: 1) <= 1
                                            }
                                        }

                                        val isFullyExpanded = viewModel.isQsFullyExpanded
                                        val containerExpanded = isTilesExpanded && isFullyExpanded

                                        if (insideTiles.isNotEmpty() || isEditingSections) {
                                            OneUITileContainer(
                                                isExpanded = containerExpanded,
                                                animateSize = isFullyExpanded,
                                                collapsedRows = collapsedRows,
                                                onExpandChange = { isTilesExpanded = it },
                                                onEditClick = {
                                                    viewModel.openOneUiEdit()
                                                },
                                                isEditMode = isEditingSections,
                                                isDropHover = isDropHover,
                                                tileMargin = MainScreenOneUiDefaults.GridSpacing,
                                                cellSizeScale = MainScreenOneUiDefaults.CellSizeScale,
                                                evenEdgeSpacing = true,
                                                modifier = Modifier
                                                    .padding(bottom = 2.dp)
                                                    .onGloballyPositioned { coordinates ->
                                                        val pos = coordinates.positionInRoot()
                                                        oneUIContainerBounds = androidx.compose.ui.geometry.Rect(
                                                            pos.x, pos.y, pos.x + coordinates.size.width, pos.y + coordinates.size.height
                                                        )
                                                    }
                                            ) {
                                                CompositionLocalProvider(
                                                    LocalQSCompactMode provides !containerExpanded,
                                                    LocalCollapsedRows provides collapsedRows
                                                ) {
                                                    OneUiInsideTileArea(
                                                        insideSpecs = insideTiles,
                                                        allTilesMap = insideAllTilesMap,
                                                        isEditMode = false,
                                                        isDropTarget = isEditingSections && !isEditingOneUi &&
                                                            isDropHover && canReorderDropInOneUi,
                                                        containerBounds = oneUIContainerBounds,
                                                        interactionsEnabled = !isEditingSections,
                                                        animateSize = isFullyExpanded,
                                                        onTileClick = { spec, expandable ->
                                                            containerViewModel.tileGridViewModel.onTileClick(spec, expandable)
                                                        },
                                                        onReorder = { specs -> oneUiTileStore.setTiles(specs) },
                                                        onDragOut = { spec ->
                                                            oneUiTileStore.removeTile(spec)
                                                            val spanCols = if (spec.spec == "brightness") 3 else 1
                                                            viewModel.sectionEditModeViewModel.addMainQSTile(
                                                                FloatingTile(spec, SectionType.TILES, spanCols, 1)
                                                            )
                                                            viewModel.containerViewModel.editModeViewModel.addTile(spec)
                                                        },
                                                        gridSpacing = MainScreenOneUiDefaults.GridSpacing,
                                                        cellSizeScale = MainScreenOneUiDefaults.CellSizeScale,
                                                        evenEdgeSpacing = true,
                                                        modifier = Modifier.fillMaxWidth(),
                                                    )
                                                }
                                            }
                                        }
                                    }

                                Box(
                                    modifier =
                                        Modifier.fillMaxWidth()
                                            .scale(editModeScale)
                                            .sysuiResTag(ResIdTags.quickSettingsPanel)
                                            .padding(
                                                top = qsHorizontalMargin(),
                                                start = qsHorizontalMargin(),
                                                end = qsHorizontalMargin(),
                                            )
                                ) {
                                    EditableQuickSettingsLayout(
                                        flatLayout = flatLayout,
                                        sectionEditModeViewModel = viewModel.sectionEditModeViewModel,
                                        isEditMode = isEditingSections,
                                        isEditingOneUi = isEditingOneUi,
                                        scrollState = scrollState,
                                        nonBrightnessAlpha = contentAlpha,
                                        allTilesFlow = viewModel.containerViewModel.tileGridViewModel.tiles,
                                        brightness = {},
                                        tileBrightness = { isVertical ->
                                            BrightnessTileContent(
                                                viewModel = viewModel.containerViewModel.brightnessSliderViewModel,
                                                isVertical = isVertical,
                                                isEditMode = isEditingSections,
                                            )
                                        },
                                        onFloatingTileClick = { spec, expandable ->
                                            viewModel.containerViewModel.tileGridViewModel.onTileClick(spec, expandable)
                                        },
                                        onAddTile = { spec ->
                                            viewModel.containerViewModel.editModeViewModel.addTile(spec)
                                        },
                                        onRemoveTileFromSystem = { spec ->
                                            viewModel.containerViewModel.editModeViewModel.removeTile(spec)
                                        },
                                        tiles = TileGrid,
                                        media = {}, // media lives in QQS only
                                        oneUIContainerBounds = oneUIContainerBounds,
                                        onDropInOneUIContainer = { spec, newOrder ->
                                            val shouldAppendToEnd =
                                                (FloatingTileDragState.ghostFloatingTile?.spanRows ?: 1) > 1
                                            if (newOrder != null && !shouldAppendToEnd) {
                                                oneUiTileStore.setTiles(newOrder)
                                            } else {
                                                oneUiTileStore.addTile(spec)
                                            }
                                            // Ensure the tile leaves the main list backend
                                            viewModel.sectionEditModeViewModel.removeFloatingTile(spec)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isEditingSections) {
                val toolbarAlpha by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = tween(250),
                    label = "toolbar_alpha"
                )
                val toolbarScale by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = tween(250),
                    label = "toolbar_scale"
                )

                val removedSectionHeaders = flatLayout
                    .filterIsInstance<QSLayoutItem.SectionHeader>()
                    .filter { !it.visible && it.type != SectionType.BRIGHTNESS && it.type != SectionType.MEDIA }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 32.dp)
                        .alpha(toolbarAlpha)
                        .scale(toolbarScale),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    AnimatedVisibility(
                        visible = isEditingSections && removedSectionHeaders.isNotEmpty(),
                        enter   = slideInVertically(tween(220)) { it } + fadeIn(tween(220)),
                        exit    = slideOutVertically(tween(180)) { it } + fadeOut(tween(180)),
                    ) {
                        Row(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerHighest,
                                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            removedSectionHeaders.forEach { header ->
                                Surface(
                                    onClick = {
                                        val updated = flatLayout.toMutableList()
                                        val idx = updated.indexOfFirst {
                                            it is QSLayoutItem.SectionHeader && it.type == header.type
                                        }
                                        if (idx != -1) {
                                            updated[idx] = (updated[idx] as QSLayoutItem.SectionHeader)
                                                .copy(visible = true)
                                            viewModel.sectionEditModeViewModel.setFlatLayout(updated)
                                        }
                                    },
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shadowElevation = 2.dp,
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Text(
                                            text = header.type.name
                                                .lowercase()
                                                .replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = "Restore",
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shadowElevation = 4.dp,
                        modifier = Modifier.height(64.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.openTilePicker() },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.accessibility_menu))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Editing Layout",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.sectionEditModeViewModel.stopEditingSections() },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = "Done")
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BrightnessSlider(
        viewModel: QSFragmentComposeViewModel,
        layoutState: SceneTransitionLayoutState,
    ) {
        Box(
            Modifier.systemGestureExclusionInShade(
                enabled = {
                    /*
                     * While we are transitioning into QS (either from QQS
                     * or from gone), the global position of the brightness
                     * slider will change in every frame. This causes
                     * the modifier to send a new gesture exclusion
                     * rectangle on every frame. Instead, only apply the
                     * modifier when this is settled.
                     */
                    layoutState.transitionState is TransitionState.Idle &&
                        viewModel.isNotTransitioning
                }
            )
        ) {
            val brightnessViewModel = viewModel.containerViewModel.brightnessSliderViewModel
            val isDraggingBrightness = brightnessViewModel.showMirror
            AlwaysDarkMode {
                Box(Modifier.graphicsLayer { 
                    if (isDraggingBrightness) alpha = 1f 
                }) {
                    BrightnessSliderContainer(
                        viewModel =
                            viewModel.containerViewModel.brightnessSliderViewModel,
                        containerColors =
                            ContainerColors(
                                Color.Transparent,
                                ContainerColors.defaultContainerColor,
                            ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }

    @Composable
    private fun EditModeElement(modifier: Modifier = Modifier) {
        // No need for top padding, the Scaffold inside takes care of the correct insets
        val dragHandler: (TileSpec) -> Unit = { spec ->
            viewModel.sectionEditModeViewModel.addFloatingTile(
                FloatingTile(spec, SectionType.TILES, 1, 1),
                SectionType.TILES
            )
            viewModel.containerViewModel.editModeViewModel.removeTile(spec)
            viewModel.containerViewModel.editModeViewModel.stopEditing()
        }

        CompositionLocalProvider(
            LocalTileDragHandler provides dragHandler
        ) {
            val horizontalMargin = qsHorizontalMargin()
            EditMode(
                viewModel = viewModel.containerViewModel.editModeViewModel,
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalMargin)
                        .padding(top = { viewModel.qqsHeaderHeight }),
            )
        }
    }

    private fun Modifier.collapseExpandSemanticAction(label: String): Modifier {
        return viewModel.collapseExpandAccessibilityAction?.let {
            semantics {
                customActions =
                    listOf(
                        CustomAccessibilityAction(label) {
                            it.run()
                            true
                        }
                    )
            }
        } ?: this
    }

    private fun registerDumpable() {
        val instanceId = instanceProvider.getNextId()
        // Add an instanceId because the system may have more than 1 of these when re-inflating and
        // DumpManager doesn't like repeated identifiers. Also, put it first because DumpHandler
        // matches by end.
        val stringId = "$instanceId-QSFragmentCompose"
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                try {
                    dumpManager.registerNormalDumpable(stringId, this@QSFragmentCompose)
                    awaitCancellation()
                } finally {
                    dumpManager.unregisterDumpable(stringId)
                }
            }
        }
    }

    private val clipData
        get() = containerView?.clipData

    override fun dump(pw: PrintWriter, args: Array<out String>) {
        pw.asIndenting().run {
            printSection("NotificationScrimClippingParams") {
                println("isEnabled", clipData?.first)
                println("params", clipData?.second)
            }
            printSection("QQS positioning") {
                println("qqsHeight", "${headerHeight}px")
                println("qqsTop", "${headerTop}px")
                println("qqsBottom", "${headerBottom}px")
                println("qqsLeft", "${headerLeft}px")
                println("qqsPositionOnRoot", qqsPositionOnRoot)
                val rect = Rect()
                getHeaderBoundsOnScreen(rect)
                println("qqsPositionOnScreen", rect)
            }
            println("QQS visible", qqsVisible.value)
            println("bottom QS padding", bottomContentPadding)
            if (::viewModel.isInitialized) {
                printSection("View Model") { viewModel.dump(this@run, args) }
            }
        }
    }
}

private suspend inline fun <Listener : Any, Data> setListenerJob(
    listenerFlow: MutableStateFlow<Listener?>,
    dataFlow: Flow<Data>,
    crossinline onCollect: suspend Listener.(Data) -> Unit,
) {
    coroutineScope {
        try {
            listenerFlow.collectLatest { listenerOrNull ->
                listenerOrNull?.let { currentListener ->
                    launch {
                        // Called when editing mode changes
                        dataFlow.collect { currentListener.onCollect(it) }
                    }
                }
            }
            awaitCancellation()
        } finally {
            listenerFlow.value = null
        }
    }
}

private val instanceProvider =
    object {
        private var currentId = 0

        fun getNextId(): Int {
            return currentId++
        }
    }

object SceneKeys {
    val QuickQuickSettings = SceneKey("QuickQuickSettingsScene")
    val QuickSettings = SceneKey("QuickSettingsScene")
    val EditMode = SceneKey("EditModeScene")
    val TilePicker = SceneKey("TilePickerScene")

    val QqsBrightnessSlider = ElementKey("QqsBrightnessSlider")
    val QsBrightnessSlider = ElementKey("QsBrightnessSlider")

    val QuickQuickSettingsContent = ElementKey("QuickQuickSettingsContent")

    val TransitionState.Transition.debugName: String
        get() = "[from=${fromContent.debugName}, to=${toContent.debugName}]"

    fun QSFragmentComposeViewModel.QSExpansionState.toIdleSceneKey(): SceneKey {
        return when {
            progress < 0.5f -> QuickQuickSettings
            else -> QuickSettings
        }
    }

    val QqsTileElementMatcher =
        object : ElementMatcher {
            override fun matches(key: ElementKey, content: ContentKey): Boolean {
                return content == SceneKeys.QuickQuickSettings &&
                    Elements.TileElementMatcher.matches(key, content)
            }
        }
}

private suspend fun synchronizeQsState(
    state: MutableSceneTransitionLayoutState,
    editMode: Flow<Boolean>,
    sectionEditMode: Flow<Boolean>,
    showTilePicker: Flow<Boolean>,
    isEditingOneUi: Flow<Boolean>,
    expansion: Flow<Float>,
) {
    coroutineScope {
        val animationScope = this

        var currentTransition: ExpansionTransition? = null

        fun snapTo(scene: SceneKey) {
            state.snapTo(scene)
            currentTransition = null
        }

        combine(editMode, sectionEditMode, showTilePicker, isEditingOneUi, expansion) { isEditMode, isSectionEditMode, isTilePicker, isOneUiEdit, progress ->
            class State(val isEditMode: Boolean, val isSectionEditMode: Boolean, val isTilePicker: Boolean, val isOneUiEdit: Boolean, val progress: Float)
            State(isEditMode, isSectionEditMode, isTilePicker, isOneUiEdit, progress)
        }.collectLatest { data ->

            if (!data.isTilePicker && !data.isOneUiEdit && data.isEditMode && state.currentScene != SceneKeys.EditMode) {
                state.setTargetScene(SceneKeys.EditMode, animationScope)?.second?.join()
            } else if (!data.isTilePicker && !data.isOneUiEdit && !data.isEditMode) {
                if (state.currentScene == SceneKeys.EditMode) {
                    state.setTargetScene(SceneKeys.QuickSettings, animationScope)?.second?.join()
                }
            }

            if (!data.isEditMode && !data.isSectionEditMode && !data.isTilePicker && !data.isOneUiEdit) {
                when (data.progress) {
                    0f -> snapTo(QuickQuickSettings)
                    1f -> snapTo(QuickSettings)
                    else -> {
                        val transition = currentTransition
                        if (transition != null) {
                            transition.progress = data.progress
                            return@collectLatest
                        }

                        val newTransition =
                            ExpansionTransition(data.progress).also { currentTransition = it }
                        state.startTransitionImmediately(
                            animationScope = animationScope,
                            transition = newTransition,
                        )
                    }
                }
            }
        }
    }
}

private class ExpansionTransition(currentProgress: Float) :
    TransitionState.Transition.ChangeScene(
        fromScene = QuickQuickSettings,
        toScene = QuickSettings,
    ) {
    override val currentScene: SceneKey
        get() {
            // This should return the logical scene. If the QS STLState is only driven by
            // synchronizeQSState() then it probably does not matter which one we return, this is
            // only used to compute the current user actions of a STL.
            return QuickQuickSettings
        }

    override var progress: Float by mutableFloatStateOf(currentProgress)

    override val progressVelocity: Float
        get() = 0f

    override val isInitiatedByUserInput: Boolean
        get() = true

    override val isUserInputOngoing: Boolean
        get() = true

    private val finishCompletable = CompletableDeferred<Unit>()

    override suspend fun run() {
        // This transition runs until it is interrupted by another one.
        finishCompletable.await()
    }

    override fun freezeAndAnimateToCurrentState() {
        finishCompletable.complete(Unit)
    }
}

private const val EDIT_MODE_TIME_MILLIS = 500

/**
 * Performs different touch handling based on the state of the ComposeView:
 * * Ignore touches below the value returned by [clipData.second.top], when clipping is enabled, as
 *   per [clipData.first].
 * * Intercept touches that would overscroll QS forward and instead allow them to be used to close
 *   the shade.
 * * Ignore touches in [isInBottomReservedArea] (bottom area when editing). This allows the shade to
 *   close on bottom swipes when editing when using gesture nav.
 */
private class FrameLayoutTouchPassthrough(
    context: Context,
    private val canScrollQs: CanScrollQs,
    private val emitMotionEventForFalsing: () -> Unit,
    private val logBuffer: TableLogBuffer,
    private val backgroundDispatcher: CoroutineDispatcher,
    private val isInBottomReservedArea: (Float, Float) -> Boolean,
    private val isInEditMode: () -> Boolean,
    private val isInSectionEditMode: () -> Boolean,
    private val isInTilePickerScene: () -> Boolean,
) : FrameLayout(context) {

    private val lastConfig = Configuration(context.resources.configuration)

    init {
        repeatWhenAttached {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launchTraced("FrameLayoutTouchPassthrough.logs", backgroundDispatcher) {
                    _clipData
                        .pairwise(initialValue = false to NotificationScrimClipParams())
                        .collect { (prev, new) ->
                            logBuffer.logDiffs(
                                columnPrefix = PREFIX_PARAMS,
                                prevVal = prev.second,
                                newVal = new.second,
                            )
                            if (prev.first != new.first) {
                                logBuffer.logChange(
                                    columnName = COL_CLIP_ENABLED,
                                    value = new.first,
                                    isInitial = false,
                                )
                            }
                        }
                }
            }
        }
    }

    private val currentClippingPath = Path()

    private val _clipData = MutableStateFlow(false to NotificationScrimClipParams())

    // [first] is enabled and [second] is the clipping params
    var clipData
        get() = _clipData.value
        set(value) {
            if (_clipData.value != value) {
                _clipData.value = value
                dirtyClipData = true
                invalidate()
            }
        }

    var qsVisible: Boolean = false
        set(value) {
            if (value != field) {
                field = value
                invalidate()
            }
        }

    private var dirtyClipData = false

    private val clipEnabled
        get() = clipData.first

    private val clipParams
        get() = clipData.second

    private fun updateClippingPath() {
        currentClippingPath.rewind()
        val (clipEnabled, clipParams) = clipData
        if (clipEnabled) {
            val right = width + clipParams.rightInset
            val left = -clipParams.leftInset
            val top = clipParams.top
            val bottom = clipParams.bottom
            currentClippingPath.addRoundRect(
                left.toFloat(),
                top.toFloat(),
                right.toFloat(),
                bottom.toFloat(),
                clipParams.radius.toFloat(),
                clipParams.radius.toFloat(),
                Path.Direction.CW,
            )
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (dirtyClipData) {
            dirtyClipData = false
            updateClippingPath()
        }
        if (!currentClippingPath.isEmpty) {
            canvas.translate(0f, -translationY)
            canvas.clipOutPath(currentClippingPath)
            canvas.translate(0f, translationY)
        }
        if (qsVisible) {
            // If QS should not be visible, there's no need to draw this tree at all. We do this
            // in the view (instead of in compose) so it's completely synchronized with the clip.
            // As this FrameLayout doesn't have any content, and the ComposeView is the only child,
            // this is equivalent to blocking the draw in `drawChild`.
            super.dispatchDraw(canvas)
        }
    }

    override fun isTransformedTouchPointInView(
        x: Float,
        y: Float,
        child: View?,
        outLocalPoint: PointF?,
    ): Boolean {
        return if (clipEnabled && y + translationY > clipParams.top) {
            false
        } else if (isInBottomReservedArea(x, y)) { // no translation as it's relative to root
            false
        } else {
            super.isTransformedTouchPointInView(x, y, child, outLocalPoint)
        }
    }

    val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    var downY = 0f
    var downX = 0f
    var preventingIntercept = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                preventingIntercept = false
                val isEditing = isInEditMode() || isInSectionEditMode() || isInTilePickerScene()
                if (canScrollQs.forward() || isEditing) {
                    // If we can scroll down, make sure we're not intercepted by the parent
                    preventingIntercept = true
                    parent?.requestDisallowInterceptTouchEvent(true)
                } else if (!canScrollQs.backward() && !isEditing) {
                    // Don't pass on the touch to the view, because scrolling will unconditionally
                    // disallow interception even if we can't scroll.
                    // if a user can't scroll at all, we should never listen to the touch.
                    return false
                }
            }
            MotionEvent.ACTION_UP -> {
                if (preventingIntercept) {
                    emitMotionEventForFalsing()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun dispatchConfigurationChanged(newConfig: Configuration) {
        if (lastConfig.updateFrom(newConfig) != 0) {
            super.dispatchConfigurationChanged(newConfig)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // If there's a touch on this view and we can scroll down, we don't want to be intercepted
        val action = ev.actionMasked

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                preventingIntercept = false
                val isEditing = isInEditMode() || isInSectionEditMode() || isInTilePickerScene()
                if (canScrollQs.forward() || isEditing) {
                    // If we can scroll down, make sure none of our parents intercepts us.
                    preventingIntercept = true
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
                downY = ev.y
                downX = ev.x
            }

            MotionEvent.ACTION_MOVE -> {
                val y = ev.y
                val x = ev.x
                val yDiff = y - downY
                val xDiff = x - downX
                val collapsing = yDiff < -touchSlop && !canScrollQs.forward()
                val vertical = Math.abs(xDiff) < Math.abs(yDiff)

                if (collapsing && vertical && !isInEditMode() && !isInSectionEditMode() && !isInTilePickerScene()) {
                    return true
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    private companion object {
        const val COL_CLIP_ENABLED = "enabled"
        const val PREFIX_PARAMS = "params"
    }
}

private interface CanScrollQs {
    fun forward(): Boolean

    fun backward(): Boolean
}

@Composable
private fun rememberQsBrightnessSettings(): QsBrightnessSettings {
    val context = LocalContext.current
    val cr = remember { context.contentResolver }

    fun readCurrent(): QsBrightnessSettings {
        val position = runCatching {
            LineageSettings.Secure.getIntForUser(
                cr, LineageSettings.Secure.QS_BRIGHTNESS_SLIDER_POSITION,
                0, UserHandle.USER_CURRENT
            )
        }.getOrElse { 0 }

        val showSliderValue = runCatching {
            LineageSettings.Secure.getIntForUser(
                cr, LineageSettings.Secure.QS_SHOW_BRIGHTNESS_SLIDER,
                1, UserHandle.USER_CURRENT
            )
        }.getOrElse { 1 }

        return QsBrightnessSettings(
            sliderAtTop = position == 0,
            showSlider = showSliderValue,
        )
    }

    var state by remember {
        mutableStateOf(readCurrent())
    }

    DisposableEffect(Unit) {
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                context.mainExecutor.execute {
                    state = readCurrent()
                }
            }
        }

        cr.registerContentObserver(
            LineageSettings.Secure.getUriFor(LineageSettings.Secure.QS_BRIGHTNESS_SLIDER_POSITION),
            false, observer, UserHandle.USER_ALL
        )
        cr.registerContentObserver(
            LineageSettings.Secure.getUriFor(LineageSettings.Secure.QS_SHOW_BRIGHTNESS_SLIDER),
            false, observer, UserHandle.USER_ALL
        )

        onDispose {
            cr.unregisterContentObserver(observer)
        }
    }

    return state
}

private const val QQS_SHOW_BRIGHTNESS_SLIDER = "qqs_show_brightness_slider"
private const val QQS_SHOW_MEDIA = "qqs_show_media"

private data class QqsContentSettings(
    val showBrightness: Boolean,
    val showMedia: Boolean,
)

@Composable
private fun rememberQqsContentSettings(): QqsContentSettings {
    val context = LocalContext.current
    val cr = remember { context.contentResolver }

    fun readFlag(key: String): Boolean =
        Settings.System.getIntForUser(cr, key, 1, UserHandle.USER_CURRENT) != 0

    fun readCurrent() =
        QqsContentSettings(
            showBrightness = readFlag(QQS_SHOW_BRIGHTNESS_SLIDER),
            showMedia = readFlag(QQS_SHOW_MEDIA),
        )

    var state by remember { mutableStateOf(readCurrent()) }

    DisposableEffect(Unit) {
        val observer =
            object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean) {
                    context.mainExecutor.execute { state = readCurrent() }
                }
            }
        listOf(QQS_SHOW_BRIGHTNESS_SLIDER, QQS_SHOW_MEDIA).forEach {
            cr.registerContentObserver(
                Settings.System.getUriFor(it),
                false,
                observer,
                UserHandle.USER_ALL,
            )
        }
        onDispose { cr.unregisterContentObserver(observer) }
    }

    return state
}

private object ResIdTags {
    const val quickSettingsPanel = "quick_settings_panel"
    const val quickQsPanel = "quick_qs_panel"
    const val qsScroll = "expanded_qs_scroll_view"
    const val qsFooterActions = "qs_footer_actions"
}

private object MainScreenOneUiDefaults {
    val GridSpacing = 20.dp

    const val CellSizeScale = 0.93f
}

@Composable private fun qsHorizontalMargin() = dimensionResource(id = R.dimen.qs_horizontal_margin)

@Composable
private fun interactionsConfig() =
    InteractionsConfig(
        hoverOverlayColor = MaterialTheme.colorScheme.onSurface,
        hoverOverlayAlpha = 0.11f,
        pressedOverlayColor = MaterialTheme.colorScheme.onSurface,
        pressedOverlayAlpha = 0.15f,
        // we are OK using this as our content is clipped and all corner radius are larger than this
        surfaceCornerRadius = 16.dp,
    )

private inline val alwaysCompose
    get() = false

/**
 * Forces the configuration and themes to be dark theme. This is needed in order to have
 * [colorResource] retrieve the dark mode colors.
 *
 * This should be removed when [notificationShadeBlur] is removed
 */
@Composable
private fun AlwaysDarkMode(content: @Composable () -> Unit) {
    if (notificationShadeBlur()) {
        content()
    } else {
        val currentConfig = LocalConfiguration.current
        val darkConfig =
            Configuration(currentConfig).apply {
                uiMode =
                    (uiMode and (Configuration.UI_MODE_NIGHT_MASK.inv())) or
                        Configuration.UI_MODE_NIGHT_YES
            }
        val newContext = LocalContext.current.createConfigurationContext(darkConfig)
        CompositionLocalProvider(
            LocalConfiguration provides darkConfig,
            LocalContext provides newContext,
        ) {
            content()
        }
    }
}

private data class QsBrightnessSettings(
    val sliderAtTop: Boolean,
    val showSlider: Int,
)
