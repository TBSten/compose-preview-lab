---
title: All Builtin Fields
sidebar_position: 2
---

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';
import KDocLink from '@site/src/components/KDocLink';
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# All Builtin Fields Reference

## 1. Primitive Fields

Kotlin の基本的なプリミティブ型（String、Boolean、数値型など）に対応するフィールドです。最も基本的で頻繁に使用されるフィールド群です。

### <KDocLink path="core/me.tbsten.compose.preview.lab.field/-string-field/index.html">StringField</KDocLink>

<table>
    <tr>
        <th>対応する 型</th>
        <td> `kotlin.String` </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐⭐⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-string-field/index.html">StringField</KDocLink> </td>
    </tr>
</table>

文字列を入力するためのフィールドです。TextField による文字列入力が可能です。

```kt
PreviewLab {
    MyButton(
        // highlight-next-line
        text = fieldValue { StringField("text", "Click Me") },
    )
}
```

<EmbeddedPreviewLab 
 previewId="StringFieldExample"
 title="StringField Example"
/>

<details>
<summary><code>prefix</code> や <code>suffix</code> を指定する</summary>

`prefix` と `suffix` パラメータを使用して、TextField の前後にコンポーザブルを追加できます。
例えば単位を表示する際に有用です。

```kt
PreviewLab {
    MyButton(
        text = fieldValue {
            StringField(
                label = "text",
                initialValue = "Click Me",
                // highlight-next-line
                prefix = { Text("$") },
                // highlight-next-line
                suffix = { Text("USD") },
            )
        },
    )
}
```

<EmbeddedPreviewLab
 previewId="StringFieldWithPrefixSuffixExample"
 title="StringField with Prefix/Suffix Example"
/>

</details>

<details>
<summary><code>.withTextHint()</code> でよく使うテキスト候補を追加する</summary>

`MutablePreviewLabField<String>.withTextHint()` 拡張関数を使うと、よく使うテキストパターン（空文字・短いテキスト・本文テキスト・長文など）をワンタップで切り替えられるヒントを追加できます。

```kt
PreviewLab {
    val description = fieldValue {
        StringField("description", "Short")
            // highlight-next-line
            .withTextHint()
    }
    Text(description)
}
```

`withTextHint()` の内部では `withHint()` が使われており、以下のような候補があらかじめ用意されています（実装は `StringField` の KDoc を参照してください）。

- `Empty`: 空文字列
- `Short`: 短いテキスト
- `Body`: 複数行の本文テキスト
- `Long`: 非常に長いテキスト

<EmbeddedPreviewLab
  previewId="StringFieldWithTextHintExample"
  title="StringField with Text Hint Example"
/>

</details>

### <KDocLink path="core/me.tbsten.compose.preview.lab.field/-boolean-field/index.html">BooleanField</KDocLink>

<table>
    <tr>
        <th>対応する 型</th>
        <td> `kotlin.Boolean` </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐⭐⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-boolean-field/index.html">BooleanField</KDocLink> </td>
    </tr>
</table>

Boolean 値を切り替えるためのフィールドです。トグルスイッチで `true` と `false` を選択できます。

```kt
PreviewLab {
    MyButton(
        // highlight-next-line
        enabled = fieldValue { BooleanField("enabled", true) },
    )
}
```

<EmbeddedPreviewLab 
 previewId="BooleanFieldExample"
 title="BooleanField Example"
/>

### 数値型フィールド

Kotlin の数値型（Int、Long、Byte、Double、Float）に対応するフィールドです。数値入力が可能です。

<table>
    <tr>
        <th>フィールド</th>
        <th>対応する 型</th>
        <th>利用頻度</th>
        <th>KDoc</th>
    </tr>
    <tr>
        <td> IntField </td>
        <td> `kotlin.Int` </td>
        <td> ⭐⭐⭐ </td>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-int-field/index.html">IntField</KDocLink> </td>
    </tr>
    <tr>
        <td> LongField </td>
        <td> `kotlin.Long` </td>
        <td> ⭐⭐ </td>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-long-field/index.html">LongField</KDocLink> </td>
    </tr>
    <tr>
        <td> ByteField </td>
        <td> `kotlin.Byte` </td>
        <td> ⭐ </td>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-byte-field/index.html">ByteField</KDocLink> </td>
    </tr>
    <tr>
        <td> DoubleField </td>
        <td> `kotlin.Double` </td>
        <td> ⭐⭐ </td>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-double-field/index.html">DoubleField</KDocLink> </td>
    </tr>
    <tr>
        <td> FloatField </td>
        <td> `kotlin.Float` </td>
        <td> ⭐⭐ </td>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-float-field/index.html">FloatField</KDocLink> </td>
    </tr>
</table>

数値型の値を入力するためのフィールドです。TextField による数値入力が可能です。

```kt
PreviewLab {
    MyCartScreen(
        // highlight-next-line
        count = fieldValue { IntField("quantity", 1) },
    )
}
```

<EmbeddedPreviewLab 
 previewId="IntFieldExample"
 title="IntField Example"
/>

<details>
<summary><code>inputType</code> で <code>prefix</code> や <code>suffix</code> を指定する</summary>

`inputType` パラメータで `NumberField.InputType.TextField` を指定すると、`prefix` と `suffix` を追加できます。
例えば単位を表示する際に有用です。

```kt
PreviewLab {
    Counter(
        count = fieldValue {
            IntField(
                label = "count",
                initialValue = 0,
                // highlight-next-line
                inputType = NumberField.InputType.TextField(
                    prefix = { Text("$") },
                    suffix = { Text("yen") }
                )
            )
        },
    )
}
```

<EmbeddedPreviewLab
 previewId="IntFieldWithPrefixSuffixExample"
 title="IntField with Prefix/Suffix Example"
/>

</details>

<details>
<summary>Long / Byte / Double / Float の例</summary>

数値型フィールドは **型ごとにユースケースが違う** ことが多いので、代表例をいくつか載せます。

- **LongField**: 大きな値（ファイルサイズ、タイムスタンプ、ID など）
- **ByteField**: ビットフラグなどの低レベル表現
- **DoubleField**: 金額・割合計算などの小数（精度が必要な場合は注意）
- **FloatField**: アニメーション/アルファなど UI パラメータ（0..1 など範囲を意識）

<Tabs groupId="numberFieldExamples">
  <TabItem value="long" label="Long">
    <EmbeddedPreviewLab
      previewId="LongFieldExample"
      title="LongField Example"
    />
  </TabItem>
  <TabItem value="byte" label="Byte">
    <EmbeddedPreviewLab
      previewId="ByteFieldExample"
      title="ByteField Example"
    />
  </TabItem>
  <TabItem value="double" label="Double">
    <EmbeddedPreviewLab
      previewId="DoubleFieldExample"
      title="DoubleField Example"
    />
  </TabItem>
  <TabItem value="float" label="Float">
    <EmbeddedPreviewLab
      previewId="FloatFieldExample"
      title="FloatField Example"
    />
  </TabItem>
</Tabs>

</details>

### <KDocLink path="field/me.tbsten.compose.preview.lab.field/-instant-field/index.html">InstantField</KDocLink>

<table>
    <tr>
        <th>対応する 型</th>
        <td> `kotlin.time.Instant` </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <KDocLink path="field/me.tbsten.compose.preview.lab.field/-instant-field/index.html">InstantField</KDocLink> </td>
    </tr>
</table>

`kotlin.time.Instant` を編集するためのフィールドです。

編集 UI では **Epoch milliseconds**（`Long`）として入力し、値は `Instant.fromEpochMilliseconds(...)` として扱われます。
（`kotlin.time.Instant` は現状 Experimental のため、利用側で `@OptIn(ExperimentalTime::class)` が必要になる場合があります）

```kt
PreviewLab {
    Text(
        text = "Create at: ${
            // highlight-next-line
            fieldValue { InstantField("createAt", Clock.System.now()) }
        }",
    )
}
```

<EmbeddedPreviewLab
 previewId="InstantFieldExample"
 title="InstantField Example"
/>

## 2. Enhance Fields

既存のフィールドを拡張・強化するためのフィールドです。選択肢から選ぶフィールド、複数のフィールドを組み合わせるフィールド、既存のフィールドを拡張するユーティリティフィールドが含まれます。詳細は [Enhance Fields](./enhance-fields) を参照してください。

### <KDocLink path="core/me.tbsten.compose.preview.lab.field/-selectable-field/index.html">SelectableField</KDocLink>

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

### <KDocLink path="core/me.tbsten.compose.preview.lab.field/-combined-field/index.html">CombinedField</KDocLink>

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

### <KDocLink path="core/me.tbsten.compose.preview.lab.field/-nullable-field/index.html">NullableField</KDocLink>

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

### <KDocLink path="core/me.tbsten.compose.preview.lab.field/-transform-field/index.html">TransformField</KDocLink>

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

### <KDocLink path="core/me.tbsten.compose.preview.lab.field/-with-hint-field/index.html">WithHintField</KDocLink>

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

<details>
<summary><code>.withHintAction()</code> でアクションを実行するヒントを追加する</summary>

`withHintAction()` を使用すると、単純な値の設定ではなくカスタムアクションを実行するヒントを追加できます。
アクションのラムダでは Field 自身を `this` として受け取るため、`value` プロパティに直接アクセスして複雑な操作が可能です。

```kt
PreviewLab {
    val items = fieldValue {
        ListField(
            label = "items",
            elementField = { StringField(label, initialValue) },
            initialValue = emptyList<String>(),
        )
            // highlight-start
            .withHintAction(
                "Add 3 items" to { value = value + listOf("Item A", "Item B", "Item C") },
                "Clear all" to { value = emptyList() },
            )
            // highlight-end
    }
    ItemList(items = items)
}
```

また、単一のアクションを追加する場合は、トレーリングラムダ構文も使用できます：

```kt
PreviewLab {
    val text = fieldValue {
        StringField("text", "hello")
            // highlight-start
            .withHint("Uppercase") { value = value.uppercase() }
            .withHint("Clear") { value = "" }
            // highlight-end
    }
    Text(text)
}
```

詳細は [Enhance Fields](./enhance-fields#fieldwithhintaction-アクションを実行するヒントを追加) を参照してください。

</details>

### <KDocLink path="core/me.tbsten.compose.preview.lab.field/-polymorphic-field/index.html">PolymorphicField</KDocLink>

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

### <KDocLink path="core/me.tbsten.compose.preview.lab.field/-fixed-field/index.html">FixedField</KDocLink>

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

### <KDocLink path="core/me.tbsten.compose.preview.lab.field/-with-value-code-field/index.html">WithValueCodeField</KDocLink> / `.withValueCode()`

任意の Field に対して、Inspector の Code タブに表示されるコード表現だけを差し替えるユーティリティです。UI や値の型はそのままに、コードスニペットを自分のプロジェクトの API 形式に合わせたいときに使用します。

詳細は [Enhance Fields](./enhance-fields#fieldwithvaluecode-code-タブに出力されるコードをカスタマイズ) を参照してください。

### <KDocLink path="field/me.tbsten.compose.preview.lab.field/-with-serializer-field/index.html">WithSerializerField</KDocLink> / `.withSerializer()`

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

詳細は [Enhance Fields](./enhance-fields#fieldwithserializer-field-のシリアライザを設定する) を参照してください。

## 3. Compose Value Fields

Compose 固有の値型（Dp、Sp、Color、Offset、Size、Modifier、Composable など）に対応するフィールドです。Compose UI の構築に特化した型を扱います。

### <KDocLink path="core/me.tbsten.compose.preview.lab.field/-dp-field/index.html">DpField</KDocLink>

<table>
    <tr>
        <th>対応する 型</th>
        <td> `androidx.compose.ui.unit.Dp` </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐⭐⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-dp-field/index.html">DpField</KDocLink> </td>
    </tr>
</table>

Compose の Dp（密度非依存ピクセル）値を編集するためのフィールドです。自動的に "dp" サフィックスが付きます。

```kt
PreviewLab {
    Box(
        modifier = Modifier
            // highlight-next-line
            .padding(fieldValue { DpField("padding", 16.dp) })
    ) {
        Text("Padded Content")
    }
}
```

<EmbeddedPreviewLab 
 previewId="DpFieldExample"
 title="DpField Example"
/>

### <KDocLink path="core/me.tbsten.compose.preview.lab.field/-sp-field/index.html">SpField</KDocLink>

<table>
    <tr>
        <th>対応する 型</th>
        <td> `androidx.compose.ui.unit.TextUnit` </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐⭐⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-sp-field/index.html">SpField</KDocLink> </td>
    </tr>
</table>

Compose の Sp（スケーラブルピクセル）値を編集するためのフィールドです。フォントサイズなどのテキスト関連の寸法に使用します。自動的に "sp" サフィックスが付きます。

```kt
PreviewLab {
    Text(
        text = "Sample Text",
        // highlight-next-line
        fontSize = fieldValue { SpField("fontSize", 16.sp) },
    )
}
```

<EmbeddedPreviewLab 
 previewId="SpFieldExample"
 title="SpField Example"
/>

### <KDocLink path="core/me.tbsten.compose.preview.lab.field/-color-field/index.html">ColorField</KDocLink>

<table>
    <tr>
        <th>対応する 型</th>
        <td> `androidx.compose.ui.graphics.Color` </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐⭐⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-color-field/index.html">ColorField</KDocLink> </td>
    </tr>
</table>

Compose の Color を選択するためのフィールドです。インタラクティブなカラーピッカーで HSV スライダーとアルファチャンネルコントロールを使用して色を選択できます。

```kt
PreviewLab {
    Box(
        modifier = Modifier
            .size(100.dp)
            // highlight-next-line
            .background(fieldValue { ColorField("background", Color.Blue) })
    )
}
```

<EmbeddedPreviewLab 
 previewId="ColorFieldExample"
 title="ColorField Example"
/>

<details>
<summary><code>.withPredefinedColorHint()</code> でよく使う色をワンタップで選べるようにする</summary>

`ColorField` はカラーピッカーで自由に色を選べますが、`Color.Red` など **Compose が定義している定番色**を素早く選びたい場合があります。

その場合は `ColorField(...).withPredefinedColorHint()` を付けることで、`ColorField.predefinedColorNames` に定義されている色（例：`Color.Red`, `Color.Blue` など）がヒントとして追加され、ワンタップで切り替えできるようになります（内部的には `withHint()` を使っています）。

含まれる色の例：

- Primary: `Color.Red`, `Color.Green`, `Color.Blue`, `Color.Black`, `Color.White`
- Secondary: `Color.Cyan`, `Color.Magenta`, `Color.Yellow`
- Grays: `Color.Gray`, `Color.DarkGray`, `Color.LightGray`
- Special: `Color.Transparent`, `Color.Unspecified`

```kt
PreviewLab {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(
                fieldValue {
                    ColorField("background", Color.Blue)
                        // highlight-next-line
                        .withPredefinedColorHint()
                }
            )
    )
}
```

</details>

### Offset / Size フィールド

座標やサイズを編集するためのフィールドです。各座標や寸法を独立して編集できます。

<table>
    <tr>
        <th>フィールド</th>
        <th>対応する 型</th>
        <th>利用頻度</th>
        <th>KDoc</th>
    </tr>
    <tr>
        <td> OffsetField </td>
        <td> `androidx.compose.ui.geometry.Offset` </td>
        <td> ⭐⭐ </td>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-offset-field/index.html">OffsetField</KDocLink> </td>
    </tr>
    <tr>
        <td> DpOffsetField </td>
        <td> `androidx.compose.ui.unit.DpOffset` </td>
        <td> ⭐⭐ </td>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-dp-offset-field/index.html">DpOffsetField</KDocLink> </td>
    </tr>
    <tr>
        <td> SizeField </td>
        <td> `androidx.compose.ui.geometry.Size` </td>
        <td> ⭐⭐ </td>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-size-field/index.html">SizeField</KDocLink> </td>
    </tr>
    <tr>
        <td> DpSizeField </td>
        <td> `androidx.compose.ui.unit.DpSize` </td>
        <td> ⭐⭐ </td>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-dp-size-field/index.html">DpSizeField</KDocLink> </td>
    </tr>
</table>

```kt
PreviewLab {
    Column {
        // OffsetField: Float 単位の座標
        Box(
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer {
                    // highlight-next-line
                    val offset = fieldValue { OffsetField("position", Offset(50f, 100f)) }
                    translationX = offset.x
                    translationY = offset.y
                }
        )

        // DpOffsetField: Dp 単位の座標
        Text(
            text = "Positioned Text",
            modifier = Modifier
                // highlight-next-line
                .offset(fieldValue { DpOffsetField("textOffset", DpOffset(16.dp, 8.dp)) })
        )

        // SizeField: Float 単位のサイズ
        Canvas(
            modifier = Modifier.size(200.dp)
        ) {
            // highlight-next-line
            val canvasSize = fieldValue { SizeField("canvasSize", Size(200f, 150f)) }
            drawRect(Color.Blue, size = canvasSize)
        }

        // DpSizeField: Dp 単位のサイズ
        Button(
            onClick = { },
            modifier = Modifier
                // highlight-next-line
                .size(fieldValue { DpSizeField("buttonSize", DpSize(120.dp, 48.dp)) })
        ) {
            Text("Sized Button")
        }
    }
}
```

<EmbeddedPreviewLab 
 previewId="OffsetAndSizeFieldExample"
 title="Offset and Size Field Example"
/>

### <KDocLink path="core/me.tbsten.compose.preview.lab.field/-modifier-field/index.html">ModifierField</KDocLink>

<table>
    <tr>
        <th>対応する 型</th>
        <td> `androidx.compose.ui.Modifier` </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐⭐⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-modifier-field/index.html">ModifierField</KDocLink> </td>
    </tr>
</table>

Compose の Modifier チェーンを構築・編集するためのインタラクティブなフィールドです。ビルダーパターンで複雑な Modifier チェーンを視覚的に構築できます。

```kt
PreviewLab {
    Button(
        onClick = { },
        // highlight-next-line
        modifier = fieldValue { ModifierField("buttonModifier") },
    ) {
        Text("Styled Button")
    }
}
```

<EmbeddedPreviewLab 
 previewId="ModifierFieldExample"
 title="ModifierField Example"
/>

<details>
<summary><code>ModifierFieldValue.mark()</code> で視覚的なマーキングを追加する</summary>

`ModifierFieldValue.mark()` を使用すると、視覚的なマーキング（ボーダーと背景）を追加できます。

```kt
PreviewLab {
    Button(
        onClick = { },
        modifier = fieldValue {
            ModifierField(
                label = "buttonModifier",
                // highlight-next-line
                initialValue = ModifierFieldValue.mark()
            )
        },
    ) {
        Text("Styled Button")
    }
}
```

<EmbeddedPreviewLab
 previewId="ModifierFieldWithMarkExample"
 title="ModifierField with Mark Example"
/>

</details>

### <KDocLink path="core/me.tbsten.compose.preview.lab.field/-composable-field/index.html">ComposableField</KDocLink>

<table>
    <tr>
        <th>対応する 型</th>
        <td> `@Composable () -> Unit` </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <KDocLink path="core/me.tbsten.compose.preview.lab.field/-composable-field/index.html">ComposableField</KDocLink> </td>
    </tr>
</table>

定義済みの Composable コンテンツオプションから選択するためのフィールドです。

```kt
PreviewLab {
    MyContainer(
        content = fieldValue {
            // highlight-next-line
            ComposableField(
                label = "content",
                initialValue = ComposableFieldValue.Red32X32
            )
        },
    )
}
```

<EmbeddedPreviewLab 
 previewId="ComposableFieldExample"
 title="ComposableField Example"
/>

<details>
<summary><code>ComposableFieldValue</code> の定義済み値を使用する</summary>

`ComposableFieldValue` には、`ColorBox`、`Text`、`Empty` などの定義済み値が用意されています。

```kt
PreviewLab {
    MyContainer(
        content = fieldValue {
            ComposableField(
                label = "content",
                // highlight-next-line
                initialValue = ComposableFieldValue.Red32X32,
                choices = listOf(
                    ComposableFieldValue.Red32X32,
                    ComposableFieldValue.SimpleText,
                    ComposableFieldValue.Empty
                )
            )
        },
    )
}
```

<EmbeddedPreviewLab
 previewId="ComposableFieldWithPredefinedValuesExample"
 title="ComposableField with Predefined Values Example"
/>

</details>

## 4. Collection Fields

コレクション型（List、Set）を編集するためのフィールドです。要素の追加・削除・挿入が可能で、各要素は個別のフィールドとして編集できます。

### <KDocLink path="field/me.tbsten.compose.preview.lab.field/-list-field/index.html">ListField</KDocLink>

<table>
    <tr>
        <th>対応する 型</th>
        <td> `kotlin.collections.List` </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <KDocLink path="field/me.tbsten.compose.preview.lab.field/-list-field/index.html">ListField</KDocLink> </td>
    </tr>
</table>

リスト値を編集するためのフィールドです。要素の追加・削除・挿入が可能で、各要素は `elementField` で指定したフィールドとして編集できます。

```kt
PreviewLab {
    val characters by fieldValue {
        // highlight-start
        ListField(
            label = "characters",
            initialValue = listOf("Alice", "Bob", "Charlie"),
            elementField = { StringField(label, initialValue) },
        )
        // highlight-end
    }
    Text(characters.joinToString(", "))
}
```

<EmbeddedPreviewLab
 previewId="ListFieldExample"
 title="ListField Example"
/>

<details>
<summary><code>.withEmptyHint()</code> で空リストの選択肢を追加する</summary>

`MutablePreviewLabField<List<Value>>.withEmptyHint()` 拡張関数を使うと、空リストをワンタップで選択できるヒントを追加できます。

```kt
PreviewLab {
    val items by fieldValue {
        ListField(
            label = "items",
            initialValue = listOf("Item 1", "Item 2"),
            elementField = { StringField(label, initialValue) },
        )
            // highlight-next-line
            .withEmptyHint()
    }
    Text("Items: ${items.size}")
}
```

</details>

<details>
<summary><code>defaultValue</code> で新規要素のデフォルト値を指定する</summary>

`defaultValue` パラメータを指定すると、新しい要素を挿入する際のデフォルト値をカスタマイズできます。指定しない場合は、`initialValue` の最初の要素がデフォルト値として使用されます。

```kt
PreviewLab {
    val numbers by fieldValue {
        ListField(
            label = "numbers",
            initialValue = listOf(1, 2, 3),
            elementField = { IntField(label, initialValue) },
            // highlight-next-line
            defaultValue = { 0 }  // 新しい要素のデフォルト値
        )
    }
    Text(numbers.joinToString(", "))
}
```

</details>

### <KDocLink path="field/me.tbsten.compose.preview.lab.field/-set-field/index.html">SetField</KDocLink>

<table>
    <tr>
        <th>対応する 型</th>
        <td> `kotlin.collections.Set` </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <KDocLink path="field/me.tbsten.compose.preview.lab.field/-set-field/index.html">SetField</KDocLink> </td>
    </tr>
</table>

セット値を編集するためのフィールドです。`ListField` と同様に要素の追加・削除が可能ですが、重複する値がある場合はエラーインジケーターで警告されます。

```kt
PreviewLab {
    val fruits by fieldValue {
        // highlight-start
        SetField(
            label = "fruits",
            initialValue = setOf("Apple", "Banana", "Cherry"),
            elementField = { StringField(label, initialValue) },
        )
        // highlight-end
    }
    Text(fruits.joinToString(", "))
}
```

<EmbeddedPreviewLab
 previewId="SetFieldExample"
 title="SetField Example"
/>

<details>
<summary>重複検出機能について</summary>

SetField は編集中に重複する値を自動的に検出し、エラーカラーでハイライト表示します。重複があってもエラーにはなりませんが、最終的な `value` は Set として重複が除去された状態で返されます。

これは、ユーザーが誤って同じ値を追加した場合に視覚的にフィードバックを提供するための機能です。

</details>

<details>
<summary><code>.withEmptyHint()</code> で空セットの選択肢を追加する</summary>

`MutablePreviewLabField<Set<Value>>.withEmptyHint()` 拡張関数を使うと、空セットをワンタップで選択できるヒントを追加できます。

```kt
PreviewLab {
    val tags by fieldValue {
        SetField(
            label = "tags",
            initialValue = setOf("tag1", "tag2"),
            elementField = { StringField(label, initialValue) },
        )
            // highlight-next-line
            .withEmptyHint()
    }
    Text("Tags: ${tags.size}")
}
```

</details>
