package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.LocalTextStyle
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.adaptive
import me.tbsten.compose.preview.lab.ui.generated.resources.PreviewLabUiRes
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_check
import org.jetbrains.compose.resources.imageResource

@Composable
@InternalComposePreviewLabApi
fun <V> PreviewLabSelectButton(
    choices: List<V>,
    currentIndex: Int,
    onSelect: (Int) -> Unit,
    title: (V) -> String,
    modifier: Modifier = Modifier,
    itemDetail: (V) -> String? = { null },
) = PreviewLabSelectButton(
    value = choices[currentIndex],
    choices = choices,
    onSelect = { onSelect(choices.indexOf(it)) },
    title = title,
    modifier = modifier,
    itemDetail = itemDetail,
)

@Composable
@InternalComposePreviewLabApi
fun <V> PreviewLabSelectButton(
    value: V,
    choices: List<V>,
    onSelect: (V) -> Unit,
    title: (V) -> String,
    modifier: Modifier = Modifier,
    itemDetail: (V) -> String? = { null },
) {
//    val menuState = rememberMenuState(expanded = false)
    var isOpenMenu by remember { mutableStateOf(false) }

    Column {
        PreviewLabButton(
            variant = PreviewLabButtonVariant.PrimaryOutlined,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            onClick = { isOpenMenu = true },
            modifier = modifier,
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides PreviewLabTheme.typography.label3,
            ) {
                PreviewLabText(title(value))
            }
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
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (isSelected) {
                                PreviewLabIcon(
                                    imageResource(PreviewLabUiRes.drawable.icon_check),
                                    contentDescription = "selected",
                                )
                            }
                            Column {
                                PreviewLabText(text = title(item), style = PreviewLabTheme.typography.body2)
                                itemDetail(item)?.let {
                                    PreviewLabText(
                                        text = it,
                                        style = PreviewLabTheme.typography.body3,
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
