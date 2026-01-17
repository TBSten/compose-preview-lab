package me.tbsten.compose.preview.lab.sample.extension.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.extension.navigation.NavControllerField
import me.tbsten.compose.preview.lab.field.FixedField
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.combined
import me.tbsten.compose.preview.lab.field.splitedOf
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.sample.extension.navigation.common.SampleHomeScreen
import me.tbsten.compose.preview.lab.sample.extension.navigation.common.SampleProfileScreen
import me.tbsten.compose.preview.lab.sample.extension.navigation.common.SampleSettingsScreen

// Type-safe route definitions
@Serializable
data object Home

@Serializable
data class Profile(val userId: String)

@Serializable
data object Settings

/**
 * Example showing how to use NavControllerField with type-safe routes.
 * This preview demonstrates:
 * - NavGraph structure visualization
 * - BackStack tracking
 * - Route selection via PolymorphicField with editable parameters
 */
@OptIn(ExperimentalComposePreviewLabApi::class)
@ComposePreviewLabOption(id = "NavControllerFieldExample")
@Preview
@Composable
private fun NavControllerFieldExample() = PreviewLab {
    @Suppress("ktlint:standard:backing-property-naming", "LocalVariableName")
    val _navController = rememberNavController()
    val navController = fieldValue {
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
    LaunchedEffect(Unit) {
        navController.graph.forEach { dest ->
            println("dest: ${dest::class.qualifiedName}")
            println("dest:     - detail: $dest")

            (dest as? ComposeNavigator.Destination)?.apply {
                val isHome = hasRoute<Home>()
                val isProfile = hasRoute<Profile>()
                val isSettings = hasRoute<Settings>()
                println("dest:     - isHome = $isHome")
                println("dest:     - isProfile = $isProfile")
                println("dest:     - isSettings = $isSettings")
            }

            (dest as? NavGraph)?.apply {
                println("dest:     - route = ${this.route}")
                println("dest:     - nodes = ${this.map { it.route }}")
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Home,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable<Home> {
            SampleHomeScreen(onNavigateProfile = { navController.navigate(Profile(userId = "user1")) })
        }
        composable<Profile> { backStackEntry ->
            val profile: Profile = backStackEntry.toRoute()
            SampleProfileScreen(
                userId = profile.userId,
                onSettingsNavigate = { navController.navigate(Settings) },
            )
        }
        composable<Settings> {
            SampleSettingsScreen(onNavigateHome = { navController.navigate(Home) })
        }
        navigation("a-1", "a") {
            composable("a-1") {}
            composable("a-2") {}
        }
    }
}
