---
title: All Fields
sidebar_position: 2
---

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# All Fields Reference

## 1. Primitive Fields

Kotlin の基本的なプリミティブ型（String、Boolean、数値型など）に対応するフィールドです。最も基本的で頻繁に使用されるフィールド群です。

### [StringField](https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-string-field/index.html)

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
        <td> <a href="https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-string-field/index.html">StringField</a> </td>
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
/>

</details>

### [BooleanField](https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-boolean-field/index.html)

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
        <td> <a href="https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-boolean-field/index.html">BooleanField</a> </td>
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
        <td> <a href="https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-int-field/index.html">IntField</a> </td>
    </tr>
    <tr>
        <td> LongField </td>
        <td> `kotlin.Long` </td>
        <td> ⭐⭐ </td>
        <td> <a href="https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-long-field/index.html">LongField</a> </td>
    </tr>
    <tr>
        <td> ByteField </td>
        <td> `kotlin.Byte` </td>
        <td> ⭐ </td>
        <td> <a href="https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-byte-field/index.html">ByteField</a> </td>
    </tr>
    <tr>
        <td> DoubleField </td>
        <td> `kotlin.Double` </td>
        <td> ⭐⭐ </td>
        <td> <a href="https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-double-field/index.html">DoubleField</a> </td>
    </tr>
    <tr>
        <td> FloatField </td>
        <td> `kotlin.Float` </td>
        <td> ⭐⭐ </td>
        <td> <a href="https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-float-field/index.html">FloatField</a> </td>
    </tr>
</table>

数値型の値を入力するためのフィールドです。TextField による数値入力が可能です。

```kt
PreviewLab {
    Counter(
        // highlight-next-line
        count = fieldValue { IntField("Count", 0) },
    )
}
```

<EmbeddedPreviewLab 
 previewId="IntFieldExample"
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
                label = "Count",
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
/>

</details>

## 2. Enhance Fields

既存のフィールドを拡張・強化するためのフィールドです。選択肢から選ぶフィールド、複数のフィールドを組み合わせるフィールド、既存のフィールドを拡張するユーティリティフィールドが含まれます。詳細は [Enhance Fields](./enhance-fields) を参照してください。

### [SelectableField](https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-selectable-field/index.html)

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
        <td> <a href="https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-selectable-field/index.html">SelectableField</a> </td>
    </tr>
</table>

指定された選択肢のリストから1つのオプションを選択するためのフィールドです。ドロップダウンまたはチップ形式で表示できます。

```kt
PreviewLab {
    MyApp(
        theme = fieldValue {
            // highlight-start
            SelectableField(
                label = "Theme",
                choices = listOf("Light", "Dark", "Auto")
            )
            // highlight-end
        },
    )
}
```

<EmbeddedPreviewLab 
 previewId="SelectableFieldExample"
/>

<details>
<summary><code>type</code> で表示形式を変更する</summary>

`type` パラメータで `SelectableField.Type.CHIPS` を指定すると、チップ形式で表示されます。

```kt
PreviewLab {
    MyApp(
        theme = fieldValue {
            SelectableField(
                label = "Theme",
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
                label = "Theme",
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
            SelectableField<String>(label = "Theme") {
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
/>

</details>

### [EnumField](https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-enum-field.html)

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
        <td> <a href="https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-enum-field.html">EnumField</a> </td>
    </tr>
</table>

Enum クラスの値から選択するためのフィールドです。`SelectableField` のショートハンドとして実装されています。

```kt
enum class ButtonVariant { Primary, Secondary, Tertiary }

PreviewLab {
    MyButton(
        variant = fieldValue {
            // highlight-next-line
            EnumField("Variant", ButtonVariant.Primary)
        },
    )
}
```

<EmbeddedPreviewLab 
 previewId="EnumFieldExample"
/>

### [CombinedField](https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-combined-field/index.html)

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
        <td> <a href="https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-combined-field/index.html">CombinedField</a> </td>
    </tr>
</table>

複数のサブフィールドを1つの複合値に結合するフィールドです。`combine` 関数と `split` 関数を使用して、複数のフィールドを1つの値にまとめます。

```kt
data class Padding(val horizontal: Dp, val vertical: Dp)

PreviewLab {
    Box(
        modifier = Modifier
            .background(Color.LightGray)
            .padding(
                fieldValue {
                    // highlight-start
                    CombinedField(
                        label = "Padding",
                        fields = listOf(
                            DpField("Horizontal", 16.dp),
                            DpField("Vertical", 8.dp)
                        ),
                        combine = { values -> Padding(values[0] as Dp, values[1] as Dp) },
                        split = { listOf(it.horizontal, it.vertical) }
                    )
                    // highlight-end
                }.horizontal,
                fieldValue { /* ... */ }.vertical
            )
    ) {
        Text("Content")
    }
}
```

<EmbeddedPreviewLab 
 previewId="CombinedFieldExample"
/>

<details>
<summary><code>combine</code> と <code>split</code> 引数について</summary>

`combine` 関数は、複数のサブフィールドの値を1つの複合値に結合する関数です。`split` 関数は、複合値を元のサブフィールドの値のリストに分解する関数です。

- `combine`: `List<Any> -> T` の形式で、サブフィールドの値のリストを受け取り、複合型の値を返します
- `split`: `T -> List<Any>` の形式で、複合型の値を受け取り、サブフィールドの値のリストを返します

これらの関数は、`CombinedField` がサブフィールドの値と複合値の間で変換を行うために使用されます。

</details>

<details>
<summary><code>combined</code> 関数を使用する</summary>

`combined` 関数を使用すると、より簡潔に記述できます（2〜10個のフィールドに対応）。

```kt
data class Padding(val horizontal: Dp, val vertical: Dp)

PreviewLab {
    Box(
        modifier = Modifier
            .background(Color.LightGray)
            .padding(
                fieldValue {
                    // highlight-start
                    combined(
                        label = "Padding",
                        field1 = DpField("Horizontal", 16.dp),
                        field2 = DpField("Vertical", 8.dp),
                        combine = { h, v -> Padding(h, v) },
                        split = { splitedOf(it.horizontal, it.vertical) }
                    )
                    // highlight-end
                }.horizontal,
                fieldValue {
                    combined(
                        label = "Padding",
                        field1 = DpField("Horizontal", 16.dp),
                        field2 = DpField("Vertical", 8.dp),
                        combine = { h, v -> Padding(h, v) },
                        split = { splitedOf(it.horizontal, it.vertical) }
                    )
                }.vertical
            )
    ) {
        Text("Content")
    }
}
```

<EmbeddedPreviewLab
 previewId="CombinedFieldWithCombinedFunctionExample"
/>

</details>

### [NullableField](https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-nullable-field/index.html)

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
        <td> <a href="https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-nullable-field/index.html">NullableField</a> </td>
    </tr>
</table>

既存のフィールドを Nullable にするためのフィールドです。チェックボックスで null と値の切り替えができます。

```kt
PreviewLab {
    UserName(
        userName = fieldValue {
            // highlight-next-line
            StringField("User Name", "John Doe").nullable()
        },
    )
}
```

<EmbeddedPreviewLab 
 previewId="NullableFieldExample"
/>

### [TransformField](https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-transform-field/index.html)

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
        <td> <a href="https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-transform-field/index.html">TransformField</a> </td>
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
/>

### [WithHintField](https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-with-hint-field/index.html)

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
        <td> <a href="https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-with-hint-field/index.html">WithHintField</a> </td>
    </tr>
</table>

既存のフィールドにヒント選択肢を追加するフィールドです。定義済みの値から素早く選択できるチップが表示されます。

```kt
PreviewLab {
    Text(
        text = "Sample Text",
        fontSize = fieldValue {
            SpField(label = "Font Size", initialValue = 16.sp)
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
/>

## 3. Compose Value Fields

Compose 固有の値型（Dp、Sp、Color、Offset、Size、Modifier、Composable など）に対応するフィールドです。Compose UI の構築に特化した型を扱います。

### [DpField](https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-dp-field/index.html)

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
        <td> <a href="https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-dp-field/index.html">DpField</a> </td>
    </tr>
</table>

Compose の Dp（密度非依存ピクセル）値を編集するためのフィールドです。自動的に "dp" サフィックスが付きます。

```kt
PreviewLab {
    Box(
        modifier = Modifier
            // highlight-next-line
            .padding(fieldValue { DpField("Padding", 16.dp) })
    ) {
        Text("Padded Content")
    }
}
```

<EmbeddedPreviewLab 
 previewId="DpFieldExample"
/>

### [SpField](https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-sp-field/index.html)

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
        <td> <a href="https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-sp-field/index.html">SpField</a> </td>
    </tr>
</table>

Compose の Sp（スケーラブルピクセル）値を編集するためのフィールドです。フォントサイズなどのテキスト関連の寸法に使用します。自動的に "sp" サフィックスが付きます。

```kt
PreviewLab {
    Text(
        text = "Sample Text",
        // highlight-next-line
        fontSize = fieldValue { SpField("Font Size", 16.sp) },
    )
}
```

<EmbeddedPreviewLab 
 previewId="SpFieldExample"
/>

### [ColorField](https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-color-field/index.html)

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
        <td> <a href="https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-color-field/index.html">ColorField</a> </td>
    </tr>
</table>

Compose の Color を選択するためのフィールドです。インタラクティブなカラーピッカーで HSV スライダーとアルファチャンネルコントロールを使用して色を選択できます。

```kt
PreviewLab {
    Box(
        modifier = Modifier
            .size(100.dp)
            // highlight-next-line
            .background(fieldValue { ColorField("Background", Color.Blue) })
    )
}
```

<EmbeddedPreviewLab 
 previewId="ColorFieldExample"
/>

### [OffsetField](https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-offset-field/index.html)

<table>
    <tr>
        <th>対応する 型</th>
        <td> `androidx.compose.ui.geometry.Offset` </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <a href="https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-offset-field/index.html">OffsetField</a> </td>
    </tr>
</table>

Compose の Offset 値（x, y 座標）を編集するためのフィールドです。各座標を独立して編集できます。

```kt
PreviewLab {
    Box(
        modifier = Modifier
            .size(100.dp)
            .graphicsLayer {
                // highlight-next-line
                val offset = fieldValue { OffsetField("Position", Offset(50f, 100f)) }
                translationX = offset.x
                translationY = offset.y
            }
    )
}
```

<EmbeddedPreviewLab 
 previewId="OffsetFieldExample"
/>

### [DpOffsetField](https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-dp-offset-field/index.html)

<table>
    <tr>
        <th>対応する 型</th>
        <td> `androidx.compose.ui.unit.DpOffset` </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <a href="https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-dp-offset-field/index.html">DpOffsetField</a> </td>
    </tr>
</table>

Compose の DpOffset 値（Dp 単位の x, y 座標）を編集するためのフィールドです。

```kt
PreviewLab {
    Text(
        text = "Positioned Text",
        modifier = Modifier
            // highlight-next-line
            .offset(fieldValue { DpOffsetField("Offset", DpOffset(16.dp, 8.dp)) }.x)
    )
}
```

<EmbeddedPreviewLab 
 previewId="DpOffsetFieldExample"
/>

### [SizeField](https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-size-field/index.html)

<table>
    <tr>
        <th>対応する 型</th>
        <td> `androidx.compose.ui.geometry.Size` </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <a href="https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-size-field/index.html">SizeField</a> </td>
    </tr>
</table>

Compose の Size 値（幅、高さ）を編集するためのフィールドです。各寸法を独立して編集できます。

```kt
PreviewLab {
    Canvas(
        modifier = Modifier.size(200.dp)
    ) {
        // highlight-next-line
        val canvasSize = fieldValue { SizeField("Canvas", Size(200f, 150f)) }
        drawRect(Color.Blue, size = canvasSize)
    }
}
```

<EmbeddedPreviewLab 
 previewId="SizeFieldExample"
/>

### [DpSizeField](https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-dp-size-field/index.html)

<table>
    <tr>
        <th>対応する 型</th>
        <td> `androidx.compose.ui.unit.DpSize` </td>
    </tr>
    <tr>
        <th>利用頻度</th>
        <td> ⭐⭐ </td>
    </tr>
    <tr>
        <th>KDoc</th>
        <td> <a href="https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-dp-size-field/index.html">DpSizeField</a> </td>
    </tr>
</table>

Compose の DpSize 値（Dp 単位の幅、高さ）を編集するためのフィールドです。

```kt
PreviewLab {
    Button(
        onClick = { },
        modifier = Modifier
            // highlight-next-line
            .size(fieldValue { DpSizeField("Button Size", DpSize(120.dp, 48.dp)) })
    ) {
        Text("Sized Button")
    }
}
```

<EmbeddedPreviewLab 
 previewId="DpSizeFieldExample"
/>

### [ModifierField](https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-modifier-field/index.html)

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
        <td> <a href="https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-modifier-field/index.html">ModifierField</a> </td>
    </tr>
</table>

Compose の Modifier チェーンを構築・編集するためのインタラクティブなフィールドです。ビルダーパターンで複雑な Modifier チェーンを視覚的に構築できます。

```kt
PreviewLab {
    Button(
        onClick = { },
        // highlight-next-line
        modifier = fieldValue { ModifierField("Button modifier") },
    ) {
        Text("Styled Button")
    }
}
```

<EmbeddedPreviewLab 
 previewId="ModifierFieldExample"
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
                label = "Button modifier",
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
/>

</details>

### [ComposableField](https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-composable-field/index.html)

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
        <td> <a href="https://tbsten.github.io/compose-preview-lab/dokka/core/me.tbsten.compose.preview.lab.field/-composable-field/index.html">ComposableField</a> </td>
    </tr>
</table>

定義済みの Composable コンテンツオプションから選択するためのフィールドです。

```kt
PreviewLab {
    MyContainer(
        content = fieldValue {
            // highlight-next-line
            ComposableField(
                label = "Content",
                initialValue = ComposableFieldValue.Red32X32
            )
        },
    )
}
```

<EmbeddedPreviewLab 
 previewId="ComposableFieldExample"
/>

<details>
<summary><code>ComposableFieldValue</code> の定義済み値を使用する</summary>

`ComposableFieldValue` には、`ColorBox`、`Text`、`Empty` などの定義済み値が用意されています。

```kt
PreviewLab {
    MyContainer(
        content = fieldValue {
            ComposableField(
                label = "Content",
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
/>

</details>
