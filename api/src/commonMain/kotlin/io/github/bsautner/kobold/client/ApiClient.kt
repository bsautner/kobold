package io.github.bsautner.kobold.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object ApiClient {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend inline fun <reified T> postData(endpoint: String, data: T): HttpResponse {
        return client.post(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(data)
        }
    }
}
