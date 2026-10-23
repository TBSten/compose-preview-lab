package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.adaptive
import me.tbsten.compose.preview.lab.ui.generated.resources.PreviewLabUiRes
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_arrow_drop_down
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_check
import org.jetbrains.compose.resources.painterResource

/**
 * Outlined drop-down button: shows `title(value)` with a caret and opens a [PreviewLabMenu] listing [choices] as
 * [PreviewLabListItem]s (the current [value] is marked with a check icon).
 *
 * @param value Currently selected choice.
 * @param choices Selectable choices, in menu order.
 * @param onSelect Called with the chosen item; the menu closes afterwards.
 * @param title Label of a choice (button label and menu item title).
 * @param modifier Modifier applied to the button.
 * @param itemDetail Optional secondary line shown under a menu item title.
 */
@Composable
@UiComposePreviewLabApi
fun <V> PreviewLabSelectButton(
    value: V,
    choices: List<V>,
    onSelect: (V) -> Unit,
    title: (V) -> String,
    modifier: Modifier = Modifier,
    itemDetail: (V) -> String? = { null },
) {
    var isOpenMenu by remember { mutableStateOf(false) }

    Column {
        PreviewLabButton(
            onClick = { isOpenMenu = true },
            modifier = modifier,
            style = PreviewLabButtonStyle.Outlined,
            buttonSize = PreviewLabButtonSize.Small,
            contentPadding = PaddingValues(start = 12.dp, end = 8.dp),
        ) {
            PreviewLabText(title(value), style = PreviewLabTheme.typography.label2)
            PreviewLabIcon(
                painter = painterResource(PreviewLabUiRes.drawable.icon_arrow_drop_down),
                contentDescription = null,
                modifier = Modifier.size(SelectButtonCaretSize),
            )
        }
        DisableSelection {
            PreviewLabMenu(
                expanded = isOpenMenu,
                onDismissRequest = { isOpenMenu = false },
            ) {
                choices.forEach { item ->
                    val isSelected = item == value
                    PreviewLabListItem(
                        isSelected = isSelected,
                        onSelect = {
                            onSelect(item)
                            isOpenMenu = false
                        },
                        modifier = Modifier.widthIn(min = adaptive(100, 200).dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (isSelected) {
                                PreviewLabIcon(
                                    painter = painterResource(PreviewLabUiRes.drawable.icon_check),
                                    contentDescription = "selected",
                                    modifier = Modifier.size(SelectButtonCheckSize),
                                )
                            }
                            Column {
                                PreviewLabText(text = title(item), style = PreviewLabTheme.typography.body2)
                                itemDetail(item)?.let {
                                    PreviewLabText(
                                        text = it,
                                        style = PreviewLabTheme.typography.body3,
                                        color = PreviewLabTheme.colors.muted,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val SelectButtonCaretSize = 16.dp
private val SelectButtonCheckSize = 16.dp
