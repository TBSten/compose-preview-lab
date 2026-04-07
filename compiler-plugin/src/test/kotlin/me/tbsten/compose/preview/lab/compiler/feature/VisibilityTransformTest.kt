package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase

class VisibilityTransformTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        test("private な @Preview 関数が internal に変更され PreviewList から直接参照できる") {
            val source = SourceFile.kotlin(
                "PrivatePreview.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            private fun SecretPreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            // PreviewList から参照できること (= コンパイルが通ること自体が証拠)
            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as? List<*>
            previewList!!.size shouldBe 1
        }

        test("public な @Preview 関数の visibility は変更されない") {
            val source = SourceFile.kotlin(
                "PublicPreview.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun PublicPreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val method = result.classLoader
                .loadClass("test.source.PublicPreviewKt")
                .methods
                .find { it.name == "PublicPreview" }
            // public のまま (Modifiers.isPublic)
            method shouldNotBe null
            java.lang.reflect.Modifier.isPublic(method!!.modifiers) shouldBe true
        }

        test("internal な @Preview 関数は visibility が変更されない") {
            val source = SourceFile.kotlin(
                "InternalPreview.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            internal fun InternalPreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            // internal は JVM では public になるため、コンパイルが通ることで確認
            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as? List<*>
            previewList!!.size shouldBe 1
        }
    })
