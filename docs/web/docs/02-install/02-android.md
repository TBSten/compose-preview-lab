---
title: Android project
sidebar_position: 2
---

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';
import ComposePreviewLabVersion from "@site/src/components/ComposePreviewLabVersion"

# Android 単体プロジェクトへの導入

Kotlin Multiplatform を利用していない純粋な Android プロジェクトでも利用できます（ただし、機能は一部制限されます）。

より具体的な実践方法は [チュートリアル](../tutorials) を参照してください。

:::warning Android 単体プロジェクトでの注意点

Android プロジェクト単体でも利用できますが、**ブラウザ上での Preview 表示や一部の機能が制限**されます。 
 
例え最終的にリリースする必要のあるプラットフォームが Android だけであっても、開発体験向上のためにプロジェクトを Compose Multiplatform で構成してから [Compose Multiplatform 向けの Install](./cmp) を行うことを推奨しています。

すでに Android プロジェクトがある場合は [MahmoudRH/kmpify](https://github.com/MahmoudRH/kmpify) が役に立つかもしれません。

:::

:::info インストールの全体像
- Gradle プラグイン（`me.tbsten.compose.preview.lab`）を追加する
- KSP プラグインを有効化する
- `starter` ライブラリ（または個別モジュール）と `ksp-plugin` を依存関係に追加する
- 必要に応じて `composePreviewLab { ... }` で挙動をカスタマイズする
:::

## 0. プロジェクトを用意する

もしまだプロジェクトがなければ Android Studio の新規プロジェクトウィザード などを使ってプロジェクトを用意してください。

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
dependencies {
    // highlight-start
    implementation("me.tbsten.compose.preview.lab:starter:<compose-preview-lab-version>")
    ksp("me.tbsten.compose.preview.lab:ksp-plugin:<compose-preview-lab-version>")
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

## 4. PreviewLab を使った最初の Preview を作る

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

