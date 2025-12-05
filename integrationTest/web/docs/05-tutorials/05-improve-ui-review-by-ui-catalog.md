---
title: UI カタログで Review 体験を向上する
sidebar_position: 5
---

:::warning

このページは生成 AI によって自動生成されたページです。

:::

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# UI カタログで Review 体験を向上する

UI 実装のレビューでは、コードだけでなく **実際の見た目や動き** を確認することが重要です。  
Compose Preview Lab の UI カタログ機能を使うと、Pull Request と連携した「プレビュー付きレビュー体験」を構築できます。

## ゴール

- 各 PR ごとに専用の UI カタログページを用意する  
- レビュワーがブラウザから直接 Preview を確認できるようにする  
- 差分のある Preview だけを効率よくレビューできるようにする

## 1. UI カタログを PR と紐付ける

まずは、[UI カタログを構築する](./preview-ui-catalog) で説明したように、`PreviewList` を使ったギャラリーを用意します。

```kt title="Desktop / Web エントリ例"
fun main() = previewLabApplication(
    previewList = app.PreviewList,
    // 必要なら featuredFileList や openFileHandler を設定
) {
    // ここにアプリ全体のテーマやレイアウトを定義
}
```

このアプリケーションを CI（例: GitHub Actions）でビルドし、  
GitHub Pages などの静的ホスティングにデプロイすることで、PR ごとに以下のような URL を発行できます：

```text
https://your-org.github.io/your-repo/preview-lab/?pr=123
```

:::tip ブランチごとにデプロイする
PR ごとに独立したブランチ（例: `preview/pr-123`）に Web ギャラリーをデプロイし、  
GitHub Actions のコメント機能を使って PR に URL を自動投稿する、というワークフローがよく使われます。
:::

## 2. OpenFileHandler でソースコードへジャンプ

レビュー中に「このボタン、どのファイルで実装されている？」と感じたとき、  
PreviewLab の `openFileHandler` を設定しておくと、1 クリックで IDE や GitHub 上の該当ファイルを開けます。

```kt title="previewLabApplication の例"
fun main() = previewLabApplication(
    previewList = app.PreviewList,
    openFileHandler = UrlOpenFileHandler("https://github.com/your-org/your-repo/blob/main")
)
```

これにより、PreviewLab の UI からソースコードへのリンクが表示され、  
レビュワーはクリックだけで GitHub 上の該当ファイル・行にジャンプできます。

## 3. 差分のある Preview だけをレビューする

`Featured Files` 機能を使うと、PR で変更されたファイルだけをまとめたグループを作成できます。

```text title=".composepreviewlab/featured/Under Review"
src/commonMain/kotlin/com/example/feature/profile/ProfileScreen.kt
src/commonMain/kotlin/com/example/feature/timeline/TimelineItem.kt
```

このグループを UI カタログ内から選択することで、**今回の PR に関係する Preview だけ** を素早く確認できます。

:::info レビューフローの一例
1. 開発者が PR を作成し、`Under Review` グループに対象ファイルを追加  
2. CI が PreviewLab ギャラリーをビルド & デプロイ  
3. PR のコメントにギャラリーへのリンクを自動投稿  
4. レビュワーはブラウザで UI を確認し、問題があればコードにジャンプしてレビュー  
:::

## 4. UI カタログからのコミュニケーション

PreviewLab の Events 機能を使うと、レビュー時の気づきを記録する仕組みも構築できます。

```kt
@Preview
@Composable
fun ReviewNotesPreview() = PreviewLab {
    val note = fieldValue { StringField("note", "ここにレビューコメントを書けます") }

    Column {
        Text("変更内容を確認したら、気になった点をメモしましょう。")
        TextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Review Note") },
        )
        Button(
            onClick = { onEvent("Review note submitted", note) },
        ) {
            Text("送信")
        }
    }
}
```

<EmbeddedPreviewLab
  previewId="FieldQuickSummary"
/>

## 5. ベストプラクティス

:::tip レビュー体験を最大化するために
- **UI カタログへのリンクを必ず PR 説明に含める**  
- **差分の多い PR では Featured Files を活用して重要な Preview を絞り込む**  
- **Events タブでユーザー操作の流れを確認し、バグの再現手順を明文化する**  
- **チームで「レビューガイドライン」を決め、UI カタログを前提にしたレビューを行う**  
:::

## 関連ドキュメント

- [Preview を収集して UI カタログを構築する](./preview-ui-catalog)  
- [Featured Files](../guides/featured-files)  
- [Events](../guides/events)  

