package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import io.github.bsautner.kobold.Kompose
import io.ktor.resources.*
import java.util.*


lateinit var logger: KSPLogger

class KoboldProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    private val composeGenerator = ComposeGenerator(env)

    private val processedSymbols = mutableSetOf<KSAnnotated>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger = env.logger

        val annotationFqName = Resource::class.qualifiedName!!
        val symbols = resolver.getSymbolsWithAnnotation(annotationFqName)
            .filter { it is KSClassDeclaration && it.validate() }
            .filterNot { it in processedSymbols }
            .toList()

        processedSymbols.addAll(symbols)
        processList(symbols)
        return emptyList()
    }


    private fun processList(list: List<KSAnnotated>) {

        if (list.isNotEmpty()) {

            list.forEach {
                processClass(it as KSClassDeclaration)

            }
        }
    }

    private fun processClass(classDeclaration: KSClassDeclaration) {
        classDeclaration.superTypes.forEach {
            val superType = it.resolve()
            if (superType.declaration is KSClassDeclaration) {
                val superTypeClassDecl = superType.declaration as KSClassDeclaration
                 superTypeClassDecl.qualifiedName?.let { name ->
                    when (name.asString()) {
                        Kompose::class.qualifiedName -> {
                            composeGenerator.createComposable(classDeclaration)
                        }
                    }
                }

                if (superTypeClassDecl.qualifiedName?.asString() == Kompose::class.qualifiedName) {
                    // This supertype is MyClass
                    log("${classDeclaration.simpleName.asString()} extends Kompose")
                }
            }
        }
    }

}

private fun log(text: Any) {
    logger.warn("ksp cg: ${Date()} $text")
}





