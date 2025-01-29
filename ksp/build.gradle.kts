plugins {
    alias(libs.plugins.buildsrc)
    alias(libs.plugins.ksp)
    alias(libs.plugins.serialization)

}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}


dependencies {
    implementation(project(":api"))
    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.ksp)
    implementation(libs.bundles.ktorServer)
    implementation(libs.bundles.ktorClient)

    implementation(libs.bundles.poet)
    testImplementation(kotlin("test"))
    implementation("io.github.microutils:kotlin-logging:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    testImplementation("io.mockk:mockk:1.13.16")

}

