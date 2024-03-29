import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.gadostudio.common.App


fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "S3Explorer") {
        App()
    }
}
