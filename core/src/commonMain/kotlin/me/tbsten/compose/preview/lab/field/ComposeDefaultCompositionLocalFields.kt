package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import me.tbsten.compose.preview.lab.PreviewLabScope

@Composable
fun PreviewLabScope.provideDefaultCompositionLocalFields(
    densityField: (MutablePreviewLabField<Float>) -> MutablePreviewLabField<Float> =
        { it },
    fontScaleField: (MutablePreviewLabField<Float>) -> MutablePreviewLabField<Float> =
        { it.withBasicFontScalesHint() },
): Array<ProvidedValue<*>> = arrayOf(
    LocalDensity provides LocalDensity.current.let { density ->
        Density(
            density = fieldValue { FloatField("[default] density", density.density).let(densityField) },
            fontScale = fieldValue { FloatField("[default] fontScale", density.fontScale).let(fontScaleField) },
        )
    },
    LocalLayoutDirection provides fieldValue {
        SelectableField(
            "[default] layoutDirection",
            choices = LayoutDirection.entries,
        )
    },
    *providePlatformDefaultCompositionLocalFields(),
)

internal expect fun PreviewLabScope.providePlatformDefaultCompositionLocalFields(): Array<ProvidedValue<*>>

fun MutablePreviewLabField<Float>.withBasicFontScalesHint() = withHint(
    "small" to 1.0f,
    "normal" to 1.5f,
    "large" to 2.0f,
)
