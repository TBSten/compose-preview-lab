# fix-ci-pr-comments

PR レビューコメント対応ワークフロー

## 目的

PR に付いたレビューコメント（指摘事項）に対応し、修正を完了させて、コメントを resolved にする。

## ⚠️ 重要: タスク管理について

**レビューコメント対応時は必ず TodoWrite を使用してタスクを追跡すること。**

特に以下のワークフロー を1セットとして管理する：

```
1. レビューコメントを確認
2. 指摘箇所を修正
3. テスト実行・検証
4. コミット・プッシュ
5. 対応したコメントを resolved にする  ← 忘れがち！
```

**コミット・プッシュで終わりではない。resolved にするまでが対応完了。**

## ワークフロー

### Step 1: レビューコメントの確認

**GitHub MCP を使用**:

```
mcp__github__pull_request_read
- method: get_review_comments
- owner: TBSten
- repo: compose-preview-lab
- pullNumber: (PR番号)
```

**確認事項**:

- 未解決（isResolved: false）のスレッドを抽出
- 各コメントの指摘内容を理解
- 修正が必要な箇所を特定
- スレッド ID を記録（resolved 時に使用）

### Step 2: 指摘箇所を修正

**修正の流れ**:

1. Read で指摘されたファイルを確認
2. コメント内容に従ってコードを修正
3. Edit または Write を使用して修正を実行

**例：KDoc の修正**

```
修正前: KDoc に @sample タグが不正に使用されている
修正後: @see タグに変更する
```

### Step 3: テスト実行・検証

**修正後にテストを実行**:

```bash
# 関連するテストを実行
./gradlew ktlintCheck
./gradlew jvmTest
cd integrationTest && ./gradlew compileKotlin
```

**確認事項**:

- ビルドが成功するか
- テストが成功するか
- 修正が完全か（不完全な修正は resolve しない）

### Step 4: コミット・プッシュ

**コミットメッセージのポイント**:

- 修正内容を明確に記述
- 複数のコメントに対応する場合は、各コメントの参照 ID を含める

```bash
git add <modified-files>
git commit -m "fix: address PR review comments

- Fix KDoc @sample tag (PRRT_kwDOOftTdM5oDw_S)
- Update module description (PRRT_kwDOOftTdM5oDxAq)
- Standardize data object usage (PRRT_kwDOOftTdM5oDw_q)

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>"

git push origin <branch-name>
```

### Step 5: レビューコメントを resolved にする

**GitHub GraphQL API を使用**:

```bash
gh api graphql -f query='
  mutation {
    resolveReviewThread(input: {threadId: "THREAD_ID"}) {
      thread {
        isResolved
      }
    }
  }
'
```

**複数コメントを一度に resolve する場合**:

```bash
for thread_id in "ID1" "ID2" "ID3"; do
  echo "Resolving thread: $thread_id"
  gh api graphql -f query="mutation { resolveReviewThread(input: {threadId: \"$thread_id\"}) { thread { isResolved } } }"
done
```

## チェックリスト

レビューコメント対応時は以下を確認:

- [ ] レビューコメントを全て確認した
- [ ] 未解決（isResolved: false）のスレッドを抽出した
- [ ] 各修正が CI テストで検証された
- [ ] コミット・プッシュが完了した
- [ ] resolve するコマンドを実行した
- [ ] resolve が成功したことを確認した（JSON レスポンスで `isResolved: true`）

## よくある間違い

### ❌ 間違い1: modified されて CI 実行されるまで resolve する

```bash
# 間違い
git push
sleep 1
gh api graphql ... (即座に resolve)
```

**正しい方法**:

```bash
# 正しい
git push
# CI が成功することを確認してから
gh run view ... (CI ステータスを確認)
# 成功したら resolve
gh api graphql ...
```

### ❌ 間違い2: 部分的な修正で resolve する

修正があった指摘コメントは、修正が完全になるまで resolve しない。

### ❌ 間違い3: テストを実行しないで修正完了と判断

修正が実装コードに影響する場合は、必ずテストを実行して動作確認をする。

## 参考コマンド

### PR の詳細情報を確認

```bash
gh pr view <PR-number>
gh pr view <PR-number> --json commits,reviews,comments
```

### ローカルで PR のコードを確認

```bash
git fetch origin pull/<PR-number>/head:<branch-name>
git checkout <branch-name>
```

### PR コメントの詳細を確認

```bash
mcp__github__pull_request_read
- method: get_review_comments
```
