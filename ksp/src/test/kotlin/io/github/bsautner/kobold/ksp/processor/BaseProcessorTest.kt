package io.github.bsautner.utils.io.github.bsautner.kobold.ksp.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import io.github.bsautner.ksp.processor.BaseProcessor
import io.github.bsautner.ksp.processor.toFile
import io.github.bsautner.ksp.processor.util.isEmpty
import io.github.bsautner.ksp.processor.util.notExist
import io.github.bsautner.ksp.processor.util.touchFile
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.io.File
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProcessorUnderTest(env: SymbolProcessorEnvironment) : BaseProcessor(env) {
	override fun create(sequence: Sequence<KSAnnotated>) {
		TODO("Not yet implemented")
	}

	override fun addImports(
		builder: FileSpec.Builder,
		sequence: Sequence<KSAnnotated>
	) {
		TODO("Not yet implemented")
	}

	override fun generate(sequence: Sequence<KSAnnotated>): CodeBlock {
		TODO("Not yet implemented")
	}
}

@TestInstance(Lifecycle.PER_METHOD)
class BaseProcessorTest {


	private val sessionId = UUID.randomUUID().toString()
	val testPackage = "io.github.bsautner.kobold"
	val fileName = "TestFile"
	val env = mockk<SymbolProcessorEnvironment>()
	val logger = mockk<KSPLogger>()
	val rootDir = File("/home/ben/kobold_tests")
	val code : FileSpec = FileSpec.builder( testPackage, fileName ).build()
	lateinit var  processorUnderTest : BaseProcessor

	@BeforeTest
	fun setup() {
		every { logger.warn(any()) }  returns Unit
		every { logger.info(any()) }  returns Unit
		every { env.logger }  returns logger
		every { env.options } returns mapOf<String, String>("output-dir" to rootDir.absolutePath)
		processorUnderTest = ProcessorUnderTest(env)

	}

	@AfterTest
	fun cleanup() {


	}


	@Test
	fun `test toFile ext function`() {

		val  path = "$rootDir/${testPackage.replace('.', '/')}/$fileName.kt"
		val code : FileSpec = FileSpec.builder( testPackage, fileName ).build()
		val testFile = code.toFile(rootDir)

		assertEquals(path, testFile.absolutePath)

	}




}