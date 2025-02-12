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

package io.github.bsautner.ksp.util

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec

object ImportManager {
	val imports = mutableMapOf<KSClassDeclaration, MutableMap<String, MutableList<String>>>()
	val routerImports = mutableMapOf<String, MutableList<String>>()

	fun addRouterImport(importPackage: String, vararg className: String) {
		className.forEach {
			routerImports.getOrPut(importPackage) { className.toMutableList() }.add(it)
		}


	}

	fun addImport(id: KSClassDeclaration, importPackage: String, vararg className: String) {
	        val packageImports = imports.getOrPut(id) { mutableMapOf() }
	        val classList = packageImports.getOrPut(importPackage) { mutableListOf() }
	        classList.addAll(className)
	    }


	fun addRouterImportBlock(fileSpec: FileSpec.Builder) : FileSpec.Builder {

			routerImports.forEach { item ->
				item.value.forEach { v ->
					 	fileSpec.addImport(item.key, v)

				}

			}
 		return fileSpec
	}

	fun addImportBlock(id: KSClassDeclaration, fileSpec: FileSpec.Builder) : FileSpec.Builder {
		imports[id]?.let {
			it.forEach { item ->

				fileSpec.addImport(item.key, item.value)
			}

		}

		return fileSpec
	}


}