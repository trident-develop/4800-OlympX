package org.example.project.screens.journey

enum class PathType(val display: String) {
    Heroic("Heroic"),
    Trickster("Trickster"),
    Oracle("Oracle"),
}

enum class ChapterState { Completed, Active, Locked }

data class Chapter(
    val index: Int,
    val id: String,
    val title: String,
    val synopsis: String,
    val state: ChapterState,
    val durationMinutes: Int,
)

data class Choice(
    val chapter: String,
    val summary: String,
)

object JourneyCatalog {
    val chapters = listOf(
        Chapter(1, "delphi", "The Oracle's Warning", "Delphi stirs. The Pythia whispers of a fate only you may rewrite.", ChapterState.Active, 8),
        Chapter(2, "hades", "Beneath the Black Sail", "You descend toward Hades' river carrying a coin that is not yours.", ChapterState.Active, 12),
        Chapter(3, "bifrost", "The Bridge of Flame", "Heimdall's horn trembles — the Bifrost cracks under moonless storm.", ChapterState.Active, 14),
        Chapter(4, "osiris", "Halls of Osiris", "The feather of Ma'at tips; your heart must answer for every oath.", ChapterState.Active, 10),
        Chapter(5, "avalon", "Avalon's Mist", "Arthur's tomb pulses with a tide older than time.", ChapterState.Active, 16),
        Chapter(6, "troy", "The Fire of Troy", "The wooden horse is opened in the dark. Smoke turns the moon red.", ChapterState.Active, 12),
        Chapter(7, "chichen", "Serpent on the Steps", "The priest-king calls for you at the foot of the pyramid.", ChapterState.Active, 11),
        Chapter(8, "pasargadae", "Cyrus's Garden", "Four quadrants of paradise wait at the tomb of the King of Kings.", ChapterState.Active, 9),
        Chapter(9, "stonehenge", "Stones of Salisbury", "The solstice dawn approaches. Nine druids wait for your answer.", ChapterState.Active, 13),
        Chapter(10, "greatwall", "Dragon of the Wall", "From the watchtower you see torches on the steppe — and they are moving.", ChapterState.Active, 14),
        Chapter(11, "babylon", "Writing on the Wall", "Nebuchadnezzar's feast is halted by a hand that writes in fire.", ChapterState.Active, 11),
        Chapter(12, "cumae", "The Sibyl's Leaves", "Descending into the gallery, you hear leaves already scattered.", ChapterState.Active, 15),
    )

    val recentChoices = listOf(
        Choice("Chapter II", "You offered the coin rather than keep it — Charon remembered."),
        Choice("Chapter II", "You refused to speak the name of the lost — the shade wept and passed."),
        Choice("Chapter I", "You heeded the Oracle even when she contradicted the priests."),
    )
}
