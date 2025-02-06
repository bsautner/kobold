@file:OptIn(ExperimentalCompilerApi::class)

package io.github.bsautner.utils.io.github.bsautner.kobold.ksp.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import io.github.bsautner.utils.TestProcessorProvider
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.sourceKindForIncOrDec
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import readResourceFile
import java.io.File
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ComposeGeneratorTest {

	@Test
	fun `test read code`() {
		println("${System.getProperty("user.dir")}")
		val testCode = File("${System.getProperty("user.dir")}/../demo/src/commonMain/kotlin/io/github/bsautner/kobold/LoginScreenExample.kt").readText()
		val sourceFile = SourceFile.Companion.kotlin("LoginScreenExample.kt", testCode)
		println(testCode)
	}

	@Test
	fun `test getting type parameters`() {

		val provider = TestProcessorProvider { declaration ->
			println("Compose Callback ${declaration.simpleName.asString()}")
}
		val testCode = File("${System.getProperty("user.dir")}/../demo/src/commonMain/kotlin/io/github/bsautner/kobold/LoginScreenExample.kt").readText()
		val sourceFile = SourceFile.Companion.kotlin("MyUserInterface.kt", testCode)

		val compilation = KotlinCompilation().apply {
			configureKsp(useKsp2 = true) {
				symbolProcessorProviders += provider
			}
			sources = listOf(sourceFile)
			inheritClassPath = true
		}

		val result = compilation.compile()


	}



}