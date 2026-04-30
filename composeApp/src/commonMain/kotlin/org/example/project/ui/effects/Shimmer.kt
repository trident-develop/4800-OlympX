package org.example.project.ui.effects

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import org.example.project.theme.MythColors

fun Modifier.mythShimmer(
    active: Boolean = true,
    widthRatio: Float = 0.35f,
    color: Color = MythColors.CyanBright,
): Modifier = composed {
    if (!active) return@composed this
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
        label = "shimmer-x"
    )
    this
        .onSizeChanged { size = it }
        .drawWithContent {
            drawContent()
            if (size.width == 0) return@drawWithContent
            val shimmerWidth = size.width * widthRatio
            val startX = x * size.width - shimmerWidth
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        color.copy(alpha = 0.35f),
                        color.copy(alpha = 0.75f),
                        color.copy(alpha = 0.35f),
                        Color.Transparent,
                    ),
                    start = Offset(startX, 0f),
                    end = Offset(startX + shimmerWidth, size.height.toFloat())
                ),
                size = Size(size.width.toFloat(), size.height.toFloat()),
                blendMode = BlendMode.Plus
            )
        }
}
