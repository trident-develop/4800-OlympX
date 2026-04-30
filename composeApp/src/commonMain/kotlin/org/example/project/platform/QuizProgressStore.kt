package org.example.project.platform

expect fun loadQuizProgress(chapterId: String): List<Int>
expect fun saveQuizProgress(chapterId: String, answers: List<Int>)
expect fun clearQuizProgress(chapterId: String)

expect fun loadLastChapterId(): String?
expect fun saveLastChapterId(id: String?)
