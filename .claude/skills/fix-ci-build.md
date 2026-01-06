# fix-ci-build

ビルドエラーの診断と修正スキル

## 目的

Gradle ビルドが失敗した場合、エラーを診断して修正する。

## 問題の診断

### Step 1: エラーの確認

ビルドコマンドを実行:

```bash
./gradlew build --no-daemon 2>&1 | tail -50
```

またはより詳細なエラー情報を取得:

```bash
./gradlew build --info
./gradlew build --stacktrace
```

### Step 2: エラーの分類

エラーメッセージから以下を特定:

1. **コンパイルエラー**: `Compilation failed`
2. **依存関係エラー**: `Could not find`, `Dependency`, `version`
3. **タスク実行エラー**: `Task` が見つからない、実行に失敗
4. **設定エラー**: `build.gradle.kts` の問題

## 修正手順

### パターン A: コンパイルエラー

**原因**: 構文エラーや型エラー

**修正方法**:

1. エラーメッセージからファイルと行番号を特定
2. Read でソースコードを確認
3. Edit でコードを修正

```kotlin
// 例：型エラー
val value: String = 123  // エラー：Int を String に割り当てることはできない
```

### パターン B: 依存関係エラー

**原因**: ライブラリが見つからない、バージョン競合

**修正方法**:

1. `gradle/libs.versions.toml` を Read で確認
2. ライブラリが定義されているか確認
3. バージョン指定に問題がないか確認
4. Edit で修正

```toml
# gradle/libs.versions.toml の例
[versions]
kotlin = "2.0.0"
compose = "1.7.0"

[libraries]
androidx-compose-ui = { module = "androidx.compose.ui:ui", version.ref = "compose" }
```

### パターン C: タスク実行エラー

**原因**: 存在しないタスク、プラグイン設定エラー

**修正方法**:

1. エラーメッセージからどのタスクが失敗したかを確認
2. `build.gradle.kts` を Read で確認
3. プラグインが適切に適用されているか確認
4. タスク定義に問題がないか確認
5. Edit で修正またはタスク削除

### パターン D: 設定エラー

**原因**: `build.gradle.kts` の設定が不正

**修正方法**:

1. `build.gradle.kts` を Read で確認
2. Kotlin DSL の構文が正しいか確認
3. 必須の設定が不足していないか確認
4. Edit で修正

## 特別なケース

### キャッシュの問題

`~/.gradle` ディレクトリのキャッシュが古い、または破損している:

```bash
./gradlew clean
./gradlew build
```

または:

```bash
rm -rf ~/.gradle
./gradlew build
```

### マルチプロジェクトビルドの問題

サブプロジェクトのビルドが失敗:

1. `settings.gradle.kts` を確認してプロジェクト構造を理解
2. 特定のサブプロジェクトのみをビルド：`./gradlew :module:build`
3. ビルド依存関係を確認：`./gradlew :module:dependencies`

### Kotlin/Compose の問題

Kotlin バージョン、Compose バージョンの不一致:

1. `gradle/libs.versions.toml` で各バージョンを確認
2. 互換性マトリックスを確認（Jetbrains、Google のドキュメント）
3. 必要に応じてバージョンを更新

## 検証

修正後、ビルドが成功することを確認:

```bash
./gradlew build
```

またはルートとサブプロジェクトの両方でビルド:

```bash
./gradlew build
cd integrationTest && ./gradlew build
```

## よくある問題と解決方法

| 問題 | 原因 | 解決方法 |
|------|------|---------|
| `Could not find` | ライブラリが見つからない | `gradle/libs.versions.toml` を確認、Repository 設定を確認 |
| `Duplicate class` | 重複するライブラリ | 依存関係を確認、exclude ルール を追加 |
| `Version conflict` | バージョン競合 | 互換性のあるバージョンに統一 |
| `Out of memory` | ヒープが不足 | `org.gradle.jvmargs` を設定 |
| `Timeout` | ビルドが遅い | Gradle デーモンを再起動：`./gradlew --stop` |
