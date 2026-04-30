---
title: "Build Settings"
sidebar_position: 7
---

# Build Settings

Compose Preview Lab の Gradle プラグインには、コード生成や Preview の収集方法を制御するための **ビルド設定 (Build Settings)** が用意されています。  
このページでは、`composePreviewLab { ... }` ブロックで設定できる主なオプションと、その使いどころを解説します。

:::info どこに書くのか？
`composePreviewLab { ... }` ブロックは **Compose Preview Lab Gradle プラグインを適用したモジュール** の `build.gradle.kts` に記述します。

```kotlin
plugins {
    id("me.tbsten.compose.preview.lab") version "<version>"
}

composePreviewLab {
    // ここに設定を追加する
}
```
:::

## 1. generatePackage

`FeaturedFileList` などの生成ファイルのパッケージ名を指定します。  
デフォルトはプロジェクト名から自動生成されます（例: `my-app` → `myApp`）。

```kotlin title="build.gradle.kts"
composePreviewLab {
    generatePackage = "com.example.preview"
}
```

:::tip いつ変更する？
- 生成されたファイルを特定のパッケージ配下にまとめたい  
という場合に、明示的にパッケージ名を指定します。
:::

## 2. generateFeaturedFiles

`.composepreviewlab/featured/` ディレクトリから **FeaturedFileList** を生成するかどうかを制御します。  
詳しくは [Featured Files](./featured-files) を参照してください。

デフォルトで false です。

```kotlin
composePreviewLab {
    // Featured Files 機能を使うときのみ true にする
    generateFeaturedFiles = true
}
```

:::tip Featured Files を使うときの典型パターン
- `.composepreviewlab/featured/` にグループごとのテキストファイルを作る  
- `generateFeaturedFiles = true` を有効にする  
- 生成された `FeaturedFileList` を `previewLabApplication(...)` に渡す  
:::

