package org.example.project.screens.play

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.project.platform.decodeImageBitmap
import org.example.project.screens.profile.rememberProfileState
import org.example.project.theme.MythColors
import org.example.project.ui.components.GlowStyle
import org.example.project.ui.components.GlowingButton
import org.example.project.ui.components.MythicCard
import org.example.project.ui.components.MythicDialog
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PlayScreen() {
    val game = rememberGameState()
    val scroll = rememberScrollState()
    var chooseBuildDialog by remember { mutableStateOf(false) }
    var rollFirstDialog by remember { mutableStateOf(false) }
    var noEnergyDialog by remember { mutableStateOf(false) }
    var nowTick by remember { mutableStateOf(0L) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            game.tick()
            nowTick = org.example.project.platform.currentTimeMillis()
            delay(1000)
        }
    }
    // Outgoing (old) card animatables
    val outOffset = remember { Animatable(0f) }
    val outScale = remember { Animatable(1f) }
    val outAlpha = remember { Animatable(1f) }
    val outRotation = remember { Animatable(0f) }
    // Incoming (new) card animatables
    val inOffset = remember { Animatable(-130f) }
    val inScale = remember { Animatable(0.4f) }
    val inAlpha = remember { Animatable(0f) }
    val inRotation = remember { Animatable(12f) }
    var shuffleActive by remember { mutableStateOf(false) }
    var isShuffling by remember { mutableStateOf(false) }

    LaunchedEffect(game.phase) {
        when (game.phase) {
            BattlePhase.Revealing -> {
                delay(720)          // flip duration
                game.onRevealAnimationDone()
            }
            BattlePhase.Resolving -> {
                delay(900)          // dwell on revealed card before outcome
                game.onResolveDelayDone()
            }
            BattlePhase.Unrevealing -> {
                delay(720)          // flip-back duration
                game.onUnrevealAnimationDone()
            }
            else -> {}
        }
    }

    val triggerShuffle: () -> Unit = {
        if (game.phase == BattlePhase.Building && !isShuffling) {
            isShuffling = true
            scope.launch {
                // Reset both cards to starting positions
                outOffset.snapTo(0f)
                outScale.snapTo(1f)
                outAlpha.snapTo(1f)
                outRotation.snapTo(0f)
                inOffset.snapTo(-130f)
                inScale.snapTo(0.4f)
                inAlpha.snapTo(0f)
                inRotation.snapTo(12f)
                shuffleActive = true
                // Swap deck now — both cards are back-face so the content is identical
                game.shuffleDeck()

                coroutineScope {
                    // Outgoing card flies into the deck, tilts and fades
                    launch { outOffset.animateTo(-130f, tween(460, easing = FastOutLinearInEasing)) }
                    launch { outScale.animateTo(0.35f, tween(460, easing = FastOutLinearInEasing)) }
                    launch { outRotation.animateTo(-14f, tween(460, easing = FastOutLinearInEasing)) }
                    launch {
                        delay(140)
                        outAlpha.animateTo(0f, tween(260))
                    }
                    // Incoming card emerges from the deck a moment later
                    launch {
                        delay(180)
                        coroutineScope {
                            launch { inAlpha.animateTo(1f, tween(260)) }
                            launch { inOffset.animateTo(0f, tween(440, easing = LinearOutSlowInEasing)) }
                            launch { inScale.animateTo(1f, tween(440, easing = LinearOutSlowInEasing)) }
                            launch { inRotation.animateTo(0f, tween(440, easing = LinearOutSlowInEasing)) }
                        }
                    }
                }
                // Both cards are now overlapping at center with alpha=1 — snap the
                // idle/outgoing card back to rest so it stays visible after we hide
                // the incoming overlay below.
                outOffset.snapTo(0f)
                outScale.snapTo(1f)
                outAlpha.snapTo(1f)
                outRotation.snapTo(0f)
                shuffleActive = false
                isShuffling = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(14.dp))
            PlayHeader(score = game.score, power = game.playerPower)
            Spacer(Modifier.height(10.dp))
            EnergyBar(
                energy = game.energy,
                maxEnergy = GameState.MAX_ENERGY,
                msUntilNext = remember(nowTick, game.energy) { game.msUntilNextEnergyPoint() },
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(bottom = 100.dp)
            ) {
                BattleArena(
                    game = game,
                    shuffleActive = shuffleActive,
                    outOffsetXDp = outOffset.value,
                    outScale = outScale.value,
                    outAlpha = outAlpha.value,
                    outRotation = outRotation.value,
                    inOffsetXDp = inOffset.value,
                    inScale = inScale.value,
                    inAlpha = inAlpha.value,
                    inRotation = inRotation.value,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(16.dp))
                EquipmentRow(
                    equipped = game.equipped,
                    lockedType = game.lockedType,
                    boostedType = game.boostedType,
                    changesLeft = game.buildChangesLeft,
                    maxChanges = game.maxBuildChanges,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(16.dp))
                ControlRow(
                    game = game,
                    onFightTapped = {
                        when (game.tryStartFight()) {
                            FightStartResult.Started -> {}
                            FightStartResult.NeedConfirm -> chooseBuildDialog = true
                            FightStartResult.NoEnergy -> noEnergyDialog = true
                        }
                    },
                    onShuffleTapped = triggerShuffle,
                    onConfirmTapped = {
                        when {
                            game.buildConfirmed -> {}
                            game.phase != BattlePhase.Building -> {}
                            !game.hasRolledBuild -> rollFirstDialog = true
                            else -> game.confirmBuild()
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
//                Spacer(Modifier.height(14.dp))
//                BattleLog(game.battleLog, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        OutcomeOverlay(game = game)

        MythicDialog(
            visible = chooseBuildDialog,
            onDismiss = { chooseBuildDialog = false },
            title = "Choose your build first",
            message = "Tap the slots below to forge your loadout, then press Confirm. You have ${game.buildChangesLeft} of ${game.maxBuildChanges} reforges left.",
            dismissLabel = "Understood",
        )

        MythicDialog(
            visible = rollFirstDialog,
            onDismiss = { rollFirstDialog = false },
            title = "Forge your loadout first",
            message = "Tap 'Roll full build' at least once to summon your equipment before confirming.",
            dismissLabel = "Understood",
        )

        val msLeft = remember(nowTick, game.energy) { game.msUntilNextEnergyPoint() }
        MythicDialog(
            visible = noEnergyDialog,
            onDismiss = { noEnergyDialog = false },
            title = "Out of energy",
            message = "You've spent all ${GameState.MAX_ENERGY} charges. Next point in ${formatEnergyDuration(msLeft)}. Rest and return for glory.",
            dismissLabel = "Understood",
        )
    }
}

private fun formatEnergyDuration(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L).toInt()
    val m = totalSec / 60
    val s = totalSec % 60
    val mm = if (m < 10) "0$m" else "$m"
    val ss = if (s < 10) "0$s" else "$s"
    return "$mm:$ss"
}

@Composable
private fun EnergyBar(energy: Int, maxEnergy: Int, msUntilNext: Long, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        MythColors.Surface.copy(alpha = 0.6f),
                        MythColors.BgDeep.copy(alpha = 0.6f),
                    )
                )
            )
            .border(1.dp, MythColors.DividerSoft, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⚡", color = MythColors.Gold, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Row {
                repeat(maxEnergy) { i ->
                    val filled = i < energy
                    Box(
                        modifier = Modifier
                            .padding(end = 3.dp)
                            .size(width = 8.dp, height = 14.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (filled) MythColors.CyanBright else MythColors.DividerSoft.copy(alpha = 0.6f)
                            )
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "$energy / $maxEnergy",
                color = MythColors.TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
            )
        }
        if (energy < maxEnergy) {
            Text(
                "Next · ${formatEnergyDuration(msUntilNext)}",
                color = MythColors.Sky,
                fontSize = 11.sp,
                letterSpacing = 0.6.sp,
                fontWeight = FontWeight.SemiBold,
            )
        } else {
            Text(
                "FULL",
                color = MythColors.Emerald,
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun PlayHeader(score: Int, power: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "THE COLISEUM",
                color = MythColors.Gold,
                letterSpacing = 3.sp,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Trial of Cards",
                color = MythColors.TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 0.5.sp,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Glory", color = MythColors.TextSecondary, fontSize = 10.sp, letterSpacing = 1.5.sp)
            Text(
                text = "$score",
                color = MythColors.CyanBright,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Spacer(Modifier.height(2.dp))
            Text("Power · $power", color = MythColors.Sky, fontSize = 11.sp, letterSpacing = 0.5.sp)
        }
    }
}

@Composable
private fun BattleArena(
    game: GameState,
    shuffleActive: Boolean = false,
    outOffsetXDp: Float = 0f,
    outScale: Float = 1f,
    outAlpha: Float = 1f,
    outRotation: Float = 0f,
    inOffsetXDp: Float = -130f,
    inScale: Float = 0.4f,
    inAlpha: Float = 0f,
    inRotation: Float = 0f,
    modifier: Modifier = Modifier,
) {
    MythicCard(
        modifier = modifier.fillMaxWidth(),
        corner = 26.dp,
        padding = PaddingValues(14.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(340.dp)) {
            ArenaBackdrop()
            val revealed = game.phase == BattlePhase.Revealing ||
                game.phase == BattlePhase.Resolving ||
                game.phase == BattlePhase.Outcome
            val flipAnimating = game.phase == BattlePhase.Revealing ||
                game.phase == BattlePhase.Unrevealing
            // Incoming (new) card — only rendered while shuffling
            if (shuffleActive) {
                EnemyCardView(
                    enemy = game.currentEnemy,
                    revealed = revealed,
                    flipTriggeredInPhase = flipAnimating,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = inOffsetXDp.dp)
                        .graphicsLayer {
                            scaleX = inScale
                            scaleY = inScale
                            alpha = inAlpha
                            rotationZ = inRotation
                        }
                )
            }
            // Outgoing / idle card
            EnemyCardView(
                enemy = game.currentEnemy,
                revealed = revealed,
                flipTriggeredInPhase = flipAnimating,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = outOffsetXDp.dp)
                    .graphicsLayer {
                        scaleX = outScale
                        scaleY = outScale
                        alpha = outAlpha
                        rotationZ = outRotation
                    }
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .zIndex(1f)
            ) {
                DeckStack(shuffling = shuffleActive)
            }
            UserAvatar(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
            )
        }
    }
}

@Composable
private fun ArenaBackdrop() {
    val t = rememberInfiniteTransition(label = "arena")
    val rot by t.animateFloat(
        0f, 360f,
        animationSpec = infiniteRepeatable(tween(36_000, easing = LinearEasing)),
        label = "arena-rot"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.radialGradient(
                    listOf(
                        MythColors.SurfaceGlow.copy(alpha = 0.5f),
                        MythColors.BgDeep,
                        MythColors.BgAbyss
                    )
                )
            )
            .border(1.dp, MythColors.DividerSoft, RoundedCornerShape(18.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().rotate(rot)) {
            val cx = size.width / 2
            val cy = size.height / 2
            val radius = minOf(size.width, size.height) / 2.6f
            drawCircle(
                color = MythColors.Cyan.copy(alpha = 0.35f),
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f)))
            )
            drawCircle(
                color = MythColors.Gold.copy(alpha = 0.25f),
                radius = radius * 0.7f,
                center = Offset(cx, cy),
                style = Stroke(width = 0.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 10f)))
            )
        }
    }
}

@Composable
private fun DeckStack(shuffling: Boolean = false) {
    val t = rememberInfiniteTransition(label = "deck")
    val glow by t.animateFloat(
        0.6f, 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "deckglow"
    )
    val wobble by animateFloatAsState(
        targetValue = if (shuffling) 6f else 0f,
        animationSpec = tween(220, easing = LinearEasing),
        label = "deckwobble",
    )
    val shake = rememberInfiniteTransition(label = "deckshake")
    val shakePhase by shake.animateFloat(
        -1f, 1f,
        animationSpec = infiniteRepeatable(tween(140, easing = LinearEasing), RepeatMode.Reverse),
        label = "shakephase",
    )
    val shakeX = if (shuffling) shakePhase * wobble else 0f
    Box(
        modifier = Modifier
            .size(width = 96.dp, height = 140.dp)
            .offset(x = shakeX.dp),
        contentAlignment = Alignment.TopStart
    ) {
        listOf(6.dp, 3.dp, 0.dp).forEachIndexed { i, offset ->
            val alpha = 0.4f + i * 0.2f
            Box(
                modifier = Modifier
                    .size(width = 72.dp, height = 104.dp)
                    .offset(x = offset, y = offset)
                    .shadow(6.dp, RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MythColors.Cobalt.copy(alpha = alpha),
                                MythColors.BgMid.copy(alpha = alpha)
                            )
                        )
                    )
                    .border(1.dp, MythColors.Cyan.copy(alpha = 0.3f + i * 0.2f), RoundedCornerShape(10.dp))
            )
        }
        Box(
            modifier = Modifier
                .size(width = 72.dp, height = 104.dp)
                .offset(x = 0.dp, y = 0.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MythColors.BgDeep)
                .border(1.dp, MythColors.CyanBright.copy(alpha = glow), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⚜", color = MythColors.Gold, fontSize = 28.sp)
                Spacer(Modifier.height(4.dp))
                Text("DECK", color = MythColors.CyanBright, fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun EnemyCardView(
    enemy: EnemyCard?,
    revealed: Boolean,
    flipTriggeredInPhase: Boolean,
    modifier: Modifier = Modifier,
) {
    val t = rememberInfiniteTransition(label = "enemy")
    val drift by t.animateFloat(
        -3f, 3f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift"
    )
    val aura by t.animateFloat(
        0.55f, 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Reverse),
        label = "aura"
    )

    // Flip rotation: animate 0 → 180 during Revealing phase, held at 180 afterward, snaps to 0 in Building.
    val flip by animateFloatAsState(
        targetValue = when {
            revealed -> 180f
            else -> 0f
        },
        animationSpec = tween(
            durationMillis = if (flipTriggeredInPhase || revealed) 720 else 0,
            easing = LinearEasing,
        ),
        label = "flip",
    )

    if (enemy == null) {
        Box(
            modifier = modifier.size(width = 170.dp, height = 240.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Deck empty", color = MythColors.TextSecondary, fontSize = 12.sp)
        }
        return
    }

    Box(
        modifier = modifier
            .size(width = 182.dp, height = 252.dp)
            .offset(y = drift.dp)
    ) {
        // Back face (visible while flip < 90°).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = flip
                    cameraDistance = 14f * density
                }
        ) {
            if (flip <= 90f) {
                EnemyCardBack()
            }
        }
        // Front face (visible once flip >= 90°). Counter-rotate so content isn't mirrored.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = flip - 180f
                    cameraDistance = 14f * density
                }
        ) {
            if (flip > 90f) {
                EnemyCardFront(enemy = enemy, aura = aura)
            }
        }
    }
}

@Composable
private fun EnemyCardBack() {
    val t = rememberInfiniteTransition(label = "back")
    val shimmer by t.animateFloat(
        0.6f, 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Reverse),
        label = "shim",
    )
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .shadow(20.dp, shape, ambientColor = MythColors.Gold, spotColor = MythColors.Gold)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(MythColors.Surface, MythColors.BgMid, MythColors.BgDeep)
                )
            )
            .border(1.4.dp, MythColors.Gold.copy(alpha = shimmer), shape),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2
            val r = minOf(size.width, size.height) / 2.6f
            drawCircle(
                color = MythColors.Gold.copy(alpha = 0.35f * shimmer),
                radius = r,
                center = Offset(cx, cy),
                style = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))),
            )
            drawCircle(
                color = MythColors.CyanBright.copy(alpha = 0.25f),
                radius = r * 0.65f,
                center = Offset(cx, cy),
                style = Stroke(width = 0.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f))),
            )
            for (i in 0 until 12) {
                val a = (i.toFloat() / 12f) * 2f * PI.toFloat()
                drawCircle(
                    color = MythColors.Gold.copy(alpha = 0.6f * shimmer),
                    radius = 2f,
                    center = Offset(cx + cos(a) * r, cy + sin(a) * r)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚔", color = MythColors.Gold, fontSize = 46.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                "HIDDEN",
                color = MythColors.Gold,
                letterSpacing = 4.sp,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Fight to reveal",
                color = MythColors.TextSecondary,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
            )
        }
    }
}

@Composable
private fun EnemyCardFront(enemy: EnemyCard, aura: Float) {
    val shape = RoundedCornerShape(18.dp)
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(enemy.accent.copy(alpha = 0.45f * aura), Color.Transparent),
                    center = Offset(size.width / 2, size.height / 2),
                    radius = size.width * 0.7f
                ),
                radius = size.width * 0.7f,
                center = Offset(size.width / 2, size.height / 2)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
                .shadow(22.dp, shape, ambientColor = enemy.accent, spotColor = enemy.accent)
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MythColors.Surface,
                            enemy.accent.copy(alpha = 0.18f),
                            MythColors.BgDeep
                        )
                    )
                )
                .border(1.2.dp, enemy.accent.copy(alpha = 0.8f), shape)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "POWER",
                        color = MythColors.TextSecondary,
                        fontSize = 9.sp,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${enemy.power}",
                        color = MythColors.CyanBright,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MythColors.BgAbyss)
                        .border(1.dp, enemy.accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(enemy.symbol, color = enemy.accent, fontSize = 18.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MythColors.BgAbyss.copy(alpha = 0.7f))
                    .border(1.dp, enemy.accent.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                EnemySigil(enemy = enemy, aura = aura)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                enemy.name,
                color = MythColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
            Text(
                enemy.epithet,
                color = MythColors.TextSecondary,
                fontSize = 10.sp,
                letterSpacing = 0.8.sp
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MythColors.Crimson.copy(alpha = 0.18f))
                    .border(1.dp, MythColors.Crimson.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    "On defeat: lose ${enemy.penalty.display}",
                    color = MythColors.TextPrimary,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun EnemySigil(enemy: EnemyCard, aura: Float) {
    Canvas(modifier = Modifier.size(72.dp)) {
        val c = Offset(size.width / 2, size.height / 2)
        val r = size.width / 2
        drawCircle(
            color = enemy.accent.copy(alpha = 0.35f * aura),
            radius = r,
            center = c
        )
        drawCircle(
            color = enemy.accent,
            radius = r * 0.88f,
            center = c,
            style = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f)))
        )
        for (i in 0 until 6) {
            val a = (i.toFloat() / 6f) * 2f * PI.toFloat()
            drawCircle(
                color = enemy.accent,
                radius = 2f,
                center = Offset(c.x + cos(a) * r * 0.88f, c.y + sin(a) * r * 0.88f)
            )
        }
    }
}

@Composable
private fun UserAvatar(modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "avatar")
    val pulse by t.animateFloat(
        0.75f, 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val profile = rememberProfileState()
    val bytes = profile.imageBytes
    val image = remember(bytes) { bytes?.let { decodeImageBitmap(it) } }
    Box(
        modifier = modifier.size(width = 96.dp, height = 120.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(MythColors.Cyan.copy(alpha = 0.45f * pulse), Color.Transparent),
                    center = Offset(size.width / 2, size.height * 0.6f),
                    radius = size.width * 0.9f
                ),
                radius = size.width * 0.9f,
                center = Offset(size.width / 2, size.height * 0.6f)
            )
        }
        Box(
            modifier = Modifier
                .size(78.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(MythColors.SurfaceGlow, MythColors.BgDeep)
                    )
                )
                .border(1.4.dp, MythColors.CyanBright.copy(alpha = pulse), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (image != null) {
                Image(
                    bitmap = image,
                    contentDescription = "Profile portrait",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Canvas(Modifier.size(54.dp)) {
                    val w = size.width
                    val h = size.height
                    // Head
                    drawCircle(
                        color = MythColors.CyanBright,
                        radius = w * 0.22f,
                        center = Offset(w / 2, h * 0.32f)
                    )
                    // Shoulders — filled dome whose bottom extends past the canvas
                    drawArc(
                        color = MythColors.CyanBright,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(0f, h * 0.58f),
                        size = androidx.compose.ui.geometry.Size(w, h * 0.9f),
                    )
                }
            }
        }
    }
}

@Composable
private fun EquipmentRow(
    equipped: Map<EquipmentType, EquipmentCard?>,
    lockedType: EquipmentType?,
    boostedType: EquipmentType?,
    changesLeft: Int,
    maxChanges: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Loadout", color = MythColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(
                "Reforges · $changesLeft / $maxChanges",
                color = if (changesLeft > 0) MythColors.CyanBright else MythColors.TextMuted,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EquipmentType.entries.forEach { type ->
                EquipmentSlot(
                    type = type,
                    card = equipped[type],
                    locked = lockedType == type,
                    boosted = boostedType == type,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EquipmentSlot(
    type: EquipmentType,
    card: EquipmentCard?,
    locked: Boolean,
    boosted: Boolean,
    modifier: Modifier = Modifier,
) {
    val hasItem = card != null
    val t = rememberInfiniteTransition(label = "slot-${type.name}")
    val glowAlpha by t.animateFloat(
        0.45f, 0.95f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Reverse),
        label = "slotglow"
    )
    val rarityAccent = card?.rarity?.color ?: MythColors.TextMuted
    val borderColor = when {
        locked -> MythColors.Crimson
        boosted -> MythColors.Gold
        hasItem -> rarityAccent.copy(alpha = glowAlpha)
        else -> MythColors.DividerSoft
    }
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .height(128.dp)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        when {
                            locked -> MythColors.Crimson.copy(alpha = 0.18f)
                            boosted -> MythColors.Gold.copy(alpha = 0.18f)
                            hasItem -> rarityAccent.copy(alpha = 0.18f)
                            else -> MythColors.Surface.copy(alpha = 0.5f)
                        },
                        MythColors.BgDeep
                    )
                )
            )
            .border(1.dp, borderColor, shape)
            .padding(8.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (hasItem) Brush.radialGradient(listOf(rarityAccent.copy(alpha = 0.75f), MythColors.BgDeep))
                        else Brush.radialGradient(listOf(MythColors.BgDeep, MythColors.BgAbyss))
                    )
                    .border(1.dp, rarityAccent.copy(alpha = if (hasItem) 1f else 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (locked) "⛔" else type.glyph,
                    color = if (hasItem) Color.White else MythColors.TextMuted,
                    fontSize = 18.sp,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                type.display,
                color = MythColors.TextSecondary,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            when {
                locked -> Text("Barred", color = MythColors.Crimson, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                card != null -> Text(
                    card.name,
                    color = MythColors.TextPrimary,
                    fontSize = 10.sp,
                    maxLines = 2,
                    fontWeight = FontWeight.Medium,
                )
                boosted -> Text("Blessed", color = MythColors.Gold, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                else -> Text("Empty", color = MythColors.TextMuted, fontSize = 10.sp)
            }
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "PWR",
                    color = MythColors.TextSecondary,
                    fontSize = 8.sp,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    if (card != null) "${card.power}" else "–",
                    color = if (hasItem) rarityAccent else MythColors.TextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ControlRow(
    game: GameState,
    onFightTapped: () -> Unit,
    onShuffleTapped: () -> Unit,
    onConfirmTapped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fightDim = game.phase != BattlePhase.Building || !game.buildConfirmed
    val fightLabel = when (game.phase) {
        BattlePhase.Building -> if (game.buildConfirmed) "Fight" else "Fight"
        BattlePhase.Revealing -> "Revealing…"
        BattlePhase.Resolving -> "Resolving…"
        BattlePhase.Outcome -> "Awaiting tribute"
        BattlePhase.Unrevealing -> "Drawing next…"
    }
    val canConfirm = game.phase == BattlePhase.Building && game.hasRolledBuild
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { alpha = if (fightDim) 0.55f else 1f }
            ) {
                GlowingButton(
                    text = fightLabel,
                    onClick = onFightTapped,
                    style = GlowStyle.Primary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { alpha = if (game.buildConfirmed || canConfirm) 1f else 0.55f }
            ) {
                GlowingButton(
                    text = if (game.buildConfirmed) "Confirmed ✓" else "Confirm build",
                    onClick = onConfirmTapped,
                    style = if (game.buildConfirmed) GlowStyle.Ghost else GlowStyle.Gold,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val rollEnabled = game.phase == BattlePhase.Building && game.buildChangesLeft > 0
            Box(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { alpha = if (rollEnabled) 1f else 0.55f }
            ) {
                GlowingButton(
                    text = "Roll full build",
                    onClick = { if (rollEnabled) game.rollBuild() },
                    style = GlowStyle.Gold,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            GlowingButton(
                text = "Shuffle deck",
                onClick = onShuffleTapped,
                style = GlowStyle.Ghost,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BattleLog(entries: List<String>, modifier: Modifier = Modifier) {
    if (entries.isEmpty()) return
    MythicCard(
        modifier = modifier.fillMaxWidth(),
        corner = 18.dp,
        padding = PaddingValues(14.dp),
        glow = false
    ) {
        Column {
            Text("Battle Chronicle", color = MythColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            entries.forEach { line ->
                Text(
                    "· $line",
                    color = MythColors.TextSecondary,
                    fontSize = 11.sp,
                    letterSpacing = 0.4.sp
                )
            }
        }
    }
}

@Composable
private fun OutcomeOverlay(game: GameState) {
    val show = game.phase == BattlePhase.Outcome
    val result = game.lastResult
    val scale by animateFloatAsState(
        if (show) 1f else 0.8f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "overlay-scale",
    )
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(tween(260)) + scaleIn(initialScale = 0.9f),
        exit = fadeOut(tween(180)) + scaleOut(targetScale = 1.05f),
    ) {
        if (result == null) return@AnimatedVisibility
        val color = when (result.outcome) {
            BattleOutcome.Win -> MythColors.Emerald
            BattleOutcome.Draw -> MythColors.Gold
            BattleOutcome.Lose -> MythColors.Crimson
        }
        val title = when (result.outcome) {
            BattleOutcome.Win -> "Victory"
            BattleOutcome.Draw -> "Stalemate"
            BattleOutcome.Lose -> "Defeated"
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MythColors.BgAbyss.copy(alpha = 0.72f)),
            contentAlignment = Alignment.Center,
        ) {
            MythicCard(
                modifier = Modifier.scale(scale).padding(horizontal = 24.dp),
                corner = 26.dp,
                padding = PaddingValues(horizontal = 22.dp, vertical = 22.dp),
                brush = Brush.verticalGradient(listOf(MythColors.Surface, color.copy(alpha = 0.22f), MythColors.BgDeep)),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        title.uppercase(),
                        color = color,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${result.enemy.name} · ${result.enemy.power} vs ${result.playerPower}",
                        color = MythColors.TextPrimary,
                        fontSize = 13.sp,
                        letterSpacing = 0.8.sp,
                    )
                    Spacer(Modifier.height(14.dp))
                    when (result.outcome) {
                        BattleOutcome.Win -> RewardPicker(game = game, accent = color)
                        BattleOutcome.Lose -> LossPanel(game = game, penalty = result.enemy.penalty, accent = color)
                        BattleOutcome.Draw -> DrawPanel(game = game)
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardPicker(game: GameState, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "CHOOSE A BLESSING",
            color = MythColors.Gold,
            fontSize = 9.sp,
            letterSpacing = 2.5.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            "One slot is blessed with +${game.boostRangeLabel} power next battle.",
            color = MythColors.TextSecondary,
            fontSize = 10.sp,
            letterSpacing = 0.4.sp,
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            game.rewardOptions.forEach { type ->
                RewardCard(type = type, onClick = { game.chooseReward(type) }, modifier = Modifier.weight(1f))
            }
        }
    }
    // accent unused param kept for future variations
    @Suppress("UNUSED_EXPRESSION") accent
}

@Composable
private fun RewardCard(type: EquipmentType, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(listOf(MythColors.Gold.copy(alpha = 0.22f), MythColors.BgDeep))
            )
            .border(1.dp, MythColors.Gold, RoundedCornerShape(14.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(10.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(MythColors.Gold.copy(alpha = 0.8f), MythColors.BgDeep)))
                    .border(1.dp, MythColors.GoldBright, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(type.glyph, color = Color.White, fontSize = 16.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(type.display, color = MythColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(1.dp))
            Text("Blessed +10–15 PWR", color = MythColors.Gold, fontSize = 9.sp, letterSpacing = 0.8.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun LossPanel(game: GameState, penalty: EquipmentType, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(MythColors.Crimson.copy(alpha = 0.18f))
                .border(1.dp, MythColors.Crimson.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MythColors.BgAbyss)
                        .border(1.dp, MythColors.Crimson, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(penalty.glyph, color = MythColors.Crimson, fontSize = 16.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "${penalty.display} barred",
                        color = MythColors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "One battle without this slot.",
                        color = MythColors.TextSecondary,
                        fontSize = 10.sp,
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        GlowingButton(
            text = "Continue",
            onClick = { game.acknowledgeOutcome() },
            style = GlowStyle.Primary,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    @Suppress("UNUSED_EXPRESSION") accent
}

@Composable
private fun DrawPanel(game: GameState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Fate hesitates.",
            color = MythColors.TextSecondary,
            fontSize = 12.sp,
        )
        Spacer(Modifier.height(12.dp))
        GlowingButton(
            text = "Draw next card",
            onClick = { game.acknowledgeOutcome() },
            style = GlowStyle.Primary,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
