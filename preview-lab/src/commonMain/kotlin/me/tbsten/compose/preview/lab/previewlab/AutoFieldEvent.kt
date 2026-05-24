package me.tbsten.compose.preview.lab.previewlab

import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.field.BooleanField
import me.tbsten.compose.preview.lab.field.ByteField
import me.tbsten.compose.preview.lab.field.DoubleField
import me.tbsten.compose.preview.lab.field.FloatField
import me.tbsten.compose.preview.lab.field.IntField
import me.tbsten.compose.preview.lab.field.LongField
import me.tbsten.compose.preview.lab.field.StringField

/**
 * Default label used by [autoField] / [autoEvent] when the compiler plugin cannot
 * (or has not been configured to) inject a more specific label.
 *
 * The compiler plugin replaces this default at call sites where [autoField] / [autoEvent]
 * appears as a named/positional argument of another call, by injecting the surrounding
 * parameter name (e.g. `name = autoField()` → `name = autoField(label = "name")`).
 */
@ExperimentalComposePreviewLabApi
public const val AutoDefaultLabel: String = "auto"

/**
 * Generates a default [me.tbsten.compose.preview.lab.PreviewLabField] for primitive types
 * so callers do not have to spell out `fieldValue { StringField("name", "") }` for every
 * parameter.
 *
 * # Usage
 *
 * ```kt
 * @Preview
 * @Composable
 * private fun UserCardPreview() = PreviewLab {
 *     UserCard(
 *         // The compiler plugin injects the parameter name as the field label,
 *         // so this is equivalent to `fieldValue { StringField("name", "") }`.
 *         name = autoField(),
 *         age = autoField(),
 *         isPremium = autoField(),
 *     )
 * }
 * ```
 *
 * # Supported types
 *
 * - [String] → [StringField]
 * - [Int] → [IntField]
 * - [Long] → [LongField]
 * - [Byte] → [ByteField]
 * - [Float] → [FloatField]
 * - [Double] → [DoubleField]
 * - [Boolean] → [BooleanField]
 *
 * For any other type, call [autoField] throws at runtime with a message pointing at the
 * existing `field { ... }` / `fieldValue { ... }` / `fieldState { ... }` APIs. The
 * compiler plugin does NOT rewrite unsupported types at compile time — this is intentional
 * so that runtime support for new types can be added without re-releasing the plugin.
 *
 * # Compiler plugin label injection
 *
 * When the Compose Preview Lab compiler plugin is applied to the module, a call site like
 * `name = autoField()` is rewritten to `name = autoField(label = "name")` during the IR
 * phase. Callers can override the injected label by passing [label] explicitly; the
 * plugin only fills in [AutoDefaultLabel] (the declared default), never an explicit
 * argument.
 *
 * When the plugin is **not** applied, [label] falls back to [AutoDefaultLabel] so the
 * field still renders (all fields named `"auto"` collapse into one inspector entry, which
 * is the expected degraded behavior — a clear hint that the plugin is missing).
 *
 * @param label Label shown in the inspector pane. The compiler plugin injects the
 * surrounding parameter name when possible.
 *
 * @see autoEvent
 */
@ExperimentalComposePreviewLabApi
@Composable
public inline fun <reified T> PreviewLabScope.autoField(label: String = AutoDefaultLabel): T {
    @Suppress("UNCHECKED_CAST")
    return when (T::class) {
        String::class -> fieldValue { StringField(label, "") } as T
        Int::class -> fieldValue { IntField(label, 0) } as T
        Long::class -> fieldValue { LongField(label, 0L) } as T
        Byte::class -> fieldValue { ByteField(label, 0) } as T
        Float::class -> fieldValue { FloatField(label, 0f) } as T
        Double::class -> fieldValue { DoubleField(label, 0.0) } as T
        Boolean::class -> fieldValue { BooleanField(label, false) } as T
        else -> error(
            "autoField<${T::class.simpleName}>() is not supported. " +
                "Use field { ... } / fieldValue { ... } / fieldState { ... } " +
                "with an explicit Field type, or open an issue at " +
                "https://github.com/TBSten/compose-preview-lab/issues/69 " +
                "to request a default Field for ${T::class.simpleName}.",
        )
    }
}

/**
 * Generates a `() -> Unit` callback that records an event via
 * [PreviewLabScope.onEvent] using the injected [label] as the title.
 *
 * # Usage
 *
 * ```kt
 * @Preview
 * @Composable
 * private fun MyButtonPreview() = PreviewLab {
 *     MyButton(
 *         text = "Click",
 *         // The compiler plugin injects "onClick" as the event title.
 *         onClick = autoEvent(),
 *     )
 * }
 * ```
 *
 * # Compiler plugin label injection
 *
 * When the Compose Preview Lab compiler plugin is applied to the module, a call site like
 * `onClick = autoEvent()` is rewritten to `onClick = autoEvent(label = "onClick")` during
 * the IR phase. Callers can override the injected label by passing [label] explicitly.
 *
 * When the plugin is **not** applied, [label] falls back to [AutoDefaultLabel] — all
 * recorded events share the same title, which is the expected degraded behavior.
 *
 * @param label Title shown on the event toast and in the Events tab. The compiler plugin
 * injects the surrounding parameter name when possible.
 *
 * @see autoField
 */
@ExperimentalComposePreviewLabApi
public fun PreviewLabScope.autoEvent(label: String = AutoDefaultLabel): () -> Unit = {
    onEvent(title = label)
}
