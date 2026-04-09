package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase

class PreviewDiscoveryTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        test("@Preview 関数がコンパイル後に PreviewList として参照可能") {
            val source = SourceFile.kotlin(
                "Previews.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun HelloPreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as? List<*>
            previewList shouldNotBe null
            previewList!!.size shouldBe 1
        }

        test("Android の @Preview も収集される") {
            val source = SourceFile.kotlin(
                "AndroidPreview.kt",
                """
            package test.source

            @androidx.compose.ui.tooling.preview.Preview
            fun AndroidPreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as? List<*>
            previewList shouldNotBe null
            previewList!!.size shouldBe 1
        }

        test("@ComposePreviewLabOption(ignore=true) の関数はスキップされる") {
            val source = SourceFile.kotlin(
                "Ignored.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(ignore = true)
            fun IgnoredPreview() {}

            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun VisiblePreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as? List<*>
            previewList shouldNotBe null
            previewList!!.size shouldBe 1
        }

        test("@Preview が付いていない関数は収集されない") {
            val source = SourceFile.kotlin(
                "Mixed.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun PreviewFunc() {}

            fun NonPreviewFunc() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as? List<*>
            previewList!!.size shouldBe 1
        }

        test("Android と CMP の @Preview が混在する場合に両方収集される") {
            val source = SourceFile.kotlin(
                "MixedPreviews.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun CmpPreview() {}

            @androidx.compose.ui.tooling.preview.Preview
            fun AndroidPreviewFunc() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as? List<*>
            previewList shouldNotBe null
            previewList!!.size shouldBe 2
        }

        test("同一ファイル内に複数の @Preview 関数がある場合に全て収集される") {
            val source = SourceFile.kotlin(
                "MultiPreviews.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun Preview1() {}

            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun Preview2() {}

            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun Preview3() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as? List<*>
            previewList!!.size shouldBe 3
        }

        test("異なるパッケージの同名 @Preview 関数が衝突しない") {
            val source1 = SourceFile.kotlin(
                "com/example/a/Preview.kt",
                """
            package com.example.a

            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun SameNamePreview() {}
            """,
            )
            val source2 = SourceFile.kotlin(
                "com/example/b/Preview.kt",
                """
            package com.example.b

            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun SameNamePreview() {}
            """,
            )
            val result = base.compile(source1, source2)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as? List<*>
            previewList!!.size shouldBe 2

            val ids = previewList.map { p ->
                p!!::class.members.find { it.name == "id" }!!.call(p) as String
            }
            ids.toSet().size shouldBe 2 // 両方ユニークな id を持つ
        }
    })
