package me.tbsten.compose.preview.lab.previewlab.inspectorspane

internal actual val DefaultInspectorTabs: List<InspectorTab> = SharedDefaultInspectorTabs + listOf(
    LayoutAndSemanticsTab,
).also { println("jvm DefaultInspectorTabs") }
