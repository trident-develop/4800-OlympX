package org.example.project.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.theme.MythColors

@Composable
fun MythicChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val bg by animateColorAsState(
        if (selected) MythColors.Cyan.copy(alpha = 0.22f) else MythColors.Surface.copy(alpha = 0.35f),
        animationSpec = tween(280)
    )
    val border by animateColorAsState(
        if (selected) MythColors.Cyan else MythColors.DividerSoft,
        animationSpec = tween(280)
    )
    val fg by animateColorAsState(
        if (selected) MythColors.CyanBright else MythColors.TextSecondary,
        animationSpec = tween(280)
    )
    val scale by animateFloatAsState(if (selected) 1.02f else 1f, animationSpec = tween(200))
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(bg)
            .border(1.dp, border, shape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            color = fg,
        )
    }
}

@Composable
fun <T> MythicChipRow(
    items: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
    ) {
        items(items.size) { i ->
            val item = items[i]
            MythicChip(
                label = label(item),
                selected = item == selected,
                onClick = { onSelect(item) }
            )
        }
    }
}

val AccentGradientBrush: Brush get() = MythColors.AccentGradient
