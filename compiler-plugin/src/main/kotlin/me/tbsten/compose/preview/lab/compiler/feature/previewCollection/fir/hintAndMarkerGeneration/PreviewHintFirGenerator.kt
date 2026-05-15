@file:OptIn(
    org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi::class,
)

package me.tbsten.compose.preview.lab.compiler.feature.previewCollection.fir.hintAndMarkerGeneration

import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.HintEntry
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.PreviewAnnotationPredicates
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.PreviewKeys
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.COLLECTED_PREVIEW_CLASS_ID
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.HINT_PACKAGE
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.hintEntriesProvider
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.hintFunctionCallableId
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.fir.hintGeneration.markAsDeprecatedHidden
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.fir.markAsInternalSyntheticHint
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
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
import org.jetbrains.kotlin.fir.types.constructType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Per-declaration hint generator. For each `@Preview` it emits (1) a marker interface and
 * (2) a scope-suffixed hint function into the `me.tbsten.compose.preview.lab.hints` package.
 *
 * **Generated Kotlin (semantically equivalent), per `@Preview`**
 * (for `com.example.app.MyButtonPreview` with the default scope):
 *
 * ```kotlin
 * // file: PreviewHint_<hash>.kt
 * package me.tbsten.compose.preview.lab.hints
 *
 * @kotlin.Deprecated("...", level = HIDDEN)
 * @me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
 * @me.tbsten.compose.preview.lab.SyntheticPreviewHint
 * public interface PreviewHintMarker_com_example_app_MyButtonPreview_<hash>
 *
 * @kotlin.Deprecated("...", level = HIDDEN)
 * @me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
 * @me.tbsten.compose.preview.lab.SyntheticPreviewHint
 * public fun previewHint_default(value: PreviewHintMarker_com_example_app_MyButtonPreview_<hash>?): CollectedPreview =
 *     error("Stub! Filled by IR.")
 * ```
 *
 * Only the return type and function name are declared at the FIR layer; the
 * `CollectedPreview(...)` constructor call carrying the actual metadata is injected by the
 * IR side
 * ([me.tbsten.compose.preview.lab.compiler.feature.previewCollection.ir.collectPreviewsReplacement.FillPreviewHintIrBody]).
 *
 * The two non-`@Deprecated` markers serve complementary roles:
 * * `@InternalComposePreviewLabApi` keeps the marker class and the hint function out of the
 *   BCV baseline on every CMP target (KLIB / JVM / Android), via the existing
 *   `nonPublicMarkers` filter in `apiValidation`.
 * * `@SyntheticPreviewHint` is the IR-side `DiscoverHints`'s positive proof that this
 *   declaration was emitted by *us*. Third-party code that happens to live in the
 *   `me.tbsten.compose.preview.lab.hints` package — whether by accident or by namespace
 *   squatting — cannot be picked up by `collectAllModulePreviews()` because it lacks the
 *   marker.
 *
 * # Design points
 *
 * ## Hint function name encodes the scope
 *
 * Cross-module discovery is implemented via
 * `referenceFunctions(CallableId(HINT_PACKAGE, "previewHint_<scope>"))`, so the IR side can
 * filter by scope without inspecting each hint individually. The K2 package-walk APIs do
 * not load external-module declarations on demand, so the combination of a known per-scope
 * callable name plus `referenceFunctions` is the de-facto pattern.
 *
 * ```kotlin
 * // consumer side (IR pass), scope = "design"
 * referenceFunctions(CallableId(HINT_PACKAGE, "previewHint_design"))
 *     .filter { it.owner.isFromExternalModule() }
 *     .forEach { ... } // call each hint
 * ```
 *
 * ## Marker interface for IdSignature disambiguation
 *
 * The KLIB linker derives an IdSignature from `(name, parameterTypes)`, so to disambiguate
 * scope-suffixed hints the parameter type must still be unique per `@Preview`. The marker
 * is an **interface** with `ABSTRACT` modality (Konan-compatible and avoids Compose's
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
internal class PreviewHintFirGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {

    /**
     * Session-shared per-`@Preview` metadata, populated lazily by
     * [HintEntriesProvider]. This generator consumes the same list as every other
     * `previewCollection` FIR consumer (= SSoT).
     *
     * Predicate registration ([registerPredicates] below) is still required here even
     * though the lookup itself lives in [HintEntriesProvider]: `getSymbolsByPredicate`
     * only returns symbols whose annotation FQNs are registered by *some*
     * `FirDeclarationGenerationExtension` in the same session. The provider, being a
     * `FirExtensionSessionComponent`, cannot register predicates on its own.
     */
    private val hintEntries: List<HintEntry>
        get() = session.hintEntriesProvider.hintEntries

    /** Reverse lookup from marker class short name (`PreviewHintMarker_<sanitized_fqn>_<hash>`) to hash. */
    private val hashByMarkerShortName: Map<String, String> by lazy {
        hintEntries.associate { it.markerShortName to it.hash }
    }

    /**
     * Pre-computed `previewHint_<scope>` callable id → matching entries lookup, built from
     * [hintEntries]. Replaces the per-call early-return chain in [generateFunctions]
     * (`packageName == HINT_PACKAGE` + `callableName.startsWith(PreviewHintFunctionPrefix)`
     * + `entry.scopes` walk) with a single `Map.get` — the framework only calls us back
     * for the callable ids we returned from [getTopLevelCallableIds], and any unexpected
     * id surfaces as `null` here, so the lookup itself is the predicate.
     */
    private val entriesByHintCallableId: Map<CallableId, List<HintEntry>> by lazy {
        buildMap<CallableId, MutableList<HintEntry>> {
            hintEntries.forEach { entry ->
                entry.scopes.forEach { scope ->
                    val callableId = hintFunctionCallableId(scope)
                    getOrPut(callableId) { mutableListOf() }.add(entry)
                }
            }
        }
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(PreviewAnnotationPredicates.previewPredicate)
        register(PreviewAnnotationPredicates.optionPredicate)
    }

    override fun getTopLevelClassIds(): Set<ClassId> = hintEntries.mapTo(mutableSetOf()) { entry ->
        ClassId(HINT_PACKAGE, Name.identifier(entry.markerShortName))
    }

    override fun getTopLevelCallableIds(): Set<CallableId> {
        if (hintEntries.isEmpty()) return emptySet()
        // One callable id per distinct scope across every entry. The marker class parameter
        // still makes the IdSignature unique per `@Preview`, so multiple hints (whether from
        // different previews or from the same preview registered into multiple scopes) can
        // share the same scope-suffixed callable name without colliding.
        return hintEntries.flatMapTo(mutableSetOf()) { entry ->
            entry.scopes.map { hintFunctionCallableId(it) }
        }
    }

    override fun hasPackage(packageFqName: FqName): Boolean = packageFqName == HINT_PACKAGE

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
     *
     * `markAsInternalSyntheticHint` then layers `@InternalComposePreviewLabApi` (so BCV
     * filters this class out of every target's baseline) and `@SyntheticPreviewHint` (so
     * the IR-side hint discovery can distinguish plugin-emitted markers from any other
     * class that ends up in the same package).
     */
    override fun generateTopLevelClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>? {
        if (classId.packageFqName != HINT_PACKAGE) return null
        val shortName = classId.shortClassName.asString()
        if (shortName !in hashByMarkerShortName) return null

        val klass = createTopLevelClass(classId, PreviewKeys.PreviewLabHintMarkerInterface, ClassKind.INTERFACE) {
            modality = Modality.ABSTRACT
        }
        klass.markAsDeprecatedHidden(session)
        klass.markAsInternalSyntheticHint(session)
        return klass.symbol
    }

    override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> = emptyList()

    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> =
        emptySet()

    /**
     * Generates the hint function stub.
     *
     * **Emitted Kotlin (semantically equivalent), per hash + scope**:
     * ```kotlin
     * public fun previewHint_<scope>(value: PreviewHintMarker_<sanitized_fqn>_<hash>?): CollectedPreview
     * ```
     *
     * The function name encodes the scope (`previewHint_default` for unscoped previews;
     * `previewHint_design` for `@ComposePreviewLabOption(collectScopes = ["design"])`). Within
     * a scope the marker class parameter still makes the IdSignature unique per `@Preview`,
     * which lets multiple `@Preview` declarations share the same scope-suffixed name. The
     * IR side rewrites `collectModulePreviews(scope = "design")` into a per-scope
     * `referenceFunctions` lookup, so cross-module discovery is filtered at lookup time
     * rather than after the fact.
     *
     * No body is emitted here (FIR cannot hold a body). The [PreviewKeys.PreviewLabHint] origin
     * is the signal that the IR side
     * ([me.tbsten.compose.preview.lab.compiler.feature.previewCollection.ir.collectPreviewsReplacement.FillPreviewHintIrBody])
     * uses to fill in a body that returns the corresponding `CollectedPreview(...)`
     * constructor call.
     *
     * Each generated hint function is then annotated by `markAsInternalSyntheticHint`,
     * which attaches `@InternalComposePreviewLabApi` (BCV filtering) and
     * `@SyntheticPreviewHint` (IR-side `DiscoverHints` positive-proof marker so namespace
     * squatting cannot inject hints into a downstream consumer's `collectAllModulePreviews`).
     */
    override fun generateFunctions(callableId: CallableId, context: MemberGenerationContext?): List<FirNamedFunctionSymbol> {
        // Unrecognised callable id → not ours to generate. The pre-computed map covers the
        // package-name and `previewHint_*` prefix checks at construction time, so the
        // single `null` branch here replaces three sequential early returns.
        val matchingEntries = entriesByHintCallableId[callableId] ?: return emptyList()

        val collectedPreviewSymbol = session.symbolProvider
            .getClassLikeSymbolByClassId(COLLECTED_PREVIEW_CLASS_ID)
            ?: return emptyList()
        val collectedPreviewType = collectedPreviewSymbol.constructType(emptyArray())

        return matchingEntries.map { entry ->
            val markerClassId = ClassId(
                HINT_PACKAGE,
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
                PreviewKeys.PreviewLabHint,
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
            }.also {
                it.markAsDeprecatedHidden(session)
                it.markAsInternalSyntheticHint(session)
            }.symbol
        }
    }
}
