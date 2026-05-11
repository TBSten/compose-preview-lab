package me.tbsten.compose.preview.lab.compiler.feature.previewCollection.fir.hintAndMarkerGeneration

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.feature.previewHintMarkerNames
import me.tbsten.compose.preview.lab.compiler.isAtLeast
import java.io.File

/**
 * Records the answer to "should the per-declaration marker interface be `sealed`?"
 * via executable evidence rather than a doc-only verdict.
 *
 * **Background**: PR #176 review suggested making the marker `sealed` so external
 * modules cannot implement it. PR #180 (the immediate parent of this branch) instead
 * attaches `@Deprecated(level = HIDDEN)` to every marker, which removes the symbol
 * from scope-based name resolution in consumer modules.
 *
 * The sealed-ization follow-up
 * (`.local/ticket/followup-sealed-hint-marker-interface.md`) is the open question:
 * *given* `@Deprecated(HIDDEN)`, does a consumer attempting `class X : PreviewHintMarker_<...>`
 * already fail to compile? If yes, sealing the marker is redundant — close the
 * ticket as wontfix. If no, follow up with the actual sealed-modality change.
 *
 * The first test below closes the question by asserting that the consumer compile
 * does fail with a "is invisible" / "is hidden" diagnostic when it tries to extend
 * a HIDDEN-deprecated marker. The second test serves as a positive control: an
 * un-related class with no inheritance compiles without trouble.
 *
 * **Skipped on Kotlin < 2.3.21**: per-declaration hint generation only runs on Kotlin
 * 2.3.21 and later, so the question this test answers does not apply on older versions.
 */
class PreviewHintMarkerSealOrHiddenTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        val testKotlinVersion = System.getProperty("test.kotlin.version") ?: "2.3.21"
        val supports = testKotlinVersion.isAtLeast(2, 3, 20)

        test("HIDDEN alone keeps consumers from extending the marker (sealed-ization is redundant)")
            .config(enabled = supports) {
                // Stage 1: lib emits PreviewHintMarker_uilib_Foo_<hash>.
                val libResult = base.compile(
                    SourceFile.kotlin(
                        "UiLib.kt",
                        """
                        package uilib

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun Foo() {}
                        """,
                    ),
                )
                libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val markerFqn = libResult.outputDirectory.singleHintMarkerFqn()

                // Stage 2: a consumer module tries to extend the marker. Without HIDDEN
                // this would compile cleanly (the marker is `public abstract interface`).
                // With HIDDEN, name resolution should reject the reference outright.
                val appResult = base.compile(
                    SourceFile.kotlin(
                        "App.kt",
                        """
                        package app

                        class MyMarker : $markerFqn
                        """,
                    ),
                    extraClasspaths = listOf(libResult.outputDirectory),
                )

                // The compile must end in a *user-facing* compile error, not an
                // internal compiler crash — `INTERNAL_ERROR` would also satisfy a plain
                // `shouldNotBe OK`, and it might mask a regression where the plugin
                // throws inside the FIR generator. Pin to `COMPILATION_ERROR` so the
                // wider symptom (compile failure of any flavour) cannot accidentally
                // satisfy this test.
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
                // Different Kotlin patches phrase the diagnostic slightly differently
                // ("is invisible" / "is hidden" / "Cannot access ..."); we accept any of
                // the canonical wordings so this test stays green across the supported
                // version range.
                val msg = appResult.messages
                val canonicalErrorPhrases = listOf("invisible", "hidden", "cannot access", "deprecated")
                val matched = canonicalErrorPhrases.any { phrase ->
                    msg.contains(phrase, ignoreCase = true)
                }
                matched shouldBe true
            }

        test("control: consumer that does NOT touch the marker still compiles")
            .config(enabled = supports) {
                // Sanity check: HIDDEN should *only* gate references to the marker, not
                // the entire dependency. If this were to fail, the test above would
                // catch a false positive.
                val libResult = base.compile(
                    SourceFile.kotlin(
                        "UiLib.kt",
                        """
                        package uilib

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun Foo() {}
                        """,
                    ),
                )
                libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val appResult = base.compile(
                    SourceFile.kotlin(
                        "App.kt",
                        """
                        package app

                        class Plain
                        """,
                    ),
                    extraClasspaths = listOf(libResult.outputDirectory),
                )
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK
            }
    })

private fun File.singleHintMarkerFqn(): String = previewHintMarkerNames().single()
