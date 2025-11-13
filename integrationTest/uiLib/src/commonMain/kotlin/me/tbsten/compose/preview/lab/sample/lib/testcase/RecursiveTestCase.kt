package me.tbsten.compose.preview.lab.sample.lib.testcase

import me.tbsten.compose.preview.lab.generatecombinedfield.GenerateCombinedField

/**
 * Test case for recursive field generation without explicit @GenerateCombinedField annotation
 *
 * ChildState does NOT have @GenerateCombinedField, but it's a data class with companion object
 * ParentState DOES have @GenerateCombinedField and contains ChildState
 *
 * Expected: The KSP should recognize ChildState as a data class with companion object
 * and generate field() for it recursively
 */

// This data class does NOT have @GenerateCombinedField annotation
data class ChildState(val title: String, val count: Int,) {
    companion object
}

fun ChildState.Companion.fake() = ChildState(
    title = "Child Title",
    count = 10,
)

// This data class HAS @GenerateCombinedField annotation
// and contains ChildState which doesn't have the annotation
@GenerateCombinedField
data class ParentState(val child: ChildState, val enabled: Boolean,) {
    companion object
}

fun ParentState.Companion.fake() = ParentState(
    child = ChildState.fake(),
    enabled = true,
)

/*
 * Expected generated code for ParentState:
 *
 * fun ParentState.Companion.field(label: String, initialValue: ParentState) = CombinedField2(
 *     label = label,
 *     field1 = ChildState.field(label = "child", initialValue = initialValue.child), // Should use field() recursively
 *     field2 = BooleanField(label = "enabled", initialValue = initialValue.enabled),
 *     combine = { child, enabled -> ParentState(child = child, enabled = enabled) },
 *     split = { splitedOf(it.child, it.enabled) },
 * )
 *
 * And for ChildState (generated recursively):
 *
 * fun ChildState.Companion.field(label: String, initialValue: ChildState) = CombinedField2(
 *     label = label,
 *     field1 = StringField(label = "title", initialValue = initialValue.title),
 *     field2 = IntField(label = "count", initialValue = initialValue.count),
 *     combine = { title, count -> ChildState(title = title, count = count) },
 *     split = { splitedOf(it.title, it.count) },
 * )
 */
