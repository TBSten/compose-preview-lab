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
- `starter` ライブラリ（または個別モジュール）を依存関係に追加する
- `collectModulePreviews()` で Preview の収集ポイントを宣言する
- 必要に応じて `composePreviewLab { ... }` で挙動をカスタマイズする
:::

## 0. プロジェクトを用意する

もしまだプロジェクトがなければ Android Studio の新規プロジェクトウィザード などを使ってプロジェクトを用意してください。

## 1. Gradle Plugin の設定

`build.gradle.kts` に Gradle プラグインを追加します。

:::info プラグインの適用順序（Kotlin バージョンによって異なります）
- **Kotlin 2.3.0 以降**: 適用順序は問いません。Gradle プラグインが自動で `-Xcompiler-plugin-order` を注入します。
- **Kotlin 2.1.20 / 2.2.x**: `me.tbsten.compose.preview.lab` を Compose Compiler より**前**に記述してください（順序が逆の場合は Gradle プラグインが早期エラーで通知します）。
:::

<table>
<tbody>
  <tr>
    <th><code>&lt;compose-preview-lab-version&gt;</code></th>
    <td><ComposePreviewLabVersion /></td>
  </tr>
</tbody>
</table>

```kotlin title="build.gradle.kts"
plugins {
    // highlight-start
    // ⭐️ Compose Preview Lab
    // Kotlin 2.3+: 適用順序は問わない (Gradle plugin が -Xcompiler-plugin-order を自動注入)
    // Kotlin 2.1.20 / 2.2.x: composeCompiler より前に記述
    id("me.tbsten.compose.preview.lab") version "<compose-preview-lab-version>"
    // highlight-end
    id("org.jetbrains.kotlin.plugin.compose")
}
```

## 2. 依存関係の追加

`starter` ライブラリを依存関係に追加します。

<table>
<tbody>
  <tr>
    <th><code>&lt;compose-preview-lab-version&gt;</code></th>
    <td><ComposePreviewLabVersion /></td>
  </tr>
</tbody>
</table>

```kotlin title="build.gradle.kts"
dependencies {
    // highlight-start
    implementation("me.tbsten.compose.preview.lab:starter:<compose-preview-lab-version>")
    // highlight-end
}
```

## 3. Preview の収集ポイントを宣言する

`val` プロパティを作成します。Compiler Plugin がモジュール内の `@Preview` 関数を検出して `appPreviews` に自動的に注入します。

```kotlin title="src/main/kotlin/Previews.kt"
package app

import me.tbsten.compose.preview.lab.collectModulePreviews

// highlight-start
val appPreviews by collectModulePreviews()
// highlight-end
```

:::info Extension モジュール

他のライブラリ（kotlinx-datetime、Navigation など）を使用している場合、対応する **Extension モジュール** の依存関係を追加することで、それらの型に対応した Field を利用できます。

Extension モジュールは `starter` には含まれていないため、必要に応じて個別に依存関係を追加してください。

詳しくは [Extensions](../guides/extensions) を参照してください。

:::

## 4. 必要に応じてオプションの設定を検討

1〜3 のセクションで説明した内容で最低限のセットアップは整っています。

デフォルトでは一般的なユースケースをカバーするために適切なデフォルト設定になっていますが、`composePreviewLab { }` ブロックでコード生成の動作をカスタマイズできます。  

詳しくは [Build Settings](../guides/build-settings) を参照してください。

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

