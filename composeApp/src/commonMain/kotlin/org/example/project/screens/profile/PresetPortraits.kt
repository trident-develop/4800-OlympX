package org.example.project.screens.profile

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import org.example.project.theme.MythColors

const val PROFILE_PRESET_COUNT = 4

private const val PRESET_PREFIX = "PRESET:"

fun presetBytes(index: Int): ByteArray = "$PRESET_PREFIX$index".encodeToByteArray()

fun ByteArray.presetIndexOrNull(): Int? {
    if (size > 16) return null
    val text = runCatching { decodeToString() }.getOrNull() ?: return null
    if (!text.startsWith(PRESET_PREFIX)) return null
    val idx = text.removePrefix(PRESET_PREFIX).toIntOrNull() ?: return null
    return if (idx in 0 until PROFILE_PRESET_COUNT) idx else null
}

data class PresetPortraitInfo(val title: String, val subtitle: String)

val presetPortraitInfo: List<PresetPortraitInfo> = listOf(
    PresetPortraitInfo("Storm", "Bearer of the bolt"),
    PresetPortraitInfo("Solar", "Crowned in dawnfire"),
    PresetPortraitInfo("Lunar", "Keeper of the tide"),
    PresetPortraitInfo("Ember", "Forged in the deep"),
)

@Composable
fun PresetPortrait(index: Int, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "preset-$index")
    val pulse by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            val (primary, secondary, accent) = presetColors(index)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(primary.copy(alpha = 0.45f * pulse), Color.Transparent)
                ),
                radius = size.minDimension / 2f,
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(secondary.copy(alpha = 0.85f), MythColors.BgDeep)
                ),
                radius = size.minDimension * 0.36f,
            )
            drawCircle(
                color = primary.copy(alpha = 0.65f * pulse),
                radius = size.minDimension * 0.46f,
                style = Stroke(width = size.minDimension * 0.012f),
            )
            when (index) {
                0 -> drawStormGlyph(primary, accent)
                1 -> drawSolarGlyph(primary, accent)
                2 -> drawLunarGlyph(primary, accent, secondary)
                else -> drawEmberGlyph(primary, accent)
            }
        }
    }
}

private fun presetColors(index: Int): Triple<Color, Color, Color> = when (index) {
    0 -> Triple(MythColors.CyanBright, MythColors.Azure, MythColors.Gold)
    1 -> Triple(MythColors.Gold, MythColors.GoldBright, MythColors.Crimson)
    2 -> Triple(MythColors.Sky, MythColors.Cyan, MythColors.GoldBright)
    else -> Triple(MythColors.Crimson, MythColors.Gold, MythColors.GoldBright)
}

private fun DrawScope.drawStormGlyph(primary: Color, accent: Color) {
    val w = size.width
    val cx = w / 2f
    val cy = size.height / 2f
    val unit = w * 0.04f
    val bolt = Path().apply {
        moveTo(cx - unit * 1.2f, cy - unit * 4f)
        lineTo(cx + unit * 1.2f, cy - unit * 0.6f)
        lineTo(cx - unit * 0.2f, cy - unit * 0.4f)
        lineTo(cx + unit * 1.6f, cy + unit * 4f)
        lineTo(cx - unit * 1.4f, cy + unit * 0.4f)
        lineTo(cx + unit * 0.4f, cy + unit * 0.2f)
        close()
    }
    drawPath(bolt, color = accent.copy(alpha = 0.95f))
    drawPath(bolt, color = primary, style = Stroke(width = w * 0.008f))
    listOf(-1, 0, 1).forEach { i ->
        val angle = -90f + i * 55f
        val rad = (angle * kotlin.math.PI / 180f).toFloat()
        val r = w * 0.36f
        val x = cx + r * cos(rad)
        val y = cy + r * sin(rad)
        drawCircle(color = primary.copy(alpha = 0.7f), radius = unit * 0.55f, center = Offset(x, y))
    }
}

private fun DrawScope.drawSolarGlyph(primary: Color, accent: Color) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val rays = 12
    val inner = size.minDimension * 0.18f
    val outer = size.minDimension * 0.40f
    repeat(rays) { i ->
        val angle = i * (360f / rays)
        val rad = (angle * kotlin.math.PI / 180f).toFloat()
        val sx = cx + inner * cos(rad)
        val sy = cy + inner * sin(rad)
        val ex = cx + outer * cos(rad)
        val ey = cy + outer * sin(rad)
        drawLine(
            color = primary.copy(alpha = if (i % 2 == 0) 1f else 0.55f),
            start = Offset(sx, sy),
            end = Offset(ex, ey),
            strokeWidth = size.width * 0.014f,
        )
    }
    drawCircle(
        brush = Brush.radialGradient(listOf(accent, primary)),
        radius = inner,
    )
    drawCircle(
        color = MythColors.BgDeep.copy(alpha = 0.4f),
        radius = inner,
        style = Stroke(width = size.width * 0.01f),
    )
}

private fun DrawScope.drawLunarGlyph(primary: Color, accent: Color, secondary: Color) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = size.minDimension * 0.30f
    drawCircle(
        brush = Brush.radialGradient(listOf(primary, secondary)),
        radius = r,
        center = Offset(cx, cy),
    )
    drawCircle(
        color = MythColors.BgDeep,
        radius = r * 0.92f,
        center = Offset(cx + r * 0.42f, cy - r * 0.05f),
    )
    val stars = listOf(
        Offset(cx - r * 1.30f, cy - r * 0.85f),
        Offset(cx - r * 1.55f, cy + r * 0.20f),
        Offset(cx - r * 1.05f, cy + r * 1.10f),
    )
    stars.forEach { p ->
        drawCircle(color = accent, radius = size.width * 0.012f, center = p)
        drawCircle(
            color = accent.copy(alpha = 0.4f),
            radius = size.width * 0.025f,
            center = p,
            style = Stroke(width = size.width * 0.004f),
        )
    }
}

private fun DrawScope.drawEmberGlyph(primary: Color, accent: Color) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val w = size.width * 0.28f
    val h = size.height * 0.42f
    val flame = Path().apply {
        moveTo(cx, cy - h / 1.5f)
        cubicTo(
            cx + w, cy - h / 4f,
            cx + w * 1.1f, cy + h / 3f,
            cx, cy + h / 1.4f,
        )
        cubicTo(
            cx - w * 1.1f, cy + h / 3f,
            cx - w, cy - h / 4f,
            cx, cy - h / 1.5f,
        )
        close()
    }
    drawPath(flame, brush = Brush.verticalGradient(listOf(accent, primary)))
    val inner = Path().apply {
        moveTo(cx, cy - h / 3f)
        cubicTo(
            cx + w * 0.55f, cy - h * 0.05f,
            cx + w * 0.55f, cy + h * 0.25f,
            cx, cy + h / 2f,
        )
        cubicTo(
            cx - w * 0.55f, cy + h * 0.25f,
            cx - w * 0.55f, cy - h * 0.05f,
            cx, cy - h / 3f,
        )
        close()
    }
    drawPath(inner, color = accent.copy(alpha = 0.85f))
    drawCircle(
        color = primary.copy(alpha = 0.55f),
        radius = size.minDimension * 0.42f,
        center = Offset(cx, cy),
        style = Stroke(width = size.width * 0.006f),
    )
}
