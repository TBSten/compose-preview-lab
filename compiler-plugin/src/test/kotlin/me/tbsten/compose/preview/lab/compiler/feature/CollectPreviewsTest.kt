package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase

/**
 * Tests for detection of `collectModulePreviews()` call sites.
 *
 * Verifies that simply writing `val previews by collectModulePreviews()` is enough for
 * the IR phase to replace the `Lazy` body and inject the list of `@Preview` functions.
 */
class CollectPreviewsTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        test("val by collectModulePreviews() collects @Preview functions") {
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

        test("compiles successfully with an empty list when zero @Preview functions are present") {
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

        test("collected entries carry the correct id") {
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
