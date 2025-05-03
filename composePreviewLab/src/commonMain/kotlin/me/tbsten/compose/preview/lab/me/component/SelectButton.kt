package me.tbsten.compose.preview.lab.me.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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

@Composable
internal fun <V> SelectButton(
    choices: List<V>,
    currentIndex: Int,
    onSelect: (Int) -> Unit,
    title: (V) -> String,
    modifier: Modifier = Modifier,
    itemDetail: (V) -> String? = { null },
) {
    var showMenu by remember { mutableStateOf(false) }

    OutlinedButton(onClick = { showMenu = true }, modifier = modifier) {
        Text(title(choices[currentIndex]))
    }

    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false },
    ) {
        choices.forEachIndexed { index, item ->
            DropdownMenuItem(
                text = {
                    Column(modifier = Modifier.padding(4.dp)) {
                        Text(
                            text = title(item),
                            fontWeight = FontWeight.Bold,
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
                    onSelect(index)
                    showMenu = false
                },
                enabled = currentIndex != index,
                leadingIcon = if (currentIndex == index) {
                    @Composable { Icon(Icons.Default.Check, contentDescription = null) }
                } else null,
            )
        }
    }
}