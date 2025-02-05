package io.github.bsautner.utils



import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import io.github.bsautner.ksp.processor.KoboldProcessor

class TestProcessorProvider(private val onProcess: (KSClassDeclaration) -> Unit) : SymbolProcessorProvider {
	override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
		KoboldProcessor(environment, onProcess)
}

