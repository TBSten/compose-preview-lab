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
 * Per-declaration hint generator。 `@Preview` 1 つにつき (1) marker interface + (2) hint 関数
 * を `me.tbsten.compose.preview.lab.hints` package に emit する。
 *
 * **Generated Kotlin (semantically equivalent), per `@Preview`** (`com.example.app.MyButtonPreview` の場合):
 *
 * ```kotlin
 * // file: PreviewHint_<hash>.kt
 * package me.tbsten.compose.preview.lab.hints
 *
 * public interface PreviewHintMarker_com_example_app_MyButtonPreview_<hash>
 *
 * public fun previewHint(value: PreviewHintMarker_com_example_app_MyButtonPreview_<hash>?): CollectedPreview =
 *     error("Stub! Filled by IR.")
 * ```
 *
 * 戻り値型 / 関数名のみ FIR で declare し、 metadata を含む `CollectedPreview(...)` constructor
 * 呼び出しは IR 側 ([me.tbsten.compose.preview.lab.compiler.ir.PreviewHintIrBodyFiller]) で
 * 注入する。
 *
 * # 設計ポイント
 *
 * ## Hint 関数名は固定 (`previewHint`)
 *
 * cross-module discovery を `referenceFunctions(CallableId(HINT_PACKAGE, "previewHint"))` で
 * 実現するため。 K2 の package 全 walk API は外部 module の宣言を on-demand load しないため、
 * fixed-name + `referenceFunctions` の組み合わせが de facto 標準。
 *
 * ```kotlin
 * // consumer 側 (IR pass)
 * referenceFunctions(CallableId(HINT_PACKAGE, "previewHint"))
 *     .filter { it.owner.isFromExternalModule() }
 *     .forEach { ... } // 各 hint を call
 * ```
 *
 * ## Marker interface for IdSignature 区別
 *
 * KLIB linker は `(name, parameterTypes)` で IdSignature を導出するため、 fixed-name の hint を
 * 区別するには parameter type を `@Preview` ごとにユニークにする必要がある。 marker は
 * **interface** で `ABSTRACT` (Konan 互換 + Compose `$stableprop` 回避)。
 *
 * ## Marker / hint 関数名 hash
 *
 * `sha256(canonicalKey)` where `canonicalKey = "<sourceFqn>(<paramTypeFqns>)"`。 同名 overload も
 * canonical key の signature 部で区別される。 marker 名には sanitized FQN も含めて debuggability
 * を上げる: `PreviewHintMarker_com_example_app_MyButtonPreview_<hash>`。
 *
 * ## Predicate walk のタイミング
 *
 * cache loader 内で `predicateBasedProvider` を walk すると Kotlin 2.3.21 frontend resolution
 * cycle に当たる。 [getTopLevelCallableIds] / [getTopLevelClassIds] 内で遅延評価する。
 *
 * ## Visibility
 *
 * `Visibilities.Public` 必須 (cross-module から発見されるため)。
 *
 * # 参考
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
     * このセッションで発見された `@Preview` 関数 1 つ ↔ marker / hint 1 セットの対応表。
     * `getTopLevelCallableIds()` / `getTopLevelClassIds()` で初めて触られた時点で walk する
     * (cache loader 内で walk しない)。
     */
    private val hintEntries: List<HintEntry> by lazy { computeHintEntries() }

    /** Marker class 短名 (`PreviewHintMarker_<sanitized_fqn>_<hash>`) → hash の reverse lookup. */
    private val hashByMarkerShortName: Map<String, String> by lazy {
        hintEntries.associate { it.markerShortName to it.hash }
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(previewPredicate)
    }

    override fun getTopLevelClassIds(): Set<ClassId> = hintEntries.mapTo(mutableSetOf()) { entry ->
        ClassId(PreviewLabFirBuiltIns.HINT_PACKAGE, Name.identifier(entry.markerShortName))
    }

    override fun getTopLevelCallableIds(): Set<CallableId> = if (hintEntries.isEmpty()) {
        emptySet()
    } else {
        // hint 関数名は固定 (`previewHint`)。 marker class 引数で IdSignature を区別する。
        setOf(PreviewLabFirBuiltIns.HINT_FUNCTION_CALLABLE_ID)
    }

    override fun hasPackage(packageFqName: FqName): Boolean = packageFqName == PreviewLabFirBuiltIns.HINT_PACKAGE

    /**
     * Marker interface を生成する。
     *
     * **Emitted Kotlin (semantically equivalent), per `@Preview`** (`fun com.example.app.MyButtonPreview()` の場合):
     * ```kotlin
     * package me.tbsten.compose.preview.lab.hints
     * public interface PreviewHintMarker_com_example_app_MyButtonPreview_<hash>
     * ```
     *
     * 名前 format は `PreviewHintMarker_<sanitized_fqn>_<hash>`。 sanitized FQN は `.` を `_` に
     * 置換した値で、 IDE / stack trace / KLIB IC log で marker がどの `@Preview` 由来か
     * 一目で分かる。 hash は同名 overload の区別用 (sourceFqn のみだと衝突する)。
     *
     * `INTERFACE` (not `CLASS` / `OBJECT`) で `Modality.ABSTRACT` を明示することで、
     * Compose Compiler の `$stableprop` 合成 (JS / Wasm IC 衝突原因) と Konan の
     * `Expected a class, found interface` (interface に対する FINAL modality 拒否) を回避する。
     */
    override fun generateTopLevelClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>? {
        if (classId.packageFqName != PreviewLabFirBuiltIns.HINT_PACKAGE) return null
        val shortName = classId.shortClassName.asString()
        if (shortName !in hashByMarkerShortName) return null

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
     * public fun previewHint(value: PreviewHintMarker_<sanitized_fqn>_<hash>?): CollectedPreview
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
        if (hintEntries.isEmpty()) return emptyList()

        val collectedPreviewSymbol = session.symbolProvider
            .getClassLikeSymbolByClassId(PreviewLabFirBuiltIns.COLLECTED_PREVIEW_CLASS_ID)
            ?: return emptyList()
        val collectedPreviewType = collectedPreviewSymbol.constructType(emptyArray())

        return hintEntries.map { entry ->
            val markerClassId = ClassId(
                PreviewLabFirBuiltIns.HINT_PACKAGE,
                Name.identifier(entry.markerShortName),
            )
            val markerSymbol = session.symbolProvider
                .getClassLikeSymbolByClassId(markerClassId) as? FirClassSymbol<*>
                ?: error(
                    "Expected marker class ${markerClassId.asString()} to have been generated by " +
                        "generateTopLevelClassLikeDeclaration, but symbol provider returned null",
                )
            val fileName = "PreviewHint_${entry.hash}.kt"
            createTopLevelFunction(
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
            }.symbol
        }
    }

    /**
     * Predicate walk + 各 `@Preview` 関数から hint hash と marker class 短名を計算する。
     *
     * `getTopLevelClassIds()` / `getTopLevelCallableIds()` 経由で初めて触られた時点で評価される
     * (lazy)。
     */
    private fun computeHintEntries(): List<HintEntry> {
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
                val hash = computeHintHash(canonicalKey)
                HintEntry(
                    hash = hash,
                    markerShortName = buildMarkerShortName(sourceFqn, hash),
                )
            }
            .distinctBy { it.markerShortName }
    }

    /**
     * `@Preview` 関数の value parameter type を hint canonical key 用の FQN リストに変換する。
     *
     * 同名 overload (`fun MyButton()` と `fun MyButton(text: String)`) を区別するため、
     * 各パラメータの type FQN を含めて hash 入力を作る。 parameter type の resolve には
     * TYPES phase が必要なので [lazyResolveToPhase] で進める。
     *
     * **Format** (FIR / IR 共通):
     * - `<classId>` (classId が解決できる non-nullable type)
     * - `<classId>?` (classId が解決できる nullable type)
     * - `?` (classId 未解決 = generic type parameter / 解決失敗。 nullability は無視する)
     */
    private fun FirNamedFunctionSymbol.parameterTypeFqnsForHash(): List<String> {
        lazyResolveToPhase(FirResolvePhase.TYPES)
        return valueParameterSymbols.map { paramSymbol ->
            val coneType = paramSymbol.resolvedReturnTypeRef.coneType
            val classFqn = coneType.classId?.asFqNameString() ?: return@map "?"
            if (coneType.isMarkedNullable) "$classFqn?" else classFqn
        }
    }

    /** `@Preview` 1 つに対応する marker / hint 関数のメタ。 */
    private data class HintEntry(
        /** canonical key の sha256 base-36 8 文字 (`computeHintHash` の戻り値)。 */
        val hash: String,
        /** marker interface の短名 `PreviewHintMarker_<sanitized_fqn>_<hash>`。 */
        val markerShortName: String,
    )
}
