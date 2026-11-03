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

package com.android.systemui.qs.external.ui.dialog

import android.content.Context
import android.content.DialogInterface.BUTTON_NEGATIVE
import android.content.DialogInterface.BUTTON_POSITIVE
import android.content.DialogInterface.OnClickListener
import android.content.DialogInterface.OnMultiChoiceClickListener
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.compose.PlatformButton
import com.android.compose.PlatformOutlinedButton
import com.android.compose.theme.PlatformTheme
import com.android.systemui.Flags
import com.android.systemui.common.shared.model.Icon
import com.android.compose.dialog.AlertDialogContent
import com.android.systemui.lifecycle.rememberViewModel
import com.android.systemui.qs.external.TileData
import com.android.systemui.qs.external.ui.viewmodel.TileRequestDialogViewModel
import com.android.systemui.qs.panels.ui.compose.infinitegrid.CommonTileDefaults.TileHeight
import com.android.systemui.qs.panels.ui.compose.infinitegrid.LargeTileContent
import com.android.systemui.qs.panels.ui.compose.infinitegrid.TileColors
import com.android.systemui.qs.tileimpl.QSTileImpl
import com.android.systemui.res.R
import com.android.systemui.statusbar.phone.SystemUIDialog
import com.android.systemui.statusbar.phone.SystemUIDialogFactory
import com.android.systemui.statusbar.phone.create
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class TileRequestDialogDelegate
@AssistedInject
constructor(
    private val sysuiDialogFactory: SystemUIDialogFactory,
    private val tileRequestDialogViewModelFactory: TileRequestDialogViewModel.Factory,
    @Assisted private val tileData: TileData,
    @Assisted private val dialogListener: OnMultiChoiceClickListener,
) : SystemUIDialog.Delegate {

    override fun createDialog(): SystemUIDialog {
        return sysuiDialogFactory
            .create { TileRequestDialogContent(it) }
            .apply {
                window?.attributes?.accessibilityTitle =
                    context.getString(R.string.qs_tile_request_dialog_title)
            }
    }

    @Composable
    private fun TileRequestDialogContent(dialog: SystemUIDialog) {
        PlatformTheme {
            val selectedLargeFormat = remember { mutableStateOf(false) }
            AlertDialogContent(
                title = {
                    if (Flags.qsSizesInTileRequestDialog()) {
                        Text(text = stringResource(R.string.qs_tile_request_dialog_title))
                    }
                },
                content = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = spacedBy(16.dp),
                    ) {
                        val viewModel =
                            rememberViewModel(traceName = "TileRequestDialog", key = tileData) {
                                tileRequestDialogViewModelFactory.create(dialog.context, tileData)
                            }

                        val bodyResourceId =
                            if (Flags.qsSizesInTileRequestDialog()) {
                                R.string.qs_tile_request_dialog_text_with_size
                            } else {
                                R.string.qs_tile_request_dialog_text
                            }
                        Text(
                            text = stringResource(bodyResourceId, tileData.appName),
                            textAlign = TextAlign.Start,
                        )

                        DialogTile(
                            uiState = viewModel.uiState,
                            iconProvider = viewModel.iconProvider,
                            context = dialog.context,
                            modifier =
                                Modifier.width(
                                    dimensionResource(
                                        id = R.dimen.qs_tile_service_request_tile_width
                                    )
                                ),
                        )
                    }
                },
                positiveButton = {
                    PlatformButton(
                        onClick = {
                            dialogListener.onClick(
                                dialog,
                                BUTTON_POSITIVE,
                                selectedLargeFormat.value,
                            )
                            dialog.dismiss()
                        }
                    ) {
                        Text(stringResource(R.string.qs_tile_request_dialog_add))
                    }
                },
                negativeButton = {
                    PlatformOutlinedButton(
                        onClick = {
                            dialogListener.onClick(
                                dialog,
                                BUTTON_NEGATIVE,
                                selectedLargeFormat.value,
                            )
                            dialog.dismiss()
                        }
                    ) {
                        Text(stringResource(R.string.qs_tile_request_dialog_not_add))
                    }
                },
            )
        }
    }

    @Composable
    private fun DialogTile(
        uiState: com.android.systemui.qs.panels.ui.viewmodel.TileUiState,
        iconProvider: com.android.systemui.qs.panels.ui.viewmodel.IconProvider,
        context: Context,
        modifier: Modifier = Modifier,
    ) {
        val colors = TileColors(
            background = MaterialTheme.colorScheme.surfaceContainerHigh,
            iconBackground = MaterialTheme.colorScheme.surfaceContainerHighest,
            label = MaterialTheme.colorScheme.onSurface,
            secondaryLabel = MaterialTheme.colorScheme.onSurfaceVariant,
            icon = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Box(
            modifier
                .clip(RoundedCornerShape(24.dp))
                .background(colors.background)
                .height(TileHeight)
        ) {
            val iconProviderContext: Context.() -> Icon = { 
                iconProvider.icon?.let {
                    if (it is QSTileImpl.ResourceIcon) {
                        Icon.Resource(it.resId, null)
                    } else {
                        Icon.Loaded(it.getDrawable(context), null)
                    }
                } ?: Icon.Resource(R.drawable.ic_error_outline, null)
            }
            
            LargeTileContent(
                label = uiState.label,
                secondaryLabel = uiState.secondaryLabel,
                iconProvider = iconProviderContext,
                sideDrawable = uiState.sideDrawable,
                colors = colors,
                iconShape = RoundedCornerShape(16.dp),
                toggleClick = null,
                onLongClick = null,
                accessibilityUiState = uiState.accessibilityUiState,
                squishiness = { 1f },
                isVisible = { true },
                textScale = { 1f },
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            tiledata: TileData,
            dialogListener: OnMultiChoiceClickListener,
        ): TileRequestDialogDelegate
    }
}
