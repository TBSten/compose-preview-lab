# Feature: previewCollection

> [English version](./README.md)

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

## 処理の流れ

各 logic は **特定の module 役割** において **特定の compiler phase** で動作する。 同一の compiler plugin
コードが依存先全 module に load されるが、 「Preview を定義する module」 と 「`collect[All]ModulePreviews()`
を呼ぶ module」 の **役割** によって実際に出力を出す logic が決まる。 1 つの module が `@Preview` を持ちつつ
自身で `collectModulePreviews()` を呼ぶ場合は両役割を兼ねる。

以下で使う役割定義:

- **Preview があるモジュール** (= upstream / library module): `@Preview` 関数を宣言する module。 hint と marker
  は **ここで生成** されるため、 後段の依存先 module が discovery 可能になる。
- **Collect したいモジュール** (= downstream / app module): `collectModulePreviews()` または
  `collectAllModulePreviews()` を呼ぶ module。 自身および依存先 module の hint をここで **発見** し、
  `CollectedPreview` インスタンスへ **具体化** する。

### Phase 1 — FIR (frontend) phase

**両方の役割** で実行される。 この時点では plugin は module の役割を知り得ないため、 FIR extension は全 module で
install され、 該当しない module では no-op となる。

1. **scopeValidation** — `CollectScopeAnnotationChecker` + `CollectScopeCallChecker` が
   `@ComposePreviewLabOption(collectScopes = [...])` と `collect[All]ModulePreviews(scope = ...)` の文字列
   literal を検証する。 違反は FIR diagnostic 経由で IDE 上 red-squiggly として通知。 **annotation は Preview
   があるモジュール、 call は Collect したいモジュール側でそれぞれ発火**。 詳細: [scope-validation.md](./scope-validation.md)
2. **hintAndMarkerGeneration (FIR 側)** — `PreviewHintFirGenerator` が現 module 内の全 `@Preview` を走査し、
   marker interface 1 つ + scope ごとの `previewHint_<scope>(...)` stub 関数 declaration を合成して register する。
   **Preview があるモジュールでのみ発火** (`@Preview` のない module では何も生成しない)。 詳細:
   [hint-and-marker-generation.md](./hint-and-marker-generation.md) / [marker-generation.md](./marker-generation.md)
3. **transformPrivatePreviewToInternal** — 別 feature だが (詳細:
   [`../transformPrivatePreviewToInternal/README.ja.md`](../transformPrivatePreviewToInternal/README.ja.md))、
   IR phase より前に sequencing される。 `@Preview private fun` を `internal` に昇格し、 IR phase で合成 hint body
   から参照できるようにする。 **Preview があるモジュールでのみ発火**。

### Phase 2 — IR (backend) phase

こちらも両役割で動作するが、 重い IR 書き換えは綺麗に役割分離される。

4. **hintAndMarkerGeneration (IR 側)** — `FillPreviewHintIrBody` が、 FIR phase で declaration のみ作成された
   hint stub 関数の body を埋める。 各 stub の中で実際の `@Preview` callable reference と `CollectedPreview`
   builder を呼ぶ IR を構築する。 **Preview があるモジュールでのみ発火** (stub body を埋めるべき module は
   `@Preview` を持つ module のみ)。 詳細: [hint-and-marker-generation.md](./hint-and-marker-generation.md)
5. **collectPreviewsReplacement** — **Collect したいモジュール側で**:
    - `DiscoverHints` が現 module + 全依存先 module から marker interface prefix を持つ class を走査し、
      対応する `previewHint_<scope>` 関数を収集する
    - `ReplaceCollectPreviewsFunBody` が `collect[All]ModulePreviews()` 呼び出しの sentinel body を
      `PreviewExport(lazy { lazyPreviewSequence({factory}, ...) })` で置換する
    - sub-logic `buildPreviewSequence/` が実際の lazy sequence IR を構築し、 発見された各 hint 関数を呼んで
      `CollectedPreview` を収集する。 hash-based 重複排除は `BuildPreviewByHashMap` が担当

   詳細: [collect-previews-replacement.md](./collect-previews-replacement.md)

### Module 役割サマリ

| Logic | FIR / IR | Preview があるモジュール | Collect したいモジュール |
| --- | --- | --- | --- |
| `scopeValidation` | FIR | 発火 (annotation literal) | 発火 (call-site literal) |
| `hintAndMarkerGeneration` (FIR) | FIR | 発火 | no-op |
| `transformPrivatePreviewToInternal` | FIR | 発火 | no-op |
| `hintAndMarkerGeneration` (IR) | IR | 発火 | no-op |
| `collectPreviewsReplacement` (incl. `buildPreviewSequence`) | IR | no-op | 発火 |

`@Preview` を定義しつつ自身で `collectModulePreviews()` を呼ぶ module では上記全行が動く (FIR / IR 各 phase が
同一 module 内で両役割を見るだけ)。

---

## 構成 logic 一覧

### FIR 側

- **hintAndMarkerGeneration** — `@Preview` ごとに marker interface (`PreviewHintMarker_<sanitized_fqn>_<hash>`) と
  hint 関数 (`previewHint_<scope>`) を synthesize。 詳細: [hint-and-marker-generation.md](./hint-and-marker-generation.md) /
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

- **[hint-and-marker-generation.md](./hint-and-marker-generation.md)** — Part 1 が `HintCanonicalKey` + `HintFunName` +
  `MarkerInterfaceName` の関係 SSoT。 どの class がどの命名関数を参照するか、 truncated hash 長さの設計判断、
  sanitized FQN の情報損失設計など。 hint / marker / discovery の 3 sites の一致を保証するための
  **single source of truth** として、 他 logic doc から参照される。 Part 2 はその命名を実体化する FIR hint generation logic を扱う
- **[error-flow.md](./error-flow.md)** — FIR diagnostic (`KtDiagnosticFactory*`) と IR 構造化 Error
  (`ComposePreviewLabCompilerPluginError`) の役割分担、 二重防衛が必要な理由、 各 Error 発火条件一覧

---

## 読み順 (推奨)

1. **[hint-and-marker-generation.md](./hint-and-marker-generation.md)** (Part 1 — Naming) — 命名規則を頭に入れる。 全 logic doc がこの規則を前提に書かれている
2. **[error-flow.md](./error-flow.md)** — 各 Error がどの phase / 役割で発火するかの全体地図
3. **[hint-and-marker-generation.md](./hint-and-marker-generation.md)** (Part 2 — Hint Generation) + **[marker-generation.md](./marker-generation.md)** — FIR 生成側
4. **[scope-validation.md](./scope-validation.md)** — FIR Checker 側 (= IDE 上のリアクション)
5. **[collect-previews-replacement.md](./collect-previews-replacement.md)** — IR 側 (= 実際の置換と body 埋め込み)
