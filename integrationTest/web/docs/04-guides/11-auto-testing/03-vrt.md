---
title: "[TODO] VRT (Visual Regression Testing)"
sidebar_position: 3
---

:::warning

このページは生成 AI によって自動生成されたページです。

:::

# VRT (Visual Regression Testing)

Visual Regression Testing（ビジュアルリグレッションテスト）においても、PreviewLab は有用な基盤になり得ます。

:::info 典型的な VRT フロー
1. PreviewLabGallery から全 Preview を一括レンダリング  
2. 各 Preview のスクリーンショットを取得  
3. 以前のスナップショットと比較し、差分が一定以上あれば失敗とする  
4. 差分が発生した Preview だけを人間がレビューする
:::

現時点では VRT 専用の API はまだありませんが、  
`testValues()` と Compose UI のスクリーンショット取得機能を組み合わせることで、  
将来的に自動化された VRT ワークフローを構築できる余地があります。

関連 Issue:  
[https://github.com/TBSten/compose-preview-lab/issues/20](https://github.com/TBSten/compose-preview-lab/issues/20)

