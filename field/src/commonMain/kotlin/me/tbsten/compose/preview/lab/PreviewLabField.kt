package me.tbsten.compose.preview.lab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer
import me.tbsten.compose.preview.lab.field.DefaultFieldView
import me.tbsten.compose.preview.lab.ui.components.PreviewLabListItem

/** Default placeholder for fields without a custom [PreviewLabField.valueCode] override. */
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
    var label: String
    val initialValue: Value
    val value: Value
    val valueFlow: Flow<Value>

    val coroutineScope: CoroutineScope

    /**
     * Kotlin source representation of the current value, used by the Code tab in the
     * Inspector pane to produce copy-pastable code that reproduces the preview state.
     * The returned string must be valid Kotlin code.
     *
     * Custom field — override to control the generated literal:
     *
     * ```kotlin
     * class MyCustomField(label: String, initialValue: MyType) :
     *     MutablePreviewLabField<MyType>(label, initialValue) {
     *     override fun valueCode(): String = "MyType(param = ${value.param})"
     *     @Composable override fun Content() { /* ... */ }
     * }
     * ```
     *
     * To customise an existing field without subclassing, use
     * [me.tbsten.compose.preview.lab.field.withValueCode].
     *
     * @see me.tbsten.compose.preview.lab.field.withValueCode
     * @see defaultValueCode
     */
    fun valueCode(): String = defaultValueCode(label)

    /**
     * Representative values for property-based testing and visual regression testing —
     * boundary / edge cases the consumer wants to scan across. Default: `listOf(initialValue)`.
     *
     * Used with property-based testing frameworks to vary the field while asserting on the
     * preview:
     *
     * ```kotlin
     * forAll(Arb.string().plusEdgecases(myField.testValues())) { value ->
     *     myField.value = value
     *     awaitIdle()
     *     // assertions...
     *     true
     * }
     * ```
     *
     * Custom field — override to declare meaningful edge cases (e.g. 0 / 50 / 100 for a
     * percentage). For existing fields, use
     * [me.tbsten.compose.preview.lab.field.withTestValues].
     *
     * @see me.tbsten.compose.preview.lab.field.withTestValues
     */
    fun testValues(): List<Value> = listOf(initialValue)

    /**
     * Serializer used to persist the field value across sessions (e.g. via URL params or
     * saved-state). Return `null` to opt out of persistence. Built-in primitive fields
     * (String / Int / Boolean / …) provide serializers automatically.
     *
     * Custom field — override when the value type is `@Serializable`:
     *
     * ```kotlin
     * class MyConfigField(label: String, initialValue: MyConfig) :
     *     MutablePreviewLabField<MyConfig>(label, initialValue) {
     *     override fun serializer(): KSerializer<MyConfig> = MyConfig.serializer()
     *     @Composable override fun Content() { /* ... */ }
     * }
     * ```
     *
     * To bolt onto an existing field, use
     * [me.tbsten.compose.preview.lab.field.withSerializer].
     *
     * @see me.tbsten.compose.preview.lab.field.withSerializer
     */
    fun serializer(): KSerializer<Value>? = null

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
    fun View(menuItems: List<ViewMenuItem<Value>> = ViewMenuItem.defaults(this)) = DefaultFieldView(
        menuItems = menuItems,
    )

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

    abstract class ViewMenuItem<Value>(open val field: PreviewLabField<Value>) {
        abstract val title: String
        open val enabled: Boolean = true

        abstract fun onClick()

        @Composable
        open fun Content(close: () -> Unit) {
            PreviewLabListItem(
                title = title,
                isSelected = false,
                isEnabled = enabled,
                onSelect = {
                    onClick()
                    close()
                },
            )
        }

        class ResetValue<Value>(override val field: MutablePreviewLabField<Value>) : ViewMenuItem<Value>(field) {
            override val title: String = "Reset Value"

            override fun onClick() {
                field.value = field.initialValue
            }
        }

        companion object {
            operator fun <Value> invoke(field: PreviewLabField<Value>, title: String, onClick: () -> Unit) =
                object : ViewMenuItem<Value>(field) {
                    override val title: String = title

                    override fun onClick() = onClick()
                }

            fun <Value> defaults(field: MutablePreviewLabField<Value>): List<ViewMenuItem<Value>> = listOf(
                ResetValue(field),
            )

            fun <Value> defaults(field: PreviewLabField<Value>): List<ViewMenuItem<Value>> = listOf()
        }
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
    label: String,
    override val initialValue: Value,
    private val state: MutableState<Value>,
) : PreviewLabField<Value>,
    State<Value> by state {
    override var label by mutableStateOf(label)

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
    label: String,
    override val initialValue: Value,
    state: MutableState<Value>,
) : PreviewLabField<Value>,
    MutableState<Value> by state {
    override var label by mutableStateOf(label)

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
