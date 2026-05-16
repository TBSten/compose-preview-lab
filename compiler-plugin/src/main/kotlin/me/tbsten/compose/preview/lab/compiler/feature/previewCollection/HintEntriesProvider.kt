package me.tbsten.compose.preview.lab.compiler.feature.previewCollection

import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.compiler.compat.getBooleanArgumentCompat
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.fir.scopeValidation.SCOPE_VALIDATION_REGEX
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.toAnnotationClassIdSafe
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirAnnotationCall
import org.jetbrains.kotlin.fir.expressions.FirCall
import org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
import org.jetbrains.kotlin.fir.expressions.FirNamedArgumentExpression
import org.jetbrains.kotlin.fir.expressions.FirVarargArgumentsExpression
import org.jetbrains.kotlin.fir.extensions.FirExtensionSessionComponent
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase

/**
 * Metadata for the marker / hint function pair(s) generated for one `@Preview`.
 *
 * Hint generator and marker generator both consume this — one entry produces:
 * - exactly one marker interface (`PreviewHintMarker_<sanitized_fqn>_<hash>`)
 * - one hint function overload per scope (`previewHint_<scope>(value: ...): CollectedPreview`)
 */
internal data class HintEntry(
    /** Canonical-key sha256 truncated to 8 base-36 chars ([computeHintHash] output). */
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

/**
 * Session-scoped cache of the per-`@Preview` [HintEntry] list, consumed by
 * [me.tbsten.compose.preview.lab.compiler.feature.previewCollection.fir.hintAndMarkerGeneration.PreviewHintFirGenerator]
 * (which emits both the marker interface and the hint-function overloads from the same
 * entry list).
 *
 * **Why a separate `FirExtensionSessionComponent`**: extracting the walk lets the
 * generator focus on declaration emission while this component owns the shared cache
 * lifecycle. If hint and marker generation are later split into two
 * `FirDeclarationGenerationExtension`s, the second consumer slots into the same cache
 * without re-walking `predicateBasedProvider.getSymbolsByPredicate(...)`. The provider
 * performs the walk once (lazily on first touch) and exposes the same list to every
 * consumer.
 *
 * **Lazy by design**: the FIR predicate-based walk inside the provider's body triggers
 * a Kotlin 2.3.21 frontend resolution cycle if executed eagerly. Deferring the
 * computation behind `by lazy { ... }` ensures it only runs from inside a generator's
 * `getTopLevelClassIds` / `getTopLevelCallableIds` callback (which is the safe entry
 * point for the predicate provider).
 *
 * **Sample access** (inside a hint or marker generator):
 * ```kotlin
 * override fun getTopLevelClassIds(): Set<ClassId> =
 *     session.hintEntriesProvider.hintEntries.mapTo(mutableSetOf()) { entry ->
 *         ClassId(HINT_PACKAGE, Name.identifier(entry.markerShortName))
 *     }
 * ```
 */
internal class HintEntriesProvider(session: FirSession) : FirExtensionSessionComponent(session) {

    /**
     * One entry per `@Preview` discovered in this session, tying it to its marker /
     * hint pair. Walked once across the whole FIR session, shared by every generator
     * that needs it.
     */
    val hintEntries: List<HintEntry> by lazy { computeHintEntries() }

    /**
     * Walks the `@Preview` predicate and computes the hint hash, marker class short
     * name, and collection scope list for each function.
     *
     * **Behavior on `@ComposePreviewLabOption(ignore = true)`**: such `@Preview` symbols
     * are filtered out *before* hint emission. Cross-module consumers therefore cannot
     * discover ignored previews via `referenceFunctions`. The argument is read via
     * `resolvedAnnotationsWithArguments` (lazy advance to `ANNOTATION_ARGUMENTS` phase),
     * preferring the stdlib `getBooleanArgument` helper but falling back to a manual
     * walk of the raw `argumentList.arguments` when the resolved name → expression
     * mapping is empty (a state observed for inherit-classpath builds in our kctfork
     * tests).
     *
     * **Behavior on `@ComposePreviewLabOption(collectScopes = [...])`**: each scope is
     * read with [resolveCollectScopes] and one overload per element is emitted by the
     * hint generator. Regex validation (`[A-Za-z0-9_]+`) of those elements happens
     * earlier in the FIR Checker. Invalid values are still defensively dropped here to
     * avoid emitting Kotlin identifiers that would crash the compiler downstream.
     */
    private fun computeHintEntries(): List<HintEntry> {
        val symbols = session.predicateBasedProvider.getSymbolsByPredicate(
            PreviewAnnotationPredicates.previewPredicate,
        )
        return symbols
            .filterIsInstance<FirNamedFunctionSymbol>()
            .filter { it.callableId.classId == null }
            .filterNot { symbol -> symbol.isIgnoredByComposePreviewLabOption() }
            .mapNotNull { symbol ->
                val callableId = symbol.callableId
                val packageName = callableId.packageName.asString()
                val simpleName = callableId.callableName.asString()
                val sourceFqn = if (packageName.isEmpty()) simpleName else "$packageName.$simpleName"
                val scopes = resolveCollectScopes(symbol) ?: return@mapNotNull null
                val parameterTypeFqns = symbol.parameterTypeFqns()
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
     * Resolves `@ComposePreviewLabOption(collectScopes = [...])` for [symbol] into a
     * concrete scope list, substituting `ComposePreviewLabOption.DefaultCollectScope`
     * with the module-level default. Returns `null` when all elements were invalid (=
     * Checker has already failed the build); the hint generator silently skips entries
     * with `null` scopes.
     */
    private fun resolveCollectScopes(symbol: FirNamedFunctionSymbol): List<String>? {
        symbol.lazyResolveToPhase(FirResolvePhase.ANNOTATION_ARGUMENTS)
        val moduleDefault = session.previewLabFirBuiltIns.config.defaultCollectScope
        val optionAnnotation = symbol.resolvedAnnotationsWithArguments
            .firstOrNull { it.toAnnotationClassIdSafe(session) == COMPOSE_PREVIEW_LAB_OPTION_CLASS_ID }
            ?: return listOf(moduleDefault)

        val rawScopes = optionAnnotation.readCollectScopesFromRawArguments()
            ?.takeUnless { it.isEmpty() }
            ?: return listOf(moduleDefault)

        val resolved = rawScopes
            .map { if (it == ComposePreviewLabOption.DefaultCollectScope) moduleDefault else it }
            .filter { SCOPE_VALIDATION_REGEX.matches(it) }
            .distinct()
        return resolved.ifEmpty { null }
    }

    /**
     * Manual fallback reader for `Array<String>` `collectScopes` arguments. Accepts the
     * named (`(collectScopes = ["d"])`), positional (4th argument), vararg / array
     * literal / single-string compatibility forms — see commit history of the previous
     * `PreviewHintFirGenerator` for the empirical reasoning behind each branch.
     */
    private fun FirAnnotation.readCollectScopesFromRawArguments(): List<String>? {
        val annotationCall = this as? FirAnnotationCall ?: return null
        for ((argumentIndex, argument) in annotationCall.argumentList.arguments.withIndex()) {
            val (argumentName, argumentExpression) = when (argument) {
                is FirNamedArgumentExpression -> argument.name to argument.expression
                else -> null to argument
            }
            val isCollectScopeArgument = when {
                argumentName == COLLECT_SCOPE_NAME -> true
                argumentName != null -> false
                else -> argumentIndex == CollectScopesParameterIndex
            }
            if (!isCollectScopeArgument) continue

            (argumentExpression as? FirVarargArgumentsExpression)?.let { vararg ->
                return vararg.arguments.mapNotNull {
                    (it as? FirLiteralExpression)?.value as? String
                }
            }

            (argumentExpression as? FirCall)?.let { call ->
                return call.argumentList.arguments.mapNotNull {
                    (it as? FirLiteralExpression)?.value as? String
                }
            }

            (argumentExpression as? FirLiteralExpression)?.let { literal ->
                return (literal.value as? String)?.let(::listOf)
            }
        }
        return null
    }

    /**
     * Whether the `@Preview` symbol also carries
     * `@ComposePreviewLabOption(ignore = true)`. Tries the stdlib
     * `getBooleanArgumentCompat` fast path first; falls back to a manual walk of raw
     * `argumentList.arguments` when the resolved name-→-expression mapping is empty
     * (observed for inherit-classpath kctfork builds).
     */
    private fun FirNamedFunctionSymbol.isIgnoredByComposePreviewLabOption(): Boolean {
        lazyResolveToPhase(FirResolvePhase.ANNOTATION_ARGUMENTS)
        val optionAnnotation = resolvedAnnotationsWithArguments
            .firstOrNull { it.toAnnotationClassIdSafe(session) == COMPOSE_PREVIEW_LAB_OPTION_CLASS_ID }
            ?: return false

        optionAnnotation.getBooleanArgumentCompat(IGNORE_NAME, session)?.let { return it }

        val annotationCall = optionAnnotation as? FirAnnotationCall ?: return false
        for ((argumentIndex, argument) in annotationCall.argumentList.arguments.withIndex()) {
            val (argumentName, argumentExpression) = when (argument) {
                is FirNamedArgumentExpression -> argument.name to argument.expression
                else -> null to argument
            }
            val isIgnoreArgument = when {
                argumentName == IGNORE_NAME -> true
                argumentName != null -> false
                else -> argumentIndex == IgnoreParameterIndex
            }
            if (!isIgnoreArgument) continue
            val literal = argumentExpression as? FirLiteralExpression ?: continue
            val booleanValue = literal.value as? Boolean ?: continue
            return booleanValue
        }
        return false
    }
}

/** Session accessor — pattern adapted from [PreviewLabFirBuiltIns]. */
internal val FirSession.hintEntriesProvider: HintEntriesProvider by FirSession.sessionComponentAccessor()
