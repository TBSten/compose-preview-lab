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
 * Per-declaration hint を **依存モジュール側** から発見する。
 * `me.tbsten.compose.preview.lab.hints.previewHint` 関数を `pluginContext.referenceFunctions` で
 * fixed-name lookup することで、 全 classpath 上の hint を発見する。
 *
 * **Sample call**:
 * ```kotlin
 * val hints = discoverHints(pluginContext, compatContext)
 * // → 依存モジュール側で生成された
 * //   `previewHint(value: PreviewHintMarker_uilib_button_MyButton_<hash1>?): CollectedPreview`,
 * //   `previewHint(value: PreviewHintMarker_uilib_text_MyText_<hash2>?): CollectedPreview`, ...
 * //   の IrSimpleFunction list が返る (自モジュール emit 分は filter 済み)
 * ```
 *
 * # 設計ポイント
 *
 * ## Fixed name + marker param
 *
 * hint 関数名は固定 (`previewHint`)、 引数の marker class が `@Preview` ごとにユニーク。
 * KLIB IdSignature は `(name, paramTypes)` で導出されるので、 marker が違えば別 IdSignature。
 * `referenceFunctions(fixed-name)` で classpath 全体から全 hint を 1 回の lookup で発見できる。
 *
 * ```kotlin
 * pluginContext.referenceFunctions(CallableId(HINT_PACKAGE, Name.identifier("previewHint")))
 * // → 自モジュール + 依存モジュールの previewHint(...) を全部返す
 * ```
 *
 * ## Cross-module gate
 *
 * hint discovery は呼び出し元 ([PreviewLabIrBodyFiller.replaceCollectPreviewsProperty][me.tbsten.compose.preview.lab.compiler.ir.PreviewLabIrBodyFiller])
 * で [CompatContext.supportsKlibCrossModuleHint] を pre-check して、 不可なら早期 error report
 * で IR transform を中止する。 そのため [discoverHints] が実際に呼ばれる時点では gate 条件は
 * 満たされている前提。
 *
 * ## Filter 条件
 *
 * - [IrDeclarationOriginCompat.IR_EXTERNAL_DECLARATION_STUB] origin (= 外部 module 由来)
 * - marker 引数 1 個 + その class が hint package 内
 * - marker 名が `PreviewHintMarker_` で始まる
 * - 戻り値型が `CollectedPreview`
 *
 * # 参考
 *
 * `referenceFunctions(callableId)` は K2 で deprecation warning が付く API だが、
 * 推奨後継 (`finderForBuiltins` / `finderForSource(fromFile)`) は **builtins / 単一 file の
 * scope 限定** で、 classpath 全体を fixed-name で walk する用途には対応していない。
 * 現時点では既存 API を使い続ける必要がある。 K2 が classpath-wide finder を生やしたら
 * 移行する (follow-up)。
 */
internal fun discoverHints(pluginContext: IrPluginContext, compatContext: CompatContext,): List<IrSimpleFunction> {
    if (!compatContext.supportsKlibCrossModuleHint()) return emptyList()

    // referenceFunctions は K2 で deprecated だが、 推奨後継 (finderForBuiltins / finderForSource)
    // は classpath 全体の fixed-name lookup に対応していないので継続使用する。 詳細は KDoc 参照。
    @Suppress("DEPRECATION")
    val hintSymbols = pluginContext.referenceFunctions(PreviewLabFirBuiltIns.HINT_FUNCTION_CALLABLE_ID)

    return hintSymbols.mapNotNull { hintSymbol ->
        val hintFunction = hintSymbol.owner

        // 自モジュール emit の hint は thisModulePreviews 経由で既に列挙されているため除外。
        if (hintFunction.origin != IrDeclarationOriginCompat.IR_EXTERNAL_DECLARATION_STUB) return@mapNotNull null

        // marker 引数 1 個 + 戻り値が CollectedPreview であることを sanity check。
        val regularParams = hintFunction.parameters.filter { it.kind == IrParameterKind.Regular }
        if (regularParams.size != 1) return@mapNotNull null
        val markerFqn = regularParams[0].type.classFqName ?: return@mapNotNull null
        if (markerFqn.parent() != PreviewLabFirBuiltIns.HINT_PACKAGE) return@mapNotNull null
        if (!markerFqn.shortName().asString().startsWith(PreviewLabFirBuiltIns.PreviewHintMarkerPrefix)) return@mapNotNull null
        if (hintFunction.returnType.classFqName?.asString() != CollectedPreviewFqn) return@mapNotNull null

        hintFunction
    }
}

private const val CollectedPreviewFqn = "me.tbsten.compose.preview.lab.CollectedPreview"
