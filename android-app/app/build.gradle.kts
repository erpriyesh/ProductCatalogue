plugins {
    alias(libs.plugins.catalogue.android.application)
    alias(libs.plugins.catalogue.android.compose)
    alias(libs.plugins.catalogue.hilt)
}

// The backend URL is a build setting, never hard-coded in source. See gradle.properties.
val apiBaseUrl: String = providers.gradleProperty("catalogue.apiBaseUrl").get().let {
    if (it.endsWith("/")) it else "$it/"
}

android {
    namespace = "com.productcatalogue.app"

    defaultConfig {
        applicationId = "com.productcatalogue.app"
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // PoC: signed with the debug key so a release build can be tried. A real upload key
            // would come from a keystore kept outside version control.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))
    implementation(project(":feature:catalogue"))
    implementation(project(":feature:enquiry"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
}
