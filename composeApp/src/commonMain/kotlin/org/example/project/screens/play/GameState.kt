package org.example.project.screens.play

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.example.project.platform.currentTimeMillis
import org.example.project.platform.loadEnergyState
import org.example.project.platform.loadGloryScore
import org.example.project.platform.readGameStateBlob
import org.example.project.platform.saveEnergyState
import org.example.project.platform.saveGloryScore
import org.example.project.platform.writeGameStateBlob
import kotlin.random.Random

enum class BattlePhase { Building, Revealing, Resolving, Outcome, Unrevealing }

enum class FightStartResult { Started, NeedConfirm, NoEnergy }

@Stable
class GameState internal constructor(seed: Long) {
    private val rng = Random(seed)
    private val equipmentPool = CardCatalog.generateEquipmentPool()
    private val initialDeck: List<EnemyCard> = CardCatalog.buildDeck(rng)
    private val enemyCatalog: Map<Int, EnemyCard> = initialDeck.associateBy { it.id }

    val maxBuildChanges: Int = 3
    private val boostMin: Int = 10
    private val boostMax: Int = 15
    val boostRangeLabel: String get() = "$boostMin–$boostMax"

    var deck by mutableStateOf(initialDeck)
        private set

    var currentEnemy by mutableStateOf<EnemyCard?>(deck.firstOrNull())
        private set

    var equipped by mutableStateOf<Map<EquipmentType, EquipmentCard?>>(emptyMap())
        private set

    var score by mutableStateOf(loadGloryScore())
        private set

    var lastResult by mutableStateOf<BattleResult?>(null)
        private set

    var battleLog by mutableStateOf<List<String>>(emptyList())
        private set

    var phase by mutableStateOf(BattlePhase.Building)
        private set

    var buildChangesLeft by mutableStateOf(maxBuildChanges)
        private set

    var buildConfirmed by mutableStateOf(false)
        private set

    var hasRolledBuild by mutableStateOf(false)
        private set

    var boostedType by mutableStateOf<EquipmentType?>(null)
        private set

    var lockedType by mutableStateOf<EquipmentType?>(null)
        private set

    var rewardOptions by mutableStateOf<List<EquipmentType>>(emptyList())
        private set

    var wins by mutableStateOf(0)
        private set

    var losses by mutableStateOf(0)
        private set

    var draws by mutableStateOf(0)
        private set

    var blessings by mutableStateOf(0)
        private set

    var defeatedEnemies by mutableStateOf<Set<Int>>(emptySet())
        private set

    val battlesPlayed: Int get() = wins + losses + draws

    private var pendingBoost: EquipmentType? = null
    private var pendingLock: EquipmentType? = null

    var energy by mutableStateOf(MAX_ENERGY)
        private set

    private var energyAnchor: Long = 0L

    val playerPower: Int
        get() = equipped.values.filterNotNull().sumOf { it.power }

    val deckCount: Int
        get() = deck.size

    init {
        readGameStateBlob()
            ?.let { decodeGameState(it) }
            ?.let { applyRestored(it) }
        loadEnergyState()?.let { (e, a) ->
            energy = e.coerceIn(0, MAX_ENERGY)
            energyAnchor = a
        }
        recomputeEnergy()
    }

    companion object {
        const val MAX_ENERGY = 5
        const val ENERGY_REGEN_MS = 10L * 60L * 1000L
    }

    fun forgeReplacement(type: EquipmentType) {
        if (phase != BattlePhase.Building) return
        if (lockedType == type) return
        if (boostedType == type) return   // blessed this battle — cannot be changed
        if (buildChangesLeft <= 0) return
        val base = equipmentPool.getValue(type).random(rng)
        equipped = equipped + (type to base)
        buildChangesLeft--
        persist()
    }

    fun rollBuild() {
        if (phase != BattlePhase.Building) return
        if (buildChangesLeft <= 0) return
        val rolled = EquipmentType.entries
            .filter { it != lockedType }
            .associateWith<EquipmentType, EquipmentCard?> { type ->
                if (type == boostedType) equipped[type] // keep the blessed item as-is
                else equipmentPool.getValue(type).random(rng)
            }
        equipped = rolled
        buildChangesLeft--
        hasRolledBuild = true
        persist()
    }

    fun confirmBuild() {
        if (phase != BattlePhase.Building) return
        if (!hasRolledBuild) return
        buildConfirmed = true
        persist()
    }

    fun tryStartFight(): FightStartResult {
        if (phase != BattlePhase.Building || !buildConfirmed) return FightStartResult.NeedConfirm
        recomputeEnergy()
        if (energy <= 0) return FightStartResult.NoEnergy
        // Consume one energy point; start the regen clock if we were at max.
        if (energy == MAX_ENERGY) energyAnchor = currentTimeMillis()
        energy--
        saveEnergyState(energy, energyAnchor)
        phase = BattlePhase.Revealing
        persist()
        return FightStartResult.Started
    }

    /** Advances energy regeneration based on wall-clock time. Safe to call repeatedly. */
    fun tick() {
        recomputeEnergy()
    }

    /** Milliseconds until at least one additional energy point is restored, or 0 if full. */
    fun msUntilNextEnergyPoint(): Long {
        if (energy >= MAX_ENERGY) return 0L
        val elapsed = currentTimeMillis() - energyAnchor
        if (elapsed < 0) return ENERGY_REGEN_MS
        return (ENERGY_REGEN_MS - elapsed).coerceAtLeast(0L)
    }

    private fun recomputeEnergy() {
        if (energy >= MAX_ENERGY) {
            if (energyAnchor != 0L) {
                energyAnchor = 0L
                saveEnergyState(energy, energyAnchor)
            }
            return
        }
        val now = currentTimeMillis()
        var elapsed = now - energyAnchor
        if (elapsed < 0L) {
            // Device clock moved backwards — restart regen from now.
            energyAnchor = now
            saveEnergyState(energy, energyAnchor)
            return
        }
        if (elapsed < ENERGY_REGEN_MS) return
        val pointsGained = (elapsed / ENERGY_REGEN_MS).toInt()
        val leftover = elapsed % ENERGY_REGEN_MS
        val newEnergy = (energy + pointsGained).coerceAtMost(MAX_ENERGY)
        energy = newEnergy
        energyAnchor = if (newEnergy >= MAX_ENERGY) 0L else (now - leftover)
        saveEnergyState(energy, energyAnchor)
    }

    fun onRevealAnimationDone() {
        if (phase != BattlePhase.Revealing) return
        val enemy = currentEnemy ?: run {
            phase = BattlePhase.Outcome
            persist()
            return
        }
        val power = playerPower
        val outcome = when {
            power > enemy.power -> BattleOutcome.Win
            power == enemy.power -> BattleOutcome.Draw
            else -> BattleOutcome.Lose
        }
        val logLine = when (outcome) {
            BattleOutcome.Win -> "Victory over ${enemy.name} · +${enemy.power} glory"
            BattleOutcome.Draw -> "Stalemate with ${enemy.name}"
            BattleOutcome.Lose -> "Fell to ${enemy.name} · ${enemy.penalty.display} barred next battle"
        }
        lastResult = BattleResult(enemy, power, outcome, null)
        if (outcome == BattleOutcome.Win) {
            score += enemy.power
            saveGloryScore(score)
        }
        battleLog = (listOf(logLine) + battleLog).take(6)

        when (outcome) {
            BattleOutcome.Win -> {
                wins++
                defeatedEnemies = defeatedEnemies + enemy.id
                val pool = EquipmentType.entries.filter { it != pendingLock }
                rewardOptions = pool.shuffled(rng).take(2)
                pendingLock = null
            }
            BattleOutcome.Lose -> {
                losses++
                rewardOptions = emptyList()
                pendingLock = enemy.penalty
                pendingBoost = null
            }
            BattleOutcome.Draw -> {
                draws++
                rewardOptions = emptyList()
                pendingLock = null
            }
        }
        phase = BattlePhase.Resolving
        persist()
    }

    fun onResolveDelayDone() {
        if (phase != BattlePhase.Resolving) return
        phase = BattlePhase.Outcome
        persist()
    }

    fun chooseReward(type: EquipmentType) {
        if (phase != BattlePhase.Outcome) return
        pendingBoost = type
        blessings++
        phase = BattlePhase.Unrevealing
        persist()
    }

    fun acknowledgeOutcome() {
        if (phase != BattlePhase.Outcome) return
        phase = BattlePhase.Unrevealing
        persist()
    }

    fun onUnrevealAnimationDone() {
        if (phase != BattlePhase.Unrevealing) return
        startNewBattle()
    }

    fun shuffleDeck() {
        if (phase != BattlePhase.Building) return
        deck = deck.shuffled(rng)
        currentEnemy = deck.firstOrNull()
        battleLog = listOf("Shuffled the deck") + battleLog
        persist()
    }

    private fun startNewBattle() {
        val remaining = deck.drop(1)
        deck = remaining
        currentEnemy = remaining.firstOrNull()
        lastResult = null
        rewardOptions = emptyList()

        boostedType = pendingBoost
        pendingBoost = null
        lockedType = pendingLock
        pendingLock = null

        // If a slot is blessed, pre-forge that item with a +10..15 bonus.
        val initial = buildMap<EquipmentType, EquipmentCard?> {
            val boosted = boostedType
            if (boosted != null) {
                val base = equipmentPool.getValue(boosted).random(rng)
                val bonus = rng.nextInt(boostMin, boostMax + 1)
                put(boosted, base.copy(power = base.power + bonus))
            }
        }
        equipped = initial
        buildChangesLeft = maxBuildChanges
        buildConfirmed = false
        hasRolledBuild = false
        phase = BattlePhase.Building
        persist()
    }

    private fun applyRestored(s: GameStateSnapshot) {
        runCatching {
            phase = s.phase
            buildChangesLeft = s.buildChangesLeft
            buildConfirmed = s.buildConfirmed
            hasRolledBuild = s.hasRolledBuild
            boostedType = s.boostedType
            lockedType = s.lockedType
            pendingBoost = s.pendingBoost
            pendingLock = s.pendingLock

            val restoredEquipped = mutableMapOf<EquipmentType, EquipmentCard?>()
            for ((type, spec) in s.equipped) {
                val base = equipmentPool[type]?.firstOrNull { it.id == spec.id } ?: continue
                restoredEquipped[type] = base.copy(power = spec.power)
            }
            equipped = restoredEquipped

            val restoredDeck = s.deckIds.mapNotNull { enemyCatalog[it] }
            if (restoredDeck.isNotEmpty() || s.deckIds.isEmpty()) {
                deck = restoredDeck
                currentEnemy = restoredDeck.firstOrNull()
            }

            rewardOptions = s.rewardOptions
            battleLog = s.battleLog

            lastResult = s.lastResult?.let { spec ->
                enemyCatalog[spec.enemyId]?.let { enemy ->
                    BattleResult(enemy, spec.playerPower, spec.outcome, null)
                }
            }

            wins = s.wins
            losses = s.losses
            draws = s.draws
            blessings = s.blessings
            defeatedEnemies = s.defeatedEnemies
        }
    }

    private fun persist() {
        val snap = GameStateSnapshot(
            phase = phase,
            buildChangesLeft = buildChangesLeft,
            buildConfirmed = buildConfirmed,
            hasRolledBuild = hasRolledBuild,
            boostedType = boostedType,
            lockedType = lockedType,
            pendingBoost = pendingBoost,
            pendingLock = pendingLock,
            equipped = equipped
                .mapNotNull { (type, card) -> card?.let { type to EquippedSpec(it.id, it.power) } }
                .toMap(),
            deckIds = deck.map { it.id },
            rewardOptions = rewardOptions,
            battleLog = battleLog,
            lastResult = lastResult?.let { LastResultSpec(it.enemy.id, it.playerPower, it.outcome) },
            wins = wins,
            losses = losses,
            draws = draws,
            blessings = blessings,
            defeatedEnemies = defeatedEnemies,
        )
        writeGameStateBlob(encodeGameState(snap))
    }
}

private val SharedGameState: GameState = GameState(1337L)

@Composable
fun rememberGameState(seed: Long = 1337L): GameState = SharedGameState
