package com.example.votingappnew.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color // أضفنا الـ Import ده
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DeepBlue40,
    secondary = DeepGreen40,
    tertiary = DarkGray40,
    background = DarkGray40,
    surface = DarkGray40,
    onPrimary = CreamWhite80,
    onSecondary = CreamWhite80,
    onBackground = CreamWhite80,
    onSurface = CreamWhite80
)

private val LightColorScheme = lightColorScheme(
    primary = SoftBlue80,
    secondary = SoftGreen80,
    tertiary = CreamWhite80,
    background = CreamWhite80,
    surface = CreamWhite80,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun VotingAppNewTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}