package io.github.bsautner.ksp.classtools

import com.google.devtools.ksp.symbol.KSClassDeclaration

data class ClassMetaData(
        val declaration: KSClassDeclaration,
        val packageName: String = "",
        val qualifiedName: String = "",
        val simpleName: String = "",
        val defaultValues: Map<String, String?> = emptyMap<String, String>(),
        val typeParameters: List<ClassMetaData> = emptyList<ClassMetaData>(),
        val interfaces: List<ClassMetaData> = emptyList<ClassMetaData>(), 
        val imports: Map<String, List<String>> = emptyMap<String, List<String>>()
    ) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClassMetaData

            return qualifiedName == other.qualifiedName
        }

        override fun hashCode(): Int {
            return qualifiedName.hashCode()
        }
    }
