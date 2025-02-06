package io.github.bsautner.ksp.util

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import org.slf4j.event.KeyValuePair

object ImportManager {
	val imports = mutableMapOf<String, MutableMap<String, MutableList<String>>>()
	val routerImports = mutableMapOf<String, MutableList<String>>()

	fun addRouterImport(importPackage: String, vararg className: String) {
		className.forEach {
			routerImports.getOrPut(importPackage) { className.toMutableList() }.add(it)
		}


	}

	fun addImport(id: String, importPackage: String, vararg className: String) {

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

	fun addImportBlock(id: String, fileSpec: FileSpec.Builder) : FileSpec.Builder {
		imports[id]?.let {
			it.forEach { item ->

				fileSpec.addImport(item.key, item.value)
			}

		}

		return fileSpec
	}


}