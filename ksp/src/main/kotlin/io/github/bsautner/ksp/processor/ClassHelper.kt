package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument
import io.github.bsautner.kobold.annotations.Kobold
import java.io.File

class ClassHelper {

    data class ClassMetaData(
                            val packageName: String,
                             val className: String,
                             val defaultValues : Map<String, String?>
    )

    fun getUserProvidedDataClass(classDeclaration: KSClassDeclaration): ClassMetaData {
        val serializableClass = getSerializableClassDeclaration(classDeclaration)
        if (serializableClass == null) {
            throw Exception("Could not get serializable class definition for ksp.")
        }
        val packageName = serializableClass.packageName.asString()
        val className = serializableClass.simpleName.asString()
        val defaultValues = extractDefaultValues(serializableClass)
        return ClassMetaData(packageName, className, defaultValues)
    }

    fun extractDefaultValues(classDeclaration: KSClassDeclaration): Map<String, String?> {
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

            // If a default value is found, store it; otherwise, set null
            defaultValues[paramName] = defaultValue
        }

        return defaultValues
    }

    private fun getSerializableClassDeclaration(classDeclaration: KSClassDeclaration): KSClassDeclaration? {
        val autoRoutingAnnotation = classDeclaration.annotations
            .firstOrNull { it.shortName.asString() == Kobold::class.simpleName }

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


}

fun KSClassDeclaration.getImport() : Pair<String, String> {
    val qualifiedName = this.qualifiedName?.asString()
        ?: throw IllegalArgumentException("Class declaration must have a qualified name")

    val packageName = qualifiedName.substringBeforeLast(".")
    val className = qualifiedName.substringAfterLast(".")
    return Pair(packageName, className)

}