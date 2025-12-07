---
title: Basic
sidebar_position: 1
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import ComposePreviewLabVersion from "@site/src/components/ComposePreviewLabVersion"

# Basic for Testing with Compose Preview Lab

Compose Preview Lab を使った自動テストを支援するパッケージが用意されています。
テスト用ソースセットに追加してください。

<Tabs>
  <TabItem value="compose-multiplatform" label="Compose Multiplatform" default>

<table>
<tr>
<th> `<compose-preview-lab-version>` </th>
<td> <ComposePreviewLabVersion /> </td>
</tr>
</table>

```kts
kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation("me.tbsten.compose.preview.lab:testing:<compose-preview-lab-version>")
        }
    }
}
```

  </TabItem>
  <TabItem value="android-jvm" label="Android / JVM">

<table>
<tr>
<th> `<compose-preview-lab-version>` </th>
<td> <ComposePreviewLabVersion /> </td>
</tr>
</table>

```kts
dependencies {
    testImplementation("me.tbsten.compose.preview.lab:testing:<compose-preview-lab-version>")
}
```

  </TabItem>
</Tabs>

## どのようなテストができるか？

- TODO 箇条書き

## 基本構成

Compose Preview Lab を使ったテストは基本的に以下のステップでテストを記述することが推奨されています。

1. PreviewLabState をインスタンス化する。
2. `TestPreviewLab(state)` で state を PreviewLab に登録する。これにより、Preview 内で定義した Field や 呼び出された Event が
   PreviewLabState 内に保持されます。
3. `PreviewLabState.field<フィールドの型>(ラベル)` を使って Field を取得する。
4. `Field.value` を使って Field の値を取得したり、Field の値を更新する。

以下にサンプルコードを示します。

<Tabs>
  <TabItem value="test" label="テストコード">

```kt
// 1. PreviewLabState をインスタンス化
val state = PreviewLabState()

// 2. `TestPreviewLab(state)` で state を PreviewLab に登録
composeRule.setContent {
    TestPreviewLab(state = state) {
      // TestPreviewLab 内で呼び出される PreviewLab に state が自動的に設定されます。
      myApp.PreviewList.MyButtonPreview()
    }
}

// 3. `PreviewLabState.field<フィールドの型>(ラベル)` を使って Field を取得
val textField: PreviewLabField<String> by state.field<String>("text")

// 4. `Field.value` を利用する
assertEquals("Click Me!", textField.value)
textField.value = "Click MyButton!"
```

  </TabItem>
  <TabItem value="target" label="MyButton.kt">

```kt
@Preview
@Composable
fun MyButtonPreview() = PreviewLab {
  MyButton(
    text = fieldValue { 
      StringField("text", "Click Me!") 
    }
  )
}
```

  </TabItem>
</Tabs>

## Compose Preview Lab を使ったテスト例

- カスタム Field のテスト
- [testValues() を用いて Property-based test する](./property-based-testing)
