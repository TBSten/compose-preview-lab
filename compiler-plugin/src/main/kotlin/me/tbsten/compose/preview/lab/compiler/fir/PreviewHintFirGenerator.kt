@file:OptIn(
    org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi::class,
)

package me.tbsten.compose.preview.lab.compiler.fir

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.plugin.createTopLevelClass
import org.jetbrains.kotlin.fir.plugin.createTopLevelFunction
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.constructType
import org.jetbrains.kotlin.fir.types.isMarkedNullable
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Per-declaration hint generator (Metro 風)。 `@Preview` annotation 付き top-level 関数 1 つに
 * つき (1) marker interface + (2) hint 関数 を `me.tbsten.compose.preview.lab.hints` package に
 * emit する。
 *
 * **Generated Kotlin (semantically equivalent), per `@Preview`**:
 *
 * ```kotlin
 * // file: previewHint_<hash>__Hint.kt
 * package me.tbsten.compose.preview.lab.hints
 *
 * public interface PreviewHintMarker_<hash>
 *
 * public fun previewHint(value: PreviewHintMarker_<hash>): CollectedPreview =
 *     error("Stub! Filled by IR.")
 * ```
 *
 * 戻り値型 / 関数名のみ FIR で declare し、 metadata を含む `CollectedPreview(...)` constructor
 * 呼び出しは IR 側 ([me.tbsten.compose.preview.lab.compiler.ir.PreviewHintIrBodyFiller]) で
 * 注入する。
 *
 * # 設計ポイント
 *
 * - **Hint 関数名は固定 (`previewHint`)**: cross-module discovery を `referenceFunctions(
 *   CallableId(HINT_PACKAGE, "previewHint"))` で実現するため。 K2 の package 全 walk API は
 *   外部 module の宣言を on-demand load しないため、 fixed-name + `referenceFunctions` の
 *   組み合わせが de facto 標準 (V1 と同じパターン)
 * - **Marker interface for IdSignature 区別**: KLIB linker は `(name, parameterTypes)` で
 *   IdSignature を導出するため、 fixed-name の hint を区別するには parameter type を
 *   per-`@Preview` でユニークにする必要がある。 marker は **interface** で `ABSTRACT`
 *   (Konan 互換 + Compose `$stableprop` 回避)
 * - **Marker / hint 関数名 hash**: `sha256(canonicalKey)` where `canonicalKey =
 *   "<sourceFqn>(<paramTypeFqns>)"`。 同名 overload 区別対応
 * - **Predicate walk のタイミング**: cache loader 内で `predicateBasedProvider` を walk すると
 *   Kotlin 2.3.21 frontend resolution cycle に当たる。 [getTopLevelCallableIds] /
 *   [getTopLevelClassIds] 内で遅延評価する
 * - **Visibility**: `Visibilities.Public` 必須 (cross-module から発見されるため)
 *
 * Pattern adapted from Metro's
 * [ContributionHintFirGenerator](https://github.com/ZacSweers/metro/blob/main/compiler/src/main/kotlin/dev/zacsweers/metro/compiler/fir/generators/ContributionHintFirGenerator.kt)
 * + 旧モジュール集約 hint の fixed-name discovery 方式の hybrid。
 */
internal class PreviewHintFirGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {

    /**
     * `@Preview` 関数を発見するための predicate。 CMP / Android 両方の `@Preview` annotation を
     * 対象にする。
     */
    private val previewPredicate: LookupPredicate = LookupPredicate.create {
        annotated(
            PreviewLabFirBuiltIns.CMP_PREVIEW_ANNOTATION_FQN,
            PreviewLabFirBuiltIns.ANDROID_PREVIEW_ANNOTATION_FQN,
        )
    }

    /**
     * このセッションで発見された `@Preview` 関数群と対応する hint hash。
     * `getTopLevelCallableIds()` / `getTopLevelClassIds()` で初めて触られた時点で walk する
     * (cache loader 内で walk しない)。
     */
    private val hintHashes: List<String> by lazy { computeHintHashes() }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(previewPredicate)
    }

    override fun getTopLevelClassIds(): Set<ClassId> = hintHashes.mapTo(mutableSetOf()) { hash ->
        ClassId(PreviewLabFirBuiltIns.HINT_PACKAGE, Name.identifier("$MarkerClassPrefix$hash"))
    }

    override fun getTopLevelCallableIds(): Set<CallableId> = if (hintHashes.isEmpty()) {
        emptySet()
    } else {
        // hint 関数名は固定 (`previewHint`)。 marker class 引数で IdSignature を区別する。
        setOf(PreviewLabFirBuiltIns.HINT_FUNCTION_CALLABLE_ID)
    }

    override fun hasPackage(packageFqName: FqName): Boolean = packageFqName == PreviewLabFirBuiltIns.HINT_PACKAGE

    /**
     * Marker interface を生成する。
     *
     * **Emitted Kotlin (semantically equivalent), per hash**:
     * ```kotlin
     * package me.tbsten.compose.preview.lab.hints
     * public interface PreviewHintMarker_<hash>
     * ```
     *
     * `INTERFACE` (not `CLASS` / `OBJECT`) で `Modality.ABSTRACT` を明示することで、
     * Compose Compiler の `$stableprop` 合成 (JS / Wasm IC 衝突原因) と Konan の
     * `Expected a class, found interface` (interface に対する FINAL modality 拒否) を回避する。
     */
    override fun generateTopLevelClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>? {
        if (classId.packageFqName != PreviewLabFirBuiltIns.HINT_PACKAGE) return null
        val shortName = classId.shortClassName.asString()
        if (!shortName.startsWith(MarkerClassPrefix)) return null
        val hash = shortName.removePrefix(MarkerClassPrefix)
        if (hash !in hintHashes) return null

        val klass = createTopLevelClass(classId, Keys.PreviewLabHintMarker, ClassKind.INTERFACE) {
            modality = Modality.ABSTRACT
        }
        return klass.symbol
    }

    override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> = emptyList()

    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext,): Set<Name> =
        emptySet()

    /**
     * Hint 関数 stub を生成する。
     *
     * **Emitted Kotlin (semantically equivalent), per hash**:
     * ```kotlin
     * public fun previewHint(value: PreviewHintMarker_<hash>): CollectedPreview
     * ```
     *
     * 関数名は固定 (`previewHint`)、 marker class 引数で IdSignature を per-`@Preview`
     * ユニークにする。 cross-module consumer は `referenceFunctions(fixed-name)` で全 hint
     * を発見できる。
     *
     * Body は emit しない (FIR は body を持てない)。 [Keys.PreviewLabHint] origin が IR 側
     * [me.tbsten.compose.preview.lab.compiler.ir.PreviewHintIrBodyFiller] への signal で、
     * IR pass で `CollectedPreview(...)` constructor 呼び出しを return する body が埋まる。
     */
    override fun generateFunctions(callableId: CallableId, context: MemberGenerationContext?): List<FirNamedFunctionSymbol> {
        if (callableId != PreviewLabFirBuiltIns.HINT_FUNCTION_CALLABLE_ID) return emptyList()
        if (hintHashes.isEmpty()) return emptyList()

        val collectedPreviewSymbol = session.symbolProvider
            .getClassLikeSymbolByClassId(PreviewLabFirBuiltIns.COLLECTED_PREVIEW_CLASS_ID)
            ?: return emptyList()
        val collectedPreviewType = collectedPreviewSymbol.constructType(emptyArray())

        return hintHashes.map { hash ->
            val markerClassId = ClassId(
                PreviewLabFirBuiltIns.HINT_PACKAGE,
                Name.identifier("$MarkerClassPrefix$hash"),
            )
            val markerSymbol = session.symbolProvider
                .getClassLikeSymbolByClassId(markerClassId) as FirClassSymbol<*>
            val fileName = "PreviewHint_$hash.kt"
            val function = createTopLevelFunction(
                Keys.PreviewLabHint,
                callableId,
                collectedPreviewType,
                fileName,
            ) {
                visibility = Visibilities.Public
                // Marker param は IdSignature 区別だけが目的で実値を必要としないため、
                // **nullable** にしてから consumer 側は `null` を渡す形にする。 marker class
                // の instance 生成 (interface なので不可) や object singleton emit を避ける
                // ためのトレード。
                valueParameter(
                    name = Name.identifier("value"),
                    type = markerSymbol.constructType(isMarkedNullable = true),
                )
            }
            function.symbol
        }
    }

    /**
     * Predicate walk + 各 `@Preview` 関数から hint hash を計算する。
     *
     * `getTopLevelClassIds()` / `getTopLevelCallableIds()` 経由で初めて触られた時点で評価される
     * (lazy)。
     */
    private fun computeHintHashes(): List<String> {
        val symbols = session.predicateBasedProvider.getSymbolsByPredicate(previewPredicate)
        return symbols
            .filterIsInstance<FirNamedFunctionSymbol>()
            .filter { it.callableId.classId == null }
            .map { symbol ->
                val callableId = symbol.callableId
                val packageName = callableId.packageName.asString()
                val simpleName = callableId.callableName.asString()
                val sourceFqn = if (packageName.isEmpty()) simpleName else "$packageName.$simpleName"
                val parameterTypeFqns = symbol.parameterTypeFqnsForHash()
                val canonicalKey = buildPreviewHintCanonicalKey(sourceFqn, parameterTypeFqns)
                computeHintHash(canonicalKey)
            }
            .distinct()
    }

    /**
     * `@Preview` 関数の value parameter type を hint canonical key 用の FQN リストに変換する。
     *
     * 同名 overload (`fun MyButton()` と `fun MyButton(text: String)`) を区別するため、
     * 各パラメータの type FQN を含めて hash 入力を作る。 parameter type の resolve には
     * TYPES phase が必要なので [lazyResolveToPhase] で進める。
     *
     * **Format**: 各 type は `<classId>` (nullable なら `?` を suffix)。 unknown は `?` を返す。
     */
    private fun FirNamedFunctionSymbol.parameterTypeFqnsForHash(): List<String> {
        lazyResolveToPhase(FirResolvePhase.TYPES)
        return valueParameterSymbols.map { paramSymbol ->
            val coneType = paramSymbol.resolvedReturnTypeRef.coneType
            val classFqn = coneType.classId?.asFqNameString() ?: "?"
            if (coneType.isMarkedNullable) "$classFqn?" else classFqn
        }
    }

    private companion object {
        const val MarkerClassPrefix = "PreviewHintMarker_"
    }
}
