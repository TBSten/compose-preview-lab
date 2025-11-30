package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

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

    fun arbValues(): Sequence<Value> = sequenceOf(initialValue)

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
    constructor(
        label: String,
        initialValue: Value,
    ) : this(
        label = label,
        initialValue = initialValue,
        state = mutableStateOf(initialValue),
    )
}
