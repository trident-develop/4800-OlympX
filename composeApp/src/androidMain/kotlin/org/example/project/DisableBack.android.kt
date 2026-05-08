package org.example.project

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun DisableBack() {
    BackHandler(enabled = true) {

    }
}

@SuppressLint("ContextCastToActivity")
@Composable
actual fun QuitGame() {
    val activity = LocalContext.current as? Activity

    BackHandler(enabled = true) {
        activity?.finish()
    }
}