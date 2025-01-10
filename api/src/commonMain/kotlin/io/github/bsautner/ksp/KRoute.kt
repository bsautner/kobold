package io.github.bsautner.ksp

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import kotlin.jvm.Transient

@Serializable
sealed interface KResponse

@Serializable
open class KComposeResponse : KResponse

@Serializable
open class KJsonResponse : KResponse

@Serializable
open class KHtmlResponse : KResponse

@Serializable
open class KPostBody

open class KomposeResponse(@Transient val render: @Composable () -> Unit) : KResponse

