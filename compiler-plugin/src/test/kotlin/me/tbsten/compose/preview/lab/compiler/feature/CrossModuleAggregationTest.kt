package me.tbsten.compose.preview.lab.compiler.feature

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase
import me.tbsten.compose.preview.lab.compiler.loadCollectedPreviews

class CrossModuleAggregationTest :
    FunSpec(
        {
            val base = CompilerPluginTestBase()

            /*
             * `collectAllModulePreviews()` が依存モジュールの preview を **手動 Gradle 設定なしで**
             * 自動集約することを 2 段 kctfork compilation で検証する。
             *
             * 流れ:
             * 1. uiLib1 / uiLib2 をそれぞれ独立にコンパイル
             *    (compiler plugin の hint generator が `me.tbsten.compose.preview.lab.exports.previewLabExport`
             *     hint 関数を synthetic file で生成する)
             * 2. app を uiLib1 / uiLib2 の output を classpath に追加してコンパイル
             *    (compiler plugin の `collectAllModulePreviews()` 置換ロジックが、依存 jar から
             *     hint 関数を `pluginContext.referenceFunctions` で発見し、原 property を resolve、
             *     その値を集約に組み込む)
             * 3. リフレクションで `app.appPreviews` を読み出し、両モジュールの preview が含まれることを検証
             */
            test("collectAllModulePreviews() が依存モジュール 2 つの preview を手動設定なしで集約する") {
                // Stage 1a: uiLib1
                val lib1Result = base.compile(
                    SourceFile.kotlin(
                        "UiLib1.kt",
                        """
                    package uilib1

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun UiLib1Preview1() {}

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun UiLib1Preview2() {}

                    val uiLib1Previews by me.tbsten.compose.preview.lab.collectModulePreviews()
                    """,
                    ),
                )
                lib1Result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                // Stage 1b: uiLib2
                val lib2Result = base.compile(
                    SourceFile.kotlin(
                        "UiLib2.kt",
                        """
                    package uilib2

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun UiLib2Preview() {}

                    val uiLib2Previews by me.tbsten.compose.preview.lab.collectModulePreviews()
                    """,
                    ),
                )
                lib2Result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                // Stage 2: app — uiLib1/uiLib2 の output を classpath に追加して compile
                val appResult = base.compile(
                    SourceFile.kotlin(
                        "App.kt",
                        """
                    package app

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun AppPreview() {}
                    """,
                    ),
                    base.collectAllModulePreviewsEntry("appPreviews"),
                    extraClasspaths = listOf(lib1Result.outputDirectory, lib2Result.outputDirectory),
                )
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                // 検証: appPreviews には app + uiLib1 + uiLib2 の合計 4 つの preview が含まれる
                val previews = appResult.loadCollectedPreviews(propertyName = "appPreviews")
                val ids = previews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }
                ids shouldContainExactlyInAnyOrder listOf(
                    "app.AppPreview",
                    "uilib1.UiLib1Preview1",
                    "uilib1.UiLib1Preview2",
                    "uilib2.UiLib2Preview",
                )
            }

            test("依存なしの単一モジュールでも collectAllModulePreviews() が動く") {
                val result = base.compile(
                    SourceFile.kotlin(
                        "App.kt",
                        """
                    package app

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun AppPreview() {}
                    """,
                    ),
                    base.collectAllModulePreviewsEntry("appPreviews"),
                )
                result.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val previews = result.loadCollectedPreviews(propertyName = "appPreviews")
                previews.size shouldBe 1
            }

            test("依存モジュール側で collectAllModulePreviews() を使った property も上位の集約に含まれる") {
                // Stage 1: uiLib — このモジュール自身が collectAllModulePreviews() を使う
                // (依存無しなので uiLibPreviews には UiLibPreview のみ含まれる)
                val uiLibResult = base.compile(
                    SourceFile.kotlin(
                        "UiLib.kt",
                        """
                    package uilib

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun UiLibPreview() {}

                    val uiLibPreviews by me.tbsten.compose.preview.lab.collectAllModulePreviews()
                    """,
                    ),
                )
                uiLibResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                // Stage 2: app — uiLib を classpath に追加して collectAllModulePreviews() で集約
                val appResult = base.compile(
                    SourceFile.kotlin(
                        "App.kt",
                        """
                    package app

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun AppPreview() {}
                    """,
                    ),
                    base.collectAllModulePreviewsEntry("appPreviews"),
                    extraClasspaths = listOf(uiLibResult.outputDirectory),
                )
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val previews = appResult.loadCollectedPreviews(propertyName = "appPreviews")
                val ids = previews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }
                ids shouldContainExactlyInAnyOrder listOf(
                    "app.AppPreview",
                    "uilib.UiLibPreview",
                )
            }

            test("collectModulePreviews() なしで @Preview だけ持つ依存モジュールも自動収集される") {
                // 子モジュールが `val xxxPreviews by collectModulePreviews()` を一切書かなくても、
                // compiler plugin が auto provider function + hint を自動生成し、
                // app の `collectAllModulePreviews()` から発見される。
                val libResult = base.compile(
                    SourceFile.kotlin(
                        "UiLib.kt",
                        """
                    package uilib

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun UiLibPreview1() {}

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun UiLibPreview2() {}

                    // collectModulePreviews() は意図的に書かない
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
                    base.collectAllModulePreviewsEntry("appPreviews"),
                    extraClasspaths = listOf(libResult.outputDirectory),
                )
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val previews = appResult.loadCollectedPreviews(propertyName = "appPreviews")
                val ids = previews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }
                ids shouldContainExactlyInAnyOrder listOf(
                    "app.AppPreview",
                    "uilib.UiLibPreview1",
                    "uilib.UiLibPreview2",
                )
            }

            test("default package (package 宣言なし) のモジュールでも auto-collect が動く") {
                // package 宣言を持たないファイルでも provider 関数名生成パスが正常に動作すること。
                // (sanitizedPkg は `DEFAULT_PACKAGE_TOKEN` にフォールバックする)
                val libResult = base.compile(
                    SourceFile.kotlin(
                        "RootPkgLib.kt",
                        """
                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun RootPreview() {}
                    """,
                    ),
                    moduleName = "rootPkgLib",
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
                    base.collectAllModulePreviewsEntry("appPreviews"),
                    extraClasspaths = listOf(libResult.outputDirectory),
                    moduleName = "app",
                )
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val previews = appResult.loadCollectedPreviews(propertyName = "appPreviews")
                val ids = previews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }
                ids shouldContainExactlyInAnyOrder listOf("app.AppPreview", "RootPreview")
            }

            test("同一モジュール内の複数パッケージにまたがる @Preview がすべて auto-collect される") {
                // provider 関数名は最初の preview の package から派生するが、
                // body に含まれる preview は package を問わず全部であることを保証する。
                val libResult = base.compile(
                    SourceFile.kotlin(
                        "FeatureA.kt",
                        """
                    package multilib.featureA

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun FeatureAPreview() {}
                    """,
                    ),
                    SourceFile.kotlin(
                        "FeatureB.kt",
                        """
                    package multilib.featureB

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun FeatureBPreview() {}
                    """,
                    ),
                    moduleName = "multiPkgLib",
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
                    base.collectAllModulePreviewsEntry("appPreviews"),
                    extraClasspaths = listOf(libResult.outputDirectory),
                    moduleName = "app",
                )
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val previews = appResult.loadCollectedPreviews(propertyName = "appPreviews")
                val ids = previews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }
                ids shouldContainExactlyInAnyOrder listOf(
                    "app.AppPreview",
                    "multilib.featureA.FeatureAPreview",
                    "multilib.featureB.FeatureBPreview",
                )
            }

            test("同じパッケージを共有する 2 つの auto-collect 依存モジュールが両方とも収集される") {
                // 複数の依存モジュールが同じパッケージ名を使うケース
                // (例: マルチモジュールプロジェクトでモジュール間で base package を共有する)。
                // provider function 名がパッケージのみから派生していると名前衝突して
                // 片方の preview がサイレントに脱落する回帰を防ぐ。
                val libAResult = base.compile(
                    SourceFile.kotlin(
                        "LibA.kt",
                        """
                    package shared

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun LibAPreview() {}
                    """,
                    ),
                    moduleName = "libA",
                )
                libAResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val libBResult = base.compile(
                    SourceFile.kotlin(
                        "LibB.kt",
                        """
                    package shared

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun LibBPreview() {}
                    """,
                    ),
                    moduleName = "libB",
                )
                libBResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val appResult = base.compile(
                    SourceFile.kotlin(
                        "App.kt",
                        """
                    package app

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun AppPreview() {}
                    """,
                    ),
                    base.collectAllModulePreviewsEntry("appPreviews"),
                    extraClasspaths = listOf(libAResult.outputDirectory, libBResult.outputDirectory),
                    moduleName = "app",
                )
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val previews = appResult.loadCollectedPreviews(propertyName = "appPreviews")
                val ids = previews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }
                ids shouldContainExactlyInAnyOrder listOf(
                    "app.AppPreview",
                    "shared.LibAPreview",
                    "shared.LibBPreview",
                )
            }

            test("auto-collect モジュールと manual collectModulePreviews() モジュールが混在しても重複しない") {
                // Stage 1: manualLib — 明示的に collectModulePreviews() を書く
                val manualLibResult = base.compile(
                    SourceFile.kotlin(
                        "ManualLib.kt",
                        """
                    package manuallib

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun ManualLibPreview() {}

                    val manualLibPreviews by me.tbsten.compose.preview.lab.collectModulePreviews()
                    """,
                    ),
                )
                manualLibResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                // Stage 2: autoLib — collectModulePreviews() を書かない (auto-hint 経路)
                val autoLibResult = base.compile(
                    SourceFile.kotlin(
                        "AutoLib.kt",
                        """
                    package autolib

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun AutoLibPreview() {}
                    """,
                    ),
                )
                autoLibResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                // Stage 3: app — 両モジュールを classpath に追加
                val appResult = base.compile(
                    SourceFile.kotlin(
                        "App.kt",
                        """
                    package app

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun AppPreview() {}
                    """,
                    ),
                    base.collectAllModulePreviewsEntry("appPreviews"),
                    extraClasspaths = listOf(manualLibResult.outputDirectory, autoLibResult.outputDirectory),
                )
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val previews = appResult.loadCollectedPreviews(propertyName = "appPreviews")
                val ids = previews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }
                ids shouldContainExactlyInAnyOrder listOf(
                    "app.AppPreview",
                    "manuallib.ManualLibPreview",
                    "autolib.AutoLibPreview",
                )
            }

            test("3 段の入れ子: app(all) → ui(all) → core(single) で全 preview が集約され重複しない") {
                // Stage 1: core — collectModulePreviews()
                val coreResult = base.compile(
                    SourceFile.kotlin(
                        "CoreLib.kt",
                        """
                    package corelib

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun CorePreview() {}

                    val corePreviews by me.tbsten.compose.preview.lab.collectModulePreviews()
                    """,
                    ),
                )
                coreResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                // Stage 2: ui — core を classpath に追加して collectAllModulePreviews()
                // (uiPreviews には UiPreview + CorePreview が含まれる)
                val uiResult = base.compile(
                    SourceFile.kotlin(
                        "UiLib.kt",
                        """
                    package uilib

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun UiPreview() {}

                    val uiPreviews by me.tbsten.compose.preview.lab.collectAllModulePreviews()
                    """,
                    ),
                    extraClasspaths = listOf(coreResult.outputDirectory),
                )
                uiResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                // Stage 3: app — ui と core を classpath に追加して collectAllModulePreviews()
                // app は ui の集約結果 (Ui+Core) と core の hint 由来 (Core) の双方を発見するが、
                // 期待動作としては id 単位で重複排除されること
                val appResult = base.compile(
                    SourceFile.kotlin(
                        "App.kt",
                        """
                    package app

                    @org.jetbrains.compose.ui.tooling.preview.Preview
                    fun AppPreview() {}
                    """,
                    ),
                    base.collectAllModulePreviewsEntry("appPreviews"),
                    extraClasspaths = listOf(uiResult.outputDirectory, coreResult.outputDirectory),
                )
                appResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

                val previews = appResult.loadCollectedPreviews(propertyName = "appPreviews")
                val ids = previews.map { p -> p::class.members.find { it.name == "id" }!!.call(p) as String }
                ids shouldContainExactlyInAnyOrder listOf(
                    "app.AppPreview",
                    "uilib.UiPreview",
                    "corelib.CorePreview",
                )
            }
        },
    )
