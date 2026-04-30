package org.example.project.platform

import android.content.Context
import org.example.project.android.AppContextHolder

private const val PREFS = "olympx_quiz_progress"

private fun prefs() = AppContextHolder.application
    ?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

actual fun loadQuizProgress(chapterId: String): List<Int> {
    val raw = prefs()?.getString("progress_$chapterId", null) ?: return emptyList()
    if (raw.isBlank()) return emptyList()
    return raw.split(',').mapNotNull { it.toIntOrNull() }
}

actual fun saveQuizProgress(chapterId: String, answers: List<Int>) {
    prefs()?.edit()
        ?.putString("progress_$chapterId", answers.joinToString(","))
        ?.apply()
}

actual fun clearQuizProgress(chapterId: String) {
    prefs()?.edit()?.remove("progress_$chapterId")?.apply()
}

actual fun loadLastChapterId(): String? = prefs()?.getString("last_chapter", null)

actual fun saveLastChapterId(id: String?) {
    val editor = prefs()?.edit() ?: return
    if (id == null) editor.remove("last_chapter") else editor.putString("last_chapter", id)
    editor.apply()
}
