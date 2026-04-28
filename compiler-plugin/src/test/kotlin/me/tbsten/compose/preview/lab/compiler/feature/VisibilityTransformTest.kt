package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.loadCollectedPreviews

class VisibilityTransformTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        test("private な @Preview 関数が internal に変更され collectModulePreviews() から参照できる") {
            val result = base.compile(
                SourceFile.kotlin(
                    "PrivatePreview.kt",
                    """
                    package test.source

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    private fun SecretPreview() {}
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            // collectModulePreviews() から参照できること (= コンパイルが通ること自体が証拠)
            result.loadCollectedPreviews().size shouldBe 1
        }

        test("public な @Preview 関数の visibility は変更されない") {
            val result = base.compile(
                SourceFile.kotlin(
                    "PublicPreview.kt",
                    """
                    package test.source

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun PublicPreview() {}
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
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
            val result = base.compile(
                SourceFile.kotlin(
                    "InternalPreview.kt",
                    """
                    package test.source

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    internal fun InternalPreview() {}
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            // internal は JVM では public になるため、コンパイルが通ることで確認
            result.loadCollectedPreviews().size shouldBe 1
        }
    })
