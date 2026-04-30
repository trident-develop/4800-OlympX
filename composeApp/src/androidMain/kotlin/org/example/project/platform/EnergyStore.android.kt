package org.example.project.platform

import android.content.Context
import org.example.project.android.AppContextHolder

private const val PREFS_NAME = "olympx_play"
private const val KEY_ENERGY = "energy_blob_v1"

actual fun readEnergyBlob(): String? {
    val app = AppContextHolder.application ?: return null
    return app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_ENERGY, null)
}

actual fun writeEnergyBlob(blob: String?) {
    val app = AppContextHolder.application ?: return
    app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .apply { if (blob == null) remove(KEY_ENERGY) else putString(KEY_ENERGY, blob) }
        .apply()
}
