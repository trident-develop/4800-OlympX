package org.example.project.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.example.project.platform.loadProfileImage
import org.example.project.platform.readProfileName
import org.example.project.platform.saveProfileImage
import org.example.project.platform.writeProfileName

const val PROFILE_NAME_MAX_LENGTH = 10

@Stable
class ProfileState {
    var name by mutableStateOf(readProfileName() ?: "Kael")
        private set
    var title by mutableStateOf("ORACLE-TOUCHED")
        private set
    var imageBytes by mutableStateOf<ByteArray?>(loadProfileImage())
        private set

    fun setImage(bytes: ByteArray) {
        imageBytes = bytes
        saveProfileImage(bytes)
    }

    fun updateName(value: String) {
        val clipped = value.take(PROFILE_NAME_MAX_LENGTH)
        name = clipped
        writeProfileName(clipped)
    }

    fun clearIdentity() {
        name = ""
        imageBytes = null
        writeProfileName("")
        saveProfileImage(null)
    }
}

private val SharedProfileState = ProfileState()

@Composable
fun rememberProfileState(): ProfileState = SharedProfileState
