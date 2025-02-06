package io.github.bsautner.ksp.routing

import com.squareup.kotlinpoet.CodeBlock
import io.github.bsautner.kobold.KoboldStatic
import io.github.bsautner.ksp.classtools.ClassHelper
import io.github.bsautner.ksp.classtools.ClassMetaData
import io.github.bsautner.ksp.classtools.id
import io.github.bsautner.ksp.routing.AutoRouter.Companion.PATH
import io.github.bsautner.ksp.util.ImportManager
import io.ktor.resources.Resource
import kotlin.collections.get

class RouteGenerator()  {

	fun addImports(metaData: ClassMetaData, note: String) {
		val map = mutableMapOf<String, MutableList<String>>()
		map.put(metaData.packageName, metaData.simpleName)
		metaData.imports.forEach {
			it.value.forEach { name ->
				if (name == "Login") {
					println("here.")
					val path = metaData.declaration.containingFile?.filePath
					println(path)
				}
				map.put(it.key, name)
				//map.put(it.key, "$name $note")
			}

		}
		metaData.typeParameters.forEach {
			addImports(it, "$note | REC ${metaData.simpleName}")
		}
		map.forEach {
			it.value.forEach { i ->
				ImportManager.addRouterImport(it.key, i)
			}
		}
		println("ksp Added Imports ${ImportManager.routerImports.size }")
	}

	fun createPostRoute(metaData: ClassMetaData) {

	 	addImports(metaData, "createPostRoute ${metaData.simpleName}")
		val block = CodeBlock.builder()


			metaData.typeParameters.let {

				val response = it[1]
				block.beginControlFlow("post<${metaData.simpleName}>")
				block.addStatement("val response : ${response.simpleName} = it.process(call.receive())")
				block.addStatement("call.respond(response,  typeInfo = TypeInfo(${response.simpleName}::class))")

				block.endControlFlow()
			}

		Routes.map[metaData] = block.build()
	}

	fun createGetRoute(metaData: ClassMetaData) {


		addImports(metaData, "createGetRoute ${metaData.simpleName}")
		val block = CodeBlock.builder()


			metaData.typeParameters.firstOrNull()?.let {

				val response = it.simpleName
				block.beginControlFlow("get<${metaData.simpleName}>")
				block.addStatement("call.respond(it.render.invoke() as $response, typeInfo = TypeInfo($response::class))")

				block.endControlFlow()
			}

		Routes.map[metaData] = block.build()

	}

	fun createStaticGetRoute(metaData: ClassMetaData) {
		val block = CodeBlock.Companion.builder()

		metaData.declaration.annotations.firstOrNull { it.shortName.asString() == Resource::class.simpleName }?.let {
			it.arguments.firstOrNull { check -> check.name?.asString() == PATH}?.let { resource ->
				metaData.declaration.annotations.firstOrNull { it.shortName.asString() == KoboldStatic::class.simpleName }?.let {
					it.arguments.firstOrNull { check -> check.name?.asString() == PATH }?.let { path ->
						block. beginControlFlow("staticResources(%S, %S)", resource.value, path.value)
							.addStatement("default(%S)", "index.html")
							. endControlFlow()
					}
				}
			}
		}


		Routes.map[metaData] = block.build()
	}

}

private fun MutableMap<String, MutableList<String>>.put(key: String, value: String) {
	 this.getOrPut(key) {mutableListOf<String>()}.add(value)

}
