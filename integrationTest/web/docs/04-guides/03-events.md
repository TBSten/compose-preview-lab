---
title: Events
sidebar_position: 3
---

:::warning

このページは生成 AI によって自動生成されたページです。

:::

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# Events

`Events` タブは、Preview 内で発生したユーザー操作や状態変化をログとして可視化するための機能です。  
ボタンタップやスクロールなどのイベントを記録することで、**「どの操作でどんな状態になったか」** を確認しやすくなり、デバッグやレビューが効率的になります。

## 基本: onEvent() でイベントを記録する

`PreviewLab` のスコープ内では `onEvent(title: String, description: String? = null)` を呼び出すだけで、Events タブにログが追加されます。

```kt
@Preview
@Composable
fun ButtonEventPreview() = PreviewLab {
    MyButton(
        text = fieldValue { StringField("text", "Click Me") },
        // highlight-next-line
        onClick = { onEvent("Button clicked", "ユーザーがボタンをタップしました") },
    )
}
```

`onEvent` を呼ぶたびに、Events タブには以下の情報が記録されます：

- いつ発生したか（タイムスタンプ）
- イベント名（`title`）
- 任意の詳細メッセージ（`description`）

### 実際の動作を試す

次の Preview では、ボタンをクリックすると `onEvent("onClick")` が記録されます。右側の **Events** タブを開いて動作を確認してみてください。

<EmbeddedPreviewLab
  previewId="GetStarted"
/>

## withEvent(): ハンドラーをラップして記録を楽にする

`withEvent` ヘルパーを使うと、イベント処理と `onEvent` 呼び出しを 1 行でまとめられます。

```kt
@Preview
@Composable
fun WithEventPreview() = PreviewLab {
    var text by fieldState { StringField("text", "") }

    TextField(
        value = text,
        // highlight-start
        onValueChange = withEvent("Text changed") { newValue ->
            text = newValue
        }
        // highlight-end
    )
}
```

`withEvent("Text changed") { ... }` は内部で `onEvent("Text changed")` を呼び出しつつ、元の処理 (`{ newValue -> ... }`) も実行します。

`withEvent` には引数の数に応じたオーバーロードがあり、`onClick()`, `onValueChange(value: String)` などさまざまなイベントをシンプルにラップできます。

## Events タブで確認できること

`PreviewLab` を実行している画面の右側に **Events** タブが表示されます。

:::info Events タブに表示される情報
- 発生したイベントの一覧（新しいものが上に追加される）
- イベント名（`title`）
- 発生時刻（タイムスタンプ）
- オプションの `description`（追加情報）
:::

<details>
<summary>Events タブの基本的な使い方</summary>

1. `PreviewLab { ... }` 内で `onEvent("...")` を呼び出します  
2. 画面右上の **Inspector** を開き、`Events` タブを選択します  
3. 各イベント行をクリックすると、詳細情報（`description` や発生時刻）を確認できます  
4. `Clear` ボタンを押すと、これまでのイベントログをリセットできます

</details>

## デバッグワークフローの例

Events 機能は、UI の動作確認やデバッグに非常に役立ちます。

```kt
@Preview
@Composable
fun LoginFormPreview() = PreviewLab {
    var email by fieldState { StringField("email", "user@example.com") }
    var password by fieldState { StringField("password", "") }

    Column {
        TextField(
            value = email,
            onValueChange = withEvent("Email changed") { email = it },
            label = { Text("Email") },
        )
        TextField(
            value = password,
            onValueChange = withEvent("Password changed") { password = it },
            label = { Text("Password") },
        )
        Button(
            // highlight-start
            onClick = withEvent(
                title = "Login clicked",
                description = "email=$email",
            ) {
                // 本来のログイン処理をここに記述
            }
            // highlight-end
        ) {
            Text("Login")
        }
    }
}
```

:::tip ベストプラクティス
- 意味のあるイベント名を付ける（\"onClick\" ではなく、\"LoginButton clicked\" など）  
- 重要なパラメータは `description` に含めて、後から動作を再現しやすくする  
- すべての主要なユーザー操作（クリック、スクロール、フォーム送信など）に `onEvent` / `withEvent` を仕込んでおく  
- `PreviewLabState` と UI テスト（`runDesktopComposeUiTest` など）を組み合わせて、イベントが期待どおりに発火しているか自動テストで検証する  
:::

