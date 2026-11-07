plugins {
    alias(libs.plugins.catalogue.android.library)
    alias(libs.plugins.catalogue.android.compose)
}

android {
    namespace = "com.productcatalogue.core.designsystem"
}

dependencies {
    api(project(":core:model"))
    api(libs.androidx.compose.material.icons.extended)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
