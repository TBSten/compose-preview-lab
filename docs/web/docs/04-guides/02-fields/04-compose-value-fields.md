---
title: Compose Value Fields
---

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';
import KDocLink from '@site/src/components/KDocLink';

# Compose Value Fields

Compose 固有の値型（Dp、Sp、Color、Offset、Size、Modifier、Composable など）に対応するフィールドです。Compose UI の構築に特化した型を扱います。

### <KDocLink path="field/me.tbsten.compose.preview.lab.field/-dp-field/index.html">DpField</KDocLink>

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
        <td> <KDocLink path="field/me.tbsten.compose.preview.lab.field/-dp-field/index.html">DpField</KDocLink> </td>
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

### <KDocLink path="field/me.tbsten.compose.preview.lab.field/-sp-field/index.html">SpField</KDocLink>

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
        <td> <KDocLink path="field/me.tbsten.compose.preview.lab.field/-sp-field/index.html">SpField</KDocLink> </td>
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

### <KDocLink path="field/me.tbsten.compose.preview.lab.field/-color-field/index.html">ColorField</KDocLink>

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
        <td> <KDocLink path="field/me.tbsten.compose.preview.lab.field/-color-field/index.html">ColorField</KDocLink> </td>
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
        <td> <KDocLink path="field/me.tbsten.compose.preview.lab.field/-offset-field/index.html">OffsetField</KDocLink> </td>
    </tr>
    <tr>
        <td> DpOffsetField </td>
        <td> `androidx.compose.ui.unit.DpOffset` </td>
        <td> ⭐⭐ </td>
        <td> <KDocLink path="field/me.tbsten.compose.preview.lab.field/-dp-offset-field/index.html">DpOffsetField</KDocLink> </td>
    </tr>
    <tr>
        <td> SizeField </td>
        <td> `androidx.compose.ui.geometry.Size` </td>
        <td> ⭐⭐ </td>
        <td> <KDocLink path="field/me.tbsten.compose.preview.lab.field/-size-field/index.html">SizeField</KDocLink> </td>
    </tr>
    <tr>
        <td> DpSizeField </td>
        <td> `androidx.compose.ui.unit.DpSize` </td>
        <td> ⭐⭐ </td>
        <td> <KDocLink path="field/me.tbsten.compose.preview.lab.field/-dp-size-field/index.html">DpSizeField</KDocLink> </td>
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

### <KDocLink path="field/me.tbsten.compose.preview.lab.field/-modifier-field/index.html">ModifierField</KDocLink>

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
        <td> <KDocLink path="field/me.tbsten.compose.preview.lab.field/-modifier-field/index.html">ModifierField</KDocLink> </td>
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

### <KDocLink path="field/me.tbsten.compose.preview.lab.field/-composable-field/index.html">ComposableField</KDocLink>

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
        <td> <KDocLink path="field/me.tbsten.compose.preview.lab.field/-composable-field/index.html">ComposableField</KDocLink> </td>
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
