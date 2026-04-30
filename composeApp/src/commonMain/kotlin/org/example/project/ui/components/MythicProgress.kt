package org.example.project.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.example.project.theme.MythColors

@Composable
fun MythicProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    track: Color = MythColors.Surface.copy(alpha = 0.5f),
    brush: Brush = MythColors.AccentGradient,
) {
    val p by animateFloatAsState(progress.coerceIn(0f, 1f), animationSpec = tween(600))
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(track)
    ) {
        Canvas(Modifier.fillMaxWidth().height(height)) {
            val w = size.width * p
            if (w <= 0f) return@Canvas
            drawRoundRect(
                brush = brush,
                topLeft = Offset(0f, 0f),
                size = Size(w, size.height),
                cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
            )
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, Color.White.copy(alpha = 0.35f), Color.Transparent),
                    startX = 0f,
                    endX = w
                ),
                topLeft = Offset(0f, 0f),
                size = Size(w, size.height / 2f),
                cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
            )
        }
    }
}
