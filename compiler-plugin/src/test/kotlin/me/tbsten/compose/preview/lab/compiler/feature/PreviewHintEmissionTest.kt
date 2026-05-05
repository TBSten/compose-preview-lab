package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.fir.buildPreviewHintCanonicalKey
import me.tbsten.compose.preview.lab.compiler.fir.computeHintHash

/**
 * Per-declaration hint generator (Metro 風) が `@Preview` 1 個に対し
 * (a) `interface PreviewHintMarker_<hash>` (b) `fun previewHint(value: PreviewHintMarker_<hash>?): CollectedPreview`
 * を 1 セット emit し、 IR で hint 関数の body に `CollectedPreview(...)` constructor 呼び出しを
 * 埋め込んでいることを検証する。
 *
 * 関数名は固定 (`previewHint`) で marker class が IdSignature を per-`@Preview` 区別する
 * fixed-name + marker pattern。 cross-module discovery は `referenceFunctions(fixed-name)` で
 * 全 hint を発見する。
 *
 * **Skipped on Kotlin < 2.3.21**: hint generator は Kotlin 2.3.21+ でのみ稼働
 * ([CompatContext.supportsKlibCrossModuleHint][me.tbsten.compose.preview.lab.compiler.compat.CompatContext.supportsKlibCrossModuleHint])。
 */
class PreviewHintEmissionTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        val testKotlinVersion = System.getProperty("test.kotlin.version") ?: "2.3.21"
        val supports = testKotlinVersion.isAtLeast(2, 3, 21)

        test("@Preview 1 個 → marker (PreviewHintMarker_<hash>) + previewHint(marker?) 1 セット emit される")
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

                val expectedHash = computeHintHash(
                    buildPreviewHintCanonicalKey("test.source.MyButton", emptyList()),
                )
                // FIR generator は file 名 `PreviewHint_<hash>.kt` で emit するので、
                // file facade class は `PreviewHint_<hash>Kt`、 marker class は同 package の
                // `PreviewHintMarker_<hash>` で並ぶ。
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

                val expectedHash = computeHintHash(
                    buildPreviewHintCanonicalKey("test.source.MyButton", emptyList()),
                )
                val hintFacade = result.classLoader
                    .loadClass("me.tbsten.compose.preview.lab.hints.PreviewHint_${expectedHash}Kt")
                // hint 関数名は固定 (`previewHint`) で、 marker class param で区別する。
                // Java reflection からは marker param 型の Class object を渡して getMethod する。
                val markerClass = result.classLoader
                    .loadClass("me.tbsten.compose.preview.lab.hints.PreviewHintMarker_$expectedHash")
                val hintMethod = hintFacade.getMethod("previewHint", markerClass)

                // hint(null) を invoke すると CollectedPreview インスタンスが返る
                val collected = hintMethod.invoke(null, null)
                checkNotNull(collected) { "hint 関数が null を返した" }

                val collectedClass = collected.javaClass
                val id = collectedClass.getMethod("getId").invoke(collected) as String
                val displayName = collectedClass.getMethod("getDisplayName").invoke(collected) as String

                id shouldBe "test.source.MyButton"
                displayName shouldBe "test.source.MyButton"
            }

        test("@Preview が複数あれば hint が複数 emit され、 hash は別 (overload も signature で区別)")
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

                val firstHash = computeHintHash(buildPreviewHintCanonicalKey("test.source.First", emptyList()))
                val secondHash = computeHintHash(buildPreviewHintCanonicalKey("test.source.Second", emptyList()))
                // 別 sourceFqn なので別 hash → 別 hint 関数として共存
                firstHash shouldNotBe secondHash

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

        test("同名 overload を canonical key の signature で区別する (hash unit-level)") {
            // hint canonical key 自体が parameter signature を含むので、 同 sourceFqn でも
            // 異なる signature なら異なる hash になることを確認。
            // (parameterized `@Preview` を実際に compile した end-to-end test は orthogonal な
            //  「lambda body が param 付き関数を call できない」 課題があるため別 ticket で扱う)
            val noArgHash = computeHintHash(buildPreviewHintCanonicalKey("test.source.MyButton", emptyList()))
            val withStringArgHash = computeHintHash(
                buildPreviewHintCanonicalKey("test.source.MyButton", listOf("kotlin.String")),
            )
            val withIntArgHash = computeHintHash(
                buildPreviewHintCanonicalKey("test.source.MyButton", listOf("kotlin.Int")),
            )
            val withNullableStringArgHash = computeHintHash(
                buildPreviewHintCanonicalKey("test.source.MyButton", listOf("kotlin.String?")),
            )
            noArgHash shouldNotBe withStringArgHash
            withStringArgHash shouldNotBe withIntArgHash
            withStringArgHash shouldNotBe withNullableStringArgHash
        }

        test("hash 関数が canonical key で再現可能 (projectRootPath 非依存)")
            .config(enabled = supports) {
                val key = buildPreviewHintCanonicalKey("uiLib.button.MyButton", emptyList())
                val a = computeHintHash(key)
                val b = computeHintHash(key)
                a shouldBe b
            }
    })

/**
 * `"2.3.9"` 等の lexicographic 比較で誤って `>= 2.3.21` 判定される問題を避けるため、
 * `MAJOR.MINOR.PATCH` を numeric 比較する簡易 helper。
 *
 * **Sample**: `"2.3.21".isAtLeast(2, 3, 21)` → `true`、 `"2.3.9".isAtLeast(2, 3, 21)` → `false`、
 * `"2.4.0".isAtLeast(2, 3, 21)` → `true`、 `"2.3.21-Beta1".isAtLeast(2, 3, 21)` → `true`
 * (suffix は無視)。
 */
private fun String.isAtLeast(major: Int, minor: Int, patch: Int): Boolean {
    val parts = substringBefore('-').split('.')
    val v0 = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val v1 = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val v2 = parts.getOrNull(2)?.toIntOrNull() ?: 0
    return when {
        v0 != major -> v0 > major
        v1 != minor -> v1 > minor
        else -> v2 >= patch
    }
}
