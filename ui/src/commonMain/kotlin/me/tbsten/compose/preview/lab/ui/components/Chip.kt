package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi

/**
 * A compact selectable chip: a small [PreviewLabButton] drawn [PreviewLabButtonStyle.Primary] when [selected] and
 * [PreviewLabButtonStyle.Outlined] otherwise.
 *
 * @param modifier Modifier applied to the chip.
 * @param enabled Whether the chip can be interacted with.
 * @param selected Whether the chip is selected.
 * @param onClick Called when the chip is activated.
 * @param contentPadding Padding applied inside the chip bounds, around the chip content.
 * @param shape Shape used for the chip container, border, and focus ring.
 * @param interactionSource Interaction source used for focus, press, hover, and indication state.
 * @param leadingIcon Optional content placed before [label].
 * @param trailingIcon Optional content placed after [label].
 * @param label The chip label.
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabChip(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    contentPadding: PaddingValues = ChipContentPadding,
    shape: Shape = PreviewLabButtonDefaults.ButtonShape,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    label: @Composable (() -> Unit),
) {
    PreviewLabButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        style = if (selected) PreviewLabButtonStyle.Primary else PreviewLabButtonStyle.Outlined,
        shape = shape,
        buttonSize = PreviewLabButtonSize.Small,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    ) {
        leadingIcon?.invoke()
        label()
        trailingIcon?.invoke()
    }
}

private val ChipContentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
