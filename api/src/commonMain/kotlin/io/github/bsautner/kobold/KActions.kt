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

