package org.example.project.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember

@Composable
actual fun MediaPickerHost(content: @Composable () -> Unit) {
    val handle = remember { IosMediaPicker }
    CompositionLocalProvider(LocalMediaPicker provides handle) {
        content()
    }
}

private object IosMediaPicker : MediaPickerHandle {
    override fun takePhoto(onResult: (PickerOutcome) -> Unit) {
        onResult(PickerOutcome.Canceled)
    }
    override fun pickFromGallery(onResult: (PickerOutcome) -> Unit) {
        onResult(PickerOutcome.Canceled)
    }
    override fun openAppSettings() {
        // No-op on iOS — this build does not request any permissions.
    }
}
