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
import me.tbsten.compose.preview.lab.LocalIsInPreviewLabGalleryCardBody
import me.tbsten.compose.preview.lab.previewlab.LocalEnforcePreviewLabState
import me.tbsten.compose.preview.lab.previewlab.PreviewLabState

/**
 * Test helper composable that provides the necessary environment for testing PreviewLab components.
 *
 * This function sets up all required CompositionLocals (ViewModelStoreOwner, LifecycleOwner, and PreviewLabState)
 * needed for PreviewLab components to work correctly in tests.
 *
 * Note: This sets LocalIsInPreviewLabGalleryCardBody to true to skip PreviewLab's full UI
 * (including Toaster) and avoid coroutine issues in tests.
 */
@Composable
internal fun TestPreviewLab(
    state: PreviewLabState,
    viewModelStoreOwner: ViewModelStoreOwner = defaultTestViewModelStoreOwner(),
    lifecycleOwner: LifecycleOwner = defaultTestLifecycleOwner(),
    block: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalViewModelStoreOwner provides viewModelStoreOwner,
        LocalLifecycleOwner provides lifecycleOwner,
        LocalEnforcePreviewLabState provides state,
        // Skip PreviewLab's full UI (including Toaster) to avoid coroutine issues in tests
        LocalIsInPreviewLabGalleryCardBody provides true,
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
