package me.tbsten.compose.preview.lab.me.field

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <Value> PreviewLabField<Value>.DefaultFieldView(
    label: @Composable () -> Unit = {
        Text(
            text = this@DefaultFieldView.label,
            style = MaterialTheme.typography.labelMedium,
        )
    },
    modifier: Modifier = Modifier,
    input: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        label()
        input()
    }
}
