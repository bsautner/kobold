package io.github.bsautner.demo

import io.github.bsautner.kobold.KGet
import io.github.bsautner.kobold.KJsonResponse
import io.github.bsautner.kobold.KResponse
import io.github.bsautner.kobold.annotations.KRouting
import io.ktor.resources.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
data class Test(val firstName: String = "foo", val lastName: String = "bar") : KJsonResponse()

/**
 * This will be detected at compile time and calls to /test will be routed to this lamba
 */

@KRouting()
@Resource("/test")
class BasicJsonGet: KGet<KJsonResponse> {

    @Transient
    override var render: () -> KResponse = { Test("Hello World!!!") }

}