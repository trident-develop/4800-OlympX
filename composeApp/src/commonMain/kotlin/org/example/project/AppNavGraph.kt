package org.example.project

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import org.example.project.media.MediaPickerHost
import org.example.project.screens.journey.JourneyScreen
import org.example.project.screens.play.PlayScreen
import org.example.project.screens.profile.ProfileScreen
import org.example.project.screens.webview.WebViewScreen
import org.example.project.screens.world.WorldScreen
import org.example.project.ui.effects.AuroraBackground
import org.example.project.ui.effects.ParticleField
import org.example.project.ui.nav.MythicBottomBar
import org.example.project.ui.nav.NavTab

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppNavGraph() {
    MediaPickerHost {
        var currentTab by remember { mutableStateOf(NavTab.World) }
        var webViewUrl by remember { mutableStateOf<String?>(null) }

        // Close the web view overlay first when it's open.
        BackHandler(enabled = webViewUrl != null) {
            webViewUrl = null
        }
        // On non-World tabs, system back sends the user to World.
        // On World (with no overlay), the handler is disabled so the
        // OS takes over and the app exits.
        BackHandler(enabled = webViewUrl == null && currentTab != NavTab.World) {
            currentTab = NavTab.World
        }

        AuroraBackground(modifier = Modifier.fillMaxSize()) {
            ParticleField(count = 26, seed = 17L)
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    MythicBottomBar(
                        current = currentTab,
                        onSelect = { currentTab = it },
                        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                    )
                }
            ) { inner ->
                val ld = LocalLayoutDirection.current
                val pad = PaddingValues(
                    start = inner.calculateStartPadding(ld),
                    end = inner.calculateEndPadding(ld),
                    top = inner.calculateTopPadding(),
                    bottom = 30.dp,
                )
                Box(Modifier.fillMaxSize().padding(pad)) {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            (fadeIn(tween(260)) + scaleIn(initialScale = 0.985f, animationSpec = tween(260)))
                                .togetherWith(fadeOut(tween(180)) + scaleOut(targetScale = 1.015f, animationSpec = tween(180)))
                        },
                        label = "tabs"
                    ) { tab ->
                        when (tab) {
                            NavTab.World -> WorldScreen()
                            NavTab.Journey -> JourneyScreen()
                            NavTab.Play -> PlayScreen()
                            NavTab.Profile -> ProfileScreen(onOpenWeb = { webViewUrl = it })
                        }
                    }
                }
            }
        }

        if (webViewUrl != null) {
            WebViewScreen(url = webViewUrl!!, onClose = { webViewUrl = null })
        }
    }
}
