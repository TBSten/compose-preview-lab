package me.tbsten.compose.preview.lab.screenshot

import io.kotest.core.Tag

/**
 * Kotest tag for the UI screenshot harness.
 *
 * Run only the screenshot cases with `-Dkotest.tags='Screenshot'`.
 * The cases additionally skip themselves unless the `previewlab.screenshot.dir`
 * system property is set (see [ScreenshotOutput]), so a plain `jvmTest` run
 * (e.g. CI's `-Dkotest.tags='!PBT'`) does not produce any files.
 */
object Screenshot : Tag()
