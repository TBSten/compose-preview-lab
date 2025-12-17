package me.tbsten.compose.preview.lab.previewlab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlin.time.ExperimentalTime
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.PreviewLabAction
import me.tbsten.compose.preview.lab.PreviewLabAction0
import me.tbsten.compose.preview.lab.PreviewLabAction1
import me.tbsten.compose.preview.lab.PreviewLabAction2
import me.tbsten.compose.preview.lab.PreviewLabAction3
import me.tbsten.compose.preview.lab.PreviewLabEvent
import me.tbsten.compose.preview.lab.PreviewLabField

/**
 * The scope of the [PreviewLab], which provides methods to create fields, handle events, and manage layout nodes.
 *
 * @see PreviewLabField
 * @see PreviewLabEvent
 */
@OptIn(ExperimentalTime::class)
class PreviewLabScope(val state: PreviewLabState) {
    private var onEffectHandler: (Effect) -> Unit = {}

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
        val field = remember(key1 = key) { builder(PreviewLabScope.FieldBuilderScope()) }
        DisposableEffect(Unit) {
            state.fields.add(field)
            onDispose {
                state.fields.remove(field)
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
        val field = remember(key1 = key) { builder(PreviewLabScope.FieldBuilderScope()) }
        DisposableEffect(Unit) {
            state.fields.add(field)
            onDispose {
                state.fields.remove(field)
            }
        }
        return field.value
    }

    @Composable
    fun <R> action(label: String, key: Any? = null, action: suspend () -> R): PreviewLabAction0<R> {
        val action = remember(key) { PreviewLabAction0(label = label, action = action) }
        DisposableEffect(action) {
            state.actions.add(action)
            onDispose {
                state.actions.remove(action)
            }
        }
        return action
    }

    @Composable
    fun <A1, R> action(
        label: String,
        key: Any? = null,
        argFields: PreviewLabAction.ArgFieldsScope.() -> PreviewLabField<A1>,
        action: suspend (A1) -> R,
    ): PreviewLabAction1<A1, R> {
        val action = remember(key) { PreviewLabAction1(label = label, argFields = argFields, action = action) }
        DisposableEffect(key) {
            state.actions.add(action)
            onDispose {
                state.actions.remove(action)
            }
        }
        return action
    }

    @Composable
    fun <A1, A2, R> action(
        label: String,
        key: Any? = null,
        argFields: PreviewLabAction.ArgFieldsScope.() -> Pair<PreviewLabField<A1>, PreviewLabField<A2>>,
        action: suspend (A1, A2) -> R,
    ): PreviewLabAction2<A1, A2, R> {
        val action = PreviewLabAction2(label = label, argFields = argFields, action = action)
        DisposableEffect(key) {
            state.actions.add(action)
            onDispose {
                state.actions.remove(action)
            }
        }
        return action
    }

    @Composable
    fun <A1, A2, A3, R> action(
        label: String,
        key: Any? = null,
        argFields: PreviewLabAction.ArgFieldsScope.() -> Triple<PreviewLabField<A1>, PreviewLabField<A2>, PreviewLabField<A3>>,
        action: suspend (A1, A2, A3) -> R,
    ): PreviewLabAction3<A1, A2, A3, R> {
        val action = PreviewLabAction3(label = label, argFields = argFields, action = action)
        DisposableEffect(key) {
            state.actions.add(action)
            onDispose {
                state.actions.remove(action)
            }
        }
        return action
    }

    // TODO Action<A1, A2, A3, ..., R>

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
        state.events.add(event)
        onEffectHandler.invoke(Effect.ShowEventToast(event = event))
    }

    @Composable
    internal fun HandleEffect(onEffect: (Effect) -> Unit) {
        onEffectHandler = rememberUpdatedState(onEffect).value
    }

    internal sealed interface Effect {
        data class ShowEventToast(val event: PreviewLabEvent) : Effect
    }

    class FieldBuilderScope
}
