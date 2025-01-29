package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import io.github.bsautner.ksp.processor.util.touchFile
import java.io.File
import java.util.UUID

class KoboldProcessorProvider : SymbolProcessorProvider {
	override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {

		if (environment.options[OUTPUT_DIR] == null) {
			logger.error(
				"Kobold output directory not set!.  Please add this to your build.gradle: ksp {\n" +
						"    arg(\"source\", \"demo\")\n" +
						"    arg(\"output-dir\", \"${"$"}{layout.buildDirectory.get().asFile}/generated/ksp/common/kotlin\")\n" +
						"}"
			)
		}

		val sessionId = UUID.randomUUID()
	    var outputDir = File(environment.options[OUTPUT_DIR].toString())
		val historyFile = File(outputDir, sessionId.toString())

		if (! historyFile.exists()) {
			outputDir.mkdirs()
			touchFile(historyFile)
		}


		return KoboldProcessor(environment, historyFile)
	}
	companion object {
		const val OUTPUT_DIR = "output-dir"
	}

}
