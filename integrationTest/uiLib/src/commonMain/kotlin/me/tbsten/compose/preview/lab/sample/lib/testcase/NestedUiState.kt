package me.tbsten.compose.preview.lab.sample.lib.testcase

import me.tbsten.compose.preview.lab.generatecombinedfield.GenerateCombinedField

/**
 * Test case 2: Nested data classes
 *
 * This tests the case where a data class contains another data class
 * that also has @GenerateCombinedField annotation.
 *
 * Expected behavior:
 * - Section1State.Companion.field() should be generated
 * - NestedUiState.Companion.field() should be generated
 * - NestedUiState's field should use Section1State.field() internally
 */
@GenerateCombinedField
data class Section1State(
    val heading: String,
    val body: String,
) {
    companion object
}

fun Section1State.Companion.fake() = Section1State(
    heading = "Section Heading",
    body = "Section body text goes here",
)

@GenerateCombinedField
data class NestedUiState(
    val section1State: Section1State,
    val enableButton: Boolean,
) {
    companion object
}

fun NestedUiState.Companion.fake() = NestedUiState(
    section1State = Section1State.fake(),
    enableButton = true,
)

/**
 * Expected generated code:
 *
 * fun Section1State.Companion.field(label: String, initialValue: Section1State) = CombinedField2(
 *     label = label,
 *     field1 = StringField("heading", initialValue = initialValue.heading),
 *     field2 = StringField("body", initialValue = initialValue.body),
 *     combine = { heading, body ->
 *         Section1State(heading = heading, body = body)
 *     },
 *     split = { splitedOf(it.heading, it.body) },
 * )
 *
 * fun NestedUiState.Companion.field(label: String, initialValue: NestedUiState): MutablePreviewLabField<NestedUiState> = CombinedField2(
 *     label = label,
 *     field1 = Section1State.field(label = "section1State", initialValue = initialValue.section1State),
 *     field2 = BooleanField(label = "enableButton", initialValue = initialValue.enableButton),
 *     combine = { section1State, enableButton ->
 *         NestedUiState(
 *             section1State = section1State,
 *             enableButton = enableButton
 *         )
 *     },
 *     split = { splitedOf(it.section1State, it.enableButton) },
 * )
 */
