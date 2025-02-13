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

package io.github.bsautner.ksp.routing

import com.squareup.kotlinpoet.CodeBlock
import io.github.bsautner.kobold.KoboldStatic
import io.github.bsautner.ksp.classtools.ClassMetaData
import io.github.bsautner.ksp.routing.AutoRouter.Companion.PATH
import io.github.bsautner.ksp.util.ImportManager
import io.ktor.resources.*

class RouteGenerator()  {

	fun addImports(metaData: ClassMetaData, note: String) {
		val map = mutableMapOf<String, MutableList<String>>()
		map.put(metaData.packageName, metaData.simpleName)
		metaData.imports.forEach {
			it.value.forEach { name ->
				map.put(it.key, name)
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
		ImportManager.addRouterImport("java.io", "File")
		metaData.declaration.annotations.firstOrNull { it.shortName.asString() == Resource::class.simpleName }?.let {
			it.arguments.firstOrNull { check -> check.name?.asString() == PATH}?.let { resource ->
				metaData.declaration.annotations.firstOrNull { it.shortName.asString() == KoboldStatic::class.simpleName }?.let {
					it.arguments.firstOrNull { check -> check.name?.asString() == PATH }?.let { path ->
						block.beginControlFlow("staticFiles(%S, File(%S))",  resource.value, path.value)
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
