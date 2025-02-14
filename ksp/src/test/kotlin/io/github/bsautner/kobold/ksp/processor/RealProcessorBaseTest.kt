@file:OptIn(ExperimentalCompilerApi::class)

package io.github.bsautner.utils.io.github.bsautner.kobold.ksp.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.kspProcessorOptions
import io.github.bsautner.ksp.classtools.directSubClasses
import io.github.bsautner.utils.TestProcessorProvider
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import readResourceFile
import java.io.File

open class RealProcessorTest {

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

open class RealProcessorMenuTest {




	val provider = TestProcessorProvider { declaration ->
		println("Callback ${declaration.simpleName.asString()}")

		val children = declaration.directSubClasses()
		println(children.size)
		children.forEach {
			val l2 = it.directSubClasses()
			println(l2.size)
		}
	}

	val testCode = readResourceFile("/io/github/bsautner/kobold/TestModelComposeMenu.kt")
	val sourceFile = SourceFile.kotlin("TestModelComposeMenu.kt", testCode)
	val compilation = KotlinCompilation().apply {

		configureKsp(useKsp2 = true) {
			symbolProcessorProviders += provider

		}
		sources = listOf(sourceFile)
		inheritClassPath = true
		kspProcessorOptions = mutableMapOf("output-dir" to "/tmp/kobold")
	}


}