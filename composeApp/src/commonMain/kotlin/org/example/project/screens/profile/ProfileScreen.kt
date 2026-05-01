package org.example.project.screens.profile

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import org.example.project.media.LocalMediaPicker
import org.example.project.media.PickerOutcome
import org.example.project.screens.play.GameState
import org.example.project.screens.play.rememberGameState
import org.example.project.theme.ThemeMode
import org.example.project.theme.ThemeState
import org.example.project.platform.SavedStory
import org.example.project.platform.TargetPlatform
import org.example.project.platform.currentPlatform
import org.example.project.platform.decodeImageBitmap
import org.example.project.platform.loadSavedStories
import org.example.project.platform.removeSavedStory
import org.example.project.theme.MythColors
import org.example.project.ui.components.ChoiceSheet
import org.example.project.ui.components.GlowStyle
import org.example.project.ui.components.GlowingButton
import org.example.project.ui.components.MythicCard
import org.example.project.ui.components.MythicDialog
import org.example.project.ui.components.MythicProgressBar
import org.example.project.ui.components.SheetOption

private const val ANDROID_PRIVACY = "https://telegra.ph/Privacy-Policy-for-OlympX-04-22"
private const val IOS_PRIVACY = "https://telegra.ph/Privacy-Policy-for-Zephy-Land-04-22"
private const val IOS_TERMS = "https://telegra.ph/Terms--Conditions-for-Zephy-Land-04-22"

@Composable
fun ProfileScreen(onOpenWeb: (String) -> Unit) {
    val profile = rememberProfileState()
    val picker = LocalMediaPicker.current
    val focusManager = LocalFocusManager.current
    var sheetOpen by remember { mutableStateOf(false) }
    var presetPickerOpen by remember { mutableStateOf(false) }
    var permanentlyDenied by remember { mutableStateOf<String?>(null) }
    var confirmReset by remember { mutableStateOf(false) }

    val handleOutcome: (PickerOutcome) -> Unit = { outcome ->
        when (outcome) {
            is PickerOutcome.Success -> profile.setImage(outcome.bytes)
            is PickerOutcome.PermissionDenied -> {
                if (outcome.permanent) {
                    permanentlyDenied = "This feature needs permission. Open Settings to enable access."
                }
            }
            is PickerOutcome.Error -> { /* silent */ }
            PickerOutcome.Canceled -> { /* silent */ }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProfileHeaderFixed(
            profile = profile,
            onChangePhoto = {
                when (currentPlatform) {
                    TargetPlatform.Android -> sheetOpen = true
                    TargetPlatform.Ios -> presetPickerOpen = true
                }
            },
        )
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProfileIdentity(profile = profile)
            Spacer(Modifier.height(18.dp))
            StatsCard(modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(14.dp))
            ChroniclesSection(modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(14.dp))
            ThemeSection(modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(14.dp))
            LegalSection(
                onOpenWeb = onOpenWeb,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(18.dp))
            ResetIdentityButton(
                onReset = { confirmReset = true },
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
    }

    ChoiceSheet(
        visible = sheetOpen,
        title = "Update your portrait",
        onDismiss = { sheetOpen = false },
        options = listOf(
            SheetOption("Take photo", "Capture via the camera") {
                picker.takePhoto(handleOutcome)
            },
            SheetOption("Choose from gallery", "Pick an image from your library") {
                picker.pickFromGallery(handleOutcome)
            },
        )
    )

    PresetPortraitPicker(
        visible = presetPickerOpen,
        selectedIndex = profile.imageBytes?.presetIndexOrNull(),
        onDismiss = { presetPickerOpen = false },
        onSelect = { idx ->
            profile.setImage(presetBytes(idx))
            presetPickerOpen = false
        },
    )

    MythicDialog(
        visible = permanentlyDenied != null,
        onDismiss = { permanentlyDenied = null },
        title = "Permission needed",
        message = permanentlyDenied,
        confirmLabel = "Open Settings",
        onConfirm = {
            picker.openAppSettings()
            permanentlyDenied = null
        },
        dismissLabel = "Not now",
    )

    MythicDialog(
        visible = confirmReset,
        onDismiss = { confirmReset = false },
        title = "Clear name and portrait?",
        message = "Your chosen name and portrait will be removed. The rest of your progress stays intact.",
        confirmLabel = "Clear",
        onConfirm = {
            confirmReset = false
            profile.clearIdentity()
        },
        dismissLabel = "Cancel",
    )
}

@Composable
private fun ResetIdentityButton(onReset: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MythColors.Crimson.copy(alpha = 0.14f))
            .border(1.dp, MythColors.Crimson.copy(alpha = 0.55f), shape)
            .clickable(interactionSource = interaction, indication = null, onClick = onReset)
            .padding(vertical = 12.dp, horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("✕", color = MythColors.Crimson, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Text(
                "Clear name & portrait",
                color = MythColors.Crimson,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
            )
        }
    }
}

@Composable
private fun ProfileHeaderFixed(profile: ProfileState, onChangePhoto: () -> Unit) {
    val t = rememberInfiniteTransition(label = "profile-head")
    val rot by t.animateFloat(
        0f, 360f,
        animationSpec = infiniteRepeatable(tween(36_000, easing = LinearEasing)),
        label = "rot"
    )
    val glow by t.animateFloat(
        0.6f, 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow"
    )
    Box(
        modifier = Modifier.fillMaxWidth().height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MythColors.SurfaceGlow.copy(alpha = 0.55f * glow),
                        Color.Transparent
                    ),
                    center = Offset(size.width / 2, size.height / 2),
                    radius = size.width * 0.7f
                )
            )
        }
        Canvas(modifier = Modifier.size(240.dp).rotate(rot)) {
            drawCircle(
                color = MythColors.Cyan.copy(alpha = 0.45f),
                radius = size.width / 2 * 0.96f,
                style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 14f)))
            )
        }
        Canvas(modifier = Modifier.size(180.dp).rotate(-rot / 1.4f)) {
            drawCircle(
                color = MythColors.Gold.copy(alpha = 0.45f),
                radius = size.width / 2 * 0.96f,
                style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 10f)))
            )
        }

        AvatarDisplay(
            bytes = profile.imageBytes,
            modifier = Modifier.size(132.dp),
        )

        Box(
            modifier = Modifier
                .padding(bottom = 18.dp)
                .align(Alignment.BottomCenter)
        ) {
            GlowingButton(
                text = "Change portrait",
                onClick = onChangePhoto,
                style = GlowStyle.Ghost,
                corner = 22.dp
            )
        }
    }
}

@Composable
private fun ProfileIdentity(profile: ProfileState) {
    val focusManager = LocalFocusManager.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        BasicTextField(
            value = profile.name,
            onValueChange = { new -> profile.updateName(new) },
            singleLine = true,
            textStyle = TextStyle(
                color = MythColors.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                textAlign = TextAlign.Center,
            ),
            cursorBrush = SolidColor(MythColors.CyanBright),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier
                .padding(horizontal = 56.dp)
                .fillMaxWidth()
                .onFocusChanged { /* keyboard auto-hides when focus is lost */ },
            decorationBox = { inner ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (profile.name.isEmpty()) {
                            Text(
                                "Your name",
                                color = MythColors.TextMuted,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Light,
                                letterSpacing = 0.8.sp,
                            )
                        }
                        inner()
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MythColors.CyanBright.copy(alpha = 0.45f))
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "${profile.name.length}/$PROFILE_NAME_MAX_LENGTH",
                        color = MythColors.TextMuted,
                        fontSize = 9.sp,
                        letterSpacing = 1.2.sp,
                    )
                }
            },
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = profile.title,
            color = MythColors.CyanBright,
            fontSize = 12.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun AvatarDisplay(bytes: ByteArray?, modifier: Modifier = Modifier) {
    val presetIndex = remember(bytes) { bytes?.presetIndexOrNull() }
    val image = remember(bytes, presetIndex) {
        if (presetIndex == null) bytes?.let { decodeImageBitmap(it) } else null
    }
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(MythColors.Surface, MythColors.BgDeep)
                )
            )
            .border(2.dp, MythColors.CyanBright.copy(alpha = 0.7f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when {
            presetIndex != null -> PresetPortrait(presetIndex, modifier = Modifier.fillMaxSize())
            image != null -> Image(
                bitmap = image,
                contentDescription = "Profile",
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
            else -> DefaultAvatarArt()
        }
    }
}

@Composable
private fun DefaultAvatarArt() {
    Canvas(modifier = Modifier.fillMaxSize().padding(18.dp)) {
        val c = Offset(size.width / 2, size.height / 2)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(MythColors.Sky.copy(alpha = 0.35f), Color.Transparent)
            ),
            radius = size.width / 2
        )
        drawCircle(
            color = MythColors.CyanBright,
            radius = size.width / 4f,
            center = Offset(c.x, c.y - size.height / 7f)
        )
        drawArc(
            color = MythColors.Sky,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(c.x - size.width * 0.42f, c.y - size.height * 0.1f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.84f, size.height * 0.95f),
            style = Stroke(width = size.width / 9f)
        )
    }
}

@Composable
private fun StatsCard(modifier: Modifier = Modifier) {
    val game = rememberGameState()
    val glory = game.score
    val wins = game.wins
    val losses = game.losses
    val draws = game.draws
    val battlesPlayed = game.battlesPlayed
    val blessings = game.blessings
    val realms = game.defeatedEnemies.size
    val level = 1 + glory / 100
    val progress = (glory % 100).toFloat() / 100f

    val animatedGlory by animateIntAsState(
        targetValue = glory,
        animationSpec = tween(durationMillis = 900, easing = LinearEasing),
        label = "glory-count",
    )
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 700, easing = LinearEasing),
        label = "level-progress",
    )
    val winRate: Float = if (battlesPlayed == 0) 0f else wins.toFloat() / battlesPlayed

    MythicCard(modifier = modifier.fillMaxWidth(), corner = 22.dp, padding = PaddingValues(18.dp)) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "MYTHIC PROGRESS",
                        color = MythColors.Gold,
                        letterSpacing = 2.5.sp,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Trial standings",
                        color = MythColors.TextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 0.4.sp,
                    )
                }
                LevelBadge(level = level)
            }
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BattleRatioDonut(
                    wins = wins,
                    losses = losses,
                    draws = draws,
                    winRate = winRate,
                )
                Spacer(Modifier.width(18.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "GLORY",
                        color = MythColors.TextSecondary,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "$animatedGlory",
                            color = MythColors.CyanBright,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "⚡",
                            color = MythColors.Gold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                    Text(
                        if (battlesPlayed == 0) "No trials yet" else "$battlesPlayed trials · ${(winRate * 100).toInt()}% victory",
                        color = MythColors.TextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 0.4.sp,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            MythicProgressBar(progress = animatedProgress)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "Lvl $level",
                    color = MythColors.Gold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                )
                Text(
                    "${glory % 100} / 100",
                    color = MythColors.TextMuted,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                )
            }
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatTile("Wins", wins, MythColors.Emerald, "⚔", modifier = Modifier.weight(1f))
                StatTile("Losses", losses, MythColors.Crimson, "✖", modifier = Modifier.weight(1f))
                StatTile("Relics", blessings, MythColors.Gold, "✦", modifier = Modifier.weight(1f))
                StatTile("Realms", realms, MythColors.Sky, "⟡", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LevelBadge(level: Int) {
    val t = rememberInfiniteTransition(label = "lvl")
    val pulse by t.animateFloat(
        0.75f, 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "lvlpulse",
    )
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(MythColors.Gold.copy(alpha = 0.55f * pulse), MythColors.BgDeep)
                )
            )
            .border(1.4.dp, MythColors.Gold.copy(alpha = pulse), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "LVL",
                color = MythColors.Gold,
                fontSize = 8.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "$level",
                color = MythColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun BattleRatioDonut(wins: Int, losses: Int, draws: Int, winRate: Float) {
    val total = (wins + losses + draws).coerceAtLeast(1)
    val winFrac = wins.toFloat() / total
    val lossFrac = losses.toFloat() / total
    val drawFrac = draws.toFloat() / total
    val animatedWin by animateFloatAsState(winFrac, tween(700), label = "winfrac")
    val animatedLoss by animateFloatAsState(lossFrac, tween(700), label = "lossfrac")

    Box(
        modifier = Modifier.size(96.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 10.dp.toPx()
            val inset = stroke / 2f
            val topLeft = Offset(inset, inset)
            val arcSize = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)

            // Background ring
            drawArc(
                color = MythColors.DividerSoft.copy(alpha = 0.6f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke),
            )

            if (wins + losses + draws > 0) {
                val winSweep = 360f * animatedWin
                val lossSweep = 360f * animatedLoss
                val drawSweep = 360f - winSweep - lossSweep
                var cursor = -90f
                drawArc(
                    color = MythColors.Emerald,
                    startAngle = cursor,
                    sweepAngle = winSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke),
                )
                cursor += winSweep
                drawArc(
                    color = MythColors.Crimson,
                    startAngle = cursor,
                    sweepAngle = lossSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke),
                )
                cursor += lossSweep
                drawArc(
                    color = MythColors.Gold,
                    startAngle = cursor,
                    sweepAngle = drawSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${(winRate * 100).toInt()}%",
                color = MythColors.CyanBright,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "WIN RATE",
                color = MythColors.TextMuted,
                fontSize = 8.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun StatTile(label: String, value: Int, color: Color, glyph: String, modifier: Modifier = Modifier) {
    val animatedValue by animateIntAsState(value, tween(600), label = "tile-$label")
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(color.copy(alpha = 0.18f), MythColors.BgDeep)
                )
            )
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(glyph, color = color, fontSize = 14.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                "$animatedValue",
                color = MythColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                label.uppercase(),
                color = MythColors.TextMuted,
                fontSize = 9.sp,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ThemeSection(modifier: Modifier = Modifier) {
    val current = ThemeState.mode
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Appearance",
            color = MythColors.TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ThemeOptionCard(
                label = "Midnight",
                subtitle = "Default · deep void",
                selected = current == ThemeMode.Dark,
                preview = listOf(MythColors.BgAbyss, MythColors.Surface, MythColors.Cyan, MythColors.Gold),
                onSelect = { ThemeState.select(ThemeMode.Dark) },
                modifier = Modifier.weight(1f),
            )
            ThemeOptionCard(
                label = "Azure",
                subtitle = "Brighter blue",
                selected = current == ThemeMode.Light,
                preview = listOf(MythColors.BgAbyss, MythColors.Surface, MythColors.CyanBright, MythColors.GoldBright),
                onSelect = { ThemeState.select(ThemeMode.Light) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ThemeOptionCard(
    label: String,
    subtitle: String,
    selected: Boolean,
    preview: List<Color>,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val accent = if (selected) MythColors.CyanBright else MythColors.DividerSoft
    val background = Brush.verticalGradient(
        listOf(
            if (selected) MythColors.Cyan.copy(alpha = 0.2f) else MythColors.Surface.copy(alpha = 0.5f),
            MythColors.BgDeep,
        )
    )
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = accent,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onSelect)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            preview.forEach { c ->
                Box(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(c)
                        .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                )
            }
            Spacer(Modifier.weight(1f))
            if (selected) {
                Text(
                    "✓",
                    color = MythColors.CyanBright,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Column {
            Text(
                label,
                color = MythColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.4.sp,
            )
            Text(
                subtitle,
                color = MythColors.TextSecondary,
                fontSize = 10.sp,
                letterSpacing = 0.4.sp,
            )
        }
    }
}

@Composable
private fun ChroniclesSection(modifier: Modifier = Modifier) {
    var stories by remember { mutableStateOf(emptyList<SavedStory>()) }
    var refreshTick by remember { mutableStateOf(0) }
    var openedStory by remember { mutableStateOf<SavedStory?>(null) }
    androidx.compose.runtime.LaunchedEffect(refreshTick) {
        stories = loadSavedStories()
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "My Chronicles",
                color = MythColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
            Text(
                if (stories.isEmpty()) "none yet" else "${stories.size} saved",
                color = MythColors.TextSecondary,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
            )
        }
        Spacer(Modifier.height(8.dp))
        if (stories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MythColors.Surface.copy(alpha = 0.4f))
                    .border(1.dp, MythColors.DividerSoft, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Text(
                    "Walk through a chapter in The Saga to save your first chronicle here.",
                    color = MythColors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                stories.forEach { s ->
                    ChronicleRow(
                        story = s,
                        onOpen = { openedStory = s },
                        onDelete = {
                            removeSavedStory(s.id)
                            refreshTick++
                        },
                    )
                }
            }
        }
    }
    ChronicleDialog(
        story = openedStory,
        onClose = { openedStory = null },
    )
}

@Composable
private fun ChronicleRow(story: SavedStory, onOpen: () -> Unit, onDelete: () -> Unit) {
    val dominant = listOf(
        "Courage" to story.courage,
        "Wisdom" to story.wisdom,
        "Cunning" to story.cunning,
        "Compassion" to story.compassion,
    ).maxByOrNull { it.second } ?: ("Wisdom" to 0)
    val accent = when (dominant.first) {
        "Courage" -> MythColors.Crimson
        "Wisdom" -> MythColors.Sky
        "Cunning" -> MythColors.Emerald
        else -> MythColors.GoldBright
    }
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MythColors.Surface.copy(alpha = 0.55f))
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onOpen)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.2f))
                .border(1.dp, accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                dominant.first.first().toString(),
                color = accent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.size(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                story.title,
                color = MythColors.TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Dominant: ${dominant.first} · C${story.courage} W${story.wisdom} X${story.cunning} M${story.compassion}",
                color = MythColors.TextSecondary,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp,
            )
        }
        val trashInteraction = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(MythColors.Crimson.copy(alpha = 0.18f))
                .border(1.dp, MythColors.Crimson.copy(alpha = 0.55f), CircleShape)
                .clickable(interactionSource = trashInteraction, indication = null, onClick = onDelete),
            contentAlignment = Alignment.Center,
        ) {
            Text("×", color = MythColors.Crimson, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ChronicleDialog(story: SavedStory?, onClose: () -> Unit) {
    MythicDialog(
        visible = story != null,
        onDismiss = onClose,
        title = story?.title ?: "",
        dismissLabel = "Close",
        dismissOnScrimClick = false,
        content = {
            val s = story ?: return@MythicDialog
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ChronicleStat("Cour.", s.courage, MythColors.Crimson, modifier = Modifier.weight(1f))
                    ChronicleStat("Wisd.", s.wisdom, MythColors.Sky, modifier = Modifier.weight(1f))
                    ChronicleStat("Cunn.", s.cunning, MythColors.Emerald, modifier = Modifier.weight(1f))
                    ChronicleStat("Comp.", s.compassion, MythColors.GoldBright, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MythColors.BgDeep)
                        .border(1.dp, MythColors.DividerSoft, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        s.story,
                        color = MythColors.TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                    )
                }
            }
        }
    )
}

@Composable
private fun ChronicleStat(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MythColors.BgDeep)
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 8.dp)
    ) {
        Column {
            Text(
                label.uppercase(),
                color = MythColors.TextMuted,
                fontSize = 9.sp,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "$value",
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun PresetPortraitPicker(
    visible: Boolean,
    selectedIndex: Int?,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    MythicDialog(
        visible = visible,
        onDismiss = onDismiss,
        title = "Choose a portrait",
        message = "Pick a sigil to wear into the trials.",
        dismissLabel = "Close",
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    PresetTile(0, selectedIndex == 0, onSelect, modifier = Modifier.weight(1f))
                    PresetTile(1, selectedIndex == 1, onSelect, modifier = Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    PresetTile(2, selectedIndex == 2, onSelect, modifier = Modifier.weight(1f))
                    PresetTile(3, selectedIndex == 3, onSelect, modifier = Modifier.weight(1f))
                }
            }
        },
    )
}

@Composable
private fun PresetTile(
    index: Int,
    selected: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val info = presetPortraitInfo[index]
    val interaction = remember { MutableInteractionSource() }
    val accent = if (selected) MythColors.CyanBright else MythColors.DividerSoft
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        if (selected) MythColors.Cyan.copy(alpha = 0.22f) else MythColors.Surface.copy(alpha = 0.5f),
                        MythColors.BgDeep,
                    )
                )
            )
            .border(
                width = if (selected) 1.6.dp else 1.dp,
                color = accent,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(interactionSource = interaction, indication = null) { onSelect(index) }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(MythColors.Surface, MythColors.BgDeep))
                )
                .border(1.5.dp, accent.copy(alpha = 0.85f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            PresetPortrait(index, modifier = Modifier.fillMaxSize())
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MythColors.CyanBright)
                        .border(1.dp, MythColors.BgDeep, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "✓",
                        color = MythColors.BgDeep,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            info.title,
            color = MythColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.6.sp,
        )
        Text(
            info.subtitle,
            color = MythColors.TextSecondary,
            fontSize = 10.sp,
            letterSpacing = 0.4.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LegalSection(onOpenWeb: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Legal & Privacy",
            color = MythColors.TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
        )
        when (currentPlatform) {
            TargetPlatform.Android -> {
                GlowingButton(
                    text = "Privacy Policy",
                    onClick = { onOpenWeb(ANDROID_PRIVACY) },
                    style = GlowStyle.Ghost,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            TargetPlatform.Ios -> {
                GlowingButton(
                    text = "Privacy Policy",
                    onClick = { onOpenWeb(IOS_PRIVACY) },
                    style = GlowStyle.Ghost,
                    modifier = Modifier.fillMaxWidth()
                )
                GlowingButton(
                    text = "Terms of Use",
                    onClick = { onOpenWeb(IOS_TERMS) },
                    style = GlowStyle.Ghost,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
