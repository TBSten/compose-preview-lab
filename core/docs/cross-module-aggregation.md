# Cross-module Preview Aggregation

> [日本語版](./cross-module-aggregation.ja.md)

Reference document for the cross-module discovery semantics of
[`collectAllModulePreviews()`](../src/commonMain/kotlin/me/tbsten/compose/preview/lab/CollectModulePreviews.kt)
and the duplicate-detection runtime path that backs it. The KDoc on each function summarises
the API contract; this document is the single source of truth for the Kotlin-version matrix,
discovery mechanism, and the duplicate-detection diagnostic flow.

## Kotlin-version matrix

`collectAllModulePreviews()` works on different Kotlin versions depending on the target:

- **JVM / Android**: works from Kotlin **2.3.20** (the FIR per-declaration hint generator
  stabilised there; JVM bytecode dependency resolution has no incremental-compile
  complications).
- **JS / Wasm JS / iOS / Native**: works from Kotlin **2.3.21** (additionally requires the
  KT-82395 KLIB IC fix that landed in 2.3.21).

On older Kotlin the plugin's IR pass detects the `val x by collectAllModulePreviews()`
delegated-property pattern and reports a compile-time error with a clear upgrade-or-downgrade
message. Direct calls outside a property delegate (which are not the supported usage) compile
and fall back to the `IllegalStateException` thrown by the placeholder body.
`collectModulePreviews()` (single-module) continues to work on every Kotlin version.

## Discovery mechanism

The compiler plugin emits a per-`@Preview` hint
(`previewHint_<scope>(value: PreviewHintMarker_<hash>?): CollectedPreview` in the
`me.tbsten.compose.preview.lab.hints` package). The consumer side discovers these via
`IrPluginContext.referenceFunctions`, which works equally for JVM bytecode and KLIB modules.

The argument to `collectAllModulePreviews(scope = ...)` must reach the compiler plugin's IR
pass as a compile-time string constant — an inline string literal or a `const val` reference
both work, because both end up as an `IrConst<String>` before the pass runs. Non-`const`
vals, string concatenations, and other expressions that produce an `IrCall` /
`IrStringConcatenation` are reported as compile errors. The resolved value must also match
`[A-Za-z0-9_]+` because the plugin embeds it into the synthetic hint function name.

## Mixed-classpath caveat

The aggregation only sees dependency-module previews if the dependency artifact itself was
compiled with the Compose Preview Lab plugin under **Kotlin 2.3.20+**, so its bytecode /
KLIB carries the synthetic `previewHint_<scope>` overloads that the consumer side walks via
`referenceFunctions`. A dependency built with an older compiler — or built without the
plugin — emits no marker / hint pair and is silently absent from the aggregated result.

Compose Preview Lab cannot detect this at compile time; if a known-existing dep `@Preview`
does not show up downstream, **check the dep's Kotlin version + plugin application first**.

## Per-call scope substitution

The scope is **strictly per-module**: a library that pins its own previews to
`defaultCollectScope = "acme_ui"` registers them under `acme_ui`, but a downstream consumer
app's `collectAllModulePreviews()` (= the sentinel, substituted with the *consumer's* DSL
value) will **NOT** see them unless the consumer explicitly asks
`collectAllModulePreviews(scope = "acme_ui")`. This isolation is the primary reason the DSL
exists. See [`annotation/docs/collect-scopes.md`](../../annotation/docs/collect-scopes.md)
for the full scope-resolution semantics.

## Duplicate detection

Both `distinctPreviewsById` (eager `List` variant) and `distinctPreviewsByIdSequence` (lazy
`Sequence` variant) deduplicate the aggregated list by `CollectedPreview.id`. When a
dependency module also uses `collectAllModulePreviews()` (= it re-exports its own
dependencies), the same `CollectedPreview` may reach the aggregator through more than one
hint chain. Deduplicating by `id` guarantees a stable result regardless of how many paths a
preview travels through.

### Cross-artifact same-FQN previews — silent edge case

When two dependency modules declare a `@Preview` with identical fully-qualified name (e.g.
both expose `com.example.SharedPreview()`), the FIR/IR generator produces a marker class
with the same `<canonical-key>` hash on both sides. The JVM classloader and the KLIB linker
each resolve the duplicate to a single symbol — **the other preview's body is silently
lost**. The runtime can detect this only by observing duplicate `CollectedPreview.id` values
reaching this aggregator, which is rare because the same FQN collapse already eliminated one
of them upstream.

When duplicates are observed at runtime, `distinctPreviewsById` / `distinctPreviewsByIdSequence`
emit a warning per id via [`warnDuplicatePreview`](../src/commonMain/kotlin/me/tbsten/compose/preview/lab/WarnDuplicatePreview.kt),
which routes per platform:

- **JVM / Android / iOS**: stdout (`println`) — keeps the historical behaviour so build /
  test logs continue to surface the warning.
- **JS / Wasm JS**: `console.warn` so the warning lights up in browser DevTools (yellow
  highlighting) and survives headless test runners that filter `console.log`.

The compiler plugin also attempts a best-effort compile-time warning when its symbol scan
returns duplicate hints (in `HintDiscovery.discoverHints`, which lives in the
`:compiler-plugin` module and is not Dokka-linkable from here), but the same upstream
collapse means that warning rarely fires; the runtime signal is the durable detection path.

### Resolution — rename the function

**Rename the underlying `@Preview` function** in one of the colliding artifacts so its FQN
no longer matches. `@ComposePreviewLabOption(id = "...")` override does NOT help on any
platform: the FQN-based hint generator name collides at IR time before the override id is
read on JVM / Android, and on KLIB-based targets the linker further dedups by IdSignature.
Renaming the function is the only fix that works on every CMP target.

### Web visibility caveat

Even with `console.warn`, browser console output is not visible to a user who has not opened
DevTools. If a `@Preview` "disappears" on a JS / Wasm JS deploy target, **opening DevTools
is the first triage step** — the warning fires once per app launch, on first read of the
`collect[All]ModulePreviews()` property.

## Laziness — `PreviewExport` + `Sequence` semantics

`collect[All]ModulePreviews()` returns a property delegate of type
`ReadOnlyProperty<Any?, Sequence<CollectedPreview>>` backed by a [`PreviewExport`](../src/commonMain/kotlin/me/tbsten/compose/preview/lab/PreviewExport.kt).
The delegate wraps the underlying sequence in a [`Lazy`] so that the per-`@Preview` factory
lambdas the compiler plugin emits are not invoked until the property is first read; iterating
the sequence then constructs each `CollectedPreview` on demand.

Consumers that only need the first few entries (e.g. a search filter that returns 10 hits
out of thousands of `@Preview` declarations) avoid materialising the rest, dropping peak
memory usage on large preview corpora.

The factory-lambda indirection is what enables the laziness — building a `List` of
`CollectedPreview` directly would force every `@Composable` lambda inside each preview to be
allocated up front.
