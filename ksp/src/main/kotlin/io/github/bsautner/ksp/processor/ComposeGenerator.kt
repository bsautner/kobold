package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import io.github.bsautner.kobold.KPost
import io.github.bsautner.ksp.routing.RoutingGenerator.getRouteClassDeclaration
import io.github.bsautner.ksp.classtools.ClassMetaData
import io.github.bsautner.ksp.classtools.id
import io.github.bsautner.ksp.util.Const
import io.github.bsautner.ksp.util.ImportManager

class ComposeGenerator(env: SymbolProcessorEnvironment) : BaseProcessor(env) {


    fun create(metaData: ClassMetaData) {

        val classDeclaration = metaData.declaration
        val outputClassName = "${classDeclaration.simpleName.asString()}Composable"
        val packageName = classDeclaration.packageName.asString()
        log("Creating Composable: $packageName.$outputClassName")
        val fileBuilder = FileSpec.builder(packageName, outputClassName)
        fileBuilder.tag(TargetPlatform::class, TargetPlatform.commonMain)


        createComposables(classDeclaration, outputClassName, fileBuilder)

    }

    fun createComposables(classDeclaration: KSClassDeclaration, outputClassName: String, fileBuilder: FileSpec.Builder) {
        val functionBuilder = FunSpec.builder(outputClassName)
            .addAnnotation(ClassName("androidx.compose.runtime", "Composable"))
            .addCode(generate(classDeclaration))
        addBaseComposeImports(classDeclaration.id())
        fileBuilder
            .addFileComment(Const.comment)
            .addFunction(functionBuilder.build())
        log("Created Composable: ${fileBuilder.packageName}.${fileBuilder.name}" )
        writeToFile(fileBuilder.build())
    }


    private fun addBaseComposeImports(id: String) {
1
        ImportManager.apply {
            this.addImport(id, "androidx.compose.foundation.layout", "Box", "fillMaxSize", "Column", "Spacer", "height", "padding")
            this.addImport(id, "androidx.compose.foundation", "border", "clickable")
            this.addImport(id, "androidx.compose.foundation.text", "BasicText", "BasicTextField")
            this.addImport(id, "androidx.compose.material", "Button", "Text")
            this.addImport(id, "androidx.compose.runtime", "Composable", "LaunchedEffect", "remember", "mutableStateOf", "getValue", "setValue")
            this.addImport(id, "androidx.compose.ui", "Modifier", "Alignment")
            this.addImport(id, "androidx.compose.ui.graphics", "Color")
            this.addImport(id, "androidx.compose.ui.text", "TextStyle")
            this.addImport(id, "androidx.compose.ui.unit", "dp")
            this.addImport(id, "kotlinx.coroutines", "CoroutineScope", "Dispatchers", "launch")
            this.addImport(id, "io.ktor.client.statement", "bodyAsText")
            this.addImport(id, "io.github.bsautner.kobold", "introspectSerializableClass")
            this.addImport(id, "io.github.bsautner.kobold.client", "ApiClient")
        }



    }

     fun generate(classDeclaration: KSClassDeclaration): CodeBlock {
        val code = CodeBlock.builder()

        code.addStatement("val defaults = remember { mutableStateOf(mutableMapOf<String, String?>()) }")
        code.addStatement("var isInitialized by remember { mutableStateOf(false) }")

        val metaData = classHelper.getClassMetaData(classDeclaration)
        val interfaces = metaData.interfaces.map { it.qualifiedName }
     //   if (interfaces.contains(KPost::class.qualifiedName)) {
            log("KPost Detected, Creating Form")
            generateComposableForm(metaData, code)

     //   }

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
                    BasicText("${typeParams.simpleName}")
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
                        Text("OK 10")
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
