package io.github.bsautner.kobold

import androidx.compose.runtime.Composable
import io.ktor.resources.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable


class MyUserInterface(render: @Composable () -> Unit) : KCompose(render)

@Serializable
data class LoginRequest2(val userName: String = "foo", val password: String = "bar") : KRequest
@Serializable
data class LoginResponse2(val message: String) : KResponse


@Kobold
@Resource("/test")
data object ComposeExample: KComposable, KPost<LoginRequest2, LoginResponse2> {

    @Contextual
    override val render: KCompose = MyUserInterface {
        //  ComposeExampleComposable()
    }
  override val process: (LoginRequest2) -> LoginResponse2 = {
      LoginResponse2("got it")
  }


}


