package me.tbsten.compose.preview.lab.me.field

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <Value> MutablePreviewLabField<Value>.TextFieldContent(
    toString: (Value) -> String,
    toValue: (String) -> Result<Value>,
    modifier: Modifier = Modifier,
    prefix: (@Composable () -> Unit)? = null,
    suffix: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
) {
    var textFieldText by remember(value) { mutableStateOf(toString(value)) }
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
