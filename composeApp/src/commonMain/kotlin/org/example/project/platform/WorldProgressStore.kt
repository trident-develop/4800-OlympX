package org.example.project.platform

expect fun loadVisitedPlaces(): Set<String>
expect fun saveVisitedPlaces(ids: Set<String>)
