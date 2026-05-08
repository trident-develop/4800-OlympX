package org.example.project.db

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

class GameRepoImpl(
    private val dataStore: DataStore<Preferences>
) : GameRepo {

    private companion object {
        val SAVED_LINK = stringPreferencesKey("saved_link")
        val NOTIFY_SHOWN = booleanPreferencesKey("notify_shown")
    }

    override suspend fun getSavedScore(): String? {
        return dataStore.data.first()[SAVED_LINK]
    }

    override suspend fun saveScore(score: String) {
        dataStore.edit { prefs ->
            val current = prefs[SAVED_LINK]

            if (current != null) {
//                Log.d("MYTAG", "LINK ALREADY EXISTS -> skip")
                return@edit
            }

            prefs[SAVED_LINK] = score
//            Log.d("MYTAG", "SAVE LINK -> $score")
        }
    }

    override suspend fun isNotifyShown(): Boolean {
        return dataStore.data.first()[NOTIFY_SHOWN] ?: false
    }

    override suspend fun markNotifyShown() {
        dataStore.edit { prefs ->
            prefs[NOTIFY_SHOWN] = true
        }
    }
}