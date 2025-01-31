package io.github.bsautner.kobold


import io.github.bsautner.demo.TestClass
import io.github.bsautner.demo.TestPost
import io.github.bsautner.demo.TestResponse
import io.github.bsautner.kobold.annotations.autoRoute

import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.resources.Resources
import io.ktor.server.resources.post
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.util.reflect.TypeInfo

fun main() {
    println("http://localhost:8080")
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json()
        }
        install(Resources)
        autoRoute()
    }.start(wait = true)
}





