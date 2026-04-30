package org.example.project.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.theme.MythColors
import org.example.project.ui.effects.AuroraBackground
import org.example.project.ui.effects.ParticleField
import org.example.project.ui.effects.mythShimmer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun LoadingScreen() {
    // Progress counter: shoots to 60 % quickly, then crawls toward 99 %.
    // Capped at 99 so it never visually "completes" — the 2-second gate in
    // AppGate is what actually advances the app.
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        // Fast ramp to 60 %, then progressively slower segments so the
        // counter visibly drags as it approaches 99 %.
        progress.animateTo(0.60f, tween(durationMillis = 280, easing = LinearEasing))
        progress.animateTo(0.80f, tween(durationMillis = 650, easing = LinearEasing))
        progress.animateTo(0.92f, tween(durationMillis = 700, easing = LinearEasing))
        progress.animateTo(0.97f, tween(durationMillis = 900, easing = LinearEasing))
        progress.animateTo(0.99f, tween(durationMillis = 1400, easing = LinearEasing))
    }
    val percent = (progress.value * 100f).toInt().coerceIn(0, 99)

    AuroraBackground(modifier = Modifier.fillMaxSize()) {
        ParticleField(count = 48)

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                MythicSigil()
                Spacer(Modifier.height(32.dp))
                Text(
                    text = "AWAKENING THE PANTHEON",
                    color = MythColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 4.sp,
                    modifier = Modifier.mythShimmer()
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "summoning ancient tides...",
                    color = MythColors.TextSecondary.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "$percent%",
                    color = MythColors.CyanBright,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                )
                Spacer(Modifier.height(14.dp))
                LoadingDots()
            }
        }
    }
}

@Composable
private fun MythicSigil() {
    val t = rememberInfiniteTransition(label = "sigil")
    val rot by t.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(18_000, easing = LinearEasing), RepeatMode.Restart),
        label = "rot"
    )
    val counterRot by t.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(26_000, easing = LinearEasing), RepeatMode.Restart),
        label = "counter"
    )
    val pulse by t.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val glow by t.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = Modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = min(size.width, size.height) / 2f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MythColors.Cyan.copy(alpha = 0.6f * glow),
                        MythColors.Azure.copy(alpha = 0.25f * glow),
                        Color.Transparent,
                    ),
                    center = Offset(size.width / 2, size.height / 2),
                    radius = r * pulse * 1.1f
                ),
                radius = r * pulse
            )
        }

        Canvas(modifier = Modifier.fillMaxSize().rotate(rot)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = min(size.width, size.height) / 2f * 0.88f
            drawCircle(
                color = MythColors.Cyan.copy(alpha = 0.9f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.2f)
            )
            drawCircle(
                color = MythColors.CyanBright.copy(alpha = 0.6f),
                radius = radius * 0.96f,
                center = center,
                style = Stroke(
                    width = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 10f))
                )
            )
            val runes = 12
            for (i in 0 until runes) {
                val a = (i.toFloat() / runes) * 2 * PI.toFloat()
                val rx = center.x + cos(a) * radius
                val ry = center.y + sin(a) * radius
                drawCircle(
                    color = MythColors.CyanBright,
                    radius = 2.6f,
                    center = Offset(rx, ry)
                )
            }
        }

        Canvas(modifier = Modifier.fillMaxSize().rotate(counterRot)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = min(size.width, size.height) / 2f * 0.68f
            drawCircle(
                color = MythColors.Sky.copy(alpha = 0.8f),
                radius = radius,
                center = center,
                style = Stroke(
                    width = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f))
                )
            )
            for (i in 0 until 3) {
                val a1 = (i.toFloat() / 3f) * 2 * PI.toFloat()
                val a2 = ((i + 1).toFloat() / 3f) * 2 * PI.toFloat()
                drawLine(
                    color = MythColors.Gold.copy(alpha = 0.85f),
                    start = Offset(center.x + cos(a1) * radius, center.y + sin(a1) * radius),
                    end = Offset(center.x + cos(a2) * radius, center.y + sin(a2) * radius),
                    strokeWidth = 1.2f
                )
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val r = min(size.width, size.height) / 2f * 0.42f
            withTransform({ rotate(rot / 2f, pivot = center) }) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(MythColors.Gold.copy(alpha = 0.75f * glow), Color.Transparent),
                        center = center,
                        radius = r
                    ),
                    radius = r,
                    center = center
                )
                for (i in 0 until 4) {
                    val a = (i * 45f) * (PI.toFloat() / 180f)
                    val cosA = cos(a)
                    val sinA = sin(a)
                    val half = r * 0.7f
                    drawLine(
                        color = MythColors.GoldBright.copy(alpha = 0.9f * glow),
                        start = Offset(center.x - cosA * half, center.y - sinA * half),
                        end = Offset(center.x + cosA * half, center.y + sinA * half),
                        strokeWidth = 1.6f
                    )
                }
                drawCircle(
                    color = MythColors.CyanBright,
                    radius = r * 0.18f,
                    center = center
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, MythColors.CyanBright, Color.Transparent),
                        center = center,
                        radius = r * 0.4f
                    ),
                    radius = r * 0.4f,
                    center = center
                )
            }
        }
    }
}

@Composable
private fun LoadingDots() {
    val t = rememberInfiniteTransition(label = "dots")
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { i ->
            val phase by t.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(1600, delayMillis = i * 120, easing = LinearEasing),
                    RepeatMode.Reverse
                ),
                label = "dot-$i"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .width(6.dp)
                    .height(6.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = MythColors.CyanBright.copy(alpha = 0.35f + 0.65f * phase),
                        radius = 3f + 2f * phase
                    )
                }
            }
        }
    }
}
