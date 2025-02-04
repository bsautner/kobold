package io.github.bsautner.kobold

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import kotlin.jvm.Transient
import kotlin.reflect.KClass

@Serializable
open class KJsonResponse : KResponse

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Kobold()

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KoboldStatic(val path: String)


interface KResponse
interface KRequest

@Serializable
open class KPostBody : KRequest



@Serializable
open class KHtmlResponse : KResponse

open class KCompose(@Transient val render: @Composable () -> Unit) : KResponse {

	@Composable
	operator fun invoke() {
		render()
	}

}
