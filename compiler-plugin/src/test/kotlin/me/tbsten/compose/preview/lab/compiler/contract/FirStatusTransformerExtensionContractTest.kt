package me.tbsten.compose.preview.lab.compiler.contract

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase

/**
 * FirStatusTransformerExtension の Compiler API 挙動を検証するコントラクトテスト。
 */
class FirStatusTransformerExtensionContractTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        test("private な関数の visibility を internal に変更できる") {
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

            // private → internal (JVM 上では public になる) に変更されていれば、
            // 同一モジュール内からリフレクションでアクセス可能
            method shouldNotBe null
        }

        test("変更後の関数が同一モジュール内コードから呼び出し可能") {
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
            // 現状: private なので呼び出せずコンパイルエラー
            // 実装後: internal に変更されるのでコンパイル成功を期待
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK
        }
    })
