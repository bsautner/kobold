package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import io.github.bsautner.kobold.KPost
import io.github.bsautner.kobold.KResponse
import io.github.bsautner.ksp.processor.RoutingGenerator.getRouteClassDeclaration

class ComposeGenerator(env: SymbolProcessorEnvironment) : BaseProcessor(env) {


    override fun create(sequence: Sequence<KSAnnotated>) {

        val classDeclaration = sequence.first() as KSClassDeclaration
        val outputClassName = "${classDeclaration.simpleName.asString()}Composable"
        val packageName = classDeclaration.packageName.asString()
        log("Creating Composable: $packageName.$outputClassName")
        val fileBuilder = FileSpec.builder(packageName, outputClassName)
        fileBuilder.tag(TargetPlatform::class, TargetPlatform.commonMain)
        addImports(fileBuilder, sequence)
        val functionBuilder = FunSpec.builder(outputClassName)
            .addAnnotation(ClassName("androidx.compose.runtime", "Composable"))
            .addCode(generate(sequence))

        fileBuilder
            .addFileComment(Const.comment)
            .addFunction(functionBuilder.build())
        log("Created Composable: ${fileBuilder.packageName}.${fileBuilder.name}" )
        writeToFile(fileBuilder.build())
    }



    override fun addImports(builder: FileSpec.Builder,sequence: Sequence<KSAnnotated>) {

        super.addImports(builder, sequence)

        builder.addImport("androidx.compose.foundation.layout", "Box", "fillMaxSize", "Column", "Spacer", "height", "padding")
            .addImport("androidx.compose.foundation", "border", "clickable")
            .addImport("androidx.compose.foundation.text", "BasicText", "BasicTextField")
            .addImport("androidx.compose.material", "Button", "Text")
            .addImport("androidx.compose.runtime", "Composable", "LaunchedEffect", "remember", "mutableStateOf", "getValue", "setValue")
            .addImport("androidx.compose.ui", "Modifier", "Alignment")
            .addImport("androidx.compose.ui.graphics", "Color")
            .addImport("androidx.compose.ui.text", "TextStyle")
            .addImport("androidx.compose.ui.unit", "dp")
            .addImport("kotlinx.coroutines", "CoroutineScope", "Dispatchers", "launch")
            .addImport("io.ktor.client.statement", "bodyAsText")
            .addImport("io.github.bsautner.kobold", "introspectSerializableClass")
            .addImport("io.github.bsautner.kobold.client", "ApiClient")


    }

    override fun generate(sequence: Sequence<KSAnnotated>): CodeBlock {
        val code = CodeBlock.builder()

        code.addStatement("val defaults = remember { mutableStateOf(mutableMapOf<String, String?>()) }")
        code.addStatement("var isInitialized by remember { mutableStateOf(false) }")
        val classDeclaration = sequence.first() as KSClassDeclaration

        val typeParams = classHelper.getTypeParameters(classDeclaration)



        typeParams.entries.firstOrNull()?.value?.firstOrNull()?.let { typeParams ->


            typeParams.defaultValues.forEach {

                code.addStatement("defaults.value[\"${it.key}\"] = ${it.value?.removeSuffix(")")}")
            }

            val route = getRouteClassDeclaration(classDeclaration) ?: ""
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




        return code.build()
    }

    private fun generateFormFields(classMetaData: ClassHelper.ClassMetaData): CodeBlock {
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
