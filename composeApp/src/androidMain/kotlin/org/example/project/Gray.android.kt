package org.example.project

import androidx.compose.runtime.Composable
import org.example.project.theme.MythTheme

@Composable
actual fun Gray(
    loading: @Composable (() -> Unit),
    noInternet: @Composable ((onRetry: () -> Unit) -> Unit),
    white: @Composable (() -> Unit)
) {
    MythTheme {
        AppGate(loading = loading, noInternet = noInternet, white = white)
    }
}
