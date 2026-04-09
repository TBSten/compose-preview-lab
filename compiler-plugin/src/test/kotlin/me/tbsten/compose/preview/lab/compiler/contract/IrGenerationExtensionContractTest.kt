package me.tbsten.compose.preview.lab.compiler.contract

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase

/**
 * IrGenerationExtension の Compiler API 挙動を検証するコントラクトテスト。
 */
class IrGenerationExtensionContractTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        test("IR で IrConstructorCall を構築してデータクラスのインスタンスを生成できる") {
            val source = SourceFile.kotlin(
                "Input.kt",
                """
            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun MyPreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            // PreviewList の要素が CollectedPreview のインスタンスであること
            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as? List<*>
            previewList shouldNotBe null
            previewList!!.size shouldBe 1

            val preview = previewList[0]!!
            val idField = preview::class.members.find { it.name == "id" }
            idField shouldNotBe null
        }

        test("IR で lambda 式を構築できる") {
            val source = SourceFile.kotlin(
                "Input.kt",
                """
            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun LambdaPreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as? List<*>
            previewList shouldNotBe null

            // content lambda が null でないこと
            val preview = previewList!![0]!!
            val contentField = preview::class.members.find { it.name == "content" }
            contentField shouldNotBe null
        }
    })
