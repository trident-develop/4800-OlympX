package org.example.project.platform

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

expect fun readProfileImageBlob(): String?
expect fun writeProfileImageBlob(blob: String?)

@OptIn(ExperimentalEncodingApi::class)
fun loadProfileImage(): ByteArray? {
    val blob = readProfileImageBlob() ?: return null
    if (blob.isEmpty()) return null
    return runCatching { Base64.decode(blob) }.getOrNull()
}

@OptIn(ExperimentalEncodingApi::class)
fun saveProfileImage(bytes: ByteArray?) {
    if (bytes == null) {
        writeProfileImageBlob(null)
        return
    }
    writeProfileImageBlob(Base64.encode(bytes))
}
