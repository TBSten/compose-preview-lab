@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.fir.PreviewLabFirBuiltIns
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Computes the auto-provider function name for [moduleFragment] (the function emitted by
 * [GenerateAutoPreviewExport] that returns this module's `List<CollectedPreview>`).
 *
 * **Sample call**: `computeAutoProviderName(moduleFragment)` for a module whose Kotlin module
 * name is `:uiLib` (hashed → `a3k9z2x1`).
 *
 * **Result** (semantically): `Name.identifier("previewLabAutoProvider_a3k9z2x1")`.
 *
 * The base-36 hash of `moduleFragment.name.asString()` (up to 8 characters; `Int.hashCode`
 * stringified in base-36 is at most 7 characters before sign stripping) matches the suffix
 * the FIR-side marker class uses (`PreviewLabExportMarker_<hash>`), so a downstream consumer
 * can derive this provider's FQN from the hint function's parameter type alone — no
 * `@PreviewExportHint` annotation lookup needed. See
 * [PreviewListIrBuilder.collectDependencyGetters] for the consumer-side derivation.
 *
 * The hash uses the Kotlin module name (unique per dependency JAR) so two sibling modules
 * sharing a base package never collide on the same FQN.
 */
internal fun computeAutoProviderName(moduleFragment: IrModuleFragment): Name {
    // `hashCode().toString(36)` can produce a leading `-` for negative hashes; strip the
    // sign with `and Int.MAX_VALUE` so the suffix is always a valid Kotlin identifier
    // tail. Without this, `Name.identifier(...)` throws on the consumer side when
    // re-parsing the FQN out of `@PreviewExportHint(fqn = ...)`.
    val moduleNameHash = (moduleFragment.name.asString().hashCode() and Int.MAX_VALUE).toString(36).takeLast(8)
    return Name.identifier("$AutoProviderPrefix$moduleNameHash")
}

/**
 * Computes the fully-qualified name of the auto-provider function for [moduleFragment].
 *
 * **Sample call**: same module as [computeAutoProviderName].
 *
 * **Result**: `"me.tbsten.compose.preview.lab.exports.previewLabAutoProvider_a3k9z2x1"`.
 */
internal fun computeAutoProviderFqn(moduleFragment: IrModuleFragment): String =
    "${HINT_PACKAGE.asString()}.${computeAutoProviderName(moduleFragment).asString()}"

internal val HINT_PACKAGE: FqName = PreviewLabFirBuiltIns.HINT_PACKAGE

internal const val AutoProviderPrefix = "previewLabAutoProvider_"
