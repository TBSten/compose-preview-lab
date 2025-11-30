import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import me.tbsten.compose.preview.lab.sample.ComposeMultiplatformWizardDefaultUI
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController =
    ComposeUIViewController {
        var isRotating by remember { mutableStateOf(false) }

        ComposeMultiplatformWizardDefaultUI(
            isRotating = isRotating,
            onIsRotatingChange = { isRotating = it },
        )
    }
