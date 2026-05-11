@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.feature.previewCollection.ir.collectPreviewsReplacement.buildPreviewSequence

import me.tbsten.compose.preview.lab.compiler.error.PreviewExportNotFoundError
import me.tbsten.compose.preview.lab.compiler.error.throwAsException
import me.tbsten.compose.preview.lab.compiler.utils.classIdOf
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors

/**
 * Builds the `PreviewExport(<lazyExpr>)` constructor call wrapping a
 * `Lazy<Sequence<CollectedPreview>>`.
 *
 * **Sample call → resulting IR**:
 * ```kotlin
 * BuildPreviewExportIr(context).invoke(lazyExpr)
 * // result IR ≡  PreviewExport(lazyExpr)
 * ```
 *
 * The backing field of properties declared as `val x by collectModulePreviews()` /
 * `val x by collectAllModulePreviews()` ends up holding the resulting `PreviewExport`
 * instance; the property's `getValue` then returns the `PreviewExport` itself, so
 * consumers pick `asList()` or `asSequence()` at the use site.
 *
 * Class shares [PreviewSequenceBuildContext] with the rest of `buildPreviewSequence/`
 * so the `PreviewExport` class symbol is resolved exactly once.
 */
internal class BuildPreviewExportIr(private val context: PreviewSequenceBuildContext) {

    private val previewExportClass by lazy {
        context.pluginContext.referenceClass(
            classIdOf("me.tbsten.compose.preview.lab", "PreviewExport"),
        ) ?: PreviewExportNotFoundError().throwAsException()
    }

    private val previewExportType by lazy { previewExportClass.typeWith() }

    operator fun invoke(lazyExpr: IrExpression): IrExpression {
        val ctor = previewExportClass.constructors.first()
        return IrConstructorCallImpl(
            startOffset = SYNTHETIC_OFFSET,
            endOffset = SYNTHETIC_OFFSET,
            type = previewExportType,
            symbol = ctor,
            typeArgumentsCount = 0,
            constructorTypeArgumentsCount = 0,
        ).apply {
            arguments[0] = lazyExpr
        }
    }
}
