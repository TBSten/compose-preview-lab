@file:OptIn(
    org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi::class,
)

package me.tbsten.compose.preview.lab.compiler.fir

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.getBooleanArgument
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
internal class PreviewHintFirGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {

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
        annotated(PreviewLabFirBuiltIns.COMPOSE_PREVIEW_LAB_OPTION_FQN)
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
        register(optionPredicate)
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
     */
    override fun generateTopLevelClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>? {
        if (classId.packageFqName != PreviewLabFirBuiltIns.HINT_PACKAGE) return null
        val shortName = classId.shortClassName.asString()
        if (shortName !in hashByMarkerShortName) return null

        // TODO: the marker is purely internal (its only purpose is to disambiguate the
        //   IdSignature per `@Preview`), so it should switch to `Modality.SEALED` to lock out
        //   external implementations. Deferred because `sealed` + `@Deprecated(HIDDEN)` needs
        //   cross-Kotlin-version (2.1 through 2.4-Beta) and KLIB IC validation.
        //   follow-up: .local/ticket/followup-sealed-hint-marker-interface.md
        // TODO: add `@Deprecated(level = HIDDEN)` so user code cannot accidentally reference
        //   the marker. Deferred because building the FIR annotation has non-trivial
        //   boilerplate that needs cross-Kotlin-version compatibility checks.
        //   follow-up: .local/ticket/followup-deprecated-hidden-hint-declarations.md
        val klass = createTopLevelClass(classId, Keys.PreviewLabHintMarkerInterface, ClassKind.INTERFACE) {
            modality = Modality.ABSTRACT
        }
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
            // TODO: add `@Deprecated(level = HIDDEN)` so user code cannot directly invoke
            //   `previewHint(...)`. Cross-module discovery via `referenceFunctions` should
            //   still work. follow-up: .local/ticket/followup-deprecated-hidden-hint-declarations.md
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
            }.symbol
        }
    }

    /**
     * Walks the predicate and computes the hint hash plus marker class short name for each
     * `@Preview` function.
     *
     * **Behavior on `@ComposePreviewLabOption(ignore = true)`**: such `@Preview` symbols are
     * filtered out *before* hint emission, so neither the marker interface nor the
     * `previewHint` overload is generated for them. Cross-module consumers therefore cannot
     * discover ignored previews via `referenceFunctions`. The annotation argument is read
     * via `resolvedAnnotationsWithArguments` (lazy advance to `ANNOTATION_ARGUMENTS` phase),
     * preferring the stdlib `getBooleanArgument` helper but falling back to a manual walk of
     * the raw `argumentList.arguments` when the resolved name → expression mapping is empty
     * (a state observed for inherit-classpath builds in our kctfork tests). See
     * [isIgnoredByComposePreviewLabOption] for the readout details.
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
            .firstOrNull { it.toAnnotationClassIdSafe(session) == PreviewLabFirBuiltIns.COMPOSE_PREVIEW_LAB_OPTION_CLASS_ID }
            ?: return false

        // Fast path: stdlib helper using the resolved name → expression mapping. Returns
        // null when the mapping is empty (common in our tests' inherit-classpath setup),
        // which falls through to the manual argument walk below.
        optionAnnotation.getBooleanArgument(PreviewLabFirBuiltIns.IGNORE_NAME, session)?.let { return it }

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
                argumentName == PreviewLabFirBuiltIns.IGNORE_NAME -> true
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

    /** Metadata for the marker / hint function pair generated for one `@Preview`. */
    private data class HintEntry(
        /** Canonical-key sha256 truncated to 8 base-36 chars (`computeHintHash` output). */
        val hash: String,
        /** Marker interface short name `PreviewHintMarker_<sanitized_fqn>_<hash>`. */
        val markerShortName: String,
    )
}
