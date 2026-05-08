package me.tbsten.compose.preview.lab.compiler.contract

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase

class CompilerPluginRegistrationContractTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        test("CompilerPluginRegistrar + CommandLineProcessor receives SubpluginOption") {
            val source = SourceFile.kotlin(
                "Main.kt",
                """
            fun main() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK
        }
    })
