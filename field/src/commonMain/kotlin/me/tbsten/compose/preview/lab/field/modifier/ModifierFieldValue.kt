package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Represents a value that can be applied as a Modifier to Compose components.
 *
 * ModifierFieldValue allows dynamic configuration of UI modifiers through the Preview Lab interface.
 * Each implementation represents a specific modifier (e.g., padding, background, size) that can be
 * applied to preview components.
 *
 * @see ModifierFieldValueList
 */
public interface ModifierFieldValue {
    /**
     * Creates a modifier from this field value.
     *
     * @return A Modifier that applies the configuration represented by this field value
     */
    public fun Modifier.createModifier(): Modifier

    /**
     * Renders the UI builder interface for configuring this modifier.
     * This composable is typically displayed in the Preview Lab control panel.
     */
    @Composable
    public fun Builder()

    /**
     * Combines this modifier field value with another to create a list.
     *
     * @param other The modifier field value to combine with this one
     * @return A ModifierFieldValueList containing both values
     */
    public fun then(other: ModifierFieldValue): ModifierFieldValueList = ModifierFieldValueList(this, other)

    public companion object : ModifierFieldValueList()
}

internal fun createModifierFrom(value: ModifierFieldValue) = Modifier.then(with(value) { Modifier.createModifier() })

/**
 * A list of ModifierFieldValue objects that can be combined to create a composite modifier.
 *
 * This class allows chaining multiple modifier field values together and applying them
 * as a single modifier to Compose components in the preview.
 *
 * @param values The list of modifier field values to combine
 */
public open class ModifierFieldValueList(private val values: List<ModifierFieldValue>) : List<ModifierFieldValue> by values {
    public constructor(vararg values: ModifierFieldValue) : this(values = values.toList())

    /**
     * Creates a combined modifier from all values in this list.
     *
     * @return A Modifier that applies all field values in sequence
     */
    public fun createModifier(): Modifier = values.fold<ModifierFieldValue, Modifier>(Modifier) { modifier, value ->
        modifier.then(createModifierFrom(value))
    }

    /**
     * Adds another modifier field value to this list.
     *
     * @param other The modifier field value to add
     * @return A new ModifierFieldValueList containing all existing values plus the new one
     */
    public fun then(other: ModifierFieldValue): ModifierFieldValueList = ModifierFieldValueList(
        this.values + other,
    )

    /**
     * Combines this list with another ModifierFieldValueList.
     *
     * @param other The other list to combine with this one
     * @return A new ModifierFieldValueList containing values from both lists
     */
    public fun then(other: ModifierFieldValueList): ModifierFieldValueList = ModifierFieldValueList(
        this.values + other.values,
    )

    public companion object : ModifierFieldValueList()
}
