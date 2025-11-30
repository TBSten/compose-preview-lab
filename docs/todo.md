# Compose Preview Lab - ドキュメント構造TODO

## 推奨サイドバー階層構造

以下は、ライブラリ全体の調査結果に基づいた、学習しやすく網羅的なドキュメント構造案です。

```
📚 Compose Preview Lab Documentation
│
├── 🚀 Getting Started
│   ├── Introduction (新規)
│   │   └── What is Compose Preview Lab?
│   │   └── Key Features
│   │   └── When to use it
│   ├── Installation (02-install.md - TODO)
│   │   └── Gradle setup
│   │   └── KSP plugin configuration
│   │   └── Platform-specific setup (Android/iOS/Web/Desktop)
│   └── Quick Start (01-get-started.md - 既存)
│       └── First Preview
│       └── Adding Fields
│       └── Testing with Events
│
├── 📖 Core Concepts (新規セクション)
│   ├── Architecture Overview (03-guides/02-basic-architecture.md - WIP)
│   │   └── PreviewLab vs PreviewLabGallery
│   │   └── PreviewLabScope
│   │   └── State management
│   ├── Preview Basics (新規)
│   │   └── Creating a Preview
│   │   └── Using PreviewLab wrapper
│   │   └── Content root customization
│   └── Field System Overview (03-guides/03-fields/index.md - 既存)
│       └── fieldValue() vs fieldState()
│       └── Field lifecycle
│       └── When to use which
│
├── 🎛️ Fields (重要 - 最も詳しいセクション)
│   ├── Basic Fields
│   │   ├── All Built-in Fields (03-guides/03-fields/02-all-fields.md - TODO)
│   │   │   └── StringField
│   │   │   └── BooleanField
│   │   │   └── Numeric Fields (Int/Long/Float/Double/Byte)
│   │   │   └── ColorField
│   │   │   └── SelectableField
│   │   │   └── EnumField
│   │   │   └── ModifierField
│   │   │
│   │   ├── Field Input Types (新規)
│   │   │   └── TextField vs Slider
│   │   │   └── Switch vs Checkbox
│   │   │   └── Dropdown vs Chips
│   │   │
│   │   └── Field Options (新規)
│   │       └── Labels and descriptions
│   │       └── Prefix and suffix
│   │       └── Validation (if applicable)
│   │
│   ├── Enhanced Fields
│   │   ├── Nullable Fields (03-guides/03-fields/03-enhance-fields.md - TODO)
│   │   │   └── Using .nullable()
│   │   │   └── Default null vs non-null
│   │   │   └── Use cases
│   │   │
│   │   ├── Fields with Hints (含む)
│   │   │   └── Using .withHint()
│   │   │   └── Quick value selection
│   │   │   └── Best practices
│   │   │
│   │   ├── Combined Fields (含む)
│   │   │   └── CombinedField2～10
│   │   │   └── Composing complex types
│   │   │   └── combine and split functions
│   │   │
│   │   └── Transform Fields (新規)
│   │       └── Transforming field values
│   │       └── Mapping between types
│   │
│   └── Custom Fields (03-guides/03-fields/04-custom-fields.md - TODO)
│       ├── Creating Custom Fields
│       │   └── Implementing PreviewLabField interface
│       │   └── View() vs Content()
│       │   └── State management in custom fields
│       │
│       ├── Custom UI Components (新規)
│       │   └── Building field UI
│       │   └── Styling and theming
│       │
│       └── Advanced Patterns (新規)
│           └── Reusable field templates
│           └── Field composition
│
├── 🖥️ Multi-Device Testing (新規セクション)
│   ├── Screen Sizes (新規)
│   │   └── Default screen sizes
│   │   └── Custom screen sizes
│   │   └── ScreenSize presets
│   │   └── Testing responsive layouts
│   │
│   ├── Device Frames (新規)
│   │   └── Enabling device frames
│   │   └── Frame customization
│   │
│   └── Zoom and Pan (新規)
│       └── Preview navigation controls
│       └── Keyboard shortcuts
│
├── 🎯 Events and Debugging (03-guides/04-events.md - 基本のみ)
│   ├── Event System (既存を拡張)
│   │   └── Using onEvent()
│   │   └── Event titles and descriptions
│   │   └── Event log UI
│   │
│   ├── Debugging with Events (新規)
│   │   └── Tracking user interactions
│   │   └── Testing callback behavior
│   │   └── Event-driven testing workflow
│   │
│   └── Toast Notifications (新規)
│       └── Displaying feedback
│       └── Custom toast duration
│
├── 📂 Preview Gallery (新規セクション)
│   ├── PreviewLabGallery Basics (新規)
│   │   └── Setting up a gallery
│   │   └── @Preview annotation detection
│   │   └── Navigation and search
│   │
│   ├── Organizing Previews (03-guides/05-featured-files.md - 既存)
│   │   └── Featured files
│   │   └── Grouping by category
│   │   └── Custom navigation
│   │
│   └── Gallery Customization (新規)
│       └── Sidebar configuration
│       └── Preview metadata
│
├── 🔧 Advanced Topics (新規セクション)
│   ├── Modifier Field Deep Dive (新規)
│   │   └── ModifierFieldValue chain
│   │   └── Available modifier methods
│   │   └── Visual marking (.mark())
│   │   └── Custom modifier extensions
│   │
│   ├── State Management (新規)
│   │   └── PreviewLabState
│   │   └── rememberSaveable integration
│   │   └── State persistence
│   │   └── Resetting state
│   │
│   ├── Theme Integration (新規)
│   │   └── Using contentRoot
│   │   └── Material Theme wrapping
│   │   └── Dark mode testing
│   │
│   └── Performance Optimization (新規)
│       └── Managing many previews
│       └── Field state optimization
│       └── Memory considerations
│
├── 🌐 Platform-Specific Features (新規セクション)
│   ├── Web/WASM (新規)
│   │   └── Embedded previews
│   │   └── renderPreviewLabPreview()
│   │   └── @JsExport usage
│   │   └── iframe integration
│   │
│   ├── Android (新規)
│   │   └── Android-specific features
│   │   └── Integration with Android Studio
│   │
│   ├── iOS (新規)
│   │   └── iOS-specific considerations
│   │
│   └── Desktop (新規)
│       └── Desktop window management
│
├── 🎓 Tutorials (実践的な使用例)
│   ├── Building a UI Catalog (04-tutorials/02-preview-ui-catalog.md - TODO)
│   │   └── Setting up a component library
│   │   └── Creating comprehensive previews
│   │   └── Organizing design system
│   │
│   ├── Improving UI Review Workflow (04-tutorials/03-improve-ui-review.md - TODO)
│   │   └── Using PreviewLab in Pull Requests
│   │   └── Visual regression testing
│   │   └── Collaborative review
│   │
│   ├── Embedded Playground (04-tutorials/04-embedded-playground.md - TODO)
│   │   └── Building interactive docs
│   │   └── Embedding in Docusaurus/VitePress
│   │   └── OpenFileHandler integration
│   │
│   └── Testing Complex Components (新規)
│       └── Multi-parameter testing
│       └── State-driven components
│       └── Event-driven workflows
│
└── 📚 API Reference (新規セクション)
    ├── Core API (新規)
    │   └── PreviewLab composable
    │   └── PreviewLabGallery composable
    │   └── PreviewLabScope
    │
    ├── Field API (新規)
    │   └── All field constructors
    │   └── Field modifiers (.nullable(), .withHint())
    │   └── CombinedField functions
    │
    ├── Event API (新規)
    │   └── onEvent()
    │   └── PreviewLabEvent
    │
    └── Utility API (新規)
        └── ScreenSize
        └── OpenFileHandler
        └── renderPreviewLabPreview()
```

---

## 優先度マトリックス

### 🔴 最優先 (P0) - ユーザーが最初に必要とするドキュメント

| 項目 | 既存ファイル | 状態 | 理由 |
|:---|:---|:---|:---|
| Introduction | - | 新規 | ライブラリの価値提案を明確に |
| Installation | `02-install.md` | TODO | 使い始められないと何もできない |
| Quick Start | `01-get-started.md` | ✅ 既存 | 最初の体験を提供 |
| All Built-in Fields | `03-guides/03-fields/02-all-fields.md` | TODO | 最も頻繁に参照されるリファレンス |
| Field System Overview | `03-guides/03-fields/index.md` | ✅ 既存 | fieldValue/fieldStateの違いは重要 |
| Nullable Fields | `03-guides/03-fields/03-enhance-fields.md` | TODO | 実用的なユースケースで頻出 |
| Fields with Hints | 同上 | TODO | UX改善の重要パターン |

### 🟡 中優先 (P1) - 実用上重要な機能

| 項目 | 既存ファイル | 状態 | 理由 |
|:---|:---|:---|:---|
| Architecture Overview | `03-guides/02-basic-architecture.md` | WIP | 設計思想の理解に必要 |
| Combined Fields | `03-guides/03-fields/03-enhance-fields.md` | TODO | 複雑な型を扱う際に必須 |
| Custom Fields | `03-guides/03-fields/04-custom-fields.md` | TODO | 拡張性を活かすために重要 |
| Events and Debugging | `03-guides/04-events.md` | 基本のみ | インタラクティブテストの要 |
| Screen Sizes | - | 新規 | マルチデバイステストの基本 |
| Preview Gallery | - | 新規 | 複数Preview管理の要 |
| Organizing Previews | `03-guides/05-featured-files.md` | ✅ 既存 | スケールする際に重要 |

### 🟢 低優先 (P2) - 高度な使用例・特殊ケース

| 項目 | 既存ファイル | 状態 | 理由 |
|:---|:---|:---|:---|
| Building a UI Catalog | `04-tutorials/02-preview-ui-catalog.md` | TODO | 実践例だが必須ではない |
| Improving UI Review | `04-tutorials/03-improve-ui-review.md` | TODO | ワークフロー改善提案 |
| Embedded Playground | `04-tutorials/04-embedded-playground.md` | TODO | Web特化の高度な使い方 |
| Modifier Field Deep Dive | - | 新規 | ModifierFieldは高度な機能 |
| State Management | - | 新規 | 内部実装の理解 |
| Theme Integration | - | 新規 | 応用的なカスタマイズ |
| Performance Optimization | - | 新規 | 大規模プロジェクト向け |
| Platform-Specific Features | - | 新規 | 特定プラットフォーム依存 |
| API Reference | - | 新規 | KDocから自動生成可能 |

---

## 各セクションで書くべき内容の詳細

### 🚀 Getting Started

#### Introduction (新規)
```markdown
# What is Compose Preview Lab?

## 概要
- 従来の@Previewとの違い
- インタラクティブプレビューの価値
- Field駆動型テストの利点

## Key Features
- 🎛️ Dynamic field controls
- 📱 Multi-device testing
- 🎯 Event tracking
- 📂 Preview gallery
- 🌐 Embeddable previews

## When to use it
- Component library development
- Design system documentation
- UI review workflow
- Interactive documentation
```

#### Installation (TODO - `02-install.md`)
```markdown
# Installation

## Requirements
- Kotlin Multiplatform 2.x
- Compose Multiplatform 1.7+
- KSP (Kotlin Symbol Processing)

## Gradle Setup

### 1. Add repository
### 2. Add dependencies
   - core library
   - KSP processor (for @Preview detection)
### 3. Configure KSP
### 4. Platform-specific configuration
   - Android: manifest, compose setup
   - iOS: framework export
   - Web/WASM: webpack/Vite config
   - Desktop: compose.desktop setup

## Troubleshooting
- Common errors
- Version compatibility
```

### 🎛️ Fields - 最重要セクション

#### All Built-in Fields (TODO - `03-guides/03-fields/02-all-fields.md`)
```markdown
# All Built-in Fields

各Fieldについて:

## StringField
### 説明
文字列入力フィールド

### 構文
`StringField(label: String, initialValue: String, prefix: String? = null, suffix: String? = null)`

### 使用例
fieldValue { StringField("Name", "John Doe") }

### オプション
- prefix: プレフィックステキスト
- suffix: サフィックステキスト

### ユースケース
- テキスト入力
- URL入力
- メールアドレス

---

## BooleanField
... (同様のフォーマット)

## IntField
### InputType
- TextField (default): テキスト入力
- Slider: スライダー UI

### 使用例
fieldValue { IntField("Count", 5, inputType = IntField.InputType.Slider(0..100)) }

---

(すべてのField型を網羅)
```

#### Enhanced Fields (TODO - `03-guides/03-fields/03-enhance-fields.md`)
```markdown
# Enhanced Fields

## Nullable Fields

### 基本的な使い方
anyField.nullable(initialValue = null)

### ユースケース
- Optional parameters
- Conditional rendering

### 例
val bio: String? = fieldValue {
    StringField("Bio", "Default bio")
        .nullable(initialValue = null)
}

---

## Fields with Hints

### 基本的な使い方
anyField.withHint("Label1" to value1, "Label2" to value2)

### ユースケース
- よく使う値のクイック選択
- デフォルト値候補の提示

### 例
val padding = fieldValue {
    IntField("Padding", 16)
        .withHint(
            "None" to 0,
            "Small" to 8,
            "Medium" to 16,
            "Large" to 24
        )
}

---

## Combined Fields

### CombinedField2～10
複数のFieldを結合して複合型を作成

### 基本構文
combined(
    label = "Combined",
    field1 = Field1(...),
    field2 = Field2(...),
    combine = { v1, v2 -> Result(v1, v2) },
    split = { splitedOf(it.property1, it.property2) }
)

### ユースケース
- data class のフィールド化
- 複数パラメータの一括管理

### 例: Padding
data class Padding(val horizontal: Dp, val vertical: Dp)

val padding = fieldValue {
    combined(
        label = "Padding",
        field1 = DpField("Horizontal", 16.dp),
        field2 = DpField("Vertical", 8.dp),
        combine = { h, v -> Padding(h, v) },
        split = { splitedOf(it.horizontal, it.vertical) }
    )
}

### 例: 3つ以上のフィールド
combined(
    label = "RGB Color",
    field1 = IntField("R", 255, inputType = Slider(0..255)),
    field2 = IntField("G", 0, inputType = Slider(0..255)),
    field3 = IntField("B", 0, inputType = Slider(0..255)),
    combine = { r, g, b -> Color(r, g, b) },
    split = { splitedOf(it.red, it.green, it.blue) }
)
```

#### Custom Fields (TODO - `03-guides/03-fields/04-custom-fields.md`)
```markdown
# Creating Custom Fields

## PreviewLabField インターフェース

### 必須実装
- `View()`: ラベル付きフルUI
- `Content()`: ラベルなしコンテンツ部分

### Immutable vs Mutable
- ImmutablePreviewLabField<T>: 値が変わらない
- MutablePreviewLabField<T>: 状態が変わる

## Step-by-step ガイド

### 1. Fieldクラスを作成
class DateField(
    override val label: String,
    initialValue: LocalDate
) : MutablePreviewLabField<LocalDate> {
    override val value: MutableState<LocalDate> = mutableStateOf(initialValue)
}

### 2. Content UIを実装
@Composable
override fun Content() {
    DatePicker(
        date = value.value,
        onDateChange = { value.value = it }
    )
}

### 3. (Optional) View をオーバーライド
@Composable
override fun View() {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Content()
    }
}

## ベストプラクティス
- Content()のみオーバーライドを推奨
- 状態管理は MutableState を使用
- Compose UIコンポーネントを活用

## 高度なパターン
- 他のFieldを内部で使う
- バリデーション機能の追加
- 複雑なUIコンポーネント
```

### 🖥️ Multi-Device Testing (新規セクション)

```markdown
# Screen Sizes

## デフォルトスクリーンサイズ
- Phone: 360dp × 640dp
- Tablet: 1024dp × 768dp
- Desktop: 1920dp × 1080dp

## カスタムスクリーンサイズ
PreviewLab(
    defaultScreenSizes = listOf(
        ScreenSize(360.dp, 800.dp, "Modern Phone"),
        ScreenSize(768.dp, 1024.dp, "iPad"),
        ScreenSize(1440.dp, 900.dp, "Laptop")
    )
) { ... }

## 単一サイズ指定
PreviewLab(
    maxWidth = 360.dp,
    maxHeight = 640.dp
) { ... }

## プリセット
- ScreenSize.SmartphoneAndDesktops (default)
- ScreenSize.AllPresets
- カスタムリスト

## ユースケース
- レスポンシブレイアウトのテスト
- 異なるアスペクト比での確認
- デバイス固有UIの検証
```

### 🎯 Events and Debugging (拡張 - `03-guides/04-events.md`)

```markdown
# Events and Debugging

## Event System

### onEvent() の使い方
@Composable
fun PreviewLabScope.MyComponentPreview() {
    Button(
        onClick = { onEvent("Button clicked") }
    ) { Text("Click me") }
}

### 詳細説明付きイベント
onEvent(
    PreviewLabEvent(
        title = "Form submitted",
        description = "Username: ${username.value}"
    )
)

## イベントログUI
- イベント一覧の表示
- タイムスタンプ
- フィルタリング

## デバッグワークフロー

### 1. インタラクションの追跡
全てのonClickやonChangeでonEvent()を呼ぶ

### 2. イベントログで確認
期待通りの順序でイベントが発火しているか

### 3. パラメータとの関係
Fieldの値を変えたときのイベント変化を観察

## ベストプラクティス
- 意味のあるイベント名を付ける
- 重要なパラメータをdescriptionに含める
- イベントを階層化しない(フラットに保つ)
```

### 🌐 Platform-Specific Features - Web/WASM (新規)

```markdown
# Web/WASM - Embedded Previews

## renderPreviewLabPreview() の使い方

### 基本的な埋め込み
@OptIn(ExperimentalJsExport::class)
@JsExport
fun renderMyComponentPreview() {
    renderPreviewLabPreview {
        MyComponentPreview()
    }
}

### HTMLから呼び出し
<div id="preview-container"></div>
<script>
  renderMyComponentPreview();
</script>

## Docusaurus統合

### iframeコンポーネント
import EmbeddedPreview from '@site/src/components/EmbeddedPreview';

<EmbeddedPreview src="/previews/button.html" />

### OpenFileHandler
外部エディタでファイルを開く機能

renderPreviewLabPreview(
    openFileHandler = { filePath, line ->
        // VSCode, IntelliJ連携
        window.open("vscode://file/$filePath:$line")
    }
) { ... }

## ユースケース
- インタラクティブドキュメント
- デザインシステムサイト
- コンポーネントカタログ
```

### 🎓 Tutorials

#### Building a UI Catalog (TODO - `04-tutorials/02-preview-ui-catalog.md`)
```markdown
# Building a UI Catalog

## ゴール
デザインシステムの全コンポーネントをPreviewLabでカタログ化

## ステップ

### 1. プロジェクト構成
src/
  commonMain/kotlin/
    components/          # 実際のコンポーネント
    previews/           # Preview専用
      ButtonPreviews.kt
      TextFieldPreviews.kt
      ...

### 2. 各コンポーネントのPreview作成
// ButtonPreviews.kt
@Preview
@Composable
fun ButtonVariantsPreview() = PreviewLab {
    val variant by fieldState {
        EnumField("Variant", ButtonVariant.Primary)
    }
    val enabled by fieldState {
        BooleanField("Enabled", true)
    }

    MyButton(
        text = fieldValue { StringField("Text", "Click me") },
        variant = variant,
        enabled = enabled,
        onClick = { onEvent("Button clicked") }
    )
}

### 3. PreviewLabGalleryのセットアップ
@Composable
fun App() {
    PreviewLabGallery {
        // 自動的に@Preview を収集して表示
    }
}

### 4. Featured Filesでグループ化
- Buttons
- Inputs
- Layout
- Typography
など

## ベストプラクティス
- 1コンポーネント = 複数Preview (variants, states, sizes)
- わかりやすいPreview名
- すべてのPropsをFieldで制御可能にする
```

---

## 既存ドキュメントのマッピング

| 既存ファイル | 推奨配置 | 状態 | アクション |
|:---|:---|:---|:---|
| `01-get-started.md` | Getting Started > Quick Start | ✅ 完成 | そのまま使用 |
| `02-install.md` | Getting Started > Installation | ❌ TODO | 新規作成 |
| `03-guides/02-basic-architecture.md` | Core Concepts > Architecture Overview | ⚠️ WIP | 内容を充実させる |
| `03-guides/03-fields/index.md` | Core Concepts > Field System Overview | ✅ 完成 | そのまま使用 |
| `03-guides/03-fields/02-all-fields.md` | Fields > Basic Fields > All Built-in Fields | ❌ TODO | 新規作成 (P0) |
| `03-guides/03-fields/03-enhance-fields.md` | Fields > Enhanced Fields | ❌ TODO | 新規作成 (P0) |
| `03-guides/03-fields/04-custom-fields.md` | Fields > Custom Fields | ❌ TODO | 新規作成 (P1) |
| `03-guides/04-events.md` | Events and Debugging > Event System | ⚠️ 基本のみ | 拡張が必要 |
| `03-guides/05-featured-files.md` | Preview Gallery > Organizing Previews | ✅ 完成 | そのまま使用 |
| `04-tutorials/02-preview-ui-catalog.md` | Tutorials > Building a UI Catalog | ❌ TODO | 新規作成 (P2) |
| `04-tutorials/03-improve-ui-review.md` | Tutorials > Improving UI Review | ❌ TODO | 新規作成 (P2) |
| `04-tutorials/04-embedded-playground.md` | Tutorials > Embedded Playground | ❌ TODO | 新規作成 (P2) |

---

## 新規作成が必要なドキュメント (優先度順)

### P0 (最優先)

1. **Getting Started > Introduction**
   - ライブラリの価値提案
   - 他のツールとの違い

2. **Getting Started > Installation**
   - Gradle設定の詳細
   - KSPプラグイン
   - プラットフォーム別設定

3. **Fields > Basic Fields > All Built-in Fields**
   - 15種類以上のField型を網羅
   - 各Fieldの詳細な説明と例

4. **Fields > Enhanced Fields**
   - nullable(), withHint(), combined()
   - 実用的なパターン

### P1 (中優先)

5. **Fields > Custom Fields**
   - PreviewLabField実装ガイド
   - カスタムUI作成

6. **Multi-Device Testing > Screen Sizes**
   - スクリーンサイズの使い方
   - レスポンシブテスト

7. **Preview Gallery > PreviewLabGallery Basics**
   - ギャラリーのセットアップ
   - ナビゲーション

8. **Events and Debugging** (拡張)
   - デバッグワークフローの追加
   - イベント駆動テスト

### P2 (低優先)

9. **Advanced Topics > Modifier Field Deep Dive**
   - ModifierFieldValueの詳細

10. **Advanced Topics > State Management**
    - PreviewLabStateの内部実装

11. **Platform-Specific > Web/WASM**
    - renderPreviewLabPreview()
    - iframe埋め込み

12. **Tutorials > 各チュートリアル**
    - UI Catalog
    - UI Review Workflow
    - Embedded Playground

13. **API Reference**
    - KDocから自動生成を検討

---

## 実装の進め方

### Phase 1: 基礎固め (P0)
1. Introduction作成
2. Installation作成
3. All Built-in Fields作成 (最重要)
4. Enhanced Fields作成

→ この段階でユーザーは基本的な使い方を習得できる

### Phase 2: 実用化 (P1)
5. Custom Fields作成
6. Architecture Overviewを充実
7. Screen Sizes作成
8. PreviewLabGallery Basics作成
9. Events and Debuggingを拡張

→ 実践的なプロジェクトで活用できる

### Phase 3: 応用・最適化 (P2)
10. Advanced Topicsセクション作成
11. Platform-Specific Features作成
12. Tutorialsセクション作成
13. API Reference作成

→ 大規模プロジェクトや特殊ケースに対応

---

## サイドバー実装 (docusaurus.config.ts)

```typescript
const sidebars = {
  docs: [
    {
      type: 'category',
      label: '🚀 Getting Started',
      items: [
        'intro',
        'install',
        'get-started',
      ],
    },
    {
      type: 'category',
      label: '📖 Core Concepts',
      items: [
        'guides/architecture',
        'guides/preview-basics',
        'guides/fields/overview',
      ],
    },
    {
      type: 'category',
      label: '🎛️ Fields',
      items: [
        {
          type: 'category',
          label: 'Basic Fields',
          items: [
            'guides/fields/all-fields',
            'guides/fields/input-types',
            'guides/fields/options',
          ],
        },
        {
          type: 'category',
          label: 'Enhanced Fields',
          items: [
            'guides/fields/nullable',
            'guides/fields/hints',
            'guides/fields/combined',
            'guides/fields/transform',
          ],
        },
        {
          type: 'category',
          label: 'Custom Fields',
          items: [
            'guides/fields/custom-creating',
            'guides/fields/custom-ui',
            'guides/fields/custom-advanced',
          ],
        },
      ],
    },
    {
      type: 'category',
      label: '🖥️ Multi-Device Testing',
      items: [
        'guides/screen-sizes',
        'guides/device-frames',
        'guides/zoom-pan',
      ],
    },
    {
      type: 'category',
      label: '🎯 Events and Debugging',
      items: [
        'guides/events/system',
        'guides/events/debugging',
        'guides/events/toast',
      ],
    },
    {
      type: 'category',
      label: '📂 Preview Gallery',
      items: [
        'guides/gallery/basics',
        'guides/gallery/organizing',
        'guides/gallery/customization',
      ],
    },
    {
      type: 'category',
      label: '🔧 Advanced Topics',
      items: [
        'guides/advanced/modifier-field',
        'guides/advanced/state-management',
        'guides/advanced/theme-integration',
        'guides/advanced/performance',
      ],
    },
    {
      type: 'category',
      label: '🌐 Platform-Specific',
      items: [
        'guides/platform/web',
        'guides/platform/android',
        'guides/platform/ios',
        'guides/platform/desktop',
      ],
    },
    {
      type: 'category',
      label: '🎓 Tutorials',
      items: [
        'tutorials/ui-catalog',
        'tutorials/ui-review',
        'tutorials/embedded-playground',
        'tutorials/complex-components',
      ],
    },
    {
      type: 'category',
      label: '📚 API Reference',
      items: [
        'api/core',
        'api/fields',
        'api/events',
        'api/utils',
      ],
    },
  ],
};
```

---

## 次のステップ

1. このTODOリストをチームでレビュー
2. Phase 1 (P0)から着手
3. 各ドキュメントのテンプレートを作成
4. KDocから自動生成できる部分を検討
5. スクリーンショット/GIFの準備
6. 実際のコード例の動作確認
7. 継続的に既存コードとドキュメントの同期を維持

---

## メモ: ドキュメント作成のベストプラクティス

- **Show, don't tell**: コード例を豊富に
- **Progressive disclosure**: 簡単なものから複雑なものへ
- **Runnable examples**: できる限り実行可能なコード
- **Screenshots/GIFs**: 視覚的な説明を追加
- **Cross-references**: 関連ドキュメントへのリンク
- **Version notes**: 機能追加時のバージョン情報
- **Common pitfalls**: よくある間違いと解決策
