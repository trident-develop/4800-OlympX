package org.example.project.screens.play

private const val SEP1 = "|"
private const val SEP2 = "^"
private const val SEP3 = "~"
private const val NIL = "-"
private const val VERSION_V1 = "v1"
private const val VERSION_V2 = "v2"

internal data class EquippedSpec(val id: Int, val power: Int)

internal data class LastResultSpec(
    val enemyId: Int,
    val playerPower: Int,
    val outcome: BattleOutcome,
)

internal data class GameStateSnapshot(
    val phase: BattlePhase,
    val buildChangesLeft: Int,
    val buildConfirmed: Boolean,
    val hasRolledBuild: Boolean,
    val boostedType: EquipmentType?,
    val lockedType: EquipmentType?,
    val pendingBoost: EquipmentType?,
    val pendingLock: EquipmentType?,
    val equipped: Map<EquipmentType, EquippedSpec>,
    val deckIds: List<Int>,
    val rewardOptions: List<EquipmentType>,
    val battleLog: List<String>,
    val lastResult: LastResultSpec?,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val blessings: Int = 0,
    val defeatedEnemies: Set<Int> = emptySet(),
)

internal fun encodeGameState(s: GameStateSnapshot): String = buildString {
    append(VERSION_V2); append(SEP1)
    append(s.phase.name); append(SEP1)
    append(s.buildChangesLeft); append(SEP1)
    append(if (s.buildConfirmed) "1" else "0"); append(SEP1)
    append(if (s.hasRolledBuild) "1" else "0"); append(SEP1)
    append(s.boostedType?.name ?: NIL); append(SEP1)
    append(s.lockedType?.name ?: NIL); append(SEP1)
    append(s.pendingBoost?.name ?: NIL); append(SEP1)
    append(s.pendingLock?.name ?: NIL); append(SEP1)
    append(
        s.equipped.entries.joinToString(SEP2) { (type, spec) ->
            "${type.name}$SEP3${spec.id}$SEP3${spec.power}"
        }
    ); append(SEP1)
    append(s.deckIds.joinToString(",")); append(SEP1)
    append(s.rewardOptions.joinToString(SEP2) { it.name }); append(SEP1)
    append(s.battleLog.joinToString(SEP2)); append(SEP1)
    val r = s.lastResult
    if (r == null) append(NIL)
    else append("${r.enemyId}$SEP3${r.playerPower}$SEP3${r.outcome.name}")
    append(SEP1)
    append(s.wins); append(SEP1)
    append(s.losses); append(SEP1)
    append(s.draws); append(SEP1)
    append(s.blessings); append(SEP1)
    append(s.defeatedEnemies.joinToString(","))
}

internal fun decodeGameState(raw: String): GameStateSnapshot? = runCatching {
    val parts = raw.split(SEP1)
    if (parts.size < 14) return@runCatching null
    val version = parts[0]
    if (version != VERSION_V1 && version != VERSION_V2) return@runCatching null

    val phase = BattlePhase.valueOf(parts[1])
    val changes = parts[2].toInt()
    val confirmed = parts[3] == "1"
    val hasRolled = parts[4] == "1"
    val boosted = parseNullableEquip(parts[5])
    val locked = parseNullableEquip(parts[6])
    val pendingB = parseNullableEquip(parts[7])
    val pendingL = parseNullableEquip(parts[8])

    val equipped = if (parts[9].isEmpty()) emptyMap() else parts[9]
        .split(SEP2)
        .associate { rec ->
            val f = rec.split(SEP3)
            EquipmentType.valueOf(f[0]) to EquippedSpec(f[1].toInt(), f[2].toInt())
        }

    val deckIds = if (parts[10].isEmpty()) emptyList() else parts[10].split(",").map { it.toInt() }

    val rewards = if (parts[11].isEmpty()) emptyList() else
        parts[11].split(SEP2).map { EquipmentType.valueOf(it) }

    val log = if (parts[12].isEmpty()) emptyList() else parts[12].split(SEP2)

    val result = if (parts[13] == NIL || parts[13].isEmpty()) null else {
        val f = parts[13].split(SEP3)
        LastResultSpec(f[0].toInt(), f[1].toInt(), BattleOutcome.valueOf(f[2]))
    }

    // v2 fields — default to 0 / empty if blob is older
    val wins = parts.getOrNull(14)?.toIntOrNull() ?: 0
    val losses = parts.getOrNull(15)?.toIntOrNull() ?: 0
    val draws = parts.getOrNull(16)?.toIntOrNull() ?: 0
    val blessings = parts.getOrNull(17)?.toIntOrNull() ?: 0
    val defeated = parts.getOrNull(18)
        ?.takeIf { it.isNotEmpty() }
        ?.split(",")
        ?.mapNotNull { it.toIntOrNull() }
        ?.toSet()
        ?: emptySet()

    GameStateSnapshot(
        phase, changes, confirmed, hasRolled,
        boosted, locked, pendingB, pendingL,
        equipped, deckIds, rewards, log, result,
        wins, losses, draws, blessings, defeated,
    )
}.getOrNull()

private fun parseNullableEquip(s: String): EquipmentType? =
    if (s == NIL || s.isEmpty()) null else EquipmentType.valueOf(s)
