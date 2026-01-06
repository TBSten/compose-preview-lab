# fix-ci-binary

バイナリ互換性チェック（apiCheck）の診断と修正スキル

## 目的

`./gradlew apiCheck` が失敗した場合、API 変更を確認して修正する。

## 問題の診断

### Step 1: エラーの確認

**実行コマンド**:

```bash
./gradlew apiCheck --info
```

**エラーメッセージから把握すべき情報**:

- 変更された API は何か（クラス、関数、プロパティ）
- 予期しない変更か、意図的な変更か
- 影響を受けるモジュールはどれか

エラーメッセージ例:

```
Expected file with API declarations 'extension/navigation3/api/android/extension-navigation3.api' does not exist.
```

## 修正手順

### パターン A: API ファイルが存在しない

**原因**: 新しいモジュールが追加された場合や、API ファイルがコミットされていない。

**修正方法**:

1. **API ファイルを生成**: `./gradlew :module:apiDump` を実行
2. **ファイルを確認**: `api/` ディレクトリに API ファイルが生成されたことを確認
3. **モジュールが空の場合**: モジュール自体をコメントアウトするか、プレースホルダーを追加

```bash
./gradlew :extension:extension-navigation:apiDump
./gradlew :extension:extension-navigation3:apiDump
```

### パターン B: API が意図的に変更された

**確認手順**:

1. 変更されたAPI を確認
2. Read でソースコードを読む
3. 変更が意図的かどうかを判断

**修正方法** （意図的な変更の場合）:

1. `api/` ディレクトリのファイルを更新
2. 変更内容が API ファイルに反映されていることを確認

### パターン C: API が意図せず変更された

**修正方法**:

1. ソースコードを Read で確認
2. Edit で変更を元に戻す
3. 再度 `./gradlew apiCheck` を実行

## 検証

修正後、以下のコマンドで確認:

```bash
./gradlew apiCheck
```

バイナリ互換性チェックが成功すること（終了コード 0）を確認。

## 参考情報

- API チェックは Kotlin Multiplatform でバイナリ互換性を保証するための機能
- `api/` ディレクトリには、各プラットフォーム（Android, JVM, Native等）の API 宣言が保存される
- API ファイルは手動編集せず、`apiDump` タスクで自動生成することが推奨される
