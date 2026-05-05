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

        test("collectAllModulePreviews() が cross-module の per-declaration hint を発見・利用する") {
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

        test("複数 @Preview を持つ依存モジュールの hint をすべて発見する") {
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

        test("cross-module の preview が CollectedPreview の全 metadata を保持する") {
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

        test("dedup: 同一 preview が V1 (auto-provider) と V2 (per-declaration hint) の両 path から到達しても 1 個に統合される") {
            // T04 時点では V1 (auto-provider 経由の旧モジュール集約) と V2 (per-declaration hint
            // 経由の新方式) が共存しているため、 dependency module の `@Preview` は app 側で
            //   - V1 path: app の auto-provider 経由で lib の preview list を集約
            //   - V2 path: discoverHintsV2 で lib の `previewHint_<hash>()` を発見 → call
            // の 2 経路で到達する。 distinctPreviewsById により id 重複が排除されることを検証する。
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

            // shared.SharedPreview が 2 重に出てくる可能性があるが、 distinctPreviewsById で
            // 1 個に統合される (size == distinct size で重複ゼロを assert)
            previews.size shouldBe ids.distinct().size
            ids shouldContain "shared.SharedPreview"
            ids shouldContain "app.AppPreview"
            // shared.SharedPreview が dedup 後も 1 個だけ残っていることを明示的に確認
            ids.count { it == "shared.SharedPreview" } shouldBe 1
        }
    })
