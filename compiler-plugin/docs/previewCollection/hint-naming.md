# Hint / Marker Naming (Single Source of Truth)

`previewCollection` feature では、 1 つの `@Preview` に対して **1 つの marker interface** と
**(scope の数だけ) hint 関数** を synthesize する。 これらの名前フォーマットは、 FIR 生成側 / IR 復元側 /
IR 発見側の **3 sites** で必ず一致しなければならない (一致しないと cross-module discovery が無音破綻する)。

このドキュメントが命名規則の **Single Source of Truth** であり、
[`feature/previewCollection/HintCanonicalKey.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/HintCanonicalKey.kt) /
[`HintFunName.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/HintFunName.kt) /
[`MarkerInterfaceName.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/MarkerInterfaceName.kt) が
実装側 SSoT (top-level `const` / pure function) を持つ。 KDoc は関数単位の Before/After を、
本ドキュメントは **3 ファイル横断の関係性** と **どの class がどこを参照するか** を扱う。

---

## 全体図

```
@Preview fun com.example.MyButton()
         │
         ├─→ canonical key     = "com.example.MyButton()"                                   (HintCanonicalKey)
         ├─→ hash              = sha256(canonical key) → 8 chars base-36, e.g. "a3k9z2x1"   (HintCanonicalKey)
         ├─→ marker short name = "PreviewHintMarker_com_example_MyButton_a3k9z2x1"          (MarkerInterfaceName)
         └─→ hint fun name     = "previewHint_<scope>"  e.g. "previewHint_default"          (HintFunName)
```

最終的に下記の synthetic 宣言が `me.tbsten.compose.preview.lab.hints` パッケージに出る:

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

`@ComposePreviewLabOption(collectScopes = ["design", "screenshot"])` が付いている場合、
**1 marker × 2 hint overload** が出る (`previewHint_design` と `previewHint_screenshot` がそれぞれ同じ
marker 型を受け取る)。

---

## 3 つの SSoT 構成要素

### 1. `HintCanonicalKey` ([HintCanonicalKey.kt](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/HintCanonicalKey.kt))

- `buildPreviewHintCanonicalKey(sourceFqn, parameterTypeFqns)` —
  `"<sourceFqn>(<paramFqn1>,<paramFqn2>,...)"` 形式の environment-independent key。
  `projectRootPath` / `moduleName` は混ぜない (incremental compilation と reproducible build の両立)。
- `computeHintHash(canonicalKey)` — SHA-256 の先頭 8 バイト → base-36 エンコード → 末尾 8 文字 (`HashLength = 8`)。
  約 41 bit 有効。 同 FQN + 同 signature の overload を区別するための disambiguator。

### 2. `HintFunName` ([HintFunName.kt](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/HintFunName.kt))

- `PreviewHintFunctionPrefix = "previewHint_"` (`const val`)
- `hintFunctionCallableId(scope: String)` →
  `CallableId(HINT_PACKAGE, Name.identifier("previewHint_$scope"))`
- `isHintFunctionName(name: Name)` — structural チェック (prefix のみ)。
  scope 部分の regex 検証は `SCOPE_VALIDATION_REGEX` (`scopeValidation/ScopeValidationRegex.kt`) と組み合わせる。

scope を **関数名に埋め込む** ことで、 IR 発見側は `referenceFunctions(CallableId(HINT_PACKAGE, "previewHint_<scope>"))` の
**1 callable lookup だけ** で必要 scope の hint を全件回収できる
(per-hint annotation inspection 不要)。

### 3. `MarkerInterfaceName` ([MarkerInterfaceName.kt](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/MarkerInterfaceName.kt))

- `PreviewHintMarkerPrefix = "PreviewHintMarker_"` (`const val`)
- `HashLength = 8` (`const val`)
- `buildMarkerShortName(sourceFqn, hash)` —
  `"PreviewHintMarker_" + sanitized(sourceFqn) + "_" + hash`
  `sanitized` = `[^A-Za-z0-9_]` を全て `_` に置換 (backtick 識別子の文字も処理)。
- `isMarkerShortName(shortName)` — structural チェック (prefix のみ)。
- `extractHashFromMarkerShortName(shortName)` — 末尾 `HashLength` 文字を取り出す。
  `sanitized(sourceFqn)` は lossy 可能だが、 hash 部分は固定長なので tail slice で復元可能。

**marker と hint の役割分担**:
- marker は **`@Preview` 1 件あたり 1 個** (KLIB IdSignature を唯一化するためのトークン)
- hint 関数は **scope の数だけ overload** (parameter 型に同じ marker を取る)

KLIB IdSignature は `(name, paramTypes)` から導出されるので、 marker 型が違えば IdSignature が違う。
これにより `previewHint_<scope>` 同名 callable の overload 集合として、 同じ classpath 上の異なる `@Preview` 由来の
hint が共存できる。

---

## 参照元クラス一覧 (どの class がどこを参照するか)

| Symbol | Defined in | FIR 生成側 | IR 復元側 | IR 発見側 |
|---|---|---|---|---|
| `buildPreviewHintCanonicalKey` | `HintCanonicalKey.kt` | `HintEntriesProvider.computeHintEntries` | `BuildPreviewByHashMap.invoke` | — |
| `computeHintHash` | `HintCanonicalKey.kt` | `HintEntriesProvider.computeHintEntries` | `BuildPreviewByHashMap.invoke` | — |
| `PreviewHintFunctionPrefix` | `HintFunName.kt` | `PreviewHintFirGenerator` (= `hintAndMarkerGeneration/`) | — | `isHintFunctionName` 経由で indirect |
| `hintFunctionCallableId` | `HintFunName.kt` | `PreviewHintFirGenerator` | — | `DiscoverHints` (`referenceFunctions(callableId)`) |
| `isHintFunctionName` | `HintFunName.kt` | — | — | (将来の防御コードのための utility) |
| `PreviewHintMarkerPrefix` | `MarkerInterfaceName.kt` | `PreviewHintFirGenerator` | — | `DiscoverHints` (短名 prefix チェック) |
| `HashLength` | `MarkerInterfaceName.kt` | `HintEntriesProvider` (indirect, via `computeHintHash` 出力長) | `extractHashFromMarkerShortName` 経由 | `extractHashFromMarkerShortName` 経由 |
| `buildMarkerShortName` | `MarkerInterfaceName.kt` | `HintEntriesProvider.computeHintEntries` | — | — |
| `isMarkerShortName` | `MarkerInterfaceName.kt` | — | `FillPreviewHintIrBody.visitSimpleFunction` (パラメータ型短名のチェック) | `DiscoverHints.invoke` (structural sanity check) |
| `extractHashFromMarkerShortName` | `MarkerInterfaceName.kt` | — | `FillPreviewHintIrBody.visitSimpleFunction` | — |

> **Find Usages との突合**: 上記表は実装の rename / 移動に弱い。 IDE の Find Usages を真の SSoT として扱い、
> rename 時に必ず本表も更新する。

---

## なぜ canonical key に scope を含めないか

scope は **関数名 (`previewHint_<scope>`)** に embed する設計で、 canonical key (= marker hash 入力) には
**含めない**。 理由:

- 同じ `@Preview` 関数が `collectScopes = ["design", "screenshot"]` で 2 つの scope に参加する場合、
  emit される marker は **1 個** (= 同じ hash) で hint 関数のみが 2 overload 生まれる。
  canonical key に scope を入れると hash が scope ごとに変わってしまい、 marker が scope ごとに重複生成される。
- discovery 側は `referenceFunctions(previewHint_<scope>)` の 1 lookup で scope を filter するため、
  scope 情報は **関数名にあれば十分** で hash には不要。
- canonical key を「FQN + parameter types」だけにしておけば、 IR 側で同じ key を再計算する際に
  scope を context として渡す必要がなくなる (IR 復元側は marker hash → preview の lookup のみで動く)。

---

## なぜ hash は SHA-256 truncated 8 chars (base-36) か

- **8 chars base-36** ≈ `36^8 ≈ 2.8e12` 通り → 約 41 bit。 1000 previews の hash 衝突確率は約 `10^-7`。
  この衝突は IR 側 `BuildPreviewByHashMap` + `HashMapWithCollisionDetection` で検出して
  `HintHashCollisionError` (構造化 Error) を上げる (詳細は [error-flow.md](./error-flow.md))。
- **base-36 (小文字英数)** にする理由: Kotlin identifier として valid な文字集合で、 大文字小文字を区別しない
  ファイルシステム (例: macOS の HFS+) でも file 名に紛れ込んでも安全。
- **truncate to 8** にする理由: marker class 短名 / hint 関数のパラメータ型短名に embed されるため、
  full SHA-256 (64 hex) は人間可読性 / KLIB IdSignature 文字列長の両面で過剰。 1k preview スケールで実用的に
  衝突しない最小長として 8 を採用。

---

## なぜ `sanitized(sourceFqn)` で情報損失を許すか

`buildMarkerShortName` の `sanitized(sourceFqn)` は `[^A-Za-z0-9_]` を全て `_` に置換するため、
`com.example.A` と `com_example_A` が同じ sanitized 結果になる。 これは:

- **debug 性のための識別子** であり、 一意性は **hash 部分** が担保する
- KLIB IdSignature と stack trace / IDE navigation の人間可読性を両立させるための trade-off
- backtick 識別子 (例: `fun \`my preview\`()`) を Kotlin source level で扱うために、 単純な `.` → `_` 置換では不十分

`extractHashFromMarkerShortName` が末尾固定長 slice なのもこの設計の帰結 (sanitized 部分を parse する必要がない)。

---

## 関連ドキュメント

- [hint-generation.md](./hint-generation.md) — hint 関数生成の logic 詳細
- [marker-generation.md](./marker-generation.md) — marker interface 生成の logic 詳細
- [collect-previews-replacement.md](./collect-previews-replacement.md) — IR 側 hint 発見 / body 埋め込み / hash map 構築
- [error-flow.md](./error-flow.md) — hash collision など Error の役割分担
