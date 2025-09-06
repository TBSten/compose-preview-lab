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

/**
 * A modifier field value that makes a component fill the maximum available size.
 *
 * @param fraction The fraction of the maximum size to fill (0.0 to 1.0)
 */
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

    /**
     * Factory for creating FillMaxSizeModifierFieldValue instances with configurable initial values.
     *
     * @param initialFraction Initial fraction value for filling the maximum size (0.0 to 1.0)
     */
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

/**
 * Adds a fillMaxSize modifier to this modifier list.
 *
 * @param fraction The fraction of the maximum size to fill (0.0 to 1.0)
 * @return A new ModifierFieldValueList with fillMaxSize applied
 */
fun ModifierFieldValueList.fillMaxSize(fraction: Float = 1f) = then(
    FillMaxSizeModifierFieldValue(
        fraction = fraction,
    ),
)

/**
 * A modifier field value that makes a component fill the maximum available width.
 *
 * @param fraction The fraction of the maximum width to fill (0.0 to 1.0)
 */
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

    /**
     * Factory for creating FillMaxWidthModifierFieldValue instances with configurable initial values.
     *
     * @param initialFraction Initial fraction value for filling the maximum width (0.0 to 1.0)
     */
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

/**
 * Adds a fillMaxWidth modifier to this modifier list.
 *
 * @param fraction The fraction of the maximum width to fill (0.0 to 1.0)
 * @return A new ModifierFieldValueList with fillMaxWidth applied
 */
fun ModifierFieldValueList.fillMaxWidth(fraction: Float = 1f) = then(
    FillMaxWidthModifierFieldValue(
        fraction = fraction,
    ),
)

/**
 * A modifier field value that makes a component fill the maximum available height.
 *
 * @param fraction The fraction of the maximum height to fill (0.0 to 1.0)
 */
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

    /**
     * Factory for creating FillMaxHeightModifierFieldValue instances with configurable initial values.
     *
     * @param initialFraction Initial fraction value for filling the maximum height (0.0 to 1.0)
     */
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

/**
 * Adds a fillMaxHeight modifier to this modifier list.
 *
 * @param fraction The fraction of the maximum height to fill (0.0 to 1.0)
 * @return A new ModifierFieldValueList with fillMaxHeight applied
 */
fun ModifierFieldValueList.fillMaxHeight(fraction: Float = 1f) = then(
    FillMaxHeightModifierFieldValue(
        fraction = fraction,
    ),
)
