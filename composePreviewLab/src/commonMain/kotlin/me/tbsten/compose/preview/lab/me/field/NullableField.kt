package me.tbsten.compose.preview.lab.me.field

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

fun <Value : Any> PreviewLabField<Value>.nullable(
    initialValue: Value? = this.initialValue,
) = NullableField(
    baseField = this,
    initialValue = initialValue,
)

class NullableField<Value : Any> internal constructor(
    private val baseField: PreviewLabField<Value>,
    initialValue: Value?,
) : MutablePreviewLabField<Value?>(
    label = baseField.label,
    initialValue = initialValue,
) {
    private var isNull by mutableStateOf(initialValue == null)
    override var value: Value? = initialValue
        get() = if (isNull) null else baseField.value

    @Composable
    override fun Content() {
        val baseFieldContent = remember { movableContentOf { baseField.Content() } }

        AnimatedContent(
            targetState = isNull,
            transitionSpec = { fadeIn() togetherWith fadeOut() using SizeTransform(clip = false) },
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart,
        ) { isNull ->
            if (!isNull) {
                Row {
                    SwitchIsNullCheckbox()
                    baseFieldContent()
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SwitchIsNullCheckbox()
                    Text(
                        text = "null",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

    @Composable
    private fun SwitchIsNullCheckbox() {
        Checkbox(
            checked = !isNull,
            onCheckedChange = { this@NullableField.isNull = !isNull },
        )
    }
}
