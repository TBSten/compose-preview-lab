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
 * Validates that invalid scope inputs are rejected with a clear compile-time error rather
 * than producing silently-broken hint emission or empty buckets.
 *
 * Two layers reject invalid input — both surface a `[ComposePreviewLab/<phase>,...]`
 * prefix in the build output (FIR side renders `[ComposePreviewLab/FIR,...]`; IR side
 * renders `[ComposePreviewLab/IR,...]`), but they target different shapes of mistake and
 * run at different points in the pipeline:
 *
 * - **FIR `CollectScopeAnnotationChecker` / `CollectScopeCallChecker`** — runs at the
 *   FIR analysis (`CHECKERS`) phase, so errors surface in the IDE as red squigglies and
 *   in `gradle build` before any IR generation runs. Rejects literals that violate
 *   `[A-Za-z0-9_]+` (in both `@ComposePreviewLabOption(collectScopes = [...])` annotations
 *   and `collect[All]ModulePreviews(scope = "...")` call sites) plus call-site arguments
 *   that are clearly non-literal (string concatenations, function calls).
 * - **IR `PreviewLabIrBodyFiller`** — second-line check for call-site `scope` arguments
 *   that look like a property access (`val design`). At FIR analysis time we cannot tell
 *   a `const val` reference (legal — inlined to `IrConst<String>`) from a non-`const`
 *   `val` reference (illegal — never inlined), so the IR pass — which sees the
 *   const-folded result — is where this gets caught.
 *
 * **Test gating**: cases that depend on FIR-emitted hints only run on Kotlin 2.3.21+
 * ([CompatContext.supportsKlibCrossModuleHint]). The annotation-side checker tests do not
 * need the hint generator to be active — the checker runs unconditionally — but they are
 * gated on 2.3.21+ anyway because the rest of the test suite is.
 */
class PreviewHintScopeValidationTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        val testKotlinVersion = System.getProperty("test.kotlin.version") ?: "2.3.21"
        val supports = testKotlinVersion.isAtLeast(2, 3, 20)

        context("annotation-side scope shape (FIR)") {
            test("collectScope with a space is rejected with an actionable error")
                .config(enabled = supports) {
                    val result = base.compile(
                        SourceFile.kotlin(
                            "Mod.kt",
                            """
                            package self

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(collectScopes = ["has space"])
                            fun A() {}
                            """,
                        ),
                    )
                    assertSoftly {
                        result.exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
                        result.messages shouldContain "[ComposePreviewLab/"
                        result.messages shouldContain "has space"
                        result.messages shouldContain "[A-Za-z0-9_]+"
                    }
                }

            test("collectScope with a hyphen is rejected")
                .config(enabled = supports) {
                    val result = base.compile(
                        SourceFile.kotlin(
                            "Mod.kt",
                            """
                            package self

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(collectScopes = ["with-hyphen"])
                            fun A() {}
                            """,
                        ),
                    )
                    assertSoftly {
                        result.exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
                        result.messages shouldContain "with-hyphen"
                    }
                }

            test("digits-only scope is allowed (still matches [A-Za-z0-9_]+)")
                .config(enabled = supports) {
                    val result = base.compile(
                        SourceFile.kotlin(
                            "Mod.kt",
                            """
                            package self

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            @me.tbsten.compose.preview.lab.ComposePreviewLabOption(collectScopes = ["v2"])
                            fun A() {}
                            """,
                        ),
                    )
                    result.exitCode shouldBe KotlinCompilation.ExitCode.OK
                }
        }

        context("call-site scope shape (IR)") {
            test("collectModulePreviews(scope = \"has space\") is rejected at the call site") {
                val result = base.compile(
                    SourceFile.kotlin(
                        "Entry.kt",
                        """
                        package test.entry

                        import me.tbsten.compose.preview.lab.collectModulePreviews

                        val previews by collectModulePreviews(scope = "has space")
                        """,
                    ),
                )
                assertSoftly {
                    result.exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
                    result.messages shouldContain "[ComposePreviewLab/"
                    // FIR `CollectScopeCallChecker` flags the offending literal directly,
                    // so the rendered message names the value plus the regex rule.
                    result.messages shouldContain "has space"
                    result.messages shouldContain "[A-Za-z0-9_]+"
                }
            }

            test(
                "collectModulePreviews(scope = COMPILE_TIME_CONST) is accepted (const val is inlined to a literal at IR-pass time)"
            ) {
                // `private const val` references resolve to an IrConst by the time the
                // compiler plugin's IR generation extension runs, so they look identical to
                // a literal scope. We treat that as the same as a hand-written literal —
                // simpler for users and consistent with what the IR sees.
                val result = base.compile(
                    SourceFile.kotlin(
                        "Entry.kt",
                        """
                        package test.entry

                        import me.tbsten.compose.preview.lab.collectModulePreviews

                        private const val DESIGN = "design"
                        val previews by collectModulePreviews(scope = DESIGN)
                        """,
                    ),
                )
                result.exitCode shouldBe KotlinCompilation.ExitCode.OK
            }

            test("collectModulePreviews(scope = \"a\" + \"b\") is rejected as non-literal") {
                val result = base.compile(
                    SourceFile.kotlin(
                        "Entry.kt",
                        """
                        package test.entry

                        import me.tbsten.compose.preview.lab.collectModulePreviews

                        val previews by collectModulePreviews(scope = "a" + "b")
                        """,
                    ),
                )
                assertSoftly {
                    result.exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
                    result.messages shouldContain "[ComposePreviewLab/"
                    result.messages shouldContain "inline string literal"
                }
            }

            test("collectAllModulePreviews(scope = NON_CONST_VAL) is rejected as non-literal")
                .config(enabled = supports) {
                    // A regular `val` (not `const val`) does NOT get inlined and reaches the
                    // IR pass as an `IrCall` to its property getter, so the compiler plugin
                    // can no longer treat it as a known literal.
                    val result = base.compile(
                        SourceFile.kotlin(
                            "Entry.kt",
                            """
                            package test.entry

                            import me.tbsten.compose.preview.lab.collectAllModulePreviews

                            private val design: String = "design"
                            val previews by collectAllModulePreviews(scope = design)
                            """,
                        ),
                    )
                    assertSoftly {
                        result.exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
                        result.messages shouldContain "collectAllModulePreviews"
                        result.messages shouldContain "inline string literal"
                    }
                }
        }
    })
