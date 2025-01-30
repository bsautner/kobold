package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.writeTo
import io.github.bsautner.kobold.KGet
import io.github.bsautner.kobold.KPost
import io.github.bsautner.kobold.KStatic
import io.github.bsautner.kobold.KWeb
import io.github.bsautner.kobold.annotations.Kobold
import io.ktor.http.parseHeaderValue
import io.ktor.resources.*
import io.ktor.server.application.*
import java.io.File
import java.util.*
import kotlin.reflect.KClass
import com.google.devtools.ksp.symbol.*
import io.github.bsautner.kobold.annotations.KoboldStatic

/**
 * TODO - add sorting and organize the generated routes.
 * add kdocs
 *
 */
class AutoRouter(val env: SymbolProcessorEnvironment,  sessionId: String) : BaseProcessor(env, sessionId), SymbolProcessor {

	override fun process(resolver: Resolver): List<KSAnnotated> {
		val annotationFqName = Resource::class.qualifiedName!!
		val symbols = resolver.getSymbolsWithAnnotation(annotationFqName)
		val sequence = symbols.filter { it is KSClassDeclaration && it.validate() }
		val js = env.options["js"] == "true"
		val generatedDir = env.options["ksp.generated.dir"] //?: error("KSP generated directory not specified!")
		log("generatedDir: $generatedDir")

		if (sequence.toList().isNotEmpty()) {

			createRouter(sequence)


		}
		return sequence.toList()
	}

	fun createRouter(sequence: Sequence<KSAnnotated>) {

		log("Creating Router with ${sequence.toList().size} symbols.")
		//  log("starting code generation 2 ${env.options}")
		val className = "AutoRouter"
		val classPackage = Kobold::class.qualifiedName?.substringBeforeLast(".")


		val specBuilder = FileSpec.builder(classPackage!!, className)
		addImports(specBuilder, sequence)
		specBuilder.addFunction(
			FunSpec
				.builder("autoRoute")
				.receiver(Application::class)
				.addCode(buildCodeBlock(sequence))
				.build()
		)
		val specFile = specBuilder.build()
		log("Router Created ${specFile.relativePath}")

		writeToFile(specFile, PlatformType.jvm)


	}

	private fun addImports(file: FileSpec.Builder, sequence: Sequence<KSAnnotated>) {


		file.addImport("io.ktor.server.routing", "routing")
			.addImport("io.ktor.server.resources", "get", "post", "delete", "put")
			.addImport("io.ktor.util.reflect", "TypeInfo")
			.addImport("io.ktor.server.html", "respondHtml")
			.addImport("kotlinx.html", "html", "body", "div")
			.addImport("io.github.bsautner.kobold", "getPostBodyClass", "getPostResponseBodyClass")
			.addImport("io.ktor.server.request", "receive")
			.addImport("io.ktor.server.request", "receiveMultipart", "receiveParameters")
			.addImport("io.ktor.http", "HttpStatusCode")
			.addImport("kotlin.reflect", "safeCast")
			.addImport("io.ktor.server.http.content", "staticResources", "staticFiles")
			.addImport("java.io", "File")



		sequence.toList().forEach {

			val import = (it as KSClassDeclaration).getImport()
			file.addImport(import.first, import.second)

			val annotationClass = getAutoRoutingKClassName(it)
			annotationClass?.let {
				file.addImport(annotationClass.first, annotationClass.second)
			}

		}
	}


	private fun buildCodeBlock(sequence: Sequence<KSAnnotated>): CodeBlock {

		log("Building Code Block from ${sequence.toList().size}")
		val builder = CodeBlock.builder()
		builder
			.beginControlFlow("routing")
			.add(buildRouteCodeBlock(sequence))
			.endControlFlow()

		return builder.build()
	}


	private fun buildRouteCodeBlock(sequence: Sequence<KSAnnotated>): CodeBlock {
		val builder = CodeBlock.builder()
		log("Building Code Block for ${sequence.toList().size} routes")
		sequence.toList().forEach {
			(it as KSClassDeclaration).let { ksc ->

				val getBlock = CodeBlock.builder()
				log("Processing Route: ${ksc.qualifiedName?.asString()}")
				if (ksc.implementsInterface(KGet::class)) {
					val target = getAutoRoutingKClassName(ksc)
					log("target $target")
					getBlock.beginControlFlow("get<${ksc.simpleName.asString()}>")
					//	getBlock.add("call.respond(HttpStatusCode.OK,  it.render.invoke())")
					val responseClass = getAutoRoutingKClassName(ksc)
					getBlock.addStatement("// response class ${responseClass == null}")
					responseClass?.let {
						getBlock.addStatement(" call.respond(it.render.invoke() as ${responseClass.second}, typeInfo = TypeInfo(${responseClass.second}::class))")
					}
					getBlock.endControlFlow()
				}
				if (ksc.implementsInterface(KWeb::class)) {
					/**
					 *   routing {
					 *         get<Website.HomePage> {
					 *             call.respondHtml {
					 *
					 *                 body {
					 *                     it.render.invoke(this)
					 *
					 *                 }
					 *             }
					 *
					 *         }
					 *     }
					 */

					getBlock.beginControlFlow("get<${ksc.simpleName.asString()}>")

					getBlock.beginControlFlow("call.respondHtml")
					getBlock.beginControlFlow("body")
					getBlock.addStatement("it.render.invoke(this)")
					getBlock.endControlFlow().endControlFlow()

					// getBlock.addStatement(" call.respond(it.render.invoke() as ${responseClass.second}, typeInfo = TypeInfo(${responseClass.second}::class))")

					getBlock.endControlFlow()
				}


				//				staticFiles("/", File("build/dist/wasmJs/productionExecutable")) {
//					default("index.html")
//				}
				if (ksc.implementsInterface(KStatic::class)) {
					log("Processing Static Resources")

					ksc.annotations.firstOrNull { it.shortName.asString() == Resource::class.simpleName }?.let {
						log("Found Static Resource Annotation")
					     it.arguments.firstOrNull { check -> check.name?.asString() == "path" }?.let { resource ->
							log("Found Path Resource ${resource.value}")
						     ksc.annotations.firstOrNull { it.shortName.asString() == KoboldStatic::class.simpleName }?.let {
							     log("Found Static Annotation")
							     val path = it.arguments.firstOrNull { check -> check.name?.asString() == "path" }?.let { path ->
								     log("Found Path Param ${path.value}")

								     getBlock. beginControlFlow("staticFiles(%S, File(%S))", resource.value, path.value)
								     getBlock.addStatement("default(%S)", "index.html")
								     getBlock. endControlFlow()

							     }

						     }
						}

					}
				}

				if (ksc.implementsInterface(KPost::class)) {
					/**
					 *   routing {
					 *         post<Sensor> {
					 *             val body = call.receive(it.getPostBodyClass())
					 *             val response = it.process(body as TestPostBody)
					 *             call.respond(response, TypeInfo(it.getPostResponseBodyClass()))
					 *         }
					 *     }
					 *

					 */
					val responsePackage = getAutoRoutingKClassName(ksc)?.first
					val responseClass = getAutoRoutingKClassName(ksc)?.second

					log("***************${responseClass!!::class.simpleName}")
					log("***************${responsePackage}")
					if (responseClass.isEmpty() == true) {
					//	logger.error("AutoRouter processed a POST but the post body KClass is missing from the Annotation.")
					}

					getBlock.beginControlFlow("post<${ksc.simpleName.asString()}>")
					getBlock.addStatement("val response : $responseClass = it.process(call.receive())")
					getBlock.addStatement(" call.respond(response,  typeInfo = TypeInfo($responseClass::class))")

					getBlock.endControlFlow()
				}
				builder.add(getBlock.build())
			}
		}

		return builder.build()
	}

//fun KSClassDeclaration.getImport() : Pair<String, String> {
//	val qualifiedName = this.qualifiedName?.asString()
//		?: throw IllegalArgumentException("Class declaration must have a qualified name")
//
//	val packageName = qualifiedName.substringBeforeLast(".")
//	val className = qualifiedName.substringAfterLast(".")
//	return Pair(packageName, className)
//
//}

	fun getAutoRoutingKClassName(classDeclaration: KSClassDeclaration): Pair<String, String>? {
		// Find the AutoRouting annotation

//	classDeclaration.annotations.forEach {
//		log(it.shortName.asString())
//	}

		val autoRoutingAnnotation = classDeclaration.annotations
			.firstOrNull { it.shortName.asString() == Kobold::class.simpleName }


		// If the annotation is present, retrieve its argument
		autoRoutingAnnotation?.arguments?.forEach { argument: KSValueArgument ->

			if (argument.name?.getShortName() == "serializableResponse") {
				val kClassReference = argument.value

				if (kClassReference is KSType) {
					val param = kClassReference.declaration
					val qualifiedName = param.qualifiedName?.asString()

					param.packageName.let { packageName ->
						param.simpleName.let { simpleNameName ->
							return Pair(packageName.asString(), simpleNameName.asString())
						}
					}


				}

			}
		}

		return null
	}


	// Function to check if the class or any of its superclasses implement the given interface
	fun KSClassDeclaration.implementsInterface(interfaceClass: KClass<*>): Boolean {
		val interfaceFqn = interfaceClass.qualifiedName ?: return false

		fun KSClassDeclaration.checkSuperTypes(): Boolean {
			// Check if any of the super types match the interface
			return this.superTypes.any { superTypeRef: KSTypeReference ->
				val resolvedType: KSType = superTypeRef.resolve()
				val declaration = resolvedType.declaration as? KSClassDeclaration

				// Check if the resolved type's qualified name matches the interface
				if (resolvedType.declaration.qualifiedName?.asString() == interfaceFqn) {
					return true
				}

				// Recursively check the super types of the current super class
				declaration?.checkSuperTypes() ?: false
			}
		}

		return checkSuperTypes()
	}

}





