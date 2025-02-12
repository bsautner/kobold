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

package io.github.bsautner.ksp.classtools

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import io.github.bsautner.kobold.Kobold
import io.github.bsautner.ksp.routing.Routes
import io.github.bsautner.ksp.util.ImportManager
import java.io.File
import kotlin.reflect.KClass

class ClassHelper {

    fun getClassMetaData(classDeclaration: KSClassDeclaration): ClassMetaData {

        val packageName = classDeclaration.packageName.asString()
        val className = classDeclaration.qualifiedName?.asString() ?: classDeclaration.simpleName.asString()
        val simpleName = classDeclaration.simpleName.asString()
        val defaultValues = extractDefaultValues(classDeclaration)
        val typeParams = getTypeParameters(classDeclaration)
        val interfaces = getImplementedInterfaces(classDeclaration)
        val imports = getImports(classDeclaration)
        return ClassMetaData(classDeclaration, packageName, className, simpleName, defaultValues, typeParams, interfaces, imports)
    }

     private fun getImports(declaration: KSClassDeclaration): Map<String, List<String>> {
                val result = mutableMapOf<String, List<String>>()

                declaration.toImportStatement().let {
                    result[it.first] = listOf(it.second)
                }

                getTypeParameters(declaration).forEach { meta ->
                    meta.declaration.toImportStatement().let {
                       result[it.first] = result.getOrDefault(it.first, emptyList()) + it.second
                    }
                }

                getImplementedInterfaces(declaration).forEach {
                    it.declaration.toImportStatement().let {
                        result[it.first] = result.getOrDefault(it.first, emptyList()) + it.second
                    }
                }

                return result
            }


    fun getImplementedInterfaces(declaration: KSClassDeclaration): List<ClassMetaData> {

        val interfaces = declaration.superTypes.toList()
        val result = mutableListOf<ClassMetaData>()
        interfaces.forEach {
            val declaration = (it.resolve().declaration as KSClassDeclaration)
            if (declaration.classKind == ClassKind.INTERFACE) {
                result.add(getClassMetaData(declaration))
            }
        }

        return result


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
        val interfacesTypeParams = mutableListOf<ClassMetaData>()
        val implementedInterfaces: Sequence<KSTypeReference> = classDeclaration.superTypes
        implementedInterfaces.forEach { typeRef ->
               val resolvedType = typeRef.resolve()
             val typeParams = resolvedType.arguments.mapNotNull { arg ->
                arg.type?.resolve()?.declaration?.let { declaration ->

                    getClassMetaData(declaration as KSClassDeclaration)

                }
            }
            interfacesTypeParams.addAll(typeParams)

        }
        return interfacesTypeParams
    }



}

fun areCodeStringsIdentical(code1: String, code2: String): Boolean {
    fun removeTopComments(code: String): String {
        return code
            .lineSequence()
            .dropWhile { it.trimStart().startsWith("//") || it.trimStart().startsWith("/*") }
            .joinToString("\n")
            .trim()
    }

    return removeTopComments(code1) == removeTopComments(code2)
}

fun KSClassDeclaration.toImportStatement(): Pair<String, String> {
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

    val result = checkSuperTypes()

    return result
}