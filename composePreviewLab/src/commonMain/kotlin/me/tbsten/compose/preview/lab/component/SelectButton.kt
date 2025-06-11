package me.tbsten.compose.preview.lab.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.composepreviewlab.generated.resources.Res
import me.tbsten.compose.preview.lab.composepreviewlab.generated.resources.icon_check
import org.jetbrains.compose.resources.imageResource


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

    OutlinedButton(onClick = { showMenu = true }, modifier = modifier) {
        Text(title(value))
    }

    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false },
    ) {
        choices.forEachIndexed { index, item ->
            val isSelected = item == value
            DropdownMenuItem(
                text = {
                    Column(modifier = Modifier.padding(4.dp)) {
                        Text(
                            text = title(item),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        itemDetail(item)?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                },
                onClick = {
                    onSelect(item)
                    showMenu = false
                },
                enabled = !isSelected,
                leadingIcon = if (isSelected) {
                    @Composable {
                        Icon(
                            imageResource(Res.drawable.icon_check),
                            contentDescription = null
                        )
                    }
                } else null,
            )
        }
    }
}
