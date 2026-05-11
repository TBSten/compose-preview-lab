---
paths:
    - "compiler-plugin/src/main/kotlin/me/tbsten/compose/preview/lab/compiler/utils/**/*.kt"
---

# Rule: compiler-plugin/utils/ には compose-preview-lab 固有の知識を持たない汎用 utility のみ

## 適用範囲

`compiler-plugin/src/main/kotlin/me/tbsten/compose/preview/lab/compiler/utils/` 配下のすべての `.kt` ファイル。 サブディレクトリ (`utils/fir/`, `utils/ir/`) を含む。

## 要件

`utils/` 配下に置けるのは、**Kotlin compiler API (FIR / IR) のラッパー、 共通定型処理、 純粋な変換関数** などで、 以下のような **compose-preview-lab 固有の知識** に依存しない汎用 utility のみ。

`utils/` 配下に置いては **いけない** ものの例:

- `@Preview` annotation の検出 (CMP / Android 双方の FQN を知っている)
- `PreviewLab` 命名規則 (`previewHint_<scope>`, `PreviewHintMarker_<sanitized>_<hash>`)
- hint hash の計算式 / hash 衝突検出 (hint hashing は preview 固有の domain 知識)
- 関数 signature → FQN list の format (`<classFqn>` / `<classFqn>?` / `?` 形式は hint canonical key のためだけ)
- `@ComposePreviewLabOption` の読み取り (`ignore`, `collectScopes` 引数解釈)
- preview hint stub の生成 / 復元
- `@SyntheticPreviewHint` / `@InternalComposePreviewLabApi` の注入

これらは **`feature/<featureName>/` 配下** (例: `feature/previewCollection/`、 `feature/previewCollection/fir/hintGeneration/`) に置くこと。

## Good 例

### 1. compiler API のラッパー (preview 固有知識ゼロ)

```kotlin
// utils/CompilerIds.kt
internal fun classIdOf(packageName: String, name: String): ClassId =
    ClassId(FqName(packageName), Name.identifier(name))

internal fun callableIdOf(packageName: String, name: String): CallableId =
    CallableId(FqName(packageName), Name.identifier(name))
```

`@Preview` も `PreviewLab` も知らない、純粋な `ClassId` / `CallableId` constructor の短縮 helper。 他の Kotlin Compiler Plugin プロジェクトにそのままコピペできる。

### 2. IR Declaration → CompilerMessageLocation の変換 (preview 固有知識ゼロ)

```kotlin
// utils/ir/CompilerMessageLocation.kt
internal fun IrDeclaration.compilerMessageLocation(): CompilerMessageLocation? {
    val entry = runCatching { file }.getOrNull()?.fileEntry ?: return null
    val offset = startOffset.takeIf { it >= 0 } ?: return null
    val line = entry.getLineNumber(offset) + 1
    val column = entry.getColumnNumber(offset) + 1
    return CompilerMessageLocation.create(entry.name, line, column, null)
}
```

`IrDeclaration` から source location を取り出す純粋関数。 preview 固有知識を持たない。

### 3. TargetPlatform の判定 wrapper (preview 固有知識ゼロ)

```kotlin
// utils/ir/PlatformUtil.kt
internal val TargetPlatform?.requiresKlibIcSafetyForCrossModuleHint: Boolean
    get() {
        if (this == null) return false
        if (isJvm()) return false
        return isJs() || isWasm() || isNative()
    }
```

`TargetPlatform` の判定のみ。 名前に `CrossModuleHint` が含まれているが、 関数自体は preview の知識を持たない (= JVM 以外の KLIB-based platform 判定の wrapper)。 名前で動機を示すだけで、 logic は汎用。

### 4. FIR `ClassId` → `FirAnnotation` 構築 (preview 固有知識ゼロ)

```kotlin
// utils/fir/AnnotationBuilders.kt
internal fun ClassId.buildSimpleAnnotation(session: FirSession): FirAnnotation =
    buildAnnotation { /* ... */ }
```

任意の `ClassId` から no-arg annotation を作る。 `@InternalComposePreviewLabApi` 専用ではなく、 どんな annotation でも作れる汎用 builder。

## Bad 例

### 1. `@Preview` 検出 (preview 固有 = NG)

```kotlin
// utils/fir/PreviewAnnotationPredicates.kt  ← NG: utils/ に置くな
internal val PreviewPredicate: LookupPredicate = LookupPredicate.create {
    annotated(
        FqName("org.jetbrains.compose.ui.tooling.preview.Preview"),
        FqName("androidx.compose.ui.tooling.preview.Preview"),
    )
}
```

→ 正しい場所: `feature/previewCollection/PreviewAnnotationPredicates.kt` (preview 検出は feature 直下の domain 知識)

### 2. `parameterTypeFqns` (hint hash 用 format = NG)

```kotlin
// utils/ir/ParameterTypeFqns.kt  ← NG: utils/ に置くな
internal fun IrSimpleFunction.parameterTypeFqns(): List<String> = /* ... */
```

→ 用途が hint hash の canonical key のための format (`<classFqn>?` / `?` 3 形式) なので preview 固有。 正しい場所: `feature/previewCollection/ParameterTypeFqns.kt`

### 3. `HashMapWithCollisionDetection` (hint hash collision 検出用 = NG)

```kotlin
// utils/ir/HashMapWithCollisionDetection.kt  ← NG: 一見汎用だが用途が hint hash 衝突
internal fun <V> buildHashMapWithCollisionDetection(/* ... */): Map<String, V>
```

→ generic shape だが用途が preview hint hashing。 将来汎用化が必要になった時点で utils/ に格上げし、 `BuildPreviewByHashMap` との結合 (= preview 固有 callback) を別 wrapper に切り出すこと。 正しい場所 (現状): `feature/previewCollection/ir/collectPreviewsReplacement/HashMapWithCollisionDetection.kt`

### 4. `DeprecationHidden` (hint stub 用 = NG)

```kotlin
// utils/fir/DeprecationHidden.kt  ← NG: 用途が hint stub 隠蔽
internal fun FirCallableDeclaration.markAsDeprecatedHidden(session: FirSession, compat: CompatContext)
```

→ 用途が hint stub を `@Deprecated(level = HIDDEN)` で隠す処理なので preview 固有 (hint stub という concept が compose-preview-lab 固有)。 正しい場所: `feature/previewCollection/fir/hintGeneration/DeprecationHidden.kt`

## チェックリスト (PR レビュー時)

- [ ] `utils/` 配下の新規ファイルは `@Preview` / `PreviewLab` / hint / marker / scope / option のいずれかを「知っている」コードを含まない
- [ ] `utils/` 配下の関数名・class 名は `Preview` や `Hint` を含まない (含まない方が望ましいが、 名前で動機を示すだけで logic が汎用なら許容 ─ 例: `requiresKlibIcSafetyForCrossModuleHint`)
- [ ] preview / hint / marker / scope に関連する domain 知識を持つコードは `feature/<featureName>/` 配下に置かれている
- [ ] 「`utils/` に置きたいが preview 知識を含む」場合は preview 固有 callback を別 wrapper に切り出して、 wrapper 側を logic 配下に置く設計を検討した
