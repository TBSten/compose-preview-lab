package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.PreviewLabButton
import me.tbsten.compose.preview.lab.ui.components.PreviewLabButtonVariant
import me.tbsten.compose.preview.lab.ui.components.PreviewLabDivider
import me.tbsten.compose.preview.lab.ui.components.PreviewLabIcon
import me.tbsten.compose.preview.lab.ui.components.PreviewLabIconButton
import me.tbsten.compose.preview.lab.ui.components.PreviewLabListItem
import me.tbsten.compose.preview.lab.ui.components.PreviewLabMenu
import me.tbsten.compose.preview.lab.ui.components.PreviewLabModal
import me.tbsten.compose.preview.lab.ui.components.PreviewLabText
import me.tbsten.compose.preview.lab.ui.components.toast.ToastType
import me.tbsten.compose.preview.lab.ui.components.toast.rememberToastHostState
import me.tbsten.compose.preview.lab.ui.generated.resources.PreviewLabUiRes
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_add
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_remove
import org.jetbrains.compose.resources.painterResource

/**
 * Default configuration object for ModifierBuilder functionality.
 * Provides default sets of modifier field value factories organized by category.
 */
object ModifierBuilderDefaults {
    val modifierFieldValueFactories: ModifierFieldValueFactories = listOf(
        // Layout
        PaddingModifierFieldValue.Factory(initialAll = 10.dp),
        // Size
        SizeModifierFieldValue.Factory(initialWidth = 200.dp, initialHeight = 80.dp),
        WidthModifierFieldValue.Factory(initialWidth = 200.dp),
        HeightModifierFieldValue.Factory(initialHeight = 200.dp),
        FillMaxSizeModifierFieldValue.Factory(initialFraction = 1f),
        FillMaxWidthModifierFieldValue.Factory(initialFraction = 1f),
        FillMaxHeightModifierFieldValue.Factory(initialFraction = 1f),
        WrapContentSizeModifierFieldValue.Factory(),
        WrapContentWidthModifierFieldValue.Factory(),
        WrapContentHeightModifierFieldValue.Factory(),
        AspectRatioModifierFieldValue.Factory(initialRatio = null),
        // Offset
        OffsetModifierFieldValue.Factory(initialX = 0.dp, initialY = 0.dp),
        ZIndexModifierFieldValue.Factory(initialZIndex = 0f),
        // Layout Listener
        CaptureSizeModifierFieldValue.Factory(),
        CaptureOffsetModifierFieldValue.Factory(),
        CaptureLayoutRectModifierFieldValue.Factory(),
        // Graphics
        BorderModifierFieldValue.Factory(initialColor = Color.Red, initialWidth = 1.dp),
        BackgroundModifierFieldValue.Factory(initialColor = Color.Red),
        AlphaModifierFieldValue.Factory(initialAlpha = 0.5f),
        RotateModifierFieldValue.Factory(initialDegrees = 45f),
        ScaleModifierFieldValue.Factory(initialScaleX = 1.25f, initialScaleY = 1.25f),
        // Animation
        AnimateContentSizeModifierFieldValue.Factory(),
    )
}

@Composable
internal fun ModifierBuilder(
    state: ModifierBuilderState,
    modifier: Modifier = Modifier,
    selectableModifierFieldValueFactories: ModifierFieldValueFactories =
        ModifierBuilderDefaults.modifierFieldValueFactories,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        state.values.forEachIndexed { index, modifierFieldValue ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    modifierFieldValue.Builder()
                }

                PreviewLabIconButton(
                    painter = painterResource(PreviewLabUiRes.drawable.icon_remove),
                    contentDescription = "Remove",
                    onClick = { state.onRemove(index, modifierFieldValue) },
                )
            }
        }

        Column {
            PreviewLabButton(
                variant = PreviewLabButtonVariant.Ghost,
                onClick = state.addMenu::toggle,
                modifier = Modifier.fillMaxWidth(),
            ) {
                PreviewLabIcon(
                    painter = painterResource(PreviewLabUiRes.drawable.icon_add),
                    contentDescription = "Add",
                )
            }

            AddModifierMenu(
                state = state,
                selectableModifierFieldValueFactories = selectableModifierFieldValueFactories,
            )
        }
    }
}

@Composable
private fun AddModifierMenu(state: ModifierBuilderState, selectableModifierFieldValueFactories: ModifierFieldValueFactories) {
    val toastHostState = rememberToastHostState()

    PreviewLabMenu(
        expanded = state.addMenu.isAddMenuOpen,
        onDismissRequest = state.addMenu::toggle,
    ) {
        PreviewLabText(
            text = "Add Modifier",
            style = PreviewLabTheme.typography.body2,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.defaultMinSize(minWidth = 200.dp).padding(horizontal = 8.dp, vertical = 4.dp),
        )
        PreviewLabDivider()

        selectableModifierFieldValueFactories.forEach { factory ->
            fun onCreate() {
                if (factory.canCreate) {
                    factory.create()
                        .fold(
                            onSuccess = {
                                state.addNewValue(it)
                                state.addMenu.close()
                            },
                            onFailure = {
                                toastHostState.show(
                                    message = it.message ?: "Error",
                                    type = ToastType.Error,
                                )
                            },
                        )
                }
            }

            var openDetailSettingDialog by remember { mutableStateOf(false) }
            PreviewLabListItem(
                title = factory.title,
                isSelected = false,
                onSelect = { if (factory.canCreate) onCreate() else openDetailSettingDialog = true },
                modifier = Modifier.fillMaxWidth(),
            )

            PreviewLabModal(
                isVisible = openDetailSettingDialog,
                onDismissRequest = { openDetailSettingDialog = false },
            ) {
                factory.Content(
                    createButton = {
                        PreviewLabButton(
                            text = "Create",
                            onClick = ::onCreate,
                            isEnabled = factory.canCreate,
                        )
                    },
                )
            }
        }
    }
}
