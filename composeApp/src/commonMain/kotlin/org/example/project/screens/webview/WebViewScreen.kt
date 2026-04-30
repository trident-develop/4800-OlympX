package org.example.project.screens.webview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.PlatformWebView
import org.example.project.theme.MythColors

@Composable
fun WebViewScreen(url: String, onClose: () -> Unit) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 6 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 6 })
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MythColors.BgAbyss)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                WebToolbar(title = "Privacy & Terms", onClose = onClose)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, MythColors.DividerSoft, RoundedCornerShape(18.dp))
                        .background(Color.White)
                ) {
                    PlatformWebView(url = url, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun WebToolbar(title: String, onClose: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(MythColors.BgMid, MythColors.BgDeep)))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MythColors.SurfaceHigh)
                .border(1.dp, MythColors.DividerSoft, CircleShape)
                .clickable(interactionSource = interaction, indication = null, onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "✕", color = MythColors.CyanBright, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            text = title,
            color = MythColors.TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            letterSpacing = 1.sp,
        )
        androidx.compose.foundation.layout.Spacer(Modifier.size(40.dp).height(1.dp))
    }
}
