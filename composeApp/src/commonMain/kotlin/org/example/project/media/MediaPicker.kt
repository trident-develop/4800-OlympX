package org.example.project.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf

@Stable
interface MediaPickerHandle {
    fun takePhoto(onResult: (PickerOutcome) -> Unit)
    fun pickFromGallery(onResult: (PickerOutcome) -> Unit)
    fun openAppSettings()
}

val LocalMediaPicker = staticCompositionLocalOf<MediaPickerHandle> {
    object : MediaPickerHandle {
        override fun takePhoto(onResult: (PickerOutcome) -> Unit) {
            onResult(PickerOutcome.Error("MediaPickerHost not installed"))
        }
        override fun pickFromGallery(onResult: (PickerOutcome) -> Unit) {
            onResult(PickerOutcome.Error("MediaPickerHost not installed"))
        }
        override fun openAppSettings() {}
    }
}

@Composable
expect fun MediaPickerHost(content: @Composable () -> Unit)
