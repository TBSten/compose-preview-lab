package me.tbsten.compose.preview.lab.sample.helloComposePreviewLab

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import compose_preview_lab_integration_test.hellocomposepreviewlab.generated.resources.Res
import compose_preview_lab_integration_test.hellocomposepreviewlab.generated.resources.cover
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.field.BooleanField
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.previewlab.PreviewLabState
import me.tbsten.compose.preview.lab.previewlab.inspectorspane.InspectorTab
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.tooling.preview.Preview

/**
 * Example of a custom inspector tab implementation.
 *
 * This demonstrates how library users can create their own tabs
 * to extend PreviewLab's inspector functionality.
 */
object InfoTab : InspectorTab {
    override val title: String = "Info"
    override val icon: @Composable () -> Painter = { painterResource(Res.drawable.cover) }

    @Composable
    override fun InspectorTab.ContentContext.Content() {
        InfoTabContent(state)
    }
}

@Composable
private fun InfoTabContent(state: PreviewLabState) {
    Card(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Text(
                text = "Custom Tab Example",
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "This is a custom inspector tab!",
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "You can display any content here, such as:",
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("• Component documentation")
            Text("• Usage examples")
            Text("• Design guidelines")
            Text("• Custom debugging tools")
            Text("• Performance metrics")
            Text("• Accessibility information")

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Implementation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create your own InspectorTab by implementing the interface:\n\n" +
                    "• title: Display name for the tab\n" +
                    "• icon: Composable icon painter\n" +
                    "• content: Your custom UI content",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@ComposePreviewLabOption(displayName = "Custom Tab Example", id = "CustomTabExample")
@Preview
@Composable
private fun CustomTabExamplePreview() = PreviewLab(
    inspectorTabs = InspectorTab.defaults + listOf(InfoTab, InfoTab),
) {
    Column(
        modifier = Modifier.padding(16.dp),
    ) {
        Text(
            text = fieldValue { StringField("text", "Hello, Custom Tab!") },
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            enabled = fieldValue { BooleanField("enabled", true) },
            onClick = { onEvent("button clicked") },
        ) {
            Text("Click Me")
        }
    }
}
