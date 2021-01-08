package demo

import androidx.compose.desktop.Window
import androidx.compose.ui.unit.IntSize

fun main() {
    Window(
        title = "Demo",
        centered = true,
        undecorated = false,
        size = IntSize(400, 600),
    ) { appBody() }
}
