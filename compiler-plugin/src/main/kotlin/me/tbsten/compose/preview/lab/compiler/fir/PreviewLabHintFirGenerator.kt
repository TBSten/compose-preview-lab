@file:OptIn(ExperimentalTopLevelDeclarationsGenerationApi::class)

package me.tbsten.compose.preview.lab.compiler.fir

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.plugin.createTopLevelClass
import org.jetbrains.kotlin.fir.plugin.createTopLevelFunction
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
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

    private val hintEntries = PreviewLabHintEntries(session)

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        // No predicates needed yet; auto-export hint emission is unconditional. Manual
        // `collectModulePreviews()` property discovery (follow-up step) will register a
        // `LookupPredicate.annotated(...)` for the sentinel calls and walk
        // `predicateBasedProvider` here.
    }

    override fun getTopLevelClassIds(): Set<ClassId> = hintEntries.get().map { it.markerClassId }.toSet()

    override fun getTopLevelCallableIds(): Set<CallableId> {
        val entries = hintEntries.get()
        if (entries.isEmpty()) return emptySet()
        return buildSet {
            add(PreviewLabFirBuiltIns.HINT_FUNCTION_CALLABLE_ID)
            for (entry in entries) {
                val hash = entry.markerClassId.shortClassName.asString()
                    .removePrefix(PreviewLabHintEntries.MarkerClassPrefix)
                add(
                    CallableId(
                        PreviewLabFirBuiltIns.HINT_PACKAGE,
                        Name.identifier("${PreviewLabFirBuiltIns.AutoProviderPrefix}$hash"),
                    ),
                )
            }
        }
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
        if (hintEntries.get().none { it.markerClassId == classId }) return null
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
        val entries = hintEntries.get()
        if (entries.isEmpty()) return emptyList()
        if (callableId.packageName != PreviewLabFirBuiltIns.HINT_PACKAGE) return emptyList()

        return when (callableId) {
            PreviewLabFirBuiltIns.HINT_FUNCTION_CALLABLE_ID -> generateHintFunctions(callableId, entries)
            else -> generateAutoProviderStub(callableId, entries)
        }
    }

    private fun generateHintFunctions(callableId: CallableId, entries: List<HintEntry>): List<FirNamedFunctionSymbol> =
        entries.sortedBy { it.markerClassId.asString() }.map { entry ->
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

    /**
     * Emits the `previewLabAutoProvider_<hash>(): List<CollectedPreview>` stub matching the
     * marker class with the same hash. Body filled by
     * [me.tbsten.compose.preview.lab.compiler.ir.PreviewLabHintIrBodyFiller] in the IR pass.
     *
     * **Generated Kotlin (semantically equivalent)**:
     * ```kotlin
     * package me.tbsten.compose.preview.lab.exports
     * public fun previewLabAutoProvider_<hash>(): List<CollectedPreview>
     * ```
     *
     * Emitting through FIR (instead of building a fresh `IrSimpleFunction` post-hoc in IR) is
     * what gives the function a proper KLIB IdSignature: FIR-declared top-level callables go
     * through the compiler's standard declaration pipeline, so consumer-side
     * `referenceFunctions(callableId)` lookups land on a symbol whose IdSignature matches the
     * call IR. With the leaf-only emission gate in [PreviewLabHintEntries.compute], only one
     * FIR session per Kotlin module compile reaches this code path, so there is exactly one
     * provider stub per marker — no `IrSimpleFunctionSymbolImpl is already bound` from
     * sibling sessions duplicating the same signature.
     */
    private fun generateAutoProviderStub(callableId: CallableId, entries: List<HintEntry>,): List<FirNamedFunctionSymbol> {
        val expectedHash = callableId.callableName.asString()
            .removePrefix(PreviewLabFirBuiltIns.AutoProviderPrefix)
        val matching = entries.firstOrNull { entry ->
            entry.markerClassId.shortClassName.asString()
                .removePrefix(PreviewLabHintEntries.MarkerClassPrefix) == expectedHash
        } ?: return emptyList()

        val collectedPreviewSymbol = session.symbolProvider
            .getClassLikeSymbolByClassId(PreviewLabFirBuiltIns.COLLECTED_PREVIEW_CLASS_ID)
            as? FirRegularClassSymbol ?: return emptyList()
        val listSymbol = session.symbolProvider
            .getClassLikeSymbolByClassId(ClassId(FqName("kotlin.collections"), Name.identifier("List")))
            as? FirRegularClassSymbol ?: return emptyList()

        val listOfCollectedPreview = listSymbol.constructType(
            arrayOf(collectedPreviewSymbol.constructType(emptyArray())),
        )

        val fileName = "PreviewLabAutoProvider_$expectedHash.kt"
        val fn = createTopLevelFunction(
            Keys.PreviewLabAutoProvider,
            callableId,
            listOfCollectedPreview,
            fileName,
        ) {
            visibility = Visibilities.Public
        }

        // `matching` was already used for the early-return guard above; reading it once more
        // here keeps the variable load alive against a future ktlint/IDE pruning pass that
        // might mistake the lookup for dead code (the lookup defends against `entries`
        // skipping ahead of `getTopLevelCallableIds`).
        require(matching.markerClassId.shortClassName.asString().startsWith(PreviewLabHintEntries.MarkerClassPrefix))
        return listOf(fn.symbol)
    }

    override fun hasPackage(packageFqName: FqName): Boolean = packageFqName == PreviewLabFirBuiltIns.HINT_PACKAGE

    private companion object {
        fun hintFileName(markerClassId: ClassId): String = "PreviewLabExport_${markerClassId.shortClassName.asString()}.kt"
    }
}
