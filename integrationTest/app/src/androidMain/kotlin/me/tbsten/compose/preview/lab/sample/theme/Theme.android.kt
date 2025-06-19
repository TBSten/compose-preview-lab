package me.tbsten.compose.preview.lab.sample.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

@Composable
internal actual fun SystemAppearance(isDark: Boolean) {
    val view = LocalView.current
    LaunchedEffect(isDark) {
        val window = view.context.getActivity().window
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = isDark
            isAppearanceLightNavigationBars = isDark
        }
    }
}

private fun Context.getActivity(): Activity = when (this) {
    is Activity -> this
    is ContextWrapper -> this.baseContext.getActivity()
    else -> error("can not get Activity from Context: $this")
}
