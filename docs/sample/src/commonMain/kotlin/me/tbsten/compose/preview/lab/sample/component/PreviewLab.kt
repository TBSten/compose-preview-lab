package me.tbsten.compose.preview.lab.sample.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.field.ScreenSize
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.previewlab.PreviewLabDefaults
import me.tbsten.compose.preview.lab.previewlab.PreviewLabScope
import me.tbsten.compose.preview.lab.previewlab.inspectorspane.InspectorTab

@Composable
fun SamplePreviewLab(
    modifier: Modifier = Modifier,
    inspectorTabs: List<InspectorTab> = PreviewLabDefaults.inspectorTabs(),
    enable: Boolean = PreviewLabDefaults.enable(),
    content: @Composable PreviewLabScope.() -> Unit,
) = PreviewLab(
    modifier = modifier,
    showScreenSizeField = false,
    screenSizes = listOf(ScreenSize(320.dp, 400.dp)) + PreviewLabDefaults.screenSizes(),
    inspectorTabs = inspectorTabs,
    enable = enable,
    content = content,
)
