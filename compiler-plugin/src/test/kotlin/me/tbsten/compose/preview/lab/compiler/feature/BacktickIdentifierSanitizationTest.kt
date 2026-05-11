package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.PreviewHintMarkerPrefix
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.buildMarkerShortName
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.buildPreviewHintCanonicalKey
import me.tbsten.compose.preview.lab.compiler.feature.previewCollection.computeHintHash
import me.tbsten.compose.preview.lab.compiler.isAtLeast

/**
 * Verifies that the marker class name sanitization handles `@Preview` functions whose
 * `kotlinFqName` contains characters that are valid in Kotlin source (via
 * backtick-quoting) but illegal as a JVM/KLIB identifier — e.g. spaces, hyphens,
 * non-ASCII letters.
 *
 * Without sanitization, `Name.identifier(...)` would throw and the entire compilation
 * would fail when a developer ships `` fun `my preview`() {} ``. We expect the marker
 * name to fall in `[A-Za-z0-9_]+` regardless of input.
 */
class BacktickIdentifierSanitizationTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        val testKotlinVersion = System.getProperty("test.kotlin.version") ?: "2.3.21"
        val supports = testKotlinVersion.isAtLeast(2, 3, 20)

        test("buildMarkerShortName replaces every non-identifier character with '_'") {
            val identifierLegal = Regex("^$PreviewHintMarkerPrefix[A-Za-z0-9_]+\$")

            assertSoftly {
                // Plain identifier: dots become underscores.
                buildMarkerShortName("uilib.button.MyButton", "a3k9z2x1") shouldBe
                    "PreviewHintMarker_uilib_button_MyButton_a3k9z2x1"

                // Backtick-quoted name with space.
                buildMarkerShortName("uilib.`my preview`", "h4sh1234") shouldMatch identifierLegal
                buildMarkerShortName("uilib.`my preview`", "h4sh1234") shouldBe
                    "PreviewHintMarker_uilib__my_preview__h4sh1234"

                // Hyphens and other punctuation.
                buildMarkerShortName("ui-lib.foo-bar", "h4sh1234") shouldMatch identifierLegal

                // Non-ASCII letters (Kotlin allows kana/kanji identifiers).
                buildMarkerShortName("uilib.プレビュー", "h4sh1234") shouldMatch identifierLegal
            }
        }

        test("@Preview with a backtick-quoted name compiles without crashing the marker generator")
            .config(enabled = supports) {
                val result = base.compile(
                    SourceFile.kotlin(
                        "BacktickPreview.kt",
                        """
                        package test.source

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun `my preview`() {}
                        """.trimIndent(),
                    ),
                )
                result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                // The hash is computed against the canonical key built from the *plain* FQN
                // (Kotlin's `kotlinFqName` strips the backticks).
                val expectedHash = computeHintHash(
                    buildPreviewHintCanonicalKey("test.source.my preview", emptyList()),
                )
                val expectedMarker = buildMarkerShortName("test.source.my preview", expectedHash)
                val expectedHintFile = "me/tbsten/compose/preview/lab/hints/PreviewHint_${expectedHash}Kt.class"
                val expectedMarkerFile = "me/tbsten/compose/preview/lab/hints/$expectedMarker.class"

                val classFiles = result.outputDirectory.walkTopDown()
                    .filter { it.isFile && it.name.endsWith(".class") }
                    .map { it.relativeTo(result.outputDirectory).path }
                    .toList()

                assertSoftly {
                    // Sanitized marker name is identifier-legal.
                    expectedMarker shouldMatch Regex("^[A-Za-z0-9_]+\$")
                    // Both the hint file facade and the marker class are emitted.
                    classFiles shouldExist { it == expectedHintFile }
                    classFiles shouldExist { it == expectedMarkerFile }
                }
            }
    })
