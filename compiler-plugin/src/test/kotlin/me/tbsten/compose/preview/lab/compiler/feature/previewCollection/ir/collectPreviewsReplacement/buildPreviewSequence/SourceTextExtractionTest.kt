package me.tbsten.compose.preview.lab.compiler.feature.previewCollection.ir.collectPreviewsReplacement.buildPreviewSequence

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.loadCollectedPreviews

class SourceTextExtractionTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        test("CollectedPreview.code contains the source code string") {
            val result = base.compile(
                SourceFile.kotlin(
                    "Preview.kt",
                    """
                    package test.source

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun CodePreview() {
                        val x = 42
                    }
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val preview = result.loadCollectedPreviews().first()
            val code = preview::class.members.find { it.name == "code" }!!.call(preview) as? String
            code shouldNotBe null
            code!! shouldContain "42"
        }

        test("kdoc field is populated when a KDoc is present") {
            val result = base.compile(
                SourceFile.kotlin(
                    "Preview.kt",
                    """
                    package test.source

                    /** This is a KDoc comment */
                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun DocPreview() {}
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val preview = result.loadCollectedPreviews().first()
            val kdoc = preview::class.members.find { it.name == "kdoc" }!!.call(preview) as? String
            kdoc shouldNotBe null
            kdoc!! shouldContain "KDoc comment"
        }

        test("kdoc field is null when no KDoc is present") {
            val result = base.compile(
                SourceFile.kotlin(
                    "Preview.kt",
                    """
                    package test.source

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun NoDocPreview() {}
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val preview = result.loadCollectedPreviews().first()
            val kdoc = preview::class.members.find { it.name == "kdoc" }!!.call(preview)
            kdoc shouldBe null
        }

        test("code is extracted from an expression-body function (= ...)") {
            val result = base.compile(
                SourceFile.kotlin(
                    "Preview.kt",
                    """
                    package test.source

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun ExprBodyPreview() = println("expression body")
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val preview = result.loadCollectedPreviews().first()
            val code = preview::class.members.find { it.name == "code" }!!.call(preview) as? String
            code shouldNotBe null
            code!! shouldContain "expression body"
        }
    })
