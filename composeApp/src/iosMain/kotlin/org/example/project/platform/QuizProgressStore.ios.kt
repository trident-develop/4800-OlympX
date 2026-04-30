package org.example.project.platform

import platform.Foundation.NSUserDefaults

private fun defaults() = NSUserDefaults.standardUserDefaults

actual fun loadQuizProgress(chapterId: String): List<Int> {
    val raw = defaults().stringForKey("progress_$chapterId") ?: return emptyList()
    if (raw.isBlank()) return emptyList()
    return raw.split(',').mapNotNull { it.toIntOrNull() }
}

actual fun saveQuizProgress(chapterId: String, answers: List<Int>) {
    defaults().setObject(answers.joinToString(","), "progress_$chapterId")
}

actual fun clearQuizProgress(chapterId: String) {
    defaults().removeObjectForKey("progress_$chapterId")
}

actual fun loadLastChapterId(): String? = defaults().stringForKey("last_chapter")

actual fun saveLastChapterId(id: String?) {
    if (id == null) defaults().removeObjectForKey("last_chapter")
    else defaults().setObject(id, "last_chapter")
}
