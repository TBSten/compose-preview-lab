# Feature: previewCollection

`@Preview` 関数の **収集**、 `collect[All]ModulePreviews()` の置換、 cross-module hint discovery を担う feature。

このディレクトリは feature の **設計ドキュメント entrypoint** で、 各 logic の詳細トピックは別ファイルに分かれている。
ディレクトリ構造そのものの overview は [`compiler-plugin/README.md`](../../README.md) のみが持つ (SSoT)。
本 README は feature 内の **構成 logic 一覧** と **各詳細トピックへの導線** に専念する。

---

## Feature Overview

ユーザ側コード:

```kotlin
@Preview fun MyButton() { ... }
val myPreviews by collectModulePreviews()        // module 内のみ
val allPreviews by collectAllModulePreviews()    // module + dependency modules
```

これを「 plugin が `@Preview` を収集して `myPreviews` / `allPreviews` に注入する」 ように見せるための処理を、
FIR phase + IR phase の 2 段階で実現する。

```
FIR phase
├── scopeValidation/              — collectScopes / scope 引数の literal / regex 検証 (IDE red-squiggly)
└── hintAndMarkerGeneration/      — 各 @Preview に対して marker interface + previewHint_<scope> stub を合成

IR phase
└── collectPreviewsReplacement/   — collect[All]ModulePreviews() を置換 + dependency hint 発見 + hint body 埋め込み
    └── buildPreviewSequence/     — lazyPreviewSequence + PreviewExport IR を構築 (sub-logic)
```

---

## 構成 logic 一覧

### FIR 側

- **hintAndMarkerGeneration** — `@Preview` ごとに marker interface (`PreviewHintMarker_<sanitized_fqn>_<hash>`) と
  hint 関数 (`previewHint_<scope>`) を synthesize。 詳細: [hint-generation.md](./hint-generation.md) /
  [marker-generation.md](./marker-generation.md)
  - 設計初版では 2 logic 分離 (`hintGeneration/` + `markerGeneration/`) だったが、 PR #200 で 1 logic 統合形に。
    維持 vs 再分離の判断基準は [marker-generation.md](./marker-generation.md) 参照
  - 配置: [`fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt)
  - helper: [`fir/hintGeneration/DeprecationHidden.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/hintGeneration/DeprecationHidden.kt) (hint/marker 双方で利用)、
    [`fir/AttachInternalApi.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/AttachInternalApi.kt) (hint/marker 双方で利用)
- **scopeValidation** — `@ComposePreviewLabOption(collectScopes = [...])` と
  `collect[All]ModulePreviews(scope = ...)` の文字列引数を `[A-Za-z0-9_]+` で検証。 詳細: [scope-validation.md](./scope-validation.md)
  - 配置: [`fir/scopeValidation/`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/scopeValidation/)

### IR 側

- **collectPreviewsReplacement** — `collect[All]ModulePreviews()` 呼び出しの IR 置換、 dependency module の hint 関数
  発見、 hint stub body 埋め込み、 hash → preview map 構築。 詳細: [collect-previews-replacement.md](./collect-previews-replacement.md)
  - 配置: [`ir/collectPreviewsReplacement/`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/)
  - sub-logic **buildPreviewSequence** — `lazyPreviewSequence({factory}, ...)` / `lazy { ... }` / `PreviewExport(...)` /
    cross-module 連結 IR 構築 (PR #196 系 Sequence refactor 反映)。 詳細は
    [collect-previews-replacement.md](./collect-previews-replacement.md) の「sub-logic」セクション

---

## 横断トピック

- **[hint-naming.md](./hint-naming.md)** — `HintCanonicalKey` + `HintFunName` + `MarkerInterfaceName` の関係 SSoT。
  どの class がどの命名関数を参照するか、 truncated hash 長さの設計判断、 sanitized FQN の情報損失設計など。
  hint / marker / discovery の 3 sites の一致を保証するための **single source of truth** として、 他 logic doc から参照される
- **[error-flow.md](./error-flow.md)** — FIR diagnostic (`KtDiagnosticFactory*`) と IR 構造化 Error
  (`ComposePreviewLabCompilerPluginError`) の役割分担、 二重防衛が必要な理由、 各 Error 発火条件一覧

---

## 読み順 (推奨)

1. **[hint-naming.md](./hint-naming.md)** — 命名規則を頭に入れる。 全 logic doc がこの規則を前提に書かれている
2. **[error-flow.md](./error-flow.md)** — 各 Error がどの phase / 役割で発火するかの全体地図
3. **[hint-generation.md](./hint-generation.md)** + **[marker-generation.md](./marker-generation.md)** — FIR 生成側
4. **[scope-validation.md](./scope-validation.md)** — FIR Checker 側 (= IDE 上のリアクション)
5. **[collect-previews-replacement.md](./collect-previews-replacement.md)** — IR 側 (= 実際の置換と body 埋め込み)
