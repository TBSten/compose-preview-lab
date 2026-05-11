---
paths:
    - "compiler-plugin/**/*.kt"
---

# Rule: compiler-plugin の error / warning は構造化 Error / Warning interface 経由で書く

## 適用範囲

`compiler-plugin/src/main/kotlin/me/tbsten/compose/preview/lab/compiler/` 配下のすべての `.kt` ファイル。

ただし以下は対象外:

- `compat/` および `compat-k*/` 配下 — Kotlin compiler API のバージョン差を吸収する境界レイヤなので、原則 touch しない方針 (CLAUDE.md 参照)
- **FIR diagnostic (`KtDiagnosticFactory*` + `KtDiagnosticsContainer`)** — Kotlin 標準の診断仕組みに乗せる必要があるため、`Error` interface への移行対象外。renderer chain と factory が密結合のため統合不可。`feature/previewCollection/fir/scopeValidation/CollectScopeErrors.kt` (Ticket 1 後の path) はこのカテゴリ
- **FIR-side defensive `error(\"...\")`** — FIR 拡張 (FirDeclarationGenerationExtension / FirStatusTransformerExtension) 内の symbol 解決失敗ガードは、 FIR `KtDiagnosticFactory` でも IR `MessageCollector` でもなく `IllegalStateException` を投げるのが Kotlin compiler API の慣例。`Error` interface に乗せると semantic がずれるため対象外 (= 下記「段階的移行中の既知の rule 違反」末尾も同カテゴリ)

## 段階的移行中の既知の rule 違反

Ticket 0 (本 rule 導入時) では、 IR-side WARNING 2 件 + FIR-side defensive `error()` 3 件の計 5 sites を未移行のまま残していた。 そのうち IR-side WARNING 2 件は **Ticket 4 (A) で構造化済み** (`HintNamespaceSquattingWarning` / `CrossArtifactHintDuplicateWarning` に置換)。 残る 3 件は移行予定なし。

### Ticket 0 のまま維持 (FIR-side defensive `error()` 3 件)

以下 3 sites は FIR 拡張の symbol 解決失敗ガードで、 `Error` interface ではなく Kotlin compiler API 慣例の `IllegalStateException` を保持する:

- `compiler-plugin/src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt:280` — FIR 生成拡張が hint 関数 owner class を解決できない場合の defensive guard
- `compiler-plugin/src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/hintGeneration/DeprecationHidden.kt:56` — `@Deprecated` 注釈の `level` parameter を resolve できない場合の defensive guard
- `compiler-plugin/src/main/kotlin/me/tbsten/compose/preview/lab/compiler/utils/fir/AnnotationBuilders.kt:41` — annotation FQN が FIR session symbol provider に存在しない場合の defensive guard

これらは ↑ 「FIR-side defensive `error()`」適用範囲外と整合する (= rule 違反とはみなさない)。 移行予定なし。

## 要件

`compiler-plugin/` 配下では、以下を **禁止**:

1. **生 `messageCollector.report(CompilerMessageSeverity.ERROR, "...", location)` の直書き** — 構造化されていない literal メッセージを compiler 出力に流す
2. **生 `messageCollector.report(CompilerMessageSeverity.WARNING, "...", location)` の直書き** — 同上 (warning)
3. **生 `error("...")` (= `IllegalStateException(...)`) の直書き** — defensive ガードであっても message 部分を literal で書く

代わりに **必須**:

- ERROR は `error/Errors.kt` (将来的に feature 別に分割可) に `ComposePreviewLabCompilerPluginError` 実装 class を追加し、呼び出し側は
  `messageCollector.report(SomeError(args), location)` (extension function = `error/ReportError.kt`) を使う
- WARNING は `warning/Warnings.kt` に `ComposePreviewLabCompilerPluginWarning` 実装 class を追加し、呼び出し側は
  `messageCollector.report(SomeWarning(args), location)` を使う
- defensive `error("...")` は `SomeError(args).throwAsException()` (= `error/ReportError.kt::throwAsException`) で置き換える。
  message は対応する Error 実装の `message` / `description` / `context` / `replies` で構造化する

各 Error / Warning 実装は以下のフィールドを `override`:

- `categories: List<Category>` — `IR` / `FIR` / `PREVIEW_COLLECTION` / `INVALID_USAGE` / `VERSION_GATE` / `TRANSFORM_PRIVATE_PREVIEW_TO_INTERNAL` から選ぶ。renderer が `[ComposePreviewLab/<categories>]` prefix として整形する
- `message: String` — 1 行サマリー (prefix 直後に表示)
- `description: String?` — 静的な発生条件 / 予防策の散文 (optional)
- `context: List<ContextEntry>` — `contextOf { "label"(value) }` DSL で動的値を詰める (`String.invoke(value)` で entry を append。 boolean / tag 用に no-arg `"isHoge"()` overload もあり)
- `replies: List<String>` — 共通 reply は `error/Replies.kt` の const から取る (`Replies.Unknown` 等)

## Good / Bad 例

### Good — ERROR を構造化

```kotlin
// compiler-plugin/.../error/Errors.kt
class HintHashCollisionError(
    private val hash: String,
    private val previewA: String,
    private val previewB: String,
) : ComposePreviewLabCompilerPluginError {
    override val categories = listOf(Category.IR, Category.PREVIEW_COLLECTION)
    override val message = "hint hash collision detected on '$hash'"
    override val description: String =
        "Two distinct @Preview functions hash to the same SHA-256 truncated 7-byte value."
    override val context = contextOf {
        "hash"(hash)
        "preview_a"(previewA)
        "preview_b"(previewB)
    }
    override val replies = listOf(Replies.Unknown)
}

// 呼び出し側
messageCollector.report(
    HintHashCollisionError(hash, existingSignature, conflictingSignature),
    conflicting.function.compilerMessageLocation(),
)
```

### Bad — literal 直書き

```kotlin
// compiler-plugin/.../ir/PreviewLabIrGenerationExtension.kt
messageCollector.report(
    CompilerMessageSeverity.ERROR,
    "[ComposePreviewLab] hint hash collision detected on `$hash`. " +     // ← 構造化されていない
        "Two distinct @Preview functions hash to the same value: ...",     // ← prefix も literal
    conflicting.function.compilerMessageLocation(),
)
```

### Good — defensive `error()` を `throwAsException()` で構造化

```kotlin
private val lazyPreviewSequenceFun by lazy {
    val callableId = callableIdOf("me.tbsten.compose.preview.lab", "lazyPreviewSequence")
    pluginContext.referenceFunctions(callableId).firstOrNull()
        ?: RuntimeFunctionNotFoundError(callableId).throwAsException()
}
```

### Bad — defensive `error()` literal

```kotlin
private val lazyPreviewSequenceFun by lazy {
    pluginContext.referenceFunctions(
        callableIdOf("me.tbsten.compose.preview.lab", "lazyPreviewSequence"),
    ).firstOrNull() ?: error(                                              // ← Error 実装なし
        "me.tbsten.compose.preview.lab.lazyPreviewSequence not found ...",
    )
}
```

### Good — WARNING を構造化

`Error` と同じ要領で、 `warning/Warnings.kt` に
`ComposePreviewLabCompilerPluginWarning` 実装 class を追加し、
`messageCollector.report(warning, location)` extension で呼び出す。 `contextOf { ... }`
DSL も Error 側と完全に同一 (= `"label"(value)` で entry を append。 boolean / tag は
no-arg overload `"isHoge"()` で append) で、 `+` prefix は使わない。

```kotlin
// compiler-plugin/.../warning/Warnings.kt
class HintNamespaceSquattingWarning(
    private val packageName: String,
    private val markerFqn: String,
) : ComposePreviewLabCompilerPluginWarning {
    override val categories = listOf(Category.IR, Category.PREVIEW_COLLECTION)
    override val message =
        "a function in '$packageName' matching the per-scope hint shape is missing " +
            "the @SyntheticPreviewHint marker (marker parameter '$markerFqn') — " +
            "candidate dropped to prevent namespace squatting"
    override val description: String =
        "Only the compose-preview-lab compiler plugin should emit declarations into the " +
            "reserved hint package..."
    override val context = contextOf {
        "hint_package"(packageName)
        "marker"(markerFqn)
    }
    override val replies = listOf(Replies.InvestigateHintsPackageOwner)
}

// 呼び出し側
messageCollector.report(
    HintNamespaceSquattingWarning(
        packageName = HINT_PACKAGE.asString(),
        markerFqn = markerFqn.asString(),
    ),
    hintFunction.compilerMessageLocation(),
)
```

### Bad — literal WARNING 直書き

```kotlin
// compiler-plugin/.../ir/HintDiscovery.kt
messageCollector.report(
    CompilerMessageSeverity.WARNING,
    "Compose Preview Lab: a function in '${HINT_PACKAGE.asString()}' " +     // ← 構造化なし
        "matching the per-scope hint shape is missing the @SyntheticPreviewHint marker " +
        "(marker parameter '$markerFqn'). ...",
)                                                                            // ← location も渡せていない
```

## 例外: ComposePreviewLabCommandLineProcessor の `CliOptionProcessingException`

`ComposePreviewLabCommandLineProcessor.kt:68` の
`throw CliOptionProcessingException("Unknown option: $optionName")` は Kotlin compiler API の
規約に従う defensive ガードで、Kotlin compiler 側のエラーハンドリングと整合させる必要があるため
**そのまま維持** する。Error 実装でラップしない。

## 新規 Warning を追加する時の手順

(Ticket 4 後の運用想定)

1. **判断**: 「処理を止める必要があるか」を最初に判断。 止める = `error/Errors.kt` (ERROR)、 止めない = `warning/Warnings.kt` (WARNING)。 境界が曖昧なものは Error 側に倒すか `@ComposePreviewLabOption` で opt-in に逃がす
2. **false positive 検証**: 「ユーザーが意図的にそうしている可能性が高い」 入力にまで warning を出さないかを 3 軸で評価:
   - ユーザーが知りたい状況か
   - 処理を止めるほどではないか
   - false positive にならないか
3. **Kotlin 標準の警告との重複回避**: Kotlin compiler が出す deprecated / unused / unchecked cast 等の警告と重複しない、 本 plugin 固有の知識でしか出せないものに限定
4. **`warning/Warnings.kt` に Warning class 追加**:
   - constructor parameter として動的値を受け取る
   - `categories: List<Category>` で `IR` / `FIR` + feature 軸 (`PREVIEW_COLLECTION` 等) を選ぶ
   - `message` / `description` / `context` (= `contextOf { "label"(value) }`) / `replies` を `override`
   - 共通 reply は `error/Replies.kt` (= `ComposePreviewLabCompilerPluginError.Replies` の typealias) の const から取る。 新規 reply を追加する場合は `Replies` object に const として追加し、 wording を 1 箇所で管理
5. **呼び出し側**: `messageCollector.report(SomeWarning(args), location)` extension で呼ぶ。 `location` は `IrDeclaration.compilerMessageLocation()` (= `utils/ir/CompilerMessageLocation.kt`) で取得すると IDE 連携が効く
6. **テスト**: 「warning が出る入力」 と 「warning が出ない正常入力」 の 両方を kctfork 経由で 検証 (false positive guard)

## チェックリスト (PR レビュー時)

- [ ] `compiler-plugin/.../*.kt` で `messageCollector.report(CompilerMessageSeverity.ERROR, "...", ...)` の直書きがない
- [ ] `compiler-plugin/.../*.kt` で `messageCollector.report(CompilerMessageSeverity.WARNING, "...", ...)` の直書きがない (`compat/` 配下は除外)
- [ ] `compiler-plugin/.../*.kt` で `error("...")` の直書きがない (`compat/` 配下は除外、`ComposePreviewLabCommandLineProcessor.kt` の `CliOptionProcessingException` も除外)
- [ ] 新規 Error 実装は `error/Errors.kt` に追加 / 新規 Warning 実装は `warning/Warnings.kt` に追加
- [ ] `categories` の選択が妥当 (`IR` / `FIR` + feature 軸 + 必要なら `INVALID_USAGE` / `VERSION_GATE`)
- [ ] 共通 reply は `error/Replies.kt` の const から取る (Bug-report `Replies.Unknown` / version upgrade `Replies.UpgradeKotlin2321` 等)
- [ ] FIR 側 `KtDiagnosticFactory` (= `CollectScopeErrors.kt`) は対象外として明示的に許容
- [ ] 新規 Warning は false positive リスクを 3 軸で評価済み (「ユーザーが知りたい状況か」「処理を止めるほどでないか」「false positive にならないか」)
