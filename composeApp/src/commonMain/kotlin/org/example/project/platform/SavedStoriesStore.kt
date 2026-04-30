package org.example.project.platform

data class SavedStory(
    val id: String,
    val chapterId: String,
    val title: String,
    val completedAtMillis: Long,
    val courage: Int,
    val wisdom: Int,
    val cunning: Int,
    val compassion: Int,
    val story: String,
)

private const val RS = '' // record separator
private const val US = '' // unit separator

internal fun encodeStories(list: List<SavedStory>): String = list.joinToString(RS.toString()) { s ->
    listOf(
        s.id, s.chapterId, s.title,
        s.completedAtMillis.toString(),
        s.courage.toString(), s.wisdom.toString(),
        s.cunning.toString(), s.compassion.toString(),
        s.story,
    ).joinToString(US.toString())
}

internal fun decodeStories(raw: String?): List<SavedStory> {
    if (raw.isNullOrEmpty()) return emptyList()
    return raw.split(RS).mapNotNull { row ->
        val parts = row.split(US)
        if (parts.size < 9) return@mapNotNull null
        SavedStory(
            id = parts[0],
            chapterId = parts[1],
            title = parts[2],
            completedAtMillis = parts[3].toLongOrNull() ?: 0L,
            courage = parts[4].toIntOrNull() ?: 0,
            wisdom = parts[5].toIntOrNull() ?: 0,
            cunning = parts[6].toIntOrNull() ?: 0,
            compassion = parts[7].toIntOrNull() ?: 0,
            story = parts[8],
        )
    }
}

expect fun loadSavedStories(): List<SavedStory>
expect fun saveSavedStories(stories: List<SavedStory>)

fun addSavedStory(story: SavedStory) {
    val list = loadSavedStories().toMutableList()
    list.add(0, story)
    saveSavedStories(list)
}

fun removeSavedStory(id: String) {
    saveSavedStories(loadSavedStories().filter { it.id != id })
}
