---
title: Build Settings
sidebar_position: 7
---

:::warning

このページは生成 AI によって自動生成されたページです。

:::

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
    // ここに設定を書く
}
```
:::

## 1. generatePackage

生成される `PreviewList` / `PreviewAllList` のパッケージ名を指定します。

```kotlin title="build.gradle.kts"
composePreviewLab {
    // デフォルトはプロジェクト名から自動生成される (e.g. my-app -> myApp)
    generatePackage = "com.example.preview"
}
```

:::tip いつ変更する？
- 複数モジュールから共通の PreviewList をインポートしたい  
- 生成されたクラスを特定のパッケージ配下にまとめたい  
という場合に、明示的にパッケージ名を指定します。
:::

## 2. publicPreviewList

生成される `PreviewList` / `PreviewAllList` の可視性を制御します。

```kotlin
composePreviewLab {
    // true: 他モジュールからも参照できる public な PreviewList を生成
    // false: モジュール内だけで使う internal な PreviewList を生成 (デフォルト)
    publicPreviewList = true
}
```

複数モジュールから Preview を集約して 1 つの UI カタログを作りたい場合は `true` にするのがおすすめです。

## 3. projectRootPath

生成される Preview のメタデータに含まれるファイルパス解決に利用される、プロジェクトルートパスを指定します。

```kotlin
composePreviewLab {
    // 通常はデフォルト (rootProject.projectDir) のままで問題ありません
    projectRootPath = project.rootProject.projectDir.absolutePath
}
```

この値は、外部エディタ連携（`OpenFileHandler` など）でファイルパスを扱うときに使用されます。

## 4. generatePreviewList / generatePreviewAllList

Preview リスト生成そのものを制御するフラグです。

```kotlin
composePreviewLab {
    // モジュール内の @Preview を集約した PreviewList を生成するか
    generatePreviewList = true

    // 依存関係を含めてすべての Preview を集約する PreviewAllList を生成するか
    generatePreviewAllList = true
}
```

通常はどちらも `true` のままで問題ありません。  
「このモジュールでは PreviewList を生成したくない」といった特殊なケースのみ、`false` に変更します。

## 5. generateFeaturedFiles

`.composepreviewlab/featured/` ディレクトリから **FeaturedFileList** を生成するかどうかを制御します。  
詳しくは [Featured Files](./featured-files) を参照してください。

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

## 6. サンプル: integrationTest/app の設定

`integrationTest/app` モジュールで実際に使われている設定は以下のようになっています。

```kotlin title="integrationTest/app/build.gradle.kts"
plugins {
    // 省略...
    id("com.google.devtools.ksp")
    id("me.tbsten.compose.preview.lab")
}

// ...

composePreviewLab {
    generateFeaturedFiles = true
}
```

この設定により、`@ComposePreviewLabOption` でマークされた Preview から PreviewList が生成され、  
さらに `.composepreviewlab/featured/` ディレクトリから Featured Files 用のリストも作成されます。

## 7. Web 用ギャラリーのビルドタスク

`integrationTest/app` では、Compose Preview Lab の Web ギャラリーを生成するためにカスタムタスクを定義しています。

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

:::info Docusaurus との連携
`integrationTest/web` のドキュメントサイトでは、上記タスクで生成された `compose-preview-lab-gallery` を  
`<EmbeddedPreviewLab />` コンポーネント経由で iframe として埋め込んでいます。  
これにより、ドキュメント内から実際の PreviewLab ギャラリーをインタラクティブに操作できます。
:::


