package com.example.elysiaapp.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Sci-Fi Cyber Color Palette
object SciFiColors {
    // Primary Colors - Neon Cyan/Electric Blue
    val NeonCyan = Color(0xFF00FFFF)
    val ElectricBlue = Color(0xFF0080FF)
    val DeepCyan = Color(0xFF008B8B)
    
    // Secondary Colors - Purple/Magenta
    val NeonPurple = Color(0xFFBB00FF)
    val ElectricPurple = Color(0xFF8A2BE2)
    val DeepPurple = Color(0xFF4B0082)
    
    // Accent Colors - Neon Green/Lime
    val NeonGreen = Color(0xFF00FF00)
    val ElectricLime = Color(0xFF32CD32)
    val DeepGreen = Color(0xFF008000)
    
    // Background Colors - Dark with hints of blue
    val DarkBackground = Color(0xFF0A0A0F)
    val DarkSurface = Color(0xFF1A1A2E)
    val DarkCard = Color(0xFF16213E)
    val DarkElevated = Color(0xFF0F3460)
    
    // Text Colors
    val TextPrimary = Color(0xFFE0E6ED)
    val TextSecondary = Color(0xFFB0C4DE)
    val TextAccent = Color(0xFF00FFFF)
    
    // Status Colors
    val ErrorRed = Color(0xFFFF3333)
    val WarningOrange = Color(0xFFFF8C00)
    val SuccessGreen = Color(0xFF00FF41)
    
    // Special Effects
    val GlowEffect = Color(0x4000FFFF)
    val BorderGlow = Color(0x8000FFFF)
}

val SciFiDarkColorScheme = darkColorScheme(
    primary = SciFiColors.NeonCyan,
    onPrimary = SciFiColors.DarkBackground,
    primaryContainer = SciFiColors.DeepCyan,
    onPrimaryContainer = SciFiColors.TextPrimary,
    
    secondary = SciFiColors.NeonPurple,
    onSecondary = SciFiColors.TextPrimary,
    secondaryContainer = SciFiColors.DeepPurple,
    onSecondaryContainer = SciFiColors.TextPrimary,
    
    tertiary = SciFiColors.NeonGreen,
    onTertiary = SciFiColors.DarkBackground,
    tertiaryContainer = SciFiColors.DeepGreen,
    onTertiaryContainer = SciFiColors.TextPrimary,
    
    background = SciFiColors.DarkBackground,
    onBackground = SciFiColors.TextPrimary,
    
    surface = SciFiColors.DarkSurface,
    onSurface = SciFiColors.TextPrimary,
    surfaceVariant = SciFiColors.DarkCard,
    onSurfaceVariant = SciFiColors.TextSecondary,
    
    error = SciFiColors.ErrorRed,
    onError = SciFiColors.TextPrimary,
    errorContainer = Color(0xFF4D1F1F),
    onErrorContainer = SciFiColors.ErrorRed,
    
    outline = SciFiColors.BorderGlow,
    outlineVariant = SciFiColors.GlowEffect,
    
    inverseSurface = SciFiColors.TextPrimary,
    inverseOnSurface = SciFiColors.DarkBackground,
    inversePrimary = SciFiColors.DeepCyan
)

val SciFiLightColorScheme = lightColorScheme(
    primary = SciFiColors.ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = SciFiColors.NeonCyan,
    onPrimaryContainer = SciFiColors.DarkBackground,
    
    secondary = SciFiColors.ElectricPurple,
    onSecondary = Color.White,
    secondaryContainer = SciFiColors.NeonPurple,
    onSecondaryContainer = SciFiColors.DarkBackground,
    
    tertiary = SciFiColors.ElectricLime,
    onTertiary = SciFiColors.DarkBackground,
    tertiaryContainer = SciFiColors.NeonGreen,
    onTertiaryContainer = SciFiColors.DarkBackground,
    
    background = Color(0xFFF8F8FF),
    onBackground = SciFiColors.DarkBackground,
    
    surface = Color.White,
    onSurface = SciFiColors.DarkBackground,
    surfaceVariant = Color(0xFFF0F8FF),
    onSurfaceVariant = SciFiColors.DarkSurface,
    
    error = SciFiColors.ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFE6E6),
    onErrorContainer = SciFiColors.ErrorRed,
    
    outline = SciFiColors.ElectricBlue,
    outlineVariant = SciFiColors.DeepCyan
)