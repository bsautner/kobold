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
 *
 * @param render lambda to invoke as a composable
 *
 * @property invoke call this from inside a Composable to include the rendered output.
 *
 * @sample
 *
 * @Composable
 * fun App() {
 *     MaterialTheme {
 *         var showContent by remember { mutableStateOf(false) }
 *         Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
 *             ComposeExample.render()
 *         }
 *     }
 * }
 *
 */
open class KCompose(@Transient val render: @Composable () -> Unit) : KResponse {
	@Composable
	operator fun invoke() {
		render()
	}
}
