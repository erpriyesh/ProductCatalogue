package com.productcatalogue.feature.catalogue.presentation.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.filled.Woman
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * An icon for each known category, so category links are recognisable at a glance. Unknown
 * categories (a new data source may add some) get a generic icon rather than breaking.
 */
internal fun categoryIcon(slug: String): ImageVector = when (slug) {
    "electronics" -> Icons.Filled.Devices
    "jewelery", "jewellery" -> Icons.Filled.Diamond
    "mens-clothing" -> Icons.Filled.Man
    "womens-clothing" -> Icons.Filled.Woman
    else -> Icons.Filled.Category
}
