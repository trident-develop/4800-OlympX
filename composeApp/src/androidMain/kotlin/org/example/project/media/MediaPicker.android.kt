package org.example.project.media

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.project.ui.components.MythicDialog
import java.io.InputStream

@Composable
actual fun MediaPickerHost(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scope = rememberCoroutineScope()

    var pendingCamera by remember { mutableStateOf<((PickerOutcome) -> Unit)?>(null) }
    var pendingGallery by remember { mutableStateOf<((PickerOutcome) -> Unit)?>(null) }
    var showCamera by remember { mutableStateOf(false) }
    var showRationale by remember { mutableStateOf<RationaleInfo?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        val callback = pendingGallery
        pendingGallery = null
        if (uri == null) {
            callback?.invoke(PickerOutcome.Canceled)
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val bytes = withContext(Dispatchers.IO) {
                runCatching {
                    val stream: InputStream? = context.contentResolver.openInputStream(uri)
                    stream?.use { it.readBytes() }
                }.getOrNull()
            }
            if (bytes != null) callback?.invoke(PickerOutcome.Success(bytes))
            else callback?.invoke(PickerOutcome.Error("Unable to read selected image"))
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showCamera = true
        } else {
            val activityRef = activity
            val permanent = activityRef != null &&
                !activityRef.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
            val callback = pendingCamera
            pendingCamera = null
            if (permanent) {
                showRationale = RationaleInfo(
                    title = "Camera access needed",
                    message = "Enable the camera permission in Settings to capture a portrait.",
                    permanent = true,
                    onRetry = {},
                    onConfirmSettings = { openAppSettings(context) },
                    onResult = { callback?.invoke(PickerOutcome.PermissionDenied(permanent = true)) }
                )
            } else {
                callback?.invoke(PickerOutcome.PermissionDenied(permanent = false))
            }
        }
    }

    val handle = remember(context, activity) {
        object : MediaPickerHandle {
            override fun takePhoto(onResult: (PickerOutcome) -> Unit) {
                pendingCamera = onResult
                when {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED -> {
                        showCamera = true
                    }
                    activity != null &&
                        activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                        showRationale = RationaleInfo(
                            title = "Camera access",
                            message = "OlympX uses the camera to capture your mythic portrait.",
                            permanent = false,
                            onRetry = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                            onConfirmSettings = { openAppSettings(context) },
                            onResult = { onResult(PickerOutcome.PermissionDenied(permanent = false)) }
                        )
                    }
                    else -> {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            }

            override fun pickFromGallery(onResult: (PickerOutcome) -> Unit) {
                pendingGallery = onResult
                // PickVisualMedia uses the system Photo Picker on Android 13+ and
                // falls back to ACTION_OPEN_DOCUMENT (Storage Access Framework) on
                // older versions. Neither path requires READ_MEDIA_IMAGES or
                // READ_EXTERNAL_STORAGE — access is granted per-URI.
                galleryLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }

            override fun openAppSettings() {
                openAppSettings(context)
            }
        }
    }

    CompositionLocalProvider(LocalMediaPicker provides handle) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
            AnimatedVisibility(
                visible = showCamera,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CameraCaptureScreen(
                    onCaptured = { bytes ->
                        showCamera = false
                        val cb = pendingCamera
                        pendingCamera = null
                        cb?.invoke(PickerOutcome.Success(bytes))
                    },
                    onCancel = {
                        showCamera = false
                        val cb = pendingCamera
                        pendingCamera = null
                        cb?.invoke(PickerOutcome.Canceled)
                    }
                )
            }
        }
    }

    val rationale = showRationale
    MythicDialog(
        visible = rationale != null,
        onDismiss = {
            val r = showRationale
            showRationale = null
            r?.onResult?.invoke()
        },
        title = rationale?.title ?: "",
        message = rationale?.message,
        confirmLabel = if (rationale?.permanent == true) "Open Settings" else "Allow",
        onConfirm = {
            val r = showRationale
            showRationale = null
            if (r?.permanent == true) r.onConfirmSettings()
            else r?.onRetry?.invoke()
        },
        dismissLabel = "Not now",
    )
}

private data class RationaleInfo(
    val title: String,
    val message: String,
    val permanent: Boolean,
    val onRetry: () -> Unit,
    val onConfirmSettings: () -> Unit,
    val onResult: () -> Unit,
)

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}

internal fun Context.findActivity(): ComponentActivity? {
    var c: Context = this
    while (c is ContextWrapper) {
        if (c is ComponentActivity) return c
        c = c.baseContext
    }
    return null
}
