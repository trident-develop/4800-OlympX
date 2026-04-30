package org.example.project.platform

expect fun readEnergyBlob(): String?
expect fun writeEnergyBlob(blob: String?)

/** Loads (energy, anchorMillis). Returns null if nothing stored yet. */
fun loadEnergyState(): Pair<Int, Long>? {
    val blob = readEnergyBlob() ?: return null
    val parts = blob.split(",")
    if (parts.size < 2) return null
    val e = parts[0].toIntOrNull() ?: return null
    val a = parts[1].toLongOrNull() ?: return null
    return e to a
}

fun saveEnergyState(energy: Int, anchorMillis: Long) {
    writeEnergyBlob("$energy,$anchorMillis")
}
