package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.isAtLeast
import me.tbsten.compose.preview.lab.compiler.loadCollectedPreviews

/**
 * **Skipped on Kotlin < 2.3.20**: `collectAllModulePreviews()` relies on the per-`@Preview`
 * hint emission performed by `PreviewHintFirGenerator`, which is only registered when
 * `CompatContext.supportsFirHintGeneration()` returns `true` (= Kotlin 2.3.20+). On earlier
 * Kotlin versions the FIR generator stays unregistered, no hints are emitted, and the
 * aggregation returns an empty list — the assertions here would fail spuriously.
 */
class AggregationTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        val testKotlinVersion = System.getProperty("test.kotlin.version") ?: "2.3.21"
        val supports = testKotlinVersion.isAtLeast(2, 3, 20)

        test("collectAllModulePreviews() collects @Preview functions")
            .config(enabled = supports) {
                val result = base.compile(
                    SourceFile.kotlin(
                        "Preview.kt",
                        """
                        package test.source

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun AggPreview() {}
                        """,
                    ),
                    base.collectAllModulePreviewsEntry(),
                )
                result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                result.loadCollectedPreviews(propertyName = "allPreviews").size shouldBe 1
            }

        test("collectAllModulePreviews() matches collectModulePreviews() in a single-module project")
            .config(enabled = supports) {
                val result = base.compile(
                    SourceFile.kotlin(
                        "Preview.kt",
                        """
                        package test.source

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun SingleModulePreview() {}
                        """,
                    ),
                    SourceFile.kotlin(
                        "Entry.kt",
                        """
                        package test.entry

                        import me.tbsten.compose.preview.lab.collectModulePreviews
                        import me.tbsten.compose.preview.lab.collectAllModulePreviews

                        val previews by collectModulePreviews()
                        val allPreviews by collectAllModulePreviews()
                        """,
                    ),
                )
                result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val previews = result.loadCollectedPreviews(propertyName = "previews")
                val allPreviews = result.loadCollectedPreviews(propertyName = "allPreviews")

                previews.size shouldBe allPreviews.size
                val ids = previews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }
                val allIds = allPreviews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }
                ids shouldBe allIds
            }
    })
