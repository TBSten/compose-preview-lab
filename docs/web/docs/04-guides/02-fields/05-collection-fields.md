---
title: Collection Fields
---

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';
import KDocLink from '@site/src/components/KDocLink';

# Collection Fields

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
