package org.example.project.platform

import android.content.Context
import org.example.project.android.AppContextHolder

private const val PREFS_NAME = "olympx_world_progress"
private const val KEY_VISITED = "visited_place_ids"

actual fun loadVisitedPlaces(): Set<String> {
    val app = AppContextHolder.application ?: return emptySet()
    return app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getStringSet(KEY_VISITED, emptySet())
        ?.toSet()
        ?: emptySet()
}

actual fun saveVisitedPlaces(ids: Set<String>) {
    val app = AppContextHolder.application ?: return
    app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putStringSet(KEY_VISITED, ids)
        .apply()
}
