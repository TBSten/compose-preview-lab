# Cross-module Preview Aggregation (日本語)

> [English](./cross-module-aggregation.md)

[`collectAllModulePreviews()`](../src/commonMain/kotlin/me/tbsten/compose/preview/lab/CollectModulePreviews.kt)
の cross-module discovery と、 それを支える runtime の duplicate detection 経路に関する詳細ドキュメント。
各関数の KDoc は API 契約の要約に留め、 Kotlin バージョン要件 / discovery mechanism /
duplicate detection の診断フローはこのファイルが single source of truth。

## Kotlin バージョン要件

`collectAllModulePreviews()` の cross-module 動作には target ごとに最低 Kotlin バージョンがある:

- **JVM / Android**: Kotlin **2.3.20** から (FIR per-declaration hint generator が 2.3.20 で
  stabilise、 JVM bytecode の依存解決は incremental compile の複雑性が無い)。
- **JS / Wasm JS / iOS / Native**: Kotlin **2.3.21** から (KT-82395 KLIB IC fix が 2.3.21 で landed)。

古い Kotlin では plugin の IR pass が `val x by collectAllModulePreviews()` という
delegated-property パターンを検出して、 アップグレード / ダウングレードを促す compile-time error
を出す。 property delegate 以外での直接呼び出し (サポート外の使い方) は compile が通り、
runtime で placeholder body の `IllegalStateException` に落ちる。 `collectModulePreviews()`
(single-module) は全 Kotlin バージョンで動く。

## Discovery mechanism

compiler plugin は `@Preview` ごとに hint 関数を生成する
(`previewHint_<scope>(value: PreviewHintMarker_<hash>?): CollectedPreview` を
`me.tbsten.compose.preview.lab.hints` package に)。 consumer 側は
`IrPluginContext.referenceFunctions` で発見する (JVM bytecode と KLIB のどちらでも同じ仕組み)。

`collectAllModulePreviews(scope = ...)` の引数は compiler plugin の IR pass に compile-time
string constant として届く必要がある。 inline string literal と `const val` reference の
どちらも OK (両方とも IR pass 実行前に `IrConst<String>` になる)。 非 `const` val、
string concatenation、 その他 `IrCall` / `IrStringConcatenation` を生む式は compile error。
解決値は `[A-Za-z0-9_]+` にも match する必要がある (plugin が synthetic hint 関数名に埋め込むため)。

## Mixed-classpath caveat

aggregation が依存 module の preview を見つけられるのは、 その依存 artifact 自体が
Compose Preview Lab plugin で Kotlin **2.3.20+** でコンパイルされている場合のみ
(bytecode / KLIB に synthetic `previewHint_<scope>` overload が含まれている必要がある)。
古い compiler で build された依存 / plugin 未適用の依存は marker / hint pair を emit しないため、
集約結果からは silently 抜け落ちる。

Compose Preview Lab は compile time にこれを検出できない。 既知の `@Preview` が downstream
で出てこないなら、 **その依存の Kotlin バージョンと plugin 適用をまず確認**。

## Per-call scope の置換

scope は **strictly per-module**: library が `defaultCollectScope = "acme_ui"` で
自分の preview を pin した場合、 それらは `acme_ui` に登録される。 ただし downstream consumer app の
`collectAllModulePreviews()` (= sentinel が consumer の DSL 値で置換される) は、
consumer が明示的に `collectAllModulePreviews(scope = "acme_ui")` を呼ばない限り **見えない**。
この隔離が DSL が存在する主な理由。 完全な scope-resolution semantics は
[`annotation/docs/collect-scopes.md`](../../annotation/docs/collect-scopes.md) を参照。

## Duplicate detection

`distinctPreviewsById` (eager `List` 変種) と `distinctPreviewsByIdSequence` (lazy `Sequence`
変種) は集約済み list を `CollectedPreview.id` で dedup する。 依存 module 側でも
`collectAllModulePreviews()` を使っている (= その module も自分の依存を re-export している)
場合、 同じ `CollectedPreview` が複数の hint chain を経て aggregator に届くことがある。
`id` で dedup することで、 経由 path の数に関係なく結果が安定する。

### Cross-artifact same-FQN previews — silent edge case

2 つの 依存 module が同じ FQN の `@Preview` (例: 両方とも
`com.example.SharedPreview()`) を declare すると、 FIR/IR generator が両方とも同じ
`<canonical-key>` ハッシュの marker class を生成する。 JVM classloader と KLIB linker は
duplicate を 1 つの symbol に解決するので、 **もう一方の preview の body は silently 消える**。
runtime はこれを「aggregator に届いた duplicate `CollectedPreview.id` 値の観測」 でしか検出できない。
上流での FQN collapse が片方を消すので、 これは稀。

runtime に duplicate が届くと `distinctPreviewsById` / `distinctPreviewsByIdSequence` は
id ごとに 1 件 warning を
[`warnDuplicatePreview`](../src/commonMain/kotlin/me/tbsten/compose/preview/lab/WarnDuplicatePreview.kt)
経由で emit する。 routing は platform ごと:

- **JVM / Android / iOS**: stdout (`println`) — 既存の build / test log に出続けるよう従来通り。
- **JS / Wasm JS**: `console.warn` — browser DevTools の黄色ハイライト + `console.log` を
  filter する headless test runner でも生き残る。

compiler plugin も symbol scan で duplicate hint を見つけた場合に best-effort
compile-time warning を出す (`HintDiscovery.discoverHints`、 これは `:compiler-plugin`
module 内なので Dokka link 不可) が、 上流 collapse のため warning はほとんど発火しない。
runtime signal が durable な検出パス。

### Resolution — 関数 rename

collide している artifact のうち 1 つで **underlying `@Preview` 関数を rename** する。
`@ComposePreviewLabOption(id = "...")` override は **どの platform でも効かない**: FQN-based
hint generator 名が IR 時点で collide (JVM / Android は override id を読む前)、 KLIB-based
target では linker が IdSignature でさらに dedup する。 関数 rename が唯一の portable fix。

### Web visibility caveat

`console.warn` を使っても、 DevTools を開いていない user には見えない。 JS / Wasm JS deploy で
`@Preview` が「消えた」 ように見えるなら、 **DevTools を開くのが triage 第一手** — warning は
app launch ごとに 1 回 (= `collect[All]ModulePreviews()` プロパティ初回 read 時) 発火する。

## Laziness — `PreviewExport` + `Sequence` semantics

`collect[All]ModulePreviews()` は `ReadOnlyProperty<Any?, Sequence<CollectedPreview>>` の
property delegate を返す ([`PreviewExport`](../src/commonMain/kotlin/me/tbsten/compose/preview/lab/PreviewExport.kt) が裏側)。
delegate は underlying sequence を [`Lazy`] で wrap しており、 compiler plugin が emit する
`@Preview` ごとの factory lambda は property 初回 read まで invoke されない。 sequence
iteration もまた、 `CollectedPreview` を 1 つずつ on-demand で構築する。

最初の数件しか必要としない consumer (例: 何千 `@Preview` のうち 10 hit を返す search filter)
は、 残りを materialise せずに済む。 large preview corpora で peak memory を抑える効果がある。

factory lambda indirection が laziness の本体 — `List<CollectedPreview>` を直接 build すると
全 `@Composable` lambda を up-front で allocate しなければならなくなる。
