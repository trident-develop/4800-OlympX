package org.example.project.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.theme.MythColors

@Composable
fun GlowingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: GlowStyle = GlowStyle.Primary,
    contentPadding: PaddingValues = PaddingValues(horizontal = 22.dp, vertical = 13.dp),
    corner: Dp = 16.dp,
    leading: @Composable (() -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) 0.96f else 1f, animationSpec = tween(120))
    val elevation by animateFloatAsState(if (enabled) 14f else 0f, animationSpec = tween(200))
    val brush: Brush = when (style) {
        GlowStyle.Primary -> MythColors.AccentGradient
        GlowStyle.Gold -> MythColors.GoldGradient
        GlowStyle.Ghost -> Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
        GlowStyle.Danger -> Brush.linearGradient(listOf(MythColors.Crimson, Color(0xFF992A35)))
    }
    val fg: Color = when (style) {
        GlowStyle.Primary -> MythColors.BgAbyss
        GlowStyle.Gold -> MythColors.BgAbyss
        GlowStyle.Ghost -> MythColors.TextPrimary
        GlowStyle.Danger -> MythColors.TextPrimary
    }
    val borderColor: Color = when (style) {
        GlowStyle.Ghost -> MythColors.Cyan.copy(alpha = 0.7f)
        else -> Color.White.copy(alpha = 0.2f)
    }

    val shape = RoundedCornerShape(corner)
    Row(
        modifier = modifier
            .scale(scale)
            .shadow(elevation.dp, shape, ambientColor = MythColors.Cyan, spotColor = MythColors.Azure)
            .clip(shape)
            .background(brush)
            .border(1.dp, borderColor, shape)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        leading?.invoke()
        CompositionLocalProvider(LocalContentColor provides fg) {
            Text(
                text = text.uppercase(),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.4.sp,
                color = fg,
            )
        }
    }
}

enum class GlowStyle { Primary, Gold, Ghost, Danger }
