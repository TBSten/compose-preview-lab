# Hint / Marker Naming + Hint Generation

> [日本語版](./hint-and-marker-generation.ja.md)

This document covers two tightly coupled topics in the `previewCollection` feature:

1. **Naming SSoT** — the cross-reference relationships among `HintCanonicalKey` + `HintFunName` +
   `MarkerInterfaceName`, and which class references which symbol.
2. **Hint generation logic** — the FIR logic that synthesizes the per-`@Preview` hint stub function
   (the marker-class side is documented separately in [marker-generation.md](./marker-generation.md), which
   also explains why hint and marker are fused into one generator).

The naming rules come first because every logic doc — including the hint-generation section below — assumes
you know them. After the naming section, read the generation section to see how those names are actually
materialized in FIR.

---

## Part 1 — Naming (Single Source of Truth)

In the `previewCollection` feature, every single `@Preview` produces **one marker interface** and
**as many hint functions as there are scopes**. The name formats of these declarations must agree at
**three sites** — the FIR generation side, the IR restore side, and the IR discovery side. If they ever
disagree, cross-module discovery silently breaks.

This document is the **Single Source of Truth** for the naming rules.
[`feature/previewCollection/HintCanonicalKey.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/HintCanonicalKey.kt) /
[`HintFunName.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/HintFunName.kt) /
[`MarkerInterfaceName.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/MarkerInterfaceName.kt) hold
the implementation-side SSoT (top-level `const` / pure functions). KDocs describe per-function Before/After,
while this document covers the **relationships across the three files** and **which class references which symbol**.

### Overall picture

```
@Preview fun com.example.MyButton()
         │
         ├─→ canonical key     = "com.example.MyButton()"                                   (HintCanonicalKey)
         ├─→ hash              = sha256(canonical key) → 8 chars base-36, e.g. "a3k9z2x1"   (HintCanonicalKey)
         ├─→ marker short name = "PreviewHintMarker_com_example_MyButton_a3k9z2x1"          (MarkerInterfaceName)
         └─→ hint fun name     = "previewHint_<scope>"  e.g. "previewHint_default"          (HintFunName)
```

The synthetic declarations that end up in the `me.tbsten.compose.preview.lab.hints` package look like:

```kotlin
package me.tbsten.compose.preview.lab.hints

@kotlin.Deprecated("...", level = HIDDEN)
@me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
@me.tbsten.compose.preview.lab.SyntheticPreviewHint
public interface PreviewHintMarker_com_example_MyButton_a3k9z2x1

@kotlin.Deprecated("...", level = HIDDEN)
@me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
@me.tbsten.compose.preview.lab.SyntheticPreviewHint
public fun previewHint_default(value: PreviewHintMarker_com_example_MyButton_a3k9z2x1?): CollectedPreview =
    error("Stub! Filled by IR.")
```

When `@ComposePreviewLabOption(collectScopes = ["design", "screenshot"])` is present, the result is
**1 marker × 2 hint overloads** (`previewHint_design` and `previewHint_screenshot`, each receiving the same marker type).

### The three SSoT components

#### 1. `HintCanonicalKey` ([HintCanonicalKey.kt](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/HintCanonicalKey.kt))

- `buildPreviewHintCanonicalKey(sourceFqn, parameterTypeFqns)` —
  An environment-independent key of the form `"<sourceFqn>(<paramFqn1>,<paramFqn2>,...)"`.
  `projectRootPath` / `moduleName` are deliberately excluded (to keep both incremental compilation and reproducible builds working).
- `computeHintHash(canonicalKey)` — Takes the first 8 bytes of SHA-256, base-36 encodes them, then keeps the last 8 characters (`HashLength = 8`).
  About 41 effective bits. Acts as a disambiguator that distinguishes overloads sharing the same FQN + signature.

#### 2. `HintFunName` ([HintFunName.kt](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/HintFunName.kt))

- `PreviewHintFunctionPrefix = "previewHint_"` (`const val`)
- `hintFunctionCallableId(scope: String)` →
  `CallableId(HINT_PACKAGE, Name.identifier("previewHint_$scope"))`
- `isHintFunctionName(name: Name)` — Structural check (prefix only).
  Regex validation on the scope portion is performed in combination with `SCOPE_VALIDATION_REGEX`
  (`scopeValidation/ScopeValidationRegex.kt`).

By **embedding the scope into the function name**, the IR discovery side can recover every hint for a given
scope with a **single callable lookup**: `referenceFunctions(CallableId(HINT_PACKAGE, "previewHint_<scope>"))`.
No per-hint annotation inspection is required.

#### 3. `MarkerInterfaceName` ([MarkerInterfaceName.kt](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/MarkerInterfaceName.kt))

- `PreviewHintMarkerPrefix = "PreviewHintMarker_"` (`const val`)
- `HashLength = 8` (`const val`)
- `buildMarkerShortName(sourceFqn, hash)` —
  `"PreviewHintMarker_" + sanitized(sourceFqn) + "_" + hash`.
  `sanitized` replaces every `[^A-Za-z0-9_]` with `_` (which also handles characters from backtick-quoted identifiers).
- `isMarkerShortName(shortName)` — Structural check (prefix only).
- `extractHashFromMarkerShortName(shortName)` — Takes the last `HashLength` characters.
  `sanitized(sourceFqn)` is lossy, but because the hash portion has a fixed length, a tail slice always recovers it.

**Roles of marker and hint**:
- The marker is **one per `@Preview`** (a token used to make the KLIB IdSignature unique).
- The hint function is **one overload per scope** (each takes the same marker as its parameter type).

KLIB IdSignatures are derived from `(name, paramTypes)`, so a different marker type yields a different IdSignature.
This is what lets multiple `@Preview`-originated hints with the same callable name `previewHint_<scope>` coexist
as an overload set on the same classpath.

### Reference matrix (which class references which symbol)

| Symbol | Defined in | FIR generation side | IR restore side | IR discovery side |
|---|---|---|---|---|
| `buildPreviewHintCanonicalKey` | `HintCanonicalKey.kt` | `HintEntriesProvider.computeHintEntries` | `BuildPreviewByHashMap.invoke` | — |
| `computeHintHash` | `HintCanonicalKey.kt` | `HintEntriesProvider.computeHintEntries` | `BuildPreviewByHashMap.invoke` | — |
| `PreviewHintFunctionPrefix` | `HintFunName.kt` | `PreviewHintFirGenerator` (= `hintAndMarkerGeneration/`) | — | Indirect, via `isHintFunctionName` |
| `hintFunctionCallableId` | `HintFunName.kt` | `PreviewHintFirGenerator` | — | `DiscoverHints` (`referenceFunctions(callableId)`) |
| `isHintFunctionName` | `HintFunName.kt` | — | — | (Utility for future defensive code) |
| `PreviewHintMarkerPrefix` | `MarkerInterfaceName.kt` | `PreviewHintFirGenerator` | — | `DiscoverHints` (short-name prefix check) |
| `HashLength` | `MarkerInterfaceName.kt` | `HintEntriesProvider` (indirect, via the output length of `computeHintHash`) | Via `extractHashFromMarkerShortName` | Via `extractHashFromMarkerShortName` |
| `buildMarkerShortName` | `MarkerInterfaceName.kt` | `HintEntriesProvider.computeHintEntries` | — | — |
| `isMarkerShortName` | `MarkerInterfaceName.kt` | — | `FillPreviewHintIrBody.visitSimpleFunction` (checks the parameter-type short name) | — (`DiscoverHints` calls `startsWith(PreviewHintMarkerPrefix)` directly rather than going through `isMarkerShortName`) |
| `extractHashFromMarkerShortName` | `MarkerInterfaceName.kt` | — | `FillPreviewHintIrBody.visitSimpleFunction` | — |

> **Cross-checking against Find Usages**: The table above is brittle against renames or moves in the implementation.
> Treat the IDE's Find Usages as the true SSoT, and update this table whenever you rename.

### Why the canonical key does not include scope

The scope is **embedded in the function name (`previewHint_<scope>`)** by design, and is intentionally **not part
of the canonical key** (which feeds the marker hash). Rationale:

- When the same `@Preview` function participates in two scopes via `collectScopes = ["design", "screenshot"]`,
  we want to emit **one marker** (the same hash) and only have the hint function be generated as two overloads.
  If scope were folded into the canonical key, the hash would change per scope and we would end up generating
  duplicate markers for each scope.
- The discovery side filters by scope using the single `referenceFunctions(previewHint_<scope>)` lookup, so the
  scope information **only needs to live in the function name** — it has no business being part of the hash.
- Keeping the canonical key restricted to "FQN + parameter types" means the IR side, when recomputing the same
  key, does not need to be passed the scope as context (the IR restore side runs purely on `marker hash → preview` lookups).

### Why the hash is a SHA-256 truncated to 8 base-36 chars

- **8 base-36 chars** ≈ `36^8 ≈ 2.8e12` distinct values → about 41 bits. The probability of a hash collision
  among 1000 previews is on the order of `10^-7`. The IR side catches such collisions in `BuildPreviewByHashMap` +
  `HashMapWithCollisionDetection` and raises `HintHashCollisionError` (a structured error). See
  [error-flow.md](./error-flow.md) for details.
- **base-36 (lowercase alphanumerics)** is chosen because the character set is valid as a Kotlin identifier and
  also safe even on case-insensitive filesystems (e.g. macOS HFS+) should the value end up in a file name.
- **Truncating to 8** is a deliberate trade-off: the hash is embedded in the marker class short name and in the
  hint function's parameter type short name, so a full SHA-256 (64 hex chars) would be excessive both from a
  human-readability standpoint and a KLIB IdSignature length standpoint. 8 is the shortest length we can use
  while practically avoiding collisions at the 1k-preview scale.

### Why `sanitized(sourceFqn)` accepts information loss

The `sanitized(sourceFqn)` step in `buildMarkerShortName` replaces every `[^A-Za-z0-9_]` with `_`, so
`com.example.A` and `com_example_A` collapse to the same sanitized result. That is acceptable because:

- This part is **a debugging-friendly identifier**; uniqueness is owned by the **hash portion**.
- It is a trade-off between human readability of KLIB IdSignatures / stack traces / IDE navigation and round-tripability.
- To handle backtick identifiers (e.g. `fun \`my preview\`()`) at the Kotlin source level, a naive `.` → `_`
  substitution is not sufficient.

The fact that `extractHashFromMarkerShortName` is a fixed-length tail slice is a direct consequence of this
design (there is no need to parse the sanitized portion).

---

## Part 2 — Hint Generation (FIR logic)

The FIR logic that synthesizes, for every `@Preview`, the **hint stub function** used by cross-module discovery.
As of 2026-05 this is **fused with marker interface generation into a single logic**, and a single
`PreviewHintFirGenerator` (= [`fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt))
handles both API surfaces — callables (hint) and classes (marker). See [marker-generation.md](./marker-generation.md)
for the marker side.

This section focuses on the **callable-side API for the hint stubs**. The marker class-side API is documented
separately in [marker-generation.md](./marker-generation.md), which also explains why we keep the merged form.

### Input / Generated

#### Input

```kotlin
package com.example.app
@Preview fun MyButtonPreview() { ... }
```

If `@ComposePreviewLabOption(collectScopes = [...])` is present, one hint is emitted per scope; otherwise a
single hint is emitted with `["default"]`.

#### Generated (semantically equivalent Kotlin)

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

### Related classes and their roles

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

### Design rationale

#### Why "**1 marker × N hint overloads**"

For a multi-scope preview such as `@ComposePreviewLabOption(collectScopes = ["design", "screenshot"])`:

- The marker is **one** (`PreviewHintMarker_<sanitized_fqn>_<hash>`) — because the KLIB IdSignature is derived from
  `(name, paramTypes)`, emitting multiple markers from the same `@Preview` would be redundant.
- The hint functions are **overloaded per scope** (`previewHint_design`, `previewHint_screenshot`) — each takes
  the same marker type.

With this layout the consumer side (IR `DiscoverHints`) can retrieve every hint for `design` scope in a **single
lookup** via `referenceFunctions(CallableId(HINT_PACKAGE, "previewHint_design"))` (hints for other scopes have a
different name and are excluded at lookup time).

This is the same naming layout established in Part 1; the rationale on the canonical-key side lives in
"Why the canonical key does not include scope" above.

#### Why `HintEntriesProvider` is factored out as its own `FirExtensionSessionComponent`

`PreviewHintFirGenerator` itself has two callbacks, `getTopLevelClassIds()` (markers) and
`getTopLevelCallableIds()` (hints), so walking the `@Preview` symbols twice does not happen in the current
unified form. Even so, in case we ever re-split hint and marker into two logics, we cache the walk result as
a session-scoped lazy so the two generators can **share it without losing the SSoT property**. `HintEntriesProvider`
is registered as a `FirExtensionSessionComponent` by
[`PreviewLabFirExtensionRegistrar`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/PreviewLabFirExtensionRegistrar.kt)
and accessed as `session.hintEntriesProvider.hintEntries`.

#### Why predicate registration stays on the `PreviewHintFirGenerator` side

`FirDeclarationGenerationExtension.registerPredicates` is the canonical entrypoint for the predicate registry,
and `FirExtensionSessionComponent` (= `HintEntriesProvider`) cannot register predicates. The walk itself happens
inside the provider, but registering the `@Preview` annotation FQN that drives the walk has to stay on the
generator side — a Kotlin compiler API constraint.

#### Why a lazy walk

Eagerly evaluating `predicateBasedProvider.getSymbolsByPredicate(...)` from the `HintEntriesProvider` constructor
provokes a frontend resolution cycle on Kotlin 2.3.21. Using `by lazy { computeHintEntries() }` defers evaluation
until the first time the generator's `getTopLevelClassIds` / `getTopLevelCallableIds` callback (= the predicate
provider's safe entry point) touches it.

#### Why we attach `@Deprecated(level=HIDDEN)`

Hints and markers are plugin-internal synthetic declarations; users are not expected to reference them at the
source level. Setting `level = HIDDEN` removes them from IDE completion and `import` candidates, which closes
off the namespace-squatting path that would otherwise let them be referenced by mistake. The reason we also
call `replaceDeprecationsProvider` is documented in the KDoc of
`DeprecationHidden.kt::markAsDeprecatedHidden`.

#### Why we also attach `@SyntheticPreviewHint`

`@Deprecated(HIDDEN)` only closes off source-level reachability; it is not a binary-level filter.
On the discovery side ([`DiscoverHints`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/DiscoverHints.kt)),
when walking `previewHint_<scope>` callables on the classpath we need a positive proof that the callable was
**emitted by the plugin**, and `@SyntheticPreviewHint` provides exactly that. If a user manually writes a
`previewHint_default`, it will lack this annotation and discovery will reject it (= namespace-squatting countermeasure).

### Handling of `ignore = true`

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
- [collect-previews-replacement.md](./collect-previews-replacement.md) — IR-side hint discovery, body filling, and hash-map construction.
- [scope-validation.md](./scope-validation.md) — Validation of `collectScopes` values.
- [error-flow.md](./error-flow.md) — Role separation for errors such as `HintHashCollisionError`.
