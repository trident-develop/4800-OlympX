package org.example.project.platform

import platform.Foundation.NSUserDefaults

private const val KEY_STORIES = "olympx_saved_stories_blob_v1"

actual fun loadSavedStories(): List<SavedStory> {
    val raw = NSUserDefaults.standardUserDefaults.stringForKey(KEY_STORIES)
    return decodeStories(raw)
}

actual fun saveSavedStories(stories: List<SavedStory>) {
    NSUserDefaults.standardUserDefaults.setObject(encodeStories(stories), KEY_STORIES)
}
