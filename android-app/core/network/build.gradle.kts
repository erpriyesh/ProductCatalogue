plugins {
    alias(libs.plugins.catalogue.android.library)
    alias(libs.plugins.catalogue.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.productcatalogue.core.network"
    buildFeatures { buildConfig = true }
}

dependencies {
    // safeApiCall returns DataResult / DataError.
    api(project(":core:model"))
    // Features declare their own Retrofit services on top of the shared client.
    api(libs.retrofit)
    api(libs.okhttp)
    api(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.logging)
}
