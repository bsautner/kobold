import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17)) // Use JDK 17
    }
}

kotlin {

    js {
        browser()
    }

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()

    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))

            }
        }
        val commonMain by getting {
            kotlin.srcDir("src/commonMain/kotlin")
            dependencies {

                implementation(kotlin("stdlib-common"))
                implementation(libs.bundles.ktor)
                implementation(compose.runtime)
             //   implementation(compose.foundation)
              //  implementation(compose.material)
              //  implementation(compose.ui)
            }
        }
        val jvmMain by getting {
            kotlin.srcDir("src/jvmMain/kotlin")
            dependencies {

                implementation(kotlin("stdlib"))
            }
        }

    }

}

//dependencies {
//    implementation(libs.bundles.kotlinxEcosystem)
//    implementation(libs.bundles.ktor)
//    testImplementation(kotlin("test"))
//}