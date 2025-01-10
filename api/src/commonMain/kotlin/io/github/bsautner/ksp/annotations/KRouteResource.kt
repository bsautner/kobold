package io.github.bsautner.ksp.annotations

import kotlin.reflect.KClass


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KRouting(val serializableResponse: KClass<*> = Any::class)
