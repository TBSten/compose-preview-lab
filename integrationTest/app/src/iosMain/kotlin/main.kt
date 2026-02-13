import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import me.tbsten.compose.preview.lab.extension.debugger.ui.Dialog
import me.tbsten.compose.preview.lab.sample.ComposeMultiplatformWizardDefaultUI
import me.tbsten.compose.preview.lab.sample.debugmenu.AppDebugMenu
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController =
    ComposeUIViewController {
        var isRotating by remember { mutableStateOf(false) }

        ComposeMultiplatformWizardDefaultUI(
            isRotating = isRotating,
            onIsRotatingChange = { isRotating = it },
        )

        // デバッグメニューダイアログ (シェイクで表示)
        AppDebugMenu.Dialog()
    }
