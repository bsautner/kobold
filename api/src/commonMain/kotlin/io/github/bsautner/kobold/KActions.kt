package io.github.bsautner.kobold

import kotlin.reflect.KClass


interface KGet<T: KResponse> {
    var render: () -> T
}


interface KPost<T : KResponse, R : KPostBody> {
    var process: (R) -> T
}

interface KStatic

interface KComposable  {
    val render: KCompose
}
interface KWeb<T> {
    var render: (T) -> Unit
}

inline fun <reified T : KResponse, reified R: KPostBody> KPost<T, R>.getPostBodyClass() : KClass<T> {
    return T::class
}
inline fun <reified T: KResponse, reified R : KPostBody> KPost<T, R>.getPostResponseBodyClass() : KClass<R> {
    return R::class
}

