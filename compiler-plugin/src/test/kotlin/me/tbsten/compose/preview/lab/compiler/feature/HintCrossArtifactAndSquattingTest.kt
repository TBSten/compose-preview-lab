package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain as shouldContainElement
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.isAtLeast
import com.tschuchort.compiletesting.KotlinCompilation

/**
 * Namespace-squatting prevention end-to-end test for
 * `me.tbsten.compose.preview.lab.compiler.ir.discoverHints`.
 *
 * **Note on the cross-artifact dedup case** (the second leg of the corresponding ticket):
 * the IR-side `discoverHints` does emit a compile-time warning when
 * `pluginContext.referenceFunctions` returns two or more symbols sharing a marker FQN,
 * but exercising that path through kctfork is not practical:
 * - On JVM the classloader / Kotlin symbol provider resolves duplicate FQNs to the
 *   first classpath root, so `referenceFunctions` returns a single symbol even when
 *   two artifacts ship the same hint class.
 * - On KLIB the linker collapses the same IdSignature down to a single symbol with
 *   identical effect.
 *
 * The warning therefore fires only in unusual environments where the symbol provider
 * surfaces both declarations (e.g. some incremental-compile partial states); the user
 * already gets the runtime `distinctPreviewsById` signal in the common case. The
 * cross-artifact KDoc on `distinctPreviewsById` documents this honestly.
 *
 * **Skipped on Kotlin < 2.3.21**: the per-declaration hint pipeline only runs on
 * Kotlin 2.3.21+ (see `CompatContext.supportsKlibCrossModuleHint`).
 */
class HintCrossArtifactAndSquattingTest :
    FunSpec(
        {
            val base = CompilerPluginTestBase()
            val testKotlinVersion = System.getProperty("test.kotlin.version") ?: "2.3.21"
            val supports = testKotlinVersion.isAtLeast(2, 3, 21)

            test("squatting prevention: a non-plugin-emitted function in the hints package is skipped with a warning")
                .config(enabled = supports) {
                    // Squatter dep: manually declares a function shaped like a preview hint
                    // — same package, same name pattern, same return type, same marker-shape
                    // parameter — but the marker class and the function are user code, so
                    // neither carries `@SyntheticPreviewHint`. (The plugin never auto-emits
                    // a marker / hint pair here because the source has no `@Preview`.)
                    val squatter = base.compile(
                        SourceFile.kotlin(
                            "Squatter.kt",
                            """
                            package me.tbsten.compose.preview.lab.hints

                            import me.tbsten.compose.preview.lab.CollectedPreview

                            interface PreviewHintMarker_squatter_Fake_deadbeef

                            fun previewHint_default(
                                value: PreviewHintMarker_squatter_Fake_deadbeef?,
                            ): CollectedPreview = CollectedPreview(
                                id = "squatter.Injected",
                                displayName = "squatter.Injected",
                                filePath = "fake",
                                startLineNumber = 0,
                                endLineNumber = 0,
                                code = "",
                                kdoc = null,
                                content = {},
                            )
                            """,
                        ),
                        moduleName = "squatter",
                    )
                    squatter.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    // App with the squatter on classpath. The IR-side discovery filters out
                    // the squatter's hint (no `@SyntheticPreviewHint`) and emits a warning.
                    val app = base.compile(
                        SourceFile.kotlin(
                            "App.kt",
                            """
                            package app

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun AppPreview() {}
                            """,
                        ),
                        base.collectAllModulePreviewsEntry("appPreviews"),
                        extraClasspaths = listOf(squatter.outputDirectory),
                    )
                    app.exitCode shouldBe KotlinCompilation.ExitCode.OK
                    app.messages shouldContain "missing the @SyntheticPreviewHint marker"
                    app.messages shouldContain "PreviewHintMarker_squatter_Fake_deadbeef"

                    // The squatter preview must NOT end up in the aggregated list.
                    val previews = app.classLoader
                        .loadClass("test.entry.EntryKt")
                        .getMethod("getAppPreviews")
                        .invoke(null) as List<*>

                    @Suppress("UNCHECKED_CAST")
                    val ids = previews.map { p ->
                        p!!::class.members.first { m -> m.name == "id" }.call(p) as String
                    }
                    ids shouldContainElement "app.AppPreview"
                    ids.none { it == "squatter.Injected" } shouldBe true
                }

            test("baseline: a plain dep without squatters does not trigger any namespace-squatting warning")
                .config(enabled = supports) {
                    val lib = base.compile(
                        SourceFile.kotlin(
                            "Lib.kt",
                            """
                            package uilib

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun OnlyPreview() {}
                            """,
                        ),
                        moduleName = "single-lib",
                    )
                    lib.exitCode shouldBe KotlinCompilation.ExitCode.OK

                    val app = base.compile(
                        SourceFile.kotlin(
                            "App.kt",
                            """
                            package app

                            @org.jetbrains.compose.ui.tooling.preview.Preview
                            fun AppPreview() {}
                            """,
                        ),
                        base.collectAllModulePreviewsEntry("appPreviews"),
                        extraClasspaths = listOf(lib.outputDirectory),
                    )
                    app.exitCode shouldBe KotlinCompilation.ExitCode.OK
                    app.messages shouldNotContain "missing the @SyntheticPreviewHint marker"
                    app.messages shouldNotContain "share marker"
                }
        },
    )
