plugins {
    alias(libs.plugins.buildsrc)
    alias(libs.plugins.ksp)
    alias(libs.plugins.serialization)

}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17)) // Use JDK 17
    }
}
dependencies {
    implementation(project(":api"))
    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.ksp)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.poet)
    testImplementation(kotlin("test"))
}

