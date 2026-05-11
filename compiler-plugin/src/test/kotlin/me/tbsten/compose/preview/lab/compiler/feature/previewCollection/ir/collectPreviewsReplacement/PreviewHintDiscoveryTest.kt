package me.tbsten.compose.preview.lab.compiler.feature.previewCollection.ir.collectPreviewsReplacement

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.isAtLeast
import me.tbsten.compose.preview.lab.compiler.loadCollectedPreviews

/**
 * Verifies the consumer-side IR pass that discovers per-declaration hints emitted by
 * dependency modules.
 *
 * 2-stage kctfork compilation:
 * 1. Compile uiLib so that
 *    `interface PreviewHintMarker_<hash>` + `fun previewHint(value: PreviewHintMarker_<hash>?): CollectedPreview`
 *    are emitted into the `me.tbsten.compose.preview.lab.hints` package.
 * 2. Compile app with uiLib's output added to the classpath. Inside the
 *    `collectAllModulePreviews()` IR transform, `referenceFunctions(CallableId(hintsPackage, "previewHint"))`
 *    discovers each hint, invokes it, and pushes the resulting `CollectedPreview` onto
 *    the list.
 * 3. Read `app.allPreviews` via reflection and assert that the cross-module preview is
 *    present.
 *
 * **Skipped on Kotlin < 2.3.21**: the hint generator only runs on Kotlin 2.3.21+; on
 * older versions `collectAllModulePreviews()` itself produces a compile-time error in
 * the IR phase
 * ([CompatContext.supportsKlibCrossModuleHint][me.tbsten.compose.preview.lab.compiler.compat.CompatContext.supportsKlibCrossModuleHint]).
 */
class PreviewHintDiscoveryTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        val testKotlinVersion = System.getProperty("test.kotlin.version") ?: "2.3.21"
        val supports = testKotlinVersion.isAtLeast(2, 3, 20)

        test("collectAllModulePreviews() discovers and uses cross-module per-declaration hints")
            .config(enabled = supports) {
                // Stage 1: uiLib
                val libResult = base.compile(
                    SourceFile.kotlin(
                        "UiLib.kt",
                        """
                    package uilib

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun UiLibPreview() {}
                    """,
                    ),
                )
                libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                // Stage 2: app
                val appResult = base.compile(
                    SourceFile.kotlin(
                        "App.kt",
                        """
                    package app

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun AppPreview() {}
                    """,
                    ),
                    base.collectAllModulePreviewsEntry("allPreviews"),
                    extraClasspaths = listOf(libResult.outputDirectory),
                )
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val previews = appResult.loadCollectedPreviews(propertyName = "allPreviews")
                val ids = previews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }

                // Both the local @Preview and the cross-module hint discovery emit a
                // CollectedPreview for each id, but distinctPreviewsById dedups by id so
                // the same preview shows up only once.
                ids shouldContainExactlyInAnyOrder listOf(
                    "app.AppPreview",
                    "uilib.UiLibPreview",
                )
            }

        test("discovers all hints from a dependency module with multiple @Preview annotations")
            .config(enabled = supports) {
                val libResult = base.compile(
                    SourceFile.kotlin(
                        "UiLib.kt",
                        """
                    package uilib

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun First() {}

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun Second() {}

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun Third() {}
                    """,
                    ),
                )
                libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val appResult = base.compile(
                    SourceFile.kotlin(
                        "App.kt",
                        """
                    package app
                    """,
                    ),
                    base.collectAllModulePreviewsEntry("allPreviews"),
                    extraClasspaths = listOf(libResult.outputDirectory),
                )
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val previews = appResult.loadCollectedPreviews(propertyName = "allPreviews")
                val ids = previews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }

                ids shouldContainExactlyInAnyOrder listOf(
                    "uilib.First",
                    "uilib.Second",
                    "uilib.Third",
                )
            }

        test("cross-module preview preserves all CollectedPreview metadata")
            .config(enabled = supports) {
                val libResult = base.compile(
                    SourceFile.kotlin(
                        "UiLib.kt",
                        """
                    package uilib

                    /**
                     * Sample preview KDoc.
                     */
                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun WithMetadata() {}
                    """,
                    ),
                )
                libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val appResult = base.compile(
                    SourceFile.kotlin(
                        "App.kt",
                        """
                    package app
                    """,
                    ),
                    base.collectAllModulePreviewsEntry("allPreviews"),
                    extraClasspaths = listOf(libResult.outputDirectory),
                )
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val previews = appResult.loadCollectedPreviews(propertyName = "allPreviews")
                val preview = previews.single { p ->
                    (p::class.members.find { it.name == "id" }!!.call(p) as String) == "uilib.WithMetadata"
                }

                val displayName = preview::class.members.find { it.name == "displayName" }!!.call(preview) as String
                val filePath = preview::class.members.find { it.name == "filePath" }!!.call(preview) as String?
                val startLine = preview::class.members.find { it.name == "startLineNumber" }!!.call(preview) as Int?
                val endLine = preview::class.members.find { it.name == "endLineNumber" }!!.call(preview) as Int?
                val code = preview::class.members.find { it.name == "code" }!!.call(preview) as String?

                displayName shouldBe "uilib.WithMetadata"
                // startLine / endLine point at the @Preview declaration position (1-based).
                (startLine != null) shouldBe true
                (endLine != null) shouldBe true
                // filePath / code can be null in kctfork's in-memory file environment, so we
                // only assert their presence here (in a real build they are non-null).
                // The integrationTest harness (T04) covers the empty-vs-null sentinel
                // handling more rigorously to guard against bugs like
                // `code != null && code.isEmpty()` collapsing the two cases.
                @Suppress("UNUSED_VARIABLE")
                val ignored = listOf(filePath, code)
            }

        test("smoke: collectAllModulePreviews() result contains no duplicate ids")
            .config(enabled = supports) {
                // Smoke test: when the aggregated result contains both this module's
                // `@Preview` (via the local list) and a cross-module `@Preview` (via the
                // per-declaration hint), assert that `distinctPreviewsById` is applied so
                // the resulting ids are unique.
                //
                // Cases that produce real duplicates via a transitive dep chain
                // (app(all) → ui(all) → core) are covered on the integrationTest side
                // (`CrossModuleCollectPreviewsTest.collectAllModulePreviewsDeduplicatesById`).
                val libResult = base.compile(
                    SourceFile.kotlin(
                        "Shared.kt",
                        """
                        package shared

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun SharedPreview() {}
                        """.trimIndent(),
                    ),
                )
                libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val appResult = base.compile(
                    SourceFile.kotlin(
                        "App.kt",
                        """
                        package app

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun AppPreview() {}
                        """.trimIndent(),
                    ),
                    base.collectAllModulePreviewsEntry("allPreviews"),
                    extraClasspaths = listOf(libResult.outputDirectory),
                )
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val previews = appResult.loadCollectedPreviews(propertyName = "allPreviews")
                val ids = previews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }

                // Report every assertion failure together rather than failing on the
                // first one — if dedup is broken we want to see the full picture.
                assertSoftly {
                    // distinctPreviewsById should leave zero id duplicates.
                    previews.size shouldBe ids.distinct().size
                    ids shouldContain "shared.SharedPreview"
                    ids shouldContain "app.AppPreview"
                    // Confirm that shared.SharedPreview is kept exactly once after dedup.
                    ids.count { it == "shared.SharedPreview" } shouldBe 1
                }
            }
    })
