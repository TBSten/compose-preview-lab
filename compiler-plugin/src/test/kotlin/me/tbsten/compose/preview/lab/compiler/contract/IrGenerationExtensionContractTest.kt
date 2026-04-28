package me.tbsten.compose.preview.lab.compiler.contract

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.loadCollectedPreviews

/**
 * IrGenerationExtension の Compiler API 挙動を検証するコントラクトテスト。
 */
class IrGenerationExtensionContractTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        test("IR で IrConstructorCall を構築してデータクラスのインスタンスを生成できる") {
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

        test("IR で lambda 式を構築できる") {
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
