# Hint / Marker Naming (Single Source of Truth)

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

---

## Overall picture

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

---

## The three SSoT components

### 1. `HintCanonicalKey` ([HintCanonicalKey.kt](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/HintCanonicalKey.kt))

- `buildPreviewHintCanonicalKey(sourceFqn, parameterTypeFqns)` —
  An environment-independent key of the form `"<sourceFqn>(<paramFqn1>,<paramFqn2>,...)"`.
  `projectRootPath` / `moduleName` are deliberately excluded (to keep both incremental compilation and reproducible builds working).
- `computeHintHash(canonicalKey)` — Takes the first 8 bytes of SHA-256, base-36 encodes them, then keeps the last 8 characters (`HashLength = 8`).
  About 41 effective bits. Acts as a disambiguator that distinguishes overloads sharing the same FQN + signature.

### 2. `HintFunName` ([HintFunName.kt](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/HintFunName.kt))

- `PreviewHintFunctionPrefix = "previewHint_"` (`const val`)
- `hintFunctionCallableId(scope: String)` →
  `CallableId(HINT_PACKAGE, Name.identifier("previewHint_$scope"))`
- `isHintFunctionName(name: Name)` — Structural check (prefix only).
  Regex validation on the scope portion is performed in combination with `SCOPE_VALIDATION_REGEX`
  (`scopeValidation/ScopeValidationRegex.kt`).

By **embedding the scope into the function name**, the IR discovery side can recover every hint for a given
scope with a **single callable lookup**: `referenceFunctions(CallableId(HINT_PACKAGE, "previewHint_<scope>"))`.
No per-hint annotation inspection is required.

### 3. `MarkerInterfaceName` ([MarkerInterfaceName.kt](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/MarkerInterfaceName.kt))

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

---

## Reference matrix (which class references which symbol)

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

---

## Why the canonical key does not include scope

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

---

## Why the hash is a SHA-256 truncated to 8 base-36 chars

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

---

## Why `sanitized(sourceFqn)` accepts information loss

The `sanitized(sourceFqn)` step in `buildMarkerShortName` replaces every `[^A-Za-z0-9_]` with `_`, so
`com.example.A` and `com_example_A` collapse to the same sanitized result. That is acceptable because:

- This part is **a debugging-friendly identifier**; uniqueness is owned by the **hash portion**.
- It is a trade-off between human readability of KLIB IdSignatures / stack traces / IDE navigation and round-tripability.
- To handle backtick identifiers (e.g. `fun \`my preview\`()`) at the Kotlin source level, a naive `.` → `_`
  substitution is not sufficient.

The fact that `extractHashFromMarkerShortName` is a fixed-length tail slice is a direct consequence of this
design (there is no need to parse the sanitized portion).

---

## Related documents

- [hint-generation.md](./hint-generation.md) — Logic-level details of hint function generation.
- [marker-generation.md](./marker-generation.md) — Logic-level details of marker interface generation.
- [collect-previews-replacement.md](./collect-previews-replacement.md) — IR-side hint discovery, body filling, and hash-map construction.
- [error-flow.md](./error-flow.md) — Role separation for errors such as hash collisions.
