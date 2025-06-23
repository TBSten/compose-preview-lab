package me.tbsten.compose.preview.lab.ui.components.textfield

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.textfield.base.CommonDecorationBox
import me.tbsten.compose.preview.lab.ui.components.textfield.base.FocusedOutlineThickness
import me.tbsten.compose.preview.lab.ui.components.textfield.base.HorizontalIconPadding
import me.tbsten.compose.preview.lab.ui.components.textfield.base.SupportingTopPadding
import me.tbsten.compose.preview.lab.ui.components.textfield.base.TextFieldColors
import me.tbsten.compose.preview.lab.ui.components.textfield.base.TextFieldHorizontalPadding
import me.tbsten.compose.preview.lab.ui.components.textfield.base.TextFieldMinHeight
import me.tbsten.compose.preview.lab.ui.components.textfield.base.TextFieldVerticalPadding
import me.tbsten.compose.preview.lab.ui.components.textfield.base.UnfocusedOutlineThickness
import me.tbsten.compose.preview.lab.ui.components.textfield.base.containerUnderline

@Composable
fun UnderlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
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
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    placeholder: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    colors: TextFieldColors = UnderlinedTextFieldDefaults.colors(),
    cursorBrush: Brush = SolidColor(colors.cursorColor(isError).value),
) {
    val textColor =
        textStyle.color.takeOrElse {
            colors.textColor(enabled, isError, interactionSource).value
        }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    CompositionLocalProvider(LocalTextSelectionColors provides colors.selectionColors) {
        BasicTextField(
            modifier =
            modifier
                .defaultMinSize(
                    minHeight = UnderlinedTextFieldDefaults.MinHeight,
                )
                .fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = mergedTextStyle,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = visualTransformation,
            onTextLayout = onTextLayout,
            interactionSource = interactionSource,
            cursorBrush = cursorBrush,
            decorationBox = @Composable { innerTextField ->
                UnderlinedTextFieldDefaults.DecorationBox(
                    value = value,
                    innerTextField = innerTextField,
                    visualTransformation = visualTransformation,
                    label = label,
                    placeholder = placeholder,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    prefix = prefix,
                    suffix = suffix,
                    supportingText = supportingText,
                    enabled = enabled,
                    isError = isError,
                    interactionSource = interactionSource,
                    colors = colors,
                )
            },
        )
    }
}

@Composable
fun UnderlinedTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
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
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    placeholder: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    colors: TextFieldColors = UnderlinedTextFieldDefaults.colors(),
    cursorBrush: Brush = SolidColor(colors.cursorColor(isError).value),
) {
    val textColor =
        textStyle.color.takeOrElse {
            colors.textColor(enabled, isError, interactionSource).value
        }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    CompositionLocalProvider(LocalTextSelectionColors provides colors.selectionColors) {
        BasicTextField(
            modifier =
            modifier
                .defaultMinSize(
                    minHeight = UnderlinedTextFieldDefaults.MinHeight,
                )
                .fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = mergedTextStyle,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = visualTransformation,
            onTextLayout = onTextLayout,
            interactionSource = interactionSource,
            cursorBrush = cursorBrush,
            decorationBox = @Composable { innerTextField ->
                UnderlinedTextFieldDefaults.DecorationBox(
                    value = value.text,
                    innerTextField = innerTextField,
                    visualTransformation = visualTransformation,
                    label = label,
                    placeholder = placeholder,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    prefix = prefix,
                    suffix = suffix,
                    supportingText = supportingText,
                    enabled = enabled,
                    isError = isError,
                    interactionSource = interactionSource,
                    colors = UnderlinedTextFieldDefaults.colors(),
                )
            },
        )
    }
}

@Immutable
object UnderlinedTextFieldDefaults {
    val MinHeight = TextFieldMinHeight

    private fun contentPadding(
        start: Dp = 0.dp,
        end: Dp = 0.dp,
        top: Dp = TextFieldVerticalPadding,
        bottom: Dp = TextFieldVerticalPadding,
    ): PaddingValues = PaddingValues(start, top, end, bottom)

    private fun labelPadding(start: Dp = 0.dp, top: Dp = 0.dp, end: Dp = 0.dp, bottom: Dp = 0.dp,): PaddingValues =
        PaddingValues(start, top, end, bottom)

    private fun supportingTextPadding(
        start: Dp = 0.dp,
        top: Dp = SupportingTopPadding,
        end: Dp = TextFieldHorizontalPadding,
        bottom: Dp = 0.dp,
    ): PaddingValues = PaddingValues(start, top, end, bottom)

    @Composable
    private fun leadingIconPadding(
        start: Dp = 0.dp,
        top: Dp = 0.dp,
        end: Dp = HorizontalIconPadding / 2,
        bottom: Dp = 0.dp,
    ): PaddingValues = PaddingValues(start, top, end, bottom)

    @Composable
    private fun trailingIconPadding(
        start: Dp = HorizontalIconPadding / 2,
        top: Dp = 0.dp,
        end: Dp = 0.dp,
        bottom: Dp = 0.dp,
    ): PaddingValues = PaddingValues(start, top, end, bottom)

    @Composable
    fun containerBorderThickness(interactionSource: InteractionSource,): Dp {
        val focused by interactionSource.collectIsFocusedAsState()

        return if (focused) FocusedOutlineThickness else UnfocusedOutlineThickness
    }

    @Composable
    fun DecorationBox(
        value: String,
        innerTextField: @Composable () -> Unit,
        enabled: Boolean,
        visualTransformation: VisualTransformation,
        interactionSource: InteractionSource,
        isError: Boolean = false,
        label: @Composable (() -> Unit)? = null,
        placeholder: @Composable (() -> Unit)? = null,
        leadingIcon: @Composable (() -> Unit)? = null,
        trailingIcon: @Composable (() -> Unit)? = null,
        prefix: @Composable (() -> Unit)? = null,
        suffix: @Composable (() -> Unit)? = null,
        supportingText: @Composable (() -> Unit)? = null,
        colors: TextFieldColors = colors(),
        container: @Composable () -> Unit = {
            ContainerBox(enabled, isError, interactionSource, colors)
        },
    ) {
        CommonDecorationBox(
            value = value,
            innerTextField = innerTextField,
            visualTransformation = visualTransformation,
            placeholder = placeholder,
            label = label,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            enabled = enabled,
            isError = isError,
            interactionSource = interactionSource,
            colors = colors,
            contentPadding = contentPadding(),
            labelPadding = labelPadding(),
            supportingTextPadding = supportingTextPadding(),
            leadingIconPadding = leadingIconPadding(),
            trailingIconPadding = trailingIconPadding(),
            container = container,
        )
    }

    @Composable
    fun ContainerBox(
        enabled: Boolean,
        isError: Boolean,
        interactionSource: InteractionSource,
        colors: TextFieldColors,
        borderThickness: Dp = containerBorderThickness(interactionSource),
    ) {
        Box(
            Modifier
                .background(colors.containerColor(enabled, isError, interactionSource).value)
                .containerUnderline(enabled, isError, interactionSource, colors, borderThickness),
        )
    }

    @Composable
    fun colors(): TextFieldColors = TextFieldColors(
        focusedTextColor = PreviewLabTheme.colors.text,
        unfocusedTextColor = PreviewLabTheme.colors.text,
        disabledTextColor = PreviewLabTheme.colors.onDisabled,
        errorTextColor = PreviewLabTheme.colors.text,
        focusedContainerColor = PreviewLabTheme.colors.transparent,
        unfocusedContainerColor = PreviewLabTheme.colors.transparent,
        disabledContainerColor = PreviewLabTheme.colors.transparent,
        errorContainerColor = PreviewLabTheme.colors.transparent,
        cursorColor = PreviewLabTheme.colors.primary,
        errorCursorColor = PreviewLabTheme.colors.error,
        textSelectionColors = LocalTextSelectionColors.current,
        focusedOutlineColor = PreviewLabTheme.colors.primary,
        unfocusedOutlineColor = PreviewLabTheme.colors.secondary,
        disabledOutlineColor = PreviewLabTheme.colors.disabled,
        errorOutlineColor = PreviewLabTheme.colors.error,
        focusedLeadingIconColor = PreviewLabTheme.colors.primary,
        unfocusedLeadingIconColor = PreviewLabTheme.colors.primary,
        disabledLeadingIconColor = PreviewLabTheme.colors.onDisabled,
        errorLeadingIconColor = PreviewLabTheme.colors.primary,
        focusedTrailingIconColor = PreviewLabTheme.colors.primary,
        unfocusedTrailingIconColor = PreviewLabTheme.colors.primary,
        disabledTrailingIconColor = PreviewLabTheme.colors.onDisabled,
        errorTrailingIconColor = PreviewLabTheme.colors.primary,
        focusedLabelColor = PreviewLabTheme.colors.primary,
        unfocusedLabelColor = PreviewLabTheme.colors.primary,
        disabledLabelColor = PreviewLabTheme.colors.textDisabled,
        errorLabelColor = PreviewLabTheme.colors.error,
        focusedPlaceholderColor = PreviewLabTheme.colors.textSecondary,
        unfocusedPlaceholderColor = PreviewLabTheme.colors.textSecondary,
        disabledPlaceholderColor = PreviewLabTheme.colors.textDisabled,
        errorPlaceholderColor = PreviewLabTheme.colors.textSecondary,
        focusedSupportingTextColor = PreviewLabTheme.colors.primary,
        unfocusedSupportingTextColor = PreviewLabTheme.colors.primary,
        disabledSupportingTextColor = PreviewLabTheme.colors.textDisabled,
        errorSupportingTextColor = PreviewLabTheme.colors.error,
        focusedPrefixColor = PreviewLabTheme.colors.primary,
        unfocusedPrefixColor = PreviewLabTheme.colors.primary,
        disabledPrefixColor = PreviewLabTheme.colors.onDisabled,
        errorPrefixColor = PreviewLabTheme.colors.primary,
        focusedSuffixColor = PreviewLabTheme.colors.primary,
        unfocusedSuffixColor = PreviewLabTheme.colors.primary,
        disabledSuffixColor = PreviewLabTheme.colors.onDisabled,
        errorSuffixColor = PreviewLabTheme.colors.primary,
    )
}
