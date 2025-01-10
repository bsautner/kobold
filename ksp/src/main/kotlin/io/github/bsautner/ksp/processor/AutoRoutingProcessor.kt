package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import java.io.File
import java.util.*

/**
 * TODO - add sorting and organize the generated routes.
 * add kdoc
 *
 *
 *
 *
 */

lateinit var logger: KSPLogger

private val processed = mutableSetOf<String>()

private var done = false

class AutoRoutingProcessor(val env: SymbolProcessorEnvironment) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger = env.logger
        log("AutoRoutingProcessor started ${env.options}")
        return emptyList()
    }
    private fun log(text: Any) {
        logger.warn("CG: ${Date()} $text")
    }
}





