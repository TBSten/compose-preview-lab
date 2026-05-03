# Metro FIR-based hint generator – 実装ガイド (Compose Preview Lab 移植用)

> 調査対象 commit: `73817f2` (Metro main, 2026-04-30 頃)
> 引用は `/tmp/metro-research/metro/` 配下の絶対パスではなく、Metro リポジトリ相対パスで示す。

このドキュメントは Compose Preview Lab の compiler-plugin を **KLIB platform (JS / WasmJS / iOS)
でも cross-module aggregation を成立させる** ため、Metro の FIR-based contribution hint generator
パターンを移植する際の参照資料。

---

## TL;DR — 移植の核心

Metro が KLIB IdSignature 衝突を回避している仕組みは「marker class を別生成する」ことではなく、
**`@Contributes*` を付けた user class そのものを `valueParameter` の型に使う** こと。
その結果、各モジュールの hint 関数は `(<scope>Hint)(contributed: TheUserClass)` という
シグネチャになり、IdSignature が `(name, parameterTypes)` ベースでも自然に unique になる。

Compose Preview Lab には「user が contribute する class」がそもそも無いので、
**plugin が module ごとに 1 個の synthetic marker class を FIR 段で `getTopLevelClassIds()` /
`generateTopLevelClassLikeDeclaration()` で生成し、その class を hint 関数の parameter 型に使う**
のが正解になる。詳細は §J。

それ以外の項目 (FIR↔IR 連携、`markAsDeprecatedHidden`, `containingFileName`, KLIB IC など) は
Metro の実装をほぼそのまま流用できる。

---

## A. FIR generator の全体構造

### A.1 ファイル

主役: `compiler/src/main/kotlin/dev/zacsweers/metro/compiler/fir/generators/ContributionHintFirGenerator.kt`

クラス階層:

```kotlin
internal class ContributionHintFirGenerator(
  session: FirSession,
  compatContext: CompatContext,
  private val externalExtensions: List<MetroFirDeclarationGenerationExtension>,
) : FirDeclarationGenerationExtension(session), CompatContext by compatContext { ... }
```
(L47-51)

`FirDeclarationGenerationExtension` を継承し、`CompatContext` を delegate で混ぜている。
`compatContext` には API 差分吸収のための helper が乗る。

### A.2 オーバーライドしている関数

ContributionHintFirGenerator では **3 個** のみ:

| Override                                       | 行番号    | 何を返すか                                                       |
|------------------------------------------------|--------|-------------------------------------------------------------|
| `registerPredicates()`                         | 154-157 | `injectAnnotation` / `contributesAnnotation` の 2 述語を register |
| `getTopLevelCallableIds()`                     | 160-162 | `contributedClassesByScope` cache の `keys`                   |
| `generateFunctions(callableId, context)`       | 165-188 | scope ごとの hint 関数群 (overloaded)                              |
| `hasPackage(packageFqName)`                    | 190-196 | `metroHintsPackage` のときだけ `true`                             |

ContributionHintFirGenerator は **クラス生成は一切しない**。`getTopLevelClassIds()` /
`generateTopLevelClassLikeDeclaration()` / `getCallableNamesForClass()` /
`getNestedClassifiersNames()` は override されていない。

> **Compose Preview Lab への対応関係**
> Compose Preview Lab では「unique な marker class を module ごとに 1 個生成する」必要があるため、
> 上記に加えて `getTopLevelClassIds()` と `generateTopLevelClassLikeDeclaration(classId)` も
> override する必要がある。`ContributionsFirGenerator` (`compiler/src/main/kotlin/.../fir/generators/ContributionsFirGenerator.kt:213-248`) が良い参考例:
>
> ```kotlin
> @ExperimentalTopLevelDeclarationsGenerationApi
> override fun getTopLevelClassIds(): Set<ClassId> { ... }
>
> @ExperimentalTopLevelDeclarationsGenerationApi
> override fun generateTopLevelClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>? {
>   if (getHolder(classId) == null) return null
>   return createTopLevelClass(classId, Keys.ContributionProviderHolderDeclaration) {
>       modality = Modality.ABSTRACT
>     }
>     .apply {
>       buildHiddenFromObjCAnnotation(session)?.let { replaceAnnotationsSafe(listOf(it)) }
>       markAsDeprecatedHidden(session)
>     }
>     .symbol
> }
> ```

### A.3 FIR コールバックの timing

- `registerPredicates()` — session 構築時に 1 回。これが届かないと
  `predicateBasedProvider.getSymbolsByPredicate(...)` が空を返す。
- `getTopLevelCallableIds()` — supertype generation の 1 phase 後 (predicate 評価が終わった後)。
  ここで返した CallableId 集合に対して順に `generateFunctions` がコールバックされる。
- `generateFunctions(callableId, context)` — 上記で返した各 callableId について 1 回ずつ。
  context は class member generation のとき non-null、top-level のとき `null`。
- `getTopLevelClassIds()` も同様 (本ファイルでは未使用)。

---

## B. registerPredicates の使い方

```kotlin
// L154-157
override fun FirDeclarationPredicateRegistrar.registerPredicates() {
  register(session.predicates.contributesAnnotationPredicate)
  register(session.predicates.injectAnnotationPredicate)
}
```

predicate そのものは `compiler/src/main/kotlin/dev/zacsweers/metro/compiler/fir/ExtensionPredicates.kt` で定義されている:

```kotlin
internal class ExtensionPredicates(private val classIds: ClassIds) {
  internal val qualifiersPredicate = DeclarationPredicate.create {
    metaAnnotated(classIds.qualifierAnnotations.asFqNames(), includeItself = false)
  }
  internal val contributesAnnotationPredicate =
    annotated(classIds.allContributesAnnotations.asFqNames())
  internal val injectAnnotationPredicate = annotated(classIds.injectAnnotations.asFqNames())
  // ...
}
```
(`ExtensionPredicates.kt:13-46`)

述語は2種類: `annotated(...)` (直接アノテーション) と `metaAnnotated(...)` (アノテーション付き
アノテーション)。Metro は `org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate.BuilderContext`
の DSL を使っている。

述語は **session 起動時に classpath を走査して候補を集める**。後段の `getSymbolsByPredicate` は
その内部 cache を引くだけ。

#### Evaluation cycle 回避

ContributionHintFirGenerator の cache 構築時:

```kotlin
// L80-86
val contributingClasses = contributedClassSymbols()
for (contributingClass in contributingClasses) {
  val contributions =
    contributingClass
      .annotationsIn(session, session.classIds.allContributesAnnotations)
      .toList()

  if (contributions.isEmpty()) continue
```

ここで `annotationsIn` は **resolved** な annotation を返す helper だが、
contributing class の annotation 解決中に `resolvedScopeClassId` が再帰的に他の generator を
要求するケースがある。Metro では `MetroFirTypeResolver.Factory(session)` を per-class instance
化することで予防しており (`L69-95`)、IDE では別途 lazy 化が施されている (CHANGELOG 0.12.1 の
「Make `allSessions` lookup lazy to avoid lockups in the IDE」参照)。

> **Compose Preview Lab への対応関係**
> Compose Preview Lab はそもそも user class を述語で集めない (集めるのは `@Preview` 関数だが、
> その情報は IR phase で `moduleFragment.files` を scan して既に得ている)。
> したがって述語登録は不要、もしくは `@Preview` 関数所在 file を発見するための flag に留めれば良い。

---

## C. CallableId / ClassId の lazy 構築 (FirCache パターン)

```kotlin
// L71-95
private val contributedClassesByScope:
  FirCache<Unit, Map<CallableId, Set<FirClassSymbol<*>>>, Unit> =
  session.firCachesFactory.createCache { _, _ ->
    val callableIds = mutableMapOf<CallableId, MutableSet<FirClassSymbol<*>>>()
    val contributingClasses = contributedClassSymbols()
    for (contributingClass in contributingClasses) {
      val contributions =
        contributingClass
          .annotationsIn(session, session.classIds.allContributesAnnotations)
          .toList()
      if (contributions.isEmpty()) continue
      val typeResolver = typeResolverFactory.create(contributingClass) ?: continue
      val contributionScopes: Set<ClassId> = contributions.mapNotNullToSet { ... }
      for (contributionScope in contributionScopes) {
        val hintName = contributionScope.scopeHintFunctionName()
        callableIds.getAndAdd(
          CallableId(Symbols.FqNames.metroHintsPackage, hintName),
          contributingClass,
        )
      }
    }
    // ...
    callableIds
  }
```

そして:

```kotlin
@ExperimentalTopLevelDeclarationsGenerationApi
override fun getTopLevelCallableIds(): Set<CallableId> {
  return contributedClassesByScope.getValue(Unit, Unit).keys  // L161
}
```

#### ポイント

- `FirCache<Unit, Map<…>, Unit>` でキャッシュキーは Unit。1 回計算したら再利用。
- `firCachesFactory.createCache(...)` を使うのは、FIR の lazy resolve サイクルとうまく協調するため
  (`session.firCachesFactory.createCache` を使うと FIR 内部のロックスキームに乗っかる)。
- `getTopLevelCallableIds()` は cache を使い切る前にも呼ばれうる (predicate 解決前など) が、
  Metro は predicate 評価結果の cache (`predicateBasedProvider`) を session 側が握っているので、
  この時点では既に充足している前提で書かれている。

#### Chicken-and-egg 問題

Metro の場合、CallableId は `<scopeClass>HintFunctionName` という形で **決定的** に求まる
(scope class id から純関数で計算)。CallableId を返すために結局 contributing class の
annotation を解決する必要があるが、predicate ベースの lookup は循環しない設計
(`predicateBasedProvider` は session レベルでキャッシュされる)。

```kotlin
// util.kt:197
internal fun ClassId.scopeHintFunctionName(): Name = joinSimpleNames().shortClassName

// Symbols.kt:131-141
object CallableIds {
  fun scopeHint(scopeClassId: ClassId): CallableId {
    return CallableId(FqNames.metroHintsPackage, scopeClassId.joinSimpleNames().shortClassName)
  }
}
```

> **Compose Preview Lab への対応関係**
> Compose Preview Lab では callable name は **モジュールごとに 1 個の固定名**
> (`previewLabExport`) で良い。CallableId 自体は固定で、parameter type が module-unique な
> marker class になる。これを成立させるには:
>
> 1. `getTopLevelClassIds()` で marker class を **複数** 返す (将来の拡張用) or **1 個** 返す
>    (コンパイル単位 = モジュール単位)
> 2. `getTopLevelCallableIds()` では固定名 `previewLabExport` を CallableId として返す
> 3. `generateFunctions` で marker class symbol を解決し、それを value parameter type に流す
>
> 注意: FIR 段では「自モジュール内の `@Preview` 関数の集合」は predicate 経由で集めても良いが、
> Compose Preview Lab は IR で source 走査するため、FIR 段では「`@Preview` が 1 個でもあるか」
> を判定するか、無条件に marker class を 1 個生成するかのどちらか。後者の方が単純。

---

## D. Marker class と hint 関数の生成詳細

### D.1 hint 関数生成の本体

```kotlin
// L164-188
@OptIn(ExperimentalTopLevelDeclarationsGenerationApi::class)
override fun generateFunctions(
  callableId: CallableId,
  context: MemberGenerationContext?,
): List<FirNamedFunctionSymbol> {
  val contributionsToScope =
    contributedClassesByScope.getValue(Unit, Unit)[callableId] ?: return emptyList()
  return contributionsToScope
    .sortedBy { it.classId.asFqNameString() }
    .map { contributingClass ->
      val containingFileName =
        HintGenerator.hintFileName(contributingClass.classId, callableId.callableName)
      createTopLevelFunction(
          Keys.ContributionHint,
          callableId,
          session.builtinTypes.unitType.coneType,
          containingFileName = containingFileName,
        ) {
          visibility = contributingClass.rawStatus.visibility
          valueParameter(Symbols.Names.contributed, { contributingClass.constructType(it) })
        }
        .apply { markAsDeprecatedHidden(session) }
        .symbol as FirNamedFunctionSymbol
    }
}
```

#### 注目ポイント

1. **同じ callableId に対して複数の関数を返している**
   `sortedBy { it.classId.asFqNameString() }.map { ... }` で、同 scope への各 contributing class
   ごとに 1 個ずつ FirNamedFunctionSymbol を生成。
   FIR ↔ IR レイヤは「同名 callable の overload」をサポートするため、これが overload として届く。

2. **`createTopLevelFunction` の `containingFileName` 引数**
   FIR plugin DSL の API。各関数を別ファイルに分ける指示。
   ファイル名は `HintGenerator.hintFileName(...)` で計算 (後述)。

3. **value parameter の型は `contributingClass.constructType(it)`**
   ```kotlin
   internal fun FirClassSymbol<*>.constructType(
     typeParameterRefs: List<FirTypeParameterRef>
   ): ConeClassLikeType {
     return constructType(typeParameterRefs.mapToArray { it.symbol.toConeType() })
   }
   ```
   (`fir.kt:946-950`)

   `it` は generated function の type parameter list (空)。要するに「contributing class への
   no-projection type」。これによって parameter 型が contributing class そのものになり、
   IdSignature が unique になる。**KLIB 衝突回避の核心**。

4. **`markAsDeprecatedHidden(session)`**
   ```kotlin
   // fir.kt:896-902
   context(compatContext: CompatContext)
   internal fun FirCallableDeclaration.markAsDeprecatedHidden(session: FirSession) {
     with(compatContext) {
       replaceAnnotations(annotations + listOf(createDeprecatedHiddenAnnotation(session)))
       getDeprecationsProviderCompat(session)?.let(::replaceDeprecationsProvider)
     }
   }
   ```

   生成 hint を `@Deprecated(level = HIDDEN)` 相当にして user code から見えなくする。
   IDE 上の補完 / 警告ノイズ防止と、誤って呼ばれることを防ぐため (誤呼出した場合 metro は
   `error("Stub!")` 相当の挙動)。
   CHANGELOG 1270 行 "Hide FIR-generated hint functions" もこの fix を表す。

5. **`Keys.ContributionHint` (GeneratedDeclarationKey)**
   ```kotlin
   // fir/Keys.kt:116-118
   data object ContributionHint : GeneratedDeclarationKey() {
     override fun toString() = "ContributionHint"
   }
   ```
   FIR generator が出した宣言を IR phase で見分けるためのマーカ。
   `Origins.kt`:
   ```kotlin
   val ContributionHint: IrDeclarationOrigin =
     IrDeclarationOrigin.GeneratedByPlugin(Keys.ContributionHint)
   ```

### D.2 file name (sanitize) ロジック

```kotlin
// HintGenerator.kt:147-159
companion object {
  fun hintFileName(sourceClassId: ClassId, hintName: Name): String {
    val fileNameWithoutExtension =
      sequence {
          yieldAll(sourceClassId.packageFqName.pathSegments())
          yield(sourceClassId.joinSimpleNames(separator = "", camelCase = true).shortClassName)
          yield(hintName)
        }
        .joinToString(separator = "") { it.asString().capitalizeUS() }
        .decapitalizeUS()
    return "$fileNameWithoutExtension.kt"
  }
}
```

例: `com.example.MyClass` を AppScope に contribute → file name は
`comExampleMyClassAppScopeHint.kt` 風 (実際は camelCase で繋ぐ)。

> **Compose Preview Lab への対応関係**
> 既存の `GeneratePreviewExportHint.createHintFile`
> (`/home/user/compose-preview-lab/compiler-plugin/src/main/kotlin/me/tbsten/compose/preview/lab/compiler/ir/GeneratePreviewExportHint.kt:154-183`)
> の `PreviewLabExport_${sanitizedFqn}.kt` パターンをそのまま FIR 側に移植する。
> sanitize ロジックは Metro の `sourceClassId.joinSimpleNames(...)` を真似て、
> 自分の marker class id (例: `me.tbsten.compose.preview.lab.exports.PreviewMarker_<moduleHash>`)
> から作る。

### D.3 marker class が無いことについて

Metro は **新たに marker class を作っていない**。`@Contributes*` の付いた user class
そのものが parameter type になる。これが最大の差分なので Compose Preview Lab では
独自に marker class を生成する必要がある (§J 参照)。

---

## E. Downstream 側の発見

### E.1 ファイル

`compiler/src/main/kotlin/dev/zacsweers/metro/compiler/fir/generators/ContributedInterfaceSupertypeGenerator.kt`

### E.2 hint 経由 lookup (核心 30 行)

```kotlin
// L123-181
private val generatedScopesToContributions:
  FirCache<ClassId, Map<ClassId, Boolean>, TypeResolveService> =
  session.firCachesFactory.createCache { scopeClassId, typeResolver ->
    val scopeHintFqName = Symbols.FqNames.scopeHint(scopeClassId)
    val functionsInPackage =
      session.symbolProvider.getTopLevelFunctionSymbols(
        scopeHintFqName.parent(),
        scopeHintFqName.shortName(),
      )

    val filteredFunctions = functionsInPackage.filter {
      when (it.visibility) {
        Visibilities.Internal -> {
          it.moduleData == session.moduleData ||
            @OptIn(SymbolInternals::class)
            session.moduleVisibilityChecker?.isInFriendModule(it.fir) == true
        }
        else -> true
      }
    }

    val contributingClasses = filteredFunctions.mapNotNull { contribution ->
      // This is the single value param
      contribution.valueParameterSymbols.single().resolvedReturnType.toRegularClassSymbol(session)
    }
    // ...
    getScopedContributions(contributingClasses, scopeClassId, typeResolver)
  }
```

#### 抽出すべきパターン

1. **`session.symbolProvider.getTopLevelFunctionSymbols(pkg, name)`**
   classpath 上の全 module から指定 (pkg, name) を持つ top-level 関数 symbol を全部返す。
   IR の `referenceFunctions(callableId)` の FIR 版。

2. **`Visibilities.Internal` の friend module フィルタ**
   internal な hint も同 module / friend module からは見える。
   `session.moduleVisibilityChecker?.isInFriendModule(it.fir)` で判定。

3. **value parameter の型から contributing class を逆引き**
   ```kotlin
   contribution.valueParameterSymbols.single().resolvedReturnType.toRegularClassSymbol(session)
   ```
   hint 関数のシグネチャから contributing class を再構成。
   Compose Preview Lab で言うと、marker class の type を取り出して、その class id から元の
   provider/property を関連付ける作業に対応。

### E.3 自モジュール由来 hint の除外

```kotlin
// L317-323
val inCompilationContributions =
  inCompilationScopesToContributions.getValue(scopeClassId, typeResolver)
for ((classId, isBindingContainer) in
  (inCompilationContributions + classPathContributions)) {
  put(classId, isBindingContainer)
}
```

Metro は「自モジュール」と「依存モジュール」の hint 集合を **別ソース** で集めて merge する:

- 自モジュール: `inCompilationScopesToContributions` (`predicateBasedProvider` 経由で
  contribute した user class を直接 walk; hint 関数は経由しない)
- 依存モジュール: `generatedScopesToContributions` (上の hint 経由 lookup)

つまり「自モジュールの hint を発見しないようにフィルタする」のではなく、
**最初から自モジュールは hint を経由しない経路で集める**。

> **Compose Preview Lab への対応関係**
> 現状実装 `PreviewListIrBuilder.collectDependencyGetters`
> (`PreviewListIrBuilder.kt:320-360`) は `IR_EXTERNAL_DECLARATION_STUB` origin で自モジュール
> 由来 hint を弾いている。FIR 移行後も同じく「自モジュールの previews は IR で moduleFragment
> walk から既に手元にあるので、hint 経由 lookup は依存モジュール分だけにする」発想で OK。
> Metro 流の二経路収集を真似ても良い。

---

## F. IR 段との連携

### F.1 IR 段は「stub body を埋める」だけ

```kotlin
// ContributionHintIrTransformer.kt:33-39
// Only executed if generateContributionHintsInFir is enabled
// Implements the FIR-generated declarations with empty bodies
fun visitFunction(declaration: IrSimpleFunction) {
  if (declaration.origin == Origins.ContributionHint) {
    declaration.apply { body = stubExpressionBody() }
  }
}
```

`Origins.ContributionHint` は `IrDeclarationOrigin.GeneratedByPlugin(Keys.ContributionHint)`。
FIR で生成したものは IR phase に届くと **body が空** なので、ここで `error("Stub!")` 相当の
expression body を入れて backend が文句を言わないようにする。

### F.2 driver 側

```kotlin
// CoreTransformers.kt:98-103
override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
  if (options.generateContributionHintsInFir) {
    contributionHintIrTransformer.value.visitFunction(declaration)
  }
  return super.visitSimpleFunction(declaration)
}
```

```kotlin
// CoreTransformers.kt:131-136
// Native/Wasm/JS compilation hint gen can't be done in IR
// https://youtrack.jetbrains.com/issue/KT-75865
val generateHints = options.generateContributionHints && !options.generateContributionHintsInFir
if (generateHints) {
  trace("ContributionHint") { contributionHintIrTransformer.value.visitClass(declaration) }
}
```

`generateContributionHintsInFir` が true なら IR では body を埋めるだけ。
false (=旧 IR-based path) なら従来通り `HintGenerator.generateHint()` で synthetic IrFile を
注入する。

### F.3 FIR↔IR 状態共有 (`FirExtensionSessionComponent`)

`MetroFirBuiltIns` は `FirExtensionSessionComponent(session)` を継承していて、
session 越しに builder / classIds / options を共有する:

```kotlin
// MetroFirBuiltIns.kt:22-28
internal class MetroFirBuiltIns(
  session: FirSession,
  val classIds: ClassIds,
  val predicates: ExtensionPredicates,
  val options: MetroOptions,
  val compatContext: CompatContext,
) : FirExtensionSessionComponent(session) {
  // ... lazy memoize で FIR 上の symbol lookup を cache
}

// L220
internal val FirSession.metroFirBuiltIns: MetroFirBuiltIns by FirSession.sessionComponentAccessor()
```

これはハイレベルには「FIR session 全体で共有したい中央レジストリ」を実現する仕組み。
ContributionHintFirGenerator も `session.metroFirBuiltIns.options.generateContributionProviders`
のようにここから読み出している。

> **Compose Preview Lab への対応関係**
> Compose Preview Lab には現状 `PluginConfig` を引き回している場所があるので、
> 同じくらいの機能を `PreviewLabFirBuiltIns : FirExtensionSessionComponent(session)` として
> 用意し、`session.previewLabFirBuiltIns` 拡張プロパティを生やすのが綺麗。
> ただし最小実装では「ExtensionRegistrar が options を constructor に直渡す」だけで足りるかも。

---

## G. Kotlin バージョン gate

### G.1 Metro の判定

```kotlin
// MetroOptions.kt:1457-1485
public companion object {
  /** Minimum Kotlin version on the 2.3.x line that supports JS IC with top-level declarations. */
  private val MIN_KOTLIN_2_3_JS_IC = KotlinToolingVersion("2.3.21-RC")
  /** Minimum Kotlin dev version on the 2.4.x line that supports JS IC with top-level declarations. */
  private val MIN_KOTLIN_2_4_DEV_JS_IC = KotlinToolingVersion("2.4.0-dev-8064")
  /** Minimum Kotlin non-dev version on the 2.4.x line that supports JS IC with top-level declarations. */
  private val MIN_KOTLIN_2_4_JS_IC = KotlinToolingVersion("2.4.0-Beta2")

  private fun kotlinVersionSupportsJsIC(version: KotlinToolingVersion): Boolean {
    if (version.major > 2) return true
    return when (version.minor) {
      in 0..2 -> false
      3 -> version >= MIN_KOTLIN_2_3_JS_IC
      4 ->
        if (version.maturity == KotlinToolingVersion.Maturity.DEV) {
          version >= MIN_KOTLIN_2_4_DEV_JS_IC
        } else {
          version >= MIN_KOTLIN_2_4_JS_IC
        }
      else -> true
    }
  }
}
```

CHANGELOG 0.10.0:
> **[Gradle / FIR]** Enable FIR hint generation by default on Kotlin `2.3.20-Beta1` or later.

CHANGELOG 0.12.1:
> Support top-level FIR gen (contribution hints, function inject, etc) in Kotlin/JS on `2.3.21`+ and `2.4.0-Beta2`+.

つまり:

- **2.3.20-Beta1 以上** で FIR-based hint generation がデフォルト
- **JS で IC を有効にしている場合** は更に厳しく `2.3.21-RC` / `2.4.0-Beta2` 以上が必要 (KT-82395)

### G.2 古い Kotlin への fallback

```kotlin
// MetroFirExtensionRegistrar.kt:147-153
if (options.generateContributionHints && options.generateContributionHintsInFir) {
  add(
    wrapNativeGenerator("FirGen - ContributionHints", true) { session, compatContext ->
      ContributionHintFirGenerator(session, compatContext, externalExtensions)
    }(session)
  )
}
```

`generateContributionHintsInFir = false` のとき (= 旧 path) は IR の `ContributionHintIrTransformer.visitClass`
が hint を生成する。Gradle plugin 側が Kotlin version を見てこの flag を切り替える設計。

> **Compose Preview Lab への対応関係**
> Compose Preview Lab は `compiler-plugin-compat-k230` / `compiler-plugin-compat-k240_beta2` で
> 既に Kotlin version を切り分けている。FIR-based path は `k230` 以降のみで有効化する。
> `k230` 未満では現行の IR-based path (JVM のみ) を維持する。
>
> Metro と違い、Compose Preview Lab は KLIB なら必ず FIR path を使う必要がある (IR path だと
> KLIB 衝突する) ので、boolean 1 個ではなく
> `(platform=JVM && kotlin<2.3.20) → IR path / それ以外 → FIR path` という分岐が良い。

---

## H. JS incremental compile 対応 (KT-82395)

### H.1 Metro の対応

```kotlin
// MetroOptions.kt:1416-1450
private fun validateKotlinJsIC(
  compilerVersion: KotlinToolingVersion,
  configuration: CompilerConfiguration,
  onError: (String) -> Unit,
): Boolean {
  val supportJsIc =
    !configuration.jsIncrementalCompilationEnabled ||
      configuration.wasmCompilation ||
      kotlinVersionSupportsJsIC(compilerVersion)
  if (supportJsIc) {
    return true
  }

  val jsICOptions = buildList {
    if (enableTopLevelFunctionInjection) add("enableTopLevelFunctionInjection")
    if (generateContributionHints) add("generateContributionHints")
    if (generateContributionHintsInFir) add("generateContributionHintsInFir")
  }

  if (jsICOptions.isNotEmpty()) {
    onError(
      "Kotlin/JS does not support generating top-level declarations with incremental compilation enabled. " +
        "See https://youtrack.jetbrains.com/issue/KT-82395 and https://youtrack.jetbrains.com/issue/KT-82989. " +
        "Either disable ${jsICOptions.joinToString()} for JS targets or disable JS IC."
    )
    return false
  }
  return true
}
```

`wasmCompilation = true` のときは IC 対応を要求しない (Wasm IC は別経路)。

### H.2 KT-82395 がいつから effective か

CHANGELOG 0.12.1 (2026-03-30) より:
> Support top-level FIR gen (contribution hints, function inject, etc) in Kotlin/JS on `2.3.21`+ and `2.4.0-Beta2`+.

それ以前は JS + IC + top-level FIR gen の組合せはエラー。Compose Preview Lab でもこの 3 条件
(JS, IC enabled, top-level decl gen) を validate して、満たさない場合はエラー or warn する必要がある。

> **Compose Preview Lab への対応関係**
> 現状の `PluginConfig` には Kotlin version も JS IC flag も載っていない。Gradle plugin 側で
> validate するのが妥当 (Metro と同様)。`subplugin` で Kotlin version を読んで checking を入れる。

---

## I. テスト戦略

### I.1 Metro のテストインフラ

`compiler/src/test/kotlin/dev/zacsweers/metro/compiler/MetroCompilerTest.kt` が共通の
JUnit / kctfork 基盤。`fir/` 配下に FIR generator 単体のテストもあるが、
本タスクで重要なのは **integration test** (cross-module aggregation の動作確認)。

Metro はモジュールを kctfork で擬似的に分離してビルドし、依存関係を classpath に追加して
downstream compile を行う。FIR phase で hint discovery が走り、生成された contribution が
graph supertype に乗ることを assertion する。

snapshot test は `reportsDestination` に書き出される `discovered-hints-fir/...` のテキストを
diff する形式 (`ContributedInterfaceSupertypeGenerator.kt:149-178`):

```kotlin
session.metroFirBuiltIns.writeDiagnostic(
  "discovered-hints-fir",
  { "${scopeClassId.safePathString}.txt" },
) {
  // all functions / filtered functions / contributing classes をテキストでダンプ
}
```

### I.2 各 platform での verify

Metro は Gradle integration test で実際に jvm/js/wasmJs/native 各 target をビルドし、
graph 上の依存解決が成功することを確認する `:integration-tests:*` プロジェクトを持つ。
Compose Preview Lab の現行 `integrationTest/` プロジェクトと同種の構造。

> **Compose Preview Lab への対応関係**
> 既存 integration test に「KLIB platform で 2 つのモジュールにまたがる aggregation が機能する」
> assertion を追加するだけで足りる。`(cd integrationTest && ./gradlew jsBrowserTest)` で
> `collectAllModulePreviews()` が dep の Preview を含むことを Kotest で assertion。

---

## J. Compose Preview Lab への適用上の注意点

### J.1 Metro と Compose Preview Lab のギャップ

| 項目                          | Metro                                                                  | Compose Preview Lab                                       |
|------------------------------|------------------------------------------------------------------------|-----------------------------------------------------------|
| contribute する単位             | user class (`@ContributesBinding` 等)                                  | module 内の `@Preview` 関数集合                            |
| hint の単位                    | scope class id (複数あり得る)                                          | module 内の `collectModulePreviews()` プロパティ (複数 OK) |
| hint 関数の callable name       | scope class 由来 (`AppScope` など)                                     | 固定 (`previewLabExport`)                                 |
| hint 関数の parameter type     | contributing user class                                                | **plugin 生成の synthetic marker class**                  |
| KLIB IdSignature unique 化   | parameter type が user class なので自然に unique                       | marker class を per-FQN unique 化する必要あり            |

要するに **Metro は user class を marker として再利用しているのに対し、Compose Preview Lab は
marker class を 0 から作る必要がある**。

### J.2 推奨アーキテクチャ (擬似コード)

```kotlin
// 新規ファイル: PreviewLabHintFirGenerator.kt
//
// 各コンパイルユニット (= モジュール) に対して 1 個の synthetic marker class を生成し、
// 各 collectModulePreviews() / collectAllModulePreviews() / auto-export 1 件ごとに
// 1 個の hint 関数を生成する。
//
// Marker class id 例 (unique 化の決定法):
//   me.tbsten.compose.preview.lab.exports.<sanitizedPropertyFqn>Marker
// (FIR 段では module 名は読みづらいので、property FQN ベースで生成して属性を残す)
//
// 生成 (semantically equivalent Kotlin):
//   // file: PreviewLabExport_uiLib_uiLibPreviews.kt
//   package me.tbsten.compose.preview.lab.exports
//
//   @Deprecated(level = HIDDEN)
//   public class UiLibUiLibPreviewsMarker private constructor()  // marker
//
//   @Deprecated(level = HIDDEN)
//   @PreviewExportHint(fqn = "uiLib.uiLibPreviews")
//   public fun previewLabExport(value: UiLibUiLibPreviewsMarker): Unit {}

internal class PreviewLabHintFirGenerator(
    session: FirSession,
    compatContext: CompatContext,
    private val pluginConfig: PluginConfig,
) : FirDeclarationGenerationExtension(session), CompatContext by compatContext {

    private val moduleHintsCache: FirCache<Unit, ModuleHintInfo, Unit> =
        session.firCachesFactory.createCache { _, _ ->
            // (1) Source 走査で `@Preview` 関数が 1 個でもあれば、または
            // (2) collectModulePreviews() / collectAllModulePreviews() の property が
            // 1 個でもあれば、対象 module として hint を生成する
            //
            // 各 hint には property FQN (manual) または synthetic provider FQN (auto)
            // が紐づく。各 hint ごとに unique な marker class id を計算。
            collectModuleHintEntries(session)
        }

    private data class HintEntry(
        val targetFqn: String,           // property FQN or auto-provider FQN
        val markerClassId: ClassId,      // unique per hint
    )
    private data class ModuleHintInfo(val entries: List<HintEntry>)

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(/* @Preview annotation predicate */)
        register(/* @ComposePreviewLabOption annotation predicate */)
    }

    @ExperimentalTopLevelDeclarationsGenerationApi
    override fun getTopLevelClassIds(): Set<ClassId> =
        moduleHintsCache.getValue(Unit, Unit).entries.map { it.markerClassId }.toSet()

    @ExperimentalTopLevelDeclarationsGenerationApi
    override fun generateTopLevelClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>? {
        val info = moduleHintsCache.getValue(Unit, Unit)
        if (info.entries.none { it.markerClassId == classId }) return null
        return createTopLevelClass(classId, Keys.PreviewLabHintMarker) {
            modality = Modality.FINAL
            classKind = ClassKind.CLASS
        }.apply {
            markAsDeprecatedHidden(session)
        }.symbol
    }

    // marker class に private constructor が必要なら getCallableNamesForClass + generateConstructors

    @ExperimentalTopLevelDeclarationsGenerationApi
    override fun getTopLevelCallableIds(): Set<CallableId> {
        // すべての hint で同じ name を使う (Metro と違って scope は無い)
        val info = moduleHintsCache.getValue(Unit, Unit)
        return if (info.entries.isEmpty()) emptySet()
        else setOf(CallableId(HINT_PACKAGE, HINT_FUNCTION_NAME))
    }

    @OptIn(ExperimentalTopLevelDeclarationsGenerationApi::class)
    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): List<FirNamedFunctionSymbol> {
        if (callableId.callableName != HINT_FUNCTION_NAME) return emptyList()
        val info = moduleHintsCache.getValue(Unit, Unit)
        return info.entries.sortedBy { it.targetFqn }.map { entry ->
            val markerSymbol =
                session.symbolProvider.getClassLikeSymbolByClassId(entry.markerClassId)
                    as FirClassSymbol<*>
            val fileName = hintFileName(entry.markerClassId, callableId.callableName)
            createTopLevelFunction(
                Keys.PreviewLabHint,
                callableId,
                session.builtinTypes.unitType.coneType,
                containingFileName = fileName,
            ) {
                visibility = Visibilities.Public
                valueParameter(Symbols.Names.value, { markerSymbol.constructType(it) })
            }.apply {
                addPreviewExportHintAnnotation(entry.targetFqn)  // 既存の attachHintAnnotation 相当
                markAsDeprecatedHidden(session)
            }.symbol as FirNamedFunctionSymbol
        }
    }

    override fun hasPackage(packageFqName: FqName): Boolean =
        packageFqName == HINT_PACKAGE || super.hasPackage(packageFqName)

    companion object {
        val HINT_PACKAGE = FqName("me.tbsten.compose.preview.lab.exports")
        val HINT_FUNCTION_NAME = Name.identifier("previewLabExport")
    }
}
```

### J.3 Downstream 側 (collectAllModulePreviews) の発見

IR phase の `PreviewListIrBuilder.collectDependencyGetters()` をそのまま流用できる。
変更点:

- parameter type の check を「`PreviewExport` 完全一致」から「`HINT_PACKAGE` 配下の任意 marker
  class」に緩める
- `IR_EXTERNAL_DECLARATION_STUB` フィルタは引き続き有効 (自モジュール由来除外)

擬似コード:

```kotlin
// PreviewListIrBuilder.collectDependencyGetters の修正案
val regularParams = hintFunction.parameters.filter { it.kind == IrParameterKind.Regular }
if (regularParams.size != 1) return@mapNotNull null
val paramClassFqName = regularParams[0].type.classFqName ?: return@mapNotNull null
// 旧: paramClassFqName == PREVIEW_EXPORT_FQN
// 新: marker class は HINT_PACKAGE 配下に住んでいる
if (paramClassFqName.parent() != GeneratePreviewExportHint.HINT_PACKAGE) return@mapNotNull null
// あとは @PreviewExportHint annotation の fqn を読むのは同じ
```

### J.4 IR 段の責務

Metro と同じく、FIR で生成した hint 関数の body を IR 段で stub 化する transformer が必要:

```kotlin
internal class PreviewLabHintIrBodyFiller(...) : IrElementTransformerVoid() {
    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        if (declaration.origin == Origins.PreviewLabHint) {
            declaration.body = builder.irBlockBody { /* empty */ }
        }
        return super.visitSimpleFunction(declaration)
    }
}
```

ただし `markAsDeprecatedHidden` だけで多くのケース pass する場合もあり、要追加検証
(Metro は `stubExpressionBody()` を入れているので、Compose Preview Lab も入れる方が安全)。

### J.5 Auto-export の扱い

現行 `AutoPreviewExportGenerator` は IR 段で synthetic file (provider 関数 + hint) を作っている。
FIR 移行後は:

- **provider 関数本体** (= `previewLabAutoProvider_<pkg>(): List<CollectedPreview>`) は IR 段で
  作るのが自然 (body に IR expression が必要なため)。
- **hint 関数** は FIR で生成。`HintEntry.targetFqn` に provider 関数の FQN を入れておく。

つまり provider 関数だけ IR で生成 → metadata visible 化、hint は FIR で生成という二段構え。
両 path が同じ marker class id rule で hint を吐けば、downstream は一様に発見できる。

### J.6 Marker class の決定的命名

KLIB IdSignature 衝突回避のためには **module を跨いで marker class id が衝突しない** ことが必須。
property FQN は module 跨ぎでも基本 unique なので、`<sanitizedPropertyFqn>Marker` で十分。
ただし以下の留意点:

- `default` package (root) のとき: `RootDefault<sanitized>Marker` のような prefix を付ける
- auto-export の場合: provider 関数 FQN を sanitize する
- 同 module 内で同じ property fqn が出ないことを assert する (debug)

### J.7 verify が必要なポイント (要追加検証)

1. **`createTopLevelFunction` の `containingFileName` 引数の型と効果**
   FIR plugin DSL のシグネチャは Kotlin version で変動している可能性。`compat-k230` 以上で
   実装する想定。
2. **`getTopLevelClassIds` から返した marker class が実際に classpath 上に metadata として
   出力されるか**
   metadataDeclarationRegistrar 相当の追加処理が要らないか確認。FIR generator が `Synthetic.PluginFile`
   origin で吐けば自動的に metadata に乗るが、KLIB の場合は更に検証必要。
3. **JS IC で marker class の追加が KT-82395 / KT-82989 を踏まないか**
   Metro 0.12.1 で `2.3.21+` が条件と明記されているので、それ未満は IC 無効化を強制。
4. **`markAsDeprecatedHidden` を marker class 自身にも掛ける必要があるか**
   Metro では `ContributionsFirGenerator` の holder class 生成時に
   `markAsDeprecatedHidden(session)` を掛けている (`L244-246`)。同様にすべき。
5. **value parameter name の衝突**
   Metro は `Symbols.Names.contributed`、Compose Preview Lab は `value` を使う想定。
   既存実装の `HINT_VALUE_PARAM_NAME = Name.identifier("value")` を踏襲。
6. **annotation `@PreviewExportHint(fqn=...)` の FIR 段での attach 方法**
   IR 段の `attachHintAnnotation` (`GeneratePreviewExportHint.kt:130-143`) と等価なものを FIR で
   行う必要あり。`buildAnnotation { ... }` + `replaceAnnotations` パターンを使う。
   `ContributionsFirGenerator` の `replaceAnnotationsSafe(...)` が参考になる。

---

## K. 主要ファイル参照早見表

| ファイル                                                                                      | 役割                              |
|---------------------------------------------------------------------------------------------|----------------------------------|
| `compiler/src/main/kotlin/.../fir/generators/ContributionHintFirGenerator.kt`               | FIR hint 生成本体 (本命)         |
| `compiler/src/main/kotlin/.../fir/generators/ContributedInterfaceSupertypeGenerator.kt`     | downstream 発見ロジック           |
| `compiler/src/main/kotlin/.../fir/generators/ContributionsFirGenerator.kt`                  | top-level class 生成のリファレンス |
| `compiler/src/main/kotlin/.../fir/Keys.kt`                                                  | GeneratedDeclarationKey 定義      |
| `compiler/src/main/kotlin/.../fir/MetroFirBuiltIns.kt`                                      | FirExtensionSessionComponent     |
| `compiler/src/main/kotlin/.../fir/MetroFirExtensionRegistrar.kt`                            | FIR 拡張登録のエントリポイント     |
| `compiler/src/main/kotlin/.../fir/ExtensionPredicates.kt`                                   | 述語定義                          |
| `compiler/src/main/kotlin/.../fir/fir.kt:889-902`                                           | `markAsDeprecatedHidden`         |
| `compiler/src/main/kotlin/.../fir/fir.kt:946-950`                                           | `FirClassSymbol.constructType`   |
| `compiler/src/main/kotlin/.../ir/transformers/HintGenerator.kt`                             | 旧 IR-based 実装 (file 名 sanitize 参照元) |
| `compiler/src/main/kotlin/.../ir/transformers/ContributionHintIrTransformer.kt`             | IR 段の body filler              |
| `compiler/src/main/kotlin/.../symbols/Symbols.kt:112-142`                                   | `metroHintsPackage` / `scopeHint` CallableId |
| `compiler/src/main/kotlin/.../util.kt:197`                                                  | `ClassId.scopeHintFunctionName`  |
| `compiler/src/main/kotlin/.../MetroOptions.kt:1457-1485`                                    | Kotlin version gate              |
| `compiler/src/main/kotlin/.../MetroCompilerPluginRegistrar.kt`                              | エントリポイント (compatContext load) |

---

## L. 実装着手手順 (推奨)

1. **`Keys.PreviewLabHint` / `Keys.PreviewLabHintMarker`** を新設 (GeneratedDeclarationKey)。
2. **`PreviewLabFirBuiltIns`** (FirExtensionSessionComponent) 作成。少なくとも `PluginConfig`
   と classIds を保持する。
3. **`PreviewLabHintFirGenerator`** を §J.2 の擬似コードベースで実装。
   - `getTopLevelClassIds()` で marker class id を返す
   - `generateTopLevelClassLikeDeclaration()` で marker class を生成
   - `getTopLevelCallableIds()` / `generateFunctions()` で hint 関数を生成
4. **`PreviewLabFirExtensionRegistrar`** で上記を register。Kotlin 2.3.20+ のみ有効化する gate 追加。
5. **IR 段の body filler** を追加 (FIR 生成の hint origin を見て stub を注入)。
6. **`PreviewListIrBuilder.collectDependencyGetters`** の parameter type check を marker class
   prefix ベースに変更。
7. **既存 `GeneratePreviewExportHint` (IR-based)** は Kotlin <2.3.20 用に残す。`PluginConfig` に
   `useFirHintGeneration: Boolean` を足して切り替える。Gradle plugin 側で Kotlin version を
   detect して flag 設定。
8. **AutoPreviewExportGenerator** を二段化: provider 関数本体は IR で、hint は FIR で。
9. **Integration test 追加**: KLIB platform (jsBrowserTest / wasmJsBrowserTest) で
   cross-module aggregation が動くこと。
10. **JS IC validate** を Gradle plugin 側に追加 (Metro 流の checking)。

---

## 付録: 関連 YouTrack issue

- [KT-82395](https://youtrack.jetbrains.com/issue/KT-82395) JS IC × top-level decl gen — 0.12.1 で 2.3.21+ / 2.4.0-Beta2+ で解消
- [KT-75865](https://youtrack.jetbrains.com/issue/KT-75865) WASM file count
- [KT-74778](https://youtrack.jetbrains.com/issue/KT-74778) synthetic file path (HintGenerator.kt:117 の TODO 参照)
- [KT-80412](https://youtrack.jetbrains.com/issue/KT-80412) LookupSymbols not converted to ProgramSymbols (`$$` prefix 回避)
- CHANGELOG `0.10.0 — Enable FIR hint generation by default on Kotlin 2.3.20-Beta1+`
- CHANGELOG `0.12.1 — Support top-level FIR gen in Kotlin/JS on 2.3.21+ and 2.4.0-Beta2+`
