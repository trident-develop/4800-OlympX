package org.example.project.screens.play

import androidx.compose.ui.graphics.Color
import org.example.project.theme.MythColors

enum class EquipmentType(val display: String, val glyph: String, val accent: Color) {
    Boots("Boots", "֍", MythColors.Emerald),
    Armor("Armor", "▨", MythColors.Cyan),
    Sword("Sword", "⚔", MythColors.Gold),
    Shield("Shield", "◈", MythColors.Azure),
    Helmet("Helmet", "☢", MythColors.Electric);
}

data class EquipmentCard(
    val id: Int,
    val type: EquipmentType,
    val power: Int,
    val name: String,
) {
    val rarity: Rarity
        get() = when {
            power >= 9 -> Rarity.Mythic
            power >= 7 -> Rarity.Legendary
            power >= 5 -> Rarity.Rare
            power >= 3 -> Rarity.Uncommon
            else -> Rarity.Common
        }
}

enum class Rarity(val label: String, val color: Color) {
    Common("Common", MythColors.TextMuted),
    Uncommon("Uncommon", MythColors.Emerald),
    Rare("Rare", MythColors.Sky),
    Legendary("Legendary", MythColors.Gold),
    Mythic("Mythic", MythColors.CyanBright),
}

data class EnemyCard(
    val id: Int,
    val name: String,
    val epithet: String,
    val power: Int,
    val penalty: EquipmentType,
    val symbol: String,
    val accent: Color,
)

enum class BattleOutcome { Win, Lose, Draw }

data class BattleResult(
    val enemy: EnemyCard,
    val playerPower: Int,
    val outcome: BattleOutcome,
    val lostItem: EquipmentCard? = null,
)

object CardCatalog {
    private val equipmentNames = mapOf(
        EquipmentType.Boots to listOf(
            "Sandals of Hermes", "Greaves of Swiftwind", "Frost-Stride", "Sabatons of Anubis", "Hunter's Soles",
            "Thunderstep", "Kelp-bound Boots", "Pilgrim's Path", "Stormrunner", "Sunwalker"
        ),
        EquipmentType.Armor to listOf(
            "Aegis Mail", "Dragonhide Vest", "Robe of Isis", "Bronze Lorica", "Runespun Hauberk",
            "Mantle of Tides", "Scales of Jörmungandr", "Featherweave", "Vestments of Ra", "Obsidian Cuirass"
        ),
        EquipmentType.Sword to listOf(
            "Gleipnir Edge", "Khopesh of Horus", "Gladius Solaris", "Ivory Fang", "Wavecutter",
            "Mjölnir Shard", "Blade of Avalon", "Sickle of Kronos", "Starforged Saber", "Serpent's Kiss"
        ),
        EquipmentType.Shield to listOf(
            "Aspis of Dawn", "Scarab Bulwark", "Tower of Tyr", "Mirror of Perseus", "Coralcrest",
            "Sunwheel", "Shield of Heimdall", "Wyrmhide Targe", "Lunar Guard", "Stormwall"
        ),
        EquipmentType.Helmet to listOf(
            "Crown of Zeus", "Pharaoh's Diadem", "Valkyrie Helm", "Crest of Athena", "Seafoam Visor",
            "Raven Circlet", "Hood of the Oracle", "Solar Tiara", "Galea Imperialis", "Mooncaster's Cowl"
        ),
    )

    fun generateEquipmentPool(): Map<EquipmentType, List<EquipmentCard>> {
        var id = 0
        return EquipmentType.values().associateWith { type ->
            equipmentNames.getValue(type).mapIndexed { i, name ->
                EquipmentCard(id = id++, type = type, power = i + 1, name = name)
            }
        }
    }

    private val enemyNames = listOf(
        "Minotaur", "Hydra", "Chimera", "Cyclops", "Gorgon", "Cerberus", "Sphinx",
        "Wendigo", "Anubis-wraith", "Kraken", "Basilisk", "Harpy", "Gryphon", "Manticore",
        "Siren of Tides", "Banshee", "Nemean Lion", "Fafnir's Brood", "Typhon", "Empusa",
        "Fenrir's whelp", "Jörmungandr-kin", "Nidhogg-scion", "Mummy Prince", "Dragon of Ladon",
        "Ammit", "Set-stalker", "Centaur Raider", "Leviathan", "Oni Moonlord",
        "Golem of Sand", "Wraith of Thebes", "Undine", "Dryad Huntress", "Satyr Warlord",
        "Roc Ancient", "Kelpie Warlord", "Sea Serpent", "Phoenix Shadow", "Wraith-king",
        "Tartarus Spawn", "Pale Revenant", "Cursed Valkyrie", "Blood Raven", "Chronos-spawn",
        "Obsidian Bull", "Ashen Griffon", "Wicker Titan", "Void-caller", "Chronos Oracle",
    )
    private val epithets = listOf(
        "of the Twilight Vale", "Breaker of Chains", "the Unwaning", "Shade of Hades",
        "Scion of Fenrir", "Daughter of Storms", "Tomb-Wanderer", "of Black Tides",
        "the Moonless", "of Starlit Dunes"
    )
    private val palette = listOf(
        MythColors.Crimson, MythColors.Indigo, MythColors.Azure,
        MythColors.Gold, MythColors.Sky, MythColors.Emerald, MythColors.Electric
    )
    private val glyphs = listOf("☠", "☾", "⚚", "☽", "✦", "❂", "⚕", "⟡", "✷", "◈")

    fun buildDeck(rng: kotlin.random.Random): List<EnemyCard> = List(50) { i ->
        EnemyCard(
            id = i,
            name = enemyNames[i % enemyNames.size],
            epithet = epithets[rng.nextInt(epithets.size)],
            power = 1 + rng.nextInt(49),
            penalty = EquipmentType.values().random(rng),
            symbol = glyphs[rng.nextInt(glyphs.size)],
            accent = palette[rng.nextInt(palette.size)],
        )
    }.shuffled(rng)
}
