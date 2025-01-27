package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.symbol.KSAnnotated
import io.github.bsautner.kobold.annotations.Kobold

class AutoRouter {

	fun createRouter(sequence: Sequence<KSAnnotated>) {
		 log("Creating AutoRouter")
		val className = Kobold::class.simpleName
		val classPackage = Kobold::class.qualifiedName?.substringBeforeLast(".")
	}

}