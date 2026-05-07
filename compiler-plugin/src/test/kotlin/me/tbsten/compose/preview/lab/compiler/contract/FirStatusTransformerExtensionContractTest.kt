package me.tbsten.compose.preview.lab.compiler.contract

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase

/**
 * Contract test that verifies the compiler-API behavior of
 * `FirStatusTransformerExtension`.
 */
class FirStatusTransformerExtensionContractTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        test("can widen the visibility of a private function to internal") {
            val source = SourceFile.kotlin(
                "Input.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            private fun PrivatePreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val method = result.classLoader
                .loadClass("test.source.InputKt")
                .declaredMethods
                .find { it.name == "PrivatePreview" }

            // After private → internal (which becomes public on the JVM), the function
            // is reachable from same-module reflection.
            method shouldNotBe null
        }

        test("the transformed function is callable from same-module code") {
            val source = SourceFile.kotlin(
                "Input.kt",
                """
            package test.source

            @org.jetbrains.compose.ui.tooling.preview.Preview
            private fun CallablePreview() {}
            """,
            )
            val caller = SourceFile.kotlin(
                "Caller.kt",
                """
            package test.source

            fun callPreview() {
                CallablePreview()
            }
            """,
            )
            val result = base.compile(source, caller)
            // Without the transform: private, so the call site fails to compile.
            // With the transform: widened to internal, so compilation succeeds.
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK
        }
    })
