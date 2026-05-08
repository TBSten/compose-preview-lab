package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.isAtLeast
import me.tbsten.compose.preview.lab.compiler.loadCollectedPreviews

/**
 * End-to-end behaviour of `@ComposePreviewLabOption(collectScope = ["..."])` and
 * `collect[All]ModulePreviews(scope = "...")`.
 *
 * Covers the happy paths:
 *
 * - resolution: a preview without the annotation lands in `"default"`; an explicit
 *   `collectScope = ["design"]` lands under `"design"`.
 * - emission: every emitted hint function is named `previewHint_<scope>` so cross-module
 *   discovery can filter by scope at lookup time.
 * - cross-module filter: an app that calls `collectAllModulePreviews(scope = "design")` only
 *   sees previews from dependency modules whose `collectScope` matches.
 * - same-module filter: `collectModulePreviews(scope = "buttons")` filters this module's own
 *   previews by their `collectScope` annotation.
 *
 * **Skipped on Kotlin < 2.3.21**: the per-declaration hint generator only runs on
 * Kotlin 2.3.21+ ([CompatContext.supportsKlibCrossModuleHint]).
 */
class PreviewHintScopeTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        val testKotlinVersion = System.getProperty("test.kotlin.version") ?: "2.3.21"
        val supports = testKotlinVersion.isAtLeast(2, 3, 21)

        context("default scope (no annotation)") {
            test("preview without @ComposePreviewLabOption emits previewHint_default")
                .config(enabled = supports) {
                    val result = base.compile(
                        SourceFile.kotlin(
                            "Mod.kt",
                            """
                            package self

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun Plain() {}
                            """,
                        ),
                    )
                    result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    val facadeName = result.outputDirectory.previewHintFacadeNames().single()
                    val facade = result.classLoader.loadClass(facadeName)
                    facade.declaredMethods.any { it.name == "previewHint_default" } shouldBe true
                }

            test("@ComposePreviewLabOption() (no collectScope override) still uses default")
                .config(enabled = supports) {
                    val result = base.compile(
                        SourceFile.kotlin(
                            "Mod.kt",
                            """
                            package self

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(displayName = "X")
                            fun WithDisplayName() {}
                            """,
                        ),
                    )
                    result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    val facadeName = result.outputDirectory.previewHintFacadeNames().single()
                    val facade = result.classLoader.loadClass(facadeName)
                    facade.declaredMethods.any { it.name == "previewHint_default" } shouldBe true
                }
        }

        context("explicit scope") {
            test("@ComposePreviewLabOption(collectScope = \"design\") emits previewHint_design")
                .config(enabled = supports) {
                    val result = base.compile(
                        SourceFile.kotlin(
                            "Mod.kt",
                            """
                            package self

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(collectScope = ["design"])
                            fun Themed() {}
                            """,
                        ),
                    )
                    result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    val facadeName = result.outputDirectory.previewHintFacadeNames().single()
                    val facade = result.classLoader.loadClass(facadeName)
                    facade.declaredMethods.any { it.name == "previewHint_design" } shouldBe true
                }

            test("multiple scopes coexist: each preview emits its own previewHint_<scope>")
                .config(enabled = supports) {
                    val result = base.compile(
                        SourceFile.kotlin(
                            "Mod.kt",
                            """
                            package self

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun A() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(collectScope = ["buttons"])
                            fun B() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(collectScope = ["layouts"])
                            fun C() {}
                            """,
                        ),
                    )
                    result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    val emittedHintNames = result.outputDirectory
                        .previewHintFacadeNames()
                        .flatMap { fqn ->
                            result.classLoader.loadClass(fqn).declaredMethods.map { it.name }
                        }
                        // Synthetic Kotlin lambda methods land alongside the `previewHint_*`
                        // ones (e.g. `previewHint_default$lambda$0`); filtering on `$` keeps
                        // just the public API entry points.
                        .filter { it.startsWith("previewHint_") && '$' !in it }
                    emittedHintNames shouldContainExactlyInAnyOrder
                        listOf("previewHint_default", "previewHint_buttons", "previewHint_layouts")
                }
        }

        context("same-module collect filter") {
            test("collectModulePreviews(scope = \"buttons\") returns only matching previews")
                .config(enabled = supports) {
                    val result = base.compile(
                        SourceFile.kotlin(
                            "Mod.kt",
                            """
                            package self

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun Plain() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(collectScope = ["buttons"])
                            fun PrimaryButton() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(collectScope = ["buttons"])
                            fun SecondaryButton() {}
                            """,
                        ),
                        SourceFile.kotlin(
                            "Entry.kt",
                            """
                            package test.entry

                            import me.tbsten.compose.preview.lab.collectModulePreviews

                            val buttons by collectModulePreviews(scope = "buttons")
                            """,
                        ),
                    )
                    result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    result
                        .loadCollectedPreviews(propertyName = "buttons")
                        .previewIds() shouldContainExactlyInAnyOrder listOf("self.PrimaryButton", "self.SecondaryButton")
                }

            test("collectModulePreviews() (omitted scope) only sees default-scope previews")
                .config(enabled = supports) {
                    val result = base.compile(
                        SourceFile.kotlin(
                            "Mod.kt",
                            """
                            package self

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun Plain() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(collectScope = ["buttons"])
                            fun PrimaryButton() {}
                            """,
                        ),
                        base.collectModulePreviewsEntry(),
                    )
                    result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    result
                        .loadCollectedPreviews(propertyName = "previews")
                        .previewIds() shouldContainExactlyInAnyOrder listOf("self.Plain")
                }

            test("collectModulePreviews(scope = \"empty\") returns an empty list when no preview matches")
                .config(enabled = supports) {
                    val result = base.compile(
                        SourceFile.kotlin(
                            "Mod.kt",
                            """
                            package self

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(collectScope = ["buttons"])
                            fun PrimaryButton() {}
                            """,
                        ),
                        SourceFile.kotlin(
                            "Entry.kt",
                            """
                            package test.entry

                            import me.tbsten.compose.preview.lab.collectModulePreviews

                            val nothing by collectModulePreviews(scope = "empty")
                            """,
                        ),
                    )
                    result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    result.loadCollectedPreviews(propertyName = "nothing").shouldBeEmpty()
                }
        }

        context("cross-module collect filter") {
            test("collectAllModulePreviews(scope = \"design\") only sees matching previews from a dep")
                .config(enabled = supports) {
                    val libResult = base.compile(
                        SourceFile.kotlin(
                            "UiLib.kt",
                            """
                            package uilib

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun Plain() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(collectScope = ["design"])
                            fun ThemeShowcase() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(collectScope = ["design"])
                            fun ColorPalette() {}
                            """,
                        ),
                    )
                    libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    val appCompileResult = base.compile(
                        SourceFile.kotlin(
                            "App.kt",
                            """
                            package app
                            """,
                        ),
                        SourceFile.kotlin(
                            "Entry.kt",
                            """
                            package test.entry

                            import me.tbsten.compose.preview.lab.collectAllModulePreviews

                            val designOnly by collectAllModulePreviews(scope = "design")
                            """,
                        ),
                        extraClasspaths = listOf(libResult.outputDirectory),
                    )
                    appCompileResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    appCompileResult
                        .loadCollectedPreviews(propertyName = "designOnly")
                        .previewIds() shouldContainExactlyInAnyOrder listOf("uilib.ThemeShowcase", "uilib.ColorPalette")
                }

            test("scope buckets are independent: requesting one scope ignores another scope's previews")
                .config(enabled = supports) {
                    val libResult = base.compile(
                        SourceFile.kotlin(
                            "UiLib.kt",
                            """
                            package uilib

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(collectScope = ["design"])
                            fun A() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(collectScope = ["feature"])
                            fun B() {}
                            """,
                        ),
                    )
                    libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    val appCompileResult = base.compile(
                        SourceFile.kotlin(
                            "App.kt",
                            """
                            package app
                            """,
                        ),
                        SourceFile.kotlin(
                            "Entry.kt",
                            """
                            package test.entry

                            import me.tbsten.compose.preview.lab.collectAllModulePreviews

                            val featureOnly by collectAllModulePreviews(scope = "feature")
                            """,
                        ),
                        extraClasspaths = listOf(libResult.outputDirectory),
                    )
                    appCompileResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    appCompileResult
                        .loadCollectedPreviews(propertyName = "featureOnly")
                        .previewIds() shouldContainExactlyInAnyOrder listOf("uilib.B")
                }

            test("two scope-bucketed properties on the same app side return independent lists")
                .config(enabled = supports) {
                    val libResult = base.compile(
                        SourceFile.kotlin(
                            "UiLib.kt",
                            """
                            package uilib

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(collectScope = ["design"])
                            fun DesignA() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(collectScope = ["feature"])
                            fun FeatureA() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun PlainOne() {}
                            """,
                        ),
                    )
                    libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    val appCompileResult = base.compile(
                        SourceFile.kotlin(
                            "Entry.kt",
                            """
                            package test.entry

                            import me.tbsten.compose.preview.lab.collectAllModulePreviews

                            val design by collectAllModulePreviews(scope = "design")
                            val feature by collectAllModulePreviews(scope = "feature")
                            val defaultOne by collectAllModulePreviews()
                            """,
                        ),
                        extraClasspaths = listOf(libResult.outputDirectory),
                    )
                    appCompileResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    assertSoftly {
                        appCompileResult.loadCollectedPreviews(propertyName = "design")
                            .previewIds() shouldContainExactlyInAnyOrder listOf("uilib.DesignA")
                        appCompileResult.loadCollectedPreviews(propertyName = "feature")
                            .previewIds() shouldContainExactlyInAnyOrder listOf("uilib.FeatureA")
                        appCompileResult.loadCollectedPreviews(propertyName = "defaultOne")
                            .previewIds() shouldContainExactlyInAnyOrder listOf("uilib.PlainOne")
                    }
                }
        }
        context("multi-scope (one preview, multiple buckets)") {
            test("@ComposePreviewLabOption(collectScope = [\"design\", \"showcase\"]) lands the same preview in both buckets")
                .config(enabled = supports) {
                    val libResult = base.compile(
                        SourceFile.kotlin(
                            "UiLib.kt",
                            """
                            package uilib

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(
                                collectScope = ["design", "showcase"]
                            )
                            fun Shared() {}
                            """,
                        ),
                    )
                    libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    // Two `previewHint_<scope>` overloads on the same facade — one marker,
                    // two functions.
                    val facade = libResult.classLoader.loadClass(
                        libResult.outputDirectory.previewHintFacadeNames().single(),
                    )
                    val emittedScopeFunctions = facade.declaredMethods
                        .map { it.name }
                        .filter { it.startsWith("previewHint_") && '$' !in it }
                    emittedScopeFunctions shouldContainExactlyInAnyOrder
                        listOf("previewHint_design", "previewHint_showcase")

                    val appCompileResult = base.compile(
                        SourceFile.kotlin(
                            "Entry.kt",
                            """
                            package test.entry

                            import me.tbsten.compose.preview.lab.collectAllModulePreviews

                            val design by collectAllModulePreviews(scope = "design")
                            val showcase by collectAllModulePreviews(scope = "showcase")
                            """,
                        ),
                        extraClasspaths = listOf(libResult.outputDirectory),
                    )
                    appCompileResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    assertSoftly {
                        appCompileResult.loadCollectedPreviews(propertyName = "design")
                            .previewIds() shouldContainExactlyInAnyOrder listOf("uilib.Shared")
                        appCompileResult.loadCollectedPreviews(propertyName = "showcase")
                            .previewIds() shouldContainExactlyInAnyOrder listOf("uilib.Shared")
                    }
                }
        }
    })

private fun List<Any>.previewIds(): List<String> = map { collectedPreview ->
    collectedPreview::class.members.find { it.name == "id" }!!.call(collectedPreview) as String
}
