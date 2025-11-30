---
title: PreviewLabGallery
sidebar_position: 4
---

:::warning

このページは生成 AI によって自動生成されたページです。

:::

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# PreviewLabGallery

`PreviewLabGallery` は、プロジェクト内のすべての Preview を一覧表示し、選択した Preview を表示するためのコンポーネントです。通常、一つのプロジェクトに1つあれば十分です。

## 概要

`PreviewLabGallery` は以下の機能を提供します：

- サイドバーおよびコンテンツエリアに Preview をリスト表示
- 選択された Preview をコンテンツエリアに表示
- CompositionLocal を通じて、一部の機能を PreviewLab に流し込み

## 基本的な使い方

`PreviewLabGallery` を使用するには、Preview のリストを渡します：

```kt
fun main() = previewLabApplication {
    PreviewLabGallery(
        previewList = app.PreviewList,
    )
}
```

または、`previewLabApplication` を使わずに直接使用することもできます：

```kt
setContent {
    PreviewLabGallery(
        previewList = app.PreviewList,
    )
}
```

## パラメータ

`PreviewLabGallery` には以下のパラメータがあります：

- `previewList: List<PreviewLabPreview>` - 表示する Preview のリスト
- `modifier: Modifier` - ルートレイアウトに適用する Modifier（デフォルト: `Modifier`）
- `state: PreviewLabGalleryState` - PreviewLabGallery の状態を管理する State（デフォルト: `remember { PreviewLabGalleryState() }`）
- `openFileHandler: OpenFileHandler<out Any?>?` - ソースコードを表示するためのハンドラー（デフォルト: `null`）
- `featuredFileList: Map<String, List<String>>` - Preview をグループ化するためのファイルパスのマップ（デフォルト: `emptyMap()`）

## PreviewLabGalleryState

`PreviewLabGalleryState` は、選択された Preview の状態を管理します。デフォルトでは `remember` を使用しますが、必要に応じて ViewModel などの状態ホルダーに移動して、状態のスコープ（保存期間）を調整できます。

```kt
val state = remember { PreviewLabGalleryState() }

PreviewLabGallery(
    previewList = app.PreviewList,
    state = state,
)
```

## OpenFileHandler

`OpenFileHandler` を指定すると、Preview に対応するソースコードを表示する「Source Code」ボタンが表示されます。

```kt
PreviewLabGallery(
    previewList = app.PreviewList,
    openFileHandler = MyOpenFileHandler(),
)
```

## Featured Files

`featuredFileList` を使用すると、Preview をグループ化して整理できます。重要な Preview や、現在作業中の Preview を簡単にアクセスできるようにします。

```kt
PreviewLabGallery(
    previewList = app.PreviewList,
    featuredFileList = mapOf(
        "Featured" to listOf(
            "src/commonMain/kotlin/me/example/FeaturedComponent.kt"
        ),
        "Work in Progress" to listOf(
            "src/commonMain/kotlin/me/example/WipComponent.kt"
        ),
    ),
)
```

詳しくは [Featured Files](./07-featured-files.md) のドキュメントを参照してください。

## カスタマイズ

`PreviewLabGallery` を使わずに、独自の PreviewLabGallery を実装することもできます。`PreviewLabGalleryNavigator` や `LocalPreviewLabGalleryNavigator` を使用して、カスタムナビゲーションを実装できます。

## 実際の動作を試す

以下の埋め込み Preview で、PreviewLabGallery の動作を確認できます：

<EmbeddedPreviewLab previewId={null} size="large" />

