@file:OptIn(ExperimentalTopLevelDeclarationsGenerationApi::class)

package me.tbsten.compose.preview.lab.compiler.fir

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.caches.FirCache
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.plugin.createConstructor
import org.jetbrains.kotlin.fir.plugin.createTopLevelClass
import org.jetbrains.kotlin.fir.plugin.createTopLevelFunction
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.constructType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

/**
 * FIR-side hint generator for the bug-017 KLIB-safe cross-module aggregation pipeline.
 *
 * Per compilation unit it emits one synthetic marker class plus one
 * `previewLabExport(value: <Marker>): Unit` hint function. KLIB-based platforms (JS / Wasm /
 * iOS) derive [org.jetbrains.kotlin.ir.util.IdSignature] from `(name, parameterTypes)`, so
 * having the parameter type be a per-module-unique marker class makes every hint's
 * IdSignature naturally unique and side-steps the link-time clash that the legacy
 * `previewLabExport(PreviewExport)` hints would cause.
 *
 * **Generated Kotlin (semantically equivalent), per export target**:
 *
 * ```kotlin
 * // file: PreviewLabExport_<sanitized>.kt
 * package me.tbsten.compose.preview.lab.exports
 *
 * public class PreviewLabExportMarker_<sanitized>_<hash8> public constructor()
 *
 * public fun previewLabExport(value: PreviewLabExportMarker_<sanitized>_<hash8>): Unit {}
 * ```
 *
 * **Current scope (bug-017 step 2)**:
 *
 *  - One hint emitted per compilation unit, targeting the deterministic auto-provider FQN
 *    that [me.tbsten.compose.preview.lab.compiler.ir.GenerateAutoPreviewExport] computes from
 *    `(session.moduleData.name)` during the IR pass. Both phases compute the FQN identically
 *    so they always agree.
 *  - The hint is emitted unconditionally (regardless of whether the module actually contains
 *    `@Preview` functions). Modules without previews end up with a dangling marker class
 *    whose `targetFqn` does not resolve to anything during the downstream
 *    `referenceFunctions(...)` lookup; the lookup quietly skips it. This avoids walking the
 *    `predicateBasedProvider` for `@Preview` annotations from inside the cache loader, which
 *    triggers a Kotlin 2.3.21 frontend resolution cycle.
 *  - Manual `val x by collectModulePreviews()` properties continue to be exported via the
 *    legacy IR-based [me.tbsten.compose.preview.lab.compiler.ir.GeneratePreviewExportHint] on
 *    JVM. The two hint families coexist because their value-parameter types differ
 *    (FIR-generated hints take a marker class; legacy IR-generated hints take `PreviewExport`).
 *  - The `@PreviewExportHint(fqn = ...)` annotation that downstream
 *    `PreviewListIrBuilder.collectDependencyGetters()` reads is **not** attached yet; the
 *    follow-up step wires that up alongside the migration of the manual-property path to
 *    FIR.
 *
 * **Body filling**: `createTopLevelClass` / `createConstructor` / `createTopLevelFunction`
 * produce declarations with `body == null`. The JVM backend asserts on that, so
 * [me.tbsten.compose.preview.lab.compiler.ir.PreviewLabHintIrBodyFiller] runs during the IR
 * pass and injects the canonical `super<Any>(); <init>` body for the constructor and an
 * empty block body for the hint function.
 *
 * Pattern adapted from Metro's
 * [ContributionHintFirGenerator](https://github.com/ZacSweers/metro/blob/main/compiler/src/main/kotlin/dev/zacsweers/metro/compiler/fir/generators/ContributionHintFirGenerator.kt).
 */
internal class PreviewLabHintFirGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {

    /**
     * One hint to emit, paired with the marker class id that disambiguates it from sibling hints.
     *
     * `targetFqn` is the FQN that downstream `collectAllModulePreviews()` will resolve via
     * `referenceFunctions(callableId)` after reading the hint's `@PreviewExportHint(fqn = ...)`.
     */
    private data class HintEntry(val targetFqn: String, val markerClassId: ClassId)

    private val hintEntriesCache: FirCache<Unit, List<HintEntry>, Nothing?> =
        session.firCachesFactory.createCache { _, _ -> computeHintEntries() }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        // No predicates needed yet; auto-export hint emission is unconditional. Manual
        // `collectModulePreviews()` property discovery (follow-up step) will register a
        // `LookupPredicate.annotated(...)` for the sentinel calls and walk
        // `predicateBasedProvider` here.
    }

    /**
     * Synthesizes one [HintEntry] per compilation unit pointing at the deterministic
     * auto-provider FQN. The marker class id is derived from the FQN so it stays unique
     * across modules.
     */
    private fun computeHintEntries(): List<HintEntry> {
        val moduleNameHash = session.moduleData.name.asString().hashCode().toUInt()
            .toString(36).padStart(8, '0').takeLast(8)
        val providerFnName = "previewLabAutoProvider_${moduleNameHash}_$AutoProviderDefaultPackageToken"
        val providerFqn = "${PreviewLabFirBuiltIns.HINT_PACKAGE.asString()}.$providerFnName"
        val markerClassId = computeMarkerClassId(providerFqn)
        return listOf(HintEntry(providerFqn, markerClassId))
    }

    override fun getTopLevelClassIds(): Set<ClassId> = hintEntriesCache.getValue(Unit, null).map { it.markerClassId }.toSet()

    override fun getTopLevelCallableIds(): Set<CallableId> = if (hintEntriesCache.getValue(Unit, null).isEmpty()) {
        emptySet()
    } else {
        setOf(PreviewLabFirBuiltIns.HINT_FUNCTION_CALLABLE_ID)
    }

    /**
     * Generates the synthetic marker class for [classId] with a private no-arg constructor.
     *
     * **Generated Kotlin (semantically equivalent)**:
     * ```kotlin
     * package me.tbsten.compose.preview.lab.exports
     * public class PreviewLabExportMarker_<hash> private constructor()
     * ```
     *
     * Returns null if [classId] is not one of the marker classes computed for this module.
     */
    override fun generateTopLevelClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>? {
        if (hintEntriesCache.getValue(Unit, null).none { it.markerClassId == classId }) return null
        val klass = createTopLevelClass(classId, Keys.PreviewLabHintMarker, ClassKind.CLASS) {}
        return klass.symbol
    }

    override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> {
        val owner = context.owner
        if (owner.classId.packageFqName != PreviewLabFirBuiltIns.HINT_PACKAGE) return emptyList()
        val ctor = createConstructor(owner, Keys.PreviewLabHintMarker, isPrimary = true) {
            visibility = Visibilities.Public
        }
        return listOf(ctor.symbol)
    }

    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext,): Set<Name> {
        if (classSymbol.classId.packageFqName != PreviewLabFirBuiltIns.HINT_PACKAGE) return emptySet()
        return setOf(SpecialNames.INIT)
    }

    /**
     * Generates one `previewLabExport(value: <Marker>): Unit` hint per [HintEntry].
     *
     * Each hint lives in its own synthetic file (`containingFileName`) so the file-facade
     * splitting works the same way it did in the legacy IR-based generator. The hint's
     * value-parameter type points at the marker class, which gives every hint a distinct
     * KLIB IdSignature.
     *
     * **Generated Kotlin (semantically equivalent), per entry**:
     * ```kotlin
     * package me.tbsten.compose.preview.lab.exports
     * public fun previewLabExport(value: PreviewLabExportMarker_<hash>): Unit {}
     * ```
     *
     * The body is left empty here; [me.tbsten.compose.preview.lab.compiler.ir.PreviewLabHintIrBodyFiller]
     * (added in step 3) injects a stub `Unit` expression body during the IR pass so the JVM
     * backend does not assert on empty bodies.
     *
     * The `@PreviewExportHint(fqn = ...)` annotation that downstream
     * `PreviewListIrBuilder.collectDependencyGetters()` reads is **not** attached here; the
     * IR-side body filler attaches it when it has access to the IR `IrConstructorCallImpl`
     * APIs (also added in step 3).
     */
    override fun generateFunctions(callableId: CallableId, context: MemberGenerationContext?,): List<FirNamedFunctionSymbol> {
        if (callableId != PreviewLabFirBuiltIns.HINT_FUNCTION_CALLABLE_ID) return emptyList()
        val entries = hintEntriesCache.getValue(Unit, null)
        if (entries.isEmpty()) return emptyList()
        return entries.sortedBy { it.targetFqn }.map { entry ->
            val markerSymbol = session.symbolProvider
                .getClassLikeSymbolByClassId(entry.markerClassId) as FirClassSymbol<*>
            val fileName = hintFileName(entry.markerClassId)
            val fn = createTopLevelFunction(
                Keys.PreviewLabHint,
                callableId,
                session.builtinTypes.unitType.coneType,
                fileName,
            ) {
                visibility = Visibilities.Public
                valueParameter(
                    name = PreviewLabFirBuiltIns.HINT_VALUE_PARAM_NAME,
                    type = markerSymbol.constructType(emptyArray()),
                )
            }
            fn.symbol
        }
    }

    override fun hasPackage(packageFqName: FqName): Boolean = packageFqName == PreviewLabFirBuiltIns.HINT_PACKAGE

    private companion object Companion {
        const val AutoProviderDefaultPackageToken = "default"
        const val MarkerClassPrefix = "PreviewLabExportMarker_"

        /**
         * Computes the marker class id for a hint targeting [targetFqn].
         *
         * Naming: `me.tbsten.compose.preview.lab.exports.PreviewLabExportMarker_<sanitized>_<hash8>`
         * - `<sanitized>`: [targetFqn] with non-alphanumeric characters replaced by `_`,
         *   truncated to keep file names manageable.
         * - `<hash8>`: 8-char base-36 hash of [targetFqn] for cross-module collision avoidance.
         */
        fun computeMarkerClassId(targetFqn: String): ClassId {
            val sanitized = targetFqn
                .replace(Regex("[^A-Za-z0-9]+"), "_")
                .trim('_')
                .take(40)
                .ifEmpty { "Root" }
            val hash = targetFqn.hashCode().toUInt().toString(36).padStart(8, '0').takeLast(8)
            val simpleName = "$MarkerClassPrefix${sanitized}_$hash"
            return ClassId(PreviewLabFirBuiltIns.HINT_PACKAGE, Name.identifier(simpleName))
        }

        fun hintFileName(markerClassId: ClassId): String = "PreviewLabExport_${markerClassId.shortClassName.asString()}.kt"
    }
}
