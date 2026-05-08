package me.tbsten.compose.preview.lab.compiler.feature

import java.io.File

/**
 * Shared scan helpers for tests that inspect output of `PreviewHintFirGenerator`.
 *
 * Per-`@Preview` the generator emits two artifacts into
 * `me.tbsten.compose.preview.lab.hints.*`:
 *
 * - **marker interface** — class file `PreviewHintMarker_<sanitized_fqn>_<hash>.class`
 *   (Java reflection: a regular `Class<*>`).
 * - **hint function facade** — Kotlin file facade
 *   `PreviewHint_<hash>Kt.class` whose static `previewHint(...)` method returns the
 *   `CollectedPreview` for the original `@Preview`.
 *
 * The helpers below walk a kctfork `outputDirectory` for one or both kinds. Tests use
 * either the `*Files()` variants (return `File`s, useful for filename-pattern asserts)
 * or the `*Names()` variants (return FQNs, useful for `classLoader.loadClass(...)`).
 */
private const val HintsPackage = "me.tbsten.compose.preview.lab.hints"

/** Class files for `PreviewHintMarker_*` — synthetic marker interfaces (one per `@Preview`). */
internal fun File.previewHintMarkerFiles(): List<File> = walkTopDown()
    .filter { it.isFile && it.extension == "class" }
    .filter { it.parentFile.toRelativeString(this).normalizeAsPackage() == HintsPackage }
    .filter { it.name.startsWith("PreviewHintMarker_") && !it.name.endsWith("Kt.class") }
    .toList()

/** Class files for `PreviewHint_<hash>Kt.class` — synthetic Kotlin file facades carrying `previewHint(...)`. */
internal fun File.previewHintFacadeFiles(): List<File> = walkTopDown()
    .filter { it.isFile && it.extension == "class" }
    .filter { it.parentFile.toRelativeString(this).normalizeAsPackage() == HintsPackage }
    .filter { it.name.startsWith("PreviewHint_") && it.name.endsWith("Kt.class") }
    .toList()

/** FQNs of the synthetic marker interfaces — load via `classLoader.loadClass(...)`. */
internal fun File.previewHintMarkerNames(): List<String> = previewHintMarkerFiles()
    .map { "$HintsPackage.${it.nameWithoutExtension}" }

/** FQNs of the `PreviewHint_<hash>Kt` file facades — load via `classLoader.loadClass(...)`. */
internal fun File.previewHintFacadeNames(): List<String> = previewHintFacadeFiles()
    .map { "$HintsPackage.${it.nameWithoutExtension}" }

private fun String.normalizeAsPackage(): String = replace('/', '.').replace('\\', '.')
