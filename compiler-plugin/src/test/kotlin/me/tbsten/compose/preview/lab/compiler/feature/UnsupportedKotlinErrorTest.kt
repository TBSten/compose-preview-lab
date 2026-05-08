package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.isAtLeast

/**
 * Verifies that calling `collectAllModulePreviews()` on a Kotlin compiler version below
 * 2.3.21 surfaces a structured `ERROR` diagnostic via `MessageCollector`, instead of
 * silently emitting a half-broken IR.
 *
 * This is the negative counterpart to the version-gated tests in
 * `PreviewHintEmissionTest` / `PreviewHintDiscoveryTest`: those tests run only on
 * Kotlin 2.3.21+; this test runs **only on older Kotlin** because the smoke matrix
 * (`scripts/compiler-plugin-test.sh`) swaps in different `kotlin-compiler-embeddable`
 * versions to exercise the older code path.
 */
class UnsupportedKotlinErrorTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        val testKotlinVersion = System.getProperty("test.kotlin.version") ?: "2.3.21"
        val supportsHint = testKotlinVersion.isAtLeast(2, 3, 21)

        test("collectAllModulePreviews() on unsupported Kotlin reports a clear ERROR diagnostic")
            .config(enabled = !supportsHint) {
                val result = base.compile(
                    SourceFile.kotlin(
                        "App.kt",
                        """
                        package test.app

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun AppPreview() {}
                        """.trimIndent(),
                    ),
                    base.collectAllModulePreviewsEntry("appPreviews"),
                )

                assertSoftly {
                    result.exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
                    // The error message must be specific enough that a user can act on it.
                    result.messages shouldContain "[ComposePreviewLab]"
                    result.messages shouldContain "collectAllModulePreviews()"
                    result.messages shouldContain "Kotlin 2.3.21"
                    // And it should mention the workaround.
                    result.messages shouldContain "collectModulePreviews()"
                }
            }

        test("collectModulePreviews() (single-module) still works on unsupported Kotlin")
            .config(enabled = !supportsHint) {
                // The version gate only blocks the cross-module API. Single-module
                // collection has no KLIB-hint dependency and should keep working on
                // older Kotlin so users can incrementally adopt features.
                val result = base.compile(
                    SourceFile.kotlin(
                        "App.kt",
                        """
                        package test.app

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun AppPreview() {}
                        """.trimIndent(),
                    ),
                    base.collectModulePreviewsEntry("modulePreviews"),
                )
                result.exitCode shouldBe KotlinCompilation.ExitCode.OK
            }
    })
