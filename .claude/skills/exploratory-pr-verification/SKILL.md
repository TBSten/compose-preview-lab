---
name: exploratory-pr-verification
description: PR の探索的検証で 5 並列 subagent を category 別に kick する際の運用規約。 PDCA / MCP / 設定ファイル拡張 / CI ログ / apiDump / ticket 起票 / PR コメント / gradle 隔離 / working tree / 時間管理 / 他 KCP 比較 / 各 cat 役割 / ループ終了処理 / メンテナ反応 latency trace / close 後 handshake を含む。 各探索 iter で subagent を kick する前に必読。
model: opus
---

# Exploratory PR Verification — 運用規約

PR の探索的検証で 5 並列 subagent を kick する際の標準ルール。 各 iter で subagent を起動する前に本ファイルを参照し、 prompt に必要事項を組み込む。

## 0. Core constraints (= 全運用の前提、 違反禁止)

過去 phase で **ユーザが 4 回以上繰り返し指摘した** 最重要ルール。 §1 以降の細則より優先する:

### Constraint A: 止まらない (= 4 回ユーザ指摘)

1 ループ完了通知後に「次どうしますか?」 とユーザに聞き返す行為は **禁止**。 §17 終了条件 list を機械的に通し、 未達なら次 iter を即 kick、 達成なら close 処理 → 終了報告。 メンテナ反応待ちで動的検証が薄い時間も止まらず ticket 整理 / FINAL-SUMMARY update / cluster 分析で deadline まで埋める。

過去事例: 4 回繰り返し指摘されるまで §17 が skill 化されず、 各 iter 完了後に聞き返す事故で総合効率 -20-30%。

### Constraint B: ユーザ指摘 → 即 skill 反映 (= 7 回ユーザ指摘)

ユーザが運用上の指摘 (新ルール / 追加観点 / プロセス改善) を **1 回でも** すれば、 即本 SKILL.md に反映する。 ad-hoc に適用 (= memory / 一時 prompt 調整) で済ませると、 次 iter 以降 drift する。

反映 flow:
1. 指摘を受けた turn で SKILL.md を Edit
2. commit (= 探索終了後に bulk 反映ではなく即時)
3. 反映済みであることをユーザに短く報告

過去事例: skill 化 lag = 平均 5-10 iter (全 20 iter の 25-50%)、 反省会自体も PR 作成後に依頼される始末。

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
   - source code (test fixture / sample preview) — **§9 の clean 化方針に従い sandbox 内のみ改変可**。 main の repository 直下の test / sample source は触らない (= revert で完全に戻すため、 sandbox 隔離が必須)
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
6. 累計コメント **8 通超** → 飽和警戒域、 narrow 絞り込みで P1 のみ
7. 累計コメント **12 通超** → no more hard limit (= PR が noise レベル)
8. メンテナ反応 latency が短い (< 1h) → active phase、 cluster 投稿 ROI 高 (但し noise リスク)

### 投稿前の動的 PoC 検証 (= iter 17 case B reverse 教訓)

**修正案を含む PR コメントを投稿する場合、 投稿前に最低 1 回は動的 PoC で検証する**。 過去事例: iter 17 cat2 の修正案 `@get:` qualifier 提案が iter 18 cat3 で **Kotlin compiler に構文禁止** と 5 platform 全 fail で disprove、 自己訂正コメントを次 iter で投稿する経路を強いた。

検証 checklist:
- [ ] 修正案を sandbox dir に適用 (`.local/<exploration-name>/tmp/iter<N>-poc/`)
- [ ] 主要 platform (jvm + android + klib) で `:compileKotlin*` を回す
- [ ] 期待挙動を assert (= apiDump diff / runtime behavior / compile error 有無)
- [ ] reverse 結果が出ないこと確認 (= compiler が syntax 禁止、 build 全 fail 等)

PoC が通れば PR コメント投稿、 通らなければ修正案を変えて再 PoC、 もしくは「fix 不能 = 明文化」 に切り替え。

## 8. gradle 隔離

各 cat に独立した project-cache:

```
--project-cache-dir=.local/.../gradle-isolation/cat<N>
-Dorg.gradle.daemon=false
```

注意: `--project-cache-dir` は `.gradle/` のみ隔離、 `<module>/build/` は project root で **物理共有**。 並列 build で task race の可能性あり (= 同 task が複数 cat で同時に走ると `<module>/build/` の output file が衝突する known issue)。

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

### deadline 動的変更 protocol

ユーザから deadline 変更指示が来た場合 (例: 「12:00 → 5:00 に変更」):

1. **即 cron update**: 既存 deadline cron を `CronDelete` → 新時刻で `CronCreate` (= one-shot、 deadline 直前 3 min fire 推奨)
2. **ticket reserve range 再計算**: 残 iter 数で reserve scheme を縮小 (= 残 iter 1-2 なら sequential mode 推奨)
3. **§17 終了条件 list を即チェック**: 新 deadline で「超過」 が早期 true なら close 処理発動
4. **進行中 subagent への影響評価**: deadline 短縮で in-flight subagent が間に合わない場合、 完了を待つか kill 判断
5. **FINAL-SUMMARY の deadline 表記更新**

過去事例: ユーザ「5 時超えたら一旦終了」 で 12:00 → 5:00 へ変更、 cron `e566872c` 削除 + `edc9068e` 新設で対応。 但し protocol が事前 skill 化されていなかったため判断が遅れた。

### 残 deadline < 1.5h での sequential mode 自動 trigger

deadline 残 1.5h を切ったら、 **5 並列 kick を禁止し sequential mode に自動切替**:

- 5 並列だと build/ race + working tree 衝突リスクが顕在化、 final clean-up に時間が取られる
- sequential mode = 1-2 cat focus、 25-30 分以内で完結
- final iter は **clean-up 兼ねた 1-2 件起票** で OK、 ROI 鈍化判定材料に使う

主管側が iter kick 前に `date` で残時間を計算、 < 1.5h なら sequential prompt template を選択。

## 11. 必読 file (各 cat 共通)

各 cat 起動時に subagent prompt で以下を必読指示:

- 本 SKILL.md (`<repo-root>/.claude/skills/exploratory-pr-verification/SKILL.md`) — **必ず最初に**
- `.local/<exploration-name>/FINAL-SUMMARY.md` (最新スナップショット、 例: `.local/exploratory-pr-186-187/FINAL-SUMMARY.md`)
- 直近 iter の log file (各 cat の前回結果)
- 関連 ticket file の本文 (重要 ticket のみ、 全件読む必要なし)

**主管側 (= subagent kick する側) も** 各 iter 開始前に本 SKILL.md を Read で再読する (= ユーザ追加指示の取りこぼし防止)。

### 環境整備の default sweep (= iter 1 で必ず回す)

各探索の **iter 1** で以下の sweep を default 実行 (= release blocker / docs gap の早期発見):

1. **docs site grep**: `grep -rn "<主機能 keyword>" docs/ README.md CHANGELOG.md` で 0 件 hit を確認 (= release blocker 候補、 過去事例 #0177 で iter 19 まで遅延)
2. **PR description grep**: `gh pr view <NNN> --json body --jq .body | grep "BREAKING"` で破壊的変更の明示確認
3. **api dump baseline 整合**: `git diff --stat -- '*.api' '*.klib.api'` で baseline drift 即確認
4. **CI warning baseline 数**: `gh run view <run-id> --log | grep -ic warning` で baseline 数を記録、 後 iter で増減観察
5. **CHANGELOG.md / RELEASE-NOTES.md 存在確認** (`find . -name "CHANGELOG*"` で 0 件なら #0186 系の C-7 family 候補)

これらは static で時間 < 5 min、 iter 1 cat1 / cat2 で必ず回す。

### log 完全保存ルール (= user CLAUDE.md 由来)

cat3 / cat4 / cat5 の動的検証で実行する gradle / mcp / gh command の出力は、 **完全保存**:

```bash
./gradlew apiDump --continue > .local/<exploration-name>/tmp/iter<N>-cat<X>-<ts>-apiDump.log 2>&1
gh run view <run-id> --log > .local/<exploration-name>/tmp/iter<N>-cat<X>-<ts>-gh-run.log
```

**禁止事項** (= 出力が多くても全文保存する方針への違反):
- `| grep` / `| head` / `| tail` などのパイプによる **出力切り捨て** (warning / deprecated info も保存対象)
- log を tmp dir に保存せず stdout に流す

**OK な書き方** (= 推奨例で使っている `2>&1` は **stderr を stdout に合流して保存** する用途であり、 切り捨てではない):
- `> file 2>&1` (stderr を stdout にマージしつつ全文を file に保存)
- `tee` (画面出力と file 保存の両立)

切り捨てたいケースでも、 raw log を **まず file に全文保存** してから別 step で `grep` / `head` するか、 `awk '/pattern/' "$logfile"` で本文ファイルから抽出する形にする。

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

対象 PR の現実装と対比し、 設計優劣を判定。 ticket 化候補があれば起票。

### cat5 は iter 1 から rotation in 推奨 (= 後付けではない)

過去事例で cat5 を iter 14 から「他 KCP 比較」 に rotate した結果、 iter 1-13 で P1 として扱った #0079 (KLIB IdSignature cross-jar dedup) が iter 17 cat5 で「Metro も同 limitation = release blocker から除外可能」 と判明、 P 評価を 12-13 iter 後に修正。 = **早期に他 KCP 業界 norm を確認していれば P 評価の妥当性が初期段階で確保できた**。

iter 1 cat5 配分:
- iter 1: 当該 PR の主要 KCP design pattern × 3-4 件 (Metro 必須 + 直近の同種 PR 比較対象 2-3 件)
- iter 2-3: 不足 angle を埋める比較
- iter 4 以降: angle 飽和したら他 cat に rotate

cat1-4 と並行して iter 1 から比較を回し、 設計選択の妥当性を early に audit。

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
   - **cron / 監視 task を全件 clean up**: `CronList` で全 job 列挙 → deadline 通知 cron 以外をすべて `CronDelete`、 `<<autonomous-loop>>` `<<autonomous-loop-dynamic>>` 系 sentinel が close 後に fire する事故を防止 (= 過去事例で `b6efca95` autonomous-loop が close 後に fire、 静かに終了したが整理不足)
   - working tree clean 最終確認 (`git status --porcelain` で untracked が想定内のもののみ)
   - **反省会 (KPT) の実施**: Keep / Problem / Try を整理、 改善案を本 SKILL.md に反映してから skill 化を完了 (= 過去事例で PR 作成後に反省会依頼が来て後追い修正した教訓)

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

## 19. 反省 meta-analysis (= 過去 phase の指摘累計)

過去 phase で **どのテーマがユーザに何回指摘されたか** を記録し、 重要度判断と次 phase の skill 改善に使う:

| 指摘テーマ | ユーザ指摘回数 | 反映先 section | skill 化 lag |
|---|---:|---|---:|
| 止まらない (loop 継続) | 4 | §0 Constraint A + §17 | iter 17 以降 (= 80% 経過) |
| skill 化 cycle | 7 | §0 Constraint B + §17 step 5 | iter 13 以降 |
| MCP / PDCA 活用 | 4 | §3 | iter 13 以降 |
| 他 KCP / Metro 比較 | 3 | §12 | iter 14 (= 70% 経過) |
| CI ログ / api dump | 3 | §4 / §5 | iter 8 / iter 13 |
| 設定ファイル幅広く | 1 | §3 step 2 | 同 turn |
| 並列数 / 競合 | 1 | §8 | 同 turn |
| ライブラリユーザ pattern | 1 | context/04 (= 個別 phase) | 同 turn |
| deadline 動的変更 | 1 | §10 | 同 phase 内 |
| 反省会 cycle | 1 | §17 step 5 | 同 phase 内 (PR 作成後) |

### lag pattern

- **指摘回数 1 回 = 同 turn 反映 (= ベスト)**
- **指摘回数 2-3 回 = phase 中盤反映 (= 改善余地あり)**
- **指摘回数 4 回以上 = 大幅 lag (= core constraint への昇格を検討)**

= ユーザが繰り返し指摘するルールほど skill 化が遅れる傾向 (= 知見の depth と skill 化の coupling 不足)。

### 次 phase の improvement 指標

- 全 lag を「同 turn 反映 (= 1 回指摘で skill 化)」 に近付ける
- 探索開始前に過去 phase の §19 table を読み、 既知の重要 constraint (= §0) を default 適用
- phase 終了時 (§17 step 5) に新 indication を §19 に追記

## 20. 累積 ticket family cluster (各探索 phase の最終 snapshot)

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
