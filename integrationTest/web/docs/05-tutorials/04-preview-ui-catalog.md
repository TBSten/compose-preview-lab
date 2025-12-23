---
title: "[TODO] Preview を収集して UI カタログを構築する"
sidebar_position: 4
---

:::warning

このページは生成 AI によって自動生成されたページです。

:::

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# Preview を収集して UI カタログを構築する

このページでは、Compose Preview Lab を使って **UI カタログ（コンポーネント一覧）** を構築する方法を紹介します。

## ゴール

- プロジェクト内のコンポーネントを Preview 単位で整理する  
- PreviewLabGallery を使って「UI カタログ」画面を作る  
- 各コンポーネントのバリエーションや状態を手早く確認できるようにする

## 1. プロジェクト構成の例

```text
src/
  commonMain/kotlin/
    components/          # 実際のコンポーネント
      Button.kt
      TextField.kt
      Card.kt
    previews/            # Preview 専用
      ButtonPreviews.kt
      TextFieldPreviews.kt
      CardPreviews.kt
```

:::tip Preview 用ソースを分けるメリット
- プロダクションコードと Preview 用コードを分離できる  
- Previews ディレクトリを眺めるだけで「どんな UI があるか」が把握しやすくなる  
- 不要になった Preview の整理もしやすい  
:::

## 2. 各コンポーネントの Preview を作成する

例えばボタンコンポーネントのバリエーションを管理する Preview は、次のように記述できます。

```kt title="ButtonPreviews.kt"
@Preview
@Composable
fun ButtonVariantsPreview() = PreviewLab {
    val variant by fieldState {
        EnumField("Variant", ButtonVariant.Primary)
    }
    val enabled by fieldState {
        BooleanField("Enabled", true)
    }

    MyButton(
        text = fieldValue { StringField("Text", "Click me") },
        variant = variant,
        enabled = enabled,
        onClick = { onEvent("Button clicked") },
    )
}
```

<EmbeddedPreviewLab
  previewId="FieldQuickSummary"
  title="Field Quick Summary"
/>

## 3. PreviewLabGallery を使った UI カタログ画面

Compose Preview Lab では、KSP によって `PreviewList` が自動生成されます。  
これを `PreviewLabGallery` に渡すことで、簡単に UI カタログ画面を構築できます。

```kt
@Composable
fun App() {
    PreviewLabGallery(
        previewList = app.PreviewList,
    )
}
```

`PreviewLabGallery` は以下のような機能を提供します：

- 左側に Preview のリスト（カテゴリ / ファイル / displayName など）  
- 右側に選択中の Preview を表示  
- Inspector（Fields / Events / 追加タブ）を右ペインに表示

## 4. Featured Files で重要な Preview をグループ化

多数の Preview がある場合は、[Featured Files](../guides/featured-files) を使ってグループ化すると便利です。

```text
.composepreviewlab/featured/
  Important
  Under Review
  Core Components
```

```text title=".composepreviewlab/featured/Core Components"
src/commonMain/kotlin/com/example/ui/components/Button.kt
src/commonMain/kotlin/com/example/ui/components/TextField.kt
src/commonMain/kotlin/com/example/ui/components/Card.kt
```

`composePreviewLab { generateFeaturedFiles = true }` を有効にすると、  
`FeaturedFileList` が生成され、UI カタログ画面から「重要な Preview だけを見る」といった使い方ができます。

## 5. ベストプラクティス

:::tip UI カタログ構築時のチェックリスト
- 1 コンポーネントにつき **複数の Preview** を用意する（variants / states / sizes）  
- Preview 名は用途がわかるようにする（`PrimaryEnabled` / `ErrorState` など）  
- すべての主要な Props を Field で制御できるようにする  
- 必要に応じて Additional Tabs（InspectorTab）を追加し、ドキュメントやコードスニペットを同じ画面に表示する  
:::

## 6. 次のステップ

- [UI カタログで Review 体験を向上する](./improve-ui-review-by-ui-catalog) で PR レビューとの連携方法を学ぶ  
- [Featured Files](../guides/featured-files) で Preview のグループ管理を詳しく知る  
- [Inspector Tab](../guides/inspector-tab) でカスタムタブを追加し、ドキュメントや設計情報を UI カタログ内に埋め込む  

