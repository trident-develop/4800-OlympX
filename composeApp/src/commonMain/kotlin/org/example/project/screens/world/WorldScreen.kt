package org.example.project.screens.world

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.QuitGame
import org.example.project.platform.loadVisitedPlaces
import org.example.project.platform.saveVisitedPlaces
import org.example.project.theme.MythColors
import org.example.project.ui.components.MythicCard
import org.example.project.ui.components.MythicDialog
import org.example.project.ui.components.MythicProgressBar
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WorldScreen() {
    QuitGame()

    val realms = WorldCatalog.realms
    val pagerState = rememberPagerState(pageCount = { realms.size })
    var selected by remember { mutableStateOf<HistoricPlace?>(null) }
    var selectedCharacter by remember { mutableStateOf<HistoricCharacter?>(null) }
    var compareTarget by remember { mutableStateOf<HistoricCharacter?>(null) }
    var pickerOpen by remember { mutableStateOf(false) }
    val visited = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        val saved = loadVisitedPlaces()
        if (saved.isNotEmpty()) {
            visited.clear()
            visited.addAll(saved)
        }
        snapshotFlow { visited.toList() }
            .collect { saveVisitedPlaces(it.toSet()) }
    }

    val currentRealm by remember { derivedStateOf { realms[pagerState.currentPage] } }
    val totalPlaces = remember { realms.sumOf { it.places.size } }
    val progress = visited.size.toFloat() / totalPlaces

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.height(18.dp))
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "THE OLD WORLD",
                color = MythColors.TextSecondary,
                letterSpacing = 4.sp,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Maps of Antiquity",
                color = MythColors.TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                lineHeight = 32.sp,
                letterSpacing = 0.5.sp,
            )
        }
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                DiscoveryBar(
                    discovered = visited.size,
                    total = totalPlaces,
                    progress = progress,
                    accent = currentRealm.accent,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            item {
                RealmHeadline(realm = currentRealm, modifier = Modifier.padding(horizontal = 20.dp))
            }
            item {
                Box(modifier = Modifier.fillMaxWidth().height(340.dp)) {
                    HorizontalPager(state = pagerState) { page ->
                        val realm = realms[page]
                        RealmMapView(
                            realm = realm,
                            visited = visited,
                            onSelectPlace = { place ->
                                if (place.id !in visited) visited.add(place.id)
                                selected = place
                            },
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                }
            }
            item {
                PagerDots(
                    count = realms.size,
                    current = pagerState.currentPage,
                    accent = currentRealm.accent,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Places of memory",
                        color = MythColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )
                    val realmVisited = currentRealm.places.count { it.id in visited }
                    Text(
                        text = "$realmVisited / ${currentRealm.places.size} uncovered",
                        color = MythColors.TextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                    )
                }
            }
            items(currentRealm.places, key = { it.id }) { place ->
                PlaceRow(
                    place = place,
                    visited = place.id in visited,
                    onOpen = {
                        if (place.id !in visited) visited.add(place.id)
                        selected = place
                    },
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            item {
                ChronicleScroll(
                    visited = visited,
                    realms = realms,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
    }

    PlaceDetailDialog(
        place = selected,
        onClose = { selected = null },
        onCharacterTap = { c -> selectedCharacter = c },
    )

    CharacterDetailDialog(
        character = selectedCharacter,
        onClose = { selectedCharacter = null },
        onCompare = { pickerOpen = true },
    )

    CharacterPickerDialog(
        visible = pickerOpen,
        excludeId = selectedCharacter?.id,
        onPick = { c ->
            compareTarget = c
            pickerOpen = false
        },
        onClose = { pickerOpen = false },
    )

    CharacterCompareDialog(
        left = selectedCharacter,
        right = compareTarget,
        onClose = { compareTarget = null },
    )
}

@Composable
private fun DiscoveryBar(
    discovered: Int,
    total: Int,
    progress: Float,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    MythicCard(modifier = modifier.fillMaxWidth(), corner = 18.dp, padding = PaddingValues(14.dp)) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "CHRONICLE",
                        color = MythColors.Gold,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Uncovered $discovered of $total legends",
                        color = MythColors.TextPrimary,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp,
                    )
                }
                Text(
                    "${(progress * 100).toInt()}%",
                    color = accent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            }
            Spacer(Modifier.height(10.dp))
            MythicProgressBar(progress = progress)
        }
    }
}

@Composable
private fun RealmHeadline(realm: AncientRealm, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = realm,
        transitionSpec = {
            (fadeIn(tween(260)).togetherWith(fadeOut(tween(180))))
        },
        label = "realm-headline",
        modifier = modifier.fillMaxWidth(),
    ) { r ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(modifier = Modifier.padding(end = 12.dp)) {
                Text(
                    r.era.uppercase(),
                    color = r.accent,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    r.name,
                    color = MythColors.TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                )
                Text(
                    r.tagline,
                    color = MythColors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }
            Text(
                text = "◂ swipe ▸",
                color = MythColors.TextMuted,
                fontSize = 10.sp,
                letterSpacing = 1.5.sp,
            )
        }
    }
}

@Composable
private fun PagerDots(
    count: Int,
    current: Int,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(count) { i ->
            val active = i == current
            val width by animateFloatAsState(if (active) 22f else 8f, tween(260))
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = width.dp, height = 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) accent else MythColors.DividerSoft,
                    )
            )
        }
    }
}

@Composable
private fun RealmMapView(
    realm: AncientRealm,
    visited: List<String>,
    onSelectPlace: (HistoricPlace) -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = rememberInfiniteTransition(label = "map-${realm.id}")
    val breath by t.animateFloat(
        0.55f, 1f,
        animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing), RepeatMode.Reverse),
        label = "breath"
    )
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        realm.accent.copy(alpha = 0.10f),
                        MythColors.BgDeep,
                        MythColors.BgAbyss,
                    )
                )
            )
            .border(1.dp, realm.accent.copy(alpha = 0.35f), shape)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRealmMap(realm.mapStyle, realm.accent, breath)
        }
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            val w = maxWidth
            val h = maxHeight
            realm.places.forEach { place ->
                val pinSize = 44.dp
                val half = pinSize / 2
                val visitedFlag = place.id in visited
                PlacePin(
                    place = place,
                    visited = visitedFlag,
                    onClick = { onSelectPlace(place) },
                    modifier = Modifier
                        .offset(
                            x = w * place.relPos.x - half,
                            y = h * place.relPos.y - half,
                        )
                        .size(pinSize)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MythColors.BgAbyss.copy(alpha = 0.55f))
                .border(1.dp, realm.accent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                realm.name.uppercase(),
                color = realm.accent,
                fontSize = 10.sp,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp)
                .size(44.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCompass(realm.accent)
            }
        }
    }
}

@Composable
private fun PlacePin(
    place: HistoricPlace,
    visited: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val t = rememberInfiniteTransition(label = "pin-${place.id}")
    val pulse by t.animateFloat(
        0.7f, 1.1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier.clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(place.accent.copy(alpha = 0.55f * pulse), Color.Transparent)
                ),
                radius = size.width / 2 * pulse,
            )
        }
        Box(
            modifier = Modifier
                .size(26.dp)
                .scale(if (visited) 1f else pulse.coerceIn(0.9f, 1.08f))
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            place.accent.copy(alpha = if (visited) 1f else 0.85f),
                            MythColors.BgAbyss,
                        )
                    )
                )
                .border(1.2.dp, place.accent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                place.type.glyph,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        if (visited) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MythColors.Emerald)
                    .border(1.dp, MythColors.BgAbyss, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("✓", color = MythColors.BgAbyss, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PlaceRow(
    place: HistoricPlace,
    visited: Boolean,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    MythicCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(interactionSource = interaction, indication = null, onClick = onOpen),
        corner = 20.dp,
        padding = PaddingValues(14.dp),
        onClick = null,
        glow = visited,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(place.accent.copy(alpha = 0.75f), MythColors.BgDeep)
                        )
                    )
                    .border(1.dp, place.accent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(place.type.glyph, color = Color.White, fontSize = 18.sp)
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.fillMaxWidth(0.78f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        place.name,
                        color = MythColors.TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (visited) {
                        Spacer(Modifier.size(6.dp))
                        Text(
                            "UNCOVERED",
                            color = MythColors.Emerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            letterSpacing = 1.2.sp,
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    "${place.type.label} · ${place.era}",
                    color = MythColors.TextSecondary,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    place.synopsis,
                    color = MythColors.TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                )
            }
            Spacer(Modifier.size(4.dp))
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(place.accent.copy(alpha = 0.18f))
                    .border(1.dp, place.accent.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text("›", color = place.accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ChronicleScroll(
    visited: List<String>,
    realms: List<AncientRealm>,
    modifier: Modifier = Modifier,
) {
    if (visited.isEmpty()) return
    var expanded by remember { mutableStateOf(false) }
    val arrowAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(260),
        label = "chronicle-arrow",
    )
    val recent = remember(visited) {
        visited.takeLast(8).reversed().mapNotNull { id ->
            realms.flatMap { r -> r.places.map { r to it } }.firstOrNull { it.second.id == id }
        }
    }
    val interaction = remember { MutableInteractionSource() }
    MythicCard(
        modifier = modifier.fillMaxWidth(),
        corner = 22.dp,
        padding = PaddingValues(16.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(interactionSource = interaction, indication = null) { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "CHRONICLE SCROLL",
                        color = MythColors.Gold,
                        fontSize = 10.sp,
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (expanded) "Last footprints across the maps"
                        else "${recent.size} footprints · tap to unroll",
                        color = MythColors.TextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MythColors.Gold.copy(alpha = 0.15f))
                        .border(1.dp, MythColors.Gold.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "▾",
                        color = MythColors.Gold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.rotate(arrowAngle),
                    )
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(200)) + expandVertically(tween(260)),
                exit = fadeOut(tween(160)) + shrinkVertically(tween(200)),
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    recent.forEach { (realm, place) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(place.accent.copy(alpha = 0.25f))
                                    .border(1.dp, place.accent.copy(alpha = 0.7f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(place.type.glyph, color = place.accent, fontSize = 12.sp)
                            }
                            Spacer(Modifier.size(10.dp))
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    place.name,
                                    color = MythColors.TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    "${realm.name} · ${place.era}",
                                    color = MythColors.TextMuted,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceDetailDialog(
    place: HistoricPlace?,
    onClose: () -> Unit,
    onCharacterTap: (HistoricCharacter) -> Unit,
) {
    MythicDialog(
        visible = place != null,
        onDismiss = onClose,
        title = place?.name ?: "",
        dismissLabel = "Close",
        dismissOnScrimClick = false,
        content = {
            val p = place ?: return@MythicDialog
            val scroll = rememberScrollState()
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                // BoxWithConstraints reports the actual bounded height that
                // this slot was given by MythicCard's inner Column — already
                // accounts for system insets, bottom bar and card padding.
                // Subtract an estimate for the title (~30dp), the two spacers
                // around the content (~32dp) and the DialogActions row (~56dp).
                val maxContent = (maxHeight - 140.dp).coerceIn(240.dp, 700.dp)
                Column(
                    modifier = Modifier
                        .heightIn(max = maxContent)
                        .verticalScroll(scroll),
                ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(p.accent.copy(alpha = 0.85f), MythColors.BgDeep)
                                )
                            )
                            .border(1.dp, p.accent, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(p.type.glyph, color = Color.White, fontSize = 20.sp)
                    }
                    Spacer(Modifier.size(10.dp))
                    Column {
                        Text(
                            "${p.type.label.uppercase()} · ${p.era}",
                            color = p.accent,
                            fontSize = 10.sp,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            p.synopsis,
                            color = MythColors.TextPrimary,
                            fontSize = 13.sp,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(p.accent.copy(alpha = 0.10f))
                        .border(1.dp, p.accent.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        p.story,
                        color = MythColors.TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "STATISTICA",
                    color = MythColors.Gold,
                    fontSize = 10.sp,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    p.stats.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            row.forEach { stat ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MythColors.BgDeep)
                                        .border(1.dp, p.accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 10.dp)
                                ) {
                                    Column {
                                        Text(
                                            stat.label.uppercase(),
                                            color = MythColors.TextMuted,
                                            fontSize = 8.sp,
                                            letterSpacing = 1.2.sp,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        Spacer(Modifier.height(3.dp))
                                        Text(
                                            stat.value,
                                            color = p.accent,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                            repeat(3 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "FIGURES OF THE AGE",
                    color = MythColors.Gold,
                    fontSize = 10.sp,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                p.characters.forEach { c ->
                    val interaction = remember(c.id) { MutableInteractionSource() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MythColors.Surface.copy(alpha = 0.55f))
                            .border(1.dp, c.accent.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .clickable(interactionSource = interaction, indication = null) { onCharacterTap(c) }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(c.accent.copy(alpha = 0.85f), MythColors.BgDeep)
                                    )
                                )
                                .border(1.dp, c.accent, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(c.glyph, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.size(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                c.name,
                                color = MythColors.TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                c.role,
                                color = c.accent,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp,
                            )
                        }
                        Text("›", color = c.accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                }
            }
        }
    )
}

// ---------- Character dialogs ----------

@Composable
private fun CharacterDetailDialog(
    character: HistoricCharacter?,
    onClose: () -> Unit,
    onCompare: () -> Unit,
) {
    MythicDialog(
        visible = character != null,
        onDismiss = onClose,
        title = character?.name ?: "",
        dismissLabel = "Close",
        confirmLabel = "Compare",
        onConfirm = onCompare,
        dismissOnScrimClick = false,
        content = {
            val c = character ?: return@MythicDialog
            val realm = remember(c.id) { WorldCatalog.findRealmOf(c.id) }
            val charScroll = rememberScrollState()
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val maxContent = (maxHeight - 140.dp).coerceIn(240.dp, 700.dp)
                Column(
                    modifier = Modifier
                        .heightIn(max = maxContent)
                        .verticalScroll(charScroll),
                ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(c.accent.copy(alpha = 0.9f), MythColors.BgDeep)
                                )
                            )
                            .border(1.5.dp, c.accent, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(c.glyph, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Text(
                            c.role.uppercase(),
                            color = c.accent,
                            fontSize = 10.sp,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "\"${c.epithet}\"",
                            color = MythColors.TextPrimary,
                            fontSize = 13.sp,
                        )
                        if (realm != null) {
                            Text(
                                "From ${realm.name} · ${realm.era}",
                                color = MythColors.TextMuted,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "TRAITS",
                    color = MythColors.Gold,
                    fontSize = 10.sp,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                TraitBars(character = c, accent = c.accent)
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MythColors.BgDeep)
                        .border(1.dp, c.accent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "LEGEND TOTAL",
                                color = MythColors.TextMuted,
                                fontSize = 9.sp,
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                "${c.total} / 500",
                                color = c.accent,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Text(
                            tier(c.total),
                            color = c.accent,
                            fontSize = 12.sp,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                }
            }
        }
    )
}

@Composable
private fun CharacterPickerDialog(
    visible: Boolean,
    excludeId: String?,
    onPick: (HistoricCharacter) -> Unit,
    onClose: () -> Unit,
) {
    val all = remember { WorldCatalog.allCharacters }
    val shown = remember(excludeId) { all.filter { it.id != excludeId } }
    MythicDialog(
        visible = visible,
        onDismiss = onClose,
        title = "Compare with…",
        dismissLabel = "Close",
        dismissOnScrimClick = false,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(shown, key = { it.id }) { c ->
                        val interaction = remember(c.id) { MutableInteractionSource() }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MythColors.Surface.copy(alpha = 0.45f))
                                .border(1.dp, c.accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                .clickable(interactionSource = interaction, indication = null) { onPick(c) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(c.accent.copy(alpha = 0.85f), MythColors.BgDeep)
                                        )
                                    )
                                    .border(1.dp, c.accent, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(c.glyph, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.size(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    c.name,
                                    color = MythColors.TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    c.role,
                                    color = c.accent,
                                    fontSize = 10.sp,
                                )
                            }
                            Text(
                                "${c.total}",
                                color = c.accent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun CharacterCompareDialog(
    left: HistoricCharacter?,
    right: HistoricCharacter?,
    onClose: () -> Unit,
) {
    MythicDialog(
        visible = left != null && right != null,
        onDismiss = onClose,
        title = "Side by side",
        dismissLabel = "Close",
        dismissOnScrimClick = false,
        content = {
            val a = left ?: return@MythicDialog
            val b = right ?: return@MythicDialog
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CompareHeader(c = a, modifier = Modifier.weight(1f))
                    CompareHeader(c = b, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(14.dp))
                listOf(
                    Triple("Might", a.might, b.might),
                    Triple("Wisdom", a.wisdom, b.wisdom),
                    Triple("Renown", a.renown, b.renown),
                    Triple("Cunning", a.cunning, b.cunning),
                    Triple("Divinity", a.divinity, b.divinity),
                ).forEach { (label, va, vb) ->
                    CompareRow(
                        label = label,
                        leftValue = va,
                        rightValue = vb,
                        leftAccent = a.accent,
                        rightAccent = b.accent,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MythColors.BgDeep)
                        .border(1.dp, MythColors.DividerSoft, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "VERDICT",
                            color = MythColors.Gold,
                            fontSize = 10.sp,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        val winner = when {
                            a.total > b.total -> a
                            b.total > a.total -> b
                            else -> null
                        }
                        Text(
                            when (winner) {
                                null -> "Stalemate · ${a.total} = ${b.total}"
                                else -> "${winner.name} prevails · ${winner.total} vs ${if (winner == a) b.total else a.total}"
                            },
                            color = winner?.accent ?: MythColors.TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun CompareHeader(c: HistoricCharacter, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    listOf(c.accent.copy(alpha = 0.18f), MythColors.BgDeep)
                )
            )
            .border(1.dp, c.accent.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(c.accent.copy(alpha = 0.9f), MythColors.BgDeep)
                    )
                )
                .border(1.2.dp, c.accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(c.glyph, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            c.name,
            color = MythColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            c.role,
            color = c.accent,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp,
        )
    }
}

@Composable
private fun CompareRow(
    label: String,
    leftValue: Int,
    rightValue: Int,
    leftAccent: Color,
    rightAccent: Color,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "$leftValue",
                color = if (leftValue >= rightValue) leftAccent else MythColors.TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                label.uppercase(),
                color = MythColors.TextSecondary,
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "$rightValue",
                color = if (rightValue >= leftValue) rightAccent else MythColors.TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Left side — bar grows leftwards from the centre
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(leftValue / 100f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(leftAccent.copy(alpha = 0.15f), leftAccent)
                            )
                        )
                )
            }
            Spacer(modifier = Modifier.size(6.dp))
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(rightValue / 100f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(rightAccent, rightAccent.copy(alpha = 0.15f))
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun TraitBars(character: HistoricCharacter, accent: Color) {
    val traits = listOf(
        "Might" to character.might,
        "Wisdom" to character.wisdom,
        "Renown" to character.renown,
        "Cunning" to character.cunning,
        "Divinity" to character.divinity,
    )
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        traits.forEach { (label, value) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    label.uppercase(),
                    color = MythColors.TextSecondary,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(76.dp),
                )
                Box(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MythColors.BgDeep)
                            .border(1.dp, MythColors.DividerSoft, RoundedCornerShape(3.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(value / 100f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(accent.copy(alpha = 0.3f), accent)
                                )
                            )
                    )
                }
                Spacer(Modifier.size(8.dp))
                Text(
                    value.toString().padStart(3),
                    color = accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private fun tier(total: Int): String = when {
    total >= 430 -> "MYTHIC"
    total >= 370 -> "LEGENDARY"
    total >= 300 -> "HEROIC"
    total >= 220 -> "NOTABLE"
    else -> "MORTAL"
}

// ---------- Map drawing ----------

private fun DrawScope.drawRealmMap(style: MapStyle, accent: Color, breath: Float) {
    val w = size.width
    val h = size.height

    // Decorative grid (latitudes / longitudes)
    val gridColor = MythColors.DividerSoft.copy(alpha = 0.18f)
    for (i in 1..5) {
        drawLine(gridColor, Offset(0f, h * i / 6f), Offset(w, h * i / 6f), 0.6f)
        drawLine(gridColor, Offset(w * i / 6f, 0f), Offset(w * i / 6f, h), 0.6f)
    }

    // Pulse halo in water
    drawCircle(
        brush = Brush.radialGradient(
            listOf(accent.copy(alpha = 0.08f * breath), Color.Transparent),
            center = Offset(w * 0.5f, h * 0.5f),
            radius = w * 0.7f,
        ),
        radius = w * 0.7f,
        center = Offset(w * 0.5f, h * 0.5f),
    )

    when (style) {
        MapStyle.Hellas -> drawHellas(w, h, accent)
        MapStyle.Kemet -> drawKemet(w, h, accent)
        MapStyle.Skandia -> drawSkandia(w, h, accent)
        MapStyle.Albion -> drawAlbion(w, h, accent)
        MapStyle.Persis -> drawPersis(w, h, accent)
        MapStyle.Shinar -> drawShinar(w, h, accent)
        MapStyle.Zhongguo -> drawZhongguo(w, h, accent)
        MapStyle.Mayab -> drawMayab(w, h, accent)
        MapStyle.Latium -> drawLatium(w, h, accent)
    }
}

private val LandBrush: Brush
    get() = Brush.verticalGradient(
        listOf(MythColors.SurfaceHigh, MythColors.Surface, MythColors.BgMid)
    )

private fun DrawScope.drawLand(path: Path, accent: Color, fill: Boolean = true) {
    if (fill) drawPath(path, brush = LandBrush)
    drawPath(path, color = accent.copy(alpha = 0.85f), style = Stroke(1.4f))
    drawPath(
        path,
        color = accent.copy(alpha = 0.25f),
        style = Stroke(3.5f, pathEffect = PathEffect.cornerPathEffect(8f))
    )
}

private fun DrawScope.drawHellas(w: Float, h: Float, accent: Color) {
    val main = Path().apply {
        moveTo(w * 0.20f, h * 0.08f)
        lineTo(w * 0.45f, h * 0.06f)
        lineTo(w * 0.58f, h * 0.14f)
        lineTo(w * 0.52f, h * 0.28f)
        lineTo(w * 0.42f, h * 0.34f)
        lineTo(w * 0.48f, h * 0.44f)
        lineTo(w * 0.66f, h * 0.50f)
        lineTo(w * 0.72f, h * 0.60f)
        lineTo(w * 0.64f, h * 0.70f)
        lineTo(w * 0.50f, h * 0.66f)
        lineTo(w * 0.42f, h * 0.58f)
        lineTo(w * 0.30f, h * 0.52f)
        lineTo(w * 0.22f, h * 0.40f)
        lineTo(w * 0.18f, h * 0.22f)
        close()
    }
    drawLand(main, accent)

    val anatolia = Path().apply {
        moveTo(w * 0.76f, h * 0.18f)
        lineTo(w * 0.96f, h * 0.15f)
        lineTo(w * 0.96f, h * 0.40f)
        lineTo(w * 0.80f, h * 0.42f)
        lineTo(w * 0.72f, h * 0.32f)
        close()
    }
    drawLand(anatolia, accent)

    val crete = Path().apply {
        moveTo(w * 0.40f, h * 0.82f)
        lineTo(w * 0.72f, h * 0.83f)
        lineTo(w * 0.76f, h * 0.90f)
        lineTo(w * 0.42f, h * 0.90f)
        close()
    }
    drawLand(crete, accent)

    // Small islands scatter
    listOf(
        Offset(w * 0.70f, h * 0.32f) to w * 0.02f,
        Offset(w * 0.66f, h * 0.22f) to w * 0.015f,
        Offset(w * 0.80f, h * 0.62f) to w * 0.02f,
        Offset(w * 0.22f, h * 0.68f) to w * 0.018f,
    ).forEach { (c, r) ->
        drawCircle(MythColors.Surface, radius = r, center = c)
        drawCircle(accent.copy(alpha = 0.7f), radius = r, center = c, style = Stroke(1f))
    }
}

private fun DrawScope.drawKemet(w: Float, h: Float, accent: Color) {
    val land = Path().apply {
        moveTo(w * 0.18f, h * 0.04f)
        lineTo(w * 0.82f, h * 0.04f)
        lineTo(w * 0.78f, h * 0.20f)
        lineTo(w * 0.68f, h * 0.30f)
        lineTo(w * 0.70f, h * 0.48f)
        lineTo(w * 0.62f, h * 0.66f)
        lineTo(w * 0.64f, h * 0.86f)
        lineTo(w * 0.58f, h * 0.96f)
        lineTo(w * 0.42f, h * 0.96f)
        lineTo(w * 0.42f, h * 0.70f)
        lineTo(w * 0.38f, h * 0.50f)
        lineTo(w * 0.30f, h * 0.32f)
        lineTo(w * 0.22f, h * 0.18f)
        close()
    }
    drawLand(land, accent)

    // Nile
    val nile = Path().apply {
        moveTo(w * 0.50f, h * 0.06f)
        lineTo(w * 0.52f, h * 0.20f)
        lineTo(w * 0.48f, h * 0.34f)
        lineTo(w * 0.52f, h * 0.50f)
        lineTo(w * 0.50f, h * 0.68f)
        lineTo(w * 0.52f, h * 0.84f)
        lineTo(w * 0.50f, h * 0.96f)
    }
    drawPath(nile, color = MythColors.CyanBright.copy(alpha = 0.85f), style = Stroke(2.4f))
    drawPath(nile, color = MythColors.CyanBright.copy(alpha = 0.25f), style = Stroke(6f))

    // Delta fan
    val delta = Path().apply {
        moveTo(w * 0.50f, h * 0.04f)
        lineTo(w * 0.30f, h * 0.12f)
        moveTo(w * 0.50f, h * 0.04f)
        lineTo(w * 0.40f, h * 0.16f)
        moveTo(w * 0.50f, h * 0.04f)
        lineTo(w * 0.58f, h * 0.18f)
        moveTo(w * 0.50f, h * 0.04f)
        lineTo(w * 0.68f, h * 0.12f)
    }
    drawPath(delta, color = MythColors.CyanBright.copy(alpha = 0.6f), style = Stroke(1.4f))

    // Dunes texture — arcs on the east
    for (i in 0 until 8) {
        val y = h * (0.14f + i * 0.095f)
        drawArc(
            color = MythColors.Gold.copy(alpha = 0.25f),
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(w * 0.70f, y),
            size = Size(w * 0.08f, h * 0.05f),
            style = Stroke(0.8f),
        )
    }
}

private fun DrawScope.drawSkandia(w: Float, h: Float, accent: Color) {
    // Fjord-jagged land on the right
    val main = Path().apply {
        moveTo(w * 0.96f, h * 0.02f)
        var x = 0.96f
        var y = 0.02f
        val jags = 9
        repeat(jags) {
            x -= 0.03f
            y += 1f / jags * 0.96f
            lineTo(w * x, h * y)
            x += 0.04f
            y += 0.02f
            lineTo(w * x, h * y)
        }
        lineTo(w * 0.96f, h * 0.98f)
        close()
    }
    drawLand(main, accent)

    // Inner mountain ridge
    val ridge = Path().apply {
        moveTo(w * 0.80f, h * 0.10f)
        lineTo(w * 0.72f, h * 0.30f)
        lineTo(w * 0.78f, h * 0.50f)
        lineTo(w * 0.70f, h * 0.70f)
        lineTo(w * 0.82f, h * 0.90f)
    }
    drawPath(ridge, color = accent.copy(alpha = 0.55f), style = Stroke(1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))))

    // Jutland + Danish isles on the left
    val jut = Path().apply {
        moveTo(w * 0.28f, h * 0.64f)
        lineTo(w * 0.44f, h * 0.68f)
        lineTo(w * 0.46f, h * 0.86f)
        lineTo(w * 0.30f, h * 0.88f)
        lineTo(w * 0.24f, h * 0.78f)
        close()
    }
    drawLand(jut, accent)

    listOf(
        Offset(w * 0.48f, h * 0.90f) to w * 0.022f,
        Offset(w * 0.54f, h * 0.80f) to w * 0.018f,
        Offset(w * 0.18f, h * 0.72f) to w * 0.014f,
    ).forEach { (c, r) ->
        drawCircle(MythColors.Surface, radius = r, center = c)
        drawCircle(accent.copy(alpha = 0.7f), radius = r, center = c, style = Stroke(1f))
    }
}

private fun DrawScope.drawAlbion(w: Float, h: Float, accent: Color) {
    val main = Path().apply {
        moveTo(w * 0.35f, h * 0.08f)
        lineTo(w * 0.60f, h * 0.06f)
        lineTo(w * 0.72f, h * 0.18f)
        lineTo(w * 0.80f, h * 0.34f)
        lineTo(w * 0.74f, h * 0.52f)
        lineTo(w * 0.66f, h * 0.68f)
        lineTo(w * 0.50f, h * 0.82f)
        lineTo(w * 0.34f, h * 0.78f)
        lineTo(w * 0.26f, h * 0.60f)
        lineTo(w * 0.28f, h * 0.38f)
        lineTo(w * 0.30f, h * 0.22f)
        close()
    }
    drawLand(main, accent)

    // Ireland
    val eire = Path().apply {
        moveTo(w * 0.08f, h * 0.26f)
        lineTo(w * 0.20f, h * 0.22f)
        lineTo(w * 0.22f, h * 0.48f)
        lineTo(w * 0.14f, h * 0.58f)
        lineTo(w * 0.06f, h * 0.50f)
        close()
    }
    drawLand(eire, accent)

    // Inner contour rings
    val contour = Path().apply {
        moveTo(w * 0.48f, h * 0.40f)
        lineTo(w * 0.58f, h * 0.46f)
        lineTo(w * 0.56f, h * 0.60f)
        lineTo(w * 0.40f, h * 0.60f)
        lineTo(w * 0.40f, h * 0.46f)
        close()
    }
    drawPath(
        contour,
        color = accent.copy(alpha = 0.4f),
        style = Stroke(1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f)))
    )

    // Stones markers
    for (i in 0 until 5) {
        val a = i.toFloat() / 5f * 2f * PI.toFloat()
        val c = Offset(w * 0.48f + cos(a) * w * 0.05f, h * 0.54f + sin(a) * w * 0.05f)
        drawCircle(accent.copy(alpha = 0.7f), radius = 2f, center = c)
    }
}

private fun DrawScope.drawPersis(w: Float, h: Float, accent: Color) {
    // Plateau — broad rectangular mass with mountain spines
    val main = Path().apply {
        moveTo(w * 0.10f, h * 0.12f)
        lineTo(w * 0.36f, h * 0.06f)
        lineTo(w * 0.62f, h * 0.08f)
        lineTo(w * 0.86f, h * 0.14f)
        lineTo(w * 0.94f, h * 0.34f)
        lineTo(w * 0.90f, h * 0.58f)
        lineTo(w * 0.78f, h * 0.78f)
        lineTo(w * 0.54f, h * 0.88f)
        lineTo(w * 0.30f, h * 0.82f)
        lineTo(w * 0.14f, h * 0.66f)
        lineTo(w * 0.08f, h * 0.42f)
        close()
    }
    drawLand(main, accent)

    // Zagros mountain chain
    val zagros = Path().apply {
        moveTo(w * 0.18f, h * 0.22f)
        lineTo(w * 0.32f, h * 0.30f)
        lineTo(w * 0.44f, h * 0.42f)
        lineTo(w * 0.58f, h * 0.54f)
        lineTo(w * 0.70f, h * 0.66f)
    }
    drawPath(
        zagros,
        color = accent.copy(alpha = 0.65f),
        style = Stroke(1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))),
    )
    // Mountain peaks
    for (i in 0 until 5) {
        val cx = w * (0.24f + i * 0.11f)
        val cy = h * (0.28f + i * 0.09f)
        val tri = Path().apply {
            moveTo(cx, cy - h * 0.025f)
            lineTo(cx - w * 0.02f, cy + h * 0.01f)
            lineTo(cx + w * 0.02f, cy + h * 0.01f)
            close()
        }
        drawPath(tri, color = accent.copy(alpha = 0.4f))
    }
    // Persian gulf (southwest)
    drawArc(
        color = MythColors.CyanBright.copy(alpha = 0.4f),
        startAngle = 210f,
        sweepAngle = 120f,
        useCenter = false,
        topLeft = Offset(-w * 0.1f, h * 0.5f),
        size = Size(w * 0.45f, h * 0.55f),
        style = Stroke(1.5f),
    )
}

private fun DrawScope.drawShinar(w: Float, h: Float, accent: Color) {
    // Mesopotamia — land between twin rivers
    val land = Path().apply {
        moveTo(w * 0.22f, h * 0.04f)
        lineTo(w * 0.82f, h * 0.06f)
        lineTo(w * 0.78f, h * 0.40f)
        lineTo(w * 0.72f, h * 0.66f)
        lineTo(w * 0.58f, h * 0.88f)
        lineTo(w * 0.44f, h * 0.94f)
        lineTo(w * 0.32f, h * 0.82f)
        lineTo(w * 0.26f, h * 0.56f)
        lineTo(w * 0.22f, h * 0.28f)
        close()
    }
    drawLand(land, accent)

    // Tigris (east)
    val tigris = Path().apply {
        moveTo(w * 0.68f, h * 0.10f)
        lineTo(w * 0.64f, h * 0.30f)
        lineTo(w * 0.60f, h * 0.52f)
        lineTo(w * 0.54f, h * 0.76f)
        lineTo(w * 0.50f, h * 0.92f)
    }
    drawPath(tigris, color = MythColors.CyanBright.copy(alpha = 0.85f), style = Stroke(2f))
    drawPath(tigris, color = MythColors.CyanBright.copy(alpha = 0.22f), style = Stroke(6f))

    // Euphrates (west)
    val euph = Path().apply {
        moveTo(w * 0.34f, h * 0.08f)
        lineTo(w * 0.36f, h * 0.30f)
        lineTo(w * 0.42f, h * 0.54f)
        lineTo(w * 0.46f, h * 0.78f)
        lineTo(w * 0.50f, h * 0.92f)
    }
    drawPath(euph, color = MythColors.CyanBright.copy(alpha = 0.85f), style = Stroke(2f))
    drawPath(euph, color = MythColors.CyanBright.copy(alpha = 0.22f), style = Stroke(6f))

    // Ziggurat glyphs scattered
    for (i in 0 until 4) {
        val cx = w * (0.30f + i * 0.12f)
        val cy = h * (0.20f + (i % 2) * 0.30f)
        val step = Path().apply {
            moveTo(cx - w * 0.03f, cy + h * 0.015f)
            lineTo(cx + w * 0.03f, cy + h * 0.015f)
            lineTo(cx + w * 0.022f, cy + h * 0.005f)
            lineTo(cx + w * 0.014f, cy - h * 0.005f)
            lineTo(cx, cy - h * 0.018f)
            lineTo(cx - w * 0.014f, cy - h * 0.005f)
            lineTo(cx - w * 0.022f, cy + h * 0.005f)
            close()
        }
        drawPath(step, color = accent.copy(alpha = 0.35f))
    }
}

private fun DrawScope.drawZhongguo(w: Float, h: Float, accent: Color) {
    // Broad land
    val land = Path().apply {
        moveTo(w * 0.12f, h * 0.16f)
        lineTo(w * 0.32f, h * 0.08f)
        lineTo(w * 0.60f, h * 0.06f)
        lineTo(w * 0.84f, h * 0.10f)
        lineTo(w * 0.96f, h * 0.26f)
        lineTo(w * 0.94f, h * 0.54f)
        lineTo(w * 0.82f, h * 0.78f)
        lineTo(w * 0.62f, h * 0.90f)
        lineTo(w * 0.40f, h * 0.86f)
        lineTo(w * 0.22f, h * 0.72f)
        lineTo(w * 0.10f, h * 0.48f)
        close()
    }
    drawLand(land, accent)

    // Yellow River (Huang He) — serpentine
    val huang = Path().apply {
        moveTo(w * 0.18f, h * 0.40f)
        cubicTo(
            w * 0.30f, h * 0.28f,
            w * 0.44f, h * 0.56f,
            w * 0.56f, h * 0.44f
        )
        cubicTo(
            w * 0.68f, h * 0.32f,
            w * 0.82f, h * 0.56f,
            w * 0.92f, h * 0.50f
        )
    }
    drawPath(huang, color = MythColors.Gold.copy(alpha = 0.85f), style = Stroke(2.2f))
    drawPath(huang, color = MythColors.Gold.copy(alpha = 0.25f), style = Stroke(7f))

    // Great Wall along the north
    val wall = Path().apply {
        moveTo(w * 0.20f, h * 0.22f)
        lineTo(w * 0.32f, h * 0.18f)
        lineTo(w * 0.44f, h * 0.22f)
        lineTo(w * 0.56f, h * 0.16f)
        lineTo(w * 0.68f, h * 0.22f)
        lineTo(w * 0.82f, h * 0.20f)
    }
    drawPath(wall, color = accent.copy(alpha = 0.9f), style = Stroke(2f))
    // Wall towers
    for (i in 0 until 6) {
        val cx = w * (0.22f + i * 0.12f)
        val cy = h * (0.19f + (i % 2) * 0.02f)
        drawRect(
            color = accent.copy(alpha = 0.8f),
            topLeft = Offset(cx - w * 0.008f, cy - h * 0.016f),
            size = Size(w * 0.016f, h * 0.022f),
            style = Stroke(1f),
        )
    }
}

private fun DrawScope.drawMayab(w: Float, h: Float, accent: Color) {
    // Yucatan-like peninsula jutting down
    val land = Path().apply {
        moveTo(w * 0.14f, h * 0.08f)
        lineTo(w * 0.72f, h * 0.06f)
        lineTo(w * 0.86f, h * 0.18f)
        lineTo(w * 0.80f, h * 0.36f)
        lineTo(w * 0.72f, h * 0.56f)
        lineTo(w * 0.62f, h * 0.76f)
        lineTo(w * 0.46f, h * 0.90f)
        lineTo(w * 0.28f, h * 0.82f)
        lineTo(w * 0.16f, h * 0.64f)
        lineTo(w * 0.10f, h * 0.40f)
        close()
    }
    drawLand(land, accent)

    // Jungle canopy texture (small ellipses)
    for (i in 0 until 14) {
        val cx = w * (0.18f + (i * 0.09f) % 0.6f)
        val cy = h * (0.18f + (i * 0.11f) % 0.6f)
        drawCircle(
            color = MythColors.Emerald.copy(alpha = 0.35f),
            radius = w * 0.018f,
            center = Offset(cx, cy),
        )
    }
    // Cenotes (sacred wells)
    listOf(
        Offset(w * 0.42f, h * 0.32f),
        Offset(w * 0.58f, h * 0.48f),
        Offset(w * 0.36f, h * 0.58f),
    ).forEach { c ->
        drawCircle(MythColors.CyanBright.copy(alpha = 0.7f), radius = w * 0.016f, center = c)
        drawCircle(MythColors.CyanBright, radius = w * 0.016f, center = c, style = Stroke(1f))
    }
    // Step-pyramid glyph
    val pyr = Path().apply {
        val cx = w * 0.32f
        val cy = h * 0.74f
        moveTo(cx - w * 0.04f, cy + h * 0.02f)
        lineTo(cx + w * 0.04f, cy + h * 0.02f)
        lineTo(cx + w * 0.028f, cy - h * 0.004f)
        lineTo(cx + w * 0.02f, cy - h * 0.014f)
        lineTo(cx - w * 0.02f, cy - h * 0.014f)
        lineTo(cx - w * 0.028f, cy - h * 0.004f)
        close()
    }
    drawPath(pyr, color = accent.copy(alpha = 0.55f))
}

private fun DrawScope.drawLatium(w: Float, h: Float, accent: Color) {
    // Italian "boot"
    val boot = Path().apply {
        moveTo(w * 0.20f, h * 0.08f)
        lineTo(w * 0.40f, h * 0.10f)
        lineTo(w * 0.52f, h * 0.24f)
        lineTo(w * 0.60f, h * 0.40f)
        lineTo(w * 0.70f, h * 0.54f)
        lineTo(w * 0.82f, h * 0.66f)
        lineTo(w * 0.88f, h * 0.74f)
        lineTo(w * 0.78f, h * 0.78f)
        lineTo(w * 0.70f, h * 0.72f)
        lineTo(w * 0.58f, h * 0.70f)
        lineTo(w * 0.46f, h * 0.56f)
        lineTo(w * 0.36f, h * 0.38f)
        lineTo(w * 0.28f, h * 0.26f)
        lineTo(w * 0.18f, h * 0.18f)
        close()
    }
    drawLand(boot, accent)

    // Sicily (triangle)
    val sicily = Path().apply {
        moveTo(w * 0.46f, h * 0.82f)
        lineTo(w * 0.60f, h * 0.82f)
        lineTo(w * 0.56f, h * 0.92f)
        close()
    }
    drawLand(sicily, accent)

    // Sardinia
    val sardinia = Path().apply {
        moveTo(w * 0.22f, h * 0.50f)
        lineTo(w * 0.30f, h * 0.50f)
        lineTo(w * 0.30f, h * 0.66f)
        lineTo(w * 0.22f, h * 0.68f)
        close()
    }
    drawLand(sardinia, accent)

    // Apennines spine
    val spine = Path().apply {
        moveTo(w * 0.30f, h * 0.20f)
        lineTo(w * 0.38f, h * 0.32f)
        lineTo(w * 0.48f, h * 0.46f)
        lineTo(w * 0.58f, h * 0.58f)
        lineTo(w * 0.68f, h * 0.66f)
    }
    drawPath(
        spine,
        color = accent.copy(alpha = 0.65f),
        style = Stroke(1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))),
    )
    // Aqueduct arches glyph near Rome
    for (i in 0 until 5) {
        val cx = w * (0.34f + i * 0.018f)
        drawArc(
            color = accent.copy(alpha = 0.45f),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx - w * 0.008f, h * 0.30f),
            size = Size(w * 0.016f, h * 0.014f),
            style = Stroke(1f),
        )
    }
}

private fun DrawScope.drawCompass(accent: Color) {
    val c = Offset(size.width / 2, size.height / 2)
    val r = size.width / 2
    drawCircle(
        color = MythColors.BgAbyss.copy(alpha = 0.4f),
        radius = r,
        center = c,
    )
    drawCircle(
        color = accent.copy(alpha = 0.7f),
        radius = r * 0.95f,
        center = c,
        style = Stroke(1f)
    )
    drawCircle(
        color = accent.copy(alpha = 0.4f),
        radius = r * 0.65f,
        center = c,
        style = Stroke(0.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 4f)))
    )
    val arrow = Path().apply {
        moveTo(c.x, c.y - r * 0.85f)
        lineTo(c.x - r * 0.15f, c.y)
        lineTo(c.x, c.y + r * 0.35f)
        lineTo(c.x + r * 0.15f, c.y)
        close()
    }
    drawPath(arrow, color = accent)
    val arrowDark = Path().apply {
        moveTo(c.x, c.y + r * 0.85f)
        lineTo(c.x - r * 0.15f, c.y)
        lineTo(c.x, c.y - r * 0.35f)
        lineTo(c.x + r * 0.15f, c.y)
        close()
    }
    drawPath(arrowDark, color = accent.copy(alpha = 0.3f))
    // Tick marks
    for (i in 0 until 16) {
        val a = i.toFloat() / 16f * 2f * PI.toFloat()
        val r1 = r * 0.95f
        val r2 = r * (if (i % 4 == 0) 0.80f else 0.88f)
        drawLine(
            color = accent.copy(alpha = 0.6f),
            start = Offset(c.x + cos(a) * r1, c.y + sin(a) * r1),
            end = Offset(c.x + cos(a) * r2, c.y + sin(a) * r2),
            strokeWidth = 0.8f,
        )
    }
    // Suppress unused
    abs(0f)
}
