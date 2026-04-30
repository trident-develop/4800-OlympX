package org.example.project.platform

enum class TargetPlatform { Android, Ios }

expect val currentPlatform: TargetPlatform
