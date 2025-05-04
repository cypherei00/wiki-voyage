package com.example.wikipedia_app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TealCyan,
    secondary = RustyRed,
    tertiary = DarkBrown,
    background = BackgroundBeige,
    surface = BackgroundBeige,
    onPrimary = CreamOffWhite,
    onSecondary = CreamOffWhite,
    onTertiary = CreamOffWhite,
    onBackground = DarkBrown,
    onSurface = DarkBrown
)

private val LightColorScheme = lightColorScheme(
    primary = TealCyan,
    secondary = RustyRed,
    tertiary = DarkBrown,
    background = BackgroundBeige,
    surface = BackgroundBeige,
    onPrimary = CreamOffWhite,
    onSecondary = CreamOffWhite,
    onTertiary = CreamOffWhite,
    onBackground = DarkBrown,
    onSurface = DarkBrown
)

@Composable
fun WikipediaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to use our custom color scheme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}