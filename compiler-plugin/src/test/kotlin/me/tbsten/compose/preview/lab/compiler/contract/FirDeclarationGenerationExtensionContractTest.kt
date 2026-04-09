package me.tbsten.compose.preview.lab.compiler.contract

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase

/**
 * FirDeclarationGenerationExtension の Compiler API 挙動を検証するコントラクトテスト。
 * Kotlin バージョン更新時にどの API が壊れたか特定するために使う。
 */
class FirDeclarationGenerationExtensionContractTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        test("getTopLevelClassIds() で object 宣言を生成できる") {
            val source = SourceFile.kotlin(
                "Input.kt",
                """
            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun MyPreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewListClass = result.classLoader.loadClass("test.generated.PreviewList")
            previewListClass shouldNotBe null
        }

        test("getTopLevelCallableIds() でトップレベルプロパティを生成できる") {
            val source = SourceFile.kotlin(
                "Input.kt",
                """
            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun MyPreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            // ソース生成方式では aggregate property は AggregatePreviewProperty.kt に配置
            val clazz = result.classLoader.loadClass(
                "me.tbsten.compose.preview.lab.generated.AggregatePreviewPropertyKt",
            )
            clazz shouldNotBe null
        }

        test("predicateBasedProvider でアノテーション付き関数を発見できる") {
            val source = SourceFile.kotlin(
                "Input.kt",
                """
            @org.jetbrains.compose.ui.tooling.preview.Preview
            fun DiscoverablePreview() {}

            fun notAPreview() {}
            """,
            )
            val result = base.compile(source)
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previewList = result.classLoader
                .loadClass("test.generated.PreviewList")
                .kotlin.objectInstance as? List<*>
            previewList shouldNotBe null
            previewList!!.size shouldBe 1
        }
    })
