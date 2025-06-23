package me.tbsten.compose.preview.lab.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ui.LocalTextStyle
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Button
import me.tbsten.compose.preview.lab.ui.components.ButtonVariant
import me.tbsten.compose.preview.lab.ui.components.Text

@Composable
internal fun <V> SelectButton(
    choices: List<V>,
    currentIndex: Int,
    onSelect: (Int) -> Unit,
    title: (V) -> String,
    modifier: Modifier = Modifier,
    itemDetail: (V) -> String? = { null },
) = SelectButton(
    value = choices[currentIndex],
    choices = choices,
    onSelect = { onSelect(choices.indexOf(it)) },
    title = title,
    modifier = modifier,
    itemDetail = itemDetail,
)

@Composable
internal fun <V> SelectButton(
    value: V,
    choices: List<V>,
    onSelect: (V) -> Unit,
    title: (V) -> String,
    modifier: Modifier = Modifier,
    itemDetail: (V) -> String? = { null },
) {
    var showMenu by remember { mutableStateOf(false) }

    Button(
        variant = ButtonVariant.PrimaryOutlined,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        onClick = { showMenu = true },
        modifier = modifier,
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides PreviewLabTheme.typography.label3,
        ) {
            Text(title(value))
        }
    }

    // TODO
//    DropdownMenu(
//        expanded = showMenu,
//        onDismissRequest = { showMenu = false },
//    ) {
//        choices.forEachIndexed { index, item ->
//            val isSelected = item == value
//            DropdownMenuItem(
//                text = {
//                    Column(modifier = Modifier.padding(4.dp)) {
//                        Text(
//                            text = title(item),
//                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
//                            style = PreviewLabTheme.typography.bodySmall,
//                        )
//                        itemDetail(item)?.let {
//                            Text(
//                                text = it,
//                                style = PreviewLabTheme.typography.bodySmall,
//                            )
//                        }
//                    }
//                },
//                onClick = {
//                    onSelect(item)
//                    showMenu = false
//                },
//                enabled = !isSelected,
//                leadingIcon = if (isSelected) {
//                    @Composable {
//                        Icon(
//                            imageResource(Res.drawable.icon_check),
//                            contentDescription = null,
//                        )
//                    }
//                } else {
//                    null
//                },
//            )
//        }
//    }
}
