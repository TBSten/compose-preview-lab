---
title: Collect Preview
sidebar_position: 4
---

import KDocLink from '@site/src/components/KDocLink';

# Collect Preview

Compose Preview Lab は、KSP (Kotlin Symbol Processing) プラグインを使用してプロジェクト内の `@Preview` アノテーションを自動的に収集し、`PreviewList` を生成します。

PreviewList は主に PreviewLabGallery に渡して、アプリ内の Preview を一覧表示するために使用します。

```kt
PreviewLabGallery(
    previewList = 
        // highlight-start
        // app モジュール内の Preview が app.PreviewList に収集されます
        app.PreviewList,
        // highlight-end
)
```

## セットアップ

### 1. Gradle プラグインと KSP の設定

[インストール](../install/cmp) に従って Gradle Plugin, KSP Plugin をセットアップします。

プロジェクトの `build.gradle.kts` に以下を追加します。

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import ComposePreviewLabVersion from "@site/src/components/ComposePreviewLabVersion"

<Tabs>
  <TabItem value="compose-multiplatform" label="Compose Multiplatform" default>

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
    // ⭐️ KSP を追加して @Preview を収集
    id("com.google.devtools.ksp") version "<ksp-version>"
    // ⭐️ Compose Preview Lab Gradle プラグインを追加
    id("me.tbsten.compose.preview.lab") version "<compose-preview-lab-version>"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("me.tbsten.compose.preview.lab:starter:<compose-preview-lab-version>")
        }
    }
}

dependencies {
    // ⭐️ Compose Preview Lab KSP プラグインを追加
    val composePreviewLabKspPlugin =
        "me.tbsten.compose.preview.lab:ksp-plugin:<compose-preview-lab-version>"
    add("kspCommonMainMetadata", composePreviewLabKspPlugin)
    // 各プラットフォーム
    add("kspAndroid", composePreviewLabKspPlugin)
    add("kspJvm", composePreviewLabKspPlugin)
    add("kspJs", composePreviewLabKspPlugin)
    add("kspWasmJs", composePreviewLabKspPlugin)
    // iOS ターゲット（必要な場合）
    // add("kspIosX64", composePreviewLabKspPlugin)
    // add("kspIosArm64", composePreviewLabKspPlugin)
    // add("kspIosSimulatorArm64", composePreviewLabKspPlugin)
}
```

  </TabItem>
  <TabItem value="android-jvm" label="Android / JVM">

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
    // ⭐️ KSP を追加して @Preview を収集
    id("com.google.devtools.ksp") version "<ksp-version>"
    // ⭐️ Compose Preview Lab Gradle プラグインを追加
    id("me.tbsten.compose.preview.lab") version "<compose-preview-lab-version>"
}

dependencies {
    implementation("me.tbsten.compose.preview.lab:starter:<compose-preview-lab-version>")
    ksp("me.tbsten.compose.preview.lab:ksp-plugin:<compose-preview-lab-version>")
}
```

  </TabItem>
</Tabs>

### 2. プロジェクトをビルドする

プロジェクトを実行すると、モジュール内の `@Preview` を検出し `<モジュール名>.PreviewList` が生成されます。

```kt title="<module>.PreviewList.kt"
package app

import me.tbsten.compose.preview.lab.CollectedPreview

internal object PreviewList : List<CollectedPreview> by listOf(
    CollectedPreview(...) { ... },
    ...
)
```

あとはこの PreviewList にアクセスすることで Preview のメタデータを取得することができます。

## カスタマイズ

Preview の収集機能は Gradle Plugin, KSP Plugin をセットアップすることですぐ利用できますが、生成されるコードを細かくカスタマイズするためのオプションが用意されています。

### 1. `generatePackage`

PreviewList などを生成するパッケージを指定します。デフォルトではモジュール名を `lowerCamelCase` にした文字列が設定されます。

```kt title="ui-lib/build.gradle.kts"
composePreviewLab {
    generatePackage = "ui_lib" // デフォルト: uiLib
}
```

```kt title="auto generated code"
package ui_lib

internal object PreviewList { ... }
```

### 2. `publicPreviewList`

生成される PreviewList はデフォルトでは `internal` でマークされるため、そのモジュール内でしか利用できませんが、`publicPreviewList` オプションに true を設定することで PreviewList を `public` にして 他のモジュールからも利用できるようにします。

```kt title="<module>/build.gradle.kts"
composePreviewLab {
    publicPreviewList = true
}
```

```kt title="auto generated code"
public object PreviewList { ... }
```

### 3. `generatePreviewList`

PreviewList を生成するかを設定します。デフォルトで `true` ですが、`false` にすることで PreviewList の生成を抑制することができます。

```kt title="<module>/build.gradle.kts"
composePreviewLab {
    // PreviewList が自動生成されなくなります
    publicPreviewList = false
}
```

## PreviewList のメタデータにアクセスする

PreviewList は主に PreviewLabGallery に渡してアプリ内の Preview をギャラリー表示するために使用されますが、PreviewList のプロパティにアクセスしてここの Preview やそのメタデータにアクセスできます。

```kt
@Preview
@Composable
private fun MyButtonPreview() = ...

// 個々の Preview に type safe にアクセスできます
app.PreviewList.MyButtonPreview

// MyButtonPreview の内部管理用 ID を取得します
app.PreviewList.MyButtonPreview.id

// MyButtonPreview を呼び出します
app.PreviewList.MyButtonPreview.content() 
```

取得できる情報については <KDocLink path="core/me.tbsten.compose.preview.lab/-preview-lab-preview/index.html">`PreviewLabPreview` の KDoc</KDocLink> を参照してください。

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
<tr> <th> `ignore` </th> <td> true を設定すると PreviewList からの収集を抑制します。 </td> </tr>
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

## マルチモジュールと `PreviewAllList`

`PreviewList` は モジュールごとに Preview を収集してリストアップします。
マルチモジュール構成になっているアプリでアプリ内全ての Preview を収集するためには以下の手順で各モジュールの PreviewList を手動で結合します。

これは現状安定してサポートされている唯一の方法です。

1. 各モジュールの PreviewList を **publicPreviewList** で 外部からアクセスできるようにします。
2. エントリーポイントのモジュール (app モジュール) で **各モジュールの PreviewList を `+` で連結** します。


```kts title="<module>/build.gradle.kts"
composePreviewLab {
    publicPreviewList = true
}
```

```kt title="app/src/jvmMain/com/myapp/PreviewGallery.kt"
val allPreviewList = app.PreviewList + feature1.PreviewList + feature2.PreviewList + ...

PreviewLagGallery(
    allPreviewList = allPreviewList,
)
```

この手間を省くために 現在実験的な API として `PreviewAllList` が用意されています。

`PreviewList` とは違い `PreviewAllList` は そのモジュール内だけでなく、依存するモジュール内全ての PreviewList を自動的に連結します。

```kt
app.PreviewList // appモジュール内の Preview 一覧
app.PreviewAllList // appモジュールとそれに依存する全てのモジュールの Preview 一覧
```

:::danger PreviewAllList 機能は現在実験的な機能です。利用には注意してください。

- jvm プラットフォームでのみ正しく動作することがテストされています。
- 特に js, wasmJs プラットフォームにおいて正しく PreviewList が収集されないことが確認されています。(おそらくこれは KSP または Kotlin Analysis API のバグによるものと思われます)

:::


## private Preview

Preview は通常他のファイルで利用することはないため private (ファイル外からアクセス禁止) にするのが慣習になっています。
しかし Compose Preview Lab を利用し Preview を収集して Gallery を表示する場合、この慣習により Gallery から Preview を参照できなくなってしまいます。

この問題に対処するため、Compose Preview Lab では Preview を直接参照するのではなく、Preview の内容をコピーした `__copied__<Preview名>` という Composable 関数を生成します。

```kt title="Preview がコピーされる"
@Preview
@Composable
private fun MyButtonPreview() = PreviewLab {
    MyButton(...)
}

// auto generated

// Compose Preview Lab は 
// private な MyButtonPreview ではなく 
// internal な __copied__MyButtonPreview を参照します
internal fun __copied__MyButtonPreview() = PreviewLab {
    MyButton(...)
}
```

これにより、Preview 内で private な宣言を利用している場合エラーになる可能性があります。

```kt
private val someText = "Some Text"
@Preview
@Composable
private fun SomeTextPreview() = PreviewLab {
    Text(text = someText)
}

// auto generated
internal fun __copied__SomeTextPreview() = PreviewLab {
    // highlight-next-line
    Text(text = someText) // ❌ can not reference error ! : someText
}
```

このような問題が発生しないように、極力 **Preview は対象の Composable と PreviewLab の API 以外の要素** を呼ばないよう心がける必要があります。

