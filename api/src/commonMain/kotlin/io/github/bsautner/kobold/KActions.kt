package io.github.bsautner.kobold

import kotlin.reflect.KClass


interface KGet<T: KResponse> {
    val render: () -> T
}


interface KPost<T : KPostBody, R : KResponse> {
    val process: (T) -> R
}

interface KStatic

interface KComposable  {
    val render: KCompose
}
interface KWeb<T> {
    val render: (T) -> Unit
}

inline fun <reified T : KPostBody, reified R: KResponse > KPost<T, R>.getPostBodyClass() : KClass<T> {
    return T::class
}
inline fun <reified T: KPostBody, reified R: KResponse> KPost<T, R>.getPostResponseBodyClass() : KClass<R> {
    return R::class
}

