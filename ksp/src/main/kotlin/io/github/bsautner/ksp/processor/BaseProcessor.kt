package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.squareup.kotlinpoet.FileSpec
import io.github.bsautner.ksp.classtools.ClassHelper
import java.io.File

//TODO mark all files dirty, process with not dirty mark and delete dirty files.

abstract class BaseProcessor(env: SymbolProcessorEnvironment) : KoboldClassBuilder{

	private var logger: KSPLogger = env.logger
	private val output = env.options["output-dir"] ?: "/tmp/kobold"
	private var outputDirOption : File = File(output)
	val classHelper = ClassHelper()

	fun log(text: Any) {
		 logger.warn("ben: $text")
	}

	fun writeToFile(fileSpec: FileSpec) {
		log(fileSpec.tags)
		log("Writing to file: ${fileSpec.tag(TargetPlatform::class)}")
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


//		list.forEach {
//				builder.addImport(it.packageName, it.simpleName)
//				addImports(builder, it.typeParameters)
//				addImports(builder, it.interfaces)
//			}
//		}
}
	fun FileSpec.toFile(rootDir: File) : File {
		return File(rootDir, this.relativePath)
	}

