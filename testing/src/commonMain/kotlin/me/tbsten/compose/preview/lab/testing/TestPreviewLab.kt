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
import me.tbsten.compose.preview.lab.LocalPreviewLabState
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.field.MutablePreviewLabField

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
 *     val intField = state.field<Int>("intValue")
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
        LocalPreviewLabState provides state,
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

/**
 * Finds a mutable field by its label in the PreviewLabState.
 *
 * @param Value The type of the field's value
 * @param label The label of the field to find
 * @return The field if found and matches the type, null otherwise
 *
 * Example:
 * ```kotlin
 * val intField = state.fieldOrNull<Int>("intValue")
 * if (intField != null) {
 *     intField.value = 42
 * }
 * ```
 */
@ExperimentalComposePreviewLabApi
inline fun <reified Value> PreviewLabState.fieldOrNull(label: String): MutablePreviewLabField<Value>? =
    scope.fields.find { it.label == label } as? MutablePreviewLabField<Value>

/**
 * Finds a mutable field by its label in the PreviewLabState, throwing an error if not found.
 *
 * @param Value The type of the field's value
 * @param label The label of the field to find
 * @return The field if found and matches the type
 * @throws IllegalStateException if the field is not found
 *
 * Example:
 * ```kotlin
 * val intField = state.field<Int>("intValue")
 * intField.value = 42
 * ```
 */
@ExperimentalComposePreviewLabApi
inline fun <reified Value> PreviewLabState.field(label: String): MutablePreviewLabField<Value> =
    fieldOrNull<Value>(label = label)
        ?: error("Can not find update target field: label=$label")
