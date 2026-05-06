---
title: PreviewLabGallery
sidebar_position: 5
---

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';
import KDocLink from '@site/src/components/KDocLink';

# PreviewLabGallery

## `PreviewLabGallery` Composable

`PreviewLabGallery` Composable は、プロジェクト内のすべての Preview を **一覧表示** し、選択した Preview を表示するための エントリーポイントとなるコンポーネントです。通常、一つのプロジェクトに1つあれば十分です。

`PreviewLabGallery` には Preview 情報のリストを渡す必要がある他、オプションで細かな設定を設定することができます。

```kt
val appPreviews by collectAllModulePreviews()

PreviewLabGallery(
    previewList = appPreviews,
)
```

- previewList 引数には `collectModulePreviews()` / `collectAllModulePreviews()` で収集したプレビューリスト、または手動で設定した PreviewLabPreview のインスタンスを指定してください。

ただしいくつかのプラットフォームでは PreviewLabGallery をラップし、[次セクション](#プラットフォームに最適化された-previewlabgallery) で説明するプラットフォームでは通常そちらを利用することが推奨されます。

## プラットフォームに最適化された PreviewLabGallery

### `PreviewLabGalleryWindows` (Desktop)

PreviewLabGallery をラップして Desktop 向けに最適化して表示します。

現在は 1つの Window を表示するだけですが、将来的に複数の Window が表示されるようになる可能性が高いため、Dekstop においては **こちらを利用することが強く推奨** されます。

```kt
val appPreviews by collectAllModulePreviews()

fun main() {
    application {
        PreviewLabGalleryWindows(
            previewList = appPreviews,
        )
    }
}
```

### `EmbeddedPreviewOrGallery` (JS, WasmJs)

PreviewLabGallery をラップして Web 向けに最適化して表示します。

```kt
val appPreviews by collectAllModulePreviews()

fun main() {
    ComposeViewport(document.body!!) {
        EmbeddedPreviewOrGallery(
            previewList = appPreviews.toList(),
        )
    }
}
```

## `PreviewLabGallery` のオプション

PreviewLabGallery (PreviewLabGalleryWindows 等も含む) に設定できるオプションは以下の通りです。

```kt
@Composable
fun PreviewLabGallery(
    previewList: List<PreviewLabPreview>,
    modifier: Modifier = Modifier,
    state: PreviewLabGalleryState = remember { PreviewLabGalleryState() },
    openFileHandler: OpenFileHandler<out Any?>? = null,
    featuredFileList: Map<String, List<String>> = emptyMap(),
) = ...
```

<KDocLink path="core/me.tbsten.compose.preview.lab/-preview-lab-gallery.html">PreviewLabGallery の KDoc</KDocLink> もご覧ください。

### `previewList`

`collectModulePreviews()` / `collectAllModulePreviews()` で収集したプレビューリストを渡してください。ここに渡されたプレビューが一覧表示されます。または 自分で PreviewLabPreview のインスタンスを作成して渡すこともできます。Preview の収集については [Collect Preview](./collect-preview) を参照してください。

<details open>
<summary>基本例</summary>

```kt
PreviewLabGallery(
    previewList = appPreviews,
)
```

</details>

<details>
<summary>辞書順に並べ替える</summary>

```kt
PreviewLabGallery(
    previewList = appPreviews
        // highlight-next-line
        .sortedBy { it.displayName },
)
```

</details>

<details>
<summary>手動で構築したリストと結合する</summary>

```kt
val customizedPreviewList = listOf(
    PreviewLabPreview(
        id = "my-composable",
        displayName = "My Composable",
    ) { // @Composable () -> Unit
        PreviewLab {
            MyComposable()
        }
    },
    // ...
)
PreviewLabGallery(
    previewList = appPreviews + customizedPreviewList,
)
```

PreviewLabPreview については <KDocLink path="core/me.tbsten.compose.preview.lab/-preview-lab-preview.html?query=fun%20PreviewLabPreview(id:%20String,%20displayName:%20String%20=%20id,%20filePath:%20String?%20=%20null,%20startLineNumber:%20Int?%20=%20null,%20code:%20String?%20=%20null,%20content:%20()%20-%3E%20Unit):%20PreviewLabPreview">PreviewLabPreview の KDoc</KDocLink> を参照してください。

</details>


### `state` (option)

PreviewLabGallery の状態は PreviewLabGalleryState に格納されています。
この state を手動で管理したい場合、 state 引数に PreviewLabGalleryState のインスタンスをします。

PreviewLabGalleryState の詳しい機能については <KDocLink path="core/me.tbsten.compose.preview.lab/-preview-lab-gallery-state/index.html">PreviewLabGalleryState の KDoc</KDocLink> を参照してください。

### `openFileHandler` (option)

[OpenFileHandler](./open-file-handler) を参照してください。

### `featuredFileList` (option)

[Featured Files](./featured-files) を参照してください。

