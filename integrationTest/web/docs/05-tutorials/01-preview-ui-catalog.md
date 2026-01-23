---
title: "Preview を収集して UI カタログを構築する"
sidebar_position: 1
---

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# Preview を収集して UI カタログを構築する

このページでは、Compose Preview Lab を使って **UI カタログ（コンポーネント一覧）** を構築する方法を紹介します。

## ゴール

- プロジェクト内のコンポーネントを Preview 単位で整理する  
- PreviewLabGallery を使って「UI カタログ」画面を作る  
- 各コンポーネントのバリエーションや状態を手早く確認できるようにする

TODO イメージ画像

## 1. プロジェクトを用意して Compose Preview Lab をインストール

1. Compose Multiplatform プロジェクトを用意します。[Compose Multiplatform Wizard](https://terrakok.github.io/Compose-Multiplatform-Wizard/) が便利です。
2. [Install](../install/cmp) を参考にして Compose Preview Lab をプロジェクトにセットアップしてください。**4. (オプション) ターゲットの設定** をスキップせずに jvm, js, wasmJs の ターゲットを設定してください。
3. アプリ内にいくつか @Preview を用意してください。

## 2. Preview を収集する

Compose Preview Lab の KSP plugin, Gradle plugin ではデフォルトで KSP を用いた Preview の収集機能が有効になっています。

この収集機能は `モジュール名.PreviewList` という global 変数を作成し、そこから Preview にアクセスできるように収集します。

`kspKotlinJvm` Gradle task などでプロジェクトをビルドして PreviewList を生成しましょう。

```shell
./gradlew kspKotlinJvm
```

`モジュール/build/generated/ksp/` 内を見ると PreviewList が生成されていることがわかります。

TODO 画像

## 3. Gallery を実装

生成した PreviewList を UI として表示します。

プラットフォームごとに Gallery を表示するためのエントリーポイントとなる Composable・関数 が用意されています。

Gallery を表示する Composable または関数に PreviewList を渡すことで、UI カタログを表示できます。

```kt title="jvm"
fun main() {
    application {
        PreviewLabGalleryWindows(
            previewList = モジュール名.PreviewList,
        )
    }
}
```

```kt title="js, wasmJs"
fun main() {
    previewLabApplication(
        previewList = app.PreviewList,
    )
}
```

```kt title="それ以外"
ComposePreviewLabGallery(
    previewList = モジュール名.PreviewList,
)
```

通常通りアプリケーションを実行することでアプリケーション内に Preview 一覧が表示されます。

アプリケーションの実行方法は [Compose Multiplatform の公式ドキュメント](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-create-first-app.html#run-your-application) に詳しく記載があります。

TODO 画像

Gallery の詳しいオプションについては [PreviewLabGallery の Guide](../guides/preview-lab-gallery) を参照してください。

## 4. (オプション) マルチモジュールプロジェクト

KSP と Compiler plugin の制約により、PreviewList はモジュールごとに生成されます。
マルチモジュールプロジェクトでは、プロジェクト内すべての Preview を表示するには、以下のように手動で連結する必要があります。

```kt
// highlight-start
val allPreviewList = app.PreviewList +
  uiComponent.PreviewList +
  featureHome.PreviewList +
  featureMyPage.PreviewList
// highlight-end

application {
    PreviewLabGalleryWindows(
        previewList = allPreviewList,
    )
}
```
