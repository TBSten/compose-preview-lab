package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import me.tbsten.compose.preview.lab.compiler.CompilerPluginJsTestBase
import me.tbsten.compose.preview.lab.compiler.assertOk
import me.tbsten.compose.preview.lab.compiler.klibFiles

/**
 * Verifies that KLIB IdSignature collision avoidance and marker-class-based cross-module
 * discovery work correctly under the JS_IR backend.
 *
 * The JVM counterpart (`CrossModuleAggregationTest`) only exercises linking via the
 * classloader and does not directly cover the KLIB `IdSignature` collision-avoidance
 * logic (the FIR-side per-module marker classes). This test runs an actual two-stage
 * JS_IR compile so that KLIB linking is exercised end-to-end and marker class naming is
 * confirmed to be unique across modules.
 */
class CrossModuleAggregationKlibTest :
    FunSpec(
        {
            val base = CompilerPluginJsTestBase()

            // Mirror the version gate used by `PreviewLabFirExtensionRegistrar` /
            // `CompatContext.supportsKlibCrossModuleHint()`. Below 2.3.21 the FIR-side
            // hint generator is not registered at all, so these tests cannot exercise
            // the marker-class scheme they depend on. Skipping rather than failing keeps
            // the older-Kotlin smoke matrix (`scripts/compiler-plugin-test.sh`) green.
            val testKotlinVersion = System.getProperty("test.kotlin.version") ?: "2.3.21"
            val supportsKlibHint = testKotlinVersion.compareTo("2.3.21") >= 0 ||
                testKotlinVersion.startsWith("2.4")

            test("E-1: a single dependency module's @Preview is aggregated by app's collectAllModulePreviews()")
                .config(enabled = supportsKlibHint) {
                    // Stage 1: build the dependency KLIB (contains only @Preview functions).
                    val lib = base.compileJs(
                        SourceFile.kotlin(
                            "Lib.kt",
                            """
                        package uilib

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun UiLibPreview1() {}

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun UiLibPreview2() {}
                        """,
                        ),
                        moduleName = "preview-lab-js-e1-lib",
                    )
                    lib.assertOk()

                    // Stage 2: the app references the dependency KLIB via `-libraries`
                    // and calls `collectAllModulePreviews()`. If the FIR-emitted hint and
                    // marker class were missing from the KLIB this would surface as an
                    // unresolved reference.
                    val app = base.compileJs(
                        SourceFile.kotlin(
                            "App.kt",
                            """
                        package app

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun AppPreview() {}
                        """,
                        ),
                        base.collectAllModulePreviewsEntry("appPreviews"),
                        extraLibraries = lib.klibFiles(),
                        moduleName = "preview-lab-js-e1-app",
                    )
                    app.assertOk()
                    // Note: kctfork's JS_IR backend only produces a KLIB, so per-preview-id
                    // runtime value assertions are not possible here. Confirming "the
                    // dependency preview's id is included" lives in the PR2
                    // integrationTest scope (real Gradle build + Node execution).
                }

            test("a JS_IR compile of an app with two dependency KLIBs links without IdSignature collisions")
                .config(enabled = supportsKlibHint) {
                    // Stage 1a: uiLib1 KLIB.
                    val lib1 = base.compileJs(
                        SourceFile.kotlin(
                            "UiLib1.kt",
                            """
                    package uilib1

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun UiLib1Preview1() {}

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun UiLib1Preview2() {}
                    """,
                        ),
                        moduleName = "preview-lab-js-uilib1",
                    )
                    lib1.assertOk()

                    // Stage 1b: uiLib2 KLIB.
                    val lib2 = base.compileJs(
                        SourceFile.kotlin(
                            "UiLib2.kt",
                            """
                    package uilib2

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun UiLib2Preview() {}
                    """,
                        ),
                        moduleName = "preview-lab-js-uilib2",
                    )
                    lib2.assertOk()

                    // Stage 2: the app references both KLIBs via `-libraries` and calls
                    // collectAllModulePreviews. Marker class naming is unique per
                    // module-hash, so the `previewLabExport(<Marker>)` from both libs do
                    // not collide on IdSignature and link successfully.
                    val app = base.compileJs(
                        SourceFile.kotlin(
                            "App.kt",
                            """
                    package app

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun AppPreview() {}
                    """,
                        ),
                        base.collectAllModulePreviewsEntry("appPreviews"),
                        extraLibraries = lib1.klibFiles() + lib2.klibFiles(),
                        moduleName = "preview-lab-js-app",
                    )
                    app.assertOk()
                }

            test("an app aggregates a dependency KLIB that has a manual collectModulePreviews delegate")
                .config(enabled = supportsKlibHint) {
                    val lib = base.compileJs(
                        SourceFile.kotlin(
                            "UiLib.kt",
                            """
                    package uilib

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun UiLibPreview() {}

                    val uiLibPreviews by me.tbsten.compose.preview.lab.collectModulePreviews()
                    """,
                        ),
                        moduleName = "preview-lab-js-uilib-manual",
                    )
                    lib.assertOk()

                    val app = base.compileJs(
                        SourceFile.kotlin(
                            "App.kt",
                            """
                    package app

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun AppPreview() {}
                    """,
                        ),
                        base.collectAllModulePreviewsEntry("appPreviews"),
                        extraLibraries = lib.klibFiles(),
                        moduleName = "preview-lab-js-app-manual",
                    )
                    app.assertOk()
                }

            test("E-2: two dependencies that share a collectModulePreviews property name do not collide on IdSignature")
                .config(enabled = supportsKlibHint) {
                    // Both modules expose the same property name `sharedPreviews`. Under
                    // the legacy IR-based hint scheme this caused the
                    // `previewLabExport(PreviewExport)` IdSignatures from both modules to
                    // be identical, and linking failed. The new FIR-based path uses
                    // module-name-hashed marker classes to keep them unique, so both
                    // link successfully.
                    val lib1 = base.compileJs(
                        SourceFile.kotlin(
                            "Lib1.kt",
                            """
                    package lib1

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun Lib1Preview() {}

                    val sharedPreviews by me.tbsten.compose.preview.lab.collectModulePreviews()
                    """,
                        ),
                        moduleName = "preview-lab-js-collide-lib1",
                    )
                    lib1.assertOk()

                    val lib2 = base.compileJs(
                        SourceFile.kotlin(
                            "Lib2.kt",
                            """
                    package lib2

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun Lib2Preview() {}

                    val sharedPreviews by me.tbsten.compose.preview.lab.collectModulePreviews()
                    """,
                        ),
                        moduleName = "preview-lab-js-collide-lib2",
                    )
                    lib2.assertOk()

                    val app = base.compileJs(
                        SourceFile.kotlin(
                            "App.kt",
                            """
                    package app

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun AppPreview() {}
                    """,
                        ),
                        base.collectAllModulePreviewsEntry("appPreviews"),
                        extraLibraries = lib1.klibFiles() + lib2.klibFiles(),
                        moduleName = "preview-lab-js-collide-app",
                    )
                    app.assertOk()
                }

            test("E-4: linking is unaffected by a dependency module that has no @Preview")
                .config(enabled = supportsKlibHint) {
                    // An empty dependency KLIB (no previews, no properties). The FIR
                    // generator still emits marker + hint, so the auto-provider must be
                    // generated as a function that returns an empty list.
                    // (Before the P1 fix, `previews.isEmpty()` short-circuited provider
                    // generation; downstream `referenceFunctions` would return empty so
                    // aggregation still ran, but hints without their provider were left
                    // in a strange dangling state.)
                    val emptyLib = base.compileJs(
                        SourceFile.kotlin(
                            "EmptyLib.kt",
                            """
                    package emptylib

                    fun nonPreviewFunction(): Int = 42
                    """,
                        ),
                        moduleName = "preview-lab-js-empty-lib",
                    )
                    emptyLib.assertOk()

                    val app = base.compileJs(
                        SourceFile.kotlin(
                            "App.kt",
                            """
                    package app

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun AppPreview() {}
                    """,
                        ),
                        base.collectAllModulePreviewsEntry("appPreviews"),
                        extraLibraries = emptyLib.klibFiles(),
                        moduleName = "preview-lab-js-empty-app",
                    )
                    app.assertOk()
                }
        },
    )
