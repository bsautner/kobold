@file:OptIn(ExperimentalCompilerApi::class)

package io.github.bsautner.utils.io.github.bsautner.kobold.ksp.processor

import com.tschuchort.compiletesting.KotlinCompilation
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import kotlin.test.assertEquals

@TestInstance(Lifecycle.PER_METHOD)
class ComposeGeneratorRealTest : RealProcessorTest() {


	@Test
	fun `test static routing`() {

		val result = compilation.compile()
	//	assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

	}

}