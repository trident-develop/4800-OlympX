package org.example.project.platform

import android.content.Context
import org.example.project.android.AppContextHolder

private const val PREFS_NAME = "olympx_play"
private const val KEY_STATE = "game_state_blob_v1"

actual fun readGameStateBlob(): String? {
    val app = AppContextHolder.application ?: return null
    return app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_STATE, null)
}

actual fun writeGameStateBlob(blob: String?) {
    val app = AppContextHolder.application ?: return
    app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .apply { if (blob == null) remove(KEY_STATE) else putString(KEY_STATE, blob) }
        .apply()
}
