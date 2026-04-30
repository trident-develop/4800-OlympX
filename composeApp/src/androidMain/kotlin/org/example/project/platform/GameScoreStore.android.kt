package org.example.project.platform

import android.content.Context
import org.example.project.android.AppContextHolder

private const val PREFS_NAME = "olympx_play"
private const val KEY_GLORY = "glory_score_v1"

actual fun loadGloryScore(): Int {
    val app = AppContextHolder.application ?: return 0
    return app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_GLORY, 0)
}

actual fun saveGloryScore(score: Int) {
    val app = AppContextHolder.application ?: return
    app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putInt(KEY_GLORY, score)
        .apply()
}
