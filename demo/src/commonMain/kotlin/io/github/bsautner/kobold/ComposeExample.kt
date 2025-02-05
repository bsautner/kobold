package io.github.bsautner.kobold

import androidx.compose.runtime.Composable
import io.ktor.resources.*
import kotlinx.serialization.Contextual


class MyUserInterface(render: @Composable () -> Unit) : KCompose(render)

@Kobold
@Resource("/test")
data object ComposeExample: KComposable {

    @Contextual
    override val render: KCompose = MyUserInterface {
         //ComposeExampleComposable()
    }
}


