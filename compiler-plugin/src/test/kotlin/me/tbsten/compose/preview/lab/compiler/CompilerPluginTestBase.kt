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

    /** collectModulePreviews / collectAllModulePreviews / distinctPreviewsById のスタブ */
    private val collectPreviewsStubs = listOf(
        SourceFile.kotlin(
            "CollectModulePreviews.kt",
            """
            package me.tbsten.compose.preview.lab
            fun collectModulePreviews(): PreviewExport = PreviewExport(lazy { emptyList() })
            fun collectAllModulePreviews(): PreviewExport = PreviewExport(lazy { emptyList() })
            fun distinctPreviewsById(previews: List<CollectedPreview>): List<CollectedPreview> =
                previews.distinctBy { it.id }
            """,
        ),
    )

    fun compile(
        vararg sources: SourceFile,
        pluginRegistrars: List<CompilerPluginRegistrar> = listOf(ComposePreviewLabCompilerPluginRegistrar()),
        pluginOptions: List<PluginOption> = emptyList(),
        extraClasspaths: List<java.io.File> = emptyList(),
    ): JvmCompilationResult = KotlinCompilation().apply {
        this.sources = previewAnnotationStubs + collectPreviewsStubs + sources.toList()
        this.compilerPluginRegistrars = pluginRegistrars
        this.commandLineProcessors = listOf(ComposePreviewLabCommandLineProcessor())
        this.pluginOptions = pluginOptions
        this.inheritClassPath = true
        if (extraClasspaths.isNotEmpty()) {
            this.classpaths = this.classpaths + extraClasspaths
        }
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

    /**
     * `val <propertyName> by collectModulePreviews()` を `test.entry.EntryKt` に持つ補助ソース。
     * テストはこのプロパティを通して IR 変換後の List<CollectedPreview> を検証する。
     */
    fun collectModulePreviewsEntry(propertyName: String = "previews"): SourceFile = SourceFile.kotlin(
        "Entry.kt",
        """
        package test.entry

        import me.tbsten.compose.preview.lab.collectModulePreviews

        val $propertyName by collectModulePreviews()
        """,
    )

    /**
     * `val <propertyName> by collectAllModulePreviews()` を `test.entry.EntryKt` に持つ補助ソース。
     * `collectModulePreviewsEntry` と排他で使うこと（同一ファイルに両方を入れたい場合は手動で組み立てる）。
     */
    fun collectAllModulePreviewsEntry(propertyName: String = "allPreviews"): SourceFile = SourceFile.kotlin(
        "Entry.kt",
        """
        package test.entry

        import me.tbsten.compose.preview.lab.collectAllModulePreviews

        val $propertyName by collectAllModulePreviews()
        """,
    )
}

/**
 * `collectModulePreviewsEntry` / `collectAllModulePreviewsEntry` で生成したプロパティを
 * リフレクションで取得する。テスト側のボイラープレートを抑えるための薄いラッパー。
 */
@Suppress("UNCHECKED_CAST")
internal fun JvmCompilationResult.loadCollectedPreviews(propertyName: String = "previews"): List<Any> {
    val getter = "get" + propertyName.replaceFirstChar { it.uppercase() }
    return classLoader
        .loadClass("test.entry.EntryKt")
        .getMethod(getter)
        .invoke(null) as List<Any>
}
