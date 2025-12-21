---
title: "Custom Fields"
sidebar_position: 4
---

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# Custom Fields

Compose Preview Lab には多くのビルトイン Field が用意されていますが、  
独自の型（たとえば `LocalDate` やアプリ固有の `UiState`）を 独自の UI で編集したい場合は **Custom Field** を実装することで柔軟に拡張できます。

:::warning

Custom Field のインターフェースは頻繁に更新される可能性があるため、**できる限り避けるべき** です。

Custom Fields を作成する前に [組み込みのフィールドを強化する](./enhance-fields) ことで Field を作成できないか検討してください。

組み込みのフィールドだけでは実現できない UI を提供したい場合、組み込みフィールドでは実現できない機能を提供したい場合に以下のガイドに進んでください。

:::

## Custom Field 実装の基本

Custom Field は通常 **`MutablePreviewLabField` 抽象クラスを継承** したクラスとして作成します。

```kt
class MyClassField(
    label: String,
    initialValue: MyClass,
// highlight-start
) : MutablePreviewLabField<MyClass>(
    label = label,
    initialValue = initialValue,
// highlight-end
) { ... }
```

- 型引数として Field に対応する型を指定します。
- コンストラクタ引数に `label` (String), `initialValue` (Field の型) を指定します。

次に **@Composable Content()メソッドを override** して、Field の編集 UI を作成します。

```kt
class MyClassField(label: String, initialValue: MyClass) : MutablePreviewLabField<MyClass>(
    label = label,
    initialValue = initialValue,
) {
// highlight-start
    @Composable
    override fun Content() {
        TextField(
            value = this.value,
            onValueChange = { this.value = it },
        )
    }
// highlight-end
}
```

Content メソッド内では `value` のような Field のメンバを利用して編集 UI を実装できます。

## 組み込みの Field の実装を参考にする

StringField, WithHintField などの組み込みの Field は全てこのガイドの内容に沿って実装されており、それらのソースコードは公開されています。

Custom Field の実装に迷ったら組み込みフィールドの実装が参考になる場合が多いでしょう。

例えば valueCode() メソッドの実装方法に悩んだ場合, [Compose Preview Lab の Github リポジトリの検索窓](https://github.com/search?q=repo%3ATBSten%2Fcompose-preview-lab+path%3A*.kt+&type=code) から `"override fun valueCode()"` のように入力して valueCode() をオーバーライドしているコードを検索することで有用な実装が見つかるかもしれません。

import searchGithubImage from "./custom-fields-serach-github.png"

<img src={searchGithubImage} width="600" />

また [DeepWiki](https://deepwiki.com/TBSten/compose-preview-lab) に質問することも有用です。
検索キーワードがわからない場合、定期的に Field の実装を深く学習している AI に曖昧な自然言語で質問することができます。

## 推奨されるカスタマイズ

Custom Field 実装の基本セクションで紹介した実装で Custom Field 実装に必要な最低限の実装は完了です。

続いて以下セクションを参考に Field の実装を進めてください。以下セクションの内容は全てオプションですが、実装することでよりユーザビリティの高い Field を実装することができます。実装する際は `PreviewLab` interface の各メソッドの KDoc も参考にしてください。

### `valueCode()`: Kotlin コード表現を提供する

`valueCode()` メソッドは現在の `value` の値に基づいて Kotlin コード表現を提供するメソッドです。
[Code タブ](../inspector-tab#inspectortabcode) で Field 呼び出しを Kotlin コードとして表示するために利用されます。

```kt
class MyClassField(...) : MutablePreviewLabField<MyClass>(...) {
    ...

    // highlight-next-line
    override fun valueCode() = value.toString()
}
```

### `testValues()`: テスト用の値を提供する

テストの際に利用できる値のリストをリストアップします。利用方法については [Property-based testing](../auto-testing/property-based-testing#testvalues-api) のドキュメントを参照してください。

```kt
class MyClassField(...) : MutablePreviewLabField<MyClass>(...) {
    ...

    // highlight-start
    override fun testValues(): List<MyClass> = super.testValues() + listOf(
        MyClass(/* test pattern 1 */),
        MyClass(/* test pattern 2 */),
        // ...
    )
    // highlight-end
}
```

:::info

override する際、**`super.testValues()` に追加** する形で値のリストを提供してください。
super.testValues() を無視する場合、親フィールドで定義された `testValues()` が無視されてしまいます。

:::

### `serializer()`: 共有リンクコピー時に状態を含められるようにする

```kt
class MyClassField(...) : MutablePreviewLabField<MyClass>(...) {
    ...

    // highlight-start
    override fun serializer() : KSerializer<MyClass> = MyClass.serializer()
    // highlight-end
}
```

`serializer()` メソッドをオーバーライドして KSerializer (kotlinx.serialization) を提供することで Web でのリンク共有時にリンク内に状態を含めることができるようになります。

これにより PreviewLab ユーザが特定の Field の状態を他のユーザに共有することができるようになり ユーザビリティが向上します。

import copySettingImage from "./custom-fields-copy-setting.png"

<img src={copySettingImage} width="500" />

Serialize 不可能なフィールドの場合は null を返してください。

## より高度なカスタマイズ

より高度なカスタマイズが必要な場合は以下のガイドを参考に Field をさらにカスタマイズできます。
以下は推奨されるカスタマイズセクションで紹介した内容より高難易度な実装が求められますが、Field を完全にカスタマイズすることができるようになります。

### `View()`: Field の 編集 UI 全体をカスタマイズする

`Content()` メソッドは Field の編集 UI のメインコンテンツのみを定義しますが、`View()` メソッドでは Field のラベルを表示するヘッダーなどを含めた全ての UI を定義することができます。

import fieldViewContentImage from "./custom-fields-field-view-content.png"

<img src={fieldViewContentImage} width="400" />

View() には通常 `DefaultFieldView` を使用して `label` や 外部から指定された `List<ViewMenuItem<Value>>` を表示します。

```kt
class MyClassField(...) : MutablePreviewLabField<MyClass>(...) {
    ...

    // highlight-start
    @Composable
    override fun View(menuItems: List<ViewMenuItem<Value>> = ViewMenuItem.defaults(this)) {
        DefaultFieldView(
            menuItems = menuItems,
        )
    }
    // highlight-end
}
```

View() メソッドをオーバーライドすることで、必要に応じて 追加の menuItems を追加したり、編集 UI を提供することができます。

### MutablePreviewLabField の代わりに PreviewLabField, ImmutablePreviewLabField を使用する

通常 Field は値の変更ができるため MutablePreviewLabField を継承すべきですが、設計上の理由などにより `value` プロパティを介した更新が望まれない場合 ImmutablePreviewLabField や PreviewLabField を直接 override することができます。
