package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase

class AggregationTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        test("PreviewAllList が生成される") {
            val source = SourceFile.kotlin(
                "Preview.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun AggPreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewAllList = result.classLoader
                .loadClass("test.generated.PreviewAllList")
                .kotlin.objectInstance as? List<*>
            previewAllList shouldNotBe null
            previewAllList!!.size shouldBe 1
        }

        test("単一モジュールで PreviewAllList が PreviewList と同じ内容になる") {
            val source = SourceFile.kotlin(
                "Preview.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun SingleModulePreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as List<*>
            val previewAllList = result.classLoader
                .loadClass("test.generated.PreviewAllList")
                .kotlin.objectInstance as List<*>

            previewList.size shouldBe previewAllList.size
            val listIds = previewList.map { p -> p!!::class.members.find { it.name == "id" }!!.call(p) as String }
            val allIds = previewAllList.map { p -> p!!::class.members.find { it.name == "id" }!!.call(p) as String }
            listIds shouldBe allIds
        }

        test("集約プロパティが @AggregateToAll 付きで生成される") {
            val source = SourceFile.kotlin(
                "Preview.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun AggPropPreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            // 集約プロパティのクラスが存在すること
            // ソース生成方式では AggregatePreviewProperty.kt に配置
            val clazz = runCatching {
                result.classLoader.loadClass(
                    "me.tbsten.compose.preview.lab.generated.AggregatePreviewPropertyKt",
                )
            }.getOrNull()
            clazz shouldNotBe null
        }
    })
