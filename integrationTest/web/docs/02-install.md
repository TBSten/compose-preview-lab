---
title: Install
sidebar_position: 2
---

:::warning

このページは生成 AI によって自動生成されたページです。

:::

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# インストール

Compose Preview Lab を既存のプロジェクトに導入するための手順を説明します。ここでは主に **Kotlin Multiplatform** と **Android 単体プロジェクト** の両方をカバーします。

:::info インストールの全体像
- Gradle プラグイン（`me.tbsten.compose.preview.lab`）を追加する  
- KSP プラグインを有効化する  
- `core` ライブラリと `ksp-plugin` を依存関係に追加する  
- 必要に応じて `composePreviewLab { ... }` で挙動をカスタマイズする
:::

## 1. 前提条件

- Kotlin Multiplatform または Android プロジェクト  
- Jetpack Compose（Android）または Compose Multiplatform を利用していること  
- Gradle 7 以降（Kotlin DSL 推奨）

:::tip バージョンの確認
公式ドキュメントや [Maven Central の `me.tbsten.compose.preview.lab:core`](https://central.sonatype.com/artifact/me.tbsten.compose.preview.lab/core) を確認し、利用したいバージョンを決定します。
:::

## 2. Kotlin Multiplatform プロジェクトへの導入（推奨）

`build.gradle.kts`（KMP モジュール）に以下を追加します。

```kotlin title="build.gradle.kts"
plugins {
    // Kotlin Multiplatform / Compose
    kotlin("multiplatform")
    id("org.jetbrains.compose")

    // KSP プラグイン（Preview のメタデータ生成に必要）
    id("com.google.devtools.ksp") version "<ksp-version>"

    // Compose Preview Lab Gradle プラグイン
    id("me.tbsten.compose.preview.lab") version "<compose-preview-lab-version>"
}

kotlin {
    jvm()          // 必要なターゲットを追加
    androidTarget()
    js(IR) { browser() }
    // wasmJs { ... } など必要に応じて

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose UI
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.uiToolingPreview)

                // ✅ Compose Preview Lab 本体
                implementation("me.tbsten.compose.preview.lab:core:<compose-preview-lab-version>")
            }
        }
    }
}

dependencies {
    // ✅ Compose Preview Lab KSP プラグイン
    val composePreviewLabKsp = "me.tbsten.compose.preview.lab:ksp-plugin:<compose-preview-lab-version>"
    add("kspCommonMainMetadata", composePreviewLabKsp)
    add("kspAndroid", composePreviewLabKsp)
    add("kspJvm", composePreviewLabKsp)
    add("kspJs", composePreviewLabksp)
    add("kspWasmJs", composePreviewLabksp)
}
```

### 2-1. `composePreviewLab` の設定（任意）

`composePreviewLab` ブロックでコード生成の動作をカスタマイズできます。

```kotlin title="build.gradle.kts"
composePreviewLab {
    // 生成される PreviewList / PreviewAllList のパッケージ
    generatePackage = "com.example.preview"

    // 他モジュールから PreviewList を参照したい場合に true
    publicPreviewList = true

    // プロジェクトルートパス（デフォルトは project.rootDir）
    // projectRootPath = project.rootProject.projectDir.absolutePath

    // PreviewList の生成有無（通常は true のままで OK）
    generatePreviewList = true
    generatePreviewAll = true

    // Featured Files 機能を使う場合に有効化
    generateFeaturedFiles = true
}
```

:::tip Preview 一覧の場所
`composePreviewLab` を設定してビルドすると、`build/generated` 配下に `PreviewList` / `PreviewAllList` などのクラスが自動生成されます。  
`integrationTest/app` では `:app` モジュールの `composePreviewLab` 設定により、`me.tbsten.compose.preview.lab.sample` パッケージに `PreviewList` などが生成されています。
:::

## 3. Android 単体プロジェクトへの導入

Kotlin Multiplatform を利用していない純粋な Android プロジェクトでも利用できます（ただし、機能は一部制限されます）。

```kotlin title="app/build.gradle.kts"
plugins {
    id("com.android.application")
    kotlin("android")

    // KSP
    id("com.google.devtools.ksp") version "<ksp-version>"

    // Compose Preview Lab Gradle プラグイン
    id("me.tbsten.compose.preview.lab") version "<compose-preview-lab-version>"
}

android {
    // 通常の Android 設定（namespace, compileSdk など）
}

dependencies {
    implementation("androidx.compose.ui:ui:<compose-version>")
    implementation("androidx.compose.material3:material3:<version>")

    // ✅ Compose Preview Lab 本体
    implementation("me.tbsten.compose.preview.lab:core:<compose-preview-lab-version>")

    // ✅ KSP プラグイン
    ksp("me.tbsten.compose.preview.lab:ksp-plugin:<compose-preview-lab-version>")
}
```

:::warning Android 単体プロジェクトでの注意点
Android プロジェクト単体でも利用できますが、ブラウザ上での Preview 表示や一部の高度な機能は制限されます。  
可能であれば Kotlin Multiplatform 構成での利用を推奨します。
:::

## 4. PreviewLab を使った最初の Preview を作る

インストールが完了したら、`@Preview` を `PreviewLab` でラップして実際に動かしてみましょう。

```kotlin title="MyButtonPreview.kt"
@Preview
@Composable
fun MyButtonPreview() = PreviewLab {
    val text = fieldValue { StringField("text", "Click Me") }
    MyButton(
        text = text,
        onClick = { onEvent("MyButton.onClick") }
    )
}
```

<EmbeddedPreviewLab
  previewId="GetStarted"
/>

:::tip 次のステップ
- [Basic Architecture](./basic-architecture) で PreviewLab の内部構造を理解する  
- [Fields](./04-guides/02-fields/index.md) で Field の使い方を学ぶ  
- [Events](./04-guides/03-events.md) でイベントログ機能を試す  
:::

