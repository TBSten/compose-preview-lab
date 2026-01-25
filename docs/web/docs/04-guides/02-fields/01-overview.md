---
title: Overview
---

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';
import DocCardList from '@theme/DocCardList';

# Fields

## Quick Summary

- Field は **Preview 内の値を動的に変更できるようにする** ための Compose Preview Lab のコア機能です。
- **`fieldValue {}`** (または `fieldState {}`) と **Field クラス** を組み合わせて定義します。

```kt
@Preview
@Composable
private fun MyButtonPreview() = PreviewLab {
    MyButton(
        // highlight-next-line
        text = fieldValue { StringField(label = "text", initialValue = "Click Me") },
        enabled = fieldValue { BooleanField(label = "enabled", initialValue = true) },
    )
}
```

<EmbeddedPreviewLab
 previewId="FieldQuickSummary"
 title="Field Quick Summary"
/>

## Field の基本的な使い方

Field を使用すると、Preview 内でコンポーネントのパラメータを動的に変更できます。

### 基本例

```kotlin
@Preview
@Composable
private fun MyButtonPreview() = PreviewLab {
    MyButton(
        // highlight-next-line
        text = fieldValue { StringField("text", "Click Me") },
        enabled = fieldValue { BooleanField("enabled", true) },
    )
}
```

ハイライトした行に注目してください。
一般的な Preview では Preview 表示用の固定値を入れることが多いですが、PreviewLab を使う場合は固定値を `fieldValue { フィールドクラス(ラベル, 固定値) }` のように `fieldValue` と フィールドクラス で囲むことで コンポーネントに渡す引数を手動で変更できるようになります。

<EmbeddedPreviewLab
  previewId="FieldBasic"
  title="Field Basic"
/>

この例では：

- `text` パラメータは String 型であるため String 型に対応する Field である `StringField` で制御され、テキスト入力で変更可能になります。
- `enabled` パラメータは Boolean 型であるため Boolean 型に対応する `BooleanField` で制御され、トグルスイッチで変更可能になります。

<details>
<summary>fieldValue と Field クラスがなぜ分割されているかが気になりましたか？</summary>

以下のように別の役割があります。

- fieldValue の役割
  - Field クラスのインスタンスを必要なタイミングで初期化して適切なスコープで保持します。
  - PreviewLab に 編集用 UI を提供します。例えば StringField の場合、Field の値を編集できる TextField を提供します。
  - **`remember { }` と似ています**
- Field クラス の役割
  - これは Compose の State を内包して値を管理する
  - 編集用の UI を提供します
  - **`mutableStateOf()` と似ています**。

```kt
// in Compose
val buttonText = remember { mutableStateOf("Click Me!") }

// with Compose PreviewLab
val buttonText = fieldValue { StringField("buttonText", "Click Me!") }
```

</details>

## fieldValue

`fieldValue { }` は、Field から現在の値を取得するための関数です。

Field から現在の値を取り出して返却します。

```kotlin
@Preview
@Composable
private fun MyComponentPreview() = PreviewLab {
    MyComponent(
        // highlight-next-line
        text = fieldValue { 
            StringField("text", "Hello") 
        },
    )
}
```

## fieldState

`fieldState { }` は、Field から `MutableState<T>` オブジェクトを取得するための関数です。値そのものではなく、リアクティブな State を取得したい場合に使用します。
戻り値が MutableState であるため `by` を使って通常の MutableState のように

```kotlin
@Preview
@Composable
private fun MyTextFieldPreview() = PreviewLab {
    // highlight-next-line
    var text: String by fieldState { StringField("text", "Hello") }
    MyTextField(
        text = text,
        onTextChange = { text = it },
    )
}
```

<EmbeddedPreviewLab
  previewId="FieldState"
  size="small"
  title="Field State"
/>

<details>
<summary><code>fieldValue { }</code> と <code>fieldState { }</code> の使い分け</summary>

- **`fieldValue { }` を使う場合**：
  - Preview 内では値の読み取り
  - ほとんどの通常のケース
- **`fieldState { }` を使う場合**：
  - Preview 内で状態の更新もできるようにする必要がある場合
  - State オブジェクトが必要な場合
  - `derivedStateOf` などと組み合わせる場合
  - より複雑な State 管理が必要な場合

なるべく `fieldValue { }` を利用した方が良いが Preview 内で値の更新が必要な場合は `fieldState { }` を利用すると覚えてください。

</details>

## Field class

`Field` は 画面右の Field タブ内の状態の管理 や 編集用 UI を提供するクラスです。
型ごとに対応した Field を用意する必要がありますが、Primitive などの多くの型はすでにビルトインで用意されています。

![field](./field.png)

`PreviewLabField` には、現在の値を Kotlin コードとして表現する `valueCode()` メソッドがあり、これは Inspector の Code タブで使用されます。多くのビルトイン Field では、適切なコード表現（`"文字列"`, `123`, `Color(0xFF...)`, `16.dp` など）が自動的に生成されるため、通常は意識する必要はありません。詳細なカスタマイズ方法については、[Enhance Fields](./enhance-fields#fieldwithvaluecode-code-タブに出力されるコードをカスタマイズ) を参照してください。

### ビルトインの Field

Compose Preview Lab には、多くのビルトイン Field が用意されています。
代表的なものは以下のとおりです。

| Field名            | 対応するkotlinの型         | 説明                                         |
|--------------------|---------------------------|----------------------------------------------|
| `StringField`      | `String`                  | TextField による文字列入力                   |
| `IntField`         | `Int`                     | Int 型の値を受け取る                         |
| `BooleanField`     | `Boolean`                 | トグルスイッチ                               |
| `SelectableField`  | 任意     | リストから選択する                           |
| `ColorField`       | `Color`                   | Compose の Color を選択する                  |
| `ModifierField`    | `Modifier`                | Compose の Modifier を動的に組み上げる        |

<EmbeddedPreviewLab
  previewId="FieldCommonly"
  title="Field Commonly"
/>

ただしここにある Field はほんの一部です。
自分で作成することもできます。

すべての Field の完全なリストは、以下のページを参照してください：
- [Primitive Fields](./primitive-fields)
- [Enhance Fields](./enhance-fields)
- [Compose Value Fields](./compose-value-fields)
- [Collection Fields](./collection-fields)

## Field のラベルと初期値

Field クラスには共通で `label` (String) と `initialValue` (Field の型) という引数が存在します。

```kt
MyButton(
    enabled = fieldValue {
        // highlight-next-line
        label = "enabled"
        // highlight-next-line
        initialValue = "enabled"
    },
)
```

`label` は Field の編集 UI 上に表示されるテキストです。なるべく他の引数と被らないように命名してください。

多くの場合 Composable の対応する引数の名前を入れれば十分でしょう。(将来的には 自動的に設定される仕組みを用意する予定です)

`initialValue` は Field の初期値を設定してください。

## Field のカスタマイズ

Compose Preview Lab の Field は、ビルトインではない独自の型（例：あなたのアプリの UiState など）をサポートしたり、独自のエディタ UI を提供することができるように設計されています。

以下のオプションを検討してください：

### 1. **既存の Field を強化する**

- SelectableField, CombinedField などを利用することで独自の型であっても比較的簡単にカスタム Field を用意された UI で実装できるようになっています。まずはこちらを検討してください。
- [Enhance Fields](./enhance-fields) を参照してください。

### 2. **完全にカスタムの Field を実装する**

- 既存の Field を強化する で実装できない 完全独自の 編集 UI が必要な場合には カスタムの Field を実装してください。
- 完全にカスタム UI・Field のロジックを実装することができます。
- [Custom Fields](./custom-fields) を参照してください。

## 次のステップ

- [Primitive Fields](./primitive-fields)、[Enhance Fields](./enhance-fields)、[Compose Value Fields](./compose-value-fields)、[Collection Fields](./collection-fields) で利用可能なすべての Field を確認してください。
- [Enhance Fields](./enhance-fields) で Field の強化方法を学んでください。
- [Custom Fields](./custom-fields) で独自の Field を作成する方法を学んでください。

---

<DocCardList />
