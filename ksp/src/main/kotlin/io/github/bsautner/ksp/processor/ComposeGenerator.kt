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
import io.github.bsautner.kobold.KPost
import io.github.bsautner.ksp.routing.RoutingGenerator.getRouteClassDeclaration
import io.github.bsautner.ksp.classtools.ClassMetaData
import io.github.bsautner.ksp.util.Const
import io.github.bsautner.ksp.util.ImportManager
import java.util.Date

class ComposeGenerator(env: SymbolProcessorEnvironment) : BaseProcessor(env) {


    fun create(metaData: ClassMetaData, callback : (String) -> Unit) {

        val classDeclaration = metaData.declaration
        val outputClassName = "${classDeclaration.simpleName.asString()}Composable"
        val packageName = classDeclaration.packageName.asString()
        log("Creating Composable: $packageName.$outputClassName")
        val fileBuilder = FileSpec.builder(packageName, outputClassName)
        fileBuilder.tag(TargetPlatform::class, TargetPlatform.commonMain)


        createComposables(classDeclaration, outputClassName, fileBuilder, callback)

    }

    fun createComposables(classDeclaration: KSClassDeclaration, outputClassName: String, fileBuilder: FileSpec.Builder, callback: (String) -> Unit) {
        addBaseComposeImports(classDeclaration)

        val functionBuilder = FunSpec.builder(outputClassName)
            .addAnnotation(ClassName("androidx.compose.runtime", "Composable"))
            .addCode(generate(classDeclaration))
        addBaseComposeImports(classDeclaration)
        fileBuilder
            .addFileComment(Const.comment)
            .addFunction(functionBuilder.build())
        log("Created Composable: ${fileBuilder.packageName}.${fileBuilder.name}" )
        ImportManager.addImportBlock(classDeclaration, fileBuilder)
        writeToFile(fileBuilder.build(), callback)
    }


    private fun addBaseComposeImports(declaration: KSClassDeclaration) {

        val meta = classHelper.getClassMetaData(declaration)

1
        ImportManager.apply {

            meta.imports.forEach { pack ->
                pack.value.forEach { cls ->
                    this.addImport(declaration, pack.key,  cls)
                }

            }
            meta.interfaces.forEach {
                this.addImport(declaration, it.declaration.packageName.asString(), it.declaration.simpleName.asString())
            }


            this.addImport(declaration, "androidx.compose.foundation.layout", "Box", "fillMaxSize", "Column", "Spacer", "height", "padding")
            this.addImport(declaration, "androidx.compose.foundation", "border", "clickable")
            this.addImport(declaration, "androidx.compose.foundation.text", "BasicText", "BasicTextField")
            this.addImport(declaration, "androidx.compose.material", "Button", "Text")
            this.addImport(declaration, "androidx.compose.runtime", "Composable", "LaunchedEffect", "remember", "mutableStateOf", "getValue", "setValue")
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

     fun generate(classDeclaration: KSClassDeclaration): CodeBlock {
        val code = CodeBlock.builder()

        code.addStatement("val defaults = remember { mutableStateOf(mutableMapOf<String, String?>()) }")
        code.addStatement("var isInitialized by remember { mutableStateOf(false) }")

        val metaData = classHelper.getClassMetaData(classDeclaration)
        val interfaces = metaData.interfaces.map { it.qualifiedName }
        if (interfaces.contains(KPost::class.qualifiedName)) {
            log("KPost Detected, Creating Form")
            generateComposableForm(metaData, code)

        }

        return code.build()
    }

    private fun generateComposableForm(
        metaData: ClassMetaData,
        code: CodeBlock.Builder
    ) {
        val typeParams = metaData.typeParameters
        typeParams.firstOrNull()?.let { typeParams ->
            code.addStatement("// This reflects the class you used for KPost<T, R> where T is the post body of this form")
            typeParams.defaultValues.forEach {

                code.addStatement("defaults.value[\"${it.key}\"] = ${it.value?.removeSuffix(")")}")
            }

            val route = getRouteClassDeclaration(metaData.declaration) ?: ""
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
