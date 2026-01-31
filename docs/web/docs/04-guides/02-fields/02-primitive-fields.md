---
title: Primitive Fields
---

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';
import KDocLink from '@site/src/components/KDocLink';
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Primitive Fields

Kotlin の基本的なプリミティブ型（String、Boolean、数値型など）に対応するフィールドです。最も基本的で頻繁に使用されるフィールド群です。

### <KDocLink path="field/me.tbsten.compose.preview.lab.field/-string-field/index.html">StringField</KDocLink>

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
        <td> <KDocLink path="field/me.tbsten.compose.preview.lab.field/-string-field/index.html">StringField</KDocLink> </td>
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

### <KDocLink path="field/me.tbsten.compose.preview.lab.field/-boolean-field/index.html">BooleanField</KDocLink>

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
        <td> <KDocLink path="field/me.tbsten.compose.preview.lab.field/-boolean-field/index.html">BooleanField</KDocLink> </td>
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
        <td> <KDocLink path="field/me.tbsten.compose.preview.lab.field/-int-field/index.html">IntField</KDocLink> </td>
    </tr>
    <tr>
        <td> LongField </td>
        <td> `kotlin.Long` </td>
        <td> ⭐⭐ </td>
        <td> <KDocLink path="field/me.tbsten.compose.preview.lab.field/-long-field/index.html">LongField</KDocLink> </td>
    </tr>
    <tr>
        <td> ByteField </td>
        <td> `kotlin.Byte` </td>
        <td> ⭐ </td>
        <td> <KDocLink path="field/me.tbsten.compose.preview.lab.field/-byte-field/index.html">ByteField</KDocLink> </td>
    </tr>
    <tr>
        <td> DoubleField </td>
        <td> `kotlin.Double` </td>
        <td> ⭐⭐ </td>
        <td> <KDocLink path="field/me.tbsten.compose.preview.lab.field/-double-field/index.html">DoubleField</KDocLink> </td>
    </tr>
    <tr>
        <td> FloatField </td>
        <td> `kotlin.Float` </td>
        <td> ⭐⭐ </td>
        <td> <KDocLink path="field/me.tbsten.compose.preview.lab.field/-float-field/index.html">FloatField</KDocLink> </td>
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
