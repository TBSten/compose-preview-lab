package me.tbsten.compose.preview.lab

import kotlin.properties.ReadOnlyProperty

/**
 * Provides a delegate that collects `@Preview` functions from **this module only**.
 *
 * The Compose Preview Lab compiler plugin replaces the body at compile time so that the returned
 * delegate holds the actual preview list. If the compiler plugin is not applied,
 * accessing the value throws an [IllegalStateException].
 *
 * The returned [PreviewExport] functions as a property delegate for a `List<CollectedPreview>`.
 * It wraps a [Lazy] so that `@Composable` lambdas inside [CollectedPreview] are initialized
 * on first access rather than at class-load time, avoiding `ExceptionInInitializerError`.
 * The same wrapper acts as a marker type that downstream modules pick up automatically —
 * see [collectAllModulePreviews].
 *
 * This stable no-arg overload delegates to the experimental scope-aware overload with the
 * default-scope sentinel, so consumers do not have to opt in unless they need explicit
 * scope filtering. See the scope-aware overload for the full scope semantics.
 *
 * Example — single-module preview collection:
 * ```kotlin
 * val myPreviews by collectModulePreviews()
 * ```
 *
 * Example — used in a library module with public visibility:
 * ```kotlin
 * // uiLib/src/commonMain/kotlin/Previews.kt
 * val uiLibPreviews by collectModulePreviews()
 * ```
 *
 * @return a [PreviewExport] delegate wrapping the collected preview list; the body is replaced by the compiler plugin
 * @see collectAllModulePreviews
 */
@OptIn(InternalComposePreviewLabApi::class, ExperimentalComposePreviewLabApi::class)
public fun collectModulePreviews(): ReadOnlyProperty<Any?, Sequence<CollectedPreview>> =
    collectModulePreviews(scope = ComposePreviewLabOption.DefaultCollectScope)

/**
 * Scope-aware variant of [collectModulePreviews] that limits the result to previews
 * whose `@ComposePreviewLabOption(collectScopes = [...])` array contains [scope].
 *
 * **Experimental**: the scope feature is still stabilizing. Consumers must opt in with
 * `@OptIn(ExperimentalComposePreviewLabApi::class)` (or `@file:OptIn(...)`) to call this
 * overload. The no-arg [collectModulePreviews] overload remains stable for callers that
 * do not need explicit scope filtering.
 *
 * Example:
 * ```kotlin
 * @file:OptIn(me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi::class)
 *
 * val buttonPreviews by collectModulePreviews(scope = "buttons")
 * ```
 *
 * @param scope The collection scope to draw from. Only previews annotated with the
 * matching `@ComposePreviewLabOption(collectScopes = ["..."])` end up in the result.
 * The literal value `"default"` (= [ComposePreviewLabOption.DefaultCollectScope]) acts as
 * a sentinel: it does **not** mean a literal `"default"` bucket — the compiler plugin
 * substitutes it with the module's `composePreviewLab.collectPreviews.defaultCollectScope`
 * Gradle DSL value (which itself defaults to `"default"`). So a library that pins every
 * preview to its own bucket via `defaultCollectScope = "acme_ui"` automatically reads
 * back the same bucket here.
 *
 * The argument must reach the compiler plugin's IR pass as a compile-time string
 * constant — an inline string literal or a `const val` reference both work, because
 * both end up as an `IrConst<String>` before our pass runs. Non-`const` vals, string
 * concatenations, and other expressions that produce an `IrCall` /
 * `IrStringConcatenation` are reported as compile errors. The resolved value must also
 * match `[A-Za-z0-9_]+` because the plugin embeds it into the synthetic hint function
 * name.
 * @return a [PreviewExport] delegate wrapping the collected preview list; the body is replaced by the compiler plugin
 * @see collectAllModulePreviews
 */
@ExperimentalComposePreviewLabApi
@OptIn(InternalComposePreviewLabApi::class)
public fun collectModulePreviews(scope: String): ReadOnlyProperty<Any?, Sequence<CollectedPreview>> = PreviewExport(
    lazy {
        error(
            "[ComposePreviewLab] collectModulePreviews(scope = \"$scope\") was not replaced by the compiler plugin. " +
                "Apply the Compose Preview Lab Gradle plugin to this module:\n" +
                "  // build.gradle.kts\n" +
                "  plugins {\n" +
                "      id(\"me.tbsten.compose.preview.lab\")\n" +
                "  }\n" +
                "Then re-run the build. See https://github.com/TBSten/compose-preview-lab " +
                "for the full setup guide.",
        )
    },
)

/**
 * Provides a delegate that collects `@Preview` functions from **this module and every
 * dependency module that has the Compose Preview Lab Gradle plugin applied**.
 *
 * The compiler plugin replaces the body at compile time so that the returned [PreviewExport]
 * holds a concatenation of this module's previews and the previews of every dependency module
 * on the classpath. Discovery is fully automatic — applying the Gradle plugin in a dependency
 * module is enough; **`collectModulePreviews()` does not need to be declared** there. Modules
 * that do declare it explicitly continue to work unchanged.
 *
 * **Cross-module aggregation works on every Compose Multiplatform target** (JVM / Android /
 * JS / Wasm JS / iOS) when running Kotlin 2.3.21 or later. The compiler plugin emits a
 * per-`@Preview` hint (`previewHint_<scope>(value: PreviewHintMarker_<hash>?): CollectedPreview` in the
 * `me.tbsten.compose.preview.lab.hints` package) and the consumer side discovers them via
 * `IrPluginContext.referenceFunctions`, which works equally for JVM bytecode and KLIB modules.
 *
 * Older Kotlin (pre-2.3.21) does not support the FIR-based hint generator. The plugin's
 * IR pass detects the `val x by collectAllModulePreviews()` delegated-property pattern and
 * reports a compile-time error with a clear upgrade-or-downgrade message. Direct calls outside
 * a property delegate (which are not the supported usage) compile and fall back to the
 * `IllegalStateException` thrown by the placeholder body. `collectModulePreviews()`
 * (single-module) continues to work on every Kotlin version.
 *
 * Example — app module aggregating its own previews and any library previews on the classpath:
 * ```kotlin
 * // app/src/commonMain/kotlin/Previews.kt
 * val appPreviews by collectAllModulePreviews()
 * ```
 *
 * Example — dependency module just applies the Gradle plugin; no `Previews.kt` file required:
 * ```kotlin
 * // uiLib/build.gradle.kts
 * plugins {
 *     id("me.tbsten.compose.preview.lab")
 * }
 * // any @Preview function in uiLib is now picked up by upstream collectAllModulePreviews()
 * ```
 *
 * This stable no-arg overload delegates to the experimental scope-aware overload with the
 * default-scope sentinel, so consumers do not have to opt in unless they need explicit
 * scope filtering. See the scope-aware overload below for the full scope semantics.
 *
 * @return a [PreviewExport] delegate wrapping the aggregated preview list; the body is replaced by the compiler plugin
 * @see collectModulePreviews
 */
@OptIn(InternalComposePreviewLabApi::class, ExperimentalComposePreviewLabApi::class)
public fun collectAllModulePreviews(): ReadOnlyProperty<Any?, Sequence<CollectedPreview>> =
    collectAllModulePreviews(scope = ComposePreviewLabOption.DefaultCollectScope)

/**
 * Scope-aware variant of [collectAllModulePreviews] that limits the aggregated result to
 * previews whose `@ComposePreviewLabOption(collectScopes = [...])` array contains [scope].
 *
 * **Experimental**: the scope feature is still stabilizing. Consumers must opt in with
 * `@OptIn(ExperimentalComposePreviewLabApi::class)` (or `@file:OptIn(...)`) to call this
 * overload. The no-arg [collectAllModulePreviews] overload remains stable for callers that
 * do not need explicit scope filtering.
 *
 * Example:
 * ```kotlin
 * @file:OptIn(me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi::class)
 *
 * val designPreviews by collectAllModulePreviews(scope = "design")
 * ```
 *
 * @param scope The collection scope to draw from. Only previews annotated with the
 * matching `@ComposePreviewLabOption(collectScopes = ["..."])` end up in the result.
 * The literal value `"default"` (= [ComposePreviewLabOption.DefaultCollectScope]) acts as
 * a sentinel: it does **not** mean a literal `"default"` bucket — the compiler plugin
 * substitutes it with the **calling** module's
 * `composePreviewLab.collectPreviews.defaultCollectScope` Gradle DSL value (which itself
 * defaults to `"default"`). The substitution is **strictly per-module**: a library that
 * pins its own previews to `defaultCollectScope = "acme_ui"` will register them under
 * `acme_ui`, but a downstream consumer app's `collectAllModulePreviews()` (= the
 * sentinel, substituted with the *consumer's* DSL value) will **NOT** see them unless the
 * consumer explicitly asks `collectAllModulePreviews(scope = "acme_ui")`. This isolation
 * is the primary reason the DSL exists.
 *
 * The argument must reach the compiler plugin's IR pass as a compile-time string
 * constant — an inline string literal or a `const val` reference both work, because
 * both end up as an `IrConst<String>` before our pass runs. Non-`const` vals, string
 * concatenations, and other expressions that produce an `IrCall` /
 * `IrStringConcatenation` are reported as compile errors. The resolved value must also
 * match `[A-Za-z0-9_]+` because the plugin embeds it into the synthetic hint function
 * name.
 * @return a [PreviewExport] delegate wrapping the aggregated preview list; the body is replaced by the compiler plugin
 * @see collectModulePreviews
 */
@ExperimentalComposePreviewLabApi
@OptIn(InternalComposePreviewLabApi::class)
public fun collectAllModulePreviews(scope: String): ReadOnlyProperty<Any?, Sequence<CollectedPreview>> = PreviewExport(
    lazy {
        error(
            "[ComposePreviewLab] collectAllModulePreviews(scope = \"$scope\") was not replaced by the compiler plugin. " +
                "Apply the Compose Preview Lab Gradle plugin to this module and to every " +
                "dependency module whose previews you want to aggregate:\n" +
                "  // build.gradle.kts\n" +
                "  plugins {\n" +
                "      id(\"me.tbsten.compose.preview.lab\")\n" +
                "  }\n" +
                "Then re-run the build. Cross-module discovery requires Kotlin 2.3.21+. " +
                "See https://github.com/TBSten/compose-preview-lab for the full setup guide.",
        )
    },
)

/**
 * Removes duplicates from the aggregated preview list by [CollectedPreview.id], warning
 * via stdout when a duplicate is observed.
 *
 * Called from the IR generated by `collectAllModulePreviews()` to merge previews from this
 * module with previews discovered through the per-declaration hint mechanism
 * (`previewHint_<scope>(value: PreviewHintMarker_<hash>?): CollectedPreview` functions). When a
 * dependency module also uses `collectAllModulePreviews()` (i.e. it re-exports its own
 * dependencies), the same `CollectedPreview` may reach the aggregator through more than
 * one hint chain. Deduplicating by `id` guarantees a stable result regardless of how many
 * paths a preview travels through.
 *
 * **Cross-artifact same-FQN previews — silent edge case**: when two dependency modules
 * declare a `@Preview` with identical fully-qualified name (e.g. both expose
 * `com.example.SharedPreview()`), the FIR/IR generator produces a marker class with the
 * same `<canonical-key>` hash on both sides. The JVM classloader and the KLIB linker
 * each resolve the duplicate to a single symbol — the **other** preview's body is
 * silently lost. The runtime can detect this only by observing duplicate
 * `CollectedPreview.id` values reaching this aggregator, which is rare because the
 * same FQN collapse already eliminated one of them upstream. We surface those
 * duplicates via [warnDuplicatePreview], which routes per platform: JVM / Android /
 * iOS keep the existing stdout behavior so build / test logs continue to surface the
 * warning, while JS / Wasm JS write via `console.warn` so the warning lights up in
 * browser DevTools (yellow highlighting) and survives headless test runners that
 * filter `console.log`.
 *
 * The compiler plugin also attempts a best-effort compile-time warning when its symbol
 * scan returns duplicate hints (in `HintDiscovery.discoverHints`, which lives in the
 * `:compiler-plugin` module and is therefore not Dokka-linkable from here), but the
 * same upstream collapse means that warning rarely fires; the runtime signal here is
 * the durable detection path.
 *
 * Resolution: **rename the underlying `@Preview` function** in one of the colliding
 * artifacts so its FQN no longer matches. `@ComposePreviewLabOption(id = "...")`
 * override does NOT help on any platform: the FQN-based hint generator name collides at
 * IR time before the override id is read on JVM / Android, and on KLIB-based targets
 * the linker further dedups by IdSignature. Renaming the function is the only fix that
 * works on every CMP target.
 *
 * **Web visibility caveat**: even with `console.warn`, browser console output is not
 * visible to a user who has not opened DevTools. If a `@Preview` "disappears" on a JS /
 * Wasm JS deploy target, opening DevTools is the first triage step — the warning fires
 * once per app launch, on first read of the `collect[All]ModulePreviews()` property.
 *
 * Internal API — meant only as a callsite for the compiler plugin.
 */
@InternalComposePreviewLabApi
public fun distinctPreviewsById(previews: List<CollectedPreview>): List<CollectedPreview> {
    // Single-pass dedup: build the distinct list while detecting duplicates so we don't
    // walk `previews` twice (the previous `groupBy` + `distinctBy` combination allocated
    // a full `Map<String, List<CollectedPreview>>` only to drop most of it). `dupCounts`
    // only stores ids that actually duplicated, so the common no-collision path keeps
    // its map at zero entries.
    val seen = HashSet<String>(previews.size)
    val distinct = ArrayList<CollectedPreview>(previews.size)
    val dupCounts = HashMap<String, Int>()
    for (preview in previews) {
        if (seen.add(preview.id)) {
            distinct += preview
        } else {
            // Already seen once. The first collision records 2 (the originally-emitted
            // entry + this duplicate); each subsequent collision adds 1.
            val existing = dupCounts[preview.id]
            dupCounts[preview.id] = if (existing == null) 2 else existing + 1
        }
    }
    // [warnDuplicatePreview] routes per platform — stdout on JVM / Android / iOS,
    // `console.warn` on JS / Wasm JS — so the warning is visible in the medium where
    // users most often look for runtime signals on each target. The warning fires
    // once per app launch even if the duplicated preview is never displayed, so users
    // see it during dev iteration.
    for ((id, count) in dupCounts) {
        warnDuplicatePreview(
            "[ComposePreviewLab] WARNING: $count `CollectedPreview` entries share " +
                "id=\"$id\". Likely cause: a `@Preview` with the same fully-qualified name " +
                "is declared in two or more dependency artifacts on the classpath, so their " +
                "marker / hint classes collide and only one survives — the other preview's " +
                "body is unreachable. Resolution: rename the underlying `@Preview` function " +
                "in one of the artifacts so the FQN no longer collides. " +
                "(`@ComposePreviewLabOption(id = \"...\")` override does NOT help on any " +
                "CMP target: the FQN-based hint generator name collides at IR time before " +
                "the override id is read on JVM / Android, and on KLIB-based targets the " +
                "linker further dedups by IdSignature. Renaming the function is the only " +
                "portable fix.)",
        )
    }
    return distinct
}

/**
 * Sequence-shaped variant of [distinctPreviewsById] used by the compiler plugin's
 * `collectAllModulePreviews()` IR rewrite. Walks [previews] once, yields each first
 * occurrence by [CollectedPreview.id] in encounter order, and on the final
 * `next() == null` step prints one stdout warning per id that collided two-or-more
 * times.
 *
 * The behavior matches [distinctPreviewsById] except the fold is lazy: an early-exit
 * consumer (`previews.firstOrNull { ... }`, `previews.take(2)`) only iterates the
 * prefix it needs, and dup detection runs only over that prefix. A consumer that
 * drains the sequence via `previews.toList()` sees the full warning set, identical
 * to the pre-laziness behavior — and uses the per-platform [warnDuplicatePreview]
 * routing so JS / Wasm JS surface the warning via `console.warn` rather than
 * `console.log`.
 *
 * Internal API — meant only as a callsite for the compiler plugin.
 */
@InternalComposePreviewLabApi
public fun distinctPreviewsByIdSequence(previews: Sequence<CollectedPreview>): Sequence<CollectedPreview> = sequence {
    val seen = HashSet<String>()
    val dupCounts = HashMap<String, Int>()
    for (preview in previews) {
        if (seen.add(preview.id)) {
            yield(preview)
        } else {
            val existing = dupCounts[preview.id]
            dupCounts[preview.id] = if (existing == null) 2 else existing + 1
        }
    }
    for ((id, count) in dupCounts) {
        warnDuplicatePreview(
            "[ComposePreviewLab] WARNING: $count `CollectedPreview` entries share " +
                "id=\"$id\". Likely cause: a `@Preview` with the same fully-qualified name " +
                "is declared in two or more dependency artifacts on the classpath, so their " +
                "marker / hint classes collide and only one survives — the other preview's " +
                "body is unreachable. Resolution: rename the underlying `@Preview` function " +
                "in one of the artifacts so the FQN no longer collides. " +
                "(`@ComposePreviewLabOption(id = \"...\")` override does NOT help on any " +
                "CMP target: the FQN-based hint generator name collides at IR time before " +
                "the override id is read on JVM / Android, and on KLIB-based targets the " +
                "linker further dedups by IdSignature. Renaming the function is the only " +
                "portable fix.)",
        )
    }
}

/**
 * Returns a [Sequence] that yields one [CollectedPreview] per element of [factories],
 * invoking each factory lambda the first time the corresponding element is requested.
 *
 * **Sample call (from compiler-plugin IR)**:
 * ```kotlin
 * lazyPreviewSequence(
 *     { CollectedPreview("id1", "Preview1", ..., content = { Preview1() }) },
 *     { CollectedPreview("id2", "Preview2", ..., content = { Preview2() }) },
 * )
 * ```
 *
 * **Result** (semantically): a `Sequence<CollectedPreview>` that, when iterated to
 * completion, returns both `CollectedPreview` instances in order. Each factory lambda
 * is invoked at most once per iteration; partial consumption (`take(1).toList()`) leaves
 * the second factory unevaluated. The returned sequence is multi-iterable: walking it
 * twice invokes each factory twice.
 *
 * The factory-lambda indirection is what enables the laziness — building a `List` of
 * `CollectedPreview` directly would force every `@Composable` lambda inside each
 * preview to be allocated up front.
 *
 * Internal API — meant only as a callsite for the compiler plugin.
 */
@InternalComposePreviewLabApi
public fun lazyPreviewSequence(vararg factories: () -> CollectedPreview,): Sequence<CollectedPreview> = Sequence {
    iterator {
        for (factory in factories) yield(factory())
    }
}
