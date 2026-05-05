@file:OptIn(
    UnsafeDuringIrConstructionAPI::class,
    org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI::class,
)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.compat.IrDeclarationOriginCompat
import me.tbsten.compose.preview.lab.compiler.fir.PreviewLabFirBuiltIns
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.platform.jvm.isJvm

/**
 * Per-declaration hint (Metro 風) を **依存モジュール側** から発見する。
 * `me.tbsten.compose.preview.lab.hints` package 配下の `previewHint_*` 関数を全列挙する。
 *
 * **Sample call**:
 * ```
 * val hints = discoverHintsV2(pluginContext, compatContext)
 * // → 依存モジュール側で生成された
 * //   `me.tbsten.compose.preview.lab.hints/previewHint_<hash1>(): CollectedPreview`,
 * //   `me.tbsten.compose.preview.lab.hints/previewHint_<hash2>(): CollectedPreview`, ...
 * //   の IrSimpleFunction list が返る (自モジュール emit 分は filter 済み)
 * ```
 *
 * 自モジュールが emit した hint は scan 対象外にする (filter via `IR_EXTERNAL_DECLARATION_STUB`
 * origin)。 自モジュールの `@Preview` は別経路で `thisModulePreviews` として既に列挙されて
 * いるため、 含めると `distinctPreviewsById` で dedup されるとはいえ無駄な call IR を増やす。
 *
 * # 設計ポイント
 *
 * - **Package 全 walk**: K2 の `pluginContext.referenceFunctions(CallableId)` は specific 名
 *   での lookup なので、 `previewHint_<hash>` のように hash が module ごとに異なる場合は
 *   先に **関数名一覧** を `moduleDescriptor.getPackage(...).memberScope.getFunctionNames()`
 *   で取得する必要がある。 これは `@ObsoleteDescriptorBasedAPI` 扱いだが、 Metro 等の
 *   標準的な plugin で使われているパターン
 * - **Cross-module gate**: KLIB cross-module aggregation は Kotlin 2.3.21+ で機能する。
 *   それ未満では JVM only fallback (file facade で disambiguate) なので、 V1 と同じ条件で
 *   gate する
 * - **filter 条件**: 関数名が `previewHint_` で始まること + 外部 module 由来であること
 *   + 戻り値型が `CollectedPreview` であること (sanity check)
 */
internal fun discoverHintsV2(pluginContext: IrPluginContext, compatContext: CompatContext,): List<IrSimpleFunction> {
    if (!compatContext.supportsKlibCrossModuleHint() && pluginContext.platform?.isJvm() != true) {
        return emptyList()
    }

    val packageView = pluginContext.moduleDescriptor.getPackage(PreviewLabFirBuiltIns.HINT_PACKAGE_V2)
    val functionNames = packageView.memberScope.getFunctionNames()

    val result = mutableListOf<IrSimpleFunction>()
    for (name in functionNames) {
        if (!name.asString().startsWith(PreviewLabFirBuiltIns.PreviewHintV2Prefix)) continue
        val symbols = pluginContext.referenceFunctions(
            CallableId(PreviewLabFirBuiltIns.HINT_PACKAGE_V2, name),
        )
        for (sym in symbols) {
            val fn = sym.owner
            // 自モジュール emit の hint は thisModulePreviews 経由で既に列挙されているため除外。
            // 外部 module から linked された関数は IR_EXTERNAL_DECLARATION_STUB origin で識別。
            if (fn.origin != IrDeclarationOriginCompat.IR_EXTERNAL_DECLARATION_STUB) continue
            // 戻り値型が CollectedPreview であることを sanity check
            if (fn.returnType.classFqName?.asString() != CollectedPreviewFqn) continue
            result.add(fn)
        }
    }
    return result
}

private const val CollectedPreviewFqn = "me.tbsten.compose.preview.lab.CollectedPreview"
