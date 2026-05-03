package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import me.tbsten.compose.preview.lab.compiler.CompilerPluginJsTestBase
import me.tbsten.compose.preview.lab.compiler.assertOk
import me.tbsten.compose.preview.lab.compiler.klibFiles

/**
 * KLIB IdSignature 衝突回避と marker class ベースの cross-module discovery が
 * JS_IR backend で正しく機能することを検証する。
 *
 * JVM 版 (`CrossModuleAggregationTest`) は class loader 経由の linking のみ検証し、
 * KLIB の `IdSignature` collision 回避ロジック (FIR で per-module marker class を生成)
 * を直接 exercise しない。本テストは 2 段 JS_IR compile で KLIB linking を実際に走らせ、
 * marker class 命名が module 跨ぎで unique であることを担保する。
 */
class CrossModuleAggregationKlibTest :
    FunSpec(
        {
            val base = CompilerPluginJsTestBase()

            test("@Preview のみの単一モジュールが JS_IR backend で KLIB を生成できる") {
                val result = base.compileJs(
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
                )
                result.assertOk()
            }

            test("依存 KLIB 2 つを含む app の JS_IR compile が IdSignature 衝突なく成功する") {
                // Stage 1a: uiLib1 の KLIB
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

                // Stage 1b: uiLib2 の KLIB
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

                // Stage 2: app は両 KLIB を `-libraries` で参照しつつ collectAllModulePreviews を使う。
                // marker class 命名が module hash で unique なので、両 lib の `previewLabExport(<Marker>)`
                // は IdSignature が衝突せず link 可能。
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

            test("manual collectModulePreviews delegate を持つ依存 KLIB を app が集約できる") {
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
        },
    )
