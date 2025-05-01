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
import androidx.compose.ui.unit.dp

@Composable
fun <Value> PreviewLabField<Value>.TextFieldContent(
    toString: (Value) -> String,
    toValue: (String) -> Result<Value>,
    prefix: (@Composable () -> Unit)? = null,
    suffix: (@Composable () -> Unit)? = null,
) {
    var textFieldText by remember(value) { mutableStateOf(toString(value)) }
    var isValid by remember {
        mutableStateOf(true)
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                Text(label)
            },
            prefix = prefix,
            suffix = suffix,
            isError = !isValid,
        )
    }
}
