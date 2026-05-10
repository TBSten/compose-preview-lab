@file:OptIn(
    org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi::class,
)

package me.tbsten.compose.preview.lab.compiler.feature.previewCollection.fir.hintAndMarkerGeneration

import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.compiler.PreviewLabConstants
import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.compat.getBooleanArgumentCompat
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.PreviewKeys
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.buildMarkerShortName
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.buildPreviewHintCanonicalKey
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.computeHintHash
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.parameterTypeFqns
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.previewLabFirBuiltIns
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.fir.hintGeneration.markAsDeprecatedHidden
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.fir.markAsInternalSyntheticHint
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.toAnnotationClassIdSafe
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
 * IR side ([me.tbsten.compose.preview.lab.compiler.ir.PreviewHintIrBodyFiller]).
 *
 * The two non-`@Deprecated` markers serve complementary roles:
 * * `@InternalComposePreviewLabApi` keeps the marker class and the hint function out of the
 *   BCV baseline on every CMP target (KLIB / JVM / Android), via the existing
 *   `nonPublicMarkers` filter in `apiValidation`.
 * * `@SyntheticPreviewHint` is the IR-side `HintDiscovery`'s positive proof that this
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
internal class PreviewHintFirGenerator(session: FirSession, private val compat: CompatContext,) :
    FirDeclarationGenerationExtension(session) {

    /**
     * Predicate that locates the `@Preview` functions to generate hints for. Targets both
     * the CMP and Android `@Preview` annotations.
     */
    private val previewPredicate: LookupPredicate = LookupPredicate.create {
        annotated(
            PreviewLabConstants.CMP_PREVIEW_ANNOTATION_FQN,
            PreviewLabConstants.ANDROID_PREVIEW_ANNOTATION_FQN,
        )
    }

    /**
     * Auxiliary predicate that pulls `@ComposePreviewLabOption` into the FIR
     * predicate-based provider's resolved annotation set.
     *
     * Without this, the annotation's `annotationTypeRef` stays unresolved on `@Preview`
     * symbols during STATUS phase: `toAnnotationClassIdSafe(session)` then returns null
     * for the option annotation, even after `lazyResolveToPhase(ANNOTATION_ARGUMENTS)`.
     * Registering the predicate signals the resolver that this annotation type must be
     * eagerly resolved on every symbol it appears on, so [computeHintEntries] can read
     * `ignore = true` reliably.
     */
    private val optionPredicate: LookupPredicate = LookupPredicate.create {
        annotated(PreviewLabConstants.COMPOSE_PREVIEW_LAB_OPTION_FQN)
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
                    val callableId = PreviewLabConstants.hintFunctionCallableId(scope)
                    getOrPut(callableId) { mutableListOf() }.add(entry)
                }
            }
        }
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(previewPredicate)
        register(optionPredicate)
    }

    override fun getTopLevelClassIds(): Set<ClassId> = hintEntries.mapTo(mutableSetOf()) { entry ->
        ClassId(PreviewLabConstants.HINT_PACKAGE, Name.identifier(entry.markerShortName))
    }

    override fun getTopLevelCallableIds(): Set<CallableId> {
        if (hintEntries.isEmpty()) return emptySet()
        // One callable id per distinct scope across every entry. The marker class parameter
        // still makes the IdSignature unique per `@Preview`, so multiple hints (whether from
        // different previews or from the same preview registered into multiple scopes) can
        // share the same scope-suffixed callable name without colliding.
        return hintEntries.flatMapTo(mutableSetOf()) { entry ->
            entry.scopes.map { PreviewLabConstants.hintFunctionCallableId(it) }
        }
    }

    override fun hasPackage(packageFqName: FqName): Boolean = packageFqName == PreviewLabConstants.HINT_PACKAGE

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
        if (classId.packageFqName != PreviewLabConstants.HINT_PACKAGE) return null
        val shortName = classId.shortClassName.asString()
        if (shortName !in hashByMarkerShortName) return null

        val klass = createTopLevelClass(classId, PreviewKeys.PreviewLabHintMarkerInterface, ClassKind.INTERFACE) {
            modality = Modality.ABSTRACT
        }
        klass.markAsDeprecatedHidden(session, compat)
        klass.markAsInternalSyntheticHint(session, compat)
        return klass.symbol
    }

    override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> = emptyList()

    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext,): Set<Name> =
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
     * ([me.tbsten.compose.preview.lab.compiler.ir.PreviewHintIrBodyFiller]) uses to fill in
     * a body that returns the corresponding `CollectedPreview(...)` constructor call.
     *
     * Each generated hint function is then annotated by `markAsInternalSyntheticHint`,
     * which attaches `@InternalComposePreviewLabApi` (BCV filtering) and
     * `@SyntheticPreviewHint` (IR-side `HintDiscovery` positive-proof marker so namespace
     * squatting cannot inject hints into a downstream consumer's `collectAllModulePreviews`).
     */
    override fun generateFunctions(callableId: CallableId, context: MemberGenerationContext?): List<FirNamedFunctionSymbol> {
        // Unrecognised callable id → not ours to generate. The pre-computed map covers the
        // package-name and `previewHint_*` prefix checks at construction time, so the
        // single `null` branch here replaces three sequential early returns.
        val matchingEntries = entriesByHintCallableId[callableId] ?: return emptyList()

        val collectedPreviewSymbol = session.symbolProvider
            .getClassLikeSymbolByClassId(PreviewLabConstants.COLLECTED_PREVIEW_CLASS_ID)
            ?: return emptyList()
        val collectedPreviewType = collectedPreviewSymbol.constructType(emptyArray())

        return matchingEntries.map { entry ->
            val markerClassId = ClassId(
                PreviewLabConstants.HINT_PACKAGE,
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
                it.markAsDeprecatedHidden(session, compat)
                it.markAsInternalSyntheticHint(session, compat)
            }.symbol
        }
    }

    /**
     * Walks the predicate and computes the hint hash, marker class short name, and
     * collection scope for each `@Preview` function.
     *
     * **Behavior on `@ComposePreviewLabOption(ignore = true)`**: such `@Preview` symbols are
     * filtered out *before* hint emission, so neither the marker interface nor the scope-
     * suffixed `previewHint_<scope>` overload is generated for them. Cross-module consumers
     * therefore cannot discover ignored previews via `referenceFunctions`. The annotation
     * argument is read via `resolvedAnnotationsWithArguments` (lazy advance to
     * `ANNOTATION_ARGUMENTS` phase), preferring the stdlib `getBooleanArgument` helper but
     * falling back to a manual walk of the raw `argumentList.arguments` when the resolved
     * name → expression mapping is empty (a state observed for inherit-classpath builds in
     * our kctfork tests). See [isIgnoredByComposePreviewLabOption] for the readout details.
     *
     * **Behavior on `@ComposePreviewLabOption(collectScopes = [...])`**: each scope value is
     * read with [resolveCollectScopes] and embedded into a synthetic hint function name
     * (`previewHint_<scope>`), one overload per element in the array. Regex validation
     * (`[A-Za-z0-9_]+`) of those elements happens earlier in
     * `me.tbsten.compose.preview.lab.compiler.fir.checkers.CollectScopeAnnotationChecker`
     * (FIR `CHECKERS` phase), so by the time we get here invalid values have already been
     * surfaced to the user as clickable diagnostics. We defensively filter them out again
     * to avoid emitting Kotlin identifiers that would crash the compiler downstream — a
     * silent drop is fine because the checker has already failed the build.
     *
     * Evaluated lazily the first time [getTopLevelClassIds] / [getTopLevelCallableIds] is
     * touched.
     */
    private fun computeHintEntries(): List<HintEntry> {
        val symbols = session.predicateBasedProvider.getSymbolsByPredicate(previewPredicate)
        return symbols
            .filterIsInstance<FirNamedFunctionSymbol>()
            .filter { it.callableId.classId == null }
            .filterNot { symbol -> symbol.isIgnoredByComposePreviewLabOption() }
            .mapNotNull { symbol ->
                val callableId = symbol.callableId
                val packageName = callableId.packageName.asString()
                val simpleName = callableId.callableName.asString()
                val sourceFqn = if (packageName.isEmpty()) simpleName else "$packageName.$simpleName"
                val scopes = resolveCollectScopes(symbol, sourceFqn) ?: return@mapNotNull null
                val parameterTypeFqns = symbol.parameterTypeFqnsForHash()
                val canonicalKey = buildPreviewHintCanonicalKey(sourceFqn, parameterTypeFqns)
                val hash = computeHintHash(canonicalKey)
                HintEntry(
                    hash = hash,
                    markerShortName = buildMarkerShortName(sourceFqn, hash),
                    scopes = scopes,
                )
            }
            .distinctBy { it.markerShortName }
    }

    /**
     * Index of `collectScope` in the `@ComposePreviewLabOption(displayName, ignore, id, collectScope)`
     * parameter list. Used by [resolveCollectScopes] when walking unnamed positional arguments —
     * only the literal at this index is interpreted as `collectScope`, mirroring the same
     * positional-fallback strategy as [ignoreParameterIndex].
     */
    private val collectScopeParameterIndex: Int = 3

    /**
     * Resolves the `@ComposePreviewLabOption(collectScopes = [...])` argument for [symbol]
     * into a concrete list of scope strings, substituting the sentinel
     * `ComposePreviewLabOption.DefaultCollectScope` ( = `"default"`) with the module-level
     * `composePreviewLab.collectPreviews.defaultCollectScope` Gradle DSL value
     * ([me.tbsten.compose.preview.lab.compiler.feature.previewCollection.PreviewLabFirBuiltIns.config]'s
     * `defaultCollectScope`). That means a library
     * module pinning every preview to a library-specific bucket only needs the Gradle DSL
     * line — no per-`@Preview` annotation required.
     *
     * **Resolution rules** (assuming Gradle DSL `defaultCollectScope = "acme_ui"`):
     * - no `@ComposePreviewLabOption` → `["acme_ui"]`
     * - `@ComposePreviewLabOption()` (`collectScopes` defaulted) → `["acme_ui"]`
     * - `@ComposePreviewLabOption(collectScopes = ["acme_ui"])` → `["acme_ui"]`
     * - `@ComposePreviewLabOption(collectScopes = ["custom"])` → `["custom"]` (override wins)
     * - `@ComposePreviewLabOption(collectScopes = ["custom", DefaultCollectScope])` → `["custom", "acme_ui"]`
     *
     * Validation against `[A-Za-z0-9_]+` happens in
     * [me.tbsten.compose.preview.lab.compiler.fir.checkers.CollectScopeAnnotationChecker]
     * during the FIR `CHECKERS` phase, so by the time this method runs any value that
     * does not match has already been surfaced as a clickable diagnostic. We still
     * defensively drop invalid elements here to avoid emitting Kotlin identifiers that
     * would crash the compiler downstream — a silent drop is fine because the Checker has
     * already failed the build.
     */
    private fun resolveCollectScopes(symbol: FirNamedFunctionSymbol, sourceFqn: String): List<String>? {
        symbol.lazyResolveToPhase(FirResolvePhase.ANNOTATION_ARGUMENTS)
        val moduleDefault = session.previewLabFirBuiltIns.config.defaultCollectScope
        val optionAnnotation = symbol.resolvedAnnotationsWithArguments
            .firstOrNull { it.toAnnotationClassIdSafe(session) == PreviewLabConstants.COMPOSE_PREVIEW_LAB_OPTION_CLASS_ID }
            ?: return listOf(moduleDefault)

        val rawScopes = optionAnnotation.readCollectScopesFromRawArguments()
            ?.takeUnless { it.isEmpty() }
            ?: return listOf(moduleDefault)

        val resolved = rawScopes
            .map { if (it == ComposePreviewLabOption.DefaultCollectScope) moduleDefault else it }
            .filter { PreviewLabConstants.SCOPE_VALIDATION_REGEX.matches(it) }
            .distinct()
        return resolved.ifEmpty { null }
    }

    /**
     * Manual fallback that reads the `Array<String>` `collectScope` argument from the raw
     * `argumentList.arguments` of the FIR annotation. Accepts:
     *
     * - `(collectScopes = ["design"])` — `FirArrayLiteral` / `FirArrayOfCall` named.
     * - `("X", false, "id", ["design"])` — positional array at [collectScopeParameterIndex].
     * - `(collectScopes = "design")` — defensive single-string fallback for callers that
     *   were compiled against a pre-Array binary baseline.
     */
    private fun org.jetbrains.kotlin.fir.expressions.FirAnnotation.readCollectScopesFromRawArguments(): List<String>? {
        val annotationCall = this as? org.jetbrains.kotlin.fir.expressions.FirAnnotationCall ?: return null
        for ((argumentIndex, argument) in annotationCall.argumentList.arguments.withIndex()) {
            val (argumentName, argumentExpression) = when (argument) {
                is org.jetbrains.kotlin.fir.expressions.FirNamedArgumentExpression ->
                    argument.name to argument.expression
                else -> null to argument
            }
            val isCollectScopeArgument = when {
                argumentName == PreviewLabConstants.COLLECT_SCOPE_NAME -> true
                argumentName != null -> false
                else -> argumentIndex == collectScopeParameterIndex
            }
            if (!isCollectScopeArgument) continue

            // Vararg form: `Array<String>` annotation values arrive on this code path
            // when the annotation is read from a compiled binary, where the elements are
            // packed into a `FirVarargArgumentsExpression`.
            (argumentExpression as? org.jetbrains.kotlin.fir.expressions.FirVarargArgumentsExpression)
                ?.let { vararg ->
                    return vararg.arguments.mapNotNull {
                        (it as? org.jetbrains.kotlin.fir.expressions.FirLiteralExpression)?.value as? String
                    }
                }

            // Array literal form: `["a", "b"]` written directly in source surfaces as a
            // `FirCall` (specifically `FirCollectionLiteral`) whose `argumentList.arguments`
            // are the element expressions.
            (argumentExpression as? org.jetbrains.kotlin.fir.expressions.FirCall)?.let { call ->
                return call.argumentList.arguments.mapNotNull {
                    (it as? org.jetbrains.kotlin.fir.expressions.FirLiteralExpression)?.value as? String
                }
            }

            // Single-string form (compatibility with pre-Array binaries).
            (argumentExpression as? org.jetbrains.kotlin.fir.expressions.FirLiteralExpression)?.let { literal ->
                return (literal.value as? String)?.let(::listOf)
            }
        }
        return null
    }

    /**
     * Index of `ignore` in the `@ComposePreviewLabOption(displayName, ignore, id)` parameter
     * list. Used by [isIgnoredByComposePreviewLabOption] when walking unnamed positional
     * arguments — only the literal at this index is interpreted as `ignore`, so future
     * additions of other `Boolean` parameters cannot accidentally flip the readout.
     */
    private val ignoreParameterIndex: Int = 1

    /**
     * Returns true when the `@Preview` symbol also carries
     * `@ComposePreviewLabOption(ignore = true)`. Used by [computeHintEntries] to skip
     * ignored previews up front, so neither the marker interface nor the `previewHint`
     * overload is emitted for them.
     *
     * **Behavior**:
     * ```kotlin
     * @Preview
     * @ComposePreviewLabOption(ignore = true)
     * fun Hidden()                                 // → true   (filtered out)
     *
     * @Preview
     * fun Plain()                                  // → false  (no option annotation)
     *
     * @Preview
     * @ComposePreviewLabOption(displayName = "X")  // → false  (`ignore` omitted, default false)
     * fun Named()
     * ```
     *
     * # Why the manual argument walk
     *
     * The stdlib `getBooleanArgument` helper relies on the resolved
     * `argumentMapping.mapping` (name → expression) populated during the
     * `ANNOTATION_ARGUMENTS` resolve phase. In practice — and in particular when the
     * generator runs on a source build that has `@ComposePreviewLabOption` declared in
     * the same module's `inheritClassPath` — the named mapping is empty even after
     * `lazyResolveToPhase(ANNOTATION_ARGUMENTS)`, while the raw `argumentList.arguments`
     * does carry the literal. So the helper is tried first (cheap, correct when
     * available) and a manual `FirLiteralExpression` cast off the raw argument list is
     * the fallback.
     *
     * # Which argument forms are recognized
     *
     * - `(ignore = true)` — matched by `argumentName == IGNORE_NAME` and accepted regardless
     *   of position.
     * - `("X", true)` — positional Boolean literal at the slot reserved for `ignore`
     *   ([ignoreParameterIndex]). Other positional indices are ignored even if they hold
     *   a Boolean literal, so the readout cannot misfire if `ComposePreviewLabOption` ever
     *   gains another `Boolean` parameter elsewhere in the parameter list.
     * - `(true)` alone — does not compile (the preceding `displayName: String` has no
     *   default that can be skipped positionally), so this case never occurs in practice.
     */
    private fun FirNamedFunctionSymbol.isIgnoredByComposePreviewLabOption(): Boolean {
        lazyResolveToPhase(FirResolvePhase.ANNOTATION_ARGUMENTS)
        val optionAnnotation = resolvedAnnotationsWithArguments
            .firstOrNull { it.toAnnotationClassIdSafe(session) == PreviewLabConstants.COMPOSE_PREVIEW_LAB_OPTION_CLASS_ID }
            ?: return false

        // Fast path: stdlib helper using the resolved name → expression mapping. Returns
        // null when the mapping is empty (common in our tests' inherit-classpath setup),
        // which falls through to the manual argument walk below.
        optionAnnotation.getBooleanArgumentCompat(PreviewLabConstants.IGNORE_NAME, session)?.let { return it }

        // Fallback: walk the raw argument list. Accepts named (`ignore = true`) directly,
        // and positional Boolean literals only when they sit in the `ignore` parameter
        // slot ([ignoreParameterIndex]). This guards against future `ComposePreviewLabOption`
        // signature changes that might add another Boolean parameter.
        val annotationCall = optionAnnotation as? org.jetbrains.kotlin.fir.expressions.FirAnnotationCall
            ?: return false
        for ((argumentIndex, argument) in annotationCall.argumentList.arguments.withIndex()) {
            val (argumentName, argumentExpression) = when (argument) {
                is org.jetbrains.kotlin.fir.expressions.FirNamedArgumentExpression ->
                    argument.name to argument.expression
                else -> null to argument
            }
            val isIgnoreArgument = when {
                argumentName == PreviewLabConstants.IGNORE_NAME -> true
                argumentName != null -> false // some other named argument
                else -> argumentIndex == ignoreParameterIndex // positional fallback
            }
            if (!isIgnoreArgument) continue
            val literal = argumentExpression as? org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
                ?: continue
            val booleanValue = literal.value as? Boolean ?: continue
            return booleanValue
        }
        return false
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

    /** Metadata for the marker / hint function pair(s) generated for one `@Preview`. */
    private data class HintEntry(
        /** Canonical-key sha256 truncated to 8 base-36 chars (`computeHintHash` output). */
        val hash: String,
        /** Marker interface short name `PreviewHintMarker_<sanitized_fqn>_<hash>`. */
        val markerShortName: String,
        /**
         * Collection scopes this preview participates in. One marker class is emitted, and
         * one `previewHint_<scope>` overload is emitted per element. `["default"]` when no
         * `@ComposePreviewLabOption(collectScopes = [...])` is present.
         */
        val scopes: List<String>,
    )
}
