# Feature: previewCollection

> [日本語版](./README.ja.md)

The feature responsible for **collecting** `@Preview` functions, replacing `collect[All]ModulePreviews()`, and discovering hints across modules.

This directory is the **entrypoint for the feature's design documents**, and each logic-level topic lives in a dedicated file.
The overview of the directory structure itself is owned exclusively by [`compiler-plugin/README.md`](../../README.md) (SSoT).
This README focuses on the **list of constituent logics** in the feature and on **links into each detailed topic**.

---

## Feature Overview

User-side code:

```kotlin
@Preview fun MyButton() { ... }
val myPreviews by collectModulePreviews()        // current module only
val allPreviews by collectAllModulePreviews()    // current module + dependency modules
```

The plugin makes this look as if it "collects all `@Preview` functions and injects them into `myPreviews` / `allPreviews`",
implementing that illusion in two phases: FIR phase + IR phase.

```
FIR phase
├── scopeValidation/              — collectScopes / scope-argument literal / regex validation (IDE red-squiggly)
└── hintAndMarkerGeneration/      — synthesizes a marker interface + previewHint_<scope> stub for every @Preview

IR phase
└── collectPreviewsReplacement/   — rewrites collect[All]ModulePreviews(), discovers dependency hints, fills hint bodies
    └── buildPreviewSequence/     — builds the lazyPreviewSequence + PreviewExport IR (sub-logic)
```

---

## How it works

Each logic runs at a specific compiler phase **in a specific module role**. The same compiler plugin code is loaded
into every module that depends on it, but the **module role** (= a module that *contains* `@Preview` functions vs.
a module that *calls* `collect[All]ModulePreviews()`) determines which logic actually emits output. The two roles
overlap when a single module both defines `@Preview` and calls `collectModulePreviews()` for itself.

Roles used below:

- **Preview-defining module** (= upstream / library module): a module that declares `@Preview` functions. Hints
  and markers are *generated* here so dependent modules can later discover them.
- **Preview-collecting module** (= downstream / app module): a module that calls `collectModulePreviews()` or
  `collectAllModulePreviews()`. Hints from this module and its dependencies are *discovered* and *materialized*
  into `CollectedPreview` instances here.

### Phase 1 — FIR (frontend) phase

Runs in **both** the preview-defining module and the preview-collecting module. The plugin cannot know at this
point which role the module plays, so all FIR extensions are installed everywhere and become no-ops on modules
that have nothing to contribute.

1. **scopeValidation** — `CollectScopeAnnotationChecker` + `CollectScopeCallChecker` validate the string literals
   on `@ComposePreviewLabOption(collectScopes = [...])` and `collect[All]ModulePreviews(scope = ...)`. Reports
   IDE red-squigglies via FIR diagnostics. **Fires in either role** depending on which construct is present
   (annotation is typical for the preview-defining module, the call is typical for the collecting module).
   Details: [scope-validation.md](./scope-validation.md).
2. **hintAndMarkerGeneration (FIR side)** — `PreviewHintFirGenerator` walks every `@Preview` in the current
   module, synthesizes one marker interface plus per-scope `previewHint_<scope>(...)` function declarations,
   and registers them. **Fires only in the preview-defining module** (modules with no `@Preview` produce
   nothing). Details: [hint-and-marker-generation.md](./hint-and-marker-generation.md) (Part 2 — Hint Generation, Part 3 — Marker Generation).
3. **transformPrivatePreviewToInternal** — A separate feature (see
   [`../transformPrivatePreviewToInternal/README.md`](../transformPrivatePreviewToInternal/README.md)) but
   sequenced before IR. Promotes `@Preview private fun` to `internal` so that the IR phase can legally emit
   references to them from synthesized hint bodies. **Fires only in the preview-defining module**.

### Phase 2 — IR (backend) phase

Also runs in both roles, but the heavy IR rewrites are split cleanly.

4. **hintAndMarkerGeneration (IR side)** — `FillPreviewHintIrBody` fills in the bodies of the hint-stub functions
   that the FIR phase declared, threading the actual `@Preview` callable reference + `CollectedPreview` builder
   into each one. **Fires only in the preview-defining module** (only modules with `@Preview` produce stub
   bodies to fill). Details: [hint-and-marker-generation.md](./hint-and-marker-generation.md).
5. **collectPreviewsReplacement** — In the **preview-collecting module**:
    - `DiscoverHints` scans this module and all dependency modules for the marker interface prefix and gathers
      the matching `previewHint_<scope>` functions.
    - `ReplaceCollectPreviewsFunBody` replaces the sentinel body of each `collect[All]ModulePreviews()` call
      with `PreviewExport(lazy { lazyPreviewSequence({factory}, ...) })`.
    - The sub-logic `buildPreviewSequence/` constructs the actual IR for the lazy sequence, calling each
      discovered hint function and collecting the resulting `CollectedPreview`s, with `BuildPreviewByHashMap`
      providing hash-based deduplication.

   Details: [collect-previews-replacement.md](./collect-previews-replacement.md).

### Cross-module summary

| Logic | FIR / IR | Preview-defining module | Preview-collecting module |
| --- | --- | --- | --- |
| `scopeValidation` | FIR | yes (annotation literals) | yes (call-site literals) |
| `hintAndMarkerGeneration` (FIR) | FIR | yes | no |
| `transformPrivatePreviewToInternal` | FIR | yes | no |
| `hintAndMarkerGeneration` (IR) | IR | yes | no |
| `collectPreviewsReplacement` (incl. `buildPreviewSequence`) | IR | no | yes |

A single module that both *defines* `@Preview` and *calls* `collectModulePreviews()` runs every row above —
the FIR/IR phases simply each see both roles in the same module.

---

## List of constituent logics

### FIR side

- **hintAndMarkerGeneration** — Synthesizes a marker interface (`PreviewHintMarker_<sanitized_fqn>_<hash>`) and
  hint functions (`previewHint_<scope>`) for every `@Preview`. Details: [hint-and-marker-generation.md](./hint-and-marker-generation.md)
  (Part 2 — Hint Generation, Part 3 — Marker Generation)
  - The initial design split this into two logics (`hintGeneration/` + `markerGeneration/`), but PR #200 merged
    them into a single logic. Criteria for keeping the unified form versus re-splitting are in
    [hint-and-marker-generation.md](./hint-and-marker-generation.md) (Part 3 — Marker Generation).
  - Location: [`fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt)
  - Helpers: [`fir/hintGeneration/DeprecationHidden.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/hintGeneration/DeprecationHidden.kt) (shared by both hint and marker),
    [`fir/AttachInternalApi.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/AttachInternalApi.kt) (shared by both hint and marker)
- **scopeValidation** — Validates the string arguments of `@ComposePreviewLabOption(collectScopes = [...])` and
  `collect[All]ModulePreviews(scope = ...)` against `[A-Za-z0-9_]+`. Details: [scope-validation.md](./scope-validation.md)
  - Location: [`fir/scopeValidation/`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/scopeValidation/)

### IR side

- **collectPreviewsReplacement** — Replaces the IR of `collect[All]ModulePreviews()` calls, discovers hint
  functions from dependency modules, fills hint stub bodies, and builds the hash → preview map.
  Details: [collect-previews-replacement.md](./collect-previews-replacement.md)
  - Location: [`ir/collectPreviewsReplacement/`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/)
  - Sub-logic **buildPreviewSequence** — Builds IR for `lazyPreviewSequence({factory}, ...)` / `lazy { ... }` /
    `PreviewExport(...)` and cross-module concatenation (reflecting the PR #196-series Sequence refactor).
    For details, see the "sub-logic" section in [collect-previews-replacement.md](./collect-previews-replacement.md).

---

## Cross-cutting topics

- **[hint-and-marker-generation.md](./hint-and-marker-generation.md)** — Part 1 is the SSoT for the relationship
  between `HintCanonicalKey` + `HintFunName` + `MarkerInterfaceName`. Which class references which naming function,
  the design rationale for the truncated hash length, the lossy-by-design nature of `sanitized` FQNs, and more.
  Serves as the **single source of truth** guaranteeing agreement among the three sites (hint / marker / discovery)
  and is referenced from the other logic docs. Part 2 covers the FIR hint-generation logic that materializes those names.
- **[error-flow.md](./error-flow.md)** — Division of responsibility between FIR diagnostics
  (`KtDiagnosticFactory*`) and IR-side structured errors (`ComposePreviewLabCompilerPluginError`), why we need
  two-level defense, and a list of trigger conditions for each error.

---

## Recommended reading order

1. **[hint-and-marker-generation.md](./hint-and-marker-generation.md)** (Part 1 — Naming) — Internalize the naming rules. Every logic doc assumes you know them.
2. **[error-flow.md](./error-flow.md)** — The big-picture map of which error fires in which phase / role.
3. **[hint-and-marker-generation.md](./hint-and-marker-generation.md)** (Part 2 — Hint Generation, Part 3 — Marker Generation) — The FIR generation side.
4. **[scope-validation.md](./scope-validation.md)** — The FIR Checker side (i.e. how users see it in the IDE).
5. **[collect-previews-replacement.md](./collect-previews-replacement.md)** — The IR side (i.e. the actual replacement and body filling).
