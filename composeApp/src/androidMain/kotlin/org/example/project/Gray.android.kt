package org.example.project

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.example.project.android.isFlowersConnected
import org.example.project.event.StartSideEffect
import org.example.project.theme.MythTheme
import org.example.project.viewmodel.StartViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@SuppressLint("ContextCastToActivity")
@Composable
actual fun Gray(
    loading: @Composable (() -> Unit),
    noInternet: @Composable ((onRetry: () -> Unit) -> Unit),
    white: @Composable (() -> Unit)
) {
    var showContent by remember { mutableStateOf(false) }
    val context = LocalContext.current as MainActivity
    var retryKey by remember { mutableIntStateOf(0) }
    val isConnected = remember(retryKey) { context.isFlowersConnected() }

    Crossfade(
        targetState = showContent,
        animationSpec = tween(600),
        label = "gate"
    ) { gs ->
        if (gs) {
            white()
        } else {
            if (isConnected) {
                loading()

                val viewModel: StartViewModel = koinViewModel()
                val state by viewModel.collectAsState()

                LaunchedEffect(Unit) {
                    viewModel.start()
                }

                LaunchedEffect(context.TV3) {
                    viewModel.observeTVEvents(context.TV3)
                }

                viewModel.collectSideEffect { sideEffect ->
                    when (sideEffect) {
                        is StartSideEffect.OpenBuiltScore -> {
                            context.TV3.loadUrl(sideEffect.score)
                        }

                        is StartSideEffect.OpenTypeA -> {
                            context.TV3.loadUrl(sideEffect.score)
                        }

                        is StartSideEffect.OpenTypeB -> {
                            showContent = true
                        }

                        StartSideEffect.OpenGame -> {
                            showContent = true
                        }
                    }
                }
                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize())
                }

            } else {
                noInternet {
                    retryKey++
                }
            }
        }
    }
}
