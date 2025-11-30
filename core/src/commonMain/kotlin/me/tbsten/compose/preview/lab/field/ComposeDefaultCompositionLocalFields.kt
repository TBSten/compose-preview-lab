package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import me.tbsten.compose.preview.lab.PreviewLabScope

/**
 * Provides default Compose composition local fields for [PreviewLab][me.tbsten.compose.preview.lab.PreviewLab].
 *
 * This function creates interactive fields for commonly used composition locals like:
 * - [LocalDensity] with customizable density and font scale
 * - [LocalLayoutDirection] with selectable layout directions
 * - Platform-specific composition locals
 *
 * @param densityField Transform function for the density field. Default is identity.
 * @param fontScaleField Transform function for the font scale field. Default adds basic font scale hints.
 * @return Array of [ProvidedValue] containing the composition local fields.
 *
 * # Usage
 *
 * ```kt
 * @Preview
 * @Composable
 * fun MyPreview() = PreviewLab {
 *     CompositionLocalProvider(
 *         *provideDefaultCompositionLocalFields(),
 *     ) {
 *         MyComponent()
 *     }
 * }
 * ```
 */
@Composable
fun PreviewLabScope.provideDefaultCompositionLocalFields(
    densityField: (MutablePreviewLabField<Float>) -> MutablePreviewLabField<Float> =
        { it },
    fontScaleField: (MutablePreviewLabField<Float>) -> MutablePreviewLabField<Float> =
        { it.withBasicFontScalesHint() },
): Array<ProvidedValue<*>> = arrayOf(
    LocalDensity provides LocalDensity.current.let { density ->
        Density(
            density = fieldValue { FloatField("density", density.density).let(densityField) },
            fontScale = fieldValue { FloatField("fontScale", density.fontScale).let(fontScaleField) },
        )
    },
    LocalLayoutDirection provides fieldValue {
        SelectableField(
            "layoutDirection",
            choices = LayoutDirection.entries,
        )
    },
    *providePlatformDefaultCompositionLocalFields(),
)

internal expect fun PreviewLabScope.providePlatformDefaultCompositionLocalFields(): Array<ProvidedValue<*>>

/**
 * Adds basic font scale hints to a float field.
 *
 * This extension function provides common font scale presets:
 * - "small": 1.0f
 * - "normal": 1.5f
 * - "large": 2.0f
 *
 * # Usage
 *
 * ```kt
 * val fontScaleField: MutablePreviewLabField<Float> = FloatField("fontScale", 1.0f)
 * val fieldWithHints: MutablePreviewLabField<Float> = fontScaleField.withBasicFontScalesHint()
 * ```
 *
 * @return [MutablePreviewLabField] with font scale hints added.
 */
fun MutablePreviewLabField<Float>.withBasicFontScalesHint() = withHint(
    "small" to 1.0f,
    "normal" to 1.5f,
    "large" to 2.0f,
)
