package org.example.project.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openInBrowser(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    val app = UIApplication.sharedApplication
    if (app.canOpenURL(nsUrl)) {
        app.openURL(nsUrl, options = emptyMap<Any?, Any>(), completionHandler = null)
    }
}
