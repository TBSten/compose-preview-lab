---
title: Property-based testing
sidebar_position: 2
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import ComposePreviewLabVersion from "@site/src/components/ComposePreviewLabVersion"

# Property-based testing

PreviewLab は Property-based testing との相性が良く、Field の `testValues()` API を使って効率的にテストを記述できます。

このページでは kotest property と Compose Preview Lab を組み合わせて Property-based testing を実装する方法を紹介します。

- Compose Preview Lab を使ったテストについては [Basic](./basic) を確認してください。
- Property-based testing については [kotest のドキュメント](https://kotest.io/docs/proptest/property-based-testing.html)
  を参照してください。

## セットアップ

kotest の property 依存関係をセットアップします。
テスト環境のセットアップについては [Basic](./basic) を参照してください。

<Tabs>
  <TabItem value="cmp" label="Compose Multiplatform">

<table>
<tr>
<th> `<kotest-version>` </th>
<td> [See Kotest release note](https://github.com/kotest/kotest/releases) </td>
</tr>
</table>

```kt
kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation("io.kotest:kotest-property:<kotest-version>")
        }
    }
}
```

  </TabItem>
  <TabItem value="android-jvm" label="Android / JVM">

<table>
<tr>
<th> `<kotest-version>` </th>
<td> [See Kotest release note](https://github.com/kotest/kotest/releases) </td>
</tr>
</table>

```kt
dependencies {
    testImplementation("io.kotest:kotest-property:<kotest-version>")
}
```

  </TabItem>
</Tabs>

## testValues() API

各 Field は `testValues()` メソッドを持っており、テストに有用な値のリストを返します。

```kotlin
interface PreviewLabField<Value> {
    // ...
    // highlight-next-line
    fun testValues(): List<Value>
}
```

### 各 Field の testValues() の挙動

| Field             | testValues() の内容                     |
|-------------------|--------------------------------------|
| デフォルト             | `[initialValue]`                     |
| `BooleanField`    | `[true, false]`                      |
| `SelectableField` | 選択可能なすべてのオプション                       |
| `EnumField`       | すべての enum 値                          |
| `NullableField`   | `baseField.testValues() + [null]`    |
| `CombinedField`   | サブフィールドの testValues() の直積 (全ての組み合わせ) |

- NullableField など、他のフィールドと組み合わせるタイプの Field は、ベースとなるフィールドの testValues() と自身の仕様を組み合わせて値を返します。
- 被りがある場合は重複しないように自動で取り除かれます。

```kt title="例"
// Preview 内
val str: String? =
    fieldValue {
        SelectableField("str", listOf("a", "b", "c"))
            .nullable()
    }

// Test
val strField by state.field<String?>("str")
strField.testValues() // will be: ["a", "b", "c", null]
```

## kotest-property との連携

[kotest-property](https://kotest.io/docs/proptest/property-based-testing.html) の Arb/Exhansive と組み合わせることで、ランダムな値と
`testValues()` のエッジケースを両方テストできます。

```kotlin
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.plusEdgecases
import io.kotest.property.forAll

@Test
fun `IntField should update preview when value changes`() = runDesktopComposeUiTest {
    val state = PreviewLabState()
    setContent { TestPreviewLab(state) { MyPreview() } }

    val countField by state.field<Int>("Count")

    // highlight-start
    // ランダムな int 値 + testValues() のエッジケースをテスト
    forAll(Arb.int().plusEdgecases(countField.testValues())) { intValue ->
    // highlight-end
        countField.value = intValue
        awaitIdle()

        onNodeWithText("Count: $intValue")
            .assertExists()
        true
    }
}
```

### 様々な Field のテスト例

#### BooleanField

```kotlin
val enabledField by state.field<Boolean>("Enabled")

forAll(Arb.boolean().plusEdgecases(enabledField.testValues())) { boolValue ->
    enabledField.value = boolValue
    awaitIdle()
    // TODO assert
}
```

#### SelectableField / EnumField

```kotlin
val themeField by state.field<Theme>("Theme")

// testValues() にはすべての選択肢が含まれる
forAll(Arb.of(themeField.testValues()).plusEdgecases(themeField.testValues())) { theme ->
    themeField.value = theme
    awaitIdle()
    // TODO assert
}
```

#### NullableField

```kotlin
val userNameField by state.field<String?>("User Name")

forAll(Arb.string(1..20).orNull().plusEdgecases(userNameField.testValues())) { userName ->
    userNameField.value = userName
    awaitIdle()

    val expectedText = userName ?: "No user name"
    // TODO assert
}
```

## カスタム testValues の追加

既存の Field に対して `.withTestValues()` 拡張関数を使って、追加のテスト値を指定できます。

```kotlin
@Preview
@Composable
fun MyPreview() = PreviewLab {
    val count = fieldValue {
        IntField("Count", 0)
            // highlight-next-line
            .withTestValues(-1, 0, 1, Int.MAX_VALUE, Int.MIN_VALUE)
    }
    // ...
}
```

## カスタムフィールドの testValues()

カスタムフィールドを作成している場合は必要に応じて testValues() メソッドをオーバーライドする必要があります。

```kt
open class MyField(...) : MutablePreviewLabField<MyValue>(...) {
    // ...
    // highlight-next-line
    override fun testValues(): List<MyValue> = 
        super.testValues() + 
            listOf(testMyValue1, testMyValue2, ...)
```

:::danger[`testValues()` をオーバーライドする際は必ず `super.testValues()` に追加する形で追加の値を提供してください]

`super.testValues()` を呼び出さないと親要素の testValues() が無視されてしまいます。

:::
