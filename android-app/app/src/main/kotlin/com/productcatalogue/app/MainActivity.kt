package com.productcatalogue.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.productcatalogue.app.navigation.CatalogueNavHost
import com.productcatalogue.core.designsystem.theme.CatalogueTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CatalogueTheme {
                CatalogueNavHost()
            }
        }
    }
}
