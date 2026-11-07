import com.productcatalogue.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Pure Kotlin modules (model, domain, testing). No Android dependency is possible here, which
 * keeps business rules portable and their tests fast.
 */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")

        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = com.productcatalogue.buildlogic.AndroidConfig.JAVA_VERSION
            targetCompatibility = com.productcatalogue.buildlogic.AndroidConfig.JAVA_VERSION
        }
        extensions.configure<KotlinJvmProjectExtension> {
            compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
        }

        dependencies {
            add("testImplementation", libs.findLibrary("junit").get())
            add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
        }
    }
}
