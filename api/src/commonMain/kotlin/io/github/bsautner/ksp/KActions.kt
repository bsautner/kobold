package io.github.bsautner.ksp

import androidx.compose.runtime.Composable
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

interface Kompose  {
    val render: @Composable () -> KomposeResponse
}

inline fun <reified T, reified R> KPost<T, R>.getPostBodyClass() : KClass<*> {
    return T::class
}
inline fun <reified T, reified R> KPost<T, R>.getPostResponseBodyClass() : KClass<*> {
    return R::class
}


interface AutoPut
interface AutoDelete
