package org.example.project.db

interface GameRepo {
    suspend fun getSavedScore(): String?
    suspend fun saveScore(score: String)
    suspend fun isNotifyShown(): Boolean
    suspend fun markNotifyShown()
}