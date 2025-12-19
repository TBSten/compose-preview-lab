package me.tbsten.compose.preview.lab.action.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.PreviewLabAction
import me.tbsten.compose.preview.lab.PreviewLabField
import me.tbsten.compose.preview.lab.readonly
import me.tbsten.compose.preview.lab.ui.components.Text

@Composable
internal fun <R> PreviewLabAction<R>.SuccessView(result: R, field: PreviewLabAction<R>.(R) -> PreviewLabField<R>?,) {
    val resolvedField = field(result)?.readonly(value = result)

    Column(Modifier.padding(8.dp)) {
        if (resolvedField != null) {
            resolvedField.Content()
        } else {
            DefaultResultView(result = result)
        }
    }
}

@Composable
private fun <R> DefaultResultView(result: R) {
    Column {
        Text("(.toString()):")
        Text(result.toString())
    }
}
