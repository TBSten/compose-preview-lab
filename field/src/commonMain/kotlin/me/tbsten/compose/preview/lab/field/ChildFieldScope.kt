package me.tbsten.compose.preview.lab.field

import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi

/**
 * Scope provided when creating child fields in auto-generated field factories.
 *
 * This class is used by the KSP-generated code to provide context when creating
 * child fields for data classes and sealed classes. It carries the label and
 * initial value for each child field.
 *
 * ## Usage in Generated Code
 *
 * For a data class like:
 * ```kotlin
 * data class User(val name: String, val age: Int)
 * ```
 *
 * The generated factory uses ChildFieldScope to pass context to child field factories:
 * ```kotlin
 * fun Fields.user(
 *     label: String,
 *     initialValue: User,
 *     nameField: ChildFieldScope<String>.() -> MutablePreviewLabField<String> = Fields.user.nameField(),
 *     ageField: ChildFieldScope<Int>.() -> MutablePreviewLabField<Int> = Fields.user.ageField(),
 * ): PreviewLabField<User> = CombinedField2(
 *     label = label,
 *     field1 = nameField(ChildFieldScope("name", initialValue.name)),
 *     field2 = ageField(ChildFieldScope("age", initialValue.age)),
 *     combine = { name, age -> User(name, age) },
 *     split = { splitedOf(it.name, it.age) },
 * )
 * ```
 *
 * @param Value The type of the child field's value
 * @property label The label for the child field (property name in lowerCamelCase)
 * @property initialValue The initial value extracted from the parent object
 */
@ExperimentalComposePreviewLabApi
class ChildFieldScope<Value>(val label: String, val initialValue: Value,)
