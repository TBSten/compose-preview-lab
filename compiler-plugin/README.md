# Compiler Plugin

Compose Preview Lab の `@Preview` 関数収集を行う Kotlin Compiler Plugin。

## Overview

このプラグインは以下を行う:

1. **FIR Phase**: `@Preview` が付いた `private` 関数を `internal` に昇格 (collection-side から参照可能にする) し、 cross-module discovery 用の `previewHint_<scope>(value: PreviewHintMarker_<sanitized_fqn>_<hash>?): CollectedPreview` stub + marker interface を synthesize する。
2. **IR Phase**: モジュール内の `@Preview` 関数を収集し、 `collectModulePreviews()` / `collectAllModulePreviews()` の呼び出しを `PreviewExport(lazy { lazyPreviewSequence(...) })` に置き換える。 cross-module の dependency hints (= 上記 FIR で synthesized された `previewHint_<scope>` 関数群) を `referenceFunctions` で発見して連結する。

## Source Structure

`compiler-plugin/src/{main,test}/kotlin/me/tbsten/compose/preview/lab/compiler/` 配下は **feature × logic** の 3 層構造で再編されている (ticket-1-restructure)。

```
compiler-plugin/
├── src/main/kotlin/me/tbsten/.../compiler/
│   ├── ComposePreviewLabCompilerPluginRegistrar.kt   # 最上位エントリ点 (Kotlin compiler plugin API)
│   ├── ComposePreviewLabCommandLineProcessor.kt      # CLI オプション → Config
│   ├── PluginConfig.kt                                # Config データクラス
│   ├── PreviewLabConstants.kt                         # FQN / ClassId / CallableId / Name / Regex の SSoT
│   │
│   ├── error/                                         # 構造化 Error interface (ticket-0)
│   │   ├── ComposePreviewLabCompilerPluginError.kt
│   │   ├── ErrorContextDsl.kt
│   │   ├── Errors.kt                                  # 各 Error 実装
│   │   ├── Replies.kt
│   │   └── ReportError.kt
│   │
│   ├── warning/                                       # 構造化 Warning interface (ticket-0、 ticket-4 で拡充予定)
│   │   ├── ComposePreviewLabCompilerPluginWarning.kt
│   │   ├── WarningContextDsl.kt
│   │   ├── Warnings.kt
│   │   └── ReportWarning.kt
│   │
│   ├── utils/                                         # preview 固有知識を持たない汎用 helper (.claude/rules/compiler-plugin-utils.md)
│   │   ├── CompilerIds.kt                             # classIdOf / callableIdOf
│   │   ├── fir/AnnotationBuilders.kt                  # ClassId.buildSimpleAnnotation
│   │   └── ir/
│   │       ├── CompilerMessageLocation.kt             # IrDeclaration.compilerMessageLocation()
│   │       └── PlatformUtil.kt                        # TargetPlatform.requiresKlibIcSafetyForCrossModuleHint
│   │
│   ├── feature/
│   │   ├── previewCollection/                         # feature: @Preview 収集
│   │   │   ├── HintCanonicalKey.kt                    # computeHintHash / buildPreviewHintCanonicalKey
│   │   │   ├── HintFunName.kt                         # hintFunctionCallableId / isHintFunctionName
│   │   │   ├── MarkerInterfaceName.kt                 # buildMarkerShortName / extractHashFromMarkerShortName
│   │   │   ├── ParameterTypeFqns.kt                   # FIR / IR 共通の format spec
│   │   │   ├── PreviewAnnotationPredicates.kt         # @Preview / @ComposePreviewLabOption 用 FIR LookupPredicate
│   │   │   ├── PreviewFunctionInfo.kt                 # 収集メタデータ (data class)
│   │   │   ├── PreviewKeys.kt                         # GeneratedDeclarationKey (PreviewLabHint / PreviewLabHintMarkerInterface)
│   │   │   ├── PreviewLabFirBuiltIns.kt               # FirExtensionSessionComponent (Config wrapper)
│   │   │   ├── HintEntriesProvider.kt                 # FirExtensionSessionComponent (session-scoped lazy `hintEntries`)
│   │   │   │
│   │   │   ├── fir/
│   │   │   │   ├── AttachInternalApi.kt               # @InternalComposePreviewLabApi / @SyntheticPreviewHint injection (hint/marker 双方から利用)
│   │   │   │   ├── hintAndMarkerGeneration/           # logic: hint 関数 + marker interface 生成 (1 logic 統合形 — 後段で 2 logic 分離予定)
│   │   │   │   │   └── PreviewHintFirGenerator.kt
│   │   │   │   ├── hintGeneration/
│   │   │   │   │   └── DeprecationHidden.kt           # `@Deprecated(level = HIDDEN)` injection
│   │   │   │   └── scopeValidation/                   # logic: @ComposePreviewLabOption(collectScopes) と collect[All]ModulePreviews(scope) の literal 検証
│   │   │   │       ├── CheckCollectScopeAnnotation.kt
│   │   │   │       ├── CheckCollectScopeCall.kt
│   │   │   │       ├── CollectScopeErrors.kt          # KtDiagnosticFactory*
│   │   │   │       └── PreviewLabFirCheckersExtension.kt
│   │   │   │
│   │   │   └── ir/
│   │   │       └── collectPreviewsReplacement/        # logic: collect[All]ModulePreviews 呼び出しの IR 置換
│   │   │           ├── ReplaceCollectPreviewsFunBody.kt   # IrElementTransformerVoid (`visitProperty` → 置換)
│   │   │           ├── DiscoverHints.kt                   # cross-module hint 発見 (referenceFunctions)
│   │   │           ├── FillPreviewHintIrBody.kt           # previewHint_<scope> stub body 埋め込み
│   │   │           ├── BuildPreviewByHashMap.kt           # hash → PreviewFunctionInfo マップ構築
│   │   │           ├── HashMapWithCollisionDetection.kt   # hint hash 衝突検出
│   │   │           └── buildPreviewSequence/              # sub-logic: lazyPreviewSequence + PreviewExport IR 構築
│   │   │               ├── BuildPreviewSequenceIr.kt      # orchestrator + 共有 PreviewSequenceBuildContext
│   │   │               ├── BuildLazyWrapperIr.kt          # lazy { ... }
│   │   │               ├── BuildPreviewExportIr.kt        # PreviewExport(...)
│   │   │               ├── BuildConcatenatedPreviewSequencesIr.kt   # cross-module 連結 + distinctPreviewsByIdSequence
│   │   │               ├── BuildCollectedPreviewIr.kt     # CollectedPreview(...) ctor call
│   │   │               └── ExtractedSourceText.kt         # source / KDoc 抽出
│   │   │
│   │   └── transformPrivatePreviewToInternal/
│   │       └── fir/
│   │           └── visibilityPromotion/
│   │               └── PreviewLabFirStatusTransformerExtension.kt  # private → internal 昇格
│   │
│   └── registry/                                       # FIR / IR extension registrar
│       ├── PreviewLabFirExtensionRegistrar.kt
│       └── PreviewLabIrGenerationExtension.kt
│
├── src/main/resources/META-INF/services/              # CompilerPluginRegistrar / CommandLineProcessor 登録
└── compat/                                            # 共有 SPI (`:compiler-plugin:compat`)

compiler-plugin-compat-k210/         # Kotlin 2.1.20+ 実装
compiler-plugin-compat-k222/         # Kotlin 2.2.x 実装
compiler-plugin-compat-k2220/        # Kotlin 2.2.20+ の差分
compiler-plugin-compat-k230/         # Kotlin 2.3.0–2.3.19 実装
compiler-plugin-compat-k2320/        # Kotlin 2.3.20+ (FIR per-declaration hint gen)
compiler-plugin-compat-k2321/        # Kotlin 2.3.21+ (KLIB IC safety, KT-82395 fix)
compiler-plugin-compat-k240_beta2/   # Kotlin 2.4+ 実装
```

### feature / logic の用語

- **feature** — ユーザ目線の機能単位 (`previewCollection`, `transformPrivatePreviewToInternal`)
- **logic** — feature 内の plugin 動作単位 (`hintAndMarkerGeneration`, `scopeValidation`, `collectPreviewsReplacement` ...)。 ディレクトリ名は **動詞句** (`<verb><Object>`) で、 配下の代表クラス名は **`動詞Xxx { operator fun invoke(...) }`** 形式に揃える (Kotlin compiler API 継承クラス除く)。
- **sub-logic** — 更に細かい動作単位 (`buildPreviewSequence`)。

詳細な class 命名規則と SSoT 維持規約は [`.local/compiler-plugin-restructure/`](../.local/compiler-plugin-restructure/) (本リファクタの設計ドキュメント) を参照。 永続的な詳細設計は ticket-3 で `compiler-plugin/docs/<feature-name>/` 配下に整備予定。

## compat レイヤ

`:compiler-plugin:compat` が共有 SPI (`CompatContext`) を提供し、 各 `compiler-plugin-compat-k***` モジュールは独自の `kotlin-compiler-embeddable:X.Y.Z` を `compileOnly` でピンしてバージョン固有のバイトコードを生成する。 最終的な `compiler-plugin` JAR は ShadowJar で全 compat module をバンドルし、 ServiceLoader が実行時の Kotlin バージョンに合致する `CompatContext.Factory` を選択する。
詳細は [`docs/support-kotlin-versions.md`](../docs/support-kotlin-versions.md) を参照。

## How It Works

### 1. Plugin Registration

```
Gradle Plugin (ComposePreviewLabSubplugin)
  ↓ SubpluginOption
ComposePreviewLabCommandLineProcessor   (CLI option → PluginConfig)
  ↓ CompilerConfiguration
ComposePreviewLabCompilerPluginRegistrar
  ├── FirExtensionRegistrarAdapter.registerExtension(PreviewLabFirExtensionRegistrar(config))
  └── IrGenerationExtension.registerExtension(PreviewLabIrGenerationExtension(config, messageCollector))
```

`PreviewLabFirExtensionRegistrar` (under `registry/`) は以下を順に登録する:

1. `PreviewLabFirBuiltIns` — session-bound `PluginConfig` wrapper
2. `HintEntriesProvider` — session-scoped lazy `hintEntries` cache (hint/marker 双方が参照する SSoT)
3. `PreviewLabFirStatusTransformerExtension` — private → internal 昇格
4. `PreviewLabFirCheckersExtension` (= `scopeValidation/`) — `compat.supportsFirCheckers()` (Kotlin 2.3.20+) 時のみ登録
5. `PreviewHintFirGenerator` (= `hintAndMarkerGeneration/`) — `compat.supportsFirHintGeneration()` (Kotlin 2.3.20+) かつ `config.collectPreviewsEnabled` 時のみ登録

### 2. FIR Phase

- **`scopeValidation/`** が `CHECKERS` phase で `@ComposePreviewLabOption(collectScopes)` と `collect[All]ModulePreviews(scope)` を regex `[A-Za-z0-9_]+` で検証 (構造化 `KtDiagnosticFactory`)。
- **`transformPrivatePreviewToInternal/.../visibilityPromotion/`** が `@Preview private fun` の visibility を `internal` に書き換える。
- **`hintAndMarkerGeneration/PreviewHintFirGenerator`** が `session.hintEntriesProvider.hintEntries` を input として、 各 `@Preview` ごとに:
    - `interface PreviewHintMarker_<sanitized_fqn>_<hash>` (marker class、 IdSignature 唯一化)
    - `fun previewHint_<scope>(value: PreviewHintMarker_<...>?): CollectedPreview` stub (per-scope)

  を `me.tbsten.compose.preview.lab.hints` パッケージに synthesize する。 stub の body は IR phase で埋め込まれる。

### 3. IR Phase

- **`collectPreviewsReplacement/ReplaceCollectPreviewsFunBody`** が `val x by collect[All]ModulePreviews()` プロパティの delegate field initializer を `PreviewExport(lazy { lazyPreviewSequence(...) })` に置換 (`buildPreviewSequence/` sub-logic を呼ぶ)。
- **`collectPreviewsReplacement/DiscoverHints`** が `collectAllModulePreviews()` 時に cross-module の `previewHint_<scope>` 関数を `referenceFunctions` で発見。
- **`collectPreviewsReplacement/FillPreviewHintIrBody`** が FIR が emit した `previewHint_<scope>` stub に body (= 対応する `CollectedPreview(...)` constructor call) を埋め込む。 hash → preview map は `BuildPreviewByHashMap` が `HashMapWithCollisionDetection` 経由で構築。
- **`buildPreviewSequence/`** sub-logic は `lazyPreviewSequence({factoryLambda}, ...)` の構築・`lazy { ... }` ラッピング・`PreviewExport(...)` ctor 呼び出し・cross-module 連結 (`distinctPreviewsByIdSequence`) を 4 つの builder + 1 つの `BuildCollectedPreviewIr` で構成する。

エラーは全て `error/` 配下の構造化 `Error` 実装経由で `MessageCollector.report(error, location)` extension に流れる (詳細は `.claude/rules/compiler-plugin-error.md`)。
