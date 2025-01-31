import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {

    `maven-publish`
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

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
                implementation(libs.bundles.ktorClient)

                implementation(compose.runtime)
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
publishing {
    publications {
        // Reconfigure the existing JVM publication
        named<MavenPublication>("jvm") {
            artifactId = "kobold-jvm"
            groupId = "io.github.bsautner"
            version = "0.0.1-SNAPSHOT"
            pom {
                name.set("Kobold JVM")
                description.set("Kobold Code Generator for the JVM target.")
                url.set("https://github.com/bsautner/kobold")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                scm {
                    connection.set("scm:git:git://home/bsautner/kobold.git")
                    developerConnection.set("scm:git:ssh://home:bsautner/kobold")
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
}