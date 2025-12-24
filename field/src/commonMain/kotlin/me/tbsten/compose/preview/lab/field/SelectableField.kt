package me.tbsten.compose.preview.lab.field

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.PreviewLabField.ViewMenuItem
import me.tbsten.compose.preview.lab.defaultValueCode
import me.tbsten.compose.preview.lab.field.SelectableField.Type
import me.tbsten.compose.preview.lab.field.SelectableField.Type.CHIPS
import me.tbsten.compose.preview.lab.field.SelectableField.Type.DROPDOWN
import me.tbsten.compose.preview.lab.field.SelectableField.Type.RADIO
import me.tbsten.compose.preview.lab.ui.components.Chip
import me.tbsten.compose.preview.lab.ui.components.RadioButton
import me.tbsten.compose.preview.lab.ui.components.SelectButton
import me.tbsten.compose.preview.lab.ui.components.Text

/**
 * A field that allows selection of one option from a list of specified choices.
 *
 * # Usage
 *
 * ```kt
 * // Basic usage with list of values
 * @Preview
 * @Composable
 * fun MyPreview() = PreviewLab {
 *     val theme: String = fieldValue {
 *         SelectableField(
 *             label = "Theme",
 *             choices = listOf("Light", "Dark", "Auto")
 *         )
 *     }
 *     MyApp(theme = theme)
 * }
 *
 * // With custom labels using CHIPS type
 * @Preview
 * @Composable
 * fun ButtonPreview() = PreviewLab {
 *     val size: Int = fieldValue {
 *         SelectableField(
 *             label = "Size",
 *             choices = listOf(32, 48, 64),
 *             choiceLabel = { "${it}dp" },
 *             type = SelectableField.Type.CHIPS
 *         )
 *     }
 *     MyButton(size = size)
 * }
 *
 * // Using builder syntax
 * @Preview
 * @Composable
 * fun IconPreview() = PreviewLab {
 *     val icon: ImageVector = fieldValue {
 *         SelectableField<ImageVector>(label = "Icon") {
 *             choice(Icons.Default.Home, "Home", isDefault = true)
 *             choice(Icons.Default.Search, "Search")
 *             choice(Icons.Default.Settings, "Settings")
 *         }
 *     }
 *     Icon(icon, contentDescription = null)
 * }
 * ```
 *
 * @param choiceLabel Text to be displayed in the UI to select a choice.
 * @param type Select UI type, default is [Type.DROPDOWN]. See also [SelectableField.Type].
 * @param serializer Optional serializer for the value type. If provided, enables serialization support.
 *                   For enum types, consider using [EnumField] which provides automatic serialization.
 */
open class SelectableField<Value>(
    label: String,
    val choices: List<Value>,
    private val choiceLabel: (Value) -> String = { it.toString() },
    initialType: Type = DROPDOWN,
    initialValue: Value = choices[0],
    private val valueCode: (Value) -> String = { defaultValueCode(label) },
    private val serializer: KSerializer<Value>? = null,
) : MutablePreviewLabField<Value>(
    label = label,
    initialValue = initialValue,
) {
    var type: Type by mutableStateOf(initialType)
        private set

    override fun testValues(): List<Value> = super.testValues() + choices
    override fun valueCode(): String = valueCode.invoke(value)
    override fun serializer(): KSerializer<Value>? = serializer

    @Composable
    override fun View(menuItems: List<ViewMenuItem<Value>>) {
        val typeMenuItems = Type.entries.map { targetType ->
            ChangeTypeMenuItem(this, targetType)
        }
        DefaultFieldView(menuItems = menuItems + typeMenuItems)
    }

    private class ChangeTypeMenuItem<Value>(
        override val field: SelectableField<Value>,
        private val targetType: Type,
    ) : ViewMenuItem<Value>(field) {
        override val title: String = "Type: ${targetType.name}"
        override val enabled: Boolean = field.type != targetType

        override fun onClick() {
            field.type = targetType
        }
    }

    class Builder<Value> internal constructor() {
        internal val choices = mutableListOf<Pair<String, Value>>()
        internal var defaultValue: Value? = null
        internal var isDefaultValueSet = false
        fun choice(value: Value, label: String = value.toString(), isDefault: Boolean = false) {
            choices.add(label to value)
            if (isDefault) {
                defaultValue = value
                isDefaultValueSet = true
            }
        }
    }

    /**
     * The type of UI to select.
     */
    enum class Type {
        /**
         * Dropdown menu.
         */
        DROPDOWN,

        /**
         * Chips that can be selected.
         */
        CHIPS,

        /**
         * Radio button list.
         */
        RADIO,
    }

    @Composable
    override fun Content() {
        when (type) {
            DROPDOWN -> SelectButton(
                value = value,
                choices = choices,
                onSelect = { value = it },
                title = choiceLabel,
            )
            CHIPS -> ChipsContent()
            RADIO -> RadioContent()
        }
    }

    @Composable
    private fun ChipsContent() {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        ) {
            choices.forEach { choice ->
                Chip(
                    selected = value == choice,
                    label = { Text(choiceLabel(choice)) },
                    onClick = { value = choice },
                )
            }
        }
    }

    @Composable
    private fun RadioContent() {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            choices.forEach { choice ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clickable { value = choice }
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                ) {
                    RadioButton(
                        selected = value == choice,
                        onClick = null,
                    )
                    Text(choiceLabel(choice))
                }
            }
        }
    }
}

/**
 * Create a [SelectableField] with the given label and choices using a builder syntax.
 *
 * # Usage
 *
 * ```kt
 * @Preview
 * @Composable
 * fun MyPreview() = PreviewLab {
 *     val alignment: Alignment = fieldValue {
 *         SelectableField<Alignment>(label = "Alignment") {
 *             choice(Alignment.Start, "Start", isDefault = true)
 *             choice(Alignment.Center, "Center")
 *             choice(Alignment.End, "End")
 *         }
 *     }
 *     MyComponent(alignment = alignment)
 * }
 * ```
 *
 * @see SelectableField
 */
@Suppress("UNCHECKED_CAST")
fun <Value> SelectableField(
    label: String,
    type: Type = DROPDOWN,
    builder: SelectableField.Builder<Value>.() -> Unit,
): SelectableField<Value> {
    val builder = SelectableField.Builder<Value>().apply(builder)
    return SelectableField<Value>(
        label = label,
        choices = builder.choices.map { it.second },
        initialType = type,
        choiceLabel = { choice -> builder.choices.first { it.second == choice }.first },
        initialValue = if (builder.isDefaultValueSet) builder.defaultValue as Value else builder.choices[0].second,
    )
}

/**
 * Create a [SelectableField] with the given label and choices from a map.
 *
 * # Usage
 *
 * ```kt
 * @Preview
 * @Composable
 * fun MyPreview() = PreviewLab {
 *     val language: Locale = fieldValue {
 *         SelectableField(
 *             label = "Language",
 *             choices = mapOf(
 *                 "English" to Locale.ENGLISH,
 *                 "Japanese" to Locale.JAPANESE,
 *                 "French" to Locale.FRENCH
 *             )
 *         )
 *     }
 *     MyApp(locale = language)
 * }
 * ```
 *
 * @see SelectableField
 */
@Suppress("UNCHECKED_CAST")
fun <Value> SelectableField(
    label: String,
    choices: Map<String, Value>,
    type: Type = DROPDOWN,
    initialValue: Value = choices.entries.first().value,
): SelectableField<Value> = SelectableField(
    label = label,
    type = type,
) {
    choices.forEach { (label, value) -> choice(value, label) }
    defaultValue = initialValue
}

/**
 * Create a [SelectableField] from enum class values.
 *
 * This function automatically provides serialization support for enum types.
 *
 * # Usage
 *
 * ```kt
 * enum class ButtonVariant { Primary, Secondary, Tertiary }
 *
 * @Preview
 * @Composable
 * fun ButtonPreview() = PreviewLab {
 *     val variant: ButtonVariant = fieldValue {
 *         EnumField(
 *             label = "Variant",
 *             initialValue = ButtonVariant.Primary
 *         )
 *     }
 *     MyButton(variant = variant)
 * }
 *
 * // With custom labels
 * @Preview
 * @Composable
 * fun LayoutPreview() = PreviewLab {
 *     val direction: LayoutDirection = fieldValue {
 *         EnumField(
 *             label = "Direction",
 *             initialValue = LayoutDirection.Ltr,
 *             choiceLabel = { if (it == LayoutDirection.Ltr) "Left to Right" else "Right to Left" }
 *         )
 *     }
 *     MyLayout(layoutDirection = direction)
 * }
 * ```
 *
 * @see SelectableField
 */
@Suppress("FunctionName")
inline fun <reified E : Enum<E>> EnumField(
    label: String,
    initialValue: E,
    type: Type = DROPDOWN,
    noinline choiceLabel: (E) -> String = { it.name },
) = SelectableField<E>(
    label = label,
    choices = enumValues<E>().toList(),
    choiceLabel = choiceLabel,
    initialType = type,
    initialValue = initialValue,
    serializer = runCatching { serializer<E>() }.getOrNull(),
)

/**
 * Extension function to convert a [List] to a [SelectableField].
 *
 * # Usage
 *
 * ```kt
 * @Preview
 * @Composable
 * fun ColorPreview() = PreviewLab {
 *     val color: Color = fieldValue {
 *         listOf(Color.Red, Color.Green, Color.Blue).toField(
 *             label = "Color",
 *             choiceLabel = { it.toString() }
 *         )
 *     }
 *     Box(Modifier.background(color).size(100.dp))
 * }
 *
 * // With custom type
 * @Preview
 * @Composable
 * fun SizePreview() = PreviewLab {
 *     val size: Int = fieldValue {
 *         listOf(8, 16, 24, 32).toField(
 *             label = "Spacing",
 *             choiceLabel = { "${it}dp" },
 *             type = SelectableField.Type.CHIPS
 *         )
 *     }
 *     Spacer(Modifier.height(size.dp))
 * }
 * ```
 */
fun <Value> List<Value>.toField(
    label: String,
    choiceLabel: (Value) -> String = { it.toString() },
    type: Type = DROPDOWN,
    initialValue: Value = this[0],
): SelectableField<Value> = SelectableField(
    label = label,
    choices = this,
    choiceLabel = choiceLabel,
    initialType = type,
    initialValue = initialValue,
)
