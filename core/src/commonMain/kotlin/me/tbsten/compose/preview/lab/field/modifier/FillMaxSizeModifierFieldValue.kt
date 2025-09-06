package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.component.FloatTransformer
import me.tbsten.compose.preview.lab.component.NullableFloatTransformer
import me.tbsten.compose.preview.lab.component.TransformableTextField
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text

class FillMaxSizeModifierFieldValue(fraction: Float) : ModifierFieldValue {
    var fraction by mutableStateOf(fraction)

    override fun Modifier.createModifier(): Modifier = fillMaxSize(
        fraction = fraction,
    )

    @Composable
    override fun Builder() = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            appendLine(".fillMaxSize(")

            append("  fraction = ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("$fraction")
            }
            appendLine()

            append("f)")
        },
        menuContent = {
            DefaultMenu {
                TextFieldItem(
                    label = "fraction",
                    value = fraction,
                    onValueChange = { fraction = it },
                    transformer = FloatTransformer,
                )
            }
        },
    )

    class Factory(initialFraction: Float? = null) : ModifierFieldValueFactory<FillMaxSizeModifierFieldValue> {
        override val title: String = ".fillMaxSize(...)"
        var fraction by mutableStateOf(initialFraction)

        override val canCreate: Boolean
            get() = fraction != null

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) = Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TransformableTextField(
                value = fraction,
                onValueChange = { fraction = it },
                transformer = NullableFloatTransformer,
                textStyle = PreviewLabTheme.typography.label1,
                prefix = { Text("fraction: ", style = PreviewLabTheme.typography.label2) },
            )

            Row { createButton() }
        }

        override fun create(): Result<FillMaxSizeModifierFieldValue> = runCatching {
            FillMaxSizeModifierFieldValue(
                fraction = requireNotNull(fraction) { "fraction is null" },
            )
        }
    }
}

fun ModifierFieldValueList.fillMaxSize(fraction: Float = 1f) = then(
    FillMaxSizeModifierFieldValue(
        fraction = fraction,
    ),
)

class FillMaxWidthModifierFieldValue(fraction: Float) : ModifierFieldValue {
    var fraction by mutableStateOf(fraction)

    override fun Modifier.createModifier(): Modifier = fillMaxWidth(
        fraction = fraction,
    )

    @Composable
    override fun Builder() = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            appendLine(".fillMaxWidth(")

            append("  fraction = ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("$fraction")
            }
            appendLine()

            append(")")
        },
        menuContent = {
            DefaultMenu {
                TextFieldItem(
                    label = "fraction",
                    value = fraction,
                    onValueChange = { fraction = it },
                    transformer = FloatTransformer,
                )
            }
        },
    )

    class Factory(initialFraction: Float? = null) : ModifierFieldValueFactory<FillMaxWidthModifierFieldValue> {
        override val title: String = ".fillMaxWidth(...)"
        var fraction by mutableStateOf(initialFraction)

        override val canCreate: Boolean
            get() = fraction != null

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) = Column {
            TransformableTextField(
                value = fraction,
                onValueChange = { fraction = it },
                transformer = NullableFloatTransformer,
                textStyle = PreviewLabTheme.typography.label1,
                prefix = { Text("fraction: ", style = PreviewLabTheme.typography.label2) },
            )

            Row { createButton() }
        }

        override fun create(): Result<FillMaxWidthModifierFieldValue> = runCatching {
            FillMaxWidthModifierFieldValue(
                fraction = requireNotNull(fraction) { "fraction is null" },
            )
        }
    }
}

fun ModifierFieldValueList.fillMaxWidth(fraction: Float = 1f) = then(
    FillMaxWidthModifierFieldValue(
        fraction = fraction,
    ),
)

class FillMaxHeightModifierFieldValue(fraction: Float) : ModifierFieldValue {
    var fraction by mutableStateOf(fraction)

    override fun Modifier.createModifier(): Modifier = fillMaxHeight(
        fraction = fraction,
    )

    @Composable
    override fun Builder() = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            appendLine(".fillMaxHeight(")

            append("  fraction = ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("$fraction")
            }
            appendLine()

            append(")")
        },
        menuContent = {
            DefaultMenu {
                TextFieldItem(
                    label = "fraction",
                    value = fraction,
                    onValueChange = { fraction = it },
                    transformer = FloatTransformer,
                )
            }
        },
    )

    class Factory(initialFraction: Float? = null) : ModifierFieldValueFactory<FillMaxHeightModifierFieldValue> {
        override val title: String = ".fillMaxHeight(...)"
        var fraction by mutableStateOf(initialFraction)

        override val canCreate: Boolean
            get() = fraction != null

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) = Column {
            TransformableTextField(
                value = fraction,
                onValueChange = { fraction = it },
                transformer = NullableFloatTransformer,
                textStyle = PreviewLabTheme.typography.label1,
                prefix = { Text("fraction: ", style = PreviewLabTheme.typography.label2) },
            )

            Row { createButton() }
        }

        override fun create(): Result<FillMaxHeightModifierFieldValue> = runCatching {
            FillMaxHeightModifierFieldValue(
                fraction = requireNotNull(fraction) { "fraction is null" },
            )
        }
    }
}

fun ModifierFieldValueList.fillMaxHeight(fraction: Float = 1f) = then(
    FillMaxHeightModifierFieldValue(
        fraction = fraction,
    ),
)
