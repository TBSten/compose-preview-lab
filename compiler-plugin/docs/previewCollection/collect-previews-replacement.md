# Logic: collectPreviews Replacement (+ sub-logic `buildPreviewSequence/`)

`val previews by collectModulePreviews()` / `val allPreviews by collectAllModulePreviews()` の
**property delegate field initializer を IR phase で書き換え**、 actual な `Sequence<CollectedPreview>`
構築 IR を埋め込む logic。

ユーザ目線では sentinel call (`collectModulePreviews()` / `collectAllModulePreviews()`) が「 module 内の全
`@Preview` を集める」 マジックな関数だが、 実体は IR transform で書き換えられる **synthetic な call site** に過ぎない。
同時に hint stub の body 埋め込み (= FIR 側 [`hintAndMarkerGeneration/`](./hint-generation.md) の続き) もこの logic 内で完結する。

---

## 入出力 (Before / After)

### Input

```kotlin
// userland module
val myPreviews by collectModulePreviews()
val allPreviews by collectAllModulePreviews()
```

### After (semantically equivalent Kotlin)

```kotlin
// same-module only
val myPreviews by PreviewExport(
    lazy {
        lazyPreviewSequence(
            { CollectedPreview("com.example.MyButton", ..., content = { com.example.MyButton() }) },
            { CollectedPreview("com.example.MyText", ..., content = { com.example.MyText() }) },
        )
    },
)

// same-module + dependency modules (cross-module aggregation)
val allPreviews by PreviewExport(
    lazy {
        distinctPreviewsByIdSequence(
            lazyPreviewSequence({ CollectedPreview("com.example.MyButton", ...) }, ...)
                + // dependency module's previewHint_default(null) returns:
                lazyPreviewSequence({ CollectedPreview("uilib.Button", ...) }, ...),
        )
    },
)
```

各 `CollectedPreview(...)` constructor call は `() -> CollectedPreview` factory lambda にラップされ、
`asSequence().take(n)` イテレーション時に **必要な要素だけ** `@Composable { ... }` を allocate する遅延構築になる。

---

## 構成: logic + sub-logic

```
collectPreviewsReplacement/
├── ReplaceCollectPreviewsFunBody.kt        # orchestrator (IrElementTransformerVoid)
├── CollectPreviewsCallFqns.kt              # COLLECT_MODULE_PREVIEWS_FQN / COLLECT_ALL_MODULE_PREVIEWS_FQN (SSoT)
├── DiscoverHints.kt                        # cross-module hint 発見 (referenceFunctions per scope)
├── FillPreviewHintIrBody.kt                # FIR が emit した previewHint_<scope> stub body 埋め込み
├── BuildPreviewByHashMap.kt                # hash → PreviewFunctionInfo マップ構築
├── HashMapWithCollisionDetection.kt        # 衝突検出付き map builder (preview-specific helper)
└── buildPreviewSequence/                   # sub-logic: IR 構築 builder 群
    ├── BuildPreviewSequenceIr.kt           # orchestrator + 共有 PreviewSequenceBuildContext
    ├── BuildLazyWrapperIr.kt               # lazy { ... }
    ├── BuildPreviewExportIr.kt             # PreviewExport(...)
    ├── BuildConcatenatedPreviewSequencesIr.kt  # cross-module 連結 + distinctPreviewsByIdSequence
    ├── BuildCollectedPreviewIr.kt          # CollectedPreview(...) ctor call
    └── ExtractedSourceText.kt              # source / KDoc 抽出 helper
```

---

## logic 詳細

### `ReplaceCollectPreviewsFunBody` — orchestrator

`IrElementTransformerVoid` を継承し、 `visitProperty` で `val x by collect[All]ModulePreviews()` 構造を検出。
発見した property の delegate field initializer を [`BuildPreviewSequenceIr`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/buildPreviewSequence/BuildPreviewSequenceIr.kt) /
[`BuildPreviewExportIr`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/buildPreviewSequence/BuildPreviewExportIr.kt) /
[`BuildLazyWrapperIr`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/buildPreviewSequence/BuildLazyWrapperIr.kt) /
[`BuildConcatenatedPreviewSequencesIr`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/buildPreviewSequence/BuildConcatenatedPreviewSequencesIr.kt) に委譲して書き換える。

ここでは以下の 4 グループの Error 発火元になる ([error-flow.md](./error-flow.md) 参照):

- `UnsupportedCollectAllError` — Kotlin <2.3.20/<2.3.21 で `collectAllModulePreviews()`
- `CollectPreviewsDisabledError` — `collectPreviewsEnabled=false` 時
- `NonLiteralScopeIrError` / `InvalidScopeIrError` — scope 引数の IR-pass backstop
- `PropertyHasNoGetterError` — defensive (`property.getter == null`)

### `DiscoverHints` — cross-module hint 発見

`collectAllModulePreviews(scope = "design")` のとき、 dependency module が emit した
`previewHint_design(value: PreviewHintMarker_<...>?): CollectedPreview` 関数群を `referenceFunctions(CallableId(HINT_PACKAGE, "previewHint_design"))`
で 1 lookup で発見する。

#### scope を関数名に embed する設計のメリット

scope 違いの hint は **関数名が違う** ため、 lookup の時点で除外される。 per-hint annotation inspection が不要。
詳細は [hint-naming.md](./hint-naming.md) 参照。

#### Platform gate

KLIB targets (JS / Wasm / Native) は KT-82395 の `referenceFunctions` IC-safety fix (= Kotlin 2.3.21+) が必須。
JVM / Android は Kotlin 2.3.20+ の FIR per-declaration hint generator (= `supportsFirHintGeneration`) だけで動く。
gate は [`utils/ir/PlatformUtil.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/utils/ir/PlatformUtil.kt) の
`TargetPlatform?.requiresKlibIcSafetyForCrossModuleHint` extension で抽象化されている。

#### Squatting guard / cross-artifact dup warning

`@SyntheticPreviewHint` annotation の有無で「 plugin が emit した hint か user 手書きか」 を区別する
(= namespace squatting guard)。 また同じ marker class が複数 artifact から見える場合
(cross-artifact duplicate detection) も警告対象。

> 2026-05 時点では `messageCollector.report(WARNING, ...)` の literal 直書きとして残っており、 Ticket 4 で
> `warning/Warnings.kt` の `ComposePreviewLabCompilerPluginWarning` 実装に migrate 予定 (詳細は
> [`.claude/rules/compiler-plugin-error.md`](../../../.claude/rules/compiler-plugin-error.md) 「段階的移行中の既知の rule 違反」)。

### `FillPreviewHintIrBody` — hint stub body 埋め込み

FIR 側 [`hintAndMarkerGeneration/PreviewHintFirGenerator`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/hintAndMarkerGeneration/PreviewHintFirGenerator.kt) が
body なしで emit した `previewHint_<scope>(value: PreviewHintMarker_<...>?)` stub に、 IR phase で
`return CollectedPreview(...)` を埋める。

#### hint と `@Preview` の対応付け

`previewHint_<scope>(value: PreviewHintMarker_<sanitized_fqn>_<hash>?)` の parameter 型 (marker class) の
**短名末尾 8 文字** = hash を [`extractHashFromMarkerShortName`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/MarkerInterfaceName.kt) で取り出し、
[`BuildPreviewByHashMap`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/BuildPreviewByHashMap.kt) が
構築した `hash → PreviewFunctionInfo` map で lookup する。 FIR / IR 双方が同じ canonical key + hash function
([`HintCanonicalKey.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/HintCanonicalKey.kt))
を使うため、 一意な対応付けが成立する。

#### Origin チェック

`IrSimpleFunction.origin === IrDeclarationOrigin.GeneratedByPlugin && origin.pluginKey === PreviewKeys.PreviewLabHint`
で「 FIR generator が emit した stub」 と限定。 visit 対象を絞ることで他の `previewHint_*` 名衝突に対する安全マージン。

### `BuildPreviewByHashMap` + `HashMapWithCollisionDetection`

`hash → PreviewFunctionInfo` map を、 衝突検出 callback 付きで構築する。

```kotlin
BuildPreviewByHashMap().invoke(previews) { hash, existing, conflicting ->
    messageCollector.report(
        HintHashCollisionError(hash, existing.signature, conflicting.signature),
        conflicting.function.compilerMessageLocation(),
    )
}
```

- canonical key (`<sourceFqn>(<paramTypes>)`) を hash 入力としている。 同 FQN の overload は別 canonical key →
  別 hash → 衝突しない
- truncated SHA-256 (8 chars base-36) は約 41bit、 1k previews の真の衝突確率 ≈ `10^-7`
- 同じ canonical key を 2 度 register するのは idempotent overwrite として扱い、 衝突扱いしない
- `HashMapWithCollisionDetection.kt` は **preview-specific helper** として `utils/` ではなく logic 内に閉じる
  (callback shape が preview の semantic に依存しているため一般化禁止。 詳細は同 file の KDoc 参照)

### `CollectPreviewsCallFqns`

- `COLLECT_MODULE_PREVIEWS_FQN = "me.tbsten.compose.preview.lab.collectModulePreviews"`
- `COLLECT_ALL_MODULE_PREVIEWS_FQN = "me.tbsten.compose.preview.lab.collectAllModulePreviews"`

FQN の SSoT。 `ReplaceCollectPreviewsFunBody` (IR sentinel call 検出) と
[`scopeValidation/CheckCollectScopeCall`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/fir/scopeValidation/CheckCollectScopeCall.kt) (FIR Checker target) が同じ FQN を参照する。

---

## sub-logic `buildPreviewSequence/` 詳細

PR #196 系の Sequence refactor 反映済。 旧 `listOf(CollectedPreview(...), ...)` 経路は撤去され、
`lazyPreviewSequence({ CollectedPreview(...) }, { CollectedPreview(...) }, ...)` の factory lambda vararg 形式に移行している。

### `BuildPreviewSequenceIr` (orchestrator)

`lazyPreviewSequence(*factories)` の **同 module 専用** sequence を構築する builder。 sibling builder と
共有する `PreviewSequenceBuildContext` を内部に持ち、 各 sub-builder にこの context を渡す。

`PreviewSequenceBuildContext` は以下を share:

- `lazyPreviewSequence` / `Sequence<CollectedPreview>` / factory lambda 型 (`() -> CollectedPreview`) の **lazy 解決**
  (1 transformer 1 module あたり 1 回)
- `factoryLambdaCounter` — sibling 匿名 factory lambda の JVM lowering 名衝突 (`<containing>$N` mangling) を回避する
  ためのカウンタ。 各 factory に `previewFactory_$N` を付与
- `previewBuilder: BuildCollectedPreviewIr` — `CollectedPreview(...)` ctor call の構築 reuse

### `BuildLazyWrapperIr`

`lazy { sequenceExpr }` を構築するだけの最小 builder。 `kotlin.Lazy<Sequence<CollectedPreview>>` 型の値を返す。

### `BuildPreviewExportIr`

`PreviewExport(lazyExpr)` constructor call を構築。 [`PreviewExportNotFoundError`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/error/Errors.kt) の発火元。

### `BuildConcatenatedPreviewSequencesIr` (cross-module 連結)

`collectAllModulePreviews()` のとき、 同 module の `lazyPreviewSequence({...}, ...)` と、 dependency module の
`previewHint_<scope>(null)` 呼び出し結果群を `+` 演算子で連結し、 さらに
`distinctPreviewsByIdSequence(...)` で id 重複を排除する。

`distinctPreviewsByIdSequence` の defensive lookup 失敗時は [`RuntimeFunctionNotFoundError`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/error/Errors.kt) で
`.throwAsException()`。

### `BuildCollectedPreviewIr`

1 件の `@Preview` から 1 件の `CollectedPreview(id, displayName, filePath, ..., content = @Composable { ... })`
constructor call IR を構築する。 PR #196 以降は factory lambda 化は呼び出し側 (`BuildPreviewSequenceIr.invoke`) で行うため、
ここでは ctor call の組み立てに専念する。

### `ExtractedSourceText`

各 preview の source code / KDoc 抽出を担う data class + helper。 `CollectedPreview(code = "...", kdoc = "...")` に
渡す素材を生成。

---

## 設計判断

### なぜ factory lambda 化するか

`CollectedPreview(content = @Composable { com.example.MyButton() })` を直接 sequence に並べると、
sequence 構築時点で全 preview の `@Composable` lambda が allocate される。 大量 preview を持つ module で
gallery の最初の 10 件しか表示しないケースでも全件 allocate されるとパフォーマンス劣化。

`{ CollectedPreview(...) }` の `() -> CollectedPreview` factory lambda にして
`lazyPreviewSequence(*factories)` で iterate すると、 `asSequence().take(10)` で先頭 10 件のみが
deal with される (= 残り N-10 件の `@Composable` lambda は never allocated)。

### なぜ `BuildPreviewSequenceIr` を orchestrator として残すか

sub-builder (`BuildLazyWrapperIr` 等) を直接 `ReplaceCollectPreviewsFunBody` から call すると、
`PreviewSequenceBuildContext` (= 共有 lookup cache) を毎回 inject する記述が散る。 orchestrator が
context を 1 度作って sub-builder を呼ぶことで、 caller (= `ReplaceCollectPreviewsFunBody`) は
`BuildPreviewSequenceIr(...).invoke(builder, parent, scope)` の 1 呼び出しで済む。

### なぜ `HashMapWithCollisionDetection` は `utils/` に置かないか

`onCollision` callback の shape (= `(hash, existing, conflicting)`) が preview semantic に密結合
(「同じ canonical key を 2 度 register するのは idempotent overwrite として扱う」 という preview-specific ルール)。
`utils/` に置くなら callback を非 preview-specific な API surface に直す必要があり、 抽象化コストが見合わない。
詳細は [`HashMapWithCollisionDetection.kt`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/ir/collectPreviewsReplacement/HashMapWithCollisionDetection.kt) の KDoc 参照。

### ignore = true は FIR side で既に除外済み

[`HintEntriesProvider.computeHintEntries`](../../src/main/kotlin/me/tbsten/compose/preview/lab/compiler/feature/previewCollection/HintEntriesProvider.kt) は
`filterNot { it.isIgnoredByComposePreviewLabOption() }` で ignore を除外しているため、
IR 側 `BuildPreviewByHashMap` に渡る `previews: List<PreviewFunctionInfo>` には ignore 済みのものは含まれない。
これにより:

- ignore preview の hash と通常 preview の hash が衝突しても false-positive ERROR にならない
- hash map に余分なエントリが入らないので hint stub lookup も無駄打ちしない

詳細は [hint-generation.md](./hint-generation.md) 「ignore = true の扱い」を参照。

---

## 関連ドキュメント

- [hint-generation.md](./hint-generation.md) — FIR 側で emit される hint stub の全体像
- [marker-generation.md](./marker-generation.md) — marker class の役割と hint との 1:N 関係
- [hint-naming.md](./hint-naming.md) — IR 側で marker 短名から hash を復元する SSoT
- [scope-validation.md](./scope-validation.md) — IR backstop check の FIR 側 counterpart
- [error-flow.md](./error-flow.md) — Error 発火条件と reply 文言
