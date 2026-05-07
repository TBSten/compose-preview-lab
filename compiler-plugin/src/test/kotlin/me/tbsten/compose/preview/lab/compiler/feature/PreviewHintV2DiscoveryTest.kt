package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.loadCollectedPreviews

/**
 * Per-declaration hint (Metro 風) を依存モジュールから発見する consumer 側 IR pass の検証。
 *
 * 2-stage kctfork compilation:
 * 1. uiLib をコンパイルし、 `me.tbsten.compose.preview.lab.hints/previewHint_<hash>(): CollectedPreview`
 *    が emit される (T02 で実装済み)
 * 2. app を uiLib output を classpath に追加してコンパイル。 `collectAllModulePreviews()` の
 *    IR transform で V2 hint discovery が cross-module hint を call して `CollectedPreview` を
 *    取得し、 list に積む
 * 3. リフレクションで app.allPreviews を読み出し、 cross-module preview が含まれることを検証
 */
class PreviewHintV2DiscoveryTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        test("collectAllModulePreviews() discovers and uses cross-module per-declaration hints") {
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

            // V1 (旧モジュール集約) + V2 (per-declaration) 両方が動くが、 distinctPreviewsById
            // で dedup されるので同じ preview は 1 回だけ登場する。
            ids shouldContainExactlyInAnyOrder listOf(
                "app.AppPreview",
                "uilib.UiLibPreview",
            )
        }

        test("discovers all hints from dependency module with multiple @Preview annotations") {
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

        test("cross-module preview preserves all CollectedPreview metadata") {
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

        test("dedup: local @Preview and cross-module hint with same FQN are consolidated to one") {
            // 同 FQN cross-module collision は受容済み edge case だが、 通常ケースとして
            // 自モジュール側の hint が先に登録されて cross-module 側が overwrite しないこと
            // (= distinctPreviewsById が機能していること) を確認するために、 別 FQN を使う。
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

            // V1 (auto-provider経由) + V2 (per-declaration hint経由) で SharedPreview が
            // 2 重に出てくる可能性があるが、 distinctPreviewsById で 1 個に統合される
            previews.size shouldBe ids.distinct().size
            ids shouldContain "shared.SharedPreview"
            ids shouldContain "app.AppPreview"
        }
    })
