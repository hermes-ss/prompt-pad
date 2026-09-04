package com.hermes.promptpad

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val Charcoal = Color(0xFF1C1C1E)
val Accent = Color(0xFFF26609)
val DotIdle = Color(0xFF444446)
val DotBad = Color(0xFFFF453A)
val Dim = Color(0xFFE6E6E6)
val Color0E = Color(0xFF0E0E0E)
val Border = Color(0xFF2A2A2A)

/** 0 sans, 1 serif, 2 "easier to read" (monospace: widest glyph separation, best on e-ink). */
fun fontFor(profile: Int): FontFamily = when (profile) {
    1 -> FontFamily.Serif
    2 -> FontFamily.Monospace
    else -> FontFamily.SansSerif
}

@Composable
fun MinimalTheme(prefs: Prefs, content: @Composable () -> Unit) {
    @Suppress("UNUSED_EXPRESSION") isSystemInDarkTheme() // ponytail: dark-only, light theme never built.
    val f = fontFor(prefs.fontProfile)
    val s = prefs.textScale / 100f
    val easy = prefs.fontProfile == 2
    fun st(size: Int, w: FontWeight = FontWeight.Normal) =
        TextStyle(fontFamily = f, fontSize = (size * s).sp, fontWeight = w, color = White,
            letterSpacing = if (easy) 0.05.sp * size else 0.sp)
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent, onPrimary = Black, background = Black, onBackground = White,
            surface = Charcoal, onSurface = White, surfaceVariant = Charcoal, onSurfaceVariant = Dim,
            secondary = Accent, error = DotBad
        ),
        typography = Typography(
            headlineSmall = st(20, FontWeight.Medium),
            titleMedium = st(15, FontWeight.Medium),
            bodyMedium = st(13),
            bodySmall = st(11),
            labelSmall = st(10)
        ),
        content = content
    )
}
