package org.example.project.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.example.project.platform.readThemeModeBlob
import org.example.project.platform.writeThemeModeBlob

object ThemeState {
    var mode: ThemeMode by mutableStateOf(loadInitial())
        private set

    private fun loadInitial(): ThemeMode {
        val raw = readThemeModeBlob()
        val m = when (raw) {
            ThemeMode.Dark.name -> ThemeMode.Dark
            ThemeMode.Light.name -> ThemeMode.Light
            else -> ThemeMode.Dark
        }
        MythColors.applyTheme(m)
        return m
    }

    fun select(newMode: ThemeMode) {
        if (mode == newMode) return
        mode = newMode
        MythColors.applyTheme(newMode)
        writeThemeModeBlob(newMode.name)
    }
}
