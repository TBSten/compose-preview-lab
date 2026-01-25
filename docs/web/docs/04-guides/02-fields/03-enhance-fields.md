---
title: Enhance fields
---

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';
import KDocLink from '@site/src/components/KDocLink';

# Field を強化する

既存のフィールドを拡張・強化するためのフィールドです。選択肢から選ぶフィールド、複数のフィールドを組み合わせるフィールド、既存のフィールドを拡張するユーティリティフィールドが含まれます。

## <KDocLink path="core/me.tbsten.compose.preview.lab.field/-selectable-field/index.html">SelectableField</KDocLink>

<table>
    <tr>
        <th>対応する 型</th>
        <td> 任意の型 </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐⭐⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-selectable-field/index.html">SelectableField</KDocLink> </td>
    </tr>
</table>

指定された選択肢のリストから1つのオプションを選択するためのフィールドです。ドロップダウンまたはチップ形式で表示できます。

```kt
PreviewLab {
    MyApp(
        theme = fieldValue {
            // highlight-start
            SelectableField(
                label = "theme",
                choices = listOf("Light", "Dark", "Auto")
            )
            // highlight-end
        },
    )
}
```

<EmbeddedPreviewLab 
 previewId="SelectableFieldExample"
 title="SelectableField Example"
/>

<details>
<summary><code>type</code> で表示形式を変更する</summary>

`type` パラメータで `SelectableField.Type.CHIPS` を指定すると、チップ形式で表示されます。

```kt
PreviewLab {
    MyApp(
        theme = fieldValue {
            SelectableField(
                label = "theme",
                choices = listOf("Light", "Dark", "Auto"),
                // highlight-next-line
                type = SelectableField.Type.CHIPS
            )
        },
    )
}
```

<EmbeddedPreviewLab
 previewId="SelectableFieldChipsExample"
 title="SelectableField Chips Example"
/>

</details>

<details>
<summary>Map から作成する</summary>

Map から作成すると、キーがラベル、値が選択肢の値として使用されます。

```kt
PreviewLab {
    MyApp(
        theme = fieldValue {
            // highlight-start
            SelectableField(
                label = "theme",
                choices = mapOf(
                    "Light Mode" to "Light",
                    "Dark Mode" to "Dark",
                    "Auto (System)" to "Auto"
                )
            )
            // highlight-end
        },
    )
}
```

<EmbeddedPreviewLab
 previewId="SelectableFieldMapExample"
 title="SelectableField Map Example"
/>

</details>

<details>
<summary>ビルダー構文から作成する</summary>

ビルダー構文を使用すると、各選択肢にラベルを設定したり、デフォルト値を指定したりできます。

```kt
PreviewLab {
    MyApp(
        theme = fieldValue {
            // highlight-start
            SelectableField<String>(label = "theme") {
                choice("Light", label = "Light Mode", isDefault = true)
                choice("Dark", label = "Dark Mode")
                choice("Auto", label = "Auto (System)")
            }
            // highlight-end
        },
    )
}
```

<EmbeddedPreviewLab
 previewId="SelectableFieldBuilderExample"
 title="SelectableField Builder Example"
/>

</details>

### <KDocLink path="core/me.tbsten.compose.preview.lab.field/-enum-field.html">EnumField</KDocLink>

<table>
    <tr>
        <th>対応する 型</th>
        <td> `kotlin.Enum` </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐⭐⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-enum-field.html">EnumField</KDocLink> </td>
    </tr>
</table>

Enum クラスの値から選択するためのフィールドです。`SelectableField` のショートハンドとして実装されています。

```kt
enum class ButtonVariant { Primary, Secondary, Tertiary }

PreviewLab {
    MyButton(
        variant = fieldValue {
            // highlight-next-line
            EnumField("variant", ButtonVariant.Primary)
        },
    )
}
```

<EmbeddedPreviewLab 
 previewId="EnumFieldExample"
 title="EnumField Example"
/>

## <KDocLink path="core/me.tbsten.compose.preview.lab.field/-with-hint-field/index.html">WithHintField</KDocLink> / `.withHint()`

<table>
    <tr>
        <th>対応する 型</th>
        <td> 元のフィールドと同じ型 </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-with-hint-field/index.html">WithHintField</KDocLink> </td>
    </tr>
</table>

既存のフィールドにヒント選択肢を追加するフィールドです。定義済みの値から素早く選択できるチップが表示されます。

```kt
PreviewLab {
    Text(
        text = "Sample Text",
        fontSize = fieldValue {
            SpField(label = "fontSize", initialValue = 16.sp)
                // highlight-start
                .withHint(
                    "Small" to 12.sp,
                    "Medium" to 16.sp,
                    "Large" to 20.sp,
                    "XLarge" to 24.sp,
                )
                // highlight-end
        },
    )
}
```

<EmbeddedPreviewLab
 previewId="WithHintFieldExample"
 title="WithHintField Example"
/>

`Field.withHint()` はあくまでも既存の Field の編集 UI によくある選択肢を追加するだけです。
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

## Field.withHintAction(): アクションを実行するヒントを追加

`Field.withHintAction()` は `withHint()` と似ていますが、単純な値の設定ではなくカスタムアクションを実行できます。
アクションのラムダでは Field 自身を `this` として受け取るため、`value` プロパティに直接アクセスして複雑な操作が可能です。
アクションは `suspend` 関数として定義されているため、非同期処理も可能です。

```kt
val items = fieldValue {
    ListField(
        label = "items",
        elementField = { StringField(label, initialValue) },
        initialValue = emptyList<String>(),
    )
        // highlight-start
        .withHintAction(
            "Add 3 items" to {
                value = value + listOf("Item A", "Item B", "Item C")
            },
            "Clear all" to {
                value = emptyList()
            },
        )
        // highlight-end
}
```

また、単一のアクションを追加する場合は、trailing lambda 構文を使用できます:

```kt
val text = fieldValue {
    StringField("text", "hello")
        .withHint("Uppercase") { value = value.uppercase() }
        .withHint("Clear") { value = "" }
}
```

<EmbeddedPreviewLab
previewId="WithHintActionExample"
title="Field withHintAction Example"
/>

### 利用例

<details>
<summary>リストの一括操作</summary>

```kt
val tags = fieldValue {
    ListField(
        label = "tags",
        elementField = { StringField(label, initialValue) },
        initialValue = emptyList<String>(),
    )
        .withHintAction(
            // highlight-start
            "Add sample tags" to {
                value = value + listOf("kotlin", "compose", "android")
            },
            "Remove duplicates" to {
                value = value.distinct()
            },
            "Sort A-Z" to {
                value = value.sorted()
            },
            // highlight-end
        )
}
```

</details>

<details>
<summary>withHint と withHintAction の組み合わせ</summary>

```kt
val text = fieldValue {
    StringField("text", "hello")
        // highlight-start
        .withHint(
            "Hello" to "Hello, World!",
            "Lorem" to "Lorem ipsum dolor sit amet",
        )
        .withHintAction(
            "Uppercase" to { value = value.uppercase() },
            "Clear" to { value = "" },
        )
    // highlight-end
}
```

</details>

## <KDocLink path="core/me.tbsten.compose.preview.lab.field/-nullable-field/index.html">NullableField</KDocLink> / `.nullable()`

<table>
    <tr>
        <th>対応する 型</th>
        <td> 任意の Nullable 型 </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-nullable-field/index.html">NullableField</KDocLink> </td>
    </tr>
</table>

既存のフィールドを Nullable にするためのフィールドです。チェックボックスで null と値の切り替えができます。

```kt
PreviewLab {
    UserName(
        userName = fieldValue {
            // highlight-next-line
            StringField("userName", "John Doe").nullable()
        },
    )
}
```

<EmbeddedPreviewLab 
 previewId="NullableFieldExample"
 title="NullableField Example"
/>

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

## <KDocLink path="core/me.tbsten.compose.preview.lab.field/-combined-field/index.html">CombinedField</KDocLink>

<table>
    <tr>
        <th>対応する 型</th>
        <td> 任意の複合型 </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-combined-field/index.html">CombinedField</KDocLink> </td>
    </tr>
</table>

複数のサブフィールドを1つの複合値に結合するフィールドです。`combine` 関数と `split` 関数を使用して、複数のフィールドを1つの値にまとめます。

```kt
data class Padding(val horizontal: Dp, val vertical: Dp)

PreviewLab {
    val padding: Padding = fieldValue {
        // highlight-start
        combined(
            label = "padding",
            field1 = DpField("horizontal", 16.dp),
            field2 = DpField("vertical", 8.dp),
            combine = { horizontal, vertical -> Padding(horizontal, vertical) },
            split = { listOf(it.horizontal, it.vertical) },
        )
        // highlight-end
    }

    Box(
        modifier = Modifier
            .background(Color.LightGray)
            .padding(horizontal = padding.horizontal, vertical = padding.vertical),
    ) {
        Text("Content")
    }
}
```

<EmbeddedPreviewLab 
 previewId="CombinedFieldExample"
 title="CombinedField Example"
/>

<details>
<summary><code>combine</code> と <code>split</code> 引数について</summary>

`combine` 関数は、複数のサブフィールドの値を1つの複合値に結合する関数です。`split` 関数は、複合値を元のサブフィールドの値のリストに分解する関数です。

- `combine`: `List<Any> -> T` の形式で、サブフィールドの値のリストを受け取り、複合型の値を返します
- `split`: `T -> List<Any>` の形式で、複合型の値を受け取り、サブフィールドの値のリストを返します

これらの関数は、`CombinedField` がサブフィールドの値と複合値の間で変換を行うために使用されます。

</details>

複数の Field を合体して、1つの Field として扱うことができます。
複数のプロパティを持つ data class に対応する、完全に操作可能な Field を簡単に作成することができます。

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
        field3 = BooleanField("isLoading", ...),
        combine = { title, description, isLoading -> MyUiState(title, description, isLoading) },
        split = { splitedOf(it.title, it.description, it.isLoading) },
    )
}
```

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

## <KDocLink path="core/me.tbsten.compose.preview.lab.field/-transform-field/index.html">TransformField</KDocLink>

<table>
    <tr>
        <th>対応する 型</th>
        <td> 変換後の任意の型 </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-transform-field/index.html">TransformField</KDocLink> </td>
    </tr>
</table>

既存のフィールドの値を別の型に変換するためのフィールドです。`transform` 関数と `reverse` 関数を使用して型変換を行います。

```kt
PreviewLab {
    val intValue = fieldValue {
        StringField("number", "42")
            // highlight-start
            .transform(
                transform = { it.toIntOrNull() ?: 0 },
                reverse = { it.toString() }
            )
            // highlight-end
    }
}
```

<EmbeddedPreviewLab 
 previewId="TransformFieldExample"
 title="TransformField Example"
/>

## <KDocLink path="core/me.tbsten.compose.preview.lab.field/-with-value-code-field/index.html">WithValueCodeField</KDocLink> / `.withValueCode()`

任意の Field に対して、Inspector の Code タブに表示されるコード表現だけを差し替えるユーティリティです。UI や値の型はそのままに、コードスニペットを自分のプロジェクトの API 形式に合わせたいときに使用します。

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
    SpField(label = "fontSize", initialValue = 16.sp)
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

## <KDocLink path="field/me.tbsten.compose.preview.lab.field/-with-serializer-field/index.html">WithSerializerField</KDocLink> / `.withSerializer()`

<table>
    <tr>
        <th>対応する 型</th>
        <td> 元のフィールドと同じ型 </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <KDocLink path="field/me.tbsten.compose.preview.lab.field/-with-serializer-field/index.html">WithSerializerField</KDocLink> </td>
    </tr>
</table>

任意の Field にカスタムシリアライザを設定するユーティリティです。`SelectableField` で enum や sealed class を使用する場合など、デフォルトでシリアライザを持たない Field にシリアライズ機能を追加したいときに使用します。

```kt
@Serializable
enum class Theme { Light, Dark, System }

PreviewLab {
    val theme = fieldValue {
        SelectableField(
            label = "theme",
            choices = Theme.entries,
            choiceLabel = { it.name },
        )
            // highlight-next-line
            .withSerializer(Theme.serializer())
    }

    AppTheme(theme = theme)
}
```

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

| Field             | Serializer                      |
|-------------------|---------------------------------|
| `BooleanField`    | `Boolean.serializer()`          |
| `StringField`     | `String.serializer()`           |
| `IntField`        | `Int.serializer()`              |
| `LongField`       | `Long.serializer()`             |
| `ByteField`       | `Byte.serializer()`             |
| `DoubleField`     | `Double.serializer()`           |
| `FloatField`      | `Float.serializer()`            |
| `NullableField`   | ベース Field の nullable serializer |
| `ColorField`      | `ColorSerializer`               |
| `DpField`         | `DpSerializer`                  |
| `SpField`         | `TextUnitSerializer`            |
| `OffsetField`     | `OffsetSerializer`              |
| `DpOffsetField`   | `DpOffsetSerializer`            |
| `SizeField`       | `SizeSerializer`                |
| `DpSizeField`     | `DpSizeSerializer`              |
| `ScreenSizeField` | `ScreenSizeSerializer`          |
| `TransformField`  | ベース Field の serializer を変換      |

以下の Field はデフォルトで `null` を返します（シリアライズ不可）：

- `SelectableField`
- `PolymorphicField`
- `CombinedField`
- `ComposableField`
- `ModifierField`

その場合 `SelectableField` で enum を使用する場合は、`.withSerializer()` を使用してシリアライザを設定することで、値の永続化や共有が可能になります。

## <KDocLink path="core/me.tbsten.compose.preview.lab.field/-polymorphic-field/index.html">PolymorphicField</KDocLink>

<table>
    <tr>
        <th>対応する 型</th>
        <td> 複数の型から選択可能な型 </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-polymorphic-field/index.html">PolymorphicField</KDocLink> </td>
    </tr>
</table>

複数のフィールドから1つを選択して使用するフィールドです。値の型に応じて適切なフィールドが自動的に選択され、編集UIが切り替わります。sealed interface や sealed class など、継承階層があるクラスのField化に特に有用です。

```kt
sealed interface UiState {
    data object Loading : UiState
    data class Success(val data: String) : UiState
    data class Error(val message: String) : UiState
}

PreviewLab {
    val uiState: UiState = fieldValue {
        // highlight-start
        PolymorphicField(
            label = "uiState",
            initialValue = UiState.Loading,
            fields = listOf(
                FixedField("loading", UiState.Loading),
                combined(
                    label = "success",
                    field1 = StringField("data", "Sample data"),
                    combine = { data -> UiState.Success(data) },
                    split = { splitedOf(it.data) }
                ),
                combined(
                    label = "error",
                    field1 = StringField("message", "Something went wrong"),
                    combine = { message -> UiState.Error(message) },
                    split = { splitedOf(it.message) }
                )
            )
        )
        // highlight-end
    }

    // ...
}
```

<EmbeddedPreviewLab 
 previewId="PolymorphicFieldExample"
 title="PolymorphicField Example"
/>

## <KDocLink path="core/me.tbsten.compose.preview.lab.field/-fixed-field/index.html">FixedField</KDocLink>

<table>
    <tr>
        <th>対応する 型</th>
        <td> 任意の型（固定値） </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-fixed-field/index.html">FixedField</KDocLink> </td>
    </tr>
</table>

編集不可の固定値を持つフィールドです。`PolymorphicField` 内で固定値の選択肢を提供する場合などに使用します。

```kt
sealed interface FixedFieldExampleUiState {
    data object Initial : FixedFieldExampleUiState
    data object Stable : FixedFieldExampleUiState
}

PreviewLab {
    val value = fieldValue {
        PolymorphicField(
            label = "value",
            initialValue = FixedFieldExampleUiState.Initial,
            fields = listOf(
                // highlight-start
                FixedField("initial", FixedFieldExampleUiState.Initial),
                FixedField("stable", FixedFieldExampleUiState.Stable),
                // highlight-end
            )
        )
    }
}
```

## 不十分ですか？

より高度なカスタマイズが必要な場合、[フィールドを独自で作成する](./custom-fields) ことができます。
