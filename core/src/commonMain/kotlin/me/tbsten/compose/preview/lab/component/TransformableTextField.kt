package me.tbsten.compose.preview.lab.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.textfield.OutlinedTextField
import me.tbsten.compose.preview.lab.ui.components.textfield.TextField
import me.tbsten.compose.preview.lab.ui.components.textfield.TextFieldDefaults
import me.tbsten.compose.preview.lab.ui.components.textfield.base.TextFieldColors

interface Transformer<Value> {
    fun toString(value: Value): String
    fun fromString(string: String): Value
}

@Composable
internal fun <Value> TransformableTextField(
    value: Value,
    onValueChange: (Value) -> Unit,
    transformer: Transformer<Value>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = PreviewLabTheme.typography.input,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: TransformableTextFieldSlotScope.(TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    placeholder: @Composable (TransformableTextFieldSlotScope.() -> Unit)? = null,
    prefix: @Composable (TransformableTextFieldSlotScope.() -> Unit)? = null,
    suffix: @Composable (TransformableTextFieldSlotScope.() -> Unit)? = null,
    label: @Composable (TransformableTextFieldSlotScope.() -> Unit)? = null,
    leadingIcon: @Composable (TransformableTextFieldSlotScope.() -> Unit)? = null,
    trailingIcon: @Composable (TransformableTextFieldSlotScope.() -> Unit)? = null,
    supportingText: @Composable (TransformableTextFieldSlotScope.() -> Unit)? = null,
    isError: Boolean? = null,
    shape: Shape = TextFieldDefaults.Shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    cursorBrush: Brush = SolidColor(colors.cursorColor(isError == true).value),
) {
    var textFieldValue by remember { mutableStateOf(transformer.toString(value)) }
        .bindTransform(
            value = value,
            onValueChange = onValueChange,
            transformer = transformer,
        )
    val isError = isError ?: runCatching { transformer.fromString(textFieldValue) }.isFailure

    val slotScope =
        TransformableTextFieldSlotScope(
            isError = isError,
        )

    TextField(
        value = textFieldValue,
        onValueChange = { textFieldValue = it },
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        visualTransformation = visualTransformation,
        onTextLayout = { slotScope.onTextLayout(it) },
        interactionSource = interactionSource,
        placeholder = placeholder?.let { { placeholder.invoke(slotScope) } },
        prefix = prefix?.let { { prefix.invoke(slotScope) } },
        suffix = suffix?.let { { suffix.invoke(slotScope) } },
        label = label?.let { { label.invoke(slotScope) } },
        leadingIcon = leadingIcon?.let { { leadingIcon.invoke(slotScope) } },
        trailingIcon = trailingIcon?.let { { trailingIcon.invoke(slotScope) } },
        supportingText = supportingText?.let { { supportingText.invoke(slotScope) } },
        isError = isError,
        shape = shape,
        colors = colors,
        cursorBrush = cursorBrush,
    )
}

@Composable
fun <Value> MutableState<String>.bindTransform(
    value: Value,
    onValueChange: (Value) -> Unit,
    transformer: Transformer<Value>,
): MutableState<String> = this.also { textFieldValue ->
    LaunchedEffect(value) {
        textFieldValue.value = transformer.toString(value)
    }
    LaunchedEffect(textFieldValue.value) {
        runCatching { transformer.fromString(textFieldValue.value) }
            .onSuccess { onValueChange(it) }
    }
}

object DpTransformer : Transformer<Dp> {
    override fun toString(value: Dp) = value.value.toString()
    override fun fromString(string: String) = string.toFloatOrNull()?.dp ?: error("$string is not a valid dp")
}

object NullableDpTransformer : Transformer<Dp?> {
    override fun toString(value: Dp?) = value?.value?.toString() ?: ""
    override fun fromString(string: String) = string.toFloatOrNull()?.dp
}

object FloatTransformer : Transformer<Float> {
    override fun toString(value: Float) = value.toString()
    override fun fromString(string: String) = string.toFloatOrNull() ?: error("$string is not a valid float")
}

object NullableFloatTransformer : Transformer<Float?> {
    override fun toString(value: Float?) = value?.toString() ?: ""
    override fun fromString(string: String) = string.toFloatOrNull()
}

class TransformableTextFieldSlotScope internal constructor(val isError: Boolean)

@Composable
internal fun <Value> TransformableOutlinedTextField(
    value: Value,
    onValueChange: (Value) -> Unit,
    transformer: Transformer<Value>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = PreviewLabTheme.typography.input,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: TransformableTextFieldSlotScope.(TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    placeholder: @Composable (TransformableTextFieldSlotScope.() -> Unit)? = null,
    prefix: @Composable (TransformableTextFieldSlotScope.() -> Unit)? = null,
    suffix: @Composable (TransformableTextFieldSlotScope.() -> Unit)? = null,
    label: @Composable (TransformableTextFieldSlotScope.() -> Unit)? = null,
    leadingIcon: @Composable (TransformableTextFieldSlotScope.() -> Unit)? = null,
    trailingIcon: @Composable (TransformableTextFieldSlotScope.() -> Unit)? = null,
    supportingText: @Composable (TransformableTextFieldSlotScope.() -> Unit)? = null,
    isError: Boolean? = null,
    shape: Shape = TextFieldDefaults.Shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    cursorBrush: Brush = SolidColor(colors.cursorColor(isError == true).value),
) {
    var textFieldValue by remember { mutableStateOf(transformer.toString(value)) }
        .bindTransform(
            value = value,
            onValueChange = onValueChange,
            transformer = transformer,
        )
    val isError = isError ?: runCatching { transformer.fromString(textFieldValue) }.isFailure

    val slotScope =
        TransformableTextFieldSlotScope(
            isError = isError,
        )

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { textFieldValue = it },
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        visualTransformation = visualTransformation,
        onTextLayout = { slotScope.onTextLayout(it) },
        interactionSource = interactionSource,
        placeholder = placeholder?.let { { placeholder.invoke(slotScope) } },
        prefix = prefix?.let { { prefix.invoke(slotScope) } },
        suffix = suffix?.let { { suffix.invoke(slotScope) } },
        label = label?.let { { label.invoke(slotScope) } },
        leadingIcon = leadingIcon?.let { { leadingIcon.invoke(slotScope) } },
        trailingIcon = trailingIcon?.let { { trailingIcon.invoke(slotScope) } },
        supportingText = supportingText?.let { { supportingText.invoke(slotScope) } },
        isError = isError,
        shape = shape,
        colors = colors,
        cursorBrush = cursorBrush,
    )
}
