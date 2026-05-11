# Logic: Hint Generation

`@Preview` ごとに、 cross-module discovery で使う **hint stub 関数** を synthesize する FIR logic。
2026-05 時点では marker interface 生成と **1 つの logic に統合** されており、 単一の
`PreviewHintFirGenerator` (= [`fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt))
が hint と marker の両 API surface (callable / class) を担当する (詳細は [marker-generation.md](./marker-generation.md))。

このドキュメントは **hint stub の callable 側 API** にフォーカスする。 marker class 側 API は
[marker-generation.md](./marker-generation.md) に分けて記述している。 統合形を維持する理由は同 doc 参照。

---

## 入出力 (Input / Generated)

### Input

```kotlin
package com.example.app
@Preview fun MyButtonPreview() { ... }
```

`@ComposePreviewLabOption(collectScopes = [...])` が付いている場合は scope の数だけ、 付いていない場合は
`["default"]` で 1 個の hint が出る。

### Generated (semantically equivalent Kotlin)

```kotlin
// synthetic file in package me.tbsten.compose.preview.lab.hints
package me.tbsten.compose.preview.lab.hints

@kotlin.Deprecated("...", level = HIDDEN)
@me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
@me.tbsten.compose.preview.lab.SyntheticPreviewHint
public fun previewHint_default(value: PreviewHintMarker_com_example_app_MyButtonPreview_<hash>?): CollectedPreview =
    error("Stub! Filled by IR.")
```

scope `"design"` も付いている場合は `previewHint_design` の overload が追加される (同じ marker 型を受ける)。

**body は FIR では emit しない** — `error("Stub!")` 相当の placeholder のままで FIR phase を抜け、
IR phase の [`FillPreviewHintIrBody`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/FillPreviewHintIrBody.kt) が
`return CollectedPreview(...)` を埋める。

---

## 関連クラスと役割

| 構成要素 | 配置 | 役割 |
|---|---|---|
| `PreviewHintFirGenerator` | `fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt` | `FirDeclarationGenerationExtension` 実装。 hint / marker 双方の API surface を持つ |
| `HintEntriesProvider` + `HintEntry` | `feature/previewCollection/HintEntriesProvider.kt` | session-scoped lazy で `@Preview` を walk して `List<HintEntry>` を作る。 hint と marker の両 generator が共有 |
| `parameterTypeFqns()` (`FirNamedFunctionSymbol` extension) | `feature/previewCollection/ParameterTypeFqns.kt` | hint canonical key の素材となる parameter 型 FQN list を取り出す (FIR 側; IR 側にも別実装あり) |
| `isIgnoredByComposePreviewLabOption` / `resolveCollectScopes` (private) | `HintEntriesProvider` 内 | `@ComposePreviewLabOption(ignore=true, collectScopes=[...])` の annotation argument 読み取り |
| `DeprecationHidden.kt::markAsDeprecatedHidden` | `fir/hintGeneration/DeprecationHidden.kt` | hint 関数に `@Deprecated(level=HIDDEN)` を付与し、 deprecation cache を invalidate |
| `AttachInternalApi.kt::markAsInternalSyntheticHint` | `fir/AttachInternalApi.kt` | `@InternalComposePreviewLabApi` + `@SyntheticPreviewHint` を付与 (hint / marker 双方から利用するため feature/fir/ 直下) |
| `PreviewKeys.PreviewLabHint` | `feature/previewCollection/PreviewKeys.kt` | hint stub の `GeneratedDeclarationKey`。 IR の `FillPreviewHintIrBody` が origin チェックに使う |
| `COLLECTED_PREVIEW_CLASS_ID` | `feature/previewCollection/CollectedPreviewClassId.kt` | hint 関数の戻り値型 `me.tbsten.compose.preview.lab.CollectedPreview` の ClassId |

---

## 設計判断

### なぜ「**1 marker × N hint overload**」の形にするか

`@ComposePreviewLabOption(collectScopes = ["design", "screenshot"])` のような multi-scope preview に対して:

- marker は **1 個** (`PreviewHintMarker_<sanitized_fqn>_<hash>`) — KLIB IdSignature は `(name, paramTypes)` から
  derive されるため、 同じ `@Preview` から複数 marker を出すと冗長
- hint 関数は **scope ごとに overload** (`previewHint_design`, `previewHint_screenshot`) — 同じ marker 型を受ける

これにより consumer 側 (IR `DiscoverHints`) は `referenceFunctions(CallableId(HINT_PACKAGE, "previewHint_design"))` の
**1 lookup** で design scope の hint を全件取得できる (scope 違い hint は名前が違うため lookup の時点で除外)。

詳細は [hint-naming.md](./hint-naming.md) を参照。

### なぜ `HintEntriesProvider` を別 `FirExtensionSessionComponent` として切り出すか

`PreviewHintFirGenerator` 自身も `getTopLevelClassIds()` (marker) と `getTopLevelCallableIds()` (hint) の
2 callback を持つので、 `@Preview` symbol walk を 2 回実行することはない。 ただし将来 hint と marker を
2 つの logic に再分離する場合に備え、 walk 結果を session-scoped lazy cache として持つことで
**SSoT を保ったまま 2 generator に share** できる構造にしてある。 `HintEntriesProvider` は
[`PreviewLabFirExtensionRegistrar`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/PreviewLabFirExtensionRegistrar.kt) で
`FirExtensionSessionComponent` として登録され、 `session.hintEntriesProvider.hintEntries` でアクセスする。

### なぜ predicate 登録は `PreviewHintFirGenerator` 側で行うか

`FirDeclarationGenerationExtension.registerPredicates` は predicate registry の正規の入口で、
`FirExtensionSessionComponent` (= `HintEntriesProvider`) は predicate を登録できない。 walk 自体は provider 内で
やるが、 walk 対象の `@Preview` annotation FQN 登録は generator 側に残す。 これは Kotlin compiler API の制約。

### なぜ lazy walk か

`predicateBasedProvider.getSymbolsByPredicate(...)` を `HintEntriesProvider` の init で eager 評価すると、
Kotlin 2.3.21 で frontend resolution cycle が起きる。 `by lazy { computeHintEntries() }` で、
generator の `getTopLevelClassIds` / `getTopLevelCallableIds` callback (= predicate provider の safe entry point)
が初回 touch するまで evaluation を延期する。

### `@Deprecated(level=HIDDEN)` を付ける理由

hint / marker は plugin internal な合成宣言で、 user が source level で参照することは想定していない。
`level = HIDDEN` にすると IDE 補完 / `import` 候補から消え、 namespace squatting で誤って参照する経路が
塞がる。 `replaceDeprecationsProvider` も呼ぶ理由は `DeprecationHidden.kt::markAsDeprecatedHidden` の KDoc 参照。

### `@SyntheticPreviewHint` も付ける理由

`@Deprecated(HIDDEN)` は source-level reachability を遮断するだけで、 binary level の filter ではない。
discovery 側 ([`DiscoverHints`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/DiscoverHints.kt)) が
classpath 上の `previewHint_<scope>` callable を walk する際、 **plugin が emit したもの** だけを採用するための
positive proof として `@SyntheticPreviewHint` を見る。 user が手書きで `previewHint_default` を作っても
この annotation がないため discovery で弾かれる (= namespace squatting 対策)。

---

## ignore = true の扱い

`@ComposePreviewLabOption(ignore = true)` が付いた `@Preview` は **hint emission 自体を skip** する。
`HintEntriesProvider.computeHintEntries` 内で `filterNot { it.isIgnoredByComposePreviewLabOption() }` する。

ignore された preview は:

- hint も marker も emit されない
- cross-module discovery (`referenceFunctions(previewHint_<scope>)`) で発見されない
- IR 側 [`BuildPreviewByHashMap`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/BuildPreviewByHashMap.kt) の hash map にも入らない (consumer 側で同期 filter)

これにより、 ignore された preview の hash が真の preview の hash と truncated-SHA-256 衝突して
false-positive `HintHashCollisionError` を生むリスクも除去される。

---

## 関連ドキュメント

- [marker-generation.md](./marker-generation.md) — marker interface 側 API の logic 詳細 + hint/marker 統合形の維持判断
- [hint-naming.md](./hint-naming.md) — 命名規則 SSoT (3 関連ファイルの参照関係)
- [collect-previews-replacement.md](./collect-previews-replacement.md) — IR 側 hint body 埋め込みと cross-module discovery
- [scope-validation.md](./scope-validation.md) — collectScopes の値検証
- [error-flow.md](./error-flow.md) — `HintHashCollisionError` など Error の役割分担
