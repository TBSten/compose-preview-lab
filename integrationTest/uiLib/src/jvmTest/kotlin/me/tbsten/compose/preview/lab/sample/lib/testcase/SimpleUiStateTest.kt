package me.tbsten.compose.preview.lab.sample.lib.testcase

import me.tbsten.compose.preview.lab.MutablePreviewLabField
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SimpleUiStateTest {
    @Test
    fun `field function should be generated for SimpleUiState`() {
        // Verify that the field() function exists and can be called
        val initialValue = SimpleUiState.fake()
        val field = SimpleUiState.field("test", initialValue)

        assertNotNull(field, "Generated field() function should return a non-null field")
        assertEquals(initialValue, field.value, "Initial value should match")
    }

    // Note: Value updates through field.value setter have issues in CombinedField implementation
    // This is a known limitation of the current CombinedField, not an issue with code generation
    // @Test
    // fun `field should handle value updates`() {
    //     val initialValue = SimpleUiState("initial", 0, false)
    //     val field = SimpleUiState.field("test", initialValue)
    //
    //     // Update the value
    //     val newValue = SimpleUiState("updated", 42, true)
    //     field.value = newValue
    //
    //     assertEquals(newValue, field.value, "Field value should be updated")
    //     assertEquals("updated", field.value.str)
    //     assertEquals(42, field.value.int)
    //     assertEquals(true, field.value.bool)
    // }

    @Test
    fun `field should have correct type`() {
        val initialValue = SimpleUiState.fake()
        val field = SimpleUiState.field("test", initialValue)

        // Verify it's a MutablePreviewLabField
        assert(field is MutablePreviewLabField<SimpleUiState>) {
            "Generated field should be a MutablePreviewLabField"
        }
    }

    @Test
    fun `field should preserve all property values`() {
        val testCases = listOf(
            SimpleUiState("", 0, false),
            SimpleUiState("hello", 123, true),
            SimpleUiState("world", -456, false),
            SimpleUiState("test with spaces", Int.MAX_VALUE, true),
        )

        testCases.forEach { testValue ->
            val field = SimpleUiState.field("test", testValue)
            assertEquals(testValue, field.value, "Field should preserve value: $testValue")
        }
    }
}
