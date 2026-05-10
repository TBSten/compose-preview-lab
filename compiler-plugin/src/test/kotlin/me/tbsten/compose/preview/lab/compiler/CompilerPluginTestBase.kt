package me.tbsten.compose.preview.lab.compiler

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.PluginOption
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

@OptIn(ExperimentalCompilerApi::class)
open class CompilerPluginTestBase {

    // Each call to `compile()` without an explicit moduleName gets a unique sequential
    // module name. The auto-provider FQN scheme hashes the Kotlin module name to derive a
    // per-module-unique provider function name, so multi-stage cross-module tests would
    // collide on the same FQN if every stage shared kctfork's default module name.
    private val moduleNameCounter = java.util.concurrent.atomic.AtomicInteger(0)

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

    /** Test stubs of the `@Preview` annotation (both CMP and Android variants). */
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

    /**
     * Stubs of `collectModulePreviews` / `collectAllModulePreviews` / `distinctPreviewsById`.
     *
     * The default scope value is interpolated from
     * [me.tbsten.compose.preview.lab.ComposePreviewLabOption.DefaultCollectScope] so the test
     * stub stays in lockstep with the production sentinel — changing the constant on the
     * annotation side propagates here automatically (and would cause this stub to compile
     * with the new value rather than silently desyncing on `"default"`).
     */
    private val collectPreviewsStubs = listOf(
        SourceFile.kotlin(
            "CollectModulePreviews.kt",
            """
            package me.tbsten.compose.preview.lab
            import kotlin.properties.ReadOnlyProperty
            fun collectModulePreviews(scope: String = "${me.tbsten.compose.preview.lab.ComposePreviewLabOption.DefaultCollectScope}"): ReadOnlyProperty<Any?, Sequence<CollectedPreview>> = PreviewExport(lazy { emptySequence<CollectedPreview>() })
            fun collectAllModulePreviews(scope: String = "${me.tbsten.compose.preview.lab.ComposePreviewLabOption.DefaultCollectScope}"): ReadOnlyProperty<Any?, Sequence<CollectedPreview>> = PreviewExport(lazy { emptySequence<CollectedPreview>() })
            fun distinctPreviewsById(previews: List<CollectedPreview>): List<CollectedPreview> =
                previews.distinctBy { it.id }
            fun distinctPreviewsByIdSequence(previews: Sequence<CollectedPreview>): Sequence<CollectedPreview> =
                previews.distinctBy { it.id }
            fun lazyPreviewSequence(vararg factories: () -> CollectedPreview): Sequence<CollectedPreview> =
                Sequence { iterator { for (f in factories) yield(f()) } }
            """,
        ),
    )

    fun compile(
        vararg sources: SourceFile,
        pluginRegistrars: List<CompilerPluginRegistrar> = listOf(ComposePreviewLabCompilerPluginRegistrar()),
        pluginOptions: List<PluginOption> = emptyList(),
        extraClasspaths: List<java.io.File> = emptyList(),
        moduleName: String? = null,
    ): JvmCompilationResult = KotlinCompilation().apply {
        this.sources = previewAnnotationStubs + collectPreviewsStubs + sources.toList()
        this.compilerPluginRegistrars = pluginRegistrars
        this.commandLineProcessors = listOf(ComposePreviewLabCommandLineProcessor())
        this.pluginOptions = pluginOptions
        this.inheritClassPath = true
        if (extraClasspaths.isNotEmpty()) {
            this.classpaths = this.classpaths + extraClasspaths
        }
        this.moduleName = moduleName ?: "preview-lab-test-${moduleNameCounter.incrementAndGet()}"
        // Use the compiler's own major.minor as languageVersion to avoid
        // FirIncompatibleClassExpressionChecker crashing in Kotlin 2.1.x when
        // languageVersion="2.0" is used (source must not be null).
        this.languageVersion = testLanguageVersion
        // The collectScopes feature is marked @ExperimentalComposePreviewLabApi on the
        // production side. Test fixtures freely write `@ComposePreviewLabOption(collectScopes = ...)`
        // and `collect[All]ModulePreviews(scope = "...")` to exercise the IR pipeline,
        // so opt in unconditionally here rather than peppering every inline source with
        // `@file:OptIn(...)`.
        this.kotlincArguments = this.kotlincArguments + listOf(
            "-opt-in=me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi",
        )
        if (needsCompatibilityFlags) {
            this.kotlincArguments = this.kotlincArguments + listOf(
                "-Xskip-prerelease-check",
                "-Xskip-metadata-version-check",
            )
        }
    }.compile()

    /**
     * Helper source that defines `val <propertyName> by collectModulePreviews()` in
     * `test.entry.EntryKt`. Tests inspect the post-IR-transform `List<CollectedPreview>`
     * via this property.
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
     * Helper source that defines `val <propertyName> by collectAllModulePreviews()` in
     * `test.entry.EntryKt`. Use exclusively with [collectModulePreviewsEntry]; if you
     * need both in the same file, build the source manually.
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
 * Loads the property generated by [CompilerPluginTestBase.collectModulePreviewsEntry] /
 * [CompilerPluginTestBase.collectAllModulePreviewsEntry] via reflection and materializes
 * the inner sequence to a list. A thin wrapper that hides the boilerplate on the test
 * side; tests that need lazy iteration should use [loadCollectedPreviewSequence] directly.
 */
@Suppress("UNCHECKED_CAST")
internal fun JvmCompilationResult.loadCollectedPreviews(propertyName: String = "previews"): List<Any> =
    (loadCollectedPreviewSequence(propertyName).toList() as List<Any?>)
        .filterNotNull()

/**
 * Loads the `Sequence<CollectedPreview>` returned by `val <propertyName> by
 * collect[All]ModulePreviews()` via reflection. Use this when a test needs to verify
 * lazy iteration behavior (e.g. `take(2)` does not invoke the third factory). For the
 * eager `List` shape, [loadCollectedPreviews] above wraps a `toList()` call.
 */
@Suppress("UNCHECKED_CAST")
internal fun JvmCompilationResult.loadCollectedPreviewSequence(propertyName: String = "previews"): Sequence<Any> {
    val getter = "get" + propertyName.replaceFirstChar { it.uppercase() }
    val raw = classLoader
        .loadClass("test.entry.EntryKt")
        .getMethod(getter)
        .invoke(null)
    return raw as? Sequence<Any>
        ?: error(
            "Property `$propertyName` getter returned ${raw?.javaClass?.name ?: "null"}, expected " +
                "`kotlin.sequences.Sequence<CollectedPreview>`. If the runtime API changed, update " +
                "this helper.",
        )
}
