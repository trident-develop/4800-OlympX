package org.example.project.platform

import android.content.Intent
import android.net.Uri
import org.example.project.android.AppContextHolder

actual fun openInBrowser(url: String) {
    val ctx = AppContextHolder.application ?: return
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { ctx.startActivity(intent) }
}
