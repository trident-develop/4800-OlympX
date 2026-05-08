package org.example.project.db

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.gameDataStore: DataStore<Preferences> by preferencesDataStore("olympx_store")