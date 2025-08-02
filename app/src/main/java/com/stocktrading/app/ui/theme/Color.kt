package com.stocktrading.app.ui.theme

import androidx.compose.ui.graphics.Color

val BullGreen = Color(0xFF00C896)        // Bright success green for gains
val BearRed = Color(0xFFFF3D47)          // Clean error red for losses
val DarkBlue = Color(0xFF1A1D29)         // Professional dark primary
val LightBlue = Color(0xFF2D3142)        // Secondary dark blue
val AccentGold = Color(0xFFFFD700)       // Premium gold accent
val NeutralGray = Color(0xFF9B9B9B)      // Subtle text color

//  Dark Theme Colors
val DarkBackground = Color(0xFF0A0A0A)   // Very dark background (same as your image)
val DarkSurface = Color(0xFF1F1F1F)      // Lighter card surfaces for better contrast
val DarkSurfaceVariant = Color(0xFF2E2E2E) // Even lighter variant surfaces
val DarkOnSurface = Color(0xFFFFFFFF)    // White text on dark
val DarkOnSurfaceVariant = Color(0xFFB0B0B0) // Gray text

//  Light Theme Colors
val LightBackground = Color(0xFFFFFFFF)  // Pure white background
val LightSurface = Color(0xFFFAFAFA)     // Light card background
val LightSurfaceVariant = Color(0xFFF5F5F5) // Variant surfaces
val LightOnSurface = Color(0xFF1A1A1A)   // Dark text on light
val LightOnSurfaceVariant = Color(0xFF666666) // Gray text

//  Gradient colors for  card effects
val GradientBlue1 = Color(0xFF4F46E5)    // Primary gradient start
val GradientBlue2 = Color(0xFF7C3AED)    // Primary gradient end
val GradientGreen1 = Color(0xFF059669)   // Success gradient start
val GradientGreen2 = Color(0xFF0891B2)   // Success gradient end
val GradientRed1 = Color(0xFFDC2626)     // Error gradient start
val GradientRed2 = Color(0xFFEF4444)     // Error gradient end

//  Section Colors
val GainersGreen = Color(0xFF4ADE80)     // Green for Top Gainers
val LosersRed = Color(0xFFEF4444)        // Red for Top Losers  
val ActiveBlue = Color(0xFF3B82F6)       // Blue for Most Active

// Glass effect colors
val GlassWhite = Color(0xFFFFFFFF)       // White with alpha for glass effect
val GlassBlack = Color(0xFF000000)       // Black with alpha for glass effect
val GlassBorder = Color(0xFFE2E8F0)      // Subtle border for glass cards

// Legacy colors
val Purple80 = GradientBlue1
val PurpleGrey80 = NeutralGray
val Pink80 = AccentGold

val Purple40 = DarkBlue
val PurpleGrey40 = LightBlue
val Pink40 = BearRed
