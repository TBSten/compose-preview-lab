package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.isAtLeast
import me.tbsten.compose.preview.lab.compiler.loadCollectedPreviews

/**
 * **Skipped on Kotlin < 2.3.20**: every test below uses `collectAllModulePreviews()`,
 * which requires the per-`@Preview` hint emission performed by `PreviewHintFirGenerator`.
 * That generator is only registered when `CompatContext.supportsFirHintGeneration()`
 * returns `true` (= Kotlin 2.3.20+). On earlier Kotlin versions hints are not emitted,
 * the aggregation returns an empty list, and the assertions here would fail spuriously.
 *
 * The whole spec block early-returns when the gate is closed so the tests do not
 * register at all (kotest reports them as 0 tests for this class on pre-2.3.20).
 */
class CrossModuleAggregationTest :
    FunSpec(
        body@{
            val testKotlinVersion = System.getProperty("test.kotlin.version") ?: "2.3.21"
            if (!testKotlinVersion.isAtLeast(2, 3, 20)) return@body

            val base = CompilerPluginTestBase()

            /*
             * Verifies that `collectAllModulePreviews()` automatically aggregates the
             * previews from dependency modules **without any manual Gradle wiring**, via
             * a two-stage kctfork compilation.
             *
             * Flow:
             * 1. Compile uiLib1 / uiLib2 independently. (The compiler plugin's FIR
             *    generator emits one
             *    `me.tbsten.compose.preview.lab.hints.previewHint(value: PreviewHintMarker_..._<hash>?): CollectedPreview`
             *    hint per `@Preview`, with a sibling marker interface in the same
             *    package.)
             * 2. Compile app with uiLib1 / uiLib2 outputs added to its classpath. (The
             *    compiler plugin's `collectAllModulePreviews()` rewrite discovers each
             *    hint via `pluginContext.referenceFunctions(CallableId(HINT_PACKAGE, "previewHint"))`,
             *    invokes it with `null`, and folds the returned `CollectedPreview` into
             *    the aggregated list.)
             * 3. Read `app.appPreviews` via reflection and assert that both modules'
             *    previews are present.
             */
            test("collectAllModulePreviews() aggregates two dependency modules' previews without manual wiring") {
                // Stage 1a: uiLib1
                val lib1Result = base.compile(
                    SourceFile.kotlin(
                        "UiLib1.kt",
                        """
                    package uilib1

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun UiLib1Preview1() {}

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun UiLib1Preview2() {}

                    val uiLib1Previews by me.tbsten.compose.preview.lab.collectModulePreviews()
                    """,
                    ),
                )
                lib1Result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                // Stage 1b: uiLib2
                val lib2Result = base.compile(
                    SourceFile.kotlin(
                        "UiLib2.kt",
                        """
                    package uilib2

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun UiLib2Preview() {}

                    val uiLib2Previews by me.tbsten.compose.preview.lab.collectModulePreviews()
                    """,
                    ),
                )
                lib2Result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                // Stage 2: app — compile with uiLib1/uiLib2 outputs added to the classpath.
                val appResult = base.compile(
                    SourceFile.kotlin(
                        "App.kt",
                        """
                    package app

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun AppPreview() {}
                    """,
                    ),
                    base.collectAllModulePreviewsEntry("appPreviews"),
                    extraClasspaths = listOf(lib1Result.outputDirectory, lib2Result.outputDirectory),
                )
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                // Assert: appPreviews contains 4 entries total — app + uiLib1 (×2) + uiLib2.
                val previews = appResult.loadCollectedPreviews(propertyName = "appPreviews")
                val ids = previews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }
                ids shouldContainExactlyInAnyOrder listOf(
                    "app.AppPreview",
                    "uilib1.UiLib1Preview1",
                    "uilib1.UiLib1Preview2",
                    "uilib2.UiLib2Preview",
                )
            }

            test("collectAllModulePreviews() works on a standalone module with no dependencies") {
                val result = base.compile(
                    SourceFile.kotlin(
                        "App.kt",
                        """
                    package app

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun AppPreview() {}
                    """,
                    ),
                    base.collectAllModulePreviewsEntry("appPreviews"),
                )
                result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val previews = result.loadCollectedPreviews(propertyName = "appPreviews")
                previews.size shouldBe 1
            }

            test("a dependency module that itself uses collectAllModulePreviews() still gets aggregated upstream") {
                // Stage 1: uiLib — this module itself calls collectAllModulePreviews().
                // (It has no dependencies, so uiLibPreviews contains only UiLibPreview.)
                val uiLibResult = base.compile(
                    SourceFile.kotlin(
                        "UiLib.kt",
                        """
                    package uilib

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun UiLibPreview() {}

                    val uiLibPreviews by me.tbsten.compose.preview.lab.collectAllModulePreviews()
                    """,
                    ),
                )
                uiLibResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                // Stage 2: app — add uiLib to the classpath and aggregate with collectAllModulePreviews().
                val appResult = base.compile(
                    SourceFile.kotlin(
                        "App.kt",
                        """
                    package app

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun AppPreview() {}
                    """,
                    ),
                    base.collectAllModulePreviewsEntry("appPreviews"),
                    extraClasspaths = listOf(uiLibResult.outputDirectory),
                )
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val previews = appResult.loadCollectedPreviews(propertyName = "appPreviews")
                val ids = previews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }
                ids shouldContainExactlyInAnyOrder listOf(
                    "app.AppPreview",
                    "uilib.UiLibPreview",
                )
            }

            test("a dependency module with @Preview but no collectModulePreviews() is still auto-collected") {
                // Even if the child module never writes
                // `val xxxPreviews by collectModulePreviews()`, the compiler plugin
                // synthesizes an auto-provider function plus a hint, which the app's
                // `collectAllModulePreviews()` then discovers.
                val libResult = base.compile(
                    SourceFile.kotlin(
                        "UiLib.kt",
                        """
                    package uilib

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun UiLibPreview1() {}

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun UiLibPreview2() {}

                    // Intentionally no collectModulePreviews() call.
                    """,
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
                    """,
                    ),
                    base.collectAllModulePreviewsEntry("appPreviews"),
                    extraClasspaths = listOf(libResult.outputDirectory),
                )
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val previews = appResult.loadCollectedPreviews(propertyName = "appPreviews")
                val ids = previews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }
                ids shouldContainExactlyInAnyOrder listOf(
                    "app.AppPreview",
                    "uilib.UiLibPreview1",
                    "uilib.UiLibPreview2",
                )
            }

            test("auto-collect also works for modules in the default (no-declared) package") {
                // Verifies that the provider-name generation path also handles files
                // without a package declaration.
                // (sanitizedPkg falls back to `DEFAULT_PACKAGE_TOKEN`.)
                val libResult = base.compile(
                    SourceFile.kotlin(
                        "RootPkgLib.kt",
                        """
                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun RootPreview() {}
                    """,
                    ),
                    moduleName = "rootPkgLib",
                )
                libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val appResult = base.compile(
                    SourceFile.kotlin(
                        "App.kt",
                        """
                    package app

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun AppPreview() {}
                    """,
                    ),
                    base.collectAllModulePreviewsEntry("appPreviews"),
                    extraClasspaths = listOf(libResult.outputDirectory),
                    moduleName = "app",
                )
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val previews = appResult.loadCollectedPreviews(propertyName = "appPreviews")
                val ids = previews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }
                ids shouldContainExactlyInAnyOrder listOf("app.AppPreview", "RootPreview")
            }

            test("@Previews spread across multiple packages within one module are all auto-collected") {
                // The provider function name is derived from the first preview's package,
                // but the body must include every preview regardless of package.
                val libResult = base.compile(
                    SourceFile.kotlin(
                        "FeatureA.kt",
                        """
                    package multilib.featureA

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun FeatureAPreview() {}
                    """,
                    ),
                    SourceFile.kotlin(
                        "FeatureB.kt",
                        """
                    package multilib.featureB

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun FeatureBPreview() {}
                    """,
                    ),
                    moduleName = "multiPkgLib",
                )
                libResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val appResult = base.compile(
                    SourceFile.kotlin(
                        "App.kt",
                        """
                    package app

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun AppPreview() {}
                    """,
                    ),
                    base.collectAllModulePreviewsEntry("appPreviews"),
                    extraClasspaths = listOf(libResult.outputDirectory),
                    moduleName = "app",
                )
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val previews = appResult.loadCollectedPreviews(propertyName = "appPreviews")
                val ids = previews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }
                ids shouldContainExactlyInAnyOrder listOf(
                    "app.AppPreview",
                    "multilib.featureA.FeatureAPreview",
                    "multilib.featureB.FeatureBPreview",
                )
            }

            test("two auto-collect dependency modules that share the same package are both collected") {
                // Covers the case where multiple dependency modules use the same package
                // name (e.g. a multi-module project where modules share a base package).
                // Guards against regressions where the provider function name was derived
                // from only the package and one preview silently disappeared due to a
                // name collision.
                val libAResult = base.compile(
                    SourceFile.kotlin(
                        "LibA.kt",
                        """
                    package shared

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun LibAPreview() {}
                    """,
                    ),
                    moduleName = "libA",
                )
                libAResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val libBResult = base.compile(
                    SourceFile.kotlin(
                        "LibB.kt",
                        """
                    package shared

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun LibBPreview() {}
                    """,
                    ),
                    moduleName = "libB",
                )
                libBResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val appResult = base.compile(
                    SourceFile.kotlin(
                        "App.kt",
                        """
                    package app

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun AppPreview() {}
                    """,
                    ),
                    base.collectAllModulePreviewsEntry("appPreviews"),
                    extraClasspaths = listOf(libAResult.outputDirectory, libBResult.outputDirectory),
                    moduleName = "app",
                )
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val previews = appResult.loadCollectedPreviews(propertyName = "appPreviews")
                val ids = previews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }
                ids shouldContainExactlyInAnyOrder listOf(
                    "app.AppPreview",
                    "shared.LibAPreview",
                    "shared.LibBPreview",
                )
            }

            test("auto-collect and manual collectModulePreviews() modules can be mixed without duplicates") {
                // Stage 1: manualLib — explicitly writes collectModulePreviews().
                val manualLibResult = base.compile(
                    SourceFile.kotlin(
                        "ManualLib.kt",
                        """
                    package manuallib

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun ManualLibPreview() {}

                    val manualLibPreviews by me.tbsten.compose.preview.lab.collectModulePreviews()
                    """,
                    ),
                )
                manualLibResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                // Stage 2: autoLib — no collectModulePreviews() call (auto-hint path).
                val autoLibResult = base.compile(
                    SourceFile.kotlin(
                        "AutoLib.kt",
                        """
                    package autolib

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun AutoLibPreview() {}
                    """,
                    ),
                )
                autoLibResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                // Stage 3: app — add both modules to the classpath.
                val appResult = base.compile(
                    SourceFile.kotlin(
                        "App.kt",
                        """
                    package app

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun AppPreview() {}
                    """,
                    ),
                    base.collectAllModulePreviewsEntry("appPreviews"),
                    extraClasspaths = listOf(manualLibResult.outputDirectory, autoLibResult.outputDirectory),
                )
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val previews = appResult.loadCollectedPreviews(propertyName = "appPreviews")
                val ids = previews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }
                ids shouldContainExactlyInAnyOrder listOf(
                    "app.AppPreview",
                    "manuallib.ManualLibPreview",
                    "autolib.AutoLibPreview",
                )
            }

            test("3-level nesting: app(all) → ui(all) → core(single) aggregates every preview without duplicates") {
                // Stage 1: core — collectModulePreviews().
                val coreResult = base.compile(
                    SourceFile.kotlin(
                        "CoreLib.kt",
                        """
                    package corelib

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun CorePreview() {}

                    val corePreviews by me.tbsten.compose.preview.lab.collectModulePreviews()
                    """,
                    ),
                )
                coreResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                // Stage 2: ui — add core to the classpath and call
                // collectAllModulePreviews(). (uiPreviews contains UiPreview + CorePreview.)
                val uiResult = base.compile(
                    SourceFile.kotlin(
                        "UiLib.kt",
                        """
                    package uilib

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun UiPreview() {}

                    val uiPreviews by me.tbsten.compose.preview.lab.collectAllModulePreviews()
                    """,
                    ),
                    extraClasspaths = listOf(coreResult.outputDirectory),
                )
                uiResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                // Stage 3: app — add both ui and core to the classpath and call
                // collectAllModulePreviews(). app discovers both ui's aggregated result
                // (Ui+Core) and core's hint-emitted entry (Core); the expected behavior
                // is that the result is deduplicated by id.
                val appResult = base.compile(
                    SourceFile.kotlin(
                        "App.kt",
                        """
                    package app

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun AppPreview() {}
                    """,
                    ),
                    base.collectAllModulePreviewsEntry("appPreviews"),
                    extraClasspaths = listOf(uiResult.outputDirectory, coreResult.outputDirectory),
                )
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val previews = appResult.loadCollectedPreviews(propertyName = "appPreviews")
                val ids = previews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }
                ids shouldContainExactlyInAnyOrder listOf(
                    "app.AppPreview",
                    "uilib.UiPreview",
                    "corelib.CorePreview",
                )
            }
        },
    )
