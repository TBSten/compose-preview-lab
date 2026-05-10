import kotlinx.validation.ExperimentalBCVApi

plugins {
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.kotlinJvm).apply(false)
    alias(libs.plugins.composeCompiler).apply(false)
    alias(libs.plugins.composeMultiplatform).apply(false)
    alias(libs.plugins.lumoUi)
    alias(libs.plugins.androidApplication).apply(false)
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.hotReload).apply(false)
    alias(libs.plugins.vanniktechMavenPublish).apply(false)
    alias(libs.plugins.dokka)
    alias(libs.plugins.binaryCompatibilityValidator)
}

allprojects {
    group = "me.tbsten.compose.preview.lab"
    version = rootProject.libs.versions.composePreviewLab.get()
}

apiValidation {
    @OptIn(ExperimentalBCVApi::class)
    klib.enabled = true
    // The compiler-plugin and its compat layers are internal compiler infrastructure,
    // not consumer-facing API. The plugin entry point is a Gradle plugin DSL — the
    // FIR/IR generators inside `:compiler-plugin` and the per-Kotlin-version compat
    // implementations are bundled into a shadow jar and loaded by the Kotlin compiler
    // via ServiceLoader. Each new Kotlin version requires a new compat-kXXX module
    // that intentionally tracks compiler API drift, so a BCV baseline here would
    // flag every legitimate addition as a breaking change.
    ignoredProjects.addAll(
        listOf(
            projects.dev.name,
            projects.compilerPlugin.name,
            projects.compilerPlugin.compat.name,
            projects.compilerPlugin.compatK210.name,
            projects.compilerPlugin.compatK222.name,
            projects.compilerPlugin.compatK2220.name,
            projects.compilerPlugin.compatK230.name,
            projects.compilerPlugin.compatK2320.name,
            projects.compilerPlugin.compatK2321.name,
            projects.compilerPlugin.compatK240Beta2.name,
        ),
    )
    nonPublicMarkers.addAll(
        listOf(
            "me.tbsten.compose.preview.lab.InternalComposePreviewLabApi",
            "me.tbsten.compose.preview.lab.UiComposePreviewLabApi",
            // Experimental signatures are intentionally excluded from BCV baseline so
            // experimental→stable promotions show up as concrete diffs (the marker
            // would otherwise be invisible to apiCheck because BCV does not record
            // @RequiresOptIn markers in dump output).
            //
            // Known limitation (#issuecomment-4413236974, #issuecomment-4413308245):
            // annotation-class properties marked with `@property:ExperimentalComposePreviewLabApi`
            // (e.g. `ComposePreviewLabOption.collectScopes`) are filtered correctly only on
            // KLIB targets. On JVM / Android the Kotlin compiler attaches the marker to a
            // synthetic `<name>$annotations()` helper rather than the abstract getter, so
            // BCV's method-level annotation scan does not see the marker and the property
            // signature stays in `*.api` baselines. The use-site qualifiers `@get:` /
            // `@field:` / `@param:` are forbidden on `@RequiresOptIn` markers by Kotlin's
            // opt-in mechanism, so we cannot work around this from the Kotlin source side
            // alone. Tracked for upstream (BCV); until then, treat experimental→stable
            // promotions of annotation-class properties as a release-notes / KDoc concern,
            // not a baseline-diff signal.
            "me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi",
        ),
    )
}

val logVersion by tasks.registering {
    val version = libs.versions.composePreviewLab
    doLast {
        println(version.get())
    }
}
