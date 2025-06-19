import androidx.compose.ui.window.ComposeUIViewController
import me.tbsten.compose.preview.lab.sample.ComposeMultiplatformWizardDefaultUI
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController =
    ComposeUIViewController { ComposeMultiplatformWizardDefaultUI() }
