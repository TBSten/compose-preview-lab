# Logic: Scope Validation

> [!WARNING]
> このドキュメントは 生成 AI によって生成されています。

> [English version](./scope-validation.md)

`@ComposePreviewLabOption(collectScopes = [...])` annotation argument と
`collect[All]ModulePreviews(scope = ...)` call argument の **文字列引数を FIR analysis phase で検証** する logic。
PR #187 由来。

scope は最終的に `previewHint_<scope>` 関数名に embed されるため、 Kotlin identifier として valid な文字
(= regex `[A-Za-z0-9_]+`) でなければならない。 これに反する値は **IDE 上で red-squiggly** + コンパイル時 ERROR で
即座に弾く。

## なぜ FIR / IR の両側で検証するか (二重防衛)

このドキュメントで深掘りする中心トピック。 詳細は [error-flow.md](./error-flow.md) の「軸 3: 二重防衛」を参照。

### FIR side (本 logic)

- **目的**: IDE 上の即時フィードバック (red-squiggly)
- **検出範囲**: 文字列リテラル / `[A-Za-z0-9_]+` regex 違反 / 非リテラル式 (連結 / 関数呼び出し)
- **検出**できない: `const val` 経由で渡された値 (FIR の literal value inspection が `val` と `const val` を区別できない場面)

### IR side ([`ReplaceCollectPreviewsFunBody`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/ReplaceCollectPreviewsFunBody.kt))

- **目的**: FIR を取りこぼした const-folded 違反の最終 backstop
- **検出範囲**: const-folded 後の `IrConst<String>` を直接見て regex / literal 性を再検証
- 詳細 Error class: `NonLiteralScopeIrError`, `InvalidScopeIrError` ([error-flow.md](./error-flow.md))

```kotlin
private const val BAD_SCOPE = "has space"     // FIR は BAD_SCOPE の const-fold まで追えない
collectModulePreviews(scope = BAD_SCOPE)      // FIR Checker は通過。 IR Checker で InvalidScopeIrError 発火
```

---

## 入出力 (Input / Reported diagnostic)

### Input 例 1: 不正な scope 値 (annotation 側)

```kotlin
@ComposePreviewLabOption(collectScopes = ["good", "has-hyphen", "with space"])
@Preview fun bar() {}
```

→ `INVALID_COLLECT_SCOPE_VALUE("has-hyphen")` と `INVALID_COLLECT_SCOPE_VALUE("with space")` を **要素ごとに**
報告 (IDE が該当 string literal を underline)。 `"good"` は素通り。

### Input 例 2: 不正な scope 値 (call 側)

```kotlin
val a by collectModulePreviews(scope = "has-hyphen")
```

→ `INVALID_COLLECT_SCOPE_VALUE("has-hyphen")` を報告。

### Input 例 3: 非リテラル scope

```kotlin
val b by collectModulePreviews(scope = "ok" + "?")
```

→ `NON_LITERAL_COLLECT_SCOPE("collectModulePreviews")` を報告。

### Input 例 4: `const val` reference (FIR は素通り、 IR でチェック)

```kotlin
private const val BAD = "has space"
val c by collectModulePreviews(scope = BAD)
```

→ FIR は何も報告しない (`const val` reference を `IrConst<String>` 化後でないと literal value を取れない)。
IR side ([`ReplaceCollectPreviewsFunBody`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/ReplaceCollectPreviewsFunBody.kt))
で `InvalidScopeIrError("collectModulePreviews", "has space")` が発火。

---

## 関連クラスと役割

| 構成要素 | 配置 | 役割 |
|---|---|---|
| `PreviewLabFirCheckersExtension` | `fir/scopeValidation/PreviewLabFirCheckersExtension.kt` | `FirAdditionalCheckersExtension` 実装。 2 checker を `CHECKERS` phase に登録 |
| `CheckCollectScopeAnnotation` | `fir/scopeValidation/CheckCollectScopeAnnotation.kt` | `FirDeclarationChecker<FirNamedFunction>` 実装。 `@ComposePreviewLabOption(collectScopes=[...])` の各要素を validate |
| `CheckCollectScopeCall` | `fir/scopeValidation/CheckCollectScopeCall.kt` | `FirExpressionChecker<FirFunctionCall>` 実装。 `collect[All]ModulePreviews(scope = ...)` を validate |
| `CollectScopeErrors` | `fir/scopeValidation/CollectScopeErrors.kt` | `KtDiagnosticsContainer` 実装。 `INVALID_COLLECT_SCOPE_VALUE` と `NON_LITERAL_COLLECT_SCOPE` の 2 factory + Renderer |
| `SCOPE_VALIDATION_REGEX` | `fir/scopeValidation/ScopeValidationRegex.kt` | `Regex("[A-Za-z0-9_]+")` の SSoT。 FIR Checker と IR const-fold seam ([`ReplaceCollectPreviewsFunBody`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/ReplaceCollectPreviewsFunBody.kt)) の両側が import する |

---

## 設計判断

### なぜ `KtDiagnosticFactory*` を使うか (構造化 `Error` interface ではなく)

[`.claude/rules/compiler-plugin-error.md`](../../../.claude/rules/compiler-plugin-error.md) の通り、 FIR 側 diagnostic は
**Kotlin 標準仕組み** (`KtDiagnosticsContainer` + `BaseDiagnosticRendererFactory`) に乗せる。 構造化 `Error` interface
への移行対象外。 理由:

- IDE highlighter / source positioning strategy / diagnostic id ベースの suppress (`@Suppress("INVALID_COLLECT_SCOPE_VALUE")`)
  は Kotlin 標準仕組みに紐づく
- renderer chain と factory 定義が密結合 (`KtDiagnosticFactory1` の generic 引数が message template の placeholder と対応)
- `Error` interface に migration すると IDE 経由のフィードバックを失う

詳細は [error-flow.md](./error-flow.md) の「軸 1: phase」参照。

### なぜ `KtDiagnosticFactory1(...)` を直接構築するか (`by error1<...>()` delegate ではなく)

Kotlin 2.3.21 で `error1` の declaration は `context(...)` parameters を含むため、 delegate path は
`-Xcontext-parameters` enabled でないと使えない。 直接 constructor で必要引数を渡せば同じ結果が得られるので
context parameters への依存を避けている (詳細は [`CollectScopeErrors.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/scopeValidation/CollectScopeErrors.kt) の KDoc 参照)。

### なぜ extension 登録は compat gate するか

[`PreviewLabFirExtensionRegistrar`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/PreviewLabFirExtensionRegistrar.kt) は
`compat.supportsFirCheckers()` (= Kotlin 2.3.20+) のときだけ `PreviewLabFirCheckersExtension` を登録する。
理由: `PreviewLabFirCheckersExtension.simpleFunctionCheckers` は
`Set<FirDeclarationChecker<FirNamedFunction>>` 型で、 `FirNamedFunction` は Kotlin 2.3.20+ で
`FirSimpleFunction` を置き換えた API。 古い Kotlin だと `NoClassDefFoundError` で plugin loading 全体が落ちる。

callable reference (`::PreviewLabFirCheckersExtension`) は **lazy 評価** なので、 `if` 分岐で実際に呼ばれない限り
JVM が class loading をしない。 これにより gate が effective に機能する。

### `const val` の扱い (FIR では検出しない理由を実例付きで)

```kotlin
private const val OK = "design"     // FIR で resolve できれば const-fold できるが…
collectModulePreviews(scope = OK)   // …call site の FIR phase では FirPropertyAccessExpression のまま
```

FIR analysis phase は **literal expression structure** を見るが、 `OK` という symbol が `const val` か
ただの `val` かを **constant folding なしには判別できない** (FIR Checker は IR phase より前)。
FIR 側で `const val` を検出して値を取りに行こうとすると、 `const val` ではないただの `val` も
誤検出 → false negative or false positive のリスクが上がる。 そのため:

- FIR Checker は **明らかな非リテラル** (連結 / 関数呼び出し / `FirStringConcatenationCall`) のみ拒否
- `FirPropertyAccessExpression` は素通り (= `const val` reference の可能性があるため)
- 素通り分の最終 backstop は IR 側 `IrConst<String>` チェック

---

## 関連ドキュメント

- [error-flow.md](./error-flow.md) — FIR diagnostic vs IR 構造化 Error の役割分担 (本 logic は両側の発火元)
- [hint-and-marker-generation.md](./hint-and-marker-generation.md) — Part 1 (Naming) で scope が `previewHint_<scope>` に embed される設計、 Part 2 (Hint Generation) で 検証後の scope が hint 関数生成にどう使われるかを扱う
- [collect-previews-replacement.md](./collect-previews-replacement.md) — IR 側 backstop check の流れ
