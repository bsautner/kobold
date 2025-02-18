@file:OptIn(ExperimentalCompilerApi::class)

package io.github.bsautner.kobold.ksp.processor.compose

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.tschuchort.compiletesting.KotlinCompilation
import io.github.bsautner.kobold.ksp.processor.BaseTestProcessor
import io.github.bsautner.kobold.ksp.processor.toSourceFile
import io.github.bsautner.kobold.samples.compose.ProfileMenuSample
import io.github.bsautner.ksp.classtools.ClassHelper
import jdk.internal.vm.ThreadContainers.root
import kotlinx.coroutines.supervisorScope
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.TestInstance
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class TestComposeMenus()  : BaseTestProcessor(ProfileMenuSample::class, { declaration ->
	val classHelper = ClassHelper()
	println("BEN: Test Callback: ${declaration.simpleName}")
	val metaData = classHelper.getClassMetaData(declaration)
	val subclassMap = classHelper.subclassMap(declaration)
	assertTrue { subclassMap.getAll().isNotEmpty() }
	val list = subclassMap.getAll()
	assertTrue { classHelper.hasChildren(declaration, declaration) }
	subclassMap.getAll()
	val nodes = subclassMap.getNodes()
	assertTrue {
		nodes.isNotEmpty()
	}
	nodes.forEach {
		assertFalse { classHelper.hasChildren(declaration, it) }
	}


}) {



	@Test
	fun `test compose menus`() {
		val file = File("/tmp/kobold/commonMain/kotlin/io/github/bsautner/kobold/samples/compose/ProfileMenuSampleComposable.kt")
		if (file.exists()) {
			file.delete()
		}
		assertNotNull(ProfileMenuSample::class.toSourceFile())
		val result = compilation.compile()
		assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

	}


}