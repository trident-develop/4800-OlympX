package org.example.project.platform.privacy

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Message
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebChromeClient.CustomViewCallback
import android.webkit.WebChromeClient.FileChooserParams
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import java.io.File
import java.io.IOException

class TV1(
    private val activity: ComponentActivity,
    private val TV3: TV3,
    private val TV2: TV2
) : WebChromeClient() {


    private var contentCallback: ValueCallback<Array<Uri>>? = null
    private var contentPermissionRequested: Boolean = false
    private var acceptTypes: Array<String>? = null
    private var allowMultiple: Boolean = false
    private var captureEnabled: Boolean = false
    private var videoOutputFileUri: Uri? = null
    private var imageOutputFileUri: Uri? = null
    private var videoCallback: PermissionRequest? = null
    private var videoPermissionRequested: Boolean = false

    private var customView: View? = null
    private var customViewCallback: CustomViewCallback? = null


    private val contentLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (contentCallback == null) return@registerForActivityResult
        var results: Array<Uri?>? = null
        if (result.resultCode == Activity.RESULT_OK) {
            results = getSelectedFiles(result.data)
        }
        val arr = results?.filterNotNull()?.toTypedArray() ?: emptyArray()
        contentCallback?.onReceiveValue(arr)
        contentCallback = null
        imageOutputFileUri = null
        videoOutputFileUri = null
        acceptTypes = null
    }


    private val videoLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (contentPermissionRequested) {
            contentPermissionRequested = false
            startPickerIntent(granted)
        }
        if (videoPermissionRequested) {
            videoPermissionRequested = false
            if (granted) {
                videoCallback?.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
            } else {
                videoCallback?.deny()
            }
            videoCallback = null
        }
    }


    override fun onPermissionRequest(request: PermissionRequest?) {
        request ?: return
        if (!request.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
            request.deny()
            return
        }
        val ctx = activity
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            request.grant(request.resources)
        } else {
            videoCallback = request
            videoPermissionRequested = true
            videoLauncher.launch(Manifest.permission.CAMERA)
        }
    }


    override fun onShowFileChooser(
        webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?
    ): Boolean {
        contentCallback = filePathCallback
        acceptTypes = fileChooserParams?.acceptTypes
        allowMultiple = fileChooserParams?.mode == FileChooserParams.MODE_OPEN_MULTIPLE
        captureEnabled = fileChooserParams?.isCaptureEnabled ?: false
        return startPickerIntent(false)
    }

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        if (view == null) return
        if (customView != null) {
            callback?.onCustomViewHidden()
            return
        }
        customView = view
        customViewCallback = callback
        TV3.isVisible = false
        TV3.popupContainer.isVisible = false
        TV3.fullscreenContainer.apply {
            removeAllViews()
            addView(
                view,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
            isVisible = true
        }
    }
    override fun onHideCustomView() {
        val view = customView ?: return
        TV3.fullscreenContainer.apply {
            removeView(view)
            isVisible = false
        }
        TV3.isVisible = true
        TV3.popupContainer.isVisible = TV3.popupContainer.isNotEmpty()
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
        customView = null
    }
    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        if (resultMsg == null) return false
        val popup = WebView(activity)
        boardSett(popup, this, TV2)
        TV3.popupContainer.isVisible = true
        TV3.popupContainer.addView(
            popup,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        val transport = resultMsg.obj as WebView.WebViewTransport
        transport.webView = popup
        resultMsg.sendToTarget()
        return true
    }
    override fun onCloseWindow(window: WebView?) {
        val popup = window ?: return
        (popup.parent as? ViewGroup)?.removeView(popup)
        TV3.popupContainer.isVisible = TV3.popupContainer.isNotEmpty()
        popup.stopLoading()
        popup.destroy()
    }


    fun onDestroy() {
        contentLauncher.unregister()
        videoLauncher.unregister()
    }


    private fun startPickerIntent(grantedCameraPermission: Boolean): Boolean {
        val ctx = activity
        val acceptImages = acceptsImagesFun(acceptTypes)
        val acceptVideos = acceptsVideoFun(acceptTypes)
        var pickerIntent: Intent? = null


        if (captureEnabled) {
            if (grantedCameraPermission || !needsCameraPermission()) {
                if (acceptImages) pickerIntent = getPhotoIntent()
                else if (acceptVideos) pickerIntent = getVideoIntent()
            } else {
                contentPermissionRequested = true
                videoLauncher.launch(Manifest.permission.CAMERA)
                return true
            }
        }


        if (pickerIntent == null) {
            val extraIntents = arrayListOf<Intent>()
            if (!needsCameraPermission()) {
                if (acceptImages) extraIntents.add(getPhotoIntent())
                if (acceptVideos) extraIntents.add(getVideoIntent())
            }
            val fileSelectionIntent = getFileChooserIntent(acceptTypes, allowMultiple)
            pickerIntent = Intent(Intent.ACTION_CHOOSER).apply {
                putExtra(Intent.EXTRA_INTENT, fileSelectionIntent)
                putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents.toTypedArray())
            }
        }


        pickerIntent.let {
            ctx.packageManager?.let { pm ->
                if (it.resolveActivity(pm) != null) {
                    contentLauncher.launch(it)
                    return true
                }
            }
        }
        return false
    }


    private fun getFileChooserIntent(acceptTypes: Array<String>?, allowMultiple: Boolean): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = DEFAULT_MIME_TYPES
        intent.putExtra(Intent.EXTRA_MIME_TYPES, getAcceptedMimeType(acceptTypes))
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple)
        return intent
    }


    private fun getPhotoIntent(): Intent {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageOutputFileUri = getOutputUri(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageOutputFileUri)
        return intent
    }


    private fun getVideoIntent(): Intent {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        videoOutputFileUri = getOutputUri(MediaStore.ACTION_VIDEO_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, videoOutputFileUri)
        return intent
    }


    private fun getOutputUri(intentType: String): Uri? {
        val ctx = activity
        val file = try { getCapturedFile(intentType) } catch (e: IOException) { null } ?: return null
        val authority = ctx.packageName + ".fileprovider"
        return try { FileProvider.getUriForFile(ctx, authority, file) } catch (e: Exception) { null }
    }


    private fun getCapturedFile(intentType: String): File? {
        val ctx = activity
        val dir = ctx.getExternalFilesDir(null) ?: return null
        return when (intentType) {
            MediaStore.ACTION_IMAGE_CAPTURE -> File.createTempFile("image", ".jpg", dir)
            MediaStore.ACTION_VIDEO_CAPTURE -> File.createTempFile("video", ".mp4", dir)
            else -> null
        }
    }


    private fun getSelectedFiles(data: Intent?): Array<Uri?>? {
        data?.data?.let { return FileChooserParams.parseResult(Activity.RESULT_OK, data) }
        data?.clipData?.let { clip ->
            val arr = Array(clip.itemCount) { i -> clip.getItemAt(i).uri }
            return arr
        }
        val mediaUri = getCapturedMediaFile()
        return if (mediaUri != null) arrayOf(mediaUri) else null
    }


    private fun getCapturedMediaFile(): Uri? {
        if (imageOutputFileUri != null && isFileNotEmpty(imageOutputFileUri)) return imageOutputFileUri
        if (videoOutputFileUri != null && isFileNotEmpty(videoOutputFileUri)) return videoOutputFileUri
        return null
    }


    private fun isFileNotEmpty(uri: Uri?): Boolean {
        val ctx = activity
        if (uri == null) return false
        return try {
            ctx.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length > 0 } == true
        } catch (e: IOException) {
            false
        }
    }


    private fun needsCameraPermission(): Boolean {
        val ctx = activity
        val pm = ctx.packageManager
        val requestedPermissions = try {
            pm.getPackageInfo(ctx.packageName, PackageManager.GET_PERMISSIONS).requestedPermissions
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        return requestedPermissions?.contains(Manifest.permission.CAMERA) == true && ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
    }


    private fun acceptsAny(types: Array<String>?): Boolean {
        if (types.isNullOrEmpty()) return true
        return types.any { it == DEFAULT_MIME_TYPES }
    }


    private fun acceptsImagesFun(types: Array<String>?): Boolean {
        val mimeTypes = getAcceptedMimeType(types)
        return acceptsAny(types) || mimeTypes.any { it?.contains("image") == true }
    }


    private fun acceptsVideoFun(types: Array<String>?): Boolean {
        val mimeTypes = getAcceptedMimeType(types)
        return acceptsAny(types) || mimeTypes.any { it?.contains("video") == true }
    }


    private fun getAcceptedMimeType(types: Array<String>?): Array<String?> {
        if (types.isNullOrEmpty()) return arrayOf(DEFAULT_MIME_TYPES)
        val list = mutableListOf<String?>()
        for (t in types) {
            if (t.matches("\\.\\w+".toRegex())) {
                val ext = t.replace(".", "")
                val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
                if (mime != null) list.add(mime)
            } else if (t.isNotEmpty()) {
                list.add(t)
            }
        }
        if (list.isEmpty()) return arrayOf(DEFAULT_MIME_TYPES)
        return list.toTypedArray()
    }


    companion object {
        private const val DEFAULT_MIME_TYPES = "*/*"
    }
}