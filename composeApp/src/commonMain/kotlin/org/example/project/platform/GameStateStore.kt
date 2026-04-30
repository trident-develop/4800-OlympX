package org.example.project.platform

expect fun readGameStateBlob(): String?
expect fun writeGameStateBlob(blob: String?)
