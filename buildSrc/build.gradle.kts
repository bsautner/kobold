plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(17)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21)) // Use JDK 17
    }
}

dependencies {
    // Add a dependency on the Kotlin Gradle plugin, so that convention plugins can apply it.
    implementation(libs.kotlinGradlePlugin)
}

configurations.all {
    resolutionStrategy {
        force("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
        force("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
        force("androidx.arch.core:core-common:2.2.0")
        force("androidx.annotation:annotation:1.7.0")
    }
}

