package me.tbsten.compose.preview.lab.sample.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.field.ScreenSize
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.previewlab.PreviewLabScope
import me.tbsten.compose.preview.lab.previewlab.inspectorspane.InspectorTab

@Composable
fun SamplePreviewLab(
    modifier: Modifier = Modifier,
    inspectorTabs: List<InspectorTab> = InspectorTab.defaults,
    content: @Composable PreviewLabScope.() -> Unit,
) = PreviewLab(
    modifier = modifier,
    screenSizes = listOf(ScreenSize(320.dp, 400.dp)) + ScreenSize.SmartphoneAndDesktops,
    inspectorTabs = inspectorTabs,
    content = content,
)
