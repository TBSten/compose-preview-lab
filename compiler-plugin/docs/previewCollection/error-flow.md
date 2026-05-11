# Error Flow

> [日本語版](./error-flow.ja.md)

The errors emitted by the `previewCollection` feature split along **three axes and four groups**.
This document covers "which violation is detected along which axis" and "why we maintain FIR diagnostics
and IR structured errors as two parallel mechanisms".
For per-error class constructor parameters and `description` text, refer to the KDocs
in [`error/Errors.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/error/Errors.kt).

---

## Axis 1: phase (FIR vs IR)

### FIR diagnostic (`KtDiagnosticFactory*`)

- Location: [`feature/previewCollection/fir/scopeValidation/CollectScopeErrors.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/scopeValidation/CollectScopeErrors.kt)
- Mechanism: Kotlin's standard `KtDiagnosticsContainer` + `BaseDiagnosticRendererFactory`. The IDE highlighter
  reads it directly and renders a red-squiggly underline.
- Prefix: The renderer prepends `[ComposePreviewLab/FIR,INVALID_USAGE,PREVIEW_COLLECTION]`.

### IR structured error (`ComposePreviewLabCompilerPluginError`)

- Location: [`error/Errors.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/error/Errors.kt)
- Mechanism: Implementation classes of `interface ComposePreviewLabCompilerPluginError`.
  The `messageCollector.report(error, location)` extension
  ([`error/ReportError.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/error/ReportError.kt))
  routes them to the kotlinc console. Defensive invariants throw a `ComposePreviewLabCompilerPluginException`
  via `error.throwAsException()`
  ([`error/ThrowAsException.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/error/ThrowAsException.kt)).
- Prefix: `error/ReportError.kt::buildErrorBody` assembles `[ComposePreviewLab/<categories>]`.

> **Why maintain these as separate axes**: `KtDiagnosticFactory` is part of the
> `KtDiagnosticsContainer` + `BaseDiagnosticRendererFactory` inheritance hierarchy, where the renderer chain and
> factory definitions are tightly coupled. Replacing this with an `Error` interface would unhook us from Kotlin's
> standard diagnostics machinery (IDE highlighter / source positioning strategy / renderer registration), so we
> deliberately keep the FIR side on the standard rails (see the
> "FIR diagnostics are out of scope" carve-out in [`.claude/rules/compiler-plugin-error.md`](../../../.claude/rules/compiler-plugin-error.md)).

---

## Axis 2: role (user-facing vs defensive)

### user-facing — diagnostics for API misuse by the user

- 2 FIR diagnostics: `INVALID_COLLECT_SCOPE_VALUE`, `NON_LITERAL_COLLECT_SCOPE`
- 4 IR ERRORs: `UnsupportedCollectAllError`, `CollectPreviewsDisabledError`, `NonLiteralScopeIrError`, `InvalidScopeIrError`
- 1 IR ERROR (hash collision): `HintHashCollisionError` (effectively user-facing — resolving the collision requires
  the user to rename something)

### defensive — internal invariants / unreachable-state guards

- IR structured errors (raised via `error.throwAsException()`):
  `PreviewExportNotFoundError`, `RuntimeFunctionNotFoundError`, `PropertyHasNoGetterError`
- FIR-side defensive `IllegalStateException` calls (out of scope for the structured-error rule — kept intentionally
  under the "FIR-side defensive `error()`" carve-out in
  [`.claude/rules/compiler-plugin-error.md`](../../../.claude/rules/compiler-plugin-error.md)), 3 sites:
  - [`feature/previewCollection/fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt) —
    When the owning class (marker symbol) of a hint function cannot be resolved.
  - [`feature/previewCollection/fir/hintGeneration/DeprecationHidden.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/hintGeneration/DeprecationHidden.kt) —
    When the `kotlin.Deprecated` annotation symbol cannot be resolved.
  - [`utils/fir/AnnotationBuilders.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/utils/fir/AnnotationBuilders.kt) —
    When an annotation FQN is missing from the FIR session's symbol provider.
- Kept for Kotlin compiler API conformance: the `CliOptionProcessingException` in `ComposePreviewLabCommandLineProcessor.kt`
  (the `else` branch of the CLI-option `when`, retained for Kotlin compiler API compatibility).

---

## Axis 3: two-level defense (cases detected by both FIR and IR)

The argument validation for `@ComposePreviewLabOption(collectScopes = [...])` and for
`collect[All]ModulePreviews(scope = ...)` is **performed on both the FIR side and the IR side**:

| Violation | FIR side | IR side |
|---|---|---|
| Not a string constant (function call / string concatenation, etc.) | `NON_LITERAL_COLLECT_SCOPE` (diagnostic, [`CheckCollectScopeCall`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/scopeValidation/CheckCollectScopeCall.kt)) | `NonLiteralScopeIrError` ([`Errors.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/error/Errors.kt)) |
| Violates regex `[A-Za-z0-9_]+` | `INVALID_COLLECT_SCOPE_VALUE` (diagnostic, [`CheckCollectScopeAnnotation`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/scopeValidation/CheckCollectScopeAnnotation.kt) / `CheckCollectScopeCall`) | `InvalidScopeIrError` ([`Errors.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/error/Errors.kt)) |

### Why we need two-level defense

The FIR Checker runs at **analysis time** and inspects source literals and expression structure, but values that
arrive via `const val` **cannot always be resolved within the same phase** (FIR's literal-value inspection cannot
reliably distinguish `val` from `const val` in every situation).

```kotlin
private const val BAD_SCOPE = "has space"   // FIR cannot follow BAD_SCOPE's const-fold
collectModulePreviews(scope = BAD_SCOPE)    // The FIR Checker lets this through.
```

By IR phase the value has been folded to an `IrConst<String>`, which the IR side can read directly. It can then
detect both `[A-Za-z0-9_]+` violations (= `InvalidScopeIrError`) and cases that fail to const-fold
(= `NonLiteralScopeIrError`) as the last line of defense.

The FIR Checker exists to provide **instant IDE feedback** (red-squiggly), and the IR ERRORs exist to act as
the **final backstop for const-folded violations the FIR phase cannot catch**.

---

## Error catalog and trigger conditions

The classes in [`error/Errors.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/error/Errors.kt),
with their firing phase and location:

| Error class | Trigger condition | Reported from | Category |
|---|---|---|---|
| `UnsupportedCollectAllError` | A `collectAllModulePreviews()` call on Kotlin <2.3.21 (KLIB) / <2.3.20 (JVM/Android) | `ReplaceCollectPreviewsFunBody` | IR, PREVIEW_COLLECTION, VERSION_GATE |
| `CollectPreviewsDisabledError` | A `collect[All]ModulePreviews()` call when `collectPreviewsEnabled=false` (introduced in PR #186) | `ReplaceCollectPreviewsFunBody` | IR, INVALID_USAGE, PREVIEW_COLLECTION |
| `NonLiteralScopeIrError` | The scope argument is not an `IrConst<String>` at IR phase (the backstop for FIR `NON_LITERAL_COLLECT_SCOPE` slipping through) | `ReplaceCollectPreviewsFunBody` | IR, INVALID_USAGE, PREVIEW_COLLECTION |
| `InvalidScopeIrError` | A const-folded `IrConst<String>` violates the regex (the backstop for FIR `INVALID_COLLECT_SCOPE_VALUE` slipping through) | `ReplaceCollectPreviewsFunBody` | IR, INVALID_USAGE, PREVIEW_COLLECTION |
| `HintHashCollisionError` | Two `@Preview` functions in the same module produce the same truncated hash (`~10^-7` at 1k previews) | `PreviewLabIrGenerationExtension` (the `onCollision` callback of `BuildPreviewByHashMap`) | IR, PREVIEW_COLLECTION |
| `PreviewExportNotFoundError` | `pluginContext.referenceClass(PREVIEW_EXPORT_CLASS_ID)` returns null (defensive) | `buildPreviewSequence/BuildPreviewExportIr` (`.throwAsException()`) | IR |
| `RuntimeFunctionNotFoundError` | `referenceFunctions(callableId)` returns empty (i.e. `lazyPreviewSequence` / `distinctPreviewsByIdSequence` missing from the classpath, defensive) | `buildPreviewSequence/BuildPreviewSequenceIr` / `BuildConcatenatedPreviewSequencesIr` (`.throwAsException()`) | IR |
| `PropertyHasNoGetterError` | `IrProperty.getter == null` (defensive — unreachable in the Kotlin property model) | `ReplaceCollectPreviewsFunBody` (`.throwAsException()`) | IR |

The FIR diagnostics are consolidated in [`CollectScopeErrors.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/scopeValidation/CollectScopeErrors.kt)
(two factories).

---

## Prefix format

Both pipelines surface the same shape to the user:

```
[ComposePreviewLab/IR,INVALID_USAGE,PREVIEW_COLLECTION] collectModulePreviews(scope = ...) accepts only a compile-time string constant
[ComposePreviewLab/FIR,INVALID_USAGE,PREVIEW_COLLECTION] collectModulePreviews(scope = ...) accepts only a compile-time string constant — ...
```

- IR side: `error/ReportError.kt::buildErrorBody` builds the prefix from `categories`.
- FIR side: Inside `Renderer.MAP` of
  [`CollectScopeErrors.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/scopeValidation/CollectScopeErrors.kt)
  the prefix is hand-embedded in the message templates. The format is hand-aligned with the IR side's
  `buildErrorBody` (please keep an eye out for drift between the two).

Aligning the two ensures that the same violation appears with the **same prefix** both in the IDE and on the kotlinc console.

---

## Shared replies (`Replies.kt`)

Recurring guidance strings are consolidated as constants in
[`error/Replies.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/error/Replies.kt):

- `Replies.Unknown` — Bug-report link (mainly for defensive errors).
- `Replies.UpgradeKotlin2321` — Kotlin upgrade guidance (`UnsupportedCollectAllError`).
- `Replies.EnableCollectPreviews` — Guidance for `collectPreviewsEnabled=true`.
- `Replies.LiteralScopeOnly` — Explanation of the literal-scope constraint.
- `Replies.ScopeFormatAllowed` — Explanation of the regex.

Each error references them from its `override val replies`.

---

## Rules

Per [`.claude/rules/compiler-plugin-error.md`](../../../.claude/rules/compiler-plugin-error.md), within
`compiler-plugin/`:

1. Writing `messageCollector.report(CompilerMessageSeverity.ERROR, "...", ...)` directly is prohibited; use
   `messageCollector.report(SomeError(...), location)` instead.
2. Writing `messageCollector.report(CompilerMessageSeverity.WARNING, "...", ...)` directly is prohibited; use
   `messageCollector.report(SomeWarning(...), location)` instead.
3. Calling `error("...")` directly is prohibited; use `SomeError(...).throwAsException()` instead.

The following are excepted:

- FIR diagnostics (`KtDiagnosticFactory*`) — out of scope because they ride on Kotlin's standard machinery.
- FIR-side defensive `IllegalStateException` (symbol-resolution-failure guards) — retained per Kotlin compiler API conventions.
- The `CliOptionProcessingException` at `ComposePreviewLabCommandLineProcessor.kt:68` — retained as-is.

---

## Related documents

- [hint-naming.md](./hint-naming.md) — Background on hash collisions that `HintHashCollisionError` detects.
- [scope-validation.md](./scope-validation.md) — Detailed treatment of two-level defense (FIR Checker + IR ERROR).
- [collect-previews-replacement.md](./collect-previews-replacement.md) — Logic-level detail of where the IR-side ERRORs fire.
