---
name: operations
description: pr-fix-loop の運用 tips - gh CLI 認証 / dirty checkout 中断 / ログ取得タイミング / rerun のブロック条件 / commit メッセージ粒度 / owner-repo 抽出など、 親 SKILL.md には書ききれない実装上の知見をまとめる。 pr-fix-loop が Step 1-7 のいずれかで実装に困った時の参照先。
---

# pr-fix-loop 運用 tips

## gh CLI が大前提

このスキルは `gh` (GitHub CLI) と `git` がローカルで動くことを前提にする。 認証は `gh auth status` で事前確認。 NG なら skill は即座にユーザーに報告してそのループは抜ける。

## owner / repo は origin から取得

PR list だけ受け取る場合、 owner/repo は

```bash
gh repo view --json owner,name -q '.owner.login + "/" + .name'
```

で derive。 multi-remote の場合は `origin` を優先。 セッション内で 1 度取って `$OWNER` / `$REPO` 環境変数に固定して使い回すのが安全 (= GraphQL クエリの `<OWNER>` / `<REPO>` プレースホルダや `gh api` の path / `-F` 変数として再利用)。

## branch checkout を中断する条件

複数 PR を順次 checkout するとローカルの worktree が頻繁に切り替わる。 ユーザーが何か作業中なら接触しないよう、 ループ開始時に `git status -s` で **dirty なら skill 起動を中断**。 強制実行モードを入れたければ `--force-on-dirty` フラグを定義 (今は未実装、 必要になったら追加)。

work 中の差分は `git stash push -u -m "pr-fix-loop temp save"` で一旦退避してから checkout 切替、 完了後 `git stash pop` で戻すのも可 (差分小規模・短時間なら)。 但し stash pop で衝突する可能性があるので「短時間 work」 が前提。

## ログ取得の注意

`gh run view --log` は **workflow 全体が完了するまで返ってこない** ことがある。 個別 job の log は

```bash
gh api "/repos/$OWNER/$REPO/actions/jobs/<jobId>/logs"
```

でほぼ即時取得できる。 workflow に `in_progress` の job が残っていても、 完了済み job の log は取れる。

## rerun のタイミング

`gh run rerun --failed` は **workflow が in-progress の間ブロックされる** (`This workflow is already running`)。 その場合は 「次回ループに送る」 と判定して、 その PR の transient failure 対応はスキップ。 強引に sleep して polling すると loop の token / 時間予算を食う。

## commit メッセージの粒度

各 PR の修正 commit は **意味ある単位で分割**。 例えば「lint fix + apiDump」 は 2 つの logical change なので別 commit にできるなら分ける。 ただし「単一の copilot review コメント対応で複数 file を直す」 ような場合は 1 commit でよい (review thread 単位)。

## non-interactive rebase

Claude Code が起動する shell では `git rebase -i` は editor を開く必要があり、 non-interactive な flow に乗らないため使えない。 Step 2 (commit 整理) では `git reset --soft <block の親> && git commit -m "..."` で 「reset → reconstruct」 パターンに置き換える。 これは「対話モードを完了できないため」 の制約であり、 リポジトリ規約 (`CLAUDE.md`) で禁止しているわけではない。

## stat のクロスプラットフォーム

`stat -f %m` (BSD/macOS) と `stat -c %Y` (GNU/Linux) は互換性なし。 24h cooldown marker の mtime チェックでは python3 (`os.path.getmtime`) または perl (`(stat shift)[9]`) のような portable な手段を使う。 plain `stat` は環境依存で動かない可能性がある。

## ページング

GitHub API はデフォルト page サイズが小さい:

- REST API (`gh api repos/.../issues/<pr>/comments`): デフォルト 30 件 → `gh api --paginate` で全 page 取得
- GraphQL `reviewThreads(first: N)`: 1 page で N 件 → `pageInfo { hasNextPage endCursor }` で paginate ループ

これらを single-call で済ませると、 50 件以上の thread / 30 件以上の comment を持つ PR で取りこぼしが発生する。
