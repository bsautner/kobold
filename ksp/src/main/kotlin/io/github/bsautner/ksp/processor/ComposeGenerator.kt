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

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import io.github.bsautner.kobold.KMenu
import io.github.bsautner.kobold.KPost
import io.github.bsautner.kobold.util.Multimap
import io.github.bsautner.ksp.routing.RoutingGenerator.getRouteClassDeclaration
import io.github.bsautner.ksp.classtools.ClassMetaData
import io.github.bsautner.ksp.util.Const
import io.github.bsautner.ksp.util.ImportManager
import kotlinx.coroutines.CoroutineScope
import java.util.Date

class ComposeGenerator(env: SymbolProcessorEnvironment) : BaseProcessor(env) {


	fun create(metaData: ClassMetaData, callback: (String) -> Unit) {

		val classDeclaration = metaData.declaration
		val outputClassName = "${classDeclaration.simpleName.asString()}Composable"
		val packageName = classDeclaration.packageName.asString()
		log("Creating Composable: $packageName.$outputClassName")
		val fileBuilder = FileSpec.builder(packageName, outputClassName)
		fileBuilder.tag(TargetPlatform::class, TargetPlatform.commonMain)


		createComposables(metaData, classDeclaration, outputClassName, fileBuilder, callback)

	}

	fun createComposables(
		metaData: ClassMetaData,
		classDeclaration: KSClassDeclaration,
		functionName: String,
		fileBuilder: FileSpec.Builder,
		callback: (String) -> Unit
	) {
		addBaseComposeImports(classDeclaration)

		val callbackLambda = LambdaTypeName.get(
			parameters = listOf(
				ParameterSpec.builder("callback", String::class).build()
			),
			returnType = Unit::class.asTypeName()
		)

		val paramSpec = ParameterSpec.builder("callback", callbackLambda).build()
		val scopeSpec = ParameterSpec.builder("scope", CoroutineScope::class).build() //context: CoroutineScope
		val functionBuilder = FunSpec.builder(functionName)
			.addParameter(scopeSpec)
			.addParameter(paramSpec)
			.addAnnotation(ClassName("androidx.compose.runtime", "Composable"))
			.addCode(generateComposable(metaData))
		addBaseComposeImports(classDeclaration)
		fileBuilder
			.addFileComment(Const.comment)
			.addFunction(functionBuilder.build())
		log("Created Composable: ${fileBuilder.packageName}.${fileBuilder.name}")
		ImportManager.addImportBlock(classDeclaration, fileBuilder)
		val built = fileBuilder.build()
		writeToFile(built, callback)
	}


	private fun addBaseComposeImports(declaration: KSClassDeclaration) {

		val meta = classHelper.getClassMetaData(declaration)

		1
		ImportManager.apply {

			meta.imports.forEach { pack ->
				pack.value.forEach { cls ->
					this.addImport(declaration, pack.key, cls)
				}

			}
			meta.interfaces.forEach {
				this.addImport(declaration, it.declaration.packageName.asString(), it.declaration.simpleName.asString())
			}


			this.addImport(
				declaration,
				"androidx.compose.foundation.layout",
				"Box",
				"fillMaxSize",
				"Column",
				"Spacer",
				"height",
				"padding"
			)
			this.addImport(declaration, "androidx.compose.foundation", "border", "clickable")
			this.addImport(declaration, "androidx.compose.foundation.text", "BasicText", "BasicTextField")
			this.addImport(declaration, "androidx.compose.material", "Button", "Text")
			this.addImport(
				declaration,
				"androidx.compose.runtime",
				"Composable",
				"LaunchedEffect",
				"remember",
				"mutableStateOf",
				"getValue",
				"setValue"
			)
			this.addImport(declaration, "androidx.compose.ui", "Modifier", "Alignment")
			this.addImport(declaration, "androidx.compose.ui.graphics", "Color")
			this.addImport(declaration, "androidx.compose.ui.text", "TextStyle")
			this.addImport(declaration, "androidx.compose.ui.unit", "dp")
			this.addImport(declaration, "kotlinx.coroutines", "CoroutineScope", "Dispatchers", "launch")
			this.addImport(declaration, "io.ktor.client.statement", "bodyAsText")
			this.addImport(declaration, "io.github.bsautner.kobold", "introspectSerializableClass")
			this.addImport(declaration, "io.github.bsautner.kobold.client", "ApiClient")
		}


	}

	fun generateComposable(metaData: ClassMetaData): CodeBlock {
		val code = CodeBlock.builder()

		metaData.baseClasses.firstOrNull { it.qualifiedName == KMenu::class.qualifiedName }?.let {
			log("KMenu Detected, Creating Menu")
			generateComposableMenu(metaData, code)
		}

		metaData.interfaces.firstOrNull { it.qualifiedName == KPost::class.qualifiedName }?.let {

			generateComposableForm(metaData, code)

		}

		return code.build()
	}

	private fun generateComposableMenu(
		metaData: ClassMetaData,
		code: CodeBlock.Builder
	) {

		addLoadingBlock(code)

		val subclassMap = classHelper.subclassMap(metaData.declaration)
		subclassMap.getAll().forEach { entry ->
			entry.value.forEach { value ->
				ImportManager.addImport(metaData.declaration, entry.key.qualifiedName!!.asString(), value.simpleName.asString())
			}

		}

		subclassMap.getAll().values.forEach { list ->
			list.forEach { c ->
				ImportManager.addImport(metaData.declaration, c.packageName.asString(), c.simpleName.asString())
			}

		}

		val parentMap = mutableMapOf<String, Boolean>()
		subclassMap.getAll().values.forEach { values ->
			values.forEach {
				val hasChildren =subclassMap.get(it)?.isNotEmpty() == true
				parentMap[it.qualifiedName!!.asString()] = hasChildren
			}
		}

		subclassMap.getAll().forEach {

				code.addStatement("var ${it.key.simpleName.asString().lowercase()}Expanded by remember { mutableStateOf(false) }")


		}


		code.beginControlFlow("Box")
		createDropdownMenu(code, metaData)

		code.endControlFlow() // Box

		ImportManager.addImport(metaData.declaration, "androidx.compose.foundation.layout", "Box")
		ImportManager.addImport(metaData.declaration, "androidx.compose.foundation.layout", "padding")

		ImportManager.addImport(metaData.declaration, "androidx.compose.material", "DropdownMenu")
		ImportManager.addImport(metaData.declaration, "androidx.compose.material", "DropdownMenuItem")
		ImportManager.addImport(metaData.declaration, "androidx.compose.material.icons", "Icons")
		ImportManager.addImport(metaData.declaration, "androidx.compose.material.icons.filled", "MoreVert")
		ImportManager.addImport(metaData.declaration, "androidx.compose.material", "Icon")
		ImportManager.addImport(metaData.declaration, "androidx.compose.material", "IconButton")


	}

	private fun createDropdownMenu(
		code: CodeBlock.Builder,
		metaData: ClassMetaData
	) {
		val subclassMap = classHelper.subclassMap(metaData.declaration)

		addDropdown(metaData.declaration, metaData.declaration, subclassMap, code)
	}


	private fun addDropdown(
		declaration: KSClassDeclaration,
		subclass: KSClassDeclaration,
		subclassMap: Multimap<KSClassDeclaration, KSClassDeclaration>,
		code: CodeBlock.Builder
	) {

		val hasChildren = classHelper.hasChildren(declaration, subclass)
		val menuItemName = subclass.simpleName.asString()
		if (hasChildren) {
			code.beginControlFlow("IconButton(onClick = { ${menuItemName.lowercase()}Expanded = !${menuItemName.lowercase()}Expanded }) ")
			code.addStatement("Icon(Icons.Default.MoreVert, contentDescription = \"$menuItemName\")")
			code.endControlFlow()
			code.beginControlFlow("DropdownMenu(expanded = ${menuItemName.lowercase()}Expanded, onDismissRequest = {${menuItemName.lowercase()}Expanded = false})")

			subclassMap.get(subclass)?.forEach { sc ->
				addDropdown(declaration, sc, subclassMap, code)
			}
			code.endControlFlow()
		} else {
			code.addStatement("DropdownMenuItem(content = {$menuItemName.render()}, onClick = {scope.launch { callback.invoke(\"$menuItemName\") }})")


		}


	}

	private fun generateComposableForm(
		metaData: ClassMetaData,
		code: CodeBlock.Builder
	) {

		code.addStatement("val defaults = remember { mutableStateOf(mutableMapOf<String, String?>()) }")

		val typeParams = metaData.typeParameters
		typeParams.firstOrNull()?.let { typeParams ->
			code.addStatement("// This reflects the class you used for KPost<T, R> where T is the post body of this form")
			typeParams.defaultValues.forEach {

				code.addStatement("defaults.value[\"${it.key}\"] = ${it.value?.removeSuffix(")")}")
			}

			addLoadingBlock(code)
			val route = getRouteClassDeclaration(metaData.declaration) ?: ""
			// State for validation tracking
			code.add("\n")

			code.add(
				"""
                val requiredFieldsValid = remember { mutableStateOf(mutableMapOf<String, Boolean>()) }
                val fields = introspectSerializableClass<${typeParams.simpleName}>()
                
                LaunchedEffect(fields) {
                    val validationState = fields.associate { field ->
                        field.name to (field.hasDefault || defaults.value[field.name]?.isNotBlank() == true)
                    }
                    requiredFieldsValid.value = validationState.toMutableMap()
                }
                
                Column {
                    Spacer(Modifier.height(16.dp))
                    BasicText("${Date()}")
                    Spacer(Modifier.height(16.dp))
                """.trimIndent()
			)
			code.add("\n")
			// Generate form fields
			code.add(generateFormFields(typeParams))

			// Submit button
			code.add("\n")
			code.add(
				"""
                    val isFormValid = requiredFieldsValid.value.all { it.value }
                    
                    Button(
                        onClick = {
                            CoroutineScope(Dispatchers.Default).launch {
                                val postBody = ${typeParams.simpleName}(
                """.trimIndent()
			)

			typeParams.defaultValues.forEach {
				code.addStatement("${it.key} = defaults.value[\"${it.key}\"] ?: \"\",")
			}

			code.add(
				"""
                                )
                                try {
                                    val response = ApiClient.postData("$route", postBody)
                                    println("Response: ${"$"}{response.bodyAsText()}")
                                } catch (e: Exception) {
                                    println("Error: ${"$"}{e.message}")
                                }
                            }
                        },
                        enabled = isFormValid
                    ) {
                        Text("OK")
                    }
                }
                """.trimIndent()

			)


		}
	}

	private fun addLoadingBlock(
		code: CodeBlock.Builder
	) {
		code.addStatement("var isInitialized by remember { mutableStateOf(false) }")
		// LaunchedEffect block for initialization
		code.beginControlFlow("LaunchedEffect(Unit)")

		code.addStatement("isInitialized = true")
		code.endControlFlow()

		// Loading State
		code.add(
			"""
                    if (!isInitialized) {
                        Text("Loading...")
                        return
                    }
                    """.trimIndent()
		)
		code.addStatement("\n")

	}

	private fun generateFormFields(classMetaData: ClassMetaData): CodeBlock {
		val code = CodeBlock.builder()
		code.beginControlFlow("fields.forEach { field ->")

		code.beginControlFlow("when (field.type)")
		code.beginControlFlow("\"kotlin.String\" ->")

		code.add(
			"""
            Column {
                Text(
                    text = field.name,
                    style = TextStyle(color = Color.Gray),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                BasicTextField(
                    value = defaults.value[field.name] ?: "",
                    onValueChange = { newValue ->
                        defaults.value = defaults.value.toMutableMap().apply {
                            this[field.name] = newValue
                        }
                        requiredFieldsValid.value = requiredFieldsValid.value.toMutableMap().apply {
                            this[field.name] = newValue.isNotBlank()
                        }
                    },
                    textStyle = TextStyle(
                        color = if (field.hasDefault || requiredFieldsValid.value[field.name] == true)
                            Color.Black
                        else
                            Color.Red
                    ),
                    modifier = Modifier
                        .border(1.dp, if (field.hasDefault || requiredFieldsValid.value[field.name] == true)
                            Color.Gray
                        else
                            Color.Red)
                        .padding(8.dp)
                )
            }
            """.trimIndent()
		)

		code.endControlFlow() // Close String case
		code.endControlFlow() // Close when block
		code.endControlFlow() // Close fields.forEach

		return code.build()
	}

}
