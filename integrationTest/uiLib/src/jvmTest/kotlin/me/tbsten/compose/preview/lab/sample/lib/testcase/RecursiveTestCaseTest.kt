package me.tbsten.compose.preview.lab.sample.lib.testcase

import me.tbsten.compose.preview.lab.MutablePreviewLabField
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Test for recursive field generation without explicit @GenerateCombinedField annotation
 *
 * This test verifies that:
 * 1. ParentState (with @GenerateCombinedField) has field() generated
 * 2. ChildState (WITHOUT @GenerateCombinedField but with companion object) also has field() generated
 * 3. The recursive dependency detection works correctly
 */
class RecursiveTestCaseTest {
    @Test
    fun `field function should be generated for ParentState`() {
        val initialValue = ParentState.fake()
        val field = ParentState.field("test", initialValue)

        assertNotNull(field, "Generated field() function should return a non-null field for ParentState")
        assertEquals(initialValue, field.value, "Initial value should match")
    }

    @Test
    fun `field function should be generated for ChildState without annotation`() {
        // This is the key test: ChildState does NOT have @GenerateCombinedField annotation
        // but it should still have field() generated because it's referenced by ParentState
        val initialValue = ChildState.fake()
        val field = ChildState.field("test", initialValue)

        assertNotNull(field, "Generated field() function should return a non-null field for ChildState")
        assertEquals(initialValue, field.value, "Initial value should match")
    }

    @Test
    fun `ParentState field properly contains nested ChildState`() {
        val child1 = ChildState("Child 1", 10)
        val child2 = ChildState("Child 2", 20)

        val parent1 = ParentState(child1, true)
        val parent2 = ParentState(child2, false)

        val field1 = ParentState.field("test1", parent1)
        assertEquals(child1, field1.value.child)
        assertEquals("Child 1", field1.value.child.title)
        assertEquals(10, field1.value.child.count)

        val field2 = ParentState.field("test2", parent2)
        assertEquals(child2, field2.value.child)
        assertEquals("Child 2", field2.value.child.title)
        assertEquals(20, field2.value.child.count)
        assertEquals(false, field2.value.enabled)
    }

    @Test
    fun `ChildState field preserves values correctly`() {
        val testCases = listOf(
            ChildState("Initial", 0),
            ChildState("Updated", 100),
            ChildState("Test", 999)
        )

        testCases.forEach { testValue ->
            val field = ChildState.field("test", testValue)
            assertEquals(testValue, field.value)
            assertEquals(testValue.title, field.value.title)
            assertEquals(testValue.count, field.value.count)
        }
    }

    @Test
    fun `field types should be correct`() {
        val parentField = ParentState.field("test", ParentState.fake())
        assert(parentField is MutablePreviewLabField<ParentState>) {
            "ParentState field should be a MutablePreviewLabField"
        }

        val childField = ChildState.field("test", ChildState.fake())
        assert(childField is MutablePreviewLabField<ChildState>) {
            "ChildState field should be a MutablePreviewLabField"
        }
    }

    @Test
    fun `recursive generation should preserve all nested properties`() {
        val testCases = listOf(
            ParentState(ChildState("A", 1), true),
            ParentState(ChildState("B", 2), false),
            ParentState(ChildState("Test with spaces", 999), true),
            ParentState(ChildState("", Int.MAX_VALUE), false),
        )

        testCases.forEach { testValue ->
            val field = ParentState.field("test", testValue)
            assertEquals(testValue, field.value, "Field should preserve value: $testValue")
            assertEquals(testValue.child.title, field.value.child.title)
            assertEquals(testValue.child.count, field.value.child.count)
            assertEquals(testValue.enabled, field.value.enabled)
        }
    }

    @Test
    fun `both parent and child should support independent field creation`() {
        // Create separate fields for parent and child
        val childField = ChildState.field("child", ChildState("Child Title", 5))
        val parentField = ParentState.field("parent", ParentState(ChildState("Parent's Child", 10), true))

        // They should be independent
        assertEquals("Child Title", childField.value.title)
        assertEquals(5, childField.value.count)

        assertEquals("Parent's Child", parentField.value.child.title)
        assertEquals(10, parentField.value.child.count)
        assertEquals(true, parentField.value.enabled)
    }
}
