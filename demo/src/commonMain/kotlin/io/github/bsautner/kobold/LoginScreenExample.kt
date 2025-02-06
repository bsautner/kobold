package io.github.bsautner.kobold

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable


@Kobold
sealed class LoginScreenExample {

	@Serializable
	data class LoginPostData(val userName: String, val password: String) : KRequest

	@Serializable
	data class LoginPostResponse(val message: String) : KResponse

	@Resource("/login")
	class LoginForm() : LoginScreenExample(), KPost<LoginPostData, LoginPostResponse> {

		override val process : (LoginPostData) -> LoginPostResponse = {
			LoginPostResponse("OK!")
		}

		@Resource("/test")
		class TestPostResource() : KPost<LoginPostData, LoginPostResponse> {

			override val process : (LoginPostData) -> LoginPostResponse = {
				LoginPostResponse("OK!")
			}

		}

	}

	@Resource("/welcome")
	class WelcomePage() : LoginScreenExample(), KGet<LoginPostResponse> {

		override val render: () -> LoginPostResponse = {
			LoginPostResponse("Welcome!")
		}


	}

}