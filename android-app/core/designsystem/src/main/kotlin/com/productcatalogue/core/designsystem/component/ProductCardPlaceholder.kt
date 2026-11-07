package com.productcatalogue.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/** The skeleton of a [ProductCard], shown while products load. */
@Composable
fun ProductCardPlaceholder(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f).shimmer())
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Line(widthFraction = 0.9f)
            Line(widthFraction = 0.6f)
            Line(widthFraction = 0.4f, height = 20)
        }
    }
}

@Composable
private fun Line(widthFraction: Float, height: Int = 14) {
    Box(
        Modifier
            .fillMaxWidth(widthFraction)
            .height(height.dp)
            .clip(RoundedCornerShape(4.dp))
            .shimmer(),
    )
}
