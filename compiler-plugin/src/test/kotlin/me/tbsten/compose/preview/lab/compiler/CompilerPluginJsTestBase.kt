package me.tbsten.compose.preview.lab.compiler

import com.tschuchort.compiletesting.JsCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinJsCompilation
import com.tschuchort.compiletesting.PluginOption
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

/**
 * JS_IR test helper. The JS counterpart of [CompilerPluginTestBase].
 *
 * Used to verify that KLIB IdSignature collision avoidance and cross-module marker class
 * discovery work correctly under the JS_IR backend.
 *
 * **Differences from the JVM helper**:
 * - `inheritClassPath` only feeds the JS_IR classpath; it does not propagate to
 *   `-libraries` (KLIB). To compensate, the stub sources are added inline to every
 *   compilation.
 * - Dependency-module references are passed via
 *   `kotlincArguments = listOf("-libraries", "<klib1>:<klib2>")`. While
 *   `KotlinJsCompilation.jsArgs()` sets `args.libraries = stdlib`, the call to
 *   `parseCommandLineArguments(kotlincArguments, args)` runs afterwards and can
 *   overwrite it.
 *
 * **References**:
 * - [kctfork (Kotlin Compile Testing fork) docs](https://github.com/ZacSweers/kotlin-compile-testing)
 *   — the underlying compile-test runner; [KotlinJsCompilation](https://github.com/ZacSweers/kotlin-compile-testing/blob/main/core/src/main/kotlin/com/tschuchort/compiletesting/KotlinJsCompilation.kt)
 *   is the entry point exercised by [compileJs].
 * - [Kotlin docs · JS-IR backend](https://kotlinlang.org/docs/js-ir-compiler.html) — the
 *   compilation pipeline this helper drives.
 * - [KLIB ABI design](https://github.com/Kotlin/KEEP/blob/main/proposals/klib/klib-abi-dump-manifest.md)
 *   — IdSignature / klib ABI background; explains why per-module marker classes are
 *   needed for cross-module hint dedup.
 * - [KT-82395](https://youtrack.jetbrains.com/issue/KT-82395) — the JS/Wasm
 *   incremental-compile bug that gates `CompatContext.supportsKlibCrossModuleHint()`
 *   on Kotlin 2.3.21+. Tests exercising cross-module discovery from this helper rely on
 *   that fix.
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

            @RequiresOptIn(message = "Internal")
            @Retention(AnnotationRetention.BINARY)
            annotation class InternalComposePreviewLabApi

            @InternalComposePreviewLabApi
            @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
            @Retention(AnnotationRetention.BINARY)
            annotation class SyntheticPreviewHint

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

            class PreviewExport(private val delegate: Lazy<Sequence<CollectedPreview>>) :
                kotlin.properties.ReadOnlyProperty<Any?, Sequence<CollectedPreview>> {
                override operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): Sequence<CollectedPreview> =
                    delegate.value
            }

            fun collectModulePreviews(): kotlin.properties.ReadOnlyProperty<Any?, Sequence<CollectedPreview>> =
                PreviewExport(lazy { emptySequence() })
            fun collectAllModulePreviews(): kotlin.properties.ReadOnlyProperty<Any?, Sequence<CollectedPreview>> =
                PreviewExport(lazy { emptySequence() })
            fun distinctPreviewsById(previews: List<CollectedPreview>): List<CollectedPreview> =
                previews.distinctBy { it.id }
            fun distinctPreviewsByIdSequence(previews: Sequence<CollectedPreview>): Sequence<CollectedPreview> =
                previews.distinctBy { it.id }
            fun lazyPreviewSequence(vararg factories: () -> CollectedPreview): Sequence<CollectedPreview> =
                Sequence { iterator { for (f in factories) yield(f()) } }
            """,
        ),
    )

    /**
     * Produces one JS_IR `.klib` for a single module.
     *
     * - [extraLibraries] is appended to `-libraries` as additional `.klib` paths (e.g.
     *   the Stage 1 output).
     * - `previewAnnotationStubs` / `collectPreviewsStubs` are inlined on every call, so a
     *   two-stage build ends up shipping the same stub classes in both the dependency's
     *   KLIB and the app's KLIB. This does not collide at the `IdSignature` level: the
     *   plugin's dependency-resolution logic excludes self-emitted declarations via
     *   `IR_EXTERNAL_DECLARATION_STUB`.
     *
     * **References**:
     * - [`KotlinJsCompilation`](https://github.com/ZacSweers/kotlin-compile-testing/blob/main/core/src/main/kotlin/com/tschuchort/compiletesting/KotlinJsCompilation.kt)
     *   — the kctfork JS_IR entry point this method drives.
     * - [Kotlin docs · `-libraries` flag](https://kotlinlang.org/docs/compiler-reference.html#kotlin-js-compiler-options)
     *   — the kotlinc-js argument for KLIB classpath that we override here when
     *   [extraLibraries] is non-empty.
     * - [Kotlin source · K2JsCompilerArgumentsParser](https://github.com/JetBrains/kotlin/blob/master/compiler/cli/cli-js/src/org/jetbrains/kotlin/cli/js/K2JsCompilerArguments.kt)
     *   — `parseCommandLineArguments` runs after `jsArgs()` so our `-libraries`
     *   override takes precedence.
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
 * Returns the produced `.klib` files. `KotlinJsCompilation.outputDir` contains a
 * `<moduleName>.klib` (or an unpacked directory).
 *
 * **References**:
 * - [Kotlin docs · KLIB format](https://kotlinlang.org/docs/native-libraries.html#library-format)
 *   — describes the on-disk layout (.klib archive vs unpacked dir).
 * - [`KotlinJsCompilation.outputDirectory`](https://github.com/ZacSweers/kotlin-compile-testing/blob/main/core/src/main/kotlin/com/tschuchort/compiletesting/KotlinJsCompilation.kt)
 *   — the result-side accessor we walk.
 */
internal fun JsCompilationResult.klibFiles(): List<java.io.File> {
    val outputDir = this.outputDirectory
    if (!outputDir.exists()) return emptyList()
    val klibs = outputDir.walkTopDown().filter { it.isFile && it.extension == "klib" }.toList()
    if (klibs.isNotEmpty()) return klibs
    // `irProduceKlibDir = true` produces an unpacked klib directory whose root is the moduleName.
    return outputDir.listFiles { f -> f.isDirectory }?.toList() ?: emptyList()
}

/**
 * Asserts the JS compilation succeeded; on failure, dumps the messages so the test log
 * shows kotlinc's stderr inline rather than a bare "expected OK" diff.
 *
 * **References**:
 * - [`KotlinCompilation.ExitCode`](https://github.com/ZacSweers/kotlin-compile-testing/blob/main/core/src/main/kotlin/com/tschuchort/compiletesting/KotlinCompilation.kt)
 *   — the kctfork exit-code enum we check against.
 */
internal fun JsCompilationResult.assertOk() {
    if (this.exitCode != KotlinCompilation.ExitCode.OK) {
        error("JS compilation failed with ${this.exitCode}:\n${this.messages}")
    }
}
