package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.loadCollectedPreviews

/**
 * `collectModulePreviews()` で集まった CollectedPreview の各メタデータ
 * (id, displayName, filePath, ソート順, ignore, placeholder 解決, etc.) を検証する。
 */
class CollectedPreviewMetadataTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        test("CollectedPreview が正しい id を持つ") {
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

        test("CollectedPreview が正しい displayName を持つ") {
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

        test("@ComposePreviewLabOption(displayName) でカスタム displayName が反映される") {
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

        test("CollectedPreview が filePath を持つ") {
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

        test("収集された CollectedPreview が displayName でソートされている") {
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

        test("@Preview 関数が 0 個の場合は空リスト (コンパイルは成功する)") {
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
            // @Preview がなくてもコンパイルエラーにならない
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK
            result.loadCollectedPreviews().size shouldBe 0
        }

        test("@ComposePreviewLabOption(id) でカスタム id が反映される") {
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

        test("プレースホルダー {{package}} {{simpleName}} {{qualifiedName}} が正しく解決される") {
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
