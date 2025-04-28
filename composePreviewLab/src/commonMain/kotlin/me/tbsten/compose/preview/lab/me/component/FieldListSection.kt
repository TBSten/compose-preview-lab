package me.tbsten.compose.preview.lab.me.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.me.field.PreviewLabField

@Composable
internal fun FieldListSection(
    fields: List<PreviewLabField<*>>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        fields.forEach { field ->
            field.View()
        }
    }
}