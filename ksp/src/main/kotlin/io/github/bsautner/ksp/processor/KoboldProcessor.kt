/*
 *
 *  * Copyright (c) 2025 Benjamin Sautner
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import io.github.bsautner.kobold.KComposable
import io.github.bsautner.kobold.KGet
import io.github.bsautner.kobold.KMenu
import io.github.bsautner.kobold.KPost
import io.github.bsautner.kobold.KStatic
import io.github.bsautner.kobold.Kobold
import io.github.bsautner.kobold.KoboldStatic
import io.github.bsautner.ksp.classtools.ClassHelper
import io.github.bsautner.ksp.routing.AutoRouter
import io.github.bsautner.ksp.routing.RouteGenerator
import io.github.bsautner.ksp.routing.Routes
import io.github.bsautner.ksp.util.touchFile
import java.io.File
import kotlin.math.log

class KoboldProcessor(val env: SymbolProcessorEnvironment, private val onProcess: (KSClassDeclaration) -> Unit):  SymbolProcessor {

    private val classHelper = ClassHelper()
    private val autoRouter: AutoRouter = AutoRouter(env)
    private val logger = env.logger
    private val outputDirectory: File
        get() = env.options["output-dir"]?.takeIf { it.isNotBlank() }?.let { File(it) }
            ?: throw KoboldKSPException("You must set the output \"output-dir\" directory in your gradle ksp{} section ")

    override fun process(resolver: Resolver): List<KSAnnotated> {
            logger.info("*******Starting Kobold*************")

            val annotations = listOf(Kobold::class.qualifiedName!!, KoboldStatic::class.qualifiedName!!)
            val symbols = annotations.flatMap { resolver.getSymbolsWithAnnotation(it) }
            //    .filterIsInstance<KSClassDeclaration>()
            //    .filter { it.validate() }

            symbols.forEach {
                logger.info("${it::class.simpleName}")
                if (it is KSClassDeclaration) {
                    logger.info("Processing KSClass...")
                    onProcess(it)
                    logger.info("Processing Kobold Annotated Code: ${it.qualifiedName?.asString()}", it)
                    processSymbol(it, ::createFileCallback)
                }

            }
            return emptyList()
        }

    override fun finish() {
        super.finish()
        val trace = traceFile().readLines()
        trace.forEach {
            logger.info("IO-OP: Traced $it")
        }
        cleanup(outputDirectory, trace)
        traceFile().delete()
        logger.info("*************Kobold KSP Completed***********")
    }

    /**
     * Iterate over each symbol annotated with @Kobold
     */
    private fun processSymbol(declaration: KSClassDeclaration,  callback: (String) -> Unit    ) {
        logger.warn("ksp Processing Symbol: ${declaration.qualifiedName?.asString()}")
        val metaData = classHelper.getClassMetaData(declaration)
        val className = metaData.qualifiedName
        val routeGenerator = RouteGenerator()
        val composeGenerator = ComposeGenerator(env)

        val details = metaData.interfaces.map { it.qualifiedName } + metaData.baseClasses.map { it.qualifiedName }
        details.forEach { name ->


            logger.warn("ksp checking $name")
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
                    composeGenerator.create(metaData, ::createFileCallback)
                }

                KMenu::class.qualifiedName -> {
                    composeGenerator.create(metaData, ::createFileCallback)
                }


            }
            logger.info("---$className Implements: $name")
        }
        declaration.getSealedSubclasses().forEach { sc ->
            val name = sc.qualifiedName?.asString()
            logger.info("----- Sealed Sub Class $name")
            processSymbol(sc, callback)
        }

        if (Routes.map.isNotEmpty()) {
            autoRouter.create(::createFileCallback)
        }
    }

    private fun cleanup(file: File, trace: List<String>) {

        file.listFiles().forEach {
            if (it.isDirectory) {
                cleanup(it, trace)
            } else {
                if (it.extension == "kt" && ! trace.contains(it.absolutePath)) {
                    logger.info("IO-OP: Deleting obsolete file: ${it.name}")
                    it.delete()
                }
            }
        }
    }

    private fun createFileCallback(path: String) {
        logger.info("IO-OP: Create File Callback: $path")
        traceFile().appendText("$path\n")
    }
    private fun traceFile() : File {
        val file =  File(outputDirectory, TRACE)
        if (! file.exists()) {
            outputDirectory.mkdirs()
            touchFile(file)
        }
        return file
    }


    companion object {
        private const val TRACE = "trace.txt"
    }

}
