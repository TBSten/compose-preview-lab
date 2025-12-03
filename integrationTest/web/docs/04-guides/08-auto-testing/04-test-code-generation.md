---
title: "【WIP】 テストコードの自動生成（構想）"
sidebar_position: 4
---

:::warning

このページは生成 AI によって自動生成されたページです。

:::

# テストコードの自動生成（構想）

現在、Compose Preview Lab では **テストコード自動生成** 機能はまだ実装されていませんが、  
次のような方向性が Issue で議論されています。

- Preview に付与されたメタデータ（`PreviewList` / `PreviewLabState`）を元に、  
  フィールド更新やイベント発火の最小テストを自動生成する
- 単純な「表示されるだけのテスト」を自動生成し、手動テストから自動テストへの移行コストを下げる

関連 Issue:  
[https://github.com/TBSten/compose-preview-lab/issues/74](https://github.com/TBSten/compose-preview-lab/issues/74)

