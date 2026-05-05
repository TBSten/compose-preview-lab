@file:OptIn(
    org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi::class,
)

package me.tbsten.compose.preview.lab.compiler.fir

import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.plugin.createTopLevelFunction
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.constructType
import org.jetbrains.kotlin.fir.types.isMarkedNullable
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Per-declaration hint generator (Metro 風)。 `@Preview` annotation 付き top-level 関数 1 つに
 * つき 1 つの hint stub `previewHint_<sha256(sourceFqn)>(): CollectedPreview` を
 * `me.tbsten.compose.preview.lab.hints` package に emit する。
 *
 * **Generated Kotlin (semantically equivalent), per `@Preview`**:
 *
 * ```kotlin
 * // file: previewHint_<hash>.kt
 * package me.tbsten.compose.preview.lab.hints
 *
 * public fun previewHint_<hash>(): CollectedPreview =
 *     error("Stub! Filled by IR.")
 * ```
 *
 * 戻り値型 / 関数名のみ FIR で declare し、 metadata を含む `CollectedPreview(...)` constructor
 * 呼び出しは IR 側 ([me.tbsten.compose.preview.lab.compiler.ir.PreviewHintIrBodyFillerV2]) で
 * 注入する。 IR pass 後の hint は次のような形になる:
 *
 * ```kotlin
 * public fun previewHint_<hash>(): CollectedPreview = CollectedPreview(
 *     id = "uiLib.button.MyButton",
 *     displayName = "uiLib.button.MyButton",
 *     filePath = "uiLib/src/.../MyButton.kt",
 *     startLineNumber = 5,
 *     endLineNumber = 9,
 *     code = "{ ... }",
 *     kdoc = null,
 *     content = @Composable { uiLib.button.MyButton() },
 * )
 * ```
 *
 * 旧モジュール集約 hint (`previewLabAutoProvider_<hash>(): List<CollectedPreview>`) を
 * per-declaration に分解した形になる。 metadata を annotation で carry する代わりに、
 * `CollectedPreview` の constructor 引数として直接運ぶ設計に倒した (FIR 側で
 * `FirAnnotationCall` を resolved phase で組む煩雑さを回避するため)。
 *
 * # 設計ポイント
 *
 * - **関数名**: `previewHint_<sha256(sourceFqn)>` 。 sourceFqn のみを入力にすることで
 *   incremental compile / 再現可能ビルドを保つ。 同 FQN cross-artifact collision は受容済み
 *   edge case (`.local/redesign-hint-mechanism/参考.md` §3)
 * - **戻り値型**: `CollectedPreview`。 既存 [CollectedPreviewIrBuilder] を IR 側で再利用できる
 * - **Predicate walk のタイミング**: cache loader 内で `predicateBasedProvider` を walk すると
 *   Kotlin 2.3.21 frontend resolution cycle に当たる。 [getTopLevelCallableIds] 内で
 *   遅延評価すること
 * - **Visibility**: `Visibilities.Public` 必須。 cross-module から `referenceFunctions(...)`
 *   で発見されるため
 * - **`ignore = true` の取り扱い**: `@ComposePreviewLabOption(ignore = true)` は FIR phase で
 *   annotation argument を読むのが安定しないため、 ここでは hint emit 時には filter せず、
 *   IR 側 / consumer 側で除外する方針 (TODO: cross-module で ignore preview が露出しない
 *   形にする follow-up が必要)
 *
 * Pattern adapted from Metro's
 * [ContributionHintFirGenerator](https://github.com/ZacSweers/metro/blob/main/compiler/src/main/kotlin/dev/zacsweers/metro/compiler/fir/generators/ContributionHintFirGenerator.kt).
 */
internal class PreviewHintFirGeneratorV2(session: FirSession) : FirDeclarationGenerationExtension(session) {

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
     * このセッションで発見された `@Preview` 関数群と対応する hint 関数の `CallableId`。
     * `getTopLevelCallableIds()` で初めて触られた時点で walk する (cache loader 内で walk
     * しない)。
     */
    private val hintCallableIds: List<CallableId> by lazy { computeHintCallableIds() }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(previewPredicate)
    }

    override fun getTopLevelCallableIds(): Set<CallableId> = hintCallableIds.toSet()

    override fun hasPackage(packageFqName: FqName): Boolean = packageFqName == PreviewLabFirBuiltIns.HINT_PACKAGE_V2

    /**
     * Hint 関数 stub を生成する。
     *
     * `callableId` がこの session で発見された hint の 1 つに該当する場合、 戻り値型
     * `CollectedPreview` で `public` な top-level 関数を 1 つ emit する。
     *
     * **Emitted Kotlin (semantically equivalent)**:
     * ```kotlin
     * public fun previewHint_<hash>(): CollectedPreview
     * ```
     *
     * Body は emit しない (FIR は body を持てない)。 [Keys.PreviewLabHintV2] origin が
     * IR 側 [me.tbsten.compose.preview.lab.compiler.ir.PreviewHintIrBodyFillerV2] への signal で、
     * IR pass で `CollectedPreview(...)` constructor 呼び出しを return する body が埋まる。
     */
    override fun generateFunctions(callableId: CallableId, context: MemberGenerationContext?): List<FirNamedFunctionSymbol> {
        if (callableId.packageName != PreviewLabFirBuiltIns.HINT_PACKAGE_V2) return emptyList()
        if (callableId !in hintCallableIds) return emptyList()

        val collectedPreviewSymbol = session.symbolProvider
            .getClassLikeSymbolByClassId(PreviewLabFirBuiltIns.COLLECTED_PREVIEW_CLASS_ID)
            ?: return emptyList()
        val collectedPreviewType = collectedPreviewSymbol.constructType(emptyArray())

        // 同 package に user-side の `previewHint_<hash>` 同名トップレベル関数があった場合に
        // file facade class が衝突しないよう、 plugin 生成 file 名には `__Hint` suffix を付ける。
        // class 名は `PreviewHint_<hash>__HintKt` になる。
        val fileName = "${callableId.callableName.asString()}__Hint.kt"
        val function = createTopLevelFunction(
            Keys.PreviewLabHintV2,
            callableId,
            collectedPreviewType,
            fileName,
        ) {
            visibility = Visibilities.Public
        }

        return listOf(function.symbol)
    }

    /**
     * Predicate walk + 各 `@Preview` 関数から hint 関数 `CallableId` を計算する。
     *
     * `getTopLevelCallableIds()` 経由で初めて触られた時点で評価される (lazy)。
     */
    private fun computeHintCallableIds(): List<CallableId> {
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
                CallableId(
                    PreviewLabFirBuiltIns.HINT_PACKAGE_V2,
                    Name.identifier("${PreviewLabFirBuiltIns.PreviewHintV2Prefix}$hash"),
                )
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
}
