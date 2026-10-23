@file:OptIn(ExperimentalTime::class)

package me.tbsten.compose.preview.lab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import me.tbsten.compose.preview.lab.action.DefaultResultsView
import me.tbsten.compose.preview.lab.action.defaultResultsViewField

/** Key identifying one execution of a [PreviewLabAction] (epoch millis of its start by default). */
typealias PreviewLabActionDoActionKey = Long

/**
 * An executable, parameterised action registered from a preview (`PreviewLabScope.action(...)`).
 *
 * Actions show up in the "Actions" inspector tab: their [argFields] can be edited and [doAction]
 * runs the suspending [action], keeping a history of every run in [doActionStatusList] that
 * [ResultsView] renders inside the preview.
 */
@ExperimentalComposePreviewLabApi
interface PreviewLabAction<R> {
    val label: String
    val argFields: List<PreviewLabField<*>>
    val action: suspend () -> R

    val doActionStatusList: Map<PreviewLabActionDoActionKey, DoActionStatus<R>>

    @OptIn(ExperimentalTime::class)
    suspend fun doAction(key: PreviewLabActionDoActionKey = Clock.System.now().toEpochMilliseconds()): Result<R>
    fun removeDoActionStatus(key: PreviewLabActionDoActionKey)
    fun removeAllDoActionStatus()

    @Composable
    fun ResultsView(field: PreviewLabAction<R>.(R) -> PreviewLabField<R>? = defaultResultsViewField()) {
        DefaultResultsView(
            field = field,
        )
    }

    @ExperimentalComposePreviewLabApi
    class ArgFieldsScope internal constructor() {
        operator fun <A1, A2> PreviewLabField<A1>.plus(other: PreviewLabField<A2>) = Pair(this, other)

        operator fun <A1, A2, A3> Pair<PreviewLabField<A1>, PreviewLabField<A2>>.plus(other: PreviewLabField<A3>) =
            Triple(this.first, this.second, other)

        // TODO <A1, A2, A3, A4, ...>.plus()
    }

    @ExperimentalComposePreviewLabApi
    sealed interface DoActionStatus<R> {
        val startTime: Instant
        val result: Result<R>?

        @ExperimentalComposePreviewLabApi
        data class Running<R>(val previousResult: Result<R>?, override val startTime: Instant = Clock.System.now()) :
            DoActionStatus<R> {
            override val result: Result<R>? = previousResult
        }

        @ExperimentalComposePreviewLabApi
        data class Done<R>(
            override val result: Result<R>,
            override val startTime: Instant,
            val endTime: Instant = Clock.System.now(),
        ) : DoActionStatus<R> {
            val duration = endTime - startTime
        }
    }
}

/** [PreviewLabAction] base that records every [doAction] run in a sorted, observable status map. */
@ExperimentalComposePreviewLabApi
abstract class DefaultPreviewLabAction<R> : PreviewLabAction<R> {
    @Suppress("ktlint:standard:backing-property-naming", "PropertyName")
    protected val _doActionStatusList = mutableStateMapOf<PreviewLabActionDoActionKey, PreviewLabAction.DoActionStatus<R>>()
    override val doActionStatusList: Map<PreviewLabActionDoActionKey, PreviewLabAction.DoActionStatus<R>> by derivedStateOf {
        _doActionStatusList.entries.sortedByDescending { it.key }.associate { it.key to it.value }
    }

    override suspend fun doAction(key: PreviewLabActionDoActionKey): Result<R> {
        val startTime = Clock.System.now()
        _doActionStatusList[key] = PreviewLabAction.DoActionStatus.Running(
            startTime = startTime,
            previousResult = _doActionStatusList[key]?.result,
        )

        val actionResult = runCatching { action() }
        if (key in _doActionStatusList.keys) {
            _doActionStatusList[key] =
                PreviewLabAction.DoActionStatus.Done(
                    result = actionResult,
                    startTime = startTime,
                )
        }

        return actionResult
    }

    override fun removeDoActionStatus(key: PreviewLabActionDoActionKey) {
        _doActionStatusList.remove(key)
    }

    override fun removeAllDoActionStatus() {
        _doActionStatusList.clear()
    }
}

internal fun <Value> PreviewLabField<Value>.readonly(value: Value = this.value) = object : PreviewLabField<Value> by this {
    override var value: Value
        get() = value
        set(_) {}
}

@Immutable
@ExperimentalComposePreviewLabApi
class PreviewLabAction0<R>(override val label: String, override val action: suspend () -> R) : DefaultPreviewLabAction<R>() {
    override val argFields: List<PreviewLabField<*>> = emptyList()
}

@Immutable
@ExperimentalComposePreviewLabApi
class PreviewLabAction1<A1, R>(
    override val label: String,
    argFields: PreviewLabAction.ArgFieldsScope.() -> PreviewLabField<A1>,
    action: suspend (A1) -> R,
) : DefaultPreviewLabAction<R>() {
    override val argFields: List<PreviewLabField<*>> = listOf(argFields(PreviewLabAction.ArgFieldsScope()))
    override val action: suspend () -> R = { action(this.argFields[0].value as A1) }
}

@Immutable
@ExperimentalComposePreviewLabApi
class PreviewLabAction2<A1, A2, R>(
    override val label: String,
    argFields: PreviewLabAction.ArgFieldsScope.() -> Pair<PreviewLabField<A1>, PreviewLabField<A2>>,
    action: suspend (A1, A2) -> R,
) : DefaultPreviewLabAction<R>() {
    override val argFields: List<PreviewLabField<*>> = argFields(PreviewLabAction.ArgFieldsScope()).toList()
    override val action: suspend () -> R = { action(this.argFields[0].value as A1, this.argFields[1].value as A2) }
}

@Immutable
@ExperimentalComposePreviewLabApi
class PreviewLabAction3<A1, A2, A3, R>(
    override val label: String,
    argFields: PreviewLabAction.ArgFieldsScope.() -> Triple<PreviewLabField<A1>, PreviewLabField<A2>, PreviewLabField<A3>>,
    action: suspend (A1, A2, A3) -> R,
) : DefaultPreviewLabAction<R>() {
    override val argFields: List<PreviewLabField<*>> = argFields(PreviewLabAction.ArgFieldsScope()).toList()
    override val action: suspend () -> R =
        { action(this.argFields[0].value as A1, this.argFields[1].value as A2, this.argFields[2].value as A3) }
}

// TODO PreviewLabAction4...32
