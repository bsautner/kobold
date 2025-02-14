@file:OptIn(ExperimentalCompilerApi::class)

package io.github.bsautner.utils.io.github.bsautner.kobold.ksp.processor

import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.incremental.deleteDirectoryContents
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.io.File
import kotlin.test.assertTrue

@TestInstance(Lifecycle.PER_METHOD)
class ComposeGeneratorRealMenuTest : RealProcessorMenuTest() {

	@BeforeEach
	fun before() {
		cleanup()
	}

	@AfterEach
	fun after() {
		val target = File("/home/ben/Code/captracks/captracks_platform/source/captracks/src/commonMain/kotlin/io/github/bsautner/captracks/TestModelComposeMenuComposable.kt")
		val source = File("/tmp/kobold/commonMain/kotlin/io/github/bsautner/captracks/TestModelComposeMenuComposable.kt")

			if (source.exists()) {
				source.copyTo(target, true)
			}
	}

	@Test
	fun `test static routing`() {

		val result = compilation.compile()




	}
	fun cleanup() {
		val tmp = File("/tmp/kobold")
		tmp.listFiles().forEach {
			it.deleteDirectoryContents()
			it.deleteRecursively()
		}
	}

}