package com.productcatalogue.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Brand palette. Placeholder values until the creative work lands; replacing them here restyles
 * every screen, because features only use MaterialTheme colour roles.
 */
private val Navy = Color(0xFF1F3A5F)
private val NavyLight = Color(0xFFA9C7FF)
private val Coral = Color(0xFFE4572E)
private val CoralLight = Color(0xFFFFB4A1)
private val Mist = Color(0xFFF7F8FA)
private val Ink = Color(0xFF111418)

internal val LightColors = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5E3FF),
    onPrimaryContainer = Color(0xFF001B3C),
    secondary = Coral,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBD1),
    onSecondaryContainer = Color(0xFF3B0900),
    background = Mist,
    surface = Mist,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFEEF1F5),
)

internal val DarkColors = darkColorScheme(
    primary = NavyLight,
    onPrimary = Color(0xFF00315F),
    primaryContainer = Color(0xFF284777),
    onPrimaryContainer = Color(0xFFD5E3FF),
    secondary = CoralLight,
    onSecondary = Color(0xFF5F1500),
    secondaryContainer = Color(0xFF862200),
    onSecondaryContainer = Color(0xFFFFDBD1),
    background = Ink,
    surface = Ink,
    surfaceContainer = Color(0xFF1C2026),
    surfaceContainerHigh = Color(0xFF262A31),
)
