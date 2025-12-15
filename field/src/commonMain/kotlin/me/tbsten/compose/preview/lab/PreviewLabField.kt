package me.tbsten.compose.preview.lab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import me.tbsten.compose.preview.lab.field.DefaultFieldView

/**
 * Returns a default placeholder code string for fields that don't have a custom [PreviewLabField.valueCode] implementation.
 *
 * This function generates a TODO comment indicating that the value needs to be set manually.
 * Used as the default implementation for [PreviewLabField.valueCode].
 *
 * @param label The field label to include in the placeholder message
 * @return A placeholder string like `/* TODO Set Label value here */`
 */
fun defaultValueCode(label: String) = "/* TODO Set $label value here */"

/**
 * Field class specified in PreviewLabScope.field/fieldValue.
 * In reality, it may be better to use the value property, getValue method, and setValue method to implement MutableState.
 *
 * In many cases, it is sufficient to use StringField, SelectableField, etc., and it is unlikely that this class will be set directly.
 * You would use this Composable if you want to create a Field of a custom type (set the value type parameter to your custom type) or if you want to create a UI for a custom Field (override the View method).
 *
 * @property label The label for this field. This is not used in any of the program logic, but only for display purposes, so it is best to set it in a language that is easy for your team members to read.
 * @property initialValue Default value for this field.
 * @property value Current value of this PreviewLabField.
 */
interface PreviewLabField<Value> {
    val label: String
    val initialValue: Value
    val value: Value
    val valueFlow: Flow<Value>

    val coroutineScope: CoroutineScope

    /**
     * Returns a Kotlin code string representing the current value of this field.
     *
     * This method is used by the Code tab in the Inspector pane to generate copy-pastable
     * Kotlin code that reproduces the current preview state. The returned string should be
     * valid Kotlin code that can be directly inserted into source files.
     *
     * # Built-in implementations
     *
     * Most built-in field types provide appropriate implementations:
     * - [me.tbsten.compose.preview.lab.field.BooleanField]: `"true"` or `"false"`
     * - [me.tbsten.compose.preview.lab.field.StringField]: `"\"Hello\""`
     * - [me.tbsten.compose.preview.lab.field.IntField]: `"42"`
     * - [me.tbsten.compose.preview.lab.field.FloatField]: `"3.14f"`
     * - [me.tbsten.compose.preview.lab.field.DpField]: `"16.dp"`
     * - [me.tbsten.compose.preview.lab.field.ColorField]: `"Color.Red"` or `"Color(0xFFFF0000)"`
     *
     * # Custom implementation
     *
     * When creating a custom field, override this method to provide appropriate code generation:
     *
     * ```kotlin
     * class MyCustomField(label: String, initialValue: MyType) :
     *     MutablePreviewLabField<MyType>(label, initialValue) {
     *
     *     override fun valueCode(): String {
     *         return "MyType(param = ${value.param})"
     *     }
     *
     *     @Composable
     *     override fun Content() { /* ... */ }
     * }
     * ```
     *
     * # Using withValueCode
     *
     * For existing fields, use [me.tbsten.compose.preview.lab.field.withValueCode] to customize the code output without creating a new class:
     *
     * ```kotlin
     * val fontWeight = fieldValue {
     *     SelectableField(
     *         label = "Font Weight",
     *         choices = listOf(FontWeight.Normal, FontWeight.Bold),
     *         choiceLabel = { it.toString() },
     *     ).withValueCode { weight ->
     *         when (weight) {
     *             FontWeight.Normal -> "FontWeight.Normal"
     *             FontWeight.Bold -> "FontWeight.Bold"
     *             else -> "FontWeight(${weight.weight})"
     *         }
     *     }
     * }
     * ```
     *
     * @return A valid Kotlin code string representing the current value
     * @see me.tbsten.compose.preview.lab.field.withValueCode
     * @see defaultValueCode
     */
    fun valueCode(): String = defaultValueCode(label)

    fun testValues(): List<Value> = listOf(initialValue)

    /**
     * Composable, which displays the entire UI for this Field.
     * If you want to customize the UI, you can override this method in your PreviewLabField to customize the UI.
     * However, in many cases where the UI is customized, overriding the content method is more appropriate.
     *
     * Within the View method, you are responsible for determining whether to display the following
     * - Display label header
     * - The main UI of this Field (i.e., Content method)
     *
     * In many cases, a header that displays the label is required. This means that it is tedious to override it every time.
     * The Content method is preferred over the View method in many cases where the UI is customized because it allows the View to remain the same and only the main UI below the label to be customized.
     *
     * If you are interested in customizing the PreviewLabField UI, please also see the [Customizing Field UI]() documentation.
     *
     * @see Content
     */
    @Composable
    fun View() = DefaultFieldView()

    /**
     * Composable, which displays the main UI for this Field.
     * If you want to customize the UI, you can override this method in your PreviewLabField to customize the UI.
     *
     * This Composable is expected to be called primarily within a View method.
     *
     * Content should include only the main UI and not the display of the label header; the display of the label header is the role of the View method.
     *
     * If you are interested in customizing the PreviewLabField UI, please also see the [Customizing Field UI]() documentation.
     *
     * @see View
     */
    @Composable
    fun Content()

    fun onCleared() {
        coroutineScope.cancel()
    }
}

/**
 * PreviewLabField that cannot be changed.
 *
 * It is mainly used to customize a Field. See the [Create your own Field]() documentation for details.
 *
 * @see MutablePreviewLabField
 */
abstract class ImmutablePreviewLabField<Value> private constructor(
    override val label: String,
    override val initialValue: Value,
    private val state: MutableState<Value>,
) : PreviewLabField<Value>,
    State<Value> by state {
    constructor(
        label: String,
        initialValue: Value,
    ) : this(
        label = label,
        initialValue = initialValue,
        state = mutableStateOf(initialValue),
    )

    override var value: Value
        get() = state.value
        protected set(newValue) {
            state.value = newValue
        }

    override val valueFlow: Flow<Value> = snapshotFlow { state.value }

    override val coroutineScope = CoroutineScope(SupervisorJob())
}

/**
 * PreviewLabField that can be changed.
 *
 * It is mainly used to customize a Field. See the [Create your own Field]() documentation for details.
 *
 * @see ImmutablePreviewLabField
 */
abstract class MutablePreviewLabField<Value> private constructor(
    override val label: String,
    override val initialValue: Value,
    state: MutableState<Value>,
) : PreviewLabField<Value>,
    MutableState<Value> by state {

    override val valueFlow: Flow<Value> = snapshotFlow { state.value }

    override val coroutineScope = CoroutineScope(SupervisorJob())

    constructor(
        label: String,
        initialValue: Value,
    ) : this(
        label = label,
        initialValue = initialValue,
        state = mutableStateOf(initialValue),
    )
}
