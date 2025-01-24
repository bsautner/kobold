package io.github.bsautner.kobold

import io.github.bsautner.demo.ComposeExample
import io.github.bsautner.demo.PostBodyExample
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureRouting() {
	routing {
		staticFiles("/", File("build/dist/wasmJs/productionExecutable")) {
			default("index.html")
		}
		get("/test") {
			call.respondText("Hello World!")
		}
		post<PostBodyExample>("/test") {
			println("post received")
			call.respond(HttpStatusCode.OK, "got it")


		}
	}
}
