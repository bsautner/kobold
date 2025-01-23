package io.github.bsautner.demo

import androidx.compose.runtime.Composable
import io.github.bsautner.kobold.KCompose
import io.github.bsautner.kobold.KJsonResponse
import io.github.bsautner.kobold.Kompose
import io.github.bsautner.kobold.annotations.KRouting
import io.ktor.resources.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class PostBodyExample(
                           private val firstName: String = "foo",
                           private val lastName: String = "bar",
                           private val test: String
                        ) : KJsonResponse()

class MyUserInterface(render: @Composable () -> Unit) : KCompose(render)

@KRouting(PostBodyExample::class)
@Resource("/test")
data object ComposeExample: Kompose {

    @Contextual
    override val render: KCompose = MyUserInterface {
        ComposeExampleComposable()
    }
}


