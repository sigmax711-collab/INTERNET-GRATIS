package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
    darkColorScheme(
        primary = ArgentineBlue,
        onPrimary = CyberBackground,
        primaryContainer = CyberSurface,
        onPrimaryContainer = CyberCyan,
        secondary = CyberCyan,
        onSecondary = CyberBackground,
        tertiary = SunGold,
        onTertiary = CyberBackground,
        background = CyberBackground,
        onBackground = TextWhite,
        surface = CyberSurface,
        onSurface = TextWhite,
        surfaceVariant = CyberPanel,
        onSurfaceVariant = TextGray,
        error = AlertRed,
        onError = TextWhite
    )

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

