import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.gradle.api.publish.maven.MavenPublication

plugins {
    `maven-publish`
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

group = "io.github.bsautner"
version = "0.0.1-SNAPSHOT"

// Configure the Kotlin Multiplatform targets
kotlin {
    // Use a common JVM toolchain configuration for all targets
    jvmToolchain(21)

    jvm()

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("src/commonMain/kotlin")
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(libs.bundles.ktorClient)
                implementation(compose.runtime)
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
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

// Java toolchain configuration for non-Kotlin parts (if needed)
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// Configure publishing for all targets using the built-in multiplatform publishing
publishing {
    publications.withType<MavenPublication>().configureEach {
        // Customize the artifactId if desired (e.g. append the publication name)
        artifactId = "kobold-${name}"
        groupId = project.group.toString()
        version = project.version.toString()

        pom {
            // You can set a different name/description per publication if needed:
            name.set("Kobold (${name})")
            description.set("Kobold Code Generator for the ${name} target.")
            url.set("https://github.com/bsautner/kobold")

            licenses {
                license {
                    name.set("Apache License 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/bsautner/kobold.git")
                developerConnection.set("scm:git:ssh://github.com/bsautner/kobold.git")
                url.set("https://github.com/bsautner/kobold")
            }
            developers {
                developer {
                    id.set("bsautner")
                    name.set("Benjamin Sautner")
                    email.set("bsautner@gmail.com")
                }
            }
        }
    }
}
