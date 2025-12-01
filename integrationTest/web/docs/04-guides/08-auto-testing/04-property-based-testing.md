---
title: Property-based testing
sidebar_position: 4
---

# Property-based testing

PreviewLab は Property-based testing との相性が良く、Field の `testValues()` API を使って効率的にテストを記述できます。

## testValues() API

各 Field は `testValues()` メソッドを持っており、テストに有用な値のリストを返します。

```kotlin
interface PreviewLabField<Value> {
    // ...
    fun testValues(): List<Value>
}
```

### 各 Field の testValues() の挙動

| Field | testValues() の内容 |
|-------|---------------------|
| `IntField`, `FloatField`, `StringField` など | `[initialValue]` |
| `BooleanField` | `[true, false]` |
| `SelectableField` | 選択可能なすべてのオプション |
| `EnumField` | すべての enum 値 |
| `NullableField` | `baseField.testValues() + [null]` |
| `WithHintField` | hints に指定された値 |
| `CombinedField` | サブフィールドの testValues() の直積 |

## kotest-property との連携

[kotest-property](https://kotest.io/docs/proptest/property-based-testing.html) と組み合わせることで、ランダムな値と `testValues()` のエッジケースを両方テストできます。

### 基本的な使い方

```kotlin
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.plusEdgecases
import io.kotest.property.forAll

@Test
fun `IntField should update preview when value changes`() = runDesktopComposeUiTest {
    val state = PreviewLabState()
    setContent { TestPreviewLab(state) { MyPreview() } }

    val countField = state.field<Int>("Count")

    // ランダムな int 値 + testValues() のエッジケースをテスト
    forAll(Arb.int().plusEdgecases(countField.testValues())) { intValue ->
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
val enabledField = state.field<Boolean>("Enabled")

forAll(Arb.boolean().plusEdgecases(enabledField.testValues())) { boolValue ->
    enabledField.value = boolValue
    awaitIdle()
    true
}
```

#### SelectableField / EnumField

```kotlin
val themeField = state.field<Theme>("Theme")

// testValues() にはすべての選択肢が含まれる
forAll(Arb.of(themeField.testValues()).plusEdgecases(themeField.testValues())) { theme ->
    themeField.value = theme
    awaitIdle()
    onNodeWithText("current theme: $theme").isDisplayed()
}
```

#### NullableField

```kotlin
val userNameField = state.field<String?>("User Name")

forAll(Arb.string(1..20).orNull().plusEdgecases(userNameField.testValues())) { userName ->
    userNameField.value = userName
    awaitIdle()

    val expectedText = userName ?: "No user name"
    onAllNodesWithText(expectedText)
        .fetchSemanticsNodes()
        .isNotEmpty()
}
```

## カスタム testValues の追加

`withTestValues()` 拡張関数を使って、追加のテスト値を指定できます。

```kotlin
@Preview
@Composable
fun MyPreview() = PreviewLab {
    val count = fieldValue {
        IntField("Count", 0)
            .withTestValues(-1, 0, 1, Int.MAX_VALUE, Int.MIN_VALUE)
    }
    // ...
}
```

## 依存関係

```kotlin
// build.gradle.kts
dependencies {
    testImplementation("io.kotest:kotest-property:5.9.1")
}
```

## 関連リンク

- [kotest-property ドキュメント](https://kotest.io/docs/proptest/property-based-testing.html)
- [GitHub Issue #75](https://github.com/TBSten/compose-preview-lab/issues/75)
