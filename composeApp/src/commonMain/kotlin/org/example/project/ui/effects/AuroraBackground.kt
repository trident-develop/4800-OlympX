package org.example.project.ui.effects

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import org.example.project.theme.MythColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    content: @Composable () -> Unit = {},
) {
    val t = rememberInfiniteTransition(label = "aurora")
    val phase by t.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(22_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aurora-phase"
    )
    val drift by t.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(38_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aurora-drift"
    )

    Box(modifier = modifier.background(MythColors.BgAbyss)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val a1 = phase * 6.2831853f
            val a2 = (phase + 0.33f) * 6.2831853f
            val a3 = (phase + 0.66f) * 6.2831853f
            val d = drift * 6.2831853f

            val o1 = Offset(w * (0.2f + 0.15f * cos(a1)), h * (0.18f + 0.08f * sin(a1 + d)))
            val o2 = Offset(w * (0.8f + 0.12f * cos(a2)), h * (0.35f + 0.1f * sin(a2)))
            val o3 = Offset(w * (0.5f + 0.2f * cos(a3 + d)), h * (0.85f + 0.06f * sin(a3)))

            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(MythColors.SurfaceGlow.copy(alpha = 0.8f * intensity), Color.Transparent),
                    center = o1,
                    radius = w * 0.7f
                ),
                size = size
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(MythColors.Azure.copy(alpha = 0.35f * intensity), Color.Transparent),
                    center = o2,
                    radius = w * 0.65f
                ),
                size = size
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(MythColors.Indigo.copy(alpha = 0.45f * intensity), Color.Transparent),
                    center = o3,
                    radius = w * 0.85f
                ),
                size = size
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, MythColors.BgAbyss.copy(alpha = 0.75f))
                ),
                size = size
            )
        }
        content()
    }
}
