package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.isAtLeast

/**
 * Verifies that every per-declaration hint declaration emitted by `PreviewHintFirGenerator`
 * (in `compiler-plugin/src/main/.../fir/`) carries `@kotlin.Deprecated(level = HIDDEN)` so
 * it stays out of IDE autocomplete and resists accidental user references — while remaining
 * discoverable via classpath lookup (`referenceFunctions`).
 *
 * Validation is done at the JVM bytecode level: the Kotlin `@Deprecated(HIDDEN)`
 * annotation is lowered to `java.lang.Deprecated` on both the marker class and the
 * `previewHint` overload (Kotlin emits `ACC_DEPRECATED` + the annotation), so JDK
 * reflection picks it up uniformly.
 *
 * **Skipped on Kotlin < 2.3.21**: the per-declaration hint generator only runs on Kotlin
 * 2.3.21+ (gated by `CompatContext.supportsKlibCrossModuleHint`).
 */
class PreviewHintHiddenDeprecationTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        val testKotlinVersion = System.getProperty("test.kotlin.version") ?: "2.3.21"
        val supports = testKotlinVersion.isAtLeast(2, 3, 21)

        test("every emitted PreviewHintMarker_* interface carries @Deprecated")
            .config(enabled = supports) {
                val result = base.compile(
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
                result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val markerNames = result.outputDirectory.previewHintMarkerNames()
                markerNames.shouldNotBeEmpty()
                markerNames.size shouldBe 3

                assertSoftly {
                    markerNames.forEach { fqn ->
                        val cls = result.classLoader.loadClass(fqn)
                        cls.isAnnotationPresent(Deprecated::class.java) shouldBe true
                    }
                }
            }

        test("every emitted previewHint(...) overload carries @Deprecated")
            .config(enabled = supports) {
                val result = base.compile(
                    SourceFile.kotlin(
                        "UiLib.kt",
                        """
                        package uilib

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun First() {}

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun Second() {}
                        """,
                    ),
                )
                result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val facadeNames = result.outputDirectory.previewHintFacadeNames()
                facadeNames.shouldNotBeEmpty()
                facadeNames.size shouldBe 2

                assertSoftly {
                    facadeNames.forEach { fqn ->
                        val facade = result.classLoader.loadClass(fqn)
                        val previewHint = facade.declaredMethods.single { it.name == "previewHint" }
                        previewHint.isAnnotationPresent(Deprecated::class.java) shouldBe true
                    }
                }
            }

        test("a single @Preview emits both marker and hint function with @Deprecated")
            .config(enabled = supports) {
                val result = base.compile(
                    SourceFile.kotlin(
                        "Single.kt",
                        """
                        package single

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun Lonely() {}
                        """,
                    ),
                )
                result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val markerName = result.outputDirectory.previewHintMarkerNames().single()
                val facadeName = result.outputDirectory.previewHintFacadeNames().single()

                val markerCls = result.classLoader.loadClass(markerName)
                val facadeCls = result.classLoader.loadClass(facadeName)
                val previewHint = facadeCls.declaredMethods.single { it.name == "previewHint" }

                assertSoftly {
                    markerCls.isAnnotationPresent(Deprecated::class.java) shouldBe true
                    previewHint.isAnnotationPresent(Deprecated::class.java) shouldBe true
                }
            }

        test("PreviewHintDiscoveryTest behaviour preserved: cross-module discovery still works under HIDDEN")
            .config(enabled = supports) {
                // Sanity: HIDDEN gates only scope resolution. `referenceFunctions(CallableId)`
                // (which the consumer-side IR pass uses) goes through classpath lookup and
                // therefore still picks up the deprecated hint. This regression is also
                // covered by PreviewHintDiscoveryTest, but kept here so the "@Deprecated +
                // discovery" combination is asserted in one place.
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
            }

        test("emitted hint declarations land in the dedicated hints package only")
            .config(enabled = supports) {
                // Asserts the deprecated declarations don't accidentally end up under the
                // user's package, which would noticeably bloat IDE autocomplete even with
                // HIDDEN if the package itself is something the user types into often.
                val result = base.compile(
                    SourceFile.kotlin(
                        "UiLib.kt",
                        """
                        package uilib

                        @org.jetbrains.compose.ui.tooling.preview.Preview
                        fun One() {}
                        """,
                    ),
                )
                result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val markers = result.outputDirectory.previewHintMarkerNames()
                val facades = result.outputDirectory.previewHintFacadeNames()
                assertSoftly {
                    markers.forEach { it.startsWith("me.tbsten.compose.preview.lab.hints.") shouldBe true }
                    facades.forEach { it.startsWith("me.tbsten.compose.preview.lab.hints.") shouldBe true }
                }
            }
    })
