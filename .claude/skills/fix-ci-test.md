# fix-ci-test

テスト失敗の診断と修正スキル

## 目的

CI で実行されるテスト（jvmTest, jsBrowserTest, wasmJsBrowserTest, Android, iOS）が失敗した場合、原因を特定して修正する。

## 実行されるテストコマンド

### Jvm Test

```bash
./gradlew jvmTest
cd integrationTest && ./gradlew jvmTest
```

### JS Test

```bash
./gradlew jsBrowserTest
cd integrationTest && ./gradlew jsBrowserTest
```

### Wasm JS Test

```bash
./gradlew wasmJsBrowserTest
cd integrationTest && ./gradlew wasmJsBrowserTest
```

### Android Test

```bash
./gradlew testDebugUnitTest testReleaseUnitTest
cd integrationTest && ./gradlew testDebugUnitTest testReleaseUnitTest
```

### iOS Test （macOS でのみ実行可能）

```bash
./gradlew iosSimulatorArm64Test
cd integrationTest && ./gradlew iosSimulatorArm64Test
```

## 問題の診断

### Step 1: テスト実行

失敗したプラットフォームのテストをローカルで実行:

```bash
./gradlew jvmTest
```

### Step 2: エラーメッセージの確認

テストログから以下を特定:

- 失敗したテストクラス/メソッド名
- 失敗原因（assertion エラー、null pointer 等）
- スタックトレース

### Step 3: テストファイルの特定

Glob を使用して該当するテストファイルを検索:

```
src/**/kotlin/**/*Test.kt
src/**/kotlin/**/*Tests.kt
```

## 修正手順

### パターン A: テスト自体の問題

**修正方法**:

1. Read でテストコードを確認
2. 失敗原因を特定（期待値の誤りなど）
3. Edit でテストコードを修正
4. 再度テストを実行

```bash
./gradlew jvmTest
```

### パターン B: 実装コードの問題

**修正方法**:

1. Read で失敗しているテストを確認
2. 実装コードの Read で問題箇所を特定
3. Edit で実装を修正
4. 再度テストを実行

### パターン C: 依存関係の問題

**確認手順**:

1. エラーメッセージで依存関係の不足を確認
2. `gradle/libs.versions.toml` を Read で確認
3. 必要なライブラリが定義されているか確認
4. バージョン不一致がないか確認

**修正方法**:

1. 依存関係を追加または更新
2. `./gradlew clean` を実行してキャッシュをクリア
3. 再度テストを実行

## 検証

修正後、以下のコマンドで各プラットフォームのテストが成功することを確認:

```bash
./gradlew jvmTest
./gradlew jsBrowserTest
./gradlew wasmJsBrowserTest
cd integrationTest && ./gradlew jvmTest
cd integrationTest && ./gradlew jsBrowserTest
```

各テストが終了コード 0 で成功することを確認。

## トラブルシューティング

### テストがローカルで再現できない場合

1. OS の違い（macOS vs Ubuntu）を確認
2. Java バージョンの違いを確認
3. 環境変数の設定を確認
4. キャッシュをクリア：`./gradlew clean`

### ブラウザテスト（JS, Wasm JS）が失敗する場合

1. Playwright や Chrome ドライバーがインストールされているか確認
2. ブラウザキャッシュをクリア
3. ヘッドレスモードでのテスト実行を試す

### Android/iOS テストがローカルで実行できない場合

1. エミュレータが起動しているか確認（Android）
2. Xcode がインストールされているか確認（iOS）
3. CI 環境でのみ実行できるテストの可能性
