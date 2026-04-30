package org.example.project.platform

import platform.Foundation.NSUserDefaults

private const val KEY_VISITED = "olympx_world_visited_place_ids"

actual fun loadVisitedPlaces(): Set<String> {
    val raw = NSUserDefaults.standardUserDefaults.arrayForKey(KEY_VISITED) ?: return emptySet()
    return raw.mapNotNull { it as? String }.toSet()
}

actual fun saveVisitedPlaces(ids: Set<String>) {
    NSUserDefaults.standardUserDefaults.setObject(ids.toList(), KEY_VISITED)
}
