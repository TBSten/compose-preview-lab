package me.tbsten.compose.preview.lab

import org.gradle.api.Project
import org.gradle.api.provider.Provider

/**
 * Single source of truth for `gradle.properties` keys recognized by the Compose Preview Lab
 * Gradle plugin.
 *
 * Every key in [AllKeys] corresponds 1:1 with a property exposed by [ComposePreviewLabExtension]
 * (or its nested config). When `composePreviewLab.<key>` is set in `gradle.properties` (or on
 * the command line via `-PcomposePreviewLab.<key>=value`), it is wired as the **convention**
 * of the matching DSL property — meaning the DSL block still wins if it sets the property
 * explicitly. The full precedence chain is therefore:
 *
 * DSL block > gradle.properties > built-in default.
 *
 * See also [ComposePreviewLabExtension] for the matching DSL definition and
 * [warnOnUnknownComposePreviewLabProperties] for the unknown-key warning.
 */
internal object ComposePreviewLabProperties {
    /** Prefix shared by every recognized property key. */
    const val Prefix: String = "composePreviewLab."

    const val GeneratePackage: String = "composePreviewLab.generatePackage"
    const val ProjectRootPath: String = "composePreviewLab.projectRootPath"
    const val GenerateFeaturedFiles: String = "composePreviewLab.generateFeaturedFiles"
    const val CollectPreviewsEnabled: String = "composePreviewLab.collectPreviews.enabled"
    const val CollectPreviewsDefaultCollectScope: String =
        "composePreviewLab.collectPreviews.defaultCollectScope"

    /** Set of every recognized key, used for invalid-key detection. */
    val AllKeys: Set<String> = setOf(
        GeneratePackage,
        ProjectRootPath,
        GenerateFeaturedFiles,
        CollectPreviewsEnabled,
        CollectPreviewsDefaultCollectScope,
    )
}

/**
 * Returns a [Provider] that resolves to the value of [key] from `gradle.properties` (or `-P`),
 * falling back to [default] when the property is absent or blank.
 *
 * The provider is intended to be passed to `Property.convention(...)`, so the DSL block
 * (`composePreviewLab { ... }`) keeps the final say while still benefiting from
 * `gradle.properties` overrides of the built-in default.
 *
 * **Resolution sources** (first non-blank wins):
 *
 * 1. `project.providers.gradleProperty(key)` — covers root `gradle.properties`,
 *    `GRADLE_USER_HOME/gradle.properties`, `-P` CLI flags, environment variables (`ORG_GRADLE_PROJECT_*`)
 *    and system properties.
 * 2. `project.findProperty(key)` — covers **subproject** `gradle.properties` files
 *    (e.g. `integrationTest/uiLibReversed/gradle.properties`), which
 *    [org.gradle.api.provider.ProviderFactory.gradleProperty] historically does not see.
 *
 * Without (2) a subproject-local `gradle.properties` entry like
 * `composePreviewLab.generateFeaturedFiles=true` would be silently ignored and the convention
 * would fall back to [default] — which is the bug verify-and-fix found in task-024.
 */
internal fun stringProp(project: Project, key: String, default: String): Provider<String> = rawStringProp(project, key)
    .map { it.ifBlank { default } }
    .orElse(default)

/**
 * Boolean variant of [stringProp]. Accepts `"true"` / `"false"` (case-insensitive); any
 * other value falls back to [default]. Blank / missing values also fall back.
 *
 * See [stringProp] for the resolution-source precedence (root `gradle.properties` /
 * `GRADLE_USER_HOME` / `-P` / env / system → subproject `gradle.properties` → [default]).
 */
internal fun boolProp(project: Project, key: String, default: Boolean): Provider<Boolean> = rawStringProp(project, key)
    .map { raw -> parseBoolProp(raw, default) }
    .orElse(default)

/**
 * Resolves [key] as a raw `String?` covering both root-level / `-P` / env / system properties
 * (via `providers.gradleProperty`) AND subproject `gradle.properties` (via `findProperty`).
 *
 * Returned provider is missing (not present) when neither source has a non-null value, so
 * downstream `.orElse(default)` kicks in. We deliberately route subproject lookup through
 * `project.provider { ... }` so the value is sampled lazily during configuration — it still
 * runs once per build, but only when the [Property.convention] is actually queried.
 */
private fun rawStringProp(project: Project, key: String): Provider<String> {
    // `filter { it.isNotBlank() }` ensures a blank value in root `gradle.properties` (e.g.
    // `composePreviewLab.generatePackage=` with an empty value) does NOT mask a non-blank
    // subproject `gradle.properties` value picked up by `findProperty` fallback.
    val fromGradleProperty = project.providers.gradleProperty(key).filter { it.isNotBlank() }
    val fromFindProperty = project.provider {
        // findProperty covers subproject gradle.properties files which `gradleProperty(...)` skips.
        (project.findProperty(key) as? String)?.takeIf { it.isNotBlank() }
    }
    return fromGradleProperty.orElse(fromFindProperty)
}

/**
 * Parses a raw `gradle.properties` value as a Boolean.
 *
 * - `"true"` / `"false"` (case-insensitive) → corresponding Boolean
 * - any other non-blank value → [default] (silently — invalid Boolean strings are tolerated to
 *   avoid hard-failing builds on typos; the unknown-key warning path covers the much more
 *   common "wrong key" mistake)
 * - blank / null-ish → [default]
 */
internal fun parseBoolProp(raw: String?, default: Boolean): Boolean {
    val trimmed = raw?.trim().orEmpty()
    return when {
        trimmed.isEmpty() -> default
        trimmed.equals("true", ignoreCase = true) -> true
        trimmed.equals("false", ignoreCase = true) -> false
        else -> default
    }
}

/**
 * Pure function counterpart of [warnOnUnknownComposePreviewLabProperties]: scans [keys] and
 * returns the unknown subset (alphabetically sorted) under [ComposePreviewLabProperties.Prefix].
 *
 * Extracted so unit tests can verify the detection logic without going through a real
 * Gradle [Project] / logger.
 */
internal fun unknownComposePreviewLabPropertyKeys(keys: Iterable<String>): List<String> = keys.asSequence()
    .filter { it.startsWith(ComposePreviewLabProperties.Prefix) }
    .filter { it !in ComposePreviewLabProperties.AllKeys }
    .sorted()
    .toList()

/**
 * Builds the warning message that [warnOnUnknownComposePreviewLabProperties] emits for the
 * given [unknown] keys. Returns `null` when [unknown] is empty (i.e. no warning to log).
 */
internal fun unknownComposePreviewLabPropertyWarning(unknown: List<String>): String? {
    if (unknown.isEmpty()) return null
    return buildString {
        append("[compose-preview-lab] Unknown gradle.properties key(s) under '")
        append(ComposePreviewLabProperties.Prefix)
        append("*' detected: ")
        append(unknown.joinToString(", "))
        append(". Known keys are: ")
        append(ComposePreviewLabProperties.AllKeys.sorted().joinToString(", "))
        append('.')
    }
}

/**
 * Walks `project.properties` and logs a `warn` for every key that starts with
 * [ComposePreviewLabProperties.Prefix] but is not listed in [ComposePreviewLabProperties.AllKeys].
 *
 * Catches typos (`composePreviewLab.generatePackge=...`) that would otherwise be silently
 * ignored by Gradle. Designed to be cheap: one filtered scan per project.
 *
 * The warning is **deduplicated at the root-project level** via an `extraProperties` flag,
 * so a typo in the root `gradle.properties` fires exactly once even when the plugin is
 * applied to many subprojects. Without this, a monorepo with N modules emits the same
 * warning N times for the same root-level typo (violates Single Source of Truth).
 */
internal fun warnOnUnknownComposePreviewLabProperties(project: Project) {
    val unknown = unknownComposePreviewLabPropertyKeys(project.properties.keys)
    val message = unknownComposePreviewLabPropertyWarning(unknown) ?: return
    val rootProject = project.rootProject
    val flagKey = "composePreviewLab._unknownKeyWarningEmitted"
    synchronized(rootProject) {
        val extra = rootProject.extensions.extraProperties
        if (extra.has(flagKey)) return
        extra.set(flagKey, true)
        rootProject.logger.warn(message)
    }
}
