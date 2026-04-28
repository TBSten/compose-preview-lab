@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.PluginConfig
import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.compat.getAnnotationCompat
import me.tbsten.compose.preview.lab.compiler.compat.hasAnnotationCompat
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.name.FqName

/**
 * IR generation extension for Compose Preview Lab.
 *
 * Collects every @Preview function in the module and injects them at the
 * `collectModulePreviews()` / `collectAllModulePreviews()` call sites.
 */
class PreviewLabIrGenerationExtension(private val config: PluginConfig) : IrGenerationExtension {

    companion object {
        private val CMP_PREVIEW_FQ = FqName("org.jetbrains.compose.ui.tooling.preview.Preview")
        private val ANDROID_PREVIEW_FQ = FqName("androidx.compose.ui.tooling.preview.Preview")
        private val CPL_OPTION_FQ = FqName("me.tbsten.compose.preview.lab.ComposePreviewLabOption")
    }

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val previews = collectPreviews(moduleFragment)
        val compatContext = CompatContext.load()
        PreviewLabIrBodyFiller(pluginContext, config, previews, compatContext).also {
            moduleFragment.transform(it, null)
        }
    }

    private fun collectPreviews(moduleFragment: IrModuleFragment): List<PreviewFunctionInfo> {
        val result = mutableListOf<PreviewFunctionInfo>()
        for (file in moduleFragment.files) {
            for (decl in file.declarations) {
                if (decl !is IrSimpleFunction) continue
                buildPreviewInfo(decl)?.let { result.add(it) }
            }
        }
        return result.sortedBy { it.displayName }
    }

    private fun buildPreviewInfo(func: IrSimpleFunction): PreviewFunctionInfo? {
        if (!func.hasAnnotationCompat(CMP_PREVIEW_FQ) && !func.hasAnnotationCompat(ANDROID_PREVIEW_FQ)) return null

        val optionAnno = func.getAnnotationCompat(CPL_OPTION_FQ)
        val ignore = (optionAnno?.arguments?.getOrNull(1) as? IrConst)?.value as? Boolean ?: false
        if (ignore) return null

        val packageName = func.file.packageFqName.asString()
        val simpleName = func.name.asString()
        val qualifiedName = if (packageName.isEmpty()) simpleName else "$packageName.$simpleName"

        fun resolve(template: String) = template
            .replace("{{package}}", packageName)
            .replace("{{simpleName}}", simpleName)
            .replace("{{qualifiedName}}", qualifiedName)

        val displayName = resolve(
            (optionAnno?.arguments?.getOrNull(0) as? IrConst)?.value as? String ?: "{{qualifiedName}}",
        )
        val id = resolve(
            (optionAnno?.arguments?.getOrNull(2) as? IrConst)?.value as? String ?: "{{qualifiedName}}",
        )

        val fileEntry = func.file.fileEntry
        val rawPath = fileEntry.name
        val filePath = config.projectRootPath?.let { root ->
            runCatching {
                java.nio.file.Paths.get(root).relativize(java.nio.file.Paths.get(rawPath)).toString()
            }.getOrNull()
        } ?: rawPath
        val startLineNumber = fileEntry.getLineNumber(func.startOffset) + 1
        val endLineNumber = fileEntry.getLineNumber(func.endOffset) + 1

        val (code, kdoc) = extractSourceText(func)

        return PreviewFunctionInfo(func, id, displayName, filePath, startLineNumber, endLineNumber, code, kdoc)
    }
}
