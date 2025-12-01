---
title: イベントを伴う UI のテスト
sidebar_position: 2
---

:::warning

このページは生成 AI によって自動生成されたページです。

:::

# イベントを伴う UI のテスト

Events 機能と組み合わせると、「ボタンを押すとイベントが記録されるか？」といった振る舞いもテストできます。

```kt title="イベントが発火することを確認するテスト例"
@Test
fun `ButtonPrimary should render and trigger event on click`() = runDesktopComposeUiTest {
    val state = PreviewLabState()
    setContent { TestPreviewLab(state) { PreviewsForUiDebug.ButtonPrimary.content() } }

    awaitIdle()

    onNodeWithTag("PrimaryButton", useUnmergedTree = true)
        .assertIsDisplayed()
        .performClick()

    awaitIdle()

    // onEvent("onClick") が発火したことをカスタムタグなどで検証
    onNodeWithTag("onClick")
        .assertExists()
}
```

:::info テスト側からイベント一覧にアクセスする
現時点では Events ログ専用のテスト API はありませんが、  
`PreviewLabState` からイベント情報を取得するユーティリティを追加することで、  
「何回 onEvent が呼ばれたか」「どんなタイトルのイベントがあったか」を検証することも可能です。
:::

