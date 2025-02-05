package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import io.github.bsautner.ksp.processor.util.touchFile
import java.io.File
import java.util.UUID

class KoboldProcessorProvider : SymbolProcessorProvider {
	override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
		return KoboldProcessor(environment) {}
	}


}
