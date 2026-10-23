package me.tbsten.compose.preview.lab.previewlab

import androidx.compose.runtime.Composable
import kotlin.reflect.typeOf
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabField
import me.tbsten.compose.preview.lab.field.BooleanField
import me.tbsten.compose.preview.lab.field.ByteField
import me.tbsten.compose.preview.lab.field.DoubleField
import me.tbsten.compose.preview.lab.field.EnumField
import me.tbsten.compose.preview.lab.field.FloatField
import me.tbsten.compose.preview.lab.field.IntField
import me.tbsten.compose.preview.lab.field.LongField
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.nullable

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
 * Generates a default [PreviewLabField] for primitive types (and their nullable variants)
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
 *         // `String?` → `StringField("nickname", "").nullable(initialValue = null)`
 *         nickname = autoField(),
 *         // enum class → `EnumField("plan", initialValue = Plan.entries.first())`
 *         // (rewritten to [autoEnumField] by the compiler plugin)
 *         plan = autoField(),
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
 * - Any of the above as a nullable type (`String?`, `Int?`, …) → the same field wrapped with
 *   [nullable], starting at `null`
 * - `enum class` (optionally nullable) → [EnumField], **when the compiler plugin is applied**:
 *   the plugin rewrites `autoField<E>()` to [autoEnumField] / [autoNullableEnumField] because the
 *   enum entries can only be enumerated with an `E : Enum<E>` bound.
 *
 * For any other type, [autoField] throws at runtime with a message pointing at the
 * existing `field { ... }` / `fieldValue { ... }` / `fieldState { ... }` APIs, and the compiler
 * plugin reports a warning at the call site. The type-based dispatch lives entirely in the
 * runtime `when (T::class)` below, so support for a new primitive type can be added by
 * editing this file without re-releasing the compiler plugin.
 *
 * # Compiler plugin label injection
 *
 * When the Compose Preview Lab compiler plugin is applied to the module, a call site like
 * `name = autoField()` is rewritten to `name = autoField(label = "name")` during the IR
 * phase. Callers can override the injected label by passing [label] explicitly; the
 * plugin only fills in [AutoDefaultLabel] (the declared default), never an explicit
 * argument.
 *
 * When the plugin is **not** applied, [label] falls back to [AutoDefaultLabel] for every
 * call site. The fields still render — each `autoField()` call still pushes a separate
 * entry into [PreviewLabState.fields] — but every entry shows the same `"auto"` label in
 * the inspector pane, which makes them visually indistinguishable. Additionally, URL
 * parameter restoration (which keys by label) collapses: only the first `"auto"` field's
 * value survives a reload, the rest fall back to their initial values. The repeated
 * `"auto"` label in the inspector is the intended hint that the plugin is missing.
 *
 * @param label Label shown in the inspector pane. The compiler plugin injects the
 * surrounding parameter name when possible.
 *
 * @see autoEvent
 * @see autoEnumField
 */
@ExperimentalComposePreviewLabApi
@Composable
public inline fun <reified T> PreviewLabScope.autoField(label: String = AutoDefaultLabel): T {
    val nullable = typeOf<T>().isMarkedNullable
    @Suppress("UNCHECKED_CAST")
    return fieldValue<Any?> {
        when (T::class) {
            String::class -> StringField(label, "").orNullable(nullable)
            Int::class -> IntField(label, 0).orNullable(nullable)
            Long::class -> LongField(label, 0L).orNullable(nullable)
            Byte::class -> ByteField(label, 0).orNullable(nullable)
            Float::class -> FloatField(label, 0f).orNullable(nullable)
            Double::class -> DoubleField(label, 0.0).orNullable(nullable)
            Boolean::class -> BooleanField(label, false).orNullable(nullable)
            else -> error(
                "autoField<${T::class.simpleName}>() is not supported. " +
                    "Use field { ... } / fieldValue { ... } / fieldState { ... } " +
                    "with an explicit Field type (autoEnumField() for enum classes without " +
                    "the compiler plugin), or open an issue at " +
                    "https://github.com/TBSten/compose-preview-lab/issues/69 " +
                    "to request a default Field for ${T::class.simpleName}.",
            )
        }
    } as T
}

/**
 * Wraps the receiver with [nullable] (starting at `null`) when [nullable] is `true`.
 *
 * Shared by every [autoField] branch so that `autoField<String?>()` produces a
 * `StringField("…", "").nullable(initialValue = null)` while `autoField<String>()` keeps the
 * plain `StringField`.
 */
@ExperimentalComposePreviewLabApi
@PublishedApi
internal fun <Value : Any> PreviewLabField<Value>.orNullable(nullable: Boolean): PreviewLabField<out Value?> =
    if (nullable) nullable(initialValue = null) else this

/**
 * [autoField] for `enum class` values: generates an [EnumField] whose initial value is the first
 * entry of [E].
 *
 * # Usage
 *
 * ```kt
 * enum class Plan { Free, Pro }
 *
 * @Preview
 * @Composable
 * private fun PlanBadgePreview() = PreviewLab {
 *     PlanBadge(
 *         // Equivalent to `fieldValue { EnumField("plan", initialValue = Plan.Free) }`.
 *         plan = autoEnumField(),
 *     )
 * }
 * ```
 *
 * With the compiler plugin applied, writing `plan = autoField()` is enough: the plugin rewrites
 * `autoField<E>()` to `autoEnumField<E>(label = "plan")` whenever `E` is an enum class (and to
 * [autoNullableEnumField] for `E?`). This function exists as a separate entry point because an
 * unbounded `autoField<T>()` cannot enumerate the entries of `T` at runtime on every platform.
 *
 * @param label Label shown in the inspector pane. The compiler plugin injects the surrounding
 * parameter name when possible.
 *
 * @see autoField
 * @see autoNullableEnumField
 */
@ExperimentalComposePreviewLabApi
@Composable
public inline fun <reified E : Enum<E>> PreviewLabScope.autoEnumField(label: String = AutoDefaultLabel): E =
    fieldValue { EnumField(label, initialValue = enumValues<E>().first()) }

/**
 * Nullable variant of [autoEnumField]: an [EnumField] wrapped with [nullable], starting at `null`.
 *
 * ```kt
 * PlanBadge(
 *     // Equivalent to `fieldValue { EnumField("plan", initialValue = Plan.Free).nullable(initialValue = null) }`.
 *     plan = autoNullableEnumField(),
 * )
 * ```
 *
 * The compiler plugin rewrites `autoField<E?>()` to this function for enum classes.
 *
 * @see autoEnumField
 */
@ExperimentalComposePreviewLabApi
@Composable
public inline fun <reified E : Enum<E>> PreviewLabScope.autoNullableEnumField(label: String = AutoDefaultLabel): E? =
    fieldValue { EnumField(label, initialValue = enumValues<E>().first()).nullable(initialValue = null) }

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
