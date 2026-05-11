package me.tbsten.compose.preview.lab

import kotlin.properties.ReadOnlyProperty

/**
 * Provides a delegate that collects `@Preview` functions from **this module only**.
 *
 * The Compose Preview Lab compiler plugin replaces the body at compile time so that the
 * returned delegate holds the actual preview list. If the compiler plugin is not applied,
 * accessing the value throws an [IllegalStateException].
 *
 * The returned [PreviewExport] functions as a property delegate for
 * `Sequence<CollectedPreview>`. It wraps a [Lazy] so that `@Composable` lambdas inside
 * [CollectedPreview] are initialised on first access rather than at class-load time,
 * avoiding `ExceptionInInitializerError`. The same wrapper acts as a marker type that
 * downstream modules pick up automatically — see [collectAllModulePreviews].
 *
 * This stable no-arg overload delegates to the experimental scope-aware overload with the
 * default-scope sentinel, so consumers do not have to opt in unless they need explicit
 * scope filtering.
 *
 * ```kotlin
 * val myPreviews by collectModulePreviews()
 * ```
 *
 * @see collectAllModulePreviews
 */
@OptIn(InternalComposePreviewLabApi::class, ExperimentalComposePreviewLabApi::class)
public fun collectModulePreviews(): ReadOnlyProperty<Any?, Sequence<CollectedPreview>> =
    collectModulePreviews(scope = ComposePreviewLabOption.DefaultCollectScope)

/**
 * Scope-aware variant of [collectModulePreviews] that limits the result to previews whose
 * `@ComposePreviewLabOption(collectScopes = [...])` array contains [scope]. See
 * [annotation/docs/collect-scopes.md](https://github.com/TBSten/compose-preview-lab/blob/main/annotation/docs/collect-scopes.md)
 * for the full scope-resolution semantics; the value
 * `ComposePreviewLabOption.DefaultCollectScope` is a sentinel that the compiler plugin
 * substitutes with the module's `composePreviewLab.collectPreviews.defaultCollectScope`
 * Gradle DSL value at compile time.
 *
 * **Experimental** — the scope feature is still stabilising. Opt in with
 * `@OptIn(ExperimentalComposePreviewLabApi::class)` (or `@file:OptIn(...)`); the no-arg
 * [collectModulePreviews] overload remains stable.
 *
 * The argument must reach the compiler plugin's IR pass as a compile-time string constant
 * — an inline string literal or a `const val` reference both work. The resolved value must
 * also match `[A-Za-z0-9_]+` because the plugin embeds it into the synthetic hint function
 * name.
 *
 * ```kotlin
 * @file:OptIn(me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi::class)
 *
 * val buttonPreviews by collectModulePreviews(scope = "buttons")
 * ```
 *
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
 * holds a concatenation of this module's previews and the previews of every dependency
 * module on the classpath. Discovery is fully automatic — applying the Gradle plugin in a
 * dependency module is enough; **`collectModulePreviews()` does not need to be declared**
 * there. Modules that do declare it explicitly continue to work unchanged.
 *
 * Cross-module discovery requires Kotlin **2.3.20+** on JVM / Android and **2.3.21+** on
 * JS / Wasm JS / iOS / Native. See
 * [core/docs/cross-module-aggregation.md](https://github.com/TBSten/compose-preview-lab/blob/main/core/docs/cross-module-aggregation.md)
 * for the version matrix, the Mixed-classpath caveat (dependencies built without the plugin are
 * silently absent), and the per-platform warning routing for duplicate-id collisions.
 *
 * This stable no-arg overload delegates to the experimental scope-aware overload with the
 * default-scope sentinel, so consumers do not have to opt in unless they need explicit
 * scope filtering.
 *
 * ```kotlin
 * // app/src/commonMain/kotlin/Previews.kt
 * val appPreviews by collectAllModulePreviews()
 *
 * // uiLib/build.gradle.kts — applying the Gradle plugin is enough; no Previews.kt needed
 * plugins {
 *     id("me.tbsten.compose.preview.lab")
 * }
 * ```
 *
 * @see collectModulePreviews
 */
@OptIn(InternalComposePreviewLabApi::class, ExperimentalComposePreviewLabApi::class)
public fun collectAllModulePreviews(): ReadOnlyProperty<Any?, Sequence<CollectedPreview>> =
    collectAllModulePreviews(scope = ComposePreviewLabOption.DefaultCollectScope)

/**
 * Scope-aware variant of [collectAllModulePreviews] that limits the aggregated result to
 * previews whose `@ComposePreviewLabOption(collectScopes = [...])` array contains [scope].
 *
 * The substitution is **strictly per-module**: a library that pins its previews to
 * `defaultCollectScope = "acme_ui"` registers them under `acme_ui`, but a downstream
 * consumer app's `collectAllModulePreviews()` will **NOT** see them unless the consumer
 * explicitly asks `collectAllModulePreviews(scope = "acme_ui")`. See
 * [annotation/docs/collect-scopes.md](https://github.com/TBSten/compose-preview-lab/blob/main/annotation/docs/collect-scopes.md)
 * for the resolution semantics and
 * [core/docs/cross-module-aggregation.md](https://github.com/TBSten/compose-preview-lab/blob/main/core/docs/cross-module-aggregation.md)
 * for the full discovery contract.
 *
 * **Experimental** — opt in with `@OptIn(ExperimentalComposePreviewLabApi::class)`. The
 * no-arg [collectAllModulePreviews] overload remains stable. The argument must reach the
 * compiler plugin's IR pass as a compile-time string constant matching `[A-Za-z0-9_]+`.
 *
 * ```kotlin
 * @file:OptIn(me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi::class)
 *
 * val designPreviews by collectAllModulePreviews(scope = "design")
 * ```
 *
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
 * Deduplicates the aggregated preview list by [CollectedPreview.id], emitting a per-id
 * warning via [warnDuplicatePreview] when duplicates are observed. Called from the IR
 * generated by `collectAllModulePreviews()` to merge previews from this module with
 * previews discovered through the per-declaration hint mechanism.
 *
 * Duplicates indicate a cross-artifact same-FQN preview collision — the JVM classloader /
 * KLIB linker has already silently collapsed one of them, so the runtime signal here is
 * usually the only diagnostic the user sees. See
 * [core/docs/cross-module-aggregation.md](https://github.com/TBSten/compose-preview-lab/blob/main/core/docs/cross-module-aggregation.md)
 * for the resolution procedure (rename one of the colliding `@Preview` functions; the
 * `@ComposePreviewLabOption(id = "...")` override does NOT help).
 *
 * Internal API — meant only as a callsite for the compiler plugin.
 */
@InternalComposePreviewLabApi
public fun distinctPreviewsById(previews: List<CollectedPreview>): List<CollectedPreview> {
    // Single-pass dedup: build the distinct list while detecting duplicates so we don't
    // walk `previews` twice. `dupCounts` only stores ids that actually duplicated, so the
    // common no-collision path keeps its map at zero entries.
    val seen = HashSet<String>(previews.size)
    val distinct = ArrayList<CollectedPreview>(previews.size)
    val dupCounts = HashMap<String, Int>()
    for (preview in previews) {
        if (seen.add(preview.id)) {
            distinct += preview
        } else {
            // First collision records 2 (= originally-emitted + this duplicate); each
            // subsequent collision adds 1.
            val existing = dupCounts[preview.id]
            dupCounts[preview.id] = if (existing == null) 2 else existing + 1
        }
    }
    for ((id, count) in dupCounts) {
        warnDuplicatePreview(duplicateWarningMessage(id, count))
    }
    return distinct
}

/**
 * Sequence-shaped lazy variant of [distinctPreviewsById]. Same dedup + per-platform warning
 * semantics, but an early-exit consumer (`previews.firstOrNull { ... }`, `previews.take(2)`)
 * only iterates the prefix it needs; full-drain (`toList()`) emits the complete warning set.
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
        warnDuplicatePreview(duplicateWarningMessage(id, count))
    }
}

private fun duplicateWarningMessage(id: String, count: Int): String =
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
        "portable fix.)"

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
 * The returned sequence is multi-iterable: walking it twice invokes each factory twice.
 * Partial consumption (`take(1).toList()`) leaves later factories unevaluated.
 *
 * Internal API — meant only as a callsite for the compiler plugin.
 */
@InternalComposePreviewLabApi
public fun lazyPreviewSequence(vararg factories: () -> CollectedPreview,): Sequence<CollectedPreview> = Sequence {
    iterator {
        for (factory in factories) yield(factory())
    }
}
