package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.fir.buildMarkerShortName
import me.tbsten.compose.preview.lab.compiler.fir.buildPreviewHintCanonicalKey
import me.tbsten.compose.preview.lab.compiler.fir.computeHintHash
import me.tbsten.compose.preview.lab.compiler.isAtLeast

/**
 * Verifies that the per-declaration hint generator emits, for each `@Preview`,
 * (a) `interface PreviewHintMarker_<sanitized_fqn>_<hash>` and
 * (b) `fun previewHint(value: PreviewHintMarker_<sanitized_fqn>_<hash>?): CollectedPreview`
 * as a pair, and that the IR pass fills the hint function body with the corresponding
 * `CollectedPreview(...)` constructor call.
 *
 * The hint function name is fixed (`previewHint`); the marker class on the parameter
 * disambiguates the IdSignature per `@Preview` (the fixed-name + marker pattern).
 * Cross-module discovery walks every hint via `referenceFunctions(fixed-name)`.
 *
 * **Skipped on Kotlin < 2.3.21**: the hint generator only runs on Kotlin 2.3.21+
 * ([CompatContext.supportsKlibCrossModuleHint][me.tbsten.compose.preview.lab.compiler.compat.CompatContext.supportsKlibCrossModuleHint]).
 */
class PreviewHintEmissionTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        val testKotlinVersion = System.getProperty("test.kotlin.version") ?: "2.3.21"
        val supports = testKotlinVersion.isAtLeast(2, 3, 20)

        test("emits one (marker, previewHint(marker?)) pair per @Preview")
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
                // The FIR generator emits the file as `PreviewHint_<hash>.kt`, so its
                // file-facade class is `PreviewHint_<hash>Kt` and the marker class lives
                // alongside it as `PreviewHintMarker_<sanitized_fqn>_<hash>`.
                val expectedClassFile = "me/tbsten/compose/preview/lab/hints/PreviewHint_${expectedHash}Kt.class"

                val classFiles = result.outputDirectory.walkTopDown()
                    .filter { it.isFile && it.name.endsWith(".class") }
                    .map { it.relativeTo(result.outputDirectory).path }
                    .toList()

                classFiles shouldExist { it == expectedClassFile }
            }

        test("invoking the hint function returns a CollectedPreview with populated metadata")
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
                // The hint function name encodes the scope (`previewHint_<scope>`); the
                // marker class parameter disambiguates calls within a scope. From Java
                // reflection we recover the method by passing the marker param's `Class`
                // object to `getMethod`. Without `@ComposePreviewLabOption(collectScopes = [...])`
                // the scope is `"default"`.
                val markerClass = result.classLoader
                    .loadClass(
                        "me.tbsten.compose.preview.lab.hints.${buildMarkerShortName("test.source.MyButton", expectedHash)}"
                    )
                val hintMethod = hintFacade.getMethod("previewHint_default", markerClass)

                // Invoking hint(null) returns a CollectedPreview instance.
                val collected = hintMethod.invoke(null, null)
                checkNotNull(collected) { "hint function returned null" }

                val collectedClass = collected.javaClass
                val id = collectedClass.getMethod("getId").invoke(collected) as String
                val displayName = collectedClass.getMethod("getDisplayName").invoke(collected) as String

                id shouldBe "test.source.MyButton"
                displayName shouldBe "test.source.MyButton"
            }

        test("multiple @Preview produce multiple hints with distinct hashes (signature also disambiguates overloads)")
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
                // Different sourceFqn → different hash → distinct hint functions coexist.
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

        test("same-name overloads are disambiguated by the canonical-key signature (hash unit-level)") {
            // The hint canonical key itself includes the parameter signature, so two
            // entries with the same sourceFqn but different signatures must hash to
            // different values.
            // (An end-to-end test that actually compiles a parameterized `@Preview` is
            //  blocked on the orthogonal "lambda body cannot call a parameterized
            //  function" issue and is tracked separately.)
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

        test("hash is reproducible from canonical key alone (independent of projectRootPath)")
            .config(enabled = supports) {
                val key = buildPreviewHintCanonicalKey("uiLib.button.MyButton", emptyList())
                val a = computeHintHash(key)
                val b = computeHintHash(key)
                a shouldBe b
            }
    })
