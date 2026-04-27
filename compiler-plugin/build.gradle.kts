import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.conventionJvm)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
    alias(libs.plugins.shadow)
}

/**
 * 配布される compiler-plugin の jar には全 compat module を bundle する。
 * gradle-plugin が SubpluginArtifact 経由でこの 1 つの artifact を参照するだけで、
 * runtime に Kotlin compiler バージョンを ServiceLoader で判定して最適な実装が選ばれる。
 *
 * Metro (https://github.com/ZacSweers/metro/blob/main/compiler/build.gradle.kts) の
 * embedded configuration + ShadowJar パターンを縮小移植している。
 */
val embedded by configurations.dependencyScope("embedded")
val embeddedClasspath by configurations.resolvable("embeddedClasspath") { extendsFrom(embedded) }

// embedded を compile / test classpath にも見せる
configurations.named("compileOnly").configure { extendsFrom(embedded) }
configurations.named("testImplementation").configure { extendsFrom(embedded) }

// `-Ptest.kotlin=X.Y.Z` で smoke-test.sh などから kctfork が動かす kotlin-compiler-embeddable を切替可能。
// `scripts/supported-kotlin-versions.txt` に列挙された各バージョンに対する compat 検証に使う。
val testKotlinVersion: String = (findProperty("test.kotlin") as String?) ?: libs.versions.kotlin.get()

// Kotlin 2.4 系は新しい kctfork が必要。
val kctforkVersion: String = when {
    testKotlinVersion.startsWith("2.4") -> "0.13.0-alpha01"
    else -> libs.versions.kctfork.get()
}

// compiler-plugin main をコンパイルする時の Kotlin compiler API バージョン。
// CompatContext で吸収していない部分 (lambda 生成、IR builder 等) は Kotlin の internal API を直接使うため、
// このバージョンと runtime のバージョンとで binary incompatible があると NoSuchMethodError 等になる。
// → 当面は libs.versions.kotlin と一致させ、これと一致するか forward-compat なバージョンのみサポート。
// (compat module で吸収できない API drift については `.local/ticket/` でフォローアップ ticket 化)
val compilerPluginBaselineKotlin: String = libs.versions.kotlin.get()

dependencies {
    api(projects.annotation)

    add(embedded.name, projects.compilerPlugin.compat)
    add(embedded.name, projects.compilerPluginCompatK230)
    add(embedded.name, projects.compilerPluginCompatK240Beta2)

    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:$compilerPluginBaselineKotlin")

    testImplementation("dev.zacsweers.kctfork:core:$kctforkVersion")
    testImplementation(libs.kotestFrameworkEngine)
    testImplementation(libs.kotestAssertionsCore)
    testImplementation(libs.kotestRunnerJunit5)
    testImplementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:$testKotlinVersion")
    testImplementation(libs.composeUiToolingPreview)
}

kotlin {
    compilerOptions {
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        // -Ptest.kotlin で 2.4-Beta などを指定された場合、test classpath には新しい
        // kotlin-compiler-embeddable metadata が混在する。compileTest は KGP 2.3.21 で走るので
        // metadata version mismatch が出るのを回避。
        freeCompilerArgs.addAll(
            "-Xskip-prerelease-check",
            "-Xskip-metadata-version-check",
        )
    }
}

// kotlin-compiler-embeddable の version 強制は test classpath だけに限定する。
// 全 configuration に適用すると KGP 自体が使う classpath にも影響して
// "Could not initialize class org.jetbrains.kotlin.buildtools.internal..." エラーになる。
// main の compileOnly は compilerPluginBaselineKotlin に固定しているので main 側には影響なし。
listOf("testCompileClasspath", "testRuntimeClasspath").forEach { name ->
    configurations.named(name).configure {
        resolutionStrategy {
            force("org.jetbrains.kotlin:kotlin-compiler-embeddable:$testKotlinVersion")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    // kctfork 内部の kotlin-compiler-embeddable が Java 26 の version 文字列を
    // 解析できないため、テスト JVM は Java 21 で実行する
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        },
    )
}

// デフォルト jar task は無効化し、shadowJar を main artifact として publish する
tasks.named<Jar>("jar") {
    enabled = false
}

val shadowJar = tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier = ""
    from(java.sourceSets["main"].output)
    configurations = listOf(embeddedClasspath)

    // 各 compat module の META-INF/services/.../CompatContext$Factory をマージ
    mergeServiceFiles()

    // Kotlin / IDE の共有依存は除外 (compileOnly で外から渡される想定)
    dependencies {
        exclude(dependency("org.jetbrains.kotlin:.*"))
        exclude(dependency("org.jetbrains:annotations"))
    }

    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

// publish される artifact (apiElements, runtimeElements) を shadowJar に差し替える
arrayOf("apiElements", "runtimeElements").forEach { name ->
    configurations.named(name).configure { artifacts.removeIf { true } }
    artifacts.add(name, shadowJar)
}

// assemble / build からも shadowJar を生成するように依存追加 (jar が disabled なため)
tasks.named("assemble") { dependsOn(shadowJar) }

publishConvention {
    artifactName = "Compiler Plugin"
    artifactId = "compiler-plugin"
    description =
        "A component catalog library that collects and lists @Preview. \n" +
        "By providing APIs such as Field, Event, etc., it provides not only display but also interactive preview.\n" +
        "compiler-plugin provides a Kotlin Compiler Plugin to collect @Preview."
}
