import androidx.compose.ui.window.ComposeUIViewController
import me.tbsten.compose.preview.lab.sample.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
