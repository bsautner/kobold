@file:OptIn(ExperimentalCompilerApi::class)

package io.github.bsautner.utils.io.github.bsautner.kobold.ksp.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import io.github.bsautner.kobold.KComposable
import io.github.bsautner.kobold.KPost
import io.github.bsautner.ksp.processor.ClassHelper
import io.github.bsautner.utils.TestProcessorProvider
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle

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
			typeParams["io.github.bsautner.kobold.KPost"]?.let {
				println(it)
				assertTrue { it[0].qualifiedName == "io.github.bsautner.kobold.LoginData" }
				assertTrue { it[1].qualifiedName == "io.github.bsautner.kobold.LoginResponse" }
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
		assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

	}


	@Test
	fun `test static routing`() {

		val provider = TestProcessorProvider { declaration ->
			println("Callback ${declaration.simpleName.asString()}")
		}
		val testCode = readResourceFile("/io.github.bsautner.kobold/StaticExample.kt")
		val sourceFile = SourceFile.kotlin("StaticExample.kt", testCode)

		val compilation = KotlinCompilation().apply {
			configureKsp(useKsp2 = true) {
				symbolProcessorProviders += provider
			}
			sources = listOf(sourceFile)
			inheritClassPath = true
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
			val interfaces = metaData.interfaces.map { it.qualifiedName?.asString() }
			val params = metaData.params
			println(params)
			assertTrue {
				interfaces.contains(KPost::class.qualifiedName)
				interfaces.contains(KComposable::class.qualifiedName)
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
		assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

	}


	fun readResourceFile(resourcePath: String): String =
		object {}.javaClass.getResource(resourcePath)?.readText(Charsets.UTF_8)
			?: error("Resource not found: $resourcePath")
}