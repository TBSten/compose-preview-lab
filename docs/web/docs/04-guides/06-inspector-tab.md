---
title: Inspector Tab
sidebar_position: 6
---

import EmbeddedPreviewLab from "@site/src/components/EmbeddedPreviewLab";
import KDocLink from '@site/src/components/KDocLink';

# Inspector Tab

Inspector Tab は、PreviewLab で 右側に表示されるインスペクターパネルに表示するタブのことです。

デフォルトでは2つの Fields, Events という2つの便利なタブが表示されていますが、ユーザが自由にカスタムのタブを追加できるように設計されています。

## 概要

PreviewLab のインスペクターパネルには、コンポーネントの状態を確認・操作するためのタブが用意されています。デフォルトでは `Fields`、
`Events`、`Code` の3つのタブが含まれており、`InspectorTab` インターフェースを実装することで独自のタブを追加できます。

## ビルトインタブ

PreviewLab にはデフォルトで以下の2つのタブが表示されます。これらは `InspectorTab.defaults` に含まれており、デフォルトで表示されます。

### InspectorTab.Fields

すべての Field を表示するタブです。コンポーネントのプロパティや状態をリアルタイムで変更できます。

import inspectorTabField from "./img/inspector-tab-field.png"

Field の詳細は [Field](./fields/overview) のドキュメントを参照してください。

<img src={inspectorTabField} width="250" />

### InspectorTab.Events

すべてのログイベントを表示するタブです。コンポーネントで発生したイベントを時系列で確認できます。

import inspectorTabEvent from "./img/inspector-tab-event.png"

<img src={inspectorTabEvent} width="250" />

Field の詳細は [Event](./events/) のドキュメントを参照してください。

### InspectorTab.Code

現在表示中の Preview の Kotlin コードスニペットを表示するタブです。各 `fieldValue { ... }` 呼び出しが、その Field の `valueCode()`
メソッドが返すコードに置き換えられた形で表示されます。

これにより、PreviewLab で設定した現在の値を、そのままコピー&ペーストできる Kotlin コードとして確認できます。

import inspectorTabCode from "./img/inspector-tab-code.png"

<img src={inspectorTabCode} width="250" />

:::warning

現在 Code タブは 現在開発中の機能で デフォルトでは表示されないよう構成されています。

利用したい場合は 以下のように ExperimentalComposePreviewLabApi を OptIn して PreviewLab.inspectorTabs 引数に Code
タブを指定する必要があります。

```kt
@OptIn(ExperimentalComposePreviewLabApi::class)
PreviewLab(
    inspectorTabs = InspectorTab.defaults + InspectorTab.Code,
) {
    // ...
}
```

:::

<details>
<summary>Code タブの動作について</summary>

Code タブは、`PreviewLabPreview.code` に保存されている元のコードを基に、各 Field の `label` と `fieldValue { ... }` 呼び出しを検索して、その
Field の `valueCode()` が返すコードに置き換えます。

各 Field の `valueCode()` をカスタマイズすることで、Code タブに表示されるコードスニペットを自分のプロジェクトの API
形式に合わせることができます。詳細は [Field.withValueCode()](./fields/enhance-fields) を参照してください。

:::note 制限事項

- Field の `label` が重複している場合、期待通りに置換されない可能性があります。
- 複雑なカスタムパターンでは、100% 正確に元のコードを再現するものではなく、「コピー&ペースト用のたたき台」として使用することを想定しています。例えば一部の
  Field では元のコードの再現が確実にはできないため、代わりに TODO コメントが挿入されます。

:::

</details>

## 表示するタブの制御

表示するタブは `PreviewLab.inspectorTabs` 引数で設定できます。
PreviewLab ごと、またはプロジェクト全体で表示する Preview を設定できます。

```kt title="PreviewLab ごとに表示するタブを制御する"
@Preview
@Composable
internal fun MyPreviewWithTab() = PreviewLab(
    inspectorTabs = InspectorTab.defaults,
    // or 
    inspectorTabs = emptyList(), // タブを一切表示しない
) {
    // ...
}
```

```kt title="プロジェクト全体で表示するタブを制御する"
@Composable
fun MyProjectPreviewLab(
    modifier: Modifier = Modifier,
    content: @Composable PreviewLabScope.() -> Unit,
) = PreviewLab(
    modifier = modifier,
    inspectorTabs = InspectorTab.defaults,
    // or
    // inspectorTabs = emptyList(), // タブを一切表示しない
    content = content,
)

@Preview
@Composable
internal fun MyPreviewWithTab() = MyProjectPreviewLab {
    // ...
}
```

## カスタムタブの実装

新たなタブを気軽に実装することができます。以下に独自タブのアイデアと実装例を示しています。

<details>
<summary>コンポーネントのドキュメントタブ</summary>

個々のコンポーネントの役割・利用法などを示すための場所として カスタムタブを利用できます。

```kt
object DocsTab : InspectorTab {
    override val title = "Docs"
    override val icon: @Composable () -> Painter = { painterResource(Res.drawable.icon_doc) }

    @Composable
    override fun InspectorTab.ContentContext.Content() {
        Column {
            Text("You can display detailed documentation for this component here.")
        }
    }
}
```

<EmbeddedPreviewLab previewId="InspectorTabDocsExample" title="Inspector Tab Docs Example" />

</details>

<details>
<summary>PreviewLabState にアクセスして 複数の Field の操作を自動化する</summary>

これを利用して複数の Field の状態を一括で変更するショートカットを配置することができます。

```kt
object DebugTab : InspectorTab {
    override val title = "Debug"
    override val icon: @Composable () -> Painter = { painterResource(Res.drawable.icon_debug) }

    @Composable
    override fun InspectorTab.ContentContext.Content() {
        Column {
            val allFields = state.fields
            Text("Field の数: ${allFields.size}")

            Button(
                onClick = {
                    val textField: MutablePreviewLabField<String> by state.field<String>(label = "text")
                    val sizeField: MutablePreviewLabField<DpSize> by state.field<DpSize>(label = "size")

                    textField.value = "very ".repeat(50) + "text"
                    sizeField.value = DpSize(300.dp, 300.dp)
                },
            ) {
                Text("Set field value to large content pattern")
            }
        }
    }
}
```

<EmbeddedPreviewLab previewId="InspectorTabDebugExample" title="Inspector Tab Debug Example" />

</details>

### InspectorTab インターフェース

カスタムタブを作成するには、`InspectorTab` インターフェースを実装します。このインターフェースには以下のプロパティとメソッドがあります：

- `title: String` - タブに表示されるタイトル
- `icon: (@Composable () -> Painter)?` - タブのアイコン（オプション）
- `ContentContext.Content()` - タブが選択されたときに表示されるコンテンツ

`ContentContext` を通じて `PreviewLabState` にアクセスでき、フィールド、イベント、その他のプレビュー状態を取得できます。

### 基本的な実装

InspectorTab のメンバをオーバーライドして実装します。

```kt
object CustomTab : InspectorTab {
    override val title = "Custom"
    override val icon: @Composable () -> Painter = { 
        painterResource(Res.drawable.icon_custom) 
    }

    @Composable
    override fun InspectorTab.ContentContext.Content() {
        Column {
            Text("Custom Tab Content")
            Text("Field count: ${state.fields.size}")
        }
    }
}
```

`InspectorTab.ContentContext.Content()` メソッド内では以下の情報にアクセスすることができます。

- `state` ... PreviewLabState の情報にアクセスできます。詳しくは <KDocLink path="core/me.tbsten.compose.preview.lab/-preview-lab-state/index.html?query=class%20PreviewLabState">PreviewLabState の KDoc</KDocLink> を参照してください。
  - `fields` ... 登録されているフィールドの一覧を取得します。
  - `field()` ... Field の型とラベルを指定して 特定のフィールドを取得します。
  - `events` ... onEvent 呼び出しにより発生したイベントのリストを取得します。
- `inspectorTabs` ... 現在表示されているタブの一覧 (`List<InspectorTab>`) を取得します。

```kt
@Composable
override fun InspectorTab.ContentContext.Content() {
    // 登録されているフィールドの数を表示する
    Text("Fields count: ${state.fields.size}")

    // text フィールドの値を更新する
    val textField by state.field<String>("text")
    textField.value = "..."

    // 現在のタブ一覧を取得する
    val tabs = inspectorTabs
}
```

### PreviewLab への適用

[表示するタブの制御](#表示するタブの制御) に従って、作成したカスタムタブを `PreviewLab` の `inspectorTabs` パラメータに渡します。

```kt
@Preview
@Composable
fun MyPreview() = PreviewLab(
    // highlight-next-line
    inspectorTabs = InspectorTab.defaults + CustomTab,
) {
    MyComponent()
}
```

### Preview コンテンツ内から state にアクセスする

`PreviewLabScope` には `state` プロパティが公開されており、Preview のコンテンツ内から `PreviewLabState` にアクセスできます。これにより、選択中のタブの変更など、プログラムから Inspector の状態を制御できます。

```kt
@Preview
@Composable
fun MyPreview() = PreviewLab(
    inspectorTabs = InspectorTab.defaults + CustomTab,
) {
    // Preview コンテンツ内から state にアクセス
    LaunchedEffect(Unit) {
        @OptIn(ExperimentalComposePreviewLabApi::class)
        state.selectedTabIndex = 2  // 3番目のタブを選択
    }

    val text = fieldValue { StringField("text", "Hello") }
    Text(text)
}
```
