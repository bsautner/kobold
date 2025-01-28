package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import io.github.bsautner.kobold.annotations.Kobold
import java.io.File
import java.util.UUID

class AutoRouter(private val env: SymbolProcessorEnvironment,  historyFile: File) : BaseProcessor(env, historyFile) {

	fun createRouter(sequence: Sequence<KSAnnotated>) {
		log("Creating AutoRouter")
		val className = Kobold::class.simpleName
		val classPackage = Kobold::class.qualifiedName?.substringBeforeLast(".")
	}



}