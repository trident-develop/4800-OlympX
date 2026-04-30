package org.example.project.platform

import android.content.Context
import org.example.project.android.AppContextHolder

private const val PREFS_NAME = "olympx_profile"
private const val KEY_NAME = "name_v1"

actual fun readProfileName(): String? {
    val app = AppContextHolder.application ?: return null
    return app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_NAME, null)
}

actual fun writeProfileName(name: String?) {
    val app = AppContextHolder.application ?: return
    app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .apply { if (name == null) remove(KEY_NAME) else putString(KEY_NAME, name) }
        .apply()
}
