package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.isAtLeast
import me.tbsten.compose.preview.lab.compiler.loadCollectedPreviews

/**
 * Verifies how `@ComposePreviewLabOption(ignore = true)` and the new `collectScope`
 * interact. The two options are independent: `ignore` is a hard binary skip per
 * declaration, `collectScope` is a soft per-bucket filter.
 *
 * **Skipped on Kotlin < 2.3.21**: the per-declaration hint generator only runs on
 * Kotlin 2.3.21+ ([CompatContext.supportsKlibCrossModuleHint]).
 */
class PreviewHintScopeIgnoreInteropTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        val testKotlinVersion = System.getProperty("test.kotlin.version") ?: "2.3.21"
        val supports = testKotlinVersion.isAtLeast(2, 3, 21)

        test("ignore=true wins over collectScope: the preview is dropped from every bucket")
            .config(enabled = supports) {
                val result = base.compile(
                    SourceFile.kotlin(
                        "Mod.kt",
                        """
                        package self

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        @me.tbsten.compose.preview.lab.ComposePreviewLabOption(
                            ignore = true,
                            collectScope = ["design"],
                        )
                        fun Hidden() {}

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        @me.tbsten.compose.preview.lab.ComposePreviewLabOption(collectScope = ["design"])
                        fun Visible() {}
                        """,
                    ),
                    SourceFile.kotlin(
                        "Entry.kt",
                        """
                        package test.entry

                        import me.tbsten.compose.preview.lab.collectModulePreviews

                        val design by collectModulePreviews(scope = "design")
                        """,
                    ),
                )
                result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                result
                    .loadCollectedPreviews(propertyName = "design")
                    .previewIds() shouldContainExactlyInAnyOrder listOf("self.Visible")
            }

        test("ignore=false + collectScope = X is collected under X")
            .config(enabled = supports) {
                val result = base.compile(
                    SourceFile.kotlin(
                        "Mod.kt",
                        """
                        package self

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        @me.tbsten.compose.preview.lab.ComposePreviewLabOption(
                            ignore = false,
                            collectScope = ["design"],
                        )
                        fun Visible() {}
                        """,
                    ),
                    SourceFile.kotlin(
                        "Entry.kt",
                        """
                        package test.entry

                        import me.tbsten.compose.preview.lab.collectModulePreviews

                        val design by collectModulePreviews(scope = "design")
                        val defaultOne by collectModulePreviews()
                        """,
                    ),
                )
                result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                result.loadCollectedPreviews(propertyName = "design")
                    .previewIds() shouldContainExactlyInAnyOrder listOf("self.Visible")
                result.loadCollectedPreviews(propertyName = "defaultOne").shouldBeEmpty()
            }

        test("only ignored previews under collectScope X means X bucket is empty")
            .config(enabled = supports) {
                val result = base.compile(
                    SourceFile.kotlin(
                        "Mod.kt",
                        """
                        package self

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        @me.tbsten.compose.preview.lab.ComposePreviewLabOption(
                            ignore = true,
                            collectScope = ["design"],
                        )
                        fun H1() {}

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        @me.tbsten.compose.preview.lab.ComposePreviewLabOption(
                            ignore = true,
                            collectScope = ["design"],
                        )
                        fun H2() {}
                        """,
                    ),
                    SourceFile.kotlin(
                        "Entry.kt",
                        """
                        package test.entry

                        import me.tbsten.compose.preview.lab.collectModulePreviews

                        val design by collectModulePreviews(scope = "design")
                        """,
                    ),
                )
                result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                result.loadCollectedPreviews(propertyName = "design").shouldBeEmpty()
            }
    })

private fun List<Any>.previewIds(): List<String> = map { collectedPreview ->
    collectedPreview::class.members.find { it.name == "id" }!!.call(collectedPreview) as String
}
