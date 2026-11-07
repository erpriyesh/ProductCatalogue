package com.productcatalogue.core.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.productcatalogue.core.model.Rating
import java.util.Locale

/**
 * Star, average and how many people rated it. The count is always shown next to the score:
 * 5.0 from one review and 4.8 from hundreds should not look the same.
 * [compact] shows "(319)" for tight spaces; otherwise "319 reviews". TalkBack reads one sentence.
 */
@Composable
fun RatingRow(rating: Rating, modifier: Modifier = Modifier, compact: Boolean = true) {
    val average = String.format(Locale.getDefault(), "%.1f", rating.average)
    val reviews = if (rating.count == 1) "1 review" else "${rating.count} reviews"
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "Rated $average out of 5 from $reviews"
        },
    ) {
        Icon(
            Icons.Filled.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(if (compact) 16.dp else 20.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            average,
            style = if (compact) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleSmall,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            if (compact) "(${rating.count})" else "· $reviews",
            style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
