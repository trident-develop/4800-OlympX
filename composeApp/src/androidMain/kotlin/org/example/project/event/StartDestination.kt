package org.example.project.event

sealed interface StartDestination {

    data class BuiltScore(
        val score: String
    ) : StartDestination

    data class OpenSavedScoreTypeA(
        val score: String
    ) : StartDestination

    data class OpenSavedScoreTypeB(
        val score: String
    ) : StartDestination

    data object OpenGame : StartDestination
}