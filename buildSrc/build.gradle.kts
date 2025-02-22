plugins {
    `kotlin-dsl`
    id("com.github.ben-manes.versions") version "0.52.0"
    id("nl.littlerobots.version-catalog-update" ) version "0.8.5"

}

kotlin {
    jvmToolchain(21)

}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    // Add a dependency on the Kotlin Gradle plugin, so that convention plugins can apply it.
    implementation(libs.kotlinGradlePlugin)
    // https://mvnrepository.com/artifact/io.github.microutils/kotlin-logging
// https://mvnrepository.com/artifact/org.jetbrains.dokka/analysis-markdown
    runtimeOnly("org.jetbrains.dokka:analysis-markdown:2.0.0")

}


configurations.all {
    resolutionStrategy {
        force("androidx.lifecycle:lifecycle-viewmodel:2.8.7")
        force("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
        force("androidx.arch.core:core-common:2.2.0")
        force("androidx.annotation:annotation:1.9.1")
    }
}

