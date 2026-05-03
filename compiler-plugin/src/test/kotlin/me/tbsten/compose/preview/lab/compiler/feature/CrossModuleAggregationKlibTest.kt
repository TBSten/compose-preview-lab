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

            // Mirror the version gate used by `PreviewLabFirExtensionRegistrar` /
            // `CompatContext.supportsKlibCrossModuleHint()`. Below 2.3.21 the FIR-side
            // hint generator is not registered at all, so these tests cannot exercise
            // the marker-class scheme they depend on. Skipping rather than failing keeps
            // the older-Kotlin smoke matrix (`scripts/compiler-plugin-test.sh`) green.
            val testKotlinVersion = System.getProperty("test.kotlin.version") ?: "2.3.21"
            val supportsKlibHint = testKotlinVersion.compareTo("2.3.21") >= 0 ||
                testKotlinVersion.startsWith("2.4")

            test("E-1: 単一依存モジュールの @Preview が app の collectAllModulePreviews() から aggregation 可能")
                .config(enabled = supportsKlibHint) {
                    // Stage 1: 依存 KLIB を ビルド (この KLIB は @Preview のみを持つ)
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

                    // Stage 2: app は依存 KLIB を `-libraries` で参照しつつ
                    // `collectAllModulePreviews()` を呼ぶ。FIR-emitted hint と marker class が
                    // KLIB に含まれていなければここで unresolved reference になる。
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
                    // Note: kctfork の JS_IR backend は KLIB output のみ作るので、preview ID 単位の
                    // runtime 値検証は不可。「依存 preview の id が含まれる」確認は PR2 の
                    // integrationTest (実 Gradle build + node 実行) スコープ。
                }

            test("依存 KLIB 2 つを含む app の JS_IR compile が IdSignature 衝突なく成功する")
                .config(enabled = supportsKlibHint) {
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

            test("manual collectModulePreviews delegate を持つ依存 KLIB を app が集約できる")
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

            test("E-2: 同名 collectModulePreviews property を持つ依存 2 つでも IdSignature 衝突しない")
                .config(enabled = supportsKlibHint) {
                    // 両モジュールが同じプロパティ名 `sharedPreviews` を持つ。旧 IR-based hint scheme では
                    // `previewLabExport(PreviewExport)` の IdSignature が両モジュールで一致して link 失敗するが、
                    // 新 FIR-based path では module-name-hash で marker class を unique 化しているので両方 link 成功する。
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

            test("E-4: @Preview を持たない依存モジュールが含まれていても link が壊れない")
                .config(enabled = supportsKlibHint) {
                    // empty な依存 KLIB (preview / property どちらも無し)。FIR generator は依然として
                    // marker + hint を出すので、auto-provider が空リストを返す形で生成される必要がある。
                    // (P1 fix を入れる前は previews.isEmpty() で provider 生成を skip していたため、
                    // 下流の `referenceFunctions` が空になり aggregation 自体は動くが、provider 不在の
                    // hint が残る奇妙な状態になっていた。)
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
