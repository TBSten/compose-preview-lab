package me.tbsten.compose.preview.lab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import me.tbsten.compose.preview.lab.field.MutablePreviewLabField

@Composable
fun PreviewLabState.Provider(block: @Composable () -> Unit) = CompositionLocalProvider(
    LocalPreviewLabState provides this,
) {
    block()
}

@OptIn(InternalComposePreviewLabApi::class)
inline fun <reified Value> PreviewLabState.field(label: String): MutablePreviewLabField<Value>? =
    scope.fields.find { it.label == label } as? MutablePreviewLabField<Value>?

@OptIn(InternalComposePreviewLabApi::class)
inline fun <reified Value> PreviewLabState.requireField(label: String): MutablePreviewLabField<Value> =
    field<Value>(label = label)
        ?: error("Can not find update target field: label=$label")
