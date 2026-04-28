package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.loadCollectedPreviews

class PreviewDiscoveryTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        test("@Preview 関数がコンパイル後に collectModulePreviews() で取得できる") {
            val result = base.compile(
                SourceFile.kotlin(
                    "Previews.kt",
                    """
                    package test.source

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun HelloPreview() {}
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            result.loadCollectedPreviews().size shouldBe 1
        }

        test("Android の @Preview も収集される") {
            val result = base.compile(
                SourceFile.kotlin(
                    "AndroidPreview.kt",
                    """
                    package test.source

                    @androidx.compose.ui.tooling.preview.Preview
                    fun AndroidPreview() {}
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            result.loadCollectedPreviews().size shouldBe 1
        }

        test("@ComposePreviewLabOption(ignore=true) の関数はスキップされる") {
            val result = base.compile(
                SourceFile.kotlin(
                    "Ignored.kt",
                    """
                    package test.source

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    @me.tbsten.compose.preview.lab.ComposePreviewLabOption(ignore = true)
                    fun IgnoredPreview() {}

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun VisiblePreview() {}
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            result.loadCollectedPreviews().size shouldBe 1
        }

        test("@Preview が付いていない関数は収集されない") {
            val result = base.compile(
                SourceFile.kotlin(
                    "Mixed.kt",
                    """
                    package test.source

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun PreviewFunc() {}

                    fun NonPreviewFunc() {}
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            result.loadCollectedPreviews().size shouldBe 1
        }

        test("Android と CMP の @Preview が混在する場合に両方収集される") {
            val result = base.compile(
                SourceFile.kotlin(
                    "MixedPreviews.kt",
                    """
                    package test.source

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun CmpPreview() {}

                    @androidx.compose.ui.tooling.preview.Preview
                    fun AndroidPreviewFunc() {}
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            result.loadCollectedPreviews().size shouldBe 2
        }

        test("同一ファイル内に複数の @Preview 関数がある場合に全て収集される") {
            val result = base.compile(
                SourceFile.kotlin(
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
                ),
                base.collectModulePreviewsEntry(),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            result.loadCollectedPreviews().size shouldBe 3
        }

        test("異なるパッケージの同名 @Preview 関数が衝突しない") {
            val result = base.compile(
                SourceFile.kotlin(
                    "com/example/a/Preview.kt",
                    """
                    package com.example.a

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun SameNamePreview() {}
                    """,
                ),
                SourceFile.kotlin(
                    "com/example/b/Preview.kt",
                    """
                    package com.example.b

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun SameNamePreview() {}
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previews = result.loadCollectedPreviews()
            previews.size shouldBe 2

            val ids = previews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }
            ids.toSet().size shouldBe 2
        }
    })
