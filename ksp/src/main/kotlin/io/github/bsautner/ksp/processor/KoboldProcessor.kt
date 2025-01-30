package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import io.github.bsautner.kobold.KComposable
import io.github.bsautner.kobold.annotations.Kobold
import io.ktor.resources.*
import java.io.File
import java.util.*


lateinit var logger: KSPLogger



class KoboldProcessor(private val env: SymbolProcessorEnvironment, historyFile:  File) : SymbolProcessor {
    private val composeGenerator = ComposeGenerator(env, historyFile)
    private val autoRouter : AutoRouter = AutoRouter (env, historyFile)
    private val processedSymbols = mutableSetOf<KSAnnotated>()
    private val processedResources = mutableSetOf<KSAnnotated>()


    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger = env.logger
        val resourcesName = Resource::class.qualifiedName!!

       val resourcesToProcess = resolver.getSymbolsWithAnnotation(resourcesName)
           .filter { it is KSClassDeclaration && it.validate() }
          .filterNot { it in processedResources }


        if (resourcesToProcess.toList().isNotEmpty()) {
            autoRouter.create(resourcesToProcess)
        }

        processedResources.addAll(resourcesToProcess)

        val annotationFqName = Kobold::class.qualifiedName!!
        val symbols = resolver.getSymbolsWithAnnotation(annotationFqName)
            .filter { it is KSClassDeclaration && it.validate() }
            .filterNot { it in processedSymbols }



        processedSymbols.addAll(symbols)
        processList(symbols)
     //   purge()
        return emptyList()
    }

    private fun processList(sequence: Sequence<KSAnnotated>) {

        if (sequence.toList().isNotEmpty()) {

            sequence.forEach {
                val single = sequenceOf(it)
                processSymbols(single)
            }
        }
    }
    private fun processSymbols(sequence: Sequence<KSAnnotated>) {
        sequence.toList().forEach {
            val classDeclaration = it as KSClassDeclaration
            classDeclaration.superTypes.forEach {
                val superType = it.resolve()
                if (superType.declaration is KSClassDeclaration) {
                    val superTypeClassDecl = superType.declaration as KSClassDeclaration
                    superTypeClassDecl.qualifiedName?.let { name ->
                        when (name.asString()) {
                            KComposable::class.qualifiedName -> {
                                composeGenerator.create(sequence)
                            }
                        }
                    }
                }
            }
        }

    }


}



