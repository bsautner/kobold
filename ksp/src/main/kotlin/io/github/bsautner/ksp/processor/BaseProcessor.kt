package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import io.github.bsautner.ksp.processor.util.touchFile
import io.ktor.server.config.configLoaders
import java.io.File
import java.util.*

//TODO mark all files dirty, process with not dirty mark and delete dirty files.

abstract class BaseProcessor(env: SymbolProcessorEnvironment) : KoboldClassBuilder{

	private var logger: KSPLogger = env.logger
	private val output = env.options["output-dir"] ?: "/tmp/kobold"
	private var outputDirOption : File = File(output)
	val classHelper = ClassHelper()

	fun log(text: Any) {
		 logger.info("ben: $text")
	}

	fun writeToFile(fileSpec: FileSpec) {
		log(fileSpec.tags)
		fileSpec.tag(TargetPlatform::class)?.let {
			val outputDir = File("$outputDirOption/${it.name}/kotlin")

			val target = "${outputDir.absolutePath}/${fileSpec.packageName.replace('.', '/')}/${fileSpec.name}.kt"

			val targetFile = File(target)
			log("Checking Target. Exists = ${targetFile.exists()}  $target")
			if (targetFile.exists()) {
				val content = targetFile.readText()
				val newContent = buildString {
					fileSpec.writeTo(this)
				}
				if (content.trim() == newContent.trim()) {
					log("Skipping identical content")
					//TODO mark as not dirty
				} else {
					createFile(fileSpec, outputDir)
				}
			} else {
				createFile(fileSpec, outputDir)
			}
		}
	}



	fun createFile(fileSpec: FileSpec, outputDir : File) : File {
		val result = fileSpec.writeTo(outputDir)
		log("Created File : ${result.path}")
		return result
	}

	override fun addImports(
		builder: FileSpec.Builder,
		sequence: Sequence<KSAnnotated>
	) {

		sequence.toList().forEach {


			val classMetaData = classHelper.getClassMetaData (sequence.first() as KSClassDeclaration)

			classMetaData.let { cmd ->
				builder.addImport(cmd.packageName, cmd.simpleName)
				addImports(builder, cmd.interfaces)
				cmd.params.forEach { p ->
					addImports(builder, p.value.map { it.declaration } .asSequence())
				}
			}
		}
	}




	companion object {
		const val PREFIX = "ksp my stuff::"


	}

}


fun FileSpec.toFile(rootDir: File) : File {
	return File(rootDir, this.relativePath)
}




