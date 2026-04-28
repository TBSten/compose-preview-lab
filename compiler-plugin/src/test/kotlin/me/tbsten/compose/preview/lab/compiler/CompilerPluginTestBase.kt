package me.tbsten.compose.preview.lab.compiler

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.PluginOption
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

@OptIn(ExperimentalCompilerApi::class)
open class CompilerPluginTestBase {

    // Derive major.minor from the actual compiler version so that kctfork uses the
    // correct language level. In Kotlin 2.1.x, mixing languageVersion="2.0" with a
    // 2.1.x compiler causes FirIncompatibleClassExpressionChecker to crash with
    // "source must not be null" on synthetic FIR elements.
    private val testKotlinVersion: String = System.getProperty("test.kotlin.version") ?: "2.3.21"
    private val testLanguageVersion: String = testKotlinVersion.split(".").take(2).joinToString(".")

    // The plugin / test classes are compiled against the baseline Kotlin (2.3.x). When the
    // test compiler is older (e.g. 2.1.20), kotlin-compiler-embeddable's
    // FirIncompatibleClassExpressionChecker rejects them with "Incompatible classes were
    // found in dependencies". These flags suppress the metadata / prerelease checks.
    // Only apply when the test compiler is older than the baseline.
    private val needsCompatibilityFlags: Boolean = testKotlinVersion.startsWith("2.1")

    /** テスト用の @Preview アノテーションスタブ (CMP / Android 両方) */
    private val previewAnnotationStubs = listOf(
        SourceFile.kotlin(
            "CmpPreview.kt",
            """
            package org.jetbrains.compose.ui.tooling.preview
            annotation class Preview
            """,
        ),
        SourceFile.kotlin(
            "AndroidPreview.kt",
            """
            package androidx.compose.ui.tooling.preview
            annotation class Preview
            """,
        ),
    )

    /** collectModulePreviews / collectAllModulePreviews のスタブ */
    private val collectPreviewsStubs = listOf(
        SourceFile.kotlin(
            "CollectModulePreviews.kt",
            """
            package me.tbsten.compose.preview.lab
            fun collectModulePreviews(): Lazy<List<CollectedPreview>> = lazy { emptyList() }
            fun collectAllModulePreviews(): Lazy<List<CollectedPreview>> = lazy { emptyList() }
            """,
        ),
    )

    fun compile(
        vararg sources: SourceFile,
        pluginRegistrars: List<CompilerPluginRegistrar> = listOf(ComposePreviewLabCompilerPluginRegistrar()),
        pluginOptions: List<PluginOption> = defaultPluginOptions(),
    ): JvmCompilationResult {
        val pkg = pluginOptions.firstOrNull { it.optionName == "previewsListPackage" }?.optionValue ?: "test.generated"
        val genList = pluginOptions.firstOrNull { it.optionName == "generatePreviewList" }?.optionValue != "false"
        val genAllList = pluginOptions.firstOrNull { it.optionName == "generatePreviewAllList" }?.optionValue != "false"
        val previewListStubs = previewListSourceStubs(pkg, genList, genAllList)

        return KotlinCompilation().apply {
            this.sources = previewAnnotationStubs + collectPreviewsStubs + previewListStubs + sources.toList()
            this.compilerPluginRegistrars = pluginRegistrars
            this.commandLineProcessors = listOf(ComposePreviewLabCommandLineProcessor())
            this.pluginOptions = pluginOptions
            this.inheritClassPath = true
            // Use the compiler's own major.minor as languageVersion to avoid
            // FirIncompatibleClassExpressionChecker crashing in Kotlin 2.1.x when
            // languageVersion="2.0" is used (source must not be null).
            this.languageVersion = testLanguageVersion
            if (needsCompatibilityFlags) {
                this.kotlincArguments = this.kotlincArguments + listOf(
                    "-Xskip-prerelease-check",
                    "-Xskip-metadata-version-check",
                )
            }
        }.compile()
    }

    /**
     * Gradle plugin が生成する PreviewList / PreviewAllList スタブソースを模倣する。
     * FIR 宣言生成を廃止し、ソースファイル生成に移行したため、
     * テスト時にもスタブを明示的に渡す必要がある。
     */
    private fun previewListSourceStubs(pkg: String, generateList: Boolean, generateAllList: Boolean,): List<SourceFile> =
        buildList {
            if (generateList) {
                add(
                    SourceFile.kotlin(
                        "PreviewList.kt",
                        """
                    package $pkg
                    import me.tbsten.compose.preview.lab.CollectedPreview
                    object PreviewList : List<CollectedPreview> by emptyList()
                    """,
                    ),
                )
            }
            if (generateAllList) {
                add(
                    SourceFile.kotlin(
                        "PreviewAllList.kt",
                        """
                    package $pkg
                    import me.tbsten.compose.preview.lab.CollectedPreview
                    object PreviewAllList : List<CollectedPreview> by emptyList()
                    """,
                    ),
                )
                val propName = "__${pkg.replace('.', '_')}__previewsForAggregateAll"
                add(
                    SourceFile.kotlin(
                        "AggregatePreviewProperty.kt",
                        """
                    package me.tbsten.compose.preview.lab.generated
                    import me.tbsten.compose.preview.lab.CollectedPreview
                    val $propName: List<CollectedPreview> = emptyList()
                    """,
                    ),
                )
            }
        }

    fun defaultPluginOptions(
        previewsListPackage: String = "test.generated",
        publicPreviewList: Boolean = false,
        generatePreviewList: Boolean = true,
        generatePreviewAllList: Boolean = true,
    ): List<PluginOption> {
        val pluginId = ComposePreviewLabCommandLineProcessor.PluginId
        return listOf(
            PluginOption(pluginId, "previewsListPackage", previewsListPackage),
            PluginOption(pluginId, "publicPreviewList", publicPreviewList.toString()),
            PluginOption(pluginId, "generatePreviewList", generatePreviewList.toString()),
            PluginOption(pluginId, "generatePreviewAllList", generatePreviewAllList.toString()),
        )
    }
}
