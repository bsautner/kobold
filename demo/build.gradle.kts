import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktor)
}

group = "io.github.bsautner"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("io.github.bsautner.kobold.main")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}


java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

ksp {
       arg("output-dir",  project.layout.buildDirectory.get().asFile.absolutePath + "/generated/ksp")
       arg("project", project.name)
}

kotlin {
    jvmToolchain(21)
    jvm()



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
        val jvmMain by getting {
            kotlin.srcDir("src/jvmMain/kotlin")
            resources.srcDir("src/jvmMain/resources")
            kotlin.srcDir("${project.layout.buildDirectory}/generated/ksp/jvmMain/kotlin")
            dependencies {
                implementation(kotlin("stdlib"))

                implementation(libs.ktor.netty)
                implementation(libs.bundles.ktorJvmServer)
                implementation(libs.bundles.ktorClient)

            }
        }

        val commonMain by getting {
            kotlin.srcDir("src/commonMain/kotlin")
            kotlin.srcDir("${project.layout.buildDirectory}/generated/ksp/commonMain/kotlin")
            dependencies {
                implementation(kotlin("stdlib-common"))
                api(project(":api"))
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


            }
        }

        val wasmJsMain by getting {
            dependencies {
                implementation(project(":api"))
                implementation(compose.runtime)
                implementation(kotlin("stdlib-wasm-js"))
                implementation(libs.bundles.ktorClient)

                implementation("org.jetbrains.compose.runtime:runtime-wasm-js:1.8.0+dev2049")
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



tasks.withType<com.google.devtools.ksp.gradle.KspTask> {
    println("ksp task from kmp compilation")
    outputs.upToDateWhen { false }
}
tasks.named("compileKotlinJvm") {
    dependsOn("kspCommonMainKotlinMetadata")
}

