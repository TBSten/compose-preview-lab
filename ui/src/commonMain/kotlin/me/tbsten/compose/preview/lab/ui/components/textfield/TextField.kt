/*
 * Copyright (c) 2026 Composable Horizons — MIT License (see ui/THIRD_PARTY_NOTICES.md).
 * Adapted for Compose Preview Lab: PreviewLab* prefix, compose-unstyled 2.6.0, PreviewLabTheme facade colours.
 */
package me.tbsten.compose.preview.lab.ui.components.textfield

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.FocusRingVisibility
import kotlin.jvm.JvmInline
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.previewLabFocusRing
import me.tbsten.compose.preview.lab.ui.components.textfield.base.CommonDecorationBox
import me.tbsten.compose.preview.lab.ui.util.thenIf

/** Visual style variants for [PreviewLabTextField]. */
@UiComposePreviewLabApi
@JvmInline
value class PreviewLabTextFieldStyle internal constructor(@Suppress("unused") private val value: Int) {
    @UiComposePreviewLabApi
    companion object {
        /** Filled container (`colors.field`) without a border — the Composables UI default. */
        val Filled = PreviewLabTextFieldStyle(0)

        /** Transparent container with a 1dp `colors.outline` border. */
        val Outlined = PreviewLabTextFieldStyle(1)

        /** Transparent container without a border; only the focus ring shows the bounds. */
        val Ghost = PreviewLabTextFieldStyle(2)

        /** Uses the default text field style. */
        val Default = Filled
    }
}

/**
 * A styled text input field (Composables UI `TextField` look on a `BasicTextField`).
 *
 * The container ([style], [shape], [backgroundColor], [borderColor]) is drawn behind the input row only; [label]
 * (`typography.label1`) sits above it and [supportingText] (`typography.body3`) below it. [placeholder], [prefix]
 * and [suffix] are rendered in [textStyle] so the field never changes text size while typing. Focus shows a 2dp
 * ring in `colors.ring`; [isError] turns the border, ring, cursor and supporting text [errorColor]. A disabled
 * field is drawn at 33 % alpha.
 *
 * @param value Text shown in the field.
 * @param onValueChange Called with the new text on every edit.
 * @param modifier Modifier applied to the whole field (label and supporting text included).
 * @param enabled Whether the field can be interacted with.
 * @param readOnly Whether the field can receive focus without allowing edits.
 * @param isError Whether the field shows its error state.
 * @param style Visual style of the container.
 * @param textStyle Text style of the input (also used by placeholder, prefix and suffix).
 * @param keyboardOptions Keyboard configuration for the field.
 * @param keyboardActions Actions triggered by the keyboard's IME action.
 * @param singleLine Whether the field is a single horizontally scrolling line.
 * @param maxLines Maximum number of visible lines.
 * @param minLines Minimum number of visible lines.
 * @param visualTransformation Transformation applied to the displayed text.
 * @param onTextLayout Called with the latest text layout result.
 * @param interactionSource Interaction source used for the focus ring.
 * @param placeholder Content shown while [value] is empty.
 * @param prefix Content shown before the text, inside the container.
 * @param suffix Content shown after the text, inside the container.
 * @param label Content shown above the container.
 * @param leadingIcon Content shown at the start of the container.
 * @param trailingIcon Content shown at the end of the container.
 * @param supportingText Content shown below the container.
 * @param shape Shape of the container and the focus ring.
 * @param backgroundColor Container colour; defaults from [style].
 * @param contentColor Colour of the text, prefix, suffix and icons.
 * @param placeholderColor Colour of the placeholder, label and supporting text.
 * @param borderColor Border colour; defaults from [style]. Unspecified or transparent draws no border.
 * @param errorColor Colour used for the border, ring, cursor and supporting text while [isError].
 * @param contentPadding Padding around the input row (prefix, text, suffix).
 * @param minHeight Minimum height of the container (the input row).
 * @param cursorBrush Brush used to draw the cursor.
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    style: PreviewLabTextFieldStyle = PreviewLabTextFieldStyle.Default,
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
    shape: Shape = PreviewLabTextFieldDefaults.Shape,
    backgroundColor: Color = PreviewLabTextFieldDefaults.backgroundColorFor(style),
    contentColor: Color = PreviewLabTheme.colors.onField,
    placeholderColor: Color = PreviewLabTheme.colors.muted,
    borderColor: Color = PreviewLabTextFieldDefaults.borderColorFor(style),
    errorColor: Color = PreviewLabTheme.colors.error,
    contentPadding: PaddingValues = PreviewLabTextFieldDefaults.contentPadding,
    minHeight: Dp = PreviewLabTextFieldDefaults.MinHeight,
    cursorBrush: Brush = SolidColor(if (isError) errorColor else contentColor),
) {
    val mergedTextStyle = textStyle.merge(TextStyle(color = textStyle.color.takeOrElse { contentColor }))

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .defaultMinSize(minWidth = PreviewLabTextFieldDefaults.MinWidth)
            .fillMaxWidth()
            .thenIf(!enabled) { graphicsLayer { alpha = DisabledAlpha } },
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
        decorationBox = { innerTextField ->
            CommonDecorationBox(
                value = value,
                innerTextField = innerTextField,
                visualTransformation = visualTransformation,
                textStyle = textStyle,
                label = label,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                prefix = prefix,
                suffix = suffix,
                supportingText = supportingText,
                isError = isError,
                contentColor = contentColor,
                placeholderColor = placeholderColor,
                errorColor = errorColor,
                contentPadding = contentPadding,
                minHeight = minHeight,
                container = {
                    TextFieldContainer(
                        isError = isError,
                        interactionSource = interactionSource,
                        shape = shape,
                        backgroundColor = backgroundColor,
                        borderColor = borderColor,
                        errorColor = errorColor,
                    )
                },
            )
        },
    )
}

/**
 * Container drawn behind the input row (label and supporting text sit outside it): a 2dp focus ring in
 * `colors.ring` (or [errorColor]) offset 2dp outside the [shape], the clipped [backgroundColor] and an optional 1dp
 * border — [errorColor] while [isError], otherwise [borderColor] when it is specified and not transparent.
 */
@Composable
internal fun TextFieldContainer(
    isError: Boolean,
    interactionSource: InteractionSource,
    shape: Shape,
    backgroundColor: Color,
    borderColor: Color,
    errorColor: Color,
) {
    val ringColor = if (isError) errorColor else PreviewLabTheme.colors.ring
    val resolvedBorderColor = when {
        isError -> errorColor
        borderColor.isSpecified && borderColor != Color.Transparent -> borderColor
        else -> Color.Unspecified
    }
    Box(
        Modifier
            .previewLabFocusRing(
                interactionSource = interactionSource,
                width = PreviewLabTextFieldDefaults.FocusRingWidth,
                color = ringColor,
                shape = shape,
                offset = 2.dp,
                visibility = FocusRingVisibility.Focused,
            )
            .clip(shape)
            .background(backgroundColor, shape)
            .thenIf(resolvedBorderColor.isSpecified) { border(1.dp, resolvedBorderColor, shape) },
    )
}

/** Defaults shared by [PreviewLabTextField] and `PreviewLabTransformableTextField` (Composables UI pointer values). */
@Immutable
@UiComposePreviewLabApi
object PreviewLabTextFieldDefaults {
    /** Container shape; also the theme's `fieldShape` token. */
    val Shape: Shape = RoundedCornerShape(12.dp)

    /** Minimum height of the container (the input row); label and supporting text add to it. */
    val MinHeight: Dp = 40.dp

    /** Minimum width of the field when the parent imposes none. */
    val MinWidth: Dp = 180.dp

    /** Padding around the input row (prefix, text, suffix). */
    val contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp)

    /** Width of the focus ring drawn 2dp outside the container. */
    val FocusRingWidth: Dp = 2.dp

    /** Container colour for [style]: `colors.field` when [PreviewLabTextFieldStyle.Filled], transparent otherwise. */
    @Composable
    @ReadOnlyComposable
    fun backgroundColorFor(style: PreviewLabTextFieldStyle): Color = when (style) {
        PreviewLabTextFieldStyle.Filled -> PreviewLabTheme.colors.field
        else -> Color.Transparent
    }

    /** Border colour for [style]: `colors.outline` when [PreviewLabTextFieldStyle.Outlined], unspecified otherwise. */
    @Composable
    @ReadOnlyComposable
    fun borderColorFor(style: PreviewLabTextFieldStyle): Color = when (style) {
        PreviewLabTextFieldStyle.Outlined -> PreviewLabTheme.colors.outline
        else -> Color.Unspecified
    }
}

/** Alpha of a disabled field (the theme's `disabledAlpha` token value). */
private const val DisabledAlpha = 0.33f
