package me.tbsten.compose.preview.lab.compiler.feature.previewCollection.ir.collectPreviewsReplacement

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.utils.previewHintFacadeFiles
import me.tbsten.compose.preview.lab.compiler.utils.previewHintMarkerFiles
import me.tbsten.compose.preview.lab.compiler.isAtLeast
import me.tbsten.compose.preview.lab.compiler.loadCollectedPreviews

/**
 * Cross-module behaviour of `@ComposePreviewLabOption(ignore = true)`.
 *
 * `PreviewHintFirGenerator` reads the option in FIR and skips hint emission for ignored
 * previews. This test verifies two angles:
 *
 * - **Discovery**: `collectAllModulePreviews()` on the app side never sees an ignored
 *   preview from a dependency module.
 * - **Emission**: the ignored preview's marker interface and `previewHint` function are
 *   absent from the lib's compiled output (the filter runs *before* emission, not on the
 *   consumer side).
 *
 * **Skipped on Kotlin < 2.3.21**: the per-declaration hint generator only runs on
 * Kotlin 2.3.21+ ([CompatContext.supportsKlibCrossModuleHint]).
 */
class PreviewHintIgnoreCrossModuleTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        val testKotlinVersion = System.getProperty("test.kotlin.version") ?: "2.3.21"
        val supports = testKotlinVersion.isAtLeast(2, 3, 20)

        context("cross-module ignore filter (the core fix)") {
            test("ignore=true preview from a dependency module is absent from collectAllModulePreviews()")
                .config(enabled = supports) {
                    val libResult = base.compile(
                        SourceFile.kotlin(
                            "UiLib.kt",
                            """
                            package uilib

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(ignore = true)
                            fun Hidden() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun Visible() {}
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
                        base.collectAllModulePreviewsEntry("allPreviews"),
                        extraClasspaths = listOf(libResult.outputDirectory),
                    )
                    appCompileResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    val ids = appCompileResult.loadCollectedPreviews(propertyName = "allPreviews").previewIds()
                    ids shouldContainExactlyInAnyOrder listOf("uilib.Visible")
                }

            test("ignore=true preview emits no previewHint overload into the lib classpath")
                .config(enabled = supports) {
                    val libResult = base.compile(
                        SourceFile.kotlin(
                            "UiLib.kt",
                            """
                            package uilib

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(ignore = true)
                            fun Hidden() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun Visible() {}
                            """,
                        ),
                    )
                    libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    // Exactly one PreviewHint_<hash>.class facade is emitted (for `Visible`),
                    // since `Hidden` is filtered out before FIR generation.
                    val hintFacades = libResult.outputDirectory.previewHintFacadeFiles()
                    hintFacades.size shouldBe 1
                }

            test("ignore=true preview emits no marker interface into the lib classpath")
                .config(enabled = supports) {
                    val libResult = base.compile(
                        SourceFile.kotlin(
                            "UiLib.kt",
                            """
                            package uilib

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(ignore = true)
                            fun Hidden() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun Visible() {}
                            """,
                        ),
                    )
                    libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    val markers = libResult.outputDirectory.previewHintMarkerFiles()
                    assertSoftly {
                        markers.size shouldBe 1
                        // The marker name embeds the sanitized FQN; the `Hidden` one would
                        // contain "_Hidden_" if it were emitted.
                        markers.none { it.nameWithoutExtension.contains("_Hidden_") } shouldBe true
                        markers.singleOrNull()?.nameWithoutExtension?.contains("_Visible_") shouldBe true
                    }
                }

            test("3 ignored + 2 visible -> app sees only 2 visible across the boundary")
                .config(enabled = supports) {
                    val libResult = base.compile(
                        SourceFile.kotlin(
                            "UiLib.kt",
                            """
                            package uilib

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(ignore = true)
                            fun H1() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(ignore = true)
                            fun H2() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(ignore = true)
                            fun H3() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun V1() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun V2() {}
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
                        base.collectAllModulePreviewsEntry("allPreviews"),
                        extraClasspaths = listOf(libResult.outputDirectory),
                    )
                    appCompileResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    val ids = appCompileResult.loadCollectedPreviews(propertyName = "allPreviews").previewIds()
                    ids shouldContainExactlyInAnyOrder listOf("uilib.V1", "uilib.V2")
                }
        }

        context("annotation-argument variations") {
            test("ignore=false (explicit) is emitted and discoverable cross-module")
                .config(enabled = supports) {
                    val libResult = base.compile(
                        SourceFile.kotlin(
                            "UiLib.kt",
                            """
                            package uilib

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(ignore = false)
                            fun ExplicitFalse() {}
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
                        base.collectAllModulePreviewsEntry("allPreviews"),
                        extraClasspaths = listOf(libResult.outputDirectory),
                    )
                    appCompileResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    appCompileResult.loadCollectedPreviews(
                        propertyName = "allPreviews"
                    ).previewIds() shouldContainExactlyInAnyOrder
                        listOf("uilib.ExplicitFalse")
                }

            test("option without ignore (default false) is emitted and discoverable cross-module")
                .config(enabled = supports) {
                    val libResult = base.compile(
                        SourceFile.kotlin(
                            "UiLib.kt",
                            """
                            package uilib

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(displayName = "X")
                            fun WithDisplayName() {}
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
                        base.collectAllModulePreviewsEntry("allPreviews"),
                        extraClasspaths = listOf(libResult.outputDirectory),
                    )
                    appCompileResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    appCompileResult.loadCollectedPreviews(
                        propertyName = "allPreviews"
                    ).previewIds() shouldContainExactlyInAnyOrder
                        listOf("uilib.WithDisplayName")
                }

            test("@Preview without @ComposePreviewLabOption is emitted and discoverable")
                .config(enabled = supports) {
                    val libResult = base.compile(
                        SourceFile.kotlin(
                            "UiLib.kt",
                            """
                            package uilib

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun Plain() {}
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
                        base.collectAllModulePreviewsEntry("allPreviews"),
                        extraClasspaths = listOf(libResult.outputDirectory),
                    )
                    appCompileResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    appCompileResult.loadCollectedPreviews(
                        propertyName = "allPreviews"
                    ).previewIds() shouldContainExactlyInAnyOrder
                        listOf("uilib.Plain")
                }

            test("ignore=true takes priority over displayName (no emission)")
                .config(enabled = supports) {
                    val libResult = base.compile(
                        SourceFile.kotlin(
                            "UiLib.kt",
                            """
                            package uilib

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(ignore = true, displayName = "X")
                            fun HiddenWithName() {}
                            """,
                        ),
                    )
                    libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    val hintFacades = libResult.outputDirectory.previewHintFacadeFiles()
                    val markers = libResult.outputDirectory.previewHintMarkerFiles()
                    assertSoftly {
                        hintFacades.shouldBeEmpty()
                        markers.shouldBeEmpty()
                    }
                }
        }

        context("self-module regression (IR-side ignore filter independence)") {
            // The IR-side ignore filter (independent of the FIR filter introduced here)
            // keeps working — verify that single-module collectModulePreviews() /
            // collectAllModulePreviews() still drop ignored entries.

            test("self-module collectModulePreviews() still drops ignore=true previews") {
                val result = base.compile(
                    SourceFile.kotlin(
                        "Self.kt",
                        """
                        package self

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        @me.tbsten.compose.preview.lab.ComposePreviewLabOption(ignore = true)
                        fun Hidden() {}

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun Visible() {}
                        """,
                    ),
                    base.collectModulePreviewsEntry(),
                )
                result.exitCode shouldBe KotlinCompilation.ExitCode.OK
                result.loadCollectedPreviews().previewIds() shouldContainExactlyInAnyOrder listOf("self.Visible")
            }

            test("self-module collectAllModulePreviews() also drops own ignore=true previews")
                .config(enabled = supports) {
                    val result = base.compile(
                        SourceFile.kotlin(
                            "Self.kt",
                            """
                            package self

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(ignore = true)
                            fun Hidden() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun Visible() {}
                            """,
                        ),
                        base.collectAllModulePreviewsEntry("allPreviews"),
                    )
                    result.exitCode shouldBe KotlinCompilation.ExitCode.OK
                    result.loadCollectedPreviews(propertyName = "allPreviews").previewIds() shouldContainExactlyInAnyOrder
                        listOf("self.Visible")
                }
        }
    })

private fun List<Any>.previewIds(): List<String> = map { p ->
    p::class.members.find { it.name == "id" }!!.call(p) as String
}
