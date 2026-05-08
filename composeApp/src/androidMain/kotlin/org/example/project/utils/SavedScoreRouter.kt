package org.example.project.utils

import org.example.project.android.buildD
import org.example.project.event.StartDestination

class SavedScoreRouter {

    fun score(savedScore: String): StartDestination {
        return when {
            !savedScore.startsWith(buildD(198456)) -> {
                StartDestination.OpenSavedScoreTypeA(savedScore)
            }

            savedScore.startsWith(buildD(198456)) -> {
                StartDestination.OpenSavedScoreTypeB(savedScore)
            }

            else -> {
                StartDestination.OpenGame
            }
        }
    }
}