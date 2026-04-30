package org.example.project.ui.nav

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.theme.MythColors

@Composable
fun MythicBottomBar(
    current: NavTab,
    onSelect: (NavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(22.dp, shape, clip = false)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MythColors.Surface.copy(alpha = 0.88f),
                        MythColors.BgDeep
                    )
                )
            )
            .border(1.dp, MythColors.DividerSoft, shape)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTab.values().forEach { tab ->
                BottomNavItem(
                    tab = tab,
                    selected = tab == current,
                    onClick = { onSelect(tab) },
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: NavTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val tint by animateColorAsState(
        if (selected) MythColors.CyanBright else MythColors.TextSecondary,
        animationSpec = tween(260)
    )
    val bgAlpha by animateFloatAsState(
        if (selected) 0.32f else 0f,
        animationSpec = tween(300)
    )
    val scale by animateFloatAsState(
        if (selected) 1.08f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )
    Column(
        modifier = modifier
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 54.dp, height = 34.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MythColors.Cyan.copy(alpha = bgAlpha)),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Canvas(Modifier.size(width = 54.dp, height = 34.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(MythColors.Cyan.copy(alpha = 0.35f), Color.Transparent),
                            center = Offset(size.width / 2, size.height / 2),
                            radius = size.width / 2
                        )
                    )
                }
            }
            Text(
                text = tab.symbol,
                color = tint,
                fontSize = 22.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.scale(scale)
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = tab.label,
            color = tint,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            letterSpacing = 0.8.sp,
        )
    }
}
