---
title: Auto Testing
sidebar_position: 8
---

:::warning

このページは生成 AI によって自動生成されたページです。

:::

# Auto Testing

当初 Compose Preview Lab は手動テストをより効率的に行うために設計されていましたが、  
`PreviewLabState` や `TestPreviewLab` などの API を組み合わせることで、**自動テスト** にも活用できます。

:::info ここで扱うテストの種類
- 単体テスト（Compose UI テスト + PreviewLabState）  
- 将来的な拡張の方向性（テストコード自動生成 / Property based testing / VRT）  
:::

## 自動テストの基本構成

`dev` モジュールには、PreviewLab を使った UI テストのサンプルが用意されています。

```kt title="PreviewsForUiDebugTest.kt 抜粋"
@OptIn(ExperimentalTestApi::class)
class PreviewsForUiDebugTest {

    @Test
    fun `IntField should update preview when value changes`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { PreviewsForUiDebug.Fields.content() } }

        // 1. Field を取得して値を変更
        val intField = state.field<Int>("intValue")
        intField.value = 42

        awaitIdle()

        // 2. 画面上に反映されていることを検証
        onNodeWithText("intValue: 42")
            .assertIsDisplayed()
    }
}
```

ポイントは以下の通りです：

1. `PreviewLabState` をテスト用に生成する  
2. `TestPreviewLab(state) { ... }` で Preview コンテンツをラップする  
3. `state.field<T>("label")` で Field の値を書き換える  
4. Compose UI テスト API (`onNodeWithText`, `performClick` など) で画面の状態を検証する

### TestPreviewLab の役割

```kt title="TestPreviewLab.kt 抜粋"
@Composable
fun TestPreviewLab(
    state: PreviewLabState,
    viewModelStoreOwner: ViewModelStoreOwner = defaultTestViewModelStoreOwner(),
    lifecycleOwner: LifecycleOwner = defaultTestLifecycleOwner(),
    block: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalViewModelStoreOwner provides viewModelStoreOwner,
        LocalLifecycleOwner provides lifecycleOwner,
        LocalPreviewLabState provides state,
    ) {
        block()
    }
}
```

テスト環境でも PreviewLab が必要とする CompositionLocal（`PreviewLabState`, `LifecycleOwner`, `ViewModelStoreOwner`）をセットアップしてくれるヘルパーです。

:::tip 最小構成でテストを書く
1. `PreviewLabState()` を生成  
2. `setContent { TestPreviewLab(state) { MyPreviewContent() } }`  
3. `state.field<T>("label")` で Field を操作  
4. `onNodeWithText(...).assertIsDisplayed()` などで検証  
:::

## イベントを伴う UI のテスト

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

## テストコードの自動生成（構想）

現在、Compose Preview Lab では **テストコード自動生成** 機能はまだ実装されていませんが、  
次のような方向性が Issue で議論されています。

- Preview に付与されたメタデータ（`PreviewList` / `PreviewLabState`）を元に、  
  フィールド更新やイベント発火の最小テストを自動生成する
- 単純な「表示されるだけのテスト」を自動生成し、手動テストから自動テストへの移行コストを下げる

関連 Issue:  
[https://github.com/TBSten/compose-preview-lab/issues/74](https://github.com/TBSten/compose-preview-lab/issues/74)

## Property based testing（構想）

Property based testing との相性も良く、以下のようなアイデアが検討されています。

- Field に対してランダムな値（あるいはジェネレータ）を流し込み、  
  UI がクラッシュしない / 特定のプロパティを常に満たすことをチェックする
- `PreviewLabState` の Field に一括で値を適用するためのヘルパーを用意し、  
  「あらゆる入力パターンに対して例外が発生しない」ことを自動検証する

関連 Issue:  
[https://github.com/TBSten/compose-preview-lab/issues/75](https://github.com/TBSten/compose-preview-lab/issues/75)

## VRT (Visual Regression Testing)（構想）

Visual Regression Testing（ビジュアルリグレッションテスト）においても、PreviewLab は有用な基盤になり得ます。

:::info 典型的な VRT フロー（構想）
1. PreviewLabGallery から全 Preview を一括レンダリング  
2. 各 Preview のスクリーンショットを取得  
3. 以前のスナップショットと比較し、差分が一定以上あれば失敗とする  
4. 差分が発生した Preview だけを人間がレビューする
:::

現時点では VRT 専用の API はまだありませんが、  
`PreviewList` と Compose UI のスクリーンショット取得機能を組み合わせることで、  
将来的に自動化された VRT ワークフローを構築できる余地があります。

関連 Issue:  
[https://github.com/TBSten/compose-preview-lab/issues/20](https://github.com/TBSten/compose-preview-lab/issues/20)


