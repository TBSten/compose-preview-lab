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

## 3. gradle.properties 経由で設定する

`composePreviewLab { ... }` ブロックで指定できる設定項目は、**`gradle.properties` (または `-PcomposePreviewLab.*=...` CLI option) からも同じ値を指定できます**。 monorepo / multi-module で共通設定をルートから一括指定したい、 CI から `-P` で値を差し替えたい、 という場合に便利です。

### キー一覧

| `gradle.properties` key | 対応する DSL property | 型 |
| --- | --- | --- |
| `composePreviewLab.generatePackage` | `generatePackage` | String |
| `composePreviewLab.projectRootPath` | `projectRootPath` | String |
| `composePreviewLab.generateFeaturedFiles` | `generateFeaturedFiles` | Boolean |
| `composePreviewLab.collectPreviews.enabled` | `collectPreviews.enabled` | Boolean |
| `composePreviewLab.collectPreviews.defaultCollectScope` | `collectPreviews.defaultCollectScope` | String |

Boolean は `true` / `false` (大文字小文字無視) のみ正規に認識されます。 それ以外の文字列はビルドを失敗させずに **default 値へフォールバック** します。

### 優先順位

3 段階で解決され、 **上にあるものほど優先** されます:

1. `composePreviewLab { ... }` DSL block での明示指定
2. `gradle.properties` (または `-PcomposePreviewLab.*=...` CLI)
3. プラグイン組み込みの default 値

つまり「`gradle.properties` で全モジュールに baseline を引き、 個別モジュールで必要なら DSL block で上書きする」 という Gradle 慣例 (`org.gradle.parallel` 等) と同じ運用ができます。

### 使用例

```properties title="gradle.properties"
# 全モジュールに対するデフォルト
composePreviewLab.generateFeaturedFiles=true
composePreviewLab.collectPreviews.defaultCollectScope=acme_ui
```

```bash title="CI から一時的に上書き"
./gradlew assemble -PcomposePreviewLab.generateFeaturedFiles=true
```

### 未知キーの検出

タイポを早期に発見できるよう、 `composePreviewLab.*` で始まる **未知のキー** が `gradle.properties` に含まれていた場合は build 時に warning を 1 メッセージにまとめて出します:

```
[compose-preview-lab] Unknown gradle.properties key(s) under 'composePreviewLab.*' detected:
composePreviewLab.generatePackge. Known keys are: composePreviewLab.collectPreviews.defaultCollectScope,
composePreviewLab.collectPreviews.enabled, composePreviewLab.generateFeaturedFiles,
composePreviewLab.generatePackage, composePreviewLab.projectRootPath.
```

warning に留めるのはあえての設計です ( ビルドそのものは壊しません )。 CI ログで気付けるようにしてあるので、 typo が見つかったら速やかに直してください。

