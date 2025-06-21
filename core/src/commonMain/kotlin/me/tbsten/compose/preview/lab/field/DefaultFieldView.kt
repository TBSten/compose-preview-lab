package me.tbsten.compose.preview.lab.field

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

@Composable
fun PreviewLabField<*>.FieldLabelHeader() {
    Text(
        text = this.label,
        style = MaterialTheme.typography.labelMedium,
    )
}
