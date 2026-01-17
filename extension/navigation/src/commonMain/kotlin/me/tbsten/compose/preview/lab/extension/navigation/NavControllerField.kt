package me.tbsten.compose.preview.lab.extension.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.ImmutablePreviewLabField
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabField
import me.tbsten.compose.preview.lab.field.PolymorphicField
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.BackStackList
import me.tbsten.compose.preview.lab.ui.components.Button
import me.tbsten.compose.preview.lab.ui.components.Checkbox
import me.tbsten.compose.preview.lab.ui.components.SegmentedCard
import me.tbsten.compose.preview.lab.ui.components.SegmentedCardSection
import me.tbsten.compose.preview.lab.ui.components.Text

/**
 * A PreviewLabField for inspecting and controlling a [NavHostController].
 *
 * This field provides:
 * - BackStack history display with animations
 * - Navigation controls (pop back, navigate to routes)
 * - Route selection with editable parameters via [PolymorphicField]
 *
 * @param label The label for this field
 * @param navController The NavHostController to inspect and control
 * @param routes List of route fields for navigation. Use [FixedField] for routes without
 *   parameters, and [combined] for routes with parameters.
 *
 * @see me.tbsten.compose.preview.lab.sample.extension.navigation.NavControllerFieldExample
 * For a complete usage example, see the integration test sample.
 */
@ExperimentalComposePreviewLabApi
class NavControllerField(
    label: String,
    private val navController: NavHostController,
    routes: List<PreviewLabField<out Any>> = emptyList(),
) : ImmutablePreviewLabField<NavHostController>(
    label = label,
    initialValue = navController,
) {
    private val routeField: PolymorphicField<Any>? = if (routes.isNotEmpty()) {
        @Suppress("UNCHECKED_CAST")
        PolymorphicField(
            label = "Route",
            initialValue = routes.first().value,
            fields = routes,
        )
    } else {
        null
    }

    override fun valueCode(): String = "rememberNavController()"
    override fun serializer() = null

    @OptIn(InternalComposePreviewLabApi::class)
    @Composable
    override fun Content() {
        val backStack by navController.currentBackStack.collectAsState()
        val canPop = backStack.size > 1

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SegmentedCard {
                SegmentedCardSection(isTop = true) {
                    Text(
                        text = "BackStack",
                        style = PreviewLabTheme.typography.label1,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    BackStackList(
                        backStack = backStack,
                        canPop = canPop,
                        onPopBack = { navController.popBackStack() },
                        displayItem = { entry ->
                            val routePattern =
                                entry.destination.route?.substringAfterLast('.') ?: "(unnamed)"
                            entry.savedStateHandle.keys().fold(routePattern) { route, key ->
                                val value = entry.savedStateHandle.get<Any>(key)?.toString() ?: key
                                route.replace("{$key}", value)
                            }
                        },
                        itemKey = { _, entry -> entry.id },
                        modifier = Modifier.height(120.dp),
                    )
                }

                if (routeField != null) {
                    SegmentedCardSection(isTop = false) {
                        Text(
                            text = "navigate()",
                            style = PreviewLabTheme.typography.label1,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )

                        routeField.Content()

                        Spacer(Modifier.height(8.dp))

                        NavOptionsSection(
                            onNavigate = { options ->
                                navController.navigate(routeField.value, options)
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(InternalComposePreviewLabApi::class)
@Composable
private fun NavOptionsSection(onNavigate: (androidx.navigation.NavOptions?) -> Unit) {
    var showOptions by remember { mutableStateOf(false) }
    var launchSingleTop by remember { mutableStateOf(false) }
    var restoreState by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (showOptions) 90f else 0f)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Button(
                onClick = {
                    val options = if (launchSingleTop || restoreState) {
                        navOptions {
                            this.launchSingleTop = launchSingleTop
                            this.restoreState = restoreState
                        }
                    } else {
                        null
                    }
                    onNavigate(options)
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("Navigate")
            }
            Button(
                onClick = { showOptions = !showOptions },
            ) {
                Text(
                    text = ">",
                    modifier = Modifier.graphicsLayer { rotationZ = rotation },
                )
            }
        }

        AnimatedVisibility(
            visible = showOptions,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Checkbox(
                        checked = launchSingleTop,
                        onCheckedChange = { launchSingleTop = it },
                    )
                    Text(
                        text = "launchSingleTop",
                        style = PreviewLabTheme.typography.body2,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Checkbox(
                        checked = restoreState,
                        onCheckedChange = { restoreState = it },
                    )
                    Text(
                        text = "restoreState",
                        style = PreviewLabTheme.typography.body2,
                    )
                }
            }
        }
    }
}
