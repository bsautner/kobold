package io.github.bsautner.kobold

import androidx.compose.runtime.Composable
import io.github.bsautner.kobold.KPostBody

import io.ktor.resources.Resource
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

class LoginPage(render: @Composable () -> Unit) : KCompose(render)

@Serializable
data class LoginData(val userName: String = "foo", val password: String = "bar") : KPostBody()

@Serializable
data class LoginResponse(val message: String) : KResponse

@Kobold @Resource("/login")
data object Login: KComposable, KPost<LoginData, LoginResponse>
{
	@Contextual
	override val render: KCompose = LoginPage {

 	}
	override val process: (LoginData) -> LoginResponse = {
		LoginResponse("got it")
	}
}
