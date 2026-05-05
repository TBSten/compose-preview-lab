@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.compat.IrDeclarationOriginCompat
import me.tbsten.compose.preview.lab.compiler.fir.PreviewLabFirBuiltIns
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classFqName

/**
 * Per-declaration hint (Metro 風) を **依存モジュール側** から発見する。
 * `me.tbsten.compose.preview.lab.hints.previewHint` 関数を `pluginContext.referenceFunctions` で
 * fixed-name lookup することで、 全 classpath 上の hint を発見する。
 *
 * **Sample call**:
 * ```
 * val hints = discoverHints(pluginContext, compatContext)
 * // → 依存モジュール側で生成された
 * //   `previewHint(value: PreviewHintMarker_<hash1>): CollectedPreview`,
 * //   `previewHint(value: PreviewHintMarker_<hash2>): CollectedPreview`, ...
 * //   の IrSimpleFunction list が返る (自モジュール emit 分は filter 済み)
 * ```
 *
 * # 設計ポイント
 *
 * - **Fixed name + marker param**: hint 関数名は固定 (`previewHint`)、 引数の marker class が
 *   per-`@Preview` ユニーク。 KLIB IdSignature は `(name, paramTypes)` で導出されるので、
 *   marker が違えば別 IdSignature。 `referenceFunctions(fixed-name)` で classpath 全体から
 *   全 hint を 1 回の lookup で発見できる (V1 の `previewLabExport` 同様の方式)
 * - **Cross-module gate**: hint discovery は呼び出し元 (`PreviewLabIrBodyFiller.replaceCollectPreviewsProperty`)
 *   で `supportsKlibCrossModuleHint()` を pre-check して、 不可なら早期 error report で IR
 *   transform を中止する。 そのため discoverHints が実際に呼ばれる時点では gate 条件は
 *   満たされている前提
 * - **filter 条件**: `IR_EXTERNAL_DECLARATION_STUB` origin (= 外部 module 由来)、 marker 引数
 *   が hint package 内、 marker 名が `PreviewHintMarker_` で始まる、 戻り値型が
 *   `CollectedPreview`
 */
internal fun discoverHints(pluginContext: IrPluginContext, compatContext: CompatContext,): List<IrSimpleFunction> {
    if (!compatContext.supportsKlibCrossModuleHint()) return emptyList()

    @Suppress("DEPRECATION")
    val hintSymbols = pluginContext.referenceFunctions(PreviewLabFirBuiltIns.HINT_FUNCTION_CALLABLE_ID)

    return hintSymbols.mapNotNull { hintSymbol ->
        val fn = hintSymbol.owner

        // 自モジュール emit の hint は thisModulePreviews 経由で既に列挙されているため除外。
        if (fn.origin != IrDeclarationOriginCompat.IR_EXTERNAL_DECLARATION_STUB) return@mapNotNull null

        // marker 引数 1 個 + 戻り値が CollectedPreview であることを sanity check。
        val regularParams = fn.parameters.filter { it.kind == IrParameterKind.Regular }
        if (regularParams.size != 1) return@mapNotNull null
        val markerFqn = regularParams[0].type.classFqName ?: return@mapNotNull null
        if (markerFqn.parent() != PreviewLabFirBuiltIns.HINT_PACKAGE) return@mapNotNull null
        if (!markerFqn.shortName().asString().startsWith(MarkerClassPrefix)) return@mapNotNull null
        if (fn.returnType.classFqName?.asString() != CollectedPreviewFqn) return@mapNotNull null

        fn
    }
}

// canonical な定義は `PreviewLabFirBuiltIns.PreviewHintMarkerPrefix`。
private val MarkerClassPrefix = PreviewLabFirBuiltIns.PreviewHintMarkerPrefix
private const val CollectedPreviewFqn = "me.tbsten.compose.preview.lab.CollectedPreview"
