@file:OptIn(ExperimentalCompilerApi::class)

package io.github.bsautner.utils.io.github.bsautner.kobold.ksp.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import io.github.bsautner.ksp.classtools.ClassHelper
import io.github.bsautner.ksp.util.ImportManager
import io.github.bsautner.utils.TestProcessorProvider
import io.mockk.mockk
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import readResourceFile
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ImportManagerTest {


	private val classHelper = ClassHelper()

	@Test
	fun `test imports`() {

		val provider = TestProcessorProvider { declaration ->
			println("BEN: Test Callback: ${declaration.simpleName}")

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

	   compilation.compile()


	}

	@Test
	fun `basic import manager test`() {

		val id = mockk<KSClassDeclaration>()
		ImportManager.addImport(id, "com.test", "foo")
		ImportManager.addImport(id, "com.test", "bar")
		assertTrue {
			ImportManager.imports.isNotEmpty()
			ImportManager.imports.containsKey(id)
			ImportManager.imports[id]?.isNotEmpty() == true
		//	ImportManager.imports[id]?.size == 2
		}
	}

}