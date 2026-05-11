# Logic: Marker Generation

> [日本語版](./marker-generation.ja.md)

The FIR logic that synthesizes, for every `@Preview`, a single **marker interface**
(`PreviewHintMarker_<sanitized_fqn>_<hash>`). It is used as the `value: <Marker>?` parameter type of the hint
function and serves to disambiguate KLIB IdSignatures.

## ★ Current state: fused with hint into one logic (`hintAndMarkerGeneration/`)

The original design planned to **split this into two logics**, one for hint and one for marker
(`hintGeneration/GeneratePreviewHintFir` + `markerGeneration/GeneratePreviewHintMarkerFir`), but PR #200
**merged them into a single logic**:

- Location: [`fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt)
- A single class overrides both **the class-side API** (`getTopLevelClassIds` / `generateTopLevelClassLikeDeclaration`)
  and **the callable-side API** (`getTopLevelCallableIds` / `generateFunctions`) of
  `FirDeclarationGenerationExtension`.

### Why we keep the fused form

Advantages of the unified form:

- Hints and markers are derived from the **same canonical key + hash** of the same `@Preview`, so they have a
  strong need to share the walk result (= `List<HintEntry>`).
- Splitting into two generators would require sharing the `HintEntry` cache indirectly via a session component
  (= `HintEntriesProvider`), raising the cost of keeping the state predictable.
- Even in a single class, the API surface is clearly partitioned by `generateTopLevelClassLikeDeclaration` vs
  `generateFunctions`, so hint-related and marker-related methods can be read as "two logics that physically
  share a file" thanks to KDoc and method names.

### Criteria for re-splitting

In the future, re-splitting into two logics should be considered if any of the following holds:

- Either side (hint or marker) needs **independent state** (a cache or lookup table).
- The total number of lines for hint-related plus marker-related methods exceeds 500 in a single file.
- One of the two logics has to behave differently for a **different KMP target / different Kotlin version**.

If we do re-split, the path candidates per the original plan are:

- `fir/hintGeneration/GeneratePreviewHintFir.kt` (callable side)
- `fir/markerGeneration/GeneratePreviewHintMarkerFir.kt` (class side)
- The shared walk cache can be absorbed by the existing `HintEntriesProvider` (no new component required).

> Note: even today,
> [`fir/hintGeneration/DeprecationHidden.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/hintGeneration/DeprecationHidden.kt)
> still lives under the `hintGeneration/` directory. This is a helper that both marker and hint call, but it
> was originally introduced as a hint-specific `@Deprecated(HIDDEN)` injection — its location is a historical
> artifact, independent of the fused-vs-split decision.

---

## Input / Generated

### Input

```kotlin
package com.example.app
@Preview fun MyButtonPreview() { ... }
```

### Generated (semantically equivalent Kotlin)

```kotlin
// synthetic file in package me.tbsten.compose.preview.lab.hints
package me.tbsten.compose.preview.lab.hints

@kotlin.Deprecated("...", level = HIDDEN)
@me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
@me.tbsten.compose.preview.lab.SyntheticPreviewHint
public interface PreviewHintMarker_com_example_app_MyButtonPreview_<hash>
```

- Modality is `ABSTRACT` (the default for `interface`). It is not `FINAL` (Konan rejects `FINAL` on interfaces).
- It is not `sealed` (`@Deprecated(HIDDEN)` already closes off source-level reachability, making it redundant).

---

## Related classes and their roles

| Component | Location | Role |
|---|---|---|
| `PreviewHintFirGenerator.generateTopLevelClassLikeDeclaration` | `fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt` | Synthesizes the marker interface |
| `PreviewHintFirGenerator.getTopLevelClassIds` | Same | Builds the ClassId set from the walk result (= `HintEntry.markerShortName`) |
| `HintEntriesProvider` + `HintEntry.markerShortName` | `feature/previewCollection/HintEntriesProvider.kt` | SSoT of the walk result |
| `MarkerInterfaceName.kt::buildMarkerShortName` | `feature/previewCollection/MarkerInterfaceName.kt` | Assembles `PreviewHintMarker_<sanitized_fqn>_<hash>` |
| `MarkerInterfaceName.kt::isMarkerShortName` / `extractHashFromMarkerShortName` | Same | SSoT for the IR side that recovers the hash from a marker short name |
| `DeprecationHidden.kt::markAsDeprecatedHidden(FirClassLikeDeclaration)` | `fir/hintGeneration/DeprecationHidden.kt` | Attaches `@Deprecated(HIDDEN)` to the marker |
| `AttachInternalApi.kt::markAsInternalSyntheticHint` | `fir/AttachInternalApi.kt` | Attaches `@InternalComposePreviewLabApi` + `@SyntheticPreviewHint` |
| `PreviewKeys.PreviewLabHintMarkerInterface` | `feature/previewCollection/PreviewKeys.kt` | The marker's `GeneratedDeclarationKey` |
| `HINT_PACKAGE` | `feature/previewCollection/HintPackage.kt` | The `FqName` SSoT for `me.tbsten.compose.preview.lab.hints` |

---

## Design rationale

### Why `interface` (rather than `class` / `object`)

- **Avoiding Compose Compiler's `$stableprop` synthesis**: With `class` / `object`, the Compose Compiler
  synthesizes a `$stableprop` symbol on JS / Wasm IC and causes a collision.
- **Konan compatibility**: Konan rejects `FINAL` modality on interfaces with `Expected a class, found interface`,
  so `interface` + `ABSTRACT` modality is the only form that is safe across targets.

### Why not `sealed`

Once `@Deprecated(level = HIDDEN)` is attached, the marker class is no longer present in the consumer's name
resolution scope. Even if a user wrote `class MyMarker : PreviewHintMarker_<sanitized_fqn>_<hash>`, compilation
would fail, so there is no need to additionally close the hierarchy structurally with `sealed` (= it would be
redundant). The executable proof is the test `PreviewHintMarkerSealOrHiddenTest`.

### Why the hash portion of the marker short name is **fixed-width, 8 characters**

So that [`extractHashFromMarkerShortName`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/MarkerInterfaceName.kt)
can recover the hash via a fixed-length tail slice. The `sanitized(sourceFqn)` part is lossy (`.` and `_` both
collapse to `_`) and cannot be parsed reliably, but if the hash is placed at the end with a fixed length, no parsing is needed.

See [hint-and-marker-generation.md](./hint-and-marker-generation.md) (Part 1 — Naming) for details.

### Why the marker's visibility is `public`

To support cross-module discovery (i.e. IR `DiscoverHints`'s `referenceFunctions`), the marker — which appears
as the parameter type of the hint function — must be **public on the classpath**. If it were merely an internal
hidden implementation, then **the hint function's signature could not be resolved at all** from a dependency
module (the visibility of a parameter type restricts the visibility of the caller).

That said, `@InternalComposePreviewLabApi` + `@SyntheticPreviewHint` ensure that:

- It is excluded from the BCV (Binary Compatibility Validator) baseline.
- It disappears from IDE completion / `import` candidates (`@Deprecated(HIDDEN)`).
- On the IR side, `DiscoverHints` uses `@SyntheticPreviewHint` as positive proof, so even if a user writes a
  class with the same name by hand it will not be falsely detected.

Together, these realize "binary visibility is public, but invisible as a user API surface".

---

## Related documents

- [hint-and-marker-generation.md](./hint-and-marker-generation.md) — Hint stub function generation (handled by the same generator) and naming SSoT including `MarkerInterfaceName`.
- [collect-previews-replacement.md](./collect-previews-replacement.md) — How the IR side recovers the hash from the marker short name to look up the preview.
