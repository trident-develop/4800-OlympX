package org.example.project.model

data class DeviceSignals(
    val referrer: String,
    val gadid: String,
    val probe: Int,
    val deviceName: String,
    val firebaseId: String,
    val installTime: String
)