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