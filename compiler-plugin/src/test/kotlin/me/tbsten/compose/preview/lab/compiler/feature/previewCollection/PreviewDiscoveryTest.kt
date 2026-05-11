package me.tbsten.compose.preview.lab.compiler.feature.previewCollection

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.loadCollectedPreviews

class PreviewDiscoveryTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        test("@Preview functions are reachable through collectModulePreviews() after compilation") {
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

        test("Android `@Preview` is also collected") {
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

        test("functions marked @ComposePreviewLabOption(ignore=true) are skipped") {
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

        test("functions without @Preview are not collected") {
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

        test("when Android and CMP @Preview coexist, both are collected") {
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

        test("multiple @Preview functions in the same file are all collected") {
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

        test("same-name @Preview functions in different packages do not collide") {
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
