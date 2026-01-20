package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.runtime.Composable

/**
 * Factory interface for creating ModifierFieldValue instances with UI for configuration.
 *
 * This interface defines how to create and configure modifier field values through a UI builder.
 * Implementations provide the title, UI content, and creation logic for specific modifier types.
 *
 * @param V The specific type of ModifierFieldValue this factory creates
 */
public interface ModifierFieldValueFactory<V : ModifierFieldValue> {
    /**
     * The display title for this modifier type in the UI.
     */
    public val title: String

    /**
     * Whether this factory can currently create a new modifier field value.
     * Typically based on the current configuration state.
     */
    public val canCreate: Boolean get() = false

    /**
     * Renders the configuration UI content for this modifier.
     *
     * @param createButton A composable button that should be displayed to trigger creation
     */
    @Composable
    public fun Content(createButton: @Composable () -> Unit)

    /**
     * Creates a new modifier field value instance based on current configuration.
     *
     * @return Result containing the created modifier field value or an error
     */
    public fun create(): Result<V>
}

/**
 * Type alias for a list of ModifierFieldValueFactory instances.
 * Used to represent collections of different modifier factories.
 */
public typealias ModifierFieldValueFactories = List<ModifierFieldValueFactory<*>>
