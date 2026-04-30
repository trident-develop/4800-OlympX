@file:OptIn(kotlinx.cinterop.BetaInteropApi::class, kotlinx.cinterop.ExperimentalForeignApi::class)

package org.example.project.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import org.example.project.ios.ViewControllerHolder
import org.example.project.platform.toByteArray
import platform.AVFoundation.AVAuthorizationStatus
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSItemProvider
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.Photos.PHAuthorizationStatus
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusDenied
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHAuthorizationStatusRestricted
import platform.Photos.PHAccessLevelReadWrite
import platform.Photos.PHPhotoLibrary
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject

private var currentPickerDelegate: NSObject? = null
private var currentCameraDelegate: NSObject? = null

@Composable
actual fun MediaPickerHost(content: @Composable () -> Unit) {
    val handle = remember { IosMediaPicker() }
    CompositionLocalProvider(LocalMediaPicker provides handle) {
        content()
    }
}

private class IosMediaPicker : MediaPickerHandle {

    override fun takePhoto(onResult: (PickerOutcome) -> Unit) {
        runOnMain {
            when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
                AVAuthorizationStatusAuthorized -> presentCamera(onResult)
                AVAuthorizationStatusNotDetermined -> {
                    AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                        runOnMain {
                            if (granted) presentCamera(onResult)
                            else onResult(PickerOutcome.PermissionDenied(permanent = false))
                        }
                    }
                }
                AVAuthorizationStatusDenied, AVAuthorizationStatusRestricted -> {
                    onResult(PickerOutcome.PermissionDenied(permanent = true))
                }
                else -> onResult(PickerOutcome.PermissionDenied(permanent = true))
            }
        }
    }

    override fun pickFromGallery(onResult: (PickerOutcome) -> Unit) {
        runOnMain {
            val status: PHAuthorizationStatus =
                PHPhotoLibrary.authorizationStatusForAccessLevel(PHAccessLevelReadWrite)
            when (status) {
                PHAuthorizationStatusAuthorized, PHAuthorizationStatusLimited -> presentGallery(onResult)
                PHAuthorizationStatusNotDetermined -> {
                    PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelReadWrite) { newStatus ->
                        runOnMain {
                            if (newStatus == PHAuthorizationStatusAuthorized ||
                                newStatus == PHAuthorizationStatusLimited
                            ) presentGallery(onResult)
                            else onResult(PickerOutcome.PermissionDenied(permanent = false))
                        }
                    }
                }
                PHAuthorizationStatusDenied, PHAuthorizationStatusRestricted ->
                    onResult(PickerOutcome.PermissionDenied(permanent = true))
                else -> onResult(PickerOutcome.PermissionDenied(permanent = true))
            }
        }
    }

    override fun openAppSettings() {
        runOnMain {
            val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return@runOnMain
            val app = UIApplication.sharedApplication
            if (app.canOpenURL(url)) {
                app.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
            }
        }
    }

    private fun presentCamera(onResult: (PickerOutcome) -> Unit) {
        val top = ViewControllerHolder.topViewController() ?: run {
            onResult(PickerOutcome.Error("No root view controller"))
            return
        }
        if (!UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)) {
            onResult(PickerOutcome.Error("Camera not available"))
            return
        }
        val picker = UIImagePickerController()
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        picker.allowsEditing = false
        val delegate = CameraPickerDelegate(onResult) { picker.dismissViewControllerAnimated(true, null) }
        currentCameraDelegate = delegate
        picker.delegate = delegate
        top.presentViewController(picker, animated = true, completion = null)
    }

    @OptIn(BetaInteropApi::class)
    private fun presentGallery(onResult: (PickerOutcome) -> Unit) {
        val top = ViewControllerHolder.topViewController() ?: run {
            onResult(PickerOutcome.Error("No root view controller"))
            return
        }
        val config = PHPickerConfiguration().apply {
            selectionLimit = 1L
            filter = PHPickerFilter.imagesFilter()
        }
        val picker = PHPickerViewController(configuration = config)
        val delegate = GalleryPickerDelegate(onResult) { picker.dismissViewControllerAnimated(true, null) }
        currentPickerDelegate = delegate
        picker.delegate = delegate
        top.presentViewController(picker, animated = true, completion = null)
    }
}

private class CameraPickerDelegate(
    private val onResult: (PickerOutcome) -> Unit,
    private val dismiss: () -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        runOnMain {
            dismiss()
            currentCameraDelegate = null
            if (image == null) {
                onResult(PickerOutcome.Canceled)
                return@runOnMain
            }
            val data = UIImageJPEGRepresentation(image, 0.92) ?: run {
                onResult(PickerOutcome.Error("Unable to encode captured image"))
                return@runOnMain
            }
            onResult(PickerOutcome.Success(data.toByteArray()))
        }
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        runOnMain {
            dismiss()
            currentCameraDelegate = null
            onResult(PickerOutcome.Canceled)
        }
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class GalleryPickerDelegate(
    private val onResult: (PickerOutcome) -> Unit,
    private val dismiss: () -> Unit,
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        val results = didFinishPicking.filterIsInstance<PHPickerResult>()
        if (results.isEmpty()) {
            runOnMain {
                dismiss()
                currentPickerDelegate = null
                onResult(PickerOutcome.Canceled)
            }
            return
        }
        val provider: NSItemProvider = results.first().itemProvider
        val imageTypeIdentifier = "public.image"
        if (!provider.hasItemConformingToTypeIdentifier(imageTypeIdentifier)) {
            runOnMain {
                dismiss()
                currentPickerDelegate = null
                onResult(PickerOutcome.Error("Selected item is not an image"))
            }
            return
        }
        provider.loadDataRepresentationForTypeIdentifier(imageTypeIdentifier) { data: NSData?, error: NSError? ->
            runOnMain {
                dismiss()
                currentPickerDelegate = null
                when {
                    error != null -> onResult(PickerOutcome.Error(error.localizedDescription))
                    data == null -> onResult(PickerOutcome.Error("No data returned"))
                    else -> onResult(PickerOutcome.Success(data.toByteArray()))
                }
            }
        }
    }
}

private fun runOnMain(block: () -> Unit) {
    NSOperationQueue.mainQueue.addOperationWithBlock(block)
}
