package io.github.bsautner.kobold

import io.github.bsautner.kobold.getPostBodyClass
import io.github.bsautner.kobold.getPostResponseBodyClass
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.staticFiles
import io.ktor.server.http.content.staticResources
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.receiveParameters
import io.ktor.server.resources.`get`
import io.ktor.server.resources.delete
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.routing.routing
import io.ktor.util.reflect.TypeInfo
import java.io.File
import kotlin.reflect.safeCast
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.html
import testClasses.Login

public fun Application.naAutoRouter() {
  routing {
    post<KComposable> {
      val response : Any = it.process(call.receive())
    }
  }
}
