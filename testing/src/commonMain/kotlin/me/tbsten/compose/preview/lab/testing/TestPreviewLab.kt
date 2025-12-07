package me.tbsten.compose.preview.lab.testing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.LocalEnforcePreviewLabState
import me.tbsten.compose.preview.lab.PreviewLabState

/**
 * Test helper composable that provides the necessary environment for testing PreviewLab components.
 *
 * This function sets up all required CompositionLocals (ViewModelStoreOwner, LifecycleOwner, and PreviewLabState)
 * needed for PreviewLab components to work correctly in tests.
 *
 * Example usage:
 * ```kotlin
 * @Test
 * fun `test preview lab functionality`() = runDesktopComposeUiTest {
 *     val state = PreviewLabState()
 *     setContent {
 *         TestPreviewLab(state) {
 *             SomePreview()
 *         }
 *     }
 *
 *     val intField by state.field<Int>("intValue")
 *     intField.value = 42
 *     awaitIdle()
 *
 *     onNodeWithText("intValue: 42").assertIsDisplayed()
 * }
 * ```
 *
 * @param state The PreviewLabState instance to provide to the content
 * @param viewModelStoreOwner The ViewModelStoreOwner to provide. Defaults to a test instance if not in a Compose context
 * @param lifecycleOwner The LifecycleOwner to provide. Defaults to a test instance if not in a Compose context
 * @param block The composable content to test
 */
@ExperimentalComposePreviewLabApi
@Suppress("VisibleForTests")
@Composable
fun TestPreviewLab(
    state: PreviewLabState,
    viewModelStoreOwner: ViewModelStoreOwner = defaultTestViewModelStoreOwner(),
    lifecycleOwner: LifecycleOwner = defaultTestLifecycleOwner(),
    block: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalViewModelStoreOwner provides viewModelStoreOwner,
        LocalLifecycleOwner provides lifecycleOwner,
        LocalEnforcePreviewLabState provides state,
    ) {
        block()
    }
}

@Composable
private fun defaultTestViewModelStoreOwner() = runCatching {
    LocalViewModelStoreOwner.current
}.getOrNull() ?: remember {
    object : ViewModelStoreOwner {
        override val viewModelStore: ViewModelStore = ViewModelStore()
    }
}

@Composable
private fun defaultTestLifecycleOwner() = runCatching {
    LocalLifecycleOwner.current
}.getOrElse {
    remember {
        object : LifecycleOwner {
            override val lifecycle: Lifecycle =
                LifecycleRegistry(this).apply {
                    handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                }
        }
    }
}
