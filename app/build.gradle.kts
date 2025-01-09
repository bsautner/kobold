plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    alias(libs.plugins.ksp)
    alias(libs.plugins.serialization)
    alias(libs.plugins.buildsrc)

    // Apply the Application plugin to add support for building an executable JVM application.
    application
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<com.google.devtools.ksp.gradle.KspTask> {
    println("ksp task")
    outputs.upToDateWhen { false }
}

sourceSets {
    test {
        java.srcDirs.clear()  // Prevents Gradle from looking for a Java folder
        kotlin.srcDirs("src/test/kotlin")
    }
}
dependencies {
    implementation(libs.ksp)
    ksp(project(":ksp"))

    // Project "app" depends on project "utils". (Project paths are separated with ":", so ":utils" refers to the top-level "utils" project.)
   // implementation(project(":utils"))
}

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "io.github.bsautner.app.AppKt"
}
