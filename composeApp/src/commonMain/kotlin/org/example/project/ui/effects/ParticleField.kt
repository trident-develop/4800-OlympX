package org.example.project.ui.effects

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import org.example.project.theme.MythColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val seed: Long,
    val x0: Float,
    val y0: Float,
    val radius: Float,
    val speed: Float,
    val amp: Float,
    val phase: Float,
    val alpha: Float,
    val tint: Color,
)

@Composable
fun ParticleField(
    modifier: Modifier = Modifier,
    count: Int = 36,
    seed: Long = 42L,
) {
    val particles = remember(count, seed) {
        val rng = Random(seed)
        List(count) {
            Particle(
                seed = rng.nextLong(),
                x0 = rng.nextFloat(),
                y0 = rng.nextFloat(),
                radius = 0.6f + rng.nextFloat() * 2.2f,
                speed = 0.3f + rng.nextFloat() * 1.1f,
                amp = 0.02f + rng.nextFloat() * 0.08f,
                phase = rng.nextFloat() * (PI * 2).toFloat(),
                alpha = 0.25f + rng.nextFloat() * 0.55f,
                tint = when (rng.nextInt(3)) {
                    0 -> MythColors.Cyan
                    1 -> MythColors.Sky
                    else -> MythColors.Electric
                }
            )
        }
    }

    val t = rememberInfiniteTransition(label = "particles")
    val time by t.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(18_000, easing = LinearEasing)
        ),
        label = "particles-t"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val tau = (PI * 2).toFloat()
        particles.forEach { p ->
            val theta = (time * p.speed) * tau + p.phase
            val dx = cos(theta) * p.amp
            val dy = sin(theta * 0.75f + p.phase) * p.amp
            val x = ((p.x0 + dx + 1f) % 1f) * w
            val y = ((p.y0 + dy + 1f) % 1f) * h
            val pulse = 0.6f + 0.4f * sin(theta * 1.7f)
            drawCircle(
                color = p.tint.copy(alpha = p.alpha * pulse * 0.6f),
                radius = p.radius * 3.5f,
                center = Offset(x, y)
            )
            drawCircle(
                color = p.tint.copy(alpha = p.alpha * pulse),
                radius = p.radius,
                center = Offset(x, y)
            )
        }
    }
}
