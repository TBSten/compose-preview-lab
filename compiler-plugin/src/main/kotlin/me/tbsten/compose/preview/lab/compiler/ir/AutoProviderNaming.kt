@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.fir.PreviewLabFirBuiltIns
import me.tbsten.compose.preview.lab.compiler.fir.computeModuleHash
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
 * The hash is computed via the shared [computeModuleHash] (SHA-256 → 8 base-36 chars), which
 * matches the suffix the FIR-side marker class uses (`PreviewLabExportMarker_<hash>`). That
 * shared identity is what lets a downstream consumer derive this provider's FQN from the hint
 * function's parameter type alone — no `@PreviewExportHint` annotation lookup needed. See
 * [PreviewListIrBuilder.collectDependencyGetters] for the consumer-side derivation.
 *
 * Using SHA-256 (instead of `String.hashCode()`) is what makes the suffix collision-resistant
 * on user-controlled module names; see [computeModuleHash] for the reasoning.
 */
internal fun computeAutoProviderName(moduleFragment: IrModuleFragment): Name {
    val moduleNameHash = computeModuleHash(moduleFragment.name.asString())
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
