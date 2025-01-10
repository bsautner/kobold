package io.github.bsautner.ksp

import io.ktor.http.*
import kotlin.reflect.KClass


interface KGet<T> {
    var render: () -> KResponse
}

interface KWeb<T> {
    var render: (T) -> Unit
}

interface KPost<T, R> {
    var process: (T) -> R
}



inline fun <reified T, reified R> KPost<T, R>.getPostBodyClass() : KClass<*> {
    return T::class
}
inline fun <reified T, reified R> KPost<T, R>.getPostResponseBodyClass() : KClass<*> {
    return R::class
}


interface AutoPut
interface AutoDelete
