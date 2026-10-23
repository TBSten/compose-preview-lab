---
title: "Dokka Plugin (KDoc に PreviewLab を埋め込む)"
sidebar_position: 16
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import ComposePreviewLabVersion from "@site/src/components/ComposePreviewLabVersion"

# Dokka Plugin (KDoc に PreviewLab を埋め込む)

`me.tbsten.compose.preview.lab:dokka-plugin` は [Dokka](https://kotl.in/dokka) のプラグインです。
KDoc に `@previewLab <previewId>` と書くだけで、Dokka が生成する API ドキュメントのページに Compose Preview Lab の Preview が **動く `<iframe>`** として埋め込まれます。

UI ライブラリやデザインシステムの API リファレンスを読みながら、その場でコンポーネントを触れるようになります。

```kotlin title="MyButton.kt"
/**
 * 押すと色が変わるボタン。
 *
 * @param onClick タップ時のハンドラ
 * // highlight-next-line
 * @previewLab MyButtonPreview 1280x680
 */
@Composable
fun MyButton(onClick: () -> Unit) { /* … */ }
```

## 仕組み

```
KDoc の @previewLab MyButtonPreview
        │  dokkaGenerate (dokka-plugin が tag を解釈)
        ▼
<iframe src="<baseUrl>/?iframe&previewId=MyButtonPreview">   ← API ページに出力される
        │  ブラウザが読み込む
        ▼
<baseUrl> にホストした Gallery (EmbeddedPreviewOrGallery)
        → ?iframe&previewId=... を受け取り、その Preview だけを表示
```

1. `dokka-plugin` が KDoc の `@previewLab` タグを拾い、API ページに `<iframe>` を出力します
2. `<iframe>` の `src` は `<baseUrl>/?iframe&previewId=<previewId>` です
3. `baseUrl` にホストされた Gallery ([EmbeddedPreviewOrGallery](./preview-lab-gallery#embeddedprevieworgallery-js-wasmjs)) が `?iframe&previewId=...` を解釈し、指定された Preview だけを表示します

つまり **Gallery を Web にホストしておく** ことが前提です。
[Preview を収集して UI カタログを構築する](../tutorials/preview-ui-catalog) チュートリアルの手順で JS / WasmJS の Gallery をビルドし、任意の静的ホスティング (GitHub Pages など) に配置してください。

## セットアップ

<table>
<tbody>
<tr>
<th> `<compose-preview-lab-version>` </th>
<td> <ComposePreviewLabVersion /> </td>
</tr>
</tbody>
</table>

KDoc を生成するモジュール (= `@previewLab` を書くモジュール) の `build.gradle.kts` に Dokka Gradle Plugin (v2) と `dokka-plugin` を追加します。

```kts title="<module>/build.gradle.kts"
plugins {
    id("org.jetbrains.dokka") version "2.1.0"
    // highlight-next-line
    id("me.tbsten.compose.preview.lab") version "<compose-preview-lab-version>"
    id("org.jetbrains.kotlin.plugin.compose")
}

dependencies {
    // ⭐️ Dokka plugin を追加
    // highlight-next-line
    dokkaPlugin("me.tbsten.compose.preview.lab:dokka-plugin:<compose-preview-lab-version>")
}

dokka {
    pluginsConfiguration {
        // ⭐️ Gallery をホストしている URL
        // highlight-next-line
        composePreviewLab {
            baseUrl = "https://example.com/compose-preview-lab-gallery"
        }
    }
}
```

:::info `composePreviewLab { }` が見つからない場合
`dokka.pluginsConfiguration.composePreviewLab { }` は **`me.tbsten.compose.preview.lab` Gradle Plugin が Dokka Gradle Plugin の適用を検知して登録** しています。
`plugins { }` ブロックで両方のプラグインを同じモジュールに適用してください。

Dokka Gradle Plugin は **v2 モード (Dokka 2.0.0 以降のデフォルト)** が必要です。
:::

あとは `./gradlew dokkaGenerate` を実行するだけです。`@previewLab` を持つ API のページにだけ `<iframe>` が出力されます。

## タグの書式

```
@previewLab <previewId> [<width>x<height>]
```

| 書き方 | 出力 |
|---|---|
| `@previewLab MyButtonPreview` | 高さ固定 (`height: 720px`, `defaultHeight` で変更可) |
| `@previewLab MyButtonPreview 1280x680` | `aspect-ratio: 1280 / 680` で横幅に追従して拡縮 |
| `@previewLab MyButtonPreview not-a-size` | サイズ指定は無視され、高さ固定にフォールバック |

- 区切りは空白、`x` は小文字、幅・高さは正の整数のみ受け付けます
- `previewId` は URL エンコードされて `src` に埋め込まれます

## previewId の決め方

`previewId` には **Preview の id** を書きます。Preview の id は `@ComposePreviewLabOption(id = ...)` で決まり、デフォルトは `{{qualifiedName}}` (例: `com.example.ui.MyButtonPreview`) です。

```kotlin
// デフォルト: id = "com.example.ui.MyButtonPreview"
@Preview
@Composable
private fun MyButtonPreview() = PreviewLab { MyButton(onClick = { onEvent("onClick") }) }
```

```kotlin
// 推奨: パッケージ移動やリネームに強い安定した id を付ける
@ComposePreviewLabOption(id = "MyButtonPreview")
@Preview
@Composable
private fun MyButtonPreview() = PreviewLab { MyButton(onClick = { onEvent("onClick") }) }
```

:::tip 運用のコツ
- KDoc に書く id は **Gallery に公開済みの Preview の id** と一致させてください。一致しない id は Gallery 側で「見つからない」状態として表示されます
- `@ComposePreviewLabOption(id = "...")` で **短く安定した id** を付けておくと、パッケージを移動しても KDoc を直す必要がありません
- `{{qualifiedName}}` のままにする場合は、パッケージ名込みの id (`com.example.ui.MyButtonPreview`) を KDoc に書きます
- `private fun` な Preview でも問題ありません。Compiler Plugin はアクセス修飾子に関係なく Preview を収集します
:::

## 設定リファレンス

`dokka.pluginsConfiguration.composePreviewLab { }` で設定できる項目です。すべて省略可能です。

```kts title="build.gradle.kts"
dokka {
    pluginsConfiguration {
        composePreviewLab {
            // Gallery の URL。`?iframe&previewId=...` が付与される
            baseUrl = "https://example.com/compose-preview-lab-gallery"

            // iframe のインラインスタイル
            iframe {
                defaultHeight = "720px"          // WxH 指定がない場合の高さ
                border = "2px solid #d0d7de"     // 枠線
                borderRadius = "0"               // 角丸
                margin = "16px"                  // iframe 周囲の余白
                backgroundColor = "transparent"  // Gallery 読み込みまでの背景色
            }
        }
    }
}
```

| 項目 | デフォルト | 説明 |
|---|---|---|
| `baseUrl` | `compose-preview-lab-gallery` | Gallery の URL。絶対 URL (`https://…`, `/…`) はそのまま使われ、相対パスは **生成された Dokka サイトのルート** を基準に解決されます |
| `iframe.defaultHeight` | `720px` | `WxH` 指定がないときの `height` |
| `iframe.border` | `2px solid #d0d7de` | `border` |
| `iframe.borderRadius` | `0` | `border-radius` |
| `iframe.margin` | `16px` | iframe 周囲の余白 (ラッパー要素の `padding`) |
| `iframe.backgroundColor` | `transparent` | `background-color` |

:::info 相対 `baseUrl` の解決先
`baseUrl` がデフォルトのままなら、`dokkaGenerate` の出力 (`build/dokka/html/`) の `index.html` の隣に `compose-preview-lab-gallery/` ディレクトリとして Gallery を置くだけで動きます。
パッケージごとのサブディレクトリではなく、**サイトルートに 1 つ** 置けば全ページから参照されます。マルチモジュール (aggregation) 構成でも、集約後のサイトルート基準で解決されます。
:::

### CSS で上書きする

出力される要素には `preview-lab-embedded-container` (ラッパー `<div>`) と `preview-lab-embedded` (`<iframe>`) クラスが付いています。
DSL で足りないスタイルは Dokka の `customStyleSheets` から上書きできます (インラインスタイルより優先するため `!important` が必要です)。

```css
.preview-lab-embedded {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15) !important;
}
```

## マルチモジュール構成での注意

Dokka の aggregation (`dokka(project(":lib"))`) を使っている場合、`<iframe>` は **各モジュールの `dokkaGenerateModuleHtml` 時** に出力されます。
そのため `dokkaPlugin(...)` と `pluginsConfiguration.composePreviewLab { }` は **集約プロジェクトではなく、`@previewLab` を書く各モジュール** に設定してください。

## 出力される HTML

```html
<div class="preview-lab-embedded-container" style="padding: 16px;">
  <iframe class="preview-lab-embedded"
          src="https://example.com/compose-preview-lab-gallery/?iframe&previewId=MyButtonPreview"
          loading="lazy"
          allow="clipboard-read; clipboard-write"
          style="display: block; width: 100%; box-sizing: border-box; aspect-ratio: 1280 / 680; height: auto; border: 2px solid #d0d7de; border-radius: 0; background-color: transparent;">
  </iframe>
</div>
```
