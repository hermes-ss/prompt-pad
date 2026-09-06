package com.hermes.promptpad

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Black = Color.Black
val White = Color.White
val Accent = Color(0xFFFC7703)
val DotIdle = Color(0xFF777777)
val DotBad = Color(0xFFFF453A)
val Dim = Color(0xFFE6E6E6)

private val LatoFamily = FontFamily(
    Font(R.font.lato_regular, FontWeight.Normal),
    Font(R.font.lato_bold, FontWeight.Bold),
)

@Composable
fun MinimalTheme(prefs: Prefs, revision: Int = 0, content: @Composable () -> Unit) {
    val scale = remember(revision) { prefs.textScale / 100f }
    fun style(size: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
        fontFamily = LatoFamily,
        fontSize = (size * scale).sp,
        fontWeight = weight,
        color = White,
    )
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent, onPrimary = Black, background = Black, onBackground = White,
            surface = Black, onSurface = White, surfaceVariant = Black, onSurfaceVariant = Dim,
            secondary = Accent, error = DotBad,
        ),
        typography = Typography(
            headlineMedium = style(26, FontWeight.Bold),
            headlineSmall = style(20, FontWeight.Bold),
            titleMedium = style(15, FontWeight.Bold),
            bodyMedium = style(13),
            bodySmall = style(11),
            labelSmall = style(10),
        ),
        content = content,
    )
}
