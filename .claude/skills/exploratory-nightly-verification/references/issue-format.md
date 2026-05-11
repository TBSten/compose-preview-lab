# issue Markdown 固定フォーマット

`.local/nightly-exploration/issues/<NN>-<kebab-slug>.md` に書き出す Markdown の規約。

Discord 通知ジョブ (`.github/scripts/build-nightly-discord-payload.sh`) がこのフォーマットを
パースして転載するため、 **項目名・見出しレベル・順序は厳格に守る**。

## ファイル名規約

- パス: `.local/nightly-exploration/issues/<NN>-<kebab-slug>.md`
- `<NN>`: 2 桁ゼロ埋め連番。 `01` から始める。 飛び番号や重複は禁止
- `<kebab-slug>`: 半角英数とハイフン、 40 文字以内。 主題が分かる単語で
- 例: `01-bcv-baseline-drift.md`, `02-kotlin-2-4-ga-not-tracked.md`

## 本文テンプレ

```markdown
# <短いタイトル>

**カテゴリ**: cat<N> (<名前>)
**重要度**: P0 | P1 | P2 | P3
**検出時 commit**: <git rev-parse HEAD の先頭 12 文字>

## 再現手順
- ...
- ...

## 詳細
<200 文字程度の説明。 Discord の通知本文に先頭数行が転載される>

## 修正案（任意）
- ...
```

## 必須項目

| 項目 | 値の例 | 備考 |
|------|--------|------|
| `# <タイトル>` | `# BCV baseline と compat-k* に乖離` | 1 行目に必ず置く。 日本語可、 60 文字以内 |
| `**カテゴリ**` | `**カテゴリ**: cat2 (CI ログ・apiDump baseline)` | `categories.md` の名称と一致させる |
| `**重要度**` | `**重要度**: P1` | P0 / P1 / P2 / P3 のいずれか |
| `**検出時 commit**` | `**検出時 commit**: 89b45afbc1a3` | `git rev-parse --short=12 HEAD` |
| `## 再現手順` | 箇条書き 2〜5 個 | Claude 以外が読んで再現できる粒度 |
| `## 詳細` | 200 文字程度 | Discord 抜粋の対象。 先頭 200 文字に「何が問題か」を凝縮 |

## 任意項目

- `## 修正案（任意）`: 修正方針の候補（あれば）
- `## 関連リンク`: PR / issue / docs / 外部 URL

## NG パターン

- 1 行目が `## ` から始まる（H1 を省略する）→ Discord 整形で空タイトルになる
- `**カテゴリ**` 行を `**Category**:` のような英語表記にする → スクリプトが拾わない
- 同じ事象を複数ファイルに書く（重複起票）→ 終盤整形で 1 件にマージする
- 連番をスキップする（`01` の次が `03`）→ 通知側の `## 探索的テスト N.` 番号がずれる
- 本文に PR コメントへの返信や TODO リストのような **作業ログ** を書く → 完結した issue として書く
