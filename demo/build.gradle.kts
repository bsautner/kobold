import org.jetbrains.kotlin.gradle.internal.platform.wasm.WasmPlatforms.wasmJs
import org.gradle.internal.impldep.com.google.api.services.storage.Storage
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin.Companion.disableClassloaderCacheForProcessors
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
plugins {
    alias(libs.plugins.buildsrc)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
}

group = "io.github.bsautner"
version = "1.0-SNAPSHOT"

ksp {
    arg("source", "demo")
}

kotlin {
   

  sourceSets {
        jvm()
//
//      @OptIn(ExperimentalWasmDsl::class)
//      wasmJs {
//          moduleName = "kobold-sample-app"
//          browser {
//              val rootDirPath = project.rootDir.path
//              val projectDirPath = project.projectDir.path
//              commonWebpackConfig {
//                  outputFileName = "composeApp.js"
//                  devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
//                      static = (static ?: mutableListOf()).apply {
//                          // Serve sources to debug inside browser
//                          add(rootDirPath)
//                          add(projectDirPath)
//                      }
//                  }
//              }
//          }
//          binaries.executable()
//      }

      commonMain {}

      val jvmMain by getting {
          dependencies {

          }
      }

      val commonMain by getting {
          kotlin.srcDir("src/commonMain/kotlin")
          dependencies {
             // implementation(libs.ksp)
              implementation(compose.runtime)
              implementation(compose.foundation)
              implementation(compose.runtime)
              implementation(compose.foundation)
              implementation(compose.material)
              implementation(compose.ui)
              implementation(compose.components.resources)
              implementation(compose.components.uiToolingPreview)
              implementation(libs.androidx.lifecycle.viewmodel)
              implementation(libs.androidx.arch.core.common)
              implementation(libs.androidx.lifecycle.runtime.compose)
              implementation(libs.androidx.annotation)



          }
      }


  }
}
dependencies {
    kspCommonMainMetadata(project(":ksp"))

    add("kspCommonMainMetadata", project(":ksp"))
    add("kspJvm", project(":ksp"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<com.google.devtools.ksp.gradle.KspTask> {
    println("ksp task from kmp compilation")
    outputs.upToDateWhen { false }
}