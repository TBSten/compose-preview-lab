---
title: "Build a UI Catalog by Collecting Previews"
sidebar_position: 1
---

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# Preview を収集して UI カタログを構築する

このページでは、Compose Preview Lab を使って **UI カタログ（コンポーネント一覧）** を構築する方法を紹介します。

以下のチュートリアルに従って実装したサンプルリポジトリが Github にあるため、詰まったときは参考にしてください。

TODO サンプルリポジトリへのリンク

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

[Collect Preview の Guide](../guides/collect-preview) には Preview 収集のカスタマイズ方法が示されているため必要に応じて参照してください。
例えば PreviewList を生成するパッケージ名

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

ただしデフォルトでは PreviewList は internal で生成されるため、他のモジュールから参照できません。
PreviewList を public にするには、それぞれのモジュールの build.gradle.kts に以下を追加します。

```kotlin title="build.gradle.kts"
composePreviewLab {
    // highlight-start
    publicPreviewList = true
    // highlight-end
}
```

PreviewList を public にしたら以下のようにアプリモジュールなどで PreviewList を手動で連結します。

```kt title="PreviewGallery"
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

## 次のステップ

- [UI カタログで Review 体験を向上する](./improve-ui-review-by-ui-catalog) で PR レビューとの連携方法を学ぶ  
- [PreviewLabGallery の Guide](../guides/preview-lab-gallery) で Gallery の詳細なオプション設定を確認する  
- [Featured Files](../guides/featured-files) で Preview のグループ管理を詳しく知る  
- [Inspector Tab](../guides/inspector-tab) でカスタムタブを追加し、ドキュメントや設計情報を UI カタログ内に埋め込む
