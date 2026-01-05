package me.tbsten.compose.preview.lab.sample.extension.navigation

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.extension.navigation.NavControllerField
import me.tbsten.compose.preview.lab.field.FixedField
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.combined
import me.tbsten.compose.preview.lab.field.splitedOf
import me.tbsten.compose.preview.lab.previewlab.PreviewLab

// Type-safe route definitions
@Serializable
data object Home

@Serializable
data class Profile(val userId: String)

@Serializable
object Settings

/**
 * Example showing how to use NavControllerField with type-safe routes.
 * This preview demonstrates:
 * - NavGraph structure visualization
 * - BackStack tracking
 * - Route selection via PolymorphicField with editable parameters
 */
@ComposePreviewLabOption(id = "NavControllerFieldExample")
@Preview
@Composable
private fun NavControllerFieldExample() = PreviewLab {
    @Suppress("ktlint:standard:backing-property-naming", "LocalVariableName")
    val _navController = rememberNavController()
    val navController = fieldValue("navController") {
        NavControllerField(
            label = "navController",
            navController = _navController,
            routes = listOf(
                FixedField("Home", Home),
                FixedField("Settings", Settings),
                combined(
                    label = "Profile",
                    field1 = StringField("userId", "default"),
                    combine = { userId: String -> Profile(userId = userId) },
                    split = { profile: Profile -> splitedOf(profile.userId) },
                ),
            ),
        )
    }

    SampleNavHost(navController = navController)
}

@Composable
internal fun SampleNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Home,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable<Home> {
            HomeScreen(onNavigate = { navController.navigate(Profile(userId = "user1")) })
        }
        composable<Profile> { backStackEntry ->
            val profile: Profile = backStackEntry.toRoute()
            ProfileScreen(
                userId = profile.userId,
                onNavigate = { navController.navigate(Settings) },
            )
        }
        composable<Settings> {
            SettingsScreen(onNavigate = { navController.navigate(Home) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(onNavigate: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigate,
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
private fun ProfileScreen(userId: String, onNavigate: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
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
                onClick = onNavigate,
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
private fun SettingsScreen(onNavigate: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            SettingsItem("Notifications")
            SettingsItem("Dark Mode")
            SettingsItem("Privacy")
            SettingsItem("About")
            Spacer(Modifier.weight(1f))
            ExtendedFloatingActionButton(
                onClick = onNavigate,
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
private fun SettingsItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    )
}
