package io.github.bsautner.ksp

import kotlinx.serialization.Serializable

@Serializable
sealed interface KResponse

@Serializable
open class KJsonResponse : KResponse

@Serializable
open class KHtmlResponse : KResponse

@Serializable
open class KPostBody
