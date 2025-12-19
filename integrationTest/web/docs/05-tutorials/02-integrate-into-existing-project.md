---
title: "[TODO] 既存のプロジェクトに Compose Preview Lab を導入する"
sidebar_position: 2
---

:::warning

このページは生成 AI によって自動生成されたページです。

:::

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# 既存のプロジェクトに Compose Preview Lab を導入する

既存の Compose プロジェクトに Compose Preview Lab を後から追加する手順を説明します。  
このガイドでは、できるだけ既存コードを壊さず、段階的に導入する方法にフォーカスします。

## ゴール

- 既存の `@Preview` を少しずつ `PreviewLab` に移行する
- Gradle プラグインと KSP を正しく設定する
- Web 上で動く UI カタログを構築するための足場を用意する

## 1. Gradle プラグインを追加する

まずは、Preview を集約するための Gradle プラグインと KSP を導入します。

```kotlin title="build.gradle.kts（既存モジュール）"
plugins {
    // 既存の設定
    kotlin("multiplatform") // または kotlin("android")
    id("org.jetbrains.compose")

    // 追加
    id("com.google.devtools.ksp") version "<ksp-version>"
    id("me.tbsten.compose.preview.lab") version "<compose-preview-lab-version>"
}

kotlin {
    // 既存のターゲット定義...

    sourceSets {
        val commonMain by getting {
            dependencies {
                // 既存の依存関係...

                // Compose Preview Lab 本体
                implementation("me.tbsten.compose.preview.lab:core:<compose-preview-lab-version>")
            }
        }
    }
}

dependencies {
    val composePreviewLabKsp = "me.tbsten.compose.preview.lab:ksp-plugin:<compose-preview-lab-version>"
    add("kspCommonMainMetadata", composePreviewLabKsp)
    add("kspJvm", composePreviewLabKsp)
    add("kspAndroid", composePreviewLabKsp)
    add("kspJs", composePreviewLabKsp)
    add("kspWasmJs", composePreviewLabKsp)
}
```

:::tip まずは 1 モジュールから
マルチモジュール構成の場合、まずは UI を多く含む 1 モジュール（例: `:app` や `:ui`）にだけ導入し、  
PreviewLab の挙動に慣れてから他モジュールにも広げるのがおすすめです。
:::

## 2. 既存の @Preview を PreviewLab に移行する

次に、既存の `@Preview` を 1 つ選んで `PreviewLab` で包んでみます。

```kotlin title="Before: 通常の Preview"
@Preview
@Composable
fun MyScreenPreview() {
    MyScreen(
        text = "Hello",
        enabled = true,
        onClick = {},
    )
}
```

```kotlin title="After: PreviewLab を使った Preview"
@Preview
@Composable
fun MyScreenPreview() = PreviewLab {
    MyScreen(
        text = fieldValue { StringField("text", "Hello") },
        enabled = fieldValue { BooleanField("enabled", true) },
        onClick = { onEvent("MyScreen.onClick") },
    )
}
```

<EmbeddedPreviewLab
  previewId="GetStarted"
  title="Get Started"
/>

:::info 段階的な移行を推奨
- すべての Preview を一度に変換する必要はありません  
- まずは 1〜2 個の代表的な画面から始め、徐々に Field や Events を追加していくと安全です
:::

## 3. Web ギャラリーのビルドを設定する

Compose Preview Lab の強みの 1 つは、**Web 上の UI カタログ** を簡単に構築できることです。  
`integrationTest/app` では、次のようなタスクで JS ビルド成果物を `compose-preview-lab-gallery` にコピーしています。

```kotlin title="integrationTest/app/build.gradle.kts"
val cleanPreviewLabGallery by tasks.registering(Delete::class) {
    delete(layout.buildDirectory.dir("compose-preview-lab-gallery"))
}

val buildDevelopmentPreviewLabGallery by tasks.registering(Copy::class) {
    dependsOn("jsBrowserDevelopmentExecutableDistribution")
    dependsOn(cleanPreviewLabGallery)

    from(layout.buildDirectory.dir("dist/js/developmentExecutable"))
    into(layout.buildDirectory.dir("web-static-content/compose-preview-lab-gallery"))
}
```

このディレクトリを静的コンテンツとしてホスティングすることで、ドキュメントサイトや CI/CD から PreviewLab ギャラリーにアクセスできます。

## 4. CI / GitHub Pages への統合（概要）

詳細なセットアップはプロジェクト環境に依存しますが、典型的なステップは次の通りです。

1. CI（GitHub Actions など）で JS/WasmJS ターゲットをビルド  
2. `compose-preview-lab-gallery` ディレクトリをアーティファクトとして公開、あるいは GitHub Pages にデプロイ  
3. Pull Request 単位でプレビュー URL を発行し、レビュー担当者がブラウザで UI カタログを確認できるようにする

:::tip 関連ドキュメント
- [Build Settings](../guides/build-settings) – `composePreviewLab` の詳細な設定  
- [Featured Files](../guides/featured-files) – 重要な Preview をグループ化する方法  
- [UI カタログで Review 体験を向上する](./improve-ui-review-by-ui-catalog) – PR レビューとの統合例  
:::

