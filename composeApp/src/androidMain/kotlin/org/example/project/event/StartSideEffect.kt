package org.example.project.event

sealed interface StartSideEffect {

    data class OpenBuiltScore(
        val score: String
    ) : StartSideEffect

    data class OpenTypeA(
        val score: String
    ) : StartSideEffect

    data class OpenTypeB(
        val score: String
    ) : StartSideEffect

    data object OpenGame : StartSideEffect
}