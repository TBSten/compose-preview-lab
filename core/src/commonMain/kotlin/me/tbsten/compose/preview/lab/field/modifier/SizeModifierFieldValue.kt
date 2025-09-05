package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.component.DpTransformer
import me.tbsten.compose.preview.lab.component.NullableDpTransformer
import me.tbsten.compose.preview.lab.component.TransformableTextField
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text

class SizeModifierFieldValue(width: Dp, height: Dp) : ModifierFieldValue {
    var width by mutableStateOf(width)
    var height by mutableStateOf(height)

    override fun Modifier.createModifier(): Modifier = size(
        width = width,
        height = height,
    )

    @Composable
    override fun Builder() = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            appendLine(".size(")

            append("  width = ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("${width.value}")
            }
            append(".dp,")
            appendLine()

            append("  height = ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("${height.value}")
            }
            append(".dp,")
            appendLine()

            append(")")
        },
        menuContent = {
            DefaultMenu {
                TextFieldItem(
                    label = "width",
                    value = width,
                    onValueChange = { width = it },
                    transformer = DpTransformer,
                    suffix = ".dp",
                )
                TextFieldItem(
                    label = "height",
                    value = height,
                    onValueChange = { height = it },
                    transformer = DpTransformer,
                    suffix = ".dp",
                )
            }
        },
    )

    class Factory(initialWidth: Dp? = null, initialHeight: Dp? = null) :
        ModifierFieldValueFactory<SizeModifierFieldValue> {
        override val title: String = ".size(...)"
        var width by mutableStateOf(initialWidth)
        var height by mutableStateOf(initialHeight)

        override val canCreate: Boolean
            get() =
                width != null && height != null

        constructor(
            initialAll: Dp? = null,
        ) : this(
            initialWidth = initialAll,
            initialHeight = initialAll,
        )

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) = Column {
            TransformableTextField(
                value = width,
                onValueChange = { width = it },
                transformer = NullableDpTransformer,
                textStyle = PreviewLabTheme.typography.label1,
                prefix = { Text("width: ", style = PreviewLabTheme.typography.label2) },
            )

            TransformableTextField(
                value = height,
                onValueChange = { height = it },
                transformer = NullableDpTransformer,
                textStyle = PreviewLabTheme.typography.label1,
                prefix = { Text("height: ", style = PreviewLabTheme.typography.label2) },
            )

            Row { createButton() }
        }

        override fun create(): Result<SizeModifierFieldValue> = runCatching {
            SizeModifierFieldValue(
                width = requireNotNull(width) { "width is null" },
                height = requireNotNull(height) { "height is null" },
            )
        }
    }
}

fun ModifierFieldValueList.size(width: Dp = 0.dp, height: Dp = 0.dp) = then(
    SizeModifierFieldValue(
        width = width,
        height = height,
    ),
)

fun ModifierFieldValueList.size(size: Dp = 0.dp) = then(
    SizeModifierFieldValue(
        width = size,
        height = size,
    ),
)

// width

class WidthModifierFieldValue(width: Dp) : ModifierFieldValue {
    var width by mutableStateOf(width)

    override fun Modifier.createModifier(): Modifier = width(width = width)

    @Composable
    override fun Builder() = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            appendLine(".width(")

            append("  width = ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("${width.value}")
            }
            append(".dp,")
            appendLine()

            append(")")
        },
        menuContent = {
            DefaultMenu {
                TextFieldItem(
                    label = "width",
                    value = width,
                    onValueChange = { width = it },
                    transformer = DpTransformer,
                    suffix = ".dp",
                )
            }
        },
    )

    class Factory(initialWidth: Dp? = null) : ModifierFieldValueFactory<WidthModifierFieldValue> {
        override val title: String = ".width(...)"
        var width by mutableStateOf(initialWidth)

        override val canCreate: Boolean
            get() = width != null

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) = Column {
            TransformableTextField(
                value = width,
                onValueChange = { width = it },
                transformer = NullableDpTransformer,
                textStyle = PreviewLabTheme.typography.label1,
                prefix = { Text("width: ", style = PreviewLabTheme.typography.label2) },
            )

            Row { createButton() }
        }

        override fun create(): Result<WidthModifierFieldValue> = runCatching {
            WidthModifierFieldValue(
                width = requireNotNull(width) { "width is null" },
            )
        }
    }
}

fun ModifierFieldValueList.width(width: Dp) = then(
    WidthModifierFieldValue(
        width = width,
    ),
)

// height

class HeightModifierFieldValue(height: Dp) : ModifierFieldValue {
    var height by mutableStateOf(height)

    override fun Modifier.createModifier(): Modifier = height(height = height)

    @Composable
    override fun Builder() = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            appendLine(".height(")

            append("  height = ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("${height.value}")
            }
            append(".dp,")
            appendLine()

            append(")")
        },
        menuContent = {
            DefaultMenu {
                TextFieldItem(
                    label = "height",
                    value = height,
                    onValueChange = { height = it },
                    transformer = DpTransformer,
                    suffix = ".dp",
                )
            }
        },
    )

    class Factory(initialHeight: Dp? = null) : ModifierFieldValueFactory<HeightModifierFieldValue> {
        override val title: String = ".height(...)"
        var height by mutableStateOf(initialHeight)

        override val canCreate: Boolean
            get() = height != null

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) = Column {
            TransformableTextField(
                value = height,
                onValueChange = { height = it },
                transformer = NullableDpTransformer,
                textStyle = PreviewLabTheme.typography.label1,
                prefix = { Text("height: ", style = PreviewLabTheme.typography.label2) },
            )

            Row { createButton() }
        }

        override fun create(): Result<HeightModifierFieldValue> = runCatching {
            HeightModifierFieldValue(
                height = requireNotNull(height) { "height is null" },
            )
        }
    }
}

fun ModifierFieldValueList.height(height: Dp) = then(
    HeightModifierFieldValue(
        height = height,
    ),
)
