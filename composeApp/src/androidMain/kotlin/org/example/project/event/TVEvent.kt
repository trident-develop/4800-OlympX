package org.example.project.event

sealed interface TVEvent {
    data object OpenGame : TVEvent
}