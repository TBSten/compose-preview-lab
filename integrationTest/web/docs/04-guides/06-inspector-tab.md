---
title: Inspector Tab
sidebar_position: 6
---

:::warning

このページは生成 AI によって自動生成されたページです。

:::

import EmbeddedPreviewLab from "@site/src/components/EmbeddedPreviewLab";

# Inspector Tab

Inspector Tab は、PreviewLab のインスペクターパネルにカスタムタブを追加する機能です。コンポーネントのドキュメント、使用例、デザインガイドライン、カスタムデバッグ情報などを表示できます。

## 概要

PreviewLab にはデフォルトで `Fields`、`Events`、`Code` の3つのタブが用意されていますが、`InspectorTab` インターフェースを実装することで、独自のタブを追加できます。`InspectorTab.defaults` には `Fields`、`Events`、`Code` が含まれています。

## 基本的な使い方

カスタムタブを作成するには、`InspectorTab` インターフェースを実装します：

```kt
object CustomTab : InspectorTab {
    override val title = "Custom"
    override val icon: @Composable () -> Painter = { 
        painterResource(Res.drawable.icon_custom) 
    }

    @Composable
    override fun InspectorTab.ContentContext.Content() {
        Column {
            Text("Custom Tab Content")
            Text("Field count: ${state.scope.fields.size}")
        }
    }
}
```

`PreviewLab` の `inspectorTabs` パラメータに渡します：

```kt
// デフォルトタブ + カスタムタブ
@Preview
@Composable
fun MyPreview() = PreviewLab(
    inspectorTabs = InspectorTab.defaults + listOf(CustomTab)
) {
    MyComponent()
}

// カスタムタブのみ（デフォルトタブなし）
@Preview
@Composable
fun MyPreview() = PreviewLab(
    inspectorTabs = listOf(CustomTab1, CustomTab2)
) {
    MyComponent()
}
```

## InspectorTab インターフェース

`InspectorTab` インターフェースには以下のプロパティとメソッドがあります：

- `title: String` - タブに表示されるタイトル
- `icon: (@Composable () -> Painter)?` - タブのアイコン（オプション）
- `ContentContext.Content()` - タブが選択されたときに表示されるコンテンツ

`ContentContext` を通じて `PreviewLabState` にアクセスでき、フィールド、イベント、その他のプレビュー状態を取得できます。

## 使用例

<details>
<summary>コンポーネントのドキュメント</summary>

コンポーネントのドキュメントのカスタムタブ例です。

```kt
object DocsTab : InspectorTab {
    override val title = "Docs"
    override val icon: @Composable () -> Painter = { painterResource(Res.drawable.icon_doc) }

    @Composable
    override fun InspectorTab.ContentContext.Content() {
        Column {
            Text("このコンポーネントの詳細なドキュメントをここに表示できます。")
        }
    }
}
```

<EmbeddedPreviewLab previewId="InspectorTabDocsExample" />

</details>

<details>
<summary>使用例</summary>

使用例を表示するカスタムタブの例です。

```kt
object UsageTab : InspectorTab {
    override val title = "Usage"
    override val icon: @Composable () -> Painter = { painterResource(Res.drawable.icon_usage) }

    @Composable
    override fun InspectorTab.ContentContext.Content() {
        Column {
            Text("サンプルコードや利用例をここに掲載します。")
        }
    }
}
```

<EmbeddedPreviewLab previewId="InspectorTabUsageExample" />

</details>

<details>
<summary>デザインガイドライン</summary>

デザインガイドラインを示すカスタムタブ例です。

```kt
object DesignTab : InspectorTab {
    override val title = "Design"
    override val icon: @Composable () -> Painter = { painterResource(Res.drawable.icon_design) }

    @Composable
    override fun InspectorTab.ContentContext.Content() {
        Column {
            Text("デザインガイドラインをここに記載します。配色やレイアウトルールなど。")
        }
    }
}
```

<EmbeddedPreviewLab previewId="InspectorTabDesignExample" />

</details>

<details>
<summary>カスタムデバッグ情報</summary>

カスタムデバッグ情報を表示するカスタムタブ例です。

```kt
object DebugTab : InspectorTab {
    override val title = "Debug"
    override val icon: @Composable () -> Painter = { painterResource(Res.drawable.icon_debug) }

    @Composable
    override fun InspectorTab.ContentContext.Content() {
        Column {
            Text("デバッグ用の情報やログ、状態などを表示します。")
        }
    }
}
```

<EmbeddedPreviewLab previewId="InspectorTabDebugExample" />

</details>

## 組み込みタブ

PreviewLab には以下の組み込みタブが用意されています：

- `InspectorTab.Fields` - すべてのインタラクティブフィールドを表示
- `InspectorTab.Events` - すべてのログイベントを表示
- `InspectorTab.Code` - 現在の Preview の Kotlin コードスニペットを表示

これらはデフォルトで表示されますが、`inspectorTabs = listOf(CustomTab)` のように指定することでカスタムタブのみを表示することもできます。

### InspectorTab.Code

`InspectorTab.Code` は、現在表示中の Preview の Kotlin コードスニペットを表示する組み込みタブです。各 `fieldValue { ... }` 呼び出しが、その Field の `valueCode()` メソッドが返すコードに置き換えられた形で表示されます。

これにより、PreviewLab で設定した現在の値を、そのままコピー&ペーストできる Kotlin コードとして確認できます。

```kt
@Preview
@Composable
fun MyPreview() = PreviewLab(
    // Code タブも含めてデフォルトタブを使用
    inspectorTabs = InspectorTab.defaults
) {
    MyComponent(
        text = fieldValue { StringField("text", "Hello") },
        enabled = fieldValue { BooleanField("enabled", true) },
    )
}
```

Code タブを開くと、上記の Preview は以下のようなコードとして表示されます：

```kt
MyComponent(
    text = "Hello",
    enabled = true,
)
```

#### Code タブの動作について

Code タブは、`LocalPreviewLabPreview.current?.code` に保存されている元のコードを基に、各 Field の `label` を手がかりに `fieldValue { ... }` 呼び出しを検索して、その Field の `valueCode()` が返すコードに置き換えます。

各 Field の `valueCode()` をカスタマイズすることで、Code タブに表示されるコードスニペットを自分のプロジェクトの API 形式に合わせることができます。詳細は [Field.withValueCode()](../02-fields/enhance-fields#fieldwithvaluecode-code-タブに出力されるコードをカスタマイズ) を参照してください。

:::note 制限事項

- Field の `label` が重複している場合、期待通りに置換されない可能性があります。
- 複雑なカスタムパターンでは、100% 正確に元のコードを再現するものではなく、「コピー&ペースト用のたたき台」として使用することを想定しています。

:::

