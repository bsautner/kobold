package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.squareup.kotlinpoet.FileSpec
import io.github.bsautner.ksp.processor.KoboldProcessorProvider.Companion.TEMP_PATH
import java.io.File
import java.util.*


open class BaseProcessor(env: SymbolProcessorEnvironment, val historyFile: File) {



	private var logger: KSPLogger = env.logger


	private var outputDir : File = File(env.options["output-dir"].toString())


	fun log(text: Any) {
		 logger.warn("$prefix ${Date()} $text")
	}

	fun writeToFile(fileSpec: FileSpec) {

		outputDir.let {

			val target = "$outputDir/${fileSpec.packageName.replace('.', '/')}/${fileSpec.name}.kt"

			val targetFile = File(target)
			log("Checking Target ${targetFile.exists()}  $target")
			if (targetFile.exists()) {
				val content = targetFile.readText()
			    	val newContent = buildString {
					fileSpec.writeTo(this)
				}
				if (content.trim() == newContent.trim()) {
					log("Skipping identical content")
					historyFile.writeText("${outputDir}/${fileSpec.relativePath}")
				} else {
					createFile(outputDir, fileSpec)
				}
			} else {
				createFile(outputDir, fileSpec,)
			}
		}
	}

	private fun createFile(file: File, fileSpec: FileSpec) {
		logger.warn("ksp creating file ${file.absolutePath}")
		file.mkdirs()
		fileSpec.writeTo(file)
		logger.warn("ksp writing file ${file.absolutePath} << ${fileSpec.relativePath}")
		 historyFile.writeText(file.absolutePath)
	}

	fun purge() {
		val goodList = mutableListOf<String>()
	    val tmpDir = File(TEMP_PATH)
		if (tmpDir.exists()) {

			tmpDir.listFiles().forEach {
				log("Looking at logs: ${it.path}")
				it.readLines().forEach { goodFile ->
					if (! goodList.contains(goodFile)) {
						log("Preserving $goodFile from deletion" )
						goodList.add(goodFile)
					}

				}
		  	it.delete()
			}
		}
		log("Deleting old code from build preserving ${goodList.size}")
		deleteOldCode(outputDir, goodList)
	}

	fun deleteOldCode(dir: File, goodList: MutableList<String>) {
		dir.listFiles().forEach {
			if (it.isDirectory) {
				deleteOldCode(it, goodList)
			} else if (! goodList.contains(it.path))  {
				log("Deleting Old Code ${it.path}")
			    it.delete()
			}
		}
	}

	companion object {
		const val prefix = "ksp cg:"


	}

}




