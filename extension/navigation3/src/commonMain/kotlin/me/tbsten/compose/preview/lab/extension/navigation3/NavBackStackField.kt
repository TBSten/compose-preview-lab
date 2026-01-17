package me.tbsten.compose.preview.lab.extension.navigation3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.savedstate.compose.serialization.serializers.SnapshotStateListSerializer
import kotlinx.serialization.KSerializer
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.ImmutablePreviewLabField
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabField
import me.tbsten.compose.preview.lab.field.PolymorphicField
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.BackStackList
import me.tbsten.compose.preview.lab.ui.components.Button
import me.tbsten.compose.preview.lab.ui.components.SegmentedCard
import me.tbsten.compose.preview.lab.ui.components.SegmentedCardSection
import me.tbsten.compose.preview.lab.ui.components.Text

/**
 * A PreviewLabField for inspecting and controlling a Navigation 3 backstack.
 *
 * Navigation 3 uses a developer-managed [SnapshotStateList] for the backstack,
 * unlike Navigation 2's [NavHostController]. This field provides:
 * - BackStack history display with animations
 * - Navigation controls (pop back, navigate to routes)
 * - Route selection with editable parameters via [PolymorphicField]
 *
 * @param T The type of route objects in the backstack
 * @param label The label for this field
 * @param backStack The mutable state list representing the navigation backstack
 * @param routes List of route fields for navigation. Use [FixedField] for routes without
 *   parameters, and [combined] for routes with parameters.
 * @param displayRoute A function to convert a route object to a display string.
 *   Defaults to calling [toString] on the route.
 *
 * Example usage:
 * ```kotlin
 * val backStack = remember { mutableStateListOf<Route>(Home) }
 *
 * PreviewLab {
 *     val bs = fieldValue("backStack") {
 *         NavBackStackField(
 *             label = "backStack",
 *             backStack = backStack,
 *             routes = listOf(
 *                 FixedField("Home", Home),
 *                 FixedField("Settings", Settings),
 *                 combined("Profile", StringField("userId", "default")) { Profile(it) },
 *             ),
 *         )
 *     }
 *
 *     NavDisplay(backStack = bs) { route ->
 *         when (route) {
 *             is Home -> HomeScreen()
 *             is Profile -> ProfileScreen(route.userId)
 *             is Settings -> SettingsScreen()
 *         }
 *     }
 * }
 * ```
 */
@ExperimentalComposePreviewLabApi
class NavBackStackField<T : Any>(
    label: String,
    private val backStack: SnapshotStateList<T>,
    routes: List<PreviewLabField<out T>>,
    private val displayRoute: (T) -> String = { it.toString().substringAfterLast('.') },
    private val elementSerializer: KSerializer<in T>? = null,
) : ImmutablePreviewLabField<SnapshotStateList<T>>(
    label = label,
    initialValue = backStack,
) {
    private val routeField: PolymorphicField<T>? = if (routes.isNotEmpty()) {
        @Suppress("UNCHECKED_CAST")
        PolymorphicField(
            label = "Route",
            initialValue = routes.first().value,
            fields = routes as List<PreviewLabField<T>>,
        )
    } else {
        null
    }

    override fun valueCode(): String = "mutableStateListOf(/* routes */)"

    @Suppress("UNCHECKED_CAST")
    override fun serializer(): KSerializer<SnapshotStateList<T>>? = runCatching {
        elementSerializer?.let {
            SnapshotStateListSerializer(elementSerializer = it as KSerializer<T>)
        }
    }.getOrNull()

    @OptIn(InternalComposePreviewLabApi::class)
    @Composable
    override fun Content() {
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
                        backStack = backStack.toList(),
                        canPop = canPop,
                        onPopBack = { backStack.removeLast() },
                        displayItem = displayRoute,
                        itemKey = { index, _ -> index },
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

                        Button(
                            onClick = { backStack.add(routeField.value) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Navigate")
                        }
                    }
                }
            }
        }
    }
}
