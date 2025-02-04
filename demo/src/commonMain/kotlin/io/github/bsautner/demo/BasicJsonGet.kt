package io.github.bsautner.demo

import io.github.bsautner.kobold.KGet
import io.github.bsautner.kobold.KJsonResponse
import io.github.bsautner.kobold.KPost
import io.github.bsautner.kobold.KPostBody
import io.github.bsautner.kobold.KStatic
import io.github.bsautner.kobold.Kobold
import io.github.bsautner.kobold.KoboldStatic
import io.ktor.resources.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
data class TestResponse(val firstName: String = "foo", val lastName: String = "bar") : KJsonResponse()


@Serializable
data class TestPost(val firstName: String = "foo", val lastName: String = "bar") : KPostBody()


@KoboldStatic("build/dist/wasmJs/productionExecutable")
@Resource("/")
class StaticExample()  : KStatic

/**
 * 		staticFiles("/", File("build/dist/wasmJs/productionExecutable")) {
 * 			default("index.html")
 * 		}
 */