package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import io.github.bsautner.kobold.KGet
import io.github.bsautner.kobold.KPost
import io.github.bsautner.kobold.KStatic
import io.github.bsautner.kobold.KWeb
import io.github.bsautner.kobold.Kobold
import io.ktor.resources.*
import io.ktor.server.application.*
import kotlin.reflect.KClass
import io.github.bsautner.kobold.KoboldStatic

/**
 * TODO - add sorting and organize the generated routes.
 * add kdocs
 *
 */
class AutoRouter(val env: SymbolProcessorEnvironment,  sessionId: String) : BaseProcessor(env, sessionId) {

	override fun create(sequence: Sequence<KSAnnotated>) {

		log("Creating Router with ${sequence.toList().size} symbols.")

		val classPackage = Kobold::class.qualifiedName?.substringBeforeLast(".")
		val specBuilder = FileSpec.builder(classPackage!!, TARGET_ROUTER_NAME)
		addImports(specBuilder, sequence)
		specBuilder.addFunction(
			FunSpec
				.builder(TARGET_ROUTER_FUN_NAME)
				.receiver(Application::class)
				.addCode(generate(sequence))
				.build()
		)
		val specFile = specBuilder.build()
		log("Router Created ${specFile.relativePath}")

		writeToFile(specFile, PlatformType.jvm)


	}

	override fun addImports(file: FileSpec.Builder, sequence: Sequence<KSAnnotated>) {


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

	override fun generate(sequence: Sequence<KSAnnotated>): CodeBlock {

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
				log("Processing Route: ${ksc.qualifiedName?.asString()}")
				if (ksc.implementsInterface(KGet::class)) {
						 builder.add(createGetRouter(ksc))
				}
				if (ksc.implementsInterface(KWeb::class)) {
						 builder.add(createWebRoute(ksc))
				}
 				if (ksc.implementsInterface(KStatic::class)) {
			            builder.add(createStaticRoute(ksc))
				}
				if (ksc.implementsInterface(KPost::class)) {
						builder.add(createPostRoute(ksc) )
				}

			}
		}

		return builder.build()
	}

	private fun createPostRoute(declaration: KSClassDeclaration) : CodeBlock {
		val block = CodeBlock.builder()

		val responseClass = getAutoRoutingKClassName(declaration)?.second
		block.beginControlFlow("post<${declaration.simpleName.asString()}>")
		block.addStatement("val response : $responseClass = it.process(call.receive())")
		block.addStatement(" call.respond(response,  typeInfo = TypeInfo($responseClass::class))")

		block.endControlFlow()
		return block.build()
	}

	private fun createStaticRoute(declaration: KSClassDeclaration) : CodeBlock {
		val block = CodeBlock.builder()
		declaration.annotations.firstOrNull { it.shortName.asString() == Resource::class.simpleName }?.let {
			it.arguments.firstOrNull { check -> check.name?.asString() == PATH}?.let { resource ->
				declaration.annotations.firstOrNull { it.shortName.asString() == KoboldStatic::class.simpleName }?.let {
				 it.arguments.firstOrNull { check -> check.name?.asString() == PATH }?.let { path ->
						block. beginControlFlow("staticFiles(%S, File(%S))", resource.value, path.value)
						.addStatement("default(%S)", "index.html")
						. endControlFlow()
					}
				}
			}
		}
		return block.build()
	}

	private fun createWebRoute(declaration: KSClassDeclaration): CodeBlock {
		val block = CodeBlock.builder()
		return block
			.beginControlFlow("get<${declaration.simpleName.asString()}>")
    	     .beginControlFlow("call.respondHtml")
		.beginControlFlow("body")
		.addStatement("it.render.invoke(this)")
		.endControlFlow().endControlFlow()
		.endControlFlow()
			.build()
	}

	fun createGetRouter(declaration: KSClassDeclaration): CodeBlock {
		val block = CodeBlock.builder()
		block.beginControlFlow("get<${declaration.simpleName.asString()}>")
		val responseClass = getAutoRoutingKClassName(declaration)
		responseClass?.let {
			block.addStatement(" call.respond(it.render.invoke() as ${responseClass.second}, typeInfo = TypeInfo(${responseClass.second}::class))")
		}
		block.endControlFlow()

		return block.build()
	}

	fun getAutoRoutingKClassName(declaration: KSClassDeclaration): Pair<String, String>? {

		val autoRoutingAnnotation = declaration.annotations
			.firstOrNull { it.shortName.asString() == Kobold::class.simpleName }

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

	companion object {

		const val TARGET_ROUTER_NAME = "AutoRouter"
		const val TARGET_ROUTER_FUN_NAME = "autoRoute"
		const val PATH = "path"
	}

}





