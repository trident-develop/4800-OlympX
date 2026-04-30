package org.example.project.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset

enum class ThemeMode { Dark, Light }

object MythColors {
    var BgAbyss by mutableStateOf(Color(0xFF030616))
        private set
    var BgDeep by mutableStateOf(Color(0xFF070D22))
        private set
    var BgMid by mutableStateOf(Color(0xFF0B1437))
        private set
    var Surface by mutableStateOf(Color(0xFF111F4A))
        private set
    var SurfaceHigh by mutableStateOf(Color(0xFF16265C))
        private set
    var SurfaceGlow by mutableStateOf(Color(0xFF1E3480))
        private set

    var Cyan by mutableStateOf(Color(0xFF4AD8E6))
        private set
    var CyanBright by mutableStateOf(Color(0xFF7BE8F5))
        private set
    var Sky by mutableStateOf(Color(0xFF7DD3FC))
        private set
    var Azure by mutableStateOf(Color(0xFF3B82F6))
        private set
    var Cobalt by mutableStateOf(Color(0xFF2449C2))
        private set
    var Indigo by mutableStateOf(Color(0xFF3F3AE0))
        private set
    var Electric by mutableStateOf(Color(0xFF6FB8FF))
        private set

    var Gold by mutableStateOf(Color(0xFFFFD27A))
        private set
    var GoldBright by mutableStateOf(Color(0xFFFFE9AE))
        private set
    var Crimson by mutableStateOf(Color(0xFFE05B6A))
        private set
    var Emerald by mutableStateOf(Color(0xFF47D6A1))
        private set

    var TextPrimary by mutableStateOf(Color(0xFFEAF4FF))
        private set
    var TextSecondary by mutableStateOf(Color(0xFFA9C2E4))
        private set
    var TextMuted by mutableStateOf(Color(0xFF6F85B0))
        private set

    var DividerSoft by mutableStateOf(Color(0x338FB7FF))
        private set
    var RuneGlow by mutableStateOf(Color(0x884AD8E6))
        private set

    val BackdropGradient: Brush
        get() = Brush.radialGradient(
            colors = listOf(
                SurfaceGlow.copy(alpha = 0.55f),
                BgMid,
                BgAbyss
            ),
            center = Offset(0.5f, 0.25f),
            radius = 1600f
        )

    val CardGradient: Brush
        get() = Brush.linearGradient(
            colors = listOf(
                SurfaceHigh,
                Surface.copy(alpha = 0.92f),
                BgMid.copy(alpha = 0.95f)
            )
        )

    val AccentGradient: Brush
        get() = Brush.linearGradient(
            colors = listOf(Cyan, Azure, Indigo)
        )

    val GoldGradient: Brush
        get() = Brush.linearGradient(
            colors = listOf(GoldBright, Gold, Color(0xFFE2A64A))
        )

    internal fun applyTheme(mode: ThemeMode) {
        when (mode) {
            ThemeMode.Dark -> applyDark()
            ThemeMode.Light -> applyLight()
        }
    }

    private fun applyDark() {
        BgAbyss = Color(0xFF030616)
        BgDeep = Color(0xFF070D22)
        BgMid = Color(0xFF0B1437)
        Surface = Color(0xFF111F4A)
        SurfaceHigh = Color(0xFF16265C)
        SurfaceGlow = Color(0xFF1E3480)

        Cyan = Color(0xFF4AD8E6)
        CyanBright = Color(0xFF7BE8F5)
        Sky = Color(0xFF7DD3FC)
        Azure = Color(0xFF3B82F6)
        Cobalt = Color(0xFF2449C2)
        Indigo = Color(0xFF3F3AE0)
        Electric = Color(0xFF6FB8FF)

        Gold = Color(0xFFFFD27A)
        GoldBright = Color(0xFFFFE9AE)
        Crimson = Color(0xFFE05B6A)
        Emerald = Color(0xFF47D6A1)

        TextPrimary = Color(0xFFEAF4FF)
        TextSecondary = Color(0xFFA9C2E4)
        TextMuted = Color(0xFF6F85B0)

        DividerSoft = Color(0x338FB7FF)
        RuneGlow = Color(0x884AD8E6)
    }

    private fun applyLight() {
        // Medium-dark blue palette — visibly lighter than Midnight, yet dark enough
        // to keep light text comfortably readable across every surface.
        BgAbyss = Color(0xFF1E3558)
        BgDeep = Color(0xFF25416B)
        BgMid = Color(0xFF2C4C7C)
        Surface = Color(0xFF34578E)
        SurfaceHigh = Color(0xFF3D63A1)
        SurfaceGlow = Color(0xFF4972B5)

        Cyan = Color(0xFF6BDCEA)
        CyanBright = Color(0xFF9FEDF7)
        Sky = Color(0xFF9BD9FC)
        Azure = Color(0xFF5A9BFC)
        Cobalt = Color(0xFF3E6BD8)
        Indigo = Color(0xFF5A54F0)
        Electric = Color(0xFF8AC8FF)

        Gold = Color(0xFFFFD27A)
        GoldBright = Color(0xFFFFE9AE)
        Crimson = Color(0xFFE26876)
        Emerald = Color(0xFF5FE0B1)

        TextPrimary = Color(0xFFF7FBFF)
        TextSecondary = Color(0xFFCFDFF5)
        TextMuted = Color(0xFF9AB0D0)

        DividerSoft = Color(0x55A7C0E6)
        RuneGlow = Color(0x996BDCEA)
    }
}
