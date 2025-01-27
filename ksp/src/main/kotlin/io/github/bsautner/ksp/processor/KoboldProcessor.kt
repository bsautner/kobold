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
import java.util.*


lateinit var logger: KSPLogger

class KoboldProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    private val composeGenerator = ComposeGenerator(env)
    private val autoRouter : AutoRouter = AutoRouter ()
    private val processedSymbols = mutableSetOf<KSAnnotated>()
    private val processedResources = mutableSetOf<KSAnnotated>()


    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger = env.logger
        log("KSP Processor Started!!!")
        val resourcesName = Resource::class.qualifiedName!!

       val resourcesToProcess = resolver.getSymbolsWithAnnotation(resourcesName)
           .filter { it is KSClassDeclaration && it.validate() }
          .filterNot { it in processedResources }
        autoRouter.createRouter(resourcesToProcess)
        log("Found ${resourcesToProcess.toList().size} Resources")
        processedResources.addAll(resourcesToProcess)

        val annotationFqName = Kobold::class.qualifiedName!!
        val symbols = resolver.getSymbolsWithAnnotation(annotationFqName)
            .filter { it is KSClassDeclaration && it.validate() }
            .filterNot { it in processedSymbols }
            .toList()
        log("Found ${symbols.size} Annotated Classes")



        processedSymbols.addAll(symbols)
    //    processList(symbols)
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
                        KComposable::class.qualifiedName -> {
                            composeGenerator.createComposable(classDeclaration)
                        }
                    }
                }



                if (superTypeClassDecl.qualifiedName?.asString() == KComposable::class.qualifiedName) {
                    // This supertype is MyClass
                    log("${classDeclaration.simpleName.asString()} extends Kompose")
                }
            }
        }
    }

}


fun log(text: Any) {
    logger.warn("ksp cg: ${Date()} $text")
}




