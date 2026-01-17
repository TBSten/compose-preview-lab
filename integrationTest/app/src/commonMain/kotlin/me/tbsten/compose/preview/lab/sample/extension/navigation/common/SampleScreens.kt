package me.tbsten.compose.preview.lab.sample.extension.navigation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleHomeScreen(title: String = "Home", onNavigateProfile: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateProfile,
                containerColor = Color(0xFF6200EE),
                contentColor = Color.White,
            ) {
                Text("Go to Profile →")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Welcome!", style = MaterialTheme.typography.headlineLarge)
            Text("This is the home screen.", style = MaterialTheme.typography.bodyLarge)
            Text("Tap the button below to view a profile.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleProfileScreen(title: String = "Profile", userId: String, onSettingsNavigate: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF03DAC5),
                    titleContentColor = Color.Black,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))
            Text("User ID: $userId", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text("This is the profile page.", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            ExtendedFloatingActionButton(
                onClick = onSettingsNavigate,
                containerColor = Color(0xFF03DAC5),
                contentColor = Color.Black,
            ) {
                Text("Go to Settings →")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleSettingsScreen(title: String = "Settings", onNavigateHome: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF5722),
                    titleContentColor = Color.White,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            SampleSettingsItem("Notifications")
            SampleSettingsItem("Dark Mode")
            SampleSettingsItem("Privacy")
            SampleSettingsItem("About")
            Spacer(Modifier.weight(1f))
            ExtendedFloatingActionButton(
                onClick = onNavigateHome,
                containerColor = Color(0xFFFF5722),
                contentColor = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text("Back to Home →")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun SampleSettingsItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    )
}
