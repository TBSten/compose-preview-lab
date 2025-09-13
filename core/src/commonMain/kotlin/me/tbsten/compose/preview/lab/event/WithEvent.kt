package me.tbsten.compose.preview.lab.event

import me.tbsten.compose.preview.lab.PreviewLabScope

/**
 * Create a function to record events. This is simply a short-hand for the onEvent call.
 *
 * ```kt
 * PreviewLab {
 *   var text by fieldState {StringField("text", "") }
 *   TextField(
 *     value = text,
 *     onValueChange = withEvent("text changed") { text = it },
 *   )
 * }
 * ```
 *
 * @param title title of the event to be passed to onEvent.
 * @param description description of the event to be passed to onEvent.
 * @param block Processing to be generated after the event is sent.
 */
fun PreviewLabScope.withEvent(title: String, description: String? = null, block: () -> Unit = {}): () -> Unit = {
    block()
    onEvent(
        title = title,
        description = description,
    )
}

/**
 * Create a function to record events. This is simply a short-hand for the onEvent call.
 *
 * ```kt
 * PreviewLab {
 *   var text by fieldState {StringField("text", "") }
 *   TextField(
 *     value = text,
 *     onValueChange = withEvent("text changed") { text = it },
 *   )
 * }
 * ```
 *
 * @param title title of the event to be passed to onEvent.
 * @param description description of the event to be passed to onEvent.
 * @param block Processing to be generated after the event is sent.
 */
fun <Arg1> PreviewLabScope.withEvent(title: String, description: String? = null, block: (Arg1) -> Unit = {}): (Arg1) -> Unit =
    { arg1 ->
        block(arg1)
        onEvent(
            title = title,
            description = description,
        )
    }

/**
 * Create a function to record events. This is simply a short-hand for the onEvent call.
 *
 * ```kt
 * PreviewLab {
 *   var text by fieldState {StringField("text", "") }
 *   TextField(
 *     value = text,
 *     onValueChange = withEvent("text changed") { text = it },
 *   )
 * }
 * ```
 *
 * @param title title of the event to be passed to onEvent.
 * @param description description of the event to be passed to onEvent.
 * @param block Processing to be generated after the event is sent.
 */
fun <Arg1, Arg2> PreviewLabScope.withEvent(
    title: String,
    description: String? = null,
    block: (Arg1, Arg2) -> Unit,
): (Arg1, Arg2) -> Unit = { arg1, arg2 ->
    block(arg1, arg2)
    onEvent(
        title = title,
        description = description,
    )
}

/**
 * Create a function to record events. This is simply a short-hand for the onEvent call.
 *
 * ```kt
 * PreviewLab {
 *   var text by fieldState {StringField("text", "") }
 *   TextField(
 *     value = text,
 *     onValueChange = withEvent("text changed") { text = it },
 *   )
 * }
 * ```
 *
 * @param title title of the event to be passed to onEvent.
 * @param description description of the event to be passed to onEvent.
 * @param block Processing to be generated after the event is sent.
 */
fun <Arg1, Arg2, Arg3> PreviewLabScope.withEvent(
    title: String,
    description: String? = null,
    block: (Arg1, Arg2, Arg3) -> Unit,
): (Arg1, Arg2, Arg3) -> Unit = { arg1, arg2, arg3 ->
    block(arg1, arg2, arg3)
    onEvent(
        title = title,
        description = description,
    )
}
