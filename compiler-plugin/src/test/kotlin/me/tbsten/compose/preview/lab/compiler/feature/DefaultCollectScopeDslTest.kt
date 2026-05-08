package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.PluginOption
import com.tschuchort.compiletesting.SourceFile
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.isAtLeast
import me.tbsten.compose.preview.lab.compiler.loadCollectedPreviews

/**
 * End-to-end behavior of the new Gradle DSL knob
 * `composePreviewLab.collectPreviews.defaultCollectScope`, threaded through the
 * compiler plugin's `defaultCollectScope` SubpluginOption.
 *
 * Pins down the **library / app isolation primary use case**: a library module sets
 * `defaultCollectScope = "acme_ui"` once in its build script, and every unscoped
 * `@Preview` in that module then lands under `previewHint_acme_ui` rather than
 * `previewHint_default`. A consumer app's `collectAllModulePreviews()` (default scope)
 * cannot see these previews unless it explicitly opts in with
 * `collectAllModulePreviews(scope = "acme_ui")`.
 *
 * **Skipped on Kotlin < 2.3.21**: the per-declaration hint generator only runs on
 * Kotlin 2.3.21+ ([CompatContext.supportsKlibCrossModuleHint]).
 */
class DefaultCollectScopeDslTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        val testKotlinVersion = System.getProperty("test.kotlin.version") ?: "2.3.21"
        val supports = testKotlinVersion.isAtLeast(2, 3, 21)

        val pluginId = "me.tbsten.compose.preview.lab.compiler"
        fun pluginOptions(defaultCollectScope: String) = listOf(
            PluginOption(pluginId, "defaultCollectScope", defaultCollectScope),
        )

        context("library / app isolation") {
            test("library with defaultCollectScope = acme_ui emits previewHint_acme_ui (no annotation needed)")
                .config(enabled = supports) {
                    val libResult = base.compile(
                        SourceFile.kotlin(
                            "UiLib.kt",
                            """
                            package uilib

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun PrimaryButtonPreview() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun SecondaryButtonPreview() {}
                            """,
                        ),
                        pluginOptions = pluginOptions("acme_ui"),
                    )
                    libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    // Each `@Preview` gets its own facade file; collect every emitted
                    // `previewHint_<scope>` overload across all facades and check the
                    // unique scope set is exactly `["acme_ui"]` (no `previewHint_default`
                    // leaked from the per-`@Preview` annotation default).
                    val emittedScopeFunctions = libResult.outputDirectory.previewHintFacadeNames()
                        .flatMap { libResult.classLoader.loadClass(it).declaredMethods.toList() }
                        .map { it.name }
                        .filter { it.startsWith("previewHint_") && '$' !in it }
                        .distinct()
                    emittedScopeFunctions shouldContainExactlyInAnyOrder listOf("previewHint_acme_ui")
                }

            test("consumer app's default-scope collectAllModulePreviews() does NOT see library previews under acme_ui")
                .config(enabled = supports) {
                    val libResult = base.compile(
                        SourceFile.kotlin(
                            "UiLib.kt",
                            """
                            package uilib

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun PrimaryButtonPreview() {}
                            """,
                        ),
                        pluginOptions = pluginOptions("acme_ui"),
                    )
                    libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    // App side has no DSL → defaultCollectScope = "default", and reads
                    // collectAllModulePreviews() (default scope). Should NOT see the lib's
                    // previews because they are under "acme_ui".
                    val appResult = base.compile(
                        SourceFile.kotlin(
                            "App.kt",
                            """
                            package app

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun AppPreview() {}
                            """,
                        ),
                        base.collectAllModulePreviewsEntry("appPreviews"),
                        extraClasspaths = listOf(libResult.outputDirectory),
                    )
                    appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    val collected = appResult.loadCollectedPreviews("appPreviews")
                        .previewIds()
                    assertSoftly {
                        collected shouldContainExactlyInAnyOrder listOf("app.AppPreview")
                        // ↑ NOT including "uilib.PrimaryButtonPreview" — that one is
                        //   pinned to "acme_ui" so this default-scope call cannot see it.
                    }
                }

            test("consumer app's collectAllModulePreviews(scope = acme_ui) DOES see library previews")
                .config(enabled = supports) {
                    val libResult = base.compile(
                        SourceFile.kotlin(
                            "UiLib.kt",
                            """
                            package uilib

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun PrimaryButtonPreview() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun SecondaryButtonPreview() {}
                            """,
                        ),
                        pluginOptions = pluginOptions("acme_ui"),
                    )
                    libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    val appResult = base.compile(
                        SourceFile.kotlin(
                            "App.kt",
                            """
                            package app

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun AppPreview() {}
                            """,
                        ),
                        SourceFile.kotlin(
                            "Entry.kt",
                            """
                            package test.entry

                            import me.tbsten.compose.preview.lab.collectAllModulePreviews

                            val libraryGallery by collectAllModulePreviews(scope = "acme_ui")
                            val defaultGallery by collectAllModulePreviews()
                            """,
                        ),
                        extraClasspaths = listOf(libResult.outputDirectory),
                    )
                    appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    assertSoftly {
                        appResult.loadCollectedPreviews("libraryGallery")
                            .previewIds() shouldContainExactlyInAnyOrder
                            listOf("uilib.PrimaryButtonPreview", "uilib.SecondaryButtonPreview")
                        appResult.loadCollectedPreviews("defaultGallery")
                            .previewIds() shouldContainExactlyInAnyOrder listOf("app.AppPreview")
                    }
                }

            test("per-@Preview override wins over the module's defaultCollectScope")
                .config(enabled = supports) {
                    // Library has defaultCollectScope = "acme_ui", but one preview opts out
                    // explicitly with `collectScopes = ["showcase"]`.
                    val libResult = base.compile(
                        SourceFile.kotlin(
                            "UiLib.kt",
                            """
                            package uilib

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun A() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(collectScopes = ["showcase"])
                            fun B() {}
                            """,
                        ),
                        pluginOptions = pluginOptions("acme_ui"),
                    )
                    libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    val emittedScopeFunctions = libResult.outputDirectory.previewHintFacadeNames()
                        .flatMap { libResult.classLoader.loadClass(it).declaredMethods.toList() }
                        .map { it.name }
                        .filter { it.startsWith("previewHint_") && '$' !in it }
                        .distinct()
                    emittedScopeFunctions shouldContainExactlyInAnyOrder
                        listOf("previewHint_acme_ui", "previewHint_showcase")
                }

            test("per-@Preview DefaultCollectScope sentinel is substituted with the module default")
                .config(enabled = supports) {
                    // `@ComposePreviewLabOption(collectScopes = ["default"])` means
                    // "use the module's configured default" — it should resolve to the
                    // Gradle DSL value, NOT land in a literal "default" bucket.
                    val libResult = base.compile(
                        SourceFile.kotlin(
                            "UiLib.kt",
                            """
                            package uilib

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(
                                collectScopes = [
                                    me.tbsten.compose.preview.lab.ComposePreviewLabOption.DefaultCollectScope
                                ]
                            )
                            fun A() {}
                            """,
                        ),
                        pluginOptions = pluginOptions("acme_ui"),
                    )
                    libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    val emittedScopeFunctions = libResult.outputDirectory.previewHintFacadeNames()
                        .flatMap { libResult.classLoader.loadClass(it).declaredMethods.toList() }
                        .map { it.name }
                        .filter { it.startsWith("previewHint_") && '$' !in it }
                        .distinct()
                    emittedScopeFunctions shouldContainExactlyInAnyOrder listOf("previewHint_acme_ui")
                }
        }

        context("CommandLineProcessor validation") {
            test("invalid defaultCollectScope (`with-hyphen`) is rejected at option-acceptance time") {
                val result = base.compile(
                    SourceFile.kotlin(
                        "Empty.kt",
                        """
                        package self

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun A() {}
                        """,
                    ),
                    pluginOptions = pluginOptions("with-hyphen"),
                )
                // The CommandLineProcessor throws `CliOptionProcessingException` on receipt;
                // kctfork converts that into `COMPILATION_ERROR` (the build aborts at option
                // parsing, before any source file is processed) without a stack trace.
                assertSoftly {
                    result.exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
                    result.messages shouldContain "defaultCollectScope"
                    result.messages shouldContain "with-hyphen"
                }
            }
        }

        context("same-module collect filter respects defaultCollectScope") {
            test("collectModulePreviews() in a module with defaultCollectScope=acme_ui sees that module's unscoped previews")
                .config(enabled = supports) {
                    // Without the per-preview substitution introduced for this test, the
                    // module's `collectModulePreviews()` would resolve to "acme_ui" but the
                    // unannotated previews would stay pinned to ["default"], so the in-module
                    // filter `scope in scopes` would yield empty. This regression test pins
                    // down that the IR generator substitutes both sides consistently.
                    val result = base.compile(
                        SourceFile.kotlin(
                            "Mod.kt",
                            """
                            package self

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun A() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun B() {}
                            """,
                        ),
                        base.collectModulePreviewsEntry("ownPreviews"),
                        pluginOptions = pluginOptions("acme_ui"),
                    )
                    result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    result.loadCollectedPreviews("ownPreviews")
                        .previewIds() shouldContainExactlyInAnyOrder listOf("self.A", "self.B")
                }
        }

        context("IR-side regex check catches `const val` reaching IR with an invalid value") {
            test("collectModulePreviews(scope = INVALID_CONST) is rejected at IR time") {
                // FIR `CollectScopeCallChecker` cannot tell a `const val` reference from a
                // non-`const` `val` reference at analysis time — they both arrive as
                // `FirPropertyAccessExpression`. Const-folding into `IrConst<String>` happens
                // between FIR and IR, so the IR pass is the first place where an invalid
                // const value can be caught. Without the IR-side regex check the synthetic
                // `previewHint_<scope>` lookup would either crash on `Name.identifier(...)`
                // or silently land on an unrelated identifier.
                val result = base.compile(
                    SourceFile.kotlin(
                        "Entry.kt",
                        """
                        package test.entry

                        import me.tbsten.compose.preview.lab.collectModulePreviews

                        private const val INVALID_CONST = "has space"
                        val previews by collectModulePreviews(scope = INVALID_CONST)
                        """,
                    ),
                )
                assertSoftly {
                    result.exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
                    result.messages shouldContain "[ComposePreviewLab]"
                    result.messages shouldContain "has space"
                    result.messages shouldContain "[A-Za-z0-9_]+"
                }
            }
        }
    })

private fun List<Any>.previewIds(): List<String> = map { collectedPreview ->
    val klass = collectedPreview::class
    val idMember = klass.members.find { it.name == "id" }
        ?: error("CollectedPreview-shaped class ${klass.qualifiedName ?: klass.simpleName} missing reflective `id` member")
    idMember.call(collectedPreview) as? String
        ?: error("Reflective `id` returned non-String for ${klass.qualifiedName ?: klass.simpleName}")
}
