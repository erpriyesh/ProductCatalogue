plugins {
    alias(libs.plugins.catalogue.android.feature)
}

android {
    namespace = "com.productcatalogue.feature.enquiry"
}

dependencies {
    // BackHandler on the Thank you screen.
    implementation(libs.androidx.activity.compose)
}
