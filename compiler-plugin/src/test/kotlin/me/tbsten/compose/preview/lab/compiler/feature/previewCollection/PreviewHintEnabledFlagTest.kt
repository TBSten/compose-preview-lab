package me.tbsten.compose.preview.lab.compiler.feature.previewCollection

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.PluginOption
import com.tschuchort.compiletesting.SourceFile
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.feature.previewHintFacadeFiles
import me.tbsten.compose.preview.lab.compiler.feature.previewHintMarkerFiles
import me.tbsten.compose.preview.lab.compiler.isAtLeast
import me.tbsten.compose.preview.lab.compiler.loadCollectedPreviews

/**
 * End-to-end behaviour of the `collectPreviewsEnabled` plugin option.
 *
 * The flag is plumbed through `PluginConfig`, gates `PreviewHintFirGenerator`
 * registration in `PreviewLabFirExtensionRegistrar`, and gates
 * `collect[All]ModulePreviews()` in `PreviewLabIrBodyFiller`. This file covers each layer
 * end-to-end via kctfork:
 *
 * 1. `enabled = false` → no marker / facade emission for this module.
 * 2. `enabled = false` + `collectModulePreviews()` call site → `COMPILATION_ERROR`.
 * 3. `enabled = false` + `collectAllModulePreviews()` call site → `COMPILATION_ERROR`.
 * 4. `enabled = true` (default) → existing behaviour preserved.
 * 5. `enabled = false` lib + `enabled = true` app → app's `collectAllModulePreviews()`
 *    sees nothing from the lib (the lib emitted no hints to discover).
 *
 * **Test gating**: scenarios that depend on the FIR hint generator being active
 * (emission, cross-module aggregation) are gated on Kotlin 2.3.21+ via
 * [CompatContext.supportsKlibCrossModuleHint]. The IR-pass error scenarios run on every
 * supported Kotlin version because the IR pass is always active — the disabled-module
 * collect call check does not depend on the hint generator. Each individual test pins
 * its `.config(enabled = ...)` accordingly.
 */
class PreviewHintEnabledFlagTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        val testKotlinVersion = System.getProperty("test.kotlin.version") ?: "2.3.21"
        val supports = testKotlinVersion.isAtLeast(2, 3, 20)

        val pluginId = "me.tbsten.compose.preview.lab.compiler"
        val disabled = listOf(PluginOption(pluginId, "collectPreviewsEnabled", "false"))

        context("emit suppression") {
            test("disabled module emits no marker interfaces and no hint facades")
                .config(enabled = supports) {
                    val libResult = base.compile(
                        SourceFile.kotlin(
                            "UiLib.kt",
                            """
                            package uilib

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun A() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun B() {}
                            """,
                        ),
                        pluginOptions = disabled,
                    )
                    libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    assertSoftly {
                        libResult.outputDirectory.previewHintMarkerFiles().shouldBeEmpty()
                        libResult.outputDirectory.previewHintFacadeFiles().shouldBeEmpty()
                    }
                }

            test("enabled (default) module still emits one marker + one facade per @Preview")
                .config(enabled = supports) {
                    val libResult = base.compile(
                        SourceFile.kotlin(
                            "UiLib.kt",
                            """
                            package uilib

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun A() {}

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun B() {}
                            """,
                        ),
                    )
                    libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    assertSoftly {
                        libResult.outputDirectory.previewHintMarkerFiles() shouldContainExactlyInAnyOrderByName
                            listOf("_A_", "_B_")
                        libResult.outputDirectory.previewHintFacadeFiles().size shouldBe 2
                    }
                }
        }

        context("collect call sites are rejected when the module is disabled") {
            test("collectModulePreviews() in a disabled module reports an actionable error") {
                val result = base.compile(
                    SourceFile.kotlin(
                        "Mod.kt",
                        """
                        package self

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun P() {}
                        """,
                    ),
                    base.collectModulePreviewsEntry(),
                    pluginOptions = disabled,
                )

                assertSoftly {
                    result.exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
                    result.messages shouldContain "[ComposePreviewLab/"
                    result.messages shouldContain "collectModulePreviews()"
                    result.messages shouldContain "composePreviewLab"
                }
            }

            test("collectAllModulePreviews() in a disabled module reports an actionable error") {
                val result = base.compile(
                    SourceFile.kotlin(
                        "Mod.kt",
                        """
                            package self

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun P() {}
                            """,
                    ),
                    base.collectAllModulePreviewsEntry("allPreviews"),
                    pluginOptions = disabled,
                )

                assertSoftly {
                    result.exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
                    result.messages shouldContain "[ComposePreviewLab/"
                    result.messages shouldContain "collectAllModulePreviews()"
                    result.messages shouldContain "composePreviewLab"
                }
            }
        }

        context("downstream visibility") {
            test("an enabled app cannot see previews coming from a disabled lib")
                .config(enabled = supports) {
                    val libResult = base.compile(
                        SourceFile.kotlin(
                            "UiLib.kt",
                            """
                            package uilib

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun Hidden() {}
                            """,
                        ),
                        pluginOptions = disabled,
                    )
                    libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK
                    libResult.outputDirectory.previewHintFacadeFiles().shouldBeEmpty()

                    val appCompileResult = base.compile(
                        SourceFile.kotlin(
                            "App.kt",
                            """
                            package app

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun Visible() {}
                            """,
                        ),
                        base.collectAllModulePreviewsEntry("allPreviews"),
                        extraClasspaths = listOf(libResult.outputDirectory),
                    )
                    appCompileResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    appCompileResult
                        .loadCollectedPreviews(propertyName = "allPreviews")
                        .previewIds() shouldContainExactlyInAnyOrder listOf("app.Visible")
                }
        }
    })

private fun List<Any>.previewIds(): List<String> = map { collectedPreview ->
    val klass = collectedPreview::class
    val idMember = klass.members.find { it.name == "id" }
        ?: error(
            "Reflective `id` lookup on ${klass.qualifiedName ?: klass.simpleName} failed: " +
                "expected a `CollectedPreview`-shaped class with an `id` member but found none. " +
                "(This usually means the IR-injected list contains an unexpected element type.)",
        )
    val rawId = idMember.call(collectedPreview)
    rawId as? String
        ?: error(
            "Reflective `id` lookup on ${klass.qualifiedName ?: klass.simpleName} returned a " +
                "${rawId?.let { it::class.qualifiedName ?: it::class.simpleName } ?: "null"} " +
                "instead of a String — the `CollectedPreview.id` contract has changed.",
        )
}

/**
 * Asserts that the marker class file names (which embed `_<sanitized_fqn>_`) contain each
 * substring in [substringFqns] exactly once. Substrings such as `"_A_"` make the assertion
 * robust to the trailing `<hash>` portion of the marker name.
 */
private infix fun List<java.io.File>.shouldContainExactlyInAnyOrderByName(substringFqns: List<String>) {
    size shouldBe substringFqns.size
    val names = map { it.nameWithoutExtension }
    substringFqns.forEach { needle ->
        names.count { it.contains(needle) } shouldBe 1
    }
}
