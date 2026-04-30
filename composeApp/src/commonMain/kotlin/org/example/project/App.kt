package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource

import olympx.composeapp.generated.resources.Res
import olympx.composeapp.generated.resources.compose_multiplatform
import org.example.project.screens.LoadingScreen
import org.example.project.screens.NoInternetScreen

@Composable
@Preview
fun App() {
    Gray(
        loading = { LoadingScreen() },
        noInternet = { NoInternetScreen(it) },
        white = { AppNavGraph() }
    )
}