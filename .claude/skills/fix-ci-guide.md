# fix-ci 総合ガイド

CI/CD 問題の診断・修正のための統合ガイド。

## スキル選択フロー

CI が失敗したときは、以下のフローで対応するスキルを選択します。

```
CI 失敗
  ↓
エラーメッセージを確認
  ↓
エラータイプを分類
  ├→ Lint エラー → fix-ci-lint
  ├→ API チェック失敗 → fix-ci-binary
  ├→ テスト失敗 → fix-ci-test
  ├→ ビルドエラー → fix-ci-build
  ├→ PR レビューコメント対応 → fix-ci-pr-comments
  └→ セットアップ問題 → fix-ci-setup
```

## 各スキルの概要

| スキル | 対象 | 実行時間 | 難易度 |
|--------|------|---------|--------|
| **fix-ci-setup** | CI環境構築、ワークフロー確認 | 5-10分 | ⭐ 低 |
| **fix-ci-lint** | コードフォーマット（ktlintCheck） | 5-15分 | ⭐ 低 |
| **fix-ci-binary** | バイナリ互換性（apiCheck） | 10-30分 | ⭐⭐ 中 |
| **fix-ci-test** | テスト失敗（jvmTest等） | 15-60分 | ⭐⭐⭐ 高 |
| **fix-ci-build** | ビルドエラー | 15-45分 | ⭐⭐⭐ 高 |
| **fix-ci-pr-comments** | PR レビュー対応 | 10-45分 | ⭐⭐ 中 |

## 実行順序

複数の CI ジョブが失敗している場合、以下の順序で修正することを推奨:

1. **Lint チェック** → fix-ci-lint
   - 最も修正が簡単
   - 他のチェックに影響を与える可能性がある

2. **バイナリ互換性** → fix-ci-binary
   - API ファイルの生成や削除が必要

3. **ビルド** → fix-ci-build
   - コンパイルエラーの修正

4. **テスト** → fix-ci-test
   - ビルド成功後に実行

5. **PR レビューコメント** → fix-ci-pr-comments
   - CI 成功後の最終確認

## CI ジョブの対応表

### pull-request.yml

| ジョブ | スキル | エラーの種類 |
|--------|--------|-------------|
| Lint | fix-ci-lint | ktlintCheck 失敗 |
| Validate Binary Compatibility | fix-ci-binary | apiCheck 失敗 |
| Publish to Maven Local | fix-ci-binary, fix-ci-build | API ファイル不足、ビルド失敗 |
| Test Jvm | fix-ci-test | jvmTest 失敗 |
| Test JS | fix-ci-test | jsBrowserTest 失敗 |
| Test Wasm JS | fix-ci-test | wasmJsBrowserTest 失敗 |
| Test Android Debug/Release | fix-ci-test | Android テスト失敗 |
| Test iOS | fix-ci-test | iOS テスト失敗 |

## よくあるシナリオ

### シナリオ1: コードを追加後、複数のチェックが失敗

**対応順序**:

1. fix-ci-lint → コードをフォーマット
2. fix-ci-build → コンパイルエラーを修正
3. fix-ci-test → テスト失敗を修正
4. fix-ci-binary → 必要に応じて API を更新

### シナリオ2: 新しいモジュールを追加

**対応順序**:

1. fix-ci-setup → モジュール構造を確認
2. fix-ci-binary → API ファイルを生成（apiDump）
3. fix-ci-lint, fix-ci-build, fix-ci-test（通常の順序）

### シナリオ3: PR にレビューコメントが付いた

1. fix-ci-pr-comments → コメント対応
2. 必要に応じて他のスキル（修正内容に応じて）
3. コメントを resolved

## トラブルシューティング

### "Cannot locate tasks that match" エラー

**原因**: モジュールが settings.gradle.kts に登録されていない

**解決**:

1. fix-ci-setup でモジュール構造を確認
2. settings.gradle.kts を Edit で修正
3. 必要に応じてコメントアウトしたモジュールを削除

### "Expected file with API declarations does not exist"

**原因**: 新しいモジュールの API ファイルが生成されていない

**解決**: fix-ci-binary で `apiDump` を実行

### テストがローカルで再現できない

**原因**: 環境差異（OS, Java バージョン等）

**解決**: fix-ci-test のトラブルシューティング セクションを確認

## ベストプラクティス

### 1. CI が失敗したら、すぐに対応する

失敗したジョブの情報は時間とともに失われる可能性があります。

### 2. 複数の失敗は順序立てて修正する

一度に複数を修正しようとするとコンテキストが混乱します。

### 3. 修正後は必ず検証する

ローカル環境でテストを実行して、修正が完全か確認します。

### 4. PR レビューコメントは resolved を忘れずに

コミット・プッシュ後も、コメントが resolved されていなければ対応は完了していません。

### 5. TodoWrite で進捗を追跡する

複雑な対応が必要な場合は、TodoWrite でタスクを管理してください。

## 参考資料

- Gradle 公式ドキュメント: https://docs.gradle.org
- Kotlin リント: https://ktlint.github.io
- Kotlin Multiplatform: https://kotlinlang.org/docs/multiplatform-discover.html
- GitHub Actions: https://docs.github.com/en/actions
