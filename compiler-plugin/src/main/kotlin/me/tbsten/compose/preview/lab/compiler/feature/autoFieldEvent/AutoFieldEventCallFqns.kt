package me.tbsten.compose.preview.lab.compiler.feature.autoFieldEvent

import me.tbsten.compose.preview.lab.compiler.utils.callableIdOf
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName

// Sentinel FQNs that the `autoFieldEvent/` IR transformer looks for in
// `someParam = autoField()` / `someParam = autoEvent()` call sites.
//
// Only the `InjectAutoLabelIrTransformer` walks these names — it locates
// matching `IrCall` value arguments and rewrites their `label` parameter to
// embed the surrounding parameter's name. Kept inside `autoFieldEvent/`
// (rather than a feature-wide root) because they are private to this logic.

private const val PreviewLabPackage = "me.tbsten.compose.preview.lab.previewlab"

/** `me.tbsten.compose.preview.lab.previewlab.autoField` sentinel call FQN. */
internal val AUTO_FIELD_FQN: FqName = FqName("$PreviewLabPackage.autoField")

/** `me.tbsten.compose.preview.lab.previewlab.autoEvent` sentinel call FQN. */
internal val AUTO_EVENT_FQN: FqName = FqName("$PreviewLabPackage.autoEvent")

/** `autoEnumField<E : Enum<E>>(label)` — the callee `autoField<E>()` is rewritten to for enum classes. */
internal val AUTO_ENUM_FIELD_CALLABLE_ID: CallableId = callableIdOf(PreviewLabPackage, "autoEnumField")

/** `autoNullableEnumField<E : Enum<E>>(label)` — the callee `autoField<E?>()` is rewritten to for enum classes. */
internal val AUTO_NULLABLE_ENUM_FIELD_CALLABLE_ID: CallableId = callableIdOf(PreviewLabPackage, "autoNullableEnumField")

/** The name of the `label` parameter on [AUTO_FIELD_FQN], [AUTO_EVENT_FQN] and the enum variants. */
internal const val AutoLabelParameterName: String = "label"

/**
 * Types the runtime `autoField<T>()` dispatch knows how to build a Field for (nullable variants
 * included). Mirrors the `when (T::class)` in `preview-lab/.../AutoFieldEvent.kt`; enum classes are
 * handled separately by the rewrite to [AUTO_ENUM_FIELD_CALLABLE_ID].
 */
internal val SUPPORTED_AUTO_FIELD_TYPE_FQNS: Set<String> = setOf(
    "kotlin.String",
    "kotlin.Int",
    "kotlin.Long",
    "kotlin.Byte",
    "kotlin.Float",
    "kotlin.Double",
    "kotlin.Boolean",
)
