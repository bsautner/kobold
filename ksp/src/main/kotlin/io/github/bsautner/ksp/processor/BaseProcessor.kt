package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.squareup.kotlinpoet.FileSpec
import io.github.bsautner.ksp.processor.util.touchFile
import java.io.File
import java.util.*

enum class PlatformType {
	jvm, js
}

open class BaseProcessor(env: SymbolProcessorEnvironment, val sessionId: String) {

	private var logger: KSPLogger = env.logger
	private var commonOutputDir : File = File(env.options["kmp-output-dir"].toString())
	private var jvmOutputDir : File = File(env.options["jvm-output-dir"].toString())
    private var enableDeleting = false //TODO make config

	fun log(text: Any) {
		 logger.warn("$PREFIX ${Date()} $text")
	}

	fun writeToFile(fileSpec: FileSpec, platform: PlatformType) {

		val outputDir = getOutputDir(platform)
		val historyFile = File(outputDir, sessionId.toString())

		if (! historyFile.exists()) {
			outputDir.mkdirs()
			touchFile(historyFile)
		}
		outputDir.let {

			val target = "$outputDir/${fileSpec.packageName.replace('.', '/')}/${fileSpec.name}.kt"

			val targetFile = File(target)
			log("Checking Target. Exists = ${targetFile.exists()}  $target")
			if (targetFile.exists()) {
				val content = targetFile.readText()
				val newContent = buildString {
					fileSpec.writeTo(this)
				}
				if (content.trim() == newContent.trim()) {
					log("Skipping identical content")
					historyFile.appendText ("${outputDir}/${fileSpec.relativePath}\n")
				} else {
					createFile(fileSpec, outputDir, historyFile)
				}
			} else {
				createFile(fileSpec, outputDir, historyFile)
			}
		}
	}

	private fun getOutputDir(platform: PlatformType): File {
		val outputDir = {
			when (platform) {
				PlatformType.jvm -> jvmOutputDir
				PlatformType.js -> commonOutputDir
			}
		}.invoke()
		return outputDir
	}

	fun createFile(fileSpec: FileSpec, outputDir : File, historyFile: File) : File {
		val result = fileSpec.writeTo(outputDir)
    	 historyFile.appendText("${result.absolutePath}\n")
		log("Created File : ${result.path}")
		return result
	}


	fun purge(env: SymbolProcessorEnvironment, sessionId: String) {

		PlatformType.entries.forEach {
			val outputDir = getOutputDir(it)
			val historyFile = File(outputDir, sessionId.toString())
			val goodList = mutableListOf<String>()
			if (historyFile.exists()) {


				log("Looking at logs: ${historyFile.path}")
				historyFile.readLines().forEach { goodFile ->
					if (! goodList.contains(goodFile)) {
						log("Preserving $goodFile from deletion" )
						goodList.add(goodFile)
					}

				}
			}

			log("Deleting old code from build preserving ${goodList.size}")
			env.options["jvm-output-dir"]?.let {
				deleteOldCode(File(it), goodList, historyFile)
			}

			env.options["kmp-output-dir"]?.let {
				deleteOldCode(File(it), goodList, historyFile)
			}

			log("Deleting history file: ${historyFile.absolutePath}")
			if (enableDeleting) {
				historyFile.delete()
			}


		}

	}


	fun deleteOldCode(dir: File, goodList: MutableList<String>, historyFile: File) {

		dir.listFiles().forEach {
			if (it.absolutePath != historyFile.absolutePath) {
				if (it.isDirectory) {
					deleteOldCode(it, goodList, historyFile)
				} else if (!goodList.contains(it.path)) {
					log("!!!DELETING Old Code ${it.path}")
					if (enableDeleting) { it.delete() }

				}
			}
		}
	}

	companion object {
		const val PREFIX = "benlog"


	}

}


fun FileSpec.toFile(rootDir: File) : File {
	return File(rootDir, this.relativePath)
}




