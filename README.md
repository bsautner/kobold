[![Code Security](https://github.com/bsautner/kobold/actions/workflows/codeql.yml/badge.svg)](https://github.com/bsautner/kobold/actions/workflows/codeql.yml) [![CI with Gradle](https://github.com/bsautner/kobold/actions/workflows/gradle.yml/badge.svg)](https://github.com/bsautner/kobold/actions/workflows/gradle.yml)

This library is in the very early pre-alpha stage of development!

# Kobold 

A code generator for Kotlin Multiplatform, Jetpack Compose and Ktor

<img src="Writerside/images/kobold.webp" width="512">)

## To get started

Create a new multiplatform project using [Jetbrains Wizard](https://kmp.jetbrains.com/) 

add to gradle 
```

ksp {
arg("jvm-output-dir", "${project.layout.buildDirectory}/generated/ksp/jvm/kotlin")
arg("kmp-output-dir", "${project.layout.buildDirectory}/generated/ksp/common/kotlin")
}

    ksp(libs.kobold.ksp)
    implementation(libs.kobold.api.jvm)

```




This project uses [Gradle](https://gradle.org/).
To build and run the application, use the *Gradle* tool window by clicking the Gradle icon in the right-hand toolbar,
or run it directly from the terminal:

* Run `./gradlew run` to build and run the application.
* Run `./gradlew build` to only build the application.
* Run `./gradlew check` to run all checks, including tests.
* Run `./gradlew clean` to clean all build outputs.

Note the usage of the Gradle Wrapper (`./gradlew`).
This is the suggested way to use Gradle in production projects.

[Learn more about the Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html).

[Learn more about Gradle tasks](https://docs.gradle.org/current/userguide/command_line_interface.html#common_tasks).


This project follows the suggested multi-module setup and consists of the `app` and `utils` subprojects.
The shared build logic was extracted to a convention plugin located in `buildSrc`.

This project uses a version catalog (see `gradle/libs.versions.toml`) to declare and version dependencies
and both a build cache and a configuration cache (see `gradle.properties`).
