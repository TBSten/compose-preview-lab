package me.tbsten.compose.preview.lab.field

import androidx.compose.runtime.Composable

/**
 * A field that wraps another [MutablePreviewLabField] with additional composable content.
 *
 * This class allows you to add decorations or modifications around an existing field's UI
 * without changing its underlying behavior. The wrapping can be applied to either
 * [View] (the complete field UI including label) or [Content] (only the input area),
 * controlled by [WrapRange].
 *
 * @param Value The type of value this field holds
 * @param baseField The underlying field to wrap
 * @param wrapRange Determines which part of the field to wrap. Defaults to [WrapRange.OnlyContent]
 * @param content A composable that receives the original content as a parameter and renders it with wrapping
 *
 * @see wrap Extension function to create a WrapField easily
 * @see WrapRange Enum defining the wrapping scope
 */
class WrapField<Value>(
    private val baseField: MutablePreviewLabField<Value>,
    private val wrapRange: WrapRange = WrapRange.OnlyContent,
    private val content: @Composable (@Composable () -> Unit) -> Unit,
) : MutablePreviewLabField<Value>(label = baseField.label, initialValue = baseField.initialValue) {
    override var value: Value by baseField::value

    @Composable
    override fun View() {
        if (wrapRange.wrapView) {
            content {
                super.View()
            }
        } else {
            super.View()
        }
    }

    @Composable
    override fun Content() {
        if (wrapRange.wrapContent) {
            content {
                baseField.Content()
            }
        } else {
            baseField.Content()
        }
    }
}

/**
 * Wraps this field with additional composable content.
 *
 * This extension function provides a convenient way to add decorations or modifications
 * around an existing field's UI. Common use cases include:
 * - Adding tooltips or speech bubbles
 * - Wrapping with animations
 * - Adding visual indicators or badges
 *
 * Example:
 * ```kotlin
 * IntField("Count", 0)
 *     .wrap { content ->
 *         Box {
 *             content()
 *             Badge(text = "New")
 *         }
 *     }
 * ```
 *
 * @param wrapRange Determines which part of the field to wrap. Defaults to [WrapRange.OnlyContent]
 * @param content A composable that receives the original content as a parameter and renders it with wrapping
 * @return A new [WrapField] that wraps this field
 *
 * @see WrapField
 * @see WrapRange
 */
fun <Value> MutablePreviewLabField<Value>.wrap(
    wrapRange: WrapRange = WrapRange.OnlyContent,
    content: @Composable (@Composable () -> Unit) -> Unit,
) = WrapField(
    baseField = this,
    wrapRange = wrapRange,
    content = content,
)

/**
 * Defines the scope of wrapping for a [WrapField].
 *
 * This enum controls which part of the field's UI is wrapped by the custom content.
 *
 * @property wrapView Whether to wrap the [View] (complete field UI including label)
 * @property wrapContent Whether to wrap the [Content] (only the input area)
 */
enum class WrapRange(internal val wrapView: Boolean, internal val wrapContent: Boolean) {
    /**
     * Wraps the entire field view including the label.
     *
     * Use this when you want to add decorations around the complete field UI,
     * such as speech bubbles or tooltips that should appear around the whole field.
     */
    Full(wrapView = true, wrapContent = false),

    /**
     * Wraps only the content/input area, excluding the label.
     *
     * Use this when you want to add decorations only around the input component,
     * leaving the label unchanged. This is the default behavior.
     */
    OnlyContent(wrapView = false, wrapContent = true),
}
