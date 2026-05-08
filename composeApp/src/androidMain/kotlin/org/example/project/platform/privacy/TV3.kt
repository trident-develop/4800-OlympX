package org.example.project.platform.privacy

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.example.project.android.buildD
import org.example.project.db.GameRepo
import org.example.project.event.TVEvent
import org.example.project.media.postback
import org.example.project.media.regToken
import org.example.project.media.requestNotify

@SuppressLint("ViewConstructor")
class TV3(
    private val activity: ComponentActivity,
    private val scoreRepo: GameRepo,
) : WebView(activity) {
    private val contentRoot: FrameLayout = FrameLayout(activity)
    private var validTarget = false

    private var latestIntent: Intent? = null

    fun updateIntent(intent: Intent?) {
        latestIntent = intent
    }
    private val _events = MutableSharedFlow<TVEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()
    val popupContainer: FrameLayout = FrameLayout(activity).apply {
        isVisible = false
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
    }

    val fullscreenContainer: FrameLayout = FrameLayout(activity).apply {
        isVisible = false
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
    }

    private var contentCallback: ValueCallback<Array<Uri>>? = null

    private val viewClient = TV2(
        activity = activity,
        onStarted = { _, _ ->
            contentCallback?.onReceiveValue(null)
            contentCallback = null
        },
        onFinished = { _, url ->
            CoroutineScope(Dispatchers.IO).launch {
                runCatching { CookieManager.getInstance().flush() }

                handleUrl(url)
            }
        }
    )
    private val chromeClient = TV1(activity, this, viewClient)


    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {

            if (popupContainer.childCount > 0) {
                val top = popupContainer.getChildAt(popupContainer.childCount - 1) as WebView

                if (top.canGoBack()) {
                    top.goBack()
                } else {
                    top.stopLoading()
                    popupContainer.removeView(top)
                    top.destroy()
                    popupContainer.isVisible = popupContainer.childCount > 0
                }
                return
            }

            if (canGoBack()) {
                goBack()
            }
        }
    }

    init {
        contentRoot.addView(
            this,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        contentRoot.addView(popupContainer)
        contentRoot.addView(fullscreenContainer)

        contentRoot.isVisible = false

        activity.onBackPressedDispatcher.addCallback(activity, backPressedCallback)

        boardSett(this, chromeClient, viewClient)
    }


    override fun destroy() {
        chromeClient.onDestroy()
        backPressedCallback.remove()
        super.destroy()
    }

    fun setViewVisibility(isVisible: Boolean) {
        activity.runOnUiThread {

            val content = activity.findViewById<ViewGroup>(android.R.id.content)

            content.removeAllViews()

            if (contentRoot.parent == null) {
                content.addView(
                    contentRoot,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            } else {
                content.bringChildToFront(contentRoot)
            }

            contentRoot.isVisible = isVisible

            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            this@TV3.requestFocus()

            CoroutineScope(Dispatchers.IO).launch {
                if (!scoreRepo.isNotifyShown()) {
                    requestNotify(activity.activityResultRegistry)
                    scoreRepo.markNotifyShown()
                }
            }
        }
    }

    private suspend fun handleUrl(url: String?) {
        when {
            url?.startsWith(buildD(198456)) == true -> {
                scoreRepo.saveScore(url)
                _events.tryEmit(TVEvent.OpenGame)
            }

            !validTarget && url?.startsWith(buildD(198456)) == false -> {
                validTarget = true
                scoreRepo.saveScore(url)
                regToken()
                postback(latestIntent)
                setViewVisibility(true)
            }
        }
    }
}