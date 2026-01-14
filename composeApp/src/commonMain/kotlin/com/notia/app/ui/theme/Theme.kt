package com.notia.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = ErrorLight,
    onError = OnErrorLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

val PremiumDarkColorScheme = darkColorScheme(
    primary = NeonBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0040DD),
    onPrimaryContainer = Color.White,
    secondary = NeonIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3F3DBE),
    onSecondaryContainer = Color.White,
    tertiary = NeonOrange,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFBD7B00),
    onTertiaryContainer = Color.White,
    background = PremiumBackground,
    onBackground = TextPrimary,
    surface = PremiumSurface,
    onSurface = TextPrimary,
    surfaceVariant = PremiumSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = NeonRed,
    onError = Color.White,
    outline = TextTertiary,
    outlineVariant = Color(0xFF44474F)
)

// Default to Premium Dark for this MVP
val DarkColorScheme = PremiumDarkColorScheme

@Composable
expect fun NotiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
)
