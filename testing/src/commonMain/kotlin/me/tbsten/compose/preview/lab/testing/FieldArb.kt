package me.tbsten.compose.preview.lab.testing

import io.kotest.property.Arb
import io.kotest.property.arbitrary.of
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.field.PreviewLabField

/**
 * Creates a Kotest [Arb] (Arbitrary) from a [PreviewLabField]'s arbValues.
 *
 * This extension function enables property-based testing with PreviewLab fields
 * by converting the field's predefined test values into a Kotest Arb generator.
 *
 * Example usage:
 * ```kotlin
 * @Test
 * fun `test field values`() = runDesktopComposeUiTest {
 *     val state = PreviewLabState()
 *     setContent { TestPreviewLab(state) { MyPreview() } }
 *
 *     val intField = state.field<Int>("count")
 *     checkAll(intField.arb()) { value ->
 *         intField.value = value
 *         awaitIdle()
 *         // assertions...
 *     }
 * }
 * ```
 *
 * @return An [Arb] that generates values from the field's arbValues
 * @see PreviewLabField.arbValues
 */
@ExperimentalComposePreviewLabApi
fun <Value> PreviewLabField<Value>.arb(): Arb<Value> = Arb.of(arbValues().toList())
