# Logic: Scope Validation

> [日本語版](./scope-validation.ja.md)

The logic that **validates the string arguments** of `@ComposePreviewLabOption(collectScopes = [...])` annotations
and `collect[All]ModulePreviews(scope = ...)` call sites **at FIR analysis phase**. Introduced in PR #187.

Because the scope ultimately gets embedded into a function name (`previewHint_<scope>`), it has to be made up
of characters that are valid in a Kotlin identifier (i.e. regex `[A-Za-z0-9_]+`). Anything violating that is
rejected immediately with a **red-squiggly in the IDE** plus a compile-time ERROR.

## Why we validate on both the FIR and IR sides (two-level defense)

That is the central topic of this document. For full details see "Axis 3: two-level defense" in
[error-flow.md](./error-flow.md).

### FIR side (this logic)

- **Goal**: Instant IDE feedback (red-squiggly).
- **Detects**: String literals / `[A-Za-z0-9_]+` regex violations / non-literal expressions (concatenation / function calls).
- **Does not detect**: Values passed via `const val` (FIR's literal-value inspection cannot distinguish `val`
  from `const val` in every case).

### IR side ([`ReplaceCollectPreviewsFunBody`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/ReplaceCollectPreviewsFunBody.kt))

- **Goal**: The last-line backstop for const-folded violations that the FIR side cannot catch.
- **Detects**: Inspects the const-folded `IrConst<String>` directly and re-validates regex / literal-ness.
- Relevant error classes: `NonLiteralScopeIrError`, `InvalidScopeIrError` ([error-flow.md](./error-flow.md)).

```kotlin
private const val BAD_SCOPE = "has space"     // FIR cannot follow BAD_SCOPE's const-fold
collectModulePreviews(scope = BAD_SCOPE)      // The FIR Checker lets this through. The IR checker fires InvalidScopeIrError.
```

---

## Input / Reported diagnostic

### Input example 1: invalid scope value (annotation side)

```kotlin
@ComposePreviewLabOption(collectScopes = ["good", "has-hyphen", "with space"])
@Preview fun bar() {}
```

→ Reports `INVALID_COLLECT_SCOPE_VALUE("has-hyphen")` and `INVALID_COLLECT_SCOPE_VALUE("with space")` **per element**
(the IDE underlines the offending string literals). `"good"` passes through silently.

### Input example 2: invalid scope value (call side)

```kotlin
val a by collectModulePreviews(scope = "has-hyphen")
```

→ Reports `INVALID_COLLECT_SCOPE_VALUE("has-hyphen")`.

### Input example 3: non-literal scope

```kotlin
val b by collectModulePreviews(scope = "ok" + "?")
```

→ Reports `NON_LITERAL_COLLECT_SCOPE("collectModulePreviews")`.

### Input example 4: `const val` reference (passes FIR; checked at IR)

```kotlin
private const val BAD = "has space"
val c by collectModulePreviews(scope = BAD)
```

→ FIR reports nothing (we cannot read the literal value of a `const val` reference until it has been folded into
an `IrConst<String>`). The IR side
([`ReplaceCollectPreviewsFunBody`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/ReplaceCollectPreviewsFunBody.kt))
fires `InvalidScopeIrError("collectModulePreviews", "has space")`.

---

## Related classes and their roles

| Component | Location | Role |
|---|---|---|
| `PreviewLabFirCheckersExtension` | `fir/scopeValidation/PreviewLabFirCheckersExtension.kt` | `FirAdditionalCheckersExtension` implementation. Registers the two checkers in the `CHECKERS` phase |
| `CheckCollectScopeAnnotation` | `fir/scopeValidation/CheckCollectScopeAnnotation.kt` | `FirDeclarationChecker<FirNamedFunction>` implementation. Validates each element of `@ComposePreviewLabOption(collectScopes=[...])` |
| `CheckCollectScopeCall` | `fir/scopeValidation/CheckCollectScopeCall.kt` | `FirExpressionChecker<FirFunctionCall>` implementation. Validates `collect[All]ModulePreviews(scope = ...)` |
| `CollectScopeErrors` | `fir/scopeValidation/CollectScopeErrors.kt` | `KtDiagnosticsContainer` implementation. The two factories `INVALID_COLLECT_SCOPE_VALUE` and `NON_LITERAL_COLLECT_SCOPE` plus the Renderer |
| `SCOPE_VALIDATION_REGEX` | `fir/scopeValidation/ScopeValidationRegex.kt` | SSoT for `Regex("[A-Za-z0-9_]+")`. Imported by both the FIR Checker and the IR const-fold seam ([`ReplaceCollectPreviewsFunBody`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/ReplaceCollectPreviewsFunBody.kt)) |

---

## Design rationale

### Why use `KtDiagnosticFactory*` (rather than the structured `Error` interface)

Per [`.claude/rules/compiler-plugin-error.md`](../../../.claude/rules/compiler-plugin-error.md), FIR-side
diagnostics ride on the **standard Kotlin machinery** (`KtDiagnosticsContainer` + `BaseDiagnosticRendererFactory`)
and are intentionally excluded from the migration to the structured `Error` interface. Reasons:

- IDE highlighter / source positioning strategy / diagnostic-id-based suppression
  (`@Suppress("INVALID_COLLECT_SCOPE_VALUE")`) is tied to Kotlin's standard machinery.
- The renderer chain and factory definitions are tightly coupled (the generic parameters of
  `KtDiagnosticFactory1` correspond to the message template placeholders).
- Migrating to the `Error` interface would forfeit IDE-mediated feedback.

For details see "Axis 1: phase" in [error-flow.md](./error-flow.md).

### Why we construct `KtDiagnosticFactory1(...)` directly (instead of using the `by error1<...>()` delegate)

In Kotlin 2.3.21 the declaration of `error1` is parameterized by `context(...)` parameters, so the delegate
path is unavailable unless `-Xcontext-parameters` is enabled. Passing the required arguments to the constructor
directly gives the same result, which lets us avoid the context-parameters dependency (see the KDoc of
[`CollectScopeErrors.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/scopeValidation/CollectScopeErrors.kt) for more).

### Why we compat-gate the extension registration

[`PreviewLabFirExtensionRegistrar`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/PreviewLabFirExtensionRegistrar.kt)
only registers `PreviewLabFirCheckersExtension` when `compat.supportsFirCheckers()` (= Kotlin 2.3.20+).
The reason: `PreviewLabFirCheckersExtension.simpleFunctionCheckers` is typed
`Set<FirDeclarationChecker<FirNamedFunction>>`, and `FirNamedFunction` is the API that replaced
`FirSimpleFunction` in Kotlin 2.3.20+. On older Kotlin versions, plugin loading as a whole would fail with
`NoClassDefFoundError`.

The callable reference (`::PreviewLabFirCheckersExtension`) is **lazily evaluated**, so the JVM does not load
the class unless the `if` branch actually executes it. That is what makes the gate effective.

### Handling of `const val` (why FIR does not detect it, with a worked example)

```kotlin
private const val OK = "design"     // FIR could const-fold if it resolved the symbol, but…
collectModulePreviews(scope = OK)   // …at the call site's FIR phase this is still a FirPropertyAccessExpression.
```

The FIR analysis phase inspects **literal expression structure**, but cannot **distinguish a `const val` from
a plain `val` without constant folding** (the FIR Checker runs ahead of the IR phase). If we tried to detect
`const val`s and pull their values on the FIR side, we would also catch ordinary `val`s by mistake — false
negatives or false positives become much more likely. So:

- The FIR Checker only rejects **clearly non-literal** expressions (concatenation / function call /
  `FirStringConcatenationCall`).
- `FirPropertyAccessExpression` passes through (it might be a `const val` reference).
- The final backstop for what slips through is the IR-side `IrConst<String>` check.

---

## Related documents

- [error-flow.md](./error-flow.md) — Role separation between FIR diagnostics and IR structured errors (this logic is the originator of both sides).
- [hint-naming.md](./hint-naming.md) — The design that embeds the scope into `previewHint_<scope>`.
- [hint-generation.md](./hint-generation.md) — How the validated scope is then used to generate hint functions.
- [collect-previews-replacement.md](./collect-previews-replacement.md) — The IR-side backstop check flow.
