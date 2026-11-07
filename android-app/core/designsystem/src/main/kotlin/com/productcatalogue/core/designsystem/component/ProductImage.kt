package com.productcatalogue.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * Product photos are shot on white, so they sit on white in both themes and are fitted, not
 * cropped, so the whole product is visible. Coil caches images in memory and on disk.
 */
@Composable
fun ProductImage(url: String, modifier: Modifier = Modifier, contentDescription: String? = null) {
    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier.background(Color.White).padding(12.dp),
    )
}
