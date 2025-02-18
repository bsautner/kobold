@file:OptIn(ExperimentalCompilerApi::class)

package io.github.bsautner.kobold.ksp.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.kspProcessorOptions
import io.github.bsautner.kobold.samples.compose.ProfileMenuSample
import io.github.bsautner.utils.io.github.bsautner.kobold.ksp.processor.TestProcessorProvider
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import readResourceFile
import java.io.File
import kotlin.reflect.KClass
import kotlin.test.assertTrue

open class BaseTestProcessor(klass: KClass<ProfileMenuSample>, testCallback: (KSClassDeclaration) -> Unit) {


	val provider = TestProcessorProvider(testCallback)


	val compilation = KotlinCompilation().apply {
		configureKsp(useKsp2 = true) {
			symbolProcessorProviders += provider

		}
		sources = listOf(klass.toSourceFile())
		inheritClassPath = true
		kspProcessorOptions = mutableMapOf("output-dir" to "/tmp/kobold")
	}



}
///home/ben/Code/ktor/kobold/api/src.commonMain/kotlin/io/github/bsautner/kobold/samples/compose/ProfileMenuSample.kt
///home/ben/Code/ktor/kobold/api/src/commonMain/kotlin/io/github/bsautner/kobold/samples/compose/ProfileMenuSample.kt
fun KClass<*>.toSourceFile(): SourceFile {
	val root = System.getProperty("user.dir").replace("/ksp", "/api")
	val path = "$root/src/commonMain/kotlin/${this.java.name.replace('.', '/')}.kt"
	println(path)
	val file = File(path)
	assertTrue { file.exists() }

	val code =file.readText()
	return SourceFile.kotlin(this.java.simpleName + ".kt", code)
}