package com.productcatalogue.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * The single source of look and feel: colour (Color.kt), type (Type.kt) and shape (Shape.kt).
 * Dynamic colour is off on purpose so the brand palette wins over the wallpaper.
 */
@Composable
fun CatalogueTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = CatalogueTypography,
        shapes = CatalogueShapes,
        content = content,
    )
}
