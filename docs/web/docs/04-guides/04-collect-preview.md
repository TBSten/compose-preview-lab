---
title: Collect Preview
sidebar_position: 4
---

import KDocLink from '@site/src/components/KDocLink';

# Collect Preview

Compose Preview Lab は、Compiler Plugin を使用してプロジェクト内の `@Preview` アノテーションを自動的に収集します。

`collectModulePreviews()` / `collectAllModulePreviews()` を使って、収集ポイントを宣言します。

```kt
val appPreviews by collectModulePreviews()

PreviewLabGallery(
    previewList = appPreviews,
)
```

## セットアップ

### 1. Gradle Plugin の設定

[インストール](../install/cmp) に従って Gradle Plugin をセットアップします。

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import ComposePreviewLabVersion from "@site/src/components/ComposePreviewLabVersion"

<table>
<tbody>
<tr>
<th> `<compose-preview-lab-version>` </th>
<td> <ComposePreviewLabVersion /> </td>
</tr>
</tbody>
</table>

```kts title="<module>/build.gradle.kts"
plugins {
    // ⭐️ Compose Preview Lab
    // Kotlin 2.3+: 適用順序は問わない (Gradle plugin が -Xcompiler-plugin-order を自動注入)
    // Kotlin 2.1.20 / 2.2.x: composeCompiler より前に記述
    id("me.tbsten.compose.preview.lab") version "<compose-preview-lab-version>"
    // Compose Compiler
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("me.tbsten.compose.preview.lab:starter:<compose-preview-lab-version>")
        }
    }
}
```

### 2. Preview の収集ポイントを宣言する

`val` プロパティを `commonMain` に作成します。

```kt title="src/commonMain/kotlin/Previews.kt"
package app

import me.tbsten.compose.preview.lab.collectModulePreviews

val appPreviews by collectModulePreviews()
```

プロジェクトをビルドすると、Compiler Plugin がモジュール内の `@Preview` 関数を検出し、`appPreviews` に自動的に注入します。

## `collectModulePreviews()` vs `collectAllModulePreviews()`

| 関数 | 収集範囲 |
|---|---|
| `collectModulePreviews()` | **このモジュールの** `@Preview` 関数のみ |
| `collectAllModulePreviews()` | このモジュール **+ 依存モジュール** の `@Preview` 関数 |

### `collectAllModulePreviews()` のセットアップ

各依存モジュール側で `val x by collectModulePreviews()` を宣言しておけば、上位モジュールの `collectAllModulePreviews()` がその property を自動的に発見・集約します。

```kt title="uiLib/src/commonMain/kotlin/Previews.kt"
package uiLib

val uiLibPreviews by collectModulePreviews()
```

```kt title="app/src/commonMain/kotlin/Previews.kt"
package app

val appPreviews by collectAllModulePreviews()
// ↑ app 自身の Preview + uiLib の Preview が含まれる
```

依存モジュールが `collectAllModulePreviews()` を使っている場合も同様に集約されます。たとえば `app(all) → ui(all) → core(single)` のように複数段の依存関係でも、最上位の `app` から全ての Preview を取得できます。

:::warning JVM 限定
`collectAllModulePreviews()` の cross-module 自動集約は **JVM ターゲットでのみ動作します**。JS / Wasm JS / iOS など KLIB ベースの platform では `collectAllModulePreviews()` は **そのモジュール内の Preview のみ** を返します (Compiler Plugin の hint 関数が KLIB の signature 一意性制約と衝突するため)。

これらの platform では Gallery を提供したいモジュールに直接 `collectModulePreviews()` を書くか、JVM ビルドのみで Gallery を構成してください。
:::

## `@ComposePreviewLabOption`

`ComposePreviewLabOption` アノテーションを利用して、Preview 収集時のオプションを指定できます。

```kt
@ComposePreviewLabOption(
    id = "my_button_preview",
    displayName = "Basic Button Example",
    ignore = false,
)
@Preview
@Composable
private fun MyButtonPreview() = ...
```

現在サポートされているのは以下の3つです。

<table> 
<tbody>
<tr> <th> `id` </th> <td> 内部管理用のIDを指定します。 </td> </tr>
<tr> <th> `displayName` </th> <td> PreviewLabGallery で表示する名前を決定します。placeholder, `.` による階層化をサポートしています。（下記参照） </td> </tr>
<tr> <th> `ignore` </th> <td> true を設定すると収集を抑制します。 </td> </tr>
</tbody>
</table>

<details>
<summary>displayName の placeholder</summary>

`{{qualifiedName}}` `{{simpleName}}` などを displayName の文字列内に含めることでその部分を Composable の変数で置換します。

例えば com.sample.MyButtonPreview に `displayName = "{{simpleName}}_1"` オプションを指定すると、displayName は `MyButtonPreview_1` のように設定されます。

</details>

<details>
<summary>displayName の `.` による階層化</summary>

displayName 内の `.` は特別な意味を持ちます。

`.` で区切られたセグメントはディレクトリのように認識されます。

import dotHierarchySample from './dot-hierarchy-sample.png';

<img src={dotHierarchySample} alt="Dot Hierarchy Sample" width="300" />

</details>


詳しくは <KDocLink path="annotation/me.tbsten.compose.preview.lab/-compose-preview-lab-option/index.html">`ComposePreviewLabOption` の KDoc</KDocLink> も参照してください。

## private Preview

`@Preview` 関数は通常他のファイルから利用しないため `private` にするのが一般的です。

Compose Preview Lab の Compiler Plugin は、`@Preview` が付与された `private` 関数の visibility を自動的に `internal` に変更します。これにより、収集された Preview リストから各 Preview 関数を呼び出すことが可能になります。

ソースコード上の `private` 宣言は変更されません。Compiler Plugin が FIR フェーズで内部的に visibility を変換するため、ユーザーのコードには影響しません。

## ライブラリを公開するときの注意

`collectModulePreviews()` / `collectAllModulePreviews()` を宣言したモジュールには、Compiler Plugin が `me.tbsten.compose.preview.lab.exports` パッケージに `previewLabExport` という **public な hint 関数** を生成します。これは下流モジュールの `collectAllModulePreviews()` が依存先の Preview を自動発見するためのマーカー関数で、ランタイムに呼び出されることはありません。

そのため、ライブラリとして外部に公開するモジュールに `collectModulePreviews()` / `collectAllModulePreviews()` を含める場合は次の点に注意してください。

- 成果物 (jar / klib) に `me.tbsten.compose.preview.lab.exports.previewLabExport` という public な top-level 関数が含まれます
- ライブラリの consumer 側で `collectAllModulePreviews()` を使うと、そのライブラリの Preview が自動的に集約されます

`@Preview` を含まない通常のライブラリでは `collectModulePreviews()` / `collectAllModulePreviews()` を宣言する必要はありません。
