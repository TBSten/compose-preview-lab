---
title: Inspector Tab
sidebar_position: 5
---

:::warning

このページは生成 AI によって自動生成されたページです。

:::

import EmbeddedPreviewLab from "@site/src/components/EmbeddedPreviewLab";

# Inspector Tab

Inspector Tab は、PreviewLab のインスペクターパネルにカスタムタブを追加する機能です。コンポーネントのドキュメント、使用例、デザインガイドライン、カスタムデバッグ情報などを表示できます。

## 概要

PreviewLab にはデフォルトで `Fields` と `Events` の2つのタブが用意されていますが、`InspectorTab` インターフェースを実装することで、独自のタブを追加できます。

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

`PreviewLab` の `additionalTabs` パラメータに渡します：

```kt
@Preview
@Composable
fun MyPreview() = PreviewLab(
    additionalTabs = listOf(CustomTab)
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

これらはデフォルトで表示されますが、`additionalTabs` と組み合わせて使用することもできます。

