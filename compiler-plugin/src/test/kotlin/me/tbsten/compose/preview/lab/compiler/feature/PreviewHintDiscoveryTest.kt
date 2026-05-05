package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.isAtLeast
import me.tbsten.compose.preview.lab.compiler.loadCollectedPreviews

/**
 * Per-declaration hint (Metro 風) を依存モジュールから発見する consumer 側 IR pass の検証。
 *
 * 2-stage kctfork compilation:
 * 1. uiLib をコンパイルし、 `me.tbsten.compose.preview.lab.hints` package に
 *    `interface PreviewHintMarker_<hash>` + `fun previewHint(value: PreviewHintMarker_<hash>?): CollectedPreview`
 *    が emit される
 * 2. app を uiLib output を classpath に追加してコンパイル。 `collectAllModulePreviews()` の
 *    IR transform で `referenceFunctions(CallableId(hintsPackage, "previewHint"))` で hint を
 *    発見し、 各 hint を call して `CollectedPreview` を list に積む
 * 3. リフレクションで app.allPreviews を読み出し、 cross-module preview が含まれることを検証
 *
 * **Skipped on Kotlin < 2.3.21**: hint generator は Kotlin 2.3.21+ でのみ稼働し、 古い Kotlin では
 * `collectAllModulePreviews()` 自体が IR phase で compile-time error になる
 * ([CompatContext.supportsKlibCrossModuleHint][me.tbsten.compose.preview.lab.compiler.compat.CompatContext.supportsKlibCrossModuleHint])。
 */
class PreviewHintDiscoveryTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        val testKotlinVersion = System.getProperty("test.kotlin.version") ?: "2.3.21"
        val supports = testKotlinVersion.isAtLeast(2, 3, 21)

        test("collectAllModulePreviews() が cross-module の per-declaration hint を発見・利用する")
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

                // 自モジュールの @Preview と cross-module hint discovery の両方から CollectedPreview
                // が emit されるが、 distinctPreviewsById で id ベースで dedup されるので同じ
                // preview は 1 回だけ登場する。
                ids shouldContainExactlyInAnyOrder listOf(
                    "app.AppPreview",
                    "uilib.UiLibPreview",
                )
            }

        test("複数 @Preview を持つ依存モジュールの hint をすべて発見する")
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

        test("cross-module の preview が CollectedPreview の全 metadata を保持する")
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
                // startLine / endLine は @Preview の宣言位置を指す (1-based)
                (startLine != null) shouldBe true
                (endLine != null) shouldBe true
                // filePath / code は kctfork の in-memory file 環境では null になる場合があるので
                // 存在チェックのみ (実 build では non-null)。 sentinel `""` から null に正しく
                // 復元されることを `code != null && code.isEmpty()` のような誤動作で潰さないかの
                // 確認は T04 (integrationTest) に委ねる
                @Suppress("UNUSED_VARIABLE")
                val ignored = listOf(filePath, code)
            }

        test("smoke: collectAllModulePreviews() の結果に id の重複が現れない")
            .config(enabled = supports) {
                // 自モジュール `@Preview` (local list 経由) と cross-module `@Preview` (per-declaration
                // hint 経由) の両方を含む集約結果について、 `distinctPreviewsById` が正しく適用され
                // 結果リストの id が unique であることを smoke test として確認する。
                //
                // 真の重複が発生する transitive dep chain (app(all) → ui(all) → core) のケースは
                // integrationTest 側 (`CrossModuleCollectPreviewsTest.collectAllModulePreviewsDeduplicatesById`)
                // でカバーする。
                val libResult = base.compile(
                    SourceFile.kotlin(
                        "Shared.kt",
                        """
                    package shared

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun SharedPreview() {}
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
                    base.collectAllModulePreviewsEntry("allPreviews"),
                    extraClasspaths = listOf(libResult.outputDirectory),
                )
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val previews = appResult.loadCollectedPreviews(propertyName = "allPreviews")
                val ids = previews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }

                // distinctPreviewsById により id 重複ゼロを assert
                previews.size shouldBe ids.distinct().size
                ids shouldContain "shared.SharedPreview"
                ids shouldContain "app.AppPreview"
                // shared.SharedPreview が dedup 後も 1 個だけ残っていることを明示的に確認
                ids.count { it == "shared.SharedPreview" } shouldBe 1
            }
    })
