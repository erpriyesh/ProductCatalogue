package com.productcatalogue.feature.catalogue.presentation.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.productcatalogue.core.designsystem.component.BackTopBar
import com.productcatalogue.core.designsystem.component.ErrorContent
import com.productcatalogue.core.designsystem.component.ProductImage
import com.productcatalogue.core.designsystem.component.RatingRow
import com.productcatalogue.core.designsystem.component.shimmer
import com.productcatalogue.core.designsystem.format.formatPrice
import com.productcatalogue.core.model.Product
import com.productcatalogue.core.ui.LoadState
import com.productcatalogue.feature.catalogue.presentation.common.categoryIcon

@Composable
internal fun ProductScreen(
    onBack: () -> Unit,
    onEnquire: (Product) -> Unit,
    viewModel: ProductViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProductContent(state, onBack, viewModel::retry, onEnquire)
}

@Composable
internal fun ProductContent(
    state: LoadState<Product>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onEnquire: (Product) -> Unit,
) {
    Scaffold(
        topBar = { BackTopBar("Product details", onBack) },
        bottomBar = {
            // Price and call to action stay visible however long the description is.
            if (state is LoadState.Content) EnquireBar(state.value, onClick = { onEnquire(state.value) })
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (state) {
            LoadState.Loading -> DetailsSkeleton(modifier)
            is LoadState.Error -> ErrorContent(state.message, onRetry, modifier)
            is LoadState.Content -> ProductDetails(state.value, modifier)
        }
    }
}

@Composable
private fun EnquireBar(product: Product, onClick: () -> Unit) {
    Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Price", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    formatPrice(product.price),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Button(
                onClick = onClick,
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                modifier = Modifier.height(52.dp).testTag("enquire-button"),
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Enquire")
            }
        }
    }
}

@Composable
private fun ProductDetails(product: Product, modifier: Modifier = Modifier) {
    Column(modifier.verticalScroll(rememberScrollState()).testTag("product-details")) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.padding(16.dp),
        ) {
            ProductImage(
                url = product.imageUrl,
                contentDescription = product.title,
                modifier = Modifier.fillMaxWidth().aspectRatio(1.1f).clip(MaterialTheme.shapes.large),
            )
        }
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text(product.category.name) },
                leadingIcon = {
                    Icon(categoryIcon(product.category.slug), contentDescription = null, Modifier.size(AssistChipDefaults.IconSize))
                },
                colors = AssistChipDefaults.assistChipColors(
                    disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                    disabledLeadingIconContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
            Text(product.title, style = MaterialTheme.typography.headlineSmall)
            RatingRow(product.rating, compact = false)
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            Text("About this product", style = MaterialTheme.typography.titleMedium)
            Text(
                product.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun DetailsSkeleton(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(16.dp).testTag("loading"), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.fillMaxWidth().aspectRatio(1.1f).clip(MaterialTheme.shapes.large).shimmer())
        Box(Modifier.width(120.dp).height(28.dp).clip(MaterialTheme.shapes.small).shimmer())
        Box(Modifier.fillMaxWidth(0.9f).height(28.dp).clip(MaterialTheme.shapes.small).shimmer())
        Box(Modifier.fillMaxWidth(0.5f).height(20.dp).clip(MaterialTheme.shapes.small).shimmer())
    }
}
