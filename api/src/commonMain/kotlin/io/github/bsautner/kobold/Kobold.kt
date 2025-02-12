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


/**
 * Classes implementing this interface will result in Kobol creating Composables.
 * You can create data classes that @see KResponse
 */
open class KCompose(@Transient val render: @Composable () -> Unit) : KResponse {

	@Composable
	operator fun invoke() {
		render()
	}

}

class Dummy<T>

fun <T : U, U> List<U>.func() = Dummy<T>()

fun main() {

	val x: List<Double> = listOf<Double>(1.0, 2.0)

	val y: Dummy<String> = x.func()

	println(y::class.c)
}