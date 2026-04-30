package org.example.project.screens.journey

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.platform.clearQuizProgress
import org.example.project.platform.loadLastChapterId
import org.example.project.platform.loadQuizProgress
import org.example.project.platform.saveLastChapterId
import org.example.project.theme.MythColors
import org.example.project.ui.components.GlassRow
import org.example.project.ui.components.GlowStyle
import org.example.project.ui.components.GlowingButton
import org.example.project.ui.components.MythicCard
import org.example.project.ui.components.MythicChip
import org.example.project.ui.components.MythicProgressBar

@Composable
fun JourneyScreen() {
    var openedQuiz by remember { mutableStateOf<QuizChapter?>(null) }
    var lastChapterId by remember { mutableStateOf<String?>(null) }
    var activeProgressCount by remember { mutableStateOf(0) }
    var activeProgress by remember { mutableStateOf(emptyList<Int>()) }
    val chapters = JourneyCatalog.chapters
    val active = lastChapterId?.let { id -> chapters.firstOrNull { it.id == id } }
        ?: chapters.first { it.state == ChapterState.Active }
    val completed = chapters.count { it.state == ChapterState.Completed }
    val progress = completed.toFloat() / chapters.size

    LaunchedEffect(openedQuiz) {
        // Refresh when the quiz overlay closes so we pick up new progress / last id.
        if (openedQuiz == null) {
            lastChapterId = loadLastChapterId()
            val id = lastChapterId
            activeProgress = if (id != null) loadQuizProgress(id) else emptyList()
            activeProgressCount = activeProgress.size
        }
    }

    val openChapter: (Chapter) -> Unit = { ch ->
        val q = QuizCatalog.byId(ch.id)
        if (q != null) {
            saveLastChapterId(ch.id)
            lastChapterId = ch.id
            openedQuiz = q
        }
    }

    val restartChapter: (Chapter) -> Unit = { ch ->
        val q = QuizCatalog.byId(ch.id)
        if (q != null) {
            clearQuizProgress(ch.id)
            saveLastChapterId(ch.id)
            lastChapterId = ch.id
            activeProgress = emptyList()
            activeProgressCount = 0
            openedQuiz = q
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.height(18.dp))
        HeaderBlock(modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(14.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
        item {
            CurrentChapterCard(
                chapter = active,
                progress = progress,
                resumeCount = activeProgressCount,
                totalQuestions = 12,
                onContinue = { openChapter(active) },
                onRestart = { restartChapter(active) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Chapters", color = MythColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(
                    "$completed / ${chapters.size} done",
                    color = MythColors.TextSecondary,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }
        }
        items(chapters, key = { it.index }) { ch ->
            ChapterRow(
                chapter = ch,
                onClick = { openChapter(ch) },
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
        item {
            Text(
                "Choice Echoes",
                color = MythColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }
        val echoes = buildList {
            val quiz = lastChapterId?.let { QuizCatalog.byId(it) }
            if (quiz != null) {
                activeProgress.takeLast(4).reversed().forEachIndexed { localIdx, optIdx ->
                    val qIdx = activeProgress.size - 1 - localIdx
                    val q = quiz.questions.getOrNull(qIdx) ?: return@forEachIndexed
                    val opt = q.options.getOrNull(optIdx) ?: return@forEachIndexed
                    add(Triple(quiz.title to (qIdx + 1), opt.fragment, opt.trait))
                }
            }
        }
        if (echoes.isEmpty()) {
            item {
                GlassRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    Column {
                        Text(
                            "NO ECHOES YET",
                            color = MythColors.Gold,
                            fontSize = 10.sp,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "Walk a chapter and your choices will echo here.",
                            color = MythColors.TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }
        } else {
            items(echoes) { (header, fragment, trait) ->
                GlassRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    Column {
                        Text(
                            "${header.first.uppercase()} · Q${header.second} · ${trait.display.uppercase()}",
                            color = trait.color,
                            fontSize = 11.sp,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(fragment, color = MythColors.TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }
        }
        }
    }

    val active2 = openedQuiz
    if (active2 != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            QuizScreen(chapter = active2, onClose = { openedQuiz = null })
        }
    }
}

@Composable
private fun HeaderBlock(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "THE SAGA",
            color = MythColors.TextSecondary,
            letterSpacing = 4.sp,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "Your Mythic Journey",
            color = MythColors.TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Light,
            lineHeight = 32.sp,
            letterSpacing = 0.5.sp,
        )
    }
}

@Composable
private fun CurrentChapterCard(
    chapter: Chapter,
    progress: Float,
    resumeCount: Int,
    totalQuestions: Int,
    onContinue: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = rememberInfiniteTransition(label = "curr")
    val shine by t.animateFloat(
        0f, 1f,
        animationSpec = infiniteRepeatable(tween(3400, easing = LinearEasing), RepeatMode.Restart),
        label = "shine"
    )
    MythicCard(
        modifier = modifier.fillMaxWidth(),
        corner = 26.dp,
        padding = PaddingValues(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MythColors.AccentGradient)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("III", color = MythColors.BgAbyss, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                Spacer(Modifier.size(12.dp))
                Column {
                    Text(
                        "CHAPTER ${chapter.index}",
                        color = MythColors.Gold,
                        letterSpacing = 2.sp,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        chapter.title,
                        color = MythColors.TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (resumeCount in 1 until totalQuestions) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Resume · $resumeCount / $totalQuestions answered",
                            color = MythColors.CyanBright,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MythColors.BgDeep,
                                MythColors.SurfaceGlow.copy(alpha = 0.6f + 0.4f * shine),
                                MythColors.BgDeep,
                            )
                        )
                    )
                    .border(1.dp, MythColors.DividerSoft, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Text(
                    chapter.synopsis,
                    color = MythColors.TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Saga progress", color = MythColors.TextSecondary, fontSize = 11.sp, letterSpacing = 1.sp)
                    Spacer(Modifier.height(4.dp))
                    Box(modifier = Modifier.size(width = 160.dp, height = 8.dp)) {
                        MythicProgressBar(progress = progress)
                    }
                }
                Text("~${chapter.durationMinutes} min", color = MythColors.CyanBright, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlowingButton(text = "Continue", onClick = onContinue)
                GlowingButton(text = "Restart", onClick = onRestart, style = GlowStyle.Ghost)
            }
        }
    }
}

@Composable
private fun ChapterRow(chapter: Chapter, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val accent by animateColorAsState(
        when (chapter.state) {
            ChapterState.Completed -> MythColors.Emerald
            ChapterState.Active -> MythColors.CyanBright
            ChapterState.Locked -> MythColors.TextMuted
        },
        animationSpec = tween(400)
    )
    val badge = when (chapter.state) {
        ChapterState.Completed -> "✓"
        ChapterState.Active -> "▶"
        ChapterState.Locked -> "⚿"
    }
    MythicCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        corner = 20.dp,
        padding = PaddingValues(14.dp),
        glow = chapter.state == ChapterState.Active
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.2f))
                    .border(1.dp, accent, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text(badge, color = accent, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.fillMaxWidth(0.72f)) {
                Text(
                    "Chapter ${chapter.index}",
                    color = MythColors.TextSecondary,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    chapter.title,
                    color = MythColors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    chapter.synopsis,
                    color = MythColors.TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                )
            }
            Spacer(Modifier.size(6.dp))
            Text(
                "${chapter.durationMinutes}m",
                color = accent,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}
