package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import io.github.bsautner.kobold.KComposable
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
 * fail on response and request objects not being Serializable
 */
class AutoRouter(val env: SymbolProcessorEnvironment) : BaseProcessor(env) {

	override fun create(sequence: Sequence<KSAnnotated>) {

		log("Creating Router ${env.options["project"]} with ${sequence.toList().size} symbols.")
		val project = env.options["project"] ?: "na"
		val fn = "${project.first().uppercase()}${project.removeRange(0, 1)}"
		val classPackage = Kobold::class.qualifiedName?.substringBeforeLast(".")
		val specBuilder = FileSpec.builder(classPackage!!, "$fn$TARGET_ROUTER_NAME")
		specBuilder.tag(TargetPlatform::class, TargetPlatform.jvmMain)
		addImports(specBuilder, sequence)
		specBuilder.addFunction(
			FunSpec
				.builder("$project$TARGET_ROUTER_NAME")
				.receiver(Application::class)
				.addCode(generate(sequence))
				.build()
		)
		val specFile = specBuilder.build()
		log("Router Created ${specFile.relativePath}")

		writeToFile(specFile)


	}

	override fun addImports(file: FileSpec.Builder, sequence: Sequence<KSAnnotated>) {
		super.addImports(file, sequence)

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
			val typeParams = classHelper.getTypeParameters(it)
			//val annotationClass = getAutoRoutingKClassName(it)
			typeParams.let { params ->
				val meta1 = classHelper.getClassMetaData(it)
				file.addImport(meta1.packageName, meta1.simpleName)

				val meta2 = classHelper.getClassMetaData(it)
				file.addImport(meta2.packageName, meta2.simpleName)
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

		val metaData = classHelper.getClassMetaData(declaration)
		log("Creating post router with metadata: $metaData")
		//log("--params: ${metaData.first().params.toList()}")
		//log("-- interfaces: ${metaData.first().interfaces.toList()}")
		val pth = KPost::class.qualifiedName
		log("$pth")
		pth?.let {
			metaData.params[pth]?.let {
				val request = it[0].simpleName
				val response = it[1].simpleName
				block.beginControlFlow("post<${metaData.simpleName}>")
				block.addStatement("val response : $response = it.process(call.receive())")
				block.addStatement("call.respond(response,  typeInfo = TypeInfo($response::class))")

				block.endControlFlow()
			}
		}

		return block.build()
	}

	private fun createStaticRoute(declaration: KSClassDeclaration) : CodeBlock {
		val block = CodeBlock.builder()
		declaration.annotations.firstOrNull { it.shortName.asString() == Resource::class.simpleName }?.let {
			it.arguments.firstOrNull { check -> check.name?.asString() == PATH}?.let { resource ->
				declaration.annotations.firstOrNull { it.shortName.asString() == KoboldStatic::class.simpleName }?.let {
				 it.arguments.firstOrNull { check -> check.name?.asString() == PATH }?.let { path ->
						block. beginControlFlow("staticResources(%S, %S)", resource.value, path.value)
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

		val metaData = classHelper.getTypeParameters(declaration)
		log(metaData)
//
//		metaData.first().let {
//
//			block.addStatement(" call.respond(it.render.invoke() as $it, typeInfo = TypeInfo($it::class))")
//
//		}

		block.endControlFlow()

		return block.build()
	}

	companion object {

		const val TARGET_ROUTER_NAME = "AutoRouter"

		const val PATH = "path"
	}

}





