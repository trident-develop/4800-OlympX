package org.example.project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.example.project.theme.MythColors

@Composable
fun MythicCard(
    modifier: Modifier = Modifier,
    corner: Dp = 22.dp,
    padding: PaddingValues = PaddingValues(16.dp),
    glow: Boolean = true,
    brush: Brush = MythColors.CardGradient,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(corner)
    val interaction = remember { MutableInteractionSource() }
    val base = modifier
        .let { if (glow) it.shadow(18.dp, shape, ambientColor = MythColors.Cyan, spotColor = MythColors.Azure) else it }
        .clip(shape)
        .background(brush)
        .border(1.dp, MythColors.DividerSoft, shape)
    val withClick = if (onClick != null) {
        base.clickable(interactionSource = interaction, indication = null, onClick = onClick)
    } else base
    Box(modifier = withClick.padding(padding)) { content() }
}

@Composable
fun GlassRow(
    modifier: Modifier = Modifier,
    corner: Dp = 16.dp,
    tint: Color = MythColors.Surface.copy(alpha = 0.55f),
    padding: PaddingValues = PaddingValues(14.dp),
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(corner)
    Box(
        modifier
            .clip(shape)
            .background(tint)
            .border(1.dp, MythColors.DividerSoft, shape)
            .padding(padding)
    ) { content() }
}
