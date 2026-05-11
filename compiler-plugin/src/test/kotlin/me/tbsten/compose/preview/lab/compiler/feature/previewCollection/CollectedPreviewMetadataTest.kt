package me.tbsten.compose.preview.lab.compiler.feature.previewCollection

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.loadCollectedPreviews

/**
 * Verifies the metadata fields on the `CollectedPreview` entries returned by
 * `collectModulePreviews()` (id, displayName, filePath, sort order, ignore, placeholder
 * resolution, etc.).
 */
class CollectedPreviewMetadataTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        test("CollectedPreview has the correct id") {
            val result = base.compile(
                SourceFile.kotlin(
                    "Preview.kt",
                    """
                    package test.source

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun MyPreview() {}
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val preview = result.loadCollectedPreviews().first()
            val id = preview::class.members.find { it.name == "id" }!!.call(preview)
            id shouldBe "test.source.MyPreview"
        }

        test("CollectedPreview has the correct displayName") {
            val result = base.compile(
                SourceFile.kotlin(
                    "Preview.kt",
                    """
                    package test.source

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun DisplayNamePreview() {}
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val preview = result.loadCollectedPreviews().first()
            val displayName = preview::class.members.find { it.name == "displayName" }!!.call(preview)
            displayName shouldBe "test.source.DisplayNamePreview"
        }

        test("@ComposePreviewLabOption(displayName) applies a custom displayName") {
            val result = base.compile(
                SourceFile.kotlin(
                    "Preview.kt",
                    """
                    package test.source

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    @me.tbsten.compose.preview.lab.ComposePreviewLabOption(displayName = "Custom Name")
                    fun CustomPreview() {}
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val preview = result.loadCollectedPreviews().first()
            val displayName = preview::class.members.find { it.name == "displayName" }!!.call(preview)
            displayName shouldBe "Custom Name"
        }

        test("CollectedPreview has a filePath") {
            val result = base.compile(
                SourceFile.kotlin(
                    "Preview.kt",
                    """
                    package test.source

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun FilePathPreview() {}
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val preview = result.loadCollectedPreviews().first()
            val filePath = preview::class.members.find { it.name == "filePath" }!!.call(preview) as? String
            filePath shouldNotBe null
        }

        test("collected CollectedPreviews are sorted by displayName") {
            val result = base.compile(
                SourceFile.kotlin(
                    "Preview.kt",
                    """
                    package test.source

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun ZPreview() {}

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun APreview() {}
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val previews = result.loadCollectedPreviews()
            previews.size shouldBe 2

            val names = previews.map { p ->
                p::class.members.find { it.name == "displayName" }!!.call(p) as String
            }
            names shouldBe names.sorted()
        }

        test("zero @Preview functions produces an empty list (compilation still succeeds)") {
            val result = base.compile(
                SourceFile.kotlin(
                    "NoPreviews.kt",
                    """
                    package test.source

                    fun regularFunction() {}
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
            // No @Preview present, but compilation must still succeed.
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK
            result.loadCollectedPreviews().size shouldBe 0
        }

        test("@ComposePreviewLabOption(id) applies a custom id") {
            val result = base.compile(
                SourceFile.kotlin(
                    "Preview.kt",
                    """
                    package test.source

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    @me.tbsten.compose.preview.lab.ComposePreviewLabOption(id = "my-custom-id")
                    fun CustomIdPreview() {}
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val preview = result.loadCollectedPreviews().first()
            val id = preview::class.members.find { it.name == "id" }!!.call(preview)
            id shouldBe "my-custom-id"
        }

        test("placeholders {{package}} / {{simpleName}} / {{qualifiedName}} resolve correctly") {
            val result = base.compile(
                SourceFile.kotlin(
                    "Preview.kt",
                    """
                    package test.source

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    @me.tbsten.compose.preview.lab.ComposePreviewLabOption(
                        displayName = "pkg={{package}} simple={{simpleName}} fqn={{qualifiedName}}"
                    )
                    fun PlaceholderPreview() {}
                    """,
                ),
                base.collectModulePreviewsEntry(),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK

            val preview = result.loadCollectedPreviews().first()
            val displayName = preview::class.members.find { it.name == "displayName" }!!.call(preview) as String
            displayName shouldBe "pkg=test.source simple=PlaceholderPreview fqn=test.source.PlaceholderPreview"
        }
    })
