package me.tbsten.compose.preview.lab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberUpdatedState
import io.github.takahirom.rin.RetainedObserver
import io.github.takahirom.rin.rememberRetained
import kotlin.time.ExperimentalTime
import me.tbsten.compose.preview.lab.event.PreviewLabEvent
import me.tbsten.compose.preview.lab.field.MutablePreviewLabField
import me.tbsten.compose.preview.lab.field.PreviewLabField

/**
 * The scope of the [PreviewLab], which provides methods to create fields, handle events, and manage layout nodes.
 *
 * @see PreviewLabField
 * @see PreviewLabEvent
 */
@OptIn(ExperimentalTime::class)
class PreviewLabScope internal constructor() {
    @InternalComposePreviewLabApi
    val fields = mutableStateListOf<PreviewLabField<*>>()
    internal val events = mutableStateListOf<PreviewLabEvent>()

    internal var onEffectHandler: (Effect) -> Unit = {}

    // field methods

    /**
     * Creates a mutable field that can be used to store and observe state in the Preview Lab.
     * Use [fieldValue] if you do not need to update the status.
     * This is useful, for example, for a `TextField`, where you want to use both the state value and its updates. For example, use the following.
     *
     * ```kt
     * PreviewLab {
     *   var myText by fieldState { StringField("myText", "initialValue") }
     *
     *   TextField(
     *     value = myText,
     *     onValueChange = { myText = it },
     *   )
     * }
     * ```
     */
    @Composable
    fun <Value> fieldState(
        key: String? = null,
        builder: FieldBuilderScope.() -> MutablePreviewLabField<Value>,
    ): MutableState<Value> {
        val field = rememberRetained(key = key) { builder(FieldBuilderScope()) }
        rememberRetained {
            object : RetainedObserver {
                override fun onRemembered() {
                    fields.add(field)
                }

                override fun onForgotten() {
                    fields.remove(field)
                }
            }
        }
        return field
    }

    /**
     * Creates a field that can be used to store and observe state in the Preview Lab.
     * Use this if you do not need to update the status.
     * Use when the change process does not need to be included in the Preview, for example, in the button text below.
     *
     * ```kt
     * PreviewLab {
     *   TextField(
     *     value = fieldValue { StringField("myText", "Click Me!") },
     *     onValueChange = {},
     *   )
     * }
     * ```
     */
    @Composable
    fun <Value> fieldValue(key: String? = null, builder: FieldBuilderScope.() -> PreviewLabField<out Value>): Value {
        val field = rememberRetained(key = key) { builder(FieldBuilderScope()) }
        rememberRetained {
            object : RetainedObserver {
                override fun onRemembered() {
                    fields.add(field)
                }

                override fun onForgotten() {
                    fields.remove(field)
                }
            }
        }
        return field.value
    }

    /**
     * Records an event in the Preview Lab.
     * When onEvent is called, Toast is displayed and the event is recorded on the Event tab in the right sidebar.
     * This is useful for manual testing of events that may occur in components.
     *
     * ```kt
     * PreviewLab {
     *   MyButton(
     *     ...,
     *     onClick = { onEvent(title = "MyButton.onClick") },
     *   )
     * }
     * ```
     *
     * @param title The title of the event. This is used for the toast display and also appears in the event list on the Events tab.
     * @param description It will not appear on the toast, but it will appear on the event tab. If you have a lot of information, use description instead of title to make the debug UI easier to read.
     */
    fun onEvent(title: String, description: String? = null) {
        val event = PreviewLabEvent(title = title, description = description)
        events.add(event)
        onEffectHandler.invoke(Effect.ShowEventToast(event = event))
    }

    @Composable
    internal fun HandleEffect(onEffect: (Effect) -> Unit) {
        onEffectHandler = rememberUpdatedState(onEffect).value
    }

    internal sealed interface Effect {
        data class ShowEventToast(val event: PreviewLabEvent) : Effect
    }

    class FieldBuilderScope internal constructor()
}
