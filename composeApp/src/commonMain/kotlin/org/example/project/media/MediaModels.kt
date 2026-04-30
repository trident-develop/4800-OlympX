package org.example.project.media

enum class PickerSource { Camera, Gallery }

sealed interface PickerOutcome {
    data class Success(val bytes: ByteArray) : PickerOutcome {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Success) return false
            return bytes.contentEquals(other.bytes)
        }
        override fun hashCode(): Int = bytes.contentHashCode()
    }

    data object Canceled : PickerOutcome
    data class PermissionDenied(val permanent: Boolean) : PickerOutcome
    data class Error(val message: String) : PickerOutcome
}
