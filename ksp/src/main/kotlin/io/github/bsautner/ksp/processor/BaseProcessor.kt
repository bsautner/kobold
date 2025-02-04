package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.squareup.kotlinpoet.FileSpec
import io.github.bsautner.ksp.processor.util.touchFile
import java.io.File
import java.util.*

//TODO mark all files dirty, process with not dirty mark and delete dirty files.

abstract class BaseProcessor(env: SymbolProcessorEnvironment) : KoboldClassBuilder{

	private var logger: KSPLogger = env.logger
	private var outputDirOption : File =
				File(env.options["output-dir"].toString())


	fun log(text: Any) {
		 logger.warn("${Date()} $text")
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



	companion object {
		const val PREFIX = "ksp my stuff::"


	}

}


fun FileSpec.toFile(rootDir: File) : File {
	return File(rootDir, this.relativePath)
}




