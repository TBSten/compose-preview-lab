package me.tbsten.compose.preview.lab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import kotlin.time.ExperimentalTime
import me.tbsten.compose.preview.lab.ui.components.Text

interface PreviewLabAction<R> {
    val label: String
    val argFields: List<PreviewLabField<*>>
    val action: suspend () -> R

    @Composable
    fun ResultsView(field: () -> PreviewLabField<R>? = { null }) {
        val field = remember {
            field() /* ?.readonly(value = null) */
        }

        if (field != null) {
            field.View()
        } else {
            Text("TODO")
        }
    }

    class ArgFieldsScope internal constructor() {
        operator fun <A1, A2> PreviewLabField<A1>.plus(other: PreviewLabField<A2>) = Pair(this, other)

        operator fun <A1, A2, A3> Pair<PreviewLabField<A1>, PreviewLabField<A2>>.plus(other: PreviewLabField<A3>) =
            Triple(this.first, this.second, other)
    }
}

@Immutable
@OptIn(ExperimentalTime::class)
class PreviewLabAction0<R>(override val label: String, override val action: suspend () -> R) : PreviewLabAction<R> {
    override val argFields: List<PreviewLabField<*>> = emptyList()
}

@Immutable
@OptIn(ExperimentalTime::class)
class PreviewLabAction1<A1, R>(
    override val label: String,
    argFields: PreviewLabAction.ArgFieldsScope.() -> PreviewLabField<A1>,
    action: suspend (A1) -> R,
) : PreviewLabAction<R> {
    override val argFields: List<PreviewLabField<*>> = listOf(argFields(PreviewLabAction.ArgFieldsScope()))
    override val action: suspend () -> R = { action(this.argFields[0].value as A1) }
}

@Immutable
@OptIn(ExperimentalTime::class)
class PreviewLabAction2<A1, A2, R>(
    override val label: String,
    argFields: PreviewLabAction.ArgFieldsScope.() -> Pair<PreviewLabField<A1>, PreviewLabField<A2>>,
    action: suspend (A1, A2) -> R,
) : PreviewLabAction<R> {
    override val argFields: List<PreviewLabField<*>> = argFields(PreviewLabAction.ArgFieldsScope()).toList()
    override val action: suspend () -> R = { action(this.argFields[0].value as A1, this.argFields[1].value as A2) }
}

@Immutable
@OptIn(ExperimentalTime::class)
class PreviewLabAction3<A1, A2, A3, R>(
    override val label: String,
    argFields: PreviewLabAction.ArgFieldsScope.() -> Triple<PreviewLabField<A1>, PreviewLabField<A2>, PreviewLabField<A3>>,
    action: suspend (A1, A2, A3) -> R,
) : PreviewLabAction<R> {
    override val argFields: List<PreviewLabField<*>> = argFields(PreviewLabAction.ArgFieldsScope()).toList()
    override val action: suspend () -> R =
        { action(this.argFields[0].value as A1, this.argFields[1].value as A2, this.argFields[2].value as A3) }
}

// TODO PreviewLabAction4...32
