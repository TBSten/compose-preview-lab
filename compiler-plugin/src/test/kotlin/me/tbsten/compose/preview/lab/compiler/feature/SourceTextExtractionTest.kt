package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase

class SourceTextExtractionTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        test("CollectedPreview.code にソースコード文字列が含まれる") {
            val source = SourceFile.kotlin(
                "Preview.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun CodePreview() {
                val x = 42
            }
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as List<*>
            val preview = previewList[0]!!
            val code = preview::class.members.find { it.name == "code" }!!.call(preview) as? String
            code shouldNotBe null
            code!! shouldContain "42"
        }

        test("KDoc がある場合に kdoc フィールドに反映される") {
            val source = SourceFile.kotlin(
                "Preview.kt",
                """
            package test.source

            /** This is a KDoc comment */
            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun DocPreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as List<*>
            val preview = previewList[0]!!
            val kdoc = preview::class.members.find { it.name == "kdoc" }!!.call(preview) as? String
            kdoc shouldNotBe null
            kdoc!! shouldContain "KDoc comment"
        }

        test("KDoc がない場合に kdoc が null") {
            val source = SourceFile.kotlin(
                "Preview.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun NoDocPreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as List<*>
            val preview = previewList[0]!!
            val kdoc = preview::class.members.find { it.name == "kdoc" }!!.call(preview)
            kdoc shouldBe null
        }

        test("式ボディ (= ...) の関数から code が抽出される") {
            val source = SourceFile.kotlin(
                "Preview.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun ExprBodyPreview() = println("expression body")
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as List<*>
            val preview = previewList[0]!!
            val code = preview::class.members.find { it.name == "code" }!!.call(preview) as? String
            code shouldNotBe null
            code!! shouldContain "expression body"
        }
    })
