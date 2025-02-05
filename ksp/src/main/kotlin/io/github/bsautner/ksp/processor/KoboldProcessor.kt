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
import kotlin.math.log

class KoboldProcessor(env: SymbolProcessorEnvironment, private val onProcess: (KSClassDeclaration) -> Unit):  SymbolProcessor {
    private val composeGenerator = ComposeGenerator(env)
    private val autoRouter: AutoRouter = AutoRouter(env)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val resourcesName = Resource::class.qualifiedName!!
        processKtorResources(resolver, resourcesName, onProcess)
        processKoboldAnnotations(resolver, onProcess)
        return emptyList()
    }

    private fun processKoboldAnnotations(resolver: Resolver, onProcess: (KSClassDeclaration) -> Unit) {
        val annotationFqName = Kobold::class.qualifiedName!!
        val symbols = resolver.getSymbolsWithAnnotation(annotationFqName)
            .filter { it is KSClassDeclaration && it.validate() }
        symbols.toList().forEach {
            onProcess(it as KSClassDeclaration)
            processSymbols(sequenceOf(it))
        }
    }

    private fun processKtorResources(resolver: Resolver, resourcesName: String, onProcess: (KSClassDeclaration) -> Unit) {
        val resourcesToProcess = resolver.getSymbolsWithAnnotation(resourcesName)
            .filter { it is KSClassDeclaration && it.validate() }
        if (resourcesToProcess.toList().isNotEmpty()) {
            resourcesToProcess.toList().forEach {
                onProcess(it as KSClassDeclaration)
            }
            autoRouter.create(resourcesToProcess)
        }
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
