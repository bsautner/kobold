@file:OptIn(ExperimentalCompilerApi::class)

package io.github.bsautner.utils.io.github.bsautner.kobold.ksp.processor.io.github.bsautner.kobold.ksp.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import io.github.bsautner.kobold.KComposable
import io.github.bsautner.kobold.KPost
import io.github.bsautner.kobold.KResponse

import io.github.bsautner.ksp.classtools.ClassHelper
import io.github.bsautner.utils.TestProcessorProvider
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import readResourceFile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ClassHelperProcessorTest {


	private val classHelper = ClassHelper()

	@Test
	fun `test interfaces`() {

		val provider = TestProcessorProvider { declaration ->
			println("BEN: Test Callback: ${declaration.simpleName}")
			val interfaces = classHelper.getImplementedInterfaces(declaration)
			assertTrue {
				interfaces.isNotEmpty()
				interfaces.any { it.qualifiedName.toString() == KComposable::class.qualifiedName.toString() }
				interfaces.any { it.qualifiedName.toString() == KPost::class.qualifiedName.toString() }
			}
		}
		val testCode = readResourceFile("/io.github.bsautner.kobold/AuthModel.kt")
		val sourceFile = SourceFile.kotlin("AuthModel.kt", testCode)

		val compilation = KotlinCompilation().apply {
			configureKsp(useKsp2 = true) {
				symbolProcessorProviders += provider
			}
			sources = listOf(sourceFile)
			inheritClassPath = true
		}
		val result = compilation.compile()
 	}


	@Test
	fun `test type parameters`() {

		val provider = TestProcessorProvider { declaration ->
			println("BEN: Test Callback: ${declaration.simpleName}")
			val list = classHelper.getTypeParameters(declaration)
			assertTrue {
				list.isNotEmpty()
				list.any { it.simpleName.toString() == "LoginData"}
				list.any { it.simpleName.toString() == "LoginResponse" }

			}
		}
		val testCode = readResourceFile("/io.github.bsautner.kobold/AuthModel.kt")
		val sourceFile = SourceFile.kotlin("AuthModel.kt", testCode)

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