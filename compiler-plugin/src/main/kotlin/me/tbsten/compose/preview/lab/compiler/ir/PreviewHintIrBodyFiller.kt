@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.fir.Keys
import me.tbsten.compose.preview.lab.compiler.fir.PreviewLabFirBuiltIns
import me.tbsten.compose.preview.lab.compiler.fir.buildPreviewHintCanonicalKey
import me.tbsten.compose.preview.lab.compiler.fir.computeHintHash
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.isMarkedNullable
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

/**
 * [me.tbsten.compose.preview.lab.compiler.fir.PreviewHintFirGenerator] が emit した
 * `previewHint(value: PreviewHintMarker_<hash>?): CollectedPreview` stub の body で
 * `CollectedPreview` を返すようにする。 FIR は body を持てないので IR pass で
 * `CollectedPreview(...)` constructor 呼び出しを `irReturn` する形に書き換える。
 *
 * **Hint function**
 *
 * Before (FIR から渡る):
 * ```kotlin
 * public fun previewHint(value: PreviewHintMarker_<hash>?): CollectedPreview  // body == null
 * ```
 *
 * After (本 transformer が書き換え):
 * ```kotlin
 * public fun previewHint(value: PreviewHintMarker_<hash>?): CollectedPreview = CollectedPreview(
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
 * # 設計ポイント
 *
 * - **Hint と `@Preview` の照合**: hint の `value: PreviewHintMarker_<hash>?` 引数の marker
 *   class 短名から hash を取り出し、 IR module 内の `@Preview` 関数の canonical key を
 *   hash した値と突き合わせて元関数を特定する。 FIR generator と IR side で同一の
 *   [computeHintHash] / [buildPreviewHintCanonicalKey] を使うので一意に照合できる
 * - **`CollectedPreview` 構築**: 既存 [CollectedPreviewIrBuilder.buildCollectedPreviewCall] を
 *   そのまま再利用する
 * - **`@ComposePreviewLabOption(ignore = true)` の取り扱い**: hint emit 自体は ignore=true でも
 *   行うため、 IR 側でも `previews` (filter 済み) ではなく moduleFragment 全体の `@Preview`
 *   関数を直接走査して PreviewFunctionInfo を構築する。 そうしないと ignore=true の hint が
 *   orphan (body=null) になり JVM backend assert に当たる。 cross-module で ignore preview
 *   が露出する課題は redesign 完了後の follow-up で対処予定
 */
internal class PreviewHintIrBodyFiller(
    private val pluginContext: IrPluginContext,
    private val compatContext: CompatContext,
    private val previewsByHash: Map<String, PreviewFunctionInfo>,
) : IrElementTransformerVoid() {

    /** Lazily 構築。 既存 builder を再利用して `CollectedPreview(...)` IR を生成する。 */
    private val collectedPreviewBuilder by lazy {
        CollectedPreviewIrBuilder(pluginContext, compatContext)
    }

    /**
     * Hint 関数の body を `CollectedPreview(...)` constructor 呼び出しの `irReturn` に書き換える。
     *
     * `Keys.PreviewLabHint` origin の関数のみを対象にする。 元 `@Preview` 関数は hint の
     * `value: PreviewHintMarker_<hash>?` 引数の marker class 短名から特定する。
     *
     * **Before**:
     * ```kotlin
     * public fun previewHint(value: PreviewHintMarker_a3k9z2x1?): CollectedPreview  // body == null
     * ```
     *
     * **After** (semantically):
     * ```kotlin
     * public fun previewHint(value: PreviewHintMarker_a3k9z2x1?): CollectedPreview = CollectedPreview(
     *     id = "uiLib.button.MyButton", ..., content = @Composable { uiLib.button.MyButton() },
     * )
     * ```
     */
    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        if (declaration.body != null) return super.visitSimpleFunction(declaration)
        val origin = declaration.origin
        when {
            origin is IrDeclarationOrigin.GeneratedByPlugin && origin.pluginKey === Keys.PreviewLabHint -> {
                fillHintBody(declaration)
            }
        }
        return super.visitSimpleFunction(declaration)
    }

    /**
     * Hint 関数の body を `CollectedPreview(...)` constructor 呼び出しの `irReturn` に書き換える。
     *
     * 元 `@Preview` 関数は hint の `value: PreviewHintMarker_<hash>` 引数の marker class 名から
     * hash を取り出して特定する (FIR generator が hash 入りの marker class を生成しているため)。
     */
    private fun fillHintBody(declaration: IrSimpleFunction) {
        val regularParams = declaration.parameters.filter { it.kind == IrParameterKind.Regular }
        if (regularParams.size != 1) return
        val markerFqn = regularParams[0].type.classFqName ?: return
        val markerShortName = markerFqn.shortName().asString()
        if (!markerShortName.startsWith(PreviewLabFirBuiltIns.PreviewHintMarkerPrefix)) return
        val hash = markerShortName.removePrefix(PreviewLabFirBuiltIns.PreviewHintMarkerPrefix)
        val previewInfo = previewsByHash[hash] ?: return

        val builder = DeclarationIrBuilder(pluginContext, declaration.symbol)
        declaration.body = builder.irBlockBody {
            +irReturn(
                collectedPreviewBuilder.buildCollectedPreviewCall(
                    preview = previewInfo,
                    builder = builder,
                    parent = declaration,
                ),
            )
        }
    }
}

/**
 * `@Preview` annotated top-level 関数を moduleFragment から走査し、 canonical key の hash を
 * key とした [PreviewFunctionInfo] map を構築する。
 *
 * `ignore = true` の preview も含めることで、 [PreviewHintFirGenerator] が emit した
 * すべての hint declaration に body を埋められるようにする。 ignore filter は consumer 側
 * で行う想定 (TODO: cross-module で ignore preview が露出しない形にする follow-up)。
 *
 * Canonical key は同名 overload (`fun MyButton()` と `fun MyButton(text: String)`) を
 * 区別するため、 sourceFqn + parameter type FQN を含む形で組み立てる。 FIR generator と
 * 同じロジックを使う必要があるため [buildPreviewHintCanonicalKey] を共有する。
 *
 * **Sample entry**: `"a3k9z2x1" → PreviewFunctionInfo(function = fun MyButton(), id = "uiLib.button.MyButton", ...)`
 *
 * # Collision detection
 *
 * Hint hash は SHA-256 を base-36 8 文字に truncate しているため (約 41 bit、
 * [computeHintHash] 参照)、 distinct な canonical key 同士で hash 衝突する可能性が
 * 理論上ゼロではない (1k preview で ~10⁻⁷)。 衝突した場合 `put` 上書きで `@Preview` が
 * silent に集約結果から欠落するため、 衝突を検知したら [onCollision] callback で
 * 呼び出し側に通知し ERROR 報告する。
 *
 * 衝突判定は **canonical key 同士の比較** で行う (FQN だけだと同名 overload を区別
 * できないため)。 同 canonical key の重複登録 (ignore=true filter 前後の 2 度走査など)
 * は衝突ではないので silent に上書きしてよい。
 */
internal fun buildPreviewByHashMap(
    previews: List<PreviewFunctionInfo>,
    onCollision: (hash: String, existing: PreviewFunctionInfo, conflicting: PreviewFunctionInfo) -> Unit = { _, _, _ -> },
): Map<String, PreviewFunctionInfo> = buildMap {
    val canonicalKeyByHash = mutableMapOf<String, String>()
    for (preview in previews) {
        val sourceFqn = preview.function.kotlinFqName.asString()
        if (sourceFqn.isEmpty()) continue
        val parameterTypeFqns = preview.function.parameterTypeFqnsForHash()
        val canonicalKey = buildPreviewHintCanonicalKey(sourceFqn, parameterTypeFqns)
        val hash = computeHintHash(canonicalKey)
        val existingKey = canonicalKeyByHash[hash]
        if (existingKey != null && existingKey != canonicalKey) {
            // 別 canonical key が同 hash に landed = 真の hash 衝突。 同 canonical key の
            // 重複登録 (同 file 内 ignore=true 含む 2 度走査) はノイズなので silent に上書き。
            val existing = getValue(hash)
            onCollision(hash, existing, preview)
        }
        canonicalKeyByHash[hash] = canonicalKey
        put(hash, preview)
    }
}

/**
 * IR `IrSimpleFunction` の value parameter type を hint canonical key 用の FQN リストに変換する。
 * FIR side [me.tbsten.compose.preview.lab.compiler.fir.PreviewHintFirGenerator] と同じ format で
 * 揃える必要がある (nullable は `?` suffix、 unknown は `?` 単体)。
 */
private fun IrSimpleFunction.parameterTypeFqnsForHash(): List<String> = parameters
    .filter { it.kind == IrParameterKind.Regular }
    .map { param ->
        val type = param.type
        val classFqn = type.classFqName?.asString() ?: "?"
        if (type.isMarkedNullable()) "$classFqn?" else classFqn
    }
