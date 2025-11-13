package me.tbsten.compose.preview.lab.sample.lib.testcase

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLab

/**
 * Example screen components that will use the generated field() functions
 */

@Composable
fun SimpleScreen(uiState: SimpleUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("String: ${uiState.str}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Int: ${uiState.int}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Boolean: ${uiState.bool}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun NestedScreen(uiState: NestedUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = uiState.section1State.heading,
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.section1State.body,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { },
                enabled = uiState.enableButton,
            ) {
                Text("Action Button")
            }
        }
    }
}

@Composable
fun ComplexScreen(uiState: ComplexUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        // User Info Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("User Information", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Name: ${uiState.userInfo.name}")
                Text("Age: ${uiState.userInfo.age}")
                Text("Verified: ${if (uiState.userInfo.isVerified) "Yes" else "No"}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Settings Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Settings", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Dark Mode: ${if (uiState.settings.isDarkMode) "On" else "Off"}")
                Text("Font Size: ${uiState.settings.fontSize}")
                Text("Volume: ${uiState.settings.volume}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status
        if (uiState.isLoading) {
            Text("Loading...", style = MaterialTheme.typography.bodyLarge)
        }
        if (uiState.errorMessage.isNotEmpty()) {
            Text(
                text = "Error: ${uiState.errorMessage}",
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

/**
 * PreviewLab usage examples
 */

@OptIn(InternalComposePreviewLabApi::class)
@Composable
private fun SimpleScreenPreview() = PreviewLab {
    val uiState = fieldValue { SimpleUiState.field("uiState", SimpleUiState.fake()) }

    SimpleScreen(uiState = uiState)
}

@OptIn(InternalComposePreviewLabApi::class)
@Composable
private fun NestedScreenPreview() = PreviewLab {
    val uiState = fieldValue { NestedUiState.field("uiState", NestedUiState.fake()) }

    NestedScreen(uiState = uiState)
}

@OptIn(InternalComposePreviewLabApi::class)
@Composable
private fun ComplexScreenPreview() = PreviewLab {
    val uiState = fieldValue { ComplexUiState.field("uiState", ComplexUiState.fake()) }

    ComplexScreen(uiState = uiState)
}
