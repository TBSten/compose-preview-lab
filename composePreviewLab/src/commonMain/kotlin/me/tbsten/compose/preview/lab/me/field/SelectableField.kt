package me.tbsten.compose.preview.lab.me.field

import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.me.component.SelectButton

open class SelectableField<Value>(
    label: String,
    val choices: List<Value>,
    private val choiceLabel: (Value) -> String = { it.toString() },
    initialValue: Value = choices[0],
) : PreviewLabField<Value>(
    label = label,
    initialValue = initialValue,
) {
    @Composable
    override fun Content() {
//        var showChoices by remember { mutableStateOf(false) }

        SelectButton(
            choices = choices,
            currentIndex = choices.indexOf(value),
            onSelect = { value = choices[it] },
            title = choiceLabel,
        )
//        OutlinedButton(
//            onClick = { showChoices = true },
//            shape = OutlinedTextFieldDefaults.shape,
//        ) {
//            Text(choiceLabel(value))
//        }
//        DropdownMenu(
//            expanded = showChoices,
//            onDismissRequest = { showChoices = false },
//        ) {
//            choices.forEach {
//                DropdownMenuItem(
//                    text = { Text(choiceLabel(it)) },
//                    onClick = {
//                        value = it
//                        showChoices = false
//                    },
//                )
//            }
//        }
    }
}

@Suppress("FunctionName")
inline fun <reified E : Enum<E>> EnumField(
    label: String,
    initialValue: E,
) = SelectableField<E>(
    label = label,
    choices = enumValues<E>().toList(),
    choiceLabel = { it.name },
    initialValue = initialValue,
)
