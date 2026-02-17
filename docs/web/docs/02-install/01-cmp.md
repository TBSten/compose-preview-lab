---
title: Compose Multiplatform project
sidebar_position: 1
---

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';
import ComposePreviewLabVersion from "@site/src/components/ComposePreviewLabVersion"

# Compose Multiplatform プロジェクトへの導入（推奨）

Compose Preview Lab を Compose Multiplatform プロジェクトに導入する手順を説明します。

より具体的な実践方法は [チュートリアル](../tutorials) を参照してください。

:::info インストールの全体像
- Gradle プラグイン（`me.tbsten.compose.preview.lab`）を追加する
- KSP プラグインを有効化する
- `starter` ライブラリ（または個別モジュール）と `ksp-plugin` を依存関係に追加する
- 必要に応じて `composePreviewLab { ... }` で挙動をカスタマイズする
:::

## 0. プロジェクトを用意する

もしまだプロジェクトがなければ、Compose Multiplatform プロジェクトを用意してください。  
Compose Multiplatform プロジェクトを新しく作成する場合は、[Compose Multiplatform Wizard](https://compose.multiplatform.org/wizard) を使うのがおすすめです。  
すでにある Android プロジェクトを移行したい場合は KMP 化してください。

既存の Android プロジェクトを KMP に移行する場合は [MahmoudRH/kmpify](https://github.com/MahmoudRH/kmpify) が役に立つかもしれません。

## 1. Gradle と KSP Plugin の設定

`build.gradle.kts` に Gradle プラグインと KSP プラグインを追加します。

<table>
  <tr>
    <th><code>&lt;ksp-version&gt;</code></th>
    <th><a href="https://github.com/google/ksp/releases">KSP Release notes</a></th>
  </tr>
  <tr>
    <th><code>&lt;compose-preview-lab-version&gt;</code></th>
    <td><ComposePreviewLabVersion /></td>
  </tr>
</table>

```kotlin title="build.gradle.kts"
plugins {
    // Compose Multiplatform
    kotlin("multiplatform")
    id("org.jetbrains.compose")

    // highlight-start
    id("com.google.devtools.ksp") version "<ksp-version>"
    id("me.tbsten.compose.preview.lab") version "<compose-preview-lab-version>"
    // highlight-end
}
```

## 2. 依存関係の追加

`starter` ライブラリと KSP プラグインを依存関係に追加します。

<table>
  <tr>
    <th><code>&lt;compose-preview-lab-version&gt;</code></th>
    <td><ComposePreviewLabVersion /></td>
  </tr>
</table>

```kotlin title="build.gradle.kts"
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

                // highlight-start
                // Compose Preview Lab starter（すべてのコアモジュールを含む）
                implementation("me.tbsten.compose.preview.lab:starter:<compose-preview-lab-version>")
                // highlight-end
            }
        }
    }
}

dependencies {
    // highlight-start
    // Compose Preview Lab KSP プラグイン
    val composePreviewLabKsp = "me.tbsten.compose.preview.lab:ksp-plugin:<compose-preview-lab-version>"
    add("kspCommonMainMetadata", composePreviewLabKsp)
    add("kspAndroid", composePreviewLabKsp)
    add("kspJvm", composePreviewLabKsp)
    add("kspJs", composePreviewLabKsp)
    add("kspWasmJs", composePreviewLabKsp)
    // highlight-end
}
```

:::info Extension モジュール

他のライブラリ（kotlinx-datetime、Navigation など）を使用している場合、対応する **Extension モジュール** の依存関係を追加することで、それらの型に対応した Field を利用できます。

Extension モジュールは `starter` には含まれていないため、必要に応じて個別に依存関係を追加してください。

詳しくは [Extensions](../guides/extensions) を参照してください。

:::

## 3. 必要に応じてオプションの設定を検討

1, 2 のセクションで説明した内容で最低限のセットアップは整っています。

デフォルトでは一般的なユースケースをカバーするために適切なデフォルト設定になっていますが、`composePreviewLab { }` ブロックでコード生成の動作をカスタマイズできます。  

詳しくは [Build Settings](../guides/build-settings) を参照してください。

## 4. (オプション) ターゲットの設定

通常通り `kotlin { }` ブロック内でアプリで利用するターゲットを宣言します。

```kt
kotlin {
    androidTarget { ... }

    // ...
}
```

Compose Preview Lab を Web ブラウザから閲覧したい場合など、開発体験向上のためだけにアプリのターゲットとは別の追加のターゲットが必要な場合、設定を拡張関数に切り分けておくことで `build.gradle.kts` の可読性を向上させることができます。

また、開発用の sourceSet を作成することで、本番アプリではない実装の expect/actual を何度も書かなくてよくなります。

例えば、あなたのアプリが android, iOS のみをサポートするが、jvm, js, wasm を開発ツールとして利用したい場合、以下のようにビルドスクリプトを記述すると良いでしょう。

```kt
fun Project.configureForPreviewLab() {
    kotlin {
        // TODO android, iOS のターゲット定義

        jvm()
        js(IR) { browser() }
        wasmJs { browser() }

        sourceSets {
            val devToolsMain by creating {
                dependsOn(commonMain.get())
            }
            listOf(jvmMain, jsMain, wasmJsMain).forEach {
                it.get().dependsOn(devToolsMain)
            }
        }
    }
}
configureForPreviewLab()
```

詳しくは <a href="https://kotlinlang.org/docs/multiplatform.html" target="_blank" rel="noopener noreferrer">Kotlin Multiplatform のドキュメント</a> を参照してください。

## 5. PreviewLab を使った最初の Preview を作る

インストールが完了したら、あなたのプロジェクトの `@Preview` を `PreviewLab` でラップして実際に動かしてみましょう。

```kotlin title="MyButtonPreview.kt"
@Preview
@Composable
// highlight-next-line
fun MyButtonPreview() = PreviewLab {
    // highlight-next-line
    val text = fieldValue { StringField("text", "Click Me") }
    MyButton(
        text = text,
        // highlight-next-line
        onClick = { onEvent("MyButton.onClick") }
    )
}
```

<EmbeddedPreviewLab
  previewId="GetStarted"
  title="Get Started"
/>

## 次のステップ

- [Tutorials](../tutorials) でより実践的な導入方法を学ぶ  
- [Build Settings](../guides/build-settings) でコード生成やプラグインの詳細設定を確認する  
- [Basic Architecture](../basic-architecture) で PreviewLab の内部構造を理解する  
- [Fields](../guides/fields/overview) で Field の使い方を学ぶ  
- [Events](../guides/events) でイベントログ機能を試す  
