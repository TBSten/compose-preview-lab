package me.tbsten.compose.preview.lab.ui.components.textfield.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.LayoutIdParentData
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.LocalTextStyle
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.ProvideTextStyle
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme

/**
 * Decorates [innerTextField] with the optional slots and lays them out with [TextFieldLayout]: [label] above the
 * [container], [supportingText] below it, icons, prefix/suffix, placeholder and text inside it.
 *
 * Colour / text style per slot (placeholder, prefix and suffix share the field's [textStyle], so the text
 * never changes size while typing):
 * - input text, prefix, suffix: [contentColor] / [textStyle]
 * - placeholder: [placeholderColor] / [textStyle] without its colour
 * - label: [placeholderColor] / `typography.label1`
 * - supporting text: [placeholderColor], or [errorColor] while [isError] / `typography.body3`
 * - leading / trailing icon: [contentColor]
 */
@Composable
internal fun CommonDecorationBox(
    value: String,
    innerTextField: @Composable () -> Unit,
    visualTransformation: VisualTransformation,
    textStyle: TextStyle,
    label: @Composable (() -> Unit)?,
    placeholder: @Composable (() -> Unit)?,
    leadingIcon: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    prefix: @Composable (() -> Unit)?,
    suffix: @Composable (() -> Unit)?,
    supportingText: @Composable (() -> Unit)?,
    isError: Boolean,
    contentColor: Color,
    placeholderColor: Color,
    errorColor: Color,
    contentPadding: PaddingValues,
    minHeight: Dp,
    container: @Composable () -> Unit,
) {
    val transformedText =
        remember(value, visualTransformation) {
            visualTransformation.filter(AnnotatedString(value))
        }.text.text
    val typography = PreviewLabTheme.typography

    val decoratedTextField: @Composable () -> Unit = {
        Decoration(contentColor = contentColor, textStyle = textStyle, content = innerTextField)
    }

    val decoratedLabel: @Composable (() -> Unit)? =
        label?.let {
            @Composable {
                Decoration(contentColor = placeholderColor, textStyle = typography.label1, content = it)
            }
        }

    val decoratedPlaceholder: @Composable ((Modifier) -> Unit)? =
        if (placeholder != null && transformedText.isEmpty()) {
            @Composable { modifier ->
                Box(modifier) {
                    Decoration(
                        contentColor = placeholderColor,
                        textStyle = textStyle.copy(color = Color.Unspecified),
                        content = placeholder,
                    )
                }
            }
        } else {
            null
        }

    val decoratedPrefix: @Composable (() -> Unit)? =
        prefix?.let {
            @Composable {
                Decoration(contentColor = contentColor, textStyle = textStyle, content = it)
            }
        }

    val decoratedSuffix: @Composable (() -> Unit)? =
        suffix?.let {
            @Composable {
                Decoration(contentColor = contentColor, textStyle = textStyle, content = it)
            }
        }

    val decoratedLeading: @Composable (() -> Unit)? =
        leadingIcon?.let {
            @Composable {
                Decoration(contentColor = contentColor, content = it)
            }
        }

    val decoratedTrailing: @Composable (() -> Unit)? =
        trailingIcon?.let {
            @Composable {
                Decoration(contentColor = contentColor, content = it)
            }
        }

    val decoratedSupporting: @Composable (() -> Unit)? =
        supportingText?.let {
            @Composable {
                Decoration(
                    contentColor = if (isError) errorColor else placeholderColor,
                    textStyle = typography.body3,
                    content = it,
                )
            }
        }

    val containerWithId: @Composable () -> Unit = {
        Box(
            Modifier.layoutId(ContainerId),
            propagateMinConstraints = true,
        ) {
            container()
        }
    }

    TextFieldLayout(
        modifier = Modifier,
        textField = decoratedTextField,
        placeholder = decoratedPlaceholder,
        label = decoratedLabel,
        leading = decoratedLeading,
        trailing = decoratedTrailing,
        prefix = decoratedPrefix,
        suffix = decoratedSuffix,
        container = containerWithId,
        supporting = decoratedSupporting,
        paddingValues = contentPadding,
        labelPaddingValues = LabelPadding,
        supportingPaddingValues = SupportingTextPadding,
        leadingIconPaddingValues = LeadingIconPadding,
        trailingIconPaddingValues = TrailingIconPadding,
        minContainerHeight = minHeight,
    )
}

/**
 * Provides [contentColor] (and, when given, [textStyle] merged over the current `LocalTextStyle`) to [content].
 * Replaces the deleted `ProvideContentColorTextStyle`; the merge keeps inherited attributes such as the font family.
 */
@Composable
internal fun Decoration(contentColor: Color, textStyle: TextStyle? = null, content: @Composable () -> Unit) {
    ProvideContentColor(contentColor) {
        if (textStyle != null) {
            ProvideTextStyle(LocalTextStyle.current.merge(textStyle)) { content() }
        } else {
            content()
        }
    }
}

internal fun widthOrZero(placeable: Placeable?) = placeable?.width ?: 0

internal fun heightOrZero(placeable: Placeable?) = placeable?.height ?: 0

internal val IntrinsicMeasurable.layoutId: Any?
    get() = (parentData as? LayoutIdParentData)?.layoutId

internal const val TextFieldId = "TextField"
internal const val PlaceholderId = "Hint"
internal const val LabelId = "Label"
internal const val LeadingId = "Leading"
internal const val TrailingId = "Trailing"
internal const val PrefixId = "Prefix"
internal const val SuffixId = "Suffix"
internal const val SupportingId = "Supporting"
internal const val ContainerId = "Container"
internal val ZeroConstraints = Constraints(0, 0, 0, 0)

/** Label sits above the container. */
internal val LabelPadding = PaddingValues(bottom = 6.dp)

/** Supporting text sits below the container, aligned with the 12dp content inset. */
internal val SupportingTextPadding = PaddingValues(start = 12.dp, top = 4.dp, end = 12.dp)

/** Icons keep the 12dp container inset and an 8dp gap to the text (Composables UI `spacedBy(8.dp)`). */
internal val LeadingIconPadding = PaddingValues(start = 12.dp, end = 8.dp)
internal val TrailingIconPadding = PaddingValues(start = 8.dp, end = 12.dp)

internal val PrefixSuffixTextPadding = 2.dp
internal val MinTextLineHeight = 24.dp
internal val MinSupportingTextLineHeight = 16.dp

internal val IconDefaultSizeModifier = Modifier.defaultMinSize(24.dp, 24.dp)
