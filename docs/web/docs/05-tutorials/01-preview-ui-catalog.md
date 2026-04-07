---
title: "Build a UI Catalog by Collecting Previews"
sidebar_position: 1
---

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# Preview を収集して UI カタログを構築する

このページでは、Compose Preview Lab を使って **UI カタログ（コンポーネント一覧）** を構築する方法を紹介します。

## ゴール

- プロジェクト内のコンポーネントを Preview 単位で整理する  
- PreviewLabGallery を使って「UI カタログ」画面を作る  
- 各コンポーネントのバリエーションや状態を手早く確認できるようにする

## 1. プロジェクトを用意して Compose Preview Lab をインストール

1. Compose Multiplatform プロジェクトを用意します。[Compose Multiplatform Wizard](https://terrakok.github.io/Compose-Multiplatform-Wizard/) が便利です。
2. [Install](../install/cmp) を参考にして Compose Preview Lab をプロジェクトにセットアップしてください。**5. (オプション) ターゲットの設定** をスキップせずに jvm, js, wasmJs の ターゲットを設定してください。
3. アプリ内にいくつか @Preview を用意してください。

## 2. Preview の収集ポイントを宣言する

`@CollectPreviews` と `collectModulePreviews()` を使って Preview の収集ポイントを宣言します。

```kt title="src/commonMain/kotlin/Previews.kt"
package app

import me.tbsten.compose.preview.lab.CollectPreviews
import me.tbsten.compose.preview.lab.collectModulePreviews

@CollectPreviews
val appPreviews by collectModulePreviews()
```

プロジェクトをビルドすると、Compiler Plugin がモジュール内の `@Preview` 関数を検出して `appPreviews` に自動的に注入します。

[Collect Preview の Guide](../guides/collect-preview) には Preview 収集のカスタマイズ方法が示されているため必要に応じて参照してください。

## 3. Gallery を実装

収集した Preview を UI として表示します。

プラットフォームごとに Gallery を表示するためのエントリーポイントとなる Composable・関数 が用意されています。

```kt title="jvm"
fun main() {
    application {
        PreviewLabGalleryWindows(
            previewList = appPreviews,
        )
    }
}
```

```kt title="js, wasmJs"
fun main() {
    ComposeViewport(document.body!!) {
        EmbeddedPreviewOrGallery(
            previewList = appPreviews.toList(),
        )
    }
}
```

```kt title="それ以外"
PreviewLabGallery(
    previewList = appPreviews,
)
```

通常通りアプリケーションを実行することでアプリケーション内に Preview 一覧が表示されます。

Gallery の詳しいオプションについては [PreviewLabGallery の Guide](../guides/preview-lab-gallery) を参照してください。

## 4. (オプション) マルチモジュールプロジェクト

`collectModulePreviews()` はモジュールごとに Preview を収集します。
マルチモジュールプロジェクトでは、`collectAllModulePreviews()` を使うことで依存モジュールの Preview を自動的に含めることができます。

### Step 1: 各依存モジュールで `@CollectPreviews` を宣言し、Gradle で export 設定

```kt title="uiLib/src/commonMain/kotlin/Previews.kt"
package uiLib

@CollectPreviews
val uiLibPreviews by collectModulePreviews()
```

```kotlin title="uiLib/build.gradle.kts"
composePreviewLab {
    // highlight-start
    collectPreviewsExport = "uiLib.uiLibPreviews"
    // highlight-end
}
```

### Step 2: アプリモジュールで `collectAllModulePreviews()` を使用

```kt title="app/src/commonMain/kotlin/Previews.kt"
package app

@CollectPreviews
val appPreviews by collectAllModulePreviews()
// ↑ app 自身 + uiLib の Preview が自動的に含まれる
```

## 次のステップ

- [UI カタログで Review 体験を向上する](./improve-ui-review-by-ui-catalog) で PR レビューとの連携方法を学ぶ  
- [PreviewLabGallery の Guide](../guides/preview-lab-gallery) で Gallery の詳細なオプション設定を確認する  
- [Featured Files](../guides/featured-files) で Preview のグループ管理を詳しく知る  
- [Inspector Tab](../guides/inspector-tab) でカスタムタブを追加し、ドキュメントや設計情報を UI カタログ内に埋め込む
