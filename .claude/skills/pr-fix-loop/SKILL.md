---
name: pr-fix-loop
description: 複数の GitHub Pull Request を並行管理する際、 各 PR の CI ステータスと未対応 review コメントを 1 回のループで一括チェックし、 失敗 check の種類 (lint / binary compat / build / test / transient infra) を自動判定して対応する fix-ci-* skill (lint=fix-ci-lint, binary=fix-ci-binary, build=fix-ci-build, test=fix-ci-test, comments=fix-ci-pr-comments) に委譲、 review コメントは取得 → 修正 → commit → push → resolve まで実行する。 stack 関係 (PR Y の base が PR X のブランチ) があれば自動で rebase 連鎖。 `/loop` skill と組み合わせれば 5 分間隔などで自動化できる (10 回連続「変化なし」 で 終了)。 ユーザーが「複数 PR を見ながら修正」「PR ループ」「PR fix loop」「stacked PR を順番に green に」「PR を寝かせている間に CI 修正したい」「複数 PR の CI と review comment を自動で対応」 などと言ったら 必ずこの skill を使う。 単一 PR でも、 CI fix と review comment 対応をワンセットで自動化したい文脈なら trigger してよい。
---

# pr-fix-loop

## 目的

ユーザーが GitHub に開いている **複数の PR を並行管理** する状況で、 各 PR について:

1. CI ステータス取得
2. 失敗 check のタイプ判定 + 対応する fix-ci-* skill への委譲
3. 未対応 review コメントの対応 (取得 → 修正 → commit → push → resolve)
4. stack 関係 (PR Y の base = PR X の branch) なら自動 rebase 連鎖

を **1 回のループで全 PR まとめて** 処理する。

このスキル自体は **1 回分の手続き** を定義する。 周期実行したい場合は `/loop 5m /pr-fix-loop <PR-list>` で呼び出すのが目安 (CI が 5 分前後で 1 ステップ進む程度の粒度)。 「**10 回連続で 変化なし**」 を観測したらループを抜ける合図を出す ([Step 5: 終了判定] 参照)。

## 入力

呼び出し時の引数:

- **PR 番号の list** (1 件以上、 半角スペースで区切り)。 例: `179 180 181`
- 省略時は **現在のブランチに紐づく PR** を `gh pr view --json number` で 1 件 derive

ブランチ名・ base ブランチ・ stack 関係は `gh pr view <num> --json headRefName,baseRefName` で **毎ループ取得** する (静的 mapping を入力に持たない)。 そのほうが reorder / rebase / branch rename にも自動追従する。

## ワークフロー (1 ループ分)

ループ 1 回で **以下を全 PR に対して** 実行する。 PR を skip しない (in-progress で何もできない PR があっても、 他の PR は進める)。

### Step 0: 各 PR ブランチに最新 main を取り込む

ループ冒頭で `git fetch origin main` し、 各 PR の branch を **main 最新の上に rebase** する。 PR 作業中に main 側で別 PR が merge されるケース (例: 同 session 内の別 PR が main に landed、 dependabot bump、 他コラボレーターの merge) で、 PR ブランチが時代遅れになるのを防ぐ。

```bash
git fetch origin main
for pr in $PR_LIST; do
  branch=$(gh pr view "$pr" --json headRefName -q .headRefName)
  git checkout "$branch"
  git fetch origin "$branch"
  git reset --hard "origin/$branch"   # 念のため remote と揃える
  if ! git merge-base --is-ancestor origin/main HEAD; then
    git rebase origin/main || { git rebase --abort; echo "[PR #$pr] rebase conflict, skip this loop"; continue; }
    git push --force-with-lease origin "$branch"
  fi
done
```

衝突した場合は `git rebase --abort` で諦め、 「次回ループに送る」 として ユーザー報告するに留める (auto-merge は危険)。

stack PR (PR Y の base が PR X のブランチ) の場合は **base に親 PR のブランチを使う**。 親 PR がまだ main に merged されていないなら親 PR ブランチを最新に rebase してから子 PR を rebase する。 トポロジカル順で処理。

### Step 0.5: Commit 数チェックと整理 (>= 10 で発火、 project-local 拡張)

PR ブランチが base に対して **10 commits 以上** 進んでいる場合、 review しやすさのため commit を整理する。 `-i` (interactive rebase) は CLAUDE.md 規約により使えないため、 全て非対話で完結させる。

判定:

```bash
count=$(git rev-list --count "origin/$base..origin/$branch")
(( count < 10 )) && continue
```

整理ヒューリスティック (上ほど優先、 該当しない commit は触らない):

1. **同じ conventional commit prefix が連続** (例: 連続する `fix(ci): ...` 3 件) → 1 つに squash。 join メッセージは prefix を保ちつつ各 commit body を箇条書き
2. **`fixup!` / `squash!` prefix** → 直前 commit に squash (autosquash 相当を非対話で再現)
3. **同 file/file-set のみ touching する小規模 commit が連続** (例: 同 1 ファイルの typo 修正 3 連続) → 1 つに squash

prefix セットは `feat|fix|test|chore|docs|refactor|style|build|ci|perf`。 実装手順:

1. `git log --format='%H %s' origin/$base..HEAD` で commit 列挙
2. 上記ヒューリスティックでブロックを検出
3. 後ろから順に `git reset --soft <block の親>` → メッセージを join → `git commit -m "..."` で再構築
4. `git push --force-with-lease origin "$branch"`

**安全策**:

- 整理前に `git update-ref refs/pr-fix-loop-backup/$pr/$(date +%Y%m%d-%H%M%S) HEAD` でバックアップ ref を残す (任意のタイミングで `git reset --hard <backup-ref>` で完全復旧可能)
- 衝突 / 整理結果が空 / メッセージ join 失敗の場合は backup ref に戻して 「整理失敗、 次回送り」 と報告。 **commit を絶対に失わない方針**
- 同 PR を 24h 以内に再 squash しない (チャタリング防止)。 `.local/tmp/pr-fix-loop-squashed-$pr` の mtime を見て判定:

```bash
marker=".local/tmp/pr-fix-loop-squashed-$pr"
if [[ -f "$marker" ]] && (( $(date +%s) - $(stat -f %m "$marker") < 86400 )); then
  continue
fi
# ... squash 実施 ...
mkdir -p "$(dirname "$marker")"
touch "$marker"
```

10 commits 以上あっても、 ヒューリスティックで squash 対象が 0 〜 2 件しか出ない場合は無理に squash せず、 「整理候補なし — 手動整理を検討」 と報告 (logical commit が 10 件並ぶケースもある)。

stack 関係 (Step 4) との順序: **親 PR の整理 → 親 push → 子の rebase → 子の整理** の順。 子を先に整理してから親を rebase すると、 子の squash が無効化される。

ループ末尾の報告 table の 「対応」 列に `commit 整理 N → M` と記す。

### Step 1: 全 PR の状態を一括取得

PR 数だけ `gh pr view` を発行するのは安いので、 一気に集める:

```bash
for pr in $PR_LIST; do
  gh pr view "$pr" --json statusCheckRollup,state,headRefName,baseRefName,mergeable
done
```

合わせて未 resolve review thread と未対応 issue comment を取る (PR は **inline review thread** と **conversation 上の issue comment** の 2 種類があり、 どちらも見逃さない):

```bash
# inline review threads (resolve / unresolve 状態が GitHub 側にある)
gh api graphql -f query='
  query($pr: Int!){
    repository(owner:"<OWNER>",name:"<REPO>"){
      pullRequest(number:$pr){
        reviewThreads(first:50){ nodes { id isResolved path line comments(first:1){ nodes { body } } } }
      }
    }
  }' -F pr=$pr

# issue-level comments (resolve 機構なし、 本文の `<details><summary>対応済` ラップで判定)
gh api repos/<OWNER>/<REPO>/issues/<PR>/comments \
  -q '.[] | select(.body | startswith("<details><summary>対応済") | not) | {id, body: (.body | .[0:200])}'
```

owner / repo は `gh repo view --json owner,name` で 1 度取れば再利用。

「未対応」 の判定:
- review thread → `isResolved == false`
- issue comment → 本文が `<details><summary>対応済` で始まらない (= 未だ対応済ラップが付いていない)。 `<details>` ラッパは pr-fix-loop が「処理済」 とマークするための convention。 GitHub に resolve 機構がないので、 ラッパで代用する。

### Step 2: 失敗 check を分類

各 PR の `statusCheckRollup` を walk し、 `conclusion == "FAILURE"` の job について **失敗種別を判定** する。 判定は `references/failure-classification.md` のヒューリスティクス順 (transient → lint → binary → test → build) で実施。

判定後、 種別に応じて以下に **委譲**:

| 種別 | 委譲先 |
|------|--------|
| transient infra (api.foojay.io / 502 / runner image / network timeout) | `gh run rerun <runId> --failed` (workflow が `in-progress` だと拒否される — その場合は次回ループに送る) |
| lint (ktlintCheck etc.) | fix-ci-lint skill |
| binary compat (apiCheck) | fix-ci-binary skill |
| build (compileKotlin / assembleDebug etc.) | fix-ci-build skill |
| test (jvmTest / jsBrowserTest / iosTest etc.) | fix-ci-test skill |

委譲先 skill が現在のリポジトリに **存在しない** 場合は、 失敗 job のログ末尾を `gh api /repos/<owner>/<repo>/actions/jobs/<jobId>/logs` で取得して原因を要約し、 ユーザーに報告するに留める (勝手に手動修正に走らない)。

各 PR の修正は対応するブランチで行う:

```bash
git checkout <headRefName>
# fix-ci-* skill が修正と push まで担当
```

### Step 3: 未対応 review コメント / issue comment を処理

#### 3-A. inline review thread (`reviewThreads`)

未 resolve thread が 1 件以上ある PR は **fix-ci-pr-comments skill** に委譲する。 該当 skill が存在しない場合は、 各 thread のコメント本文を要約して 1 件ずつ手動対応 (修正 → commit → push → 各 thread を `resolveReviewThread` mutation で resolve)。

resolve mutation:

```bash
gh api graphql -f query='
  mutation { resolveReviewThread(input: {threadId: "THREAD_ID"}) { thread { isResolved } } }
'
```

複数 thread を一度に対応した場合は **修正と関連付けて** resolve する (バルクで全部 resolve するな)。 各 thread に「この commit で対応」 と書ければ尚良いが、 commit message に thread ID を含めるだけでも十分。

#### 3-B. issue-level comment (`issue_comments`)

GitHub 標準の resolve 機構が無いため、 「対応済」 マークは **コメント本文を `<details><summary>対応済</summary>` で囲み、 末尾に `--> {commit-hash} で対応済` を付与** する convention で代替する (この PR で確立済の運用)。

対応手順:

1. `gh api repos/<O>/<R>/issues/comments/<id> -q .body` で原文取得
2. 内容を理解、 必要な修正を実装 → commit → push
3. `gh api -X PATCH repos/<O>/<R>/issues/comments/<id> -f body="$NEW_BODY"` で本文更新:

```bash
NEW_BODY=$(printf '<details><summary>対応済</summary>\n\n%s\n\n</details>\n\n--> %s で対応済' "$ORIG" "$COMMIT_HASH")
gh api -X PATCH "repos/<O>/<R>/issues/comments/<id>" -f body="$NEW_BODY"
```

複数の独立した点を含むコメントには、 1 件単位で対応 commit を分けて (`commit-hash-A (#XX 対応)`, `commit-hash-B (#YY 対応)` のように) summary に併記する。

**見逃し防止**: ループ毎に `gh api repos/<O>/<R>/issues/<pr>/comments` で **全コメント** を取得し、 `<details><summary>対応済` ラップ無しを 1 件以上検出したら **`unresolved` 1+ と同等の扱い** で「変化あり」 ([Step 5: 終了判定]) としてカウント。 review thread のみ見て issue comment を見落とすと、 ユーザの follow-up 報告が放置される事故が起きる。

### Step 4: stack rebase 連鎖

PR の `baseRefName` が **別 PR のブランチ** (= 別 PR の `headRefName`) を指している場合 = stack。

例: `#181.baseRefName == #180.headRefName` なら #181 は #180 stack。

stack の親 (= base 側) PR で commit を push した後、 子 PR を rebase + force-push する:

```bash
git checkout <child.headRefName>
git rebase <parent.headRefName>
# rebase が clean に通った場合のみ
git push --force-with-lease origin <child.headRefName>
```

衝突した場合は **そのループでは諦めて** ユーザーに報告 (auto-merge は危険)。

stack 関係は **トポロジカル順** で処理: 親の修正と push が完了してから子を rebase する。

### Step 5: 終了判定 (10 回連続「変化なし」 → 終了)

全 PR の状態を再取得 (または Step 1 の状態を活用) し、 このループの結果を **「変化なし」** か **「変化あり」** に分類する:

- 「変化なし」 = 全 PR で `failures == [] && unresolved_review_threads == 0 && unwrapped_issue_comments == 0` **かつ** このループで自分が新しい commit を push していない
- それ以外 = 「変化あり」 (※ `unwrapped_issue_comments` は Step 1 / 3-B で取得した、 `<details><summary>対応済` ラップが付いていない issue-level コメント数)

`/loop` での自動繰り返し中にチャタリングを避けるため、 **連続 「変化なし」 streak が 10 に達したら 終了** する。 1 回でも 「変化あり」 が起きたら streak はリセット。

streak は `.local/tmp/pr-fix-loop-streak.txt` に永続化:

```bash
STREAK_FILE=.local/tmp/pr-fix-loop-streak.txt
mkdir -p "$(dirname "$STREAK_FILE")"
prev=$(cat "$STREAK_FILE" 2>/dev/null || echo 0)
if [[ "$LOOP_OUTCOME" == "no-change" ]]; then
  next=$((prev + 1))
else
  next=0
fi
echo "$next" > "$STREAK_FILE"
if (( next >= 10 )); then
  echo "[pr-fix-loop] 10 回連続で 変化なし — 終了"
  rm -f "$STREAK_FILE"  # 次回の起動でクリーンスタート
fi
```

ループ末尾の報告に **`(no-change streak: N/10)`** を必ず添える。 `10/10` に到達したら 「終了」 と明示。 ユーザーが `/loop` 自体を止めるかは別判断だが、 skill 側はこれを「もう用がない」 という signal として扱う。

## 実装メモ

### gh CLI が大前提

このスキルは `gh` (GitHub CLI) と `git` がローカルで動くことを前提にする。 認証は `gh auth status` で事前確認。 NG なら skill は即座にユーザーに報告してそのループは抜ける。

### owner / repo は origin から取得

PR list だけ受け取る場合、 owner/repo は `gh repo view --json owner,name -q '.owner.login + "/" + .name'` で derive。 multi-remote の場合は `origin` を優先。

### branch checkout に worktree を使うか

複数 PR を順次 checkout するとローカルの worktree が頻繁に切り替わる。 ユーザーが何か作業中なら接触しないよう、 ループ開始時に `git status -s` で **dirty なら skill 起動を中断**。 強制実行モードを入れたければ `--force-on-dirty` フラグを定義 (今は未実装、 必要になったら追加)。

### ログ取得の注意

`gh run view --log` は **workflow 全体が完了するまで返ってこない** ことがある。 個別 job の log は `gh api /repos/<owner>/<repo>/actions/jobs/<jobId>/logs` でほぼ即時取得できる。 workflow に `in_progress` の job が残っていても、 完了済み job の log は取れる。

### rerun のタイミング

`gh run rerun --failed` は **workflow が in-progress の間ブロックされる** (`This workflow is already running`)。 その場合は 「次回ループに送る」 と判定して、 その PR の transient failure 対応はスキップ。 強引に sleep して polling すると loop の token / 時間予算を食う。

### commit メッセージの粒度

各 PR の修正 commit は **意味ある単位で分割**。 例えば「lint fix + apiDump」 は 2 つの logical change なので別 commit にできるなら分ける。 ただし「単一の copilot review コメント対応で複数 file を直す」 ような場合は 1 commit でよい (review thread 単位)。

## 実例 (このスキルを生んだ session)

このスキルは Compose-Preview-Lab repo で PR を複数本並行に green まで持っていく作業を `/loop 5m /pr-fix-loop ...` で回した実績パターンをそのまま skill 化したもの。 1 ループあたりの典型的な動き:

- ループ 1: lint + apiCheck FAIL → fix-ci-lint / fix-ci-binary 風の手当 + push、 全 PR の review コメント対応 + resolve
- ループ 2: integrationTest 側の `.api` drift → apiDump + push、 stack 子 PR を 親 PR の上に rebase
- ループ 3: 全 in-progress、 何もせず待機 (= no-change 1 回目)
- ループ 4: transient infra failure (api.foojay.io HTTP 400) 検出 → workflow 完了後に rerun
- ループ 5..9: ステータスは緑のまま、 review もなし、 5 ループ続いたところで `10/10` streak に到達して 終了

各ループは自己完結し、 「やる事がなくなったら 報告して streak を 1 進める」 を守る。 10 連続 「変化なし」 のあいだは ユーザーが寝ていても skill が暴走しない。

## 出力 (各ループ末尾)

ループ末尾に 1 件、 以下のような短い表で報告:

```
| PR  | failures | in-progress | unresolved | 対応 |
|-----|----------|-------------|------------|------|
| 179 | 0        | 0           | 0          | (触らず — 既に green) |
| 180 | apiCheck | 1           | 0          | apiDump push、 CI rerun 待ち |
| 181 | 0        | 5           | 0          | rebase で #180 fix 取込み push |
```

最終行は **streak の現在値** を含めた 1 行で書く。 例:

- `変化なし (no-change streak 1/10) — 5 分後再確認`
- `変化あり (CI 進行中、 streak リセット) — 次回確認`
- `変化なし (no-change streak 10/10) — 終了`

`10/10` に到達したらその行で 「終了」 を明記し、 streak ファイルも消すこと (上の Step 5 のスニペット参照)。
