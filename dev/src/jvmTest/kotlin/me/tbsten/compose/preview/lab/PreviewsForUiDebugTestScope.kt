package me.tbsten.compose.preview.lab

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
import me.tbsten.compose.preview.lab.field.MutablePreviewLabField

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
            override val lifecycle: Lifecycle = LifecycleRegistry(this)
        }
    }
}

@OptIn(InternalComposePreviewLabApi::class)
inline fun <reified Value> PreviewLabState.field(label: String): MutablePreviewLabField<Value>? =
    scope.fields.find { it.label == label } as? MutablePreviewLabField<Value>?

@OptIn(InternalComposePreviewLabApi::class)
inline fun <reified Value> PreviewLabState.requireField(label: String): MutablePreviewLabField<Value> =
    field<Value>(label = label)
        ?: error("Can not find update target field: label=$label")
