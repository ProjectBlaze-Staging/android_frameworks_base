/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.android.systemui.qs.footer.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.compose.animation.scene.ContentScope
import com.android.compose.lifecycle.LaunchedEffectWithLifecycle
import com.android.compose.theme.LocalAndroidColorScheme
import com.android.compose.theme.colorAttr
import com.android.systemui.Flags.notificationShadeBlur
import com.android.systemui.animation.Expandable
import com.android.systemui.common.shared.model.Icon
import com.android.systemui.common.ui.compose.load
import com.android.systemui.compose.modifiers.sysuiResTag
import com.android.systemui.qs.footer.ui.viewmodel.FooterActionsButtonViewModel
import com.android.systemui.qs.footer.ui.viewmodel.FooterActionsForegroundServicesButtonViewModel
import com.android.systemui.qs.footer.ui.viewmodel.FooterActionsSecurityButtonViewModel
import com.android.systemui.qs.footer.ui.viewmodel.FooterActionsViewModel
import com.android.systemui.qs.shared.ui.QuickSettings
import com.android.systemui.qs.ui.composable.QuickSettingsTheme
import com.android.systemui.qs.ui.compose.borderOnFocus
import com.android.systemui.res.R
import kotlinx.coroutines.launch

@Composable
fun ContentScope.FooterActionsWithAnimatedVisibility(
    viewModel: FooterActionsViewModel,
    isCustomizing: Boolean,
    customizingAnimationDuration: Int,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = !isCustomizing,
        enter =
            expandVertically(
                animationSpec = tween(customizingAnimationDuration),
                initialHeight = { 0 },
            ) + fadeIn(tween(customizingAnimationDuration)),
        exit =
            shrinkVertically(
                animationSpec = tween(customizingAnimationDuration),
                targetHeight = { 0 },
            ) + fadeOut(tween(customizingAnimationDuration)),
        modifier = modifier.fillMaxWidth(),
    ) {
        QuickSettingsTheme {
            // This view has its own horizontal padding
            // TODO(b/321716470) This should use a lifecycle tied to the scene.
            Element(QuickSettings.Elements.FooterActions, Modifier) {
                FooterActions(viewModel = viewModel)
            }
        }
    }
}

/** The Quick Settings footer actions row. */
@Composable
fun FooterActions(viewModel: FooterActionsViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Collect alpha as soon as we are composed, even when not visible.
    val alpha by viewModel.alpha.collectAsStateWithLifecycle()

    var security by remember { mutableStateOf<FooterActionsSecurityButtonViewModel?>(null) }
    var foregroundServices by remember {
        mutableStateOf<FooterActionsForegroundServicesButtonViewModel?>(null)
    }
    var userSwitcher by remember { mutableStateOf<FooterActionsButtonViewModel?>(null) }
    var settings by remember { mutableStateOf<FooterActionsButtonViewModel?>(null) }

    LaunchedEffect(context, viewModel) {
        launch {
            // Listen for dialog requests as soon as we are composed, even when not visible.
            viewModel.observeDeviceMonitoringDialogRequests(context)
        }
    }

    // Listen for model changes only when QS are visible.
    LaunchedEffectWithLifecycle(
        viewModel.security,
        viewModel.foregroundServices,
        viewModel.userSwitcher,
        viewModel.settings,
        minActiveState = Lifecycle.State.RESUMED,
    ) {
        launch { viewModel.security.collect { security = it } }
        launch { viewModel.foregroundServices.collect { foregroundServices = it } }
        launch { viewModel.userSwitcher.collect { userSwitcher = it } }
        launch { viewModel.settings.collect { settings = it } }
    }

    val contentColor = MaterialTheme.colorScheme.onSurface
    Row(
        modifier.graphicsLayer { this.alpha = alpha },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            val useModifierBasedExpandable = true

            // Foreground services number button (icon + count badge)
            ForegroundServicesNumberButton(
                { foregroundServices },
                useModifierBasedExpandable,
            )

            // Security button as icon-only (VPN, device management, etc.)
            SecurityIconButton(
                { security },
                useModifierBasedExpandable,
                Modifier.sysuiResTag("security_button"),
            )

            IconButton(
                { userSwitcher },
                useModifierBasedExpandable,
                Modifier.sysuiResTag("multi_user_switch"),
            )
            IconButton(
                { settings },
                useModifierBasedExpandable,
                Modifier.sysuiResTag("settings_button_container"),
            )
            IconButton(
                { viewModel.power },
                useModifierBasedExpandable,
                Modifier.sysuiResTag("pm_lite"),
            )
        }
    }
}

/**
 * The foreground services button in number format.
 *
 * The visibility of this button is animated.
 */
@Composable
private fun ForegroundServicesNumberButton(
    model: () -> FooterActionsForegroundServicesButtonViewModel?,
    useModifierBasedExpandable: Boolean,
) {
    val viewModel = model()
    val alpha = if (viewModel == null) 0f else 1f

    viewModel?.let {
        val onClick: (Expandable) -> Unit =
            it.onClick.let { onClick ->
                val context = LocalContext.current
                { expandable -> onClick(context, expandable) }
            }

        NumberButton(
            it.model.foregroundServicesCount,
            contentDescription = it.model.text,
            showNewDot = it.model.hasNewChanges,
            onClick = onClick,
            useModifierBasedExpandable,
            modifier = Modifier.graphicsLayer { this.alpha = alpha },
        )
    }
}

/** A button with an icon. */
@Composable
private fun IconButton(
    model: () -> FooterActionsButtonViewModel?,
    useModifierBasedExpandable: Boolean,
    modifier: Modifier = Modifier,
) {
    val viewModel = model() ?: return
    IconButton(viewModel, useModifierBasedExpandable, modifier)
}

/** A button with an icon. */
@Composable
private fun IconButton(
    model: FooterActionsButtonViewModel,
    useModifierBasedExpandable: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = buttonColorsForModel(model)
    CircleExpandable(
        color = colors.background,
        onClick = model.onClick,
        modifier = modifier,
        useModifierBasedImplementation = useModifierBasedExpandable,
    ) {
        FooterIcon(model.icon, Modifier.size(20.dp), colors.icon)
    }
}

/** Security button with icon only (VPN, device management, etc.) */
@Composable
private fun SecurityIconButton(
    model: () -> FooterActionsSecurityButtonViewModel?,
    useModifierBasedExpandable: Boolean,
    modifier: Modifier = Modifier,
) {
    val viewModel = model() ?: return
    val context = LocalContext.current
    val colors = textButtonColors()

    CircleExpandable(
        color = colors.background,
        onClick = { expandable ->
            viewModel.onClick?.invoke(context, expandable)
        },
        modifier = modifier,
        useModifierBasedImplementation = useModifierBasedExpandable,
    ) {
        androidx.compose.material3.Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = colors.content,
        )
    }
}

// TODO(b/394738023): Use com.android.systemui.common.ui.compose.Icon instead
@Composable
private fun FooterIcon(icon: Icon, modifier: Modifier = Modifier, tint: Color) {
    val contentDescription = icon.contentDescription?.load()
    when (icon) {
        is Icon.Loaded -> {
            // Convert only when the drawable changes, not on every recomposition.
            val bitmap = remember(icon.drawable) { icon.drawable.toBitmap().asImageBitmap() }
            androidx.compose.material3.Icon(bitmap, contentDescription, modifier, tint = tint)
        }
        is Icon.Resource -> androidx.compose.material3.Icon(painterResource(icon.resId), contentDescription, modifier, tint = tint)
    }
}

/** A button with a number and an optional dot (to indicate new changes). */
@Composable
private fun NumberButton(
    number: Int,
    contentDescription: String,
    showNewDot: Boolean,
    onClick: (Expandable) -> Unit,
    useModifierBasedExpandable: Boolean,
    modifier: Modifier = Modifier,
) {
    // By default Expandable will show a ripple above its content when clicked, and clip the content
    // with the shape of the expandable. In this case we also want to show a "new changes dot"
    // outside of the shape, so we can't clip. To work around that we can pass our own interaction
    // source and draw the ripple indication ourselves above the text but below the "new changes
    // dot".
    val interactionSource = remember { MutableInteractionSource() }

    val colors = numberButtonColors()
    CircleExpandable(
        color = colors.background,
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier,
        useModifierBasedImplementation = useModifierBasedExpandable,
    ) {
        Box(Modifier.size(FooterActionsDefaults.FooterButtonHeight)) {
            Box(
                Modifier.fillMaxSize()
                    .clip(CircleShape)
                    .indication(interactionSource, LocalIndication.current)
            ) {
                Text(
                    number.toString(),
                    modifier =
                        Modifier.align(Alignment.Center).semantics {
                            this.contentDescription = contentDescription
                        },
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.content,
                    // TODO(b/242040009): This should only use a standard text style instead and
                    // should not override the text size.
                    fontSize = 18.sp,
                )
            }

            if (showNewDot) {
                NewChangesDot(Modifier.align(Alignment.BottomEnd))
            }
        }
    }
}

@Composable
private fun CircleExpandable(
    color: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = contentColorFor(color),
    borderStroke: BorderStroke? = null,
    onClick: ((Expandable) -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
    useModifierBasedImplementation: Boolean,
    content: @Composable (Expandable) -> Unit,
) {
    com.android.compose.animation.Expandable(
        color = color,
        contentColor = contentColor,
        borderStroke = borderStroke,
        shape = CircleShape,
        onClick = onClick,
        interactionSource = interactionSource,
        modifier =
            modifier.borderOnFocus(
                color = MaterialTheme.colorScheme.secondary,
                cornerSize = CornerSize(percent = 50),
            ),
        useModifierBasedImplementation = useModifierBasedImplementation,
        content = content,
    )
}

/** A dot that indicates new changes. */
@Composable
private fun NewChangesDot(modifier: Modifier = Modifier) {
    val contentDescription = stringResource(R.string.fgs_dot_content_description)
    val color = MaterialTheme.colorScheme.tertiary

    Canvas(modifier.size(12.dp).semantics { this.contentDescription = contentDescription }) {
        drawCircle(color)
    }
}

@Composable
@ReadOnlyComposable
private fun textButtonColors(): TextButtonColors {
    return if (notificationShadeBlur()) {
        FooterActionsDefaults.blurTextButtonColors()
    } else {
        FooterActionsDefaults.textButtonColors()
    }
}

@Composable
@ReadOnlyComposable
private fun numberButtonColors(): TextButtonColors {
    return if (notificationShadeBlur()) {
        FooterActionsDefaults.blurTextButtonColors()
    } else {
        FooterActionsDefaults.numberButtonColors()
    }
}

@Composable
@ReadOnlyComposable
private fun buttonColorsForModel(footerAction: FooterActionsButtonViewModel): ButtonColors {
    return if (notificationShadeBlur()) {
        when (footerAction) {
            is FooterActionsButtonViewModel.PowerActionViewModel ->
                FooterActionsDefaults.inactiveButtonColors() // Same as settings
            is FooterActionsButtonViewModel.SettingsActionViewModel ->
                FooterActionsDefaults.inactiveButtonColors()
            is FooterActionsButtonViewModel.UserSwitcherViewModel ->
                FooterActionsDefaults.userSwitcherButtonColors()
        }
    } else {
        ButtonColors(
            icon = footerAction.iconTintFallback?.let { Color(it) } ?: Color.Unspecified,
            background = colorAttr(footerAction.backgroundColorFallback),
        )
    }
}

private data class ButtonColors(val icon: Color, val background: Color)

private data class TextButtonColors(
    val content: Color,
    val background: Color,
    val border: BorderStroke?,
)

private object FooterActionsDefaults {
    val FooterButtonHeight = 40.dp

    @Composable
    @ReadOnlyComposable
    fun activeButtonColors(): ButtonColors =
        ButtonColors(
            icon = MaterialTheme.colorScheme.onPrimary,
            background = MaterialTheme.colorScheme.primary,
        )

    @Composable
    @ReadOnlyComposable
    fun inactiveButtonColors(): ButtonColors =
        ButtonColors(
            icon = MaterialTheme.colorScheme.onSurface,
            background = LocalAndroidColorScheme.current.surfaceEffect1,
        )

    @Composable
    @ReadOnlyComposable
    fun userSwitcherButtonColors(): ButtonColors =
        ButtonColors(
            icon = Color.Unspecified,
            background = LocalAndroidColorScheme.current.surfaceEffect1,
        )

    @Composable
    @ReadOnlyComposable
    fun blurTextButtonColors(): TextButtonColors =
        TextButtonColors(
            content = MaterialTheme.colorScheme.onSurface,
            background = LocalAndroidColorScheme.current.surfaceEffect1,
            border = null,
        )

    @Composable
    @ReadOnlyComposable
    fun textButtonColors(): TextButtonColors =
        TextButtonColors(
            content = colorAttr(R.attr.onShadeInactiveVariant),
            background = colorAttr(R.attr.underSurface),
            border = BorderStroke(1.dp, colorAttr(R.attr.shadeInactive)),
        )

    @Composable
    @ReadOnlyComposable
    fun numberButtonColors(): TextButtonColors =
        TextButtonColors(
            content = colorAttr(R.attr.onShadeInactiveVariant),
            background = colorAttr(R.attr.shadeInactive),
            border = null,
        )
}
