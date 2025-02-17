group = "io.github.bsautner.kobold"
val tagVersion: String? = System.getenv("GITHUB_REF")?.substringAfterLast("/")
version = tagVersion ?: "0.0.1-SNAPSHOT"

plugins {
    `maven-publish`
    `java-library`
    signing
    kotlin("kapt")
    alias(libs.plugins.buildsrc)
    alias(libs.plugins.ksp)
    alias(libs.plugins.serialization)

}

repositories {
    mavenCentral()
    mavenLocal()
    google()
}

kotlin {
    jvmToolchain(21)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    // If you're building a Kotlin-only project without Java docs, you can skip actual generation
    // or replace with Dokka if you want real Kotlin docs.
    from(tasks.named("javadoc"))
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

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
            name = "localRepo"
            url = uri("$buildDir/repo")
        }
    }
    publications {
        create<MavenPublication>("kobold-ksp") {
            from(components["java"])
            artifactId = "kobold-ksp"
            groupId = "io.github.bsautner.kobold"
            val tagVersion: String? = System.getenv("GITHUB_REF")?.substringAfterLast("/")
            version = tagVersion ?: "0.0.1-SNAPSHOT"

            // Attach sources and javadoc jars
            artifact(sourcesJar)
            artifact(javadocJar)

            pom {
                name.set("Ktor Auto Router KSP")
                description.set("Kotlin Ktor Auto Router Code Generator")
                url.set("https://github.com/bsautner/kobold")

                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                scm {
                    connection.set("scm:git:git://home/bsautner/kobold.git")
                    developerConnection.set("scm:git:ssh://home:bsautner/kobol")
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

dependencies {
    api(project(":api"))
    api(libs.ksp)
    api(libs.bundles.ktorServer)
    api(libs.bundles.ktorClient)
    api(libs.logback)
    api(libs.slf4j)
    api(libs.bundles.poet)
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.collection:collection:1.5.0-beta03")
    testImplementation(kotlin("test"))
    testImplementation("dev.zacsweers.kctfork:ksp:0.7.0")
    testImplementation("dev.zacsweers.kctfork:core:0.7.0")
    api("io.github.microutils:kotlin-logging:3.0.5")

    testImplementation("io.mockk:mockk:1.13.16")
    testImplementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.1.10")
}

signing {
    val signingKey = System.getenv("GPG_PRIVATE_KEY")
    signing {
        val signingPassword = System.getenv("GPG_PASSPHRASE")
        if (!signingKey.isNullOrEmpty() && !signingPassword.isNullOrEmpty()) {
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(publishing.publications["kobold-ksp"])
            logger.warn("*** Signed! ${signingKey.length} ${signingPassword.length}")
        } else {
            logger.warn("Signing key or password not provided; artifacts will not be signed.")
        }
    }
}

tasks.withType<Test> {
    reports.junitXml.required = false
}
tasks.withType<Test>().configureEach {
    reports.html.required = false
}


tasks.register<Zip>("zipPublishedArtifacts") {
    from("$buildDir/repo")
    archiveFileName.set("ksp.zip")
    destinationDirectory.set(File("$buildDir/zips"))
}