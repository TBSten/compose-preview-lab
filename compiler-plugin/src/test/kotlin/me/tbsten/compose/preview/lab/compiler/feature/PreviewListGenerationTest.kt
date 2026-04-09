package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase

class PreviewListGenerationTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        test("PreviewList が正しい id を持つ") {
            val source = SourceFile.kotlin(
                "Preview.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun MyPreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as List<*>
            val preview = previewList[0]!!
            val id = preview::class.members.find { it.name == "id" }!!.call(preview)
            id shouldBe "test.source.MyPreview"
        }

        test("PreviewList が正しい displayName を持つ") {
            val source = SourceFile.kotlin(
                "Preview.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun DisplayNamePreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as List<*>
            val preview = previewList[0]!!
            val displayName = preview::class.members.find { it.name == "displayName" }!!.call(preview)
            displayName shouldBe "test.source.DisplayNamePreview"
        }

        test("@ComposePreviewLabOption(displayName) でカスタム displayName が反映される") {
            val source = SourceFile.kotlin(
                "Preview.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(displayName = "Custom Name")
            fun CustomPreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as List<*>
            val preview = previewList[0]!!
            val displayName = preview::class.members.find { it.name == "displayName" }!!.call(preview)
            displayName shouldBe "Custom Name"
        }

        test("PreviewList が filePath を持つ") {
            val source = SourceFile.kotlin(
                "Preview.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun FilePathPreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as List<*>
            val preview = previewList[0]!!
            val filePath = preview::class.members.find { it.name == "filePath" }!!.call(preview) as? String
            filePath shouldNotBe null
        }

        test("PreviewList が sortedBy displayName でソートされている") {
            val source = SourceFile.kotlin(
                "Preview.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun ZPreview() {}

            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun APreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as List<*>
            previewList.size shouldBe 2

            val names = previewList.map { p ->
                p!!::class.members.find { it.name == "displayName" }!!.call(p) as String
            }
            names shouldBe names.sorted()
        }

        test("@Preview 関数が 0 個の場合は PreviewList は空リスト (コンパイルは成功する)") {
            val source = SourceFile.kotlin(
                "NoPreviews.kt",
                """
            package test.source

            fun regularFunction() {}
            """,
            )
            val result = base.compile(source)
            // @Preview がなくてもコンパイルエラーにならない
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK
            // ソース生成方式では PreviewList は常に存在するが、@Preview が無ければ空リスト
            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as List<*>
            previewList.size shouldBe 0
        }

        test("@ComposePreviewLabOption(id) でカスタム id が反映される") {
            val source = SourceFile.kotlin(
                "Preview.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(id = "my-custom-id")
            fun CustomIdPreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as List<*>
            val preview = previewList[0]!!
            val id = preview::class.members.find { it.name == "id" }!!.call(preview)
            id shouldBe "my-custom-id"
        }

        test("プレースホルダー {{package}} {{simpleName}} {{qualifiedName}} が正しく解決される") {
            val source = SourceFile.kotlin(
                "Preview.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(
                displayName = "pkg={{package}} simple={{simpleName}} fqn={{qualifiedName}}"
            )
            fun PlaceholderPreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as List<*>
            val preview = previewList[0]!!
            val displayName = preview::class.members.find { it.name == "displayName" }!!.call(preview) as String
            displayName shouldBe "pkg=test.source simple=PlaceholderPreview fqn=test.source.PlaceholderPreview"
        }
    })
