# Error Flow

> [English version](./error-flow.md)

`previewCollection` feature が出すエラーは **3 軸 4 グループ** に分かれる。
このドキュメントは「どの違反をどの軸で検出するか」「FIR diagnostic と IR 構造化 Error をなぜ二重に持つか」を扱う。
個別 Error class の constructor 引数や `description` は KDoc (= [`error/Errors.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/error/Errors.kt))
を参照。

---

## 軸 1: phase (FIR vs IR)

### FIR diagnostic (`KtDiagnosticFactory*`)

- 配置: [`feature/previewCollection/fir/scopeValidation/CollectScopeErrors.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/scopeValidation/CollectScopeErrors.kt)
- 仕組み: Kotlin 標準の `KtDiagnosticsContainer` + `BaseDiagnosticRendererFactory`。 IDE highlighter が直接読み、
  red-squiggly underline が出る。
- prefix: renderer 側で `[ComposePreviewLab/FIR,INVALID_USAGE,PREVIEW_COLLECTION]` を付与。

### IR 構造化 Error (`ComposePreviewLabCompilerPluginError`)

- 配置: [`error/Errors.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/error/Errors.kt)
- 仕組み: `interface ComposePreviewLabCompilerPluginError` の実装 class。
  `messageCollector.report(error, location)` extension ([`error/ReportError.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/error/ReportError.kt))
  で kotlinc コンソールに流す。 defensive な不変条件は `error.throwAsException()`
  ([`error/ThrowAsException.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/error/ThrowAsException.kt)) で `ComposePreviewLabCompilerPluginException` を throw。
- prefix: `error/ReportError.kt::buildErrorBody` が `[ComposePreviewLab/<categories>]` を組み立てる。

> **なぜ別軸で管理するか**: `KtDiagnosticFactory` は `KtDiagnosticsContainer` + `BaseDiagnosticRendererFactory` の
> 継承体系で、 renderer chain と factory 定義が密結合になっている。 これを `Error` interface 経由に変えると
> Kotlin 標準の diagnostic 仕組み (IDE highlighter / source positioning strategy / renderer registration) から
> 外れてしまうため、 FIR 側は標準仕組みに乗せる方針 (= ルール [`.claude/rules/compiler-plugin-error.md`](../../../.claude/rules/compiler-plugin-error.md)
> の「FIR diagnostic は対象外」)。

---

## 軸 2: 役割 (user-facing vs defensive)

### user-facing — 利用者の API 誤用に対する診断

- FIR diagnostic 2 件: `INVALID_COLLECT_SCOPE_VALUE`, `NON_LITERAL_COLLECT_SCOPE`
- IR ERROR 4 件: `UnsupportedCollectAllError`, `CollectPreviewsDisabledError`, `NonLiteralScopeIrError`, `InvalidScopeIrError`
- IR ERROR 1 件 (hash collision): `HintHashCollisionError` (実質 user-facing。 衝突解消は user 側 rename が必要)

### defensive — 内部不変条件 / 到達不能ガード

- IR 構造化 Error (`error.throwAsException()` 経由): `PreviewExportNotFoundError`, `RuntimeFunctionNotFoundError`, `PropertyHasNoGetterError`
- FIR 側 defensive `IllegalStateException` (構造化対象外、 [`.claude/rules/compiler-plugin-error.md`](../../../.claude/rules/compiler-plugin-error.md)
  の「FIR-side defensive `error()`」例外で意図的に維持) 3 件:
  - [`feature/previewCollection/fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt) — hint 関数 owner class (marker symbol) が解決できない場合
  - [`feature/previewCollection/fir/hintGeneration/DeprecationHidden.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/hintGeneration/DeprecationHidden.kt) — `kotlin.Deprecated` annotation symbol が resolve できない場合
  - [`utils/fir/AnnotationBuilders.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/utils/fir/AnnotationBuilders.kt) — annotation FQN が FIR session symbol provider に存在しない場合
- Kotlin compiler API 慣例維持: `ComposePreviewLabCommandLineProcessor.kt` の `CliOptionProcessingException`
  (CLI option `when` の `else` 分岐。 Kotlin compiler API 互換性のためそのまま維持)

---

## 軸 3: 二重防衛 (FIR + IR の両側で検出するもの)

`@ComposePreviewLabOption(collectScopes = [...])` と `collect[All]ModulePreviews(scope = ...)` の引数検証は
**FIR と IR の両方で行う**:

| 違反 | FIR side | IR side |
|---|---|---|
| 文字列定数でない (関数呼び出し / 文字列連結 etc) | `NON_LITERAL_COLLECT_SCOPE` (diagnostic, [`CheckCollectScopeCall`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/scopeValidation/CheckCollectScopeCall.kt)) | `NonLiteralScopeIrError` ([`Errors.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/error/Errors.kt)) |
| regex `[A-Za-z0-9_]+` 違反 | `INVALID_COLLECT_SCOPE_VALUE` (diagnostic, [`CheckCollectScopeAnnotation`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/scopeValidation/CheckCollectScopeAnnotation.kt) / `CheckCollectScopeCall`) | `InvalidScopeIrError` ([`Errors.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/error/Errors.kt)) |

### なぜ二重防衛が必要か

FIR Checker は **analysis time** に source の literal / 式構造を見るが、
`const val` 経由で渡された値は **同 phase 内で resolve できない** (FIR の literal value inspection は
`val` と `const val` を区別できない場面がある)。

```kotlin
private const val BAD_SCOPE = "has space"   // FIR は BAD_SCOPE の const-fold まで追えない
collectModulePreviews(scope = BAD_SCOPE)    // FIR Checker は通過する
```

IR phase では `IrConst<String>` まで const-fold された値を直接見られるので、 `[A-Za-z0-9_]+` 違反 (= `InvalidScopeIrError`) や
const-fold 不能 (= `NonLiteralScopeIrError`) を最終的に検出できる。

FIR Checker は **IDE 上の即時フィードバック** (= red-squiggly) のために、
IR ERROR は **取りこぼした const-folded 違反の最終 backstop** のために存在する。

---

## エラー一覧と発火条件

[`error/Errors.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/error/Errors.kt) の class と発火 phase / 場所:

| Error class | 発火条件 | 報告場所 | category |
|---|---|---|---|
| `UnsupportedCollectAllError` | Kotlin <2.3.21 (KLIB) / <2.3.20 (JVM/Android) で `collectAllModulePreviews()` 呼び出し | `ReplaceCollectPreviewsFunBody` | IR, PREVIEW_COLLECTION, VERSION_GATE |
| `CollectPreviewsDisabledError` | `collectPreviewsEnabled=false` で `collect[All]ModulePreviews()` 呼び出し (PR #186 由来) | `ReplaceCollectPreviewsFunBody` | IR, INVALID_USAGE, PREVIEW_COLLECTION |
| `NonLiteralScopeIrError` | IR phase で scope が `IrConst<String>` でない (FIR `NON_LITERAL_COLLECT_SCOPE` のすり抜け backstop) | `ReplaceCollectPreviewsFunBody` | IR, INVALID_USAGE, PREVIEW_COLLECTION |
| `InvalidScopeIrError` | const-folded `IrConst<String>` が regex 違反 (FIR `INVALID_COLLECT_SCOPE_VALUE` のすり抜け backstop) | `ReplaceCollectPreviewsFunBody` | IR, INVALID_USAGE, PREVIEW_COLLECTION |
| `HintHashCollisionError` | 同 module 内 2 件の `@Preview` が同じ truncated hash を生む (`~10^-7` at 1k previews) | `PreviewLabIrGenerationExtension` (`BuildPreviewByHashMap` の `onCollision` callback) | IR, PREVIEW_COLLECTION |
| `PreviewExportNotFoundError` | `pluginContext.referenceClass(PREVIEW_EXPORT_CLASS_ID)` が null (defensive) | `buildPreviewSequence/BuildPreviewExportIr` (`.throwAsException()`) | IR |
| `RuntimeFunctionNotFoundError` | `referenceFunctions(callableId)` が空 (= `lazyPreviewSequence` / `distinctPreviewsByIdSequence` が classpath にない, defensive) | `buildPreviewSequence/BuildPreviewSequenceIr` / `BuildConcatenatedPreviewSequencesIr` (`.throwAsException()`) | IR |
| `PropertyHasNoGetterError` | `IrProperty.getter == null` (defensive。 Kotlin property model 上到達不能) | `ReplaceCollectPreviewsFunBody` (`.throwAsException()`) | IR |

FIR diagnostic は [`CollectScopeErrors.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/scopeValidation/CollectScopeErrors.kt)
にまとまっている (2 factory)。

---

## prefix 形式

両系統ともユーザから見ると同じ形に揃える:

```
[ComposePreviewLab/IR,INVALID_USAGE,PREVIEW_COLLECTION] collectModulePreviews(scope = ...) accepts only a compile-time string constant
[ComposePreviewLab/FIR,INVALID_USAGE,PREVIEW_COLLECTION] collectModulePreviews(scope = ...) accepts only a compile-time string constant — ...
```

- IR side: `error/ReportError.kt::buildErrorBody` が `categories` から組み立てる。
- FIR side: [`CollectScopeErrors.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/scopeValidation/CollectScopeErrors.kt)
  の `Renderer.MAP` 内で message template に直接埋め込み。 IR 側の `buildErrorBody` と format を手で揃えている (両者が drift しないよう留意)。

両者を揃えることで、 IDE と kotlinc コンソールで同じ違反が **同じ prefix** で見える。

---

## reply 共通化 (`Replies.kt`)

繰り返し使うガイダンス文言は [`error/Replies.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/error/Replies.kt) に const として集約:

- `Replies.Unknown` — bug-report リンク (主に defensive Error 用)
- `Replies.UpgradeKotlin2321` — Kotlin upgrade ガイダンス (`UnsupportedCollectAllError`)
- `Replies.EnableCollectPreviews` — `collectPreviewsEnabled=true` 案内
- `Replies.LiteralScopeOnly` — literal scope 制約の説明
- `Replies.ScopeFormatAllowed` — regex の説明

各 Error の `override val replies` から参照する。

---

## ルール

[`.claude/rules/compiler-plugin-error.md`](../../../.claude/rules/compiler-plugin-error.md) の通り、
`compiler-plugin/` 配下では:

1. `messageCollector.report(CompilerMessageSeverity.ERROR, "...", ...)` の直書き禁止 →
   `messageCollector.report(SomeError(...), location)`
2. `messageCollector.report(CompilerMessageSeverity.WARNING, "...", ...)` の直書き禁止 →
   `messageCollector.report(SomeWarning(...), location)`
3. `error("...")` の直書き禁止 → `SomeError(...).throwAsException()`

ただし以下は例外:

- FIR diagnostic (`KtDiagnosticFactory*`) は Kotlin 標準仕組みに乗せるため対象外
- FIR-side defensive `IllegalStateException` (symbol 解決失敗ガード) は Kotlin compiler API 慣例で維持
- `ComposePreviewLabCommandLineProcessor.kt:68` の `CliOptionProcessingException` は維持

---

## 関連ドキュメント

- [hint-naming.md](./hint-naming.md) — `HintHashCollisionError` が検出する hash 衝突の背景
- [scope-validation.md](./scope-validation.md) — FIR Checker と IR ERROR の二重防衛詳細
- [collect-previews-replacement.md](./collect-previews-replacement.md) — IR 側 ERROR 発火元の logic 詳細
