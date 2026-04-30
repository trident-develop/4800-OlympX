package org.example.project.platform

import android.content.Context
import org.example.project.android.AppContextHolder

private const val PREFS_NAME = "olympx_theme"
private const val KEY_MODE = "theme_mode_v1"

actual fun readThemeModeBlob(): String? {
    val app = AppContextHolder.application ?: return null
    return app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_MODE, null)
}

actual fun writeThemeModeBlob(blob: String?) {
    val app = AppContextHolder.application ?: return
    app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .apply { if (blob == null) remove(KEY_MODE) else putString(KEY_MODE, blob) }
        .apply()
}
