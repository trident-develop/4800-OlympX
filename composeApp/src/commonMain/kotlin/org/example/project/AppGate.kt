package org.example.project

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

internal enum class GateState { Loading, NoInternet, Ready }

@Composable
internal fun AppGate(
    loading: @Composable () -> Unit,
    noInternet: @Composable (onRetry: () -> Unit) -> Unit,
    white: @Composable () -> Unit,
) {
    var state by remember { mutableStateOf(GateState.Loading) }
    val attempt = remember { mutableStateOf(0) }

    LaunchedEffect(state, attempt.value) {
        if (state == GateState.Loading) {
            delay(2_000)
            state = GateState.Ready
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                (fadeIn(tween(520)) togetherWith fadeOut(tween(360)))
            },
            label = "gate"
        ) { gs ->
            when (gs) {
                GateState.Loading -> loading()
                GateState.NoInternet -> noInternet { attempt.value++; state = GateState.Loading }
                GateState.Ready -> white()
            }
        }
    }
}
