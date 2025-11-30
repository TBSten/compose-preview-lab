---
title: Enhance fields
sidebar_position: 3
---

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# Field を強化する

[All Fields](./all-fields) で紹介されている Field と組み合わせることで、カスタム Field を作成せずとも 独自の型に対応したり、Field の編集 UI を簡単に作成できます。

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

[PreviewParameterProvider.toField()](https://github.com/TBSten/compose-preview-lab/blob/908ae45aa87e477263de2c4d6a30eb6f21e5f4bc/core/src/commonMain/kotlin/me/tbsten/compose/preview/lab/field/ComposableField.kt#L591) が用意されているため、必要に応じて活用してください。

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
        field3 = BooleanField("isLoading", ...),
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
- 標準では CombinedField2~10 まで用意されているため最大10個まで合体させることができます。11個以上の場合はライブラリの CombinedField 実装を参考に独自実装するか、型安全性が失われるものの任意の Field を合体させることができる CombinedField 合体する Field を List を受け取るバージョンの CombinedField を利用してください。

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

## 不十分ですか？

より高度なカスタマイズが必要な場合、[フィールドを独自で作成する](./all-fields) ことができます。
