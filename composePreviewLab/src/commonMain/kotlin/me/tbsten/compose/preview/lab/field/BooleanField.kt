package me.tbsten.compose.preview.lab.field

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable

open class BooleanField(
    label: String,
    initialValue: Boolean,
) : MutablePreviewLabField<Boolean>(
    label = label,
    initialValue = initialValue,
) {
    @Composable
    override fun Content() {
        Switch(
            checked = value,
            onCheckedChange = { value = it },
        )
    }
}
