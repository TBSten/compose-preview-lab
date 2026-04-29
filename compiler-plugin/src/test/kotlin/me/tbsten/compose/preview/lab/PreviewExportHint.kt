package me.tbsten.compose.preview.lab

/** Test stub for PreviewExportHint annotation (mirrors the real annotation in `core`). */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class PreviewExportHint(val fqn: String)
