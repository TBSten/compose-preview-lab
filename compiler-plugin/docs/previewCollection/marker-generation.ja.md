# Logic: Marker Generation

> [English version](./marker-generation.md)

`@Preview` ごとに 1 個の **marker interface** (`PreviewHintMarker_<sanitized_fqn>_<hash>`) を synthesize する
FIR logic。 hint 関数の `value: <Marker>?` parameter 型として使われ、 KLIB IdSignature を唯一化する役割を持つ。

## ★ 現状: hint と 1 logic 統合形 (`hintAndMarkerGeneration/`)

設計初版では hint 側 generator と marker 側 generator を **2 logic に分離** する計画だった
(`hintGeneration/GeneratePreviewHintFir` + `markerGeneration/GeneratePreviewHintMarkerFir`) が、
PR #200 で **1 logic に統合** された:

- 配置: [`fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt)
- 1 class が `FirDeclarationGenerationExtension` の **class 側 API (`getTopLevelClassIds` / `generateTopLevelClassLikeDeclaration`)**
  と **callable 側 API (`getTopLevelCallableIds` / `generateFunctions`)** の両方を override する

### なぜ統合形を維持するか

統合形のメリット:

- hint と marker は **同じ `@Preview` の同じ canonical key + hash** から導出されるため、 walk 結果 (= `List<HintEntry>`) の
  share の必要が大きい
- 2 generator に分けると `HintEntry` cache を session component (= `HintEntriesProvider`) 経由で間接 share する設計が必須となり、
  予測可能な状態に保つコストが上がる
- 1 class でも `generateTopLevelClassLikeDeclaration` と `generateFunctions` で API surface は明確に分離されており、
  hint 関連メソッドと marker 関連メソッドは KDoc + メソッド名で実質的に「物理的に同居した 2 logic」 として読める

### 再分離する場合の判断基準

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

---

## 入出力 (Input / Generated)

### Input

```kotlin
package com.example.app
@Preview fun MyButtonPreview() { ... }
```

### Generated (semantically equivalent Kotlin)

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

---

## 関連クラスと役割

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

---

## 設計判断

### なぜ `interface` (`class` / `object` ではなく) か

- **Compose Compiler の `$stableprop` synthesis 回避**: `class` / `object` だと JS / Wasm IC で
  Compose Compiler が `$stableprop` シンボルを合成して衝突を起こす
- **Konan compat**: Konan は `Expected a class, found interface` エラーで interface に `FINAL` modality を拒否するため、
  `interface` + `ABSTRACT` modality が cross-target 安全な唯一形

### なぜ `sealed` ではないか

`@Deprecated(level = HIDDEN)` が付くと、 consumer 側の name resolution scope から marker class が消える。
`class MyMarker : PreviewHintMarker_<sanitized_fqn>_<hash>` を user 側で書こうとしても compile が通らないので、
`sealed` で構造的に閉じる必要はない (= redundant)。 executable proof は test `PreviewHintMarkerSealOrHiddenTest`。

### なぜ marker 短名の hash 部分を **固定長 8 文字** にするか

[`extractHashFromMarkerShortName`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/MarkerInterfaceName.kt) が
末尾固定長 slice で hash を復元できるようにするため。 `sanitized(sourceFqn)` 部分は lossy (`.` と `_` の両方が `_` に
潰される) ので parse できないが、 hash 部分が後置きの固定長なら parse 不要。

詳細は [hint-and-marker-generation.md](./hint-and-marker-generation.md) (Part 1 — Naming) を参照。

### marker の visibility はなぜ `public` か

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

- [hint-and-marker-generation.md](./hint-and-marker-generation.md) — hint stub 関数生成 (同 generator が担当) と `MarkerInterfaceName` を含む命名 SSoT
- [collect-previews-replacement.md](./collect-previews-replacement.md) — IR 側で marker 短名から hash を復元 → preview lookup する流れ
