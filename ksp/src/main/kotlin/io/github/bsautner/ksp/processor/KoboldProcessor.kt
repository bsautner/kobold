package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import io.github.bsautner.kobold.Kompose
import io.github.bsautner.kobold.annotations.KRouting
import io.ktor.resources.*
import io.ktor.server.routing.*
import java.io.File
import java.util.*


lateinit var logger: KSPLogger

class KoboldProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    private val processedSymbols = mutableSetOf<KSAnnotated>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger = env.logger
        log("Kobold Code Generator Started ${env.options}")

        //createClient(env)

        val annotationFqName = Resource::class.qualifiedName!!
        val symbols = resolver.getSymbolsWithAnnotation(annotationFqName)
            .filter { it is KSClassDeclaration && it.validate() }
            .filterNot { it in processedSymbols }
            .toList()

        processedSymbols.addAll(symbols)
        processList(symbols)
        return emptyList()
    }


    private fun processList(list: List<KSAnnotated>) {

        if (list.isNotEmpty()) {

            list.forEach {
                processClass(it as KSClassDeclaration)

            }
        }
    }

    private fun processClass(classDeclaration: KSClassDeclaration) {
        val name = (classDeclaration as KSClassDeclaration).qualifiedName?.asString()
        log("--ksp Processor $name")
        classDeclaration.annotations.forEach {
            log("----annotation Processor $it")
        }
        classDeclaration.superTypes.forEach {
            val superType = it.resolve()
            log("----super-type Processor ${it.toString()}")
            if (superType.declaration is KSClassDeclaration) {
                val superTypeClassDecl = superType.declaration as KSClassDeclaration
                // Compare fully qualified names
                superTypeClassDecl.qualifiedName?.let { name ->
                    when (name.asString()) {
                        Kompose::class.qualifiedName -> {
                            log("handling kompose")
                            createComposable(classDeclaration)
                        }
                    }
                }


                log("${superTypeClassDecl.qualifiedName?.asString()}")
                log("${Kompose::class.qualifiedName}")
                if (superTypeClassDecl.qualifiedName?.asString() == Kompose::class.qualifiedName) {
                    // This supertype is MyClass
                    log("${classDeclaration.simpleName.asString()} extends Kompose")
                }
            }
        }
    }

    private fun createComposable(classDeclaration: KSClassDeclaration) {
        val className = "${classDeclaration.simpleName.asString()}Composable"
        val packageName = classDeclaration.packageName.asString()
        log("--ksp Composable Processor $packageName $className")
        val outputClass = getAutoRoutingKClassName(classDeclaration)
        val route = getRouteClassDeclaration(classDeclaration)
        log("ROUTE: ${route}")
        log("--outputClass $outputClass")
        val annotation = getAutoRoutingKClassName(classDeclaration)?.second
        val defaults = getAutoRoutingKClassName(classDeclaration)?.third

        val defaultValues = extractDefaultValues(classDeclaration)
        val file = FileSpec.builder(packageName, className)
        addComposeImports(file, outputClass!!)
        val fun1 = FunSpec.builder(className)
            .addAnnotation(ClassName("androidx.compose.runtime", "Composable"))

        fun1.addCode("""
                 val defaults = remember { mutableStateOf(mutableMapOf<String, String?>()) }
                 var isInitialized by remember { mutableStateOf(false) }

        """.trimIndent())
        fun1.beginControlFlow(" LaunchedEffect(Unit)")
        defaults?.forEach {
            fun1.addCode("defaults.value[\"${it.key}\"] = ${it.value}\n")
        }
        fun1.addCode("isInitialized = true\n")
        fun1.endControlFlow()

        fun1.addCode("""
             if (!isInitialized) {
        // Show a loading state while defaults are not yet set
        Text("Loading...")
        return
    }
        """.trimIndent())


        fun1.addCode(
                """
                                           
                 
                   val fields = introspectSerializableClass<$annotation>()
                    Column {
                           Spacer(Modifier.height(16.dp))
                        BasicText("$annotation")
                           Spacer(Modifier.height(16.dp))
                        fields.forEach { field ->
                            println("field:${'$'}{field} ")
                            when (field.type) {
                                        "kotlin.String" -> {
                                          println("in string field ${"$"}{field.name} ${"$"}{defaults.value[field.name] }")

                                        Column {
                                                    Text(
                                                        text = field.name,
                                                        style = TextStyle(color = Color.Gray),
                                                        modifier = Modifier.padding(bottom = 4.dp)
                                                    )
                                                    BasicTextField(
                                                        value =  defaults.value[field.name] ?: "",
                                                    onValueChange = { newValue ->
                                defaults.value = defaults.value.toMutableMap().apply {
                                    this[field.name] = newValue
                                }
                            },
                                                        textStyle = TextStyle(color = Color.Black),
                                                        modifier = Modifier
                                                            .border(1.dp, Color.Gray)
                                                            .padding(8.dp)
                                                    )
                                                }
                                        }
                                  
                            else -> {}
                        }
                          Spacer(Modifier.height(16.dp))
                    }
                         
                    
                        Spacer(Modifier.height(16.dp))
                        // Simple clickable text acting as a "submit button"
                      
                        
                        Button(onClick = {
            CoroutineScope(Dispatchers.Default).launch {
                val postBody = ${outputClass.second}(
            
 
                
"""
        )

        //todo - make fields without defaults required
        //todo - elvis op return the correct type
        defaults?.forEach {
            fun1.addCode("""
                 ${it.key} = defaults.value["${it.key}"] ?: "",
                 
                    
            """.trimIndent())
        }
        fun1.addCode(")")


        fun1.addCode("""
         
                try {
                    val response = ApiClient.postData("${route}", postBody)
                    println("Response: ${"$"}{response.bodyAsText()}")
                } catch (e: Exception) {
                    println("Error:  ${"$"}{e.message}")
                }
            }
        }) {
            Text("OK")
        }
                        
                        
                    }
                
            """.trimIndent()
            )
           // .build()
        file
            .addFileComment(Copy.comment)
            .addFunction(fun1.build())
        val output = env.options["output-dir"]?.let { File(it) }
        output?.let {
            it.mkdirs()
            file.build().writeTo(it)
        }

    }



    private fun addComposeImports(builder: FileSpec.Builder, outputClass: Triple<String, String, Map<String, String?>>) {
        builder
            .addImport(
                "androidx.compose.foundation.layout",
                "Box",
                "fillMaxSize",
                "Column",
                "Spacer",
                "height",
                "padding"
            )
            .addImport("androidx.compose.foundation", "border", "clickable")
            .addImport("androidx.compose.foundation.text", "BasicText", "BasicTextField")
            .addImport("androidx.compose.material", "Button", "MaterialTheme", "Text", "TextField")
            .addImport("androidx.compose.material", "Text", "Button")
            .addImport("androidx.compose.runtime", "Composable", "LaunchedEffect", "remember", "mutableStateOf", "getValue", "setValue")
            .addImport("androidx.compose.ui", "Modifier", "Alignment")
            .addImport("androidx.compose.ui.graphics", "Color")

            .addImport("androidx.compose.ui.text", "TextStyle")
            .addImport("androidx.compose.ui.unit", "dp")
            .addImport("kotlinx.coroutines", "CoroutineScope", "Dispatchers", "launch")

            .addImport("io.ktor.client.statement", "bodyAsText")


            .addImport("io.github.bsautner.kobold", "introspectSerializableClass")
            .addImport("io.github.bsautner.kobold.client", "ApiClient")


            .addImport(outputClass.first, outputClass.second)


    }

    private fun log(text: Any) {
        logger.warn("ksp cg: ${Date()} $text")
    }

    private fun getRoute(classDeclaration: KSClassDeclaration): String? {
        return getRouteClassDeclaration(classDeclaration)


    }

    private fun getAutoRoutingKClassName(classDeclaration: KSClassDeclaration): Triple<String, String, Map<String, String?>>? {
        val serializableClass = getSerializableClassDeclaration(classDeclaration) ?: return null

        val qualifiedName = serializableClass.qualifiedName?.asString() ?: return null
        val packageName = serializableClass.packageName.asString()
        val className = serializableClass.simpleName.asString()

        // Extract default values
        val defaultValues = extractDefaultValues(serializableClass)
        log("***********defaultValues: $defaultValues")

        return Triple(packageName, className, defaultValues)
    }

    private fun getRouteClassDeclaration(classDeclaration: KSClassDeclaration): String? {
        val autoRoutingAnnotation = classDeclaration.annotations
            .firstOrNull { it.shortName.asString() == Resource::class.simpleName }

        autoRoutingAnnotation?.arguments?.forEach { argument ->
            log("!!!!!!!!!  ${argument.name?.getShortName()}")
            if (argument.name?.getShortName() == "path") {
                return argument.value.toString()

            }
        }
        return null
    }

    private fun getSerializableClassDeclaration(classDeclaration: KSClassDeclaration): KSClassDeclaration? {
        val autoRoutingAnnotation = classDeclaration.annotations
            .firstOrNull { it.shortName.asString() == KRouting::class.simpleName }

        autoRoutingAnnotation?.arguments?.forEach { argument ->
            if (argument.name?.getShortName() == "serializableResponse") {
                val kClassReference = argument.value
                if (kClassReference is KSType) {
                    return kClassReference.declaration as? KSClassDeclaration
                }
            }
        }
        return null
    }
    private fun extractDefaultValues(classDeclaration: KSClassDeclaration): Map<String, String?> {
        val defaultValues = mutableMapOf<String, String?>()

        // Read the original source file
        val fileText = classDeclaration.containingFile?.let { file ->
            file.filePath?.let { path -> File(path).readText() }
        } ?: return defaultValues

        // Handle missing primary constructor (common in data classes)
        val constructorParams = classDeclaration.primaryConstructor?.parameters
            ?: classDeclaration.getAllFunctions()
                .firstOrNull { it.simpleName.asString() == "<init>" }
                ?.parameters
            ?: return defaultValues

        constructorParams.forEach { param ->
            val paramName = param.name?.asString() ?: return@forEach

            // Regex to extract `paramName: Type = defaultValue`
            val regex = Regex("$paramName\\s*:\\s*\\w+\\s*=\\s*(\\S+)")
            val match = regex.find(fileText)

            val defaultValue = match?.groupValues?.get(1)?.let {
                 it.trim().trimEnd(',')
            }
            log("!!! default: $defaultValue")
            // If a default value is found, store it; otherwise, set null
            defaultValues[paramName] = defaultValue
        }

        return defaultValues
    }

    private fun createClient(env: SymbolProcessorEnvironment) {

        //   if (!File("$generatedDir/Test").exists()) {
        val className = "TestBuild"
        val file = FileSpec.builder("io.github.bsautner", className)
        log("creating client for $className")
        //    val outputDir = File(generatedDir)
        //  log("Directory:${outputDir.exists()} ${outputDir.absolutePath} ")
        // outputDir.mkdirs()

        //  file.addAnnotation(annotationSpec)
        //   file.addAnnotation(ClassName("kotlin.js", "JsExport"))
        ///  file.addImport("kotlin.js", "JsExport", "ExperimentalJsExport")
        //     .addCode(CodeBlock.builder().addStatement("test") .build())
        //file.addCode (topBlock)
        val generatedClass = TypeSpec.classBuilder(className)
            .addProperty(
                PropertySpec.builder("bar", String::class)
                    .initializer("\"ben49\"")
                    .build()
            ).build()
        file.addType(generatedClass)
        //  log(file.build())
        log(file.build().relativePath)
        val f = File(file.build().relativePath)
        log(f.exists().toString())
        val output = env.options["output-dir"]?.let { File(it) }
        output?.let {
            it.mkdirs()
            file.build().writeTo(it)
        }
        val check = File(output, file.build().relativePath)
        log("checking: ${check.exists()}")
        if (check.exists()) {

        }

        // file.build().writeTo(env.codeGenerator, false)

        //  val writeTo = file.build().writeTo(outputDir)
        log("done client 1")
    }
}






