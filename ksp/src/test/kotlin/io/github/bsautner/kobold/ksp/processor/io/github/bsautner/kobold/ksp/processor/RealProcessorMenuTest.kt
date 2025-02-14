@file:OptIn(ExperimentalCompilerApi::class)

package io.github.bsautner.utils.io.github.bsautner.kobold.ksp.processor.io.github.bsautner.kobold.ksp.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.kspProcessorOptions
import io.github.bsautner.utils.TestProcessorProvider
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import readResourceFile

open class RealProcessorMenuTest {

	val provider = TestProcessorProvider { declaration ->
		println("Callback ${declaration.simpleName.asString()}")
	}

	val testCode = readResourceFile("/io/github/bsautner/kobold/MyUserInterface.kt")
	val sourceFile = SourceFile.kotlin("MyUserInterface.kt", testCode)
	val compilation = KotlinCompilation().apply {
		configureKsp(useKsp2 = true) {
			symbolProcessorProviders += provider

		}
		sources = listOf(sourceFile)
		inheritClassPath = true
		kspProcessorOptions = mutableMapOf("output-dir" to "/tmp/kobold")
	}


}