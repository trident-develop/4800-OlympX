package org.example.project.model

data class ScoreParams(
    val referrer: String,
    val gadid: String,
    val probe: Int,
    val device: String,
    val firebaseId: String,
    val installTime: String
)