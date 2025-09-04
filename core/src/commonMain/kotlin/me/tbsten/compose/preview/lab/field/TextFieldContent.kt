package me.tbsten.compose.preview.lab.field

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.takahirom.rin.rememberRetained
import me.tbsten.compose.preview.lab.ui.components.Text
import me.tbsten.compose.preview.lab.ui.components.textfield.OutlinedTextField

/**
 * Helper for UI of Fields that can be input with TextField.
 *
 * @param toString Converts the state value to a string.
 * @param toValue Converts the string to a state value.
 * @param prefix Composable to be displayed as a prefix; if null, it is not displayed.
 * @param suffix Composable to be displayed as a suffix; if null, it is not displayed.
 * @param placeholder Composable to be displayed as a placeholder; if null, the label is displayed with [Text].
 */
@Composable
fun <Value> MutablePreviewLabField<Value>.TextFieldContent(
    toString: (Value) -> String,
    toValue: (String) -> Result<Value>,
    modifier: Modifier = Modifier,
    prefix: (@Composable () -> Unit)? = null,
    suffix: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
) {
    var textFieldText by rememberRetained(value.toString()) { mutableStateOf(toString(value)) }
    var isValid by remember {
        mutableStateOf(true)
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = textFieldText,
            onValueChange = {
                textFieldText = it
                toValue(it)
                    .also { isValid = it.isSuccess }
                    .onSuccess { value = it }
            },
            placeholder = {
                placeholder?.invoke() ?: Text(label)
            },
            prefix = prefix,
            suffix = suffix,
            isError = !isValid,
        )
    }
}
