# .claude/commands スキルライブラリ

このディレクトリには、プロジェクト固有の CI/CD 診断・修正スキルが格納されています。

## スキル一覧

### 📚 ガイド

- **[fix-ci-guide.md](fix-ci-guide.md)** - CI/CD トラブルシューティング総合ガイド
  - スキル選択フロー
  - 実行順序
  - シナリオ別対応方法

### 🔧 個別スキル

- **[fix-ci-setup.md](fix-ci-setup.md)** - CI/CD 環境構築とセットアップ診断
  - ワークフロー設定確認
  - 環境変数・シークレット確認
  - 難易度: ⭐ 低

- **[fix-ci-lint.md](fix-ci-lint.md)** - Kotlin リント（ktlintCheck）修正
  - コードフォーマット自動修正
  - カスタムルール対応
  - 難易度: ⭐ 低

- **[fix-ci-binary.md](fix-ci-binary.md)** - バイナリ互換性（apiCheck）修正
  - API ファイル生成・更新
  - モジュール管理
  - 難易度: ⭐⭐ 中

- **[fix-ci-test.md](fix-ci-test.md)** - テスト失敗修正
  - JVM, JS, Wasm JS, Android, iOS テスト対応
  - テストコード修正
  - 実装コード修正
  - 難易度: ⭐⭐⭐ 高

- **[fix-ci-build.md](fix-ci-build.md)** - ビルドエラー修正
  - コンパイルエラー
  - 依存関係エラー
  - 設定エラー
  - 難易度: ⭐⭐⭐ 高

- **[fix-ci-pr-comments.md](fix-ci-pr-comments.md)** - PR レビューコメント対応
  - コメント確認とタスク管理
  - 修正実装
  - Comment の resolved
  - 難易度: ⭐⭐ 中

## 使い方

### 1. CI が失敗した場合

```bash
# ガイドを確認
cat fix-ci-guide.md

# エラーに応じた個別スキルを参照
cat fix-ci-lint.md      # Lint エラーの場合
cat fix-ci-binary.md    # API チェック失敗の場合
cat fix-ci-test.md      # テスト失敗の場合
cat fix-ci-build.md     # ビルドエラーの場合
```

### 2. PR にレビューコメントが付いた場合

```bash
cat fix-ci-pr-comments.md
```

## ⚠️ 重要なポイント

### TodoWrite の使用

複雑な修正が必要な場合は、必ず TodoWrite でタスク管理してください。

```
- [ ] エラー確認
- [ ] 修正実装
- [ ] テスト実行
- [ ] コミット・プッシュ
- [ ] コメント resolved（該当する場合）
```

### PR コメントの resolved

**最重要**: コミット・プッシュで完了ではなく、コメントを **resolved** にするまでが対応完了。

```bash
gh api graphql -f query='
  mutation {
    resolveReviewThread(input: {threadId: "THREAD_ID"}) {
      thread { isResolved }
    }
  }
'
```

## 参考リソース

- GitHub Actions ワークフロー: `.github/workflows/`
- Gradle 設定: `build.gradle.kts`, `settings.gradle.kts`
- テスト: `**/test/**`, `**/androidTest/**`
- リント設定: `build.gradle.kts` の `ktlint` セクション

## バージョン情報

- 作成日: 2026-01-06
- 分割元: fix-ci.md (422行)
- スキル数: 6個
- ドキュメント行数: 約 700行
