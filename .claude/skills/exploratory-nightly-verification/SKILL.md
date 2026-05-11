---
name: exploratory-nightly-verification
description: >
  Nightly Checking ワークフロー上で 60 分 budget の単発探索的検証を行う運用規約。
  PR 差分ではなく main の最新を対象に、 ライブラリ本体・integrationTest・docs 全体を
  多角的に揺さぶり、発見都度 `.local/nightly-exploration/issues/` へ Markdown 起票する。
  Kotlin / Compose / Gradle / AGP の最新 release 情報も探索範囲に含める。
  Use when requested: "nightly 探索", "夜間探索的検証", "exploratory-nightly".
model: opus
---

# Exploratory Nightly Verification

GitHub Actions の Nightly Checking ワークフロー (`anthropics/claude-code-action`) から
このスキルを起動して、 main の最新コードを 60 分 budget で探索的に検証する。

PR 差分ベースの `exploratory-pr-verification` とは別物。 ループ運用や PR コメント投稿、
ticket 番号予約、 メンテナ反応 latency 等の概念は **すべて廃止** している。

## 0. 最重要の不変条件

| # | 条件 | 違反した場合の影響 |
|---|------|--------------------|
| A | **逐次書き出し**: 発見ごとに即 `.local/nightly-exploration/issues/<NN>-<slug>.md` を書く | 最後にまとめて出力すると、 60 分タイムアウト時に丸ごと失われる |
| B | **時間予算は 60 分・50 分時点で打ち切り**: 残り 10 分は新規探索を止め、 起票済み issue の整形と書き込みに使う | 探索の途中で kill されると不完全な issue が出る |
| C | **issue ファイル名は連番**: `01-<kebab-slug>.md` から始まる 2 桁連番 | Discord 通知側が `sort` 前提でパースするため、 順序が壊れると整合性が崩れる |
| D | **PR への副作用は一切行わない**: コメント投稿、 issue 起票、 push、 branch 操作などはすべて禁止 | nightly は read-only 検証なので、 副作用は事故の元 |

## 1. ワークフロー（PDCA を 60 分 single shot で）

1. **環境確認（〜3 分）**
   - `pwd` / `git rev-parse HEAD` / `git status --short`
   - `cat gradle/libs.versions.toml | head -30` で現バージョン把握
2. **計画（〜5 分）**
   - `references/categories.md` を読み、 cat1 → cat5 のうち回す順序を決める
   - TodoWrite 等で各 cat の所要時間目安をメモ
3. **逐次探索（〜50 分）**
   - cat1 → cat5 の順で 1 つずつ、 各 8〜10 分目安
   - 発見ごとに即 issue file を書く（後述の固定フォーマット）
   - 1 件書いたら次の探索に移る
4. **終盤整形（50〜58 分）**
   - 既存 issue を読み直し、 重複統合・タイトル整形・本文の最終チェック
   - `.local/nightly-exploration/SUMMARY.md` に件数とカテゴリ別内訳を書き出す
5. **終了報告（〜60 分）**
   - SUMMARY.md と件数を最終 message に出して終わる

## 2. 探索カテゴリ

詳細は [`references/categories.md`](references/categories.md) を参照。 サマリだけ:

- **cat1 静的解析**: KDoc / Single source of truth / silent failure / 型設計 / TODO 残骸
- **cat2 CI ログ・apiDump baseline**: nightly 系の warning grep、 apiDump roundtrip、 docs 整合
- **cat3 動的ビルド・テスト**: `./gradlew jvmTest` `integrationTest` `docs` を必要に応じ走らせて failure / warning を拾う
- **cat4 外部依存リリース監視**: Kotlin / Compose Multiplatform / Gradle / AGP の最新 release を確認し、 libs.versions.toml と比較して乖離を起票
- **cat5 比較・残角度**: 他 KCP との比較、 過去 TODO grep、 BCP/SemVer 観点、 dev module の起動可否

並列 subagent kick は **しない**。 GitHub Actions の単一 VM 内で Claude 自身が cat1 → cat5
を sequential に回す。 これは時間予算とリソースの制約による。

## 3. issue の固定フォーマット

詳細は [`references/issue-format.md`](references/issue-format.md) を参照。 必須項目:

- ファイル名: `.local/nightly-exploration/issues/<NN>-<kebab-slug>.md`
  - `NN` は `01` から始まる 2 桁連番（書き出し順）
  - `<kebab-slug>` は半角英数とハイフン、 40 文字以内
- 本文 1 行目: `# <短いタイトル>` （日本語可、 60 文字以内）
- 続いて以下のメタ行と見出し:
  - `**カテゴリ**: cat<N> (<名前>)`
  - `**重要度**: P0 | P1 | P2 | P3`
  - `**検出時 commit**: <git rev-parse HEAD の先頭 12 文字>`
  - `## 再現手順`
  - `## 詳細`
  - `## 修正案（任意）`

タイトルとメタ行と `## 詳細` の先頭数行は Discord 通知にそのまま転載されるため、
**Discord で読んで意味が通る粒度で書く**。

## 4. 重要度の目安

| 重要度 | 目安 |
|--------|------|
| P0 | ライブラリ利用者の手元で確実にビルド失敗・実行失敗する |
| P1 | 公開 API の挙動が壊れている / セキュリティ懸念 / docs の致命的な誤り |
| P2 | 内部実装の不整合・改善余地・将来の brittleness |
| P3 | 体感ノイズ・小さな typo・改善提案 |

P0/P1 はその場で必ず起票。 P2/P3 は時間に余裕があるときに掘り下げる。

## 5. ツールの使い分け

- `Read` / `Glob` / `Grep`: 静的解析（cat1, cat2, cat5）
- `Bash`: gradle コマンド・git ・apiDump roundtrip（cat2, cat3）
  - 長時間 task は `timeout 8m` でガード（60 分 budget を保護）
  - 出力はログ切り捨てず `.local/nightly-exploration/tmp/<ts>-<cmd>.log` にリダイレクト
- `WebFetch`: Kotlin / Compose / Gradle / AGP の releases（cat4）
- `WebSearch`: cat5 で残角度を探す場合のみ

`.local/nightly-exploration/tmp/` 配下のログは artifact として残らない（issue だけが残る）ので、
**重要な発見はかならず issue Markdown に転記** する。

## 6. 想定外の失敗時の挙動

- `WebFetch` 失敗 / レート制限 → その事実自体を P3 issue として起票し、 次のカテゴリへ進む
- gradle コマンドの失敗 → 失敗内容を P1 として起票（CI 環境固有の問題なら P2 に下げる）
- スキル本体の読み込み失敗 → 起動できないので action 側の `if: always()` でカバー

## 7. 終了処理

- 探索ループを抜けたら `.local/nightly-exploration/SUMMARY.md` を書く（テンプレ）:

```markdown
# Nightly Exploration Summary

- 実施日時: <iso8601>
- 対象 commit: <git rev-parse HEAD>
- 発見件数: N 件

## カテゴリ別内訳
- cat1: N 件
- cat2: N 件
- cat3: N 件
- cat4: N 件
- cat5: N 件

## 重要度別内訳
- P0: N 件
- P1: N 件
- P2: N 件
- P3: N 件
```

- ループ運用はしない。 `/loop` も再 kick も呼ばない。
- 最後の message では「N 件起票」「SUMMARY.md を書いた」とだけ伝えて終わる
