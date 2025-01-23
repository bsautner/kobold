import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
}

group = "io.github.bsautner"
version = "1.0-SNAPSHOT"


java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}


ksp {
    arg("source", "demo")
    arg("output-dir", "$buildDir/generated/ksp/common/kotlin")
}

kotlin {
    jvmToolchain(21)
    jvm() {}

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "kobold-sample-app"
        binaries.executable()
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
    }

    sourceSets {
        val jvmMain by getting {}

        val commonMain by getting {
            kotlin.srcDir("src/commonMain/kotlin")
            kotlin.srcDir("$buildDir/generated/ksp/common/kotlin")
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(project(":api"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.kotlinxSerialization)
                implementation(libs.bundles.ktorServer)
                implementation(libs.bundles.ktorClient)

                implementation(kotlin("stdlib"))

            }
        }

        val wasmJsMain by getting {
            dependencies {
                implementation(project(":api"))
                implementation(compose.runtime)
                implementation(kotlin("stdlib"))
                implementation(kotlin("stdlib-wasm-js"))
                implementation(libs.bundles.ktorClient)

                implementation("org.jetbrains.compose.runtime:runtime-wasm-js:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-browser-wasm-js:0.3")
            }
        }
    }
}

dependencies {
    kspCommonMainMetadata(project(":ksp"))
    add("kspWasmJs", project(":ksp"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

tasks.withType<com.google.devtools.ksp.gradle.KspTask> {
    println("ksp task from kmp compilation")
    outputs.upToDateWhen { false }
}
tasks.withType<org.jetbrains.kotlin.gradle.targets.js.binaryen.BinaryenExec> {
    binaryenArgs = mutableListOf(
        "--enable-gc",
        "--enable-reference-types",
        "--enable-exception-handling",
        "--enable-bulk-memory",
        "--enable-nontrapping-float-to-int",
        "-O0"  // Minimal optimization for faster debugging
    )
}

tasks.named("compileKotlinWasmJs") {
    dependsOn("kspCommonMainKotlinMetadata")
}



tasks.named("compileKotlinJvm") {
    dependsOn("kspCommonMainKotlinMetadata")
}

