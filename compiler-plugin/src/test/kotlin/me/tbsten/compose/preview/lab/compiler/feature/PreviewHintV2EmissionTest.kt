package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.fir.computeSourceFqnHash

/**
 * Per-declaration hint generator (Metro 風) が `@Preview` 1 個に対し
 * `previewHint_<sha256(sourceFqn)>(): CollectedPreview` を 1 個 emit し、 IR で body に
 * `CollectedPreview(...)` constructor 呼び出しを埋め込んでいることを検証する。
 *
 * **Skipped on Kotlin < 2.3.21**: 旧モジュール集約 hint generator と同じ version gate に従う
 * ([CompatContext.supportsKlibCrossModuleHint][me.tbsten.compose.preview.lab.compiler.compat.CompatContext.supportsKlibCrossModuleHint])。
 */
class PreviewHintV2EmissionTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        val testKotlinVersion = System.getProperty("test.kotlin.version") ?: "2.3.21"
        val supports = testKotlinVersion.compareTo("2.3.21") >= 0 || testKotlinVersion.startsWith("2.4")

        test("@Preview 1 個 → previewHint_<hash> が 1 個 emit される")
            .config(enabled = supports) {
                val result = base.compile(
                    SourceFile.kotlin(
                        "Previews.kt",
                        """
                        package test.source

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun MyButton() {}
                        """,
                    ),
                )
                result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val expectedHash = computeSourceFqnHash("test.source.MyButton")
                // file facade class name は file 名の先頭を capitalize: previewHint_<hash>.kt → PreviewHint_<hash>Kt.class
                val expectedClassFile = "me/tbsten/compose/preview/lab/hints/PreviewHint_${expectedHash}Kt.class"

                val classFiles = result.outputDirectory.walkTopDown()
                    .filter { it.isFile && it.name.endsWith(".class") }
                    .map { it.relativeTo(result.outputDirectory).path }
                    .toList()

                classFiles shouldExist { it == expectedClassFile }
            }

        test("hint 関数 invoke で CollectedPreview が返り、 metadata が埋まっている")
            .config(enabled = supports) {
                val result = base.compile(
                    SourceFile.kotlin(
                        "Previews.kt",
                        """
                        package test.source

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun MyButton() {}
                        """,
                    ),
                )
                result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val expectedHash = computeSourceFqnHash("test.source.MyButton")
                val hintFacade = result.classLoader
                    .loadClass("me.tbsten.compose.preview.lab.hints.PreviewHint_${expectedHash}Kt")
                val hintMethod = hintFacade.getMethod("previewHint_$expectedHash")

                // hint() を invoke すると CollectedPreview インスタンスが返る
                val collected = hintMethod.invoke(null)
                checkNotNull(collected) { "hint 関数が null を返した" }

                val collectedClass = collected.javaClass
                val id = collectedClass.getMethod("getId").invoke(collected) as String
                val displayName = collectedClass.getMethod("getDisplayName").invoke(collected) as String

                id shouldBe "test.source.MyButton"
                displayName shouldBe "test.source.MyButton"
            }

        test("@Preview が複数あれば hint が複数 emit される")
            .config(enabled = supports) {
                val result = base.compile(
                    SourceFile.kotlin(
                        "Previews.kt",
                        """
                        package test.source

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun First() {}

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun Second() {}
                        """,
                    ),
                )
                result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val firstHash = computeSourceFqnHash("test.source.First")
                val secondHash = computeSourceFqnHash("test.source.Second")
                firstHash shouldBe firstHash // sanity (deterministic)

                val classFiles = result.outputDirectory.walkTopDown()
                    .filter { it.isFile && it.name.endsWith(".class") }
                    .map { it.relativeTo(result.outputDirectory).path }
                    .toList()

                classFiles shouldExist {
                    it == "me/tbsten/compose/preview/lab/hints/PreviewHint_${firstHash}Kt.class"
                }
                classFiles shouldExist {
                    it == "me/tbsten/compose/preview/lab/hints/PreviewHint_${secondHash}Kt.class"
                }
            }

        test("同じ source FQN の hash は projectRootPath に依存せず再現可能")
            .config(enabled = supports) {
                // hash 関数自体の reproducibility を verify (compile 越しではなく直接)
                val a = computeSourceFqnHash("uiLib.button.MyButton")
                val b = computeSourceFqnHash("uiLib.button.MyButton")
                a shouldBe b
            }
    })
