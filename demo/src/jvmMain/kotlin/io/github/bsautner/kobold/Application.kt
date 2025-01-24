package io.github.bsautner.kobold

import io.github.bsautner.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    println("http://localhost:8080")
    embeddedServer(Netty, port = 8080) {
        configureRouting()
        configureSerialization()
    }.start(wait = true)
}

