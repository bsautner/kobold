package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import io.github.bsautner.kobold.KComposable
import io.github.bsautner.kobold.Kobold
import io.ktor.resources.*

class KoboldProcessor(env: SymbolProcessorEnvironment,  sessionId: String):  SymbolProcessor {
    private val composeGenerator = ComposeGenerator(env, sessionId)
    private val autoRouter: AutoRouter = AutoRouter(env, sessionId)

    override fun process(resolver: Resolver): List<KSAnnotated> {

        val resourcesName = Resource::class.qualifiedName!!
        val resourcesToProcess = resolver.getSymbolsWithAnnotation(resourcesName)
            .filter { it is KSClassDeclaration && it.validate() }
         if (resourcesToProcess.toList().isNotEmpty()) {
            autoRouter.create(resourcesToProcess)
        }

        val annotationFqName = Kobold::class.qualifiedName!!
        val symbols = resolver.getSymbolsWithAnnotation(annotationFqName)
            .filter { it is KSClassDeclaration && it.validate() }

        symbols.toList().forEach {
            processSymbols(sequenceOf(it))
        }


        return emptyList()
    }
    private fun processSymbols(sequence: Sequence<KSAnnotated>) {

                val classDeclaration = sequence.first() as KSClassDeclaration
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
