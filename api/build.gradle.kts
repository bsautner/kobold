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

val tagVersion: String? = System.getenv("GITHUB_REF")?.substringAfterLast("/")
version = tagVersion ?: "0.0.1-SNAPSHOT"




val apiSourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["jvmMain"].allSource)
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

tasks.register<Zip>("zipPublishedArtifacts") {
    // Include everything published to the local repo directory.
    from("$buildDir/repo")
    archiveFileName.set("api.zip")
    destinationDirectory.set(File("$buildDir/zips"))
}
publishing {
    repositories {
        maven {
            name = "localRepo"
            url = uri("$buildDir/repo")
        }
    }

    publications.withType<MavenPublication>().configureEach {

            artifactId = "kobold-api"
            groupId = "io.github.bsautner.kobold"
            val tagVersion: String? = System.getenv("GITHUB_REF")?.substringAfterLast("/")
            version = tagVersion ?: "0.0.1-SNAPSHOT"
        //    artifact(apiSourcesJar)
       //     artifact(apiJavadocJar)
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



signing {
    val signingKey = System.getenv("GPG_PRIVATE_KEY")
    //val signingKeyBreaks = signingKey?.replace("\\n", "\n")
    signing {
        val signingPassword = System.getenv("GPG_PASSPHRASE")
        if (!signingKey.isNullOrEmpty() && !signingPassword.isNullOrEmpty()) {
            useInMemoryPgpKeys(signingKey, signingPassword)
            publishing.publications.withType<MavenPublication>().forEach { sign(it) }
            logger.warn("*** Signed all publications")
            logger.warn("*** Signed! ${signingKey.length} ${signingPassword.length}")
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