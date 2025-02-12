/*
 *
 *  * Copyright (c) 2025 Benjamin Sautner
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.squareup.kotlinpoet.FileSpec
import io.github.bsautner.ksp.classtools.ClassHelper
import io.github.bsautner.ksp.classtools.areCodeStringsIdentical
import java.io.File
import java.util.Date

//TODO mark all files dirty, process with not dirty mark and delete dirty files.

abstract class BaseProcessor(env: SymbolProcessorEnvironment) : KoboldClassBuilder{

	private var logger: KSPLogger = env.logger
	private val output = env.options["output-dir"] ?: "/tmp/kobold"
	private var outputDirOption : File = File(output)
	val classHelper = ClassHelper()

	fun log(text: Any) {
		 logger.info("${Date()} $text")
	}

	fun writeToFile(fileSpec: FileSpec, callback : (String) -> Unit) {
		log(fileSpec.tags)
		log("IO-OP: Writing to Platform: ${fileSpec.tag(TargetPlatform::class)}")
		fileSpec.tag(TargetPlatform::class)?.let {

			val outputDir = File("$outputDirOption/${it.name}/kotlin")

			val target = "${outputDir.absolutePath}/${fileSpec.packageName.replace('.', '/')}/${fileSpec.name}.kt"

			val targetFile = File(target)
			log("IO-OP: Checking Target. Exists = ${targetFile.exists()}  $target")
			if (targetFile.exists()) {
				val content = targetFile.readText()
				val newContent = buildString {
					fileSpec.writeTo(this)
				}
				if (!areCodeStringsIdentical(content, newContent)) {
					log("IO-OP: Skipping identical content $target")
					callback.invoke(targetFile.absolutePath)
				} else {
					createFile(fileSpec, outputDir, callback)
				}
			} else {
				   createFile(fileSpec, outputDir, callback)
			}
		}
	}

	fun createFile(fileSpec: FileSpec, outputDir : File, callback: (String) -> Unit) : File {
		val result = fileSpec.writeTo(outputDir)
		log("IO-OP: Created File : ${result.path}")
		callback.invoke(result.absolutePath)
		return result
	}

}
	fun FileSpec.toFile(rootDir: File) : File {
		return File(rootDir, this.relativePath)
	}

