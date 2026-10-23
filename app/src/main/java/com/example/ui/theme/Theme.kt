package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
    darkColorScheme(
        primary = LockerBluePrimaryDark,
        onPrimary = LockerBlueOnPrimaryDark,
        primaryContainer = LockerBlueContainerDark,
        onPrimaryContainer = LockerOnBlueContainerDark,
        secondary = LockerSecondary,
        secondaryContainer = LockerSecondaryContainer,
        background = LockerBackgroundDark,
        surface = LockerSurfaceDark,
        surfaceVariant = LockerSurfaceVariantDark,
        onBackground = LockerTextPrimaryDark,
        onSurface = LockerTextPrimaryDark,
        outline = LockerOutline
    )

private val LightColorScheme =
    lightColorScheme(
        primary = LockerBluePrimary,
        onPrimary = LockerBlueOnPrimary,
        primaryContainer = LockerBlueContainer,
        onPrimaryContainer = LockerOnBlueContainer,
        secondary = LockerSecondary,
        onSecondary = Color.White,
        secondaryContainer = LockerSecondaryContainer,
        onSecondaryContainer = LockerOnSecondaryContainer,
        background = LockerBackground,
        surface = LockerSurface,
        surfaceVariant = LockerSurfaceVariant,
        onBackground = LockerTextPrimary,
        onSurface = LockerTextPrimary,
        onSurfaceVariant = LockerTextSecondary,
        outline = LockerOutline,
        outlineVariant = LockerOutlineLight
    )

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional Blue & White branding
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
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
