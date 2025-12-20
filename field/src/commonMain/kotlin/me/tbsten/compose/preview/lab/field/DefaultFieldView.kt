package me.tbsten.compose.preview.lab.field

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.PreviewLabField
import me.tbsten.compose.preview.lab.PreviewLabField.ViewMenuItem
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.CommonIconButton
import me.tbsten.compose.preview.lab.ui.components.CommonMenu
import me.tbsten.compose.preview.lab.ui.components.Text
import me.tbsten.compose.preview.lab.ui.generated.resources.PreviewLabUiRes
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_more_vert
import org.jetbrains.compose.resources.painterResource

/**
 * Default UI implementation of [me.tbsten.compose.preview.lab.PreviewLabField.View]. Display a label and draw the content below it.
 *
 * Use [me.tbsten.compose.preview.lab.PreviewLabField.Content] to customize the UI of the field.
 * [me.tbsten.compose.preview.lab.PreviewLabField.View] only if you want to customize the UI, including the part that displays the label, using this Composable.
 *
 * @see me.tbsten.compose.preview.lab.PreviewLabField
 */
@Composable
fun <Value> PreviewLabField<Value>.DefaultFieldView(
    modifier: Modifier = Modifier,
    menuItems: List<ViewMenuItem<Value>> = ViewMenuItem.defaults<Value>(this),
    content: @Composable () -> Unit = { Content() },
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        FieldLabelHeader(menuItems = menuItems)
        content()
    }
}

/**
 * Display the label of [PreviewLabField].
 *
 * @see DefaultFieldView
 * @see PreviewLabField
 */
@Composable
fun <Value> PreviewLabField<Value>.FieldLabelHeader(
    menuItems: List<ViewMenuItem<Value>> = ViewMenuItem.defaults<Value>(this),
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = PreviewLabTheme.typography.label2,
            modifier = Modifier.weight(1f),
        )

        if (menuItems.isNotEmpty()) {
            var isMenuOpen by remember { mutableStateOf(false) }

            CommonIconButton(
                painter = painterResource(PreviewLabUiRes.drawable.icon_more_vert),
                onClick = { isMenuOpen = !isMenuOpen },
                modifier = Modifier.size(20.dp),
            )

            CommonMenu(
                expanded = isMenuOpen,
                onDismissRequest = { isMenuOpen = false },
            ) {
                menuItems.forEach { menuItem ->
                    menuItem.Content(
                        close = { isMenuOpen = false },
                    )
                }
            }
        }
    }
}
