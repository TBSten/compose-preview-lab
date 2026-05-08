package me.tbsten.compose.preview.lab.compiler.contract

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.loadCollectedPreviews

/**
 * Contract test that verifies the compiler-API behavior of `IrGenerationExtension`.
 */
class IrGenerationExtensionContractTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        test("can build an IrConstructorCall in IR to create a data-class instance") {
            val result = base.compile(
                SourceFile.kotlin(
                    "Input.kt",
                    """
                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun MyPreview() {}
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previews = result.loadCollectedPreviews()
            previews.size shouldBe 1

            val preview = previews[0]
            val idField = preview::class.members.find { it.name == "id" }
            idField shouldNotBe null
        }

        test("can build a lambda expression in IR") {
            val result = base.compile(
                SourceFile.kotlin(
                    "Input.kt",
                    """
                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun LambdaPreview() {}
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previews = result.loadCollectedPreviews()
            previews.size shouldBe 1

            val preview = previews[0]
            val contentField = preview::class.members.find { it.name == "content" }
            contentField shouldNotBe null
        }
    })
