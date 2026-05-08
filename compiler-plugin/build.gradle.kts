import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.conventionJvm)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
    alias(libs.plugins.shadow)
}

/**
 * The published compiler-plugin jar bundles every compat module so the gradle-plugin
 * only has to point at this single artifact via SubpluginArtifact. ServiceLoader picks
 * the implementation that matches the Kotlin compiler at runtime.
 *
 * This is a trimmed port of the embedded-configuration + ShadowJar pattern used in
 * Metro (https://github.com/ZacSweers/metro/blob/main/compiler/build.gradle.kts).
 */
val embedded by configurations.dependencyScope("embedded")
val embeddedClasspath by configurations.resolvable("embeddedClasspath") { extendsFrom(embedded) }

// Make the embedded modules visible from the compile / test classpath as well.
configurations.named("compileOnly").configure { extendsFrom(embedded) }
configurations.named("testImplementation").configure { extendsFrom(embedded) }

// `-Ptest.kotlin=X.Y.Z` lets smoke-test.sh swap the kotlin-compiler-embeddable that
// kctfork drives. It is used to verify each version listed in
// `scripts/supported-kotlin-versions.txt`.
val testKotlinVersion: String = (findProperty("test.kotlin") as String?) ?: libs.versions.kotlin.get()

// kctfork version selection per Kotlin version.
// - 2.1.x: 0.10.0 (K2JVMCompilerArguments.jvmDefaultStable absent before 2.2.0)
// - 2.4.x: 0.13.0-alpha01 (newer API changes)
val kctforkVersion: String = when {
    testKotlinVersion.startsWith("2.1") -> "0.10.0"
    testKotlinVersion.startsWith("2.4") -> "0.13.0-alpha01"
    else -> libs.versions.kctfork.get()
}

// Kotlin compiler API version against which the compiler-plugin main module is compiled.
// Anything that CompatContext does not abstract (lambda creation, IR builders, ...) calls
// into Kotlin compiler internal API directly, so a binary mismatch between this version
// and the runtime version surfaces as NoSuchMethodError etc.
// → Pin this to libs.versions.kotlin and only support versions that match it or are
//   forward-compatible with it. API drift that the compat modules cannot absorb is tracked
//   as follow-up tickets under `.local/ticket/`.
val compilerPluginBaselineKotlin: String = libs.versions.kotlin.get()

dependencies {
    api(projects.annotation)

    add(embedded.name, projects.compilerPlugin.compat)
    add(embedded.name, projects.compilerPlugin.compatK210)
    add(embedded.name, projects.compilerPlugin.compatK222)
    add(embedded.name, projects.compilerPlugin.compatK2220)
    add(embedded.name, projects.compilerPlugin.compatK230)
    add(embedded.name, projects.compilerPlugin.compatK2321)
    add(embedded.name, projects.compilerPlugin.compatK240Beta2)

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
        // FIR Checker / diagnostic factory APIs in Kotlin 2.3+ (`error1`, the abstract
        // `FirDeclarationChecker.check`, etc.) are declared with `context(...)`
        // parameters, so any code that overrides those bases or calls those extensions
        // from this module needs the experimental context-parameters language feature.
        // The flag only affects this module's main compilation; downstream consumers of
        // the published plugin jar are unaffected.
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

// When -Ptest.kotlin selects 2.4-Beta or similar, the test classpath ends up with newer
// kotlin-compiler-embeddable metadata. compileTestKotlin runs with KGP 2.3.21, so this
// suppresses the resulting metadata-version mismatch errors.
// Only apply when testKotlinVersion differs from the baseline to avoid weakening the main
// compilation safety checks.
if (testKotlinVersion != compilerPluginBaselineKotlin) {
    tasks.named<KotlinCompile>("compileTestKotlin") {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xskip-prerelease-check",
                "-Xskip-metadata-version-check",
            )
        }
    }
}

// Restrict the kotlin-compiler-embeddable version override to the test classpath only.
// Applying it to every configuration also affects the classpath that KGP itself uses,
// which leads to "Could not initialize class org.jetbrains.kotlin.buildtools.internal..." errors.
// The main compileOnly is already pinned to compilerPluginBaselineKotlin, so the main classpath
// is not affected.
listOf("testCompileClasspath", "testRuntimeClasspath").forEach { name ->
    configurations.named(name).configure {
        resolutionStrategy {
            force("org.jetbrains.kotlin:kotlin-compiler-embeddable:$testKotlinVersion")
        }
    }
}

// Resolvable configuration that pulls `kotlin-stdlib-js` as a `.klib` artifact, bypassing
// the Kotlin Multiplatform variant resolution rules that would reject it on a JVM consumer.
// kctfork's `KotlinJsCompilation` needs this to feed stage-1 KLIBs through `-libraries`.
val kotlinStdlibJsKlib by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}
dependencies {
    add(kotlinStdlibJsKlib.name, "org.jetbrains.kotlin:kotlin-stdlib-js:$testKotlinVersion@klib")
}

// Configuration-cache compatible argument provider for the kotlin-stdlib-js KLIB path.
// Captures only the FileCollection (declared as @Classpath input) so the script object
// reference does not get serialized.
abstract class StdlibJsKlibArgumentProvider : CommandLineArgumentProvider {
    @get:org.gradle.api.tasks.Classpath
    abstract val klibFiles: ConfigurableFileCollection

    override fun asArguments(): List<String> = listOf("-Dtest.kotlin.stdlib.js.klib=${klibFiles.singleFile.absolutePath}")
}

tasks.withType<Test> {
    useJUnitPlatform()
    // Pass testKotlinVersion to tests so CompilerPluginTestBase can set languageVersion
    // accordingly. In Kotlin 2.1.x, FirIncompatibleClassExpressionChecker crashes when
    // languageVersion="2.0" is used with a 2.1.x compiler (source must not be null).
    systemProperty("test.kotlin.version", testKotlinVersion)
    // Surface kotlin-stdlib-js KLIB to the JS test base so it can wire `args.libraries`.
    jvmArgumentProviders.add(
        objects.newInstance(StdlibJsKlibArgumentProvider::class).apply {
            klibFiles.from(kotlinStdlibJsKlib)
        },
    )
    // kctfork bundles a kotlin-compiler-embeddable that cannot parse Java 26 version
    // strings, so the test JVM has to run on Java 21.
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        },
    )
}

// Disable the default jar task; shadowJar takes its place as the published artifact.
tasks.named<Jar>("jar") {
    enabled = false
}

val shadowJar = tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier = ""
    from(java.sourceSets["main"].output)
    configurations = listOf(embeddedClasspath)

    // Merge each compat module's META-INF/services/.../CompatContext$Factory entries.
    mergeServiceFiles()

    // Exclude shared Kotlin / IDE dependencies; they come in via compileOnly from the outside.
    dependencies {
        exclude(dependency("org.jetbrains.kotlin:.*"))
        exclude(dependency("org.jetbrains:annotations"))
    }

    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

// Replace the published artifacts (apiElements / runtimeElements) with the shadow jar.
arrayOf("apiElements", "runtimeElements").forEach { name ->
    configurations.named(name).configure { artifacts.removeIf { true } }
    artifacts.add(name, shadowJar)
}

// `assemble` / `build` should still produce shadowJar even though the default jar task is disabled.
tasks.named("assemble") { dependsOn(shadowJar) }

publishConvention {
    artifactName = "Compiler Plugin"
    artifactId = "compiler-plugin"
    description =
        "A component catalog library that collects and lists @Preview. \n" +
        "By providing APIs such as Field, Event, etc., it provides not only display but also interactive preview.\n" +
        "compiler-plugin provides a Kotlin Compiler Plugin to collect @Preview."
}
