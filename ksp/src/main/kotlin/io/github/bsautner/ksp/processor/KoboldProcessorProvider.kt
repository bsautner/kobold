package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import java.io.File
import java.util.UUID

class KoboldProcessorProvider : SymbolProcessorProvider {
	override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
		val sessionId = UUID.randomUUID()
		val historyDirectory = File(TEMP_PATH)
		val historyFile = File(historyDirectory, sessionId.toString())

		if (! historyFile.exists()) {
			touchFile(historyFile)
		}

		if (environment.options["output-dir"] == null) {
			logger.error(
				"Kobold output directory not set!.  Please add this to your build.gradle: ksp {\n" +
						"    arg(\"source\", \"demo\")\n" +
						"    arg(\"output-dir\", \"${"$"}{layout.buildDirectory.get().asFile}/generated/ksp/common/kotlin\")\n" +
						"}"
			)
		}
		return KoboldProcessor(environment, historyFile)
	}
	companion object {
		const val  TEMP_PATH = "/tmp/kobold"
	}
}

fun touchFile(file: File) {

	if (!file.exists()) {
		file.createNewFile()
	} else {
		file.setLastModified(System.currentTimeMillis())
	}

}