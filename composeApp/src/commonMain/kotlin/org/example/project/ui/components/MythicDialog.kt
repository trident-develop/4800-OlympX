package org.example.project.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.example.project.theme.MythColors

@Composable
fun MythicDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    message: String? = null,
    confirmLabel: String? = null,
    onConfirm: (() -> Unit)? = null,
    dismissLabel: String? = "Cancel",
    dismissible: Boolean = true,
    dismissOnScrimClick: Boolean = true,
    content: @Composable (() -> Unit)? = null,
) {
    if (!visible) return
    val scrimDismiss = dismissible && dismissOnScrimClick
    Dialog(
        onDismissRequest = { if (dismissible) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = dismissible,
            dismissOnClickOutside = scrimDismiss,
            usePlatformDefaultWidth = false,
        )
    ) {
        val scrim = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MythColors.BgAbyss.copy(alpha = 0.78f))
                .clickable(interactionSource = scrim, indication = null, enabled = scrimDismiss, onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(220)) + scaleIn(
                    initialScale = 0.92f,
                    animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium)
                ),
                exit = fadeOut(tween(160)) + scaleOut(targetScale = 0.95f)
            ) {
                MythicCard(
                    modifier = Modifier
                        .padding(horizontal = 28.dp)
                        .fillMaxWidth(),
                    corner = 24.dp,
                    padding = PaddingValues(22.dp)
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = title,
                            color = MythColors.TextPrimary,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp,
                        )
                        if (message != null) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = message,
                                color = MythColors.TextSecondary,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                            )
                        }
                        if (content != null) {
                            Spacer(Modifier.height(14.dp))
                            content()
                        }
                        Spacer(Modifier.height(18.dp))
                        DialogActions(
                            dismissLabel = dismissLabel,
                            onDismiss = onDismiss,
                            confirmLabel = confirmLabel,
                            onConfirm = onConfirm,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogActions(
    dismissLabel: String?,
    onDismiss: () -> Unit,
    confirmLabel: String?,
    onConfirm: (() -> Unit)?,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        if (dismissLabel != null) {
            GlowingButton(
                text = dismissLabel,
                onClick = onDismiss,
                style = GlowStyle.Ghost,
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
            )
            Spacer(Modifier.padding(horizontal = 4.dp))
        }
        if (confirmLabel != null && onConfirm != null) {
            GlowingButton(
                text = confirmLabel,
                onClick = onConfirm,
                style = GlowStyle.Primary,
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
fun ChoiceSheet(
    visible: Boolean,
    title: String,
    onDismiss: () -> Unit,
    options: List<SheetOption>,
) {
    MythicDialog(
        visible = visible,
        onDismiss = onDismiss,
        title = title,
        confirmLabel = null,
        dismissLabel = "Close",
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                options.forEach { opt ->
                    val interaction = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(interactionSource = interaction, indication = null) {
                                onDismiss(); opt.onClick()
                            }
                    ) {
                        GlassRow(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text(
                                    text = opt.title,
                                    color = MythColors.TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                )
                                if (opt.subtitle != null) {
                                    Text(
                                        text = opt.subtitle,
                                        color = MythColors.TextSecondary,
                                        fontSize = 12.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

data class SheetOption(
    val title: String,
    val subtitle: String? = null,
    val onClick: () -> Unit,
)

val DialogDividerColor: Color = MythColors.DividerSoft
