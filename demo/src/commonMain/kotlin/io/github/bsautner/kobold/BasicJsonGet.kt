package io.github.bsautner.kobold

import io.ktor.resources.*
import kotlinx.serialization.Serializable


@Serializable
data class TestResponse(val firstName: String = "foo", val lastName: String = "bar") : KJsonResponse()


@Serializable
data class TestPost(val firstName: String = "foo", val lastName: String = "bar") : KRequest



/**
 * 		staticFiles("/", File("build/dist/wasmJs/productionExecutable")) {
 * 			default("index.html")
 * 		}
 */