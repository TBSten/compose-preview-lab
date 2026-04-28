# Compiler Plugin

Compose Preview Lab の `@Preview` 関数収集を行う Kotlin Compiler Plugin。

## Overview

このプラグインは以下を行う:

1. **FIR Phase**: `@Preview` が付いた `private` 関数を `internal` に変更（収集先から参照可能にする）
2. **IR Phase**: モジュール内の `@Preview` 関数を収集し、`collectModulePreviews()` / `collectAllModulePreviews()` の呼び出しを実際の Preview リストに置き換える

## Source Structure

```
compiler-plugin/                # main module (version-agnostic) — published as a shadow JAR
├── src/main/kotlin/
│   ├── compiler/
│   │   ├── ComposePreviewLabCommandLineProcessor.kt   ... CLI オプション定義
│   │   ├── PluginConfig.kt                            ... オプション → Config 変換
│   │   ├── compat/Compat.kt                           ... ServiceLoader 経由の CompatContext 利用ヘルパー
│   │   ├── fir/
│   │   │   ├── PreviewLabFirExtensionRegistrar.kt     ... FIR extension 登録
│   │   │   └── PreviewLabFirStatusTransformerExtension.kt ... private→internal 変換
│   │   └── ir/
│   │       ├── PreviewLabIrGenerationExtension.kt     ... @Preview 収集 + IR 変換
│   │       ├── PreviewLabIrBodyFiller.kt              ... collectModulePreviews() 置換
│   │       ├── CollectedPreviewIrBuilder.kt           ... CollectedPreview コンストラクタ呼び出し生成
│   │       ├── PreviewListIrBuilder.kt                ... listOf / lazy / 集約 IR 生成
│   │       ├── PreviewFunctionInfo.kt                 ... 収集した Preview のメタデータ
│   │       └── ExtractedSourceText.kt                 ... ソースコード・KDoc 抽出
│   └── resources/META-INF/services/                   ... CompilerPluginRegistrar / CommandLineProcessor 登録
├── compat/                                             ... 共有 SPI (CompatContext / KotlinToolingVersion / ServiceLoader)
├── compat-k210/                                        ... Kotlin 2.1.20+ 実装 (legacy IrBuilderWithScope レシーバ)
├── compat-k222/                                        ... Kotlin 2.2.x 実装 (IrBuilder レシーバ拡大を吸収)
├── compat-k2220/                                       ... Kotlin 2.2.20+ の差分
├── compat-k230/                                        ... Kotlin 2.3.x 実装 (FirFunction / IrConstructorCallImpl)
└── compat-k240_beta2/                                  ... Kotlin 2.4+ 実装 (IrAnnotationImpl)
```

各 `compat-k***` モジュールは独自の `kotlin-compiler-embeddable:X.Y.Z` を `compileOnly` でピンし、
バージョン固有のバイトコードを生成する。最終的な `compiler-plugin` JAR は ShadowJar で全 compat module を
バンドルし、ServiceLoader が実行時の Kotlin バージョンに合致する `CompatContext.Factory` を選択する。
詳細は [`docs/support-kotlin-versions.md`](../docs/support-kotlin-versions.md) を参照。

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

### Plugin Ordering

content ラムダに `@Composable` アノテーションを付与し、Compose Compiler の `ComposableLambdaLowering`
で変換されるようにする。そのため compiler plugin の IR 拡張は Compose Compiler より先に走る必要がある。

- **Kotlin 2.3.0+**: Gradle plugin が `-Xcompiler-plugin-order=...:me.tbsten.compose.preview.lab.compiler>androidx.compose.compiler.plugins.kotlin`
  を自動注入するため、`build.gradle.kts` 上の `plugins { ... }` ブロックの順序を気にする必要はない。
- **Kotlin 2.1.20 / 2.2.x**: 順序フラグが利用できないため、`me.tbsten.compose.preview.lab` プラグインを
  Compose Compiler プラグインより **前** に書く必要がある。Gradle plugin が順序の誤りを検出して
  早期に Gradle 例外を投げる。

```kotlin
// build.gradle.kts (Kotlin 2.1.20 / 2.2.x の例)
plugins {
    id("me.tbsten.compose.preview.lab")  // ← BEFORE composeCompiler
    alias(libs.plugins.composeCompiler)
}
```

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

Kotlin Compiler API のバージョン間差分は **ServiceLoader + per-version compat module** パターンで
吸収する。`compat/` が共有 SPI (`CompatContext`) を定義し、各 `compat-k***` モジュールが対応する
`kotlin-compiler-embeddable` でコンパイルされた `CompatContextImpl` を提供する。

| Compat module       | Kotlin              | 主な吸収対象                                                                            |
|---------------------|---------------------|------------------------------------------------------------------------------------------|
| `compat-k210`       | 2.1.20 / 2.1.21     | legacy `irCall` / `irGet` / `irString` の `IrBuilderWithScope` レシーバ                  |
| `compat-k222`       | 2.2.0 - 2.2.10      | `IrBuilder` レシーバ拡大に伴う API 差分                                                  |
| `compat-k2220`      | 2.2.20 - 2.2.21     | k222 への漸進的差分 (api 依存で k222 上に配置)                                            |
| `compat-k230`       | 2.3.x               | `FirFunction` / `IrConstructorCallImpl` の 4 引数版                                      |
| `compat-k240_beta2` | 2.4.0-Beta2+        | `IrAnnotationImpl` (annotation 型変更)                                                   |

最終 jar は ShadowJar が全 compat module を embed し、`META-INF/services/...CompatContext$Factory`
を merge する。実行時には `CompatContext.load()` が `META-INF/compiler.version` から検出した
Kotlin バージョンに対して `minVersion <= currentVersion` を満たす最大の factory を選ぶ。

### ローカル動作検証

`scripts/compiler-plugin-test.sh` が個別バージョンでのテスト実行をサポートする:

```bash
./scripts/compiler-plugin-test.sh 2.1.20
./scripts/compiler-plugin-test.sh 2.3.21
./scripts/compiler-plugin-test.sh 2.4.0-Beta2
```

CI matrix は `scripts/supported-kotlin-versions.txt` (SSOT) を読み、サポート全バージョンで上記
スクリプトを走らせる。新しい Kotlin バージョンを追加する手順は
[`docs/support-kotlin-versions.md`](../docs/support-kotlin-versions.md) を参照。

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
