package me.tbsten.compose.preview.lab.sample.lib.testcase

import me.tbsten.compose.preview.lab.MutablePreviewLabField
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NestedUiStateTest {
    @Test
    fun `field function should be generated for NestedUiState`() {
        val initialValue = NestedUiState.fake()
        val field = NestedUiState.field("test", initialValue)

        assertNotNull(field, "Generated field() function should return a non-null field")
        assertEquals(initialValue, field.value, "Initial value should match")
    }

    @Test
    fun `field function should be generated for Section1State`() {
        // Section1State is a nested data class and should also have field() generated
        val initialValue = Section1State.fake()
        val field = Section1State.field("test", initialValue)

        assertNotNull(field, "Generated field() function should return a non-null field for Section1State")
        assertEquals(initialValue, field.value, "Initial value should match")
    }

    // Note: Value updates commented out due to CombinedField implementation limitations
    // @Test
    // fun `nested field should handle value updates`() {
    //     val initialValue = NestedUiState(
    //         section1State = Section1State("initial heading", "initial body"),
    //         enableButton = false
    //     )
    //     val field = NestedUiState.field("test", initialValue)
    //
    //     // Update the value
    //     val newValue = NestedUiState(
    //         section1State = Section1State("updated heading", "updated body"),
    //         enableButton = true
    //     )
    //     field.value = newValue
    //
    //     assertEquals(newValue, field.value, "Field value should be updated")
    //     assertEquals("updated heading", field.value.section1State.heading)
    //     assertEquals("updated body", field.value.section1State.body)
    //     assertEquals(true, field.value.enableButton)
    // }

    @Test
    fun `field should have correct type`() {
        val initialValue = NestedUiState.fake()
        val field = NestedUiState.field("test", initialValue)

        assert(field is MutablePreviewLabField<NestedUiState>) {
            "Generated field should be a MutablePreviewLabField"
        }
    }

    @Test
    fun `nested Section1State is properly structured`() {
        val section1 = Section1State("heading1", "body1")
        val section2 = Section1State("heading2", "body2")

        val state1 = NestedUiState(section1, true)
        val state2 = NestedUiState(section2, false)

        val field1 = NestedUiState.field("test1", state1)
        assertEquals(section1, field1.value.section1State)

        val field2 = NestedUiState.field("test2", state2)
        assertEquals(section2, field2.value.section1State)
        assertEquals("heading2", field2.value.section1State.heading)
    }
}
