package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.writeTo
import io.github.bsautner.ksp.Kompose
import io.github.bsautner.ksp.annotations.KRouting
import io.github.bsautner.ksp.introspectSerializableClass
import io.ktor.resources.*
import java.io.File
import java.util.*
import kotlin.reflect.full.primaryConstructor


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
        log("--outputClass $outputClass")
        val annotation = getAutoRoutingKClassName(classDeclaration)?.second

        val file = FileSpec.builder(packageName, className)
        addComposeImports(file)
        val fun1 = FunSpec.builder(className)
            .addAnnotation(ClassName("androidx.compose.runtime", "Composable"))
            .addCode(
                """
                   var name by remember { mutableStateOf("") }
                   val fields = introspectSerializableClass<$annotation>()
                   
                    Column {
                        // Label
                           Spacer(Modifier.height(16.dp))
                        BasicText("$annotation")
                           Spacer(Modifier.height(16.dp))
                        fields.forEach { field ->
                            println("field:${'$'}{field} ")
                            when (field.type) {
                                        "kotlin.String" -> {
                                          println("in string field...")
                                        Column {
                                                    Text(
                                                        text = field.name,
                                                        style = TextStyle(color = Color.Gray),
                                                        modifier = Modifier.padding(bottom = 4.dp)
                                                    )
                                                    BasicTextField(
                                                        value = name,
                                                        onValueChange = { name = it },
                                                        textStyle = TextStyle(color = Color.Black),
                                                        modifier = Modifier
                                                            .border(1.dp, Color.Gray)
                                                            .padding(8.dp)
                                                    )
                                                }
                                        }
                                  
                            else -> {
                                    BasicTextField(
                                            value = name,
                                            onValueChange = { name = it }
                                    )
                            }
                        }
                          Spacer(Modifier.height(16.dp))
                    }
                         
                    
                        Spacer(Modifier.height(16.dp))
                        // Simple clickable text acting as a "submit button"
                        BasicText(
                            text = "Submit",
                            modifier = Modifier.clickable {
                                // Handle form submission
                                println("Form submitted with name: name")
                            }
                        )
                    }
                
            """.trimIndent()
            )
            .build()
        file
            .addFileComment(Copy.comment)
            .addFunction(fun1)


        val output = env.options["output-dir"]?.let { File(it) }
        output?.let {
            it.mkdirs()
            file.build().writeTo(it)
        }

    }

    private fun addComposeImports(builder: FileSpec.Builder) {
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
            .addImport("androidx.compose.runtime", "Composable", "remember", "mutableStateOf", "getValue", "setValue")
            .addImport("androidx.compose.ui", "Modifier", "Alignment")
            .addImport("androidx.compose.ui.graphics", "Color")

            .addImport("androidx.compose.ui.text", "TextStyle")
            .addImport("androidx.compose.ui.unit", "dp")


            .addImport("io.github.bsautner.ksp", "introspectSerializableClass")


    }

    private fun log(text: Any) {
        logger.info("ksp cg: ${Date()} $text")
    }


    private fun getAutoRoutingKClassName(classDeclaration: KSClassDeclaration): Pair<String, String>? {
        // Find the AutoRouting annotation

        classDeclaration.annotations.forEach {
            log("getAutoRoutingKClassName ${it.shortName.asString()}")
        }

        val autoRoutingAnnotation = classDeclaration.annotations
            .firstOrNull { it.shortName.asString() == KRouting::class.simpleName }


        // If the annotation is present, retrieve its argument
        autoRoutingAnnotation?.arguments?.forEach { argument: KSValueArgument ->

            if (argument.name?.getShortName() == "serializableResponse") {
                val kClassReference = argument.value

                if (kClassReference is KSType) {
                    val param = kClassReference.declaration
                    val p = param::class.primaryConstructor
                    p?.let {
                        p.parameters.forEach {
                            log("param $it")
                        }
                    }
                    val qualifiedName = param.qualifiedName?.asString()

                    param.packageName.let { packageName ->
                        param.simpleName.let { simpleNameName ->
                            return Pair(packageName.asString(), simpleNameName.asString())
                        }
                    }


                }

            }
        }

        return null
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






