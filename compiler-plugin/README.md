# Compiler Plugin

Compose Preview Lab の `@Preview` 関数収集を行う Kotlin Compiler Plugin。

## Overview

このプラグインは以下を行う:

1. **FIR Phase**: `@Preview` が付いた `private` 関数を `internal` に変更（収集先から参照可能にする）
2. **IR Phase**: モジュール内の `@Preview` 関数を収集し、`collectModulePreviews()` / `collectAllModulePreviews()` の呼び出しを実際の Preview リストに置き換える

## Source Structure

```
src/main/
├── kotlin/                ... 共通コード (全 Kotlin バージョン)
│   ├── compiler/
│   │   ├── ComposePreviewLabCommandLineProcessor.kt   ... CLI オプション定義
│   │   ├── PluginConfig.kt                            ... オプション → Config 変換
│   │   ├── fir/
│   │   │   ├── PreviewLabFirExtensionRegistrar.kt     ... FIR extension 登録
│   │   │   └── PreviewLabFirStatusTransformerExtension.kt ... private→internal 変換
│   │   └── ir/
│   │       ├── PreviewLabIrGenerationExtension.kt     ... @Preview 収集 + IR 変換
│   │       ├── PreviewFunctionInfo.kt                 ... 収集した Preview のメタデータ
│   │       └── ExtractedSourceText.kt                 ... ソースコード・KDoc 抽出
│   └── resources/META-INF/services/                   ... SPI 登録
├── kotlin-pre-2.3/        ... Kotlin 2.2.x 向け
│   └── compiler/
│       ├── ComposePreviewLabCompilerPluginRegistrar.kt ... supportsK2 のみ
│       └── compat/CompilerCompat.kt                   ... FirSimpleFunction, annotations: List<IrConstructorCall>
└── kotlin-2.3/            ... Kotlin 2.3+ 向け
    └── compiler/
        ├── ComposePreviewLabCompilerPluginRegistrar.kt ... pluginId + supportsK2
        └── compat/CompilerCompat.kt                   ... FirFunction, annotations: List<IrAnnotation>
```

## How It Works

### 1. Plugin Registration

```
Gradle Plugin (ComposePreviewLabSubplugin)
  ↓ SubpluginOption
ComposePreviewLabCommandLineProcessor
  ↓ CompilerConfiguration
ComposePreviewLabCompilerPluginRegistrar
  ├── FirExtensionRegistrarAdapter.registerExtension(...)
  └── IrGenerationExtension.registerExtension(...)
```

### 2. FIR Phase: Visibility Transform

`PreviewLabFirStatusTransformerExtension` が `@Preview` 付き `private` 関数を `internal` に変更。
IR で生成する `@Composable` ラムダがこれらの関数を呼び出せるようにする。

### 3. IR Phase: Preview Collection & Injection

`PreviewLabIrGenerationExtension.generate()` が以下を行う:

**Step 1: @Preview 関数の収集**

`moduleFragment.files` を走査し、`@Preview` (CMP / Android) 付き関数を `PreviewFunctionInfo` に変換。
`@ComposePreviewLabOption(ignore=true)` の関数はスキップ。

**Step 2: `collectModulePreviews()` / `collectAllModulePreviews()` の検出**

`PreviewLabIrBodyFiller` が IR tree を transform し、
backing field の初期化式が `collectModulePreviews()` or `collectAllModulePreviews()` であるプロパティを検出。

**Step 3: Lazy 初期化値の差し替え**

```kotlin
// Before (user code)
val myPreviews by collectModulePreviews()

// After (IR transform)
val myPreviews by lazy {
    listOf(
        CollectedPreview(id = "pkg.MyPreview", ...) { MyPreview() },
        CollectedPreview(id = "pkg.OtherPreview", ...) { OtherPreview() },
    )
}
```

`collectAllModulePreviews()` の場合は、自モジュールの Preview + 依存モジュールの Preview を結合:

```kotlin
// After (IR transform for collectAllModulePreviews)
val allPreviews by lazy {
    mutableListOf<CollectedPreview>().apply {
        addAll(listOf(/* this module's previews */))
        addAll(uiLib.uiLibPreviews)  // dependency module
    }
}
```

### CollectedPreview に含まれるメタデータ

| Field             | Source                                                        |
|-------------------|---------------------------------------------------------------|
| `id`              | qualified name or `@ComposePreviewLabOption(id=...)`          |
| `displayName`     | qualified name or `@ComposePreviewLabOption(displayName=...)` |
| `filePath`        | `IrFileEntry.name` → `projectRootPath` 相対パス              |
| `startLineNumber` | `IrFileEntry.getLineNumber(func.startOffset) + 1`            |
| `endLineNumber`   | `IrFileEntry.getLineNumber(func.endOffset) + 1`              |
| `code`            | `extractSourceText()` でソーステキスト抽出                    |
| `kdoc`            | `extractSourceText()` で KDoc 抽出                            |
| `content`         | `@Composable` ラムダ `{ PreviewFunction() }`                  |

### Plugin Ordering Requirement

content ラムダに `@Composable` アノテーションを付与し、
Compose Compiler の `ComposableLambdaLowering` で変換されるようにする。
そのため **このプラグインは Compose Compiler Plugin より先に適用する** 必要がある。

```kotlin
// build.gradle.kts
plugins {
    id("me.tbsten.compose.preview.lab")  // ← BEFORE composeCompiler
    alias(libs.plugins.composeCompiler)
}
```

Gradle Plugin が順序の誤りを検出してエラーログを出力する。

## Cross-Module Aggregation

`collectAllModulePreviews()` による依存モジュールの Preview 集約:

```
┌─────────┐     collectPreviewsExport = "uiLib.uiLibPreviews"
│  uiLib   │────────────────────────────────────────────────────┐
│ @Preview │                                                    │
│ functions│                                                    ▼
└─────────┘                                          Gradle Plugin
                                                     (SubPlugin)
┌─────────┐     dependencyCollectPreviewsFqns                   │
│   app    │◄───── "uiLib.uiLibPreviews" ──────────────────────┘
│ @Preview │          (compiler option)
│ functions│
└─────────┘
     │
     ▼ IR Phase
  app's previews + pluginContext.referenceProperties("uiLib.uiLibPreviews")
     │
     ▼
  val allPreviews = [app previews] + [uiLib previews]
```

### 制限事項

- K2 FIR の `predicateBasedProvider` は依存 KLIB 内のアノテーションをスキャンできないため、
  FIR ベースの自動 discovery は不可。Gradle Plugin による明示的な `collectPreviewsExport` 設定が必要。
- Kotlin 2.3.20+ で hint 方式 (Koin/Anvil 方式) への移行を検討可能。

## Kotlin Version Compatibility

Kotlin Compiler API のバージョン間差分を吸収するため、バージョン別ソースディレクトリを使用。

| Directory        | Kotlin  | API 差分                                              |
|------------------|---------|-------------------------------------------------------|
| `kotlin-pre-2.3` | 2.2.x   | `FirSimpleFunction`, `pluginId` なし, `List<IrConstructorCall>` |
| `kotlin-2.3`     | 2.3+    | `FirFunction`, `pluginId` 必須, `List<IrAnnotation>`  |

`build.gradle.kts` が `libs.versions.toml` の Kotlin バージョンに応じてどちらかを選択。
使わない方は `idea.module.excludeDirs` で IDE から除外される。

### ローカルで kotlin-2.3/ を検証するには

```bash
sed -i '' 's/^kotlin = .*/kotlin = "2.3.20"/' gradle/libs.versions.toml
sed -i '' 's/^compose = .*/compose = "1.11.0-beta01"/' gradle/libs.versions.toml
sed -i '' '/alias(libs.plugins.kotlinJvm)/d' buildLogic/build.gradle.kts
./gradlew :compiler-plugin:compileKotlin --no-configuration-cache
git checkout -- gradle/libs.versions.toml buildLogic/build.gradle.kts
```

CI では `test-kotlin-compat` ジョブが Kotlin 2.3.20 / 2.4.0-Beta1 でのコンパイルを自動検証する。

## Compiler Options

| Option                         | Description                                    | Required |
|--------------------------------|------------------------------------------------|----------|
| `previewsListPackage`          | レガシー PreviewList の生成パッケージ           | Yes      |
| `projectRootPath`              | filePath 算出用のプロジェクトルート             | No       |
| `dependencyCollectPreviewsFqns`| 依存モジュールのプロパティ FQN (カンマ区切り)   | No       |

## Testing

```bash
# compiler-plugin 単体テスト (kotlin-compile-testing 使用)
./gradlew :compiler-plugin:test

# integrationTest (publishToMavenLocal が必要)
./gradlew publishToMavenLocal
(cd integrationTest && ./gradlew :app:jvmTest)
```

### テスト構成

```
src/test/kotlin/
├── CollectedPreview.kt                    ... テスト用スタブ (() -> Unit で @Composable 依存回避)
├── compiler/
│   ├── CompilerPluginTestBase.kt          ... 共通ビルドインフラ (スタブ・オプション)
│   ├── contract/                          ... Compiler API コントラクトテスト
│   │   ├── CompilerPluginRegistrationContractTest.kt
│   │   ├── FirDeclarationGenerationExtensionContractTest.kt
│   │   ├── FirStatusTransformerExtensionContractTest.kt
│   │   └── IrGenerationExtensionContractTest.kt
│   └── feature/                           ... 機能テスト
│       ├── CollectPreviewsTest.kt         ... collectModulePreviews() 検出・注入
│       ├── PreviewDiscoveryTest.kt        ... @Preview 関数の収集
│       ├── PreviewListGenerationTest.kt   ... id, displayName, filePath 等
│       ├── AggregationTest.kt             ... PreviewAllList 集約
│       ├── VisibilityTransformTest.kt     ... private → internal 変換
│       └── SourceTextExtractionTest.kt    ... ソースコード・KDoc 抽出
```

- **contract/**: Kotlin Compiler API のコントラクトテスト。Kotlin バージョン更新時にどの API が壊れたか特定するために使う。
- **feature/**: 機能テスト。ユーザーから見た振る舞いを検証。
