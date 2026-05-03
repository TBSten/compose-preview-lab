package me.tbsten.compose.preview.lab.compiler

import com.tschuchort.compiletesting.JsCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinJsCompilation
import com.tschuchort.compiletesting.PluginOption
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

/**
 * JS_IR 版のテストヘルパー。`CompilerPluginTestBase` の JS counterpart。
 *
 * KLIB IdSignature 衝突 / cross-module marker class discovery が JS_IR backend で
 * 正しく動くことを検証するテスト用。
 *
 * **JVM 版との差分**:
 * - `inheritClassPath` は JS_IR では classpath だけ参照され、`-libraries` (KLIB) には
 *   伝わらない。代わりにスタブを毎モジュールでインライン source として積む。
 * - 依存モジュール参照は `kotlincArguments = listOf("-libraries", "<klib1>:<klib2>")` で渡す。
 *   `KotlinJsCompilation.jsArgs()` は `args.libraries = stdlib` を設定するが、
 *   `parseCommandLineArguments(kotlincArguments, args)` がその後に走るので上書きできる。
 */
@OptIn(ExperimentalCompilerApi::class)
open class CompilerPluginJsTestBase {

    private val moduleNameCounter = java.util.concurrent.atomic.AtomicInteger(0)

    private val testKotlinVersion: String = System.getProperty("test.kotlin.version") ?: "2.3.21"
    private val testLanguageVersion: String = testKotlinVersion.split(".").take(2).joinToString(".")

    // Mirror `CompilerPluginTestBase.needsCompatibilityFlags`. The plugin / test classes are
    // compiled against the baseline Kotlin (2.3.x), so when the smoke-test runner swaps in an
    // older `kotlin-compiler-embeddable` (e.g. `-Ptest.kotlin=2.1.20`) the FIR
    // `FirIncompatibleClassExpressionChecker` rejects the metadata as "Incompatible classes were
    // found in dependencies". The skip-check flags suppress that. JS-side compilation uses the
    // same `parseCommandLineArguments` path so the flags work identically here.
    private val needsCompatibilityFlags: Boolean = testKotlinVersion.startsWith("2.1")

    private val stdlibJsKlib: java.io.File = System.getProperty("test.kotlin.stdlib.js.klib")
        ?.let { java.io.File(it) }
        ?: error(
            "test.kotlin.stdlib.js.klib system property is not set. " +
                "Set it to the path of kotlin-stdlib-js-<version>.klib via the test task config.",
        )

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

    private val collectPreviewsStubs = listOf(
        SourceFile.kotlin(
            "CollectPreviewsStubs.kt",
            """
            package me.tbsten.compose.preview.lab

            class CollectedPreview(
                val id: String,
                val displayName: String,
                val filePath: String,
                val startLineNumber: Int,
                val endLineNumber: Int,
                val code: String,
                val kdoc: String?,
                val content: () -> Unit,
            )

            class PreviewExport(private val delegate: Lazy<List<CollectedPreview>>) {
                operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): List<CollectedPreview> =
                    delegate.value
            }

            fun collectModulePreviews(): PreviewExport = PreviewExport(lazy { emptyList() })
            fun collectAllModulePreviews(): PreviewExport = PreviewExport(lazy { emptyList() })
            fun distinctPreviewsById(previews: List<CollectedPreview>): List<CollectedPreview> =
                previews.distinctBy { it.id }
            """,
        ),
    )

    /**
     * JS_IR `.klib` 出力を 1 モジュール分作る。
     *
     * - [extraLibraries] は `-libraries` に追加される `.klib` パス (Stage1 出力など)
     * - `previewAnnotationStubs` / `collectPreviewsStubs` は毎呼び出しでインライン化されるため、
     *   2 段ビルド時は依存側 KLIB と app 側 KLIB の両方に同じ stub class が含まれる。これは
     *   `IdSignature` レベルでは衝突しないが、preview lab plugin が依存解決時に自モジュールを
     *   除外するロジック (`IR_EXTERNAL_DECLARATION_STUB`) によって正しく扱われる。
     */
    fun compileJs(
        vararg sources: SourceFile,
        pluginRegistrars: List<CompilerPluginRegistrar> = listOf(ComposePreviewLabCompilerPluginRegistrar()),
        pluginOptions: List<PluginOption> = emptyList(),
        extraLibraries: List<java.io.File> = emptyList(),
        moduleName: String? = null,
    ): JsCompilationResult = KotlinJsCompilation().apply {
        this.sources = previewAnnotationStubs + collectPreviewsStubs + sources.toList()
        this.compilerPluginRegistrars = pluginRegistrars
        this.commandLineProcessors = listOf(ComposePreviewLabCommandLineProcessor())
        this.pluginOptions = pluginOptions
        this.irProduceKlibDir = true
        this.irProduceJs = false
        val resolvedModuleName = moduleName ?: "preview-lab-js-test-${moduleNameCounter.incrementAndGet()}"
        this.moduleName = resolvedModuleName
        this.irModuleName = resolvedModuleName
        this.languageVersion = testLanguageVersion
        this.kotlinStdLibJsJar = stdlibJsKlib

        val extraKotlincArgs = mutableListOf<String>()
        if (extraLibraries.isNotEmpty()) {
            // Override `args.libraries` set inside jsArgs() — kctfork's parseCommandLineArguments
            // call runs after, so this takes precedence.
            //
            // Use `File.pathSeparator` (`:` on Unix, `;` on Windows) to keep the test runnable
            // on Windows CI runners; a hard-coded `:` would treat `C:\path1;C:\path2` as a single
            // malformed entry.
            val combined = (listOf(stdlibJsKlib) + extraLibraries).joinToString(java.io.File.pathSeparator) {
                it.absolutePath
            }
            extraKotlincArgs += listOf("-libraries", combined)
        }
        if (needsCompatibilityFlags) {
            extraKotlincArgs += listOf("-Xskip-prerelease-check", "-Xskip-metadata-version-check")
        }
        if (extraKotlincArgs.isNotEmpty()) {
            this.kotlincArguments = this.kotlincArguments + extraKotlincArgs
        }
    }.compile()

    fun collectModulePreviewsEntry(propertyName: String = "previews"): SourceFile = SourceFile.kotlin(
        "Entry.kt",
        """
        package test.entry

        import me.tbsten.compose.preview.lab.collectModulePreviews

        val $propertyName by collectModulePreviews()
        """,
    )

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
 * 出力 `.klib` ファイルを返す。`KotlinJsCompilation.outputDir` 配下に
 * `<moduleName>.klib` (or unpacked dir) が生成される。
 */
internal fun JsCompilationResult.klibFiles(): List<java.io.File> {
    val outputDir = this.outputDirectory
    if (!outputDir.exists()) return emptyList()
    val klibs = outputDir.walkTopDown().filter { it.isFile && it.extension == "klib" }.toList()
    if (klibs.isNotEmpty()) return klibs
    // `irProduceKlibDir = true` produces an unpacked klib directory whose root is the moduleName.
    return outputDir.listFiles { f -> f.isDirectory }?.toList() ?: emptyList()
}

internal fun JsCompilationResult.assertOk() {
    if (this.exitCode != KotlinCompilation.ExitCode.OK) {
        error("JS compilation failed with ${this.exitCode}:\n${this.messages}")
    }
}
