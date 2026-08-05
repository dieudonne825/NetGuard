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

private val NetGuardColorScheme = lightColorScheme(
    primary = NetPrimaryCyan,
    onPrimary = Color.White,
    primaryContainer = NetAccentBlue,
    onPrimaryContainer = Color.White,
    secondary = NetSecondaryBlue,
    onSecondary = Color.White,
    background = NetDarkBackground,
    onBackground = NetTextPrimary,
    surface = NetCardSurface,
    onSurface = NetTextPrimary,
    surfaceVariant = NetCardBorder,
    onSurfaceVariant = NetTextSecondary,
    error = NetAlertRed,
    onError = Color.White
)

@Composable
fun NetGuardTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = NetGuardColorScheme,
        typography = Typography,
        content = content
    )
}

