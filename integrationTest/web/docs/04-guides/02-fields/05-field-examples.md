---
title: "Examples of Field usage"
sidebar_position: 5
---

# Examples of Field usage

フィールドは Preview したい Composable の引数の数だけ用意するのがベストプラクティスです。
Composable の引数は Composable のインプットであり、インプットに応じてどのように Composable が表示されるか確認できるためです。

各引数に対してどのフィールドを利用すればいいかは 引数がどんな型かに応じて判別できるでしょう。
例えば `text: String` という引数がある場合 引数の型が String 型であるため StringField を利用すれば良いです。

このガイドでは、どんな型に対してどんな Field を利用すれば良いのかを紹介します。

## Field の命名規則

基本的には 各フィールドは `型名 + "Field"` という命名規則で用意されています。

## シンプル

### プリミティブ型, Compose の型

プリミティブ型、一般的な Compose の型については組み込みで用意されている

```kt
@Composable
fun MyComposable(
    str: String,
    int: Int,
    bool: Boolean,
    dp: Dp,
    color: Color,
) { ... }

PreviewLab {
    MyComposable(
        str = fieldValue { StringField("str", "") },
        int = fieldValue { IntField("int", 0) },
        bool = fieldValue { BooleanField("bool", true) },
        dp = fieldValue { DpField("dp", 100.dp) },
        color = fieldValue { ColorField("color", Color.Red) },
    )
}
```

## Compose のスロット

Compose では `@Composable () -> Unit` のような Composable 関数を引数にとってレイアウト要素を抽象化することが可能です。これらは Slot API などと呼ばれています（[参照](https://developer.android.com/develop/ui/compose/layouts/basics?hl=ja#slot-based-layouts)）。

```kt
@Composable
fun MyLayout(
    topBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    content: @Composable () -> Unit,
) { ... }
```

このような引数に対しては `ComposableField` が利用できます。`ComposableField` を利用することで Slot に様々なパターンのコンテンツが指定されることを手動テストすることができます。

```kt
PreviewLab {
    MyLayout(
        topBar = fieldValue { ComposableField("topBar", ComposableFieldValue.Red300X300) },
        // ...
    )
}
```

ComposableField は initialValue 引数に ComposableFieldValue を受け取ります (`@Composable () -> Unit` ではない点に注意が必要です) 。

ComposableFieldValue には label と Composable のコンテンツ (invoke() メソッドに対応) が定義されています。デフォルトで Slot に指定できる一般的な Composable が選択できるようになっています。

import composableFieldImage from "./field-examples-composable-field.png"

<img src={composableFieldImage} width="300" />

詳しくは [ComposableField のドキュメント](http://localhost:3000/compose-preview-lab/docs/guides/fields/all-fields#composablefield) をご覧ください。

## Modifier

UI Composable は通常その padding, size などを呼び出し側から変更できるように `modifeir` 引数を設定することが推奨されています。

`modifier` 引数がある Composable 関数の Preview では `ModifierField` を使って手動で Modifier を設定できるようにするのがベストプラクティスです。これにより Modifier の設定により予期しない UI になってしまうことを事前に手動テストして発見することができるようになります。

詳しくは [ModifeirField のドキュメント](./all-fields#modifierfield) を参照して下さい。

## data class

昨今の Kotlin を使ったアプリ開発では、アプリのドメインモデル や 画面に表示するデータをまとめた UiState を `data class` として定義することが多いと思います。

```kt
data class SignUpScreenUiState(
    val nickname: String,
    val age: Int,
    val agreeTerms: Boolean,
)
```

`data class` に対しては **`combined`** を活用することで Field を作成することができます。

```kt
PreviewLab {
    SignUpScreen(
        uiState = fieldValue {
            // highlight-start
            combined(
                label = "uiState",
                field1 = StringField("nickname", ""),
                field2 = IntField("age", 20),
                field3 = StringField("agreeTerms", false),
                combine = { nickname, age, agreeTerms -> SignUpScreenUiState(nickname, age, agreeTerms) },
                split = { splitedOf(it.nickname, it.age, it.agreeTerms) },
            )
            // highlight-end
        }
    )
}
```

ただし combined は 結合する field の数が増えたり 複雑な Field を結合する場合、編集 UI が複雑になる場合があります。
このような場合は代わりに [SelectableField](./all-fields#selectablefield) を利用して編集 UI をシンプルに保つこともできます。

```kt
PreviewLab {
    SignUpScreen(
        uiState = fieldValue {
            // highlight-start
            SelectableField(
                label = "uiState",
                choices = mapOf(
                    "Basic" to SignUpScreenUiState(...),
                    "Large content" to SignUpScreenUiState(...),
                    "Loading" to SignUpScreenUiState(...),
                    "Error" to SignUpScreenUiState(...),
                ),
            )
            // highlight-end
        }
    )
}
```

詳しくは [combined のドキュメント](./all-fields#combinedfield) を参照して下さい。

## sealed class/interface

ドメインモデル, UiState の設計に `sealed class/interface` を利用している場合も多いでしょう。

```kt
sealed interface HomeScreenUiState {
    data object Loading : HomeScreenUiState
    data class Success(val data: String) : HomeScreenUiState
    data class Error(val message: String) : HomeScreenUiState
}
```

`sealed class/interface` に対しては **`PolymorphicField`** が利用できます。

```kt
PreviewLab {
    HomeScreen(
        uiState = PolymorphicField(
            label = "uiState",
            initialValue = HomeScreenUiState.Loading,
            fields = listOf(
                FixedField("loading", HomeScreenUiState.Loading),
                combined(
                    label = "success",
                    field1 = StringField("data", "Sample data"),
                    combine = { data -> HomeScreenUiState.Success(data) },
                    split = { splitedOf(it.data) }
                ),
                combined(
                    label = "error",
                    field1 = StringField("message", "Something went wrong"),
                    combine = { message -> HomeScreenUiState.Error(message) },
                    split = { splitedOf(it.message) }
                )
            )
        ),
    )
}
```

詳しくは [PolymorphicField のドキュメント](./all-fields#polymorphicfield) を参照して下さい。

## value class

Int, String などのプリミティブ型を扱う際、単純なプリミティブのままではなく `value class` でラップすることで意味的に別のモデルと区別できるようにするテクニックがあります。

`value class` に対しては `.transform()` が便利です。

```kt
@JvmInline
value class ItemId(val value: String)

PreviewLab {
    ItemDetailScreen(
        itemId = fieldValue {
            // highlight-start
            StringField("itemId", "some-item-001")
                .transform(
                    transform = { ItemId(it) },
                    reverse = { it.value },
                )
            // highlight-end
        }
    )
}
```

## 画像の URL

アプリで画像 URL を参照することは一般的なユースケースです。

標準で用意されている `StringField.withImageUrlHint()` を利用することで画像URLを簡単に準備できます。

```kt
ItemImageScreen(
    imageUrl = fieldValue {
        StringField("imageUrl", "")
            // highlight-next-line
            .withImageUrlHint()
    }
)
```

デフォルトでは `dummyimage.com` から提供される画像を参照する URL や 無効な URL などが提供されます。
提供される画像のリストは [withImageUrlHint() の実装](https://github.com/TBSten/compose-preview-lab/blob/main/field/src/commonMain/kotlin/me/tbsten/compose/preview/lab/field/WithImageUrlHint.kt) から確認できます。
