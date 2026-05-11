# Feature: transformPrivatePreviewToInternal

`@Preview private fun` の visibility を **`internal` に昇格** する単一 logic feature。

logic が 1 つしかないため、 詳細トピックを別 .md に分割せず本 README に集約する。
ディレクトリ構造そのものの overview は [`compiler-plugin/README.md`](../../README.md) のみが持つ (SSoT)。

---

## Feature Overview

ユーザ側コード:

```kotlin
@Preview
private fun MyButtonPreview() { ... }   // private のまま collection 側からは見えない
```

これに対し plugin は `private` を `internal` に書き換えることで:

- 同 module の collection-side コード (合成された preview list / hint 関数) から呼び出し可能にする
- module 外への visibility は上げない (= `public` にはしない。 library API 表面を変えない)

```kotlin
// After (semantically equivalent)
@Preview
internal fun MyButtonPreview() { ... }
```

---

## 構成 logic 一覧

- **visibilityPromotion** — `private` を `internal` に昇格する `FirStatusTransformerExtension` logic。
  - 配置: [`fir/visibilityPromotion/PreviewLabFirStatusTransformerExtension.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/transformPrivatePreviewToInternal/fir/visibilityPromotion/PreviewLabFirStatusTransformerExtension.kt)
  - 代表クラス: `PreviewLabFirStatusTransformerExtension`
    (Kotlin compiler API の `FirStatusTransformerExtension` 継承クラスなので、 慣例として `Extension` suffix 維持)

---

## なぜ `private` → `internal` が必要か (動機)

[previewCollection feature](../previewCollection/README.md) の **cross-module hint emission** と
両立させるため:

1. plugin が同 module の `@Preview` ごとに `previewHint_<scope>(value: PreviewHintMarker_<...>?): CollectedPreview` という
   public な合成関数 (= hint stub) を emit する
2. hint stub の body 内では、 該当 `@Preview` 関数を **直接呼び出す** ことで `CollectedPreview(content = { ... })` を構築する
3. ところが `@Preview` 関数が `private` だと、 同 module 内とはいえ **synthetic な hint stub からは呼び出せない**
   (Kotlin の visibility check は file/class scope ベース)

そこで「 `private` を `internal` に格上げする」 ことで、 同 module synthetic hint からの呼び出しを通す。
`public` まで上げないのは、 library 提供者の API 表面 (BCV baseline) を変えないため。

`internal` 昇格された関数は consumer 側 (= dependency module) からは見えないため、 cross-module でも
「 user の private な preview が dependency 経由で leak する」 リスクはない。
hint stub は public だが、 hint stub の **body 内部** で internal 関数を呼ぶこと自体は同 module 内なので問題ない。

---

## 入出力 (Before / After)

### Input

```kotlin
@Preview
private fun MyButtonPreview() { ... }

@Preview                      // 元から internal / public のものは触らない
internal fun MyTextPreview() { ... }
```

### After

```kotlin
@Preview
internal fun MyButtonPreview() { ... }   // ← private が internal に書き換わった

@Preview
internal fun MyTextPreview() { ... }     // 変化なし
```

---

## 設計判断

### なぜ `private` 専用か (`protected` / `internal` は対象外)

`needTransformStatus` で `Visibilities.Private` のみ true を返す。 理由:

- `protected` は top-level に置けない (Kotlin の制約)。 nested の `@Preview` も対象外なので機能上不要
- `internal` は既に同 module から呼べるので昇格不要
- `public` も呼べるので昇格不要

ターゲットを `private` だけに絞ることで、 「予期しない visibility 書き換え」 のリスクを最小化。

### なぜ `FirMemberDeclaration` cast が必要か

`needTransformStatus(declaration: FirDeclaration)` の引数は `FirDeclaration` 型 (visibility プロパティを持たない上位型) なので、
`FirMemberDeclaration` への smart cast が必要。 `declaration !is FirMemberDeclaration` で false return することで、
property / local function 等の non-member 宣言は素通りさせる。

### `@Preview` 検出は CMP + Android の **両 annotation** に対応

- `org.jetbrains.compose.ui.tooling.preview.Preview` (Compose Multiplatform)
- `androidx.compose.ui.tooling.preview.Preview` (Android)

`previewAnnotations` field に両 `ClassId` を持ち、 いずれかが付いていれば対象。 検出は **fast path** (= `hasAnnotation(classId, session)` で
classpath 上の annotation class を直接 lookup) と **fallback** (= annotation の type reference から直接 `ClassId` を読む) の
2 段構え。 これは Compose Multiplatform の場合、 CMP annotation class 自体が classpath にないことがあるため
([`hasPreviewAnnotation` の KDoc](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/transformPrivatePreviewToInternal/fir/visibilityPromotion/PreviewLabFirStatusTransformerExtension.kt))。

### なぜ `FirStatusTransformerExtension` を使うか

FIR の `FirStatusTransformerExtension` は、 declaration の `FirDeclarationStatus`
(modality / visibility / inline 等) を **解決済み状態へ書き換える** ための正規 API。 declaration の status 解決が
他 phase より早く確定するので、 後続の checker / generator が見るときには既に `internal` に見える。

`status.transform(visibility = Visibilities.Internal)` で immutable な新 status を作って返す
(`FirDeclarationStatus` は immutable data shape なので copy-on-write)。

---

## 関連ドキュメント

- [previewCollection feature](../previewCollection/README.md) — visibility 昇格が必要な背景となる cross-module hint emission
- [`compiler-plugin/README.md`](../../README.md) — ディレクトリ構造 overview と各 feature への入口
