# Logic: Hint Generation

The FIR logic that synthesizes, for every `@Preview`, the **hint stub function** used by cross-module discovery.
As of 2026-05 this is **fused with marker interface generation into a single logic**, and a single
`PreviewHintFirGenerator` (= [`fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt))
handles both API surfaces — callables (hint) and classes (marker). See [marker-generation.md](./marker-generation.md)
for the marker side.

This document focuses on the **callable-side API for the hint stubs**. The marker class-side API is documented
separately in [marker-generation.md](./marker-generation.md), which also explains why we keep the merged form.

---

## Input / Generated

### Input

```kotlin
package com.example.app
@Preview fun MyButtonPreview() { ... }
```

If `@ComposePreviewLabOption(collectScopes = [...])` is present, one hint is emitted per scope; otherwise a
single hint is emitted with `["default"]`.

### Generated (semantically equivalent Kotlin)

```kotlin
// synthetic file in package me.tbsten.compose.preview.lab.hints
package me.tbsten.compose.preview.lab.hints

@kotlin.Deprecated("...", level = HIDDEN)
@me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
@me.tbsten.compose.preview.lab.SyntheticPreviewHint
public fun previewHint_default(value: PreviewHintMarker_com_example_app_MyButtonPreview_<hash>?): CollectedPreview =
    error("Stub! Filled by IR.")
```

When the scope `"design"` is also attached, an overload `previewHint_design` is added (taking the same marker type).

**The body is not emitted at FIR phase** — it remains an `error("Stub!")`-style placeholder through FIR, and
the IR-phase [`FillPreviewHintIrBody`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/FillPreviewHintIrBody.kt)
later fills in `return CollectedPreview(...)`.

---

## Related classes and their roles

| Component | Location | Role |
|---|---|---|
| `PreviewHintFirGenerator` | `fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt` | `FirDeclarationGenerationExtension` implementation. Owns both the hint and marker API surfaces |
| `HintEntriesProvider` + `HintEntry` | `feature/previewCollection/HintEntriesProvider.kt` | A session-scoped lazy that walks `@Preview` functions and produces `List<HintEntry>`. Shared by the hint and marker generators |
| `parameterTypeFqns()` (extension on `FirNamedFunctionSymbol`) | `feature/previewCollection/ParameterTypeFqns.kt` | Extracts the list of parameter-type FQNs that feed the hint canonical key (FIR side; the IR side has its own implementation) |
| `isIgnoredByComposePreviewLabOption` / `resolveCollectScopes` (private) | inside `HintEntriesProvider` | Reads the annotation arguments of `@ComposePreviewLabOption(ignore=true, collectScopes=[...])` |
| `DeprecationHidden.kt::markAsDeprecatedHidden` | `fir/hintGeneration/DeprecationHidden.kt` | Attaches `@Deprecated(level=HIDDEN)` to the hint function and invalidates the deprecation cache |
| `AttachInternalApi.kt::markAsInternalSyntheticHint` | `fir/AttachInternalApi.kt` | Attaches `@InternalComposePreviewLabApi` + `@SyntheticPreviewHint` (lives directly under feature/fir/ because it is used by both hint and marker) |
| `PreviewKeys.PreviewLabHint` | `feature/previewCollection/PreviewKeys.kt` | The `GeneratedDeclarationKey` for hint stubs. The IR side's `FillPreviewHintIrBody` uses it for origin checks |
| `COLLECTED_PREVIEW_CLASS_ID` | `feature/previewCollection/CollectedPreviewClassId.kt` | The ClassId of the hint function's return type `me.tbsten.compose.preview.lab.CollectedPreview` |

---

## Design rationale

### Why "**1 marker × N hint overloads**"

For a multi-scope preview such as `@ComposePreviewLabOption(collectScopes = ["design", "screenshot"])`:

- The marker is **one** (`PreviewHintMarker_<sanitized_fqn>_<hash>`) — because the KLIB IdSignature is derived from
  `(name, paramTypes)`, emitting multiple markers from the same `@Preview` would be redundant.
- The hint functions are **overloaded per scope** (`previewHint_design`, `previewHint_screenshot`) — each takes
  the same marker type.

With this layout the consumer side (IR `DiscoverHints`) can retrieve every hint for `design` scope in a **single
lookup** via `referenceFunctions(CallableId(HINT_PACKAGE, "previewHint_design"))` (hints for other scopes have a
different name and are excluded at lookup time).

See [hint-naming.md](./hint-naming.md) for details.

### Why `HintEntriesProvider` is factored out as its own `FirExtensionSessionComponent`

`PreviewHintFirGenerator` itself has two callbacks, `getTopLevelClassIds()` (markers) and
`getTopLevelCallableIds()` (hints), so walking the `@Preview` symbols twice does not happen in the current
unified form. Even so, in case we ever re-split hint and marker into two logics, we cache the walk result as
a session-scoped lazy so the two generators can **share it without losing the SSoT property**. `HintEntriesProvider`
is registered as a `FirExtensionSessionComponent` by
[`PreviewLabFirExtensionRegistrar`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/PreviewLabFirExtensionRegistrar.kt)
and accessed as `session.hintEntriesProvider.hintEntries`.

### Why predicate registration stays on the `PreviewHintFirGenerator` side

`FirDeclarationGenerationExtension.registerPredicates` is the canonical entrypoint for the predicate registry,
and `FirExtensionSessionComponent` (= `HintEntriesProvider`) cannot register predicates. The walk itself happens
inside the provider, but registering the `@Preview` annotation FQN that drives the walk has to stay on the
generator side — a Kotlin compiler API constraint.

### Why a lazy walk

Eagerly evaluating `predicateBasedProvider.getSymbolsByPredicate(...)` from the `HintEntriesProvider` constructor
provokes a frontend resolution cycle on Kotlin 2.3.21. Using `by lazy { computeHintEntries() }` defers evaluation
until the first time the generator's `getTopLevelClassIds` / `getTopLevelCallableIds` callback (= the predicate
provider's safe entry point) touches it.

### Why we attach `@Deprecated(level=HIDDEN)`

Hints and markers are plugin-internal synthetic declarations; users are not expected to reference them at the
source level. Setting `level = HIDDEN` removes them from IDE completion and `import` candidates, which closes
off the namespace-squatting path that would otherwise let them be referenced by mistake. The reason we also
call `replaceDeprecationsProvider` is documented in the KDoc of
`DeprecationHidden.kt::markAsDeprecatedHidden`.

### Why we also attach `@SyntheticPreviewHint`

`@Deprecated(HIDDEN)` only closes off source-level reachability; it is not a binary-level filter.
On the discovery side ([`DiscoverHints`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/DiscoverHints.kt)),
when walking `previewHint_<scope>` callables on the classpath we need a positive proof that the callable was
**emitted by the plugin**, and `@SyntheticPreviewHint` provides exactly that. If a user manually writes a
`previewHint_default`, it will lack this annotation and discovery will reject it (= namespace-squatting countermeasure).

---

## Handling of `ignore = true`

A `@Preview` annotated with `@ComposePreviewLabOption(ignore = true)` **skips hint emission entirely**.
Inside `HintEntriesProvider.computeHintEntries` we apply `filterNot { it.isIgnoredByComposePreviewLabOption() }`.

For an ignored preview:

- Neither a hint nor a marker is emitted.
- Cross-module discovery (`referenceFunctions(previewHint_<scope>)`) cannot find it.
- It is also absent from the hash map built by IR-side
  [`BuildPreviewByHashMap`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/BuildPreviewByHashMap.kt)
  (the consumer side mirrors the filter).

This also eliminates the risk that an ignored preview's hash collides under truncated-SHA-256 with a real
preview's hash and falsely raises `HintHashCollisionError`.

---

## Related documents

- [marker-generation.md](./marker-generation.md) — Logic-level details of the marker interface API plus the rationale for keeping hint/marker fused.
- [hint-naming.md](./hint-naming.md) — Naming SSoT (the cross-reference relationships among the three files).
- [collect-previews-replacement.md](./collect-previews-replacement.md) — IR-side hint body filling and cross-module discovery.
- [scope-validation.md](./scope-validation.md) — Validation of `collectScopes` values.
- [error-flow.md](./error-flow.md) — Role separation for errors such as `HintHashCollisionError`.
