package io.github.bsautner.ksp.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import io.github.bsautner.kobold.KResponse
import io.github.bsautner.kobold.Kobold
import kotlinx.serialization.Serializable
import java.io.File
import kotlin.reflect.KClass

data class TypeParams(val request: KSClassDeclaration?, val response: KSClassDeclaration?)

class ClassHelper {

    data class ClassMetaData(
        val packageName: String,
        val className: String,
        val defaultValues: Map<String, String?>,
        val params: List<ClassMetaData>,
        val interfaces: Sequence<KSClassDeclaration>
    )

//    fun getUserProvidedDataClass(classDeclaration: KSClassDeclaration): ClassMetaData? {
//        val serializableClass = getSerializableClassDeclaration(classDeclaration)
//        serializableClass?.let {
//            val packageName = serializableClass.packageName.asString()
//            val className = serializableClass.simpleName.asString()
//            val defaultValues = extractDefaultValues(serializableClass)
//            return ClassMetaData(packageName, className, defaultValues)
//        }
//        return null
//
//    }

    fun getClassMetaData(classDeclaration: KSClassDeclaration): ClassMetaData {

        val packageName = classDeclaration.packageName.asString()
        val className = classDeclaration.simpleName.asString()
        val defaultValues = extractDefaultValues(classDeclaration)
        val typeParams = getTypeParameters(classDeclaration)
        val interfaces = getImplementedInterfaces(classDeclaration)
        return ClassMetaData(packageName, className, defaultValues, typeParams, interfaces)
    }

    fun getImplementedInterfaces(declaration: KSClassDeclaration): Sequence<KSClassDeclaration> {
        return declaration.superTypes
            .mapNotNull { typeRef ->
                // Resolve the type reference to a type, then get its declaration as a KSClassDeclaration
                typeRef.resolve().declaration as? KSClassDeclaration
            }
            .filter { superDecl ->
                // Check if the declaration represents an interface
                superDecl.classKind == com.google.devtools.ksp.symbol.ClassKind.INTERFACE
            }
    }

    fun extractDefaultValues(classDeclaration: KSClassDeclaration): Map<String, String?> {
        val defaultValues = mutableMapOf<String, String?>()

        // Read the original source file
        val fileText = classDeclaration.containingFile?.let { file ->
            file.filePath.let { path -> File(path).readText() }
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

    @Serializable
    data class TestData(val foo: String, val bar: String) : KResponse

    @Kobold()
    class TestClass() {

    }
    fun getSerializableClassDeclaration(classDeclaration: KSClassDeclaration): KSClassDeclaration? {
        // Try to get the Kobold annotation and its 'serializableResponse' argument.
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

        // If no 'serializableResponse' was found, check if the class itself is annotated with @Serializable.
        if (classDeclaration.annotations.any { it.shortName.asString() == "Serializable" }) {
            return classDeclaration
        }

        return null
    }

    fun getTypeParameters(classDeclaration: KSClassDeclaration): List<ClassMetaData> {
        val kPostType = classDeclaration.superTypes
            .map { it.resolve() }


        val result = mutableListOf<ClassMetaData>()

        // Resolve the first type argument (LoginResponse).
        kPostType.forEach {

            val declaration = it.declaration as? KSClassDeclaration
            declaration?.let {
                val md = getClassMetaData(declaration)
                result.add(md)
            }

        }
         return result


    }

}

fun KSClassDeclaration.getImport() : Pair<String, String> {
    val qualifiedName = this.qualifiedName?.asString()
        ?: throw IllegalArgumentException("Class declaration must have a qualified name")

    val packageName = qualifiedName.substringBeforeLast(".")
    val className = qualifiedName.substringAfterLast(".")
    return Pair(packageName, className)

}

// Function to check if the class or any of its superclasses implement the given interface
fun KSClassDeclaration.implementsInterface(interfaceClass: KClass<*>): Boolean {

    val interfaceFqn = interfaceClass.qualifiedName ?: return false

    fun KSClassDeclaration.checkSuperTypes(): Boolean {
        // Check if any of the super types match the interface
        return this.superTypes.any { superTypeRef: KSTypeReference ->
            val resolvedType: KSType = superTypeRef.resolve()
            val declaration = resolvedType.declaration as? KSClassDeclaration

            // Check if the resolved type's qualified name matches the interface
            if (resolvedType.declaration.qualifiedName?.asString() == interfaceFqn) {
                return true
            }

            // Recursively check the super types of the current super class
            declaration?.checkSuperTypes() ?: false
        }
    }

    val result =  checkSuperTypes()

    return result
}

