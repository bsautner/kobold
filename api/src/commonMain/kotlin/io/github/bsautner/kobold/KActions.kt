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



package io.github.bsautner.kobold

import kotlin.reflect.KClass

/**
 * Implement this interface in your data classes to use them as a response body of posts and gets in Ktor
 *
 */
interface KGet<T: KResponse>  {
    val render: () -> T
}

/**
 * Implement this interface in your data classes to use them as a request body of posts and puts in Ktor
 *
 */
interface KPost<T : KRequest, R : KResponse>  {
    val process: (T) -> R
}

interface KStatic

interface KComposable  {
    val render: KCompose
}
interface KWeb<T> {
    val render: (T) -> Unit
}

inline fun <reified T : KRequest, reified R: KResponse > KPost<T, R>.getPostBodyClass() : KClass<T> {
    return T::class
}
inline fun <reified T: KRequest, reified R: KResponse> KPost<T, R>.getPostResponseBodyClass() : KClass<R> {
    return R::class
}

