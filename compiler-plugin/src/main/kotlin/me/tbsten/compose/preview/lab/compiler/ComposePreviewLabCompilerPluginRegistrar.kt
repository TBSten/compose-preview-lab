package me.tbsten.compose.preview.lab.compiler

import me.tbsten.compose.preview.lab.compiler.ir.ComposePreviewLabIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class ComposePreviewLabCompilerPluginRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val previewsListPackage = configuration.get(CompilerConfigKeys.PREVIEWS_LIST_PACKAGE)
            ?: return
        val publicPreviewList = configuration.get(CompilerConfigKeys.PUBLIC_PREVIEW_LIST) ?: false
        val projectRootPath = configuration.get(CompilerConfigKeys.PROJECT_ROOT_PATH)
        val generatePreviewList = configuration.get(CompilerConfigKeys.GENERATE_PREVIEW_LIST) ?: true
        val generatePreviewAllList = configuration.get(CompilerConfigKeys.GENERATE_PREVIEW_ALL_LIST) ?: true

        if (!generatePreviewList) return

        IrGenerationExtension.registerExtension(
            ComposePreviewLabIrGenerationExtension(
                previewsListPackage = previewsListPackage,
                publicPreviewList = publicPreviewList,
                projectRootPath = projectRootPath,
                generatePreviewAllList = generatePreviewAllList,
            ),
        )
    }
}
