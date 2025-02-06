package io.github.bsautner.kobold

import androidx.compose.runtime.Composable
import io.ktor.resources.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable


class MyUserInterface(render: @Composable () -> Unit) : KCompose(render)

@Serializable
data class LoginRequest(val userName: String = "foo", val password: String = "bar") : KPostBody()

@Serializable
data class LoginResponse(val message: String) : KResponse


@Kobold
@Resource("/test")
data object ComposeExample: KComposable, KPost<LoginRequest, LoginResponse> {

    @Contextual
    override val render: KCompose = MyUserInterface {
        //  ComposeExampleComposable()
    }

    override val process: (LoginRequest) -> LoginResponse = {
        LoginResponse("got it")
    }


}


