---
name: review-handling
description: pr-fix-loop の Step 5 で使用する review thread / issue-level comment の取得・修正・resolve / wrap convention の詳細手順。 inline review thread は GitHub 標準の resolve 機構があるが、 conversation 上の issue-level comment は resolve 機構が無いため、 本文を `<details><summary>対応済</summary>` で囲んで commit hash を末尾に付与する convention で「処理済」 マークを付ける。 thread / issue 両方の見逃し防止と、 一度に複数 thread 対応した時の commit 関連付け方を含む。
---

# Review handling — pr-fix-loop Step 5 詳細

`pr-fix-loop` skill の Step 5 から呼ばれる、 review thread / issue-level comment 処理の詳細運用。 親 skill (`SKILL.md`) では概要しか触れていない。

PR には **2 種類** のコメントがあり、 pr-fix-loop は **両方** 見る必要がある:

1. **inline review thread** (`gh api graphql ... reviewThreads { isResolved }`) — GitHub に標準の resolve 機構あり
2. **issue-level / conversation comment** (`gh api repos/.../issues/<pr>/comments`) — resolve 機構なし。 「対応済」 のマーク方法は本 repo の convention に従う

## 5-A. inline review thread (`reviewThreads`)

未 resolve thread が 1 件以上ある PR は **fix-ci-pr-comments skill** に委譲する。 該当 skill が存在しない場合は、 各 thread のコメント本文を要約して 1 件ずつ手動対応 (修正 → commit → push → 各 thread を `resolveReviewThread` mutation で resolve)。

resolve mutation:

```bash
gh api graphql -f query='
  mutation { resolveReviewThread(input: {threadId: "THREAD_ID"}) { thread { isResolved } } }
'
```

複数 thread を一度に対応した場合は **修正と関連付けて** resolve する (バルクで全部 resolve するな)。 各 thread に「この commit で対応」 と書ければ尚良いが、 commit message に thread ID を含めるだけでも十分。

## 5-B. issue-level comment (`issue_comments`)

GitHub 標準の resolve 機構が無いため、 「対応済」 マークは **コメント本文を `<details><summary>対応済</summary>` で囲み、 末尾に `--> {commit-hash} で対応済` を付与** する convention で代替する (この repo で確立済の運用)。

対応手順:

1. `gh api repos/$OWNER/$REPO/issues/comments/<id> -q .body` で原文取得
2. 内容を理解、 必要な修正を実装 → commit → push
3. `gh api -X PATCH repos/$OWNER/$REPO/issues/comments/<id> -f body="$NEW_BODY"` で本文更新:

```bash
NEW_BODY=$(printf '<details><summary>対応済</summary>\n\n%s\n\n</details>\n\n--> %s で対応済' "$ORIG" "$COMMIT_HASH")
gh api -X PATCH "repos/$OWNER/$REPO/issues/comments/<id>" -f body="$NEW_BODY"
```

複数の独立した点を含むコメントには、 1 件単位で対応 commit を分けて (`commit-hash-A (#XX 対応)`, `commit-hash-B (#YY 対応)` のように) summary に併記する。

## 見逃し防止

ループ毎に `gh api --paginate repos/$OWNER/$REPO/issues/<pr>/comments` で **全コメント** を取得し (ページング必須 — 1 ページ 30 件しか返らない)、 `<details><summary>対応済` ラップ無しを 1 件以上検出したら **`unresolved` 1+ と同等の扱い** で `Step 7: 終了判定` の「変化あり」 としてカウント。 review thread のみ見て issue comment を見落とすと、 ユーザの follow-up 報告が放置される事故が起きる。

inline review thread 側もページング必須: `reviewThreads(first: 100, after: $cursor)` で `pageInfo.hasNextPage` が `true` なら `endCursor` を渡して再取得。
