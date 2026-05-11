# Feature: previewCollection

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

## List of constituent logics

### FIR side

- **hintAndMarkerGeneration** — Synthesizes a marker interface (`PreviewHintMarker_<sanitized_fqn>_<hash>`) and
  hint functions (`previewHint_<scope>`) for every `@Preview`. Details: [hint-generation.md](./hint-generation.md) /
  [marker-generation.md](./marker-generation.md)
  - The initial design split this into two logics (`hintGeneration/` + `markerGeneration/`), but PR #200 merged
    them into a single logic. Criteria for keeping the unified form versus re-splitting are in
    [marker-generation.md](./marker-generation.md).
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

- **[hint-naming.md](./hint-naming.md)** — SSoT for the relationship between `HintCanonicalKey` + `HintFunName` +
  `MarkerInterfaceName`. Which class references which naming function, the design rationale for the truncated hash
  length, the lossy-by-design nature of `sanitized` FQNs, and more. Serves as the **single source of truth**
  guaranteeing agreement among the three sites (hint / marker / discovery) and is referenced from the other logic docs.
- **[error-flow.md](./error-flow.md)** — Division of responsibility between FIR diagnostics
  (`KtDiagnosticFactory*`) and IR-side structured errors (`ComposePreviewLabCompilerPluginError`), why we need
  two-level defense, and a list of trigger conditions for each error.

---

## Recommended reading order

1. **[hint-naming.md](./hint-naming.md)** — Internalize the naming rules. Every logic doc assumes you know them.
2. **[error-flow.md](./error-flow.md)** — The big-picture map of which error fires in which phase / role.
3. **[hint-generation.md](./hint-generation.md)** + **[marker-generation.md](./marker-generation.md)** — The FIR generation side.
4. **[scope-validation.md](./scope-validation.md)** — The FIR Checker side (i.e. how users see it in the IDE).
5. **[collect-previews-replacement.md](./collect-previews-replacement.md)** — The IR side (i.e. the actual replacement and body filling).
