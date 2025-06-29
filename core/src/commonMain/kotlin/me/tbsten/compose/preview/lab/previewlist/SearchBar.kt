package me.tbsten.compose.preview.lab.previewlist

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text
import me.tbsten.compose.preview.lab.ui.components.textfield.TextField

@Composable
internal fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val textStyle = PreviewLabTheme.typography.body3
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search @Preview", style = textStyle) },
        textStyle = textStyle,
        modifier = modifier
            .padding(4.dp),
    )
}
