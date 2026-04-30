package org.example.project

import androidx.compose.ui.window.ComposeUIViewController
import org.example.project.ios.ViewControllerHolder
import platform.UIKit.UIColor
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    val controller = ComposeUIViewController { App() }
    // Match MythColors.BgAbyss (#030616) so the window never flashes white
    // between SwiftUI mounting the view and Compose drawing its first frame.
    controller.view.backgroundColor = UIColor(
        red = 0x03 / 255.0,
        green = 0x06 / 255.0,
        blue = 0x16 / 255.0,
        alpha = 1.0,
    )
    ViewControllerHolder.rootController = controller
    return controller
}
