package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.squareup.kotlinpoet.FileSpec
import io.github.bsautner.ksp.processor.util.parentDir
import java.io.File
import java.util.*


open class BaseProcessor(env: SymbolProcessorEnvironment, val historyFile: File) {

	private var logger: KSPLogger = env.logger
	private var outputDir : File = File(env.options["output-dir"].toString())
    private var enableDeleting = true //TODO make config

	fun log(text: Any) {
		 logger.warn("$PREFIX ${Date()} $text")
	}

	fun writeToFile(fileSpec: FileSpec) {

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
					createFile(fileSpec)
				}
			} else {
				createFile(fileSpec,)
			}
		}
	}

	fun createFile(fileSpec: FileSpec) : File {
		val result = fileSpec.writeTo(outputDir)
    	 historyFile.appendText("${result.absolutePath}\n")
		log("Created File : ${result.path}")
		return result
	}


	fun purge() {

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
		deleteOldCode(outputDir, goodList)
		log("Deleting history file: ${historyFile.absolutePath}")
	   if (enableDeleting) {  historyFile.delete() }
	}


	fun deleteOldCode(dir: File, goodList: MutableList<String>) {

		dir.listFiles().forEach {
			if (it.absolutePath != historyFile.absolutePath) {
				if (it.isDirectory) {
					deleteOldCode(it, goodList)
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




