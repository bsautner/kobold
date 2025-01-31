package io.github.bsautner.demo

import androidx.compose.runtime.Composable
import io.github.bsautner.kobold.KCompose
import io.github.bsautner.kobold.KComposable
import io.github.bsautner.kobold.Kobold
import io.ktor.resources.*
import kotlinx.serialization.Contextual


class MyUserInterface(render: @Composable () -> Unit) : KCompose(render)

@Kobold(TestPost::class)
@Resource("/test")
data object ComposeExample: KComposable {

    @Contextual
    override val render: KCompose = MyUserInterface {
         ComposeExampleComposable()
    }
}


