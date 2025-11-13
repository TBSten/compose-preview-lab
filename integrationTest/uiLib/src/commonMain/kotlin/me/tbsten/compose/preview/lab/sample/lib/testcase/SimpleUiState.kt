package me.tbsten.compose.preview.lab.sample.lib.testcase

import me.tbsten.compose.preview.lab.generatecombinedfield.GenerateCombinedField

/**
 * Test case 1: Simple data class with primitive types only
 *
 * This should generate:
 * fun SimpleUiState.Companion.field(label: String, initialValue: SimpleUiState): MutablePreviewLabField<SimpleUiState> =
 *     CombinedField3(
 *         label = label,
 *         field1 = StringField("str", initialValue = initialValue.str),
 *         field2 = IntField("int", initialValue = initialValue.int),
 *         field3 = BooleanField("bool", initialValue = initialValue.bool),
 *         combine = { str, int, bool -> SimpleUiState(str = str, int = int, bool = bool) },
 *         split = { splitedOf(it.str, it.int, it.bool) },
 *     )
 */
@GenerateCombinedField
data class SimpleUiState(
    val str: String,
    val int: Int,
    val bool: Boolean,
) {
    companion object
}

fun SimpleUiState.Companion.fake() = SimpleUiState(
    str = "Sample Text",
    int = 42,
    bool = true,
)
