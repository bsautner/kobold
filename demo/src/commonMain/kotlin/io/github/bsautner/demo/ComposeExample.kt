package io.github.bsautner.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.bsautner.ksp.KCompose
import io.github.bsautner.ksp.KJsonResponse
import io.github.bsautner.ksp.Kompose
import io.github.bsautner.ksp.annotations.KRouting
import io.github.bsautner.ksp.introspectSerializableClass
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


