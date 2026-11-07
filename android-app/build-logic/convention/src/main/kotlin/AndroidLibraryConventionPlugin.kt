import com.android.build.api.dsl.LibraryExtension
import com.productcatalogue.buildlogic.AndroidConfig
import com.productcatalogue.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")

        extensions.configure<LibraryExtension> {
            compileSdk = AndroidConfig.COMPILE_SDK
            defaultConfig {
                minSdk = AndroidConfig.MIN_SDK
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = AndroidConfig.JAVA_VERSION
                targetCompatibility = AndroidConfig.JAVA_VERSION
            }
            testOptions {
                unitTests.isIncludeAndroidResources = true
                unitTests.all { it.jvmArgs(AndroidConfig.ROBOLECTRIC_JVM_ARG) }
            }
        }

        // Some modules are tested through others (core:network via core:data's MockWebServer test).
        tasks.withType(Test::class.java).configureEach { failOnNoDiscoveredTests.set(false) }

        dependencies {
            add("testImplementation", libs.findLibrary("junit").get())
            add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
        }
    }
}
