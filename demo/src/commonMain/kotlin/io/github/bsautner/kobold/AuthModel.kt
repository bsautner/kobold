package io.github.bsautner.kobold

import androidx.compose.runtime.Composable

import io.ktor.resources.Resource
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

class MyLoginPage(render: @Composable () -> Unit) : KCompose(render)

@Serializable
data class MyLoginData(val userName: String = "foo", val password: String = "bar") : KRequest

@Serializable
data class MyLoginResponse(val message: String) : KResponse

@Kobold @Resource("/login")
data object MyBigLoginObject: KComposable, KPost<MyLoginData, MyLoginResponse>
{
	@Contextual
	override val render: KCompose = MyLoginPage {

 	}
	override val process: (MyLoginData) -> MyLoginResponse = {
		MyLoginResponse("got it")
	}
}
