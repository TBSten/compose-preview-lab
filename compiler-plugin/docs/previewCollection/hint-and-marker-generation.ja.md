# Hint / Marker Naming + Hint and Marker Generation

> [English version](./hint-and-marker-generation.md)

`previewCollection` feature の中で密接に関連する 3 つのトピックを扱う:

1. **命名規則 SSoT** — `HintCanonicalKey` + `HintFunName` + `MarkerInterfaceName` の関係、
   どの class がどの symbol を参照するか
2. **hint generation logic** — `@Preview` ごとに hint stub 関数 (= callable 側 API) を synthesize する FIR logic
3. **marker generation logic** — `@Preview` ごとに marker interface (= class 側 API) を synthesize する FIR logic。
   hint と marker は **1 つの統合 generator** が emit する。 統合形を維持する理由は Part 3 にある。

命名規則 → hint 生成 → marker 生成 の順で記述する。 全 logic doc (本 doc 内の hint / marker generation セクションも含む) は
命名規則を前提に書かれているため、 まず命名規則を頭に入れた上で生成セクションを読むこと。

---

## Part 1 — Naming (Single Source of Truth)

`previewCollection` feature では、 1 つの `@Preview` に対して **1 つの marker interface** と
**(scope の数だけ) hint 関数** を synthesize する。 これらの名前フォーマットは、 FIR 生成側 / IR 復元側 /
IR 発見側の **3 sites** で必ず一致しなければならない (一致しないと cross-module discovery が無音破綻する)。

このドキュメントが命名規則の **Single Source of Truth** であり、
[`feature/previewCollection/HintCanonicalKey.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/HintCanonicalKey.kt) /
[`HintFunName.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/HintFunName.kt) /
[`MarkerInterfaceName.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/MarkerInterfaceName.kt) が
実装側 SSoT (top-level `const` / pure function) を持つ。 KDoc は関数単位の Before/After を、
本ドキュメントは **3 ファイル横断の関係性** と **どの class がどこを参照するか** を扱う。

### 全体図

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

### 3 つの SSoT 構成要素

#### 1. `HintCanonicalKey` ([HintCanonicalKey.kt](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/HintCanonicalKey.kt))

- `buildPreviewHintCanonicalKey(sourceFqn, parameterTypeFqns)` —
  `"<sourceFqn>(<paramFqn1>,<paramFqn2>,...)"` 形式の environment-independent key。
  `projectRootPath` / `moduleName` は混ぜない (incremental compilation と reproducible build の両立)。
- `computeHintHash(canonicalKey)` — SHA-256 の先頭 8 バイト → base-36 エンコード → 末尾 8 文字 (`HashLength = 8`)。
  約 41 bit 有効。 同 FQN + 同 signature の overload を区別するための disambiguator。

#### 2. `HintFunName` ([HintFunName.kt](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/HintFunName.kt))

- `PreviewHintFunctionPrefix = "previewHint_"` (`const val`)
- `hintFunctionCallableId(scope: String)` →
  `CallableId(HINT_PACKAGE, Name.identifier("previewHint_$scope"))`
- `isHintFunctionName(name: Name)` — structural チェック (prefix のみ)。
  scope 部分の regex 検証は `SCOPE_VALIDATION_REGEX` (`scopeValidation/ScopeValidationRegex.kt`) と組み合わせる。

scope を **関数名に埋め込む** ことで、 IR 発見側は `referenceFunctions(CallableId(HINT_PACKAGE, "previewHint_<scope>"))` の
**1 callable lookup だけ** で必要 scope の hint を全件回収できる
(per-hint annotation inspection 不要)。

#### 3. `MarkerInterfaceName` ([MarkerInterfaceName.kt](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/MarkerInterfaceName.kt))

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

### 参照元クラス一覧 (どの class がどこを参照するか)

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
| `isMarkerShortName` | `MarkerInterfaceName.kt` | — | `FillPreviewHintIrBody.visitSimpleFunction` (パラメータ型短名のチェック) | — (`DiscoverHints` は `startsWith(PreviewHintMarkerPrefix)` を直接呼び `isMarkerShortName` 経由ではない) |
| `extractHashFromMarkerShortName` | `MarkerInterfaceName.kt` | — | `FillPreviewHintIrBody.visitSimpleFunction` | — |

> **Find Usages との突合**: 上記表は実装の rename / 移動に弱い。 IDE の Find Usages を真の SSoT として扱い、
> rename 時に必ず本表も更新する。

### なぜ canonical key に scope を含めないか

scope は **関数名 (`previewHint_<scope>`)** に embed する設計で、 canonical key (= marker hash 入力) には
**含めない**。 理由:

- 同じ `@Preview` 関数が `collectScopes = ["design", "screenshot"]` で 2 つの scope に参加する場合、
  emit される marker は **1 個** (= 同じ hash) で hint 関数のみが 2 overload 生まれる。
  canonical key に scope を入れると hash が scope ごとに変わってしまい、 marker が scope ごとに重複生成される。
- discovery 側は `referenceFunctions(previewHint_<scope>)` の 1 lookup で scope を filter するため、
  scope 情報は **関数名にあれば十分** で hash には不要。
- canonical key を「FQN + parameter types」だけにしておけば、 IR 側で同じ key を再計算する際に
  scope を context として渡す必要がなくなる (IR 復元側は marker hash → preview の lookup のみで動く)。

### なぜ hash は SHA-256 truncated 8 chars (base-36) か

- **8 chars base-36** ≈ `36^8 ≈ 2.8e12` 通り → 約 41 bit。 1000 previews の hash 衝突確率は約 `10^-7`。
  この衝突は IR 側 `BuildPreviewByHashMap` + `HashMapWithCollisionDetection` で検出して
  `HintHashCollisionError` (構造化 Error) を上げる (詳細は [error-flow.md](./error-flow.md))。
- **base-36 (小文字英数)** にする理由: Kotlin identifier として valid な文字集合で、 大文字小文字を区別しない
  ファイルシステム (例: macOS の HFS+) でも file 名に紛れ込んでも安全。
- **truncate to 8** にする理由: marker class 短名 / hint 関数のパラメータ型短名に embed されるため、
  full SHA-256 (64 hex) は人間可読性 / KLIB IdSignature 文字列長の両面で過剰。 1k preview スケールで実用的に
  衝突しない最小長として 8 を採用。

### なぜ `sanitized(sourceFqn)` で情報損失を許すか

`buildMarkerShortName` の `sanitized(sourceFqn)` は `[^A-Za-z0-9_]` を全て `_` に置換するため、
`com.example.A` と `com_example_A` が同じ sanitized 結果になる。 これは:

- **debug 性のための識別子** であり、 一意性は **hash 部分** が担保する
- KLIB IdSignature と stack trace / IDE navigation の人間可読性を両立させるための trade-off
- backtick 識別子 (例: `fun \`my preview\`()`) を Kotlin source level で扱うために、 単純な `.` → `_` 置換では不十分

`extractHashFromMarkerShortName` が末尾固定長 slice なのもこの設計の帰結 (sanitized 部分を parse する必要がない)。

---

## Part 2 — Hint Generation (FIR logic)

`@Preview` ごとに、 cross-module discovery で使う **hint stub 関数** を synthesize する FIR logic。
2026-05 時点では marker interface 生成と **1 つの logic に統合** されており、 単一の
`PreviewHintFirGenerator` (= [`fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt))
が hint と marker の両 API surface (callable / class) を担当する (marker 側の詳細は
[Part 3 — Marker Generation](#part-3--marker-generation-fir-logic) を参照)。

このセクションは **hint stub の callable 側 API** にフォーカスする。 marker class 側 API は Part 3 に
分けて記述している。 統合形を維持する理由も Part 3 参照。

### 入出力 (Input / Generated)

#### Input

```kotlin
package com.example.app
@Preview fun MyButtonPreview() { ... }
```

`@ComposePreviewLabOption(collectScopes = [...])` が付いている場合は scope の数だけ、 付いていない場合は
`["default"]` で 1 個の hint が出る。

#### Generated (semantically equivalent Kotlin)

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

### 関連クラスと役割

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

### 設計判断

#### なぜ「**1 marker × N hint overload**」の形にするか

`@ComposePreviewLabOption(collectScopes = ["design", "screenshot"])` のような multi-scope preview に対して:

- marker は **1 個** (`PreviewHintMarker_<sanitized_fqn>_<hash>`) — KLIB IdSignature は `(name, paramTypes)` から
  derive されるため、 同じ `@Preview` から複数 marker を出すと冗長
- hint 関数は **scope ごとに overload** (`previewHint_design`, `previewHint_screenshot`) — 同じ marker 型を受ける

これにより consumer 側 (IR `DiscoverHints`) は `referenceFunctions(CallableId(HINT_PACKAGE, "previewHint_design"))` の
**1 lookup** で design scope の hint を全件取得できる (scope 違い hint は名前が違うため lookup の時点で除外)。

この命名分担そのものは Part 1 で確立済み。 canonical key 側の根拠は上記
「なぜ canonical key に scope を含めないか」 を参照。

#### なぜ `HintEntriesProvider` を別 `FirExtensionSessionComponent` として切り出すか

`PreviewHintFirGenerator` 自身も `getTopLevelClassIds()` (marker) と `getTopLevelCallableIds()` (hint) の
2 callback を持つので、 `@Preview` symbol walk を 2 回実行することはない。 ただし将来 hint と marker を
2 つの logic に再分離する場合に備え、 walk 結果を session-scoped lazy cache として持つことで
**SSoT を保ったまま 2 generator に share** できる構造にしてある。 `HintEntriesProvider` は
[`PreviewLabFirExtensionRegistrar`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/PreviewLabFirExtensionRegistrar.kt) で
`FirExtensionSessionComponent` として登録され、 `session.hintEntriesProvider.hintEntries` でアクセスする。

#### なぜ predicate 登録は `PreviewHintFirGenerator` 側で行うか

`FirDeclarationGenerationExtension.registerPredicates` は predicate registry の正規の入口で、
`FirExtensionSessionComponent` (= `HintEntriesProvider`) は predicate を登録できない。 walk 自体は provider 内で
やるが、 walk 対象の `@Preview` annotation FQN 登録は generator 側に残す。 これは Kotlin compiler API の制約。

#### なぜ lazy walk か

`predicateBasedProvider.getSymbolsByPredicate(...)` を `HintEntriesProvider` の init で eager 評価すると、
Kotlin 2.3.21 で frontend resolution cycle が起きる。 `by lazy { computeHintEntries() }` で、
generator の `getTopLevelClassIds` / `getTopLevelCallableIds` callback (= predicate provider の safe entry point)
が初回 touch するまで evaluation を延期する。

#### `@Deprecated(level=HIDDEN)` を付ける理由

hint / marker は plugin internal な合成宣言で、 user が source level で参照することは想定していない。
`level = HIDDEN` にすると IDE 補完 / `import` 候補から消え、 namespace squatting で誤って参照する経路が
塞がる。 `replaceDeprecationsProvider` も呼ぶ理由は `DeprecationHidden.kt::markAsDeprecatedHidden` の KDoc 参照。

#### `@SyntheticPreviewHint` も付ける理由

`@Deprecated(HIDDEN)` は source-level reachability を遮断するだけで、 binary level の filter ではない。
discovery 側 ([`DiscoverHints`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/DiscoverHints.kt)) が
classpath 上の `previewHint_<scope>` callable を walk する際、 **plugin が emit したもの** だけを採用するための
positive proof として `@SyntheticPreviewHint` を見る。 user が手書きで `previewHint_default` を作っても
この annotation がないため discovery で弾かれる (= namespace squatting 対策)。

### ignore = true の扱い

`@ComposePreviewLabOption(ignore = true)` が付いた `@Preview` は **hint emission 自体を skip** する。
`HintEntriesProvider.computeHintEntries` 内で `filterNot { it.isIgnoredByComposePreviewLabOption() }` する。

ignore された preview は:

- hint も marker も emit されない
- cross-module discovery (`referenceFunctions(previewHint_<scope>)`) で発見されない
- IR 側 [`BuildPreviewByHashMap`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/BuildPreviewByHashMap.kt) の hash map にも入らない (consumer 側で同期 filter)

これにより、 ignore された preview の hash が真の preview の hash と truncated-SHA-256 衝突して
false-positive `HintHashCollisionError` を生むリスクも除去される。

---

## Part 3 — Marker Generation (FIR logic)

`@Preview` ごとに 1 個の **marker interface** (`PreviewHintMarker_<sanitized_fqn>_<hash>`) を synthesize する
FIR logic。 hint 関数の `value: <Marker>?` parameter 型として使われ、 KLIB IdSignature を唯一化する役割を持つ。

### 現状: hint と 1 logic 統合形 (`hintAndMarkerGeneration/`)

設計初版では hint 側 generator と marker 側 generator を **2 logic に分離** する計画だった
(`hintGeneration/GeneratePreviewHintFir` + `markerGeneration/GeneratePreviewHintMarkerFir`) が、
PR #200 で **1 logic に統合** された:

- 配置: [`fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt)
- 1 class が `FirDeclarationGenerationExtension` の **class 側 API (`getTopLevelClassIds` / `generateTopLevelClassLikeDeclaration`)**
  と **callable 側 API (`getTopLevelCallableIds` / `generateFunctions`)** の両方を override する

#### なぜ統合形を維持するか

統合形のメリット:

- hint と marker は **同じ `@Preview` の同じ canonical key + hash** から導出されるため、 walk 結果 (= `List<HintEntry>`) の
  share の必要が大きい
- 2 generator に分けると `HintEntry` cache を session component (= `HintEntriesProvider`) 経由で間接 share する設計が必須となり、
  予測可能な状態に保つコストが上がる
- 1 class でも `generateTopLevelClassLikeDeclaration` と `generateFunctions` で API surface は明確に分離されており、
  hint 関連メソッドと marker 関連メソッドは KDoc + メソッド名で実質的に「物理的に同居した 2 logic」 として読める

#### 再分離する場合の判断基準

将来、 以下のいずれかが満たされる場合は 2 logic 分離を検討する:

- hint 側 / marker 側のいずれかに **独立した state** (cache / lookup table) が必要になる
- hint 関連メソッド + marker 関連メソッドの合計行数が 1 file 500 行を超える
- どちらか片方の logic を **別 KMP target / 別 Kotlin version** で異なる挙動にする必要が出る

再分離する場合の path 候補は元計画通り:

- `fir/hintGeneration/GeneratePreviewHintFir.kt` (callable 側)
- `fir/markerGeneration/GeneratePreviewHintMarkerFir.kt` (class 側)
- 共有 walk cache はそのまま `HintEntriesProvider` で吸収可能 (新規 component 不要)

> なお現状でも [`fir/hintGeneration/DeprecationHidden.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/hintGeneration/DeprecationHidden.kt) は
> `hintGeneration/` ディレクトリに残っている。 これは marker / hint 双方が呼ぶ helper だが、 元々 hint 専用の
> `@Deprecated(HIDDEN)` injection として導入された経緯による配置で、 統合 vs 分離の判断には独立した話。

### 入出力 (Input / Generated)

#### Input

```kotlin
package com.example.app
@Preview fun MyButtonPreview() { ... }
```

#### Generated (semantically equivalent Kotlin)

```kotlin
// synthetic file in package me.tbsten.compose.preview.lab.hints
package me.tbsten.compose.preview.lab.hints

@kotlin.Deprecated("...", level = HIDDEN)
@me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
@me.tbsten.compose.preview.lab.SyntheticPreviewHint
public interface PreviewHintMarker_com_example_app_MyButtonPreview_<hash>
```

- modality は `ABSTRACT` (= `interface` のデフォルト)。 `FINAL` ではない (Konan が interface FINAL を拒否)
- `sealed` ではない (`@Deprecated(HIDDEN)` で source-level reachability が遮断されるため redundant)

### 関連クラスと役割

| 構成要素 | 配置 | 役割 |
|---|---|---|
| `PreviewHintFirGenerator.generateTopLevelClassLikeDeclaration` | `fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt` | marker interface の合成 |
| `PreviewHintFirGenerator.getTopLevelClassIds` | 同上 | walk 結果 (= `HintEntry.markerShortName`) から ClassId set を組み立て |
| `HintEntriesProvider` + `HintEntry.markerShortName` | `feature/previewCollection/HintEntriesProvider.kt` | walk 結果の SSoT |
| `MarkerInterfaceName.kt::buildMarkerShortName` | `feature/previewCollection/MarkerInterfaceName.kt` | `PreviewHintMarker_<sanitized_fqn>_<hash>` の組み立て |
| `MarkerInterfaceName.kt::isMarkerShortName` / `extractHashFromMarkerShortName` | 同上 | IR 側で marker 短名 → hash を復元する際の SSoT |
| `DeprecationHidden.kt::markAsDeprecatedHidden(FirClassLikeDeclaration)` | `fir/hintGeneration/DeprecationHidden.kt` | marker に `@Deprecated(HIDDEN)` を付与 |
| `AttachInternalApi.kt::markAsInternalSyntheticHint` | `fir/AttachInternalApi.kt` | `@InternalComposePreviewLabApi` + `@SyntheticPreviewHint` を付与 |
| `PreviewKeys.PreviewLabHintMarkerInterface` | `feature/previewCollection/PreviewKeys.kt` | marker の `GeneratedDeclarationKey` |
| `HINT_PACKAGE` | `feature/previewCollection/HintPackage.kt` | `me.tbsten.compose.preview.lab.hints` の `FqName` (SSoT) |

### 設計判断

#### なぜ `interface` (`class` / `object` ではなく) か

- **Compose Compiler の `$stableprop` synthesis 回避**: `class` / `object` だと JS / Wasm IC で
  Compose Compiler が `$stableprop` シンボルを合成して衝突を起こす
- **Konan compat**: Konan は `Expected a class, found interface` エラーで interface に `FINAL` modality を拒否するため、
  `interface` + `ABSTRACT` modality が cross-target 安全な唯一形

#### なぜ `sealed` ではないか

`@Deprecated(level = HIDDEN)` が付くと、 consumer 側の name resolution scope から marker class が消える。
`class MyMarker : PreviewHintMarker_<sanitized_fqn>_<hash>` を user 側で書こうとしても compile が通らないので、
`sealed` で構造的に閉じる必要はない (= redundant)。 executable proof は test `PreviewHintMarkerSealOrHiddenTest`。

#### なぜ marker 短名の hash 部分を **固定長 8 文字** にするか

[`extractHashFromMarkerShortName`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/MarkerInterfaceName.kt) が
末尾固定長 slice で hash を復元できるようにするため。 `sanitized(sourceFqn)` 部分は lossy (`.` と `_` の両方が `_` に
潰される) ので parse できないが、 hash 部分が後置きの固定長なら parse 不要。

詳細は上の Part 1 — Naming を参照。

#### marker の visibility はなぜ `public` か

cross-module discovery (= IR `DiscoverHints` の `referenceFunctions`) に対応するため、 hint 関数の引数型としての
marker は **public** で classpath に出る必要がある。 単に hidden な internal 実装にすると、 dependency module から
hint 関数の **signature 解決自体ができなくなる** (parameter 型の visibility が呼び出し側 visibility を制約する)。

ただし `@InternalComposePreviewLabApi` + `@SyntheticPreviewHint` で:

- BCV (Binary Compatibility Validator) baseline から除外される
- IDE 補完 / `import` 候補から消える (`@Deprecated(HIDDEN)`)
- IR side `DiscoverHints` の positive-proof として `@SyntheticPreviewHint` を見るので、 user が同名 class を
  自作しても誤検出されない

これらにより、 binary visibility は public だが「 user API surface としては不可視」 が実現される。

---

## 関連ドキュメント

- [collect-previews-replacement.md](./collect-previews-replacement.md) — IR 側 hint 発見 / body 埋め込み / hash map 構築 (marker 短名から hash を復元して preview lookup する流れもここ)
- [scope-validation.md](./scope-validation.md) — collectScopes の値検証
- [error-flow.md](./error-flow.md) — `HintHashCollisionError` など Error の役割分担
