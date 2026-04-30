package org.example.project.ios

import platform.UIKit.UIViewController

object ViewControllerHolder {
    var rootController: UIViewController? = null

    fun topViewController(): UIViewController? {
        var current: UIViewController? = rootController ?: return null
        while (true) {
            val presented = current?.presentedViewController
            if (presented == null || presented.isBeingDismissed()) return current
            current = presented
        }
    }
}
