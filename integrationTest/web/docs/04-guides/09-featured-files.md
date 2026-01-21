---
title: "Featured Files"
sidebar_position: 9
---

# Featured Files

Featured Files は、プロジェクト内の Preview をグループ化して整理する機能です。`.composepreviewlab/featured` ファイルに 重要な Preview や、現在作業中の Preview をリストアップ・グループ化し簡単にアクセスできるようにします。

import featuredFilesExampleImage from "./featured-files-example.png";

<img src={featuredFilesExampleImage} width="400" />

## 概要

大規模なプロジェクトでは、数多くの Preview が存在するため、特定の Preview を見つけるのが困難になることがあります。Featured Files を使用すると：

- **プロジェクト全体の Preview を整理**：関連する Preview をグループ化
- **重要な Preview に素早くアクセス**：頻繁に確認する Preview をグループ化
- **レビューワークフローの改善**：レビュー中の Preview をまとめて表示
- **チーム間の共有**：特定の機能に関連する Preview を共有

## セットアップ

### 1. Gradle プラグインの設定

`build.gradle.kts` で Featured Files の生成を有効化します：

```kotlin
composePreviewLab {
    generateFeaturedFiles = true
}
```

### 2. Featured Files ディレクトリの作成

プロジェクトのルートディレクトリに `.composepreviewlab/featured/` ディレクトリを作成します：

```bash
mkdir -p .composepreviewlab/featured
```

### 3. グループファイルの作成

`.composepreviewlab/featured/` ディレクトリ内に、グループ名をファイル名としたファイルを作成します。

例：`.composepreviewlab/featured/Important`

```
src/commonMain/kotlin/com/example/ui/HomeScreen.kt
src/commonMain/kotlin/com/example/ui/LoginScreen.kt
src/commonMain/kotlin/com/example/ui/components/Button.kt
```

各行には、Preview を含むファイルの相対パスを記述します。

## 使用例

### 例 1: レビュー用グループ

CI で Compose Preview Lab をビルドする場合、PR の差分があるファイルを `.composepreviewlab/featured/PR` ファイルにリストアップすることで レビュー対象の Preview のみに絞ったグループを作成して Preview を探す手間が省けます。

```shell
git diff --name-only origin/main...HEAD | grep '\.kt$' > .composepreviewlab/featured/PR
```

```txt title=".composepreviewlab/featured/PR"
src/commonMain/kotlin/com/example/feature/newFeature/NewFeatureScreen.kt
src/commonMain/kotlin/com/example/feature/newFeature/NewFeatureDialog.kt
```

### 例 2: 重要なコンポーネント

頻繁に確認する重要なコンポーネントをまとめます：

`.composepreviewlab/featured/Core Components`

```
src/commonMain/kotlin/com/example/ui/components/Button.kt
src/commonMain/kotlin/com/example/ui/components/TextField.kt
src/commonMain/kotlin/com/example/ui/components/Card.kt
```

### 例 3: 機能別グループ

特定の機能に関連する Preview をまとめます：

`.composepreviewlab/featured/Authentication`

```
src/commonMain/kotlin/com/example/auth/LoginScreen.kt
src/commonMain/kotlin/com/example/auth/SignupScreen.kt
src/commonMain/kotlin/com/example/auth/ForgotPasswordScreen.kt
```

## アプリケーションへの統合

生成された `FeaturedFileList` を `previewLabApplication` または PreviewLabGallery に渡すことでアプリケーションにグループを表示することができます。

### JVM の場合

```kotlin
fun main() = previewLabApplication(
    previewList = PreviewList,
    featuredFileList = FeaturedFileList, // 追加
)
```

### JS/WasmJS の場合

```kotlin
fun main() = previewLabApplication(
    previewList = myModule.PreviewList,
    featuredFileList = myModule.FeaturedFileList, // 追加
    openFileHandler = UrlOpenFileHandler("https://github.com/user/repo/blob/main"),
)
```

## ベストプラクティス

### グループ名の命名

わかりやすく、意味のある名前を使用してください：

- ✅ `Core Components`
- ✅ `Under Review`
- ✅ `Authentication`
- ❌ `Group1`
- ❌ `Temp`

### ファイルパスの管理

- 相対パスを使用（プロジェクトルートから）
- 空行は無視されます
- コメントはサポートされていません（各行はファイルパスとして扱われます）

### バージョン管理

`.composepreviewlab/featured/` ディレクトリをバージョン管理に含めることで：

- チーム全体で Featured Files を共有
- レビュー時に関連する Preview を簡単に確認
- 機能開発のコンテキストを保持

一方、個人用の一時的なグループは `.gitignore` に追加することもできます：

```
# .gitignore
.composepreviewlab/featured/Temp*
.composepreviewlab/featured/My*
```

## トラブルシューティング

### FeaturedFileList が生成されない

1. `generateFeaturedFiles = true` が設定されているか確認
2. `.composepreviewlab/featured/` ディレクトリが存在するか確認
3. `./gradlew clean` 実行後に再度ビルドする

### ファイルパスが認識されない

- ファイルパスがプロジェクトルートからの相対パスになっているか確認
- ファイルが実際に存在するか確認
- パスの区切り文字（`/`）が正しいか確認

## 次のステップ

- [Fields](./fields/all-builtin-fields) で Preview をインタラクティブにする
- [Events](./events) でイベントハンドリングを学ぶ
- [Collect Preview](./collect-preview) で PreviewList の自動生成について詳しく学ぶ

