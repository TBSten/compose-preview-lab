package me.tbsten.compose.preview.lab.previewlab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlin.time.ExperimentalTime
import kotlinx.serialization.json.Json
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
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
     * Creates a field and returns the [PreviewLabField] instance itself.
     * Unlike [fieldState], which returns a [MutableState] for use with Kotlin's property delegation,
     * this function returns the field instance directly, giving access to field properties and methods.
     *
     * Use [fieldState] if you only need the value with property delegation syntax (`var x by fieldState { ... }`).
     * Use [fieldValue] if you do not need to update the value.
     *
     * ```kt
     * PreviewLab {
     *   val myTextField = field { StringField("myText", "initialValue") }
     *
     *   TextField(
     *     value = myTextField.value,
     *     onValueChange = { myTextField.value = it },
     *   )
     * }
     * ```
     */
    @Composable
    fun <Field : PreviewLabField<*>> field(key: String? = null, builder: FieldBuilderScope.() -> Field): Field {
        val urlParams = LocalUrlParams.current
        val field = remember(key1 = key) {
            builder(FieldBuilderScope()).also { field ->
                restoreFieldValueFromUrl(field, urlParams)
            }
        }
        DisposableEffect(Unit) {
            state.fields.add(field)
            onDispose {
                state.fields.remove(field)
                field.onCleared()
            }
        }
        return field
    }

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
        val urlParams = LocalUrlParams.current
        val field = remember(key1 = key) {
            builder(FieldBuilderScope()).also { field ->
                restoreFieldValueFromUrl(field, urlParams)
            }
        }
        DisposableEffect(Unit) {
            state.fields.add(field)
            onDispose {
                state.fields.remove(field)
                field.onCleared()
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
        val urlParams = LocalUrlParams.current
        val field = remember(key1 = key) {
            builder(PreviewLabScope.FieldBuilderScope()).also { field ->
                restoreFieldValueFromUrl(field, urlParams)
            }
        }
        DisposableEffect(Unit) {
            state.fields.add(field)
            onDispose {
                state.fields.remove(field)
                field.onCleared()
            }
        }
        return field.value
    }

    /**
     * Registers an action without arguments. It appears in the Actions inspector tab, where it can
     * be run; the returned [PreviewLabAction0] exposes its run history (`ResultsView()`).
     *
     * ```kt
     * PreviewLab {
     *   action(label = "onRefresh", action = viewModel::onRefresh)
     * }
     * ```
     *
     * @param label Label shown in the Actions tab.
     * @param key Changing the key re-creates (and re-registers) the action.
     */
    @ExperimentalComposePreviewLabApi
    @Composable
    fun <R> action(label: String, key: Any? = null, action: suspend () -> R): PreviewLabAction0<R> =
        registerAction(key) { PreviewLabAction0(label = label, action = action) }

    /**
     * Registers an action with one argument, edited through the field built by [argFields].
     *
     * ```kt
     * action(
     *   label = "onItemClick",
     *   argFields = { StringField("itemId", "item-id-1") },
     *   action = viewModel::onItemClick,
     * )
     * ```
     */
    @ExperimentalComposePreviewLabApi
    @Composable
    fun <A1, R> action(
        label: String,
        key: Any? = null,
        argFields: PreviewLabAction.ArgFieldsScope.() -> PreviewLabField<A1>,
        action: suspend (A1) -> R,
    ): PreviewLabAction1<A1, R> =
        registerAction(key) { PreviewLabAction1(label = label, argFields = argFields, action = action) }

    /**
     * Registers an action with two arguments; combine the fields with `+` inside [argFields].
     *
     * ```kt
     * action(
     *   label = "GetItemListUseCase.invoke",
     *   argFields = { BooleanField("desc", false) + BooleanField("willError", false) },
     *   action = getItemUseCase::invoke,
     * )
     * ```
     */
    @ExperimentalComposePreviewLabApi
    @Composable
    fun <A1, A2, R> action(
        label: String,
        key: Any? = null,
        argFields: PreviewLabAction.ArgFieldsScope.() -> Pair<PreviewLabField<A1>, PreviewLabField<A2>>,
        action: suspend (A1, A2) -> R,
    ): PreviewLabAction2<A1, A2, R> =
        registerAction(key) { PreviewLabAction2(label = label, argFields = argFields, action = action) }

    /** Registers an action with three arguments; combine the fields with `+` inside [argFields]. */
    @ExperimentalComposePreviewLabApi
    @Composable
    fun <A1, A2, A3, R> action(
        label: String,
        key: Any? = null,
        argFields: PreviewLabAction.ArgFieldsScope.() -> Triple<PreviewLabField<A1>, PreviewLabField<A2>, PreviewLabField<A3>>,
        action: suspend (A1, A2, A3) -> R,
    ): PreviewLabAction3<A1, A2, A3, R> =
        registerAction(key) { PreviewLabAction3(label = label, argFields = argFields, action = action) }

    // TODO Action<A1, A2, A3, ..., R>

    @ExperimentalComposePreviewLabApi
    @Composable
    private fun <A : PreviewLabAction<*>> registerAction(key: Any?, create: () -> A): A {
        val action = remember(key) { create() }
        DisposableEffect(action) {
            state.actions.add(action)
            onDispose {
                state.actions.remove(action)
            }
        }
        return action
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

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/**
 * URLパラメータからFieldの値を復元する。
 * Fieldのlabelをキーとして、URLパラメータから値を取得し、
 * serializerを使ってデシリアライズして値を設定する。
 */
@Suppress("UNCHECKED_CAST")
private fun <Value> restoreFieldValueFromUrl(field: PreviewLabField<Value>, urlParams: UrlParams) {
    val paramValue = urlParams[field.label] ?: return
    val serializer = field.serializer() ?: return

    try {
        val restoredValue = json.decodeFromString(serializer, paramValue)
        if (field is MutablePreviewLabField<Value>) {
            field.value = restoredValue
        }
    } catch (e: Exception) {
        // デシリアライズに失敗した場合は無視（初期値を使用）
        println("Failed to restore field '${field.label}' from URL: ${e.message}")
    }
}
