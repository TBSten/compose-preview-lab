package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase

/**
 * Verifies that [PreviewLabHintFirGenerator][me.tbsten.compose.preview.lab.compiler.fir.PreviewLabHintFirGenerator]
 * actually emits the synthetic marker class and hint function and that the JVM backend lowers
 * them without errors (the bug-017 step 2 baseline).
 *
 * **Skipped on Kotlin < 2.3.21**: the FIR generator is registered only when
 * [CompatContext.supportsKlibCrossModuleHint][me.tbsten.compose.preview.lab.compiler.compat.CompatContext.supportsKlibCrossModuleHint]
 * returns true. Older 2.x compilers go through the legacy IR-based hint path on JVM and
 * therefore would not produce these class files even though the plugin is correct on those
 * versions. `compiler-plugin-test.sh` already excludes the whole test task for those Kotlin
 * matrix entries; this in-test guard mirrors that gate so direct
 * `./gradlew :compiler-plugin:test -Ptest.kotlin=2.3.0` invocations do not surface a misleading
 * failure.
 */
class FirHintGeneratorEmitsDeclarationsTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        // Mirror the version gate used by `PreviewLabFirExtensionRegistrar` /
        // `CompatContext.supportsKlibCrossModuleHint()`.
        val testKotlinVersion = System.getProperty("test.kotlin.version") ?: "2.3.21"
        val supportsFirHintGenerator = testKotlinVersion.compareTo("2.3.21") >= 0 ||
            testKotlinVersion.startsWith("2.4")

        test("FIR-generated marker class and previewLabExport hint function are present in compiled output")
            .config(enabled = supportsFirHintGenerator) {
                val result = base.compile(
                    SourceFile.kotlin(
                        "Previews.kt",
                        """
                    package test.source

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun HelloPreview() {}
                    """,
                    ),
                    base.collectModulePreviewsEntry(),
                )
                result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val hintPackagePath = "me/tbsten/compose/preview/lab/exports"
                val classFiles = result.outputDirectory.walkTopDown()
                    .filter { it.isFile && it.name.endsWith(".class") }
                    .map { it.relativeTo(result.outputDirectory).path }
                    .toList()

                // FIR-generated synthetic marker class.
                classFiles shouldExist {
                    it.startsWith("$hintPackagePath/PreviewLabExportMarker_") && it.endsWith(".class")
                }

                // FIR-generated hint function (file-facade class name ends with `Kt`).
                classFiles shouldExist {
                    it.startsWith("$hintPackagePath/PreviewLabExport_PreviewLabExportMarker_") &&
                        it.endsWith("Kt.class")
                }
            }
    })
