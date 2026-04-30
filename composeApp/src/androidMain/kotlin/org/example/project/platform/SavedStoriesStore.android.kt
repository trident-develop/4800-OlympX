package org.example.project.platform

import android.content.Context
import org.example.project.android.AppContextHolder

private const val PREFS_NAME = "olympx_saved_stories"
private const val KEY_STORIES = "stories_blob_v1"

actual fun loadSavedStories(): List<SavedStory> {
    val app = AppContextHolder.application ?: return emptyList()
    val raw = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_STORIES, null)
    return decodeStories(raw)
}

actual fun saveSavedStories(stories: List<SavedStory>) {
    val app = AppContextHolder.application ?: return
    app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_STORIES, encodeStories(stories))
        .apply()
}
