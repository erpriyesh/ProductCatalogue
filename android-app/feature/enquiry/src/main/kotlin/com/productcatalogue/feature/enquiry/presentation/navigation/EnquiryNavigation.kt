package com.productcatalogue.feature.enquiry.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.productcatalogue.core.model.Product
import com.productcatalogue.feature.enquiry.presentation.EnquiryScreen
import com.productcatalogue.feature.enquiry.presentation.EnquirySentScreen

/** How other parts of the app open this feature, without knowing its routes. */
fun NavController.navigateToEnquiry(product: Product) =
    navigate(EnquiryRoute(productId = product.id, productTitle = product.title))

/**
 * [onFinished] runs when the customer leaves the Thank you screen (its button or Back). The app
 * decides where that goes, so this feature doesn't need to know which screen opened it.
 */
fun NavGraphBuilder.enquiryGraph(navController: NavController, onFinished: () -> Unit) {
    val back: () -> Unit = { navController.popBackStack() }

    composable<EnquiryRoute> { entry ->
        val route = entry.toRoute<EnquiryRoute>()
        EnquiryScreen(
            onBack = back,
            onSent = { reference ->
                navController.navigate(EnquirySentRoute(reference, route.productTitle)) {
                    // The form leaves the back stack, so Back can never resubmit it.
                    popUpTo<EnquiryRoute> { inclusive = true }
                }
            },
        )
    }
    composable<EnquirySentRoute> { entry ->
        val route = entry.toRoute<EnquirySentRoute>()
        EnquirySentScreen(reference = route.reference, productTitle = route.productTitle, onDone = onFinished)
    }
}
