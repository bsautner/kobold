@file:OptIn(ExperimentalCompilerApi::class)

package io.github.bsautner.utils.io.github.bsautner.kobold.ksp.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.kspProcessorOptions
import io.github.bsautner.kobold.KPost
import io.github.bsautner.ksp.classtools.ClassHelper
import io.github.bsautner.utils.TestProcessorProvider
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import readResourceFile

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestInstance(Lifecycle.PER_METHOD)
class ProcessorTest {


	private val classHelper = ClassHelper()

	@Test
	fun `test getting type parameters`() {

		val provider = TestProcessorProvider { declaration ->
			val typeParams = classHelper.getTypeParameters(declaration)
			assertTrue { typeParams.isNotEmpty() }
			typeParams.find{ it.qualifiedName == KPost::class.qualifiedName }?.let {
				println(it)
				assertTrue { it.qualifiedName == "io.github.bsautner.kobold.LoginData" }
				assertTrue { it.qualifiedName == "io.github.bsautner.kobold.LoginResponse" }
			}
		}
		val testCode = readResourceFile("/io/github/bsautner/kobold/AuthModel.kt")
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
	fun `test static routing`() {

		val provider = TestProcessorProvider { declaration ->
			println("Callback ${declaration.simpleName.asString()}")
		}
		val testCode = readResourceFile("/io/github/bsautner/kobold/StaticExample.kt")
		val sourceFile = SourceFile.kotlin("StaticExample.kt", testCode)

		val compilation = KotlinCompilation().apply {
			configureKsp(useKsp2 = true) {
				symbolProcessorProviders += provider

			}
			sources = listOf(sourceFile)
			inheritClassPath = true
			kspProcessorOptions = mutableMapOf("output-dir" to "/tmp/kobold")
		}

		val result = compilation.compile()
		assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

	}

	@Test
	fun `test extracting meta data about a class`() {

		println("running my tests")

		val provider = TestProcessorProvider { declaration ->

			assertEquals("Login", declaration.simpleName.asString())
			val metaData = classHelper.getClassMetaData(declaration)
			assertNotNull(metaData)
			val interfaces = metaData.interfaces
			val params = metaData.typeParameters
			println(params)
//			assertTrue {
////				interfaces.contains(KPost::class.qualifiedName)
////				interfaces.contains(KComposable::class.qualifiedName)
//			}

		}
		val testCode = readResourceFile("/io/github/bsautner/kobold/AuthModel.kt")
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