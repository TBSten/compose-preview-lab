---
title: "Improve Review Experience with UI Catalog"
sidebar_position: 2
---

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# UI カタログで Pull request のレビュー体験を向上する

UI 実装のレビューでは、コードだけでなく **実際の見た目や動き** を確認することが重要です。  
Compose Preview Lab の UI カタログ機能 と Web ページのホスティングサービスを使うと、Pull Request と連携した「プレビュー付きレビュー体験」を構築できます。

このチュートリアルでは GitHub Pages を使って UI カタログをホスティングする方法を紹介します。

以下のチュートリアルに従って実装したサンプルリポジトリが Github にあるため、詰まったときは参考にしてください。

TODO サンプルリポジトリへのリンク

## ゴール

- 各 PR ごとに専用の UI カタログページを用意する  
- レビュワーがブラウザから直接 Preview を確認できるようにする  
- 差分のある Preview だけを効率よくレビューできるようにする

TODO 画像

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

## 2. UI カタログをデプロイする

このアプリケーションを CI（例: GitHub Actions）でビルドし、  
GitHub Pages などの静的ホスティングにデプロイすることで、PR ごとに以下のような URL を発行して Pull request からシームレスに Preview を確認できます。

```text
https://your-org.github.io/your-repo/preview-lab/?pr=123
```

GitHub Pages にデプロイするために以下のような workflow を作成します。

```yaml title=".github/workflows/preview-web.yml"
TODO
```

:::tip ブランチごとにデプロイする
PR ごとに独立したブランチ（例: `preview/pr-123`）に Web ギャラリーをデプロイし、  
GitHub Actions のコメント機能を使って PR に URL を自動投稿する、というワークフローがよく使われます。
:::

## 3. OpenFileHandler でソースコードへジャンプ

レビュー中に「このボタン、どのファイルで実装されている？」と感じたとき、  
PreviewLab の `openFileHandler` を設定しておくと、1 クリックで IDE や GitHub 上の該当ファイルを開けます。

```kt title="previewLabApplication の例"
fun main() = previewLabApplication(
    previewList = app.PreviewList,
    // highlight-next-line
    openFileHandler = GithubOpenFileHandler("your-org/your-repo")
)
```

これにより、PreviewLab の UI からソースコードへのリンクが表示され、  
レビュワーはクリックだけで GitHub 上の該当ファイル・行にジャンプできます。

TODO Gif

## 4. 差分のある Preview だけをレビューする

[`Featured Files`](../guides/featured-files) 機能を使うと、PR で変更されたファイルだけをまとめたグループを作成できます。

```text title=".composepreviewlab/featured/Under Pull Request #123"
src/commonMain/kotlin/com/example/feature/profile/ProfileScreen.kt
src/commonMain/kotlin/com/example/feature/timeline/TimelineItem.kt
```

このグループを UI カタログ内から選択することで、**今回の PR に関係する Preview** を素早く確認できます。

TODO 画像

Pull request に紐づいた `.composepreviewlab/featured/***` ファイルを作成するために、デプロイのワークフロー内に以下のような step を追加します。

このコマンドは Pull request で差分があるファイルを取得し、`.composepreviewlab/featured/***` ファイルを作成します。

```yaml title=".github/workflows/preview-web.yml"
TODO
```

## 5. Pull request を作成する

以上がセットアップできたら上記の変更を main ブランチで commit, push します。

続いて 動作確認用の Pull request を作成します。

例えば `feature/first-pull-request-with-preview-lab` ブランチで Pull request を作成します。

```bash
git checkout -b feature/first-pull-request-with-preview-lab main

# TODO 適当な Composable と その Preview を追加します

git commit -m "feat: add some composable"
git push origin feature/first-pull-request-with-preview-lab

# Github CLI をインストールしている場合
gh pr create --title "feat: add preview lab" --body "Add some composable"
# Github CLI ではなく Github の Web から Pull request を作成しても構いません
```

TODO 画像

## 関連ドキュメント

- [Preview を収集して UI カタログを構築する](./preview-ui-catalog)  
- [Featured Files](../guides/featured-files)  
- [Events](../guides/events)  
