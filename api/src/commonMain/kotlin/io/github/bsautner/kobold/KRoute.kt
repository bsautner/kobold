package io.github.bsautner.kobold

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import kotlin.jvm.Transient

@Serializable
sealed interface KResponse


@Serializable
open class KPostBody : KRequest

@Serializable
sealed interface KRequest

@Serializable
open class KJsonResponse : KResponse


@Serializable
open class KHtmlResponse : KResponse


open class KCompose(@Transient val render: @Composable () -> Unit) : KResponse {

    @Composable
    operator fun invoke() {
        render()
    }

}

