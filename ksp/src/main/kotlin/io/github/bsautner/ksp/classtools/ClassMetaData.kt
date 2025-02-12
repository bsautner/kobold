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
