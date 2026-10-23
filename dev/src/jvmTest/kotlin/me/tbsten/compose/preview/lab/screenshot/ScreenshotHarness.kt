@file:OptIn(ExperimentalTestApi::class)

package me.tbsten.compose.preview.lab.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import io.kotest.core.spec.style.StringSpec
import java.io.File
import javax.imageio.ImageIO
import me.tbsten.compose.preview.lab.LocalIsInPreviewLabGalleryCardBody
import me.tbsten.compose.preview.lab.LocalPreviewLabPreview
import me.tbsten.compose.preview.lab.PreviewLabPreview
import me.tbsten.compose.preview.lab.previewlab.LocalEnforcePreviewLabState
import me.tbsten.compose.preview.lab.previewlab.PreviewLabState

/**
 * System property that enables the screenshot harness and selects the output directory.
 *
 * `dev/build.gradle.kts` forwards every `previewlab.*` system property to the test JVM:
 *
 * ```
 * ./gradlew :dev:jvmTest --tests 'me.tbsten.compose.preview.lab.screenshot.*' \
 *     -Dkotest.tags='Screenshot' -Dpreviewlab.screenshot.dir=/path/to/output
 * ```
 */
internal const val ScreenshotDirProperty = "previewlab.screenshot.dir"

/**
 * Where screenshots and the `manifest.json` go. When [isEnabled] is false (property not set)
 * every screenshot test case is disabled (reported as skipped by Kotest).
 */
internal object ScreenshotOutput {
    val dir: File? = System.getProperty(ScreenshotDirProperty)
        ?.takeIf { it.isNotBlank() }
        ?.let(::File)

    val isEnabled: Boolean get() = dir != null

    private data class ManifestEntry(
        val file: String?,
        val title: String,
        val group: String,
        val status: String,
        val note: String?,
    )

    private val entries = mutableListOf<ManifestEntry>()

    /** Write [image] to `<dir>/<fileName>` and record it in the manifest. */
    @Synchronized
    fun save(image: ImageBitmap, fileName: String, title: String, group: String, note: String? = null): File {
        val outDir = requireNotNull(dir) { "$ScreenshotDirProperty is not set" }
        outDir.mkdirs()
        val file = File(outDir, fileName)
        ImageIO.write(image.toAwtImage(), "png", file)
        println("[screenshot] wrote ${file.absolutePath} (${image.width}x${image.height})")
        record(ManifestEntry(file = fileName, title = title, group = group, status = "ok", note = note))
        return file
    }

    /** Record a case that could not be captured on purpose (e.g. interaction impossible headlessly). */
    @Synchronized
    fun skipped(fileName: String, title: String, group: String, reason: String) {
        println("[screenshot] skipped $fileName: $reason")
        record(ManifestEntry(file = fileName, title = title, group = group, status = "skipped", note = reason))
    }

    /** Record a case whose capture threw. The test itself still fails. */
    @Synchronized
    fun failed(fileName: String, title: String, group: String, error: Throwable) {
        record(
            ManifestEntry(
                file = fileName,
                title = title,
                group = group,
                status = "failed",
                note = "${error::class.qualifiedName}: ${error.message}",
            ),
        )
    }

    private fun record(entry: ManifestEntry) {
        entries.removeAll { it.file == entry.file && it.title == entry.title }
        entries += entry
        writeManifest()
    }

    /**
     * Rewrites `manifest.json` (a JSON array of `{file, title, group, status, note}`) after every
     * entry so that the manifest is complete even if a later test case crashes the JVM.
     */
    @Synchronized
    fun writeManifest() {
        val outDir = dir ?: return
        outDir.mkdirs()
        val json = buildString {
            append("[\n")
            entries.sortedWith(compareBy({ it.file ?: "" }, { it.title })).forEachIndexed { index, e ->
                if (index > 0) append(",\n")
                append("  {")
                append("\"file\": ").append(jsonString(e.file)).append(", ")
                append("\"title\": ").append(jsonString(e.title)).append(", ")
                append("\"group\": ").append(jsonString(e.group)).append(", ")
                append("\"status\": ").append(jsonString(e.status)).append(", ")
                append("\"note\": ").append(jsonString(e.note))
                append("}")
            }
            append("\n]\n")
        }
        File(outDir, "manifest.json").writeText(json)
    }

    private fun jsonString(value: String?): String {
        if (value == null) return "null"
        val escaped = buildString {
            value.forEach { c ->
                when (c) {
                    '"' -> append("\\\"")
                    '\\' -> append("\\\\")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> if (c < ' ') append("\\u%04x".format(c.code)) else append(c)
                }
            }
        }
        return "\"$escaped\""
    }
}

/**
 * Declares one screenshot test case.
 *
 * - tagged with [Screenshot] so it can be selected with `-Dkotest.tags='Screenshot'`
 * - disabled (skipped) unless `previewlab.screenshot.dir` is set
 * - exceptions are printed and recorded in the manifest, then rethrown so the case fails
 *   without blocking the other cases.
 */
internal fun StringSpec.screenshotCase(title: String, group: String, fileName: String, block: suspend () -> Unit,) {
    "[$group] $title ($fileName)".config(
        tags = setOf(Screenshot),
        enabled = ScreenshotOutput.isEnabled,
    ) {
        try {
            block()
        } catch (e: Throwable) {
            System.err.println("[screenshot] FAILED $fileName ($title): $e")
            e.printStackTrace()
            ScreenshotOutput.failed(fileName, title, group, e)
            throw e
        }
    }
}

/**
 * Provides the CompositionLocals PreviewLab needs in a desktop UI test, without hiding its full UI.
 *
 * Unlike `TestPreviewLab` (dev/src/jvmTest/.../testing/TestPreviewLab.kt), which sets
 * [LocalIsInPreviewLabGalleryCardBody] to `true` to skip the header / inspector / toast host,
 * this host keeps it `false` so the complete PreviewLab chrome is rendered and captured.
 *
 * @param state When non-null, forced into every `PreviewLab { }` below via [LocalEnforcePreviewLabState]
 *   so the test can drive inspector tabs, panel visibility, etc. Leave null for the Gallery.
 * @param preview Provided as [LocalPreviewLabPreview] (used by the KDoc tab and header).
 */
@Composable
internal fun ScreenshotHost(
    state: PreviewLabState? = null,
    preview: PreviewLabPreview? = null,
    content: @Composable () -> Unit,
) {
    val viewModelStoreOwner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = ViewModelStore()
        }
    }
    val lifecycleOwner = remember {
        object : LifecycleOwner {
            override val lifecycle: Lifecycle =
                LifecycleRegistry(this).apply {
                    handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                    handleLifecycleEvent(Lifecycle.Event.ON_START)
                    handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
                }
        }
    }
    CompositionLocalProvider(
        LocalViewModelStoreOwner provides viewModelStoreOwner,
        LocalLifecycleOwner provides lifecycleOwner,
        @Suppress("VisibleForTests")
        LocalEnforcePreviewLabState provides state,
        LocalPreviewLabPreview provides preview,
        // Render the FULL PreviewLab UI (header + content + inspector pane + toast host).
        LocalIsInPreviewLabGalleryCardBody provides false,
    ) {
        content()
    }
}

/**
 * Captures the whole window.
 *
 * `onRoot()` fails when a Popup / Dialog (menu, modal, tooltip) is open because each of them adds
 * another root node. The scene render used by `captureToImage()` includes those layers, so we pick
 * the largest root (the window) and crop to it.
 */
internal fun ComposeUiTest.captureWindow(): ImageBitmap {
    val roots = onAllNodes(isRoot()).fetchSemanticsNodes()
    check(roots.isNotEmpty()) { "No root semantics node found" }
    val mainRootIndex = roots.indices.maxBy { roots[it].size.width.toLong() * roots[it].size.height }
    return onAllNodes(isRoot())[mainRootIndex].captureToImage()
}

/**
 * Clicks the clickable node with the smallest area (used for the 20dp "⋮" field menu icon button,
 * which has no content description).
 */
internal fun ComposeUiTest.clickSmallestClickable(): SemanticsNodeInteraction {
    val nodes = onAllNodes(hasClickAction(), useUnmergedTree = true).fetchSemanticsNodes()
    check(nodes.isNotEmpty()) { "No clickable node found" }
    val index = nodes.indices.minBy { nodes[it].size.width.toLong() * nodes[it].size.height }
    return onAllNodes(hasClickAction(), useUnmergedTree = true)[index].also { it.performClick() }
}

/**
 * Clicks the first clickable node whose text contains [text] (merged tree first, then unmerged).
 * Falls back to the largest clickable node so that e.g. a summary card is still opened when its
 * text is not merged into the clickable semantics node.
 */
internal fun ComposeUiTest.clickClickableWithText(text: String) {
    val matcher = hasClickAction() and hasText(text, substring = true)
    if (onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()) {
        onAllNodes(matcher)[0].performClick()
        return
    }
    if (onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()) {
        onAllNodes(matcher, useUnmergedTree = true)[0].performClick()
        return
    }
    val nodes = onAllNodes(hasClickAction()).fetchSemanticsNodes()
    check(nodes.isNotEmpty()) { "No clickable node found (looked for text \"$text\")" }
    val index = nodes.indices.maxBy { nodes[it].size.width.toLong() * nodes[it].size.height }
    onAllNodes(hasClickAction())[index].performClick()
}
