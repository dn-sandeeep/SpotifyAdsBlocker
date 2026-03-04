package com.sandeep.admuterapp.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1d6ef0),
    onPrimary = Color.White,
    secondary = Color(0xFFb160f0),
    onSecondary = Color.White,
    background = Color(0xFF121212),
    surface = Color(0xFFb160f0),
    onSurface = Color.White,
)

@Composable
fun AdMuterAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
