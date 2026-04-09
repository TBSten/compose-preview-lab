package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase

/**
 * collectModulePreviews() 呼び出し検出のテスト。
 *
 * `val previews by collectModulePreviews()` と書くだけで
 * IR フェーズが Lazy の中身を差し替え、@Preview 関数のリストが注入されることを検証する。
 */
class CollectPreviewsTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        test("val by collectModulePreviews() に @Preview 関数が収集される") {
            val result = base.compile(
                SourceFile.kotlin(
                    "Previews.kt",
                    """
                    package test.source

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun HelloPreview() {}
                    """,
                ),
                SourceFile.kotlin(
                    "Entry.kt",
                    """
                    package test.entry

                    import me.tbsten.compose.preview.lab.collectModulePreviews

                    val myPreviews by collectModulePreviews()
                    """,
                ),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val entryClass = result.classLoader.loadClass("test.entry.EntryKt")

            @Suppress("UNCHECKED_CAST")
            val previews = entryClass.getMethod("getMyPreviews").invoke(null) as List<Any>
            previews.size shouldBe 1
        }

        test("@Preview が 0 個でも空リストでコンパイル成功") {
            val result = base.compile(
                SourceFile.kotlin(
                    "Entry.kt",
                    """
                    package test.entry

                    import me.tbsten.compose.preview.lab.collectModulePreviews

                    val noPreviews by collectModulePreviews()
                    """,
                ),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val entryClass = result.classLoader.loadClass("test.entry.EntryKt")

            @Suppress("UNCHECKED_CAST")
            val previews = entryClass.getMethod("getNoPreviews").invoke(null) as List<Any>
            previews.size shouldBe 0
        }

        test("収集された要素が正しい id を持つ") {
            val result = base.compile(
                SourceFile.kotlin(
                    "Previews.kt",
                    """
                    package test.source

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun MyPreview() {}
                    """,
                ),
                SourceFile.kotlin(
                    "Entry.kt",
                    """
                    package test.entry

                    import me.tbsten.compose.preview.lab.collectModulePreviews

                    val previews by collectModulePreviews()
                    """,
                ),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val entryClass = result.classLoader.loadClass("test.entry.EntryKt")

            @Suppress("UNCHECKED_CAST")
            val previews = entryClass.getMethod("getPreviews").invoke(null) as List<Any>
            val id = previews[0]::class.members.find { it.name == "id" }!!.call(previews[0])
            id shouldBe "test.source.MyPreview"
        }
    })
