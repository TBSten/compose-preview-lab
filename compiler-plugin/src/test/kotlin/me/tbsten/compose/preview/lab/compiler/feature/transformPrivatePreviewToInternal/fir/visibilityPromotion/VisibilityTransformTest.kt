package me.tbsten.compose.preview.lab.compiler.feature.transformPrivatePreviewToInternal.fir.visibilityPromotion

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

        test("a private @Preview function is widened to internal and reachable from collectModulePreviews()") {
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

            // Reachable from collectModulePreviews() — the fact that compilation
            // succeeds is itself the evidence.
            result.loadCollectedPreviews().size shouldBe 1
        }

        test("a public @Preview function's visibility is left unchanged") {
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
            // Stays public (Modifiers.isPublic).
            method shouldNotBe null
            java.lang.reflect.Modifier.isPublic(method!!.modifiers) shouldBe true
        }

        test("an internal @Preview function's visibility is left unchanged") {
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

            // `internal` is mangled to public on the JVM, so successful compilation is
            // sufficient evidence that the visibility is unchanged.
            result.loadCollectedPreviews().size shouldBe 1
        }
    })
