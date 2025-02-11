import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.gradle.api.publish.maven.MavenPublication
import org.jetbrains.dokka.gradle.DokkaTask
plugins {
    `maven-publish`
    signing
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.dokka)
}

group = "io.github.bsautner.kobold"
version = "0.0.1"

val apiSourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
//    from(sourceSets["main"].allSource)  this needs to use sources from this :api module jvmMain or commonMain
}

val apiJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from("$buildDir/dokka/html") //I'm not sure if this is correct for dokka
}


kotlin {

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


publishing {
    repositories {
        maven {
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("username") as String
                password = project.findProperty("password") as String

            }

        }
    }

    publications {
        create<MavenPublication>("kobold-api") {
            artifactId = "kobold-api"
            groupId = "io.github.bsautner.kobold"
            version = "0.0.1" // project.version.toString()
            artifact(apiSourcesJar)
            artifact(apiJavadocJar)
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

}

signing {
    val signingKey: String? by project
    val signingKeyBreaks = signingKey?.replace("\\n", "\n")


    signing {
        val signingPassword: String? by project
        if (!signingKeyBreaks.isNullOrEmpty() && !signingPassword.isNullOrEmpty()) {
            useInMemoryPgpKeys(signingKeyBreaks, signingPassword)
            sign(publishing.publications["kobold-api"])
        } else {
            logger.warn("Signing key or password not provided; artifacts will not be signed.")
        }
    }
}

tasks.withType<DokkaTask>().configureEach {
    moduleName.set(project.name)
    moduleVersion.set(project.version.toString())
    outputDirectory.set(layout.buildDirectory.dir("dokka/$name"))
    failOnWarning.set(false)
    suppressObviousFunctions.set(true)
    suppressInheritedMembers.set(false)
    offlineMode.set(false)

}