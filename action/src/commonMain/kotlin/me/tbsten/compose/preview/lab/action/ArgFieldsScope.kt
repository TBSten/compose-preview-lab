package me.tbsten.compose.preview.lab.action

import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabField

/**
 * Scope for defining argument fields for actions.
 *
 * This scope provides operators for combining multiple [PreviewLabField]s
 * to create multi-argument actions.
 *
 * ## Usage
 *
 * ```kotlin
 * val action = PreviewLabAction(
 *     label = "My Action",
 *     argFields = {
 *         // Single argument
 *         StringField("Name", "default")
 *
 *         // Or multiple arguments using + operator
 *         StringField("Name", "default") + IntField("Count", 1)
 *     },
 *     action = { args -> /* execute action */ }
 * )
 * ```
 *
 * @see PreviewLabField
 */
@ExperimentalComposePreviewLabApi
class ArgFieldsScope {
    /**
     * Combines two fields into a [Pair] for actions with two arguments.
     */
    operator fun <A1, A2> PreviewLabField<A1>.plus(other: PreviewLabField<A2>): Pair<PreviewLabField<A1>, PreviewLabField<A2>> =
        Pair(this, other)

    /**
     * Combines a [Pair] of fields with another field to create a [Triple] for actions with three arguments.
     */
    operator fun <A1, A2, A3> Pair<PreviewLabField<A1>, PreviewLabField<A2>>.plus(
        other: PreviewLabField<A3>
    ): Triple<PreviewLabField<A1>, PreviewLabField<A2>, PreviewLabField<A3>> = Triple(this.first, this.second, other)
}
