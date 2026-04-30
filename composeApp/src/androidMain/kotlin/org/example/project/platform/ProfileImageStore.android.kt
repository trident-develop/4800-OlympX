package org.example.project.platform

import android.content.Context
import org.example.project.android.AppContextHolder

private const val PREFS_NAME = "olympx_profile"
private const val KEY_IMAGE = "image_blob_v1"

actual fun readProfileImageBlob(): String? {
    val app = AppContextHolder.application ?: return null
    return app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_IMAGE, null)
}

actual fun writeProfileImageBlob(blob: String?) {
    val app = AppContextHolder.application ?: return
    val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().apply {
        if (blob == null) remove(KEY_IMAGE) else putString(KEY_IMAGE, blob)
    }.apply()
}
