package me.tbsten.compose.preview.lab.sample.extension.navigation3

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.previewlab.PreviewLab

interface Nav3Route : NavKey

@Serializable
data object Nav3Home : Nav3Route

@Serializable
data class Nav3Profile(val userId: String) : Nav3Route

@Serializable
data object Nav3Settings : Nav3Route

@OptIn(ExperimentalComposePreviewLabApi::class)
@ComposePreviewLabOption(id = "NavBackStackFieldExample")
@Preview
@Composable
private fun NavBackStackFieldExample() = PreviewLab {
//    val backStackField: SnapshotStateList<Nav3Route> = field<Unit> {
//        TODO()
//    }
//
//    NavDisplay<Nav3Route>(
//        sceneStrategy = TestSt(),
//        // region TODO
//        backStack = backStackField,
//        entryProvider = entryProvider {
//            entry<Nav3Home>(
//                metadata = backStackField.meta { FixedField<Nav3Home>("Home", Nav3Home) },
//            ) {
//                SampleHomeScreen(
//                    onNavigateProfile = { backStack.add(Nav3Profile(userId = "default-id-1")) },
//                )
//            }
//
//            entry<Nav3Profile>(
//                metadata = backStackField.meta {
//                    combined(
//                        label = "Profile",
//                        field1 = StringField("userId", ""),
//                        combine = { userId -> Nav3Profile(userId) },
//                        split = { splitedOf(it.userId) },
//                    )
//                },
//            ) {
//                SampleProfileScreen(
//                    userId = it.userId,
//                    onSettingsNavigate = { backStack.add(Nav3Settings) },
//                )
//            }
//
//            entry<Nav3Settings>(
//                metadata = backStackField.meta {
//                    combined(
//                        label = "Profile",
//                        field1 = StringField("userId", ""),
//                        combine = { userId -> Nav3Profile(userId) },
//                        split = { splitedOf(it.userId) },
//                    )
//                },
//            ) {
//                SampleSettingsScreen(
//                    onNavigateHome = { backStack.add(Nav3Settings) },
//                )
//            }
//        },
//        // endregion TODO
//    )
}
//
// class TestSt<T : Any> : SceneStrategy<T> {
//    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
//        TODO("Not yet implemented")
//    }
//
//    override fun then(sceneStrategy: SceneStrategy<T>): SceneStrategy<T> {
//        LaunchedEffect()
//    }
//
//    companion object {
//        fun list(): Map<String, Any?> = mapOf<String, Any?>("${TestSt::class.qualifiedName}" to ::list.name)
//        fun detail(): Map<String, Any?> = mapOf<String, Any?>("${TestSt::class.qualifiedName}" to ::detail.name)
//    }
// }
