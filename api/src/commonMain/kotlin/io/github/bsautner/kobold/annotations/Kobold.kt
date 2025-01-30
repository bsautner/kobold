package io.github.bsautner.kobold.annotations

import kotlin.reflect.KClass


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Kobold(val serializableResponse: KClass<*> = Any::class)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KoboldStatic(val path: String)
