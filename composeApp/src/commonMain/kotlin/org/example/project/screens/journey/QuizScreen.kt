package org.example.project.screens.journey

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import olympx.composeapp.generated.resources.Res
import olympx.composeapp.generated.resources.zeus
import org.example.project.platform.SavedStory
import org.example.project.platform.addSavedStory
import org.example.project.platform.clearQuizProgress
import org.example.project.platform.currentTimeMillis
import org.example.project.platform.loadQuizProgress
import org.example.project.platform.saveLastChapterId
import org.example.project.platform.saveQuizProgress
import org.example.project.theme.MythColors
import org.example.project.ui.components.GlowStyle
import org.example.project.ui.components.GlowingButton
import org.example.project.ui.components.MythicCard
import org.example.project.ui.components.MythicProgressBar
import org.example.project.ui.effects.AuroraBackground
import org.example.project.ui.effects.ParticleField
import org.jetbrains.compose.resources.painterResource
import kotlinx.coroutines.delay
import kotlin.random.Random

private enum class QuizPhase { Questions, Results, Story }

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("DEPRECATION")
@Composable
fun QuizScreen(chapter: QuizChapter, onClose: () -> Unit) {
    val answers = remember(chapter.chapterId) {
        mutableStateListOf<Int>().apply {
            val saved = loadQuizProgress(chapter.chapterId)
                .filter { it in 0..3 }
                .take(chapter.questions.size)
            addAll(saved)
        }
    }
    var phase by remember(chapter.chapterId) { mutableStateOf(QuizPhase.Questions) }
    var zeusSpeaking by remember { mutableStateOf(false) }

    LaunchedEffect(chapter.chapterId) {
        saveLastChapterId(chapter.chapterId)
    }

    BackHandler(enabled = phase == QuizPhase.Questions, onBack = onClose)
    BackHandler(enabled = phase == QuizPhase.Results, onBack = { phase = QuizPhase.Questions })
    BackHandler(enabled = phase == QuizPhase.Story, onBack = { phase = QuizPhase.Results })

    AuroraBackground(modifier = Modifier.fillMaxSize().background(MythColors.BgAbyss)) {
        ParticleField(count = 22, seed = 42L)
        LightningLayer(active = zeusSpeaking && phase == QuizPhase.Questions, accent = chapter.accent)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 78.dp)
        ) {
            QuizHeader(
                chapter = chapter,
                progress = when (phase) {
                    QuizPhase.Questions -> answers.size.toFloat() / chapter.questions.size
                    else -> 1f
                },
                index = answers.size,
                total = chapter.questions.size,
                phase = phase,
                onClose = onClose,
            )

            AnimatedContent(
                targetState = phase,
                transitionSpec = {
                    (fadeIn(tween(260)) + slideInHorizontally { it / 3 })
                        .togetherWith(fadeOut(tween(180)) + slideOutHorizontally { -it / 3 })
                },
                label = "quiz-phase",
                modifier = Modifier.fillMaxSize(),
            ) { current ->
                when (current) {
                    QuizPhase.Questions -> QuestionStage(
                        chapter = chapter,
                        answers = answers,
                        onPick = { optionIdx ->
                            answers.add(optionIdx)
                            if (answers.size >= chapter.questions.size) {
                                clearQuizProgress(chapter.chapterId)
                            } else {
                                saveQuizProgress(chapter.chapterId, answers.toList())
                            }
                        },
                        onFinish = { phase = QuizPhase.Results },
                        onTypingChange = { zeusSpeaking = it },
                    )
                    QuizPhase.Results -> ResultsStage(
                        chapter = chapter,
                        answers = answers,
                        onReadStory = { phase = QuizPhase.Story },
                        onRetry = {
                            answers.clear()
                            clearQuizProgress(chapter.chapterId)
                            phase = QuizPhase.Questions
                        },
                    )
                    QuizPhase.Story -> StoryStage(
                        chapter = chapter,
                        answers = answers,
                        onSave = { savedStory ->
                            addSavedStory(savedStory)
                            clearQuizProgress(chapter.chapterId)
                            onClose()
                        },
                        onBack = { phase = QuizPhase.Results },
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizHeader(
    chapter: QuizChapter,
    progress: Float,
    index: Int,
    total: Int,
    phase: QuizPhase,
    onClose: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${chapter.realm.uppercase()} · CHAPTER",
                    color = chapter.accent,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    chapter.title,
                    color = MythColors.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            val interaction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MythColors.Surface.copy(alpha = 0.7f))
                    .border(1.dp, MythColors.DividerSoft, CircleShape)
                    .clickable(interactionSource = interaction, indication = null, onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Text("×", color = MythColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(8.dp))
        val label = when (phase) {
            QuizPhase.Questions -> "Question ${index + 1} of $total"
            QuizPhase.Results -> "Odyssey complete"
            QuizPhase.Story -> "Your written chronicle"
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, color = MythColors.TextSecondary, fontSize = 11.sp, letterSpacing = 1.sp)
            Text(
                "${(progress * 100).toInt()}%",
                color = chapter.accent,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            )
        }
        Spacer(Modifier.height(6.dp))
        MythicProgressBar(progress = progress)
    }
}

@Composable
private fun ZeusHero(accent: Color, isTyping: Boolean) {
    val t = rememberInfiniteTransition(label = "zeus-hero")
    val glow by t.animateFloat(
        0.5f, 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Reverse),
        label = "zeus-glow",
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(190.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(accent.copy(alpha = 0.55f * glow), Color.Transparent),
                        center = Offset(size.width / 2, size.height / 2),
                        radius = size.width / 2,
                    ),
                    radius = size.width / 2,
                )
            }
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .shadow(24.dp, CircleShape, ambientColor = accent, spotColor = accent)
                    .clip(CircleShape)
                    .border(2.5.dp, accent.copy(alpha = glow), CircleShape),
            ) {
                Image(
                    painter = painterResource(Res.drawable.zeus),
                    contentDescription = "Zeus",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "ZEUS",
                color = accent,
                fontSize = 12.sp,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "The Thunderer",
                color = MythColors.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 0.5.sp,
            )
            Spacer(Modifier.height(8.dp))
            AnimatedVisibility(
                visible = isTyping,
                enter = fadeIn(tween(180)),
                exit = fadeOut(tween(120)),
            ) {
                TypingIndicator(accent = accent)
            }
        }
    }
}

@Composable
private fun TypingIndicator(accent: Color) {
    val t = rememberInfiniteTransition(label = "typing")
    val phase by t.animateFloat(
        0f, 3f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "phase",
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "speaking",
            color = accent,
            fontSize = 10.sp,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.width(8.dp))
        for (i in 0 until 3) {
            val a = (((phase - i) % 3f) / 3f).coerceIn(0f, 1f)
            val alpha = 0.3f + 0.7f * (1f - kotlin.math.abs(a - 0.5f) * 2f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = alpha))
            )
        }
    }
}

@Composable
private fun LightningLayer(active: Boolean, accent: Color) {
    val flashAlpha = remember { Animatable(0f) }
    var seed by remember { mutableStateOf(0L) }
    LaunchedEffect(active) {
        if (!active) {
            flashAlpha.snapTo(0f)
            return@LaunchedEffect
        }
        while (true) {
            delay(Random.nextLong(500, 1500))
            seed = Random.nextLong()
            flashAlpha.snapTo(1f)
            flashAlpha.animateTo(0f, tween(Random.nextInt(220, 420)))
            if (Random.nextFloat() < 0.35f) {
                delay(70)
                seed = Random.nextLong()
                flashAlpha.snapTo(0.8f)
                flashAlpha.animateTo(0f, tween(180))
            }
        }
    }
    val a = flashAlpha.value
    if (a <= 0f) return
    Canvas(modifier = Modifier.fillMaxSize()) {
        val rng = Random(seed)
        drawRect(
            color = Color.White.copy(alpha = 0.10f * a),
            topLeft = Offset.Zero,
            size = Size(size.width, size.height),
        )
        val boltCount = rng.nextInt(1, 4)
        repeat(boltCount) {
            val startX = rng.nextFloat() * size.width
            val path = Path().apply {
                moveTo(startX, 0f)
                var x = startX
                var y = 0f
                val steps = rng.nextInt(8, 14)
                val stepY = size.height / steps
                repeat(steps) {
                    x += (rng.nextFloat() - 0.5f) * size.width * 0.24f
                    x = x.coerceIn(0f, size.width)
                    y += stepY * (0.8f + rng.nextFloat() * 0.4f)
                    lineTo(x, y)
                }
            }
            drawPath(path, color = accent.copy(alpha = 0.40f * a), style = Stroke(width = 9f))
            drawPath(path, color = accent.copy(alpha = 0.65f * a), style = Stroke(width = 4f))
            drawPath(path, color = Color.White.copy(alpha = 0.95f * a), style = Stroke(width = 2f))
        }
    }
}

@Composable
private fun QuestionStage(
    chapter: QuizChapter,
    answers: List<Int>,
    onPick: (Int) -> Unit,
    onFinish: () -> Unit,
    onTypingChange: (Boolean) -> Unit = {},
) {
    val listState = rememberLazyListState()
    val typingIndex = answers.size
    val allAnswered = typingIndex >= chapter.questions.size
    val currentPrompt = chapter.questions.getOrNull(typingIndex)?.prompt ?: ""

    var typedLength by remember(typingIndex) { mutableStateOf(0) }
    val isTyping = !allAnswered && typedLength < currentPrompt.length

    LaunchedEffect(isTyping) {
        onTypingChange(isTyping)
    }

    LaunchedEffect(typingIndex) {
        if (allAnswered) return@LaunchedEffect
        typedLength = 0
        delay(260)
        val total = currentPrompt.length
        while (typedLength < total) {
            delay(22L)
            typedLength++
        }
    }

    LaunchedEffect(typingIndex, allAnswered) {
        // Scroll to latest item when a new question starts or quiz is done.
        val target = answers.size
        if (target > 0) listState.animateScrollToItem(target)
    }

    LaunchedEffect(typingIndex, isTyping, allAnswered) {
        // When typing finishes, the options expand below the question.
        // Scroll down so the options appear without the user having to drag.
        if (!allAnswered && !isTyping && currentPrompt.isNotEmpty()) {
            delay(340) // wait for AnimatedVisibility expand to start
            listState.animateScrollBy(700f)
            delay(120)
            listState.animateScrollBy(700f)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ZeusHero(accent = chapter.accent, isTyping = isTyping)
        Spacer(Modifier.height(6.dp))
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            itemsIndexed(answers, key = { i, _ -> "ans-$i" }) { idx, ansIdx ->
                val q = chapter.questions[idx]
                QAItem(
                    questionNumber = idx + 1,
                    prompt = q.prompt,
                    chosen = q.options[ansIdx],
                    accent = chapter.accent,
                )
            }
            if (!allAnswered) {
                item(key = "typing-$typingIndex") {
                    TypingQuestion(
                        questionNumber = typingIndex + 1,
                        promptFull = currentPrompt,
                        typedLength = typedLength,
                        options = chapter.questions[typingIndex].options,
                        accent = chapter.accent,
                        onPick = onPick,
                    )
                }
            } else {
                item(key = "finish") {
                    FinishCard(accent = chapter.accent, onSeeResults = onFinish)
                }
            }
        }
    }
}

@Composable
private fun QAItem(
    questionNumber: Int,
    prompt: String,
    chosen: QuizOption,
    accent: Color,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Zeus bubble
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(MythColors.SurfaceHigh.copy(alpha = 0.85f), MythColors.BgDeep)
                    )
                )
                .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .padding(14.dp),
        ) {
            Column {
                Text(
                    "ZEUS · $questionNumber",
                    color = accent,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    prompt,
                    color = MythColors.TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        // User answer bubble
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 30.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(chosen.trait.color.copy(alpha = 0.18f), MythColors.BgDeep)
                    )
                )
                .border(1.dp, chosen.trait.color.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "YOU · ${chosen.trait.display.uppercase()}",
                        color = chosen.trait.color,
                        fontSize = 9.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    chosen.fragment,
                    color = MythColors.TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

@Composable
private fun TypingQuestion(
    questionNumber: Int,
    promptFull: String,
    typedLength: Int,
    options: List<QuizOption>,
    accent: Color,
    onPick: (Int) -> Unit,
) {
    val displayed = if (typedLength >= promptFull.length) promptFull
    else promptFull.substring(0, typedLength)
    val isTyping = typedLength < promptFull.length
    // Animated caret
    val t = rememberInfiniteTransition(label = "caret")
    val caret by t.animateFloat(
        0f, 1f,
        animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
        label = "caret",
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(MythColors.SurfaceHigh.copy(alpha = 0.95f), MythColors.BgDeep)
                    )
                )
                .border(1.2.dp, accent.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                .padding(14.dp),
        ) {
            Column {
                Text(
                    "ZEUS · $questionNumber",
                    color = accent,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    buildString {
                        append(displayed)
                        if (isTyping) append(if (caret > 0.5f) "▍" else " ")
                    },
                    color = MythColors.TextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        AnimatedVisibility(
            visible = !isTyping,
            enter = fadeIn(tween(280)) + expandVertically(tween(320)),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                options.forEachIndexed { i, option ->
                    OptionCard(
                        label = option.label,
                        fragment = option.fragment,
                        trait = option.trait,
                        accent = accent,
                        onClick = { onPick(i) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FinishCard(accent: Color, onSeeResults: () -> Unit) {
    MythicCard(
        modifier = Modifier.fillMaxWidth(),
        corner = 22.dp,
        padding = PaddingValues(18.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                "THE THUNDERER IS DONE",
                color = accent,
                fontSize = 10.sp,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Your odyssey is complete.\nWill you look upon what you wrote?",
                color = MythColors.TextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
            Spacer(Modifier.height(14.dp))
            GlowingButton(
                text = "See the results",
                onClick = onSeeResults,
                style = GlowStyle.Gold,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun OptionCard(
    label: String,
    fragment: String,
    trait: Trait,
    accent: Color,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, tween(180))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        trait.color.copy(alpha = 0.18f),
                        MythColors.BgDeep,
                    )
                )
            )
            .border(1.dp, trait.color.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
            .clickable(interactionSource = interaction, indication = null) {
                pressed = true
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(trait.color.copy(alpha = 0.25f))
                    .border(1.dp, trait.color, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    trait.display.first().toString(),
                    color = trait.color,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    color = MythColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    fragment,
                    color = MythColors.TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 3,
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "›",
                color = accent,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ResultsStage(
    chapter: QuizChapter,
    answers: List<Int>,
    onReadStory: () -> Unit,
    onRetry: () -> Unit,
) {
    val tally = remember(answers) { tallyTraits(chapter, answers) }
    val dominant = remember(tally) { tally.maxByOrNull { it.value }?.key ?: Trait.Courage }
    val total = remember(tally) { tally.values.sum() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        MythicCard(corner = 22.dp, padding = PaddingValues(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "ODYSSEY COMPLETE",
                    color = MythColors.Gold,
                    fontSize = 10.sp,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "You walked the path of",
                    color = MythColors.TextSecondary,
                    fontSize = 12.sp,
                )
                Text(
                    dominant.display.uppercase(),
                    color = dominant.color,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    archetypeDescription(dominant),
                    color = MythColors.TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            "TRAIT PROFILE",
            color = MythColors.Gold,
            fontSize = 10.sp,
            letterSpacing = 3.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        Trait.values().forEach { t ->
            TraitBar(trait = t, value = tally[t] ?: 0, total = total)
            Spacer(Modifier.height(6.dp))
        }
        Spacer(Modifier.height(16.dp))
        GlowingButton(
            text = "Read your chronicle",
            onClick = onReadStory,
            style = GlowStyle.Gold,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))
        GlowingButton(
            text = "Walk the chapter again",
            onClick = onRetry,
            style = GlowStyle.Ghost,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun TraitBar(trait: Trait, value: Int, total: Int) {
    val pct = if (total == 0) 0f else value.toFloat() / total
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            trait.display.uppercase(),
            color = MythColors.TextSecondary,
            fontSize = 11.sp,
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(96.dp),
        )
        Box(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MythColors.BgDeep)
                    .border(1.dp, MythColors.DividerSoft, RoundedCornerShape(4.dp))
            )
            val progress by animateFloatAsState(pct, tween(600), label = "trait-${trait.name}")
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(trait.color.copy(alpha = 0.3f), trait.color)
                        )
                    )
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            "$value",
            color = trait.color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(28.dp),
        )
    }
}

@Composable
private fun StoryStage(
    chapter: QuizChapter,
    answers: List<Int>,
    onSave: (SavedStory) -> Unit,
    onBack: () -> Unit,
) {
    val storyText = remember(answers) { buildStory(chapter, answers) }
    val tally = remember(answers) { tallyTraits(chapter, answers) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        MythicCard(corner = 22.dp, padding = PaddingValues(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    "A CHRONICLE OF " + chapter.realm.uppercase(),
                    color = chapter.accent,
                    fontSize = 10.sp,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    chapter.title,
                    color = MythColors.TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    storyText,
                    color = MythColors.TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        GlowingButton(
            text = "Save to my chronicles",
            onClick = {
                val now = currentTimeMillis()
                val saved = SavedStory(
                    id = "${chapter.chapterId}-$now",
                    chapterId = chapter.chapterId,
                    title = chapter.title,
                    completedAtMillis = now,
                    courage = tally[Trait.Courage] ?: 0,
                    wisdom = tally[Trait.Wisdom] ?: 0,
                    cunning = tally[Trait.Cunning] ?: 0,
                    compassion = tally[Trait.Compassion] ?: 0,
                    story = storyText,
                )
                onSave(saved)
            },
            style = GlowStyle.Primary,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))
        GlowingButton(
            text = "Back to results",
            onClick = onBack,
            style = GlowStyle.Ghost,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(40.dp))
    }
}

// --------- helpers ---------

private fun tallyTraits(chapter: QuizChapter, answers: List<Int>): Map<Trait, Int> {
    val map = Trait.values().associateWith { 0 }.toMutableMap()
    answers.forEachIndexed { qIdx, optIdx ->
        val q = chapter.questions.getOrNull(qIdx) ?: return@forEachIndexed
        val opt = q.options.getOrNull(optIdx) ?: return@forEachIndexed
        map[opt.trait] = (map[opt.trait] ?: 0) + 1
    }
    return map
}

private fun buildStory(chapter: QuizChapter, answers: List<Int>): String {
    val sb = StringBuilder()
    sb.append(chapter.opening).append("\n\n")
    answers.forEachIndexed { qIdx, optIdx ->
        val q = chapter.questions.getOrNull(qIdx) ?: return@forEachIndexed
        val opt = q.options.getOrNull(optIdx) ?: return@forEachIndexed
        sb.append(opt.fragment).append(' ')
    }
    sb.append("\n\n").append(chapter.closing)
    return sb.toString()
}

private fun archetypeDescription(trait: Trait): String = when (trait) {
    Trait.Courage -> "You stepped forward where others hesitated. Your story reads like a drawn blade."
    Trait.Wisdom -> "You stopped, watched, and measured. Your story is the kind priests write down."
    Trait.Cunning -> "You read the cracks in every wall. Your story is a key turning in a forgotten lock."
    Trait.Compassion -> "You remembered the smallest voice in the room. Your story will warm someone else's hands."
}

// placeholder reference so Random import isn't unused if re-added later
@Suppress("unused")
private val seedRng = Random(0)
