---
title: "[TODO] 新規プロジェクトで Compose Preview Lab を導入する"
sidebar_position: 3
---

:::warning

このページは生成 AI によって自動生成されたページです。

:::

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# 新規プロジェクトで Compose Preview Lab を導入する

このページでは、**新しく作成した Compose Multiplatform プロジェクト** に Compose Preview Lab を最初から組み込む手順を説明します。

## ゴール

- 新規 KMP プロジェクトに Compose Preview Lab をセットアップする  
- 最初の `PreviewLab` 付き Preview を作成する  
- Web で確認できる UI カタログの足場を用意する

## 1. プロジェクトを作成する

JetBrains Template や IntelliJ の「Compose Multiplatform Application」テンプレートを使ってプロジェクトを作成します。

:::tip 推奨構成
- UI コードは `src/commonMain/kotlin` に配置  
- Android / Desktop / Web / iOS など、必要なターゲットだけを有効化  
:::

## 2. Gradle プラグインと依存関係を追加

`app`（または UI を含むモジュール）の `build.gradle.kts` に Compose Preview Lab を追加します。

```kotlin title="build.gradle.kts"
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")

    // Compose Preview Lab に必要なプラグイン
    id("com.google.devtools.ksp") version "<ksp-version>"
    id("me.tbsten.compose.preview.lab") version "<compose-preview-lab-version>"
}

kotlin {
    jvm()
    androidTarget()
    js(IR) { browser() }
    // 必要なら wasmJs() や iOS ターゲットも追加

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.uiToolingPreview)

                // ✅ Compose Preview Lab 本体
                implementation("me.tbsten.compose.preview.lab:starter:<compose-preview-lab-version>")
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

## 3. 最初の PreviewLab を書く

プロジェクトに簡単なコンポーネントと Preview を追加します。

```kotlin title="MyButton.kt"
@Composable
fun MyButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(onClick = onClick, enabled = enabled) {
        Text(text)
    }
}
```

```kotlin title="MyButtonPreview.kt"
@Preview
@Composable
fun MyButtonPreview() = PreviewLab {
    MyButton(
        // highlight-next-line
        text = fieldValue { StringField("text", "Click Me") },
        enabled = fieldValue { BooleanField("enabled", true) },
        onClick = { onEvent("MyButton.onClick") },
    )
}
```

<EmbeddedPreviewLab
  previewId="FieldQuickSummary"
  title="Field Quick Summary"
/>

:::info PreviewList の生成を確認する
`./gradlew :app:build` を実行すると、`build/generated/` 配下に `PreviewList` や `PreviewAllList` が生成されます。  
これらは Web ギャラリーやデスクトップアプリから参照され、UI カタログを構成する基盤となります。
:::

## 4. Web ギャラリーを用意する（任意）

Compose Multiplatform プロジェクトでは、JS/WasmJS ターゲットを使って Web 上に PreviewLab ギャラリーを構築できます。

```kotlin title="main() の例"
fun main() = previewLabApplication(
    previewList = app.PreviewList,
) {
    // PreviewLabGallery を表示する
}
```

このギャラリーを静的ホスティング（GitHub Pages など）にデプロイすることで、  
チームメンバーがブラウザから UI カタログを閲覧できるようになります。

## 5. 次のステップ

- さらに多くのコンポーネントに `PreviewLab` を適用し、Field と Events を増やす  
- [All Builtin Fields](../guides/fields/all-builtin-fields) で利用可能な Field の一覧を確認する  
- [Featured Files](../guides/featured-files) を使って重要な Preview をグループ化する  
- [UI カタログを構築する](./preview-ui-catalog) チュートリアルに進む  


