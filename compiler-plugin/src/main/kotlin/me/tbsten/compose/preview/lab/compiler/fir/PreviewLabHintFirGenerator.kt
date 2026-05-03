@file:OptIn(ExperimentalTopLevelDeclarationsGenerationApi::class)

package me.tbsten.compose.preview.lab.compiler.fir

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.caches.FirCache
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.moduleData
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
 * public interface PreviewLabExportMarker_<sanitized>_<hash8>
 *
 * public fun previewLabExport(value: PreviewLabExportMarker_<sanitized>_<hash8>): Unit {}
 * ```
 *
 * The marker is an empty `interface` (not a `class` / `object`) so that the Compose Compiler's
 * stability inference does not synthesize a `$stableprop` accessor for it. On JS / Wasm
 * incremental compilation, Compose-injected stability accessors over plugin-generated classes
 * end up re-binding the same `IrPropertySymbolImpl` across cache reloads and crash the IR
 * linker with `IrPropertySymbolImpl is already bound`. Interface stability is decided per
 * implementation at use sites, so Compose emits no `$stableprop` on the interface itself,
 * sidestepping the IC clash.
 *
 * **Current scope (bug-017 step 2)**:
 *
 *  - One hint emitted per compilation unit. The marker class id is derived from a hash of
 *    `session.moduleData.name` for cross-module uniqueness; no `targetFqn` is computed yet.
 *    Step 3 will add per-property hints (one per `collectModulePreviews()` /
 *    `collectAllModulePreviews()` delegate found via `predicateBasedProvider`) and an entry
 *    for the auto-export path that mirrors the FQN
 *    [me.tbsten.compose.preview.lab.compiler.ir.GenerateAutoPreviewExport] computes during
 *    the IR pass.
 *  - Hints are emitted unconditionally (regardless of whether the module actually contains
 *    `@Preview` functions). This is intentional: walking `predicateBasedProvider` for
 *    `@Preview` annotations from inside the cache loader triggers a Kotlin 2.3.21 frontend
 *    resolution cycle, and the empty-module marker is harmless because step 2's hints are
 *    never read by anything yet.
 *  - Manual `val x by collectModulePreviews()` properties continue to be exported via the
 *    legacy IR-based [me.tbsten.compose.preview.lab.compiler.ir.GeneratePreviewExportHint] on
 *    JVM. The two hint families coexist because their value-parameter types differ
 *    (FIR-generated hints take a marker class; legacy IR-generated hints take `PreviewExport`).
 *  - The `@PreviewExportHint(fqn = ...)` annotation that downstream
 *    `PreviewListIrBuilder.collectDependencyGetters()` reads is **not** attached yet; the
 *    follow-up step wires that up alongside the migration of the manual-property path to
 *    FIR.
 *
 * **Body filling**: `createTopLevelFunction` produces a declaration with `body == null`.
 * The JVM backend asserts on that, so
 * [me.tbsten.compose.preview.lab.compiler.ir.PreviewLabHintIrBodyFiller] runs during the IR
 * pass and injects an empty block body for the hint function. The marker interface has no
 * constructor, so no body filling is needed for the marker itself.
 *
 * Pattern adapted from Metro's
 * [ContributionHintFirGenerator](https://github.com/ZacSweers/metro/blob/main/compiler/src/main/kotlin/dev/zacsweers/metro/compiler/fir/generators/ContributionHintFirGenerator.kt).
 */
internal class PreviewLabHintFirGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {

    /**
     * A hint to emit, identified by the marker class id that disambiguates it from sibling
     * hints from other modules.
     *
     * Step 2 produces exactly one entry per compilation unit. Step 3+ will extend this to
     * one entry per manual `collectModulePreviews()` / `collectAllModulePreviews()` property
     * found via `predicateBasedProvider`, plus one for the auto-export path. At that point
     * each entry will also carry the `targetFqn` that gets attached as
     * `@PreviewExportHint(fqn = ...)` on the hint function — but that wiring is intentionally
     * absent in Step 2: the FIR-emitted hints exist only to validate the KLIB pipeline, not
     * to be discovered downstream yet.
     */
    private data class HintEntry(val markerClassId: ClassId)

    private val hintEntriesCache: FirCache<Unit, List<HintEntry>, Nothing?> =
        session.firCachesFactory.createCache { _, _ -> computeHintEntries() }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        // No predicates needed yet; auto-export hint emission is unconditional. Manual
        // `collectModulePreviews()` property discovery (follow-up step) will register a
        // `LookupPredicate.annotated(...)` for the sentinel calls and walk
        // `predicateBasedProvider` here.
    }

    /**
     * Synthesizes one [HintEntry] per compilation unit. The marker class id is derived from
     * a hash of `session.moduleData.name` so it is unique across modules on the same
     * classpath, which is the only invariant the Step 2 KLIB pipeline cares about.
     *
     * The Step 3 follow-up will add the matching `targetFqn` (the auto-provider FQN that
     * `GenerateAutoPreviewExport` computes during the IR pass) and attach it as
     * `@PreviewExportHint(fqn = ...)` so downstream `collectAllModulePreviews()` can resolve
     * the corresponding function. The FQN computation lives entirely in IR today; pre-computing
     * it here would just duplicate that logic with no consumer to read it.
     */
    private fun computeHintEntries(): List<HintEntry> {
        val moduleNameHash = session.moduleData.name.asString().hashCode().toString(36).takeLast(8)
        val markerClassId = ClassId(
            PreviewLabFirBuiltIns.HINT_PACKAGE,
            Name.identifier("$MarkerClassPrefix$moduleNameHash"),
        )
        return listOf(HintEntry(markerClassId))
    }

    override fun getTopLevelClassIds(): Set<ClassId> = hintEntriesCache.getValue(Unit, null).map { it.markerClassId }.toSet()

    override fun getTopLevelCallableIds(): Set<CallableId> = if (hintEntriesCache.getValue(Unit, null).isEmpty()) {
        emptySet()
    } else {
        setOf(PreviewLabFirBuiltIns.HINT_FUNCTION_CALLABLE_ID)
    }

    /**
     * Generates the synthetic marker **interface** for [classId].
     *
     * **Generated Kotlin (semantically equivalent)**:
     * ```kotlin
     * package me.tbsten.compose.preview.lab.exports
     * public interface PreviewLabExportMarker_<hash>
     * ```
     *
     * `ClassKind.INTERFACE` (not `CLASS` or `OBJECT`) because the Compose Compiler's class
     * stability inference adds `$stableprop` synthetic accessors to every regular class /
     * object it sees, including plugin-synthesized ones. On JS / Wasm incremental compilation
     * the deserializer then re-creates the synthetic property's symbol on the next compile
     * round and crashes with `IrPropertySymbolImpl is already bound`. Interfaces have no
     * intrinsic stability mask (their stability is decided per implementation at use sites)
     * so Compose does not synthesize `$stableprop` on them, sidestepping the IC clash. For
     * KLIB IdSignature uniqueness only the class id matters, and an interface's class id is
     * just as unique as a class id.
     *
     * Interfaces have no constructors, so [generateConstructors] / [getCallableNamesForClass]
     * also return empty for our hint package.
     *
     * The interface's modality must be `ABSTRACT` (the builder default is `FINAL`) — a
     * `FINAL` interface trips Konan's `ClassLayoutBuilder.vtableEntries` and aborts iOS
     * linking with `IllegalArgumentException: Expected a class, found interface`.
     *
     * Returns null if [classId] is not one of the marker classes computed for this module.
     */
    override fun generateTopLevelClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>? {
        if (hintEntriesCache.getValue(Unit, null).none { it.markerClassId == classId }) return null
        val klass = createTopLevelClass(classId, Keys.PreviewLabHintMarker, ClassKind.INTERFACE) {
            // Konan's `ClassLayoutBuilder.vtableEntries` rejects `INTERFACE` declarations
            // whose modality is the builder default of `FINAL` ("Expected a class, found
            // interface"). Setting `ABSTRACT` matches how user-source interfaces are emitted
            // and keeps the iOS linker happy.
            modality = Modality.ABSTRACT
        }
        return klass.symbol
    }

    override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> = emptyList()

    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext,): Set<Name> =
        emptySet()

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
        return entries.sortedBy { it.markerClassId.asString() }.map { entry ->
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
        const val MarkerClassPrefix = "PreviewLabExportMarker_"

        fun hintFileName(markerClassId: ClassId): String = "PreviewLabExport_${markerClassId.shortClassName.asString()}.kt"
    }
}
