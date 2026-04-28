package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.loadCollectedPreviews

class AggregationTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        test("collectAllModulePreviews() に @Preview 関数が収集される") {
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

        test("単一モジュールで collectAllModulePreviews() が collectModulePreviews() と同じ内容になる") {
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
