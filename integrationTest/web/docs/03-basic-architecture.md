---
title: Basic Architecture
---

import EmbeddedPreviewLab from "@site/src/components/EmbeddedPreviewLab"

# Basic Architecture

Compose Preview Lab は @Preview アノテーションを基盤として、Preview の手動テストを効率的に行えるようにするために以下に示す3つのコンポーネントから成っています。

- [**Build Tooling**](#build-tooling) ... Gradle Plugin, KSP Plugin をセットアップすることで Preview を収集するなどの機能を提供します。
- [**PreviewLabGallery**](#previewlabgallery) ... Preview のリストを一覧表示する Composable。
- [**PreviewLab**](#previewlab) ... Preview を PreviewLab で囲むことで Field, Event などの Preview をインタラクティブに強化する機能を追加する Composable。

これら3つのコンポーネントは完全に分離されて設計されているため、特定のコンポーネントを部分的に利用する/利用しないを柔軟に切り替えることができます。

例えば、Build Tooling で PreviewList を収集する代わりに 手動で PreviewList を定義して PreviewLabGallery で表示する順番を制御することが可能です。

## Build Tooling

[インストール](./install) に従うと Gradle Plugin, KSP Plugin がセットアップされます。

それぞれ以下のような機能があります。

- **Gradle Plugin** には KSP Plugin のセットアップを簡素化するための DSL と [FeaturedFile](./guides/featured-files) サポートのためのコード自動生成ロジックが含まれます。
- **KSP Plugin** は プロジェクト内の `@Preview` を収集し、`<project.name>.PreviewList` などを自動生成します。

## PreviewLabGallery

Preview の情報のリスト (`List<PreviewLabPreview>`) を受け取って一覧表示するコンポーネントです。
通常、一つのプロジェクトに1つあれば十分です。

- サイドバー および コンテンツエリア に Preview をリスト表示します。
- 選択された Preview をコンテンツエリアに表示します。
- CompositionLocal を通じて、一部の機能を PreviewLab に流し込みます。

```kt
setContent {
  PreviewLabGallery(
    previewList = app.PreviewList,
    // ...
  )
}
```

<!-- TODO PreviewLabGallery の画像か埋め込み -->

PreviewLabGallery を使わずに 独自の PreviewLabGallery を実装することもできます。
詳しくは PreviewLabGallery のカスタマイズを参照してください。

## PreviewLab

各 Preview を PreviewLab で囲うことで [Field](./04-guides/02-fields/index.md), [Event](./04-guides/03-events.md) などの機能を Preview に追加することができます。

```kt
// highlight-next-line
PreviewLab(/* オプションの設定 */) { // this: PreviewLabScope
  // Field
  val text = fieldValue { StringField("text", "Click Me") }

  MyButton(
    text =
      // Field
      fieldValue { StringField("text", "Click Me") },
    onClick = {
      // Event
      onEvent("onClick")
    }
  )
}
```

Preview ごとに PreviewLab を囲う必要があります。
裏を返せば Preview ごとに PreviewLab の詳細設定が可能であるため、特定の Preview では PreviewLab を使用しないなどの選択を柔軟にとることができます。

## Next Actions

- Build Tooling のセットアップ方法を [Install](./install) のドキュメントで学習してください。
- PreviewLab の基本機能である [Field](./04-guides/02-fields/index.md), [Event](./04-guides/03-events.md) についてそれぞれのドキュメントで学習してください。
- [Tutorials](./tutorials) の中から気になるもの・あなたの状況に合うものを選択して、あなたのアプリで Compose Preview Lab を活用し初めてください！
