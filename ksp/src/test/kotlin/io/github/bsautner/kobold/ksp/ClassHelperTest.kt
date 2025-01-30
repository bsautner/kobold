package io.github.bsautner.utils.io.github.bsautner.kobold.ksp

import io.github.bsautner.kobold.KStatic
import io.github.bsautner.kobold.annotations.KoboldStatic
import io.github.bsautner.ksp.processor.ClassHelper
import io.ktor.resources.Resource
import io.ktor.server.config.configLoaders
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import kotlin.test.Test
@TestInstance(Lifecycle.PER_METHOD)
class ClassHelperTest {

	val classHelper = ClassHelper()

	@Resource("/") @KoboldStatic("test")
	class StaticTest

	@Test
	fun `test getting path value`() {


	//	classHelper.extractPathValue()




	}

}