---
title: Events
sidebar_position: 3
---

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# Events

`Events` タブは、Preview 内で発生したユーザー操作や状態変化をログとして可視化するための機能です。  

ボタンタップやスクロールなどのイベントを記録することで、**「どの操作でどんな状態になったか」** を確認しやすくなり、デバッグやレビューが効率的になります。

## onEvent() でイベントを記録する

`PreviewLab` のスコープ内では `onEvent()` を呼び出すだけで、Events タブにログが追加されます。

イベントの引数 (`on~~` という名前になっていることが多いでしょう) に指定していた空ラムダ `{ }` に onEvent(ラベル) を指定するだけで、イベントの発生時にトーストが表示されるようになり イベント発生が可視化されます。

```kt
@Preview
@Composable
fun ButtonEventPreview() = PreviewLab {
    MyButton(
        text = fieldValue { StringField("text", "Click Me") },
        // highlight-next-line
        onClick = { onEvent("Button clicked") },
    )
}
```

`onEvent` を呼ぶたびに、Events タブには以下の情報が記録されます：

- イベント名（`title`）
- いつ発生したか（タイムスタンプ）
- イベントをクリックすると詳細メッセージ（`description`）が表示されます。

### 実際の動作を試す

次の Preview では、ボタンをクリックすると `onEvent("onClick")` が記録されます。右側の **Events** タブを開いて動作を確認してみてください。

<EmbeddedPreviewLab
  previewId="GetStarted"
  title="Events Example"
/>

## withEvent(): ハンドラーをラップして記録を楽にする

`withEvent` ヘルパーを使うと、イベント処理と `onEvent` 呼び出しを 1 行でまとめられます。

```kt
@Preview
@Composable
fun WithEventExamplePreview() = PreviewLab {
    Button(
        text = "",
        // highlight-next-line
        onClick = withEvent("Button clicked"),
    )
}
```

`withEvent` はイベントハンドラ関数をラップして、処理の前後で自動的にイベント記録 (`onEvent`) を呼び出します。
これにより、「イベントの記録」と「本来のイベント処理」の両方を簡潔に 1 行でまとめることができます。

また、以下のように onEvent() 呼び出しに加えて追加で処理を加えるためのオーバーロードも存在します。

```kt
@Preview
@Composable
fun WithEventExamplePreview() = PreviewLab {
    val value by fieldState { StringField("value", "") }

    TextField(
        value = value,
        // highlight-next-line
        onValueChange = withEvent("onValueChange") { value = it },
    )
}
```


## Events タブで確認できること

`PreviewLab` を実行している画面の右側に **Events** タブが表示されます。

- 発生したイベントの一覧（新しいものが上に追加される）
- イベント名（`title`）
- 発生時刻（タイムスタンプ）
- オプションの `description`（追加情報）
