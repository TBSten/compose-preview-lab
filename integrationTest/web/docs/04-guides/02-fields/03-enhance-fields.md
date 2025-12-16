---
title: Enhance fields
sidebar_position: 3
---

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# Field を強化する

[All Fields](./all-fields) で紹介されている Field と組み合わせることで、カスタム Field を作成せずとも 独自の型に対応したり、Field
の編集 UI を簡単に作成できます。

TODO サンプルの PreviewLab

## SelectableField: 複数の選択肢から1つ選ぶ

SelectableField を使用することで、選択式で編集できる編集 UI を簡単に作成できます。
SelectableField の choices には任意の型を渡せます。

```kt
val theme =
    fieldValue {
        // highlight-next-line
        SelectableField(
            label = "theme",
            // highlight-next-line
            choices = listOf("Light", "Dark", "Auto")
        )
    }
```

<EmbeddedPreviewLab
previewId="FieldSelectable"
/>

デフォルトでは選択肢のテキストは `選択肢.toString()` が使用されますが、SelectableField には label を設定できるオーバーライドも存在します。
toString() すると長くなってしまう値は適宜 label を設定すると便利です。

```kt
fieldValue {
    SelectableField(label = "theme") {
        choice(MyUiState(isLoading = true, isError = true), label = "loading")
        choice(MyUiState(isLoading = false, isError = true), label = "error")
        choice(MyUiState(isLoading = false, isError = false), label = "success", isDefault = true)
    }
}
```

### 利用例

<details>
<summary>UiState を受け取る Screen Composable の Preview</summary>

```kt
@Preview
@Composable
private fun ThemeButtonPreview() = PreviewLab {
    val theme = fieldValue {
        SelectableField(
            label = "theme",
            choices = listOf("Light", "Dark", "Auto")
        )
    }
    MyThemeButton(theme = theme)
}
```

</details>

<details>
<summary>複雑な data model を受け取る必要がある Composable に渡す値をあらかじめ定義された値のみにして PreviewLab をシンプルにする</summary>

```kt
@Preview
@Composable
private fun UiStateButtonPreview() = PreviewLab {
    val state = fieldValue {
        SelectableField(label = "state") {
            choice(MyUiState(isLoading = true, isError = true), label = "loading")
            choice(MyUiState(isLoading = false, isError = true), label = "error")
            choice(MyUiState(isLoading = false, isError = false), label = "success", isDefault = true)
        }
    }
    MyUiStateButton(state = state)
}
```

</details>

<details>
<summary>PreviewParameterProvider を利用していた Preview に渡す引数を選択可能にする</summary>

```kt
@Preview
@Composable
private fun PreviewParameterProviderButtonPreview() = PreviewLab {
    val argument = fieldValue {
        MyPreviewParameterProvider().toField(label = "argument")
    }
    MyPreviewWithParameter(argument = argument)
}
```

[PreviewParameterProvider.toField()](https://github.com/TBSten/compose-preview-lab/blob/908ae45aa87e477263de2c4d6a30eb6f21e5f4bc/core/src/commonMain/kotlin/me/tbsten/compose/preview/lab/field/ComposableField.kt#L591)
が用意されているため、必要に応じて活用してください。

</details>

## Field.withHint(): 候補の選択肢を提示

`Field.withHint()` を使用すると、よく使う設定値を Field の下に表示できます。

```kt
val email = fieldValue {
    StringField("email", "user@example.com")
        // highlight-start
        .withHint(
            "User" to "user@example.com",
            "Admin" to "admin@example.com",
            "Test" to "test@example.com",
        )
        // highlight-end
}
```

<EmbeddedPreviewLab
previewId="FieldWithHint"
/>

Field.withHint() はあくまでも既存の Field の編集 UI によくある選択肢を追加するだけです。
事前に設定した値以外を渡したくない場合は SelectableField の使用を検討してください。

### 利用例

<details>
<summary>TextField.value に 適切なデフォルト値を付与するユーティリティ</summary>

```kt
// highlight-start
fun MutablePreviewLabField<String>.withTextHint(
    normal: String = initialValue,
    long: String = "Very ".repeat(50) + "long text !",
) = withHint(
    "Empty" to "",
    "Normal" to normal,
    "Long" to long,
)
// highlight-end

// usage
var text by fieldState {
    StringField("text", "normal")
        // highlight-next-line
        .withTextHint()
}
MyTextField(
    // highlight-start
    value = text,
    onValueChange = { text = it },
    // highlight-end
)
```

</details>

## Field.nullable(): Nullable 型をサポート

任意の Field から nullable 型を簡単に作成できます。

```kt
// Make StringField nullable for optional bio
val bio: String? = fieldValue {
    StringField("bio", "I love coding!")
        // highlight-next-line
        .nullable(initialValue = null)
}

// Make IntField nullable for optional timeout
val timeout: Int? = fieldValue {
    IntField("timeout", 30)
        // highlight-next-line
        .nullable()
}
```

<EmbeddedPreviewLab
previewId="FieldNullable"
/>

## CombinedField: 複数の Field を組み合わせる

複数の Field を合体して、1つの Field として扱うことができます。
複数のプロパティを持つ data class に対応する、完全に操作可能な Field を簡単に作成することができます。

**1つのフィールドを変換する例：**

```kt
data class UserId(val value: String)

val userId: UserId = fieldValue {
    combined(
        label = "User ID",
        field1 = StringField("ID", "user-001"),
        combine = { id -> UserId(id) },
        split = { splitedOf(it.value) }
    )
}
```

**複数のフィールドを結合する例：**

```kt
data class MyUiState(
    val title: String,
    val description: String,
    val isLoading: Boolean,
)

val uiState = fieldValue {
    CombinedField3(
        label = "uiState",
        field1 = StringField("title", "..."),
        field2 = StringField(
            "description",
            "...",
        ),
        field3 = BooleanField("isLoading", ...
    ),
    combine = { title, description, isLoading -> MyUiState(title, description, isLoading) },
    split = { splitedOf(it.title, it.description, it.isLoading) },
    )
}
```

<EmbeddedPreviewLab
previewId="FieldCombined"
/>

- `combine` は それぞれの Field の値 -> 合体した値に変換する関数です。それぞれの Field の値から現在の CombinedField の値を取得するために
- `split` は 合体した値 -> それぞれの Field に分割する関数です。それぞれの編集 UI を表示する際に使用されます。
- 標準では `combined` 関数が CombinedField1~10 まで用意されているため最大10個まで合体させることができます。11個以上の場合はライブラリの
  CombinedField 実装を参考に独自実装するか、型安全性が失われるものの任意の Field を合体させることができる CombinedField 合体する
  Field を List を受け取るバージョンの CombinedField を利用してください。

:::warning
CombinedField は深くネストさせたり、あまりに多い数のプロパティがあると編集 UI が非常に使いづらくなってしまいます。
その際は 代わりに固定値を数パターン用意し、SelectableField を利用することを検討してください。
:::

:::note

CombinedField の自動生成機能も検討しています。
詳しくは以下の Pull Request を参照してください。

[https://github.com/TBSten/compose-preview-lab/pull/78](https://github.com/TBSten/compose-preview-lab/pull/78)

:::

## TransformField: Field を変換する

<EmbeddedPreviewLab
previewId="FieldTransform"
/>

## Field.withValueCode(): Code タブに出力されるコードをカスタマイズ

`withValueCode()` を使用すると、Inspector の Code タブに表示される Kotlin コード表現を、UI や値の型とは独立してカスタマイズできます。

例えば、`ColorField` の値を Code タブでは `Color(0xFF6200EE)` ではなく `MyTheme.colors.primary` のような表現で出力したい場合に便利です。

### 基本的な使い方

`PreviewLabField` には `valueCode(): String` メソッドがあり、現在の値を Kotlin コードとして表現します。多くのビルトイン Field
では、適切なコード表現が自動的に生成されますが、拡張関数 `withValueCode { value -> "..." }` を使用することで、出力だけを差し替えることができます。

```kt
val text = fieldValue {
    StringField("text", "Hello")
        // highlight-start
        .withValueCode { value -> """"prefix-$value"""" }
    // highlight-end
}
```

この例では、Code タブには `"prefix-Hello"` と表示されます。

### カスタム型での使用例

`SelectableField` など、任意の型の Field に対して、Code タブでの表現をカスタマイズできます：

```kt
enum class Theme { Light, Dark, System }

val theme = fieldValue {
    SelectableField(
        label = "theme",
        choices = listOf(Theme.Light, Theme.Dark, Theme.System)
    )
        // highlight-start
        .withValueCode { value -> "Theme.${value.name}" }
        // highlight-end
}
```

Code タブでは `Theme.Light` のように表示されます。

### ラッパー Field との組み合わせ

`.withHint()`, `.nullable()`, `.withTestValues()` などのラッパー Field は、ベース Field の `valueCode()` を尊重します。そのため、まずベース
Field で `withValueCode {}` を呼び出してから、他のラッパーを適用するのが推奨パターンです：

```kt
val fontSize = fieldValue {
    SpField(label = "Font Size", initialValue = 16.sp)
        // highlight-start
        .withValueCode { value -> "TextUnit(${value.value}.sp)" }
        // highlight-end
        .withHint(
            "Small" to 12.sp,
            "Medium" to 16.sp,
            "Large" to 20.sp,
        )
}
```

詳細については、[Inspector Tab の Code タブの説明](../inspector-tab#inspectortabcode)も参照してください。

## Field.withSerializer(): Field のシリアライザを設定する

`withSerializer()` を使用すると、Field にカスタムシリアライザを設定できます。
設定されたシリアライザは Web ブラウザの query parameter として保存する際など 永続化のために利用されます。

### 基本的な使い方

```kt
@Serializable
enum class Theme { Light, Dark, System }

val theme = fieldValue {
    SelectableField(
        label = "theme",
        choices = Theme.entries,
        choiceLabel = { it.name },
    )
        // highlight-next-line
        .withSerializer(Theme.serializer())
}
```

### いつ使うか

- `SelectableField` でシリアライズ可能な enum や sealed class を使用する場合
- デフォルトで `serializer()` が `null` を返す Field にシリアライザを設定したい場合
- デフォルトのシリアライザとは異なるカスタムシリアライザを使用したい場合

### ビルトイン Field のシリアライザ対応状況

以下の Field はデフォルトでシリアライザを提供しています：

| Field             | Serializer                        |
|-------------------|-----------------------------------|
| `BooleanField`    | `Boolean.serializer()`            |
| `StringField`     | `String.serializer()`             |
| `IntField`        | `Int.serializer()`                |
| `LongField`       | `Long.serializer()`               |
| `ByteField`       | `Byte.serializer()`               |
| `DoubleField`     | `Double.serializer()`             |
| `FloatField`      | `Float.serializer()`              |
| `NullableField`   | ベース Field の nullable serializer |
| `ColorField`      | `ColorSerializer`                 |
| `DpField`         | `DpSerializer`                    |
| `SpField`         | `TextUnitSerializer`              |
| `OffsetField`     | `OffsetSerializer`                |
| `DpOffsetField`   | `DpOffsetSerializer`              |
| `SizeField`       | `SizeSerializer`                  |
| `DpSizeField`     | `DpSizeSerializer`                |
| `ScreenSizeField` | `ScreenSizeSerializer`            |
| `TransformField`  | ベース Field の serializer を変換    |

以下の Field はデフォルトで `null` を返します（シリアライズ不可）：

- `SelectableField` - 任意の型を扱うため（`withSerializer()` で設定可能）
- `PolymorphicField` - 多態性のため（コンストラクタで serializer を指定可能）
- `CombinedField` - 複合型のため
- `ComposableField` - Composable 関数はシリアライズ不可
- `ModifierField` - Modifier はシリアライズ不可

:::tip
`SelectableField` で enum を使用する場合は、`.withSerializer()` を使用してシリアライザを設定することで、値の永続化や共有が可能になります。
:::

## 不十分ですか？

より高度なカスタマイズが必要な場合、[フィールドを独自で作成する](./all-fields) ことができます。
