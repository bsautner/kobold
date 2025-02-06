package io.github.bsautner.kobold.client

import io.github.bsautner.kobold.KRequest
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*


object ApiClient {

    val client = HttpClient {
        install(ContentNegotiation) {
            json()
          //  json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend inline fun <reified T : KRequest> postData(endpoint: String, data: T): HttpResponse {
        return client.post(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(data)
        }
    }
}
