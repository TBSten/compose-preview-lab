---
title: 基本構成
sidebar_position: 1
---

:::warning

このページは生成 AI によって自動生成されたページです。

:::

# 自動テストの基本構成

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

## TestPreviewLab の役割

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

