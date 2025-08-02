package com.stocktrading.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

//  Dark Theme Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = BullGreen,                    // Primary actions - success green
    secondary = AccentGold,                 // Secondary actions - premium gold
    tertiary = BullGreen,                   // Tertiary - blue accent
    background = DarkBackground,            // Very dark background
    surface = DarkSurface,                  // Dark card surfaces
    surfaceVariant = DarkSurfaceVariant,    // Variant surfaces
    onPrimary = Color.White,                // Text on primary
    onSecondary = DarkBlue,                 // Text on secondary  
    onBackground = DarkOnSurface,           // White text on dark background
    onSurface = DarkOnSurface,              // White text on cards
    onSurfaceVariant = DarkOnSurfaceVariant, // Gray text for subtitles
    error = BearRed,                        // Error states - bear red
    onError = Color.White,                  // White text on error
    outline = Color(0xFF404040),            // Subtle outlines
    outlineVariant = Color(0xFF303030),     // Variant outlines
    inverseSurface = Color.White,
    inverseOnSurface = Color.Black,
    inversePrimary = DarkBlue
)

// ️ Light Theme Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = DarkBlue,                     // Professional dark blue primary
    secondary = AccentGold,                 // Premium gold secondary
    tertiary = BullGreen,                   // Success green tertiary
    background = LightBackground,           // Pure white background
    surface = LightSurface,                 // Light card surface
    surfaceVariant = LightSurfaceVariant,   // Variant surfaces
    onPrimary = Color.White,                // White text on primary
    onSecondary = DarkBlue,                 // Dark text on gold
    onBackground = LightOnSurface,          // Dark text on light background
    onSurface = LightOnSurface,             // Dark text on cards
    onSurfaceVariant = LightOnSurfaceVariant, // Gray text for subtitles
    error = BearRed,                        // Error states
    onError = Color.White,                  // White text on error
    outline = Color(0xFFE0E0E0),            // Light outlines
    outlineVariant = Color(0xFFF0F0F0),     // Variant outlines
    inverseSurface = Color(0xFF1A1A1A),
    inverseOnSurface = Color.White,
    inversePrimary = BullGreen
)

val LocalThemeMode = staticCompositionLocalOf { ThemeMode.SYSTEM }

@Composable
fun StockTradingAppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalThemeMode provides themeMode) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object StockTradingColors {
    @Composable
    fun gainersColor(): Color = GainersGreen
    
    @Composable
    fun losersColor(): Color = LosersRed
    
    @Composable
    fun activeColor(): Color = ActiveBlue
    
    @Composable
    fun sectionBackground(): Color = MaterialTheme.colorScheme.surface
    
    @Composable
    fun cardBackground(): Color = MaterialTheme.colorScheme.surfaceVariant
    
    @Composable
    fun textPrimary(): Color = MaterialTheme.colorScheme.onSurface
    
    @Composable
    fun textSecondary(): Color = MaterialTheme.colorScheme.onSurfaceVariant
    
    @Composable
    fun isDarkTheme(): Boolean = LocalThemeMode.current == ThemeMode.DARK || 
        (LocalThemeMode.current == ThemeMode.SYSTEM && isSystemInDarkTheme())
}
