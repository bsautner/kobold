package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import io.github.bsautner.kobold.KComposable
import io.github.bsautner.kobold.KCompose
import io.github.bsautner.kobold.KGet
import io.github.bsautner.kobold.KPost
import io.github.bsautner.kobold.KStatic
import io.github.bsautner.kobold.Kobold
import io.github.bsautner.kobold.KoboldStatic
import io.github.bsautner.ksp.classtools.ClassHelper
import io.github.bsautner.ksp.routing.AutoRouter
import io.github.bsautner.ksp.routing.RouteGenerator
import io.github.bsautner.ksp.routing.Routes

class KoboldProcessor(env: SymbolProcessorEnvironment, private val onProcess: (KSClassDeclaration) -> Unit):  SymbolProcessor {
    private val composeGenerator = ComposeGenerator(env)
    private val classHelper = ClassHelper()
    private val autoRouter: AutoRouter = AutoRouter(env)
    private val logger = env.logger
   override fun process(resolver: Resolver): List<KSAnnotated> {
            logger.warn("*******Starting Kobold*************")

            val annotations = listOf(Kobold::class.qualifiedName!!, KoboldStatic::class.qualifiedName!!)
            val symbols = annotations.flatMap { resolver.getSymbolsWithAnnotation(it) }
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.validate() }

            symbols.forEach {
                onProcess(it)
                logger.warn("Processing Kobold Annotated Code: ${it.qualifiedName?.asString()}")
                processSymbol(it)
            }
            return emptyList()
        }

    /**
     * Iterate over each symbol annotated with @Kobold
     */
    private fun processSymbol(declaration: KSClassDeclaration) {
        logger.warn("ksp Processing Symbol: ${declaration.qualifiedName?.asString()}")
        val metaData = classHelper.getClassMetaData(declaration)
        val className = metaData.qualifiedName
        val routeGenerator = RouteGenerator()

        metaData.interfaces.forEach { i ->
            val name = i.qualifiedName
            logger.warn("ksp checking ${i.qualifiedName}")
            when (name) {
                KPost::class.qualifiedName -> {
                    routeGenerator.createPostRoute(metaData)
                }

                KGet::class.qualifiedName -> {
                    routeGenerator.createGetRoute(metaData)
                }

                KStatic::class.qualifiedName -> {
                    routeGenerator.createStaticGetRoute(metaData)
                }

                KComposable::class.qualifiedName -> {
                    composeGenerator.create(metaData)
                }


            }
            logger.info("---$className Implements: $name")
        }
        declaration.getSealedSubclasses().forEach { sc ->
            val name = sc.qualifiedName?.asString()
            logger.info("----- Sealed Sub Class $name")
            processSymbol(sc)
        }

        if (Routes.map.isNotEmpty()) {
            autoRouter.create()
        }
    }

}
