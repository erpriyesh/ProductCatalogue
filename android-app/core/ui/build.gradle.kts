plugins {
    alias(libs.plugins.catalogue.android.library)
    alias(libs.plugins.catalogue.android.compose)
}

android {
    namespace = "com.productcatalogue.core.ui"
}

dependencies {
    api(project(":core:model"))
    implementation(project(":core:designsystem"))
    implementation(libs.kotlinx.coroutines.core)
}
