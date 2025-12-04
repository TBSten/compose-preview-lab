---
title: PreviewLabGallery
sidebar_position: 5
---

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# PreviewLabGallery

## `PreviewLabGallery` Composable

`PreviewLabGallery` Compsoable は、プロジェクト内のすべての Preview を **一覧表示** し、選択した Preview を表示するための エントリーポイントとなるコンポーネントです。通常、一つのプロジェクトに1つあれば十分です。

`PreviewLabGallery` には Preview 情報のリストを渡す必要がある他、オプションで細かな設定を設定することができます。

```kt
PreviewLabGallery(
    previewList = app.PreviewList,
)
```

- previewList 引数にはに自動生成された PreviewList または手動で設定した PreviewLabPreview のインスタンスを指定してください。

ただしいくつかのプラットフォームでは PreviewLabGallery をラップし、[次セクション](#プラットフォームごとの-previewlabgallery) で説明するプラットフォームでは通常そちらを利用することが推奨されます。

## プラットフォームに最適化された PreviewLabGallery

### `PreviewLabGalleryWindows` (Desktop)

PreviewLabGallery をラップして Desktop 向けに最適化して表示します。

現在は 1つの Window を表示するだけですが、将来的に複数の Window が表示されるようになる可能性が高いため、Dekstop においては **こちらを利用することが強く推奨** されます。

```kt
fun main() {
    application {
        PreviewLabGalleryWindows(
            previewList = app.PreviewList,
        )
    }
}
```

### `previewLabApplication` (JS, WasmJs)

PreviewLabGallery をラップして Desktop 向けに最適化して表示します。

こちらも今後の追加機能により previewLabApplication の機能が増える可能性が高いため、**こちらを利用することが強く推奨** されます。

```kt
fun main() {
    previewLabApplication(
        previewList = app.PreviewList,
    )
}
```

## `PreviewLabGallery` のオプション

PreviewLabGallery (PreviewLabGalleryWindows, previewLabApplication も含む) に設定できるオプションは以下の通りです。

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

[PreviewLabGallery の KDoc](https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab/-preview-lab-gallery.html) もご覧ください。

### `previewList`

自動生成された PreviewList を渡してください。ここに表示されたプレビューが一覧表示されます。または 自分で PreviewLabPreview のインスタンスを作成して渡すこともできます。PreviewList の自動生成については [Collect Preview](./collect-preview) を参照してください。

<details open>
<summary>基本例</summary>

```kt
PreviewLabGallery(
    previewList = app.PreviewList,
)
```

</details>

<details>
<summary>辞書順に並べ替える</summary>

```kt
PreviewLabGallery(
    previewList = app.PreviewList
        // highlight-next-line
        .sortedBy { it.displayName },
)
```

</details>

<details>
<summary>PreviewList を手動で構築する</summary>

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
    previewList = app.PreviewList + customizedPreviewList,
)
```

PreviewLabPreview については [PreviewLabPreview の KDoc](https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab/-preview-lab-preview.html?query=fun%20PreviewLabPreview(id:%20String,%20displayName:%20String%20=%20id,%20filePath:%20String?%20=%20null,%20startLineNumber:%20Int?%20=%20null,%20code:%20String?%20=%20null,%20content:%20()%20-%3E%20Unit):%20PreviewLabPreview) を参照してください。

</details>


### `state` (option)

PreviewLabGallery の状態は PreviewLabGalleryState に格納されています。
この state を手動で管理したい場合、 state 引数に PreviewLabGalleryState のインスタンスをします。

PreviewLabGalleryState の詳しい機能については [PreviewLabGalleryState の KDoc](https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab/-preview-lab-gallery-state/index.html) を参照してください。

<details>
<summary>最初に Preview を表示する</summary>

以下の例では `app.PreviewList.MyButtonPreview` を最初に表示するように設定しています。

```kt
val state = rememberSavable(saver = ...) { PreviewLabGalleryState() }.also { state ->
    LaunchedEffect(state) {
        state.select(
            groupName = "all",
            preview = app.PreviewList.MyButtonPreview,
        )
    }
}

PreviewLabGallery(
    // ...
    state = state,
)
```

</details>

### `openFileHandler` (option)

[OpenFileHandler](./open-file-handler) を参照してください。

### `featuredFileList` (option)

[Featured Files](./featured-files) を参照してください。
