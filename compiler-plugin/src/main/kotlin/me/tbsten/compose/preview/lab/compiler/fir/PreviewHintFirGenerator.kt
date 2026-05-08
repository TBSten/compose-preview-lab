@file:OptIn(
    org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi::class,
)

package me.tbsten.compose.preview.lab.compiler.fir

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.plugin.createTopLevelClass
import org.jetbrains.kotlin.fir.plugin.createTopLevelFunction
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.constructType
import org.jetbrains.kotlin.fir.types.isMarkedNullable
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Per-declaration hint generator. For each `@Preview` it emits (1) a marker interface and
 * (2) a hint function into the `me.tbsten.compose.preview.lab.hints` package.
 *
 * **Generated Kotlin (semantically equivalent), per `@Preview`**
 * (for `com.example.app.MyButtonPreview`):
 *
 * ```kotlin
 * // file: PreviewHint_<hash>.kt
 * package me.tbsten.compose.preview.lab.hints
 *
 * public interface PreviewHintMarker_com_example_app_MyButtonPreview_<hash>
 *
 * public fun previewHint(value: PreviewHintMarker_com_example_app_MyButtonPreview_<hash>?): CollectedPreview =
 *     error("Stub! Filled by IR.")
 * ```
 *
 * Only the return type and function name are declared at the FIR layer; the
 * `CollectedPreview(...)` constructor call carrying the actual metadata is injected by the
 * IR side ([me.tbsten.compose.preview.lab.compiler.ir.PreviewHintIrBodyFiller]).
 *
 * # Design points
 *
 * ## Fixed hint name (`previewHint`)
 *
 * Cross-module discovery is implemented via
 * `referenceFunctions(CallableId(HINT_PACKAGE, "previewHint"))`. The K2 package-walk APIs
 * do not load external-module declarations on demand, so the combination of a fixed name
 * plus `referenceFunctions` is the de-facto pattern.
 *
 * ```kotlin
 * // consumer side (IR pass)
 * referenceFunctions(CallableId(HINT_PACKAGE, "previewHint"))
 *     .filter { it.owner.isFromExternalModule() }
 *     .forEach { ... } // call each hint
 * ```
 *
 * ## Marker interface for IdSignature disambiguation
 *
 * The KLIB linker derives an IdSignature from `(name, parameterTypes)`, so to disambiguate
 * fixed-name hints the parameter type must be unique per `@Preview`. The marker is an
 * **interface** with `ABSTRACT` modality (Konan-compatible and avoids Compose's
 * `$stableprop` synthesis).
 *
 * ## Marker / hint function name hash
 *
 * `sha256(canonicalKey)` where `canonicalKey = "<sourceFqn>(<paramTypeFqns>)"`.
 * Same-name overloads are disambiguated by the signature portion of the canonical key.
 * The marker name also embeds the sanitized FQN for debuggability:
 * `PreviewHintMarker_com_example_app_MyButtonPreview_<hash>`.
 *
 * ## Predicate-walk timing
 *
 * Walking `predicateBasedProvider` inside the cache loader triggers a Kotlin 2.3.21
 * frontend resolution cycle. Evaluate lazily inside [getTopLevelCallableIds] /
 * [getTopLevelClassIds] instead.
 *
 * ## Visibility
 *
 * `Visibilities.Public` is required because the hint must be discoverable from other
 * modules.
 *
 * # References
 *
 * Pattern adapted from Metro's
 * [ContributionHintFirGenerator](https://github.com/ZacSweers/metro/blob/main/compiler/src/main/kotlin/dev/zacsweers/metro/compiler/fir/generators/ContributionHintFirGenerator.kt),
 * combined with the fixed-name discovery approach of the legacy module-aggregation hint.
 */
internal class PreviewHintFirGenerator(session: FirSession, private val compat: CompatContext,) :
    FirDeclarationGenerationExtension(session) {

    /**
     * Predicate that locates the `@Preview` functions to generate hints for. Targets both
     * the CMP and Android `@Preview` annotations.
     */
    private val previewPredicate: LookupPredicate = LookupPredicate.create {
        annotated(
            PreviewLabFirBuiltIns.CMP_PREVIEW_ANNOTATION_FQN,
            PreviewLabFirBuiltIns.ANDROID_PREVIEW_ANNOTATION_FQN,
        )
    }

    /**
     * One entry per `@Preview` discovered in this session, tying it to its marker / hint
     * pair. Walked the first time [getTopLevelCallableIds] / [getTopLevelClassIds] is
     * touched (never inside a cache loader).
     */
    private val hintEntries: List<HintEntry> by lazy { computeHintEntries() }

    /** Reverse lookup from marker class short name (`PreviewHintMarker_<sanitized_fqn>_<hash>`) to hash. */
    private val hashByMarkerShortName: Map<String, String> by lazy {
        hintEntries.associate { it.markerShortName to it.hash }
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(previewPredicate)
    }

    override fun getTopLevelClassIds(): Set<ClassId> = hintEntries.mapTo(mutableSetOf()) { entry ->
        ClassId(PreviewLabFirBuiltIns.HINT_PACKAGE, Name.identifier(entry.markerShortName))
    }

    override fun getTopLevelCallableIds(): Set<CallableId> = if (hintEntries.isEmpty()) {
        emptySet()
    } else {
        // The hint function name is fixed (`previewHint`); the marker class parameter
        // disambiguates the IdSignature.
        setOf(PreviewLabFirBuiltIns.HINT_FUNCTION_CALLABLE_ID)
    }

    override fun hasPackage(packageFqName: FqName): Boolean = packageFqName == PreviewLabFirBuiltIns.HINT_PACKAGE

    /**
     * Generates the marker interface.
     *
     * **Emitted Kotlin (semantically equivalent), per `@Preview`**
     * (for `fun com.example.app.MyButtonPreview()`):
     * ```kotlin
     * package me.tbsten.compose.preview.lab.hints
     * public interface PreviewHintMarker_com_example_app_MyButtonPreview_<hash>
     * ```
     *
     * The name format is `PreviewHintMarker_<sanitized_fqn>_<hash>`. The sanitized FQN
     * replaces non-identifier characters with `_` so IDE navigation, stack traces, and
     * KLIB IC logs immediately reveal which `@Preview` the marker belongs to. The hash
     * suffix disambiguates same-name overloads (sourceFqn alone collides).
     *
     * `INTERFACE` (not `CLASS` / `OBJECT`) with explicit `Modality.ABSTRACT` avoids
     * Compose Compiler's `$stableprop` synthesis (which causes JS / Wasm IC collisions)
     * and Konan's `Expected a class, found interface` error (which rejects FINAL modality
     * on interfaces).
     *
     * The marker is **not** declared `sealed`. The `@Deprecated(HIDDEN)` annotation
     * applied below already removes the symbol from consumer-side scope resolution, so
     * an external `class MyMarker : PreviewHintMarker_<sanitized_fqn>_<hash>` does not
     * compile — sealed-ization would be redundant. See
     * `PreviewHintMarkerSealOrHiddenTest` for the executable proof.
     */
    override fun generateTopLevelClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>? {
        if (classId.packageFqName != PreviewLabFirBuiltIns.HINT_PACKAGE) return null
        val shortName = classId.shortClassName.asString()
        if (shortName !in hashByMarkerShortName) return null

        val klass = createTopLevelClass(classId, Keys.PreviewLabHintMarkerInterface, ClassKind.INTERFACE) {
            modality = Modality.ABSTRACT
        }
        klass.markAsDeprecatedHidden(session, compat)
        return klass.symbol
    }

    override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> = emptyList()

    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext,): Set<Name> =
        emptySet()

    /**
     * Generates the hint function stub.
     *
     * **Emitted Kotlin (semantically equivalent), per hash**:
     * ```kotlin
     * public fun previewHint(value: PreviewHintMarker_<sanitized_fqn>_<hash>?): CollectedPreview
     * ```
     *
     * The function name is fixed (`previewHint`); the marker class parameter makes the
     * IdSignature unique per `@Preview`. Cross-module consumers can discover every hint
     * with a single `referenceFunctions(fixed-name)` call.
     *
     * No body is emitted here (FIR cannot hold a body). The [Keys.PreviewLabHint] origin
     * is the signal that the IR side
     * ([me.tbsten.compose.preview.lab.compiler.ir.PreviewHintIrBodyFiller]) uses to fill in
     * a body that returns the corresponding `CollectedPreview(...)` constructor call.
     */
    override fun generateFunctions(callableId: CallableId, context: MemberGenerationContext?): List<FirNamedFunctionSymbol> {
        if (callableId != PreviewLabFirBuiltIns.HINT_FUNCTION_CALLABLE_ID) return emptyList()
        if (hintEntries.isEmpty()) return emptyList()

        val collectedPreviewSymbol = session.symbolProvider
            .getClassLikeSymbolByClassId(PreviewLabFirBuiltIns.COLLECTED_PREVIEW_CLASS_ID)
            ?: return emptyList()
        val collectedPreviewType = collectedPreviewSymbol.constructType(emptyArray())

        return hintEntries.map { entry ->
            val markerClassId = ClassId(
                PreviewLabFirBuiltIns.HINT_PACKAGE,
                Name.identifier(entry.markerShortName),
            )
            val markerSymbol = session.symbolProvider
                .getClassLikeSymbolByClassId(markerClassId) as? FirClassSymbol<*>
                ?: error(
                    "Expected marker class ${markerClassId.asString()} to have been generated by " +
                        "generateTopLevelClassLikeDeclaration, but symbol provider returned null",
                )
            val fileName = "PreviewHint_${entry.hash}.kt"
            createTopLevelFunction(
                Keys.PreviewLabHint,
                callableId,
                collectedPreviewType,
                fileName,
            ) {
                visibility = Visibilities.Public
                // The marker parameter exists only to disambiguate the IdSignature; its
                // value is never inspected. Make it **nullable** so consumers can pass
                // `null`, avoiding the need to instantiate the marker class (impossible
                // for an interface) or emit an object singleton.
                valueParameter(
                    name = Name.identifier("value"),
                    type = markerSymbol.constructType(isMarkedNullable = true),
                )
            }.also { it.markAsDeprecatedHidden(session, compat) }.symbol
        }
    }

    /**
     * Walks the predicate and computes the hint hash plus marker class short name for each
     * `@Preview` function.
     *
     * Evaluated lazily the first time [getTopLevelClassIds] / [getTopLevelCallableIds] is
     * touched.
     */
    private fun computeHintEntries(): List<HintEntry> {
        val symbols = session.predicateBasedProvider.getSymbolsByPredicate(previewPredicate)
        return symbols
            .filterIsInstance<FirNamedFunctionSymbol>()
            .filter { it.callableId.classId == null }
            .map { symbol ->
                val callableId = symbol.callableId
                val packageName = callableId.packageName.asString()
                val simpleName = callableId.callableName.asString()
                val sourceFqn = if (packageName.isEmpty()) simpleName else "$packageName.$simpleName"
                val parameterTypeFqns = symbol.parameterTypeFqnsForHash()
                val canonicalKey = buildPreviewHintCanonicalKey(sourceFqn, parameterTypeFqns)
                val hash = computeHintHash(canonicalKey)
                HintEntry(
                    hash = hash,
                    markerShortName = buildMarkerShortName(sourceFqn, hash),
                )
            }
            .distinctBy { it.markerShortName }
    }

    /**
     * Converts a `@Preview` function's value parameter types into the FQN list used in the
     * hint canonical key.
     *
     * Same-name overloads (`fun MyButton()` vs `fun MyButton(text: String)`) are
     * disambiguated by including the per-parameter type FQN in the hash input. Parameter
     * type resolution requires the TYPES phase, so [lazyResolveToPhase] is used to advance
     * to it.
     *
     * **Format** (shared between FIR and IR):
     * - `<classId>` (classId resolved, non-nullable type)
     * - `<classId>?` (classId resolved, nullable type)
     * - `?` (classId unresolved = generic type parameter or resolution failure;
     *   nullability is ignored in this case)
     */
    private fun FirNamedFunctionSymbol.parameterTypeFqnsForHash(): List<String> {
        lazyResolveToPhase(FirResolvePhase.TYPES)
        return valueParameterSymbols.map { paramSymbol ->
            val coneType = paramSymbol.resolvedReturnTypeRef.coneType
            val classFqn = coneType.classId?.asFqNameString() ?: return@map "?"
            if (coneType.isMarkedNullable) "$classFqn?" else classFqn
        }
    }

    /** Metadata for the marker / hint function pair generated for one `@Preview`. */
    private data class HintEntry(
        /** Canonical-key sha256 truncated to 8 base-36 chars (`computeHintHash` output). */
        val hash: String,
        /** Marker interface short name `PreviewHintMarker_<sanitized_fqn>_<hash>`. */
        val markerShortName: String,
    )
}
