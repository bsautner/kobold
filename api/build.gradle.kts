import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
     alias(libs.plugins.serialization)
    alias(libs.plugins.kotlinMultiplatform)
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
        val jsMain by getting {}
        val commonMain by getting {
            kotlin.srcDir("src/commonMain/kotlin")
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(libs.bundles.ktor)
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