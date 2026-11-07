package com.productcatalogue.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.productcatalogue.feature.catalogue.presentation.navigation.HomeRoute
import com.productcatalogue.feature.catalogue.presentation.navigation.catalogueGraph
import com.productcatalogue.feature.catalogue.presentation.navigation.returnToProductOrigin
import com.productcatalogue.feature.enquiry.presentation.navigation.enquiryGraph
import com.productcatalogue.feature.enquiry.presentation.navigation.navigateToEnquiry

/**
 * The only place features meet. Each feature contributes its graph; cross-feature journeys are
 * connected here through callbacks, so features never depend on each other:
 *
 * - Product → Enquire opens the enquiry feature.
 * - After Thank you, the customer returns to where they opened the product (Home, a category or
 *   search results), not to the product page they already enquired about.
 */
@Composable
fun CatalogueNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = HomeRoute) {
        catalogueGraph(navController, onEnquire = navController::navigateToEnquiry)
        enquiryGraph(navController, onFinished = navController::returnToProductOrigin)
    }
}
