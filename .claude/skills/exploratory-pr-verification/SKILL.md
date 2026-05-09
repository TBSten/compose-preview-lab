---
name: exploratory-pr-verification
description: PR の探索的検証で 5 並列 subagent を category 別に kick する際の運用規約。 PDCA / MCP / 設定ファイル拡張 / CI ログ / apiDump / ticket 起票 / PR コメント / gradle 隔離 / working tree / 時間管理 / 他 KCP 比較 / 各 cat 役割 / ループ終了処理 / メンテナ反応 latency trace / close 後 handshake を含む。 各探索 iter で subagent を kick する前に必読。
model: opus
---

# Exploratory PR Verification — 運用規約

PR の探索的検証で 5 並列 subagent を kick する際の標準ルール。 各 iter で subagent を起動する前に本ファイルを参照し、 prompt に必要事項を組み込む。

## 1. 全体ワークフロー

1. **本 SKILL.md を再読** (1 ループ開始前に毎回。 ユーザの追加指示で update されている可能性あり)
2. **状態確認**: `git fetch origin <branch>` + `git rev-list --count HEAD..origin/<branch>` で force-push 検出
3. **必要なら hard reset** (`git reset --hard origin/<branch>`)、 working tree が dirty なら revert してから
4. **5 並列で explore subagent kick** (category 1-5、 すべて `model: opus`、 `run_in_background: true`)
5. **完了通知を 1 つずつ受領**、 結果まとめ
6. **新発見が P0/P1 なら PR コメント投稿** (重複避ける)
7. **ループ終了処理 (= §17 必須)**: 終了条件確認 → 未達なら次 iter を即 kick (= 止まらない)、 達成なら final summary 作成して終了

⚠️ **止まらないこと**: 1 ループ完了通知後に「次どうしますか?」 とユーザに聞き返さない。 §17 の終了条件 check list を機械的に回し、 未達なら自動で次 iter (= step 1 から) に戻る。 メンテナ反応待ちの場合は ticket 整理 / FINAL-SUMMARY update / cluster 分析 / 既存 ticket の重複統合などで時間を埋め、 deadline まで一切止まらない。

## 2. 5 カテゴリの役割

各 iter で 5 並列、 各 cat に **autonomous な angle 選択権** を渡す:

| cat | 担当領域 | gradle 必要 | 主な活動 |
|-----|---------|------------|---------|
| **cat1** | ソースコード静的 | ✗ | KDoc / SoT 違反 / type design / silent failure / comment drift / brittleness / semver |
| **cat2** | PR・環境・docs・CI | ✗ | PR description / README / docs site / **CI ログ + warning grep** / api dump 静的 / ChangeLog |
| **cat3** | 動的 build / test | ✓ (隔離 cache) | 主要 task 再走 / **apiDump roundtrip** / publishToMavenLocal jar 検査 / compat-k* |
| **cat4** | e2e / happy path | ✓ (隔離 cache) | **MCP 必須** (Playwright/Maestro/jetbrains/claude-in-chrome)、 dev module 改造で end-to-end |
| **cat5** | 比較 / 残角度 | ✗ or ✓ | **他 KCP 比較** (Metro/Compose Compiler/kotlinx.serialization/Power-Assert/kotlinx-rpc/SKIE/Koin Compiler/Mokkery/Poko) / stress 残 / locale / interop |

cat5 は飽和したら他比較に rotate (現方針)。

## 3. PDCA cycle (動的検証で必須)

cat3/cat4/cat5 等 動的検証時は **5 段階で実施**:

1. **環境整備**: gradle 隔離 cache 作成、 publishToMavenLocal、 dependency 確認
2. **設定変更**: 以下の **どのファイル** でも改変可:
   - `build.gradle.kts` (各 module + root + buildLogic)
   - `settings.gradle.kts` (composite build / module 構成 / `includeBuild`)
   - `gradle.properties` (kotlin compiler options / android settings / `org.gradle.jvmargs` / `kotlin.code.style` 等)
   - `libs.versions.toml` (version catalog / Kotlin / AGP / Compose 切替)
   - `apiValidation { ... }` ブロック / `ignoredPackages` / `ignoredKlibPackages` / `nonPublicMarkers`
   - `local.properties` (sdk path 等)
   - `consumer-rules.pro` / `proguard-rules.pro` (Android)
   - `.kotlin-version` / `.tool-versions`
   - source code (test fixture / sample preview)
3. **実際に動かす** ← **MCP 必須 (cat4)**:
   - Web (JS/WasmJs): `:dev:jsBrowserDevelopmentRun` で localhost 起動 + `mcp__playwright__browser_navigate`
   - Android: `:dev:installDebug` + `mcp__maestro__list_devices` `mcp__maestro__launch_app`
   - IDE: `mcp__jetbrains__execute_run_configuration` 等
   - 別 browser: `mcp__claude-in-chrome__*`
4. **出力確認** ← **MCP 必須 (cat4)**:
   - `mcp__playwright__browser_snapshot` / `browser_console_messages` / `browser_evaluate`
   - `mcp__maestro__inspect_view_hierarchy` / `take_screenshot`
   - cat3 は jar bytecode (`unzip -p ... | javap -v`) で確認
5. **(option) 条件変更で 1-3 戻る**: option 値 / scope 名 / preview 数を変えて再走

## 4. CI ログ確認 (cat2 必須)

```
gh run list --branch <branch> --limit 10 --repo TBSten/compose-preview-lab
gh run view <run-id> --log --repo TBSten/compose-preview-lab > .local/.../log/iter<N>-cat2-ci-<run-id>.log
grep -i -E "warning|deprecated|unsafe|unchecked" <log>
```

CI が green でも warning が大量にあれば隠れた品質問題。 baseline からの増減を観察し、 メンテナの新 fix で warning 数の変化を tracking。

## 5. apiDump roundtrip (cat3 必須)

```
./gradlew apiDump --continue --project-cache-dir=.local/.../gradle-isolation/cat3 -Dorg.gradle.daemon=false
git diff --stat -- '*.api' '*.klib.api'
```

差分 0 が期待。 差分があれば BCV baseline と実装が drift = ticket 化候補。 検査後 `git checkout -- '*.api' '*.klib.api'` で revert。

`integrationTest` 側も同様に。

## 6. ticket 起票 rule

### 番号と prefix
- 4 桁 prefix (`#0001`, `#0102` 等)
- 既存最大 + 1 から起票 (`ls problems/ | grep -E "^[0-9]{4}-" | sort | tail -1` で確認)
- 並列 subagent で番号衝突したら、 後発側を 後ろにずらして renumber + 内部 H1 修正

### サブディレクトリ
- `problems/<NNNN>-<slug>.md` — active
- `problems/resolved/<NNNN>-<slug>.md` — メンテナ fix で解消されたもの
- `problems/methodology/<NNNN>-<slug>.md` — 探索 process / subagent 運用系
- `problems/non-pr/<NNNN>-<slug>.md` — PR 由来でない外部要因

### ファイル format
```markdown
# NNNN. <タイトル>

## 重要度

P0 / P1 / P2 / P3

## 担当

cat1 (iter 14) — autonomous source 探索

## 場所

- `path/to/file.kt:line`

## 詳細

(再現手順 / 根拠 / 修正案)

## 修正案

案 (a) / 案 (b) ...
```

### 重複回避
- 既存 ticket 一覧を `ls problems/` で確認してから起票
- 同じ事象が既起票なら追加投稿しない (cluster 化検討)
- 重要度ラベルは動的検証 / メンテナ反応で再評価

### 並列 subagent 用の番号 reserve scheme

5 並列で番号衝突を避けるため、 主管側が事前に reserve range を割り当て、 subagent prompt に明記する:

- cat1: `NNNN+0` 〜 `NNNN+2` (3 件 reserve / iter)
- cat2: `NNNN+3` 〜 `NNNN+5`
- cat3: `NNNN+6` 〜 `NNNN+8`
- cat4: `NNNN+9` 〜 `NNNN+11`
- cat5: `NNNN+12` 〜 `NNNN+14`

各 cat 内で 1-2 件の起票で OK (= 短時間モード)、 3 件全部使う必要なし。 reserve 範囲外は次 iter に持ち越し。 衝突発生時は SKILL.md 既存ルール (= 後発側を後ろにずらして renumber + 内部 H1 修正) で復旧。

## 7. PR コメント

- **P0/P1 のみ** PR にコメント投稿 (P2/P3 は ticket のみ)
- 既コメントと **内容重複しない** こと (PR を荒らさない)
- 1 iter 分は **1 つのまとめコメント** が望ましい
- ticket file ID + 重要度 + 1 段落説明 + 修正案を含む

```bash
gh pr comment <NNN> --repo <repo> --body "$(cat <<'EOF'
## Iter <N> 探索 — P1 課題 X 件

...
EOF
)"
```

### 投稿判断 flow

1. 新発見が **P0/P1 のみ** → 投稿候補。 P2/P3 は ticket only
2. 同 iter 内 P1 が **2 件以上** → cluster 投稿 (1 通でまとめる)
3. 既投稿コメントへの **自己訂正** → 別投稿で訂正 (cluster と分けて履歴明示)
4. メンテナ最近の force-push が P1 を直接 fix → ticket を `resolved/` に移動、 投稿は次 P1 まで保留
5. 同 iter 内に既に 1 通投稿済 → 追加投稿は ROI 飽和判定 (saturation で見送り)
6. 累計コメント **10 通超** → 飽和域、 narrow 絞り込みで P1 のみ
7. メンテナ反応 latency が短い (< 1h) → active phase、 cluster 投稿 ROI 高 (但し noise リスク)

## 8. gradle 隔離

各 cat に独立した project-cache:

```
--project-cache-dir=.local/.../gradle-isolation/cat<N>
-Dorg.gradle.daemon=false
```

注意: `--project-cache-dir` は `.gradle/` のみ隔離、 `<module>/build/` は project root で **物理共有**。 並列 build で task race の可能性 (`_methodology-21` 既知)。

## 9. working tree の clean 化

- 探索中の改変は **検証後必ず revert**:
  - `git checkout -- <files>`
  - `rm -rf <untracked dirs>`
  - 一時 file は `.local/.../tmp/` に集約
- 各 cat 完了時に `git status --porcelain` で確認、 log に記録
- 並列 cat 間で working tree 衝突を避けるため、 touch 領域を分ける:
  - cat1: read-only
  - cat2: read-only
  - cat3: `.api` / `.klib.api` (apiDump 後 revert)
  - cat4: `dev/` `integrationTest/` (revert)
  - cat5: `integrationTest/uiLib/.../sample/cat5stress*/` のみ

### cat3 / cat4 の source 改変ルール (= incident 教訓)

並列 subagent が main project tree の source code を直接改変すると、 別 cat の build を block する事故が発生する (= cat3 が一時 probe file を残置 → cat4 の dev module build が `e:` 数件で fail した実例あり)。

source 改変が必要な場合は **必ず sandbox に隔離**:
- cat3: `.local/<exploration-name>/tmp/iter<N>-cat3/sandbox-app/` 等の専用 dir
- cat4: `.local/<exploration-name>/tmp/iter<N>-cat4/sandbox/` 等
- main project tree 直下 (= module の `src/`) は **read-only**、 一時 file 残置禁止

検証完了時に sandbox dir を削除 + `git status --porcelain` で main project tree が clean を確認、 完了レポートに状態明記。

## 10. 時間管理

- 各 cat は **60 分以内**
- **50 分時点で revert 開始** (timeout 対策、 cat4 の前例)
- 長時間 task は `timeout 30m` で kill
- 結果を log に記録してから完了報告

## 11. 必読 file (各 cat 共通)

各 cat 起動時に subagent prompt で以下を必読指示:

- 本 SKILL.md (`/Users/tbsten/dev/compose-preview-lab-2/.claude/skills/exploratory-pr-verification/SKILL.md`) — **必ず最初に**
- `.local/exploratory-pr-186-187/FINAL-SUMMARY.md` (最新スナップショット)
- 直近 iter の log file (各 cat の前回結果)
- 関連 ticket file の本文 (重要 ticket のみ、 全件読む必要なし)

**主管側 (= subagent kick する側) も** 各 iter 開始前に本 SKILL.md を Read で再読する (= ユーザ追加指示の取りこぼし防止)。

### log 完全保存ルール (= user CLAUDE.md 由来)

cat3 / cat4 / cat5 の動的検証で実行する gradle / mcp / gh command の出力は、 **完全保存**:

```bash
./gradlew apiDump --continue > .local/<exploration-name>/tmp/iter<N>-cat<X>-<ts>-apiDump.log 2>&1
gh run view <run-id> --log > .local/<exploration-name>/tmp/iter<N>-cat<X>-<ts>-gh-run.log
```

**禁止事項**:
- `>&1` `grep` `head` `tail` で出力切り捨て (= 出力が多くても全文保存)
- `gradle ... | grep error` のような pipe 切り捨て (= warning / deprecated info も保存対象)
- log を tmp dir に保存せず stdout に流す

完了後、 末尾と冒頭を Read で確認、 必要箇所を ticket file の `## 動的 evidence` に引用。

## 12. 他 KCP 比較 (cat5 推奨)

`mcp__deepwiki__ask_question` で k2 対応 + メンテナンスされてる KCP (Kotlin Compiler Plugin) と設計比較:

- **Metro** (ZacSweers/metro) — DI、 scope/qualifier — **必ず毎回比較対象に含める**(過去 iter で同 angle はスキップ可、 但し新 angle で継続)
- **Compose Compiler** (androidx/androidx) — `@Composable` 変換、 snapshot system
- **kotlinx.serialization** (Kotlin/kotlinx.serialization) — `@Serializable` FIR/IR 責務分担
- **Power-Assert** (kotlin/kotlin) — assertion message 拡張
- **kotlinx-rpc** (Kotlin/kotlinx-rpc) — RPC client / service
- **SKIE** (touchlab/SKIE) — Swift interop、 KMP iOS
- **Koin Compiler Plugin** (InsertKoinIO/koin-annotations) — DI annotation
- **Mokkery** (lupuuss/Mokkery) — KMP mocking
- **Poko** (drewhamilton/Poko) — `data class` 風 boilerplate 削減
- 他 k2 対応で メンテナンスされてる KCP

各比較で:
- CLI option 検証 / `CliOptionProcessingException` 採用有無
- scope / qualifier 検証の責務 (FIR / IR)
- synthetic declaration の naming / hash
- internal modifier の使い方
- error message スタイル (source location / Gradle DSL hint)

PR186/187 の現実装と対比し、 設計優劣を判定。 ticket 化候補があれば起票。

## 13. autonomous angle 選択

各 cat は固定 task ではなく、 既存 ticket / iter 履歴を見て **自分で角度を選ぶ**:

- 既存 ticket と重複しない
- これまでの iter で カバーが薄い領域
- 最新メンテナ fix の独立 review
- 新 fix の境界条件 / regression check

「何を探すか」 をユーザに聞き返さない (= autonomous 探索の本旨)。

## 14. メンテナ反応 watching

- 各 iter 開始時に `git fetch && git rev-list --count HEAD..origin/<branch>` で force-push 検出
- 0 でなければ hard reset、 新 commit を `git show --stat` で確認、 既存 ticket への影響整理
- メンテナの fix で解消された ticket は `_resolved-` を経由して `resolved/` サブディレクトリへ

### rebase squash / force-push 検出時の対応

`git fetch` 後 `git rev-list --count HEAD..origin/<branch>` が 0 でない、 かつ既存 commit が **squash 統合** されている場合:

1. `git log --oneline origin/<branch> -10` で commit history 確認
2. 既存 commit hash が **消失** していないか (= `feat! + chore(bcv)!: ...` のように複数 commit が 1 commit に squash されるパターン)
3. 消失している場合、 既起票 ticket で「メンテナの `<old-hash>` で fix」 等 hash 参照しているものを new hash で update (= traceability 維持)
4. 主要動的検証 (apiDump roundtrip / dev module 起動) を再実行、 squash 統合で動作変化がないか確認
5. squash 後は `git reset --hard origin/<branch>` で sync

## 15. PR コメント投稿実績の追跡

- 各 PR コメント URL は FINAL-SUMMARY.md に記録
- 投稿前に既コメント全件 (`gh pr view <NNN> --json comments`) を読み、 内容重複を避ける

## 16. メンテナ反応 latency trace (= 探索 phase signal 強度の指標)

PR 探索の signal 強度を測る指標。 各 PR コメント投稿後の **メンテナ着弾 latency** を tracking し、 メンテナの探索受領度合いを判定:

| 投稿 → 着弾 latency | メンテナ状態 | 推奨アクション |
|---|---|---|
| < 1h | active 修正 phase | P1 cluster の追加投稿 ROI 高 (但し noise リスク) |
| 1-3h | 通常 review phase | 1 iter 1 通の原則維持 |
| 3-9h | review pending / 別作業中 | 探索継続、 ticket 整理で時間埋め |
| > 9h | low priority / 反応薄 | 投稿頻度を下げ、 P1 でも cluster 待ち |

latency 短縮 (例: 9h → 40min) は **メンテナ active phase 入りの signal**、 deadline 直前修正集中を示唆 → 探索 phase close 検討材料。

各 iter 完了時に `gh pr view <NNN> --json comments --jq '.comments[-1].createdAt'` で前回投稿の latency を計算、 FINAL-SUMMARY に記録。

## 17. ループ終了時のチェック (= 1 iter 完了通知受領後に毎回)

5 並列 cat 全完了 (= 主管側に最終結果が揃った瞬間) の直後、 **必ず以下を機械的に実行**:

1. **本 SKILL.md を Read で再読** (ユーザが新規指示を追記している可能性あり、 §10 deadline / §17 終了条件が更新されているか確認)
2. **終了条件チェック list**:
   - [ ] deadline 超過か (= §10 の current deadline)
   - [ ] 累計 active ticket が ユーザ指定上限 を超えたか (例: 「20 issues 以上」)
   - [ ] ユーザから明示的な終了指示が新たに来たか
   - [ ] 5 連続 iter で新発見 P0/P1 が 0 件 + 探索 ROI 全 cat 鈍化判定
3. **どれにも該当しなければ即 次 iter kick** (= §1 step 1 へ)。 「次どうしますか?」 とユーザに聞き返してはいけない
4. **メンテナ反応待ちで動的検証が一時的に意味薄い場合**: 止まらず、 以下のどれかで時間を埋める:
   - ticket cluster 再整理 / merge 候補の cluster 化
   - FINAL-SUMMARY.md update
   - resolved/ 移動候補の再判定
   - 累積 ticket の P レベル再評価
   - 既存 ticket の文章 polish (PR コメント candidate 用)
5. **どれかに該当すれば終了処理**:
   - FINAL-SUMMARY.md に終了時 snapshot 記録
   - ユーザに終了報告 (deadline 達 / 累計件数 / P0/P1 残数 / メンテナ反応サマリ / 投稿 PR コメント数)
   - 終了後の cron / 監視 task は CronDelete で停止

この 5 段の check は **各 iter の閉じ際** に必ず通る。 ここを skip すると loop が止まる原因になる (= 過去事例)。

### autonomous-loop sentinel の取り扱い

cron / `ScheduleWakeup` 経由で `<<autonomous-loop-dynamic>>` sentinel が fire した場合:

1. **§17 終了条件チェック list を機械的に実行**
2. 4/4 既達 (= close 済) なら **次回起動をスケジュールせず静かに終了** (= `ScheduleWakeup` を call しない)
3. ユーザに「ループは終了します」 とのみ短く報告、 sentinel fire を無視しない (= 次に何が来てるかユーザに見えるように)

### final iter は sequential mode 推奨

deadline 直前の最終 iter は **5 並列ではなく 1-2 cat focus の sequential mode** で回す:

- 5 並列だと build/ race + working tree 衝突リスク (= cat 間で final clean-up 漏れの 2 次被害)
- final iter の探索 angle は **clean-up 兼ねた 1-2 件起票** で OK
- 25-30 分以内で完結、 deadline までの残時間で FINAL-SUMMARY 整備

## 18. close 後の handshake

§17 で close 処理が発動した後、 残 deadline 期間中の挙動:

- **メンテナ追加 fix 観察**: `git fetch` を発動的に、 新 commit があれば既起票 ticket への影響を update (= ticket 文書整理)
- **新規探索は kick しない** (= ROI 低、 close 判定の意味が消える)
- **ユーザ追加質問への対応はする** (= explore loop と独立、 通常の Q&A 応答)
- **cron / autonomous-loop sentinel が fire**: §17 の autonomous-loop sentinel 取り扱いに従う (= 静かに終了)
- **deadline 到達**: 終了報告 + cron 削除確認 + working tree clean 確認

## 19. 累積 ticket family cluster (各探索 phase の最終 snapshot)

各探索 phase 終了時に cluster を整理し、 follow-up PR の scope 設計材料にする。 family 分類例 (= 一般的に再利用可能な分類軸):

- **C-1 docs gap** — README / docs site / KDoc primary example が source の最新状態と drift
- **C-2 SoT 違反** — 同じ情報が複数箇所で hardcode、 const / utility 抽出で解消可能
- **C-3 silent failure** — error / warning が surface されず empty list / null が返る経路
- **C-4 BCV / ABI break** — `*.api` `*.klib.api` baseline と実装の drift、 `nonPublicMarkers` 設定漏れ
- **C-5 hint discovery / cross-module** — KCP の synthetic declaration 名衝突、 KLIB IdSignature dedup の限界
- **C-6 test brittleness** — test fixture の hash / FQN hardcode、 platform 依存の flaky
- **C-7 publish / supply chain** — Maven coordinates / artifact metadata / reproducible build
- **C-8 dev / dogfood gap** — dev module / integrationTest が main project の最新挙動を再現していない
- **C-9 process / methodology** — 探索 process そのものの改善 (例: 並列 cat 衝突、 build race)
- **C-10 BCV upstream limitation** — kotlinx-binary-compatibility-validator 自体の bug / 制約 (workaround 不可、 upstream issue)
- **C-11 IDE-CLI asymmetric** — IntelliJ K2 plugin と CLI compiler の error 報告不一致

新発見は family のいずれかに分類するか、 新規 family として独立扱い。

---

本 SKILL.md は探索の累積知識を集約した snapshot。 ユーザの追加指示があれば随時 update。
