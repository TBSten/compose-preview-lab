---
title: Custom Fields
sidebar_position: 4
---

:::warning

このページは生成 AI によって自動生成されたページです。

:::

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# Custom Fields

Compose Preview Lab には多くのビルトイン Field が用意されていますが、  
独自の型（たとえば `LocalDate` やアプリ固有の `UiState`）を編集したい場合は **Custom Field** を実装することで柔軟に拡張できます。

:::info Custom Field とは？
- `PreviewLabField<Value>` / `MutablePreviewLabField<Value>` を継承して独自の Field クラスを作る  
- Field 内で Compose UI を自由に組み立てられる  
- 既存の Field（`StringField`, `IntField`, `DpField` など）を内部で再利用することも可能  
:::

## PreviewLabField / MutablePreviewLabField の基本

Custom Field を実装する際は、通常 `MutablePreviewLabField<T>` を継承します。

```kt
abstract class MutablePreviewLabField<Value> : PreviewLabField<Value>, MutableState<Value>
```

基本的に以下の 2 点を実装すれば動作します。

- 値を保持する **State**
- 編集 UI を描画する **`Content()` Composable**

## 1. シンプルな Custom Field の例: DateField

日付を編集する `DateField` の例です（UI 部分は疑似コードです）。

```kt
class DateField(
    override val label: String,
    initialValue: LocalDate,
) : MutablePreviewLabField<LocalDate>() {

    // 値を保持する State
    private val state = mutableStateOf(initialValue)

    override var value: LocalDate
        get() = state.value
        set(value) { state.value = value }

    @Composable
    override fun Content() {
        // ラベルと日付ピッカーを表示する
        Column {
            Text(label)
            DatePicker(
                date = value,
                onDateChange = { value = it },
            )
        }
    }
}
```

```kt
@Preview
@Composable
fun DateFieldPreview() = PreviewLab {
    val date = fieldValue { DateField("birthday", LocalDate.now()) }
    ProfileScreen(birthday = date)
}
```

:::tip View をオーバーライドする必要はある？
`PreviewLabField` には `View()` という「ラベル付きフル UI」を描画する関数もありますが、  
通常は `Content()` だけをオーバーライドすれば問題ありません。  
標準のレイアウトに乗せたい場合は `Content()` のみ実装するのがおすすめです。
:::

## 2. 既存の Field を組み合わせて Custom Field を作る

0 から UI を作るだけでなく、既存の Field を組み合わせることで複合的な Custom Field を構築できます。  
`CombinedField` や `combined()` ヘルパーはそのために用意されたユーティリティです。

### CombinedField を直接使う例

```kt
data class Padding(val horizontal: Dp, val vertical: Dp)

@Preview
@Composable
fun PaddingFieldPreview() = PreviewLab {
    val padding: Padding = fieldValue {
        CombinedField(
            label = "Padding",
            fields = listOf(
                DpField("Horizontal", 16.dp),
                DpField("Vertical", 8.dp),
            ),
            combine = { values -> Padding(values[0] as Dp, values[1] as Dp) },
            split = { listOf(it.horizontal, it.vertical) },
        )
    }

    Box(
        modifier = Modifier
            .background(Color.LightGray)
            .padding(horizontal = padding.horizontal, vertical = padding.vertical),
    ) {
        Text("Content with custom padding")
    }
}
```

<EmbeddedPreviewLab
  previewId="CombinedFieldExample"
/>

### combined() ヘルパーでより読みやすく書く

```kt
data class Padding(val horizontal: Dp, val vertical: Dp)

@Preview
@Composable
fun PaddingFieldWithHelperPreview() = PreviewLab {
    val padding: Padding = fieldValue {
        combined(
            label = "Padding",
            field1 = DpField("Horizontal", 16.dp),
            field2 = DpField("Vertical", 8.dp),
            combine = { h, v -> Padding(h, v) },
            split = { splitedOf(it.horizontal, it.vertical) },
        )
    }

    Box(
        modifier = Modifier
            .background(Color.LightGray)
            .padding(horizontal = padding.horizontal, vertical = padding.vertical),
    ) {
        Text("Content with custom padding (helper)")
    }
}
```

<EmbeddedPreviewLab
  previewId="CombinedFieldWithCombinedFunctionExample"
/>

## 3. 既存の Field をラップして拡張する

完全に新しい Field を 0 から実装する代わりに、  
既存の Field をラップして変換・ヒント追加などを行うパターンもよく使われます。

### TransformField で型を変換する

```kt
val intValue = fieldValue {
    StringField("number", "42")
        // highlight-start
        .transform(
            transform = { it.toIntOrNull() ?: 0 },
            reverse = { it.toString() },
        )
        // highlight-end
}
```

<EmbeddedPreviewLab
  previewId="TransformFieldExample"
/>

### withHint() でよく使うプリセットを追加する

```kt
val fontSize = fieldValue {
    SpField(label = "Font Size", initialValue = 16.sp)
        // highlight-start
        .withHint(
            "Small" to 12.sp,
            "Medium" to 16.sp,
            "Large" to 20.sp,
            "XLarge" to 24.sp,
        )
        // highlight-end
}
```

<EmbeddedPreviewLab
  previewId="WithHintFieldExample"
/>

:::tip まずは「拡張」から始める
完全自作の Field を実装する前に、`transform` / `withHint` / `combined` などの **Enhance Fields** を活用すると、  
少ないコードで多くのユースケースをカバーできます。  
本当に必要になったときにだけ `MutablePreviewLabField` を継承した自前 Field を検討するのがおすすめです。
:::

## 次のステップ

- [Field の使用例](./05-field-examples) - 様々な Preview シナリオでの Field の使い方

