---
title: ライブラリドキュメントに PreviewLab を埋め込む
sidebar_position: 6
---

:::warning

このページは生成 AI によって自動生成されたページです。

:::

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# ライブラリドキュメントに PreviewLab を埋め込む

Compose ベースの UI ライブラリやデザインシステムのドキュメントに、  
Compose Preview Lab を使った **インタラクティブな Playground** を埋め込む方法を説明します。

## ゴール

- コンポーネントの API ドキュメントの横に動くプレビューを表示する  
- ドキュメントの読者がその場で Props を変更して挙動を確認できるようにする  
- Docusaurus などの静的サイトと PreviewLab を連携させる

## 1. Web 用の PreviewLab ギャラリーを用意する

まずは、JS/WasmJS ターゲットで Web 上に表示できる PreviewLab ギャラリーを作成します。

```kotlin title="main() の例"
fun main() = previewLabApplication(
    previewList = myLibrary.PreviewList,
) {
    // ライブラリ用のテーマやレイアウトを適用
}
```

生成された Web アプリを GitHub Pages や Vercel などにデプロイしておきます。

## 2. Docusaurus から iframe で埋め込む

Compose Preview Lab のドキュメントサイト（このサイト）では、  
`<EmbeddedPreviewLab />` コンポーネントを使って Preview を iframe として埋め込んでいます。

```tsx title="EmbeddedPreviewLab.tsx（簡略版）"
export default function EmbeddedPreviewLab({ previewId }: { previewId: string }) {
  const url = useBaseUrl(`compose-preview-lab-gallery/?iframe&previewId=${previewId}`);
  return (
    <iframe
      src={url}
      loading="lazy"
      className="embeddedPreviewLabIframe"
    />
  );
}
```

ライブラリドキュメント側では、以下のように Preview を埋め込めます。

```mdx
import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

## Button コンポーネント

`MyButton` は プライマリ / セカンダリ / テキストボタン などのバリエーションを持つボタンコンポーネントです。

<EmbeddedPreviewLab previewId="ButtonVariantsPreview" />
```

:::tip previewId の決め方
- ライブラリ側の `@ComposePreviewLabOption(id = "...")` で指定した ID と一致させる必要があります  
- 命名には `Button.Primary` よりも `ButtonVariants` など用途がわかる名前を付けると、ドキュメント側でも扱いやすくなります  
:::

## 3. デザインシステム用のタブ構成

InspectorTab を使うと、デザインシステム向け Playgrond に独自のタブを追加できます。

```kt title="Design ガイドライン用タブの例"
object DesignTab : InspectorTab {
    override val title = "Design"

    @Composable
    override fun InspectorTab.ContentContext.Content() {
        Column {
            Text("デザインガイドラインをここに記載します。配色やレイアウトルールなど。")
            // トークン一覧やタイポグラフィスケールなどもここに表示可能
        }
    }
}

@Preview
@Composable
fun ButtonWithDesignTabPreview() = PreviewLab(
    inspectorTabs = InspectorTab.defaults + listOf(DesignTab),
) {
    // Playground 用のボタンコンポーネント
}
```

<EmbeddedPreviewLab
  previewId="InspectorTabDesignExample"
/>

## 4. 実運用でのパターン

:::info よくある構成
- ライブラリ本体: `:ui-components` などのモジュールでコンポーネントを定義  
- Playground アプリ: `:ui-components-samples` などのモジュールで Preview と PreviewLabGallery を提供  
- ドキュメントサイト: Docusaurus / mkdocs などで API / ガイドドキュメントを管理し、Playground を iframe で埋め込む  
:::

## 5. 関連ドキュメント

- [Inspector Tab](../04-guides/05-inspector-tab.md) – カスタムタブの追加方法  
- [Fields](../04-guides/02-fields/index.md) – Playground で Props を操作可能にするための仕組み  
- [Featured Files](../04-guides/07-featured-files.md) – デモ用 Preview をグループ化する  

