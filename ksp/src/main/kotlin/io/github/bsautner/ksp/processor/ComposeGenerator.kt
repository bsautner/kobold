package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import io.github.bsautner.ksp.processor.RoutingGenerator.getRouteClassDeclaration
import java.io.File
import java.util.UUID

class ComposeGenerator(env: SymbolProcessorEnvironment,  historyFile: File) : BaseProcessor(env, historyFile) {

    private val classHelper = ClassHelper()

    fun createComposable(classDeclaration: KSClassDeclaration) {
        val outputClassName = "${classDeclaration.simpleName.asString()}Composable"
        val packageName = classDeclaration.packageName.asString()
        val classMetaData = classHelper.getUserProvidedDataClass(classDeclaration)
        val route = getRouteClassDeclaration(classDeclaration) ?: ""

        val fileBuilder = FileSpec.builder(packageName, outputClassName)
        addComposeImports(fileBuilder, classMetaData)

        val functionBuilder = FunSpec.builder(outputClassName)
            .addAnnotation(ClassName("androidx.compose.runtime", "Composable"))
            .addCode(generateComposableBody(classMetaData, route))

        fileBuilder
            .addFileComment(Const.comment)
            .addFunction(functionBuilder.build())
        log("Created Composable: ${fileBuilder.packageName}.${fileBuilder.name}" )
        writeToFile(fileBuilder.build())
    }

    private fun generateComposableBody(classMetaData: ClassHelper.ClassMetaData, route: String): CodeBlock {
        val code = CodeBlock.builder()

        code.addStatement("val defaults = remember { mutableStateOf(mutableMapOf<String, String?>()) }")
        code.addStatement("var isInitialized by remember { mutableStateOf(false) }")

        // LaunchedEffect block for initialization
        code.beginControlFlow("LaunchedEffect(Unit)")
        classMetaData.defaultValues.forEach {
            code.addStatement("defaults.value[\"${it.key}\"] = ${it.value}")
        }
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
            val fields = introspectSerializableClass<${classMetaData.className}>()
            
            LaunchedEffect(fields) {
                val validationState = fields.associate { field ->
                    field.name to (field.hasDefault || defaults.value[field.name]?.isNotBlank() == true)
                }
                requiredFieldsValid.value = validationState.toMutableMap()
            }
            
            Column {
                Spacer(Modifier.height(16.dp))
                BasicText("${classMetaData.className}")
                Spacer(Modifier.height(16.dp))
            """.trimIndent()
        )
        code.add("\n")
        // Generate form fields
        code.add(generateFormFields(classMetaData))

        // Submit button
        code.add("\n")
        code.add(
            """
                val isFormValid = requiredFieldsValid.value.all { it.value }
                
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.Default).launch {
                            val postBody = ${classMetaData.className}(
            """.trimIndent()
        )

        classMetaData.defaultValues.forEach {
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
                    Text("OK 4")
                }
            }
            """.trimIndent()
        )

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

    private fun addComposeImports(builder: FileSpec.Builder, classMetaData: ClassHelper.ClassMetaData) {
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
            .addImport(classMetaData.packageName, classMetaData.className)
    }


}
