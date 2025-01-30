package io.github.bsautner.utils.io.github.bsautner.kobold.ksp.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.squareup.kotlinpoet.FileSpec
import io.github.bsautner.ksp.processor.BaseProcessor
import io.github.bsautner.ksp.processor.KoboldProcessorProvider.Companion.OUTPUT_DIR
import io.github.bsautner.ksp.processor.toFile
import io.github.bsautner.ksp.processor.util.isEmpty
import io.github.bsautner.ksp.processor.util.notExist
import io.github.bsautner.ksp.processor.util.touchFile
import io.mockk.every
import io.mockk.mockk
import mu.KotlinLogging
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.io.File
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProcessorUnderTest(env: SymbolProcessorEnvironment, historyFile: File ) : BaseProcessor(env, historyFile) {}

@TestInstance(Lifecycle.PER_METHOD)
class BaseProcessorTest {


	private val sessionId = UUID.randomUUID().toString()
	val testPackage = "io.github.bsautner.kobold"
	val fileName = "TestFile"
	val env = mockk<SymbolProcessorEnvironment>()
	val logger = mockk<KSPLogger>()
	val rootDir = File("/home/ben/kobold_tests")
	val history = File(rootDir, sessionId)
	val code : FileSpec = FileSpec.builder( testPackage, fileName ).build()
	lateinit var  processorUnderTest : BaseProcessor

	@BeforeTest
	fun setup() {
		every { logger.warn(any()) }  returns Unit
		every { logger.info(any()) }  returns Unit
		every { env.logger }  returns logger
		every { env.options } returns mapOf<String, String>(OUTPUT_DIR to rootDir.absolutePath)
		processorUnderTest = ProcessorUnderTest(env, File(rootDir, sessionId))

	}

	@AfterTest
	fun cleanup() {

		rootDir.listFiles().forEach {
			println("Deleting ${it.absolutePath}")
			it.deleteRecursively()
		}
		assertTrue { rootDir.isEmpty() }

	}


	@Test
	fun `test toFile ext function`() {

		val  path = "$rootDir/${testPackage.replace('.', '/')}/$fileName.kt"
		val code : FileSpec = FileSpec.builder( testPackage, fileName ).build()
		val testFile = code.toFile(rootDir)

		assertEquals(path, testFile.absolutePath)

	}


	@Test
	fun `create files test`() {

		val createdFile = processorUnderTest.createFile(code)
		assertEquals(code.toFile(rootDir),  createdFile)
		assertTrue { createdFile.isFile }
		val fileOnDisk = File(createdFile.absolutePath)
		assertTrue {
			fileOnDisk.exists()
			! fileOnDisk.isDirectory
		}

		assertTrue { history.exists() }
		val historyList  = history.readLines()
		assertTrue {
			historyList.isNotEmpty()
		}
		assertEquals(historyList.first(), createdFile.absolutePath)
	}

	@Test
	fun `test purge`() {
		val createdFile = processorUnderTest.createFile(code)
		val badFile = touchFile(File(rootDir, "bad.kt"))
		assertTrue {
				code.toFile(rootDir).exists()
				code.toFile(rootDir).isFile
				badFile.exists()
		}

		processorUnderTest.purge()
		assertTrue {
			code.toFile(rootDir).exists()
			code.toFile(rootDir).isFile
		}
		assertTrue {
			badFile.notExist()
		}
		assertTrue {
			history.notExist()
		}



	}

	@Test
	fun `test collecting and deleting files`() {

		val code : FileSpec = FileSpec.builder( testPackage, fileName ).build()
		val junk = File(rootDir, "bad_code.kt")

		processorUnderTest.writeToFile(code, true)
		assertTrue {
			code.toFile(rootDir).exists()
			processorUnderTest.historyFile.exists()
		}
		val files = processorUnderTest.historyFile.readLines()
		assertTrue { files.isNotEmpty() }
		 val line = files.first()
		assertEquals("${code.toFile(rootDir).absolutePath}", line)
		touchFile(junk)
		assertTrue {
			junk.exists()
		}

		processorUnderTest.purge()


	}

	@Test
	fun  `Test Multiple Writes`() {
		val code2 : FileSpec = FileSpec.builder( testPackage,  "bar" ).build()
		val code1 : FileSpec = FileSpec.builder( testPackage, "foo" ).build()

		processorUnderTest.writeToFile(code1)
		processorUnderTest.writeToFile(code2)
//		history.appendText("foo\n")
//		history.appendText("bar\n")
		assertEquals(2, history.readLines().size)
	}

}