package org.example.project.viewmodel

import androidx.lifecycle.ViewModel
import org.example.project.event.StartDestination
import org.example.project.event.StartSideEffect
import org.example.project.event.StartState
import org.example.project.event.TVEvent
import org.example.project.platform.privacy.TV3
import org.example.project.utils.ResolveStartFlowUseCase
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class StartViewModel(
    private val resolveStartFlowUseCase: ResolveStartFlowUseCase
) : ViewModel(), ContainerHost<StartState, StartSideEffect> {

    override val container = container<StartState, StartSideEffect>(
        initialState = StartState()
    )

    fun start() = intent {
        reduce {
            state.copy(isLoading = true)
        }

        val destination = resolveStartFlowUseCase()

        reduce {
            state.copy(isLoading = false)
        }

        when (destination) {
            is StartDestination.BuiltScore -> {
                postSideEffect(StartSideEffect.OpenBuiltScore(destination.score))
            }

            is StartDestination.OpenSavedScoreTypeA -> {
                postSideEffect(StartSideEffect.OpenTypeA(destination.score))
            }

            is StartDestination.OpenSavedScoreTypeB -> {
                postSideEffect(StartSideEffect.OpenTypeB(destination.score))
            }

            StartDestination.OpenGame -> {
                postSideEffect(StartSideEffect.OpenGame)
            }
        }
    }

    fun observeTVEvents(TV3: TV3) = intent {
        TV3.events.collect { event ->
            when (event) {
                TVEvent.OpenGame -> {
                    postSideEffect(StartSideEffect.OpenGame)
                }
            }
        }
    }
}