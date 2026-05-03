@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.PluginConfig
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
 * **Sample call**: `computeAutoProviderName(moduleFragment, config)` for a module whose Kotlin
 * module name is `:uiLib` and whose `config.projectRootPath` is `/work/my-app`
 * (hashed → `a3k9z2x1`).
 *
 * **Result** (semantically): `Name.identifier("previewLabAutoProvider_a3k9z2x1")`.
 *
 * The hash is computed via the shared [computeModuleHash] (SHA-256 → 8 base-36 chars), which
 * matches the suffix the FIR-side marker class uses (`PreviewLabExportMarker_<hash>`). That
 * shared identity is what lets a downstream consumer derive this provider's FQN from the hint
 * function's parameter type alone — no `@PreviewExportHint` annotation lookup needed. See
 * [PreviewListIrBuilder.collectDependencyGetters] for the consumer-side derivation.
 *
 * `config.projectRootPath` is folded in as the disambiguator so two unrelated published
 * artifacts that happen to share a Kotlin module name still hash to different suffixes
 * (each artifact was built by its own Gradle invocation with its own absolute project root).
 * Within a single project, sibling modules share the project root but already have unique
 * Kotlin module names by Gradle convention, so the disambiguator is redundant there.
 */
internal fun computeAutoProviderName(moduleFragment: IrModuleFragment, config: PluginConfig): Name {
    val moduleNameHash = computeModuleHash(
        moduleName = moduleFragment.name.asString(),
        disambiguator = config.projectRootPath,
    )
    return Name.identifier("$AutoProviderPrefix$moduleNameHash")
}

/**
 * Computes the fully-qualified name of the auto-provider function for [moduleFragment].
 *
 * **Sample call**: same module as [computeAutoProviderName].
 *
 * **Result**: `"me.tbsten.compose.preview.lab.exports.previewLabAutoProvider_a3k9z2x1"`.
 */
internal fun computeAutoProviderFqn(moduleFragment: IrModuleFragment, config: PluginConfig): String =
    "${HINT_PACKAGE.asString()}.${computeAutoProviderName(moduleFragment, config).asString()}"

internal val HINT_PACKAGE: FqName = PreviewLabFirBuiltIns.HINT_PACKAGE

internal const val AutoProviderPrefix = "previewLabAutoProvider_"
