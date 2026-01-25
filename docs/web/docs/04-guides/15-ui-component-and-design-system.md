---
title: "[TODO] UI Component and Design System"
sidebar_position: 15
---

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# UI Component and Design System

Compose Preview Lab は (Compose material 3 などを利用せずに) 独自の UI コンポーネント・デザインシステムが構築されています。
Field の UI 自作など、高度なカスタマイズの際に利用する必要があるかもしれません。このガイドでは Compose Preview Lab のデザインシステムを利用する方法を紹介します。

## アーティファクト

Compose Preview Lab の デザインシステムは `me.tbsten.compose.preview.lab:ui` maven アーティファクトとして公開されています。
プロジェクト内で利用する場合は `starter` アーティファクトには含まれていないため、利用する必要があります。

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
  <TabItem value="cmp" label="Compose Multiplatform">
    
    ```kts
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("me.tbsten.compose.preview.lab:ui:<compose-preview-lab-version>")
            }
        }
    }
    ```
    
  </TabItem>
  <TabItem value="android" label="Android">

    ```kts
    dependencies {
        implementation("me.tbsten.compose.preview.lab:ui:<compose-preview-lab-version>")
    }
    ```

  </TabItem>
</Tabs>

## Compose Preview Lab の UI コンポーネント一覧を確認する

Compose Preview Lab のコンポーネント・デザインシステムのギャラリーが Compose Preview Lab 自体を利用して公開されています。以下のリンクからアクセスできます。

TODO

コンポーネントごとの詳細な情報は以下のギャラリーや各コンポーネントの KDoc を参照してください。

## 命名規則

Compose Preview Lab の UI コンポーネントは `PreviewLab~` で始まる名前で公開されています。

## OptIn

基本的に全ての API が `@UiComposePreviewLabApi` が付与されており、`OptIn` が必要です。
これはカスタマイズを必要としないユーザにとってこれらはノイズであり、不用意に呼び出されることを防ぐためです。

<Tabs>
  <TabItem value="component" label="コンポーネントレベルで OptIn する">

```kt
@OptIn(UiComposePreviewLabApi::class)
class MyField(...) : PreviewLabField(...) {
    @Composable
    fun Content() {
        PreviewLabText("MyField")
    }
}
```

  </TabItem>
  <TabItem value="module" label="モジュールレベルで OptIn する">

```kt title="build.gradle.kts"
kotlin {
    optIn.addAll("me.tbsten.compose.preview.lab.UiComposePreviewLabApi")
}

```

  </TabItem>
</Tabs>

## テーマカスタマイズ

---

Comming soon....
