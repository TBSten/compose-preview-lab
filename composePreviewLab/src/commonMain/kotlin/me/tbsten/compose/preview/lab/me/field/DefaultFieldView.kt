package me.tbsten.compose.preview.lab.me.field

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <Value> PreviewLabField<Value>.DefaultFieldView(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = { Content() },
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        FieldLabelHeader()
        content()
    }
}
