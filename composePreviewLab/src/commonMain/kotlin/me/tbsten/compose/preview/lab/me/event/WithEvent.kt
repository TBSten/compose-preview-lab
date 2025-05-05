package me.tbsten.compose.preview.lab.me.event

import me.tbsten.compose.preview.lab.me.PreviewLabScope

fun PreviewLabScope.withEvent(
    title: String,
    description: String? = null,
    block: () -> Unit
): () -> Unit = {
    block()
    onEvent(
        title = title,
        description = description,
    )
}

fun <Arg1> PreviewLabScope.withEvent(
    title: String,
    description: String? = null,
    block: (Arg1) -> Unit
): (Arg1) -> Unit = { arg1 ->
    block(arg1)
    onEvent(
        title = title,
        description = description,
    )
}

fun <Arg1, Arg2> PreviewLabScope.withEvent(
    title: String,
    description: String? = null,
    block: (Arg1, Arg2) -> Unit
): (Arg1, Arg2) -> Unit = { arg1, arg2 ->
    block(arg1, arg2)
    onEvent(
        title = title,
        description = description,
    )
}

fun <Arg1, Arg2, Arg3> PreviewLabScope.withEvent(
    title: String,
    description: String? = null,
    block: (Arg1, Arg2, Arg3) -> Unit
): (Arg1, Arg2, Arg3) -> Unit = { arg1, arg2, arg3 ->
    block(arg1, arg2, arg3)
    onEvent(
        title = title,
        description = description,
    )
}
